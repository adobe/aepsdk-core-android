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

import android.app.PendingIntent

/**
 * Contract implemented by a **host** SDK (for example Messaging) to own tracking and intent handling
 * for out-of-the-box push-template notifications, while the UI add-on (for example
 * `aepsdk-ui-android`'s `notificationbuilder`) owns rendering.
 *
 * This is the **single tracking channel** for every interaction, terminal or re-render alike: the UI
 * add-on asks the host for a [PendingIntent] describing an interaction, and the host returns one that
 * targets its own tracker `Activity` / `BroadcastReceiver`, carrying the host's action strings and
 * tracking extras (for example `messageId` / XDM). The host decides routing per interaction type (for
 * example a user-facing click to an `Activity`, a dismissal to a silent `BroadcastReceiver`).
 *
 * The contract uses only platform-framework types so the host and the UI add-on both depend on Core
 * only, with no dependency on each other. Implementations fetch their own `Context` from Core's
 * `ServiceProvider`.
 */
interface IPushTemplateTrackingProvider {

    /**
     * Builds a tracking [PendingIntent] for the given [interaction].
     *
     * @param interaction the [PushInteraction] describing what the user did and the associated action
     *     data (type / uri / id / template state / mutability requirement)
     * @return a [PendingIntent] targeting the host's tracker `Activity` / `BroadcastReceiver`, or `null`
     *     if the host cannot handle this interaction type
     */
    fun getPendingIntent(interaction: PushInteraction): PendingIntent?
}
