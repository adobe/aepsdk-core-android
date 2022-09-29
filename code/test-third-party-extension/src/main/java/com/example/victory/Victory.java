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

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.AdobeCallbackWithError;
import com.adobe.marketing.mobile.AdobeError;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;

import java.util.HashMap;
import java.util.Map;

public class Victory {
	private static final String TAG = "Victory API";

	public static final Class<? extends Extension> Extension = VictoryExtension.class;

	public static void unregisterExtension() {
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(VictoryConstants.UNREGISTER_EXTENSION, true);
		Event event = new Event.Builder("UnregisterEvent", VictoryConstants.EVENT_TYPE_VICTORY,
										VictoryConstants.EVENT_SOURCE_VICTORY_REQUEST).setEventData(eventData).build();
		MobileCore.dispatchEvent(event);
	}

	public static void setProfile(final Map<String, String> profile) {
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(VictoryConstants.CONTEXT_DATA, profile);
		Event event = new Event.Builder("SetProfileEvent", VictoryConstants.EVENT_TYPE_VICTORY,
										VictoryConstants.EVENT_SOURCE_VICTORY_REQUEST).setEventData(eventData).build();
		MobileCore.dispatchEvent(event);
	}

	/**
	 * Response event listeners usage example
	 */
	public static void getNoEventsProcessed(final AdobeCallback<Long> callback) {
		AdobeCallbackWithError<Event> responseEventCallback = new AdobeCallbackWithError<Event>() {
			@Override
			public void fail(AdobeError error) {
				Log.warning(
						VictoryConstants.EXTENSION_NAME,
						TAG,
						String.format("getNoEventsProcessed failed with error %s", error.getErrorName()));
			}

			@Override
			public void call(final Event event) {
				if (callback != null) {
					Long eventsProcessed = DataReader.optLong(event.getEventData(), VictoryConstants.NO_EVENTS_PROCESSED, 0);
					callback.call(eventsProcessed);
				}

				Log.debug(
						VictoryConstants.EXTENSION_NAME,
						TAG,
						String.format("Response event received, type %s, source %s and data %s",
								event.getType(), event.getSource(), event.getEventData()));
			}
		};

		Event event = new Event.Builder("VictoryPairedRequest", VictoryConstants.EVENT_TYPE_VICTORY,
										VictoryConstants.EVENT_SOURCE_VICTORY_PAIRED_REQUEST).build();
		MobileCore.dispatchEventWithResponseCallback(event, VictoryConstants.API_TIMEOUT_MS, responseEventCallback);
	}

	/**
	 * MobileCore.getApplication() usage example
	 */
	public static void gotoActivity(final String activityName) {
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(VictoryConstants.GOTO_ACTIVITY_NAME, activityName);
		Event event = new Event.Builder("Goto Activity",
										VictoryConstants.EVENT_TYPE_VICTORY,
										VictoryConstants.EVENT_SOURCE_VICTORY_REQUEST)
		.setEventData(eventData)
		.build();
		MobileCore.dispatchEvent(event);
	}

	/**
	 * getApi().getSharedEventState usage example
	 */
	public static void printLatestConfig() {
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(VictoryConstants.PRINT_LATEST_CONFIG, true);
		Event event = new Event.Builder("Print latest config request",
										VictoryConstants.EVENT_TYPE_VICTORY,
										VictoryConstants.EVENT_SOURCE_VICTORY_REQUEST)
		.setEventData(eventData)
		.build();
		MobileCore.dispatchEvent(event);
	}
}
