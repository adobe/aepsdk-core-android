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

class LifecycleDispatcherResponseContent extends ModuleEventDispatcher<LifecycleExtension> {
	/**
	 * Dispatcher constructor for Response Content events from the LifecycleExtension
	 *
	 * @param hub    {@code EventHub} that will be used for dispatching events
	 * @param extension parent {@code Module} that owns this dispatcher.
	 */
	LifecycleDispatcherResponseContent(final EventHub hub, final LifecycleExtension extension) {
		super(hub, extension);
	}

	/**
	 * Dispatches a lifecycle response event onto the EventHub containing the session info as event data
	 * @param startTimestampInSeconds 			the current session start timestamp in seconds
	 * @param contextData					current lifecycle context data
	 * @param previousSessionStartInSeconds	previous session start timestamp in seconds
	 * @param previousSessionPauseInSeconds	previous session pause timestamp in seconds
	 */
	void dispatchSessionStart(final long startTimestampInSeconds,
							  final Map<String, String> contextData,
							  final long previousSessionStartInSeconds,
							  final long previousSessionPauseInSeconds) {
		EventData eventData = new EventData()
		.putStringMap(LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_CONTEXT_DATA, contextData)
		.putString(LifecycleConstants.EventDataKeys.Lifecycle.SESSION_EVENT,
				   LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_START)
		.putLong(LifecycleConstants.EventDataKeys.Lifecycle.SESSION_START_TIMESTAMP, startTimestampInSeconds)
		.putLong(LifecycleConstants.EventDataKeys.Lifecycle.MAX_SESSION_LENGTH, LifecycleConstants.MAX_SESSION_LENGTH_SECONDS)
		.putLong(LifecycleConstants.EventDataKeys.Lifecycle.PREVIOUS_SESSION_START_TIMESTAMP, previousSessionStartInSeconds)
		.putLong(LifecycleConstants.EventDataKeys.Lifecycle.PREVIOUS_SESSION_PAUSE_TIMESTAMP, previousSessionPauseInSeconds);

		Event lifecycleStartEvent = new Event.Builder(LifecycleConstants.EventName.LIFECYCLE_START_EVENT,
				EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT).setData(eventData).build();

		dispatch(lifecycleStartEvent);
	}
}
