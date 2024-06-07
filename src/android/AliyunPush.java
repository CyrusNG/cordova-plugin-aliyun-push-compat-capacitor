package com.alipush;

import static com.alipush.PushUtils.initPushService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

  @Override
  protected void pluginInitialize() {
    super.pluginInitialize();
    if (!isNotificationEnabled(cordova.getContext())) {
      showRequestNotifyDialog(cordova.getActivity(), null);
    }
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
        initPushService(cordova.getActivity().getApplication());
      } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
      }
      callbackContext.success();
      ret = true;
    } else if ("onMessage".equalsIgnoreCase(action)) {
      if (pushCallbackContext == null) {
        pushCallbackContext = callbackContext;
        new PushUtils(cordova.getActivity()).getNotice();
      }
      ret = true;
    } else if ("isEnableNotification".equalsIgnoreCase(action)) {
      boolean enable = isNotificationEnabled(cordova.getContext());
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("enable", enable);
      callbackContext.success(jsonObject);
      ret = true;
    } else if ("requireNotifyPermission".equalsIgnoreCase(action)) {
      showRequestNotifyDialog(cordova.getActivity(), callbackContext);
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
                public void onSuccess(String s) {
                  callbackContext.success(s);
                }

                @Override
                public void onFailed(String s, String s1) {
                  resError(callbackContext, s, s1);
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
                public void onSuccess(String s) {
                  callbackContext.success(s);
                }

                @Override
                public void onFailed(String s, String s1) {
                  resError(callbackContext, s, s1);
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
                  public void onSuccess(String s) {
                    callbackContext.success(s);
                  }

                  @Override
                  public void onFailed(String s, String s1) {
                    resError(callbackContext, s, s1);
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
                  public void onFailed(String s, String s1) {
                    resError(callbackContext, s, s1);
                  }

                  @Override
                  public void onSuccess(String s) {
                    LOG.d(TAG, "onSuccess:" + s);
                    callbackContext.success(s);
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
                public void onFailed(String s, String s1) {
                  resError(callbackContext, s, s1);
                }

                @Override
                public void onSuccess(String s) {
                  LOG.d(TAG, "onSuccess:" + s);
                  callbackContext.success(s);
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
                public void onFailed(String s, String s1) {
                  resError(callbackContext, s, s1);
                }

                @Override
                public void onSuccess(String s) {
                  LOG.d(TAG, "onSuccess:" + s);
                  callbackContext.success(s);
                }
              }
            );
          }
        );
      sendNoResultPluginResult(callbackContext);
      ret = true;
    } else if ("removeAlias".equalsIgnoreCase(action)) {
      final String alias = args.getString(0);
      cordova
        .getThreadPool()
        .execute(
          () -> {
            LOG.d(TAG, "PushManager#removeAlias");
            pushService.removeAlias(
              alias,
              new CommonCallback() {
                @Override
                public void onFailed(String s, String s1) {
                  resError(callbackContext, s, s1);
                }

                @Override
                public void onSuccess(String s) {
                  LOG.d(TAG, "onSuccess:" + s);
                  callbackContext.success(s);
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
            LOG.d(TAG, "PushManager#removeAlias");
            pushService.listAliases(
              new CommonCallback() {
                @Override
                public void onFailed(String s, String s1) {
                  resError(callbackContext, s, s1);
                }

                @Override
                public void onSuccess(String s) {
                  LOG.d(TAG, "onSuccess:" + s);
                  callbackContext.success(s);
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
    CallbackContext callbackContext,
    String reason,
    String res
  ) {
    LOG.d(TAG, "onFailed reason:" + reason + "res:" + res);
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("message", res);
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

  /**
   * 判断允许通知，是否已经授权
   * 返回值为true时，通知栏打开，false未打开。
   *
   * @param context 上下文
   */
  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  private static boolean isNotificationEnabled(Context context) {
    return NotificationManagerCompat.from(context).areNotificationsEnabled();
  }

  /**
   * 跳转到app的设置界面--开启通知
   *
   * @param context Context
   */
  private static void goToNotificationSetting(Activity context) {
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

  private static void showRequestNotifyDialog(
    final Activity context,
    final CallbackContext callbackContext
  ) {
    final Resources resources = context.getResources();
    final int title = resources.getIdentifier(
      "aliyun_dialog_title",
      "string",
      context.getPackageName()
    );
    final int msg = resources.getIdentifier(
      "aliyun_dialog_message",
      "string",
      context.getPackageName()
    );
    final int negativeText = resources.getIdentifier(
      "aliyun_dialog_negative_text",
      "string",
      context.getPackageName()
    );
    final int positiveText = resources.getIdentifier(
      "aliyun_dialog_positive_text",
      "string",
      context.getPackageName()
    );
    new AlertDialog.Builder(context)
      .setTitle(title)
      .setMessage(msg)
      .setNegativeButton(
        negativeText,
        (dialog, which) -> {
          if (callbackContext != null) {
            callbackContext.error("Permission dined.");
          }
        }
      )
      .setPositiveButton(
        positiveText,
        (dialog, which) -> {
          goToNotificationSetting(context);
          if (callbackContext != null) {
            callbackContext.success();
          }
        }
      )
      .create()
      .show();
  }

}
