/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 * *************************************************************************/

package com.adobe.marketing.mobile;

import java.util.Map;

class LifecycleInternal {

	private static final String CONFIGURATION_MODULE_NAME = "com.adobe.module.configuration";
	private static final String LIFECYCLE_MODULE_NAME     = "com.adobe.module.lifecycle";
	private EventHub eventHub;

	LifecycleInternal(EventHub eventHub) {
		this.eventHub = eventHub;
	}

	void startLifecycle(long startTimestampMillis, Map<String, String> contextData) {
		EventData eventData = new EventData();
		eventData.putString(LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_ACTION_KEY,
							LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_START);
		eventData.putStringMap(LifecycleConstants.EventDataKeys.Lifecycle.ADDITIONAL_CONTEXT_DATA, contextData);

		Event event = new Event.Builder("LifecycleResume", EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(startTimestampMillis)
		.setData(eventData)
		.build();

		eventHub.dispatch(event);
	}

	void startLifecycle(Map<String, String> contextData) {
		startLifecycle(System.currentTimeMillis(), contextData);
	}

	void pauseLifecycle(long pauseTimestampMillis) {
		EventData eventData = new EventData();
		eventData.putString(LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_ACTION_KEY,
							LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_PAUSE);

		Event event = new Event.Builder("LifecyclePause", EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(pauseTimestampMillis)
		.setData(eventData)
		.build();

		eventHub.dispatch(event);
	}

	void pauseLifecycle() {
		pauseLifecycle(System.currentTimeMillis());
	}

	void configureLifecycle(int sessionTimeoutSeconds) {
		try {
			eventHub.createSharedState(new Module(CONFIGURATION_MODULE_NAME, eventHub) {
			}, 0, new EventData().putInteger("lifecycle.sessionTimeout", sessionTimeoutSeconds));
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	void setAdvertisingIdentifier(String advertisingIdentifier) {
		try {
			eventHub.createSharedState(new Module(CONFIGURATION_MODULE_NAME, eventHub) {
			}, 0, new EventData()
			.putString("advertisingIdentifier", advertisingIdentifier));
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	void updateContextData(final Map<String, String> contextData) {
		EventData eventData = new EventData().putStringMap(LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_CONTEXT_DATA,
				contextData);
		Event event = new Event.Builder("LifecycleUpdateContextData", EventType.LIFECYCLE, EventSource.REQUEST_PROFILE)
		.setData(eventData)
		.build();
		eventHub.dispatch(event);
	}

	void dispatchEvent(Event event) {
		eventHub.dispatch(event);
	}

	EventData getLatestLifecycleSharedState() {
		Event event = new Event.Builder("test", EventType.LIFECYCLE, EventSource.NONE).setEventNumber(100).build();
		return eventHub.getSharedEventState(LIFECYCLE_MODULE_NAME, event, new Module(LIFECYCLE_MODULE_NAME, eventHub) {});
	}
}
