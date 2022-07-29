/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2021 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 * *************************************************************************/

package com.adobe.marketing.mobile;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class LifecycleV2DispatcherApplicationStateTest extends BaseTest {
	private LifecycleV2DispatcherApplicationState testDispatcher;
	private MockLifecycleExtension mockLifecycleExtension;

	@Before
	public void beforeEach() {
		super.beforeEach();
		mockLifecycleExtension = new MockLifecycleExtension(eventHub, platformServices);
		testDispatcher = new LifecycleV2DispatcherApplicationState(eventHub, mockLifecycleExtension);
	}

	@Test
	public void test_dispatchApplicationLaunch_Happy() throws Exception {
		Map<String, Object> testXDMMap = new HashMap<String, Object>();
		testXDMMap.put("key1", "value1");
		testXDMMap.put("key2", true);
		testXDMMap.put("key3", 3);

		Map<String, String> testFreeFormData = new HashMap<String, String>();
		testFreeFormData.put("k1", "v1");
		testFreeFormData.put("k2", "v2");

		testDispatcher.dispatchApplicationLaunch(testXDMMap, testFreeFormData);
		waitForExecutor(mockLifecycleExtension.getExecutor());
		Event dispatchedEvent = eventHub.dispatchedEvent;
		Map<String, Object> actualEventData = dispatchedEvent.getEventData();
		Map<String, Object> actualXDMData = (Map<String, Object>) actualEventData.get("xdm");
		Map<String, Object> actualFreeFormData = (Map<String, Object>) actualEventData.get("data");

		assertNotNull(actualXDMData);
		assertNotNull(actualFreeFormData);
		assertEquals(testXDMMap, actualXDMData);
		assertEquals(testFreeFormData, actualFreeFormData);
	}

	@Test
	public void test_dispatchApplicationLaunch_NoFreeFormData() throws Exception {
		Map<String, Object> testXDMMap = new HashMap<String, Object>();
		testXDMMap.put("key1", "value1");
		testXDMMap.put("key2", true);
		testXDMMap.put("key3", 3);

		testDispatcher.dispatchApplicationLaunch(testXDMMap, null);
		waitForExecutor(mockLifecycleExtension.getExecutor());
		Event dispatchedEvent = eventHub.dispatchedEvent;
		Map<String, Object> actualEventData = dispatchedEvent.getEventData();
		Map<String, Object> actualXDMData = (Map<String, Object>) actualEventData.get("xdm");

		assertFalse(actualEventData.containsKey("data"));
		assertNotNull(actualXDMData);
		assertEquals(testXDMMap, actualXDMData);
	}

	@Test
	public void test_dispatchApplicationLaunch_emptyXDMData_shouldNotDispatch() throws Exception {
		Map<String, Object> testXDMMap = new HashMap<String, Object>();

		testDispatcher.dispatchApplicationLaunch(testXDMMap, null);
		waitForExecutor(mockLifecycleExtension.getExecutor());
		Event dispatchedEvent = eventHub.dispatchedEvent;

		assertNull(dispatchedEvent);
	}

	@Test
	public void test_dispatchApplicationLaunch_nullXDMData_shouldNotDispatch() throws Exception {
		testDispatcher.dispatchApplicationLaunch(null, null);
		waitForExecutor(mockLifecycleExtension.getExecutor());
		Event dispatchedEvent = eventHub.dispatchedEvent;

		assertNull(dispatchedEvent);
	}

	@Test
	public void test_dispatchApplicationClose_Happy() throws Exception {
		Map<String, Object> testXDMMap = new HashMap<String, Object>();
		testXDMMap.put("key1", "value1");
		testXDMMap.put("key2", true);
		testXDMMap.put("key3", 3);

		testDispatcher.dispatchApplicationClose(testXDMMap);
		waitForExecutor(mockLifecycleExtension.getExecutor());
		Event dispatchedEvent = eventHub.dispatchedEvent;
		Map<String, Object> actualEventData = dispatchedEvent.getEventData();
		Map<String, Object> actualXDMData = (Map<String, Object>) actualEventData.get("xdm");

		assertNotNull(actualXDMData);
		assertEquals(testXDMMap, actualXDMData);
	}

	@Test
	public void test_dispatchApplicationClose_emptyXDMData_shouldNotDispatch() throws Exception {
		Map<String, Object> testXDMMap = new HashMap<String, Object>();

		testDispatcher.dispatchApplicationClose(testXDMMap);
		waitForExecutor(mockLifecycleExtension.getExecutor());
		Event dispatchedEvent = eventHub.dispatchedEvent;

		assertNull(dispatchedEvent);
	}

	@Test
	public void test_dispatchApplicationClose_nullXDMData_shouldNotDispatch() throws Exception {
		testDispatcher.dispatchApplicationClose(null);
		waitForExecutor(mockLifecycleExtension.getExecutor());
		Event dispatchedEvent = eventHub.dispatchedEvent;

		assertNull(dispatchedEvent);
	}
}
