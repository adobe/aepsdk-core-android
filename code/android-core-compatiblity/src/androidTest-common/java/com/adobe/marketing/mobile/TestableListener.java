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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Class that defines a custom listener {@link TestableListener} that extends {@link ExtensionListener} class of the Adobe Experience Platform SDK.
 *
 * This class provides the following features to write automated tests for a third party extension
 * that registers all the required listeners in the constructor
 *
 * 1. ability  to count how many times the constructor was called
 * 2. ability  to count how many times the hear method was called and what event was received
 * 3. uses static countdown latch to wait for all the listeners to be registered before dispatching events
 *
 * @author Adobe
 * @version 5.0
 */



class TestableListener extends ExtensionListener {

	private static final String LOG_TAG = TestableListener.class.getSimpleName();
	private List<Event> receivedEvents;
	static String confirmListenerUnregisteredCall;
	private boolean doDispatchResponseEvent = false;
	private boolean doDispatchResponseEventWithNullEvent = false;
	private boolean doDispatchAnEvent = false;
	private ExtensionError extensionError;
	protected TestableListener(final ExtensionApi extension, final String type, final String source) {
		super(extension, type, source);
		receivedEvents = new ArrayList<Event>();
	}

	@Override
	public void hear(Event event) {
		receivedEvents.add(event);
		extensionError = null;
		Log.debug(LOG_TAG, String.format("Event Heard By The Extension : %s with type %s and source %s with Thread %s",
										 this.getParentExtension().getName(), event.getType(), event.getSource(), Thread.currentThread().getName()));
		Map<String, Object> eventData = event.getEventData();
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError errorCode) {
				Log.debug(LOG_TAG, String.format("Dispatch  event failed with an error , %s", errorCode.getErrorCode()));
				extensionError = errorCode;
			}
		};


		if (eventData != null && eventData.containsKey(TestableConstants.UNREGISTER_EXTENSION)) {
			getParentExtension().getApi().unregisterExtension();
		} else {
			getParentExtension().addEvent(event);
			getParentExtension().processEvents();
		}

		if (this.getParentExtension().getName().equals("BusyWorkExtension29")
				&& event.getType().equals("com.adobe.eventtype.busywork")) {
			try {
				Thread.currentThread().sleep(75);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (doDispatchResponseEvent) {
			// Setup and Dispatch a Response Event
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("ResponseKey1", "ResponseData1");
			data.put("ResponseKey2", "ResponseData2");
			data.put("ResponseKey3", "ResponseData3");
			Event responseEvent = new Event.Builder("response event", "com.adobe.eventType.pairedresponse",
													"com.example.testable.pairedrequest").setEventData(data).build();
			MobileCore.dispatchResponseEvent(responseEvent, event, errorCallback);
			return;
		}

		if (doDispatchResponseEventWithNullEvent) {
			// Setup and Dispatch a Response Event with Null responseEvent.
			Event responseEvent = null;
			MobileCore.dispatchResponseEvent(responseEvent, event, errorCallback);
			return;
		}

		if (doDispatchAnEvent) {
			// Setup and Dispatch An Event
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("EventKey1", "EventData1");
			data.put("EventKey2", "EventData2");
			data.put("EventKey3", "EventData3");
			List<ListenerType> listenerTypes = new ArrayList<ListenerType>();
			listenerTypes.add(new ListenerType("com.example.testable.custom", "com.example.testable.request"));
			Event eventToDispatch = new Event.Builder("dispatchEvent", listenerTypes.get(0).eventType,
					listenerTypes.get(0).eventSource).setEventData(data).build();
			MobileCore.dispatchEvent(eventToDispatch, errorCallback);
			return;
		}

	}

	public void setDispatchBehavior(String type) {
		if (type == "doDispatchResponseEvent") {
			this.doDispatchResponseEvent = true;
			return;
		}

		if (type == "doDispatchResponseEventWithNullEvent") {
			this.doDispatchResponseEventWithNullEvent = true;
			return;
		}

		if (type == "doDispatchAnEvent") {
			this.doDispatchAnEvent = true;
			return;
		}
	}

	@Override
	protected TestableExtension getParentExtension() {
		return (TestableExtension)super.getParentExtension();
	}

	@Override
	public void onUnregistered() {
		confirmListenerUnregisteredCall = "ConfirmedByTestableListener";

	}

	public List<Event> getReceivedEvents() {
		return this.receivedEvents;
	}

	public void setExtensionError(ExtensionError error) {
		this.extensionError = error;

	}

	public  ExtensionError getExtensionError() {
		return this.extensionError;

	}

}
