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

package com.adobe.marketing.mobile;

import java.util.HashMap;
import java.util.Map;

public class LifecycleEventGeneratorTestHelper {

    static final String ADDITIONAL_CONTEXT_DATA = "additionalcontextdata";
    static final String LIFECYCLE_ACTION_KEY = "action";
    static final String LIFECYCLE_PAUSE = "pause";
    static final String LIFECYCLE_START = "start";

    public static Event createStartEvent(
            final Map<String, String> additionalData, final long timestamp) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(LIFECYCLE_ACTION_KEY, LIFECYCLE_START);
        eventData.put(ADDITIONAL_CONTEXT_DATA, additionalData);
        return new Event.Builder(null, EventType.GENERIC_LIFECYCLE, EventSource.REQUEST_CONTENT)
                .setTimestamp(timestamp)
                .setEventData(eventData)
                .build();
    }

    public static Event createPauseEvent(final long timestamp) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(LIFECYCLE_ACTION_KEY, LIFECYCLE_PAUSE);
        return new Event.Builder(null, EventType.GENERIC_LIFECYCLE, EventSource.REQUEST_CONTENT)
                .setTimestamp(timestamp)
                .setEventData(eventData)
                .build();
    }
}
