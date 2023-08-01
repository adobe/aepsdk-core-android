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

/**
 * A notification mechanism for the component that created the associated [Presentable] to receive events
 * about a presentation in response to user interaction, system events, or operations performed programmatically.
 */
interface PresentationEventListener<T : Presentable<*>> {
    /**
     * Invoked when the presentable is shown.
     * @param presentable the presentable that was shown
     */
    fun onShow(presentable: T)

    /**
     * Invoked when the presentable is hidden.
     * @param presentable the presentable that was hidden
     */
    fun onHide(presentable: T)

    /**
     * Invoked when the presentable is dismissed.
     * @param presentable the presentable that was dismissed
     */
    fun onDismiss(presentable: T)

    fun onError(presentable: T, error: PresentationError)
}
