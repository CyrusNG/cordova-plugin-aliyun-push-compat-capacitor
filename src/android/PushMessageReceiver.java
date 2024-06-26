package com.alipush;

import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.push.MessageReceiver;
import com.alibaba.sdk.android.push.notification.CPushMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class PushMessageReceiver extends MessageReceiver {
    /**
     * LOG TAG
     */
    private static final String LOG_TAG = PushMessageReceiver.class.getSimpleName();
    /**
     * 回调类型
     */
    private static final String ONMESSAGE = "message";
    private static final String ONNOTIFICATION = "notification";
    private static final String ONNOTIFICATIONOPENED = "notificationOpened";
    private static final String ONNOTIFICATIONRECEIVED = "notificationReceived";
    private static final String ONNOTIFICATIONREMOVED = "notificationRemoved";
    private static final String ONNOIFICATIONCLICKEDWITHNOACTION = "notificationClickedWithNoAction";
    private static final String ONNOTIFICATIONRECEIVEDINAPP = "notificationReceivedInApp";


    @Override
    public void onNotification(Context context, String title, String summary, Map<String, String> extraMap) {
        Log.i(LOG_TAG, "收到通知 Receive notification, title: " + title + ", summary: " + summary + ", extraMap: " + extraMap);
        sendPushData(ONNOTIFICATION, title, summary, extraMap);
    }

    @Override
    public void onMessage(Context context, CPushMessage cPushMessage) {
        Log.i(LOG_TAG, "收到消息 onMessage, messageId: " + cPushMessage.getMessageId() + ", title: " + cPushMessage.getTitle() + ", content:" + cPushMessage.getContent());
        sendPushData(ONMESSAGE, cPushMessage.getTitle(), cPushMessage.getContent(), null, null, cPushMessage.getMessageId());

    }

    @Override
    public void onNotificationOpened(Context context, String title, String summary, String extraMap) {
        Log.i(LOG_TAG, "打开通知 onNotificationOpened, title: " + title + ", summary: " + summary + ", extraMap:" + extraMap);
        sendPushData(ONNOTIFICATIONOPENED, title, summary, extraMap);

    }

    @Override
    protected void onNotificationClickedWithNoAction(Context context, String title, String summary, String extraMap) {
        Log.i(LOG_TAG, "onNotificationClickedWithNoAction, title: " + title + ", summary: " + summary + ", extraMap:" + extraMap);
        sendPushData(ONNOIFICATIONCLICKEDWITHNOACTION, title, summary, extraMap);

    }

    @Override
    protected void onNotificationReceivedInApp(Context context, String title, String summary, Map<String, String> extraMap, int openType, String openActivity, String openUrl) {
        Log.i(LOG_TAG, "onNotificationReceivedInApp, title: " + title + ", summary: " + summary + ", extraMap:" + extraMap + ", openType:" + openType + ", openActivity:" + openActivity + ", openUrl:" + openUrl);
        sendPushData(ONNOTIFICATIONRECEIVEDINAPP, title, summary, extraMap, openUrl);

    }

    @Override
    protected void onNotificationRemoved(Context context, String messageId) {
        Log.i(LOG_TAG, "移除通知 onNotificationRemoved");
//        EventBus.getDefault().post("移除通知 onNotificationRemoved");
        try {
            JSONObject data = new JSONObject();
            setStringData(data, "id", messageId);
            setStringData(data, "type", ONNOTIFICATIONREMOVED);
            AliyunPush.pushData(data);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    private void setStringData(JSONObject jsonObject, String name, String value) throws JSONException {
        if (value != null && !"".equals(value)) {
            jsonObject.put(name, value);
        }
    }

    private void setObjectData(JSONObject jsonObject, String name, JSONObject value) throws JSONException {
      if (value != null && value.length() > 0) {
        jsonObject.put(name, value);
      }
    }

    private void sendPushData(String type, String title, String content, Map<String, String> extraMap, String... openUrl) {
        try {
            JSONObject data = new JSONObject();
            setStringData(data, "type", type);
            setStringData(data, "title", title);
            setStringData(data, "body", content);
            setObjectData(data, "params", new JSONObject(extraMap));
            if (openUrl.length != 0) {
                setStringData(data, "url", openUrl[0]);
                if (openUrl.length > 1) {
                    setStringData(data, "id", openUrl[1]);
                }
            }
            AliyunPush.pushData(data);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    private void sendPushData(String type, String title, String content, String extraMap) {
        Log.d(LOG_TAG, type);
        if (AliyunPush.pushCallbackContext == null) { return; }
        try {
            JSONObject extra = new JSONObject();
            JSONObject data = new JSONObject();
            setStringData(extra, "extra", extraMap);
            setStringData(data, "type", type);
            setStringData(data, "title", title);
            setStringData(data, "body", content);
            setObjectData(data, "params", extra);
            AliyunPush.pushData(data);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }
}
