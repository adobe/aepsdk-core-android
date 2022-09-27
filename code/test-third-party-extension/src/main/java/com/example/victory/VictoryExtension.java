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

package com.example.victory;

import android.app.Application;
import android.content.Intent;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.SharedStateResolution;
import com.adobe.marketing.mobile.SharedStateResult;
import com.adobe.marketing.mobile.SharedStateStatus;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

class VictoryExtension extends Extension {
    private static final String LOG_TAG = "VictoryExtension";

    private long noProcessedEvents;

    protected VictoryExtension(ExtensionApi extensionApi) {
        super(extensionApi);
        noProcessedEvents = 0;
    }

    @Override
    protected String getName() {
        return VictoryConstants.EXTENSION_NAME;
    }

    @Override
    protected String getFriendlyName() {
        return VictoryConstants.EXTENSION_FRIENDLY_NAME;
    }

    @Override
    protected String getVersion() {
        return VictoryConstants.EXTENSION_VERSION;
    }


    @Override
    protected void onRegistered() {
        super.onRegistered();
        registerListeners();
    }

    private void registerListeners() {
        getApi().registerEventListener(VictoryConstants.EVENT_TYPE_VICTORY, VictoryConstants.EVENT_SOURCE_VICTORY_REQUEST, this::handleVictoryRequest);
        getApi().registerEventListener(VictoryConstants.EVENT_TYPE_VICTORY,
                VictoryConstants.EVENT_SOURCE_VICTORY_PAIRED_REQUEST,
                this::handleNoProcessedEventsRequest);
        getApi().registerEventListener(EventType.WILDCARD, EventSource.WILDCARD, this::handleEvent);
    }

    @Override
    public boolean readyForEvent(Event event) {
        SharedStateResult res = getApi().getSharedState(VictoryConstants.CONFIGURATION_SHARED_STATE, event, true, SharedStateResolution.ANY);
        return res != null && res.getStatus() == SharedStateStatus.SET;
    }

    @Override
    protected void onUnregistered() {
        noProcessedEvents = 0;
        Log.debug(VictoryConstants.EXTENSION_NAME, LOG_TAG, "Extension unregistered successfully");
    }

    void handleEvent(final Event event) {
        noProcessedEvents++;
        Map<String, Object> eventData = event.getEventData();
        if (eventData != null) {
            Log.debug(VictoryConstants.EXTENSION_NAME, LOG_TAG,
                    String.format("Started processing new event of type %s and source %s with data: %s",
                            event.getType(),
                            event.getSource(), eventData));
        }
    }

    private void handleVictoryRequest(Event event) {
        Map<String, Object> eventData = event.getEventData();
        if (eventData != null) {
            Log.debug(VictoryConstants.EXTENSION_NAME, LOG_TAG, String.format("Processing user data %s",
                    eventData.get(VictoryConstants.CONTEXT_DATA)));

            if (eventData.containsKey(VictoryConstants.PRINT_LATEST_CONFIG)) {
                printLatestConfigSharedState();
            } else if (eventData.containsKey(VictoryConstants.GOTO_ACTIVITY_NAME)) {
                gotoActivity(event);
            } else if (eventData.containsKey(VictoryConstants.UNREGISTER_EXTENSION)) {
                getApi().unregisterExtension();
            }
        }
    }

    private void handleNoProcessedEventsRequest(final Event event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(VictoryConstants.NO_EVENTS_PROCESSED, noProcessedEvents);

        Event response = new Event.Builder("VictoryResponsePaired", VictoryConstants.EVENT_TYPE_VICTORY,
                VictoryConstants.EVENT_SOURCE_VICTORY_PAIRED_RESPONSE)
                .setEventData(eventData)
                .inResponseToEvent(event)
                .build();

        getApi().dispatch(response);
    }

    private void gotoActivity(final Event event) {
        Application app = MobileCore.getApplication();

        if (app == null) {
            Log.warning(VictoryConstants.EXTENSION_NAME, LOG_TAG,  "Application from MobileCore is null!");
            return;
        }

        String activityName = DataReader.optString(event.getEventData(), VictoryConstants.GOTO_ACTIVITY_NAME, null);
        if (activityName == null) {
            Log.warning(VictoryConstants.EXTENSION_NAME, LOG_TAG, "Cannot goto Activity as event data does not contain activity name.");
            return;
        }

        try {
            Class<?> activityClass = Class.forName(activityName);
            Intent intent = new Intent(app, activityClass);
            app.startActivity(intent);

        } catch (ClassNotFoundException e) {
            Log.error(VictoryConstants.EXTENSION_NAME, LOG_TAG,  getName(), "Failed to find class with name " + activityName);
        }
    }

    private void printLatestConfigSharedState() {
        SharedStateResult result = getApi().getSharedState(VictoryConstants.CONFIGURATION_SHARED_STATE, null, true, SharedStateResolution.ANY);
        if (result.getStatus() == SharedStateStatus.PENDING) {
            Log.debug(VictoryConstants.EXTENSION_NAME, LOG_TAG, "Latest config shared state is PENDING");
        } else {
            try {
                JSONObject json = new JSONObject(result.getValue());
                Log.debug(VictoryConstants.EXTENSION_NAME, LOG_TAG,  "Latest config shared state is: \n" + json.toString(4));
            } catch (JSONException e) {
                Log.debug(VictoryConstants.EXTENSION_NAME, LOG_TAG,
                        "Failed to read latest config shared state, invalid format " + e.getLocalizedMessage());
            }
        }
    }
}
