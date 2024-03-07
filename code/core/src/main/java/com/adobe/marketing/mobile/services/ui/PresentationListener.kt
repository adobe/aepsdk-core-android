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
 * A listener for observing the lifecycle of presentations managed by the SDK.
 */
interface PresentationListener {
    /**
     * Invoked when a the presentable is shown.
     * @param presentable the [Presentable] that was shown
     */
    fun onShow(presentable: Presentable<*>)

    /**
     * Invoked when a presentable is hidden.
     * @param presentable the [Presentable] that was hidden
     */
    fun onHide(presentable: Presentable<*>)

    /**
     * Invoked when a presentable is dismissed.
     * @param presentable the [Presentable] that was dismissed
     */
    fun onDismiss(presentable: Presentable<*>)

    /**
     * Invoked when the content in the presentable is loaded.
     * @param presentable the [Presentable] into which that was loaded
     * @param presentationContent optional [PresentationContent] that was loaded into the presentable
     */
    fun onContentLoaded(presentable: Presentable<*>, presentationContent: PresentationContent?)

    /**
     * Defines the types of content that can be loaded into a [Presentable].
     */
    sealed class PresentationContent {
        /**
         * Content loaded from a URL.
         * @param url the URL from which the content was loaded
         */
        class UrlContent(val url: String) : PresentationContent()
    }
}
