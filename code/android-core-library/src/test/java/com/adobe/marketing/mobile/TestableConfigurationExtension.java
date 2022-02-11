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


public class TestableConfigurationExtension extends ConfigurationExtension {

	private static final String CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID			= "config.appId";
	private static final String CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH		= "config.filePath";
	private static final String CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE		= "config.assetFile";

	PlatformServices services;
	MockConfigurationDownloader downloader;
	TestableConfigurationDispatcherConfigurationResponseContent responseDispatcher;
	TestableConfigurationDispatcherConfigurationRequestContent requestDispatcher;
	TestableConfigurationDispatcherConfigurationResponseIdentity responseIdentityDispatcher;

	public TestableConfigurationExtension(final EventHub hub,
										  final PlatformServices services) throws MissingPlatformServicesException {
		super(hub, services);
		this.services = services;

		if (services.getSystemInfoService() != null) {
			this.downloader = new MockConfigurationDownloader(services.getNetworkService(),
					services.getSystemInfoService(), null, null);
		}
	}

	protected void setPrimaryConfig(String jsonConfigString) {
		super.configureWithJsonString(jsonConfigString, Event.SHARED_STATE_NEWEST, false);

		if (responseDispatcher != null) {
			responseDispatcher.reset(); // reset as response will get dispatched
		}

		if (requestDispatcher != null) {
			requestDispatcher.reset(); // reset as response will get dispatched
		}
	}

	protected void setPrimaryConfig(String jsonConfigString, Event event) {
		super.configureWithJsonString(jsonConfigString, event, false);

		if (responseDispatcher != null) {
			responseDispatcher.reset(); // reset as response will get dispatched
		}

		if (requestDispatcher != null) {
			requestDispatcher.reset(); // reset as response will get dispatched
		}
	}

	protected EventData getConfigSharedEventState(final String stateName, final Event event) {
		return getSharedEventState(stateName, event);
	}

	protected void setCachedContentReturnValue(final String cachedJSON) {
		if (downloader != null) {
			downloader.cachedConfigString = cachedJSON;
		}
	}


	@Override
	ConfigurationDispatcherConfigurationRequestContent createRequestDispatcher() {

		requestDispatcher = createDispatcher(TestableConfigurationDispatcherConfigurationRequestContent.class);
		return requestDispatcher;
	}

	@Override
	ConfigurationDispatcherConfigurationResponseContent createResponseDispatcher() {
		responseDispatcher = createDispatcher(TestableConfigurationDispatcherConfigurationResponseContent.class);
		return responseDispatcher;
	}

	@Override
	ConfigurationDispatcherConfigurationResponseIdentity createResponseIdentityDispatcher() {
		responseIdentityDispatcher = createDispatcher(TestableConfigurationDispatcherConfigurationResponseIdentity.class);
		return responseIdentityDispatcher;
	}

	boolean getConfigurationDownloaderWasCalled;
	String getConfigurationDownloaderParameterAppID;
	@Override
	protected ConfigurationDownloader getConfigDownloader(final String appID) {
		getConfigurationDownloaderWasCalled = true;
		getConfigurationDownloaderParameterAppID = appID;
		return downloader;
	}


	boolean configureWithJsonStringWasCalled = false;
	boolean configureWithJsonStringParametersIsUpdate;
	String configureWithJsonStringParametersJsonConfigString;
	Event configureWithJsonStringParametersEvent;
	@Override
	void configureWithJsonString(final String jsonConfigString, final Event event,
								 final boolean isUpdate) {
		super.configureWithJsonString(jsonConfigString, event, isUpdate);
		configureWithJsonStringParametersJsonConfigString = jsonConfigString;
		configureWithJsonStringParametersEvent = event;
		configureWithJsonStringWasCalled = true;
		configureWithJsonStringParametersIsUpdate = isUpdate;
	}

	@Override
	void handleGetSdkIdentitiesEvent(Event event) {
		super.handleGetSdkIdentitiesEvent(event);
	}

	boolean processGetSdkIdsEventWasCalled;
	@Override
	void processGetSdkIdsEvent() {
		processGetSdkIdsEventWasCalled = true;
		super.processGetSdkIdsEvent();
	}

	boolean processEventWasCalled;
	Event processEventParameterEvent;
	boolean processEventParameterIsFromQueue;
	int processEventCallCount = 0;
	@Override
	void handleEvent(Event event) {
		super.handleEvent(event);
		processEventCallCount ++;
		processEventWasCalled = true;
		processEventParameterEvent = event;
	}


	boolean processConfigWithFilePathEventWasCalled = false;
	String processConfigWithFilePathEventParametersFilePath;
	Event processConfigWithFilePathEventParametersEvent;
	@Override
	void processConfigWithFilePathEvent(final Event event) {
		final String filePath = event.getData().getString(
									CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH);

		super.processConfigWithFilePathEvent(event);
		this.processConfigWithFilePathEventParametersFilePath = filePath;
		this.processConfigWithFilePathEventParametersEvent = event;
		processConfigWithFilePathEventWasCalled = true;
	}

	boolean loadBundledConfigWasCalled = false;
	String loadBundledConfigWasCalledParametersAssetFileName;
	Event loadBundledConfigParametersEvent;
	@Override
	protected boolean loadBundledConfig(final Event event, final String fileName) {
		this.loadBundledConfigWasCalled = true;
		this.loadBundledConfigParametersEvent = event;
		this.loadBundledConfigWasCalledParametersAssetFileName = fileName;
		return super.loadBundledConfig(event, fileName);
	}
	public void clearLoadBundledConfig() {
		this.loadBundledConfigWasCalled = false;
		this.loadBundledConfigParametersEvent = null;
		this.loadBundledConfigWasCalledParametersAssetFileName = null;
	}

	boolean handleBootEventWasCalled;
	Event handleBootEventParameterEvent;
	@Override
	void handleBootEvent(Event event) {
		super.handleBootEvent(event);
		handleBootEventWasCalled = true;
		handleBootEventParameterEvent = event;
	}


	boolean processUpdateConfigEventWasCalled;
	int processUpdateConfigEventCallCount = 0;
	Event processUpdateConfigEventParameterEvent;
	@Override
	void processUpdateConfigEvent(Event event) {
		super.processUpdateConfigEvent(event);
		processUpdateConfigEventCallCount ++;
		processUpdateConfigEventWasCalled = true;
		processUpdateConfigEventParameterEvent = event;
	}

	boolean processClearUpdatedConfigEventWasCalled;
	int processClearUpdateConfigEventCallCount = 0;
	Event processClearUpdatedConfigEventParameterEvent;
	@Override
	void processClearUpdatedConfigEvent(Event event) {
		super.processClearUpdatedConfigEvent(event);
		processClearUpdateConfigEventCallCount++;
		processClearUpdatedConfigEventWasCalled = true;
		processClearUpdatedConfigEventParameterEvent = event;
	}

	boolean processConfigureWithAppIDEventWasCalled;
	String processConfigureWithAppIDEventParameterNewAppId;
	Event processConfigureWithAppIDEventParameterEvent;
	int processConfigureWithAppIDEventCallCount = 0;
	@Override
	void processConfigureWithAppIDEvent(Event event) {
		super.processConfigureWithAppIDEvent(event);
		processConfigureWithAppIDEventCallCount++;
		processConfigureWithAppIDEventWasCalled = true;
		processConfigureWithAppIDEventParameterEvent = event;

		if (event.getData() != null) {
			final String newAppId = event.getData().getString(
										CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID);
			processConfigureWithAppIDEventParameterNewAppId = newAppId;
		}
	}

	boolean processPublishConfigurationEventWasCalled;
	Event processPublishConfigurationEventParamEvent;
	@Override
	void processPublishConfigurationEvent(Event event) {
		super.processPublishConfigurationEvent(event);
		processPublishConfigurationEventWasCalled = true;
		processPublishConfigurationEventParamEvent = event;
	}
}


