/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2021 Adobe Systems Incorporated
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

import java.util.HashMap;
import java.util.Map;

/**
 * LifecycleV2DispatcherApplicationState class
 *
 * The responsibility of LifecycleV2DispatcherApplicationState is to dispatch application launch and close events
 * with formatted xdm data and free form data
 */
class LifecycleV2DispatcherApplicationState extends ModuleEventDispatcher<LifecycleExtension> {
	static final String SELF_LOG_TAG = "LifecycleV2DispatcherApplicationState";

	/**
	 * Dispatcher constructor for Application launch and close events from the LifecycleExtension
	 *
	 * @param hub    {@code EventHub} that will be used for dispatching events
	 * @param extension parent {@code Module} that owns this dispatcher
	 */
	LifecycleV2DispatcherApplicationState(final EventHub hub, final LifecycleExtension extension) {
		super(hub, extension);
	}

	/**
	 * Dispatches a lifecycle application launch event onto the EventHub containing the session info as xdm event data
	 * @param xdm 	the current session start xdm data
	 * @param data	additional free-form context data
	 */
	void dispatchApplicationLaunch(final Map<String, Object> xdm,
								   final Map<String, String> data) {
		if (xdm == null || xdm.isEmpty()) {
			Log.trace(LifecycleConstants.LOG_TAG, "%s - Not dispatching application launch event as xdm data was null",
					  SELF_LOG_TAG);
			return;
		}

		Map<String, Object> launchEventData = new HashMap<String, Object>();
		launchEventData.put(LifecycleV2Constants.EventDataKeys.XDM, xdm);

		if (data != null && !data.isEmpty()) {
			launchEventData.put(LifecycleV2Constants.EventDataKeys.DATA, data);
		}

		Event lifecycleLaunchEvent = new Event.Builder(LifecycleV2Constants.EventName.APPLICATION_LAUNCH_EVENT,
				EventType.LIFECYCLE,
				EventSource.APPLICATION_LAUNCH).setEventData(launchEventData).build();
		dispatch(lifecycleLaunchEvent);
	}

	/**
	 * Dispatches a lifecycle application close event onto the EventHub containing the session info as xdm event data
	 * @param xdm 	the current session close xdm data
	 */
	void dispatchApplicationClose(final Map<String, Object> xdm) {
		if (xdm == null || xdm.isEmpty()) {
			Log.trace(LifecycleConstants.LOG_TAG, "%s - Not dispatching application close event as xdm data was null",
					  SELF_LOG_TAG);
			return;
		}

		Map<String, Object> closeEventData = new HashMap<String, Object>();
		closeEventData.put(LifecycleV2Constants.EventDataKeys.XDM, xdm);

		Event lifecycleCloseEvent = new Event.Builder(LifecycleV2Constants.EventName.APPLICATION_CLOSE_EVENT,
				EventType.LIFECYCLE,
				EventSource.APPLICATION_CLOSE).setEventData(closeEventData).build();
		dispatch(lifecycleCloseEvent);
	}

}
