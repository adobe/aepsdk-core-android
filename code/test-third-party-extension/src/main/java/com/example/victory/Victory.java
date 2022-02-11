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
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;

import java.util.HashMap;
import java.util.Map;

public class Victory {
	private static final String TAG = "Victory API";
	private static final String EVENT_TYPE_VICTORY = "com.example.victory.custom";
	private static final String EVENT_SOURCE_VICTORY_PAIRED_REQUEST = "com.example.victory.pairedrequest";
	private static final String EVENT_SOURCE_VICTORY_REQUEST = "com.example.victory.request";
	private static final String CONTEXT_DATA = "victorycontextdata";
	private static final String NO_EVENTS_PROCESSED = "eventsprocessed";
	private static final String UNREGISTER_EXTENSION = "unregisterextension";

	public static void registerExtension() {
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError errorCode) {
				MobileCore.log(LoggingMode.WARNING, TAG, String.format("An error occurred while registering extension, %s",
							   errorCode.getErrorName()));
			}
		};
		MobileCore.registerExtension(VictoryExtension.class, errorCallback);
	}

	public static void unregisterExtension() {
		Map<String, Object> eventData = new HashMap<String, Object>();
		eventData.put(UNREGISTER_EXTENSION, true);
		Event event = new Event.Builder("UnregisterEvent", EVENT_TYPE_VICTORY,
										EVENT_SOURCE_VICTORY_REQUEST).setEventData(eventData).build();
		MobileCore.dispatchEvent(event, null);
	}

	public static void setProfile(final Map<String, String> profile) {
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError errorCode) {
				MobileCore.log(LoggingMode.WARNING, TAG, String.format("An error occurred when dispatching event for setProfile, %s",
							   errorCode.getErrorName()));
			}
		};

		Map<String, Object> eventData = new HashMap<String, Object>();
		eventData.put(CONTEXT_DATA, profile);
		Event event = new Event.Builder("SetProfileEvent", EVENT_TYPE_VICTORY,
										EVENT_SOURCE_VICTORY_REQUEST).setEventData(eventData).build();
		MobileCore.dispatchEvent(event, errorCallback);
	}

	public static void getNoEventsProcessed(final AdobeCallback<Long> callback) {
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError errorCode) {
				MobileCore.log(LoggingMode.WARNING, TAG,
							   String.format("An error occurred when dispatching event for getNoEventsProcessed, %s",
											 errorCode.getErrorName()));
			}
		};

		AdobeCallback<Event> responseEventCallback = new AdobeCallback<Event>() {
			@Override
			public void call(final Event event) {
				Map<String, Object> eventData = event.getEventData();

				if (callback != null) {
					Long eventsProcessed = eventData != null ? (Long) eventData.get(NO_EVENTS_PROCESSED) : 0;
					callback.call(eventsProcessed);
				}

				MobileCore.log(LoggingMode.DEBUG,
							   TAG,
							   String.format("Response event received, type %s, source %s and data %s",
											 event.getType(), event.getSource(), eventData));

			}
		};
		Event event = new Event.Builder("VictoryPairedRequest", EVENT_TYPE_VICTORY,
										EVENT_SOURCE_VICTORY_PAIRED_REQUEST).build();
		MobileCore.dispatchEventWithResponseCallback(event, responseEventCallback, errorCallback);
	}

	/**
	 * MobileCore.getApplication() usage example
	 */
	public static void gotoActivity(final String activityName) {
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(ExtensionError errorCode) {
				MobileCore.log(LoggingMode.WARNING, TAG,
							   "An error occurred when dispatching event in gotActivity: " + errorCode.getErrorName());
			}
		};

		Map<String, Object> eventData = new HashMap<>();
		eventData.put(VictoryConstants.GOTO_ACTIVITY_NAME, activityName);
		Event event = new Event.Builder("Goto Activity",
										VictoryConstants.EVENT_TYPE_VICTORY,
										VictoryConstants.EVENT_SOURCE_VICTORY_REQUEST)
		.setEventData(eventData)
		.build();
		MobileCore.dispatchEvent(event, errorCallback);
	}

	/**
	 * getApi().getSharedEventState usage example
	 */
	public static void printLatestConfig() {
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(ExtensionError errorCode) {
				MobileCore.log(LoggingMode.WARNING, TAG,
							   "An error occurred when dispatching request event for printLatestConfig: " + errorCode.getErrorName());
			}
		};

		Map<String, Object> eventData = new HashMap<>();
		eventData.put(VictoryConstants.PRINT_LATEST_CONFIG, true);
		Event event = new Event.Builder("Print latest config request",
										VictoryConstants.EVENT_TYPE_VICTORY,
										VictoryConstants.EVENT_SOURCE_VICTORY_REQUEST)
		.setEventData(eventData)
		.build();
		MobileCore.dispatchEvent(event, errorCallback);
	}
}
