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

package com.adobe.marketing.mobile.services.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import com.adobe.marketing.mobile.LocalNotificationHandler;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceConstants;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.ui.internal.MessagesMonitor;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AndroidUIService implements UIService {

    private static final String LOG_TAG = AndroidUIService.class.getSimpleName();
    private static final String UNEXPECTED_NULL_VALUE = "Unexpected Null Value";

    public static final String NOTIFICATION_CONTENT_KEY = "NOTIFICATION_CONTENT";
    public static final String NOTIFICATION_USER_INFO_KEY = "NOTIFICATION_USER_INFO";
    public static final String NOTIFICATION_IDENTIFIER_KEY = "NOTIFICATION_IDENTIFIER";
    public static final String NOTIFICATION_DEEPLINK_KEY = "NOTIFICATION_DEEPLINK";
    public static final String NOTIFICATION_SOUND_KEY = "NOTIFICATION_SOUND";
    public static final String NOTIFICATION_SENDER_CODE_KEY = "NOTIFICATION_SENDER_CODE";
    public static final int NOTIFICATION_SENDER_CODE = 750183;
    public static final String NOTIFICATION_REQUEST_CODE_KEY = "NOTIFICATION_REQUEST_CODE";
    public static final String NOTIFICATION_TITLE = "NOTIFICATION_TITLE";
    private URIHandler uriHandler;

    MessagesMonitor messagesMonitor = MessagesMonitor.getInstance();

    /*
     * Responsible for holding a single thread executor for lazy initialization only if
     * an AEPMessage will be created.
     */
    private static class ExecutorHolder {
        static final ExecutorService INSTANCE = Executors.newSingleThreadExecutor();
    }

    private static ExecutorService getExecutor() {
        return ExecutorHolder.INSTANCE;
    }

    @Override
    public void showAlert(final AlertSetting alertSetting, final AlertListener alertListener) {
        if (messagesMonitor.isDisplayed()) {
            if (alertListener != null) {
                alertListener.onError(UIError.ANOTHER_MESSAGE_IS_DISPLAYED);
            }

            Log.warning(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to show alert, another message is displayed at this time");
            return;
        }

        final Activity currentActivity =
                ServiceProvider.getInstance().getAppContextService().getCurrentActivity();

        if (currentActivity == null) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format(
                            "%s (current activity), unable to show alert", UNEXPECTED_NULL_VALUE));
            return;
        }

        if (isNullOrEmpty(alertSetting.getNegativeButtonText())
                && isNullOrEmpty(alertSetting.getPositiveButtonText())) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    "Unable to show alert, button texts are invalid.");
            return;
        }

        // Need to call the alertDialog.show() in a UI thread.
        currentActivity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog.Builder alertDialogBuilder =
                                new AlertDialog.Builder(currentActivity);
                        alertDialogBuilder.setTitle(alertSetting.getTitle());
                        alertDialogBuilder.setMessage(alertSetting.getMessage());
                        final DialogInterface.OnClickListener onClickListener =
                                getAlertDialogOnClickListener(alertListener);

                        if (alertSetting.getPositiveButtonText() != null
                                && !alertSetting.getPositiveButtonText().isEmpty()) {
                            alertDialogBuilder.setPositiveButton(
                                    alertSetting.getPositiveButtonText(), onClickListener);
                        }

                        if (alertSetting.getNegativeButtonText() != null
                                && !alertSetting.getNegativeButtonText().isEmpty()) {
                            alertDialogBuilder.setNegativeButton(
                                    alertSetting.getNegativeButtonText(), onClickListener);
                        }

                        alertDialogBuilder.setOnCancelListener(
                                getAlertDialogOnCancelListener(alertListener));
                        final AlertDialog alertDialog = alertDialogBuilder.create();

                        alertDialog.setOnShowListener(getAlertDialogOnShowListener(alertListener));

                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                    }
                });

        messagesMonitor.displayed();
    }

    /**
     * Creates a new instance of {@code OnShowListener}.
     *
     * @param alertListener The {@link AlertListener} instance to provide a callback to the SDK
     *     core.
     * @return An instance of {@link DialogInterface.OnShowListener}
     */
    DialogInterface.OnShowListener getAlertDialogOnShowListener(final AlertListener alertListener) {
        return new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                if (alertListener != null) {
                    alertListener.onShow();
                }
            }
        };
    }

    /**
     * Creates a new instance of {@code OnCancelListener}.
     *
     * @param alertListener The {@link AlertListener} instance to provide a callback to the SDK
     *     core.
     * @return An instance of {@link DialogInterface.OnCancelListener}
     */
    DialogInterface.OnCancelListener getAlertDialogOnCancelListener(
            final AlertListener alertListener) {
        return new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(final DialogInterface dialogInterface) {
                messagesMonitor.dismissed();

                if (alertListener != null) {
                    alertListener.onDismiss();
                }
            }
        };
    }

    /**
     * Creates a new instance of {@code OnClickListener}.
     *
     * @param alertListener The {@link AlertListener} instance to provide a callback to the SDK
     *     core.
     * @return An instance of {@link DialogInterface.OnClickListener}
     */
    DialogInterface.OnClickListener getAlertDialogOnClickListener(
            final AlertListener alertListener) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                messagesMonitor.dismissed();

                if (alertListener != null) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        alertListener.onPositiveResponse();
                    } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                        alertListener.onNegativeResponse();
                    }
                }
            }
        };
    }

    @SuppressLint("TrulyRandom")
    @Override
    public void showLocalNotification(final NotificationSetting notificationSetting) {
        final Context appContext =
                ServiceProvider.getInstance().getAppContextService().getApplicationContext();

        if (appContext == null) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format(
                            "%s (application context), unable to show local notification",
                            UNEXPECTED_NULL_VALUE));
            return;
        }

        final int requestCode = new SecureRandom().nextInt();

        // prefer a specified fireDate, otherwise use delaySeconds
        Calendar calendar = Calendar.getInstance();

        if (notificationSetting.getFireDate() > 0) {
            // do math to calculate number of seconds to add, because android api for
            // calendar.builder is API 26...
            final int secondsUntilFireDate =
                    (int) (notificationSetting.getFireDate() - (calendar.getTimeInMillis() / 1000));

            if (secondsUntilFireDate > 0) {
                calendar.add(Calendar.SECOND, secondsUntilFireDate);
            }
        } else {
            calendar.add(Calendar.SECOND, notificationSetting.getDelaySeconds());
        }

        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(appContext, LocalNotificationHandler.class);
        intent.putExtra(NOTIFICATION_SENDER_CODE_KEY, NOTIFICATION_SENDER_CODE);
        intent.putExtra(NOTIFICATION_IDENTIFIER_KEY, notificationSetting.getIdentifier());
        intent.putExtra(NOTIFICATION_REQUEST_CODE_KEY, requestCode);
        intent.putExtra(NOTIFICATION_DEEPLINK_KEY, notificationSetting.getDeeplink());
        intent.putExtra(NOTIFICATION_CONTENT_KEY, notificationSetting.getContent());
        intent.putExtra(
                NOTIFICATION_USER_INFO_KEY,
                (HashMap<String, Object>) notificationSetting.getUserInfo());
        intent.putExtra(NOTIFICATION_SOUND_KEY, notificationSetting.getSound());
        intent.putExtra(NOTIFICATION_TITLE, notificationSetting.getTitle());

        try {
            PendingIntent sender;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 23
                Class pendingIntentClass = PendingIntent.class;
                Field immutableFlagField = pendingIntentClass.getField("FLAG_IMMUTABLE");
                immutableFlagField.setAccessible(true);
                int immutableFlagValue = (Integer) immutableFlagField.get(null);
                sender =
                        PendingIntent.getBroadcast(
                                appContext,
                                requestCode,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT | immutableFlagValue);
            } else {
                sender =
                        PendingIntent.getBroadcast(
                                appContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            final AlarmManager alarmManager =
                    (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
            }
        } catch (Exception e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format(
                            "Unable to create PendingIntent object, error: %s",
                            e.getLocalizedMessage()));
        }
    }

    @Override
    public boolean showUrl(final String url) {
        final Activity currentActivity =
                ServiceProvider.getInstance().getAppContextService().getCurrentActivity();

        if (currentActivity == null) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format(
                            "%s (current activity), could not open URL %s",
                            UNEXPECTED_NULL_VALUE, url));
            return false;
        }

        if (isNullOrEmpty(url)) {
            Log.warning(
                    ServiceConstants.LOG_TAG, LOG_TAG, "Could not open URL - URL was not provided");
            return false;
        }

        try {
            final Intent intent = getIntentWithURI(url);
            currentActivity.startActivity(intent);
            return true;
        } catch (Exception ex) {
            Log.warning(ServiceConstants.LOG_TAG, LOG_TAG, "Could not open an Intent with URL");
        }

        return false;
    }

    @Override
    public void setURIHandler(final URIHandler uriHandler) {
        this.uriHandler = uriHandler;
    }

    @Override
    public Intent getIntentWithURI(final String uri) {
        URIHandler handler = this.uriHandler;
        Intent intent = null;
        if (handler != null) {
            intent = handler.getURIDestination(uri);
            if (intent == null) {
                Log.debug(
                        ServiceConstants.LOG_TAG,
                        LOG_TAG,
                        String.format(
                                "%s is not handled with a custom Intent, use SDK's default Intent"
                                        + " instead.",
                                uri));
            }
        }
        if (intent == null) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uri));
        }
        return intent;
    }

    @Override
    public FloatingButton createFloatingButton(final FloatingButtonListener buttonListener) {
        Activity currentActivity =
                ServiceProvider.getInstance().getAppContextService().getCurrentActivity();

        if (currentActivity == null) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format(
                            "%s (current activity), no button created.", UNEXPECTED_NULL_VALUE));
            return null;
        }

        FloatingButtonView floatingButtonView = createFloatingButtonView(currentActivity);
        FloatingButtonManager floatingButtonManager =
                new FloatingButtonManager(this, buttonListener);
        floatingButtonManager.addManagedButton(
                currentActivity.getLocalClassName(), floatingButtonView);

        return floatingButtonManager;
    }

    @Override
    public FullscreenMessage createFullscreenMessage(
            final String html,
            final FullscreenMessageDelegate listener,
            final boolean isLocalImageUsed,
            final MessageSettings settings) {
        AEPMessage message = null;

        try {
            message =
                    new AEPMessage(
                            html,
                            listener,
                            isLocalImageUsed,
                            messagesMonitor,
                            settings,
                            getExecutor());
        } catch (MessageCreationException exception) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format(
                            "Error when creating the message: %s.",
                            exception.getLocalizedMessage()));
        }

        return message;
    }

    FloatingButtonView createFloatingButtonView(final Activity currentActivity) {
        FloatingButtonView floatingButtonView = new FloatingButtonView(currentActivity);
        floatingButtonView.setTag(FloatingButtonView.VIEW_TAG);
        return floatingButtonView;
    }

    private static boolean isNullOrEmpty(final String str) {
        return str == null || str.trim().isEmpty();
    }
}
