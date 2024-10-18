package com.alipush;

import static android.content.Context.MODE_PRIVATE;
import static com.alipush.PushUtils.initPushService;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * This class echoes a string called from JavaScript.
 */
public class AliyunPush extends CordovaPlugin {

  private SharedPreferences preference;

  public AliyunPush() {
    cls = this.getClass();
  }

  public static Class<?> cls;

  private static final String TAG = AliyunPush.class.getSimpleName();
  /**
   * JS回调接口对象
   */
  static CallbackContext pushCallbackContext = null;
  private final CloudPushService pushService = PushServiceFactory.getCloudPushService();

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    preference = PreferenceManager.getDefaultSharedPreferences(cordova.getContext());
    super.initialize(cordova, webView);
  }

  /**
   * 插件主入口
   */
  @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
  @Override
  public boolean execute(
    final String action,
    final JSONArray args,
    final CallbackContext callbackContext
  )
    throws JSONException {
    LOG.d(TAG, "AliyunPush#execute");

    boolean ret = false;
    if ("boot".equalsIgnoreCase(action)) {
      try {
        final String vendorChain = args.isNull(0)? "" : args.getString(0);
        final List<String> vendorList = Arrays.asList(vendorChain.split(","));
        initPushService(cordova.getActivity().getApplication(), vendorList, new CommonCallback() {
          @Override
          public void onSuccess(String result) {
            String deviceId = pushService.getDeviceId();
            Log.d(TAG, "deviceId: " + deviceId);
            callbackContext.success(deviceId);
          }
          @Override
          public void onFailed(String reason, String message) {
            // special success case:
            // error -> PUSH_20110 # 已经调用注册，重复调用无效
            if (reason.equalsIgnoreCase("PUSH_20110")) {
              String deviceId = pushService.getDeviceId();
              Log.d(TAG, "deviceId: " + deviceId);
              callbackContext.success(deviceId);
            } else {
              resError("boot", callbackContext, reason, message);
            }
          }
        });
      } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
      }
      ret = true;
    } else if ("onMessage".equalsIgnoreCase(action)) {
      if (pushCallbackContext == null) {
        pushCallbackContext = callbackContext;
        new PushUtils(cordova.getActivity()).getNotice();
      }
      ret = true;
    } else if ("checkPermission".equalsIgnoreCase(action)) {
      try {
        final Boolean force = args.isNull(0)? false : args.getBoolean(0);
        checkPermission(callbackContext, PERMISSION_NAME, force);
      } catch (JSONException e) {
        //Believe exception only occurs when adding duplicate keys, so just ignore it
        e.printStackTrace();
      }
      ret = true;
    } else if ("openAppSettings".equalsIgnoreCase(action)) {
      openAppSettings();
      ret = true;
    } else if ("requestPermission".equalsIgnoreCase(action)) {
      requestPermission(callbackContext, PERMISSION_NAME);
      ret = true;
    } else if ("getRegisterId".equalsIgnoreCase(action)) {
      callbackContext.success(pushService.getDeviceId());
      sendNoResultPluginResult(callbackContext);
      ret = true;
    } else if ("bindAccount".equalsIgnoreCase(action)) {
      final String account = args.getString(0);
      cordova
        .getThreadPool()
        .execute(
          () -> {
            LOG.d(TAG, "PushManager#bindAccount");
            pushService.bindAccount(
              account,
              new CommonCallback() {
                @Override
                public void onSuccess(String result) {
                  callbackContext.success(result);
                }

                @Override
                public void onFailed(String reason, String message) {
                  resError("bindAccount", callbackContext, reason, message);
                }
              }
            );
          }
        );
      sendNoResultPluginResult(callbackContext);
      ret = true;
    } else if ("unbindAccount".equalsIgnoreCase(action)) {
      cordova
        .getThreadPool()
        .execute(
          () -> {
            LOG.d(TAG, "PushManager#unbindAccount");
            pushService.unbindAccount(
              new CommonCallback() {
                @Override
                public void onSuccess(String result) {
                  callbackContext.success(result);
                }

                @Override
                public void onFailed(String reason, String message) {
                  resError("unbindAccount", callbackContext, reason, message);
                }
              }
            );
          }
        );
      sendNoResultPluginResult(callbackContext);
      ret = true;
    } else if ("bindTags".equalsIgnoreCase(action)) {
      final int target = args.getInt(0);
      final String[] tags = toStringArray(args.getJSONArray(1));
      final String alias = args.length() > 2 ? args.getString(2) : null;

      cordova
        .getThreadPool()
        .execute(
          () -> {
            LOG.d(TAG, "PushManager#bindTags");

            if (tags != null && tags.length > 0) {
              pushService.bindTag(
                target,
                tags,
                alias,
                new CommonCallback() {
                  @Override
                  public void onSuccess(String result) {
                    callbackContext.success(result);
                  }

                  @Override
                  public void onFailed(String reason, String message) {
                    resError("bindTags", callbackContext, reason, message);
                  }
                }
              );
            }
          }
        );
      sendNoResultPluginResult(callbackContext);
      ret = true;
    } else if ("unbindTags".equalsIgnoreCase(action)) {
      final int target = args.getInt(0);
      final String[] tags = toStringArray(args.getJSONArray(1));
      final String alias = args.length() > 2 ? args.getString(2) : null;
      cordova
        .getThreadPool()
        .execute(
          () -> {
            LOG.d(TAG, "PushManager#unbindTags");

            if (tags != null && tags.length > 0) {
              pushService.unbindTag(
                target,
                tags,
                alias,
                new CommonCallback() {
                  @Override
                  public void onFailed(String reason, String message) {
                    resError("unbindTags", callbackContext, reason, message);
                  }

                  @Override
                  public void onSuccess(String result) {
                    LOG.d(TAG, "onSuccess:" + result);
                    callbackContext.success(result);
                  }
                }
              );
            }
          }
        );
      sendNoResultPluginResult(callbackContext);
      ret = true;
    } else if ("listTags".equalsIgnoreCase(action)) {
      cordova
        .getThreadPool()
        .execute(
          () -> {
            LOG.d(TAG, "PushManager#listTags");
            pushService.listTags(
              pushService.DEVICE_TARGET,
              new CommonCallback() {
                @Override
                public void onFailed(String reason, String message) {
                  resError("listTags", callbackContext, reason, message);
                }

                @Override
                public void onSuccess(String result) {
                  LOG.d(TAG, "onSuccess:" + result);
                  callbackContext.success(result);
                }
              }
            );
          }
        );
      sendNoResultPluginResult(callbackContext);
      ret = true;
    } else if ("addAlias".equalsIgnoreCase(action)) {
      final String alias = args.getString(0);
      cordova
        .getThreadPool()
        .execute(
          () -> {
            LOG.d(TAG, "PushManager#addAlias");
            pushService.addAlias(
              alias,
              new CommonCallback() {
                @Override
                public void onFailed(String reason, String message) {
                  resError("addAlias", callbackContext, reason, message);
                }

                @Override
                public void onSuccess(String result) {
                  LOG.d(TAG, "onSuccess:" + result);
                  callbackContext.success(result);
                }
              }
            );
          }
        );
      sendNoResultPluginResult(callbackContext);
      ret = true;
    } else if ("removeAlias".equalsIgnoreCase(action)) {
      final String alias = args.isNull(0)? null : args.getString(0);
      cordova
        .getThreadPool()
        .execute(
          () -> {
            LOG.d(TAG, "PushManager#removeAlias");
            pushService.removeAlias(
              alias,
              new CommonCallback() {
                @Override
                public void onFailed(String reason, String message) {
                  resError("removeAlias", callbackContext, reason, message);
                }

                @Override
                public void onSuccess(String result) {
                  LOG.d(TAG, "onSuccess:" + result);
                  callbackContext.success(result);
                }
              }
            );
          }
        );
      sendNoResultPluginResult(callbackContext);
      ret = true;
    } else if ("listAliases".equalsIgnoreCase(action)) {
      cordova
        .getThreadPool()
        .execute(
          () -> {
            LOG.d(TAG, "PushManager#listAliases");
            pushService.listAliases(
              new CommonCallback() {
                @Override
                public void onFailed(String reason, String message) {
                  resError("listAliases", callbackContext, reason, message);
                }

                @Override
                public void onSuccess(String result) {
                  LOG.d(TAG, "onSuccess:" + result);
                  callbackContext.success(result);
                }
              }
            );
          }
        );
      sendNoResultPluginResult(callbackContext);
      ret = true;
    }

    return ret;
  }

  private void resError(
    String label,
    CallbackContext callbackContext,
    String reason,
    String message
  ) {
    LOG.d(TAG, label + " onFailed reason:" + reason + "message:" + message);
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("message", message);
      jsonObject.put("reason", reason);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    callbackContext.error(jsonObject);
  }

  private void sendNoResultPluginResult(CallbackContext callbackContext) {
    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
    result.setKeepCallback(true);
    callbackContext.sendPluginResult(result);
  }

  /**
   * 接收推送内容并返回给前端JS
   *
   * @param data JSON对象
   */
  static void pushData(final JSONObject data) {
    Log.i(TAG, data.toString());
    if (pushCallbackContext == null) {
      return;
    }
    PluginResult result = new PluginResult(PluginResult.Status.OK, data);
    result.setKeepCallback(true);
    pushCallbackContext.sendPluginResult(result);
  }

  private static String[] toStringArray(JSONArray array) {
    if (array == null) return null;
    String[] arr = new String[array.length()];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = array.optString(i);
    }
    return arr;
  }


  private static final String TAG_PERMISSION = "permission";
  private static final String GRANTED = "granted";
  private static final String DENIED = "denied";
  private static final String ASKED = "asked";
  private static final String NEVER_ASKED = "neverAsked";
  private static final String PERMISSION_NAME = Manifest.permission.POST_NOTIFICATIONS;
  private static final int REQUEST_CODE_ENABLE_PERMISSION = 0;
  private CallbackContext permissionsCallback;

  private void openAppSettings() {
    Activity context = cordova.getActivity();
    Intent intent = new Intent();
    if (Build.VERSION.SDK_INT >= 26) {
      // android 8.0引导
      intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
      intent.putExtra(
        "android.provider.extra.APP_PACKAGE",
        context.getPackageName()
      );
    } else if (Build.VERSION.SDK_INT >= 21) {
      // android 5.0-7.0
      intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
      intent.putExtra("app_package", context.getPackageName());
      intent.putExtra("app_uid", context.getApplicationInfo().uid);
    } else {
      // 其他
      intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
      intent.setData(Uri.fromParts("package", context.getPackageName(), null));
    }
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  private void checkPermission(CallbackContext callbackContext, String permission, Boolean force) throws JSONException {
    JSONObject savedReturnObject = new JSONObject();

    // check if asked before
    boolean neverAsked = isPermissionFirstTimeAsking(PERMISSION_NAME);
    if (neverAsked) {
      savedReturnObject.put(NEVER_ASKED, true);
    } else {
      savedReturnObject.put(ASKED, true);
    }

    if (cordova.hasPermission(permission)) {
      // permission GRANTED
      savedReturnObject.put(GRANTED, true);
    } else {
      // permission NOT YET GRANTED
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // from version Android M on,
        // on runtime,
        // each permission can be temporarily denied,
        // or be denied forever
        if (neverAsked || cordova.getActivity().shouldShowRequestPermissionRationale(PERMISSION_NAME)) {
          // permission never asked before
          // OR
          // permission DENIED, BUT not for always
          // So
          // can be asked (again)
          if (force) {
            // request permission
            // so a callback as onRequestPermissionResult()
            requestPermission(callbackContext, permission);
            return;
          }
        } else {
          // permission DENIED
          // user ALSO checked "NEVER ASK AGAIN"
          savedReturnObject.put(DENIED, true);
        }
      } else {
        // below android M
        // no runtime permissions exist
        // so always
        // permission GRANTED
        savedReturnObject.put(GRANTED, true);
      }
    }
    callbackContext.success(savedReturnObject);
  }

  private void requestPermission(CallbackContext callbackContext, String permission) {
    permissionsCallback = callbackContext;
    cordova.requestPermissions(this, REQUEST_CODE_ENABLE_PERMISSION, new String[]{permission});
  }

  @Override
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
    JSONObject savedReturnObject = new JSONObject();

    // the user was apparently requested this permission
    // update the preferences to reflect this
    setPermissionFirstTimeAsking(PERMISSION_NAME, false);

    // indicate that the user has been asked to accept this permission
    savedReturnObject.put(ASKED, true);

    // check permission granted or NOT
    if (cordova.hasPermission(permissions[0])) {
      // permission GRANTED
      Log.d(TAG_PERMISSION, "Asked. Granted");
      savedReturnObject.put(GRANTED, true);
    } else {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (cordova.getActivity().shouldShowRequestPermissionRationale(PERMISSION_NAME)) {
          // permission DENIED
          // BUT not for always
          Log.d(TAG_PERMISSION, "Asked. Denied For Now");
        } else {
          // permission DENIED
          // user ALSO checked "NEVER ASK AGAIN"
          Log.d(TAG_PERMISSION, "Asked. Denied");
          savedReturnObject.put(DENIED, true);
        }
      } else {
        // below android M
        // no runtime permissions exist
        // so always
        // permission GRANTED
        Log.d(TAG_PERMISSION, "Asked. Granted");
        savedReturnObject.put(GRANTED, true);
      }
    }
    // resolve saved call
    permissionsCallback.success(savedReturnObject);
  }

  private static final String PREFS_PERMISSION_FIRST_TIME_ASKING = "PREFS_PERMISSION_FIRST_TIME_ASKING";

  private void setPermissionFirstTimeAsking(String permission, boolean isFirstTime) {
    SharedPreferences sharedPreference = cordova.getActivity().getSharedPreferences(PREFS_PERMISSION_FIRST_TIME_ASKING, MODE_PRIVATE);
    sharedPreference.edit().putBoolean(permission, isFirstTime).apply();
  }

  private boolean isPermissionFirstTimeAsking(String permission) {
    return cordova.getActivity().getSharedPreferences(PREFS_PERMISSION_FIRST_TIME_ASKING, MODE_PRIVATE).getBoolean(permission, true);
  }

}
