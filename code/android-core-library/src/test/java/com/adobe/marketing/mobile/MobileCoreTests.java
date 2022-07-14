///*
//  Copyright 2022 Adobe. All rights reserved.
//  This file is licensed to you under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License. You may obtain a copy
//  of the License at http://www.apache.org/licenses/LICENSE-2.0
//  Unless required by applicable law or agreed to in writing, software distributed under
//  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
//  OF ANY KIND, either express or implied. See the License for the specific language
//  governing permissions and limitations under the License.
// */
//
//package com.adobe.marketing.mobile;
//
//
//import android.app.Application;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.CountDownLatch;
//
//import static junit.framework.Assert.assertEquals;
//import static junit.framework.Assert.assertFalse;
//import static junit.framework.Assert.assertNotNull;
//import static junit.framework.Assert.assertNull;
//import static junit.framework.Assert.assertTrue;
//import static junit.framework.Assert.fail;
//
//@RunWith(MockitoJUnitRunner.Silent.class)
//public class MobileCoreTests {
//	private Application mockApplication;
//
//	@Before
//	public void setup() {
//
//		Log.setLoggingService(platformServices.getLoggingService());
//	}
//
//	@After
//	public void teardown() {
//		if (mockApplication != null) {
//			App.clearAppResources();
//		}
//	}
//
//
//	@Test
//	public void testSetAndGetApplicationExpectEqual() {
//		mockApplication = Mockito.mock(Application.class);
//		MobileCore.setApplication(mockApplication);
//		Application app = MobileCore.getApplication();
//		assertNotNull(app);
//		assertEquals(mockApplication, app);
//	}
//
//	@Test
//	public void testGetApplicationWithoutFirstSettingApplicationExpectNull() {
//		Application app = MobileCore.getApplication();
//		assertNull(app);
//	}
//
//	@Test
//	public void testDispatchEventWithResponseCallback() {
//		Map<String, Object> data = new HashMap<String, Object>();
//		data.put("k", "v");
//		Event event = new Event.Builder(" event", "com.adobe.eventType.dispatchEvent",
//										"com.test.MobileCoreTest").setEventData(data).build();
//		MobileCore.dispatchEventWithResponseCallback(event, new AdobeCallbackWithError<Event>() {
//			@Override
//			public void fail(AdobeError error) {
//
//			}
//
//			@Override
//			public void call(Event value) {
//
//			}
//		});
//		assertTrue(core.dispatchEventWithResponseCallbackWithTimeoutCalled);
//	}
//
//	AdobeError errorInCallback;
//	@Test
//	public void testDispatchEventWithResponseCallback_null_event() {
//		errorInCallback = null;
//		MobileCore.dispatchEventWithResponseCallback(null, new AdobeCallbackWithError<Event>() {
//			@Override
//			public void fail(AdobeError error) {
//				errorInCallback = error;
//			}
//
//			@Override
//			public void call(Event value) {
//
//			}
//		});
//		assertFalse(core.dispatchEventWithResponseCallbackWithTimeoutCalled);
//		assertEquals(AdobeError.UNEXPECTED_ERROR, errorInCallback);
//	}
//
//	@Test
//	public void testDispatchEventWithResponseCallback_null_callback() {
//		Map<String, Object> data = new HashMap<String, Object>();
//		data.put("k", "v");
//		Event event = new Event.Builder(" event", "com.adobe.eventType.dispatchEvent",
//										"com.test.MobileCoreTest").setEventData(data).build();
//		MobileCore.dispatchEventWithResponseCallback(event, null);
//		assertFalse(core.dispatchEventWithResponseCallbackWithTimeoutCalled);
//	}

