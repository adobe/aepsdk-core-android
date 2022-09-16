/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2020 Adobe
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
import static org.junit.Assert.assertTrue;

import com.adobe.marketing.mobile.identity.IdentityListenerResponseIdentity;

public class IdentityListenerResponseIdentityTest extends BaseTest {
	private IdentityListenerResponseIdentity listener;
	private MockIdentityExtension identityModule;

	@Before
	public void beforeEach() {
		super.beforeEach();
		identityModule = new MockIdentityExtension(eventHub, platformServices);
		listener = new IdentityListenerResponseIdentity(identityModule, EventType.IDENTITY, EventSource.RESPONSE_IDENTITY);
	}

	@Test
	public void testHear_handleIdentityResponseIdentityForSharedState_Called_WhenEventWithNullData() throws Exception {
		listener.hear(new Event.Builder("TEST", EventType.IDENTITY, EventSource.RESPONSE_IDENTITY).build());
		waitForExecutor(identityModule.getExecutor());
		assertTrue(identityModule.handleIdentityResponseIdentityForSharedStateWasCalled);
	}

	@Test
	public void testHear_handleIdentityResponseIdentityForSharedState_Called_WhenEventWithNonNullData() throws Exception {
		EventData eventData = new EventData();
		eventData.putString("key", "val");
		listener.hear(new Event.Builder("TEST", EventType.IDENTITY, EventSource.RESPONSE_IDENTITY)
					  .setData(eventData)
					  .build());
		waitForExecutor(identityModule.getExecutor());
		assertTrue(identityModule.handleIdentityResponseIdentityForSharedStateWasCalled);
	}
}