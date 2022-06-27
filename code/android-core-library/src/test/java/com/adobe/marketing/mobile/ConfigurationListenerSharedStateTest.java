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
import static org.junit.Assert.*;

public class ConfigurationListenerSharedStateTest extends BaseTest {
	private MockConfigurationExtension mockConfiguration;
	private ConfigurationListenerSharedState configurationListenerSharedState;


	@Before
	public void setup() {
		// Setup
		FakePlatformServices mockPlatformService =  new FakePlatformServices();
		EventHub mockEventHub = new EventHub("testHub", mockPlatformService);
		mockConfiguration = new MockConfigurationExtension(mockEventHub, mockPlatformService);
		configurationListenerSharedState = new ConfigurationListenerSharedState(mockConfiguration,
				EventType.HUB,
				EventSource.SHARED_STATE);
	}

	// TODO fix after Configuration refactor
	@Ignore
	@Test
	public void testListener_Constructor_With_ValidParameter() {
		// Test
		configurationListenerSharedState = new ConfigurationListenerSharedState(mockConfiguration,
				EventType.HUB,
				EventSource.SHARED_STATE);
		// Verify
		assertNotNull("the constructor should not return null", configurationListenerSharedState);
		assertEquals("the constructor should not return a ConfigurationListenerSharedState instance",
					 configurationListenerSharedState.getClass(),
					 ConfigurationListenerSharedState.class);
	}

	@Ignore
	@Test
	public void testListener_when_NullEventData() throws Exception {
		// Setup
		Event event = new Event.Builder("Shared State Change", EventType.HUB, EventSource.SHARED_STATE).setData(null).build();

		// Test
		configurationListenerSharedState.hear(event);
		waitForExecutor(mockConfiguration.getExecutor());

		// Verify
		assertFalse("processGetSdkIds should not be called", mockConfiguration.processGetSdkIdsEventWasCalled);
	}

	@Ignore
	@Test
	public void testListener_when_EmptyEventData() throws Exception {
		// Setup
		Event event = new Event.Builder("Shared State Change", EventType.HUB, EventSource.SHARED_STATE).build();

		// Test
		configurationListenerSharedState.hear(event);
		waitForExecutor(mockConfiguration.getExecutor());

		// Verify
		assertFalse("processGetSdkIds should not be called", mockConfiguration.processGetSdkIdsEventWasCalled);
	}

	@Ignore
	@Test
	public void testListener_when_SharedStateOwnerNull() throws Exception {
		// Setup
		EventData eventData = new EventData();
		eventData.putString("stateOwner", null);
		Event event = new Event.Builder("Shared State Change", EventType.HUB, EventSource.SHARED_STATE)
		.setData(eventData)
		.build();

		// Test
		configurationListenerSharedState.hear(event);
		waitForExecutor(mockConfiguration.getExecutor());

		// Verify
		assertFalse("processGetSdkIds should not be called", mockConfiguration.processGetSdkIdsEventWasCalled);
	}

	@Ignore
	@Test
	public void testListener_when_SharedStateOwnerInvalid() throws Exception {
		// Setup
		EventData eventData = new EventData();
		eventData.putInteger("stateowner", 4);
		Event event = new Event.Builder("Shared State Change", EventType.HUB, EventSource.SHARED_STATE)
		.setData(eventData)
		.build();

		// Test
		configurationListenerSharedState.hear(event);
		waitForExecutor(mockConfiguration.getExecutor());

		// Verify
		assertFalse("processGetSdkIds should not be called", mockConfiguration.processGetSdkIdsEventWasCalled);
	}

	@Ignore
	@Test
	public void testListener_when_SharedStateOwnerIsNotRequiredModule() throws Exception {
		// Setup
		EventData eventData = new EventData();
		eventData.putString("stateowner", "com.adobe.module.userprofile");
		Event event = new Event.Builder("Shared State Change", EventType.HUB, EventSource.SHARED_STATE)
		.setData(eventData)
		.build();

		// Test
		configurationListenerSharedState.hear(event);
		waitForExecutor(mockConfiguration.getExecutor());

		// Verify
		assertFalse("processGetSdkIds should not be called", mockConfiguration.processGetSdkIdsEventWasCalled);
	}

	@Ignore
	@Test
	public void testListener_when_SharedStateOwnerIsConfiguration() throws Exception {
		// Setup
		EventData eventData = new EventData();
		eventData.putString("stateowner", "com.adobe.module.configuration");
		Event event = new Event.Builder("Configuration Shared State Change", EventType.HUB, EventSource.SHARED_STATE)
		.setData(eventData)
		.build();

		// Test
		configurationListenerSharedState.hear(event);
		waitForExecutor(mockConfiguration.getExecutor());

		// Verify
		assertTrue("processGetSdkIds should be called", mockConfiguration.processGetSdkIdsEventWasCalled);
	}

	@Ignore
	@Test
	public void testListener_when_SharedStateOwnerIsAudience() throws Exception {
		// Setup
		EventData eventData = new EventData();
		eventData.putString("stateowner", "com.adobe.module.audience");
		Event event = new Event.Builder("Audience Shared State Change", EventType.HUB, EventSource.SHARED_STATE)
		.setData(eventData)
		.build();

		// Test
		configurationListenerSharedState.hear(event);
		waitForExecutor(mockConfiguration.getExecutor());

		// Verify
		assertTrue("processGetSdkIds should be called", mockConfiguration.processGetSdkIdsEventWasCalled);
	}

	@Ignore
	@Test
	public void testListener_when_SharedStateOwnerIsAnalytics() throws Exception {
		// Setup
		EventData eventData = new EventData();
		eventData.putString("stateowner", "com.adobe.module.analytics");
		Event event = new Event.Builder("Analytics Shared State Change", EventType.HUB, EventSource.SHARED_STATE)
		.setData(eventData)
		.build();

		// Test
		configurationListenerSharedState.hear(event);
		waitForExecutor(mockConfiguration.getExecutor());

		// Verify
		assertTrue("processGetSdkIds should be called", mockConfiguration.processGetSdkIdsEventWasCalled);
	}

	@Ignore
	@Test
	public void testListener_when_SharedStateOwnerIsIdentity() throws Exception {
		// Setup
		EventData eventData = new EventData();
		eventData.putString("stateowner", "com.adobe.module.identity");
		Event event = new Event.Builder("Identity Shared State Change", EventType.HUB, EventSource.SHARED_STATE)
		.setData(eventData)
		.build();

		// Test
		configurationListenerSharedState.hear(event);
		waitForExecutor(mockConfiguration.getExecutor());

		// Verify
		assertTrue("processGetSdkIds should be called", mockConfiguration.processGetSdkIdsEventWasCalled);
	}

	@Ignore
	@Test
	public void testListener_when_SharedStateOwnerIsTarget() throws Exception {
		// Setup
		EventData eventData = new EventData();
		eventData.putString("stateowner", "com.adobe.module.target");
		Event event = new Event.Builder("Target Shared State Change", EventType.HUB, EventSource.SHARED_STATE)
		.setData(eventData)
		.build();

		// Test
		configurationListenerSharedState.hear(event);
		waitForExecutor(mockConfiguration.getExecutor());

		// Verify
		assertTrue("processGetSdkIds should be called", mockConfiguration.processGetSdkIdsEventWasCalled);
	}


}
