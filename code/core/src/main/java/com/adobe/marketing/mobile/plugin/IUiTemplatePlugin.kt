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

import android.app.Notification

/**
 * Plugin contract for rendering an out-of-the-box push-template notification.
 *
 * The contract uses only platform-framework types plus the neutral Core types
 * [IPushTemplateTrackingProvider] / [PushInteraction], so a host SDK (for example Messaging) can use the
 * implementing add-on (for example `aepsdk-ui-android`'s `notificationbuilder`) with neither depending
 * on the other.
 *
 * Ownership split: the UI add-on **renders** the notification; the **host** owns posting, tracking and
 * intent handling. The UI add-on obtains every [android.app.PendingIntent] from the host-supplied
 * [IPushTemplateTrackingProvider].
 *
 * **Re-render.** Interactive templates request `"rerender"` interactions (see [PushInteraction]). The
 * host delivers them to its own receiver, merges the state back into the message data and calls
 * [buildPushTemplateNotification] again, posting the result under the same notification id. So first
 * render and re-render use this one method.
 *
 * **Reserved message-data keys** (fixed; the rest of the map is the push payload as received):
 * - `"messageId"` - the push message id, added by the host
 * - `"notificationId"` - the id (as a decimal string) the host posts the notification with, added by the
 *   host. The UI add-on uses it when it must cancel the notification itself (for example remind-later)
 *
 * On re-render the host builds the map in a fixed order: push payload, then the interaction's
 * [PushInteraction.templateExtras], then any RemoteInput results (later entries win).
 */
interface IUiTemplatePlugin : IAepPlugin {

    /**
     * Builds a push-template notification from the message data. Called for the first render and for
     * every re-render.
     *
     * The implementation resolves the application `Context` from Core's `ServiceProvider`. It must not
     * post the notification; the host posts it.
     *
     * @param messageData the push message data plus the reserved keys (and, on re-render, the template
     *     state and input results)
     * @param trackingProvider the host's [IPushTemplateTrackingProvider] used to build every PendingIntent
     * @return the built [Notification], or `null` if nothing should be posted. On first render the host
     *     falls back to a basic notification; on re-render it posts nothing
     */
    fun buildPushTemplateNotification(
        messageData: Map<String, String>,
        trackingProvider: IPushTemplateTrackingProvider
    ): Notification?
}
