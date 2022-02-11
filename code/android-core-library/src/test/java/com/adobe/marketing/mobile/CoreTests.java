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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

public class CoreTests {
	private TestableCore          core;
	private MockEventHubUnitTest  eventHub;
	private ExtensionErrorCallback<ExtensionError> errorCallback;

	private static final String ADOBE_PREFIX = "com.adobe.eventType.";
	public static final EventType GENERIC_TRACK = EventType.get(ADOBE_PREFIX + "generic.track");
	public static final EventType GENERIC_LIFECYLE = EventType.get(ADOBE_PREFIX + "generic.lifecycle");
	public static final EventType GENERIC_IDENTITY = EventType.get(ADOBE_PREFIX + "generic.identity");
	public static final EventType GENERIC_PII = EventType.get(ADOBE_PREFIX + "generic.pii");
	private final ExtensionError[] resultError = new ExtensionError[1];
	private final boolean[] callbackCalled = new boolean[1];
	final CountDownLatch latch = new CountDownLatch(1);
	final CountDownLatch latch2 = new CountDownLatch(1);

	@Before
	public void testSetup() {
		PlatformServices fakePlatformServices = new FakePlatformServices();
		eventHub = new MockEventHubUnitTest("MockEventHubUnitTest", fakePlatformServices);
		core = new TestableCore(fakePlatformServices, eventHub);
		resultError[0] = null;
		callbackCalled[0] = false;

		errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
				resultError[0] = extensionError;
				callbackCalled[0] = true;
				latch.countDown();
			}
		};
	}

	@Test
	public void testConfigureWithAppId_should_dispatch_Configuration_Request_Event() {
		// Test
		core.configureWithAppID("someAppID");
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals("someAppID", eventHub.dispatchedEvent.getData().optString(
						 CoreTestConstants.Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID, null));
		assertEquals("Configure with AppID", eventHub.dispatchedEvent.getName());
		assertEquals(EventType.CONFIGURATION, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
	}

	@Test
	public void testConfigureWithAppId_when_NullAppID_should_dispatch_Configuration_Request_Event() {
		// Test
		core.configureWithAppID(null);
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals(null, eventHub.dispatchedEvent.getData().optString(
						 CoreTestConstants.Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID, null));
		assertEquals("Configure with AppID", eventHub.dispatchedEvent.getName());
		assertEquals(EventType.CONFIGURATION, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
	}

	@Test
	public void testConfigureWithAppId_when_EmptyAppID_should_dispatch_Configuration_Request_Event() {
		// Test
		core.configureWithAppID("");
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals("", eventHub.dispatchedEvent.getData().optString(
						 CoreTestConstants.Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID, null));
		assertEquals("Configure with AppID", eventHub.dispatchedEvent.getName());
		assertEquals(EventType.CONFIGURATION, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());

	}

	@Test
	public void testConfigureWithFileInPath_should_dispatch_Configuration_Request_Event() {
		// Test
		core.configureWithFileInPath("fakePath");
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals("fakePath", eventHub.dispatchedEvent.getData().optString(
						 CoreTestConstants.Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH, null));
		assertEquals("Configure with FilePath", eventHub.dispatchedEvent.getName());
		assertEquals(EventType.CONFIGURATION, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
	}

	@Test
	public void testConfigureWithFileInPath_when_NullURL_shouldNot_dispatch_Configuration_Request_Event() {
		// Test
		core.configureWithFileInPath(null);
		// Verify
		assertFalse(eventHub.isDispatchedCalled);
	}

	@Test
	public void testConfigureWithFileInPath_when_EmptyURL_shouldNot_dispatch_Configuration_Request_Event() {
		// Test
		core.configureWithFileInPath("");
		// Verify
		assertFalse(eventHub.isDispatchedCalled);
	}

	@Test
	public void testUpdateConfiguration_should_dispatch_Configuration_Request_Event() throws VariantException {
		// Test
		HashMap <String, Object> configMap = new HashMap<String, Object>();
		configMap.put("configKey", "configValue");
		core.updateConfiguration(configMap);
		// Verify
		assertTrue(eventHub.isDispatchedCalled);


		Map<String, Variant> configVariantMap = Variant.fromTypedMap(configMap,
												PermissiveVariantSerializer.DEFAULT_INSTANCE).getVariantMap();
		assertEquals(configVariantMap, eventHub.dispatchedEvent.getData().optVariantMap(
						 CoreTestConstants.Configuration.CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG, null));
		assertEquals("Configuration Update", eventHub.dispatchedEvent.getName());
		assertEquals(EventType.CONFIGURATION, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
	}

	@Test
	public void testUpdateConfiguration_whenNullConfigMap_should_dispatch_Configuration_Request_Event() {
		// Test
		core.updateConfiguration(null);
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals(null, eventHub.dispatchedEvent.getData()
					 .optVariantMap(CoreTestConstants.Configuration.CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG, null));
		assertEquals("Configuration Update", eventHub.dispatchedEvent.getName());
		assertEquals(EventType.CONFIGURATION, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
	}

	@Test
	public void testClearConfiguration_should_dispatch_Configuration_Request_Event() throws VariantException {
		// Test;
		core.clearUpdatedConfiguration();
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals("Clear updated configuration", eventHub.dispatchedEvent.getName());
		assertEquals(EventType.CONFIGURATION, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
		assertEquals(true, eventHub.dispatchedEvent.getData().optBoolean(
						 CoreTestConstants.Configuration.CONFIGURATION_REQUEST_CONTENT_CLEAR_UPDATED_CONFIG, false));
	}


	@Test
	public void testDispatchEvent_whenNullEvent_noCallback_doesNotThrow() {
		// Test
		try {
			core.dispatchEvent(null, null);
		} catch (Exception e) {
			Assert.fail("No error should be thrown when the event is null");
		}

		// Verify
		assertFalse(eventHub.isDispatchedCalled);
	}

	@Test
	public void testDispatchEvent_whenNullEvent_shouldReturnFalse() {
		// Test
		boolean dispatchResult = true;

		try {
			dispatchResult = core.dispatchEvent(null, null);
		} catch (Exception e) {
			Assert.fail("No error should be thrown when the event is null");
		}

		// Verify
		assertFalse(dispatchResult);
		assertFalse(eventHub.isDispatchedCalled);
	}

	@Test
	public void testDispatchEvent_whenValidEvent_retrunsTrue() {
		// Test
		boolean dispatchResult = false;
		dispatchResult = core.dispatchEvent(new Event.Builder("test", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).build(),
											errorCallback);

		// Verify
		assertTrue(dispatchResult);
		assertTrue(eventHub.isDispatchedCalled);
	}


	@Test
	public void testDispatchEvent_whenNullEvent_withCallback_returnsErrorInCallback() throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		final ExtensionError[] resultError = new ExtensionError[1];
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
				resultError[0] = extensionError;
				latch.countDown();
			}
		};

		// Test
		try {
			core.dispatchEvent(null, errorCallback);
		} catch (Exception e) {
			Assert.fail("No error should be thrown when the event is null");
		}

		// Verify
		latch.await(500, TimeUnit.MICROSECONDS);
		assertFalse(eventHub.isDispatchedCalled);
		assertEquals(ExtensionError.EVENT_NULL, resultError[0]);
	}

	@Test
	public void testDispatchEvent_whenValidEvent_withCallback_happy() throws Exception {
		// Test
		core.dispatchEvent(new Event.Builder("test", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).build(), errorCallback);

		// Verify
		latch.await(500, TimeUnit.MICROSECONDS);
		assertTrue(eventHub.isDispatchedCalled);
		assertFalse(callbackCalled[0]);
	}

	@Test
	public void testDispatchEventWithResponseCallback_whenNullEvent_returnsErrorInCallback() throws Exception {
		final Event[] responseEvent = new Event[1];
		AdobeCallback<Event> responseCallback = new AdobeCallback<Event>() {
			@Override
			public void call(final Event value) {
				responseEvent[0] = value;
			}
		};

		// Test
		try {
			core.dispatchEventWithResponseCallback(null, responseCallback, errorCallback);
		} catch (Exception e) {
			Assert.fail("No error should be thrown when the event is null");
		}

		// Verify
		latch.await(500, TimeUnit.MICROSECONDS);
		assertFalse(eventHub.isDispatchedCalled);
		assertEquals(ExtensionError.EVENT_NULL, resultError[0]);
	}

	@Test
	public void testDispatchEventWithResponseCallback_whenValidEvent_andValidResponseCallback_registersOneTimeListener()
	throws Exception {
		final Event[] responseEvent = new Event[1];
		AdobeCallback<Event> responseCallback = new AdobeCallback<Event>() {
			@Override
			public void call(final Event value) {
				responseEvent[0] = value;
			}
		};

		Event requestEvent = new Event.Builder("test", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).build();

		// Test
		try {
			core.dispatchEventWithResponseCallback(requestEvent, responseCallback, errorCallback);
		} catch (Exception e) {
			Assert.fail("No error should be thrown when the event is valid");
		}

		// Verify
		latch.await(500, TimeUnit.MICROSECONDS);
		assertTrue(eventHub.isDispatchedCalled);
		assertTrue(eventHub.registerOneTimeListenerCalled);
		assertEquals(requestEvent.getResponsePairID(), eventHub.registerOneTimeListenerParamPairId);
		assertFalse(callbackCalled[0]);
	}

	@Test
	public void testDispatchEventWithResponseCallbackWithError_whenNullEventOrNullCallback_notDispatchEvent() throws
		Exception {
		Event requestEvent = new Event.Builder("test", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).build();

		// Test
		try {
			core.dispatchEventWithResponseCallback(null, new AdobeCallbackWithError<Event>() {
				@Override
				public void fail(AdobeError error) {
				}

				@Override
				public void call(Event value) {
				}
			});
			core.dispatchEventWithResponseCallback(requestEvent, null);
		} catch (Exception e) {
			Assert.fail("No error should be thrown when the event is null");
		}

		// Verify
		latch.await(500, TimeUnit.MICROSECONDS);
		assertFalse(eventHub.isDispatchedCalled);
	}

	@Test
	public void
	testDispatchEventWithResponseCallbackWithError_whenValidEvent_andValidResponseCallback_registersOneTimeListener()
	throws Exception {
		final Event[] responseEvent = new Event[1];
		Event requestEvent = new Event.Builder("test", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).build();

		// Test
		try {
			core.dispatchEventWithResponseCallback(requestEvent, new AdobeCallbackWithError<Event>() {
				@Override
				public void fail(AdobeError error) {

				}

				@Override
				public void call(Event value) {
					responseEvent[0] = value;
				}
			});
		} catch (Exception e) {
			Assert.fail("No error should be thrown when the event is valid");
		}

		// Verify
		latch.await(500, TimeUnit.MICROSECONDS);
		assertTrue(eventHub.isDispatchedCalled);
		assertTrue(eventHub.registerOneTimeListenerWithErrorCalled);
		assertEquals(requestEvent.getResponsePairID(), eventHub.registerOneTimeListenerWithErrorParamPairId);
		assertFalse(callbackCalled[0]);
	}

	@Test
	public void testDispatchResponseEvent_whenValidResponse_andValidRequest_dispatchesEvent() throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		final boolean[] callbackCalled = new boolean[1];
		ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
			@Override
			public void error(final ExtensionError extensionError) {
				callbackCalled[0] = true;
				latch.countDown();
			}
		};

		Event responseEvent = new Event.Builder("testResponse", EventType.ANALYTICS, EventSource.RESPONSE_CONTENT).build();

		Event requestEvent = new Event.Builder("testRequest", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).build();

		// Test
		try {
			core.dispatchResponseEvent(responseEvent, requestEvent, errorCallback);
		} catch (Exception e) {
			Assert.fail("No error should be thrown when the event is valid");
		}

		// Verify
		latch.await(500, TimeUnit.MICROSECONDS);
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals(requestEvent.getResponsePairID(), eventHub.dispatchedEvent.getPairID());
		assertFalse(callbackCalled[0]);
	}

	@Test
	public void testDispatchResponseEvent_whenValidResponse_andNullRequest_returnErrorInCallback() throws Exception {
		Event responseEvent = new Event.Builder("testResponse", EventType.ANALYTICS, EventSource.RESPONSE_CONTENT).build();

		// Test
		try {
			core.dispatchResponseEvent(responseEvent, null, errorCallback);
		} catch (Exception e) {
			Assert.fail("No error should be thrown when the request event is null");
		}

		// Verify
		latch.await(500, TimeUnit.MICROSECONDS);
		assertFalse(eventHub.isDispatchedCalled);
		assertEquals(ExtensionError.EVENT_NULL, resultError[0]);
	}

	@Test
	public void testDispatchResponseEvent_whenNullResponse_andValidRequest_returnErrorInCallback() throws Exception {
		Event requestEvent = new Event.Builder("testRequest", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).build();

		// Test
		try {
			core.dispatchResponseEvent(null, requestEvent, errorCallback);
		} catch (Exception e) {
			Assert.fail("No error should be thrown when the request event is null");
		}

		// Verify
		latch.await(500, TimeUnit.MICROSECONDS);
		assertFalse(eventHub.isDispatchedCalled);
		assertEquals(ExtensionError.EVENT_NULL, resultError[0]);
	}

	@Test
	public void testDispatchResponseEvent_whenNullResponse_andNullRequest_returnErrorInCallback() throws Exception {
		// Test
		try {
			core.dispatchResponseEvent(null, null, errorCallback);
		} catch (Exception e) {
			Assert.fail("No error should be thrown when the request event is null");
		}

		// Verify
		latch.await(500, TimeUnit.MICROSECONDS);
		assertFalse(eventHub.isDispatchedCalled);
		assertEquals(ExtensionError.EVENT_NULL, resultError[0]);
	}

	@Test
	public void testTrack_Action_Happy() {
		// Test
		Map<String, String> c = new HashMap<String, String>();
		c.put("key", "value");
		core.trackAction("action", c);
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals("action", eventHub.dispatchedEvent.getData().optString(
						 CoreTestConstants.Analytics.TRACK_ACTION, null));
		assertEquals(c, eventHub.dispatchedEvent.getData().optStringMap(
						 CoreTestConstants.Analytics.CONTEXT_DATA, null));
		assertEquals(GENERIC_TRACK, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
	}

	@Test
	public void testTrack_State_Happy() {
		// Test
		Map<String, String> c = new HashMap<String, String>();
		c.put("key", "value");
		core.trackState("state", c);
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals("state", eventHub.dispatchedEvent.getData().optString(
						 CoreTestConstants.Analytics.TRACK_STATE, null));
		assertEquals(c, eventHub.dispatchedEvent.getData().optStringMap(
						 CoreTestConstants.Analytics.CONTEXT_DATA, null));
		assertEquals(GENERIC_TRACK, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
	}

	@Test
	public void testTrack_State_No_ContextData() {
		// Test
		core.trackState("state", null);
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals("state", eventHub.dispatchedEvent.getData().optString(
						 CoreTestConstants.Analytics.TRACK_STATE, null));
		assertEquals(0, eventHub.dispatchedEvent.getData().optStringMap(
						 CoreTestConstants.Analytics.CONTEXT_DATA, null).size());
		assertEquals(GENERIC_TRACK, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
	}

	@Test
	public void testLifecycleStart_Happy() {
		// Test
		Map<String, String> c = new HashMap<String, String>();
		c.put("key", "value");
		core.lifecycleStart(c);
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals("start", eventHub.dispatchedEvent.getData().optString(
						 CoreTestConstants.Lifecycle.LIFECYCLE_ACTION_KEY, null));
		assertEquals(c, eventHub.dispatchedEvent.getData().optStringMap(
						 CoreTestConstants.Lifecycle.ADDITIONAL_CONTEXT_DATA, null));
		assertEquals(GENERIC_LIFECYLE, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
	}

	@Test
	public void testLifecycleStart_No_ContextData() {
		// Test
		core.lifecycleStart(null);
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals("start", eventHub.dispatchedEvent.getData().optString(
						 CoreTestConstants.Lifecycle.LIFECYCLE_ACTION_KEY, null));
		assertNull(eventHub.dispatchedEvent.getData().optStringMap(
					   CoreTestConstants.Lifecycle.ADDITIONAL_CONTEXT_DATA, null));
		assertEquals(GENERIC_LIFECYLE, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
	}


	@Test
	public void testLifecyclePause_Happy() {
		// Test
		core.lifecyclePause();
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals("pause", eventHub.dispatchedEvent.getData().optString(
						 CoreTestConstants.Lifecycle.LIFECYCLE_ACTION_KEY, null));
		assertNull(eventHub.dispatchedEvent.getData().optStringMap(
					   CoreTestConstants.Lifecycle.ADDITIONAL_CONTEXT_DATA, null));
		assertEquals(GENERIC_LIFECYLE, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
	}


	@Test
	public void testIdentitySetPushIdentifier_Happy() {
		// Test
		core.setPushIdentifier("pushid");
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals("pushid", eventHub.dispatchedEvent.getData().optString(
						 CoreTestConstants.Identity.PUSH_ID, null));
		assertEquals(GENERIC_IDENTITY, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
	}

	@Test
	public void testIdentitySetAdId_Happy() {
		// Test
		core.setAdvertisingIdentifier("adid");
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals("adid", eventHub.dispatchedEvent.getData().optString(
						 CoreTestConstants.Identity.ADV_ID, null));
		assertEquals(GENERIC_IDENTITY, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
	}

	@Test
	public void testCollectPii_Happy() {
		// Test
		Map<String, String> c = new HashMap<String, String>();
		c.put("key", "value");
		core.collectPii(c);
		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals(c, eventHub.dispatchedEvent.getData().optStringMap(
						 CoreTestConstants.Signal.SIGNAL_CONTEXT_DATA, null));
		assertEquals(GENERIC_PII, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
	}

	@Test
	public void testCollectPii_No_ContextData() {
		// Test
		core.collectPii(null);
		// Verify
		assertFalse(eventHub.isDispatchedCalled);
	}

	@Test
	public void testCollectData_Happy() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("deeplink", "deeplink://data");
		core.collectData(data);
		assertTrue(eventHub.isDispatchedCalled);
		EventData dispatchedData = eventHub.dispatchedEvent.getData();
		assertNotNull(dispatchedData);
		assertTrue(dispatchedData.containsKey("deeplink"));
		assertEquals(data.get("deeplink"), dispatchedData.optString("deeplink", null));

		assertEquals(EventType.GENERIC_DATA, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.OS, eventHub.dispatchedEvent.getEventSource());
	}

	@Test
	public void testCollectData_EmptyData() {
		Map<String, Object> data = new HashMap<String, Object>();
		core.collectData(data);
		assertFalse(eventHub.isDispatchedCalled);
	}

	@Test
	public void testCollectData_NullData() {
		core.collectData(null);
		assertFalse(eventHub.isDispatchedCalled);
	}

	@Test
	public void testStart_SecondCall() {
		eventHub.finishModulesRegistrationCalled = false;
		core.start(null);
		assertTrue(eventHub.finishModulesRegistrationCalled);
		eventHub.finishModulesRegistrationCalled = false;
		core.start(null);
		assertFalse(eventHub.finishModulesRegistrationCalled);
	}

	@Test
	public void testConstructor_WithVersion() {
		final Core coreWithVersion = new Core(new FakePlatformServices(), "1.2.3");

		assertNotNull(coreWithVersion.eventHub);
	}

	@Test
	public void testRegisterEventListener_Happy() throws Exception {
		String eventType = "a.b.c";
		String eventSource = "x.y.z";
		core.registerEventListener(eventType, eventSource, new AdobeCallbackWithError<Event>() {
			private int counter = 0;
			@Override
			public void fail(AdobeError error) {

			}

			@Override
			public void call(Event value) {
				assertEquals("value_test", value.getEventData().get("key_test"));
				counter ++;

				if (counter == 3) {
					latch.countDown();
				}
			}
		});
		Map<String, Object> data = new HashMap<>();
		data.put("key_test", "value_test");
		core.dispatchEvent(new Event.Builder("test1", eventType, eventSource).setEventData(data).build(), null);
		core.dispatchEvent(new Event.Builder("unexpected event", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).build(),
						   null);
		core.dispatchEvent(new Event.Builder("test2", eventType, eventSource).setEventData(data).build(), null);
		core.dispatchEvent(new Event.Builder("test3", eventType, eventSource).setEventData(data).build(), null);
		latch.await(100, TimeUnit.MICROSECONDS);
	}

	@Test
	public void testRegisterEventListener_MultipleListenersForSameEvent() throws Exception {
		String eventType = "a.b.c";
		String eventSource = "x.y.z";
		core.registerEventListener(eventType, eventSource, new AdobeCallbackWithError<Event>() {
			@Override
			public void fail(AdobeError error) {

			}

			@Override
			public void call(Event value) {
				assertEquals("value_test", value.getEventData().get("key_test"));
				latch.countDown();
			}
		});
		core.registerEventListener(eventType, eventSource, new AdobeCallbackWithError<Event>() {
			@Override
			public void fail(AdobeError error) {

			}

			@Override
			public void call(Event value) {
				assertEquals("value_test", value.getEventData().get("key_test"));
				latch2.countDown();
			}
		});
		Map<String, Object> data = new HashMap<>();
		data.put("key_test", "value_test");
		core.dispatchEvent(new Event.Builder("test1", eventType, eventSource).setEventData(data).build(), null);
		core.dispatchEvent(new Event.Builder("unexpected event", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).build(),
						   null);
		core.dispatchEvent(new Event.Builder("test2", eventType, eventSource).setEventData(data).build(), null);
		core.dispatchEvent(new Event.Builder("test3", eventType, eventSource).setEventData(data).build(), null);
		latch.await(100, TimeUnit.MICROSECONDS);
		latch2.await(100, TimeUnit.MICROSECONDS);
	}

	@Test
	public void testResetIdentities_Happy() {
		// Test
		core.resetIdentities();

		// Verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals(GENERIC_IDENTITY, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.REQUEST_RESET, eventHub.dispatchedEvent.getEventSource());
	}
}
