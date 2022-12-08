/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.adobe.marketing.mobile.internal.AppResourceStore;
import com.adobe.marketing.mobile.internal.CoreConstants;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.internal.context.App;
import com.adobe.marketing.mobile.util.StringUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class LocalNotificationHandler extends BroadcastReceiver {

    private static final String SELF_TAG = "LocalNotificationHandler";
    private static final String NOTIFICATION_CHANNEL_NAME = "ADOBE_EXPERIENCE_PLATFORM_SDK";
    private static final String NOTIFICATION_CHANNEL_ID = "ADOBE_EXPERIENCE_PLATFORM_SDK";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION =
            "Adobe Experience Platform SDK Notifications";
    private static final String NOTIFICATION_CONTENT_KEY = "NOTIFICATION_CONTENT";
    private static final String NOTIFICATION_USER_INFO_KEY = "NOTIFICATION_USER_INFO";
    private static final String NOTIFICATION_IDENTIFIER_KEY = "NOTIFICATION_IDENTIFIER";
    private static final String NOTIFICATION_DEEPLINK_KEY = "NOTIFICATION_DEEPLINK";
    private static final String NOTIFICATION_SOUND_KEY = "NOTIFICATION_SOUND";
    private static final String NOTIFICATION_SENDER_CODE_KEY = "NOTIFICATION_SENDER_CODE";
    private static final int NOTIFICATION_SENDER_CODE = 750183;
    private static final String NOTIFICATION_REQUEST_CODE_KEY = "NOTIFICATION_REQUEST_CODE";
    private static final String NOTIFICATION_TITLE = "NOTIFICATION_TITLE";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        // get message and request code from previous context
        Bundle bundle = intent.getExtras();

        if (bundle == null) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    SELF_TAG,
                    "Failed to load extras from local notification intent");
            return;
        }

        Context appContext = context.getApplicationContext();
        String message;
        String messageID;
        String deeplink;
        HashMap<String, Object> userInfo;
        String sound;
        int requestCode;
        int senderCode;
        String title;

        try {
            message = bundle.getString(NOTIFICATION_CONTENT_KEY);
            requestCode = bundle.getInt(NOTIFICATION_REQUEST_CODE_KEY);
            senderCode = bundle.getInt(NOTIFICATION_SENDER_CODE_KEY);
            messageID = bundle.getString(NOTIFICATION_IDENTIFIER_KEY);
            deeplink = bundle.getString(NOTIFICATION_DEEPLINK_KEY);
            sound = bundle.getString(NOTIFICATION_SOUND_KEY);
            userInfo = (HashMap<String, Object>) bundle.getSerializable(NOTIFICATION_USER_INFO_KEY);
            title = bundle.getString(NOTIFICATION_TITLE);
        } catch (Exception e) {
            Log.debug(CoreConstants.LOG_TAG, SELF_TAG, "Failed to process bundle (%s)", e);
            return;
        }

        // if our request codes are not matching, we don't care about this intent
        if (senderCode != NOTIFICATION_SENDER_CODE) {
            return;
        }

        // if our message is null, we still don't care
        if (message == null) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    SELF_TAG,
                    "%s (local notification message)",
                    Log.UNEXPECTED_NULL_VALUE);
            return;
        }

        Activity currentActivity =
                ServiceProvider.getInstance().getAppContextService().getCurrentActivity();
        Intent resumeIntent;

        // if we have a deep link, we need to create a new Intent because the old intents are using
        // setClass (overrides opening a deeplink)
        if (deeplink != null && !deeplink.isEmpty()) {
            resumeIntent = new Intent(Intent.ACTION_VIEW);
            resumeIntent.setData(Uri.parse(deeplink));
        } else if (currentActivity != null) {
            resumeIntent = currentActivity.getIntent();
        } else {
            resumeIntent = intent;
        }

        resumeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resumeIntent.putExtra(NOTIFICATION_IDENTIFIER_KEY, messageID);
        resumeIntent.putExtra(NOTIFICATION_USER_INFO_KEY, userInfo);

        final int buildVersion = Build.VERSION.SDK_INT;
        NotificationManager notificationManager =
                (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            // if we have an activity for this notification, use it
            PendingIntent sender;

            if (buildVersion >= Build.VERSION_CODES.M) {
                Class pendingIntentClass = PendingIntent.class;
                Field immutableFlagField = pendingIntentClass.getField("FLAG_IMMUTABLE");
                immutableFlagField.setAccessible(true);
                int immutableFlagValue = (Integer) immutableFlagField.get(null);
                sender =
                        PendingIntent.getActivity(
                                appContext,
                                senderCode,
                                resumeIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | immutableFlagValue);
            } else {
                sender =
                        PendingIntent.getActivity(
                                appContext,
                                senderCode,
                                resumeIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
            }

            if (sender == null) {
                Log.debug(
                        CoreConstants.LOG_TAG,
                        SELF_TAG,
                        "Failed to retrieve sender from broadcast, unable to post notification");
                return;
            }

            // Todo: This seems redundant as the App is first launched before handling Broadcast
            // intent
            App.INSTANCE.setAppContext(context.getApplicationContext());
            DeviceInforming systemInfoService =
                    ServiceProvider.getInstance().getDeviceInfoService();
            String appName = systemInfoService.getApplicationName();
            Object notification;
            Object notificationBuilder;
            Class<?> notificationBuilderClass;

            // notification channels are required if api level is 26 or higher
            if (buildVersion >= Build.VERSION_CODES.O) {
                ClassLoader classLoader = LocalNotificationHandler.class.getClassLoader();
                Class<?> notificationChannelClass =
                        classLoader.loadClass("android.app.NotificationChannel");
                Constructor<?> notificationChannelConstructor =
                        notificationChannelClass.getConstructor(
                                String.class, CharSequence.class, int.class);
                notificationChannelConstructor.setAccessible(true);

                // create notification channel object. high importance is used to allow a heads up
                // notification to be displayed.
                Object notificationChannel =
                        notificationChannelConstructor.newInstance(
                                NOTIFICATION_CHANNEL_ID,
                                NOTIFICATION_CHANNEL_NAME,
                                NotificationManager.IMPORTANCE_HIGH);

                // set notification channel description
                Method setDescription =
                        notificationChannelClass.getMethod("setDescription", String.class);
                setDescription.invoke(notificationChannel, NOTIFICATION_CHANNEL_DESCRIPTION);

                // TODO: handle setting of sound...the previous method got deprecated in API 26

                // create the notification channel
                notificationManager.createNotificationChannel(
                        (NotificationChannel) notificationChannel);

                // specify the notification channel id when creating the notification compat builder
                notificationBuilderClass =
                        classLoader.loadClass("androidx.core.app.NotificationCompat$Builder");
                Constructor<?> notificationConstructor =
                        notificationBuilderClass.getConstructor(
                                Context.class, NOTIFICATION_CHANNEL_ID.getClass());
                notificationConstructor.setAccessible(true);
                notificationBuilder =
                        notificationConstructor.newInstance(
                                context.getApplicationContext(), NOTIFICATION_CHANNEL_ID);

                final Method methodSetStyle =
                        notificationBuilderClass.getDeclaredMethod(
                                "setStyle",
                                classLoader.loadClass(
                                        "androidx.core.app.NotificationCompat$Style"));
                methodSetStyle.invoke(
                        notificationBuilder, getBigTextStyle(buildVersion, classLoader, message));

            } else {
                // if android api level is < 26 / Android O, setup the notification using
                // notification builder
                ClassLoader classLoader = BroadcastHandler.class.getClassLoader();
                notificationBuilderClass =
                        classLoader.loadClass("android.app.Notification$Builder");
                Constructor<?> notificationConstructor =
                        notificationBuilderClass.getConstructor(Context.class);
                notificationConstructor.setAccessible(true);
                notificationBuilder =
                        notificationConstructor.newInstance(context.getApplicationContext());

                Method setSound = notificationBuilderClass.getDeclaredMethod("setSound", Uri.class);
                setSound.invoke(
                        notificationBuilder,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

                // set priority to high to allow a heads up notification to be displayed
                Method setPriority =
                        notificationBuilderClass.getDeclaredMethod("setPriority", int.class);
                setPriority.invoke(notificationBuilder, Notification.PRIORITY_HIGH);

                final Method methodSetStyle =
                        notificationBuilderClass.getDeclaredMethod(
                                "setStyle",
                                classLoader.loadClass("android.app.Notification$Style"));
                methodSetStyle.invoke(
                        notificationBuilder, getBigTextStyle(buildVersion, classLoader, message));
            }

            // set all the notification properties (small icon, content title, and content text are
            // all required)
            // small icon shows up in the status bar
            Method setSmallIcon =
                    notificationBuilderClass.getDeclaredMethod("setSmallIcon", int.class);
            setSmallIcon.invoke(notificationBuilder, getSmallIcon());
            // large icon shows up on the left side of the open notifications
            Bitmap largeIcon = getLargeIcon(context);

            if (largeIcon != null) {
                Method setLargeIcon =
                        notificationBuilderClass.getDeclaredMethod("setLargeIcon", Bitmap.class);
                setLargeIcon.invoke(notificationBuilder, largeIcon);
            }

            // Bolded title of the notification
            Method setContentTitle =
                    notificationBuilderClass.getDeclaredMethod(
                            "setContentTitle", CharSequence.class);

            if (!StringUtils.isNullOrEmpty(title)) {
                setContentTitle.invoke(notificationBuilder, title);
            } else {
                // if no title set for the notification use the app name instead
                setContentTitle.invoke(notificationBuilder, appName);
            }

            // subtext of notification
            Method setContentText =
                    notificationBuilderClass.getDeclaredMethod(
                            "setContentText", CharSequence.class);
            setContentText.invoke(notificationBuilder, message);
            // intent that will be launched when the notification is touched
            Method setContentIntent =
                    notificationBuilderClass.getDeclaredMethod(
                            "setContentIntent", PendingIntent.class);
            setContentIntent.invoke(notificationBuilder, sender);

            // Setting the delete intent for tracking click on deletion.
            Intent deleteIntent = new Intent(appContext, NotificationDismissalHandler.class);
            deleteIntent.putExtra(NOTIFICATION_USER_INFO_KEY, userInfo);
            Method setDeleteIntent =
                    notificationBuilderClass.getDeclaredMethod(
                            "setDeleteIntent", PendingIntent.class);

            if (buildVersion >= Build.VERSION_CODES.M) {
                Class pendingIntentClass = PendingIntent.class;
                Field immutableFlagField = pendingIntentClass.getField("FLAG_IMMUTABLE");
                immutableFlagField.setAccessible(true);
                int immutableFlagValue = (Integer) immutableFlagField.get(null);
                setDeleteIntent.invoke(
                        notificationBuilder,
                        PendingIntent.getBroadcast(
                                appContext,
                                senderCode,
                                deleteIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | immutableFlagValue));
            } else {
                setDeleteIntent.invoke(
                        notificationBuilder,
                        PendingIntent.getBroadcast(
                                appContext,
                                senderCode,
                                deleteIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT));
            }

            // this causes the notification to automatically go away when it is touched
            Method setAutoCancel =
                    notificationBuilderClass.getDeclaredMethod("setAutoCancel", boolean.class);
            setAutoCancel.invoke(notificationBuilder, true);

            // show it
            Method getNotification = notificationBuilderClass.getDeclaredMethod("getNotification");
            notification = getNotification.invoke(notificationBuilder);

            if (notification == null) {
                return;
            }

            notificationManager.notify(new SecureRandom().nextInt(), (Notification) notification);
        } catch (Exception e) {
            Log.warning(
                    CoreConstants.LOG_TAG,
                    SELF_TAG,
                    "unexpected error posting notification (%s)",
                    e);
        }
    }

    // This method returns an instance of BigTextStyle to set expandable style on Notifications.
    private static Object getBigTextStyle(
            final int buildVersion, final ClassLoader classLoader, final String contentText)
            throws Exception {

        Object bigTextStyle;
        Class<?> classBigTextStyle =
                classLoader.loadClass(
                        buildVersion >= Build.VERSION_CODES.O
                                ? "androidx.core.app.NotificationCompat$BigTextStyle"
                                : "android.app.Notification$BigTextStyle");
        Constructor<?> bigTextStyleConstructor = classBigTextStyle.getConstructor();
        bigTextStyle = bigTextStyleConstructor.newInstance();
        Method methodBigText = classBigTextStyle.getDeclaredMethod("bigText", CharSequence.class);
        methodBigText.invoke(bigTextStyle, contentText);
        return bigTextStyle;
    }

    private int getSmallIcon() {
        return AppResourceStore.INSTANCE.getSmallIconResourceID() != -1
                ? AppResourceStore.INSTANCE.getSmallIconResourceID()
                : android.R.drawable.sym_def_app_icon;
    }

    private Bitmap getLargeIcon(final Context appContext)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (appContext == null) {
            return null;
        }

        Drawable iconDrawable = null;
        // first see if we have a user defined one
        int largeIconResourceId = AppResourceStore.INSTANCE.getLargeIconResourceID();

        if (largeIconResourceId != -1) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                Method getDrawable =
                        Resources.class.getDeclaredMethod(
                                "getDrawable", int.class, Resources.Theme.class);
                iconDrawable =
                        (Drawable)
                                getDrawable.invoke(
                                        appContext.getResources(),
                                        largeIconResourceId,
                                        appContext.getTheme());
            } else {
                Method getDrawable = Resources.class.getDeclaredMethod("getDrawable", int.class);
                iconDrawable =
                        (Drawable)
                                getDrawable.invoke(appContext.getResources(), largeIconResourceId);
            }
        }
        // no user defined icon, try to get one from package manager
        else {
            ApplicationInfo applicationInfo = appContext.getApplicationInfo();

            if (applicationInfo != null && appContext.getPackageManager() != null) {
                PackageManager packageManager = appContext.getPackageManager();
                iconDrawable = packageManager.getApplicationIcon(applicationInfo);
            }
        }

        if (iconDrawable == null) {
            return null;
        }

        Bitmap icon = null;
        if (iconDrawable instanceof BitmapDrawable) {
            icon = ((BitmapDrawable) iconDrawable).getBitmap();
        } else {
            icon = getBitmapFromDrawable(iconDrawable);
        }

        return icon;
    }

    /**
     * Draws the drawable provided into a new Bitmap
     *
     * @param drawable The drawable that needs to be extracted into a Bitmap
     * @return The Bitmap drawn from the drawable.
     */
    private Bitmap getBitmapFromDrawable(final Drawable drawable) {
        final Bitmap bmp =
                Bitmap.createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }
}
