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

package com.adobe.marketing.mobile.internal.util;

import androidx.annotation.NonNull;
import com.adobe.marketing.mobile.VisitorID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Utility class for serializing/deserializing {@link VisitorID} objects. */
public class VisitorIDSerializer {

    private static final String ID = "ID";
    private static final String ID_ORIGIN = "ID_ORIGIN";
    private static final String ID_TYPE = "ID_TYPE";
    private static final String STATE = "STATE";

    /**
     * Serializes the given {@link VisitorID} to a {@link Map}.
     *
     * @param visitorID {@link VisitorID} instance to serialize
     * @return a {@link Map} representing {@link VisitorID} properties
     */
    public static Map<String, Object> convertVisitorId(@NonNull final VisitorID visitorID) {
        Map<String, Object> data = new HashMap<>();
        data.put(ID, visitorID.getId());
        data.put(ID_ORIGIN, visitorID.getIdOrigin());
        data.put(ID_TYPE, visitorID.getIdType());
        data.put(STATE, visitorID.getAuthenticationState().getValue());
        return data;
    }

    /**
     * Serializes the given {@link VisitorID} list to a {@link Map} list.
     *
     * @param visitorIDList a list of {@link VisitorID} instances to serialize
     * @return a list of {@link Map} representing the given {@link VisitorID} properties
     */
    public static List<Map<String, Object>> convertVisitorIds(
            @NonNull final List<VisitorID> visitorIDList) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (VisitorID vId : visitorIDList) {
            if (vId != null) {
                data.add(VisitorIDSerializer.convertVisitorId(vId));
            }
        }
        return data;
    }

    /**
     * Deserializes the given map {@link List} to a list of {@link VisitorID}.
     *
     * @param data a list of {@link Map} to deserialize
     * @return a list of {@link VisitorID} that was deserialized from the property {@link Map}
     */
    public static List<VisitorID> convertToVisitorIds(@NonNull final List<Map> data) {
        List<VisitorID> visitorIDList = new ArrayList<>();
        for (Map item : data) {
            if (item != null) {
                String id = String.valueOf(item.get(ID));
                String origin = String.valueOf(item.get(ID_ORIGIN));
                String type = String.valueOf(item.get(ID_TYPE));
                int state = Integer.parseInt(String.valueOf(item.get(STATE)));
                visitorIDList.add(
                        new VisitorID(
                                origin,
                                type,
                                id,
                                VisitorID.AuthenticationState.fromInteger(state)));
            }
        }
        return visitorIDList;
    }
}
