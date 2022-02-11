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
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigurationDispatcherConfigurationResponseContentTest {
	private MockEventHubUnitTest         eventHub;
	private ConfigurationDispatcherConfigurationResponseContent responseDispatcher;


	@Before
	public void beforeEach() {
		FakePlatformServices fakePlatformServices = new FakePlatformServices();
		eventHub = new MockEventHubUnitTest("TestHub", fakePlatformServices);
		responseDispatcher = new ConfigurationDispatcherConfigurationResponseContent(eventHub,
				new ConfigurationExtension(eventHub,
										   fakePlatformServices));
	}

	@Test
	public void testDispatchConfigResponseWithEventData_Valid() {
		// Test
		EventData eventData = new EventData();
		responseDispatcher.dispatchConfigResponseWithEventData(eventData, "pairID");
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		Event dispatchedEvent = eventHub.dispatchedEvent;
		assertNotNull(dispatchedEvent);
		assertEquals("Configuration Response Event", dispatchedEvent.getName());
		assertEquals(EventType.CONFIGURATION, dispatchedEvent.getEventType());
		assertEquals(EventSource.RESPONSE_CONTENT, dispatchedEvent.getEventSource());
		assertEquals(eventData, dispatchedEvent.getData());
		assertEquals("pairID", dispatchedEvent.getPairID());
	}

}


