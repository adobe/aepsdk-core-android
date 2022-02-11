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


import android.app.Application;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MobileCoreTests {

	private PlatformServices platformServices = new DummyPlatformService();
	private EventHub mockEventHub = new EventHub("", platformServices);
	private MockCore core;

	private Application mockApplication;

	@Before
	public void setup() {
		Log.setLoggingService(platformServices.getLoggingService());
		core  = new MockCore(platformServices, mockEventHub);
		MobileCore.setCore(core);
	}

	@After
	public void teardown() {
		if (mockApplication != null) {
			App.clearAppResources();
		}
	}

	@Test
	public void testTrackState() throws Exception {
		//Setup
		Map<String, String> c = new HashMap<String, String>();
		c.put("key", "value");
		//Test
		MobileCore.trackState("state", c);
		//Verify
		assertTrue(core.trackStateCalled);
		assertEquals("state", core.trackStateParameterState);
		assertEquals(c, core.trackStateParameterContextData);
	}

	@Test
	public void testTrackState_With_No_ContextData() throws Exception {
		//Test
		MobileCore.trackState("state", null);
		//Verify
		assertTrue(core.trackStateCalled);
		assertEquals("state", core.trackStateParameterState);
		assertNull(core.trackStateParameterContextData);
	}

	@Test
	public void testTrackAction() throws Exception {
		//Setup
		Map<String, String> c = new HashMap<String, String>();
		c.put("key", "value");
		//Test
		MobileCore.trackAction("action", c);
		//Verify
		assertTrue(core.trackActionCalled);
		assertEquals("action", core.trackActionParameterAction);
		assertEquals(c, core.trackActionParameterContextData);
	}

	@Test
	public void testTrackAction_With_No_ContextData() throws Exception {
		//Test
		MobileCore.trackAction("action", null);
		//Verify
		assertTrue(core.trackActionCalled);
		assertEquals("action", core.trackActionParameterAction);
		assertNull(core.trackActionParameterContextData);
	}

	@Test
	public void testCollectPii() throws Exception {
		//Setup
		Map<String, String> c = new HashMap<String, String>();
		c.put("key", "value");
		//Test
		MobileCore.collectPii(c);
		//Verify
		assertTrue(core.collectPiiCalled);
		assertEquals(c, core.collectPiiParameterData);
	}

	@Test
	public void testCollectPii_With_No_Data() throws Exception {
		//Test
		MobileCore.collectPii(null);
		//Verify
		assertTrue(core.collectPiiCalled);
		assertNull(core.collectPiiParameterData);
	}

	@Test
	public void testSetAdvertisingIdentifier() throws Exception {
		//Test
		MobileCore.setAdvertisingIdentifier("advid");
		//Verify
		assertTrue(core.setAdvertisingIdentifierCalled);
		assertEquals("advid", core.setAdvertisingIdentifierParameteradid);
	}

	@Test
	public void testSetPushIdentifier() throws Exception {
		//Test
		MobileCore.setPushIdentifier("pushid");
		//Verify
		assertTrue(core.setPushIdentifierCalled);
		assertEquals("pushid", core.setPushIdentifierParameterRegistrationID);
	}

	@Test
	public void testLifecycleStart() throws Exception {
		//Setup
		Map<String, String> c = new HashMap<String, String>();
		c.put("key", "value");
		//Test
		MobileCore.lifecycleStart(c);
		//Verify
		assertTrue(core.lifecycleStartCalled);
		assertEquals(c, core.lifecycleStartCalledParameterAdditionalContextData);
	}

	@Test
	public void testLifecyclePause() throws Exception {
		//Test
		MobileCore.lifecyclePause();
		//Verify
		assertTrue(core.lifecyclePauseCalled);
	}

	@Test
	public void testSetAndGetApplicationExpectEqual() {
		mockApplication = Mockito.mock(Application.class);
		MobileCore.setApplication(mockApplication);
		Application app = MobileCore.getApplication();
		assertNotNull(app);
		assertEquals(mockApplication, app);
	}

	@Test
	public void testGetApplicationWithoutFirstSettingApplicationExpectNull() {
		Application app = MobileCore.getApplication();
		assertNull(app);
	}

	@Test
	public void testSetGetLogLevel() {

		MobileCore.setLogLevel(LoggingMode.VERBOSE);
		assertEquals(LoggingMode.VERBOSE, MobileCore.getLogLevel());

		MobileCore.setLogLevel(LoggingMode.WARNING);
		assertEquals(LoggingMode.WARNING, MobileCore.getLogLevel());

		MobileCore.setLogLevel(LoggingMode.DEBUG);
		assertEquals(LoggingMode.DEBUG, MobileCore.getLogLevel());

		MobileCore.setLogLevel(LoggingMode.ERROR);
		assertEquals(LoggingMode.ERROR, MobileCore.getLogLevel());

		// check to ensure test is updated if LoggingMode is changed
		assertEquals(4, LoggingMode.values().length);
	}

	@Test
	public void testlogVerbose() {
		final String expectedTag = "LOG_TAG";
		final String expectedMessage = "Expected log message.";
		final CountDownLatch latch = new CountDownLatch(1);

		MockLoggingService mockLoggingService = createMockLoggingService();
		mockLoggingService.setTraceCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				assertEquals(expectedTag, tag);
				assertEquals(expectedMessage, message);
				latch.countDown();
			}
		});
		Log.setLoggingService(mockLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);

		MobileCore.log(LoggingMode.VERBOSE, expectedTag, expectedMessage);
		assertEquals(0, latch.getCount()); // call is synchronous so no need to wait
	}

	@Test
	public void testlogDebug() {
		final String expectedTag = "LOG_TAG";
		final String expectedMessage = "Expected log message.";
		final CountDownLatch latch = new CountDownLatch(1);

		MockLoggingService mockLoggingService = createMockLoggingService();
		mockLoggingService.setDebugCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				assertEquals(expectedTag, tag);
				assertEquals(expectedMessage, message);
				latch.countDown();
			}
		});
		Log.setLoggingService(mockLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);

		MobileCore.log(LoggingMode.DEBUG, expectedTag, expectedMessage);
		assertEquals(0, latch.getCount()); // call is synchronous so no need to wait
	}

	@Test
	public void testlogWarning() {
		final String expectedTag = "LOG_TAG";
		final String expectedMessage = "Expected log message.";
		final CountDownLatch latch = new CountDownLatch(1);

		MockLoggingService mockLoggingService = createMockLoggingService();
		mockLoggingService.setWarningCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				assertEquals(expectedTag, tag);
				assertEquals(expectedMessage, message);
				latch.countDown();
			}
		});
		Log.setLoggingService(mockLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);

		MobileCore.log(LoggingMode.WARNING, expectedTag, expectedMessage);
		assertEquals(0, latch.getCount()); // call is synchronous so no need to wait
	}

	@Test
	public void testlogError() {
		final String expectedTag = "LOG_TAG";
		final String expectedMessage = "Expected log message.";
		final CountDownLatch latch = new CountDownLatch(1);

		MockLoggingService mockLoggingService = createMockLoggingService();
		mockLoggingService.setErrorCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				assertEquals(expectedTag, tag);
				assertEquals(expectedMessage, message);
				latch.countDown();
			}
		});
		Log.setLoggingService(mockLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);

		MobileCore.log(LoggingMode.ERROR, expectedTag, expectedMessage);
		assertEquals(0, latch.getCount()); // call is synchronous so no need to wait
	}

	@Test
	public void testVerboseLogMode() {
		final String expectedTag = "LOG_TAG";
		final String expectedMessage = "Expected log message.";
		final CountDownLatch latch = new CountDownLatch(4);

		MockLoggingService mockLoggingService = createMockLoggingService();
		mockLoggingService.setErrorCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				assertEquals(expectedTag, tag);
				assertEquals(expectedMessage, message);
				latch.countDown();
			}
		});
		mockLoggingService.setWarningCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				assertEquals(expectedTag, tag);
				assertEquals(expectedMessage, message);
				latch.countDown();
			}
		});
		mockLoggingService.setDebugCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				assertEquals(expectedTag, tag);
				assertEquals(expectedMessage, message);
				latch.countDown();
			}
		});
		mockLoggingService.setTraceCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				assertEquals(expectedTag, tag);
				assertEquals(expectedMessage, message);
				latch.countDown();
			}
		});

		Log.setLoggingService(mockLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);

		MobileCore.log(LoggingMode.ERROR, expectedTag, expectedMessage);
		MobileCore.log(LoggingMode.WARNING, expectedTag, expectedMessage);
		MobileCore.log(LoggingMode.DEBUG, expectedTag, expectedMessage);
		MobileCore.log(LoggingMode.VERBOSE, expectedTag, expectedMessage);
		assertEquals(0, latch.getCount()); // call is synchronous so no need to wait
	}

	@Test
	public void testDebugLogMode() {
		final String expectedTag = "LOG_TAG";
		final String expectedMessage = "Expected log message.";
		final CountDownLatch latch = new CountDownLatch(3);

		MockLoggingService mockLoggingService = createMockLoggingService();
		mockLoggingService.setErrorCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				assertEquals(expectedTag, tag);
				assertEquals(expectedMessage, message);
				latch.countDown();
			}
		});
		mockLoggingService.setWarningCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				assertEquals(expectedTag, tag);
				assertEquals(expectedMessage, message);
				latch.countDown();
			}
		});
		mockLoggingService.setDebugCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				assertEquals(expectedTag, tag);
				assertEquals(expectedMessage, message);
				latch.countDown();
			}
		});

		Log.setLoggingService(mockLoggingService);
		Log.setLogLevel(LoggingMode.DEBUG);

		MobileCore.log(LoggingMode.ERROR, expectedTag, expectedMessage);
		MobileCore.log(LoggingMode.WARNING, expectedTag, expectedMessage);
		MobileCore.log(LoggingMode.DEBUG, expectedTag, expectedMessage);
		MobileCore.log(LoggingMode.VERBOSE, expectedTag, expectedMessage);
		assertEquals(0, latch.getCount()); // call is synchronous so no need to wait
	}

	@Test
	public void testWarningLogMode() {
		final String expectedTag = "LOG_TAG";
		final String expectedMessage = "Expected log message.";
		final CountDownLatch latch = new CountDownLatch(2);

		MockLoggingService mockLoggingService = createMockLoggingService();
		mockLoggingService.setErrorCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				assertEquals(expectedTag, tag);
				assertEquals(expectedMessage, message);
				latch.countDown();
			}
		});
		mockLoggingService.setWarningCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				assertEquals(expectedTag, tag);
				assertEquals(expectedMessage, message);
				latch.countDown();
			}
		});

		Log.setLoggingService(mockLoggingService);
		Log.setLogLevel(LoggingMode.WARNING);

		MobileCore.log(LoggingMode.ERROR, expectedTag, expectedMessage);
		MobileCore.log(LoggingMode.WARNING, expectedTag, expectedMessage);
		MobileCore.log(LoggingMode.DEBUG, expectedTag, expectedMessage);
		MobileCore.log(LoggingMode.VERBOSE, expectedTag, expectedMessage);
		assertEquals(0, latch.getCount()); // call is synchronous so no need to wait
	}

	@Test
	public void testErrorLogMode() {
		final String expectedTag = "LOG_TAG";
		final String expectedMessage = "Expected log message.";
		final CountDownLatch latch = new CountDownLatch(1);

		MockLoggingService mockLoggingService = createMockLoggingService();
		mockLoggingService.setErrorCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				assertEquals(expectedTag, tag);
				assertEquals(expectedMessage, message);
				latch.countDown();
			}
		});

		Log.setLoggingService(mockLoggingService);
		Log.setLogLevel(LoggingMode.ERROR);

		MobileCore.log(LoggingMode.ERROR, expectedTag, expectedMessage);
		MobileCore.log(LoggingMode.WARNING, expectedTag, expectedMessage);
		MobileCore.log(LoggingMode.DEBUG, expectedTag, expectedMessage);
		MobileCore.log(LoggingMode.VERBOSE, expectedTag, expectedMessage);
		assertEquals(0, latch.getCount()); // call is synchronous so no need to wait
	}

	@Test
	public void testlogWithNullMode_shouldNotThrow() {
		final String expectedTag = "LOG_TAG";
		final String expectedMessage = "Expected log message.";

		MockLoggingService mockLoggingService = createMockLoggingService();
		Log.setLoggingService(mockLoggingService);
		Log.setLogLevel(LoggingMode.VERBOSE);

		try {
			MobileCore.log(null, expectedTag, expectedMessage);
		} catch (Exception e) {
			fail("MobileCore log should not throw for null mode");
		}
	}

	@Test
	public void testVersionNoWrapperType() {
		assertFalse(MobileCore.extensionVersion().contains("-"));
	}

	@Test
	public void testVersionWrapperTypeNone() {
		MobileCore.setWrapperType(WrapperType.NONE);
		assertFalse(MobileCore.extensionVersion().contains("-"));
	}

	@Test
	public void testVersionWrapperTypeNoneReset() {
		MobileCore.setWrapperType(WrapperType.NONE);
		MobileCore.setWrapperType(WrapperType.REACT_NATIVE);
		MobileCore.setWrapperType(WrapperType.NONE);
		assertFalse(MobileCore.extensionVersion().contains("-"));
	}

	@Test
	public void testVersionWrapperTypeReactNative() {
		MobileCore.setWrapperType(WrapperType.REACT_NATIVE);
		assertTrue(MobileCore.extensionVersion().contains("-R"));
	}

	@Test
	public void testVersionWrapperTypeReactNativeReset() {
		MobileCore.setWrapperType(WrapperType.REACT_NATIVE);
		MobileCore.setWrapperType(WrapperType.NONE);
		MobileCore.setWrapperType(WrapperType.REACT_NATIVE);
		assertTrue(MobileCore.extensionVersion().contains("-R"));
	}

	@Test
	public void testVersionWrapperTypeNoneResetFlutter() {
		MobileCore.setWrapperType(WrapperType.NONE);
		MobileCore.setWrapperType(WrapperType.FLUTTER);
		MobileCore.setWrapperType(WrapperType.NONE);
		assertFalse(MobileCore.extensionVersion().contains("-"));
	}

	@Test
	public void testVersionWrapperTypeFlutter() {
		MobileCore.setWrapperType(WrapperType.FLUTTER);
		assertTrue(MobileCore.extensionVersion().contains("-F"));
	}

	@Test
	public void testVersionWrapperTypeFlutterReset() {
		MobileCore.setWrapperType(WrapperType.FLUTTER);
		MobileCore.setWrapperType(WrapperType.NONE);
		MobileCore.setWrapperType(WrapperType.FLUTTER);
		assertTrue(MobileCore.extensionVersion().contains("-F"));
	}

	@Test
	public void testVersionWrapperTypeNoneResetCordova() {
		MobileCore.setWrapperType(WrapperType.NONE);
		MobileCore.setWrapperType(WrapperType.CORDOVA);
		MobileCore.setWrapperType(WrapperType.NONE);
		assertFalse(MobileCore.extensionVersion().contains("-"));
	}

	@Test
	public void testVersionWrapperTypeCordova() {
		MobileCore.setWrapperType(WrapperType.CORDOVA);
		assertTrue(MobileCore.extensionVersion().contains("-C"));
	}

	@Test
	public void testVersionWrapperTypeCordovaReset() {
		MobileCore.setWrapperType(WrapperType.CORDOVA);
		MobileCore.setWrapperType(WrapperType.NONE);
		MobileCore.setWrapperType(WrapperType.CORDOVA);
		assertTrue(MobileCore.extensionVersion().contains("-C"));
	}

	@Test
	public void testVersionWrapperTypeNoneResetUnity() {
		MobileCore.setWrapperType(WrapperType.NONE);
		MobileCore.setWrapperType(WrapperType.UNITY);
		MobileCore.setWrapperType(WrapperType.NONE);
		assertFalse(MobileCore.extensionVersion().contains("-"));
	}

	@Test
	public void testVersionWrapperTypeUnity() {
		MobileCore.setWrapperType(WrapperType.UNITY);
		assertTrue(MobileCore.extensionVersion().contains("-U"));
	}

	@Test
	public void testVersionWrapperTypeUnityReset() {
		MobileCore.setWrapperType(WrapperType.UNITY);
		MobileCore.setWrapperType(WrapperType.NONE);
		MobileCore.setWrapperType(WrapperType.UNITY);
		assertTrue(MobileCore.extensionVersion().contains("-U"));
	}

	@Test
	public void testVersionWrapperTypeNoneResetXamarin() {
		MobileCore.setWrapperType(WrapperType.NONE);
		MobileCore.setWrapperType(WrapperType.XAMARIN);
		MobileCore.setWrapperType(WrapperType.NONE);
		assertFalse(MobileCore.extensionVersion().contains("-"));
	}

	@Test
	public void testVersionWrapperTypeXamarin() {
		MobileCore.setWrapperType(WrapperType.XAMARIN);
		assertTrue(MobileCore.extensionVersion().contains("-X"));
	}

	@Test
	public void testVersionWrapperTypeXamarinReset() {
		MobileCore.setWrapperType(WrapperType.XAMARIN);
		MobileCore.setWrapperType(WrapperType.NONE);
		MobileCore.setWrapperType(WrapperType.XAMARIN);
		assertTrue(MobileCore.extensionVersion().contains("-X"));
	}

	/**
	 * Helper to create a {@link MockLoggingService} and populate each callback
	 * with an {@link junit.framework.Assert#fail()};
	 * @return new {@code MockLoggingService} instance
	 */
	private MockLoggingService createMockLoggingService() {
		MockLoggingService mockLoggingService = new MockLoggingService();
		mockLoggingService.setDebugCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				fail("Call to LoggingService.debug was unexpected.");
			}
		});
		mockLoggingService.setTraceCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				fail("Call to LoggingService.trace was unexpected.");
			}
		});
		mockLoggingService.setWarningCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				fail("Call to LoggingService.warning was unexpected.");
			}
		});
		mockLoggingService.setErrorCallback(new MockLoggingService.LoggingCallback() {
			@Override
			public void call(String tag, String message) {
				fail("Call to LoggingService.error was unexpected.");
			}
		});

		return mockLoggingService;
	}

	@Test
	public void testCollectMessageInfo() throws Exception {
		//Setup
		Map<String, Object> c = new HashMap<String, Object>();
		c.put("key", "value");
		c.put("action", 1);
		//Test
		MobileCore.collectMessageInfo(c);
		//Verify
		assertTrue(core.collectDataCalled);
		assertEquals(c, core.collectDataParameterMarshalledData);
	}

	@Test
	public void testCollectMessageInfo_With_Empty_Data() throws Exception {
		//Setup
		Map<String, Object> c = new HashMap<String, Object>();
		//Test
		MobileCore.collectMessageInfo(c);
		//Verify
		assertTrue(core.collectDataCalled);
		assertEquals(c, core.collectDataParameterMarshalledData);
	}

	@Test
	public void testCollectMessageInfo_With_No_Data() throws Exception {
		//Test
		MobileCore.collectMessageInfo(null);
		//Verify
		assertTrue(core.collectDataCalled);
		assertNull(core.collectDataParameterMarshalledData);
	}

	@Test
	public void testDispatchEventWithResponseCallback() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("k", "v");
		Event event = new Event.Builder(" event", "com.adobe.eventType.dispatchEvent",
										"com.test.MobileCoreTest").setEventData(data).build();
		MobileCore.dispatchEventWithResponseCallback(event, new AdobeCallbackWithError<Event>() {
			@Override
			public void fail(AdobeError error) {

			}

			@Override
			public void call(Event value) {

			}
		});
		assertTrue(core.dispatchEventWithResponseCallbackWithTimeoutCalled);
	}

	AdobeError errorInCallback;
	@Test
	public void testDispatchEventWithResponseCallback_null_event() {
		errorInCallback = null;
		MobileCore.dispatchEventWithResponseCallback(null, new AdobeCallbackWithError<Event>() {
			@Override
			public void fail(AdobeError error) {
				errorInCallback = error;
			}

			@Override
			public void call(Event value) {

			}
		});
		assertFalse(core.dispatchEventWithResponseCallbackWithTimeoutCalled);
		assertEquals(AdobeError.UNEXPECTED_ERROR, errorInCallback);
	}

	@Test
	public void testDispatchEventWithResponseCallback_null_callback() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("k", "v");
		Event event = new Event.Builder(" event", "com.adobe.eventType.dispatchEvent",
										"com.test.MobileCoreTest").setEventData(data).build();
		MobileCore.dispatchEventWithResponseCallback(event, null);
		assertFalse(core.dispatchEventWithResponseCallbackWithTimeoutCalled);
	}

	@Test
	public void testResetIdentities() throws Exception {
		//Test
		MobileCore.resetIdentities();
		//Verify
		assertTrue(core.resetIdentitiesCalled);
	}
}
