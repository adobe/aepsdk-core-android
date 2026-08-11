/*
  Copyright 2026 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/
package com.adobe.marketing.mobile.plugin

import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context

/**
 * Plugin contract for rendering an out-of-the-box push-template notification (carousel, basic,
 * input-box, timer, etc.).
 *
 * The contract uses only platform-framework types ([Context], [Notification], [Activity],
 * [BroadcastReceiver], [Map]) so a host SDK on the floor toolchain can hold this reference while the
 * implementing add-on (for example {@code aepsdk-ui-android}'s {@code notificationbuilder}) lives in
 * its own module and depends only on Core. No androidx or Firebase type appears in Core's API.
 *
 * Unlike the live-activity plugin, the template plugin only **builds** the notification and returns
 * it; the host SDK owns posting and tracking. This keeps the UI add-on free of any dependency on the
 * host SDK.
 */
interface IUiTemplatePlugin : IAepPlugin {

    /**
     * Builds a push-template notification from the FCM data map.
     *
     * @param context the application [Context]
     * @param messageData the push message data (from {@code RemoteMessage.getData()}); the template
     *     type is read from the app-defined template-type key within it
     * @param trackerActivityClass the host's tracker [Activity] used for tap/click intents, if any
     * @param broadcastReceiverClass the host's [BroadcastReceiver] used for action/dismiss intents, if any
     * @return the built [Notification], or {@code null} if the payload cannot be rendered (the caller
     *     falls back to a basic notification)
     */
    fun buildPushTemplateNotification(
        context: Context,
        messageData: Map<String, String>,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?
    ): Notification?
}
