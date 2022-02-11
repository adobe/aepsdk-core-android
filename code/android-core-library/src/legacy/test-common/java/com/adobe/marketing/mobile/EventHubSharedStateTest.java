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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class EventHubSharedStateTest {
	private static PlatformServices services = new FakePlatformServices();
	private static EventData readState = null;
	private static final Semaphore moduleReadingStateSemaphore = new Semaphore(0, true);
	private static CountDownLatch eventHubLatch = new CountDownLatch(1);
	private static int EVENTHUB_WAIT_MS = 50;

	// simple read/write test
	public static class ModuleSharingState extends InternalModule {
		public ModuleSharingState(final EventHub hub, final PlatformServices services) {
			super("SHARER", hub, services);
			this.registerListener(EventType.CUSTOM, EventSource.NONE, CreateStateListener.class);
		}

		public static class CreateStateListener extends ModuleEventListener<ModuleSharingState> {
			public CreateStateListener(final ModuleSharingState module, final EventType type, final EventSource source) {
				super(module, type, source);
			}

			public void hear(final Event e) {
				parentModule.createSharedState(e.getEventNumber(), new EventData().putString("myString", "myValue"));
			}
		}
	}

	public static class ModuleReadingState extends InternalModule {
		public ModuleReadingState(final EventHub hub, final PlatformServices services) {
			super("ModuleReadingState", hub, services);
			this.registerListener(EventType.ANALYTICS, EventSource.NONE, ReadStateListener.class);
			this.registerListener(EventType.TARGET, EventSource.NONE, EndListener.class);
		}

		public static class ReadStateListener extends ModuleEventListener<ModuleReadingState> {
			public ReadStateListener(final ModuleReadingState module, final EventType type, final EventSource source) {
				super(module, type, source);
			}

			public void hear(final Event e) {
				readState = parentModule.getSharedEventState("SHARER", e);
			}
		}

		public static class EndListener extends ModuleEventListener<ModuleReadingState> {
			public EndListener(final ModuleReadingState module, final EventType type, final EventSource source) {
				super(module, type, source);
			}

			public void hear(final Event e) {
				moduleReadingStateSemaphore.release();
			}
		}
	}

	@Test(timeout = 1000)
	public void readStateTest() throws Exception {
		final EventHub hub = new EventHub("Shared State Test Hub", services);
		hub.registerModule(ModuleSharingState.class);
		hub.registerModule(ModuleReadingState.class);

		final CountDownLatch bootLatch = new CountDownLatch(1);
		hub.finishModulesRegistration(new AdobeCallback<Void>() {
			@Override
			public void call(Void value) {
				bootLatch.countDown();
			}
		});
		bootLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		hub.dispatch(new Event.Builder("Create Shared State", EventType.CUSTOM, EventSource.NONE).build());
		hub.dispatch(new Event.Builder("Read Shared State", EventType.ANALYTICS, EventSource.NONE).build());
		hub.dispatch(new Event.Builder("Stop Waiting", EventType.TARGET, EventSource.NONE).build());

		moduleReadingStateSemaphore.tryAcquire(1000, TimeUnit.MILLISECONDS);

		Assert.assertEquals("shared state was not set", "myValue", readState.getString("myString"));
	}

	// time that the async update should "take" to run
	private static final int ASYNC_TIMEOUT = 1;

	// deferred read/write test (with an update)
	public static class ModuleSharedStateAsyncUpdate extends InternalModule {

		private final static AtomicInteger updateCount = new AtomicInteger(0);

		public ModuleSharedStateAsyncUpdate(final EventHub hub, final PlatformServices services) {
			super("SHARED", hub, services);
			this.registerListener(EventType.CUSTOM, EventSource.NONE, CreateStateListener.class);
		}

		public static class CreateStateListener extends ModuleEventListener<ModuleSharedStateAsyncUpdate> {
			public CreateStateListener(final ModuleSharedStateAsyncUpdate module, final EventType type, final EventSource source) {
				super(module, type, source);
			}

			public void hear(final Event e) {
				// create the initial null state so we can update it after background task completes.
				final int createdVersion = e.getEventNumber();
				parentModule.createSharedState(createdVersion, null);
				// fire background task
				new Thread(new AsyncBackgroundTask(createdVersion, parentModule)).run();
			}
		}

		// emulates a network request that responds in 1 second
		private static class AsyncBackgroundTask implements Runnable {
			private final int updateVersion;
			private final ModuleSharedStateAsyncUpdate module;
			private static final Semaphore waitingSemaphore = new Semaphore(0, true);
			public AsyncBackgroundTask(final int updateVersion, final ModuleSharedStateAsyncUpdate module) {
				this.updateVersion = updateVersion;
				this.module = module;
			}

			public void run() {
				try {
					waitingSemaphore.tryAcquire(ASYNC_TIMEOUT, TimeUnit.MILLISECONDS);
				} catch (Exception e) {}

				this.module.updateSharedState(updateVersion, new EventData().putString("state",
											  "updated" + updateCount.incrementAndGet()));
			}
		}
	}

	private static ArrayList<String> moduleReadingAsyncStateProcessedEvents = new ArrayList<String>();
	private static final Semaphore moduleReadingAsyncStateSemaphore = new Semaphore(0, true);

	public static class ModuleReadingAsyncState extends InternalModule {
		private final ConcurrentLinkedQueue<Event> waitingEvents;

		public ModuleReadingAsyncState(final EventHub hub, final PlatformServices services) {
			super("ModuleReadingAsyncState", hub, services);
			waitingEvents = new ConcurrentLinkedQueue<Event>();
			this.registerListener(EventType.HUB, EventSource.SHARED_STATE, StateChangeListener.class);
			this.registerListener(EventType.ANALYTICS, EventSource.NONE, EventListener.class);
		}

		private void processEvent(final Event e) {
			if (e != null) {
				waitingEvents.add(e);
			}

			while (!waitingEvents.isEmpty()) {
				Event curEvent = waitingEvents.peek();
				EventData state = getSharedEventState("SHARED", curEvent);

				if (state == null) {
					break;
				}

				moduleReadingAsyncStateProcessedEvents.add(state.getString("state"));

				if (moduleReadingAsyncStateProcessedEvents.size() == 5) {
					moduleReadingAsyncStateSemaphore.release();
				}

				waitingEvents.poll();
			}
		}

		// standard event listener
		public static class EventListener extends ModuleEventListener<ModuleReadingAsyncState> {
			public EventListener(final ModuleReadingAsyncState module, final EventType type, final EventSource source) {
				super(module, type, source);
			}

			public void hear(final Event e) {
				parentModule.processEvent(e);
			}
		}

		// listens for shared state changes
		public static class StateChangeListener extends ModuleEventListener<ModuleReadingAsyncState> {
			public StateChangeListener(final ModuleReadingAsyncState module, final EventType type, final EventSource source) {
				super(module, type, source);
			}

			public void hear(final Event e) {
				// make sure we try and process events after the state change.
				parentModule.processEvent(null);
			}
		}

	}

	@Test(timeout = 3000)
	public void readAsyncStateTest() throws Exception {
		final EventHub hub = new EventHub("Shared State Async Hub", services);
		hub.registerModule(ModuleSharedStateAsyncUpdate.class);
		hub.registerModule(ModuleReadingAsyncState.class);

		final CountDownLatch bootLatch = new CountDownLatch(1);
		hub.finishModulesRegistration(new AdobeCallback<Void>() {
			@Override
			public void call(Void value) {
				bootLatch.countDown();
			}
		});
		bootLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		hub.dispatch(new Event.Builder("Create New Shared State", EventType.CUSTOM, EventSource.NONE).build());
		hub.dispatch(new Event.Builder("Event That Needs Shared State", EventType.ANALYTICS, EventSource.NONE).build());
		hub.dispatch(new Event.Builder("Event That Needs Shared State", EventType.ANALYTICS, EventSource.NONE).build());
		hub.dispatch(new Event.Builder("Create New Shared State", EventType.CUSTOM, EventSource.NONE).build());
		hub.dispatch(new Event.Builder("Event That Needs Shared State", EventType.ANALYTICS, EventSource.NONE).build());
		hub.dispatch(new Event.Builder("Event That Needs Shared State", EventType.ANALYTICS, EventSource.NONE).build());
		hub.dispatch(new Event.Builder("Event That Needs Shared State", EventType.ANALYTICS, EventSource.NONE).build());
		hub.dispatch(new Event.Builder("Event That Needs Shared State", EventType.ANALYTICS, EventSource.NONE).build());
		hub.dispatch(new Event.Builder("Event That Needs Shared State", EventType.ANALYTICS, EventSource.NONE).build());
		hub.dispatch(new Event.Builder("Event That Needs Shared State", EventType.ANALYTICS, EventSource.NONE).build());

		// wait for events to be processed, each update to shared state above will require ASYNC_TIMEOUT to run
		moduleReadingAsyncStateSemaphore.tryAcquire(ASYNC_TIMEOUT * 2 + 500, TimeUnit.MILLISECONDS);

		Assert.assertEquals("event 1 did not receive state", "updated1", moduleReadingAsyncStateProcessedEvents.get(0));
		Assert.assertEquals("event 2 did not receive state", "updated1", moduleReadingAsyncStateProcessedEvents.get(1));
		Assert.assertEquals("event 3 did not receive state", "updated2", moduleReadingAsyncStateProcessedEvents.get(2));
		Assert.assertEquals("event 4 did not receive state", "updated2", moduleReadingAsyncStateProcessedEvents.get(3));
		Assert.assertEquals("event 5 did not receive state", "updated2", moduleReadingAsyncStateProcessedEvents.get(4));
	}


}

