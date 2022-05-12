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
