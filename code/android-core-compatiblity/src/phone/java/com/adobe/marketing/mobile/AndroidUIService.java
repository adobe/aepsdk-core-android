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

import java.util.HashMap;
import java.util.Map;

import com.adobe.marketing.mobile.internal.context.AppLifecycleListener;
import com.adobe.marketing.mobile.internal.context.AppStateListener;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.ui.AlertSetting;
import com.adobe.marketing.mobile.services.ui.NotificationSetting;
import com.adobe.marketing.mobile.services.ui.UIError;
import com.adobe.marketing.mobile.services.ui.AlertListener;
import com.adobe.marketing.mobile.services.ui.internal.MessagesMonitor;

/**
 * The Android implementation for {@link UIService}.
 */
class AndroidUIService implements UIService {
    static final String NOTIFICATION_CONTENT_KEY =
            com.adobe.marketing.mobile.services.ui.AndroidUIService.NOTIFICATION_CONTENT_KEY;
    static final String NOTIFICATION_USER_INFO_KEY =
            com.adobe.marketing.mobile.services.ui.AndroidUIService.NOTIFICATION_USER_INFO_KEY;
    static final String NOTIFICATION_IDENTIFIER_KEY =
            com.adobe.marketing.mobile.services.ui.AndroidUIService.NOTIFICATION_IDENTIFIER_KEY;
    static final String NOTIFICATION_DEEPLINK_KEY =
            com.adobe.marketing.mobile.services.ui.AndroidUIService.NOTIFICATION_DEEPLINK_KEY;
    static final String NOTIFICATION_SOUND_KEY =
            com.adobe.marketing.mobile.services.ui.AndroidUIService.NOTIFICATION_SOUND_KEY;
    static final String NOTIFICATION_SENDER_CODE_KEY =
            com.adobe.marketing.mobile.services.ui.AndroidUIService.NOTIFICATION_SENDER_CODE_KEY;
    static final int NOTIFICATION_SENDER_CODE =
            com.adobe.marketing.mobile.services.ui.AndroidUIService.NOTIFICATION_SENDER_CODE;
    static final String NOTIFICATION_REQUEST_CODE_KEY =
            com.adobe.marketing.mobile.services.ui.AndroidUIService.NOTIFICATION_REQUEST_CODE_KEY;
    static final String NOTIFICATION_TITLE = com.adobe.marketing.mobile.services.ui.AndroidUIService.NOTIFICATION_TITLE;

    MessagesMonitor messagesMonitor = MessagesMonitor.getInstance();

    private final Map<AppStateListener, com.adobe.marketing.mobile.internal.context.AppStateListener> listenerMap = new HashMap<>();

    @Override
    public void showAlert(String title, String message, String positiveButtonText, String negativeButtonText,
                          final UIAlertListener uiAlertListener) {
        AlertListener alertListener = null;

        if (uiAlertListener != null) {
            alertListener = new AlertListener() {
                @Override
                public void onPositiveResponse() {
                    uiAlertListener.onPositiveResponse();
                }

                @Override
                public void onNegativeResponse() {
                    uiAlertListener.onNegativeResponse();
                }

                @Override
                public void onShow() {
                    uiAlertListener.onShow();
                }

                @Override
                public void onDismiss() {
                    uiAlertListener.onDismiss();
                }

                @Override
                public void onError(UIError error) {

                }
            };
        }

        ServiceProvider.getInstance().getUIService().showAlert(AlertSetting.build(title, message, positiveButtonText,
                        negativeButtonText),
                alertListener);
    }

    @Override
    public UIFullScreenMessage createFullscreenMessage(final String html, final UIFullScreenListener fullscreenListener) {
        return new AndroidFullscreenMessage(html, fullscreenListener, messagesMonitor);
    }

    @Override
    public void showLocalNotification(String identifier, String content, long fireDate, int delaySeconds, String deeplink,
                                      Map<String, Object> userInfo, String sound) {
        ServiceProvider.getInstance().getUIService().showLocalNotification(NotificationSetting.build(identifier, content,
                fireDate, delaySeconds,
                deeplink, userInfo, sound, null));
    }

    @Override
    public void showLocalNotification(String identifier, String content, long fireDate, int delaySeconds, String deeplink,
                                      Map<String, Object> userInfo, String sound, String title) {
        ServiceProvider.getInstance().getUIService().showLocalNotification(NotificationSetting.build(identifier, content,
                fireDate, delaySeconds,
                deeplink, userInfo, sound, title));
    }

    @Override
    public boolean showUrl(String url) {
        return ServiceProvider.getInstance().getUIService().showUrl(url);
    }

    @Override
    public AppState getAppState() {
        return AppState.from(AppLifecycleListener.getInstance().getAppState());
    }

    @Override
    public void registerAppStateListener(final AppStateListener listener) {
        if (listener == null) {
            return;
        }
        listenerMap.put(listener, new com.adobe.marketing.mobile.internal.context.AppStateListener() {
            @Override
            public void onForeground() {
                listener.onForeground();
            }

            @Override
            public void onBackground() {
                listener.onBackground();
            }
        });
        AppLifecycleListener.getInstance().registerListener(listenerMap.get(listener));
    }

    @Override
    public void unregisterAppStateListener(final AppStateListener listener) {
        if (listener == null) {
            return;
        }
        AppLifecycleListener.getInstance().unregisterListener(listenerMap.get(listener));
    }

    @Override
    public boolean isMessageDisplayed() {
        return MessagesMonitor.getInstance().isDisplayed();
    }

    @Override
    public FloatingButton createFloatingButton(final FloatingButtonListener buttonListener) {
        final com.adobe.marketing.mobile.services.ui.FloatingButton floatingButton =
                ServiceProvider.getInstance().getUIService().createFloatingButton(buttonListener);
        return new FloatingButton() {
            @Override
            public void display() {
                floatingButton.display();
            }

            @Override
            public void remove() {
                floatingButton.remove();
            }
        };
    }

}
