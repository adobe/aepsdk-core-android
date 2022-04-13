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
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Class {@link AndroidThirdPartyExtensionsFunctionalTests} that defines all the necessary test cases to test a third party extension  {@link TestableExtension} that extends {@link ExtensionListener} class of the Adobe Experience Platform SDK.
 *
 * This covers all the test cases listed in the document to run as part of the CI build system.
 *
 * https://wiki.corp.adobe.com/pages/viewpage.action?spaceKey=adms&title=Mobile+SDK+v5+Extensions+Module+Test+Plan
 *
 * @author Adobe
 * @version 5.0
 */

@RunWith(AndroidJUnit4.class)
public class AndroidThirdPartyExtensionsFunctionalTests extends AbstractE2ETest {

	private static final String LOG_TAG = AndroidThirdPartyExtensionsFunctionalTests.class.getSimpleName();

	private TestHelper testHelper = new TestHelper();
	private ExtensionTestingHelper extensionTestingHelper = new ExtensionTestingHelper();
	private AsyncHelper asyncHelper = new AsyncHelper();

	private List<ListenerType> configListenerTypes = new ArrayList<ListenerType>();
	private List<ListenerType> identityListenerTypes = new ArrayList<ListenerType>();
	private List<ListenerType> customListenerTypes = new ArrayList<ListenerType>();
	private List<ListenerType> analyticsListenerTypes = new ArrayList<ListenerType>();
	private List<ListenerType> wildcardListener = new ArrayList<ListenerType>();

	static final String CONFIGURATION_SHARED_STATE = "com.adobe.module.configuration";
	static final String EVENT_SOURCE_SHARED_STATE = "com.adobe.eventSource.sharedstate";
	static final String EVENT_TYPE_CONFIGURATION = "com.adobe.eventType.configuration";

	private final Object executorMutex = new Object();
	private ExecutorService executor;
	private ConcurrentLinkedQueue<Event> unprocessedEvents;
	private long noProcessedEvents;

	private Map<String, Object> eventData = new HashMap<String, Object>();
	Map<String, Object> subEventData = new HashMap<String, Object>();
	Map<String, Object> pairedEventData = new HashMap<String, Object>();
	ExtensionError extensionError;
	Map<String, Object> configMap = new HashMap<String, Object>();

	@Before
	public void setUp() {
		super.setUp();
		testHelper.cleanCache(defaultContext);
		MobileCore.setLogLevel(LoggingMode.DEBUG);
		MobileCore.start(null);
		testableNetworkService.resetTestableNetworkService();

		configListenerTypes.add(new ListenerType("com.adobe.eventType.configuration", "com.adobe.eventSource.responseContent"));
		configListenerTypes.add(new ListenerType("com.adobe.eventType.configuration", "com.adobe.eventSource.requestContent"));

		identityListenerTypes.add(new ListenerType("com.adobe.eventType.identity", "com.adobe.eventSource.requestIdentity"));
		identityListenerTypes.add(new ListenerType("com.adobe.eventType.identity", "com.adobe.eventSource.responseIdentity"));

		analyticsListenerTypes.add(new ListenerType("com.adobe.eventType.analytics", "com.adobe.eventSource.requestContent"));
		analyticsListenerTypes.add(new ListenerType("com.adobe.eventType.analytics", "com.adobe.eventSource.requestIdentity"));
		analyticsListenerTypes.add(new ListenerType("com.adobe.eventType.analytics", "com.adobe.eventSource.responseContent"));
		analyticsListenerTypes.add(new ListenerType("com.adobe.eventType.analytics", "com.adobe.eventSource.responseIdentity"));
		analyticsListenerTypes.add(new ListenerType("com.adobe.eventType.generic.track",
								   "com.adobe.eventSource.requestContent"));

		wildcardListener.add(new ListenerType("com.adobe.eventtype._wildcard_", "com.adobe.eventsource._wildcard_"));

		customListenerTypes.add(new ListenerType("com.example.testable.custom", "com.example.testable.request"));

		subEventData.put("key0", "value0");
		subEventData.put("key1", "value1");
		subEventData.put("key2", "value2");
		Map<String, Object> customElement = new HashMap<String, Object>();
		//    customElement.put("customByte", (byte)127);
		//    customElement.put("customFloat", new Float(3.14));
		//    customElement.put("customShort", (short)300);
		customElement.put("customInt", new Integer(123));
		customElement.put("customLong", (long) 300);
		customElement.put("customChar", 'c');
		customElement.put("customString", "string1");
		customElement.put("customNull", null);
		customElement.put("customBoolean", true);
		customElement.put("customList", customListenerTypes);
		customElement.put("customDouble", new Double(3.14));
		customElement.put("customMap", subEventData);
		Map<String, Object> customData = new HashMap<String, Object>();
		customData.put("customElement", customElement);
		eventData.put("customData", customData);

		pairedEventData.put("ResponseKey1", "ResponseData1");
		pairedEventData.put("ResponseKey2", "ResponseData2");
		pairedEventData.put("ResponseKey3", "ResponseData3");
	}

	@After
	public void tearDown() {
		asyncHelper.waitForAppThreads(500, true);
		super.tearDown();
	}

	public void updateConfiguration() {
		configMap.put("build.environment", "dev");
		configMap.put("myExtension.server", "mydomain.dev.com");
		configMap.put("global.privacy", "optedin");
		configMap.put("experienceCloud.org", "972C898555E9F7BC7F000101@AdobeOrg");
		configMap.put("analytics.server", "obumobile5.sc.omtrdc.net");
		configMap.put("analytics.rsids", "mobile5the.v5.show");
		configMap.put("analytics.offlineEnabled", true);
		configMap.put("analytics.referrerTimeout", 15);
		configMap.put("analytics.backdatePreviousSessionInfo", true);
		configMap.put("identity.adidEnabled", true);
		configMap.put("acquisition.server", "c00.adobe.com");
		configMap.put("rules.url", "https://s3.amazonaws.com/ams-qe/ios-sdk-test-app-rules.zip");
		configMap.put("target.clientCode", "yourclientcode");
		configMap.put("target.timeout", 5);
		configMap.put("audience.server", "omniture.demdex.net");
		configMap.put("audience.timeout", 5);
		configMap.put("analytics.aamForwardingEnabled", false);
		configMap.put("analytics.batchLimit", 0);
		configMap.put("lifecycle.sessionTimeout", 300);
		MobileCore.updateConfiguration(configMap);
	}

	public ExecutorService getExecutor() {
		synchronized (executorMutex) {
			if (executor == null) {
				executor = Executors.newFixedThreadPool(2);
			}

			return executor;
		}
	}

	// Test Case No : 1
	// Register A Third Party Extension using registerExtension API
	@Test
	public void testRegisterExtension_whenValidData_returnsNoError() {

		// setup
		String extensionName = "ThirdPartyExtension01";
		// test
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,
											   configListenerTypes);
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
	}

	// Test Case No : 2 & 20
	// Get the Name Of A ThirdParty Extension using getName API in Android and name in iOS
	@Test
	public void testGetName_whenExtensionRegistered_returnsName() {

		// setup
		String extensionName = "ThirdPartyExtension02";
		// test
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,
											   configListenerTypes);
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertEquals(returnStatus.extensionName, extensionName);
	}

	// Test Case No : 2a
	@Test
	public void testGetExtensionInstance_whenExtensionNotRegistered_returnsNull() {

		// setup
		String extensionName = "ThirdPartyExtension02a";
		// test
		Extension extension = extensionTestingHelper.getExtensionInstance(extensionName);
		// verify
		assertNull(extension);
	}

	// Test Case No : 3 & 20
	// Get the Version Of A ThirdParty Extension using getVersion API in Android and version in iOS
	@Test
	public void testGetVersion_whenExtensionRegistered_returnsVersion() {

		// setup
		String extensionName = "ThirdPartyExtension03";
		// test
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, configListenerTypes);
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(returnStatus.extensionVersion.contains("1.0.0"));
	}

	// Test Case No : 4
	// Unregister A Third Party Extension using unregisterExtension API
	@Test
	public void testUnRegisterExtension_whenExtensionRegistered_returnsTrue() {

		// setup
		String extensionName = "ThirdPartyExtension04";
		// test
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, configListenerTypes);
		boolean unregisterStatus = extensionTestingHelper.unregisterExtension(extensionName);
		// verify
		assertTrue(unregisterStatus);
		assertFalse(extensionTestingHelper.isRegistered(extensionName));
	}

	// Test Case No : 5
	// OnUnregistered Call Of A Third Party Extension using unregisterExtension API in Android and onUnregister in IOS
	@Test
	public void testOnUnregistered_whenExtensionUnRegistered_OnUnregisteredIsCalled() {

		// setup
		String extensionName = "ThirdPartyExtension05";
		// test
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, configListenerTypes);
		extensionTestingHelper.confirmExtensionUnregisteredCall = "";
		boolean unregisterStatus = extensionTestingHelper.unregisterExtension(extensionName);
		// verify
		assertTrue(unregisterStatus);
		assertEquals(returnStatus.extensionName, extensionName);
		assertFalse(extensionTestingHelper.isRegistered(extensionName));
		assertTrue(extensionTestingHelper.confirmExtensionUnregisteredCall.contains("ConfirmedByTestableExtension"));
	}

	// Test Case No : 6
	// onUnexpectedError Call Of A Third Party Extension when trying
	// to a register Extension API for various errors mentioned in the ExtensionError Class
	@Test
	public void testRegisterExtension_whenNullName_returnsError() {

		// setup
		String extensionName = null;
		// test
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, configListenerTypes);
		// verify
		assertEquals(1, returnStatus.extensionUnexpectedError.getErrorCode().getErrorCode());
		assertEquals("extension.bad_extension_name", returnStatus.extensionUnexpectedError.getErrorCode().getErrorName());
	}

	// Test Case No : 6a
	@Test
	public void testRegisterExtension_whenEmptyName_returnsError() {

		// setup
		String extensionName = "";
		// test
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, configListenerTypes);
		// verify
		assertEquals(1, returnStatus.extensionUnexpectedError.getErrorCode().getErrorCode());
		assertEquals("extension.bad_extension_name", returnStatus.extensionUnexpectedError.getErrorCode().getErrorName());
	}

	// Test Case No : 7 & 21
	// Register a listener to listen an identity event using registerEventListener API for stateowner like com.adobe.module.identity
	@Test
	public void testRegisterListeners_whenEventsDispatched_eventsReceivedByRightListener() {

		// setup
		String extensionName = "ThirdPartyExtension07";
		// test
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, identityListenerTypes);
		extensionTestingHelper.dispatchAnEvent(identityListenerTypes.get(0), eventData);
		extensionTestingHelper.dispatchAnEvent(identityListenerTypes.get(1), subEventData);
		Event event1 = extensionTestingHelper.getLastEventHeardByListener(extensionName, identityListenerTypes.get(0));
		Event event2 = extensionTestingHelper.getLastEventHeardByListener(extensionName, identityListenerTypes.get(1));
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));

		assertTrue(identityListenerTypes.get(0).eventType.equalsIgnoreCase(event1.getType()));
		assertTrue(identityListenerTypes.get(0).eventSource.equalsIgnoreCase(event1.getSource()));
		assertEquals(eventData, event1.getEventData());

		assertTrue(identityListenerTypes.get(1).eventType.equalsIgnoreCase(event2.getType()));
		assertTrue(identityListenerTypes.get(1).eventSource.equalsIgnoreCase(event2.getSource()));
		assertEquals(subEventData, event2.getEventData());
	}

	// Test Case No : 8
	// OnUnregistered Call in A registered listener will be called while an extension
	// gets unregistered using unregisterExtension API
	@Test
	public void testOnUnregisteredCall_whenUnregisterExtension_OnUnregisteredCallOfListenerCalled() {

		// setup
		String extensionName = "ThirdPartyExtension08";
		// test
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, identityListenerTypes);
		extensionTestingHelper.confirmListenerUnregisteredCall = "";
		extensionTestingHelper.confirmExtensionUnregisteredCall = "";
		boolean unregisterStatus = extensionTestingHelper.unregisterExtension(extensionName);
		// verify
		assertTrue(unregisterStatus);
		assertFalse(extensionTestingHelper.isRegistered(extensionName));
		assertTrue(extensionTestingHelper.confirmExtensionUnregisteredCall.contains("ConfirmedByTestableExtension"));
		assertTrue(extensionTestingHelper.confirmListenerUnregisteredCall.contains("ConfirmedByTestableListener"));
	}

	// Test Case No : 9
	// Register A Listener to listen a custom event using registerEventListener API
	@Test
	public void testRegisterAListener_whenACustomEvent_returnsNoError() {

		// setup
		String extensionName = "ThirdPartyExtension07";
		// test
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,  customListenerTypes);
		EventListener eventListener = extensionTestingHelper.getListenerInstance(extensionName, customListenerTypes.get(0));
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertTrue(eventListener.getEventType().getName().equalsIgnoreCase("com.example.testable.custom"));
		assertTrue(eventListener.getEventSource().getName().equalsIgnoreCase("com.example.testable.request"));
	}

	// Test Case No : 10
	// Register A Wildcard Listener to listen all the events using registerWildcardListener API
	@Test
	public void testRegisterWildcardListener_whenRegistered_returnsNoError() {

		// setup
		String extensionName = "ThirdPartyExtension10";
		// test
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, wildcardListener);
		EventListener eventListener = extensionTestingHelper.getListenerInstance(extensionName, wildcardListener.get(0));
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertEquals(eventListener.getEventType().getName(), wildcardListener.get(0).eventType);
		assertEquals(eventListener.getEventSource().getName(), wildcardListener.get(0).eventSource);
	}

	// Test Case No : 10a
	// Dispatch events of types identity, analytics, custom, and config using ACPCore dispatchEvent API
	// And confirm that the wildcard listener is able to hear all those dispatched.
	@Test
	public void testDispatchEvent_whenWildcardListenerRegistered_hearsAllTheEventsAndreturnsNoError() {

		// setup
		String extensionName = "ThirdPartyExtension11a";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, wildcardListener);
		EventListener eventListener = extensionTestingHelper.getListenerInstance(extensionName, wildcardListener.get(0));
		// test
		extensionTestingHelper.dispatchAnEvent(identityListenerTypes.get(0), eventData);
		extensionTestingHelper.dispatchAnEvent(analyticsListenerTypes.get(0), subEventData);
		extensionTestingHelper.dispatchAnEvent(customListenerTypes.get(0), eventData);
		extensionTestingHelper.dispatchAnEvent(configListenerTypes.get(0), subEventData);
		List<Event> eventsHearedByWildcardListener = extensionTestingHelper.getAllEventsHeardByListener(extensionName,
				wildcardListener.get(0));
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertEquals(eventListener.getEventType().getName(), wildcardListener.get(0).eventType);
		assertEquals(eventListener.getEventSource().getName(), wildcardListener.get(0).eventSource);
		assertTrue(eventsHearedByWildcardListener.get(1).getEventType().getName().equalsIgnoreCase(identityListenerTypes.get(
					   0).eventType));
		assertTrue(eventsHearedByWildcardListener.get(1).getEventSource().getName().equalsIgnoreCase(identityListenerTypes.get(
					   0).eventSource));
		assertTrue(eventsHearedByWildcardListener.get(1).getEventData().equals(eventData));
		assertTrue(eventsHearedByWildcardListener.get(2).getEventType().getName().equalsIgnoreCase(analyticsListenerTypes.get(
					   0).eventType));
		assertTrue(eventsHearedByWildcardListener.get(2).getEventSource().getName().equalsIgnoreCase(analyticsListenerTypes.get(
					   0).eventSource));
		assertTrue(eventsHearedByWildcardListener.get(2).getEventData().equals(subEventData));
		assertTrue(eventsHearedByWildcardListener.get(3).getEventType().getName().equalsIgnoreCase(customListenerTypes.get(
					   0).eventType));
		assertTrue(eventsHearedByWildcardListener.get(3).getEventSource().getName().equalsIgnoreCase(customListenerTypes.get(
					   0).eventSource));
		assertTrue(eventsHearedByWildcardListener.get(3).getEventData().equals(eventData));
		assertTrue(eventsHearedByWildcardListener.get(4).getEventType().getName().equalsIgnoreCase(configListenerTypes.get(
					   0).eventType));
		assertTrue(eventsHearedByWildcardListener.get(4).getEventSource().getName().equalsIgnoreCase(configListenerTypes.get(
					   0).eventSource));
		assertEquals(eventsHearedByWildcardListener.get(4).getEventData(), subEventData);
	}

	// Test Case No : 11 & 12
	// Dispatch A Custom Event using ACPCore dispatchEvent API
	@Test
	public void testDispatchEvent_whenCustomEvent_returnsTrue() {

		// setup
		ListenerType listenerType =  customListenerTypes.get(0);
		// test
		boolean dispatchStatus = extensionTestingHelper.dispatchAnEvent(listenerType, eventData);
		// verify
		assertTrue(dispatchStatus);
	}


	// Test Case No :  13 &  14
	// Dispatch an event using ACPCore dispatchResponseEvent API
	@Test
	public void testDispatchEventWithResponseCallback_whenPaired_returnsPairedEventAndData() throws InterruptedException {

		// setup
		String extensionName = "ThirdPartyExtension13";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, customListenerTypes);
		TestableListener eventListener = (TestableListener) extensionTestingHelper.getListenerInstance(extensionName,
										 customListenerTypes.get(0));
		eventListener.setDispatchBehavior("doDispatchResponseEvent");
		ListenerType listenerType =  customListenerTypes.get(0);
		// test
		Event responseEvent = new Event.Builder("DispatchedEvent", listenerType.eventType,
												listenerType.eventSource).setEventData(eventData).build();
		final List<Event> result = new ArrayList<Event>();
		final CountDownLatch latch = new CountDownLatch(1);
		AdobeCallback dispatchCallback = new AdobeCallback<Event>() {
			@Override
			public void call(Event value)  {
				result.add(value);
				latch.countDown();
			}
		};
		boolean dispatchStatus = MobileCore.dispatchEventWithResponseCallback(responseEvent, dispatchCallback, null);
		latch.await(10, TimeUnit.SECONDS);
		// verify
		assertTrue(dispatchStatus);
		assertTrue(result.get(0).getType().equalsIgnoreCase("com.adobe.eventtype.pairedresponse"));
		assertTrue(result.get(0).getSource().equalsIgnoreCase("com.example.testable.pairedrequest"));
		assertEquals(pairedEventData, result.get(0).getEventData());
	}

	// Test Case No : 15, 17 & 36
	// Get Shared Event State Owned By A Configuration Event using getSharedEventState API
	// with the extension name as the stateowner like
	// com.adobe.module.identity, com.adobe.module.configuration
	@Test
	public void testGetSharedEventState_whenConfigEvent_returnsAppropriateSharedState() {

		// setup
		String extensionName = "ThirdPartyExtension15";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,
											   configListenerTypes);
		TestableExtension testableExtension = (TestableExtension) extensionTestingHelper.getExtensionInstance(extensionName);

		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		// test
		updateConfiguration();
		Event event = new Event.Builder("DispatchedEvent", configListenerTypes.get(0).eventType,
										configListenerTypes.get(0).eventSource).setEventData(null).setEventNumber(100).build();
		asyncHelper.waitForAppThreads(500, true);
		Map<String, Object> configurationSharedState =
			testableExtension.getApi().getSharedEventState(CONFIGURATION_SHARED_STATE, event, errorCallback);
		Event eventHeard = extensionTestingHelper.getLastEventHeardByListener(extensionName, configListenerTypes.get(0));
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertEquals(configMap, configurationSharedState);
		assertEquals(configMap, eventHeard.getEventData());

	}

	// Test Case No : 16
	// Get Shared Event State Owned By A Custom Event using getSharedEventState API for stateowner
	// as extensionName.
	@Test
	public void testGetSharedEventState_whenItIsSet_returnsAppropriateSharedState() {

		// setup
		String extensionName = "Extension18";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,
											   customListenerTypes);
		TestableExtension testableExtension = (TestableExtension)
											  extensionTestingHelper.getExtensionInstance(extensionName);

		Event event = new Event.Builder("DispatchedEvent", customListenerTypes.get(0).eventType,
										customListenerTypes.get(0).eventSource).setEventData(eventData).build();
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		boolean status1 = testableExtension.getApi().setSharedEventState(subEventData, event, errorCallback);

		Map<String, Object> sharedState = testableExtension.getApi().getSharedEventState(
											  testableExtension.getName(), event, errorCallback);
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertEquals(subEventData, sharedState);
	}

	// Test Case No : 18
	// Set Shared Event State that is not tied to an event using setSharedEventState API
	@Test
	public void testSetAndGetSharedEventState_whenNullEvent_setsAndGetsAppropriateSharedState() {

		// setup
		String extensionName = "Extension18";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,
											   configListenerTypes);
		TestableExtension testableExtension = (TestableExtension)
											  extensionTestingHelper.getExtensionInstance(extensionName);

		Event event = new Event.Builder("DispatchedEvent", configListenerTypes.get(0).eventType,
										configListenerTypes.get(0).eventSource).setEventData(eventData).build();
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		boolean status1 = testableExtension.getApi().setSharedEventState(subEventData, event, errorCallback);

		Map<String, Object> sharedState = testableExtension.getApi().getSharedEventState(
											  testableExtension.getName(), event, errorCallback);
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertEquals(subEventData, sharedState);
	}

	// Test Case No : 19
	// Clear Shared Event States Of A Third Party Extension without affecting
	// the Shared Event States Of other extensions using clearSharedEventStates API
	@Test
	public void testClearSharedEventStates_whenMultipleRegisteredExtensions_doesNotAffectSharedStateOfOtherExtensions() {

		// setup
		CreateExtensionResponse returnStatus1 = extensionTestingHelper.registerExtension("ThirdPartyExtensionOne",
												configListenerTypes);
		TestableExtension testableExtension1 = (TestableExtension)
											   extensionTestingHelper.getExtensionInstance("ThirdPartyExtensionOne");
		CreateExtensionResponse returnStatus2 = extensionTestingHelper.registerExtension("ThirdPartyExtensionTwo",
												configListenerTypes);
		TestableExtension testableExtension2 = (TestableExtension)
											   extensionTestingHelper.getExtensionInstance("ThirdPartyExtensionTwo");
		Event event = new Event.Builder("DispatchedEvent", configListenerTypes.get(0).eventType,
										configListenerTypes.get(0).eventSource).setEventData(eventData).build();
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		boolean status1 = testableExtension1.getApi().setSharedEventState(subEventData, event, errorCallback);
		boolean status2 = testableExtension2.getApi().setSharedEventState(subEventData, event, errorCallback);
		ExtensionErrorCallback<ExtensionError> errorCallback1 = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		Map<String, Object> sharedStateBeforeClearing1 = testableExtension1.getApi().getSharedEventState(
					testableExtension1.getName(), event, errorCallback1);
		Map<String, Object> sharedStateBeforeClearing2 = testableExtension2.getApi().getSharedEventState(
					testableExtension2.getName(), event, errorCallback1);
		ExtensionErrorCallback<ExtensionError> errorCallback2 = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
				Log.debug(LOG_TAG, String.format("An error occurred while clearing the shared states %d %s",
												 extensionError.getErrorCode(), extensionError.getErrorName()));
			}
		};
		// test
		testableExtension1.getApi().clearSharedEventStates(errorCallback2);
		Map<String, Object> getSharedStateAfterClearing1 = testableExtension1.getApi().getSharedEventState(
					testableExtension1.getName(), event, errorCallback1);
		Map<String, Object> getSharedStateAfterClearing2 = testableExtension2.getApi().getSharedEventState(
					testableExtension2.getName(), event, errorCallback1);
		// verify
		assertNull(returnStatus1.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered("ThirdPartyExtensionOne"));
		assertNull(returnStatus2.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered("ThirdPartyExtensionTwo"));
		assertEquals(subEventData, sharedStateBeforeClearing1);
		assertEquals(subEventData, sharedStateBeforeClearing2);
		assertNull(getSharedStateAfterClearing1);
		assertEquals(subEventData, getSharedStateAfterClearing2);
	}

	// Test Case No : 22
	// Make a copy of the event object received using event.copy() method,
	// and the correctness of the copied event object.
	@Test
	public void testCopyEvent_whenCopied_returnsTheCopiedEvent() {

		// setup
		ListenerType listenerType =  customListenerTypes.get(0);
		Event event = new Event.Builder("DispatchedEvent", listenerType.eventType,
										listenerType.eventSource).setEventData(eventData).build();
		// setup
		Event copiedEvent = event.copy();
		// verify
		assertEquals(eventData, copiedEvent.getEventData());
		assertEquals(listenerType.eventType, copiedEvent.getType());
		assertEquals(listenerType.eventSource, copiedEvent.getSource());
	}

	// Test Case No : 23
	// Register the same extension multiple times - should not register, the initial extension
	// should still be registered and receive events,
	@Test
	public void testCreateExtension_whenDuplicateName_returnsError() {

		// setup
		String extensionName = "ThirdPartyExtension23";
		// test
		CreateExtensionResponse returnStatus1 = extensionTestingHelper.registerExtension(extensionName,
												configListenerTypes);
		CreateExtensionResponse returnStatus2 = extensionTestingHelper.registerExtension(extensionName,
												configListenerTypes);
		// verify
		assertNull(returnStatus1.extensionUnexpectedError);
		assertEquals(2, returnStatus2.extensionUnexpectedError.getErrorCode().getErrorCode());
		assertTrue(returnStatus2.extensionUnexpectedError.getMessage().contains("Failed to register extension with name " +
				   extensionName));
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
	}

	// Test Case No : 26
	// Register an extension with null version - should be ok, no NPE should be thrown in logs
	@Test
	public void testCreateExtension_whenNullVersion_returnsNoError() {

		// setup
		String extensionName = "ExtensionWithNullVersion";
		// test
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,
											   configListenerTypes);
		TestableExtension testableExtension = (TestableExtension)
											  extensionTestingHelper.getExtensionInstance(extensionName);
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertNull(testableExtension.getVersion());
	}

	// Test Case No : 27
	// Same as Test case No : 19, but checking with the same extension by re-registering it.
	@Test
	public void testGetSharedEventState_whenExtensionReRegistered_theSharedStateIsUnaffected() {

		// setup
		String extensionName = "ThirdPartyExtension27";
		CreateExtensionResponse returnStatus1 = extensionTestingHelper.registerExtension(extensionName, configListenerTypes);
		TestableExtension testableExtension = (TestableExtension) extensionTestingHelper.getExtensionInstance(extensionName);
		Event event = new Event.Builder("DispatchedEvent", configListenerTypes.get(0).eventType,
										configListenerTypes.get(0).eventSource).setEventData(eventData).build();
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		boolean status = testableExtension.getApi().setSharedEventState(subEventData, event, errorCallback);
		Map<String, Object> sharedStateBeforeUnregister = testableExtension.getApi().getSharedEventState(
					testableExtension.getName(), event, errorCallback);
		// test
		boolean unregisterStatus = extensionTestingHelper.unregisterExtension(extensionName);
		CreateExtensionResponse returnStatus2 = extensionTestingHelper.registerExtension(extensionName, configListenerTypes);
		testableExtension = (TestableExtension) extensionTestingHelper.getExtensionInstance(extensionName);
		Map<String, Object> sharedStateAfterReregister = testableExtension.getApi().getSharedEventState(
					testableExtension.getName(), event, errorCallback);
		// verify
		assertNull(returnStatus1.extensionUnexpectedError);
		assertTrue(unregisterStatus);
		assertNull(returnStatus2.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertEquals(subEventData, sharedStateBeforeUnregister);
		assertEquals(subEventData, sharedStateAfterReregister);
	}

	// Test Case No : 28
	// Register a listener with null eventType - should be ok, no NPE should be thrown in logs
	@Test
	public void testRegisterListener_whenNullEventType_returnsErrorCode() {

		// setup
		String extensionName = "ThirdPartyExtension28a";
		// setup
		List<ListenerType> listenerType = new ArrayList<ListenerType>();
		listenerType.add(new ListenerType(null, "com.example.testable.request"));
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,
											   listenerType);
		// test
		Map<ListenerType, ExtensionError> listenerRegistrationStatus = TestableExtension.getListenerRegistrationStatus();
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertEquals(listenerRegistrationStatus.get(listenerType.get(0)).getErrorCode(), 3);
		assertEquals(listenerRegistrationStatus.get(listenerType.get(0)).getErrorName(), "extension.event_type_not_supported");
	}

	// Test Case No : 28a
	// Register a listener with null eventSource - should be ok, no NPE should be thrown in logs
	@Test
	public void testRegisterListener_whenNullEventSource_returnsErrorCode() {

		// setup
		String extensionName = "ThirdPartyExtension28b";
		// test
		List<ListenerType> listenerType = new ArrayList<ListenerType>();
		listenerType.add(new ListenerType("com.example.testable.custom", null));
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, listenerType);
		Map<ListenerType, ExtensionError> listenerRegistrationStatus = TestableExtension.getListenerRegistrationStatus();
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertEquals(listenerRegistrationStatus.get(listenerType.get(0)).getErrorCode(), 4);
		assertEquals(listenerRegistrationStatus.get(listenerType.get(0)).getErrorName(),
					 "extension.event_source_not_supported");
	}

	// Test Case No : 28b
	// Register a listener with null for both eventType and eventSource - should be ok, no NPE should be thrown in logs
	@Test
	public void testRegisterListener_whenNullEventTypeAndSource_returnsErrorCode() {

		// setup
		String extensionName = "ThirdPartyExtension28c";
		// test
		List<ListenerType> listenerType = new ArrayList<ListenerType>();
		listenerType.add(new ListenerType(null, null));
		CreateExtensionResponse returnStatus =
			extensionTestingHelper.registerExtension(extensionName, listenerType);
		Map<ListenerType, ExtensionError> listenerRegistrationStatus = TestableExtension.getListenerRegistrationStatus();
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertEquals(listenerRegistrationStatus.get(listenerType.get(0)).getErrorCode(), 3);
		assertEquals(listenerRegistrationStatus.get(listenerType.get(0)).getErrorName(), "extension.event_type_not_supported");
	}

	// Test Case No :  29
	// Register listener which does busy work on the event hub thread
	// Expected : It should not block the event hub in dispatching new events
	@Test
	@Ignore
	public void testWithAListenerDoingBusyWorkOnTheHub_whenAnotherEventIsDispatched_eventHubShouldDispatchIt() throws
		InterruptedException {

		// setup
		final String customExtension = "CustomExtension29";
		final String busyWorkExtension = "BusyWorkExtension29";

		final List<ListenerType> listenerTypes = new ArrayList<ListenerType>();

		final List<Event> eventsHeard = new ArrayList<Event>();
		final List<Event> result = new ArrayList<Event>();
		final ArrayList<Event>[] eventsHeardByListener = new ArrayList[4];
		listenerTypes.add(new ListenerType("com.adobe.eventtype.busywork", "com.example.testable.busywork"));
		listenerTypes.add(new ListenerType("com.adobe.eventtype.customevent", "com.example.testable.customevent"));
		CreateExtensionResponse registrationStatus1 = extensionTestingHelper.registerExtension(customExtension, listenerTypes);
		CreateExtensionResponse registrationStatus2 = extensionTestingHelper.registerExtension(busyWorkExtension,
				listenerTypes);

		ExtensionErrorCallback<ExtensionError> dispatchCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError ec) {
				Log.debug(LOG_TAG, String.format("[dispatchAnEvent] Dispatch failed with error %s ", ec.getErrorCode()));
			}
		};

		Event busyEvent = new Event.Builder("DispatchedBusyEvent", listenerTypes.get(0).eventType,
											listenerTypes.get(0).eventSource).setEventData(subEventData).build();
		Event customEvent = new Event.Builder("DispatchedCustomEvent", listenerTypes.get(1).eventType,
											  listenerTypes.get(1).eventSource).setEventData(subEventData).build();

		MobileCore.dispatchEvent(customEvent, dispatchCallback);
		MobileCore.dispatchEvent(customEvent, dispatchCallback);
		MobileCore.dispatchEvent(busyEvent, dispatchCallback);
		MobileCore.dispatchEvent(customEvent, dispatchCallback);
		MobileCore.dispatchEvent(customEvent, dispatchCallback);
		asyncHelper.waitForAppThreads(500, true);
		eventsHeardByListener[0] = (ArrayList) extensionTestingHelper.getAllEventsHeardByListener(customExtension,
								   listenerTypes.get(0));
		eventsHeardByListener[1] = (ArrayList) extensionTestingHelper.getAllEventsHeardByListener(customExtension,
								   listenerTypes.get(1));
		eventsHeardByListener[2] = (ArrayList) extensionTestingHelper.getAllEventsHeardByListener(busyWorkExtension,
								   listenerTypes.get(0));
		eventsHeardByListener[3] = (ArrayList) extensionTestingHelper.getAllEventsHeardByListener(busyWorkExtension,
								   listenerTypes.get(1));
		// verify
		assertEquals(1, eventsHeardByListener[0].size());
		assertEquals(4, eventsHeardByListener[1].size());
		assertEquals(1, eventsHeardByListener[2].size());
		assertEquals(4, eventsHeardByListener[3].size());
	}


	// Test Case No : 30
	// Build a custom event with null event data should not crash
	@Test
	public void testDispatchEvent_whenNullEventData_returnsNoError() {

		// setup
		String extensionName = "ThirdPartyExtension30";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,
											   identityListenerTypes);
		TestableExtension testableExtension = (TestableExtension)
											  extensionTestingHelper.getExtensionInstance(extensionName);
		ListenerType listenerType = new ListenerType("com.adobe.eventType.configuration",
				"com.adobe.eventSource.requestContent");
		Event event = new Event.Builder("DispatchedEvent", listenerType.eventType,
										listenerType.eventSource).setEventData(null).build();
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		boolean status = testableExtension.getApi().setSharedEventState(null, event, errorCallback);
		// test
		ExtensionErrorCallback<ExtensionError> errorCallback1 = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		Map<String, Object> configurationSharedState = testableExtension.getApi().getSharedEventState(
					testableExtension.getName(), event, errorCallback1);
		ExtensionErrorCallback<ExtensionError> dispatchCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError ec) {
			}
		};
		boolean dispatchStatus = MobileCore.dispatchEvent(event, dispatchCallback);
		asyncHelper.waitForAppThreads(500, true);
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertNull(configurationSharedState);
		assertTrue(dispatchStatus);
	}

	// Test Case No : 31 Skipped due to the bug AMSDK-7597
	// SDK v5 does not have support for Short, Float, and Byte Data types
	// Check under bourbon-core-java-core/code/shared/src/main/java/com/adobe/marketing/mobile
	// Build a custom event data with byte data type
	// dispatch it and check the returned values are the same.
	@Test
	public void testDispatchEvent_whenEventDataWithByteTypeValue_returnsNoError() {

		// setup
		String extensionName = "ThirdPartyExtension31a";
		Map<String, Object> testEventData = new HashMap<String, Object>();
		int byteData = 0b0010_0101;
		testEventData.put("customByte", byteData);
		// tes
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,
											   identityListenerTypes);
		extensionTestingHelper.dispatchAnEvent(identityListenerTypes.get(0), testEventData);
		Event event = extensionTestingHelper.getLastEventHeardByListener(extensionName,
					  identityListenerTypes.get(0));

		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertTrue(identityListenerTypes.get(0).eventType.equalsIgnoreCase(event.getType()));
		assertTrue(identityListenerTypes.get(0).eventSource.equalsIgnoreCase(event.getSource()));
		assertEquals(testEventData, event.getEventData());
	}

	// Test Case No : 31a Skipped due to the bug AMSDK-7596
	// Build a custom event data with Float data type
	// dispatch it and check the returned values are the same.
	@Ignore
	public void testDispatchEvent_whenEventDataWithFloatTypeValue_returnsNoError() {

		// setup
		String extensionName = "ThirdPartyExtension31b";
		Map<String, Object> testEventData = new HashMap<String, Object>();
		testEventData.put("customFloat", new Float(3.14));
		// test
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,
											   identityListenerTypes);
		extensionTestingHelper.dispatchAnEvent(identityListenerTypes.get(0), testEventData);
		Event event = extensionTestingHelper.getLastEventHeardByListener(extensionName,
					  identityListenerTypes.get(0));
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertTrue(identityListenerTypes.get(0).eventType.equalsIgnoreCase(event.getType()));
		assertTrue(identityListenerTypes.get(0).eventSource.equalsIgnoreCase(event.getSource()));
		assertEquals(testEventData, event.getEventData());
	}

	// Test Case No : 31b  Skipped due to the bug AMSDK-7595
	// Build a custom event data with Short data type
	// dispatch it and check the returned values are the same.
	@Ignore
	public void testDispatchEvent_whenEventDataWithShortTypeValue_returnsNoError() {

		// setup
		String extensionName = "ThirdPartyExtension31c";
		Map<String, Object> testEventData = new HashMap<String, Object>();
		testEventData.put("customShort", (short)300);
		// test
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,
											   identityListenerTypes);
		extensionTestingHelper.dispatchAnEvent(identityListenerTypes.get(0), testEventData);
		Event event = extensionTestingHelper.getLastEventHeardByListener(extensionName,
					  identityListenerTypes.get(0));
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertTrue(identityListenerTypes.get(0).eventType.equalsIgnoreCase(event.getType()));
		assertTrue(identityListenerTypes.get(0).eventSource.equalsIgnoreCase(event.getSource()));
		assertEquals(testEventData, event.getEventData());
	}

	// Test Case No :  32
	// Dispatch with null event, should return false and error in the callback
	@Test
	public void testDispatchEvent_whenNullEvent_returnsError() throws InterruptedException {

		// test
		ExtensionErrorCallback<ExtensionError> dispatchCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError ec) {
				extensionError = ec;
			}
		};
		boolean dispatchStatus = MobileCore.dispatchEvent(null, dispatchCallback);
		asyncHelper.waitForAppThreads(500, true);
		// verify
		assertFalse(dispatchStatus);
		assertEquals("extension.event_null", extensionError.getErrorName());
		assertEquals(6, extensionError.getErrorCode());
	}

	// Test Case No :  33
	// Dispatch response event with null event, should return false and error in the callback
	@Test
	public void testDispatchEventWithResponseCallback_whenNullResponseEvent_returnsError() throws InterruptedException {

		// setup
		String extensionName = "ThirdPartyExtension33";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, customListenerTypes);
		TestableListener eventListener = (TestableListener) extensionTestingHelper.getListenerInstance(extensionName,
										 customListenerTypes.get(0));
		eventListener.setDispatchBehavior("doDispatchResponseEventWithNullEvent");
		ListenerType listenerType =  customListenerTypes.get(0);
		// test
		Event responseEvent = new Event.Builder("DispatchedEvent", listenerType.eventType,
												listenerType.eventSource).setEventData(eventData).build();
		final List<Event> result = new ArrayList<Event>();
		final CountDownLatch latch = new CountDownLatch(1);
		AdobeCallback dispatchCallback = new AdobeCallback<Event>() {
			@Override
			public void call(Event value)  {
				result.add(value);
				latch.countDown();
			}
		};
		boolean dispatchStatus = MobileCore.dispatchEventWithResponseCallback(responseEvent, dispatchCallback, null);
		latch.await(500, TimeUnit.MILLISECONDS);
		// verify
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertTrue(dispatchStatus);
		assertTrue(result.isEmpty());
		assertEquals("extension.event_null", eventListener.getExtensionError().getErrorName());
		assertEquals(6, eventListener.getExtensionError().getErrorCode());
	}

	// Test Case No : 34
	// Get the shared state for the custom extension, should be null if not set, should be valid if set before
	@Test
	public void testGetSharedEventState_whenItIsNotSet_returnsNull() {

		// setup
		String extensionName = "ThirdPartyExtension34";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, customListenerTypes);
		TestableExtension testableExtension = (TestableExtension)
											  extensionTestingHelper.getExtensionInstance(extensionName);
		ListenerType listenerType =  customListenerTypes.get(0);
		Event event = new Event.Builder("DispatchedEvent", listenerType.eventType,
										listenerType.eventSource).setEventData(eventData).build();

		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		// test
		Map<String, Object> sharedStateBeforeItWasSet = testableExtension.getApi().getSharedEventState(
					testableExtension.getName(),
					event, errorCallback);
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertNull(sharedStateBeforeItWasSet);
	}

	// Test Case No : 35
	// SetSharedState with null state, null event, should not crash
	@Test
	public void testSetAndGetSharedEventState_whenNullStateAndEvent_returnsNoError() {

		// setup
		String extensionName = "ThirdPartyExtension35";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, customListenerTypes);
		TestableExtension testableExtension = (TestableExtension)
											  extensionTestingHelper.getExtensionInstance(extensionName);

		final ExtensionError[] extenError = new ExtensionError[1];
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
				extenError[0] = extensionError;
			}
		};
		boolean status = testableExtension.getApi().setSharedEventState(null, null, errorCallback);
		// test
		Map<String, Object> customSharedState = testableExtension.getApi().getSharedEventState(testableExtension.getName(),
												null, errorCallback);
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertNull(customSharedState);
		assertNull(extenError[0]);
	}

	// Test Case No : 38
	// Simulate an analytics 3rd party extension - dispatching & receiving generic events when trackAction called
	@Test
	public void testTrackAction_whenDispatched_receivedByGenericTrackEventType() {
		// setup
		String extensionName = "AnalyticsExtension38a";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,
											   analyticsListenerTypes);
		TestableExtension testableExtension = (TestableExtension) extensionTestingHelper.getExtensionInstance(extensionName);
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		// test
		Map<String, String> additionalContextData = new HashMap<String, String>();
		additionalContextData.put("customKey", "value");
		MobileCore.trackAction("eventDispatched", additionalContextData);
		asyncHelper.waitForAppThreads(500, true);
		Event    eventHeard = extensionTestingHelper.getLastEventHeardByListener(extensionName, analyticsListenerTypes.get(4));
		// verify
		assertEquals("eventDispatched", eventHeard.getEventData().get("action"));
		assertEquals(eventHeard.getEventData().get("contextdata"), additionalContextData);
		assertTrue(eventHeard.getEventSource().getName().equalsIgnoreCase(analyticsListenerTypes.get(4).eventSource));
		assertTrue(eventHeard.getEventType().getName().equalsIgnoreCase(analyticsListenerTypes.get(4).eventType));

		MobileCore.trackState("trackState", additionalContextData);
	}

	// Test Case No : 38a
	// Simulate an analytics 3rd party extension - dispatching & receiving generic events when TrackState called
	@Test
	public void testTrackState_whenDispatched_receivedByGenericTrackEventType() {
		// setup
		String extensionName = "AnalyticsExtension38a";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,
											   analyticsListenerTypes);
		TestableExtension testableExtension = (TestableExtension) extensionTestingHelper.getExtensionInstance(extensionName);
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		// test
		Map<String, String> additionalContextData = new HashMap<String, String>();
		additionalContextData.put("customKey", "value");
		MobileCore.trackState("trackState", additionalContextData);
		asyncHelper.waitForAppThreads(500, true);
		Event    eventHeard = extensionTestingHelper.getLastEventHeardByListener(extensionName, analyticsListenerTypes.get(4));
		// verify
		assertEquals("trackState", eventHeard.getEventData().get("state"));
		assertEquals(additionalContextData, eventHeard.getEventData().get("contextdata"));
		assertTrue(eventHeard.getEventSource().getName().equalsIgnoreCase(analyticsListenerTypes.get(4).eventSource));
		assertTrue(eventHeard.getEventType().getName().equalsIgnoreCase(analyticsListenerTypes.get(4).eventType));
	}
	// Test Case No : 38b
	// Dispatch different types of Analytics Events and
	// Confirm if they are all received by the appropriate listeners registered.
	@Test
	public void testAnalyticsExtension_whenAnalyticsEventsDispatched_receivesAllDispatchedEvents() {
		// setup
		String extensionName = "AnalyticsExtension38";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,
											   analyticsListenerTypes);
		TestableExtension testableExtension = (TestableExtension) extensionTestingHelper.getExtensionInstance(extensionName);

		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		// test
		Event[] eventHeard = new Event[analyticsListenerTypes.size()];

		for (int i = 0; i < analyticsListenerTypes.size(); i++) {
			subEventData.put("newKey" + i, "newValue" + i);
			Event event = new Event.Builder("DispatchedEvent", analyticsListenerTypes.get(i).eventType,
											analyticsListenerTypes.get(i).eventSource).setEventData(subEventData).build();
			ExtensionErrorCallback<ExtensionError> dispatchCallback = new ExtensionErrorCallback<ExtensionError>() {
				@Override
				public void error(final ExtensionError ec) {
					Log.debug(LOG_TAG, String.format("[dispatchAnEvent] Dispatch failed with error %s ", ec.getErrorCode()));
				}
			};
			MobileCore.dispatchEvent(event, dispatchCallback);
			asyncHelper.waitForAppThreads(500, true);
			eventHeard[i] = extensionTestingHelper.getLastEventHeardByListener(extensionName, analyticsListenerTypes.get(i));
			// verify
			assertEquals(subEventData, eventHeard[i].getEventData());
			assertTrue(eventHeard[i].getEventSource().getName().equalsIgnoreCase(analyticsListenerTypes.get(i).eventSource));
			assertTrue(eventHeard[i].getEventType().getName().equalsIgnoreCase(analyticsListenerTypes.get(i).eventType));
		}
	}

	// Test Case No : 39
	// Test communication between two 3rd party extensions - dispatch events and set shared states from one of them,
	// Check updates are received in the other extension
	@Test
	public void testCommunicationBetweenExtensions_whenOneDispatchesEventsAndSetsSharedState_otherExtensionReceivesThem() {

		// setup
		String extensionName1 = "Extension39One";
		CreateExtensionResponse returnStatus1 = extensionTestingHelper.registerExtension(extensionName1, customListenerTypes);
		TestableExtension testableExtension1 = (TestableExtension)
											   extensionTestingHelper.getExtensionInstance(extensionName1);

		String extensionName2 = "Extension39two";
		CreateExtensionResponse returnStatus2 = extensionTestingHelper.registerExtension(extensionName2, customListenerTypes);
		TestableExtension testableExtension2 = (TestableExtension)
											   extensionTestingHelper.getExtensionInstance(extensionName2);


		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
				Log.debug(LOG_TAG, String.format("An error occurred while setting the shared state %d %s",
												 extensionError.getErrorCode(), extensionError.getErrorName()));
			}
		};

		// test
		Event event = new Event.Builder("DispatchedEvent", customListenerTypes.get(0).eventType,
										customListenerTypes.get(0).eventSource).setEventData(eventData).build();
		MobileCore.dispatchEvent(event, errorCallback);
		testableExtension1.getApi().setSharedEventState(subEventData, event, errorCallback);
		asyncHelper.waitForAppThreads(500, true);

		Event eventHeard = extensionTestingHelper.getLastEventHeardByListener(extensionName2, customListenerTypes.get(0));
		Map<String, Object> sharedState = testableExtension2.getApi().getSharedEventState(
											  testableExtension1.getName(), event, errorCallback);

		// verify
		assertEquals(eventData, eventHeard.getEventData());
		assertTrue(eventHeard.getEventSource().getName().equalsIgnoreCase(customListenerTypes.get(0).eventSource));
		assertTrue(eventHeard.getEventType().getName().equalsIgnoreCase(customListenerTypes.get(0).eventType));
		assertEquals(subEventData, sharedState);
	}

	// Test Case No :  40
	// Check that paired response events are received by another extension in a wildcard listener, but not by a regular listener.
	// The response should be received in the callback by the first extension.
	@Ignore
	public void testDispatchEventWithResponseCallback_whenAnotherExtensionListens_OnlyItsWildcordListenersCanHearIt() throws
		InterruptedException {

		// setup
		String extensionName1 = "Extension40One";
		CreateExtensionResponse registrationStatus1 = extensionTestingHelper.registerExtension(extensionName1,
				customListenerTypes);
		Extension extensionInstance1 = extensionTestingHelper.getExtensionInstance(extensionName1);
		TestableListener eventListener1 = (TestableListener) extensionTestingHelper.getListenerInstance(extensionName1,
										  customListenerTypes.get(0));
		eventListener1.setDispatchBehavior("doDispatchResponseEvent");

		String extensionName2 = "Extension40Two";
		List<ListenerType> listenerTypesOfExtension2 = new ArrayList<>();
		listenerTypesOfExtension2.add(new ListenerType("com.adobe.eventtype._wildcard_", "com.adobe.eventsource._wildcard_"));
		listenerTypesOfExtension2.add(new ListenerType("com.adobe.eventtype.pairedresponse",
									  "com.example.testable.pairedrequest"));
		CreateExtensionResponse registrationStatus2 = extensionTestingHelper.registerExtension(extensionName2,
				listenerTypesOfExtension2);

		asyncHelper.waitForAppThreads(500, true);

		// test
		Event responseEvent = new Event.Builder("DispatchedEvent", customListenerTypes.get(0).eventType,
												customListenerTypes.get(0).eventSource).setEventData(subEventData).build();
		final List<Event> result = new ArrayList<Event>();
		final CountDownLatch latch = new CountDownLatch(1);
		AdobeCallback dispatchCallback = new AdobeCallback<Event>() {
			@Override
			public void call(Event value)  {
				result.add(value);
				latch.countDown();
			}
		};
		boolean dispatchStatus = MobileCore.dispatchEventWithResponseCallback(responseEvent, dispatchCallback, null);
		latch.await(500, TimeUnit.MILLISECONDS);

		List<Event> eventsHeardByWildcardListener = extensionTestingHelper.getAllEventsHeardByListener(extensionName2,
				listenerTypesOfExtension2.get(0));
		List<Event> eventsHeardByPairedResponseListener = extensionTestingHelper.getAllEventsHeardByListener(extensionName2,
				listenerTypesOfExtension2.get(1));
		// verify
		assertEquals(eventsHeardByWildcardListener.size(), 2);
		assertEquals(eventsHeardByPairedResponseListener.size(), 0);
		assertTrue(eventsHeardByWildcardListener.get(0).getEventType().getName().equalsIgnoreCase(customListenerTypes.get(
					   0).eventType));
		assertTrue(eventsHeardByWildcardListener.get(0).getEventSource().getName().equalsIgnoreCase(customListenerTypes.get(
					   0).eventSource));
		assertEquals(subEventData, eventsHeardByWildcardListener.get(0).getEventData());
		assertTrue(eventsHeardByWildcardListener.get(
					   1).getEventType().getName().equalsIgnoreCase("com.adobe.eventtype.pairedresponse"));
		assertTrue(eventsHeardByWildcardListener.get(
					   1).getEventSource().getName().equalsIgnoreCase("com.example.testable.pairedrequest"));
		assertEquals(pairedEventData, eventsHeardByWildcardListener.get(1).getEventData());
		assertTrue(dispatchStatus);
		assertTrue(result.get(0).getType().equalsIgnoreCase("com.adobe.eventtype.pairedresponse"));
		assertTrue(result.get(0).getSource().equalsIgnoreCase("com.example.testable.pairedrequest"));
		assertEquals(pairedEventData, result.get(0).getEventData());
	}

	// Test Case No : 41
	// Set XDM Shared Event State that is not tied to an event using setXDMSharedEventState API
	@Test
	public void testSetAndGetXDMSharedEventState_whenDanglingEvent_setsAndGetsAppropriateXDMSharedState() {
		// setup
		String extensionName = "Extension41";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName,
											   configListenerTypes);
		TestableExtension testableExtension = (TestableExtension)
											  extensionTestingHelper.getExtensionInstance(extensionName);
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertNotNull(testableExtension);

		Event event = new Event.Builder("DispatchedEvent", configListenerTypes.get(0).eventType,
										configListenerTypes.get(0).eventSource).setEventData(eventData).build();
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		assertTrue(testableExtension.getApi().setXDMSharedEventState(subEventData, event, errorCallback));

		Map<String, Object> sharedState = testableExtension.getApi().getXDMSharedEventState(
											  testableExtension.getName(), event, errorCallback);
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertEquals(subEventData, sharedState);
	}

	// Test Case No : 42
	// Clear XDM Shared Event States Of A Third Party Extension without affecting
	// the XDM Shared Event States Of other extensions using clearXDMSharedEventStates API
	@Test
	public void
	testClearXDMSharedEventStates_whenMultipleRegisteredExtensions_doesNotAffectXDMSharedStateOfOtherExtensions() {

		// setup
		CreateExtensionResponse returnStatus1 = extensionTestingHelper.registerExtension("ThirdPartyExtensionOne",
												configListenerTypes);
		TestableExtension testableExtension1 = (TestableExtension)
											   extensionTestingHelper.getExtensionInstance("ThirdPartyExtensionOne");
		assertNull(returnStatus1.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered("ThirdPartyExtensionOne"));
		assertNotNull(testableExtension1);

		CreateExtensionResponse returnStatus2 = extensionTestingHelper.registerExtension("ThirdPartyExtensionTwo",
												configListenerTypes);
		TestableExtension testableExtension2 = (TestableExtension)
											   extensionTestingHelper.getExtensionInstance("ThirdPartyExtensionTwo");
		assertNull(returnStatus2.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered("ThirdPartyExtensionTwo"));
		assertNotNull(testableExtension2);

		Event event = new Event.Builder("DispatchedEvent", configListenerTypes.get(0).eventType,
										configListenerTypes.get(0).eventSource).setEventData(eventData).build();
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		assertTrue(testableExtension1.getApi().setXDMSharedEventState(subEventData, event, errorCallback));
		assertTrue(testableExtension2.getApi().setXDMSharedEventState(subEventData, event, errorCallback));
		ExtensionErrorCallback<ExtensionError> errorCallback1 = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		Map<String, Object> sharedStateBeforeClearing1 = testableExtension1.getApi().getXDMSharedEventState(
					testableExtension1.getName(), event, errorCallback1);
		Map<String, Object> sharedStateBeforeClearing2 = testableExtension2.getApi().getXDMSharedEventState(
					testableExtension2.getName(), event, errorCallback1);
		ExtensionErrorCallback<ExtensionError> errorCallback2 = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
				Log.debug(LOG_TAG, String.format("An error occurred while clearing the XDM shared states %d %s",
												 extensionError.getErrorCode(), extensionError.getErrorName()));
			}
		};
		// test
		testableExtension1.getApi().clearXDMSharedEventStates(errorCallback2);
		Map<String, Object> getSharedStateAfterClearing1 = testableExtension1.getApi().getXDMSharedEventState(
					testableExtension1.getName(), event, errorCallback1);
		Map<String, Object> getSharedStateAfterClearing2 = testableExtension2.getApi().getXDMSharedEventState(
					testableExtension2.getName(), event, errorCallback1);
		// verify
		assertNull(returnStatus1.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered("ThirdPartyExtensionOne"));
		assertNull(returnStatus2.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered("ThirdPartyExtensionTwo"));
		assertEquals(subEventData, sharedStateBeforeClearing1);
		assertEquals(subEventData, sharedStateBeforeClearing2);
		assertNull(getSharedStateAfterClearing1);
		assertEquals(subEventData, getSharedStateAfterClearing2);
	}

	// Test Case No : 43
	// Get the XDM shared state for the custom extension, should be null if not set, should be valid if set before
	@Test
	public void testGetXDMSharedEventState_whenItIsNotSet_returnsNull() {

		// setup
		String extensionName = "ThirdPartyExtension43";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, customListenerTypes);
		TestableExtension testableExtension = (TestableExtension)
											  extensionTestingHelper.getExtensionInstance(extensionName);
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertNotNull(testableExtension);

		ListenerType listenerType =  customListenerTypes.get(0);
		Event event = new Event.Builder("DispatchedEvent", listenerType.eventType,
										listenerType.eventSource).setEventData(eventData).build();

		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
			}
		};
		// test
		Map<String, Object> sharedStateBeforeItWasSet = testableExtension.getApi().getXDMSharedEventState(
					testableExtension.getName(),
					event, errorCallback);
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertNull(sharedStateBeforeItWasSet);
	}

	// Test Case No : 44
	// SetXDMSharedState with valid state, null event, should not crash
	@Test
	public void testSetAndGetXDMSharedEventState_whenValidStateAndNullEvent_returnsNoError() {

		// setup
		String extensionName = "ThirdPartyExtension44";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, customListenerTypes);
		TestableExtension testableExtension = (TestableExtension)
											  extensionTestingHelper.getExtensionInstance(extensionName);

		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertNotNull(testableExtension);

		final ExtensionError[] extenError = new ExtensionError[1];
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
				extenError[0] = extensionError;
			}
		};
		Map<String, Object> state = new HashMap<String, Object>();
		state.put("testKey", "testVal");
		assertTrue(testableExtension.getApi().setXDMSharedEventState(state, null, errorCallback));
		// test
		Map<String, Object> customSharedState = testableExtension.getApi().getXDMSharedEventState(testableExtension.getName(),
												null, errorCallback);
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertEquals(state, customSharedState);
		assertNull(extenError[0]);
	}

	// Test Case No : 45
	// SetXDMSharedState with null state, valid event, should not crash
	@Test
	public void testSetAndGetXDMSharedEventState_whenNullStateAndValidEvent_returnsNoError() {

		// setup
		String extensionName = "ThirdPartyExtension44";
		CreateExtensionResponse returnStatus = extensionTestingHelper.registerExtension(extensionName, customListenerTypes);
		TestableExtension testableExtension = (TestableExtension)
											  extensionTestingHelper.getExtensionInstance(extensionName);

		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertNotNull(testableExtension);

		final ExtensionError[] extenError = new ExtensionError[1];
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
				extenError[0] = extensionError;
			}
		};
		Event event = new Event.Builder("DispatchedEvent", configListenerTypes.get(0).eventType,
										configListenerTypes.get(0).eventSource).setEventData(eventData).build();
		assertTrue(testableExtension.getApi().setXDMSharedEventState(null, event, errorCallback));
		// test
		Map<String, Object> customSharedState = testableExtension.getApi().getXDMSharedEventState(testableExtension.getName(),
												event, errorCallback);
		// verify
		assertNull(returnStatus.extensionUnexpectedError);
		assertTrue(extensionTestingHelper.isRegistered(extensionName));
		assertNull(customSharedState);
		assertNull(extenError[0]);
	}

}




