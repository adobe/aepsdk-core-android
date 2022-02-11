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

import com.adobe.marketing.mobile.EventHubTest.TestModule;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ModuleTest {

	private FakePlatformServices services;

	private static Event testListenerLastHeardEvent = null;
	private static CountDownLatch latch = new CountDownLatch(1);
	private static int EVENTHUB_WAIT_MS = 50;
	private EventHub hub = null;
	private Module testModule = null;

	static class TestListener extends ModuleEventListener<EventHubTest.TestModule> {
		protected TestListener(final TestModule module, final EventType type, final EventSource source) {
			super(module, type, source);
		}

		@Override
		public void hear(final Event e) {
			testListenerLastHeardEvent = e;
			latch.countDown();
		}
	}

	private void waitForLatchWithTimeout(long milli) {
		try {
			latch.await(milli, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			fail("Timed out while waiting for listener");
		}
	}

	@Before
	public void beforeEach() {
		testListenerLastHeardEvent = null;
		latch = new CountDownLatch(1);
		services = new FakePlatformServices();

		hub = new EventHub("eventhub", services);

		try {
			hub.registerModule(TestModule.class);
		} catch (InvalidModuleException e) {
			// this will not happen
		}

		hub.finishModulesRegistration(null);
		waitForLatchWithTimeout(EVENTHUB_WAIT_MS);
		assertNotNull(hub.getActiveModules());
		assertEquals(1, hub.getActiveModules().size());
		testModule = hub.getActiveModules().iterator().next();

		Log.setLoggingService(services.fakeLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);
	}

	@Test
	public void registerListener_NullEventType() {
		testModule.registerListener(null, EventSource.NONE, TestListener.class);

		assertTrue(services.fakeLoggingService.containsErrorLog(TestModule.class.getSimpleName(),
				   "Failed to register listener. EventType, EventSource and listenerClass must be non-null values"));
	}

	@Test
	public void registerListener_NullEventSource() {
		testModule.registerListener(EventType.CUSTOM, null, TestListener.class);

		assertTrue(services.fakeLoggingService.containsErrorLog(TestModule.class.getSimpleName(),
				   "Failed to register listener. EventType, EventSource and listenerClass must be non-null values"));
	}

	@Test
	public void registerListener_NullListenerClass() {
		testModule.registerListener(EventType.CUSTOM, EventSource.NONE, null);

		assertTrue(services.fakeLoggingService.containsErrorLog(TestModule.class.getSimpleName(),
				   "Failed to register listener. EventType, EventSource and listenerClass must be non-null values"));
	}

	@Test
	public void createSharedState_NullSharedStateName() {
		new Module(null, hub) {
		} .createSharedState(0, new EventData());

		assertTrue(services.fakeLoggingService.containsErrorLog(null,
				   "Unable to create shared state (com.adobe.marketing.mobile.InvalidModuleException: " +
				   "StateName was null)"));
	}

	@Test
	public void createXDMSharedState_NullSharedStateName() {
		new Module(null, hub) {
		} .createXDMSharedState(0, new EventData());

		assertTrue(services.fakeLoggingService.containsErrorLog(null,
				   "Unable to create XDM shared state (com.adobe.marketing.mobile.InvalidModuleException: " +
				   "StateName was null)"));
	}

	@Test
	public void createSharedStateAndDispatchEvent_NullSharedStateOrEvent() {
		EventData state = new EventData();
		state.putString("key", "value1");
		final EventData eventData = new EventData()
		.putString("initialkey", "initialvalue");
		final Event event = new Event.Builder("Test Event", EventType.CUSTOM, EventSource.NONE).setData(eventData)
		.build();
		new Module(null, hub) {
		} .createSharedStateAndDispatchEvent(null, event);
		assertTrue(services.fakeLoggingService.containsDebugLog(null,
				   "failed to create the shared state and dispatch the event ( null sharedState or null event)"));
		services.fakeLoggingService.clearLog();
		new Module(null, hub) {
		} .createSharedStateAndDispatchEvent(state, null);
		assertTrue(services.fakeLoggingService.containsDebugLog(null,
				   "failed to create the shared state and dispatch the event ( null sharedState or null event)"));
	}

	@Test
	public void createXDMSharedStateAndDispatchEvent_NullSharedStateOrEvent() {
		EventData state = new EventData();
		state.putString("key", "value1");
		final EventData eventData = new EventData()
		.putString("initialkey", "initialvalue");
		final Event event = new Event.Builder("Test Event", EventType.CUSTOM, EventSource.NONE).setData(eventData)
		.build();
		new Module(null, hub) {
		} .createXDMSharedStateAndDispatchEvent(null, event);
		assertTrue(services.fakeLoggingService.containsDebugLog(null,
				   "failed to create XDM shared state and dispatch the event ( null sharedState or null event)"));
		services.fakeLoggingService.clearLog();
		new Module(null, hub) {
		} .createXDMSharedStateAndDispatchEvent(state, null);
		assertTrue(services.fakeLoggingService.containsDebugLog(null,
				   "failed to create XDM shared state and dispatch the event ( null sharedState or null event)"));
	}

	@Test
	public void updateSharedState_ModuleNotRegistered() {
		new Module(null, hub) {
		} .updateSharedState(0, new EventData());

		assertTrue(services.fakeLoggingService.containsErrorLog(null,
				   "Unable to update shared state (com.adobe.marketing.mobile.InvalidModuleException: " +
				   "StateName was null)"));
	}

	@Test
	public void updateXDMSharedState_ModuleNotRegistered() {
		new Module(null, hub) {
		} .updateXDMSharedState(0, new EventData());

		assertTrue(services.fakeLoggingService.containsErrorLog(null,
				   "Unable to update XDM shared state (com.adobe.marketing.mobile.InvalidModuleException: " +
				   "StateName was null)"));
	}

	@Test
	public void createOrUpdateSharedStateWithVersion_NullSharedStateName() {
		new Module(null, hub) {
		} .createOrUpdateSharedState(0, new EventData());

		assertTrue(services.fakeLoggingService.containsErrorLog(null,
				   "Unable to create or update shared state with version (com.adobe.marketing.mobile.InvalidModuleException: " +
				   "StateName was null)"));
	}

	@Test
	public void createOrUpdateXDMSharedStateWithVersion_NullSharedStateName() {
		new Module(null, hub) {
		} .createOrUpdateXDMSharedState(0, new EventData());

		assertTrue(services.fakeLoggingService.containsErrorLog(null,
				   "Unable to create or update XDM shared state with version (com.adobe.marketing.mobile.InvalidModuleException: " +
				   "StateName was null)"));
	}

	@Test
	public void createOrUpdateSharedState_NullSharedStateName() {
		new Module(null, hub) {
		} .createOrUpdateSharedState(new EventData());

		assertTrue(services.fakeLoggingService.containsErrorLog(null,
				   "Unable to create or update shared state (com.adobe.marketing.mobile.InvalidModuleException: " +
				   "StateName was null)"));
	}

	@Test
	public void createOrUpdateXDMSharedState_NullSharedStateName() {
		new Module(null, hub) {
		} .createOrUpdateXDMSharedState(new EventData());

		assertTrue(services.fakeLoggingService.containsErrorLog(null,
				   "Unable to create or update XDM shared state (com.adobe.marketing.mobile.InvalidModuleException: " +
				   "StateName was null)"));
	}

	@Test
	public void clearSharedStates_ValidState_Works() {
		assertTrue(new Module("validModule", hub) {} .clearSharedStates());
	}

	@Test
	public void clearXDMSharedStates_ValidState_Works() {
		assertTrue(new Module("validModule", hub) {} .clearXDMSharedStates());
	}

	@Test
	public void clearSharedStates_NullStateName_doesNotThrow() {
		assertFalse(new Module(null, hub) {} .clearSharedStates());
		assertTrue(services.fakeLoggingService.containsErrorLog(null,
				   "Unable to clear the shared event states (com.adobe.marketing.mobile.InvalidModuleException: " +
				   "StateName was null)"));
	}

	@Test
	public void clearXDMSharedStates_NullStateName_doesNotThrow() {
		assertFalse(new Module(null, hub) {} .clearXDMSharedStates());
		assertTrue(services.fakeLoggingService.containsErrorLog(null,
				   "Unable to clear the XDM shared event states (com.adobe.marketing.mobile.InvalidModuleException: " +
				   "StateName was null)"));
	}

	@Test
	public void hasSharedStates_NullStateName_doesNotThrow() {
		assertFalse(testModule.hasSharedEventState(null));
		assertTrue(services.fakeLoggingService.containsErrorLog("TestModule",
				   "Unable to query shared event state (java.lang.IllegalArgumentException: " +
				   "StateName was null)"));
	}

	@Test
	public void hasXDMSharedStates_NullStateName_doesNotThrow() {
		assertFalse(testModule.hasXDMSharedEventState(null));
		assertTrue(services.fakeLoggingService.containsErrorLog("TestModule",
				   "Unable to query XDM shared event state (java.lang.IllegalArgumentException: " +
				   "StateName was null)"));
	}

	@Test
	public void unregisterListener_ListenerNotRegistered() {
		testModule.unregisterListener(EventType.CUSTOM, EventSource.BOOTED);
		waitForLatchWithTimeout(EVENTHUB_WAIT_MS);
		assertTrue(services.fakeLoggingService.containsDebugLog("EventHub(eventhub)",
				   "Failed to unregister listener (no registered listener)"));
	}

	@Test
	public void unregisterModule_Happy() {
		testModule.unregisterModule();
		waitForLatchWithTimeout(EVENTHUB_WAIT_MS);
		assertEquals(0, hub.getActiveModules().size());
	}

	@Test
	public void unregisterModule_NotRegistered() {
		Module unregisteredModule = new Module("UnRegistered", hub) {};
		unregisteredModule.unregisterModule();
		waitForLatchWithTimeout(EVENTHUB_WAIT_MS);

		assertTrue(services.fakeLoggingService.containsErrorLog("EventHub(eventhub)",
				   "Failed to unregister module, Module (UnRegistered) is not registered"));
	}

	@Test
	public void registerWildcardListener_Happy() {
		testModule.registerWildcardListener(TestListener.class);
		waitForLatchWithTimeout(EVENTHUB_WAIT_MS);

		Event event = new Event.Builder("testEvent", EventType.ACQUISITION, EventSource.BOOTED).build();
		hub.dispatch(event);
		waitForLatchWithTimeout(EVENTHUB_WAIT_MS);

		assertEquals(EventType.ACQUISITION.getName(), testListenerLastHeardEvent.getEventType().getName());
		assertEquals(EventSource.BOOTED.getName(), testListenerLastHeardEvent.getEventSource().getName());
	}

	@Test
	public void registerWildcardListener_Happy_NullListenerClass() {
		testModule.registerWildcardListener(null);
		waitForLatchWithTimeout(EVENTHUB_WAIT_MS);

		assertTrue(services.fakeLoggingService.containsErrorLog(TestModule.class.getSimpleName(),
				   "Failed to register listener. EventType, EventSource and listenerClass must be non-null values"));
	}

}
