/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Systems Incorporated
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

import static org.junit.Assert.assertTrue;

public class LifecycleListenerSharedStateTest extends BaseTest {
	private LifecycleListenerSharedState testListener;
	private MockLifecycleExtension mockLifecycleExtension;

	@Before
	public void beforeEach() {
		super.beforeEach();
		mockLifecycleExtension = new MockLifecycleExtension(eventHub, platformServices);
		testListener = new LifecycleListenerSharedState(mockLifecycleExtension, EventType.HUB, EventSource.SHARED_STATE);
	}

	@Test
	public void hear_Happy() throws Exception {
		Event event = new Event.Builder("TEST", EventType.HUB, EventSource.SHARED_STATE)
		.setData(new EventData().putString("stateOwner", "testDependency")).build();
		testListener.hear(event);
		waitForExecutor(mockLifecycleExtension.getExecutor());
		assertTrue(mockLifecycleExtension.handleSharedStateUpdateEventCalled);
	}
}
