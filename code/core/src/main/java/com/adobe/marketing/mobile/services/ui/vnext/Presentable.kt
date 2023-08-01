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
 * Represents a component that can be presented on the screen.
 */
interface Presentable<T : Presentation<*>> {

    /**
     * Represents the current visibility & activeness of the presentable.
     */
    enum class State {
        /**
         * Indicates that the presentable is visible on the screen.
         */
        VISIBLE,

        /**
         * Indicates that the presentable was previously VISIBLE but is now hidden from the screen.
         */
        HIDDEN,

        /**
         * Indicates that the presentable has either been removed from the screen or has not been shown yet.
         */
        DETACHED
    }

    /**
     * Shows the presentable on the screen.
     */
    fun show()

    /**
     * Hides the presentable from the screen.
     * Contents of the presentable can be restored by calling [show] after this operation.
     */
    fun hide()

    /**
     * Dismisses the presentable from the screen.
     * Contents of the presentable are not retained after this operation.
     */
    fun dismiss()

    /**
     * Returns the current [State] of the presentable.
     */
    fun getState(): State

    /**
     * Returns the presentation associated with the presentable.
     * @return the [Presentation] associated with the presentable
     */
    fun getPresentation(): T
}
