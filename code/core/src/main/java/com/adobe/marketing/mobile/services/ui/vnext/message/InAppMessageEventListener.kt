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

package com.adobe.marketing.mobile.services.ui.vnext.message

import com.adobe.marketing.mobile.services.ui.vnext.InAppMessage
import com.adobe.marketing.mobile.services.ui.vnext.Presentable
import com.adobe.marketing.mobile.services.ui.vnext.PresentationEventListener

/**
 * Interface for listening to events related to an InAppMessage presentation.
 * @see PresentationEventListener
 */
interface InAppMessageEventListener : PresentationEventListener<InAppMessage> {

    /**
     * Invoked when the back button is pressed via a button or a gesture while
     * the InAppMessage is being presented.
     */
    fun onBackPressed(message: Presentable<InAppMessage>)

    /**
     * Invoked when a url is about to be loaded into the InAppMessage WebView.
     * @param message the InAppMessage that is being presented
     * @param url the url that is about to be loaded
     * @return true if the url will be handled, false otherwise
     */
    fun onUrlLoading(message: Presentable<InAppMessage>, url: String): Boolean
}
