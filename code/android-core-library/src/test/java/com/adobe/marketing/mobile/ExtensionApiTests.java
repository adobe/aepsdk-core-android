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

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

class MockListener extends ExtensionListener {
	static boolean hearWasCalled;
	static boolean listenerInitialized;
	static boolean onUnregisteredWasCalled;

	public MockListener(final ExtensionApi module, final String type, final String source) {
		super(module, type, source);
		listenerInitialized = true;
	}

	@Override
	public void hear(Event e) {
		hearWasCalled = true;
	}

	@Override
	public void onUnregistered() {
		onUnregisteredWasCalled = true;
	}
}

class MockListenerThatThrows extends ExtensionListener {
	public MockListenerThatThrows(final ExtensionApi module, final String type, final String source) throws Exception {
		super(module, type, source);
		throw new ExtensionUnexpectedError(ExtensionError.UNEXPECTED_ERROR);
	}

	@Override
	public void hear(Event e) { }

	@Override
	public void onUnregistered() { }
}

public class ExtensionApiTests {
	private static CountDownLatch eventHubLatch = new CountDownLatch(1);
	private static int                                    EVENTHUB_WAIT_MS  = 50;
	private static String                                 TEST_SHARED_STATE = "com.adobe.module.configuration";
	private        ExtensionApi                           extensionApi;
	private        MockEventHubUnitTest                   mockEventHub;
	private        ExtensionErrorCallback<ExtensionError> errorCallback;
	private final  ExtensionError[]                       returnedError     = new ExtensionError[1];

	static class MockExtension extends Extension {
		static boolean onUnregisteredWasCalled;
		static boolean onUnexpectedErrorWasCalled;

		MockExtension(final ExtensionApi api) {
			super(api);
		}

		@Override
		public String getName() {
			return "testExtension";
		}

		@Override
		public String getVersion() {
			return "1.0";
		}

		@Override
		public void onUnregistered() {
			onUnregisteredWasCalled = true;
		}

		@Override
		public void onUnexpectedError(final ExtensionUnexpectedError extensionUnexpectedError) {
			onUnexpectedErrorWasCalled = true;
		}
	}

	static class MockExtension2 extends Extension {
		MockExtension2(final ExtensionApi api) {
			super(api);
		}

		@Override
		public String getName() {
			return "testExtension2";
		}

		@Override
		public String getVersion() {
			return "2.0";
		}

		@Override
		public void onUnregistered() {}
	}

	@Before
	public void setup() {

		// initialize static flags
		MockListener.hearWasCalled = false;
		MockListener.listenerInitialized = false;
		MockListener.onUnregisteredWasCalled = false;
		MockExtension.onUnregisteredWasCalled = false;

		try {
			// register the MockExtension
			mockEventHub = new MockEventHubUnitTest("hub", new MockPlatformServices());
			mockEventHub.registerExtension(MockExtension.class);
			eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
			extensionApi = (ExtensionApi)mockEventHub.getActiveModules().iterator().next();
			assertNotNull(extensionApi);
			MockExtension mockExtension = (MockExtension)extensionApi.getExtension();
			assertNotNull(mockExtension);
			returnedError[0] = null;
			errorCallback = new ExtensionErrorCallback<ExtensionError>() {
				@Override
				public void error(final ExtensionError extensionError) {
					returnedError[0] = extensionError;
				}
			};

		} catch (Exception e) {
			fail("Failure initializing test");
		}
	}

	@Test
	public void testSetExtension_works() {
		ExtensionApi extensionApi = new ExtensionApi(mockEventHub);
		extensionApi.setExtension(new MockExtension(extensionApi));
		assertEquals("testExtension", extensionApi.getModuleName());
	}

	@Test
	public void testSetExtension_doesNot_updateExtensionIfInitialized() {
		extensionApi.setExtension(new MockExtension2(extensionApi));
		assertEquals("testExtension", extensionApi.getModuleName());
		assertEquals("1.0", extensionApi.getModuleVersion());
	}

	@Test
	public void testOnUnregistered_works() throws Exception {
		extensionApi.onUnregistered();
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(MockExtension.onUnregisteredWasCalled);
	}

	@Test
	public void testOnUnregistered_doesNot_crash_when_extensionNotInitialized() {
		// test and verify does not throw
		try {
			ExtensionApi extensionApi = new ExtensionApi(mockEventHub);
			extensionApi.onUnregistered();
		} catch (Exception e) {
			fail("On unregistered should not have thrown any exception when extension not initialized");
		}

		assertFalse(MockExtension.onUnregisteredWasCalled);
	}

	@Test
	public void testRegisterEventListener_works() throws Exception {
		assertTrue(extensionApi.registerEventListener("new.event.type", "new.event.source", MockListener.class, null));
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(MockListener.listenerInitialized);
		ConcurrentLinkedQueue<EventListener> listeners = mockEventHub.getModuleListeners(extensionApi);
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		Log.error("testRegisterEventListener_works", "listeners.size()=%d", listeners.size());
		assertEquals(1, listeners.size());
		EventListener registeredListener = listeners.peek();
		assertNotNull(registeredListener);
		assertTrue(registeredListener instanceof MockListener);
		assertEquals("new.event.type", registeredListener.getEventType().getName());
		assertEquals("new.event.source", registeredListener.getEventSource().getName());
	}

	@Test
	public void testRegisterEventListener_multipleListeners_works() throws Exception {
		assertTrue(extensionApi.registerEventListener("new.event.type1", "new.event.source1", MockListener.class, null));
		assertTrue(extensionApi.registerEventListener("new.event.type2", "new.event.source2", MockListener.class, null));
		assertTrue(extensionApi.registerEventListener("new.event.type3", "new.event.source3", MockListener.class, null));
		assertTrue(extensionApi.registerEventListener("new.event.type4", "new.event.source4", MockListener.class, null));
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(MockListener.listenerInitialized);
		ConcurrentLinkedQueue<EventListener> listeners = mockEventHub.getModuleListeners(extensionApi);
		assertEquals(4, listeners.size());
	}

	@Test
	public void testRegisterEventListener_nullListener_doesNot_throw_withErrorCallback() {
		// test and verify does not throw and returns error
		try {
			assertFalse(extensionApi.registerEventListener("new.event.type", "new.event.source", null, errorCallback));
		} catch (Exception e) {
			fail("On registerEventListener should not have thrown any exception when listener null");
		}

		assertEquals(ExtensionError.UNEXPECTED_ERROR, returnedError[0]);
	}

	@Test
	public void testRegisterEventListener_nullListener_doesNot_throw() {
		// test and verify does not throw
		try {
			assertFalse(extensionApi.registerEventListener("new.event.type", "new.event.source", null, null));
		} catch (Exception e) {
			fail("On registerEventListener should not have thrown any exception when listener null");
		}
	}

	@Test
	public void testRegisterEventListener_nullEventType_doesNot_throw_withErrorCallback() {
		// test and verify does not throw and returns error
		try {
			assertFalse(extensionApi.registerEventListener(null, "new.event.source", MockListener.class, errorCallback));
		} catch (Exception e) {
			fail("On registerEventListener should not have thrown any exception when event type null");
		}

		assertEquals(ExtensionError.EVENT_TYPE_NOT_SUPPORTED, returnedError[0]);
	}

	@Test
	public void testRegisterEventListener_nullEventType_doesNot_throw() {
		// test and verify does not throw
		try {
			assertFalse(extensionApi.registerEventListener(null, "new.event.source", MockListener.class, null));
		} catch (Exception e) {
			fail("On registerEventListener should not have thrown any exception when event type null");
		}
	}

	@Test
	public void testRegisterEventListener_nullEventSource_doesNot_throw_withErrorCallback() {
		// test and verify does not throw and returns error
		try {
			assertFalse(extensionApi.registerEventListener("new.event.type", null, MockListener.class, errorCallback));
		} catch (Exception e) {
			fail("On registerEventListener should not have thrown any exception when event source null");
		}

		assertEquals(ExtensionError.EVENT_SOURCE_NOT_SUPPORTED, returnedError[0]);
	}

	@Test
	public void testRegisterEventListener_nullEventSource_doesNot_throw() {
		// test and verify does not throw
		try {
			assertFalse(extensionApi.registerEventListener("new.event.type", null, MockListener.class, null));
		} catch (Exception e) {
			fail("On registerEventListener should not have thrown any exception when event source null");
		}
	}

	@Test
	public void testRegisterEventListener_constructorThrow_callsOnUnexpectedError() {
		// test and verify does not throw
		try {
			assertTrue(extensionApi.registerEventListener("new.event.type", "new.event.source", MockListenerThatThrows.class,
					   null));
			eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			fail("On registerEventListener should not have thrown any exception for this class");
		}

		assertTrue(MockExtension.onUnexpectedErrorWasCalled);
	}

	@Test
	public void testUnregisterExtension_unregisteredCalled() throws Exception {
		extensionApi.unregisterExtension();
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(MockExtension.onUnregisteredWasCalled);
	}

	@Test
	public void testRegisterWildcardListener_works() throws Exception {
		assertTrue(extensionApi.registerWildcardListener(MockListener.class, null));
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(MockListener.listenerInitialized);
		ConcurrentLinkedQueue<EventListener> listeners = mockEventHub.getModuleListeners(extensionApi);
		Log.error("testRegisterEventListener_works", "listeners.size()=%d", listeners.size());
		assertEquals(1, listeners.size());
		EventListener registeredListener = listeners.peek();
		assertNotNull(registeredListener);
		assertTrue(registeredListener instanceof MockListener);
		assertEquals(EventType.WILDCARD, registeredListener.getEventType());
		assertEquals(EventSource.WILDCARD, registeredListener.getEventSource());
	}

	@SuppressWarnings("all")
	@Test
	public void testRegisterWildcardListener_whenCalledMultipleTimes_registersOneListener_unregisterExistingOnes() throws
		Exception {
		assertTrue(extensionApi.registerWildcardListener(MockListener.class, null));
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertFalse(MockListener.onUnregisteredWasCalled);
		assertTrue(MockListener.listenerInitialized);
		MockListener.listenerInitialized = false;
		assertTrue(extensionApi.registerWildcardListener(MockListener.class, null));
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(MockListener.onUnregisteredWasCalled);
		assertTrue(MockListener.listenerInitialized);
		MockListener.onUnregisteredWasCalled = false;
		MockListener.listenerInitialized = false;
		assertTrue(extensionApi.registerWildcardListener(MockListener.class, null));
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(MockListener.onUnregisteredWasCalled);
		assertTrue(MockListener.listenerInitialized);
		MockListener.onUnregisteredWasCalled = false;
		MockListener.listenerInitialized = false;
		ConcurrentLinkedQueue<EventListener> listeners = mockEventHub.getModuleListeners(extensionApi);
		assertEquals(1, listeners.size());
	}

	@Test
	public void testRegisterWildcardListener_nullListener_doesNot_throw_withErrorCallback() {
		// test and verify does not throw and returns error
		try {
			assertFalse(extensionApi.registerWildcardListener(null, errorCallback));
		} catch (Exception e) {
			fail("On registerWildcardListener should not have thrown any exception when listener null");
		}

		assertEquals(ExtensionError.UNEXPECTED_ERROR, returnedError[0]);
	}

	@Test
	public void testRegisterWildcardListener_nullListener_doesNot_throw() {
		// test and verify does not throw
		try {
			assertFalse(extensionApi.registerWildcardListener(null, null));
		} catch (Exception e) {
			fail("On registerWildcardListener should not have thrown any exception when listener null");
		}
	}

	@Test
	public void testUnregisterExtension_when_notRegistered_works() {
		try {
			ExtensionApi extensionApi = new ExtensionApi(mockEventHub);
			extensionApi.unregisterExtension();
		} catch (Exception e) {
			fail("On unregisterExtension should not have thrown any exception when not registered before");
		}
	}

	@Test
	public void testUnregisterExtension_eventListenerUnregister_Works() throws Exception {
		// setup
		extensionApi.registerEventListener("new.event.type", "new.event.source", MockListener.class, null);
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		// test
		extensionApi.unregisterExtension();
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		// verify
		assertTrue(MockExtension.onUnregisteredWasCalled);
		assertTrue(MockListener.onUnregisteredWasCalled);
		ConcurrentLinkedQueue<EventListener> listeners = mockEventHub.getModuleListeners(extensionApi);
		assertNull(listeners);
	}

	@Test
	public void testGetSharedEventState_works() throws Exception {
		// setup
		Map<String, Object> eventData = getMixedData();
		mockEventHub.createSharedState(TEST_SHARED_STATE, 1, new EventData().putString("first", "time"));
		mockEventHub.createSharedState(TEST_SHARED_STATE, 2,
									   EventData.fromObjectMap(eventData));
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(1).build();
		Event nextEvent = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(2).build();

		// test&verify
		Map<String, Object> resultSharedState = extensionApi.getSharedEventState(TEST_SHARED_STATE, event,
												errorCallback);
		assertEquals(new HashMap<String, Object>() {
			{
				put("first", "time");
			}
		}, resultSharedState);
		assertNull(returnedError[0]);

		resultSharedState = extensionApi.getSharedEventState(TEST_SHARED_STATE, nextEvent,
							errorCallback);
		assertEquals(eventData, resultSharedState);
		assertNull(returnedError[0]);
	}

	@Test
	public void testGetXDMSharedEventState_works() throws Exception {
		// setup
		Map<String, Object> eventData = getMixedData();
		mockEventHub.createSharedState(TEST_SHARED_STATE, 1, new EventData().putString("first", "time"), SharedStateType.XDM);
		mockEventHub.createSharedState(TEST_SHARED_STATE, 2,
									   EventData.fromObjectMap(eventData), SharedStateType.XDM);
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(1).build();
		Event nextEvent = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(2).build();

		// test&verify
		Map<String, Object> resultSharedState = extensionApi.getXDMSharedEventState(TEST_SHARED_STATE, event,
												errorCallback);
		assertEquals(new HashMap<String, Object>() {
			{
				put("first", "time");
			}
		}, resultSharedState);
		assertNull(returnedError[0]);

		resultSharedState = extensionApi.getXDMSharedEventState(TEST_SHARED_STATE, nextEvent,
							errorCallback);
		assertEquals(eventData, resultSharedState);
		assertNull(returnedError[0]);
	}

	@Test
	public void testGetSharedEventState_returnsError_whenErrorOccurs() {
		// setup
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();
		mockEventHub.throwException = true;

		// test&verify
		Map<String, Object> resultSharedState = extensionApi.getSharedEventState(TEST_SHARED_STATE, event,
												errorCallback);
		assertNull(resultSharedState);
		assertEquals(ExtensionError.UNEXPECTED_ERROR, returnedError[0]);
	}

	@Test
	public void testGetXDMSharedEventState_returnsError_whenErrorOccurs() {
		// setup
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();
		mockEventHub.throwException = true;

		// test&verify
		Map<String, Object> resultSharedState = extensionApi.getXDMSharedEventState(TEST_SHARED_STATE, event,
												errorCallback);
		assertNull(resultSharedState);
		assertEquals(ExtensionError.UNEXPECTED_ERROR, returnedError[0]);
	}

	@Test
	public void testGetSharedEventState_returnsLatestSharedState_whenEventNull() throws Exception {
		// setup
		Map<String, Object> eventData = getMixedData();
		mockEventHub.createSharedState(TEST_SHARED_STATE, 1, EventData.fromObjectMap(eventData));
		mockEventHub.createSharedState(TEST_SHARED_STATE, 2, new EventData().putString("second", "time"));

		// test&verify
		Map<String, Object> resultSharedState = extensionApi.getSharedEventState(TEST_SHARED_STATE, null,
												errorCallback);
		assertEquals(1, resultSharedState.size());
		assertEquals("time", resultSharedState.get("second"));
		assertNull(returnedError[0]);
	}

	@Test
	public void testXDMGetSharedEventState_returnsLatestSharedState_whenEventNull() throws Exception {
		// setup
		Map<String, Object> eventData = getMixedData();
		mockEventHub.createSharedState(TEST_SHARED_STATE, 1, EventData.fromObjectMap(eventData), SharedStateType.XDM);
		mockEventHub.createSharedState(TEST_SHARED_STATE, 2, new EventData().putString("second", "time"), SharedStateType.XDM);

		// test&verify
		Map<String, Object> resultSharedState = extensionApi.getXDMSharedEventState(TEST_SHARED_STATE, null,
												errorCallback);
		assertEquals(1, resultSharedState.size());
		assertEquals("time", resultSharedState.get("second"));
		assertNull(returnedError[0]);
	}

	@Test
	public void testGetSharedEventState_returnsNull_whenStateNameNull() {
		// setup
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();

		// test&verify
		Map<String, Object> resultSharedState = extensionApi.getSharedEventState(null, event,
												errorCallback);
		assertNull(resultSharedState);
		assertEquals(ExtensionError.UNEXPECTED_ERROR, returnedError[0]);
	}

	@Test
	public void testGetXDMSharedEventState_returnsNull_whenStateNameNull() {
		// setup
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();

		// test&verify
		Map<String, Object> resultSharedState = extensionApi.getXDMSharedEventState(null, event,
												errorCallback);
		assertNull(resultSharedState);
		assertEquals(ExtensionError.UNEXPECTED_ERROR, returnedError[0]);
	}

	@Test
	public void testGetSharedEventState_returnsNull_whenStatePending() {
		mockEventHub.createSharedState(TEST_SHARED_STATE, 1, EventHub.SHARED_STATE_PENDING);

		// setup
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(1)
		.build();

		// test&verify
		Map<String, Object> resultSharedState = extensionApi.getSharedEventState(TEST_SHARED_STATE, event,
												errorCallback);
		assertNull(resultSharedState);
		assertNull(returnedError[0]);
	}

	@Test
	public void testGetXDMSharedEventState_returnsNull_whenStatePending() {
		mockEventHub.createSharedState(TEST_SHARED_STATE, 1, EventHub.SHARED_STATE_PENDING, SharedStateType.XDM);

		// setup
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(1)
		.build();

		// test&verify
		Map<String, Object> resultSharedState = extensionApi.getXDMSharedEventState(TEST_SHARED_STATE, event,
												errorCallback);
		assertNull(resultSharedState);
		assertNull(returnedError[0]);
	}

	@Test
	public void testSetSharedEventState_works() {
		// setup
		Map<String, Object> eventData = getMixedData();
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();

		// test
		assertTrue(extensionApi.setSharedEventState(eventData, event, errorCallback));

		// verify
		Event nextEvent = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(101).build();
		EventData resultSharedState = mockEventHub.getSharedEventState(extensionApi.getModuleName(), nextEvent, extensionApi);
		assertEquals(eventData, resultSharedState.toObjectMap());
		assertNull(returnedError[0]);
	}

	@Test
	public void testSetXDMSharedEventState_works() {
		// setup
		Map<String, Object> eventData = getMixedData();
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();

		// test
		assertTrue(extensionApi.setXDMSharedEventState(eventData, event, errorCallback));

		// verify
		Event nextEvent = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(101).build();
		EventData resultSharedState = mockEventHub.getSharedEventState(extensionApi.getModuleName(), nextEvent,
									  extensionApi, SharedStateType.XDM);
		assertEquals(eventData, resultSharedState.toObjectMap());
		assertNull(returnedError[0]);
	}

	@Test
	public void testSetSharedEventState_WithNullEvent_works() {
		// setup
		Map<String, Object> eventData = getMixedData();

		// test
		assertTrue(extensionApi.setSharedEventState(eventData, null, errorCallback));

		// verify
		EventData resultSharedState = mockEventHub.getSharedEventState(extensionApi.getModuleName(), null, extensionApi);
		assertEquals(eventData, resultSharedState.toObjectMap());
		assertNull(returnedError[0]);
	}

	@Test
	public void testSetXDMSharedEventState_WithNullEvent_works() {
		// setup
		Map<String, Object> eventData = getMixedData();

		// test
		assertTrue(extensionApi.setXDMSharedEventState(eventData, null, errorCallback));

		// verify
		EventData resultSharedState = mockEventHub.getSharedEventState(extensionApi.getModuleName(), null, extensionApi,
									  SharedStateType.XDM);
		assertEquals(eventData, resultSharedState.toObjectMap());
		assertNull(returnedError[0]);
	}

	@Test
	public void testSetSharedEventState_eventDataIsImmutable() {
		// setup
		Map<String, Object> eventData = getMixedData();
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();

		// test
		assertTrue(extensionApi.setSharedEventState(eventData, event, errorCallback));
		eventData.clear();

		// verify
		EventData resultSharedState = mockEventHub.getSharedEventState(extensionApi.getModuleName(), null, extensionApi);
		assertEquals(3, resultSharedState.toObjectMap().size());
		assertNull(returnedError[0]);
	}

	@Test
	public void testSetXDMSharedEventState_eventDataIsImmutable() {
		// setup
		Map<String, Object> eventData = getMixedData();
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();

		// test
		assertTrue(extensionApi.setXDMSharedEventState(eventData, event, errorCallback));
		eventData.clear();

		// verify
		EventData resultSharedState = mockEventHub.getSharedEventState(extensionApi.getModuleName(), null, extensionApi,
									  SharedStateType.XDM);
		assertEquals(3, resultSharedState.toObjectMap().size());
		assertNull(returnedError[0]);
	}

	@Test
	public void testSetSharedEventState_returnsFalse_whenErrorOccurs() {
		// setup
		Map<String, Object> eventData = getMixedData();
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();
		mockEventHub.throwException = true;

		// test
		assertFalse(extensionApi.setSharedEventState(eventData, event, errorCallback));
		assertEquals(ExtensionError.UNEXPECTED_ERROR, returnedError[0]);
	}

	@Test
	public void testSetXDMSharedEventState_returnsFalse_whenErrorOccurs() {
		// setup
		Map<String, Object> eventData = getMixedData();
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();
		mockEventHub.throwException = true;

		// test
		assertFalse(extensionApi.setXDMSharedEventState(eventData, event, errorCallback));
		assertEquals(ExtensionError.UNEXPECTED_ERROR, returnedError[0]);
	}

	@Test
	public void testClearSharedEventStates_works() {
		// setup
		Map<String, Object> eventData = getMixedData();
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();
		Event nextEvent = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(101).build();

		extensionApi.setSharedEventState(eventData, event, null);
		extensionApi.setSharedEventState(eventData, nextEvent, null);

		// test&verify
		assertTrue(extensionApi.clearSharedEventStates(errorCallback));
		assertNull(returnedError[0]);
		assertNull(mockEventHub.getSharedEventState(extensionApi.getModuleName(), Event.SHARED_STATE_NEWEST, extensionApi));
	}

	@Test
	public void testClearXDMSharedEventStates_works() {
		// setup
		Map<String, Object> eventData = getMixedData();
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();
		Event nextEvent = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(101).build();

		extensionApi.setXDMSharedEventState(eventData, event, null);
		extensionApi.setXDMSharedEventState(eventData, nextEvent, null);

		// test&verify
		assertTrue(extensionApi.clearXDMSharedEventStates(errorCallback));
		assertNull(returnedError[0]);
		assertNull(mockEventHub.getSharedEventState(extensionApi.getModuleName(), Event.SHARED_STATE_NEWEST, extensionApi,
				   SharedStateType.XDM));
	}

	@Test
	public void testClearSharedEventStates_returnsFalse_whenErrorOccurs() {
		// setup
		Map<String, Object> eventData = getMixedData();
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();
		Event nextEvent = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(101).build();

		extensionApi.setSharedEventState(eventData, event, null);
		extensionApi.setSharedEventState(eventData, nextEvent, null);
		mockEventHub.throwException = true;

		// test&verify
		assertFalse(extensionApi.clearSharedEventStates(errorCallback));
		assertEquals(ExtensionError.UNEXPECTED_ERROR, returnedError[0]);
	}

	@Test
	public void testClearXDMSharedEventStates_returnsFalse_whenErrorOccurs() {
		// setup
		Map<String, Object> eventData = getMixedData();
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();
		Event nextEvent = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(101).build();

		extensionApi.setXDMSharedEventState(eventData, event, null);
		extensionApi.setXDMSharedEventState(eventData, nextEvent, null);
		mockEventHub.throwException = true;

		// test&verify
		assertFalse(extensionApi.clearXDMSharedEventStates(errorCallback));
		assertEquals(ExtensionError.UNEXPECTED_ERROR, returnedError[0]);
	}

	@Test
	public void testClearSharedEventStates_returnsFalse_whenErrorOccurs_noCallback() {
		// setup
		Map<String, Object> eventData = getMixedData();
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();
		Event nextEvent = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(101).build();

		extensionApi.setSharedEventState(eventData, event, null);
		extensionApi.setSharedEventState(eventData, nextEvent, null);
		mockEventHub.throwException = true;

		// test&verify
		boolean clearStatus = false;

		try {
			clearStatus = extensionApi.clearSharedEventStates(null);
		} catch (Exception e) {
			fail("clearSharedEventStates should not throw, but it did.");
		}

		assertFalse(clearStatus);
	}

	@Test
	public void testClearXDMSharedEventStates_returnsFalse_whenErrorOccurs_noCallback() {
		// setup
		Map<String, Object> eventData = getMixedData();
		Event event = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(100).build();
		Event nextEvent = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setEventNumber(101).build();

		extensionApi.setXDMSharedEventState(eventData, event, null);
		extensionApi.setXDMSharedEventState(eventData, nextEvent, null);
		mockEventHub.throwException = true;

		// test&verify
		boolean clearStatus = false;

		try {
			clearStatus = extensionApi.clearXDMSharedEventStates(null);
		} catch (Exception e) {
			fail("clearSharedEventStates should not throw, but it did.");
		}

		assertFalse(clearStatus);
	}

	@Test
	public void testClearSharedEventStates_works_whenNoSharedStateSet() {
		// test
		assertTrue(extensionApi.clearSharedEventStates(errorCallback));
		assertNull(returnedError[0]);
		assertNull(mockEventHub.getSharedEventState(extensionApi.getModuleName(), Event.SHARED_STATE_NEWEST, extensionApi));
	}

	@Test
	public void testClearXDMSharedEventStates_works_whenNoSharedStateSet() {
		// test
		assertTrue(extensionApi.clearXDMSharedEventStates(errorCallback));
		assertNull(returnedError[0]);
		assertNull(mockEventHub.getSharedEventState(extensionApi.getModuleName(), Event.SHARED_STATE_NEWEST, extensionApi,
				   SharedStateType.XDM));
	}

	private Map<String, Object> getMixedData() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("key1", "value1");
		data.put("key2", 1000000);
		data.put("key3", getMultilevelMap(100));
		return data;
	}

	private Map<String, Object> getMultilevelMap(final int levels) {
		return getMultilevelMap(1, levels, new HashMap<String, Object>());
	}

	private Map<String, Object> getMultilevelMap(final int level, final int maxDepth,
			final Map<String, Object> aboveLevelMap) {
		if (level == maxDepth) {
			aboveLevelMap.put(String.format("level %d", level), "test");
			return aboveLevelMap;
		}

		Map<String, Object> aboveLevels = getMultilevelMap(level + 1, maxDepth, new HashMap<String, Object>());
		Map<String, Object> multilevelMap = new HashMap<String, Object>();
		multilevelMap.put(String.format("level %d", level), aboveLevels);
		return multilevelMap;
	}
}
