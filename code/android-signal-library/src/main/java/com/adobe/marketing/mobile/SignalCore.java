/* ************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2017 Adobe Systems Incorporated
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
 **************************************************************************/

package com.adobe.marketing.mobile;

import java.util.Map;

/**
 * Signal Core code
 */
class SignalCore {

	private static final String LOG_TAG = SignalCore.class.getSimpleName();

	EventHub eventHub;

	SignalCore(final EventHub eventHub, final ModuleDetails moduleDetails) {
		if (eventHub == null) {
			Log.debug(LOG_TAG, "%s (EventHub) while initializing Signal Core", Log.UNEXPECTED_NULL_VALUE);
			return;
		}

		this.eventHub = eventHub;

		try {
			eventHub.registerModule(SignalExtension.class, moduleDetails);
			Log.trace(LOG_TAG, "Registered %s extension", SignalExtension.class.getSimpleName());
		} catch (InvalidModuleException e) {
			Log.debug(LOG_TAG, "Failed to register %s module (%s)", SignalExtension.class.getSimpleName(), e);
		}
	}

	/**
	 * Create collect PII event, which is listened by Rules Engine module to determine if the data matches any PII request.
	 *
	 * @param data the PII data to be collected, which will be used in Rules Engine comparison and request token replacement.
	 */
	void collectPii(final Map<String, String> data) {
		if (data == null || data.isEmpty()) {
			Log.debug(LOG_TAG, "Could not trigger PII, the data is null or empty.");
			return;
		}

		final EventData eventData = new EventData()
		.putStringMap(SignalConstants.EventDataKeys.Signal.SIGNAL_CONTEXT_DATA, data);
		eventHub.dispatch(new Event.Builder("CollectPII", EventType.SIGNAL,
											EventSource.REQUEST_CONTENT).setData(eventData).build());
	}


}
