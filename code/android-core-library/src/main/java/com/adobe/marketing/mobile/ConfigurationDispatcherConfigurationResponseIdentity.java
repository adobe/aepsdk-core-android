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

class ConfigurationDispatcherConfigurationResponseIdentity extends ModuleEventDispatcher<ConfigurationExtension> {

	/**
	 * Returns an instance of a ConfigurationDispatcherConfigurationResponseIdentity
	 *
	 * @param eventHub (required) an EventHub reference which should be used by the dispatcher
	 * @param module   configuration module
	 */
	ConfigurationDispatcherConfigurationResponseIdentity(final EventHub eventHub, final ConfigurationExtension module) {
		super(eventHub, module);
	}

	/**
	 * Dispatches {@code EventType.CONFIGURATION}, {@code EventSource.RESPONSE_IDENTITY} event into the {@code EventHub}
	 * with {@code allIdentitiesJson} {@code String} for the given {@code pairId}.
	 *
	 * @param pairId A unique pairing id for one-time listener
	 * @param sdkIdentitiesJson A Json {@link String} containing all the identities
	 */
	void dispatchAllIdentities(final String sdkIdentitiesJson, final String pairId) {
		EventData eventData = new EventData();
		eventData.putString(ConfigurationConstants.EventDataKeys.Configuration.CONFIGURATION_RESPONSE_IDENTITY_ALL_IDENTIFIERS,
							sdkIdentitiesJson);
		final Event event = new Event.Builder("Configuration Response Identity", EventType.CONFIGURATION,
											  EventSource.RESPONSE_IDENTITY).setData(eventData).setPairID(pairId).build();
		super.dispatch(event);
	}

}

