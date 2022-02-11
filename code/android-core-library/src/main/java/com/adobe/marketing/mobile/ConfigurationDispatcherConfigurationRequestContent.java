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

class ConfigurationDispatcherConfigurationRequestContent extends ModuleEventDispatcher<ConfigurationExtension> {

	/**
	 * Returns an instance of a ConfigurationDispatcherConfigurationRequestContent
	 *
	 * @param eventHub (required) an EventHub reference which should be used by the dispatcher
	 * @param module   configuration module
	 */
	ConfigurationDispatcherConfigurationRequestContent(final EventHub eventHub, final ConfigurationExtension module) {
		super(eventHub, module);
	}

	/**
	 * Dispatch Configuration request using App ID. Called internally by the Configuration Module.
	 *
	 * @param appId App ID used for configuration
	 */
	void dispatchInternalConfigureWithAppIdEvent(final String appId) {
		EventData eventData = new EventData();
		eventData.putString(ConfigurationConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID,
							appId);
		eventData.putBoolean(ConfigurationConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_IS_INTERNAL_EVENT,
							 true);
		final Event event = new Event.Builder("Configure with AppID Internal", EventType.CONFIGURATION,
											  EventSource.REQUEST_CONTENT).setData(eventData).build();
		super.dispatch(event);
	}

}

