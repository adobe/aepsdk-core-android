///****************************************************************************
// *
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
// *
// ***************************************************************************/
//
//package com.adobe.marketing.mobile;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.JUnit4;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//
//@RunWith(JUnit4.class)
//public class SignalModuleTest extends SystemTest {
//	private TestableNetworkService testableNetworkService;
//	private SignalCoreAPI testableCore;
//
//	@Before
//	public void beforeEach() throws Exception {
//		testableNetworkService = platformServices.getTestableNetworkService();
//		eventHub.registerModule(SignalExtension.class);
//		testableCore = new SignalCoreAPI(eventHub);
//		eventHub.clearEvents();
//	}
//
//	@After
//	public void afterEach() {
//		eventHub.clearEvents();
//	}
//
//	@Test
//	public void testSignal_Registration() {
//		assertTrue(testableCore != null);
//	}
//}
