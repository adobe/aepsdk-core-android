/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2021 Adobe
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

import com.adobe.marketing.mobile.identity.ListenerIdentityGenericIdentityRequestReset;

public class ListenerGenericIdentityRequestResetTest extends BaseTest {

	private ListenerIdentityGenericIdentityRequestReset listener;
	private MockIdentityExtension identityModule;

	@Before
	public void beforeEach() {
		super.beforeEach();
		identityModule = new MockIdentityExtension(eventHub, platformServices);
		listener = new ListenerIdentityGenericIdentityRequestReset(identityModule, EventType.GENERIC_IDENTITY,
				EventSource.REQUEST_RESET);
	}

	@Test
	public void testHear_DoesNotEnqueueEvent_When_EventIsNull() throws Exception {
		listener.hear(null);
		waitForExecutor(identityModule.getExecutor());
		assertEquals(identityModule.eventsQueue.size(), 0);
	}

	@Test
	public void testHear_EnqueuesEvent_When_EventIsNotNull() throws Exception {
		listener.hear(new Event.Builder("TEST", EventType.IDENTITY,
										EventSource.REQUEST_RESET).build());
		waitForExecutor(identityModule.getExecutor());

		assertTrue(identityModule.tryProcessingEventQueueWasCalled);
		assertEquals(identityModule.eventsQueue.size(), 1);
	}
}
