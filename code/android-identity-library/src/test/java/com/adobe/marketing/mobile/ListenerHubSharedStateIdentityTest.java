/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2017 Adobe
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.adobe.marketing.mobile.identity.ListenerHubSharedStateIdentity;

public class ListenerHubSharedStateIdentityTest extends BaseTest {

	private ListenerHubSharedStateIdentity listener;
	private MockIdentityExtension identityModule;

	@Before
	public void beforeEach() {
		super.beforeEach();
		identityModule = new MockIdentityExtension(eventHub, platformServices);
		listener = new ListenerHubSharedStateIdentity(identityModule, EventType.HUB, EventSource.SHARED_STATE);
	}

	@Test
	public void testHear_TriesEventsProcessing_When_EventSourceIsConfiguration() throws Exception {
		listener.hear(new Event.Builder("TEST", EventType.HUB,
										EventSource.SHARED_STATE).setData(new EventData().putString(IdentityTestConstants.EventDataKeys.STATE_OWNER,
												"com.adobe.module.configuration")).build());
		waitForExecutor(identityModule.getExecutor());
		assertTrue(identityModule.tryProcessingEventQueueWasCalled);
	}

	@Test
	public void testHear_DoesNotTryEventsProcessing_When_EventSourceIsNotConfiguration() throws Exception {
		listener.hear(new Event.Builder("TEST", EventType.HUB,
										EventSource.SHARED_STATE).setData(new EventData().putString(IdentityTestConstants.EventDataKeys.STATE_OWNER,
												"com.adobe.module.audience")).build());
		waitForExecutor(identityModule.getExecutor());
		assertFalse(identityModule.tryProcessingEventQueueWasCalled);
	}

	@Test
	public void testHear_TriesEventsProcessing_When_AnalyticsEventDataIsNotNull() throws Exception {
		EventData data = new EventData();
		data.putString(IdentityTestConstants.EventDataKeys.STATE_OWNER,
					   "com.adobe.module.analytics");

		listener.hear(new Event.Builder("TEST", EventType.HUB,
										EventSource.SHARED_STATE).setData(data).build());
		waitForExecutor(identityModule.getExecutor());
		assertTrue(identityModule.tryProcessingEventQueueWasCalled);
	}

	@Test
	public void testHear_TriesEventsProcessing_When_HubEventDataIsNotNull() throws Exception {
		EventData data = new EventData();
		data.putString(IdentityTestConstants.EventDataKeys.STATE_OWNER,
					   "com.adobe.module.eventhub");

		listener.hear(new Event.Builder("TEST", EventType.HUB,
										EventSource.SHARED_STATE).setData(data).build());
		waitForExecutor(identityModule.getExecutor());
		assertTrue(identityModule.tryProcessingEventQueueWasCalled);
	}
}
