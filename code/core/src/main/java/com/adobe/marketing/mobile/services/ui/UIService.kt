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

/**
 * Represents a component capable of creating and managing UI elements.
 */
interface UIService {
    /**
     * Creates a Presentable for the given [presentation].
     * @param presentation The [Presentation] type to be created
     * @param presentationUtilityProvider a [PresentationUtilityProvider] that provides components
     *        that should be used for creating the presentation.
     * @return a [Presentable] that is associated with the [presentation].
     */
    fun <T : Presentation<T>> create(
        presentation: T,
        presentationUtilityProvider: PresentationUtilityProvider
    ): Presentable<T>

    /**
     * Sets the presentation delegate for the SDK.
     * @param presentationDelegate a [PresentationDelegate] that will be used to notify presentation events
     * and query for approval before presentation is displayed.
     */
    fun setPresentationDelegate(presentationDelegate: PresentationDelegate)
}
