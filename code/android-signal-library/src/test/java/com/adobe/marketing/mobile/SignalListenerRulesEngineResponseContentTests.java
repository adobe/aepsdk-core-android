///* ***********************************************************************
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2018 Adobe Systems Incorporated
// * All Rights Reserved.
// *
// * NOTICE:  All information contained herein is, and remains
// * the property of Adobe Systems Incorporated and its suppliers,
// * if any.  The intellectual and technical concepts contained
// * herein are proprietary to Adobe Systems Incorporated and its
// * suppliers and are protected by trade secret or copyright law.
// * Dissemination of this information or reproduction of this material
// * is strictly forbidden unless prior written permission is obtained
// * from Adobe Systems Incorporated.
// **************************************************************************/
//
//package com.adobe.marketing.mobile;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
//public class SignalListenerRulesEngineResponseContentTests {
//	MockSignal module;
//	ListenerRulesEngineResponseContentSignal listener;
//	FakePlatformServices fakePlatformServices;
//
//	private Event createEmptyDataSignalEvent() {
//		EventData data = new EventData();
//		return new Event.Builder("Test", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).setData(data).build();
//	}
//
//	private Event createValidSignalEvent() {
//		HashMap<String, Variant> triggeredConsequence = new HashMap<String, Variant>();
//		HashMap<String, Variant> consequenceDetail = new HashMap<String, Variant>();
//
//		consequenceDetail.put("templateurl", Variant.fromString("my-url"));
//
//		triggeredConsequence.put("id", Variant.fromString("my-id"));
//		triggeredConsequence.put("type", Variant.fromString("pb"));
//		triggeredConsequence.put("detail", Variant.fromVariantMap(consequenceDetail));
//
//		EventData data = new EventData();
//		data.putVariantMap("triggeredconsequence", triggeredConsequence);
//
//		return new Event.Builder("Test", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).setData(data).build();
//	}
//
//	@Before()
//	public void beforeEach() {
//		fakePlatformServices = new FakePlatformServices();
//		module = new MockSignal(new MockEventHubUnitTest("UnitTest", fakePlatformServices), fakePlatformServices);
//		listener = new ListenerRulesEngineResponseContentSignal(module, null, null);
//	}
//
//	@Test
//	public void notCallOnReceiveSignalEvent_When_EventDoesNotContainSignalOrPiiIds() {
//		Event e = createEmptyDataSignalEvent();
//		listener.hear(e);
//		Assert.assertNull(module.onReceivePostbackConsequenceEvent);
//	}
//
//	@Test
//	public void callOnReceiveSignalEvent_When_EventContainSignalId() {
//		Event e = createValidSignalEvent();
//		listener.hear(e);
//		Assert.assertEquals(e, module.onReceivePostbackConsequenceEvent);
//	}
//
//}