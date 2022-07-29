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

import java.util.HashMap;

import static org.junit.Assert.*;

public class LifecycleListenerRequestContentTest extends BaseTest {

	private LifecycleListenerRequestContent testListener;
	private MockLifecycleExtension mockLifecycleExtension;

	@Before
	public void beforeEach() {
		PlatformServices fakePlatformServices = new FakePlatformServices();
		EventHub mockEventHub = new EventHub("testEventHub", fakePlatformServices);
		mockLifecycleExtension = new MockLifecycleExtension(mockEventHub, fakePlatformServices);
		testListener = new LifecycleListenerRequestContent(mockLifecycleExtension, EventType.GENERIC_LIFECYLE,
				EventSource.REQUEST_CONTENT);
	}

	@Test
	public void hear_Happy_LifecycleStart() throws Exception {
		EventData eventData = new EventData()
		.putString("lifecycle.action", "start")
		.putStringMap("lifecycle.additionalContextData", new HashMap<String, String>() {
			{
				put("key", "value");
			}
		});
		Event event = new Event.Builder("lifecycleStart", EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT)
		.setData(eventData)
		.build();
		testListener.hear(event);
		waitForExecutor(mockLifecycleExtension.getExecutor());
		assertTrue(mockLifecycleExtension.queueLifecycleEventCalled);
	}

	@Test
	public void hear_Happy_LifecyclePause() throws Exception {
		EventData eventData = new EventData().putString("lifecycle.action", "pause");
		Event event = new Event.Builder("lifecycleStart", EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT)
		.setData(eventData)
		.build();
		testListener.hear(event);
		waitForExecutor(mockLifecycleExtension.getExecutor());
		assertTrue(mockLifecycleExtension.queueLifecycleEventCalled);
	}

	@Test
	public void hear_InvalidAction() throws Exception {
		EventData eventData = new EventData()
		.putString("lifecycle.action", "invalid_action")
		.putStringMap("lifecycle.additionalContextData", new HashMap<String, String>() {
			{
				put("key", "value");
			}
		});
		Event event = new Event.Builder("lifecycleStart", EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT)
		.setData(eventData)
		.build();
		testListener.hear(event);
		waitForExecutor(mockLifecycleExtension.getExecutor());
		assertFalse(mockLifecycleExtension.startLifecycleCalled);
		assertFalse(mockLifecycleExtension.pauseLifecycleCalled);
	}

	@Test
	public void hear_NoEventData() throws Exception {
		Event event = new Event.Builder("lifecycleStart", EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT).build();
		testListener.hear(event);
		waitForExecutor(mockLifecycleExtension.getExecutor());
		assertFalse(mockLifecycleExtension.startLifecycleCalled);
		assertFalse(mockLifecycleExtension.pauseLifecycleCalled);
	}

}
