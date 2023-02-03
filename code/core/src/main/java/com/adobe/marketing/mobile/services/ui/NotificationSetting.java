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

import java.util.Map;

public class NotificationSetting {

    private String identifier;
    private String content;
    private long fireDate;
    private int delaySeconds;
    private String deeplink;
    private Map<String, Object> userInfo;
    private String sound;
    private String title;

    private NotificationSetting() {}

    /**
     * Build a {@link NotificationSetting} instance used for creating the local notification.
     *
     * @param identifier String unique identifier for the local notification
     * @param content String notification message content
     * @param fireDate {@code long} containing a specific date and time to show the notification,
     *     represented as number of seconds since epoch
     * @param delaySeconds int number of seconds to wait before displaying this local notification
     * @param deeplink String the link to be opened on notification clickthrough
     * @param userInfo {@code Map<String, Object>} of additional data for the local notification
     * @param sound {@code String} containing a custom sound to play when the notification is shown
     * @param title (@code String} notification message title
     * @return A {@link NotificationSetting} instance
     */
    public static NotificationSetting build(
            final String identifier,
            final String content,
            final long fireDate,
            final int delaySeconds,
            final String deeplink,
            final Map<String, Object> userInfo,
            final String sound,
            final String title) {
        NotificationSetting notificationSetting = new NotificationSetting();
        notificationSetting.identifier = identifier;
        notificationSetting.content = content;
        notificationSetting.fireDate = fireDate;
        notificationSetting.delaySeconds = delaySeconds;
        notificationSetting.deeplink = deeplink;
        notificationSetting.userInfo = userInfo;
        notificationSetting.sound = sound;
        notificationSetting.title = title;
        return notificationSetting;
    }

    /**
     * @return unique identifier for the local notification
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return notification message content
     */
    public String getContent() {
        return content;
    }

    /**
     * @return a specific date and time to show the notification, represented as number of seconds
     *     since epoch
     */
    public long getFireDate() {
        return fireDate;
    }

    /**
     * @return int number of seconds to wait before displaying this local notification
     */
    public int getDelaySeconds() {
        return delaySeconds;
    }

    /**
     * @return the link to be opened on notification clickthrough
     */
    public String getDeeplink() {
        return deeplink;
    }

    /**
     * @return {@code Map<String, Object>} of additional data for the local notification
     */
    public Map<String, Object> getUserInfo() {
        return userInfo;
    }

    /**
     * @return a custom sound to play when the notification is shown
     */
    public String getSound() {
        return sound;
    }

    /**
     * @return the title of the notification message
     */
    public String getTitle() {
        return title;
    }
}
