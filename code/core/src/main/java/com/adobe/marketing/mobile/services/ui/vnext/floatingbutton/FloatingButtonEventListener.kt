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

package com.adobe.marketing.mobile.services.ui.vnext.floatingbutton

import com.adobe.marketing.mobile.services.ui.vnext.FloatingButton
import com.adobe.marketing.mobile.services.ui.vnext.Presentable
import com.adobe.marketing.mobile.services.ui.vnext.PresentationEventListener

/**
 * Interface for listening to events on a floating button presentation.
 */
interface FloatingButtonEventListener : PresentationEventListener<FloatingButton> {
    /**
     * Called when a tap is detected on the floating button.
     * @param presentable the floating button presentable on which the tap was detected
     */
    fun onTapDetected(presentable: Presentable<FloatingButton>)

    /**
     * Called when a pan is detected on the floating button.
     * @param presentable the floating button presentable on which the pan was detected
     */
    fun onPanDetected(presentable: Presentable<FloatingButton>)
}
