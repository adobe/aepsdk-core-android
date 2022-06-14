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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.model.Statement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

class Retry implements TestRule {
	private int numberOfTestAttempts;
	private int currentTestRun;

	public Retry(final int numberOfTestAttempts) {
		this.numberOfTestAttempts = numberOfTestAttempts;
	}

	public Statement apply(final Statement base, final Description description) {
		return statement(base, description);
	}

	private Statement statement(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				Throwable caughtThrowable = null;

				for (int i = 0; i < numberOfTestAttempts; i++) {
					currentTestRun = i + 1;

					try {
						base.evaluate();
						return;
					} catch (Throwable t) {
						caughtThrowable = t;
						System.err.println(description.getDisplayName() + ": run " + currentTestRun + " failed, " +
										   (numberOfTestAttempts - currentTestRun) + " retries remain.");
						System.err.println("test failure caused by: " + caughtThrowable.getLocalizedMessage());
					}
				}

				System.err.println(description.getDisplayName() + ": giving up after " + numberOfTestAttempts + " failures");
				throw caughtThrowable;
			}
		};
	}
}

// TODO fix after Configuration refactor
@RunWith(JUnit4.class)
public class ConfigurationModuleTest extends SystemTest {
	// Retry failed tests up to 2 times
	@org.junit.Rule
	public Retry totalTestCount = new Retry(2);

	@org.junit.Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private static final String RULE_ENGINE_CONSEQUENCE_JSON_ID = "id";
	private static final String RULE_ENGINE_CONSEQUENCE_JSON_TYPE = "type";
	private static final String RULE_ENGINE_CONSEQUENCE_JSON_DETAIL = "detail";
	private static final String RULE_ENGINE_CONSEQUENCE_TRIGGERED = "triggeredconsequence";

	// Config sample keys
	private final static String ANALYTICS_RSID_KEY 	 		= "Analytics.rsids";
	private final static String ANALYTICS_SERVER_KEY  		= "Analytics.server";
	private final static String RULES_URL_KEY				= "rules.url";
	private final static String CUSTOMMODULE_CONFIG_KEY 	= "customModule_ConfigKey";
	private final static String CUSTOMMODULE_CONFIG_KEY_NEW 	= "customModule_ConfigKey_New";
	private final static String CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG			= "config.update";

	// Config sample values
	private final static String DOWNLOADED_RSID 			= "downloaded_rsid";
	private final static String DOWNLOADED_SERVER			= "downloaded_server";
	private final static String NEW_RSID 			        = "new_rsid";
	private final static String NEW_SERVER			        = "new_server";
	private final static String BUNDLED_RSID 			    = "bundled_rsid";
	private final static String BUNDLED_SERVER			    = "bundled_server";
	private final static String UPDATED_RSID 				= "updated_rsid";
	private final static String CUSTOMMODULE_CONFIG_VALUE	= "customModule_ConfigValue";
	private final static String CUSTOMMODULE_CONFIG_VALUE_NEW	= "customModule_ConfigValue_New";
	private final static String CUSTOMMODULE_CONFIG_VALUE_UPDATED = "customModule_ConfigValue_Updated";
	private final static String REMOTE_CONFIG_SERVER = "com.adobe.marketing.mobile.RemoteServerConfig";
	private final static String HERMETIC_CONFIG_SERVER =
		"https://sj1010006201050.corp.adobe.com/path/to/configs/HemeticTestConfigJSONFile";
	private static final String ANALYTICS_TRACK_ACTION = "action";

	private static final String CONFIGURATION_URL_BASE    = "https://assets.adobedtm.com/%s.json";

	private final static String CACHED_RSID 				= "cached_rsid";
	private final static String CACHED_SERVER				= "cached_server";

	private final static String MOCK_BUNDLED_CONFIG		= "{'" + ANALYTICS_RSID_KEY + "':'" + BUNDLED_RSID +
			"','" + ANALYTICS_SERVER_KEY + "':'" + BUNDLED_SERVER + "'}";
	private final static String MOCK_DOWNLOADED_CONFIG		= "{'" + ANALYTICS_RSID_KEY + "':'" + DOWNLOADED_RSID +
			"','" + ANALYTICS_SERVER_KEY + "':'" + DOWNLOADED_SERVER + "'}";
	private final static String MOCK_NEW_DOWNLOADED_CONFIG		= "{'" + ANALYTICS_RSID_KEY + "':'" + NEW_RSID +
			"','" + ANALYTICS_SERVER_KEY + "':'" + NEW_SERVER + "'}";
	private final static String MOCK_CACHED_CONFIG		= "{'" + ANALYTICS_RSID_KEY + "':'" + CACHED_RSID +
			"','" + ANALYTICS_SERVER_KEY + "':'" + CACHED_SERVER + "'}";
	private final static HashMap<String, String> OVERRIDDEN_CONFIG_MAP = new HashMap<String, String> () {
		{
			put(ANALYTICS_RSID_KEY, UPDATED_RSID);
			put(CUSTOMMODULE_CONFIG_KEY, CUSTOMMODULE_CONFIG_VALUE);
		}
	};
	private final static HashMap<String, String> UPDATED_CONFIG_MAP = new HashMap<String, String> () {
		{
			put(ANALYTICS_RSID_KEY, UPDATED_RSID);
			put(CUSTOMMODULE_CONFIG_KEY, CUSTOMMODULE_CONFIG_VALUE_UPDATED);
		}
	};
	private final static HashMap<String, String> UPDATED_CONFIG_MAP_NEW = new HashMap<String, String> () {
		{
			put(CUSTOMMODULE_CONFIG_KEY_NEW, CUSTOMMODULE_CONFIG_VALUE_NEW);
		}
	};
	private final static String OVERRIDDEN_CONFIG_STRING			= "{'" + ANALYTICS_RSID_KEY + "':'" + UPDATED_RSID +
			"','" + CUSTOMMODULE_CONFIG_KEY + "':'" + CUSTOMMODULE_CONFIG_VALUE + "'}";

	private TestableNetworkService testableNetworkService;

	@Before
	public void beforeEach() throws Exception {
		Log.setLoggingService(platformServices.getLoggingService());
		Log.setLogLevel(LoggingMode.VERBOSE);
		testableNetworkService = platformServices.getTestableNetworkService();
	}

	// ===============================================================
	// First Launch Tests
	// ===============================================================

	@Ignore
	@Test
	public void test_FirstLaunch_with_BundledConfiguration() {
		eventHub.ignoreAllStateChangeEvents();
		eventHub.setExpectedEventCount(1);

		setUpWithConfigBundledInAssets();

		waitForThreadsWithFailIfTimedOut(5000);

		List<Event> events =  eventHub.getEvents();
		assertEquals(1, events.size());

		// Test - Configuration Response Event dispatched with correct data
		verifyConfigurationResponseEvent_with_BundledData(events.get(0));

		// Test -  Configuration Shared state is set with correct data
		EventData sharedStateData = getLastConfigurationSharedState();
		assertEquals(BUNDLED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(BUNDLED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
	}

	@Ignore
	@Test
	public void test_FirstLaunch_ConfigureWithFilePath() {
		eventHub.ignoreAllStateChangeEvents();
		eventHub.setExpectedEventCount(2);

		setUpBasic();
		createSampleBundleConfigFile(MOCK_BUNDLED_CONFIG);
		eventHub.dispatch(configureWithFilePathEvent());

		waitForThreadsWithFailIfTimedOut(3000);

		List<Event> events =  eventHub.getEvents();
		assertEquals(2, events.size());

		// Test - Configuration Response Event dispatched with correct data
		verifyConfigurationResponseEvent_with_BundledData(events.get(1));

		// Test -  Configuration Shared state is set with correct data
		EventData sharedStateData = getLastConfigurationSharedState();
		assertEquals(BUNDLED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(BUNDLED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		deleteSampleConfigBundledFile();
	}

	@Ignore
	@Test
	public void test_FirstLaunch_with_AppIDInManifest() {
		// setup network
		testableNetworkService.setExpectedCount(1);
		eventHub.ignoreAllStateChangeEvents();
		eventHub.setExpectedEventCount(2);
		addHermeticServerToManifest();
		setUpWithAppIDInManifest();

		waitForThreadsWithFailIfTimedOut(5000);

		// Test - the events generated after initialization
		List<Event> events =  eventHub.getEvents();
		assertEquals(2, events.size());

		// Event 0: verify configureWithAppId event is generated internally
		verifyConfigureWithAppIDEvent(events.get(0), "manifestAppId");

		// Event 1 : Configuration Response Event
		verifyConfigurationResponseEvent_with_DownloadedData(events.get(1));

		//verify network call
		assertEquals(1, testableNetworkService.waitAndGetCount());
		assertEquals(getExpectedRemoteUrl("manifestAppId"), testableNetworkService.getItem(0).url);
		assertNull(testableNetworkService.getItem(0).connectPayload);
		assertNull(testableNetworkService.getItem(0).requestProperty);
		assertEquals(TestableNetworkService.NetworkRequestType.SYNC, testableNetworkService.getItem(0).type);

		// Test -  Configuration Shared state is set with correct data
		EventData sharedStateData = getLastConfigurationSharedState();
		assertEquals(DOWNLOADED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(DOWNLOADED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));

		assertTrue(checkIfManifestAppIdCacheFileExists());
	}

	@Ignore
	@Test
	public void test_FirstLaunch_AndThen_ConfigureWithAppId_should_TriggerNetworkCall() {
		setUpBasic();

		waitForThreadsWithFailIfTimedOut(5000);
		// Test - no shared state for configuration
		EventData configState = getLastConfigurationSharedState();
		assertNull(configState);

		// setup network
		testableNetworkService.setExpectedCount(1);
		testableNetworkService.setDefaultResponse(MOCK_DOWNLOADED_CONFIG);

		// simulate configureWithAppId Call
		eventHub.dispatch(configureWithAppIDEvent());

		waitForThreadsWithFailIfTimedOut(5000);

		//verify network call
		assertEquals(1, testableNetworkService.waitAndGetCount());
		assertEquals(getExpectedRemoteUrl("appID"), testableNetworkService.getItem(0).url);
		assertNull(testableNetworkService.getItem(0).connectPayload);
		assertNull(testableNetworkService.getItem(0).requestProperty);
		assertEquals(TestableNetworkService.NetworkRequestType.SYNC, testableNetworkService.getItem(0).type);
	}

	@Ignore
	@Test
	public void test_FirstLaunch_AndThen_ConfigureWithAppId_should_SaveCachedFile_And_CreateResponseEvent() {
		setUpBasic();

		// setup network
		testableNetworkService.setExpectedCount(1);
		testableNetworkService.setDefaultResponse(createMockResponse());
		eventHub.setExpectedEventCount(3);

		// simulate configureWithAppId Call
		eventHub.dispatch(configureWithAppIDEvent());

		waitForThreadsWithFailIfTimedOut(5000);

		List<Event> events =  eventHub.getEvents();
		assertEquals(4, events.size());

		// Event 0: verify configureWithAppId event
		verifyConfigureWithAppIDEvent(events.get(0), "appID");

		// Event 1 : State Change Event for EventHub
		// Event 2 : State Change Event before handling booted event
		verifyConfigStateChangeEvent(events.get(2));

		// Event 3 : Configuration Response Event
		verifyConfigurationResponseEvent_with_DownloadedData(events.get(3));

		// Test -  Configuration Shared state is set with correct data
		EventData sharedStateData = getLastConfigurationSharedState();
		assertEquals(DOWNLOADED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(DOWNLOADED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));

		// Test presence of cached File
		assertTrue(checkIfTheCacheFileExists());
	}


	// ====================================================================================
	// Subsequent Launch Tests
	// =====================================================================================\
	@Ignore
	@Test
	public void test_subsequentLaunch_When_AppIdAlreadySet_should_TriggerNetworkCall() {

		setUpWithAppIdInPersistence();
		testableNetworkService.setDefaultResponse(createMockResponse());
		testableNetworkService.setExpectedCount(1);

		waitForThreadsWithFailIfTimedOut(5000);

		//verify network call
		assertEquals(1, testableNetworkService.waitAndGetCount());
		assertEquals(getExpectedRemoteUrl("appID"), testableNetworkService.getItem(0).url);
		assertNull(testableNetworkService.getItem(0).connectPayload);
		Map<String, String> requestProperty = testableNetworkService.getItem(0).requestProperty;
		assertEquals("bytes=68-", requestProperty.get("Range"));
		assertEquals("Wed, 21 Oct 2015 07:28:00 GMT", requestProperty.get("If-Range"));
		assertEquals(TestableNetworkService.NetworkRequestType.SYNC, testableNetworkService.getItem(0).type);
	}

	@Test
	public void test_SetInternalAppID_withOldId_WillNot_RefreshConfiguration() {

		setUpWithAppIdInPersistence();
		testableNetworkService.setDefaultResponse(createMockResponse());
		testableNetworkService.setExpectedCount(1);
		waitForThreadsWithFailIfTimedOut(5000);
		testableNetworkService.reset();

		// test
		configureWithAppIDInternalEvent("OldAppID");
		eventHub.dispatch(configureWithAppIDEvent());

		//verify no network call
		assertEquals(0, testableNetworkService.waitAndGetCount());
	}

	@Ignore
	@Test
	public void test_SetInternalAppID_withSameID_Will_RefreshConfiguration() {

		setUpWithAppIdInPersistence();
		testableNetworkService.setDefaultResponse(createMockResponse());
		testableNetworkService.setExpectedCount(1);
		waitForThreadsWithFailIfTimedOut(5000);
		testableNetworkService.reset();

		// test
		configureWithAppIDInternalEvent("appID");
		eventHub.dispatch(configureWithAppIDEvent());

		waitForThreadsWithFailIfTimedOut(5000);

		//verify no network call
		assertEquals(1, testableNetworkService.waitAndGetCount());
	}

	@Ignore
	@Test
	public void test_subsequentLaunch_When_AppIdAlreadySet_and_networkError_should_ReturnCachedFile() {
		// setup network
		testableNetworkService.setExpectedCount(1);
		testableNetworkService.setDefaultResponse(createMockErrorResponse());
		eventHub.setExpectedEventCount(3);
		eventHub.ignoreAllStateChangeEvents();
		// setup
		setUpWithAppIdInPersistence();

		waitForThreadsWithFailIfTimedOut(5000);

		List<Event> events =  eventHub.getEvents();
		assertEquals(3, events.size());

		// Event 0: verify setAppID internal event
		verifyConfigureWithAppIDEvent(events.get(0), "appID");

		// Check if the initial shared state is loaded with cached configuration
		EventData cachedSharedState = getConfigurationSharedState(0);
		assertEquals(CACHED_SERVER, cachedSharedState.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(CACHED_RSID, cachedSharedState.optString(ANALYTICS_RSID_KEY, null));

		// Event 3 : Configuration Response Event
		verifyConfigurationResponseEvent_with_cachedData(events.get(1));

		// Event 4 : Configuration Response Event is loaded with cache data
		Event event7 = events.get(2);
		assertEquals(EventType.CONFIGURATION, event7.getEventType());
		assertEquals(EventSource.RESPONSE_CONTENT, event7.getEventSource());
		assertEquals(CACHED_SERVER, event7.getData().optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(CACHED_RSID, event7.getData().optString(ANALYTICS_RSID_KEY, null));

		// Test -  Final configuration Shared state is set with correct data
		EventData sharedStateData = getLastConfigurationSharedState();
		assertEquals(CACHED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(CACHED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));

		// Test presence of cached File
		assertTrue(checkIfTheCacheFileExists());
	}

	@Ignore
	@Test
	public void test_subsequentLaunch_When_AppIdAlreadySet_and_newContentFromNetwork_should_DispatchNewContent() {

		// setup network
		testableNetworkService.setExpectedCount(1);
		testableNetworkService.setDefaultResponse(createMockNewContentResponse());
		eventHub.setExpectedEventCount(3);
		eventHub.ignoreAllStateChangeEvents();
		// setup
		setUpWithAppIdInPersistence();

		waitForThreadsWithFailIfTimedOut(5000);

		List<Event> events =  eventHub.getEvents();
		assertEquals(3, events.size());

		// Event 0: verify setAppID internal event
		verifyConfigureWithAppIDEvent(events.get(0), "appID");

		// Check if the initial shared state is loaded with cached configuration
		EventData cachedSharedState = getConfigurationSharedState(0);
		assertEquals(CACHED_SERVER, cachedSharedState.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(CACHED_RSID, cachedSharedState.optString(ANALYTICS_RSID_KEY, null));

		// Event 1 : Configuration Response Event
		verifyConfigurationResponseEvent_with_cachedData(events.get(1));

		// Event 2 : Configuration Response Event is loaded with cache data
		verifyConfigurationResponseEvent_with_NewDownloadedData(events.get(2));

		// Test -  Final configuration Shared state is set with correct data
		EventData sharedStateData = getLastConfigurationSharedState();
		assertEquals(NEW_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(NEW_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));

		// Test presence of cached File
		assertTrue(checkIfTheNewCacheFileExists());
	}

	// Subsequent Launch tests for Bundled configuration and AppIdInManifest are same as its first launch test

	@Ignore
	@Test
	public void test_subsequentLaunch_when_overriddenConfigInPersistence_should_configureWithOverriddenConfig() {

		eventHub.setExpectedEventCount(4);
		// setup
		setUpWithOverriddenConfigInPersistence();

		waitForThreadsWithFailIfTimedOut(5000);

		List<Event> events =  eventHub.getEvents();
		assertEquals(3, events.size());


		// Event 0 : Event Hub Shared State Change
		// Event 1 : State Change Event
		verifyConfigStateChangeEvent(events.get(1));

		// Event 2 : Configuration Response Event
		Event event1 = events.get(2);
		assertEquals(EventType.CONFIGURATION, event1.getEventType());
		assertEquals(EventSource.RESPONSE_CONTENT, event1.getEventSource());
		assertEquals(UPDATED_RSID, event1.getData().optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE, event1.getData().optString(CUSTOMMODULE_CONFIG_KEY, null));


		// Test -  Configuration Shared state is set with correct data
		EventData sharedStateData = getLastConfigurationSharedState();
		assertEquals(UPDATED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE, sharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));
	}

	// ====================================================================================
	// Config Order of Preference Tests
	// =====================================================================================\

	@Ignore
	@Test
	public void
	test_preferences_When_AppIdAlreadySet_and_ProgrammaticConfigCreated_should_overridePrimaryConfigFromAppId() {

		// setup network
		testableNetworkService.setExpectedCount(1);
		testableNetworkService.setDefaultResponse(createMockNoConfigChangeResponse());
		eventHub.setExpectedEventCount(8);
		// setup
		setUpWithAppIdInPersistence();

		waitForThreadsWithFailIfTimedOut(5000);

		List<Event> events =  eventHub.getEvents();
		assertEquals(6, events.size());

		eventHub.clearEvents();
		eventHub.setExpectedEventCount(4);
		eventHub.dispatch(updateConfigurationInternalEvent());

		List<Event> eventsList2 =  eventHub.getEvents();
		assertEquals(3, eventsList2.size());

		// Test -  the first configuration Shared state is set with cached data
		EventData firstSharedStateData = getConfigurationSharedState(0);
		assertEquals(CACHED_SERVER, firstSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(CACHED_RSID, firstSharedStateData.optString(ANALYTICS_RSID_KEY, null));

		// Test -  Configuration Shared state is set with correct data
		EventData sharedStateData = getLastConfigurationSharedState();
		assertEquals(CACHED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(UPDATED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE, sharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));


		// Test presence of cached File
		assertTrue(checkIfTheCacheFileExists());
	}

	@Ignore
	@Test
	public void
	test_preferences_When_BundledConfiguration_and_ProgrammaticConfigCreated_should_overridePrimaryConfigFromBundle() {
		// setup
		setUpWithConfigBundledInAssets();
		waitForThreadsWithFailIfTimedOut(5000);
		eventHub.clearEvents();

		eventHub.dispatch(updateConfigurationInternalEvent());
		waitForThreadsWithFailIfTimedOut(5000);
		List<Event> events =  eventHub.getEvents();
		assertEquals(3, events.size());
		eventHub.clearEvents();

		// Test -  Configuration Shared state is set with correct data
		EventData sharedStateData = getLastConfigurationSharedState();
		assertEquals(BUNDLED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(UPDATED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE, sharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test -  the first configuration Shared state is set with cached data
		EventData firstSharedStateData = getConfigurationSharedState(0);
		assertEquals(BUNDLED_SERVER, firstSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(BUNDLED_RSID, firstSharedStateData.optString(ANALYTICS_RSID_KEY, null));

		// Test presence of cached File
		assertFalse(checkIfTheCacheFileExists());
	}

	@Ignore
	@Test
	public void test_preferences_When_everythingSetup_AppIdTakesPreference_overridden_by_programmedConfig() {

		// setup network
		testableNetworkService.setExpectedCount(1);
		testableNetworkService.setDefaultResponse(createMockNoConfigChangeResponse());
		eventHub.setExpectedEventCount(5);
		// setup
		setUpWithEverything();

		waitForThreadsWithFailIfTimedOut(5000);

		List<Event> events =  eventHub.getEvents();
		assertEquals(6, events.size());

		// Test -  the first configuration Shared state is set with cached data
		EventData firstSharedStateData = getConfigurationSharedState(0);
		assertEquals(CACHED_SERVER, firstSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(UPDATED_RSID, firstSharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE, firstSharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test -  Configuration Shared state is set with correct data
		EventData sharedStateData = getLastConfigurationSharedState();
		assertEquals(CACHED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(UPDATED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE, sharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));


		// Test presence of cached File
		assertTrue(checkIfTheCacheFileExists());
	}

	@Ignore
	@Test
	public void
	test_preferences_When_AppIdAlreadyInPersistence_and_ConfigureWithAppIdCalled_AndThen_ConfigFilePathIsCalled() {

		eventHub.ignoreAllStateChangeEvents();
		eventHub.ignoreEvents(EventType.CONFIGURATION, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.HUB, EventSource.BOOTED);

		// setup network
		testableNetworkService.setExpectedCount(1);
		testableNetworkService.setDefaultResponse(createMockNewContentResponse());
		eventHub.setExpectedEventCount(4);
		// setup
		setUpWithAppIdInPersistence();
		waitForThreadsWithFailIfTimedOut(5000);
		eventHub.clearEvents();

		createSampleBundleConfigFile(MOCK_BUNDLED_CONFIG);
		eventHub.dispatch(configureWithAppIDEvent());
		eventHub.dispatch(configureWithFilePathEvent());

		waitForThreadsWithFailIfTimedOut(5000);

		List<Event> events =  eventHub.getEvents();
		assertEquals(2, events.size());

		// Verify dispatch Event 1
		Event event1 = events.get(0);
		verifyConfigurationResponseEvent_with_NewDownloadedData(event1);

		// Verify dispatch Event 2
		Event event2 = events.get(1);
		verifyConfigurationResponseEvent_with_BundledData(event2);


		// Test -  the first configuration Shared state is set with cached data
		EventData firstSharedStateData = getConfigurationSharedState(0);
		assertEquals(CACHED_SERVER, firstSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(CACHED_RSID, firstSharedStateData.optString(ANALYTICS_RSID_KEY, null));

		// Test -  Configuration Shared state is set with correct data
		EventData sharedStateData = getLastConfigurationSharedState();
		assertEquals(BUNDLED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(BUNDLED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));

		// Test presence of cached File
		assertTrue(checkIfTheNewCacheFileExists());
		deleteSampleConfigBundledFile();
	}

	@Ignore
	@Test
	public void
	test_preferences_When_AppIdAlreadyInPersistence_and_ConfigFilePathIsCalled_AndThen_ConfigureWithAppIdCalled() {

		eventHub.ignoreAllStateChangeEvents();
		eventHub.ignoreEvents(EventType.CONFIGURATION, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.HUB, EventSource.BOOTED);

		// setup network
		testableNetworkService.setExpectedCount(1);
		testableNetworkService.setDefaultResponse(createMockNewContentResponse());
		eventHub.setExpectedEventCount(4);
		// setup
		setUpWithAppIdInPersistence();
		waitForThreadsWithFailIfTimedOut(5000);
		eventHub.clearEvents();

		createSampleBundleConfigFile(MOCK_BUNDLED_CONFIG);
		eventHub.dispatch(configureWithFilePathEvent());
		eventHub.dispatch(configureWithAppIDEvent());

		waitForThreadsWithFailIfTimedOut(5000);

		List<Event> events =  eventHub.getEvents();
		assertEquals(2, events.size());


		// Verify dispatch Event 1
		Event event1 = events.get(0);
		verifyConfigurationResponseEvent_with_BundledData(event1);

		// Verify dispatch Event 2
		Event event2 = events.get(1);
		verifyConfigurationResponseEvent_with_NewDownloadedData(event2);


		// Test -  the first configuration Shared state is set with cached data
		EventData firstSharedStateData = getConfigurationSharedState(0);
		assertEquals(CACHED_SERVER, firstSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(CACHED_RSID, firstSharedStateData.optString(ANALYTICS_RSID_KEY, null));

		// Test -  Configuration Shared state is set with correct data
		EventData sharedStateData = getLastConfigurationSharedState();
		assertEquals(NEW_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(NEW_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));

		// Test presence of cached File
		assertTrue(checkIfTheNewCacheFileExists());
		deleteSampleConfigBundledFile();
	}

	@Ignore
	@Test
	public void test_preferences_OverriddenConfig_and_UpdateConfiguration_AndThen_ClearUpdatedConfigCalled() {
		// setup
		setUpWithOverriddenConfigInPersistence();

		waitForThreadsWithFailIfTimedOut(5000);

		eventHub.clearEvents();
		eventHub.setExpectedEventCount(3);
		eventHub.dispatch(updateConfigurationInternalEvent(UPDATED_CONFIG_MAP));

		List<Event> eventsList2 =  eventHub.getEvents();
		assertEquals(3, eventsList2.size());

		eventHub.clearEvents();
		eventHub.setExpectedEventCount(3);
		eventHub.dispatch(clearUpdatedConfigurationInternalEvent());

		List<Event> eventsList3 =  eventHub.getEvents();
		assertEquals(3, eventsList3.size());
		eventHub.clearEvents();

		// Test -  the first configuration Shared state is set with cached data
		EventData firstSharedStateData = getConfigurationSharedState(0);
		assertEquals(UPDATED_RSID, firstSharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE, firstSharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test -  Configuration Shared state is set with updated config data
		EventData sharedStateData = getConfigurationSharedState(4);
		assertEquals(UPDATED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE_UPDATED, sharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test -  Configuration Shared state is set to cached data after clearing updated configuration
		EventData clearedSharedStateData = getLastConfigurationSharedState();
		assertTrue(clearedSharedStateData.isEmpty());

		// Test presence of cached File
		assertFalse(checkIfTheCacheFileExists());
	}

	@Ignore
	@Test
	public void
	test_preferences_When_AppIdAlreadyInPersistence_and_UpdateConfiguration_AndThen_ClearUpdatedConfigCalled() {
		// setup network
		testableNetworkService.setExpectedCount(1);
		testableNetworkService.setDefaultResponse(createMockNoConfigChangeResponse());
		eventHub.setExpectedEventCount(5);
		// setup
		setUpWithAppIdInPersistence();

		waitForThreadsWithFailIfTimedOut(5000);

		eventHub.clearEvents();
		eventHub.setExpectedEventCount(3);
		eventHub.dispatch(updateConfigurationInternalEvent(UPDATED_CONFIG_MAP));

		List<Event> eventsList2 =  eventHub.getEvents();
		assertEquals(3, eventsList2.size());

		eventHub.clearEvents();
		eventHub.setExpectedEventCount(3);
		eventHub.dispatch(clearUpdatedConfigurationInternalEvent());

		List<Event> eventsList3 =  eventHub.getEvents();
		assertEquals(3, eventsList3.size());
		eventHub.clearEvents();

		// Test -  the first configuration Shared state is set with cached data
		EventData firstSharedStateData = getConfigurationSharedState(0);
		assertEquals(CACHED_SERVER, firstSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(CACHED_RSID, firstSharedStateData.optString(ANALYTICS_RSID_KEY, null));

		// Test -  Configuration Shared state is set with updated config data
		EventData sharedStateData = getConfigurationSharedState(7);
		assertEquals(CACHED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(UPDATED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE_UPDATED, sharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test -  Configuration Shared state is set to cached data after clearing updated configuration
		EventData clearedSharedStateData = getLastConfigurationSharedState();
		assertEquals(CACHED_SERVER, clearedSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(CACHED_RSID, clearedSharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertNull(clearedSharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test presence of cached File
		assertTrue(checkIfTheCacheFileExists());
	}

	@Ignore
	@Test
	public void
	test_preferences_When_AppIdAlreadyInPersistence_and_overridden_by_programmedConfig_and_UpdateConfiguration_AndThen_ClearUpdatedConfigCalled() {

		// setup network
		testableNetworkService.setExpectedCount(1);
		testableNetworkService.setDefaultResponse(createMockNoConfigChangeResponse());
		eventHub.setExpectedEventCount(5);
		// setup
		setUpWithAppIDandOveriddenConfigInPersistence();

		waitForThreadsWithFailIfTimedOut(5000);

		eventHub.clearEvents();
		eventHub.setExpectedEventCount(3);
		eventHub.dispatch(updateConfigurationInternalEvent(UPDATED_CONFIG_MAP));

		List<Event> eventsList2 =  eventHub.getEvents();
		assertEquals(3, eventsList2.size());

		eventHub.clearEvents();
		eventHub.setExpectedEventCount(3);
		eventHub.dispatch(clearUpdatedConfigurationInternalEvent());

		List<Event> eventsList3 =  eventHub.getEvents();
		assertEquals(3, eventsList3.size());
		eventHub.clearEvents();

		// Test -  the first configuration Shared state is set with cached data
		EventData firstSharedStateData = getConfigurationSharedState(0);
		assertEquals(CACHED_SERVER, firstSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(UPDATED_RSID, firstSharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE, firstSharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test -  Configuration Shared state is set with updated config data
		EventData sharedStateData = getConfigurationSharedState(7);
		assertEquals(CACHED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(UPDATED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE_UPDATED, sharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test -  Configuration Shared state is set to cached data after clearing updated configuration
		EventData clearedSharedStateData = getLastConfigurationSharedState();
		assertEquals(CACHED_SERVER, clearedSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(CACHED_RSID, clearedSharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertNull(clearedSharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test presence of cached File
		assertTrue(checkIfTheCacheFileExists());
	}

	@Ignore
	@Test
	public void test_preferences_When_BundledConfiguration_and_UpdatedConfiguration_AndThen_ClearUpdatedConfigCalled() {
		// setup
		setUpWithConfigBundledInAssets();
		waitForThreadsWithFailIfTimedOut(5000);
		eventHub.clearEvents();

		eventHub.dispatch(updateConfigurationInternalEvent(UPDATED_CONFIG_MAP));
		waitForThreadsWithFailIfTimedOut(5000);
		List<Event> events =  eventHub.getEvents();
		assertEquals(3, events.size());
		eventHub.clearEvents();

		eventHub.dispatch(clearUpdatedConfigurationInternalEvent());
		waitForThreadsWithFailIfTimedOut(5000);
		List<Event> events2 =  eventHub.getEvents();
		assertEquals(3, events2.size());
		eventHub.clearEvents();

		// Test -  the first configuration Shared state is set with cached data
		EventData firstSharedStateData = getConfigurationSharedState(0);
		assertEquals(BUNDLED_SERVER, firstSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(BUNDLED_RSID, firstSharedStateData.optString(ANALYTICS_RSID_KEY, null));

		// Test -  Configuration Shared state is set with updated config data
		EventData sharedStateData = getConfigurationSharedState(4);
		assertEquals(BUNDLED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(UPDATED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE_UPDATED, sharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test -  Configuration Shared state is set to cached data after clearing updated configuration
		EventData clearedSharedStateData = getLastConfigurationSharedState();
		assertEquals(BUNDLED_SERVER, clearedSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(BUNDLED_RSID, clearedSharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertNull(clearedSharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test presence of cached File
		assertFalse(checkIfTheCacheFileExists());
	}

	@Ignore
	@Test
	public void
	test_preferences_When_BundledConfiguration_and_overridden_by_programmedConfig_and_UpdatedConfiguration_AndThen_ClearUpdatedConfigCalled() {
		// setup
		setUpWithConfigBundledInAssetsAndOverriddenConfigInPersistence();
		waitForThreadsWithFailIfTimedOut(5000);
		eventHub.clearEvents();

		eventHub.dispatch(updateConfigurationInternalEvent(UPDATED_CONFIG_MAP));
		waitForThreadsWithFailIfTimedOut(5000);
		List<Event> events =  eventHub.getEvents();
		assertEquals(3, events.size());
		eventHub.clearEvents();

		eventHub.dispatch(clearUpdatedConfigurationInternalEvent());
		waitForThreadsWithFailIfTimedOut(5000);
		List<Event> events2 =  eventHub.getEvents();
		assertEquals(3, events2.size());
		eventHub.clearEvents();

		// Test -  the first configuration Shared state is set with cached data
		EventData firstSharedStateData = getConfigurationSharedState(0);
		assertEquals(BUNDLED_SERVER, firstSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(UPDATED_RSID, firstSharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE, firstSharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));


		// Test -  Configuration Shared state is set with updated config data
		EventData sharedStateData = getConfigurationSharedState(4);
		assertEquals(BUNDLED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(UPDATED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE_UPDATED, sharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test -  Configuration Shared state is set to cached data after clearing updated configuration
		EventData clearedSharedStateData = getLastConfigurationSharedState();
		assertEquals(BUNDLED_SERVER, clearedSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(BUNDLED_RSID, clearedSharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertNull(clearedSharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test presence of cached File
		assertFalse(checkIfTheCacheFileExists());
	}

	@Ignore
	@Test
	public void
	test_preferences_When_everythingSetup_and_overridden_by_programmedConfig_AndThen_ClearUpdatedConfigCalled() {

		// setup network
		testableNetworkService.setExpectedCount(1);
		testableNetworkService.setDefaultResponse(createMockNoConfigChangeResponse());
		eventHub.setExpectedEventCount(5);
		// setup
		setUpWithEverything();

		waitForThreadsWithFailIfTimedOut(5000);

		List<Event> eventsList =  eventHub.getEvents();
		assertEquals(6, eventsList.size());

		eventHub.clearEvents();
		eventHub.setExpectedEventCount(3);
		eventHub.dispatch(updateConfigurationInternalEvent(UPDATED_CONFIG_MAP));

		List<Event> eventsList2 =  eventHub.getEvents();
		assertEquals(3, eventsList2.size());

		eventHub.clearEvents();
		eventHub.setExpectedEventCount(3);
		eventHub.dispatch(clearUpdatedConfigurationInternalEvent());

		List<Event> eventsList3 =  eventHub.getEvents();
		assertEquals(3, eventsList3.size());
		eventHub.clearEvents();

		// Test -  the first configuration Shared state is set with cached data
		EventData firstSharedStateData = getConfigurationSharedState(0);
		assertEquals(CACHED_SERVER, firstSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(UPDATED_RSID, firstSharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE, firstSharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test -  Configuration Shared state is set with updated config data
		EventData sharedStateData = getConfigurationSharedState(7);
		assertEquals(CACHED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(UPDATED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE_UPDATED, sharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test -  Configuration Shared state is set to cached data after clearing updated configuration
		EventData clearedSharedStateData = getLastConfigurationSharedState();
		assertEquals(CACHED_SERVER, clearedSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(CACHED_RSID, clearedSharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertNull(clearedSharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test presence of cached File
		assertTrue(checkIfTheCacheFileExists());
	}

	@Ignore
	@Test
	public void test_preferences_When_UpdateConfiguration_AndThen_ClearUpdatedConfig_AndThen_UpdateConfigurationCalled() {

		// setup network
		testableNetworkService.setExpectedCount(1);
		testableNetworkService.setDefaultResponse(createMockNoConfigChangeResponse());
		eventHub.setExpectedEventCount(5);
		// setup
		setUpWithEverything();

		waitForThreadsWithFailIfTimedOut(5000);

		List<Event> eventsList =  eventHub.getEvents();
		assertEquals(6, eventsList.size());

		eventHub.clearEvents();
		eventHub.setExpectedEventCount(3);
		eventHub.dispatch(updateConfigurationInternalEvent(UPDATED_CONFIG_MAP));

		List<Event> eventsList2 =  eventHub.getEvents();
		assertEquals(3, eventsList2.size());

		eventHub.clearEvents();
		eventHub.setExpectedEventCount(3);
		eventHub.dispatch(clearUpdatedConfigurationInternalEvent());

		List<Event> eventsList3 =  eventHub.getEvents();
		assertEquals(3, eventsList3.size());

		eventHub.clearEvents();
		eventHub.setExpectedEventCount(3);
		eventHub.dispatch(updateConfigurationInternalEvent(UPDATED_CONFIG_MAP_NEW));

		List<Event> eventsList4 =  eventHub.getEvents();
		assertEquals(3, eventsList4.size());

		// Test -  the first configuration Shared state is set with cached data
		EventData firstSharedStateData = getConfigurationSharedState(0);
		assertEquals(CACHED_SERVER, firstSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(UPDATED_RSID, firstSharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE, firstSharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test -  Configuration Shared state is set with updated config data
		EventData sharedStateData = getConfigurationSharedState(7);
		assertEquals(CACHED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(UPDATED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE_UPDATED, sharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test -  Configuration Shared state is set to cached data after clearing updated configuration
		EventData clearedSharedStateData = getConfigurationSharedState(10);
		assertEquals(CACHED_SERVER, clearedSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(CACHED_RSID, clearedSharedStateData.optString(ANALYTICS_RSID_KEY, null));

		// Test - Configuration Shared state is set with new updated config data
		EventData updatedSharedStateData = getLastConfigurationSharedState();
		assertEquals(CACHED_SERVER, updatedSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(CACHED_RSID, updatedSharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE_NEW, updatedSharedStateData.optString(CUSTOMMODULE_CONFIG_KEY_NEW, null));
		assertNull(clearedSharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));

		// Test presence of cached File
		assertTrue(checkIfTheCacheFileExists());
	}


	// ====================================================================================
	// Delayed Response Tests
	// =====================================================================================\

	@Ignore
	@Test
	public void test_delayedResponse_FirstLaunch_BundledConfig_ConfigureWithAppId_and_UpdateConfig() {

		setUpWithConfigBundledInAssets();
		eventHub.ignoreAllStateChangeEvents();
		testableNetworkService.setDefaultResponse(createMockResponseWithDelay());
		testableNetworkService.setExpectedCount(6);
		eventHub.setExpectedEventCount(4);

		eventHub.dispatch(configureWithAppIDEvent());
		eventHub.dispatch(updateConfigurationInternalEvent());

		waitForThreadsWithFailIfTimedOut(5000);

		// Verify the number of network calls made
		assertEquals(1, testableNetworkService.waitAndGetCount(1000));

		// Verify the number of events
		List<Event> events =  eventHub.getEvents();
		assertEquals(4, events.size());

		// verify network request
		assertEquals(getExpectedRemoteUrl("appID"), testableNetworkService.getItem(0).url);
		assertNull(testableNetworkService.getItem(0).connectPayload);
		assertEquals(TestableNetworkService.NetworkRequestType.SYNC, testableNetworkService.getItem(0).type);


		// Test -  the first configuration Shared state is set with cached data
		EventData firstSharedStateData = getConfigurationSharedState(0);
		assertEquals(BUNDLED_SERVER, firstSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(BUNDLED_RSID, firstSharedStateData.optString(ANALYTICS_RSID_KEY, null));

		// Test -  Configuration Shared state is set with correct data
		EventData sharedStateData = getLastConfigurationSharedState();
		assertEquals(UPDATED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE, sharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));
	}

	@Ignore
	@Test
	public void test_delayedResponse_When_AppIdAlreadySet_ConfigureWithAppId_and_UpdateConfig() {

		setUpWithAppIdInPersistence();
		eventHub.ignoreAllStateChangeEvents();
		testableNetworkService.setDefaultResponse(createMockNoConfigChangeResponseWithDelay());
		testableNetworkService.setExpectedCount(6);
		eventHub.setExpectedEventCount(5);

		eventHub.dispatch(configureWithAppIDEvent());
		eventHub.dispatch(updateConfigurationInternalEvent());

		waitForThreadsWithFailIfTimedOut(5000);

		// Verify the number of network calls made
		assertEquals(2, testableNetworkService.waitAndGetCount(1000));

		// Verify the number of events
		List<Event> events =  eventHub.getEvents();
		assertEquals(7, events.size());

		// verify network request
		assertEquals(getExpectedRemoteUrl("appID"), testableNetworkService.getItem(0).url);
		assertNull(testableNetworkService.getItem(0).connectPayload);
		assertEquals(TestableNetworkService.NetworkRequestType.SYNC, testableNetworkService.getItem(0).type);


		// Test -  the first configuration Shared state is set with cached data
		EventData firstSharedStateData = getConfigurationSharedState(0);
		assertEquals(CACHED_SERVER, firstSharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(CACHED_RSID, firstSharedStateData.optString(ANALYTICS_RSID_KEY, null));

		// Test -  Configuration Shared state is set with correct data
		EventData sharedStateData = getLastConfigurationSharedState();
		assertEquals(CACHED_SERVER, sharedStateData.optString(ANALYTICS_SERVER_KEY, null));
		assertEquals(UPDATED_RSID, sharedStateData.optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CUSTOMMODULE_CONFIG_VALUE, sharedStateData.optString(CUSTOMMODULE_CONFIG_KEY, null));
	}

	@Ignore
	@Test
	public void test_getPrivacyStatus_returnsValueInCallback() throws Exception {
		// setup event hub
		final String PRIVACY_KEY = "global.privacy";
		eventHub.ignoreAllStateChangeEvents();
		eventHub.ignoreEvents(EventType.CONFIGURATION, EventSource.REQUEST_CONTENT);
		setUpWithEverything();

		eventHub.getEvents(2);

		// setup callback method
		final CountDownLatch latch = new CountDownLatch(4);
		final List<MobilePrivacyStatus> status = new ArrayList<MobilePrivacyStatus>();
		AdobeCallback<MobilePrivacyStatus> callback = new AdobeCallback<MobilePrivacyStatus>() {
			@Override
			public void call(final MobilePrivacyStatus value) {
				status.add(value);
				latch.countDown();
			}
		};

		// Test -  update and request privacy status property
		updateConfiguration(new HashMap<String, String>() {
			{
				put(PRIVACY_KEY, "optedin");
			}
		});
		requestPrivacyStatus(callback);
		eventHub.getEvents(4);
		eventHub.clearEvents();
		updateConfiguration(new HashMap<String, String>() {
			{
				put(PRIVACY_KEY, "optedout");
			}
		});
		requestPrivacyStatus(callback);
		eventHub.getEvents(2);
		eventHub.clearEvents();
		updateConfiguration(new HashMap<String, String>() {
			{
				put(PRIVACY_KEY, "optunknown");
			}
		});
		requestPrivacyStatus(callback);
		eventHub.getEvents(2);
		eventHub.clearEvents();
		updateConfiguration(new HashMap<String, String>() {
			{
				put(PRIVACY_KEY, null);
			}
		});
		requestPrivacyStatus(callback);
		eventHub.getEvents(2);
		eventHub.clearEvents();
		latch.await(2, TimeUnit.SECONDS);

		assertEquals(MobilePrivacyStatus.OPT_IN, status.get(0));
		assertEquals(MobilePrivacyStatus.OPT_OUT, status.get(1));
		assertEquals(MobilePrivacyStatus.UNKNOWN, status.get(2));
		assertEquals(MobilePrivacyStatus.UNKNOWN, status.get(3));
	}

	// =============================================================================
	// Rules Download/Registration
	// =============================================================================
	@Test
	public void configEvent_WithNoRulesUrl_ShouldNotTriggerNetworkConnect() {
		eventHub.setExpectedEventCount(1);
		setUpBasic();
		testableNetworkService.setExpectedCount(0);
		triggerRulesDownloadWithEvent(null);
		waitForThreadsWithFailIfTimedOut(5000);
		assertEquals(0, testableNetworkService.waitAndGetCount());
	}

	@Ignore
	@Test
	public void configEvent_WithValidRulesUrl_ShouldTriggerNetworkRequest() throws Exception {
		setUpBasic();

		//Setup
		testableNetworkService.setExpectedCount(1);
		//setup network service to return contents from resource file
		setupNetWorkService("RulesEngineTest_Rules_ModuleTest1.zip", new Date(), 1, HttpURLConnection.HTTP_OK);

		//Send config update
		triggerRulesDownloadWithEvent("http://hello.rules");
		waitForThreadsWithFailIfTimedOut(5000);

		//verify
		assertEquals(1, testableNetworkService.waitAndGetCount());
	}

	@Test
	@Ignore
	public void ruleMatch_WithValidRuleWithOneConsequenceSetup_ShouldDispatchOneEventIfRuleMatches() throws Exception {
		//Setup
		eventHub.setExpectedEventCount(1);
		eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT);
		eventHub.ignoreEvents(EventType.HUB, EventSource.SHARED_STATE);

		//setup network service to return contents from resource file
		setupNetWorkService("RulesEngineTest_Rules_ModuleTest1.zip", new Date(), 1, HttpURLConnection.HTTP_OK);
		setupSystemInfoService(temporaryFolder.newFolder());
		setUpBasic();

		triggerRulesDownloadWithEvent("http://hello.rules");

		// Wait and clear off the loaded consequence event
		waitForThreadsWithFailIfTimedOut(5000);
		eventHub.clearEvents();

		//Setup lifecycle shared state
		Map<String, Object> lifecycleSharedState = new HashMap<String, Object>();
		lifecycleSharedState.put("lckey", "lcvalue");
		createLifecycleSharedState(lifecycleSharedState);

		waitForThreadsWithFailIfTimedOut(5000);

		//Setup and dispatch analytics event
		dispatchAnalyticsEvent();
		waitForThreadsWithFailIfTimedOut(5000);

		//Verify
		List<Event> events =  eventHub.getEvents();
		Log.debug("TEST", "total events %d", events.size());
		assertEquals(1, events.size());
		assertEquals("48181acd22b3edaebc8a447868a7df7ce629920a",
					 extractConsequenceIdFromConsequenceEvent(events.get(0)));
		assertEquals("iam",
					 extractConsequenceTypeFromConsequenceEvent(events.get(0)));

		final Map<String, Object> detail = extractConsequenceDetailFromConsequenceEvent(events.get(0));
		assertEquals("48181acd22b3edaebc8a447868a7df7ce629920a.html", detail.get("html"));
		assertEquals("fullscreen", detail.get("template"));
	}

	@Test
	@Ignore
	public void ruleMatch_WithValidRuleWithTwoConsequenceSetup_ShouldDispatchTwoEventsIfRuleMatches() throws Exception {
		//Setup
		eventHub.setExpectedEventCount(2);
		eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT);
		eventHub.ignoreEvents(EventType.HUB, EventSource.SHARED_STATE);

		//setup network service to return contents from resource file
		setupNetWorkService("RulesEngineTest_Rules_ModuleTest2.zip", new Date(), 1, HttpURLConnection.HTTP_OK);
		setupSystemInfoService(temporaryFolder.newFolder());
		setUpBasic();

		//Setup config response content event
		triggerRulesDownloadWithEvent("http://hello.rules");

		// Wait and clear off the loaded consequence event
		waitForThreadsWithFailIfTimedOut(5000);
		eventHub.clearEvents();

		//Setup lifecycle shared state
		Map<String, Object> lifecycleSharedState = new HashMap<String, Object>();
		lifecycleSharedState.put("lckey", "lcvalue");
		createLifecycleSharedState(lifecycleSharedState);

		waitForThreadsWithFailIfTimedOut(5000);

		//Setup and dispatch analytics event
		dispatchAnalyticsEvent();
		waitForThreadsWithFailIfTimedOut(5000);

		//Verify
		List<Event> events =  eventHub.getEvents();
		assertEquals(2, events.size());

		//Verify the first consequence event
		assertEquals("48181acd22b3edaebc8a447868a7df7ce629920a",
					 extractConsequenceIdFromConsequenceEvent(events.get(0)));
		assertEquals("iam",
					 extractConsequenceTypeFromConsequenceEvent(events.get(0)));
		final Map<String, Object> consequenceEvent1Detail = extractConsequenceDetailFromConsequenceEvent(events.get(0));
		assertEquals("48181acd22b3edaebc8a447868a7df7ce629920a.html", consequenceEvent1Detail.get("html"));
		assertEquals("fullscreen", consequenceEvent1Detail.get("template"));

		//Verify the second consequence event
		assertEquals("48181acd22b3edaebc8a447868a7df7ce6299234",
					 extractConsequenceIdFromConsequenceEvent(events.get(1)));
		assertEquals("iam",
					 extractConsequenceTypeFromConsequenceEvent(events.get(1)));
		final Map<String, Object> consequenceEvent2Detail = extractConsequenceDetailFromConsequenceEvent(events.get(1));
		assertEquals("48181acd22b3edaebc8a447868a7df7ce6299234.html", consequenceEvent2Detail.get("html"));
		assertEquals("fullscreen", consequenceEvent2Detail.get("template"));
	}

	@Test
	public void ruleMatch_WithValidRuleWithTwoConsequenceSetup_ShouldDispatchZeroEventsIfRuleDoesNotMatch() throws
		Exception {
		//Setup
		eventHub.setExpectedEventCount(0);
		eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT);

		//setup network service to return contents from resource file
		setupNetWorkService("RulesEngineTest_Rules_ModuleTest2.zip", new Date(), 1, HttpURLConnection.HTTP_OK);
		setupSystemInfoService(temporaryFolder.newFolder());
		setUpBasic();

		//Setup config response content event
		triggerRulesDownloadWithEvent("http://hello.rules");

		// Wait and clear off the loaded consequence event
		waitForThreadsWithFailIfTimedOut(5000);
		eventHub.clearEvents();

		waitForThreadsWithFailIfTimedOut(5000);

		//Setup and dispatch analytics event
		//The analytics data will match the rule, but the other conditions will not match,
		//failing the rule evaluation
		dispatchAnalyticsEvent();
		waitForThreadsWithFailIfTimedOut(5000);


		//Verify
		List<Event> events =  eventHub.getEvents();
		assertEquals(0, events.size());
	}

	@Test
	public void analyticsRequestEvent_WithRulesUrlDownloadReturning404() throws Exception {
		//Setup
		eventHub.setExpectedEventCount(1);
		eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT);
		eventHub.ignoreEvents(EventType.HUB, EventSource.SHARED_STATE);

		//Setup network service
		setupNetWorkService("RulesEngineTest_Rules_ModuleTest1.zip", new Date(), 1,
							HttpURLConnection.HTTP_NOT_FOUND);

		//Setup mock system service
		setupSystemInfoService(temporaryFolder.newFolder());
		setUpBasic();

		//Setup config response content event
		triggerRulesDownloadWithEvent("http://hello.rules");

		// Wait and clear off the loaded consequence event
		waitForThreadsWithFailIfTimedOut(5000);
		eventHub.clearEvents();

		//Setup lifecycle shared state
		Map<String, Object> lifecycleSharedState = new HashMap<String, Object>();
		lifecycleSharedState.put("lckey", "lcvalue");
		createLifecycleSharedState(lifecycleSharedState);

		waitForThreadsWithFailIfTimedOut(5000);

		//Setup and dispatch analytics event
		dispatchAnalyticsEvent();
		waitForThreadsWithFailIfTimedOut(5000);

		//Verify
		List<Event> events =  eventHub.getEvents();
		assertEquals("Should not dispatch any event, since rules data was not downloaded!", 0, events.size());
	}

	@Ignore
	@Test
	public void rules_with_same_url_not_download_within_time_sec() throws Exception {
		setupNetWorkService("RulesEngineTest_Rules_ModuleTest1.zip", new Date(), 1, HttpURLConnection.HTTP_OK);
		setupSystemInfoService(temporaryFolder.newFolder());
		setUpBasic();
		triggerRulesDownloadWithEvent("http://hello.rules");
		int count = testableNetworkService.waitAndGetCount();
		assertEquals(1, count);
		testableNetworkService.clearNetworkRequestList();
		eventHub.clearEvents();
		ConfigurationExtension.NOT_DOWNLOAD_RULES_WITHIN_TIME_SEC = 2;
		super.sleep(1000);
		triggerRulesDownloadWithEvent("http://hello.rules");
		super.sleep(EVENTHUB_WAIT_MS);
		count = testableNetworkService.waitAndGetCount();
		assertEquals(0, count);
	}

	@Ignore
	@Test
	public void rules_with_same_url_will_download_when_timeout() throws Exception {
		setupNetWorkService("RulesEngineTest_Rules_ModuleTest1.zip", new Date(), 1, HttpURLConnection.HTTP_OK);
		setupSystemInfoService(temporaryFolder.newFolder());
		setUpBasic();
		triggerRulesDownloadWithEvent("http://hello.rules");
		int count = testableNetworkService.waitAndGetCount();
		assertEquals(1, count);

		//		testableNetworkService.clearNetworkRequestList();
		eventHub.clearEvents();
		ConfigurationExtension.NOT_DOWNLOAD_RULES_WITHIN_TIME_SEC = 1;
		super.sleep(2000);
		triggerRulesDownloadWithEvent("http://hello.rules");
		super.sleep(EVENTHUB_WAIT_MS);
		count = testableNetworkService.waitAndGetCount();
		assertEquals(2, count);
	}


	@Test
	@Ignore
	public void configEvent_When_HttpNotFound_ShouldClearCorrespondingRules()
	throws Exception {
		//Setup
		eventHub.setExpectedEventCount(1);
		eventHub.ignoreEvents(EventType.HUB, EventSource.SHARED_STATE);
		eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT);

		//Setup network service
		setupNetWorkService("RulesEngineTest_Rules_ModuleTest1.zip", new Date(), 2, HttpURLConnection.HTTP_OK);

		//Setup mock system service
		setupSystemInfoService(temporaryFolder.newFolder());
		setUpBasic();

		//Setup config response content event
		triggerRulesDownloadWithEvent("http://hello.rules");

		// Wait and clear off the loaded consequence event
		waitForThreadsWithFailIfTimedOut(5000);
		eventHub.clearEvents();

		//Setup lifecycle shared state
		Map<String, Object> lifecycleSharedState = new HashMap<String, Object>();
		lifecycleSharedState.put("lckey", "lcvalue");
		createLifecycleSharedState(lifecycleSharedState);

		waitForThreadsWithFailIfTimedOut(5000);

		//Setup and dispatch analytics event
		dispatchAnalyticsEvent();
		waitForThreadsWithFailIfTimedOut(5000);
		//Verify that the rules data was valid at this point
		List<Event> events =  eventHub.getEvents();
		assertEquals(1, events.size());

		eventHub.clearEvents();
		ConfigurationExtension.NOT_DOWNLOAD_RULES_WITHIN_TIME_SEC = 0;
		//Change the default response to return 404 now
		setupNetWorkService("RulesEngineTest_Rules_ModuleTest2.zip", new Date(), 2,
							HttpURLConnection.HTTP_NOT_FOUND);
		//Setup config response content event
		triggerRulesDownloadWithEvent("http://hello.rules");

		// Wait and clear off the loaded consequence event
		waitForThreadsWithFailIfTimedOut(5000);
		eventHub.clearEvents();

		//Dispatch Analytics request again to check
		dispatchAnalyticsEvent();
		waitForThreadsWithFailIfTimedOut(5000);

		//Verify that the rules data was cleared due to the 404 earlier - so no events should have been dispatched
		events = eventHub.getEvents();
		assertEquals(0, events.size());

	}

	@Test
	@Ignore
	public void configEvent_shouldNotClearRulesData_WhenRemoteDownloadReturnsUnknownError() throws
		Exception {
		//Setup
		eventHub.setExpectedEventCount(1);
		eventHub.ignoreEvents(EventType.HUB, EventSource.SHARED_STATE);
		eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT);

		//Setup network service
		setupNetWorkService("RulesEngineTest_Rules_ModuleTest1.zip", new Date(), 2, HttpURLConnection.HTTP_OK);

		//Setup mock system service
		setupSystemInfoService(temporaryFolder.newFolder());
		setUpBasic();

		//Setup config response content event
		triggerRulesDownloadWithEvent("http://hello.rules");
		// Wait and clear off the loaded consequence event
		waitForThreadsWithFailIfTimedOut(5000);
		eventHub.clearEvents();

		//Setup lifecycle shared state
		Map<String, Object> lifecycleSharedState = new HashMap<String, Object>();
		lifecycleSharedState.put("lckey", "lcvalue");
		createLifecycleSharedState(lifecycleSharedState);

		waitForThreadsWithFailIfTimedOut(5000);

		//Setup and dispatch analytics event
		dispatchAnalyticsEvent();
		waitForThreadsWithFailIfTimedOut(5000);
		//Verify that the rules data was valid at this point
		List<Event> events =  eventHub.getEvents();
		assertEquals(1, events.size());

		eventHub.clearEvents();

		//Change the default response to return 502. This error is treated as an error that should not change the data
		//on the client
		setupNetWorkService("RulesEngineTest_Rules_ModuleTest1.zip", new Date(), 2,
							HttpURLConnection.HTTP_BAD_GATEWAY);

		//Setup config response content event
		triggerRulesDownloadWithEvent("http://hello.rules");
		// Wait and clear off the loaded consequence event
		waitForThreadsWithFailIfTimedOut(5000);
		eventHub.clearEvents();

		//Dispatch Analytics request again to check
		dispatchAnalyticsEvent();
		waitForThreadsWithFailIfTimedOut(5000);

		//Verify that the rules data was not cleared due to the 502 earlier
		events = eventHub.getEvents();
		assertEquals(1, events.size());

	}

	@Ignore
	@Test
	public void reprocessEvent_When_FirstAppLaunch() throws  Exception {
		eventHub.registerModule(ConfigurationExtension.class);
		super.sleep(EVENTHUB_WAIT_MS);
		eventHub.finishModulesRegistration(null);
		eventHub.dispatch(new Event.Builder("Test1", EventType.ACQUISITION, EventSource.NONE).build());
		eventHub.dispatch(new Event.Builder("Test2", EventType.ACQUISITION, EventSource.NONE).build());
		eventHub.dispatch(new Event.Builder("Test3", EventType.ACQUISITION, EventSource.NONE).build());
		ConfigurationExtension configuration = (ConfigurationExtension) eventHub.getActiveModules().iterator().next();
		super.sleep(EVENTHUB_WAIT_MS);
		assertEquals(4, eventHub.getAllEventsCount());
	}

	@After
	public void tearDown() {
		deleteCacheDirectory();
	}


	// =============================================================================
	// Verifiers
	// =============================================================================

	private void verifyBootEvent(final Event event) {
		assertEquals(EventType.HUB, event.getEventType());
		assertEquals(EventSource.BOOTED, event.getEventSource());
	}

	private void verifyConfigStateChangeEvent(final Event event) {
		assertEquals(EventType.HUB, event.getEventType());
		assertEquals(EventSource.SHARED_STATE, event.getEventSource());
		assertEquals("com.adobe.module.configuration", event.getData().optString("stateowner", null));
	}

	private void verifyConfigureWithAppIDEvent(final Event event, final String appId) {
		assertEquals(EventType.CONFIGURATION, event.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, event.getEventSource());
		assertEquals(appId, event.getData().optString("config.appId", null));
	}

	private void verifyConfigurationResponseEvent_with_BundledData(final Event event) {
		assertEquals(EventType.CONFIGURATION, event.getEventType());
		assertEquals(EventSource.RESPONSE_CONTENT, event.getEventSource());
		assertEquals(BUNDLED_RSID, event.getData().optString(ANALYTICS_RSID_KEY, null));
		assertEquals(BUNDLED_SERVER, event.getData().optString(ANALYTICS_SERVER_KEY, null));
	}

	private void verifyConfigurationResponseEvent_with_cachedData(final Event event) {
		assertEquals(EventType.CONFIGURATION, event.getEventType());
		assertEquals(EventSource.RESPONSE_CONTENT, event.getEventSource());
		assertEquals(CACHED_RSID, event.getData().optString(ANALYTICS_RSID_KEY, null));
		assertEquals(CACHED_SERVER, event.getData().optString(ANALYTICS_SERVER_KEY, null));
	}


	private void verifyConfigurationResponseEvent_with_DownloadedData(final Event event) {
		assertEquals(EventType.CONFIGURATION, event.getEventType());
		assertEquals(EventSource.RESPONSE_CONTENT, event.getEventSource());
		assertEquals(DOWNLOADED_RSID, event.getData().optString(ANALYTICS_RSID_KEY, null));
		assertEquals(DOWNLOADED_SERVER, event.getData().optString(ANALYTICS_SERVER_KEY, null));
	}

	private void verifyConfigurationResponseEvent_with_NewDownloadedData(final Event event) {
		assertEquals(EventType.CONFIGURATION, event.getEventType());
		assertEquals(EventSource.RESPONSE_CONTENT, event.getEventSource());
		assertEquals(NEW_RSID, event.getData().optString(ANALYTICS_RSID_KEY, null));
		assertEquals(NEW_SERVER, event.getData().optString(ANALYTICS_SERVER_KEY, null));
	}

	void verifyConfigurationResponseEvent_with_BundledData_and_OveridenData(final Event event) {
		assertEquals(EventType.CONFIGURATION, event.getEventType());
		assertEquals(EventSource.RESPONSE_CONTENT, event.getEventSource());
		assertEquals(BUNDLED_RSID, event.getData().optString(ANALYTICS_RSID_KEY, null));
		assertEquals(BUNDLED_SERVER, event.getData().optString(ANALYTICS_SERVER_KEY, null));
	}

	void verifySharedStateisBundledData(final EventData eventData) {

	}


	// =============================================================================
	// Setup Methods
	// =============================================================================

	private void setUpWithConfigBundledInAssets() {
		try {
			platformServices.getMockSystemInfoService().assetStream = new ByteArrayInputStream(
				MOCK_BUNDLED_CONFIG.getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}


		try {
			eventHub.registerModule(ConfigurationExtension.class);
			super.sleep(EVENTHUB_WAIT_MS);
			eventHub.finishModulesRegistration(null);
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	private void setUpWithAppIdInPersistence() {
		try {
			platformServices.getMockSystemInfoService().applicationCacheDir = new File(this.getClass().getResource("").getPath());
			platformServices.getLocalStorageService().getDataStore("AdobeMobile_ConfigState").setString("config.appID", "appID");
			createCacheFileWithData(MOCK_CACHED_CONFIG);
			eventHub.registerModule(ConfigurationExtension.class);
			super.sleep(EVENTHUB_WAIT_MS);
			eventHub.finishModulesRegistration(null);
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	private void addHermeticServerToManifest() {
		platformServices.getMockSystemInfoService().setPropertyValue(REMOTE_CONFIG_SERVER, HERMETIC_CONFIG_SERVER);
	}

	private void setUpWithAppIDInManifest() {
		try {
			platformServices.getMockSystemInfoService().setPropertyValue("ADBMobileAppID", "manifestAppId");
			platformServices.getMockSystemInfoService().applicationCacheDir = new File(this.getClass().getResource("").getPath());
			testableNetworkService.setDefaultResponse(createMockResponse());
			eventHub.registerModule(ConfigurationExtension.class);
			super.sleep(EVENTHUB_WAIT_MS);
			eventHub.finishModulesRegistration(null);
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	private void setUpWithOverriddenConfigInPersistence() {
		try {
			platformServices.getMockSystemInfoService().applicationCacheDir = new File(this.getClass().getResource("").getPath());
			platformServices.getLocalStorageService().getDataStore("AdobeMobile_ConfigState").setString("config.overridden.map",
					OVERRIDDEN_CONFIG_STRING);
			eventHub.registerModule(ConfigurationExtension.class);
			super.sleep(EVENTHUB_WAIT_MS);
			eventHub.finishModulesRegistration(null);
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	private void setUpWithAppIDandOveriddenConfigInPersistence() {
		try {
			platformServices.getMockSystemInfoService().applicationCacheDir = new File(this.getClass().getResource("").getPath());
			platformServices.getLocalStorageService().getDataStore("AdobeMobile_ConfigState").setString("config.appID", "appID");
			platformServices.getLocalStorageService().getDataStore("AdobeMobile_ConfigState").setString("config.overridden.map",
					OVERRIDDEN_CONFIG_STRING);
			createCacheFileWithData(MOCK_CACHED_CONFIG);
			eventHub.registerModule(ConfigurationExtension.class);
			super.sleep(EVENTHUB_WAIT_MS);
			eventHub.finishModulesRegistration(null);
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	private void setUpWithConfigBundledInAssetsAndOverriddenConfigInPersistence() {
		try {
			platformServices.getMockSystemInfoService().assetStream = new ByteArrayInputStream(
				MOCK_BUNDLED_CONFIG.getBytes("UTF-8"));
			platformServices.getMockSystemInfoService().applicationCacheDir = new File(this.getClass().getResource("").getPath());
			platformServices.getLocalStorageService().getDataStore("AdobeMobile_ConfigState").setString("config.overridden.map",
					OVERRIDDEN_CONFIG_STRING);
		} catch (Exception e) {
			e.printStackTrace();
		}


		try {
			eventHub.registerModule(ConfigurationExtension.class);
			super.sleep(EVENTHUB_WAIT_MS);
			eventHub.finishModulesRegistration(null);
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	private void setUpWithEverything() {
		try {
			platformServices.getMockSystemInfoService().assetStream = new ByteArrayInputStream(
				MOCK_BUNDLED_CONFIG.getBytes("UTF-8"));
			platformServices.getMockSystemInfoService().applicationCacheDir = new File(this.getClass().getResource("").getPath());
			platformServices.getLocalStorageService().getDataStore("AdobeMobile_ConfigState").setString("config.appID", "appID");
			platformServices.getLocalStorageService().getDataStore("AdobeMobile_ConfigState").setString("config.overridden.map",
					OVERRIDDEN_CONFIG_STRING);
			createCacheFileWithData(MOCK_CACHED_CONFIG);
		} catch (Exception e) {
			e.printStackTrace();
		}


		try {
			eventHub.registerModule(ConfigurationExtension.class);
			super.sleep(EVENTHUB_WAIT_MS);
			eventHub.finishModulesRegistration(null);
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	private void setUpBasic() {
		try {
			platformServices.getMockSystemInfoService().applicationCacheDir = new File(this.getClass().getResource("").getPath());
			eventHub.registerModule(ConfigurationExtension.class);
			super.sleep(EVENTHUB_WAIT_MS);
			eventHub.finishModulesRegistration(null);

		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	// =============================================================================
	// Shared state reader
	// =============================================================================

	private EventData getLastConfigurationSharedState() {
		final Event event = new Event.Builder("Test", EventType.ACQUISITION, EventSource.NONE).build();
		event.setEventNumber(1000);
		class fakeModule extends Module {
			private fakeModule(EventHub hub) {
				super("FakeModule", hub);
			}
		}
		return eventHub.getSharedEventState("com.adobe.module.configuration", event, new fakeModule(eventHub));
	}

	private EventData getConfigurationSharedState(final int eventNumber) {
		final Event event = new Event.Builder("Test", EventType.ACQUISITION, EventSource.NONE).build();
		event.setEventNumber(eventNumber);
		class fakeModule extends Module {
			private fakeModule(EventHub hub) {
				super("FakeModule", hub);
			}
		}
		return eventHub.getSharedEventState("com.adobe.module.configuration", event, new fakeModule(eventHub));
	}

	// =============================================================================
	// Event generators
	// =============================================================================

	private Event configureWithAppIDEvent() {
		EventData eventData = new EventData();
		eventData.putString("config.appId", "appID");
		return new Event.Builder("Configure with AppID", EventType.CONFIGURATION,
								 EventSource.REQUEST_CONTENT).setData(eventData).build();
	}

	private Event configureWithAppIDInternalEvent(final String appId) {
		EventData eventData = new EventData();
		eventData.putString("config.appId", appId);
		eventData.putBoolean("config.isinternalevent", true);
		final Event event = new Event.Builder("Configure with AppID", EventType.CONFIGURATION,
											  EventSource.REQUEST_CONTENT).setData(eventData).build();
		return event;
	}

	private Event configureWithFilePathEvent() {
		EventData eventData = new EventData();
		eventData.putString("config.filePath", bundledConfigFilePath());
		return new Event.Builder("Configure with AppID", EventType.CONFIGURATION,
								 EventSource.REQUEST_CONTENT).setData(eventData).build();
	}

	private Event updateConfigurationInternalEvent() {
		EventData eventData = new EventData();
		eventData.putStringMap("config.update", OVERRIDDEN_CONFIG_MAP);
		return new Event.Builder("Update Configuration", EventType.CONFIGURATION,
								 EventSource.REQUEST_CONTENT).setData(eventData).build();
	}

	private Event updateConfigurationInternalEvent(HashMap<String, String> configMap) {
		EventData eventData = new EventData();
		eventData.putStringMap("config.update", configMap);
		return new Event.Builder("Update Configuration", EventType.CONFIGURATION,
								 EventSource.REQUEST_CONTENT).setData(eventData).build();
	}

	private Event clearUpdatedConfigurationInternalEvent() {
		EventData eventData = new EventData();
		eventData.putBoolean("config.clearUpdates", true);
		return new Event.Builder("Clear updated configuration", EventType.CONFIGURATION,
								 EventSource.REQUEST_CONTENT).setData(eventData).build();
	}

	private void requestPrivacyStatus(final AdobeCallback<MobilePrivacyStatus> callback) throws Exception {
		EventData eventData = new EventData();
		eventData.putBoolean("config.getData", true);
		Event event = new Event.Builder("PrivacyStatusRequest", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setData(eventData).build();

		eventHub.registerOneTimeListener(EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT,
		event.getResponsePairID(), new Module.OneTimeListenerBlock() {
			@Override
			public void call(final Event e) {
				callback.call(MobilePrivacyStatus.fromString(e.getData().optString("global.privacy", null)));
			}
		});

		eventHub.dispatch(event);
	}

	private void updateConfiguration(final Map<String, String> data)  {
		EventData eventData = new EventData();
		eventData.putStringMap("config.update", data);
		Event event = new Event.Builder("UpdateConfig", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setData(eventData).build();
		eventHub.dispatch(event);
	}

	// =============================================================================
	// Cached file helpers methods
	// =============================================================================

	// appID
	private boolean checkIfTheCacheFileExists() {
		File cacheDirectory = new File(this.getClass().getResource("").getPath() + "adbdownloadcache");
		File cachedFile = new File(cacheDirectory,
								   "fd7a4c567c91d891657b000668f5a18d90e24fe350ab671cd81259b444c543c0.1445412480000");

		return cachedFile.exists();
	}

	private boolean checkIfManifestAppIdCacheFileExists() {
		File cacheDirectory = new File(this.getClass().getResource("").getPath() + "adbdownloadcache");
		File cachedFile = new File(cacheDirectory,
								   "2b46a833826dc9b53012bdb4f3a39e2559e5d928e671d9fdf54fb0d4b9acb4ed.1445412480000");

		return cachedFile.exists();
	}


	private boolean checkIfTheNewCacheFileExists() {
		File cacheDirectory = new File(this.getClass().getResource("").getPath() + "adbdownloadcache");
		File cachedFile = new File(cacheDirectory,
								   "fd7a4c567c91d891657b000668f5a18d90e24fe350ab671cd81259b444c543c0.1445585280000");

		return cachedFile.exists();
	}

	private void deleteCacheDirectory() {
		File cacheDirectory = new File(this.getClass().getResource("").getPath() + "adbdownloadcache");
		String[]files = cacheDirectory.list();

		if (files != null) {
			for (String file : files) {
				File currentFile = new File(cacheDirectory.getPath(), file);

				if (!currentFile.delete()) {
					System.out.println("Unable to delete file: " + currentFile.getAbsolutePath());
				}
			}
		}

		if (!cacheDirectory.delete()) {
			System.out.println("Unable to delete directory: " + cacheDirectory.getAbsolutePath());
		}
	}

	private void createCacheFileWithData(String fileData) {
		File appDirectory = new File(this.getClass().getResource("").getPath() + "adbdownloadcache");

		if (!appDirectory.mkdir()) {
			System.out.println("Unable to create appDirectory: " + appDirectory.getAbsolutePath());
		}

		try {
			final File file = new File(appDirectory + File.separator +
									   "fd7a4c567c91d891657b000668f5a18d90e24fe350ab671cd81259b444c543c0.1445412480000");
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(fileData);
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void deleteSampleConfigBundledFile() {
		File cacheDirectory = new File(this.getClass().getResource("").getPath() + "BundleFileDirectory");
		String[]files = cacheDirectory.list();

		if (files != null) {
			for (String file : files) {
				File currentFile = new File(cacheDirectory.getPath(), file);

				if (!currentFile.delete()) {
					System.out.println("Unable to delete file: " + currentFile.getAbsolutePath());
				}
			}
		}

		if (!cacheDirectory.delete()) {
			System.out.println("Unable to delete directory: " + cacheDirectory.getAbsolutePath());
		}
	}

	private String bundledConfigFilePath() {
		return this.getClass().getResource("").getPath() + "BundleFileDirectory" + File.separator + "AdobeMobileConfig.json";
	}

	private void createSampleBundleConfigFile(final String fileData) {
		File appDirectory = new File(this.getClass().getResource("").getPath() + "BundleFileDirectory");

		if (!appDirectory.mkdir()) {
			System.out.println("Unable to create directory: " + appDirectory.getAbsolutePath());
		}

		try {
			final File file = new File(appDirectory + File.separator + "AdobeMobileConfig.json");
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(fileData);
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	// =============================================================================
	// Network response mocks
	// =============================================================================

	private TestableNetworkService.NetworkResponse createMockResponse() {
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Last-Modified", "Wed, 21 Oct 2015 07:28:00 +0000");
		return new TestableNetworkService.NetworkResponse(MOCK_DOWNLOADED_CONFIG, 200, 0, headers);
	}

	private TestableNetworkService.NetworkResponse createMockNoConfigChangeResponse() {
		// Response code is made 304 to indicate that there is no change in configuration on remote server
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Last-Modified", "Wed, 21 Oct 2015 07:28:00 +0000");
		return new TestableNetworkService.NetworkResponse(MOCK_DOWNLOADED_CONFIG, 416, 0, headers);
	}


	private TestableNetworkService.NetworkResponse createMockResponseWithDelay() {
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Last-Modified", "Wed, 21 Oct 2015 07:28:00 +0000");
		return new TestableNetworkService.NetworkResponse(MOCK_DOWNLOADED_CONFIG, 200, 500, headers);
	}

	private TestableNetworkService.NetworkResponse createMockNoConfigChangeResponseWithDelay() {
		// Response code is made 304 to indicate that there is no change in configuration on remote server
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Last-Modified", "Wed, 21 Oct 2015 07:28:00 +0000");
		return new TestableNetworkService.NetworkResponse(MOCK_DOWNLOADED_CONFIG, 416, 500, headers);
	}

	private TestableNetworkService.NetworkResponse createMockErrorResponse() {
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("som", "som");
		return new TestableNetworkService.NetworkResponse("", 400, 0, headers);
	}

	private TestableNetworkService.NetworkResponse createMockNewContentResponse() {
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Last-Modified", "Wed, 23 Oct 2015 07:28:00 +0000");
		return new TestableNetworkService.NetworkResponse(MOCK_NEW_DOWNLOADED_CONFIG, 200, 0, headers);
	}

	private String getExpectedRemoteUrl(final String appId) {
		return String.format(CONFIGURATION_URL_BASE, appId);
	}

	private void setupNetWorkService(String contentResourceFileName, Date resourceLastModifiedDate,
									 int networkServiceExpectedCount, int responseCode) throws Exception {
		File testJson = getResource(contentResourceFileName);
		assertNotNull("Could not read the test resource " + contentResourceFileName, testJson);
		testableNetworkService.setExpectedCount(networkServiceExpectedCount);
		byte[] contents = getFileContents(testJson);

		Map<String, String> headers = new HashMap<String, String>();
		SimpleDateFormat simpleDateFormat = createRFC2822Formatter();

		headers.put("Last-Modified", simpleDateFormat.format(resourceLastModifiedDate));

		TestableNetworkService.NetworkResponse networkResponse =
			new TestableNetworkService.NetworkResponse(contents, responseCode, headers);
		testableNetworkService.setDefaultResponse(networkResponse);
	}

	/**
	 * Create a Date formatter in specific format
	 *
	 * @return SimpleDateFormat
	 */

	private SimpleDateFormat createRFC2822Formatter() {
		final String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
		final SimpleDateFormat rfc2822formatter = new SimpleDateFormat(pattern, Locale.US);
		rfc2822formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return rfc2822formatter;
	}

	private byte[] getFileContents(File file) throws Exception {
		if (file != null) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			FileInputStream fis = new FileInputStream(file);

			byte [] buffer = new byte[1024];
			int n;

			while ((n = fis.read(buffer)) != -1) {
				bos.write(buffer, 0, n);
			}

			return bos.toByteArray();
		}

		return null;
	}

	private void setupSystemInfoService(File cacheDir) {
		MockSystemInfoService systemInfoService = (MockSystemInfoService) platformServices.getSystemInfoService();
		systemInfoService.applicationCacheDir = cacheDir;
	}

	private void dispatchAnalyticsEvent() {
		final EventData analyticsData = new EventData()
		.putString(ANALYTICS_TRACK_ACTION, "value1");
		final Event analyticsReqEvent = new Event.Builder("AnalyticsRequest", EventType.ANALYTICS,
				EventSource.REQUEST_CONTENT).setData(analyticsData).build();
		eventHub.dispatch(analyticsReqEvent);
	}

	private void createLifecycleSharedState(Map<String, Object> lifecycleState) {
		EventData data = new EventData();
		data.putObject("lifecycle.contextData", lifecycleState);

		eventHub.createSharedState("com.adobe.module.lifecycle", 0, data);
	}

	@SuppressWarnings("unchecked")
	private String extractConsequenceIdFromConsequenceEvent(final Event event) {
		Log.debug("TEST", "Extracting consequence id from event: " + event.toString());
		Map<String, Object> consequenceMap = (Map<String, Object>)event.getData().getObject(
				RULE_ENGINE_CONSEQUENCE_TRIGGERED);
		return (String)consequenceMap.get(RULE_ENGINE_CONSEQUENCE_JSON_ID);
	}

	@SuppressWarnings("unchecked")
	private String extractConsequenceTypeFromConsequenceEvent(final Event event) {
		Log.debug("TEST", "Extracting consequence type from event: " + event.toString());
		Map<String, Object> consequenceMap = (Map<String, Object>)event.getData().getObject(
				RULE_ENGINE_CONSEQUENCE_TRIGGERED);
		return (String)consequenceMap.get(RULE_ENGINE_CONSEQUENCE_JSON_TYPE);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> extractConsequenceDetailFromConsequenceEvent(final Event event) {
		Log.debug("TEST", "Extracting consequence detail from event: " + event.toString());
		Map<String, Object> consequenceMap = (Map<String, Object>)event.getData().getObject(
				RULE_ENGINE_CONSEQUENCE_TRIGGERED);
		return (Map<String, Object>)consequenceMap.get(RULE_ENGINE_CONSEQUENCE_JSON_DETAIL);
	}

	private void triggerRulesDownloadWithEvent(final String rulesURL) {
		final EventData eventData = new EventData();
		final Map<String, String> configData = new HashMap<String, String>();
		configData.put(RULES_URL_KEY, rulesURL);
		eventData.putObject(CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG,
							configData);
		final Event configEvent = new Event.Builder("Update Configuration", EventType.CONFIGURATION,
				EventSource.REQUEST_CONTENT).setData(eventData).build();

		eventHub.dispatch(configEvent);
	}
}
