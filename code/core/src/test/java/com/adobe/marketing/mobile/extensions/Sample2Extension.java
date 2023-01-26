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

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import java.util.HashMap;
import java.util.Map;

class Sample2Constants {

    static final String NAME = "Sample2";
    static final String TYPE_REQUEST_IDENTIFIER = "com.adobe.eventType.requestIdentifier";
    static final String TYPE_RESPONSE_IDENTIFIER = "com.adobe.eventType.responseIdentifier";
    static final String EVENT_SOURCE = "com.adobe.eventSource." + NAME;
    static final String EVENT_DATA_IDENTIFIER = "trackingidentifier";
}

public class Sample2Extension extends Extension {

    Sample2Extension(ExtensionApi extensionApi) {
        super(extensionApi);
    }

    @Override
    protected String getName() {
        return Sample2Constants.NAME;
    }

    @Override
    protected void onRegistered() {
        getApi().registerEventListener(
                        Sample2Constants.TYPE_REQUEST_IDENTIFIER,
                        Sample2Constants.EVENT_SOURCE,
                        this::handleGetIdentifier);
    }

    private void handleGetIdentifier(Event e) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(Sample2Constants.EVENT_DATA_IDENTIFIER, Sample2Constants.NAME + "_ID");

        Event responseEvent =
                new Event.Builder(
                                "GetIdentifierResponse",
                                Sample2Constants.TYPE_RESPONSE_IDENTIFIER,
                                Sample2Constants.EVENT_SOURCE)
                        .inResponseToEvent(e)
                        .setEventData(eventData)
                        .build();
        getApi().dispatch(responseEvent);
    }
}
