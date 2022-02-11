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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.ByteArrayInputStream;



import static  org.junit.Assert.*;

public class ConfigurationTests extends BaseTest {

	private static final String DATASTORE_KEY 							= "AdobeMobile_ConfigState";
	private static final String PERSISTED_OVERRIDDEN_CONFIG 			= "config.overridden.map";
	private static final String PERSISTED_APPID   					    = "config.appID";

	private static final String CONFIGURATION_URL_BASE    = "https://assets.adobedtm.com/%s.json";

	// Mocks for testing
	private final static String ANALYTICS_RSID_KEY 	 		= "Analytics.rsids";
	private final static String ANALYTICS_SERVER_KEY  		= "Analytics.server";
	private final static String DOWNLOADED_RSID 			= "downloaded_rsid";
	private final static String DOWNLOADED_SERVER			= "downloaded_server";
	private final static String UPDATED_RSID 				= "updated_rsid";
	private final static String CUSTOMMODULE_CONFIG_KEY 	= "customModule_ConfigKey";
	private final static String CUSTOMMODULE_CONFIG_VALUE	= "customModule_ConfigValue";
	private final static String CUSTOMMODULE_CONFIG_VALUE_UPDATED = "customModule_ConfigValueUpdated";

	//Shared State Constants for testing
	private final static String CONFIGURATION_STATE_NAME 	= "com.adobe.module.configuration";

	private final static String CONFIG_JSON_STRING			= "{'" + ANALYTICS_RSID_KEY + "':'" + DOWNLOADED_RSID +
			"','" + ANALYTICS_SERVER_KEY + "':'" + DOWNLOADED_SERVER + "'}";
	private final static HashMap<String, String> DOWNLOADED_CONFIG_MAP = new HashMap<String, String> () {
		{
			put(ANALYTICS_RSID_KEY, DOWNLOADED_RSID);
			put(ANALYTICS_SERVER_KEY, DOWNLOADED_SERVER);
		}
	};
	private final static HashMap<String, Object> UPDATED_CONFIG_MAP = new HashMap<String, Object> () {
		{
			put(ANALYTICS_RSID_KEY, UPDATED_RSID);
			put(CUSTOMMODULE_CONFIG_KEY, CUSTOMMODULE_CONFIG_VALUE);
		}
	};


	private final static String OVERRIDDEN_CONFIG_STRING			= "{'" + ANALYTICS_RSID_KEY + "':'" + UPDATED_RSID +
			"','" + CUSTOMMODULE_CONFIG_KEY + "':'" + CUSTOMMODULE_CONFIG_VALUE + "'}";

	private final static String APP_DIRECTORY 					= "someAppDirectory";

	private final static HashMap<String, String> updateConfigMap = new HashMap<String, String> () {
		{
			put(ANALYTICS_RSID_KEY, UPDATED_RSID);
			put(CUSTOMMODULE_CONFIG_KEY, CUSTOMMODULE_CONFIG_VALUE_UPDATED);
		}
	};

	private TestableConfigurationDispatcherConfigurationResponseContent responseDispatcher;
	private TestableConfigurationDispatcherConfigurationRequestContent requestDispatcher;
	private TestableConfigurationDispatcherConfigurationResponseIdentity responseIdentityDispatcher;

	private FakeJsonUtilityService jsonUtilityService;
	private TestableConfigurationExtension configuration;

	@Before
	public void beforeEach() {
		// Mock Platform Services
		super.beforeEach();
		jsonUtilityService = new FakeJsonUtilityService();
		Log.setLoggingService(platformServices.getLoggingService());
		Log.setLogLevel(LoggingMode.VERBOSE);
	}

	@After
	public void afterEach() {
		if (platformServices.getLocalStorageService() != null) {
			resetPersistence();
		}
	}

	//	 =================================================================================================================
	//	 void handleEvent(final Event event, final  boolean isFromQueue)
	//	 =================================================================================================================

	@Test
	public void testProcessEvent_when_AppIDEvent() throws Exception  {
		// setup
		beginBasic();
		Event event = configureWithAppIDEvent("appID");
		// test
		configuration.handleEvent(event);
		waitForExecutor(configuration.getExecutor());
		assertTrue(configuration.processConfigureWithAppIDEventWasCalled);
		assertEquals(configuration.processConfigureWithAppIDEventParameterEvent, event);
		assertEquals(configuration.processConfigureWithAppIDEventParameterNewAppId, "appID");
	}

	@Test
	public void testProcessEvent_when_updateConfigEvent() throws Exception  {
		// setup
		beginBasic();
		Event event = updateConfigEvent(updateConfigMap);
		// test
		configuration.handleEvent(event);
		waitForExecutor(configuration.getExecutor());
		assertTrue(configuration.processUpdateConfigEventWasCalled);
		assertEquals(configuration.processUpdateConfigEventParameterEvent, event);
	}

	@Test
	public void testProcessEvent_when_clearUpdatedConfigEvent() throws Exception  {
		// setup
		beginBasic();
		Event event = clearUpdatedConfigEvent();
		// test
		configuration.handleEvent(event);
		waitForExecutor(configuration.getExecutor());
		assertTrue(configuration.processClearUpdatedConfigEventWasCalled);
		assertEquals(configuration.processClearUpdatedConfigEventParameterEvent, event);
	}

	@Test
	public void testProcessEvent_when_configWithFilePathEvent()  throws Exception {
		// setup
		beginBasic();
		Event event = configWithFilePathEvent("FilePath");
		// test
		configuration.handleEvent(event);
		waitForExecutor(configuration.getExecutor());
		assertTrue(configuration.processConfigWithFilePathEventWasCalled);
		assertEquals(configuration.processConfigWithFilePathEventParametersEvent, event);
		assertEquals(configuration.processConfigWithFilePathEventParametersFilePath, "FilePath");
	}

	@Test
	public void testProcessEvent_when_publishConfigEvent() throws Exception {
		// setup
		beginBasic();
		Event event = publishConfigEvent();
		// test
		configuration.handleEvent(event);
		waitForExecutor(configuration.getExecutor());
		assertTrue(configuration.processPublishConfigurationEventWasCalled);
		assertEquals(configuration.processPublishConfigurationEventParamEvent, event);
	}


	//	 =================================================================================================================
	//	 void processConfigWithFilePathEvent(final String filePath, final Event event, final boolean isUpdate)
	//	 =================================================================================================================

	@Test
	public void testProcessConfigWithFilePathEvent_NullFilePath() throws Exception  {
		// setup
		beginBasic();
		final String filePath = null;
		// test
		configuration.handleEvent(configWithFilePathEvent(filePath));
		waitForExecutor(configuration.getExecutor());
		assertTrue("ConfigureWithFilePathEvent called", configuration.processConfigWithFilePathEventWasCalled);
		// verify
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertNull("event data from sharedState should be null", configEventData);
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);

	}

	@Test
	public void testProcessConfigWithFilePathEvent_EmptyFilePath() throws Exception  {
		// setup
		beginBasic();
		final String filePath = "";
		// test shared state
		// test
		configuration.handleEvent(configWithFilePathEvent(filePath));
		waitForExecutor(configuration.getExecutor());
		assertTrue("ConfigureWithFilePathEvent called", configuration.processConfigWithFilePathEventWasCalled);
		// verify
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertNull("event data from sharedState should be null", configEventData);
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);

	}

	@Test
	public void testProcessConfigWithFilePathEvent_When_NoFileIsPresent() throws Exception  {
		// setup
		beginBasic();
		configuration.setPrimaryConfig(jsonUtilityService.createJSONObject(UPDATED_CONFIG_MAP).toString());

		final String filePath = this.getClass().getResource("").getPath();
		configuration.handleEvent(configWithFilePathEvent(filePath));
		waitForExecutor(configuration.getExecutor());

		// verify shared state
		assertTrue("ConfigureWithFilePathEvent called", configuration.processConfigWithFilePathEventWasCalled);

		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", UPDATED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", CUSTOMMODULE_CONFIG_VALUE,
					 configEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// verify dispatched event
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);

		deleteTempAppDirectory();
	}

	@Test
	public void testProcessConfigWithFilePathEvent_When_FileReturnsEmptyString() throws Exception  {
		// setup
		beginBasic();
		configuration.setPrimaryConfig(jsonUtilityService.createJSONObject(UPDATED_CONFIG_MAP).toString());

		final String filePath = createSampleFileWithData("").getPath();
		configuration.handleEvent(configWithFilePathEvent(filePath));
		waitForExecutor(configuration.getExecutor());

		assertTrue("ConfigureWithFilePathEvent called", configuration.processConfigWithFilePathEventWasCalled);

		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", UPDATED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", CUSTOMMODULE_CONFIG_VALUE,
					 configEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// verify dispatched event
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);

		deleteTempAppDirectory();
	}

	@Test
	public void testProcessConfigWithFilePathEvent_When_FileReturnsValidString() throws Exception  {
		// setup
		beginBasic();
		final String filePath = createSampleFileWithData(CONFIG_JSON_STRING).getPath();
		configuration.handleEvent(configWithFilePathEvent(filePath));
		waitForExecutor(configuration.getExecutor());
		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertTrue("ConfigWithFilePathEvent called", configuration.processConfigWithFilePathEventWasCalled);
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_SERVER,
					 configEventData.optString(ANALYTICS_SERVER_KEY, null));
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", DOWNLOADED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals("event should have the correct pairId", "pairId",
					 responseDispatcher.dispatchConfigResponseEventParameterPairID);
		deleteTempAppDirectory();
	}



	//	 =================================================================================================================
	//	 test loadBundledConfig
	//	 =================================================================================================================

	@Test
	public void testProcessConfigWithAssetFileEvent_NullFilePath() throws Exception  {
		// setup
		beginBasic();
		configuration.clearLoadBundledConfig();
		final String filePath = null;
		// test
		configuration.handleEvent(configWithAssetPathEvent(filePath));
		waitForExecutor(configuration.getExecutor());
		assertTrue("loadBundledConfig called", configuration.loadBundledConfigWasCalled);
		// verify
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertNull("event data from sharedState should be null", configEventData);
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);

	}

	@Test
	public void testProcessConfigWithAssetFileEvent_EmptyFilePath() throws Exception  {
		// setup
		beginBasic();
		configuration.clearLoadBundledConfig();
		final String filePath = "";
		// test shared state
		// test
		configuration.handleEvent(configWithAssetPathEvent(filePath));
		waitForExecutor(configuration.getExecutor());
		assertTrue("loadBundledConfig called", configuration.loadBundledConfigWasCalled);
		// verify
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertNull("event data from sharedState should be null", configEventData);
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);

	}

	@Test
	public void testProcessConfigWithAssetFileEvent_When_NoFileIsPresent() throws Exception  {
		// setup
		beginBasic();
		configuration.clearLoadBundledConfig();
		configuration.setPrimaryConfig(jsonUtilityService.createJSONObject(UPDATED_CONFIG_MAP).toString());

		configuration.handleEvent(configWithAssetPathEvent("config.json"));
		waitForExecutor(configuration.getExecutor());

		// verify shared state
		assertTrue("loadBundledConfig called", configuration.loadBundledConfigWasCalled);

		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", UPDATED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", CUSTOMMODULE_CONFIG_VALUE,
					 configEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// verify dispatched event
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);

		deleteTempAppDirectory();
	}

	@Test
	public void testProcessConfigWithAssetFileEvent_When_FileReturnsEmptyString() throws Exception  {
		// setup
		beginBasic();
		mockBundledConfig("{}");
		configuration.clearLoadBundledConfig();
		configuration.setPrimaryConfig(jsonUtilityService.createJSONObject(UPDATED_CONFIG_MAP).toString());

		configuration.handleEvent(configWithAssetPathEvent("config.json"));
		waitForExecutor(configuration.getExecutor());

		assertTrue("loadBundledConfig called", configuration.loadBundledConfigWasCalled);

		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", UPDATED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", CUSTOMMODULE_CONFIG_VALUE,
					 configEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// verify dispatched event
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);

		deleteTempAppDirectory();
	}

	@Test
	public void testProcessConfigWithAssetFileEvent_When_FileReturnsValidString() throws Exception  {
		// setup
		beginBasic();
		waitForExecutor(configuration.getExecutor());
		mockBundledConfig(CONFIG_JSON_STRING);
		configuration.clearLoadBundledConfig();

		configuration.handleEvent(configWithAssetPathEvent("config.json"));
		waitForExecutor(configuration.getExecutor());
		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertTrue("loadBundledConfig called", configuration.loadBundledConfigWasCalled);
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_SERVER,
					 configEventData.optString(ANALYTICS_SERVER_KEY, null));
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", DOWNLOADED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals("event should have the correct pairId", "pairId",
					 responseDispatcher.dispatchConfigResponseEventParameterPairID);
		deleteTempAppDirectory();
	}


	// =================================================================================================================
	// void processUpdateConfigEvent(final Event event, final boolean isUpdate)
	// =================================================================================================================

	@Test
	public void testProcessConfigUpdateEvent_when_Null() throws Exception  {
		beginBasic();
		// test
		configuration.handleEvent(updateConfigEvent(null));
		waitForExecutor(configuration.getExecutor());
		assertTrue("UpdateConfig called", configuration.processUpdateConfigEventWasCalled);

		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertNull(configEventData);
	}

	@Test
	public void testProcessConfigUpdateEvent_when_EmptyConfig() throws Exception  {
		beginBasic();
		HashMap<String, String> apiConfig = new HashMap<String, String>();
		// test
		configuration.handleEvent(updateConfigEvent(apiConfig));
		waitForExecutor(configuration.getExecutor());
		assertTrue("UpdateConfig called", configuration.processUpdateConfigEventWasCalled);

		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertNull(configEventData);
	}

	@Test
	public void testProcessConfigUpdateEvent_with_ValidConfig() throws Exception  {
		beginBasic();
		// test
		configuration.handleEvent(updateConfigEvent(updateConfigMap));
		waitForExecutor(configuration.getExecutor());
		assertTrue("UpdateConfig called", configuration.processUpdateConfigEventWasCalled);

		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data should have no data", 2, configEventData.size());
		assertEquals("event should have the correct data", UPDATED_RSID, configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", CUSTOMMODULE_CONFIG_VALUE_UPDATED,
					 configEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));
		// verify dispatchedEvent
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", UPDATED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", CUSTOMMODULE_CONFIG_VALUE_UPDATED,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));
		assertNull("event should not have pairId", responseDispatcher.dispatchConfigResponseEventParameterPairID);
	}

	@Test
	public void testProcessConfigUpdateEvent_with_ValidConfig_and_PrimaryConfigurationOccurred() throws Exception  {
		// setup
		beginBasic();
		setUpdatedConfigInPersistence();
		configuration.setPrimaryConfig(CONFIG_JSON_STRING, Event.SHARED_STATE_OLDEST);
		// test
		configuration.handleEvent(updateConfigEvent(updateConfigMap));
		waitForExecutor(configuration.getExecutor());
		assertTrue("UpdateConfig called", configuration.processUpdateConfigEventWasCalled);
		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data should have no data", 3, configEventData.size());
		assertEquals("event should have the correct data", DOWNLOADED_SERVER, configEventData.optString(ANALYTICS_SERVER_KEY,
					 null));
		assertEquals("event should have the correct data", UPDATED_RSID, configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", CUSTOMMODULE_CONFIG_VALUE_UPDATED,
					 configEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));
		// verify dispatchedEvent
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals("event should have the correct data", UPDATED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", CUSTOMMODULE_CONFIG_VALUE_UPDATED,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));
		assertNull("event should not have pairId", responseDispatcher.dispatchConfigResponseEventParameterPairID);
	}

	@Test
	public void testProcessConfigUpdateEvent_when_localStorageService_not_initialized_shouldNotCrash() throws Exception  {
		platformServices.fakeLocalStorageService = null;
		platformServices.mockSystemInfoService = null;
		beginBasic();
		// test
		configuration.handleEvent(updateConfigEvent(updateConfigMap));
		waitForExecutor(configuration.getExecutor());
		assertTrue("UpdateConfig called", configuration.processUpdateConfigEventWasCalled);
		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data should have no data", 2, configEventData.size());
		assertEquals("event should have the correct data", UPDATED_RSID, configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", CUSTOMMODULE_CONFIG_VALUE_UPDATED,
					 configEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));
		// verify dispatchedEvent
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", UPDATED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", CUSTOMMODULE_CONFIG_VALUE_UPDATED,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));
		assertNull("event should not have pairId", responseDispatcher.dispatchConfigResponseEventParameterPairID);
	}

	//	 =================================================================================================================
	//	 void processClearUpdatedConfigEvent(final Event event)
	//	 =================================================================================================================

	@Test
	public void testProcessClearUpdatedConfigEvent_when_PrimaryConfigurationNull() throws Exception  {
		beginBasic();
		// test
		configuration.handleEvent(clearUpdatedConfigEvent());
		waitForExecutor(configuration.getExecutor());
		assertTrue("ClearUpdatedConfig called", configuration.processClearUpdatedConfigEventWasCalled);
		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertTrue(configEventData.isEmpty());
		// verify dispatchedEvent
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);

	}

	@Test
	public void testProcessClearUpdatedConfigEvent_with_ValidPrimaryConfiguration() throws Exception  {
		// setup
		beginBasic();
		configuration.setPrimaryConfig(CONFIG_JSON_STRING, Event.SHARED_STATE_OLDEST);
		//test
		configuration.handleEvent(clearUpdatedConfigEvent());
		waitForExecutor(configuration.getExecutor());
		assertTrue("ClearUpdatedConfig called", configuration.processClearUpdatedConfigEventWasCalled);
		// verify shared state
		EventData configEventDataCleared = readSharedConfigurationForNextEvent();
		assertEquals("event should have the data before config was updated", 2, configEventDataCleared.size());
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 configEventDataCleared.optString(ANALYTICS_SERVER_KEY,
							 null));
		assertEquals("event should have the correct data", DOWNLOADED_RSID,
					 configEventDataCleared.optString(ANALYTICS_RSID_KEY, null));
		// verify dispatchedEvent
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals("event should have the correct data", DOWNLOADED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertNull("event should not have pairId", responseDispatcher.dispatchConfigResponseEventParameterPairID);

	}

	@Test
	public void testProcessClearUpdatedConfigEvent_with_ValidPrimaryConfiguration_OverriddenByProgrammaticConfig() throws
		Exception  {
		// setup
		beginBasic();
		setUpdatedConfigInPersistence();
		configuration.setPrimaryConfig(CONFIG_JSON_STRING, Event.SHARED_STATE_OLDEST);
		//test
		configuration.handleEvent(clearUpdatedConfigEvent());
		waitForExecutor(configuration.getExecutor());
		assertTrue("ClearUpdatedConfig called", configuration.processClearUpdatedConfigEventWasCalled);
		// verify shared state
		EventData configEventDataCleared = readSharedConfigurationForNextEvent();
		assertEquals("event should have the data before config was updated", 2, configEventDataCleared.size());
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 configEventDataCleared.optString(ANALYTICS_SERVER_KEY,
							 null));
		assertEquals("event should have the correct data", DOWNLOADED_RSID,
					 configEventDataCleared.optString(ANALYTICS_RSID_KEY, null));
		// verify dispatchedEvent
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals("event should have the correct data", DOWNLOADED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertNull("event should not have pairId", responseDispatcher.dispatchConfigResponseEventParameterPairID);
	}

	//	 =================================================================================================================
	//	 void handleBootEvent(final Event event)
	//	 =================================================================================================================

	@Test
	public void testHandleBootEvent_when_NoAppIdInManifest_AppIDPersisted_then_CachedFileIsLoaded() throws
		Exception {
		// setup
		beginWithAppIDInPersistence();
		configuration.setCachedContentReturnValue(CONFIG_JSON_STRING);
		Event bootEvent = bootEvent();
		// test
		configuration.handleBootEvent(bootEvent);
		waitForExecutor(configuration.getExecutor());
		assertEquals("mockAppID", configuration.getConfigurationDownloaderParameterAppID);
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_SERVER,
					 configEventData.optString(ANALYTICS_SERVER_KEY, null));
		// verify dispatchedEvent
		assertTrue("event should  be dispatched", requestDispatcher.dispatchInternalConfigureWithAppIdEventWasCalled);
		assertEquals("event should have the correct data", DOWNLOADED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_SERVER_KEY, null));
		assertNull(responseDispatcher.dispatchConfigResponseEventParameterPairID);
		assertTrue("configureWithAppIdInternal event is created",
				   requestDispatcher.dispatchInternalConfigureWithAppIdEventWasCalled);
		assertEquals("mockAppID", requestDispatcher.dispatchInternalConfigureWithAppIdEventParameterAppID);
	}

	@Test
	public void testHandleBootEvent_when_AppIdInManifest_then_CachedFileIsLoaded() throws Exception {
		// setup
		beginWithAppIDInManifest();
		configuration.setCachedContentReturnValue(CONFIG_JSON_STRING);
		Event bootEvent = bootEvent();
		// test
		configuration.handleBootEvent(bootEvent);
		waitForExecutor(configuration.getExecutor());
		assertEquals("manifestAppID", configuration.getConfigurationDownloaderParameterAppID);

		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_SERVER,
					 configEventData.optString(ANALYTICS_SERVER_KEY, null));
		// verify dispatchedEvent
		assertTrue("event should  be dispatched", requestDispatcher.dispatchInternalConfigureWithAppIdEventWasCalled);
		assertEquals("event should have the correct data", DOWNLOADED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_SERVER_KEY, null));
		assertNull(responseDispatcher.dispatchConfigResponseEventParameterPairID);
		assertTrue("configureWithAppIdInternal event is created",
				   requestDispatcher.dispatchInternalConfigureWithAppIdEventWasCalled);
		assertEquals("manifestAppID", requestDispatcher.dispatchInternalConfigureWithAppIdEventParameterAppID);
	}

	@Test
	public void
	testHandleBootEvent_when_AppIdInManifest_and_AppIDInPersistence_then_CachedFileIsLoaded_WithPersistedAppID()
	throws Exception  {
		// setup
		beginWithAppIDInPersistenceAndManifest();
		configuration.setCachedContentReturnValue(CONFIG_JSON_STRING);
		Event bootEvent = bootEvent();
		// test
		configuration.handleBootEvent(bootEvent);
		waitForExecutor(configuration.getExecutor());
		assertEquals("mockAppID", configuration.getConfigurationDownloaderParameterAppID);

		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_SERVER,
					 configEventData.optString(ANALYTICS_SERVER_KEY, null));
		// verify dispatchedEvent
		assertTrue("event should  be dispatched", requestDispatcher.dispatchInternalConfigureWithAppIdEventWasCalled);
		assertEquals("event should have the correct data", DOWNLOADED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_SERVER_KEY, null));
		assertNull(responseDispatcher.dispatchConfigResponseEventParameterPairID);

		assertTrue("configureWithAppIdInternal event is created",
				   requestDispatcher.dispatchInternalConfigureWithAppIdEventWasCalled);
		assertEquals("mockAppID", requestDispatcher.dispatchInternalConfigureWithAppIdEventParameterAppID);
	}

	@Test
	public void
	testHandleBootEvent_when_AppIDInPersistence_and_NoCachedFile_then_OverriddenConfiguration_isLoaded()
	throws Exception  {
		// setup
		beginWithOverriddenConfig_and_AppID_InPersistence();
		configuration.setCachedContentReturnValue(null);
		Event bootEvent = bootEvent();
		// test
		configuration.handleBootEvent(bootEvent);
		waitForExecutor(configuration.getExecutor());

		//verify
		assertEquals("mockAppID", configuration.getConfigurationDownloaderParameterAppID);

		EventData configEventData = readSharedConfigurationForNextEvent();

		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", UPDATED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", CUSTOMMODULE_CONFIG_VALUE,
					 configEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));
		// verify dispatchedEvent
		assertTrue("event should  be dispatched", requestDispatcher.dispatchInternalConfigureWithAppIdEventWasCalled);
		assertEquals("event should have the correct data", UPDATED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", CUSTOMMODULE_CONFIG_VALUE,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));
		assertNull(responseDispatcher.dispatchConfigResponseEventParameterPairID);
		assertTrue("configureWithAppIdInternal event is created",
				   requestDispatcher.dispatchInternalConfigureWithAppIdEventWasCalled);
		assertEquals("mockAppID", requestDispatcher.dispatchInternalConfigureWithAppIdEventParameterAppID);
	}

	@Test
	public void testHandleBootEvent_when_AppIDAbsent_BundledConfigPresent_thenConfigureWithBundledContent() throws
		Exception  {
		// setup
		beginWithBundledConfiguration();
		Event bootEvent = bootEvent();
		// test
		configuration.handleBootEvent(bootEvent);
		waitForExecutor(configuration.getExecutor());
		assertTrue(configuration.configureWithJsonStringWasCalled);
		assertEquals(CONFIG_JSON_STRING, configuration.configureWithJsonStringParametersJsonConfigString);
		assertEquals(bootEvent, configuration.configureWithJsonStringParametersEvent);
		assertFalse("configureWithAppIdInternal event is created",
					requestDispatcher.dispatchInternalConfigureWithAppIdEventWasCalled);
	}

	@Test
	public void testHandleBootEvent_when_AppIDAbsent_BundledConfigAbsent_WithOverriddenConfig_thenShouldConfigure()
	throws Exception {
		// setup
		beginWithOverridenConfigInPersistence();
		Event bootEvent = bootEvent();
		// test
		configuration.handleBootEvent(bootEvent);
		waitForExecutor(configuration.getExecutor());
		assertFalse(configuration.configureWithJsonStringWasCalled);
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", UPDATED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", CUSTOMMODULE_CONFIG_VALUE,
					 configEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));
		// verify dispatchedEvent
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", UPDATED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", CUSTOMMODULE_CONFIG_VALUE,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));
		assertNull(responseDispatcher.dispatchConfigResponseEventParameterPairID);
		assertFalse("configureWithAppIdInternal event is created",
					requestDispatcher.dispatchInternalConfigureWithAppIdEventWasCalled);
	}

	@Test
	public void
	testHandleBootEvent_when_AppIDAbsent_BundledConfigAbsent_NoOverriddenConfig_thenShouldNotConfigure() throws
		Exception  {
		// setup
		beginBasic();
		Event bootEvent = bootEvent();
		// test
		configuration.handleBootEvent(bootEvent);
		waitForExecutor(configuration.getExecutor());
		assertFalse(configuration.configureWithJsonStringWasCalled);
	}


	@Test
	public void
	testHandleBootEvent_when_SystemInfoService_and_LocalStorageService_unavailable_then_ShouldNotConfigure() throws
		Exception  {
		// setup
		platformServices.fakeLocalStorageService = null;
		platformServices.mockSystemInfoService = null;
		beginBasic();

		Event bootEvent = bootEvent();
		// test
		configuration.handleBootEvent(bootEvent);
		waitForExecutor(configuration.getExecutor());
		assertFalse(configuration.configureWithJsonStringWasCalled);
	}

	@Test
	public void testHandleBootEvent_when_AppID_InPersistence_NoCachedFile_WithOverriddenConfig_thenShouldConfigure()
	throws Exception {
		// setup
		beginWithOverridenConfigInPersistence();
		Event bootEvent = bootEvent();
		// test
		configuration.handleBootEvent(bootEvent);
		waitForExecutor(configuration.getExecutor());
		assertFalse(configuration.configureWithJsonStringWasCalled);
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", UPDATED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", CUSTOMMODULE_CONFIG_VALUE,
					 configEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));
		// verify dispatchedEvent
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", UPDATED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", CUSTOMMODULE_CONFIG_VALUE,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));
		assertNull(responseDispatcher.dispatchConfigResponseEventParameterPairID);
		assertFalse("configureWithAppIdInternal event is created",
					requestDispatcher.dispatchInternalConfigureWithAppIdEventWasCalled);
	}

	//	 =================================================================================================================
	//	 void processConfigureWithAppIDEvent(final String newAppId, final Event event, final boolean isUpdate)
	//	 =================================================================================================================


	@Test
	public void testProcessConfigureWithAppIdEvent_when_EmptyEventData() throws Exception  {
		beginBasic();
		Event configureWithAppIdEvent = new Event.Builder("mockAppID Event", EventType.CONFIGURATION,
				EventSource.REQUEST_CONTENT).setData(null).build();
		configuration.processConfigureWithAppIDEvent(configureWithAppIdEvent);

		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertNull(configEventData);

		// verify dispatchedEvent
		assertFalse("download method not called", configuration.downloader.downloadConfigWasCalled);
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
	}

	@Test
	public void testProcessConfigureWithAppIdInternalEvent_when_DifferentAppIdInPersistence() throws Exception  {
		beginWithAppIDInPersistence();
		configuration.processConfigureWithAppIDEvent(configureWithAppIDInternalEvent("oldAppID"));

		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertNull(configEventData);

		// verify dispatchedEvent
		assertFalse("download method not called", configuration.downloader.downloadConfigWasCalled);
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
	}

	@Test
	public void testProcessConfigureWithAppIdInternalEvent_when_SameAppIdInPersistence() throws Exception  {
		beginWithAppIDInPersistence();
		Event configureWithAppIdEvent = configureWithAppIDInternalEvent("mockAppID");
		configuration.downloader.downloadConfigReturnValue  = CONFIG_JSON_STRING;
		// test
		configuration.handleEvent(configureWithAppIdEvent);
		waitForExecutor(configuration.getExecutor());
		assertTrue("SetAppID called", configuration.processConfigureWithAppIDEventWasCalled);

		// verify
		assertEquals("mockAppID", getAppIDFromPersistence());
		assertTrue(configuration.downloader.downloadConfigWasCalled);
		assertEquals("mockAppID", configuration.getConfigurationDownloaderParameterAppID);

		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_RSID,
					 configEventData.getString(ANALYTICS_RSID_KEY));
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_SERVER,
					 configEventData.getString(ANALYTICS_SERVER_KEY));

		// verify dispatchedEvent
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", DOWNLOADED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.getString(ANALYTICS_RSID_KEY));
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.getString(ANALYTICS_SERVER_KEY));
	}

	@Test
	public void testProcessConfigureWithAppIdEvent_when_nullAppID_should_RemoveAppIdFromPersistence() throws Exception  {
		beginWithAppIDInPersistence();
		Event configureWithAppIdEvent = configureWithAppIDEvent(null);
		// test
		configuration.handleEvent(configureWithAppIdEvent);
		waitForExecutor(configuration.getExecutor());

		// verify
		assertTrue("SetAppID called", configuration.processConfigureWithAppIDEventWasCalled);
		assertEquals(null, getAppIDFromPersistence());

		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertNull(configEventData);

		// verify dispatchedEvent
		assertFalse("download method not called", configuration.downloader.downloadConfigWasCalled);
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);

	}

	@Test
	public void testProcessConfigureWithAppIdEvent_when_emptyAppID_should_RemoveAppIdFromPersistence() throws Exception  {
		beginWithAppIDInPersistence();
		Event configureWithAppIdEvent = configureWithAppIDEvent("");
		// test
		configuration.handleEvent(configureWithAppIdEvent);
		waitForExecutor(configuration.getExecutor());

		// verify
		assertTrue("SetAppID called", configuration.processConfigureWithAppIDEventWasCalled);
		assertEquals(null, getAppIDFromPersistence());

		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertNull(configEventData);

		// verify dispatchedEvent
		assertFalse("download method not called", configuration.downloader.downloadConfigWasCalled);
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);

	}


	@Test
	public void testProcessConfigureWithAppIdEvent_OnRemoteFetchSuccess() throws Exception  {
		beginBasic();
		Event configureWithAppIdEvent = configureWithAppIDEvent("newAppID");
		configuration.downloader.downloadConfigReturnValue  = CONFIG_JSON_STRING;
		// test
		configuration.handleEvent(configureWithAppIdEvent);
		waitForExecutor(configuration.getExecutor());
		assertTrue("SetAppID called", configuration.processConfigureWithAppIDEventWasCalled);

		// verify
		assertEquals("newAppID", getAppIDFromPersistence());
		assertTrue(configuration.downloader.downloadConfigWasCalled);
		assertEquals("newAppID", configuration.getConfigurationDownloaderParameterAppID);

		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_SERVER,
					 configEventData.optString(ANALYTICS_SERVER_KEY, null));

		// verify dispatchedEvent
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", DOWNLOADED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.getString(ANALYTICS_SERVER_KEY));


	}

	@Test
	public void testProcessConfigureWithAppIdEvent_OnRemoteFetchFailure() throws Exception  {
		beginBasic();
		Event configureWithAppIdEvent = configureWithAppIDEvent("newAppID");
		configuration.downloader.downloadConfigReturnValue  = null;

		// test
		configuration.handleEvent(configureWithAppIdEvent);
		waitForExecutor(configuration.getExecutor());
		assertTrue("SetAppID called", configuration.processConfigureWithAppIDEventWasCalled);

		// verify
		assertEquals("newAppID", getAppIDFromPersistence());
		assertTrue(configuration.downloader.downloadConfigWasCalled);
		assertEquals("newAppID", configuration.getConfigurationDownloaderParameterAppID);

		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should be null", null, configEventData);

		// verify dispatched event
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
	}


	@Test
	public void
	testProcessConfigureWithAppIdEvent_when_OverriddenConfig_persisted_then_OnRemoteFetchFailure_should_loadOnlyOverriddenConfig()
	throws Exception  {
		beginBasic();
		Event configureWithAppIdEvent = configureWithAppIDEvent("newAppID");
		configuration.downloader.downloadConfigReturnValue  = null;
		configuration.setPrimaryConfig(jsonUtilityService.createJSONObject(UPDATED_CONFIG_MAP).toString());

		// test
		configuration.handleEvent(configureWithAppIdEvent);
		waitForExecutor(configuration.getExecutor());
		assertTrue("SetAppID called", configuration.processConfigureWithAppIDEventWasCalled);

		// verify
		assertEquals("newAppID", getAppIDFromPersistence());
		assertTrue(configuration.downloader.downloadConfigWasCalled);
		assertEquals("newAppID", configuration.getConfigurationDownloaderParameterAppID);

		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", UPDATED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", CUSTOMMODULE_CONFIG_VALUE,
					 configEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// verify dispatchedEvent
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
	}



	@Test
	public void testProcessConfigureWithAppIdEvent_when_platformServices_notAvailable_shouldNotCrash() throws Exception  {
		platformServices.fakeLocalStorageService = null;
		platformServices.mockSystemInfoService = null;
		platformServices.mockNetworkService = null;
		beginBasic();
		Event configureWithAppIdEvent = configureWithAppIDEvent("newAppID");
		// test
		configuration.handleEvent(configureWithAppIdEvent);
		waitForExecutor(configuration.getExecutor());
		assertTrue("SetAppID called", configuration.processConfigureWithAppIDEventWasCalled);

		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertNull(configEventData);

		// verify dispatchedEvent
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
	}

	// helper for testProcessConfigureWithAppIdEvent_WithNetworkOffOffOn
	final class FakeSystemInfoServiceForNetworkTest extends MockSystemInfoService {
		public List<NetworkConnectionActiveListener> listeners = new ArrayList<NetworkConnectionActiveListener>();

		public FakeSystemInfoServiceForNetworkTest() {
			configuration.downloader.downloadConfigReturnValue  = "";
			networkConnectionStatus = ConnectionStatus.DISCONNECTED;
		}

		/**
		 * Create a thread that, in the backgorund, simulates the connection going on and on.
		 * Does not start the thread.
		 *
		 * @return the thread
		 */
		public Thread createBackgroundThread() {
			return new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(500);
						changeConnectionStatus(ConnectionStatus.DISCONNECTED);
						Thread.sleep(1500);
						changeConnectionStatus(ConnectionStatus.DISCONNECTED);
						Thread.sleep(500);
						configuration.downloader.downloadConfigReturnValue  = CONFIG_JSON_STRING;
						changeConnectionStatus(ConnectionStatus.CONNECTED);
					} catch (final InterruptedException e) {}
				}
			});
		}

		/**
		 * Simulates a connection status change. Called from background thread.
		 *
		 * @param status - the new status
		 */
		private void changeConnectionStatus(final ConnectionStatus status) {
			List<NetworkConnectionActiveListener> theListeners;

			synchronized (this) {
				networkConnectionStatus = status;
				theListeners = this.listeners;
				this.listeners = new ArrayList<NetworkConnectionActiveListener>();
			}

			for (final NetworkConnectionActiveListener listener : theListeners) {
				listener.onActive();
			}

		}

		@Override
		public boolean registerOneTimeNetworkConnectionActiveListener(final NetworkConnectionActiveListener
				networkConnectionActiveListener) {
			synchronized (this) {
				listeners.add(networkConnectionActiveListener);
			}

			return true;
		}
	}

	@Test
	public void testProcessConfigureWithAppIdEvent_WithNetworkOffOffOn() throws Exception  {
		beginBasic();
		final FakeSystemInfoServiceForNetworkTest mySystemInfoService = new FakeSystemInfoServiceForNetworkTest();
		platformServices.mockSystemInfoService = mySystemInfoService;

		Event configureWithAppIdEvent = configureWithAppIDEvent("newAppID");

		// test
		final Thread backgroundThread = mySystemInfoService.createBackgroundThread();

		backgroundThread.start();
		configuration.handleEvent(configureWithAppIdEvent);
		waitForExecutor(configuration.getExecutor());
		backgroundThread.join();
		assertTrue("SetAppID called", configuration.processConfigureWithAppIDEventWasCalled);

		// verify
		assertEquals("newAppID", getAppIDFromPersistence());
		assertTrue(configuration.downloader.downloadConfigWasCalled);
		assertEquals("newAppID", configuration.getConfigurationDownloaderParameterAppID);

		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_SERVER,
					 configEventData.optString(ANALYTICS_SERVER_KEY, null));

		// verify dispatchedEvent
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", DOWNLOADED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.getString(ANALYTICS_SERVER_KEY));


	}

	@Test
	public void testProcessConfigureWithAppIdEvent_WithNetworkOffAndCachedConfig() throws Exception  {
		beginBasic();
		platformServices.mockSystemInfoService.networkConnectionStatus = SystemInfoService.ConnectionStatus.DISCONNECTED;

		Event configureWithAppIdEvent = configureWithAppIDEvent("newAppID");
		configuration.downloader.downloadConfigReturnValue  = "";
		configuration.downloader.cachedConfigString = CONFIG_JSON_STRING;

		// test
		configuration.handleEvent(configureWithAppIdEvent);
		waitForExecutor(configuration.getExecutor());
		assertTrue("SetAppID called", configuration.processConfigureWithAppIDEventWasCalled);

		// verify
		assertEquals("newAppID", getAppIDFromPersistence());
		assertTrue(configuration.downloader.downloadConfigWasCalled);
		assertEquals("newAppID", configuration.getConfigurationDownloaderParameterAppID);

		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should have valid data", 2, configEventData.size());
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_SERVER,
					 configEventData.optString(ANALYTICS_SERVER_KEY, null));

		// verify dispatchedEvent
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", DOWNLOADED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.getString(ANALYTICS_SERVER_KEY));


	}


	//	 =================================================================================================================
	//	 void configureWithJsonString(final String jsonConfigString, final Event event, final boolean isUpdate)
	//	 =================================================================================================================

	@Test
	public void testConfigureWithJsonString_when_NullJsonString() throws Exception  {
		beginBasic();
		// test
		configuration.configureWithJsonString(null, bootEvent(), false);
		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertNull(configEventData);

		// verify dispatchedEvent
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
	}

	@Test
	public void testConfigureWithJsonString_when_EmptyJsonString() throws Exception  {
		beginBasic();
		// test
		configuration.configureWithJsonString("", bootEvent(), false);

		// verify shared state
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertNull(configEventData);

		// verify dispatchedEvent
		assertFalse("event should not be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
	}

	@Test
	public void testConfigureWithJsonString_when_ValidJsonString() throws Exception  {
		beginBasic();
		// test
		configuration.configureWithJsonString(CONFIG_JSON_STRING, bootEvent(), false);
		// verify dispatchedEvent
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", DOWNLOADED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_SERVER_KEY, null));
	}

	@Test
	public void testConfigureWithJsonString_when_ValidJsonString_and_overriddenConfigInPersistence_isUpdateIsFalse() throws
		Exception  {
		beginBasic();
		// test
		setUpdatedConfigInPersistence();
		configuration.configureWithJsonString(CONFIG_JSON_STRING, bootEvent(), false);
		// verify dispatchedEvent
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", UPDATED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals("event should have the correct data", CUSTOMMODULE_CONFIG_VALUE,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));
		// verify sharedState
		EventData configEventData = readSharedConfigurationForNextEvent();
		assertEquals("event data from sharedState should have valid data", 3, configEventData.size());
		assertEquals("event data from sharedState should have valid data", UPDATED_RSID,
					 configEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event data from sharedState should have valid data", DOWNLOADED_SERVER,
					 configEventData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals("event data from sharedState should have valid data", CUSTOMMODULE_CONFIG_VALUE,
					 configEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));

	}

	@Test
	public void testConfigureWithJsonString_when_ValidJsonString_and_overriddenConfigInPersistence_isUpdateIsTrue() throws
		Exception  {
		beginBasic();
		// test
		setUpdatedConfigInPersistence();
		configuration.configureWithJsonString(CONFIG_JSON_STRING, bootEvent(), false);
		// verify dispatchedEvent
		assertTrue("event should  be dispatched", responseDispatcher.dispatchConfigResponseWithEventDataWasCalled);
		assertEquals("event should have the correct data", UPDATED_RSID,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals("event should have the correct data", DOWNLOADED_SERVER,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals("event should have the correct data", CUSTOMMODULE_CONFIG_VALUE,
					 responseDispatcher.dispatchConfigResponseEventParameterEventData.optString(CUSTOMMODULE_CONFIG_KEY, null));
	}


	// =================================================================================================================
	// ConfigurationDownloader getConfigurationDownloader(final String url, final String pairId)
	// =================================================================================================================
	@Test
	public void testGetConfigurationDownloader_happyPath() throws Exception  {
		// test
		ConfigurationExtension configurationExtension =  new ConfigurationExtension(eventHub, platformServices);
		// verify
		RemoteDownloader downloader = configurationExtension.getConfigDownloader("someurl");
		assertNotNull("downloader should not be null", downloader);
		final String expectedUrl = String.format(CONFIGURATION_URL_BASE, "someurl");
		assertEquals(expectedUrl, downloader.url);
		assertEquals("downloader should of type ConfigurationDownloader", ConfigurationDownloader.class, downloader.getClass());
	}

	@Test
	public void testGetConfigurationDownloader_when_NetworkService_ISNull() throws Exception  {
		// test
		platformServices.mockNetworkService = null;
		ConfigurationExtension configurationExtension =  new ConfigurationExtension(eventHub, platformServices);
		// verify
		RemoteDownloader downloader = configurationExtension.getConfigDownloader("someurl");
		assertNull("downloader will be null", downloader);
	}

	// =================================================================================================================
	// void handleGetSdkIdentitiesEvent(final Event event)
	// =================================================================================================================
	@Test
	public void testHandleGetSdkIdentitiesEvent() throws Exception {
		// setup
		beginBasic();
		Event getSDKIdentitiesEvent = new Event.Builder("GetSDKIdentities Event", EventType.CONFIGURATION,
				EventSource.REQUEST_IDENTITY).build();
		eventHub.createSharedState("com.adobe.module.configuration", 0, EventHub.SHARED_STATE_PENDING);


		// test
		configuration.handleGetSdkIdentitiesEvent(getSDKIdentitiesEvent);
		waitForExecutor(configuration.getExecutor());

		// verify
		assertNotNull(configuration.getsdkIdsEventQueue);
		assertEquals(1, configuration.getsdkIdsEventQueue.size());
		assertTrue(configuration.getsdkIdsEventQueue.contains(getSDKIdentitiesEvent));
		assertTrue(configuration.processGetSdkIdsEventWasCalled);
	}

	// =================================================================================================================
	// void processGetSdkIdsEvent()
	// =================================================================================================================
	@Test
	public void testProcessGetSdkIdsEvent_WhenNoEventQueued() throws Exception {
		// setup
		beginBasic();

		// test
		configuration.processGetSdkIdsEvent();

		// verify
		assertEquals(configuration.getsdkIdsEventQueue.size(), 0);
		assertFalse(responseIdentityDispatcher.dispatchAllIdentitiesWasCalled);
	}

	@Test
	public void testProcessGetSdkIdsEvent_when_JSONUtilityServiceNull() throws Exception {
		// setup
		platformServices.fakeJsonUtilityService = null;
		beginBasic();
		Event getSDKIdentitiesEvent = new Event.Builder("GetSDKIdentities Event", EventType.CONFIGURATION,
				EventSource.REQUEST_IDENTITY)
		.setResponsePairID("pairID")
		.build();
		configuration.getsdkIdsEventQueue.add(getSDKIdentitiesEvent);

		// test
		configuration.processGetSdkIdsEvent();

		// verify
		assertEquals(configuration.getsdkIdsEventQueue.size(), 0);
		assertTrue(responseIdentityDispatcher.dispatchAllIdentitiesWasCalled);
		assertEquals("pairID", responseIdentityDispatcher.dispatchAllIdentitiesParameterPairId);
		assertEquals("{}", responseIdentityDispatcher.dispatchAllIdentitiesParametersSdkIdentitiesJson);
	}

	@Test
	public void testProcessGetSdkIdsEvent_WhenSharedStateNotReady() throws Exception {
		// setup
		beginBasic();
		Event getSDKIdentitiesEvent = new Event.Builder("GetSDKIdentities Event", EventType.CONFIGURATION,
				EventSource.REQUEST_IDENTITY)
		.setResponsePairID("pairID")
		.build();
		configuration.getsdkIdsEventQueue.add(getSDKIdentitiesEvent);
		// set all shared state to pending
		eventHub.createSharedState("com.adobe.module.configuration", 0, EventHub.SHARED_STATE_PENDING);
		eventHub.createSharedState("com.adobe.module.analytics", 0, EventHub.SHARED_STATE_PENDING);
		eventHub.createSharedState("com.adobe.module.audience", 0, EventHub.SHARED_STATE_PENDING);
		eventHub.createSharedState("com.adobe.module.target", 0, EventHub.SHARED_STATE_PENDING);
		eventHub.createSharedState("com.adobe.module.identity", 0, EventHub.SHARED_STATE_PENDING);


		// test
		configuration.processGetSdkIdsEvent();

		// verify
		assertEquals(configuration.getsdkIdsEventQueue.size(), 1);
		assertFalse(responseIdentityDispatcher.dispatchAllIdentitiesWasCalled);
	}

	@Test
	public void testProcessGetSdkIdsEvent_WhenAllSharedState() throws Exception {
		// setup
		beginBasic();
		Event getSDKIdentitiesEvent = new Event.Builder("GetSDKIdentities Event", EventType.CONFIGURATION,
				EventSource.REQUEST_IDENTITY)
		.setResponsePairID("pairID")
		.build();
		configuration.getsdkIdsEventQueue.add(getSDKIdentitiesEvent);
		EventData sampleSharedState = new EventData();
		sampleSharedState.putString("someString", "someValue");
		eventHub.createSharedState("com.adobe.module.configuration", 0, sampleSharedState);
		eventHub.createSharedState("com.adobe.module.analytics", 0, sampleSharedState);
		eventHub.createSharedState("com.adobe.module.audience", 0, sampleSharedState);
		eventHub.createSharedState("com.adobe.module.target", 0, sampleSharedState);
		eventHub.createSharedState("com.adobe.module.identity", 0, sampleSharedState);

		// test
		configuration.processGetSdkIdsEvent();

		// verify
		assertEquals(configuration.getsdkIdsEventQueue.size(), 0);
		assertTrue(responseIdentityDispatcher.dispatchAllIdentitiesWasCalled);
		assertEquals("pairID", responseIdentityDispatcher.dispatchAllIdentitiesParameterPairId);
		assertNotNull(responseIdentityDispatcher.dispatchAllIdentitiesParametersSdkIdentitiesJson);
	}

	// =================================================================================================================
	// Setup Methods
	// =================================================================================================================

	private void beginBasic() throws MissingPlatformServicesException {
		configuration =  new TestableConfigurationExtension(eventHub, platformServices);
		responseIdentityDispatcher = configuration.responseIdentityDispatcher;
		responseDispatcher = configuration.responseDispatcher;
		requestDispatcher = configuration.requestDispatcher;

		if (platformServices != null && platformServices.mockSystemInfoService != null) {
			platformServices.mockSystemInfoService.networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
		}
	}

	private void beginWithAppIDInManifest() throws MissingPlatformServicesException {
		mockAppIdInManifest();
		beginBasic();
	}

	private void beginWithAppIDInPersistence() throws MissingPlatformServicesException {
		setAppIDInPersistence();
		beginBasic();
	}

	private void beginWithAppIDInPersistenceAndManifest() throws MissingPlatformServicesException {
		mockAppIdInManifest();
		setAppIDInPersistence();
		beginBasic();
	}

	private void beginWithBundledConfiguration() throws MissingPlatformServicesException {
		mockBundledConfig();
		beginBasic();
	}

	private void beginWithOverridenConfigInPersistence() throws MissingPlatformServicesException {
		setUpdatedConfigInPersistence();
		beginBasic();
	}
	private void beginWithOverriddenConfig_and_AppID_InPersistence() throws MissingPlatformServicesException {
		setUpdatedConfigInPersistence();
		setAppIDInPersistence();
		beginBasic();
	}

	private Event configureWithAppIDEvent(final String appId) {
		EventData data = new EventData();
		data.putString("config.appId", appId);
		Event event = new Event.Builder("mockAppID Event", EventType.CONFIGURATION,
										EventSource.REQUEST_CONTENT).setData(data).build();
		event.setEventNumber(3);
		return event;
	}

	private Event configureWithAppIDInternalEvent(final String appId) {
		EventData data = new EventData();
		data.putString("config.appId", appId);
		data.putBoolean("config.isinternalevent", true);
		Event event = new Event.Builder("mockAppID Event", EventType.CONFIGURATION,
										EventSource.REQUEST_CONTENT).setData(data).build();
		event.setEventNumber(3);
		return event;
	}

	private Event updateConfigEvent(HashMap<String, String> apiConfig) {
		EventData data = new EventData();
		data.putStringMap("config.update", apiConfig);
		Event event = new Event.Builder("mock UpdateConfig Event", EventType.CONFIGURATION,
										EventSource.REQUEST_CONTENT).setData(data).build();
		event.setEventNumber(2);
		return event;
	}

	private Event clearUpdatedConfigEvent() {
		EventData data = new EventData();
		data.putBoolean("config.clearUpdates", true);
		Event event = new Event.Builder("mock clearUpdatedConfig Event", EventType.CONFIGURATION,
										EventSource.REQUEST_CONTENT).setData(data).build();
		event.setEventNumber(3);
		return event;
	}

	private Event bootEvent() {
		return new Event.Builder("EventHub", EventType.HUB, EventSource.BOOTED).build();
	}

	private Event publishConfigEvent() {
		EventData eventData = new EventData();
		eventData.putBoolean("config.getData", true);
		final Event event = new Event.Builder("CONFIGURATION", EventType.CONFIGURATION,
											  EventSource.REQUEST_CONTENT).setData(eventData).setResponsePairID("PairId").build();
		return event;
	}

	// =================================================================================================================
	// Helper Methods
	// =================================================================================================================
	private File createSampleFileWithData(String fileData) {
		File appDirectory = new File(this.getClass().getResource("").getPath() + APP_DIRECTORY);
		appDirectory.mkdir();
		File file = null;

		try {
			file = new File(appDirectory + File.separator + "config.txt");
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(fileData);
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return file;
	}


	private Event configWithFilePathEvent(final String filePath) {
		EventData eventData = new EventData();
		eventData.putString("config.filePath", filePath);
		Event event = new Event.Builder("CONFIGURATION", EventType.CONFIGURATION,
										EventSource.REQUEST_CONTENT).setData(eventData).setPairID("pairId").build();
		event.setEventNumber(4);
		return event;
	}


	private Event configWithAssetPathEvent(final String fileName) {
		EventData eventData = new EventData();
		eventData.putString("config.assetFile", fileName);
		Event event = new Event.Builder("CONFIGURATION", EventType.CONFIGURATION,
										EventSource.REQUEST_CONTENT).setData(eventData).setPairID("pairId").build();
		event.setEventNumber(4);
		return event;
	}

	private void deleteTempAppDirectory() {
		File appDirectory = new File(this.getClass().getResource("").getPath() + APP_DIRECTORY);
		String[]files = appDirectory.list();

		if (files != null) {
			for (String file : files) {
				File currentFile = new File(appDirectory.getPath(), file);
				currentFile.delete();
			}
		}

		appDirectory.delete();
	}

	private void  setUpdatedConfigInPersistence() {
		// set Updated config in local storage
		getConfigDataStore().setString(PERSISTED_OVERRIDDEN_CONFIG, OVERRIDDEN_CONFIG_STRING);
	}

	private void setAppIDInPersistence() {
		// set AppID in local storage
		getConfigDataStore().setString(PERSISTED_APPID, "mockAppID");
	}

	private String getAppIDFromPersistence() {
		// get AppID from local storage
		return getConfigDataStore().getString(PERSISTED_APPID, null);
	}

	private void resetPersistence() {
		getConfigDataStore().removeAll();
	}

	private LocalStorageService.DataStore getConfigDataStore() {
		LocalStorageService localStorageService = platformServices.getLocalStorageService();
		return localStorageService.getDataStore(DATASTORE_KEY);
	}

	private EventData readSharedConfigurationForNextEvent() {
		return configuration.getConfigSharedEventState(CONFIGURATION_STATE_NAME, MockEventCreator.createEventWithVersion(999));
	}

	private void mockBundledConfig() {
		try {
			InputStream stream = new ByteArrayInputStream(CONFIG_JSON_STRING.getBytes("UTF-8"));
			platformServices.getMockSystemInfoService().assetStream = stream;
		} catch (Exception e) {}
	}

	private void mockBundledConfig(final String content) {
		try {
			InputStream stream = new ByteArrayInputStream(content.getBytes("UTF-8"));
			platformServices.getMockSystemInfoService().assetStream = stream;
		} catch (Exception e) {}
	}


	private void mockAppIdInManifest() {
		platformServices.getMockSystemInfoService().setPropertyValue("ADBMobileAppID", "manifestAppID");
	}


}
