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

package com.adobe.marketing.mobile.services.ui

import com.adobe.marketing.mobile.services.ui.alert.AlertEventListener
import com.adobe.marketing.mobile.services.ui.alert.AlertSettings
import com.adobe.marketing.mobile.services.ui.floatingbutton.FloatingButtonEventHandler
import com.adobe.marketing.mobile.services.ui.floatingbutton.FloatingButtonEventListener
import com.adobe.marketing.mobile.services.ui.floatingbutton.FloatingButtonSettings
import com.adobe.marketing.mobile.services.ui.message.InAppMessageEventHandler
import com.adobe.marketing.mobile.services.ui.message.InAppMessageEventListener
import com.adobe.marketing.mobile.services.ui.message.InAppMessageSettings
import java.util.UUID

/**
 * Defines types of [Presentable]s supported by the AEP SDK.
 * Holds the [PresentationEventListener] for the presentation.
 */
sealed class Presentation<T : Presentation<T>>(val listener: PresentationEventListener<T>) {
    /**
     * The unique identifier for this presentation.
     */
    val id: String = UUID.randomUUID().toString()
}

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
        internal set
}

/**
 * Represents a FloatingButton presentation.
 * @param eventListener the listener for the getting notified about FloatingButton lifecycle events
 * @param settings the settings for the FloatingButton
 */
class FloatingButton(
    val settings: FloatingButtonSettings,
    val eventListener: FloatingButtonEventListener
) : Presentation<FloatingButton>(eventListener) {

    /**
     * The event handler for the FloatingButton.
     */
    lateinit var eventHandler: FloatingButtonEventHandler
        internal set
}

/**
 * Represents an Alert presentation.
 * @param settings the settings for the Alert
 * @param eventListener the listener for the getting notified about Alert lifecycle events
 */
class Alert(
    val settings: AlertSettings,
    val eventListener: AlertEventListener
) : Presentation<Alert>(eventListener)
