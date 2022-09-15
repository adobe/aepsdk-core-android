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

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DispatcherAnalyticsRequestContentIdentityTest {

	private FakePlatformServices platformService;
	private MockEventHubUnitTest mockEventHub;
	private static final String ANALYTICS_FOR_IDENTITY_REQUEST_EVENT_NAME = "AnalyticsForIdentityRequest";


	@Test
	public void testDispatchAnalyticsHit_ShouldNotDispatch_When_EventDataIsNull() {
		platformService =  new FakePlatformServices();
		mockEventHub = new MockEventHubUnitTest("Mockhub", platformService);

		DispatcherAnalyticsRequestContentIdentity dispatcherIdentityResponseEvent = new
		DispatcherAnalyticsRequestContentIdentity(mockEventHub,
				null);

		dispatcherIdentityResponseEvent.dispatchAnalyticsHit(null);
		assertNull(mockEventHub.dispatchedEvent);
	}

	@Test
	public void testDispatchAnalyticsHit_ShouldDispatch_When_EventDataIsNotNull() {
		platformService =  new FakePlatformServices();
		mockEventHub = new MockEventHubUnitTest("Mockhub", platformService);

		DispatcherAnalyticsRequestContentIdentity dispatcherIdentityResponseEvent = new
		DispatcherAnalyticsRequestContentIdentity(mockEventHub,
				null);

		EventData eventData = new EventData();
		eventData.putStringMap("empty-map", new HashMap<String, String>());

		dispatcherIdentityResponseEvent.dispatchAnalyticsHit(eventData);
		assertNotNull(mockEventHub.dispatchedEvent);
		assertEquals(mockEventHub.dispatchedEvent.getEventType(), EventType.ANALYTICS);
		assertEquals(mockEventHub.dispatchedEvent.getEventSource(), EventSource.REQUEST_CONTENT);
		assertEquals(mockEventHub.dispatchedEvent.getName(), ANALYTICS_FOR_IDENTITY_REQUEST_EVENT_NAME);
	}

}
