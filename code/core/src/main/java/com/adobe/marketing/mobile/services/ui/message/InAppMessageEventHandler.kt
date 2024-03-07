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

package com.adobe.marketing.mobile.services.ui.message

import com.adobe.marketing.mobile.AdobeCallback

/**
 * Interface for take actions on an InAppMessage presentation.
 */
interface InAppMessageEventHandler {

    /**
     * Registers a {@link JavascriptInterface} for the provided handler name to the {@link WebView}
     * associated with the InAppMessage presentation to handle Javascript messages. When the registered
     * handlers are executed via the HTML the result will be passed back to the associated [callback].
     * @param handlerName the name of the handler to register
     * @param callback the callback to be invoked with the result of the javascript execution
     */
    fun handleJavascriptMessage(handlerName: String, callback: AdobeCallback<String>)

    /**
     * Evaluates the provided javascript content in the {@link WebView} maintained by the InAppMessage
     * and passes the result to the provided [callback].
     * @param jsContent the javascript content to be executed
     * @param callback the callback to be invoked with the result of the javascript execution
     */
    fun evaluateJavascript(jsContent: String, callback: AdobeCallback<String>)
}
