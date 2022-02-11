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
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.ExtensionUnexpectedError;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class VictoryExtension extends Extension {
	private final Object executorMutex = new Object();
	private ExecutorService executor;
	private ConcurrentLinkedQueue<Event> unprocessedEvents;
	private long noProcessedEvents;
	private boolean onUnexpectedErrorWasCalled;

	protected VictoryExtension(ExtensionApi extensionApi) {
		super(extensionApi);
		unprocessedEvents = new ConcurrentLinkedQueue<Event>();
		noProcessedEvents = 0;
		registerListeners();
	}

	private void registerListeners() {
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
				MobileCore.log(LoggingMode.ERROR, getName(), String.format("Failed to register listener, error: %s",
							   extensionError.getErrorName()));
			}
		};
		getApi().registerEventListener(VictoryConstants.EVENT_TYPE_VICTORY,
									   VictoryConstants.EVENT_SOURCE_VICTORY_REQUEST, VictoryRequestListener.class, errorCallback);
		getApi().registerEventListener(VictoryConstants.EVENT_TYPE_HUB, VictoryConstants.EVENT_SOURCE_SHARED_STATE,
									   VictorySharedStateListener.class, errorCallback);
		getApi().registerEventListener(VictoryConstants.EVENT_TYPE_VICTORY,
									   VictoryConstants.EVENT_SOURCE_VICTORY_PAIRED_REQUEST,
									   VictoryOneTimeListener.class, errorCallback);
		getApi().registerWildcardListener(VictoryWildcardListener.class, errorCallback);
	}

	@Override
	protected String getName() {
		return "VictoryExtension";
	}

	@Override
	protected void onUnregistered() {
		processEvents();

		noProcessedEvents = 0;
		unprocessedEvents.clear();

		MobileCore.log(LoggingMode.DEBUG, getName(), "Extension unregistered successfully");
	}

	@Override
	protected void onUnexpectedError(final ExtensionUnexpectedError extensionUnexpectedError) {
		super.onUnexpectedError(extensionUnexpectedError);
		onUnexpectedErrorWasCalled = true;
	}

	void handleEvent(final Event event) {
		getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				noProcessedEvents++;
				Map<String, Object> eventData = event.getEventData();

				if (eventData != null) {
					MobileCore.log(LoggingMode.DEBUG,
								   getName(),
								   String.format("Started processing new event of type %s and source %s with data: %s",
												 event.getType(),
												 event.getSource(), eventData.toString()));
				}
			}
		});
	}

	void addEvent(final Event event) {
		getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				unprocessedEvents.add(event);
			}
		});
	}

	void processEvents() {
		getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				while (!unprocessedEvents.isEmpty()) {
					Event event = unprocessedEvents.peek();
					Map<String, Object> configSharedState = getApi().getSharedEventState(VictoryConstants.CONFIGURATION_SHARED_STATE, event,
															null);

					if (configSharedState == null) {
						MobileCore.log(LoggingMode.DEBUG, getName(), "Configuration shared state was null, cannot process event now");
						break;
					}

					Map<String, Object> eventData = event.getEventData();

					if (eventData != null) {
						MobileCore.log(LoggingMode.DEBUG, getName(), String.format("Processing user data %s",
									   eventData.get(VictoryConstants.CONTEXT_DATA)));

						if (eventData.containsKey(VictoryConstants.PRINT_LATEST_CONFIG)) {
							printLatestConfigSharedState();
						}
					}

					unprocessedEvents.poll();

					// call after removing event from queue
					if (eventData != null && eventData.containsKey(VictoryConstants.GOTO_ACTIVITY_NAME)) {
						gotoActivity(event);
					}
				}
			}
		});
	}

	void processNoProcessedEventsRequest(final Event event) {
		getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
					@Override
					public void error(final ExtensionError errorCode) {
						MobileCore.log(LoggingMode.WARNING, getName(), String.format("An error occurred when dispatching event, %s",
									   errorCode.getErrorName()));
					}
				};
				Map<String, Object> eventData = new HashMap<String, Object>();
				eventData.put(VictoryConstants.NO_EVENTS_PROCESSED, noProcessedEvents);
				Event response = new Event.Builder("VictoryResponsePaired", VictoryConstants.EVENT_TYPE_VICTORY,
												   VictoryConstants.EVENT_SOURCE_VICTORY_PAIRED_RESPONSE).setEventData(eventData).build();
				MobileCore.dispatchResponseEvent(response, event, errorCallback);
			}
		});
	}

	private void gotoActivity(final Event event) {
		Application app = MobileCore.getApplication();

		if (app == null) {
			MobileCore.log(LoggingMode.WARNING, getName(), "Application from MobileCore is null!");
			return;
		}

		Map<String, Object> data = event.getEventData();

		if (data == null || !data.containsKey(VictoryConstants.GOTO_ACTIVITY_NAME)) {
			MobileCore.log(LoggingMode.WARNING, getName(), "Cannot goto Activity as event data does not contain activity name.");
			return;
		}

		String name = (String)data.get(VictoryConstants.GOTO_ACTIVITY_NAME);

		try {
			Class activityClass = Class.forName(name);
			Intent intent = new Intent(app, activityClass);
			app.startActivity(intent);

		} catch (ClassNotFoundException e) {
			MobileCore.log(LoggingMode.ERROR, getName(), "Failed to find class with name " + name);
			return;
		}
	}

	private void printLatestConfigSharedState() {
		Map<String, Object> configSharedState = getApi().getSharedEventState(VictoryConstants.CONFIGURATION_SHARED_STATE, null,
		new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(ExtensionError extensionError) {
				MobileCore.log(LoggingMode.WARNING, getName(),
							   "Failed to read latest config shared state (" + extensionError.getErrorName() + ")");
			}
		});

		if (configSharedState == null) {
			MobileCore.log(LoggingMode.DEBUG, getName(), "Latest config shared state is PENDING");
		} else {
			try {
				JSONObject json = new JSONObject(configSharedState);
				MobileCore.log(LoggingMode.DEBUG, getName(), "Latest config shared state is: \n" + json.toString(4));
			} catch (JSONException e) {
				MobileCore.log(LoggingMode.DEBUG, getName(),
							   "Failed to read latest config shared state, invalid format " + e.getLocalizedMessage());
			}

		}
	}

	private ExecutorService getExecutor() {
		synchronized (executorMutex) {
			if (executor == null) {
				executor = Executors.newSingleThreadExecutor();
			}

			return executor;
		}
	}
}
