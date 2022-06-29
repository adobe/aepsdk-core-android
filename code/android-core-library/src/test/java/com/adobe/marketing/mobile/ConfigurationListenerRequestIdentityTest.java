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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConfigurationListenerRequestIdentityTest extends BaseTest {
	private MockConfigurationExtension mockConfiguration;
	private ConfigurationListenerRequestIdentity configurationListenerRequestIdentity;


	@Before
	public void setup() {
		// Setup
		FakePlatformServices mockPlatformService =  new FakePlatformServices();
		EventHub mockEventHub = new EventHub("testHub", mockPlatformService);
		mockConfiguration = new MockConfigurationExtension(mockEventHub, mockPlatformService);
		configurationListenerRequestIdentity = new ConfigurationListenerRequestIdentity(mockConfiguration,
				EventType.CONFIGURATION,
				EventSource.REQUEST_IDENTITY);
	}

	// TODO uncomment after Configuration refactor
	@Ignore
	@Test
	public void testListener_Constructor_With_ValidParameter() {
		// Test
		configurationListenerRequestIdentity = new ConfigurationListenerRequestIdentity(mockConfiguration,
				EventType.CONFIGURATION,
				EventSource.REQUEST_IDENTITY);
		// Verify
		assertNotNull("the constructor should not return Null", configurationListenerRequestIdentity);
		assertEquals("the constructor should not return a ConfigurationListenerRequestIdentity instance",
					 configurationListenerRequestIdentity.getClass(),
					 ConfigurationListenerRequestIdentity.class);
	}

	@Ignore
	@Test
	public void testListener_when_GetSDKIdentitiesEvent() throws Exception {
		// Setup
		Event getSDKIdentitiesEvent = new Event.Builder("GetSDKIdentities Event", EventType.CONFIGURATION,
				EventSource.REQUEST_IDENTITY).build();
		// Test
		configurationListenerRequestIdentity.hear(getSDKIdentitiesEvent);

		waitForExecutor(mockConfiguration.getExecutor());
		// Verify
		assertTrue("Handle GetSDKIdentities must be called", mockConfiguration.handleGetSdkIdentitiesEventCalled);
		assertEquals("Passes the correct event", getSDKIdentitiesEvent,
					 mockConfiguration.handleGetSdkIdentitiesEventParamEvent);
	}
}
