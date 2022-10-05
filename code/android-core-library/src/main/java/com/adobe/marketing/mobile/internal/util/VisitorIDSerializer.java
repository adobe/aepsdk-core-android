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

import com.adobe.marketing.mobile.VisitorID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisitorIDSerializer {
    private static final String ID = "ID";
    private static final String ID_ORIGIN = "ID_ORIGIN";
    private static final String ID_TYPE = "ID_TYPE";
    private static final String STATE = "STATE";

    public static Map<String, Object> convertVisitorId(VisitorID visitorID) {
        Map<String, Object> data = new HashMap<>();
        data.put(ID, visitorID.getId());
        data.put(ID_ORIGIN, visitorID.getIdOrigin());
        data.put(ID_TYPE, visitorID.getIdType());
        data.put(STATE, visitorID.getAuthenticationState().getValue());
        return data;
    }

    public static List<VisitorID> convertToVisitorIds(List<Map> data) {
        List<VisitorID> visitorIDList = new ArrayList<>();
        for (Map item : data) {
            String id = String.valueOf(item.get(ID));
            String origin = String.valueOf(item.get(ID_ORIGIN));
            String type = String.valueOf(item.get(ID_TYPE));
            int state = Integer.parseInt(String.valueOf(item.get(STATE)));
            visitorIDList.add(new VisitorID(origin, type, id, VisitorID.AuthenticationState.fromInteger(state)));
        }
        return visitorIDList;
    }

}