/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 ******************************************************************************/

package com.adobe.marketing.mobile;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.adobe.marketing.mobile.identity.ListenerHubBootedIdentity;

public class ListenerHubBootedIdentityTest extends BaseTest {
	private ListenerHubBootedIdentity listener;
	private MockIdentityExtension identityModule;

	@Before
	public void beforeEach() {
		super.beforeEach();
		identityModule = new MockIdentityExtension(eventHub, platformServices);
		listener = new ListenerHubBootedIdentity(identityModule, EventType.HUB, EventSource.SHARED_STATE);
	}

	@Test
	public void testHear_ShouldCallBootUpMethodOnParentModule() throws Exception {
		Event testEvent = new Event.Builder("test", EventType.HUB,
											EventSource.BOOTED).build();
		listener.hear(testEvent);
		waitForExecutor(identityModule.getExecutor());
		assertTrue(identityModule.bootupWasCalled);
	}

	@Test
	public void testHear_ShouldEnqueueForcedIDSyncNetworkCall_TryToProcessEventQueue() throws Exception {
		Event testEvent = new Event.Builder("test", EventType.HUB,
											EventSource.BOOTED).build();
		listener.hear(testEvent);
		waitForExecutor(identityModule.getExecutor());
		assertEquals(identityModule.eventsQueue.size(), 1);
		Event event = identityModule.eventsQueue.poll();
		assertEquals("id-construct-forced-sync", event.getName());
	}
}
