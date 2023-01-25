/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.extensions

import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.AdobeError
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.util.DataReader

class Sample1Kt(extensionApi: ExtensionApi) : Extension(extensionApi) {
    override fun getName(): String {
        return NAME
    }

    override fun onRegistered() {
        api.registerEventListener(TYPE_REQUEST_IDENTIFIER, EVENT_SOURCE, this::handleGetIdentifier)
    }

    private fun handleGetIdentifier(e: Event) {
        val eventData = mapOf<String, Any>(EVENT_DATA_IDENTIFIER to "${NAME}_ID")
        val responseEvent = Event.Builder("GetIdentifierResponse", TYPE_RESPONSE_IDENTIFIER, EVENT_SOURCE)
            .inResponseToEvent(e)
            .setEventData(eventData)
            .build()
        api.dispatch(responseEvent)
    }

    companion object {
        private const val NAME = "Sample1Kt"
        private const val TYPE_REQUEST_IDENTIFIER = "com.adobe.eventType.requestIdentifier"
        private const val TYPE_RESPONSE_IDENTIFIER = "com.adobe.eventType.responseIdentifier"
        private const val EVENT_SOURCE = "com.adobe.eventSource.$NAME"
        private const val EVENT_DATA_IDENTIFIER = "trackingidentifier"

        // Public APIs
        @JvmStatic
        fun getTrackingIdentifier(callback: AdobeCallbackWithError<String>) {
            val e =
                Event.Builder("GetIdentifier", TYPE_REQUEST_IDENTIFIER, EVENT_SOURCE).build()
            MobileCore.dispatchEventWithResponseCallback(
                e,
                1000,
                object : AdobeCallbackWithError<Event> {
                    override fun fail(error: AdobeError) {
                        callback.fail(error)
                    }

                    override fun call(value: Event?) {
                        val identifier =
                            DataReader.optString(value?.eventData, EVENT_DATA_IDENTIFIER, "")
                        callback.call(identifier)
                    }
                }
            )
        }
    }
}
