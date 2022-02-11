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
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class EventListenerTest {
	final static String TESTPAIRID = "THISISATESTLOL";

	private static PlatformServices services = new FakePlatformServices();
	private EventHub hub;

	private static CountDownLatch   latch = new CountDownLatch(1);
	private static CountDownLatch	countingEventLatch;
	private static int     			countingEventCount = 0;
	private static int     			wildcardListenerEventCount = 0;
	private static int 				EVENTHUB_WAIT_MS = 50;

	@Before
	public void perTest() {
		countingEventLatch = new CountDownLatch(1);
		countingEventCount = 0;
		wildcardListenerEventCount = 0;

		try {
			hub = new EventHub("Listener Test Hub", services);
			hub.registerModule(CountingTestModule.class);

			final CountDownLatch hubBootWaitLatch = new CountDownLatch(1);
			hub.finishModulesRegistration(new AdobeCallback<Void>() {
				@Override
				public void call(Void value) {
					hubBootWaitLatch.countDown();
				}
			});
			hubBootWaitLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Failed to register module!");
		}
	}

	public static class CountingTestModule extends Module {

		public CountingTestModule(final EventHub hub) {
			super("CountingTestModule", hub);
			registerListener(EventType.CUSTOM, EventSource.NONE, CountingListener.class);
			registerListener(EventType.ANALYTICS, EventSource.NONE, EndListener.class);
			registerListener(EventType.PII, EventSource.RESPONSE_CONTENT, SelfRemovingListener.class);
			registerWildcardListener(WildcardListener.class);

			// for reregister test, we should only end up with one of these.
			for (int i = 0; i < 100; i++) {
				registerListener(EventType.AUDIENCEMANAGER, EventSource.REQUEST_PROFILE, CountingListener.class);
			}

			// for oneTimeListenerTest
			hub.registerOneTimeListener(null, new Module.OneTimeListenerBlock() {
				public void call(final Event e) {
					++countingEventCount;
				}
			});

			// for pairIDTest
			hub.registerOneTimeListener(TESTPAIRID, new Module.OneTimeListenerBlock() {
				public void call(final Event e) {
					++countingEventCount;
				}
			});
		}

		protected static class SelfRemovingListener extends ModuleEventListener<CountingTestModule> {
			public SelfRemovingListener(final CountingTestModule m, final EventType t, final EventSource s) {
				super(m, t, s);
			}

			public void hear(final Event e) {
				++countingEventCount;
				parentModule.unregisterListener(EventType.PII, EventSource.RESPONSE_CONTENT);
				// This does not actually happen immediately now -- so some events may be already in the queue
			}
		}

		protected static class CountingListener extends ModuleEventListener<CountingTestModule> {
			public CountingListener(final CountingTestModule m, final EventType t, final EventSource s) {
				super(m, t, s);
			}

			public void hear(final Event e) {
				++countingEventCount;
			}
		}

		protected static class WildcardListener extends ModuleEventListener<CountingTestModule> {
			public WildcardListener(final CountingTestModule m, final EventType t, final EventSource s) {
				super(m, t, s);
			}

			public void hear(final Event e) {
				++wildcardListenerEventCount;
			}
		}

		public static class EndListener extends ModuleEventListener<CountingTestModule> {
			public EndListener(final CountingTestModule m, final EventType t, final EventSource s) {
				super(m, t, s);
			}

			public void hear(final Event e) {
				countingEventLatch.countDown();
			}
		}
	}

	private void waitForEventDone(final EventHub hub) throws Exception {
		hub.dispatch(new Event.Builder("ending event", EventType.ANALYTICS, EventSource.NONE).build());

		countingEventLatch.await(300, TimeUnit.MILLISECONDS);
	}

	@Test(timeout = 1000)
	public void multiListenerTest() throws Exception {
		final EventData eventData = new EventData()
		.putString("initialkey", "initialvalue");
		final Event newEvent = new Event.Builder("Test Event", EventType.CUSTOM, EventSource.NONE).setData(eventData)
		.build();

		for (int i = 0; i < 100; i++) {
			hub.dispatch(newEvent);
		}

		waitForEventDone(hub);
		Assert.assertEquals(100, countingEventCount);
	}

	@Test(timeout = 1000)
	public void oneTimeListenerTest() throws Exception {
		final EventData eventData = new EventData()
		.putString("initialkey", "initialvalue");
		final Event newEvent = new Event.Builder("Test Event", EventType.CUSTOM,
				EventSource.RESPONSE_CONTENT).setData(eventData).setPairID(TESTPAIRID).build();

		for (int i = 0; i < 100; i++) {
			hub.dispatch(newEvent);
		}

		waitForEventDone(hub);
		Assert.assertEquals(1, countingEventCount);
	}

	@Test(timeout = 1000)
	public void wildCardListenerTest() throws Exception {
		final EventData eventData = new EventData()
		.putString("initialkey", "initialvalue");
		final Event newEvent = new Event.Builder("Test Event", EventType.CUSTOM,
				EventSource.RESPONSE_CONTENT).setData(eventData).build();

		for (int i = 0; i < 100; i++) {
			hub.dispatch(newEvent);
		}

		waitForEventDone(hub);
		//There are 3 extra events counted:
		// - One extra one heard by the wild card listener because a event was dispatched by waitForEventDone() too.
		// - One extra for Hub Booted event from module registration
		// - One extra for eventhub shared state
		// The other 100 times event dispatched by the test were heard by the wild card listener
		Assert.assertEquals(103, wildcardListenerEventCount);
	}

	@Test(timeout = 1000)
	public void wildCardListenerTest_ThirdPartyEvent() throws Exception {
		final EventData eventData = new EventData()
		.putString("initialkey", "initialvalue");

		final Event newEvent = new Event.Builder("Test Event", EventType.get("thirdPartyType"),
				EventSource.get("thirdPartySource")).setData(eventData).build();

		for (int i = 0; i < 100; i++) {
			hub.dispatch(newEvent);
		}

		waitForEventDone(hub);
		//There are 3 extra events counted:
		// - One extra one heard by the wild card listener because a event was dispatched by waitForEventDone() too.
		// - One extra for Hub Booted event from module registration
		// - One extra for Hub shared state
		// The other 100 times event dispatched by the test were heard by the wild card listener
		Assert.assertEquals(103, wildcardListenerEventCount);
	}


	@Test(timeout = 1000)
	public void pairIDTest() throws Exception {
		final EventData eventData = new EventData()
		.putString("initialkey", "initialvalue");
		final Event newEvent = new Event.Builder("Test Event", EventType.CUSTOM,
				EventSource.RESPONSE_PROFILE).setData(eventData).setPairID(TESTPAIRID).build();

		for (int i = 0; i < 100; i++) {
			hub.dispatch(newEvent);
		}

		waitForEventDone(hub);
		Assert.assertEquals(1, countingEventCount);
	}

	@Test(timeout = 1000)
	public void unregisterTest() throws Exception {
		final EventData eventData = new EventData()
		.putString("initialkey", "initialvalue");
		final Event newEvent = new Event.Builder("Test Event", EventType.PII,
				EventSource.RESPONSE_CONTENT).setData(eventData).build();
		hub.dispatch(newEvent);

		latch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);

		for (int i = 0; i < 100; i++) {
			hub.dispatch(newEvent);
		}

		waitForEventDone(hub);

		Log.debug("unregisterTest", "countingEventCount=%d", countingEventCount);
		Assert.assertEquals(1, countingEventCount);
	}

	@Test(timeout = 1000)
	public void reregisterTest() throws Exception {
		final EventData eventData = new EventData().putString("initialkey", "initialvalue");
		final Event newEvent = new Event.Builder("Test Event", EventType.AUDIENCEMANAGER,
				EventSource.REQUEST_PROFILE).setData(eventData).build();
		// we registered the listener 100 times, but we should only have one living listener on this hub
		// a single dispatch should result in a single count.
		hub.dispatch(newEvent);

		waitForEventDone(hub);

		Log.debug("unregisterTest", "countingEventCount=%d", countingEventCount);
		Assert.assertEquals(1, countingEventCount);
	}
}
