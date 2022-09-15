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

public class ListenerAnalyticsResponseIdentityTest extends BaseTest {
	private ListenerAnalyticsResponseIdentity listener;
	private MockIdentityExtension identityModule;

	@Before
	public void beforeEach() {
		super.beforeEach();
		identityModule = new MockIdentityExtension(eventHub, platformServices);
		listener = new ListenerAnalyticsResponseIdentity(identityModule, EventType.ANALYTICS, EventSource.RESPONSE_IDENTITY);
	}


	@Test
	public void testHearWithAid() throws Exception {
		EventData data = new EventData();
		data.putString(IdentityTestConstants.EventDataKeys.Analytics.ANALYTICS_ID, "aid");
		Event event = new Event.Builder("TEST", EventType.ANALYTICS, EventSource.RESPONSE_IDENTITY)
		.setData(data)
		.build();
		listener.hear(event);
		waitForExecutor(identityModule.getExecutor());

		assertTrue(identityModule.handleAnalyticsResponseIdentityWasCalled);
		assertEquals(1, identityModule.eventsQueue.size());
	}

	@Test
	public void testHearWithOutAid() throws Exception {
		Event event = new Event.Builder("TEST", EventType.ANALYTICS, EventSource.RESPONSE_IDENTITY)
		.build();
		listener.hear(event);
		waitForExecutor(identityModule.getExecutor());

		assertTrue(identityModule.handleAnalyticsResponseIdentityWasCalled);
		assertEquals(0, identityModule.eventsQueue.size());
	}
}
