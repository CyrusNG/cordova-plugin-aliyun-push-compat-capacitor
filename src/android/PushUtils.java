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
    static void initPushService(final Application application) throws PackageManager.NameNotFoundException {
        PushServiceFactory.init(application);
        final CloudPushService pushService = PushServiceFactory.getCloudPushService();
        final ApplicationInfo appInfo = application.getPackageManager().getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA);
        final boolean enableDebug = appInfo.metaData.getBoolean("ALIYUN_PUSH_DEBUG", false);
        if (enableDebug) {
            pushService.setLogLevel(CloudPushService.LOG_DEBUG);
        }
        pushService.register(application, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                String deviceId = pushService.getDeviceId();
                Log.d(TAG, "deviceId: " + deviceId);
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d(TAG, "init cloudChannel failed -- errorCode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });

        createDefaultChannel(application);

        // 注册小米辅助通道
        String miPushAppId  = appInfo.metaData.getString("MI_PUSH_APP_ID", "").trim();
        String miPushAppKey  = appInfo.metaData.getString("MI_PUSH_APP_KEY", "").trim();
        Log.i(TAG, String.format("MiPush appId:%1$s, appKey:%2$s", miPushAppId, miPushAppKey));
        MiPushRegister.register(application, miPushAppId, miPushAppKey);

        // 注册华为辅助通道
        HuaWeiRegister.register(application);

        // 注册荣耀辅助通道
        HonorRegister.register(application);

        // 注册华为辅助通道
        VivoRegister.register(application);

        // 注册oppo辅助通道
        String oppoPushAppKey  = appInfo.metaData.getString("OPPO_PUSH_APP_KEY", "").trim();
        String oppoPushAppSecret  = appInfo.metaData.getString("OPPO_PUSH_APP_SECRET", "").trim();
        Log.i(TAG, String.format("OPPOPush appId:%1$s, appKey:%2$s", oppoPushAppKey, oppoPushAppSecret));
        OppoRegister.register(application, oppoPushAppKey, oppoPushAppSecret);

        // 注册魅族辅助通道
        String mzPushAppId  = appInfo.metaData.getString("MZ_PUSH_APP_ID", "").trim();
        String mzPushAppKey  = appInfo.metaData.getString("MZ_PUSH_APP_KEY", "").trim();
        Log.i(TAG, String.format("MZPush appId:%1$s, appKey:%2$s", mzPushAppId, mzPushAppKey));
        MeizuRegister.register(application, mzPushAppId, mzPushAppKey);

        // 注册GCM辅助通道
        // GcmRegister.register(this, sendId, applicationId);
    }

    private static void createDefaultChannel(Application application) {
        // 注册NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 通知渠道的id
            String channelId;
            ApplicationInfo appInfo;
            try {
                appInfo = application.getPackageManager().getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA);
                channelId = appInfo.metaData.getString("CHANNEL_ID", "").trim();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "CHANNEL_ID NOT FOUND!");
                return;
            }

            NotificationManager mNotificationManager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);

            // 用户可以看到的通知渠道的名字.
            CharSequence name = "通知";
            // 用户可以看到的通知渠道的描述
            String description = "通知描述";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
            // 配置通知渠道的属性
            mChannel.setDescription(description);
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            // 设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            // 最后在 notificationManager 中创建该通知渠道
            mNotificationManager.createNotificationChannel(mChannel);
            // 设置8.0系统的通知小图标,必须要纯色的图
            // PushServiceFactory.getCloudPushService().setNotificationSmallIcon(R.drawable.notify);
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
