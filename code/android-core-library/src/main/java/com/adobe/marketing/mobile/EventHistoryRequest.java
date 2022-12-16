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

import com.adobe.marketing.mobile.internal.util.MapUtilsKt;
import java.util.Map;

public class EventHistoryRequest {

    private final Map<String, Object> map;
    private final long fromDate;
    private final long toDate;

    /**
     * Used for selecting or deleting Events from Event History.
     *
     * @param map Key-value pairs that will be used to generate the hash when looking up an Event.
     * @param fromDate Date that represents the lower bounds of the date range used when looking up
     *     an Event. If not provided, the lookup will use the beginning of Event History as the
     *     lower bounds.
     * @param toDate Date that represents the upper bounds of the date range used when looking up an
     *     Event. If not provided, there will be no upper bound on the date range.
     */
    public EventHistoryRequest(
            final Map<String, Object> map, final long fromDate, final long toDate) {
        this.map = map;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public long getMaskAsDecimalHash() {
        return MapUtilsKt.convertMapToFnv1aHash(map, null);
    }

    public long getFromDate() {
        return fromDate;
    }

    public long getToDate() {
        return toDate;
    }
}
