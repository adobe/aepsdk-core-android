///* *****************************************************************************
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2020 Adobe
// * All Rights Reserved.
// *
// * NOTICE: All information contained herein is, and remains
// * the property of Adobe and its suppliers, if any. The intellectual
// * and technical concepts contained herein are proprietary to Adobe
// * and its suppliers and are protected by all applicable intellectual
// * property laws, including trade secret and copyright laws.
// * Dissemination of this information or reproduction of this material
// * is strictly forbidden unless prior written permission is obtained
// * from Adobe.
// ******************************************************************************/
//package com.adobe.marketing.mobile.identity;
//
//import org.junit.Before;
//import org.junit.Test;
//
//
//import java.util.*;
//
//import static org.junit.Assert.*;
//
//import com.adobe.marketing.mobile.AdobeCallback;
//import com.adobe.marketing.mobile.AdobeCallbackWithError;
//import com.adobe.marketing.mobile.AdobeError;
//import com.adobe.marketing.mobile.identity.IdentityCore;
//
//public class IdentityCoreTest extends BaseTest {
//
//
//	private MockEventHubUnitTest mockEventHub;
//	@Before
//	public void setup() throws Exception {
//
//		super.beforeEach();
//		mockEventHub = new MockEventHubUnitTest("Mockhub", platformServices);
//	}
//
//	@Test
//	public void testGetAdvertisingIdentifier_When_UseAdobeCallbackWithError() {
//		IdentityCore identityCore = new IdentityCore(mockEventHub, new ModuleDetails() {
//			@Override
//			public String getName() {
//				return "IdentityExtension";
//			}
//
//			@Override
//			public String getVersion() {
//				return "IdentityExtensionVersion";
//			}
//
//			@Override
//			public Map<String, String> getAdditionalInfo() {
//				return null;
//			}
//		});
//
//		AdobeCallbackWithError adobeCallbackWithError = new AdobeCallbackWithError<String>() {
//			@Override
//			public void fail(AdobeError adobeError) {
//
//			}
//
//			@Override
//			public void call(String s) {
//
//			}
//		};
//		identityCore.getAdvertisingIdentifier(adobeCallbackWithError);
//		assertTrue(mockEventHub.registerOneTimeListenerWithErrorCalled);
//		assertEquals(500, mockEventHub.registerOneTimeListenerWithErrorParamTimeoutInMilliSec);
//		assertEquals(adobeCallbackWithError, mockEventHub.registerOneTimeListenerWithErrorParamErrorCallback);
//	}
//
//	@Test
//	public void testGetAdvertisingIdentifier_When_UseAdobeCallback() {
//		IdentityCore identityCore = new IdentityCore(mockEventHub, new ModuleDetails() {
//			@Override
//			public String getName() {
//				return "IdentityExtension";
//			}
//
//			@Override
//			public String getVersion() {
//				return "IdentityExtensionVersion";
//			}
//
//			@Override
//			public Map<String, String> getAdditionalInfo() {
//				return null;
//			}
//		});
//		identityCore.getAdvertisingIdentifier(new AdobeCallback<String>() {
//			@Override
//			public void call(String s) {
//
//			}
//		});
//		assertTrue(mockEventHub.registerOneTimeListenerWithErrorCalled);
//		assertEquals(500, mockEventHub.registerOneTimeListenerWithErrorParamTimeoutInMilliSec);
//		assertNull(mockEventHub.registerOneTimeListenerWithErrorParamErrorCallback);
//	}
//
//}
