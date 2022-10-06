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

package com.adobe.marketing.mobile.identity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventCoder;
import com.adobe.marketing.mobile.services.DataEntity;

import org.json.JSONException;
import org.json.JSONObject;

final class IdentityHit {
    private static final String URL = "URL";
    private static final String EVENT = "EVENT";
    private String url;
    private Event event;

    IdentityHit(@NonNull String url, @NonNull Event event) {
        this.url = url;
        this.event = event;
    }

    String getUrl() {
        return url;
    }

    Event getEvent() {
        return event;
    }

    @Nullable
    DataEntity toDataEntity() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(URL, this.url);
            jsonObject.put(EVENT, EventCoder.encode(this.event));
            return new DataEntity(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    static IdentityHit fromDataEntity(DataEntity dataEntity) {
        if (dataEntity == null) {
            return null;
        }

        try {
            String json = dataEntity.getData();
            JSONObject jsonObject = new JSONObject(json);
            String url = jsonObject.getString(URL);
            Event event = EventCoder.decode(jsonObject.getString(EVENT));
            return new IdentityHit(url, event);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}