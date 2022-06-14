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

public class ConfigurationListenerRequestContentTest extends BaseTest {

	private MockConfigurationExtension mockConfiguration;
	private ConfigurationListenerRequestContent configurationListenerRequestContent;

	@Before
	public void setup() {
		// Setup
		FakePlatformServices mockPlatformService =  new FakePlatformServices();
		EventHub mockEventHub = new EventHub("testHub", mockPlatformService);
		mockConfiguration = new MockConfigurationExtension(mockEventHub, mockPlatformService);
		configurationListenerRequestContent = new ConfigurationListenerRequestContent(mockConfiguration,
				EventType.CONFIGURATION,
				EventSource.REQUEST_CONTENT);
	}

	// TODO fix after Configuration refactor
	@Ignore
	@Test
	public void testListener_Constructor_With_ValidParameter() {
		// Test
		configurationListenerRequestContent = new ConfigurationListenerRequestContent(mockConfiguration,
				EventType.CONFIGURATION,
				EventSource.REQUEST_CONTENT);
		// Verify
		assertNotNull("the constructor should not return Null", configurationListenerRequestContent);
		assertEquals("the constructor should not return a ConfigurationListenerRequestContent instance",
					 configurationListenerRequestContent.getClass(),
					 ConfigurationListenerRequestContent.class);
	}

	@Ignore
	@Test
	public void testListener_when_HearCalled() throws Exception {
		// Setup
		Event requestContentEvent = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT).build();

		// Test
		configurationListenerRequestContent.hear(requestContentEvent);
		waitForExecutor(mockConfiguration.getExecutor());

		// Verify
		assertTrue("Handle RequestContent event must be called", mockConfiguration.handleEventWasCalled);
		assertEquals("Passes the correct event", requestContentEvent, mockConfiguration.handleEventParamEvent);
	}

}

