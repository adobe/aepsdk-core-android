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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.adobe.marketing.mobile.identity.IdentityListenerConfigurationResponseContent;

public class IdentityListenerConfigurationResponseContentTests extends BaseTest {

	private IdentityListenerConfigurationResponseContent listener;
	private MockIdentityExtension identityModule;

	@Before
	public void beforeEach() {
		super.beforeEach();
		identityModule = new MockIdentityExtension(eventHub, platformServices);
		listener = new IdentityListenerConfigurationResponseContent(identityModule,
				EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT);
	}

	@Test
	public void testHear_UpdateLatestValidConfiguration_NotCalled_When_EventIsNull() throws Exception {
		listener.hear(null);
		waitForExecutor(identityModule.getExecutor());
		assertFalse(identityModule.updateLatestValidConfigurationWasCalled);
	}

	@Test
	public void testHear_UpdateLatestValidConfiguration_Called_When_EventIsNotNull() throws Exception {
		listener.hear(new Event.Builder("TEST", EventType.CONFIGURATION,
										EventSource.RESPONSE_CONTENT).build());
		waitForExecutor(identityModule.getExecutor());

		assertTrue(identityModule.updateLatestValidConfigurationWasCalled);
	}
}
