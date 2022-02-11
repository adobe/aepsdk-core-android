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
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

interface Callback {
	public String call();
}

/**
 * Class that defines a third party extension {@link TestableExtension} that extends {@link Extension} class of the Adobe Experience Platform SDK.
 *
 * This class provides the following features to write automated tests for a third party extension
 * that registers all the required listeners in the constructor
 *
 * 1. ability  to register multiple listeners with different types as sources
 * 2. ability  to register wildcard listeners
 * 3. ability to count how many times the constructor was called
 * 4. has provisions for unregister an extension, getting name/version
 * 5. has provisions for checking how many times and with what error code the onUnregistered / onUnexpectedError was called.
 *
 * @author Adobe
 * @version 5.0
 */

public class TestableExtension extends Extension {

	private static final String LOG_TAG = TestableExtension.class.getSimpleName();
	private final Object executorMutex = new Object();
	private ExecutorService executor;
	private ConcurrentLinkedQueue<Event> unprocessedEvents;
	private long noProcessedEvents;
	private boolean onUnexpectedErrorWasCalled;
	private static Map<ListenerType, ExtensionError> listenerRegistrationStatus;
	private static List<ListenerType> listOfListeners;
	private Extension extensionReference;
	static ExtensionUnexpectedError extensionUnexpectedError;
	static String createdExtensionName;
	static String createdExtensionVersion;
	static String confirmExtensionUnregisteredCall;
	private static int i = 0;
	private String extensionName;
	private static Callback nameCallback = new Callback() {
		public String call() {
			return "extensionName" + i++;
		};
	};

	public static void setNameCallback(final Callback callback) {
		nameCallback = callback;
	}

	protected TestableExtension(ExtensionApi extensionApi) {
		super(extensionApi);
		this.extensionName = nameCallback.call();
		extensionUnexpectedError = null;
		unprocessedEvents = new ConcurrentLinkedQueue<Event>();
		noProcessedEvents = 0;
		listenerRegistrationStatus = registerListeners();
		createdExtensionName = this.getName();
		createdExtensionVersion = this.getVersion();
	}

	public static void setListOfListeners(List<ListenerType> paramlistOfListeners) {
		listOfListeners = paramlistOfListeners;
	}


	public static ExtensionUnexpectedError getExtensionUnexpectedError() {
		return (extensionUnexpectedError);
	}

	public static Map<ListenerType, ExtensionError> getListenerRegistrationStatus() {
		return (listenerRegistrationStatus);
	}

	private Map<ListenerType, ExtensionError> registerListeners() {
		Map<ListenerType, ExtensionError> registeredListeners = new HashMap<ListenerType, ExtensionError>();

		for (ListenerType listenerType : listOfListeners) {
			final ExtensionError[] extensionError = new ExtensionError[1];
			ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
				@Override
				public void error(final ExtensionError error) {
					extensionError[0] = error;
				}
			};

			if (listenerType.eventType == null || listenerType.eventSource == null) {
				getApi().registerEventListener(listenerType.eventType, listenerType.eventSource, TestableListener.class, errorCallback);
				registeredListeners.put(listenerType, extensionError[0]);
			} else if (listenerType.eventType == "com.adobe.eventtype._wildcard_") {
				getApi().registerWildcardListener(TestableListener.class, errorCallback);
			} else {
				getApi().registerEventListener(listenerType.eventType, listenerType.eventSource, TestableListener.class, errorCallback);
			}

			registeredListeners.put(listenerType, extensionError[0]);
		}

		return registeredListeners;
	}


	@Override
	protected String getName() {
		return this.extensionName;
	}

	@Override
	protected String getVersion() {
		return (this.extensionName == "ExtensionWithNullVersion") ? null : "1.0.0";
	}

	@Override
	protected void onUnregistered() {
		confirmExtensionUnregisteredCall = "ConfirmedByTestableExtension";
		processEvents();
		noProcessedEvents = 0;
		unprocessedEvents.clear();

	}

	@Override
	protected void onUnexpectedError(final ExtensionUnexpectedError extensionUnexpectedError) {
		super.onUnexpectedError(extensionUnexpectedError);
		onUnexpectedErrorWasCalled = true;
		TestableExtension.extensionUnexpectedError = extensionUnexpectedError;
	}

	void handleEvent(final Event event) {
		getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				noProcessedEvents++;
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
					Map<String, Object> configSharedState = getApi().getSharedEventState(TestableConstants.CONFIGURATION_SHARED_STATE,
															event,
															null);

					if (configSharedState == null) {
						break;
					}

					unprocessedEvents.poll();
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
						Log.debug(LOG_TAG, String.format("An error occurred when dispatching event, %s", errorCode.getErrorCode()));
					}
				};
				Map<String, Object> eventData = new HashMap<String, Object>();
				eventData.put(TestableConstants.PROCESSED_EVENTS_NO, noProcessedEvents);
				Event response = new Event.Builder("ResponsePaired", TestableConstants.EVENT_TYPE_TESTABLE,
												   TestableConstants.EVENT_SOURCE_TESTABLE_PAIRED_RESPONSE).setEventData(eventData).build();
				MobileCore.dispatchResponseEvent(response, event, errorCallback);
			}
		});
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
