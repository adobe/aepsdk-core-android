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

/**
 * A neutral, host-agnostic description of a single push-notification interaction, passed by the UI
 * add-on to the host's [IPushTemplateTrackingProvider.getPendingIntent].
 *
 * The interaction [type] is a plain string agreed between the host and the UI add-on, so new
 * interaction types never require a Core release. Well-known values:
 * - `"content_click"` - notification body tap
 * - `"button_click"` - action button tap
 * - `"dismiss"` - notification dismissed
 * - `"input_submit"` - text reply submitted (requires [mutablePendingIntent])
 * - `"rerender"` - a gesture that must rebuild the notification (for example a carousel arrow); the host
 *   delivers it back to its receiver and calls [IUiTemplatePlugin.buildPushTemplateNotification] again
 *   with [templateExtras] merged into the message data
 *
 * A host returns `null` for a type it cannot handle; the UI add-on then leaves that action unwired.
 *
 * This is intentionally a plain (non-`data`) class so fields can be added later as defaulted
 * constructor parameters without breaking binary compatibility.
 *
 * @property type the interaction type string
 * @property actionUri the action URI (deeplink / web URL), or `null` (a click with no uri opens the app)
 * @property actionId the action identifier (for example a button label). For `"rerender"` it is set only
 *     when the gesture should be tracked
 * @property templateExtras UI-owned template state (for example a carousel index) that the host must
 *     return in the message data on re-render; `null` when the interaction carries no state
 * @property mutablePendingIntent whether the host must build a mutable PendingIntent (for example for
 *     RemoteInput text replies)
 */
class PushInteraction @JvmOverloads constructor(
    val type: String,
    val actionUri: String? = null,
    val actionId: String? = null,
    val templateExtras: Map<String, String>? = null,
    val mutablePendingIntent: Boolean = false
) {
    override fun toString(): String =
        "PushInteraction(type=$type, actionUri=$actionUri, actionId=$actionId, " +
            "templateExtras=$templateExtras, mutablePendingIntent=$mutablePendingIntent)"
}
