package com.adobe.marketing.mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;


/**
 * A {@link BroadcastReceiver} that triggers when user dismisses local notification from notification panel. It does the click tracking for local notification.
 */
public final class NotificationDismissalHandler extends BroadcastReceiver {
    private static final String KEY_BROADLOG_ID = "broadlogId";
    private static final String KEY_DELIVERY_ID = "deliveryId";
    private static final String KEY_ACTION = "action";
    private static final String NOTIFICATION_USER_INFO_KEY = "NOTIFICATION_USER_INFO";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.hasExtra(NOTIFICATION_USER_INFO_KEY)) {
            Map<String, Object> notificationData = (Map<String, Object>) intent.getSerializableExtra(NOTIFICATION_USER_INFO_KEY);

            if (notificationData != null) {
                Map<String, Object> contextData = new HashMap<String, Object>(3);
                contextData.put(KEY_BROADLOG_ID, notificationData.get(KEY_BROADLOG_ID));
                contextData.put(KEY_DELIVERY_ID, notificationData.get(KEY_DELIVERY_ID));
                contextData.put(KEY_ACTION, "2");
                MobileCore.collectMessageInfo(contextData);
            }
        }
    }
}