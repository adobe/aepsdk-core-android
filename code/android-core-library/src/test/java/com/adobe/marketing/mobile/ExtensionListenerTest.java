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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class ExtensionListenerTest {
	private static CountDownLatch eventHubLatch = new CountDownLatch(1);;
	private static int EVENTHUB_WAIT_MS  = 50;
	private MockEventHubUnitTest mockEventHub;

	static class TestableExtensionListener extends ExtensionListener {
		static int listenerWasRegisteredTimes;
		static int listenerWasUnegisteredTimes;
		static List<Event> heardEvents = new ArrayList<Event>();
		static Extension extensionInstance;
		static ExtensionApi extensionApiInstance;

		public TestableExtensionListener(ExtensionApi extension, String type, String source) {
			super(extension, type, source);
			TestableExtension.registerListenerTimes = 1;
			TestableExtension.handleEventParam = null;
			listenerWasRegisteredTimes += 1;
			heardEvents.clear();
			extensionInstance = this.getParentExtension();
			extensionApiInstance = this.getParentExtension().getApi();

		}

		@Override
		public void hear(final Event e) {
			heardEvents.add(e);
			((TestableExtension) getParentExtension()).handleEvent(e);
		}

		@Override
		public void onUnregistered() {
			listenerWasUnegisteredTimes++;
		}
	}

	static class TestableExtension extends Extension {
		static boolean onUnregisteredWasCalled;
		static int registerListenerTimes = 1;
		static Event handleEventParam;

		TestableExtension(final ExtensionApi api) {
			super(api);

			for (int i = 0; i < registerListenerTimes; i++) {
				getApi().registerEventListener("com.adobe.eventType.configuration",
											   "com.adobe.eventSource.requestContent",
											   TestableExtensionListener.class, null);
			}
		}

		@Override
		public String getName() {
			return "testExtension";
		}

		@Override
		public void onUnregistered() {
			onUnregisteredWasCalled = true;
		}

		private void handleEvent(final Event event) {
			handleEventParam = event;
		}
	}

	@Before
	public void setup() {
		mockEventHub = new MockEventHubUnitTest("hub", new MockPlatformServices());
		TestableExtensionListener.listenerWasRegisteredTimes = 0;
		TestableExtensionListener.listenerWasUnegisteredTimes = 0;
	}

	@Test
	public void testExtensionListener_constructorCalledOnce_when_ListenerRegisteredOnce() throws Exception {
		mockEventHub.registerExtension(TestableExtension.class);
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertEquals(1, TestableExtensionListener.listenerWasRegisteredTimes);
	}

	@Test
	public void testExtensionListener_unregisterIsCalled_when_ListenerRegisteredMultipleTimes() throws Exception {
		TestableExtension.registerListenerTimes = 3;
		mockEventHub.registerExtension(TestableExtension.class);
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertEquals(3, TestableExtensionListener.listenerWasRegisteredTimes);
		assertEquals(2, TestableExtensionListener.listenerWasUnegisteredTimes);
	}

	@Test
	public void testExtensionListener_getExtensionAvailable_when_ListenerRegistered() throws Exception {
		mockEventHub.registerExtension(TestableExtension.class);
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		ExtensionApi extensionApi = (ExtensionApi)mockEventHub.getActiveModules().iterator().next();
		assertEquals(extensionApi.getExtension(), TestableExtensionListener.extensionInstance);
	}

	@Test
	public void testExtensionListener_getExtensionApiAvailable_when_ListenerRegistered() throws Exception {
		mockEventHub.registerExtension(TestableExtension.class);
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		ExtensionApi extensionApi = (ExtensionApi)mockEventHub.getActiveModules().iterator().next();
		assertEquals(extensionApi, TestableExtensionListener.extensionApiInstance);
	}

	@Test
	public void testExtensionListener_hearIsCalled_when_EventHubDispatchIsCalled() throws Exception {
		mockEventHub.registerExtension(TestableExtension.class);
		mockEventHub.finishModulesRegistration(null);
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		Event configEvent = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT).build();
		mockEventHub.dispatchEvent(configEvent);
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertEquals(1, TestableExtensionListener.heardEvents.size());
		assertEquals(configEvent, TestableExtensionListener.heardEvents.get(0));
	}

	@Test
	public void testExtensionListener_privateExtensionHandler_IsAccessible_fromListener() throws Exception {
		mockEventHub.registerExtension(TestableExtension.class);
		mockEventHub.finishModulesRegistration(null);
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		Event configEvent = new Event.Builder("test", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT).build();
		mockEventHub.dispatchEvent(configEvent);
		eventHubLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		assertEquals(configEvent, TestableExtension.handleEventParam);
	}
}
