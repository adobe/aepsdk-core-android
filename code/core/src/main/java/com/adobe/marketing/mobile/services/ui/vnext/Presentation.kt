/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services.ui.vnext

import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageEventHandler
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageEventListener
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageSettings

/**
 * Defines types of [Presentable]s supported by the AEP SDK.
 * Holds the [PresentationEventListener] for the presentation.
 */
sealed class Presentation<T : Presentation<T>>(val listener: PresentationEventListener<Presentable<T>>)

// ---- Presentation Types ---- //

/**
 * Represents an InAppMessage presentation.
 * @param settings the settings for the InAppMessage
 * @param eventListener the listener for the getting notified about InAppMessage lifecycle events
 * @param eventHandler the event handler performing operations on the InAppMessage
 */
class InAppMessage(
    val settings: InAppMessageSettings,
    val eventListener: InAppMessageEventListener
) : Presentation<InAppMessage>(eventListener) {

    /**
     * The event handler for the InAppMessage.
     */
    lateinit var eventHandler: InAppMessageEventHandler
}
