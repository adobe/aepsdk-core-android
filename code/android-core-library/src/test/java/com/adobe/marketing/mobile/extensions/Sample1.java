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

package com.adobe.marketing.mobile.extensions;

import com.adobe.marketing.mobile.AdobeCallbackWithError;
import com.adobe.marketing.mobile.AdobeError;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.util.DataReader;
import java.util.HashMap;
import java.util.Map;

public class Sample1 extends Extension {

    private static final String NAME = "Sample1";
    private static final String TYPE_REQUEST_IDENTIFIER = "com.adobe.eventType.requestIdentifier";
    private static final String TYPE_RESPONSE_IDENTIFIER = "com.adobe.eventType.responseIdentifier";
    private static final String EVENT_SOURCE = "com.adobe.eventSource." + NAME;
    private static final String EVENT_DATA_IDENTIFIER = "trackingidentifier";

    // Public APIs
    public static void getTrackingIdentifier(AdobeCallbackWithError<String> callback) {
        Event e = new Event.Builder("GetIdentifier", TYPE_REQUEST_IDENTIFIER, EVENT_SOURCE).build();
        MobileCore.dispatchEventWithResponseCallback(
                e,
                1000,
                new AdobeCallbackWithError<Event>() {
                    @Override
                    public void fail(AdobeError error) {
                        callback.fail(error);
                    }

                    @Override
                    public void call(Event value) {
                        String identifier =
                                DataReader.optString(
                                        value.getEventData(), EVENT_DATA_IDENTIFIER, "");
                        callback.call(identifier);
                    }
                });
    }

    // Extension methods
    Sample1(ExtensionApi extensionApi) {
        super(extensionApi);
    }

    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    protected void onRegistered() {
        getApi().registerEventListener(
                        TYPE_REQUEST_IDENTIFIER, EVENT_SOURCE, this::handleGetIdentifier);
    }

    private void handleGetIdentifier(Event e) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(EVENT_DATA_IDENTIFIER, NAME + "_ID");

        Event responseEvent =
                new Event.Builder("GetIdentifierResponse", TYPE_RESPONSE_IDENTIFIER, EVENT_SOURCE)
                        .inResponseToEvent(e)
                        .setEventData(eventData)
                        .build();
        getApi().dispatch(responseEvent);
    }
}
