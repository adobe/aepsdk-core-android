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

public class ConfigurationDispatcherConfigurationRequestContentTest {
	private MockEventHubUnitTest         eventHub;
	private ConfigurationDispatcherConfigurationRequestContent requestDispatcher;


	@Before
	public void beforeEach() {
		FakePlatformServices fakePlatformServices = new FakePlatformServices();
		eventHub = new MockEventHubUnitTest("TestHub", fakePlatformServices);
		requestDispatcher = new ConfigurationDispatcherConfigurationRequestContent(eventHub,
				new ConfigurationExtension(eventHub,
										   fakePlatformServices));
	}

	// TODO uncomment after Configuration refactor
	/* @Test
	public void testDispatchInternalConfigureWithAppIdEvent() throws Exception {
		//Test
		requestDispatcher.dispatchInternalConfigureWithAppIdEvent("appID");
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		Event dispatchedEvent = eventHub.dispatchedEvent;
		assertNotNull(dispatchedEvent);
		assertEquals("Configure with AppID Internal", dispatchedEvent.getName());
		assertEquals(EventType.CONFIGURATION, dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, dispatchedEvent.getEventSource());
		assertEquals("appID", dispatchedEvent.getData().optString("config.appId", null));
		assertTrue(dispatchedEvent.getData().optBoolean("config.isinternalevent", false));
		assertNull(dispatchedEvent.getPairID());
	} */


}


