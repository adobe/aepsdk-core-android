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

import static com.adobe.marketing.mobile.extensions.Sample2Constants.*;

import com.adobe.marketing.mobile.AdobeCallbackWithError;
import com.adobe.marketing.mobile.AdobeError;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.util.DataReader;

public class Sample2 {

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
}
