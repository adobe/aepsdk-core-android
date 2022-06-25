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

import static org.junit.Assert.*;

public class ConfigurationListenerBootEventTest extends BaseTest {

	private MockConfigurationExtension mockConfiguration;
	private ConfigurationListenerBootEvent listener;

	@Before
	public void setup() {
		super.beforeEach();

		// Setup
		mockConfiguration = new MockConfigurationExtension(eventHub, platformServices);
		listener = new ConfigurationListenerBootEvent(mockConfiguration,
				EventType.HUB,
				EventSource.BOOTED);
	}

	// TODO uncomment after Configuration refactor
	@Ignore
	@Test
	public void testListener_Constructor_With_ValidParameter() {
		// Verify
		assertNotNull("the constructor should not return Null", listener);
		assertEquals("the constructor should not return a ConfigurationListenerRequestContent instance",
					 listener.getClass(),
					 ConfigurationListenerBootEvent.class);
	}

	@Ignore
	@Test
	public void testListener_when_BootEvent() throws Exception {
		// Setup
		Event bootedEvent = new Event.Builder("EventHub", EventType.HUB, EventSource.BOOTED).build();
		// Test
		listener.hear(bootedEvent);

		waitForExecutor(mockConfiguration.getExecutor());
		// Verify
		assertTrue("Handle Boot event much br called", mockConfiguration.handleBootEventWasCalled);
		assertEquals("Passes the correct event", bootedEvent, mockConfiguration.handleBootEventParamEvent);
	}
}
