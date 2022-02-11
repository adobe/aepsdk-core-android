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

public class MockConfigurationExtension extends ConfigurationExtension {


	private static final String CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH		= "config.filePath";

	public MockConfigurationExtension(final EventHub eventHub, final PlatformServices services) {
		super(eventHub, services);
	}

	boolean configureWithFilePathWasCalled = false;
	String configureWithFilePathParametersFilePath;
	Event configureWithFilePathParametersEvent;
	@Override
	void processConfigWithFilePathEvent(final Event event) {
		final String filePath = event.getData().optString(
									CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH, null);

		this.configureWithFilePathParametersFilePath = filePath;
		this.configureWithFilePathParametersEvent = event;
		configureWithFilePathWasCalled = true;
	}

	@Override
	ConfigurationDispatcherConfigurationResponseIdentity createResponseIdentityDispatcher() {
		return createDispatcher(MockConfigurationDispatcherConfigurationResponseIdentity.class);
	}

	@Override
	ConfigurationDispatcherConfigurationRequestContent createRequestDispatcher() {
		return createDispatcher(MockConfigurationDispatcherConfigurationRequestContent.class);
	}

	@Override
	ConfigurationDispatcherConfigurationResponseContent createResponseDispatcher() {
		return createDispatcher(MockConfigurationDispatcherConfigurationResponseContent.class);
	}

	boolean configureWithJsonStringWasCalled = false;
	boolean configureWithJsonStringParametersIsUpdate;
	String configureWithJsonStringParametersJsonConfigString;
	Event configureWithJsonStringParametersEvent;
	@Override
	void configureWithJsonString(final String jsonConfigString, final Event event,
								 final boolean isUpdate) {
		configureWithJsonStringParametersJsonConfigString = jsonConfigString;
		configureWithJsonStringParametersEvent = event;
		configureWithJsonStringWasCalled = true;
		configureWithJsonStringParametersIsUpdate = isUpdate;
	}

	boolean processPublishConfigurationEventWasCalled;
	Event processPublishConfigurationEventParamEvent;
	@Override
	void processPublishConfigurationEvent(Event event) {
		processPublishConfigurationEventWasCalled = true;
		processPublishConfigurationEventParamEvent = event;
	}

	boolean handleBootEventWasCalled = false;
	Event handleBootEventParamEvent;
	@Override
	void handleBootEvent(Event event) {
		handleBootEventParamEvent = event;
		handleBootEventWasCalled = true;
	}

	boolean handleGetSdkIdentitiesEventCalled = false;
	Event handleGetSdkIdentitiesEventParamEvent;
	@Override
	void handleGetSdkIdentitiesEvent(Event event) {
		handleGetSdkIdentitiesEventCalled = true;
		handleGetSdkIdentitiesEventParamEvent = event;
	}

	boolean processGetSdkIdsEventWasCalled = false;
	@Override
	void processGetSdkIdsEvent() {
		processGetSdkIdsEventWasCalled = true;
	}

	boolean handleEventWasCalled = false;
	Event handleEventParamEvent;
	@Override
	void handleEvent(Event event) {
		handleEventWasCalled = true;
		handleEventParamEvent = event;
	}
}
