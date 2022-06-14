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
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//public class SignalListenerConfigurationResponseContentTests {
//	MockSignal module;
//	ListenerConfigurationResponseContentSignal listener;
//	FakePlatformServices fakePlatformServices;
//
//	@Before()
//	public void beforeEach() {
//		fakePlatformServices = new FakePlatformServices();
//		module = new MockSignal(new MockEventHubUnitTest("UnitTest", fakePlatformServices), fakePlatformServices);
//		listener = new ListenerConfigurationResponseContentSignal(module, null, null);
//	}
//
//	@Test
//	public void updatePrivacyStatusWithUnknown_When_EventDataNotContainPrivacyStatus() {
//		Event e = new Event.Builder("Test", EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData()).build();;
//		listener.hear(e);
//		Assert.assertEquals(MobilePrivacyStatus.UNKNOWN, module.updatePrivacyStatusParameter);
//	}
//
//	@Test
//	public void updatePrivacyStatusWithOptout_When_EventDataContainPrivacyStatusWithOptout() {
//		Event e = new Event.Builder("Test", EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData().putString(SignalConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
//										   MobilePrivacyStatus.OPT_OUT.getValue())).build();;
//		listener.hear(e);
//		Assert.assertEquals(MobilePrivacyStatus.OPT_OUT, module.updatePrivacyStatusParameter);
//
//	}
//
//}