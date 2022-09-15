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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DispatcherIdentityResponseIdentityIdentityTest {

	private FakePlatformServices platformService;
	private MockEventHubUnitTest mockEventHub;

	@Test
	public void testDispatchResponse_ShouldHaveEventTypeResponseIdentity() {
		platformService =  new FakePlatformServices();
		mockEventHub = new MockEventHubUnitTest("Mockhub", platformService);

		DispatcherIdentityResponseIdentityIdentity dispatcherIdentityResponseIdentityIdentity = new
		DispatcherIdentityResponseIdentityIdentity(mockEventHub,
				null);
		dispatcherIdentityResponseIdentityIdentity.dispatchResponse("testEventName", null, null);
		assertNotNull(mockEventHub.dispatchedEvent);
		assertEquals(mockEventHub.dispatchedEvent.getEventType(), EventType.IDENTITY);
		assertEquals(mockEventHub.dispatchedEvent.getEventSource(), EventSource.RESPONSE_IDENTITY);
	}

}
