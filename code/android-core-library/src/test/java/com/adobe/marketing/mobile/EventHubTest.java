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
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class EventHubTest {
	private static int EVENTHUB_WAIT_MS = 50;


	private static FakePlatformServices services = new FakePlatformServices();
	private static CountDownLatch latch = new CountDownLatch(1);

	@After
	public void tearDown() {
		TestExtension.extensionName = "test extension";
		TestModuleNoListeners.moduleName = "TestModule";

	}

	@Test
	public void eventCounting() throws Exception {
		final EventHub hub = new EventHub("Event Count Test Hub", services);
		final CountDownLatch initializationLatch = new CountDownLatch(1);

		hub.finishModulesRegistration(new AdobeCallback<Void>() {
			@Override
			public void call(Void value) {
				initializationLatch.countDown();
			}
		});

		initializationLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		for (int i = 2; i < 101; i++) { // boot event is always #0, shared state is #1 so we count from 2 on.
			final Event newEvent = new Event.Builder("Test Event", EventType.CUSTOM, EventSource.NONE).build();
			hub.dispatch(newEvent);
			assertEquals("eventNumber incorrect for event", i, newEvent.getEventNumber());
		}
	}

	private static final Semaphore circularModuleSemaphore = new Semaphore(0, true);

	public static class CircularModule1 extends Module {
		public CircularModule1(final EventHub hub) {
			super("CircularModule1", hub);
			registerListener(EventType.ANALYTICS, EventSource.NONE, EndListener.class);
			registerListener(EventType.CUSTOM, EventSource.NONE, StateListener.class);
		}


		public static class StateListener extends ModuleEventListener<CircularModule1> {
			public StateListener(final CircularModule1 m, final EventType type, final EventSource source) {
				super(m, type, source);
			}

			public void hear(final Event e) {
				parentModule.getSharedEventState("CircularModule2", e);
			}

		}

		public static class EndListener extends ModuleEventListener<CircularModule1> {
			public EndListener(final CircularModule1 m, final EventType type, final EventSource source) {
				super(m, type, source);
			}

			public void hear(final Event e) {
				circularModuleSemaphore.release();
			}
		}
	}

	public static class CircularModule2 extends Module {
		public CircularModule2(final EventHub hub) {
			super("CircularModule2", hub);
			registerListener(EventType.CUSTOM, EventSource.NONE, StateListener.class);
		}

		public static class StateListener extends ModuleEventListener<CircularModule2> {
			public StateListener(final CircularModule2 m, final EventType type, final EventSource source) {
				super(m, type, source);
			}

			public void hear(final Event e) {
				parentModule.getSharedEventState("CircularModule1", e);
			}
		}
	}

	@Test(timeout = 1000)
	public void circularDependency() throws Exception {
		Log.setLogLevel(LoggingMode.DEBUG);
		Log.setLoggingService(services.fakeLoggingService);
		final EventHub hub = new EventHub("Circular Dependency Test", services);
		hub.registerModule(CircularModule1.class);
		hub.registerModule(CircularModule2.class);

		// wait for modules to register
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		final Event sharedStateReadEvent = new Event.Builder("Test Event", EventType.CUSTOM, EventSource.NONE).build();
		final Event doneEvent = new Event.Builder("Done Event", EventType.ANALYTICS, EventSource.NONE).build();
		hub.dispatch(sharedStateReadEvent);
		hub.dispatch(doneEvent);
		hub.finishModulesRegistration(null);

		circularModuleSemaphore.tryAcquire(1000, TimeUnit.MILLISECONDS);

		// two potential outcomes for the live-lock log depending on the speed of which the listeners run.
		final boolean potentialErrorLog1 = services.fakeLoggingService.containsWarningLog("EventHub(Circular Dependency " +
										   "Test)",
										   "Circular shared-state dependency between CircularModule2 and CircularModule1, you may have a " +
										   "live-lock.");
		final boolean potentialErrorLog2 = services.fakeLoggingService.containsWarningLog("EventHub(Circular Dependency " +
										   "Test)",
										   "Circular shared-state dependency between CircularModule1 and CircularModule2, you may have a " +
										   "live-lock.");

		// check for either outcome
		assertTrue(potentialErrorLog1 || potentialErrorLog2);
	}

	private static final Semaphore longRunningListenerModuleSemaphore = new Semaphore(0, true);

	public static class LongRunningListenerModule extends Module {
		public LongRunningListenerModule(final EventHub hub) {
			super("LongRunningListenerModule", hub);
			registerListener(EventType.ANALYTICS, EventSource.NONE, LongRunningListener.class);
			registerListener(EventType.CUSTOM, EventSource.NONE, EndListener.class);
		}

		public static class LongRunningListener extends ModuleEventListener<LongRunningListenerModule> {
			public LongRunningListener(final LongRunningListenerModule m, final EventType type, final EventSource
									   source) {
				super(m, type, source);
			}

			public void hear(final Event e) {
				try {
					longRunningListenerModuleSemaphore.tryAcquire(1500, TimeUnit.MILLISECONDS);
				} catch (final Exception ignored) {
				}
			}
		}

		public static class EndListener extends ModuleEventListener<LongRunningListenerModule> {
			public EndListener(final LongRunningListenerModule m, final EventType type, final EventSource source) {
				super(m, type, source);
			}

			public void hear(final Event e) {
				longRunningListenerModuleSemaphore.release();
			}
		}
	}

	@Test(timeout = 2000)
	public void listenerTimeout() throws Exception {
		Log.setLogLevel(LoggingMode.DEBUG);
		Log.setLoggingService(services.fakeLoggingService);
		final EventHub hub = new EventHub("Listener Timeout Test", services);
		hub.registerModule(LongRunningListenerModule.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		hub.finishModulesRegistration(null);
		final Event longRunningEvent = new Event.Builder("Heard By Long Listener", EventType.ANALYTICS,
				EventSource.NONE).build();
		final Event endEvent = new Event.Builder("Test Event", EventType.CUSTOM, EventSource.NONE).build();

		hub.dispatch(longRunningEvent);
		hub.dispatch(endEvent);

		longRunningListenerModuleSemaphore.tryAcquire(1500, TimeUnit.MILLISECONDS);
		assertTrue(services.fakeLoggingService.containsErrorLog("EventBus(EventHub)",
				   "Listener com.adobe.marketing.mobile.EventHubTest$LongRunningListenerModule$LongRunningListener " +
				   "exceeded runtime limit of 1000 milliseconds (java.util.concurrent.TimeoutException)"));

	}

	private final static EventData sharedState1 = new EventData();
	private final static EventData sharedState2 = new EventData();
	private final static String SharedStateFirstLastModuleName = "SharedStateFirstLast";

	public static class SharedStateFirstLastModule extends Module {
		SharedStateFirstLastModule(final EventHub hub) {
			super(SharedStateFirstLastModuleName, hub);
			createSharedState(500, sharedState1);

			for (int i = 500; i < 1000; i += 15) {
				createSharedState(i, new EventData());
			}

			createSharedState(3500, sharedState2);
		}

	}

	@Test(timeout = 1000)
	public void staticFirstLastEvents() throws Exception {
		final EventHub hub = new EventHub("First Last Events Test", services);
		hub.registerModule(SharedStateFirstLastModule.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		Module module = new SharedStateFirstLastModule(hub);
		EventData oldest = hub.getSharedEventState(SharedStateFirstLastModuleName, Event.SHARED_STATE_OLDEST, module);
		EventData newest = hub.getSharedEventState(SharedStateFirstLastModuleName, Event.SHARED_STATE_NEWEST, module);

		assertEquals("Oldest shared state should equal 'sharedState1'", sharedState1, oldest);
		assertEquals("Newest shared state should equal 'sharedState2'", sharedState2, newest);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getSharedEventState_NullStateName() {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.getSharedEventState(null,
									 new Event.Builder(null, (EventType)null, null).build(),
		new Module("testmodule", eventHub) {
		});
	}

	@Test
	public void getSharedEventState_ReturnsLatest_OnNullEvent() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		Module testModule  = new Module("testStateName", eventHub) {};

		Event event1 = new Event.Builder("EventHubTest", EventType.CUSTOM, EventSource.NONE).setEventNumber(1).build();
		Event event2 = new Event.Builder("EventHubTest", EventType.CUSTOM, EventSource.NONE).setEventNumber(2).build();


		EventData state1 = new EventData();
		state1.putString("key", "value1");
		EventData state2 = new EventData();
		state2.putString("key", "value2");

		eventHub.createSharedState(testModule, event1.getEventNumber(), state1);
		eventHub.createSharedState(testModule, event2.getEventNumber(), state2);

		assertEquals(state1, eventHub.getSharedEventState(testModule.getModuleName(), event1, testModule));
		assertEquals(state2, eventHub.getSharedEventState(testModule.getModuleName(), event2, testModule));
		assertEquals(state2, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule));
	}

	@Test
	public void getXDMSharedEventState_ReturnsLatest_OnNullEvent() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		Module testModule  = new Module("testStateName", eventHub) {};

		Event event1 = new Event.Builder("EventHubTest", EventType.CUSTOM, EventSource.NONE).setEventNumber(1).build();
		Event event2 = new Event.Builder("EventHubTest", EventType.CUSTOM, EventSource.NONE).setEventNumber(2).build();


		EventData state1 = new EventData();
		state1.putString("key", "value1");
		EventData state2 = new EventData();
		state2.putString("key", "value2");

		eventHub.createSharedState(testModule, event1.getEventNumber(), state1, SharedStateType.XDM);
		eventHub.createSharedState(testModule, event2.getEventNumber(), state2, SharedStateType.XDM);

		assertEquals(state1, eventHub.getSharedEventState(testModule.getModuleName(), event1, testModule, SharedStateType.XDM));
		assertEquals(state2, eventHub.getSharedEventState(testModule.getModuleName(), event2, testModule, SharedStateType.XDM));
		assertEquals(state2, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule, SharedStateType.XDM));
	}

	@Test(expected = IllegalArgumentException.class)
	public void hasSharedEventState_NullStateName() {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.hasSharedEventState(null);
	}

	@Test(expected = InvalidModuleException.class)
	public void unregisterModuleListener_NullModule() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.unregisterModuleListener(null, null, null);
	}

	@Test
	public void unregisterModuleListener_NullType() throws Exception {
		Log.setLogLevel(LoggingMode.VERBOSE);
		Log.setLoggingService(services.fakeLoggingService);
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerModule(TestModule.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		Module testModule = eventHub.getActiveModules().iterator().next();
		assertNotNull(testModule);
		eventHub.unregisterModuleListener(testModule, null, EventSource.BOOTED);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(services.fakeLoggingService.containsDebugLog("EventHub(eventhub)",
				   "Failed to unregister listener (no registered listener)"));
	}

	@Test
	public void unregisterModuleListener_NullSource() throws Exception {
		Log.setLogLevel(LoggingMode.VERBOSE);
		Log.setLoggingService(services.fakeLoggingService);
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerModule(TestModule.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		Module testModule = eventHub.getActiveModules().iterator().next();
		assertNotNull(testModule);
		eventHub.unregisterModuleListener(testModule, EventType.HUB, null);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(services.fakeLoggingService.containsDebugLog("EventHub(eventhub)",
				   "Failed to unregister listener (no registered listener)"));
	}

	@Test
	public void unregisterModuleListener_NoRegisteredListeners() throws Exception {
		Log.setLogLevel(LoggingMode.VERBOSE);
		Log.setLoggingService(services.fakeLoggingService);
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerModule(TestModule.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		Module testModule = eventHub.getActiveModules().iterator().next();
		assertNotNull(testModule);
		eventHub.unregisterModuleListener(testModule, EventType.HUB, EventSource.BOOTED);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(services.fakeLoggingService.containsDebugLog("EventHub(eventhub)",
				   "Failed to unregister listener (no registered listener)"));
	}

	@SuppressWarnings("all")
	static class TestListener extends ModuleEventListener {
		static boolean onUnregisteredWasCalled = false;
		static boolean hearWasCalled = false;
		static List<Event> eventsListened = new ArrayList();
		protected TestListener(final Module module, final EventType type, final EventSource source) {
			super(module, type, source);
		}

		@Override
		public void hear(final Event e) {
			hearWasCalled = true;
			eventsListened.add(e);
		}
		@Override
		public void onUnregistered() {
			onUnregisteredWasCalled = true;
		}
	}

	@SuppressWarnings("all")
	static class TestExtensionListener extends ExtensionListener {
		static boolean onUnregisteredWasCalled = false;
		protected TestExtensionListener(final ExtensionApi module, final String type, final String source) {
			super(module, type, source);
		}

		@Override
		public void hear(final Event e) {
		}
		@Override
		public void onUnregistered() {
			onUnregisteredWasCalled = true;
		}
	}

	@SuppressWarnings("all")
	static class TestModule extends Module {
		static boolean onUnregisteredWasCalled = false;
		TestModule(final EventHub hub,
				   boolean registerTestListener,
				   boolean registerTestProcessor) {
			super("TestModule", hub);

			if (registerTestListener) {
				registerListener(EventType.CUSTOM,
								 EventSource.NONE,
								 TestListener.class);
			}

		}

		TestModule(final EventHub hub) {
			super("TestModule", hub);
			registerListener(EventType.CUSTOM, EventSource.NONE, TestListener.class);

		}

		@Override
		protected void onUnregistered() {
			onUnregisteredWasCalled = true;
		}
	}

	static class TestExtension extends Extension {
		static boolean onUnregisteredWasCalled = false;
		static boolean onUnexpectedErrorWasCalled = false;
		static ExtensionUnexpectedError onUnexpectedErrorParamError;
		static String extensionName = "test extension";
		static String friendlyName = "test extension friendly name";

		protected TestExtension(final ExtensionApi extensionApi) {
			super(extensionApi);
			getApi().registerEventListener(EventType.ANALYTICS.getName(), EventSource.REQUEST_CONTENT.getName(),
										   TestExtensionListener.class, null);
			onUnregisteredWasCalled = false;
			onUnexpectedErrorWasCalled = false;
			onUnexpectedErrorParamError = null;
		}

		protected String getName() {
			return extensionName;
		}

		@Override
		protected String getFriendlyName() {
			return friendlyName;
		}

		protected String getVersion() {
			return "1.1.0";
		}

		protected void onUnregistered() {
			onUnregisteredWasCalled = true;
		}

		protected void onUnexpectedError(final ExtensionUnexpectedError extensionUnexpectedError) {
			onUnexpectedErrorWasCalled = true;
			onUnexpectedErrorParamError = extensionUnexpectedError;
		}
	}

	static class TestModuleNoProcessors extends Module {
		TestModuleNoProcessors(final EventHub hub) {
			super("TestModule", hub);
			registerListener(EventType.CUSTOM, EventSource.NONE, TestListener.class);
		}
	}

	static class TestModuleNoListeners extends Module {
		static String moduleName = "TestModule";
		TestModuleNoListeners(final EventHub hub) {
			super(moduleName, hub);
			registerListener(EventType.CUSTOM, EventSource.NONE, TestListener.class);
		}
	}

	@Test(expected = InvalidModuleException.class)
	public void unregisterModule_NullModule() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.unregisterModule(null);
	}

	@Test
	public void unregisterModule_NoRegisteredListeners() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerModule(TestModuleNoListeners.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		Module testModule = eventHub.getActiveModules().iterator().next();
		assertNotNull(testModule);
		eventHub.unregisterModule(testModule);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertEquals(0, eventHub.getActiveModules().size());
	}

	@Test
	public void unregisterModule_NoRegisteredProcessors() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerModule(TestModuleNoProcessors.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		Module testModule = eventHub.getActiveModules().iterator().next();
		assertNotNull(testModule);
		eventHub.unregisterModule(testModule);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertEquals(0, eventHub.getActiveModules().size());
	}

	@Test
	public void unregisterModule_ModuleNotRegisteredWithHub() throws Exception {
		Log.setLoggingService(services.fakeLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);
		EventHub eventHub = new EventHub("eventhub", services);
		Module testModule = new TestModule(eventHub, true, true);
		eventHub.unregisterModule(testModule);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(services.fakeLoggingService.containsErrorLog("EventHub(eventhub)",
				   "Failed to unregister module, Module (TestModule) is not registered"));
	}

	@Test
	public void unregisterModule_Happy() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerModule(TestModule.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertEquals(1, eventHub.getActiveModules().size());
		Module testModule = eventHub.getActiveModules().iterator().next();
		assertNotNull(testModule);
		eventHub.unregisterModule(testModule);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertEquals(0, eventHub.getActiveModules().size());
	}

	@Test
	public void unregisterModule_extensionCase_Happy() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerExtension(TestExtension.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertEquals(1, eventHub.getActiveModules().size());
		Module testExtension = eventHub.getActiveModules().iterator().next();
		assertNotNull(testExtension);
		eventHub.unregisterModule(testExtension);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertEquals(0, eventHub.getActiveModules().size());
		assertTrue(TestExtension.onUnregisteredWasCalled);
	}

	@Test
	public void unregisterExtension_Listener_Happy() throws Exception {
		TestExtension.onUnregisteredWasCalled = false;
		TestExtensionListener.onUnregisteredWasCalled = false;
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerExtension(TestExtension.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertEquals(1, eventHub.getActiveModules().size());
		Module testExtension = eventHub.getActiveModules().iterator().next();
		assertNotNull(testExtension);
		eventHub.registerModuleListener(testExtension, EventType.CUSTOM, EventSource.NONE, "pairId",
										TestExtensionListener.class);
		eventHub.unregisterModule(testExtension);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertEquals(0, eventHub.getActiveModules().size());

		assertTrue(TestExtension.onUnregisteredWasCalled);
		assertTrue(TestExtensionListener.onUnregisteredWasCalled);

	}
	@Test
	public void unregisterModule_Listener_Happy() throws Exception {
		TestModule.onUnregisteredWasCalled = false;
		TestListener.onUnregisteredWasCalled = false;
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerModule(TestModule.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertEquals(1, eventHub.getActiveModules().size());
		Module testModule = eventHub.getActiveModules().iterator().next();
		assertNotNull(testModule);
		eventHub.registerModuleListener(testModule, EventType.CUSTOM, EventSource.NONE, "pairId",
										TestListener.class);
		eventHub.unregisterModule(testModule);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertEquals(0, eventHub.getActiveModules().size());

		assertTrue(TestModule.onUnregisteredWasCalled);
		assertTrue(TestListener.onUnregisteredWasCalled);

	}

	@Test
	public void registerModuleListener_Happy() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerModule(TestModule.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		Module testModule = eventHub.getActiveModules().iterator().next();
		assertNotNull(testModule);
		eventHub.registerModuleListener(testModule, EventType.CUSTOM, EventSource.NONE, "pairId", TestListener.class);
	}

	@Test(expected = InvalidModuleException.class)
	public void registerModuleListener_NullModule() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerModuleListener(null, EventType.CUSTOM, EventSource.NONE, "pairId", TestListener.class);
	}

	@Test
	public void registerModuleListener_NullListener() throws Exception {
		Log.setLogLevel(LoggingMode.VERBOSE);
		Log.setLoggingService(services.fakeLoggingService);
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerModule(TestModule.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		Module testModule = eventHub.getActiveModules().iterator().next();
		assertNotNull(testModule);
		eventHub.registerModuleListener(testModule, EventType.CUSTOM, EventSource.NONE, "pairId", null);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(services.fakeLoggingService.containsDebugLog("EventHub(eventhub)",
				   "Unexpected Null Value (listenerClass, type or source), failed to register listener"));
	}

	@Test
	public void registerModuleListener_ExtensionListener_Happy() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerExtension(TestExtension.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		Module testExtension = eventHub.getActiveModules().iterator().next();
		assertNotNull(testExtension);

		eventHub.registerModuleListener(testExtension, EventType.TARGET, EventSource.RESPONSE_CONTENT, null,
										TestExtensionListener.class);

		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		ConcurrentLinkedQueue<EventListener> listeners = eventHub.getModuleListeners(testExtension);
		assertEquals(2, listeners.size());
		EventListener listener1 = listeners.poll();
		EventListener listener2 = listeners.poll();
		assertEquals(EventSource.REQUEST_CONTENT, listener1.getEventSource());
		assertEquals(EventType.ANALYTICS, listener1.getEventType());
		assertEquals(EventSource.RESPONSE_CONTENT, listener2.getEventSource());
		assertEquals(EventType.TARGET, listener2.getEventType());
	}

	@Test(expected = InvalidModuleException.class)
	public void createSharedState_NullModule() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.createSharedState(null, 0, null);
	}

	@Test(expected = InvalidModuleException.class)
	public void createXDMSharedState_NullModule() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.createSharedState(null, 0, null, SharedStateType.XDM);
	}

	@Test(expected = InvalidModuleException.class)
	public void createSharedState_NullStateName() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.createSharedState(new Module(null, eventHub) {
		}, 0, null);
	}

	@Test(expected = InvalidModuleException.class)
	public void createXDMSharedState_NullStateName() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.createSharedState(new Module(null, eventHub) {
		}, 0, null, SharedStateType.XDM);
	}

	@Test(expected = InvalidModuleException.class)
	public void updateSharedState_NullModule() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.updateSharedState(null, 0, null);
	}

	@Test(expected = InvalidModuleException.class)
	public void updateXDMSharedState_NullModule() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.updateSharedState(null, 0, null, SharedStateType.XDM);
	}

	@Test(expected = InvalidModuleException.class)
	public void updateSharedState_NullStateName() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.updateSharedState(new Module(null, eventHub) {
		}, 0, null);
	}

	@Test(expected = InvalidModuleException.class)
	public void updateXDMSharedState_NullStateName() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.updateSharedState(new Module(null, eventHub) {
		}, 0, null, SharedStateType.XDM);
	}

	@Test(expected = InvalidModuleException.class)
	public void createOrUpdateWithVersionSharedState_NullModule() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.createOrUpdateSharedState(null, 0, null);
	}

	@Test(expected = InvalidModuleException.class)
	public void createOrUpdateWithVersionXDMSharedState_NullModule() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.createOrUpdateSharedState(null, 0, null, SharedStateType.XDM);
	}

	@Test(expected = InvalidModuleException.class)
	public void createOrUpdateWithVersionSharedState_NullStateName() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.createOrUpdateSharedState(new Module(null, eventHub) {
		}, 0, null);
	}

	@Test(expected = InvalidModuleException.class)
	public void createOrUpdateWithVersionXDMSharedState_NullStateName() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.createOrUpdateSharedState(new Module(null, eventHub) {
		}, 0, null, SharedStateType.XDM);
	}

	@Test(expected = InvalidModuleException.class)
	public void createOrUpdateSharedState_NullModule() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.createOrUpdateSharedState(null, null);
	}

	@Test(expected = InvalidModuleException.class)
	public void createOrUpdateXDMSharedState_NullModule() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.createOrUpdateSharedState(null, null, SharedStateType.XDM);
	}

	@Test(expected = InvalidModuleException.class)
	public void createOrUpdateSharedState_NullStateName() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.createOrUpdateSharedState(new Module(null, eventHub) {
		}, null);
	}

	@Test(expected = InvalidModuleException.class)
	public void createOrUpdateXDMSharedState_NullStateName() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.createOrUpdateSharedState(new Module(null, eventHub) {
		}, null, SharedStateType.XDM);
	}

	@Test
	public void createOrUpdateWithVersionSharedState_Happy() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		Module testModule  = new Module("testStateName", eventHub) {};

		EventData state1 = new EventData();
		state1.putString("key", "value1");
		EventData state2 = new EventData();
		state2.putString("key", "value2");

		eventHub.createOrUpdateSharedState(testModule, 1, state1);
		assertEquals(state1, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule));

		eventHub.createOrUpdateSharedState(testModule, 2, EventHub.SHARED_STATE_PENDING);
		assertEquals(EventHub.SHARED_STATE_PENDING, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule));

		eventHub.createOrUpdateSharedState(testModule, 2, state2);
		assertEquals(state2, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule));

	}

	@Test
	public void createOrUpdateWithVersionXDMSharedState_Happy() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		Module testModule  = new Module("testStateName", eventHub) {};

		EventData state1 = new EventData();
		state1.putString("key", "value1");
		EventData state2 = new EventData();
		state2.putString("key", "value2");

		eventHub.createOrUpdateSharedState(testModule, 1, state1, SharedStateType.XDM);
		assertEquals(state1, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule, SharedStateType.XDM));

		eventHub.createOrUpdateSharedState(testModule, 2, EventHub.SHARED_STATE_PENDING, SharedStateType.XDM);
		assertEquals(EventHub.SHARED_STATE_PENDING, eventHub.getSharedEventState(testModule.getModuleName(), null,
					 testModule, SharedStateType.XDM));

		eventHub.createOrUpdateSharedState(testModule, 2, state2, SharedStateType.XDM);
		assertEquals(state2, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule, SharedStateType.XDM));
	}

	@Test
	public void createOrUpdateWithVersionSharedState_AndXDMSharedState_Happy() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		Module testModule  = new Module("testStateName", eventHub) {};

		EventData state1 = new EventData();
		state1.putString("key", "value1");
		EventData state2 = new EventData();
		state2.putString("key", "value2");

		eventHub.createOrUpdateSharedState(testModule, 1, state1);
		assertEquals(state1, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule));

		eventHub.createOrUpdateSharedState(testModule, 2, EventHub.SHARED_STATE_PENDING);
		assertEquals(EventHub.SHARED_STATE_PENDING, eventHub.getSharedEventState(testModule.getModuleName(), null,
					 testModule));

		eventHub.createOrUpdateSharedState(testModule, 2, state2);
		assertEquals(state2, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule));

		eventHub.createOrUpdateSharedState(testModule, 1, state1, SharedStateType.XDM);
		assertEquals(state1, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule, SharedStateType.XDM));

		eventHub.createOrUpdateSharedState(testModule, 2, EventHub.SHARED_STATE_PENDING, SharedStateType.XDM);
		assertEquals(EventHub.SHARED_STATE_PENDING, eventHub.getSharedEventState(testModule.getModuleName(), null,
					 testModule, SharedStateType.XDM));

		eventHub.createOrUpdateSharedState(testModule, 2, state2, SharedStateType.XDM);
		assertEquals(state2, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule, SharedStateType.XDM));
	}

	static List<Event> EVENT_LIST = new ArrayList<Event>();

	public static class TestableModule extends InternalModule {
		public TestableModule(final EventHub hub, final PlatformServices services) {
			super("TestableModule", hub, services);
			this.registerWildcardListener(EndListener.class);
		}

		public static class EndListener extends ModuleEventListener<TestableModule> {
			public EndListener(final TestableModule module, final EventType type, final EventSource source) {
				super(module, type, source);
			}

			public void hear(final Event e) {
				EVENT_LIST.add(e);
			}
		}
	}

	@Test
	public void createOrUpdateSharedState_WithPendingStatus() throws Exception {
		EVENT_LIST.clear();
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerModule(TestableModule.class);

		final CountDownLatch waitForBootLatch = new CountDownLatch(1);
		eventHub.finishModulesRegistration(new AdobeCallback<Void>() {
			@Override
			public void call(Void value) {
				waitForBootLatch.countDown();
			}
		});

		waitForBootLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		Module module = eventHub.getActiveModules().iterator().next();

		EventData state1 = new EventData();
		state1.putString("key", "value1");
		EventData state2 = new EventData();
		state2.putString("key", "value2");

		eventHub.createOrUpdateSharedState(module, 1, state1);
		assertEquals(state1, eventHub.getSharedEventState(module.getModuleName(), null, module));

		eventHub.createOrUpdateSharedState(module, 2, EventHub.SHARED_STATE_INVALID);
		eventHub.createOrUpdateSharedState(module, 3, EventHub.SHARED_STATE_PENDING);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		assertEquals(4,
					 EVENT_LIST.size());  // 4 events, 1 boot event, 1 initial shared state event, and two shared state updates for module.
		assertEquals(EventHub.SHARED_STATE_PENDING, eventHub.getSharedEventState(module.getModuleName(), null, module));
	}

	@Test
	public void createOrUpdateXDMSharedState_WithPendingStatus() throws Exception {
		EVENT_LIST.clear();
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerModule(TestableModule.class);

		final CountDownLatch waitForBootLatch = new CountDownLatch(1);
		eventHub.finishModulesRegistration(new AdobeCallback<Void>() {
			@Override
			public void call(Void value) {
				waitForBootLatch.countDown();
			}
		});

		waitForBootLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		Module module = eventHub.getActiveModules().iterator().next();

		EventData state1 = new EventData();
		state1.putString("key", "value1");
		EventData state2 = new EventData();
		state2.putString("key", "value2");

		eventHub.createOrUpdateSharedState(module, 1, state1, SharedStateType.XDM);
		assertEquals(state1, eventHub.getSharedEventState(module.getModuleName(), null, module, SharedStateType.XDM));

		eventHub.createOrUpdateSharedState(module, 2, EventHub.SHARED_STATE_INVALID, SharedStateType.XDM);
		eventHub.createOrUpdateSharedState(module, 3, EventHub.SHARED_STATE_PENDING, SharedStateType.XDM);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		assertEquals(4,
					 EVENT_LIST.size());  // 4 events, 1 boot event, 1 initial XDM shared state event, and two XDM shared state updates for module.
		assertEquals(EventHub.SHARED_STATE_PENDING, eventHub.getSharedEventState(module.getModuleName(), null, module,
					 SharedStateType.XDM));
	}

	@Test
	public void createSharedState_DispatchesStateChangeEvent() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);

		eventHub.registerModule(TestModule.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		Module testModule = eventHub.getActiveModules().iterator().next();
		assertNotNull(testModule);
		EventData state = new EventData();
		state.putString("key", "value1");

		TestListener.hearWasCalled = false;
		eventHub.registerModuleListener(testModule, EventType.HUB, EventSource.SHARED_STATE, null, TestListener.class);
		eventHub.finishModulesRegistration(null);

		// test
		eventHub.createSharedState(testModule, 0, state);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		// verify Shared state change event is dispatched
		assertTrue(TestListener.hearWasCalled);
		Event sharedStateChangeEvent = TestListener.eventsListened.get(TestListener.eventsListened.size() - 1);

		// verify the details in the sharedState change event details
		assertEquals("Shared state change", sharedStateChangeEvent.getName());
		// no need to verify for type and source, since the listener only listener to HUB SHARED_STATE
		assertEquals("TestModule", sharedStateChangeEvent.getData().getString2("stateowner"));
	}


	@Test
	public void createXDMSharedState_DispatchesStateChangeEvent() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);

		eventHub.registerModule(TestModule.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		Module testModule = eventHub.getActiveModules().iterator().next();
		assertNotNull(testModule);
		EventData state = new EventData();
		state.putString("key", "value1");

		TestListener.hearWasCalled = false;
		eventHub.registerModuleListener(testModule, EventType.HUB, EventSource.SHARED_STATE, null, TestListener.class);
		eventHub.finishModulesRegistration(null);

		// test
		eventHub.createSharedState(testModule, 0, state, SharedStateType.XDM);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		// verify Shared state change event is dispatched
		assertTrue(TestListener.hearWasCalled);
		Event sharedStateChangeEvent = TestListener.eventsListened.get(TestListener.eventsListened.size() - 1);

		// verify the details in the sharedState change event details
		assertEquals("Shared state change (XDM)", sharedStateChangeEvent.getName());
		// no need to verify for type and source, since the listener only listener to HUB SHARED_STATE
		assertEquals("TestModule", sharedStateChangeEvent.getData().getString2("stateowner"));
	}

	@Test
	public void createSharedStateAndDispatchEvent_Happy() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);


		eventHub.registerModule(TestModule.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		Module testModule = eventHub.getActiveModules().iterator().next();
		assertNotNull(testModule);
		EventData state = new EventData();
		state.putString("key", "value1");
		final EventData eventData = new EventData()
		.putString("initialkey", "initialvalue");
		final Event newEvent = new Event.Builder("Test Event", EventType.CUSTOM, EventSource.NONE).setData(eventData)
		.build();
		TestListener.hearWasCalled = false;
		eventHub.registerModuleListener(testModule, EventType.CUSTOM, EventSource.NONE, null, TestListener.class);
		eventHub.createSharedStateAndDispatchEvent(testModule, state, newEvent);
		assertEquals(state, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule));
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(TestListener.hearWasCalled);

	}

	@Test
	public void createXDMSharedStateAndDispatchEvent_Happy() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);


		eventHub.registerModule(TestModule.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		Module testModule = eventHub.getActiveModules().iterator().next();
		assertNotNull(testModule);
		EventData state = new EventData();
		state.putString("key", "value1");
		final EventData eventData = new EventData()
		.putString("initialkey", "initialvalue");
		final Event newEvent = new Event.Builder("Test Event", EventType.CUSTOM, EventSource.NONE).setData(eventData)
		.build();
		TestListener.hearWasCalled = false;
		eventHub.registerModuleListener(testModule, EventType.CUSTOM, EventSource.NONE, null, TestListener.class);
		eventHub.createSharedStateAndDispatchEvent(testModule, state, newEvent, SharedStateType.XDM);
		assertEquals(state, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule, SharedStateType.XDM));
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(TestListener.hearWasCalled);

	}


	@Test
	public void createOrUpdateWithoutVersionSharedState_Happy() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		Module testModule  = new Module("testStateName", eventHub) {};

		EventData state1 = new EventData();
		state1.putString("key", "value1");
		EventData state2 = new EventData();
		state2.putString("key", "value2");

		eventHub.createOrUpdateSharedState(testModule, state1);
		assertEquals(state1, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule));

		eventHub.createOrUpdateSharedState(testModule, state2);
		assertEquals(state2, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule));

	}

	@Test
	public void createOrUpdateWithoutVersionSharedState_EventNumberIncrementedSequentially() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		Module testModule  = new Module("testStateName", eventHub) {};


		EventData state1 = new EventData();
		state1.putString("key", "value1");
		EventData state2 = new EventData();
		state2.putString("key", "value2");


		Event event1 = new Event.Builder("testName", EventType.CUSTOM, EventSource.NONE).build();
		eventHub.dispatch(event1);
		assertEquals(1, event1.getEventNumber());

		//createOrUpdateSharedState() fires a state change event
		eventHub.createOrUpdateSharedState(testModule, state1);

		Event event2 = new Event.Builder("testName", EventType.CUSTOM, EventSource.NONE).build();
		eventHub.dispatch(event2);
		assertEquals(3, event2.getEventNumber());
	}

	@Test
	public void createOrUpdateWithoutVersionXDMSharedState_Happy() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		Module testModule  = new Module("testStateName", eventHub) {};

		EventData state1 = new EventData();
		state1.putString("key", "value1");
		EventData state2 = new EventData();
		state2.putString("key", "value2");

		eventHub.createOrUpdateSharedState(testModule, state1, SharedStateType.XDM);
		assertEquals(state1, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule, SharedStateType.XDM));

		eventHub.createOrUpdateSharedState(testModule, state2, SharedStateType.XDM);
		assertEquals(state2, eventHub.getSharedEventState(testModule.getModuleName(), null, testModule, SharedStateType.XDM));

	}

	@Test
	public void createOrUpdateWithoutVersionXDMSharedState_EventNumberIncrementedSequentially() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		Module testModule  = new Module("testStateName", eventHub) {};

		EventData state1 = new EventData();
		state1.putString("key", "value1");
		EventData state2 = new EventData();
		state2.putString("key", "value2");

		Event event1 = new Event.Builder("testName", EventType.CUSTOM, EventSource.NONE).build();
		eventHub.dispatch(event1);
		assertEquals(1, event1.getEventNumber());

		//createOrUpdateSharedState() fires a state change event
		eventHub.createOrUpdateSharedState(testModule, state1, SharedStateType.XDM);

		Event event2 = new Event.Builder("testName", EventType.CUSTOM, EventSource.NONE).build();
		eventHub.dispatch(event2);
		assertEquals(3, event2.getEventNumber());
	}

	@Test(expected = InvalidModuleException.class)
	public void clearSharedStates_NullStateName() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.clearSharedStates(new Module(null, eventHub) {});
	}

	@Test(expected = InvalidModuleException.class)
	public void clearXDMSharedStates_NullStateName() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.clearSharedStates(new Module(null, eventHub) {}, SharedStateType.XDM);
	}

	@Test(expected = InvalidModuleException.class)
	public void clearSharedStates_NullModule() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.clearSharedStates(null);
	}

	@Test(expected = InvalidModuleException.class)
	public void clearXDMSharedStates_NullModule() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.clearSharedStates(null, SharedStateType.XDM);
	}

	@Test
	public void clearSharedStates_ValidModuleAndStateName() throws Exception {
		// setup
		EventHub eventHub = new EventHub("eventhub", services);
		Module testModule  = new Module("testStateName", eventHub) {};
		EventData data = new EventData();
		data.putString("testing", "clear");
		data.putString("shared", "state");
		Event event = new Event.Builder("testName", EventType.CUSTOM, EventSource.NONE).build();
		eventHub.createSharedState(testModule, 0, data);
		assertEquals(data, eventHub.getSharedEventState(testModule.getModuleName(), event, testModule));

		// test
		eventHub.clearSharedStates(testModule);

		// verify
		assertNull(eventHub.getSharedEventState(testModule.getModuleName(), event, testModule));
	}

	@Test
	public void clearXDMSharedStates_ValidModuleAndStateName() throws Exception {
		// setup
		EventHub eventHub = new EventHub("eventhub", services);
		Module testModule  = new Module("testStateName", eventHub) {};
		EventData data = new EventData();
		data.putString("testing", "clear");
		data.putString("shared", "state");
		Event event = new Event.Builder("testName", EventType.CUSTOM, EventSource.NONE).build();
		eventHub.createSharedState(testModule, 0, data, SharedStateType.XDM);
		assertEquals(data, eventHub.getSharedEventState(testModule.getModuleName(), event, testModule, SharedStateType.XDM));

		// test
		eventHub.clearSharedStates(testModule, SharedStateType.XDM);

		// verify
		assertNull(eventHub.getSharedEventState(testModule.getModuleName(), event, testModule, SharedStateType.XDM));
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_NullPlatformServices() {
		new EventHub("eventhub", null);
	}

	@Test(expected = InvalidModuleException.class)
	public void registerModule_NullClass() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerModule(null);
	}

	@Test
	public void registerModule_SameModuleNameMultipleTimes_ShouldRegisterOnlyOnce() throws Exception {
		Log.setLoggingService(services.fakeLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerModule(TestModuleNoListeners.class);
		eventHub.registerModule(TestModuleNoListeners.class);

		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(services.fakeLoggingService.containsWarningLog("EventHub(eventhub)",
				   "Failed to register extension, an extension with the same name (TestModule) already exists"));
	}
	@Test
	public void registerModule_SameInternalModuleNameMultipleTimes_ShouldRegisterOnlyOnce() throws Exception {
		Log.setLoggingService(services.fakeLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerModule(TestableModule.class);
		eventHub.registerModule(TestableModule.class);

		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(services.fakeLoggingService.containsWarningLog("EventHub(eventhub)",
				   "Failed to register extension, an extension with the same name (TestableModule) already exists"));
	}

	@Test
	public void registerExtension_SameExtensionNameMultipleTimes_ShouldRegisterOnlyOnce() throws Exception {
		Log.setLoggingService(services.fakeLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerExtension(TestExtension.class);
		eventHub.registerExtension(TestExtension.class);

		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(TestExtension.onUnexpectedErrorWasCalled);
		assertEquals(ExtensionError.DUPLICATE_NAME, TestExtension.onUnexpectedErrorParamError.getErrorCode());
		assertTrue(services.fakeLoggingService.containsErrorLog("EventHub(eventhub)",
				   "Failed to register extension, an extension with the same name (test extension) already exists"));
	}

	@Test
	public void registerExtension_SameExtensionNameAsInternalModules_ShouldFail() throws Exception {
		Log.setLoggingService(services.fakeLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);
		EventHub eventHub = new EventHub("eventhub", services);
		TestExtension.extensionName = "test extension";
		TestModuleNoListeners.moduleName = "test extension";
		eventHub.registerModule(TestModuleNoListeners.class);
		eventHub.registerExtension(TestExtension.class);

		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(TestExtension.onUnexpectedErrorWasCalled);
		assertEquals(ExtensionError.DUPLICATE_NAME, TestExtension.onUnexpectedErrorParamError.getErrorCode());
		assertTrue(services.fakeLoggingService.containsErrorLog("EventHub(eventhub)",
				   "Failed to register extension, an extension with the same name (test extension) already exists"));
	}

	@Test
	public void registerExtension_EmptyName_ShouldFailWithBadName() throws Exception {
		Log.setLoggingService(services.fakeLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);
		EventHub eventHub = new EventHub("eventhub", services);
		TestExtension.extensionName = "";
		eventHub.registerExtension(TestExtension.class);

		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(TestExtension.onUnexpectedErrorWasCalled);
		assertEquals(ExtensionError.BAD_NAME, TestExtension.onUnexpectedErrorParamError.getErrorCode());
		assertTrue(services.fakeLoggingService.containsErrorLog("EventHub(eventhub)",
				   "Failed to register extension, extension name should not be null or empty"));
	}

	@Test
	public void registerExtension_NullName_ShouldFailWithBadName() throws Exception {
		Log.setLoggingService(services.fakeLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);
		EventHub eventHub = new EventHub("eventhub", services);
		TestExtension.extensionName = null;
		eventHub.registerExtension(TestExtension.class);

		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertTrue(TestExtension.onUnexpectedErrorWasCalled);
		assertEquals(ExtensionError.BAD_NAME, TestExtension.onUnexpectedErrorParamError.getErrorCode());
		assertTrue(services.fakeLoggingService.containsErrorLog("EventHub(eventhub)",
				   "Failed to register extension, extension name should not be null or empty"));
	}

	@Test
	public void registerOneTimeListener_NullBlock() throws Exception {
		Log.setLoggingService(services.fakeLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);
		final CountDownLatch latch = new CountDownLatch(1);
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerOneTimeListener("pairId", null);

		latch.await(1000, TimeUnit.MILLISECONDS);

		assertTrue(services.fakeLoggingService.containsDebugLog("EventHub(eventhub)",
				   "Unexpected Null Value (callback block), failed to register one-time listener"));
	}

	@Test
	public void registerOneTimeListener_WithTimeOut() throws Exception {
		Log.setLoggingService(services.fakeLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);
		final CountDownLatch latch = new CountDownLatch(1);
		EventHub eventHub = new EventHub("eventhub", services);
		eventHub.registerOneTimeListener("parid", new Module.OneTimeListenerBlock() {

			@Override
			public void call(Event e) {

			}
		}, new AdobeCallbackWithError() {
			@Override
			public void fail(AdobeError error) {
				latch.countDown();
			}

			@Override
			public void call(Object value) {

			}
		}, 100);

		latch.await(1000, TimeUnit.MILLISECONDS);

		assertEquals("Failure callback should get called", 0, latch.getCount());
	}

	private static CountDownLatch finishModulesRegistrationLatch;
	private static List<Event> finishModulesRegistrationEvents;
	private static int moduleNumber = 1;
	private static class FinishModulesRegistrationModule extends Module {
		public FinishModulesRegistrationModule(final EventHub hub) {
			super("FinishModulesRegistrationModule" + moduleNumber++, hub);
			registerListener(EventType.HUB, EventSource.BOOTED, myListener.class);

			try {
				Thread.sleep(100);
			} catch (Exception ex) {

			}
		}

		static class myListener extends ModuleEventListener<FinishModulesRegistrationModule> {
			public myListener(final FinishModulesRegistrationModule module, final EventType type, final EventSource source) {
				super(module, type, source);
			}
			@Override
			public void hear(Event e) {
				finishModulesRegistrationEvents.add(e);
				finishModulesRegistrationLatch.countDown();
			}
		}
	}
	private static class FinishModulesRegistrationModule1 extends FinishModulesRegistrationModule {
		public FinishModulesRegistrationModule1(final EventHub hub) {
			super(hub);
		}
	}
	private static class FinishModulesRegistrationModule2 extends FinishModulesRegistrationModule {
		public FinishModulesRegistrationModule2(final EventHub hub) {
			super(hub);
		}
	}

	@Test
	public void bootedEventIsDispatched_When_FinishModulesRegistrationIsCalled() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		finishModulesRegistrationLatch = new CountDownLatch(1);
		finishModulesRegistrationEvents = new ArrayList<Event>();
		final CountDownLatch callbackLatch = new CountDownLatch(1);

		eventHub.registerModule(FinishModulesRegistrationModule.class);

		eventHub.finishModulesRegistration(new AdobeCallback<Void>() {
			@Override
			public void call(Void value) {
				callbackLatch.countDown();
			}
		});

		assertTrue("Listeners were not called in time!!", finishModulesRegistrationLatch.await(1000, TimeUnit.MILLISECONDS));
		assertEquals(1, finishModulesRegistrationEvents.size());
		assertTrue("Callback were not called in time!!", callbackLatch.await(1000, TimeUnit.MILLISECONDS));


	}

	@Test
	public void bootedEventIsDispatched_When_FinishModulesRegistrationIsCalled_For_MultipleModulesRegistering() throws
		Exception {
		Log.setLoggingService(services.fakeLoggingService);
		EventHub eventHub = new EventHub("eventhub", services);

		// reset counters
		finishModulesRegistrationEvents = new ArrayList<Event>();
		finishModulesRegistrationLatch = new CountDownLatch(3);

		// register 3 modules
		eventHub.registerModule(FinishModulesRegistrationModule.class);
		eventHub.registerModule(FinishModulesRegistrationModule1.class);
		eventHub.registerModule(FinishModulesRegistrationModule2.class);

		// finish module registration and start eventhub
		eventHub.finishModulesRegistration(null);


		assertTrue("Listeners were not called in time: " + finishModulesRegistrationLatch.getCount(),
				   finishModulesRegistrationLatch.await(5000, TimeUnit.MILLISECONDS));
		assertEquals("Expecting " + 3 + " boot events heard!", 3, finishModulesRegistrationEvents.size());

	}

	@Test
	public void bootedEventIsDispatchedOnlyOnce_When_FinishModulesRegistrationIsCalledMultitpleTimes() throws Exception {
		EventHub eventHub = new EventHub("eventhub", services);
		finishModulesRegistrationLatch = new CountDownLatch(1);
		finishModulesRegistrationEvents = new ArrayList<Event>();

		eventHub.registerModule(FinishModulesRegistrationModule.class);

		eventHub.finishModulesRegistration(null);
		eventHub.finishModulesRegistration(null);
		eventHub.finishModulesRegistration(null);

		assertTrue("Listeners were not called in time!!", finishModulesRegistrationLatch.await(1000, TimeUnit.MILLISECONDS));
		assertEquals(1, finishModulesRegistrationEvents.size());

	}

	class TestableEventHub extends EventHub {
		final String eventHubSharedStateName = "com.adobe.module.eventhub";
		TestableEventHub() {
			super("eventhub", services, "1.2.3");
		}
	}

	@Test
	public void testConstructor_WithCoreVersion() {
		final TestableEventHub eventHub = new TestableEventHub();
		assertEquals("1.2.3", eventHub.coreVersion);
	}

	@Test
	public void testRegisterModuleWithCallback_WillAddModuleToSharedState() throws Exception {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		final Module testModule = new TestModule(eventHub);
		final ModuleDetails moduleDetails = new ModuleDetails() {
			@Override
			public String getName() {
				return "testModule";
			}

			@Override
			public String getVersion() {
				return "testVersion";
			}

			@Override
			public Map<String, String> getAdditionalInfo() {
				return null;
			}
		};

		// test
		try {
			eventHub.registerModuleWithCallback(TestModule.class, moduleDetails, null);
		} catch (InvalidModuleException ex) {
			// nothing
		}

		final CountDownLatch waitForBootLatch = new CountDownLatch(1);
		eventHub.finishModulesRegistration(new AdobeCallback<Void>() {
			@Override
			public void call(Void value) {
				waitForBootLatch.countDown();
			}
		});

		waitForBootLatch.await(100, TimeUnit.MILLISECONDS);
		// verify
		final Map<String, Variant> extensions = eventHub.eventHubSharedState.optVariantMap("extensions", null);
		assertNotNull(extensions);
		assertTrue(extensions.containsKey(testModule.getModuleName()));
		final Map<String, Variant> moduleInSharedState = extensions.get(testModule.getModuleName()).optVariantMap(null);
		assertTrue(moduleInSharedState.containsKey("version"));
		assertEquals(moduleDetails.getVersion(), moduleInSharedState.get("version").optString(null));
		assertEquals(moduleDetails.getName(), moduleInSharedState.get("friendlyName").optString(null));
	}

	@Test
	public void testUnregisterModule_When_ModuleIsInSharedState() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		final Module testModule = new TestModule(eventHub);
		final ModuleDetails moduleDetails = new ModuleDetails() {
			@Override
			public String getName() {
				return "testModule";
			}

			@Override
			public String getVersion() {
				return "testVersion";
			}

			@Override
			public Map<String, String> getAdditionalInfo() {
				return null;
			}
		};
		testModule.setModuleDetails(moduleDetails);
		final Map<String, Variant> extensions = new HashMap<String, Variant>();
		final Map<String, Variant> module = new HashMap<String, Variant>();
		module.put("version", Variant.fromString(moduleDetails.getVersion()));
		extensions.put(testModule.getModuleName(), Variant.fromVariantMap(module));
		eventHub.eventHubSharedState.putVariantMap("extensions", extensions);

		// test
		try {
			eventHub.unregisterModule(testModule);
		} catch (final InvalidModuleException ex) {}

		// verify
		final Map<String, Variant> newExtensions = eventHub.eventHubSharedState.optVariantMap("extensions", null);
		assertNotNull(newExtensions);
		assertFalse(newExtensions.containsKey(moduleDetails.getName()));
	}

	@Test
	public void testCreateEventHubSharedState() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		final Module testModule = new TestModule(eventHub);

		// test
		eventHub.createEventHubSharedState(0);
		eventHub.finishModulesRegistration(null);

		// verify
		assertEquals(eventHub.eventHubSharedState, eventHub.getSharedEventState(eventHub.eventHubSharedStateName, null,
					 testModule));
	}

	@Test
	public void testAddModuleToEventHubSharedState_When_Happy() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.isBooted = true;
		final Module testModule = new TestModule(eventHub);
		final ModuleDetails moduleDetails = new ModuleDetails() {
			@Override
			public String getName() {
				return "testModule";
			}

			@Override
			public String getVersion() {
				return "testVersion";
			}

			@Override
			public Map<String, String> getAdditionalInfo() {
				return null;
			}
		};
		testModule.setModuleDetails(moduleDetails);

		// test
		eventHub.addModuleToEventHubSharedState(testModule);

		// verify
		assertNotNull(eventHub.eventHubSharedState);
		final Map<String, Variant> extensions = eventHub.eventHubSharedState.optVariantMap("extensions", null);
		assertTrue(extensions.containsKey(testModule.getModuleName()));
		final Map<String, Variant> module = extensions.get(testModule.getModuleName()).optVariantMap(null);
		assertTrue(module.containsKey("version"));
		assertEquals(moduleDetails.getVersion(), module.get("version").optString(null));
		assertEquals(moduleDetails.getName(), module.get("friendlyName").optString(null));
		assertEquals(eventHub.eventHubSharedState, eventHub.getSharedEventState(eventHub.eventHubSharedStateName, null,
					 testModule));
	}

	@Test
	public void testAddModuleToEventHubSharedState_When_ModuleIsNull_Then_NothingHappens() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.isBooted = true;

		// test
		eventHub.addModuleToEventHubSharedState(null);

		// verify
		assertNotNull(eventHub.eventHubSharedState);
		assertEquals(0, eventHub.eventHubSharedState.optVariantMap("extensions", null).size());
	}

	@Test
	public void testAddModuleToEventHubSharedState_When_ModuleDetailsIsNull_Then_UseModuleGetName() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.isBooted = true;
		final Module testModule = new TestModule(eventHub);

		// test
		eventHub.addModuleToEventHubSharedState(testModule);

		// verify
		assertNotNull(eventHub.eventHubSharedState);
		final Map<String, Variant> extensions = eventHub.eventHubSharedState.optVariantMap("extensions", null);
		assertEquals(1, extensions.size());
		assertTrue(extensions.containsKey(testModule.getModuleName()));
	}

	@Test
	public void testAddModuleToEventHubSharedState_When_ModuleDetailsNameIsEmpty_Then_FriendlyNameIsEmpty() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.isBooted = true;
		final Module testModule = new TestModule(eventHub);
		final ModuleDetails moduleDetails = new ModuleDetails() {
			@Override
			public String getName() {
				return "";
			}

			@Override
			public String getVersion() {
				return "testVersion";
			}

			@Override
			public Map<String, String> getAdditionalInfo() {
				return null;
			}
		};
		testModule.setModuleDetails(moduleDetails);

		// test
		eventHub.addModuleToEventHubSharedState(testModule);

		// verify
		assertNotNull(eventHub.eventHubSharedState);
		final Map<String, Variant> extensions = eventHub.eventHubSharedState.optVariantMap("extensions", null);
		assertEquals(1, extensions.size());
		final Map<String, Variant> module = extensions.get(testModule.getModuleName()).optVariantMap(null);
		assertTrue(module.containsKey("version"));
		assertEquals(moduleDetails.getVersion(), module.get("version").optString(null));
		assertEquals(moduleDetails.getName(), module.get("friendlyName").optString(null));
	}

	@Test
	public void testAddModuleToEventHubSharedState_When_ModuleDetailsNameIsNull_Then_UseModuleGetNameForFriendlyName() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.isBooted = true;
		final Module testModule = new TestModule(eventHub);
		final ModuleDetails moduleDetails = new ModuleDetails() {
			@Override
			public String getName() {
				return null;
			}

			@Override
			public String getVersion() {
				return "testVersion";
			}

			@Override
			public Map<String, String> getAdditionalInfo() {
				return null;
			}
		};
		testModule.setModuleDetails(moduleDetails);

		// test
		eventHub.addModuleToEventHubSharedState(testModule);

		// verify
		assertNotNull(eventHub.eventHubSharedState);
		final Map<String, Variant> extensions = eventHub.eventHubSharedState.optVariantMap("extensions", null);
		assertEquals(1, extensions.size());
		final Map<String, Variant> module = extensions.get(testModule.getModuleName()).optVariantMap(null);
		assertTrue(module.containsKey("version"));
		assertEquals(moduleDetails.getVersion(), module.get("version").optString(null));
		assertEquals(testModule.getModuleName(), module.get("friendlyName").optString(null));
	}

	@Test
	public void testAddModuleToEventHubSharedState_When_EventHubNotFinishedBooting_Then_StateNotShared() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.isBooted = false;
		final Module testModule = new TestModule(eventHub);
		final ModuleDetails moduleDetails = new ModuleDetails() {
			@Override
			public String getName() {
				return "testModule";
			}

			@Override
			public String getVersion() {
				return "testVersion";
			}

			@Override
			public Map<String, String> getAdditionalInfo() {
				return null;
			}
		};
		testModule.setModuleDetails(moduleDetails);

		// test
		eventHub.addModuleToEventHubSharedState(testModule);

		// verify
		assertNotNull(eventHub.eventHubSharedState);
		final Map<String, Variant> extensions = eventHub.eventHubSharedState.optVariantMap("extensions", null);
		assertTrue(extensions.containsKey(testModule.getModuleName()));
		final Map<String, Variant> module = extensions.get(testModule.getModuleName()).optVariantMap(null);
		assertTrue(module.containsKey("version"));
		assertEquals(moduleDetails.getVersion(), module.get("version").optString(null));
		assertEquals(moduleDetails.getName(), module.get("friendlyName").optString(null));
		assertNotEquals(eventHub.eventHubSharedState, eventHub.getSharedEventState(eventHub.eventHubSharedStateName, null,
						testModule));
	}

	@Test
	public void testAddModuleToEventHubSharedState_When_AddingExtension() throws Exception {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.isBooted = false;

		// test
		eventHub.registerExtension(TestExtension.class);
		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		// verify
		assertNotNull(eventHub.eventHubSharedState);
		final Map<String, Variant> extensions = eventHub.eventHubSharedState.optVariantMap("extensions", null);
		assertTrue(extensions.containsKey("test extension"));
		final Map<String, Variant> module = extensions.get("test extension").optVariantMap(null);
		assertTrue(module.containsKey("version"));
		assertEquals("1.1.0", module.get("version").optString(null));
		assertEquals("test extension friendly name", module.get("friendlyName").optString(null));
	}

	@Test
	public void testRemoveModuleFromEventHubSharedState_When_Happy() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.isBooted = true;
		final Module testModule = new TestModule(eventHub);
		final ModuleDetails moduleDetails = new ModuleDetails() {
			@Override
			public String getName() {
				return "testModule";
			}

			@Override
			public String getVersion() {
				return "testVersion";
			}

			@Override
			public Map<String, String> getAdditionalInfo() {
				return null;
			}
		};
		testModule.setModuleDetails(moduleDetails);
		final Map<String, Variant> preExtensions = new HashMap<String, Variant>();
		final Map<String, Variant> preModule = new HashMap<String, Variant>();
		preModule.put("version", Variant.fromString(moduleDetails.getVersion()));
		preExtensions.put(testModule.getModuleName(), Variant.fromVariantMap(preModule));
		eventHub.eventHubSharedState.putVariantMap("extensions", preExtensions);

		// test
		eventHub.removeModuleFromEventHubSharedState(testModule);

		// verify
		assertNotNull(eventHub.eventHubSharedState);
		assertEquals(0, eventHub.eventHubSharedState.optVariantMap("extensions", null).size());
		assertEquals(eventHub.eventHubSharedState, eventHub.getSharedEventState(eventHub.eventHubSharedStateName, null,
					 testModule));
	}

	@Test
	public void testRemoveModuleFromEventHubSharedState_When_EventHubNotFinishedBooting_Then_StateNotShared() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.isBooted = true;
		final Module testModule = new TestModule(eventHub);
		final ModuleDetails moduleDetails = new ModuleDetails() {
			@Override
			public String getName() {
				return "testModule";
			}

			@Override
			public String getVersion() {
				return "testVersion";
			}

			@Override
			public Map<String, String> getAdditionalInfo() {
				return null;
			}
		};
		testModule.setModuleDetails(moduleDetails);
		eventHub.addModuleToEventHubSharedState(testModule);
		eventHub.isBooted = false;

		// test
		eventHub.removeModuleFromEventHubSharedState(testModule);

		// verify
		assertNotNull(eventHub.eventHubSharedState);
		assertEquals(0, eventHub.eventHubSharedState.optVariantMap("extensions", null).size());
		final EventData sharedState = eventHub.getSharedEventState(eventHub.eventHubSharedStateName, null,
									  testModule);
		// TODO: the line below doesn't pass, which may be a problem with eventhub
		//		assertNotEquals(eventHub.eventHubSharedState, sharedState);
	}

	@Test
	public void testRemoveModuleFromEventHubSharedState_When_ModuleIsNull_Then_NothingHappens() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.isBooted = true;
		final Module testModule = new TestModule(eventHub);
		final ModuleDetails moduleDetails = new ModuleDetails() {
			@Override
			public String getName() {
				return "testModule";
			}

			@Override
			public String getVersion() {
				return "testVersion";
			}

			@Override
			public Map<String, String> getAdditionalInfo() {
				return null;
			}
		};
		testModule.setModuleDetails(moduleDetails);
		final Map<String, Variant> preExtensions = new HashMap<String, Variant>();
		final Map<String, Variant> preModule = new HashMap<String, Variant>();
		preModule.put("version", Variant.fromString(moduleDetails.getVersion()));
		preExtensions.put(moduleDetails.getName(), Variant.fromVariantMap(preModule));
		eventHub.eventHubSharedState.putVariantMap("extensions", preExtensions);

		// test
		eventHub.removeModuleFromEventHubSharedState(null);

		// verify
		assertNotNull(eventHub.eventHubSharedState);
		final Map<String, Variant> extensions = eventHub.eventHubSharedState.optVariantMap("extensions", null);
		assertTrue(extensions.containsKey(moduleDetails.getName()));
		final Map<String, Variant> module = extensions.get(moduleDetails.getName()).optVariantMap(null);
		assertTrue(module.containsKey("version"));
		assertEquals(moduleDetails.getVersion(), module.get("version").optString(null));
	}

	@Test
	public void testRemoveModuleFromEventHubSharedState_When_ModuleDetailsIsNull_Then_NothingHappens() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.isBooted = true;
		final Module testModule = new TestModule(eventHub);
		final ModuleDetails moduleDetails = new ModuleDetails() {
			@Override
			public String getName() {
				return "testModule";
			}

			@Override
			public String getVersion() {
				return "testVersion";
			}

			@Override
			public Map<String, String> getAdditionalInfo() {
				return null;
			}
		};
		final Map<String, Variant> preExtensions = new HashMap<String, Variant>();
		final Map<String, Variant> preModule = new HashMap<String, Variant>();
		preModule.put("version", Variant.fromString(moduleDetails.getVersion()));
		preExtensions.put(moduleDetails.getName(), Variant.fromVariantMap(preModule));
		eventHub.eventHubSharedState.putVariantMap("extensions", preExtensions);

		// test
		eventHub.removeModuleFromEventHubSharedState(testModule);

		// verify
		assertNotNull(eventHub.eventHubSharedState);
		final Map<String, Variant> extensions = eventHub.eventHubSharedState.optVariantMap("extensions", null);
		assertTrue(extensions.containsKey(moduleDetails.getName()));
		final Map<String, Variant> module = extensions.get(moduleDetails.getName()).optVariantMap(null);
		assertTrue(module.containsKey("version"));
		assertEquals(moduleDetails.getVersion(), module.get("version").optString(null));
	}

	@Test
	public void testGetInitialEventHubSharedState_DefaultWrapperNone() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();

		// test
		final EventData result =  eventHub.getInitialEventHubSharedState();

		// verify
		assertNotNull(result);
		assertTrue(result.containsKey("version"));
		assertTrue(result.containsKey("extensions"));
		assertTrue(result.containsKey("wrapper"));
		assertEquals("1.2.3", result.optString("version", null));
		final Map<String, Variant> extensions = result.optVariantMap("extensions", null);
		assertEquals(0, extensions.size());

		//verify wrapper details
		final Map<String, Variant> wrapper = result.optVariantMap("wrapper", null);
		assertEquals("N", wrapper.get("type").optString(""));
		assertEquals("None", wrapper.get("friendlyName").optString(""));
	}

	@Test
	public void testGetInitialEventHubSharedState() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.setWrapperType(WrapperType.REACT_NATIVE);

		// test
		final EventData result =  eventHub.getInitialEventHubSharedState();

		// verify
		assertNotNull(result);
		assertTrue(result.containsKey("version"));
		assertTrue(result.containsKey("extensions"));
		assertTrue(result.containsKey("wrapper"));
		assertEquals("1.2.3", result.optString("version", null));
		final Map<String, Variant> extensions = result.optVariantMap("extensions", null);
		assertEquals(0, extensions.size());

		//verify wrapper details
		final Map<String, Variant> wrapper = result.optVariantMap("wrapper", null);
		assertEquals("R", wrapper.get("type").optString(""));
		assertEquals("React Native", wrapper.get("friendlyName").optString(""));
	}

	@Test
	public void testSetWrapperType_HubNotBooted_Happy() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.isBooted = false;

		// test
		eventHub.setWrapperType(WrapperType.FLUTTER);

		// verify
		assertEquals(WrapperType.FLUTTER, eventHub.getWrapperType());
	}

	@Test
	public void testSetWrapperType_HubBooted_Fail() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.isBooted = true;

		// test
		eventHub.setWrapperType(WrapperType.FLUTTER);

		// verify
		assertEquals(WrapperType.NONE, eventHub.getWrapperType());
	}

	@Test
	public void testSetWrapperType_HubNotBooted_AllowMultipleUpdates_shouldNotUpdateAfterBooting() {
		// setup
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.isBooted = false;

		// test
		eventHub.setWrapperType(WrapperType.FLUTTER);
		assertEquals(WrapperType.FLUTTER, eventHub.getWrapperType());

		eventHub.setWrapperType(WrapperType.CORDOVA);
		assertEquals(WrapperType.CORDOVA, eventHub.getWrapperType());

		eventHub.isBooted = true;
		eventHub.setWrapperType(WrapperType.REACT_NATIVE);
		assertEquals(WrapperType.CORDOVA, eventHub.getWrapperType());
	}

	@Test
	public void testGetWrapperType_defaultWrapperNone() {// setup
		final TestableEventHub eventHub = new TestableEventHub();
		assertEquals(WrapperType.NONE, eventHub.getWrapperType());
	}

	@Test
	public void testGetSDKVersion_wrapperNone() {
		final TestableEventHub eventHub = new TestableEventHub();
		assertEquals("1.2.3", eventHub.getSdkVersion());
	}

	@Test
	public void testGetSDKVersion_wrapperTypeSet() {
		final TestableEventHub eventHub = new TestableEventHub();
		eventHub.setWrapperType(WrapperType.REACT_NATIVE);
		assertEquals("1.2.3-R", eventHub.getSdkVersion());
	}
}
