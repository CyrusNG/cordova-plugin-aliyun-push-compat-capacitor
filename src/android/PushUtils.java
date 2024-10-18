package com.alipush;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.alibaba.sdk.android.push.huawei.HuaWeiRegister;
import com.alibaba.sdk.android.push.HonorRegister;
import com.alibaba.sdk.android.push.register.MiPushRegister;
import com.alibaba.sdk.android.push.register.VivoRegister;
import com.alibaba.sdk.android.push.register.OppoRegister;
import com.alibaba.sdk.android.push.register.MeizuRegister;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class PushUtils {
    public static final String TAG = PushUtils.class.getSimpleName();
    private SharedPreferences preference;

    public PushUtils(Context context) {
        this.preference = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * 初始化云推送通道
     *
     * @param application Application
     */
    static void initPushService(final Application application, List<String> vendorList, CommonCallback commonCallback) throws PackageManager.NameNotFoundException {
        PushServiceFactory.init(application);
        final CloudPushService pushService = PushServiceFactory.getCloudPushService();
        final ApplicationInfo appInfo = application.getPackageManager().getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA);
        final boolean enableDebug = appInfo.metaData.getBoolean("ALIYUN_PUSH_DEBUG", false);
        if (enableDebug) {
            pushService.setLogLevel(CloudPushService.LOG_DEBUG);
        }
        pushService.register(application, commonCallback);

        createDefaultChannel(application);

        // 注册xiaomi辅助通道
        if(vendorList.contains("xiaomi")) {
          Log.d(TAG, "Register xiaomi vendor channel.");
          String miPushAppId = appInfo.metaData.getString("MI_PUSH_APP_ID", "").trim();
          String miPushAppKey = appInfo.metaData.getString("MI_PUSH_APP_KEY", "").trim();
          Log.i(TAG, String.format("MiPush appId:%1$s, appKey:%2$s", miPushAppId, miPushAppKey));
          MiPushRegister.register(application, miPushAppId, miPushAppKey);
        }

        // 注册huawei辅助通道
        if(vendorList.contains("huawei")) {
          Log.d(TAG, "Register huawei vendor channel.");
          HuaWeiRegister.register(application);
        }

        // 注册honor辅助通道
        if(vendorList.contains("honor")) {
          Log.d(TAG, "Register honor vendor channel.");
          HonorRegister.register(application);
        }

        // 注册vivo辅助通道
        if(vendorList.contains("vivo")) {
          Log.d(TAG, "Register vivo vendor channel.");
          VivoRegister.register(application);
        }

        // 注册oppo辅助通道
        if(vendorList.contains("oppo")) {
          Log.d(TAG, "Register oppo vendor channel.");
          String oppoPushAppKey = appInfo.metaData.getString("OPPO_PUSH_APP_KEY", "").trim();
          String oppoPushAppSecret = appInfo.metaData.getString("OPPO_PUSH_APP_SECRET", "").trim();
          Log.i(TAG, String.format("OPPOPush appId:%1$s, appKey:%2$s", oppoPushAppKey, oppoPushAppSecret));
          OppoRegister.register(application, oppoPushAppKey, oppoPushAppSecret);
        }

        // 注册meizu辅助通道
        if(vendorList.contains("meizu")) {
          Log.d(TAG, "Register meizu vendor channel.");
          String mzPushAppId = appInfo.metaData.getString("MZ_PUSH_APP_ID", "").trim();
          String mzPushAppKey = appInfo.metaData.getString("MZ_PUSH_APP_KEY", "").trim();
          Log.i(TAG, String.format("MZPush appId:%1$s, appKey:%2$s", mzPushAppId, mzPushAppKey));
          MeizuRegister.register(application, mzPushAppId, mzPushAppKey);
        }

        // 注册GCM辅助通道
        //if(vendorList.contains("gcm")) {
        //. Log.d(TAG, "Register GCM vendor channel.");
        //  GcmRegister.register(this, sendId, applicationId);
        //}
    }

       private static void createDefaultChannel(Application application) {
        // 注册NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //通知渠道的ids
            List<String> channelList;
            ApplicationInfo appInfo;
            try {
                appInfo = application.getPackageManager().getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA);
                String channelStr = appInfo.metaData.getString("ALIYUN_PUSH_CHANNEL_ID", "1").trim();;
              channelList = Arrays.asList(channelStr.split(","));

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "ALIYUN_PUSH_CHANNEL_ID NOT FOUND!");
                return;
            }
            for (String channelId : channelList) {
              //创建渠道
              NotificationManager mNotificationManager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
              NotificationChannel mChannel = new NotificationChannel(channelId.toString(), "通知", NotificationManager.IMPORTANCE_HIGH);
              //配置通知渠道的属性
              mChannel.setDescription("通知描述");
              //设置通知出现时的闪灯（如果 android 设备支持的话）
              mChannel.enableLights(true);
              mChannel.setLightColor(Color.RED);
              //设置通知出现时的震动（如果 android 设备支持的话）
              mChannel.enableVibration(true);
              mChannel.setVibrationPattern(new long[]{500, 200, 200});
              //创建该通知渠道
              mNotificationManager.createNotificationChannel(mChannel);
              //设置8.0系统的分组和通知小图标，必须要纯色的图
              String notiIcon = appInfo.metaData.getString("NOTIFICATION_ICON", "ic_notification_icon").trim();
              int imageRes = application.getResources().getIdentifier("@drawable/" + notiIcon, null, application.getPackageName());
              PushServiceFactory.getCloudPushService().setNotificationSmallIcon(imageRes);
              PushServiceFactory.getCloudPushService().setNotificationShowInGroup(true);
            }
        }
    }

    void setNoticeJsonData(String jsonObject) {
        //response为后台返回的json数据
        preference.edit().putString("NoticeJsonData", jsonObject).apply(); //存入json串
    }


    public String getNotice() {
        String jsonData = preference.getString("NoticeJsonData", "");
        //每次取到json数据后，将其清空
        preference.edit().putString("NoticeJsonData", "").apply();
        try {
            JSONObject data = new JSONObject(jsonData);
            AliyunPush.pushData(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonData;
    }
}
