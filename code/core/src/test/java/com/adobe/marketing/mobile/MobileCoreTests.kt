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

package com.adobe.marketing.mobile

import com.adobe.marketing.mobile.internal.CoreConstants
import com.adobe.marketing.mobile.internal.eventhub.EventHub
import com.adobe.marketing.mobile.internal.eventhub.EventHubConstants
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner.Silent::class)
class MobileCoreTests {

    @Mock
    private lateinit var mockedEventHub: EventHub

    @Before
    fun setup() {
        EventHub.shared = mockedEventHub
        MobileCore.sdkInitializedWithContext = AtomicBoolean(false)
    }

    @After
    fun teardown() {
        reset(mockedEventHub)
        EventHub.shared.shutdown()
    }

    @Test
    fun testDispatchEventSimple() {
        val event = Event.Builder("test", "analytics", "requestContent").build()
        MobileCore.dispatchEvent(event)
        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals("analytics", eventCaptor.firstValue.type)
        assertEquals("requestContent", eventCaptor.firstValue.source)
    }

    // / Tests that the response callback is invoked when the trigger event is dispatched
    @Test
    fun testDispatchEventWithResponseCallbackSimple() {
        EventHub.shared = EventHub()
        // setup
        val event = Event.Builder("test", "analytics", "requestContent").build()
        val responseEvent =
            Event.Builder("testResponse", "analytics", "responseContent").inResponseToEvent(event)
                .build()

        EventHub.shared.start()

        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()

        MobileCore.dispatchEventWithResponseCallback(
            event,
            1000,
            object : AdobeCallbackWithError<Event> {
                override fun call(value: Event) {
                    capturedEvents.add(value)
                    latch.countDown()
                }

                override fun fail(error: AdobeError?) {
                    latch.countDown()
                }
            }
        )

        EventHub.shared.dispatch(responseEvent)

        assertTrue { latch.await(1, TimeUnit.SECONDS) }
        assertEquals(responseEvent, capturedEvents[0])
    }

    // / Tests that the event listener only receive the events it is registered for
    @Test
    fun testRegisterEventListener() {
        EventHub.shared = EventHub()
        val event1 = Event.Builder("test", "analytics", "requestContent").build()
        val event2 = Event.Builder("test", "analytics", "requestContent").build()
        val unexpectedEvent = Event.Builder("", "wrong", "wrong").build()

        EventHub.shared.start()

        val latch = CountDownLatch(2)
        val capturedEvents = mutableListOf<Event>()

        MobileCore.registerEventListener("analytics", "requestContent") {
            capturedEvents.add(it)
            latch.countDown()
        }

        // dispatch the events
        MobileCore.dispatchEvent(event1)
        MobileCore.dispatchEvent(event2)
        MobileCore.dispatchEvent(unexpectedEvent)

        assertTrue { latch.await(2, TimeUnit.SECONDS) }
        assertEquals(mutableListOf(event1, event2), capturedEvents)
    }

    // / Tests that the event listeners listening for same events can all receives the events
    @Test
    fun testRegisterEventListenerMultipleListenersForSameEvents() {
        EventHub.shared = EventHub()
        val event1 = Event.Builder("test", "analytics", "requestContent").build()
        val event2 = Event.Builder("test", "analytics", "requestContent").build()
        val unexpectedEvent = Event.Builder("", "wrong", "wrong").build()

        EventHub.shared.start()

        val latch1 = CountDownLatch(2)
        val capturedEvents1 = mutableListOf<Event>()
        MobileCore.registerEventListener(
            "analytics",
            "requestContent"
        ) {
            capturedEvents1.add(it)
            latch1.countDown()
        }

        val latch2 = CountDownLatch(2)
        val capturedEvents2 = mutableListOf<Event>()
        MobileCore.registerEventListener(
            "analytics",
            "requestContent"
        ) {
            capturedEvents2.add(it)
            latch2.countDown()
        }

        // dispatch the events
        MobileCore.dispatchEvent(event1)
        MobileCore.dispatchEvent(event2)
        MobileCore.dispatchEvent(unexpectedEvent)

        assertTrue { latch1.await(2, TimeUnit.SECONDS) }
        assertEquals(mutableListOf(event1, event2), capturedEvents1)
        assertTrue { latch2.await(2, TimeUnit.SECONDS) }
        assertEquals(mutableListOf(event1, event2), capturedEvents2)
    }

    // MARK: setWrapperType(...) tests
    // / No wrapper tag should be appended when the setWrapperType API is never invoked
    @Test
    fun testSetWrapperTypeNeverCalled() {
        EventHub.shared = EventHub()
        assertEquals(EventHubConstants.VERSION_NUMBER, MobileCore.extensionVersion())
    }

    // Tests that no wrapper tag is appended when the wrapper type is none
    @Test
    fun testSetWrapperTypeNone() {
        EventHub.shared = EventHub()
        MobileCore.setWrapperType(WrapperType.NONE)
        assertEquals(EventHubConstants.VERSION_NUMBER, MobileCore.extensionVersion())
    }

    // / Tests that the React Native wrapper tag is appended
    @Test
    fun testSetWrapperTypeReactNative() {
        EventHub.shared = EventHub()
        MobileCore.setWrapperType(WrapperType.REACT_NATIVE)
        assertEquals(EventHubConstants.VERSION_NUMBER + "-R", MobileCore.extensionVersion())
    }

    // / Tests that the Flutter wrapper tag is appended
    @Test
    fun testSetWrapperTypeFlutter() {
        EventHub.shared = EventHub()
        MobileCore.setWrapperType(WrapperType.FLUTTER)
        assertEquals(EventHubConstants.VERSION_NUMBER + "-F", MobileCore.extensionVersion())
    }

    // / Tests that the Cordova wrapper tag is appended
    @Test
    fun testSetWrapperTypeCordova() {
        EventHub.shared = EventHub()
        MobileCore.setWrapperType(WrapperType.CORDOVA)
        assertEquals(EventHubConstants.VERSION_NUMBER + "-C", MobileCore.extensionVersion())
    }

    // / Tests that the Unity wrapper tag is appended
    @Test
    fun testSetWrapperTypeUnity() {
        EventHub.shared = EventHub()
        MobileCore.setWrapperType(WrapperType.UNITY)
        assertEquals(EventHubConstants.VERSION_NUMBER + "-U", MobileCore.extensionVersion())
    }

    // / Tests that the Xamarin wrapper tag is appended
    @Test
    fun testSetWrapperTypeXamarin() {
        EventHub.shared = EventHub()
        MobileCore.setWrapperType(WrapperType.XAMARIN)
        assertEquals(EventHubConstants.VERSION_NUMBER + "-X", MobileCore.extensionVersion())
    }

    // / Tests that the log level in the Log class is updated to verbose
    @Test
    fun testSetLogLevelVerbose() {
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        assertEquals(LoggingMode.VERBOSE, com.adobe.marketing.mobile.services.Log.getLogLevel())
    }

    // MARK: setLogLevel(...) tests
    // / Tests that the log level in the Log class is updated to debug
    @Test
    fun testSetLogLevelDebug() {
        MobileCore.setLogLevel(LoggingMode.DEBUG)
        assertEquals(LoggingMode.DEBUG, com.adobe.marketing.mobile.services.Log.getLogLevel())
    }

    // / Tests that the log level in the Log class is updated to warning
    @Test
    fun testSetLogLevelWarning() {
        MobileCore.setLogLevel(LoggingMode.WARNING)
        assertEquals(LoggingMode.WARNING, com.adobe.marketing.mobile.services.Log.getLogLevel())
    }

    // / Tests that the log level in the Log class is updated to error
    @Test
    fun testSetLogLevelError() {
        MobileCore.setLogLevel(LoggingMode.ERROR)
        assertEquals(LoggingMode.ERROR, com.adobe.marketing.mobile.services.Log.getLogLevel())
    }

    // MARK: collectMessageInfo(...) tests
    // / When message info is empty no event should be dispatched
    @Test
    fun testCollectMessageInfoEmpty() {
        MobileCore.collectMessageInfo(HashMap())
        verifyNoInteractions(mockedEventHub)
    }

    // / When message info is not empty we should dispatch an event
    @Test
    fun testCollectMessageInfoWithData() {
        val messageInfo = mapOf("testKey" to "testVal")
        MobileCore.collectMessageInfo(messageInfo)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.GENERIC_DATA, eventCaptor.firstValue.type)
        assertEquals(EventSource.OS, eventCaptor.firstValue.source)
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER to "test-ad-id")
        assertEquals(messageInfo, eventCaptor.firstValue.eventData)
    }

    // MARK: collectLaunchInfo(...) tests
    // / When launch info is empty no event should be dispatched
    @Test
    fun testCollectLaunchInfoEmpty() {
        MobileCore.collectLaunchInfo(null)
        verifyNoInteractions(mockedEventHub)
    }

    // / When message info is not empty we should dispatch an event
    @Test
    fun testCollectLaunchInfoWithData() {
//        Todo
//        val marshalledData = mapOf(
//            "key" to "value"
//        )
//        // Scope of constructor mocking
//        mockConstruction(DataMarshaller::class.java).use { mock ->
//            // creating a mock instance
//            val marshaller = mock(DataMarshaller::class.java)
//            `when`(marshaller.getData()).thenReturn(marshalledData)
//        }
//
//        registerExtension(MockExtension::class.java)
//
//        val latch = CountDownLatch(1)
//        val capturedEvents = mutableListOf<Event>()
//        EventHub.shared.getExtensionContainer(MockExtension::class.java)?.registerEventListener(EventType.GENERIC_DATA, EventSource.OS) {
//                capturedEvents.add(it)
//                latch.countDown()
//            }
//        EventHub.shared.start()
//
//        MobileCore.collectLaunchInfo(mock(Activity::class.java))
//
//        assertTrue {
//            latch.await(1, TimeUnit.SECONDS)
//        }
//        assertEquals(marshalledData, capturedEvents[0].eventData)
    }

    // MARK: collectPii(...) tests
    // / When data is empty no event should be dispatched
    @Test
    fun testCollectPiiInfoEmpty() {
        MobileCore.collectPii(HashMap())
        verifyNoInteractions(mockedEventHub)
    }

    @Test
    fun testCollectPiiInfoWithData() {
        val piiInfo = mapOf("testKey" to "testVal")
        MobileCore.collectPii(piiInfo)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.GENERIC_PII, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val expectedData = mapOf(CoreConstants.EventDataKeys.Signal.SIGNAL_CONTEXT_DATA to piiInfo)
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }

    // MARK: setAdvertisingIdentifier(...) tests
    // / Tests that when setAdvertisingIdentifier is called that we dispatch an event with the advertising identifier in the event data
    @Test
    fun testSetAdvertisingIdentifierHappy() {
        // setup

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        MobileCore.setAdvertisingIdentifier("test-ad-id")
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())

        assertEquals(EventType.GENERIC_IDENTITY, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER to "test-ad-id")
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }

    // / Tests that when nil is passed to setAdvertisingId that we convert it to an empty string since swift cannot hold nil in a dict
    @Test
    fun testSetAdvertisingIdentifierNil() {
        MobileCore.setAdvertisingIdentifier(null)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.GENERIC_IDENTITY, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER to null)
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }

    // MARK: setPushIdentifier(...) tests
    // / Tests that when setPushIdentifier is called that we dispatch an event with the push identifier in the event data
    @Test
    fun testSetPushIdentifierHappy() {
        MobileCore.setPushIdentifier("test-push-id")

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.GENERIC_IDENTITY, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Identity.PUSH_IDENTIFIER to "test-push-id")
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }

    // / Tests that when setPushIdentifier is called that we dispatch an event with the push identifier in the event data and that an empty push id is handled properly
    @Test
    fun testSetPushIdentifierNil() {
        MobileCore.setPushIdentifier(null)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.GENERIC_IDENTITY, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val expectedData = mapOf(CoreConstants.EventDataKeys.Identity.PUSH_IDENTIFIER to null)
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }

    // MARK: Configuration methods
    // / Tests that a configuration request content event is dispatched with the appId
    @Test
    fun testConfigureWithAppId() {
        val appId = "test-app-id"
        MobileCore.configureWithAppID(appId)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.CONFIGURATION, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to appId)
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }

    // / Tests that a configuration request content event is dispatched with the filePath
    @Test
    fun testConfigureWithFilePath() {
        val filePath = "test-file-path"
        MobileCore.configureWithFileInPath(filePath)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.CONFIGURATION, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH to filePath)
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }

    // / Tests that a configuration request content event is dispatched with the fileAssets
    @Test
    fun testConfigureWithFileAssets() {
        val assertPath = "test-asset-path"
        MobileCore.configureWithFileInAssets(assertPath)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.CONFIGURATION, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE to assertPath)
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }

    // / Tests that a configuration request content event is dispatched with the updated dict
    @Test
    fun testUpdateConfiguration() {
        val updateDict = mapOf("testKey" to "testVal")
        MobileCore.updateConfiguration(updateDict)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.CONFIGURATION, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG to updateDict)
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }

    // / Tests that a configuration request content event is dispatched with the true value for a revert
    @Test
    fun testClearUpdateConfiguration() {
        MobileCore.clearUpdatedConfiguration()

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.CONFIGURATION, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_CLEAR_UPDATED_CONFIG to true)
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }

    // / Tests that set privacy status dispatches a configuration request content event with the new privacy status
    @Test
    fun testSetPrivacy() {
        MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_IN)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.CONFIGURATION, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val privacyDict =
            mapOf(CoreConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY to MobilePrivacyStatus.OPT_IN.value)
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG to privacyDict)
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }

    // / Tests that get privacy status dispatches an event of configuration request content with the correct retrieve config data
    @Test
    fun testGetPrivacy() {
        MobileCore.getPrivacyStatus { }

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.CONFIGURATION, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_RETRIEVE_CONFIG to true)
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }

    // / Tests that getSdkIdentities dispatches a configuration request identity event
    @Test
    fun testGetSdkIdentities() {
        MobileCore.getSdkIdentities {}

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.CONFIGURATION, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_IDENTITY, eventCaptor.firstValue.source)
    }

    // / Tests that resetIdentities dispatches an generic identity event
    @Test
    fun testResetIdentities() {
        MobileCore.resetIdentities()

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.GENERIC_IDENTITY, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_RESET, eventCaptor.firstValue.source)
    }

    // Track methods
    @Test
    fun testTrackAction() {
        val contextData = mapOf("testKey" to "testVal")
        val action = "myAction"
        MobileCore.trackAction(action, contextData)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.GENERIC_TRACK, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val expectedData = mapOf(
            CoreConstants.EventDataKeys.Analytics.TRACK_ACTION to action,
            CoreConstants.EventDataKeys.Analytics.CONTEXT_DATA to contextData
        )
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }

    @Test
    fun testTrackState() {
        val contextData = mapOf("testKey" to "testVal")
        val state = "myState"
        MobileCore.trackState(state, contextData)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.GENERIC_TRACK, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val expectedData = mapOf(
            CoreConstants.EventDataKeys.Analytics.TRACK_STATE to state,
            CoreConstants.EventDataKeys.Analytics.CONTEXT_DATA to contextData
        )
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }

    // Lifecycle methods
    @Test
    fun testLifecycleStart() {
        val contextData = mapOf("testKey" to "testVal")
        MobileCore.lifecycleStart(contextData)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.GENERIC_LIFECYCLE, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val expectedData = mapOf(
            CoreConstants.EventDataKeys.Lifecycle.LIFECYCLE_ACTION_KEY to CoreConstants.EventDataKeys.Lifecycle.LIFECYCLE_START,
            CoreConstants.EventDataKeys.Lifecycle.ADDITIONAL_CONTEXT_DATA to contextData
        )
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }

    @Test
    fun testLifecyclePause() {
        MobileCore.lifecyclePause()

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockedEventHub, times(1)).dispatch(eventCaptor.capture())
        assertEquals(EventType.GENERIC_LIFECYCLE, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_CONTENT, eventCaptor.firstValue.source)
        val expectedData = mapOf(
            CoreConstants.EventDataKeys.Lifecycle.LIFECYCLE_ACTION_KEY to CoreConstants.EventDataKeys.Lifecycle.LIFECYCLE_PAUSE
        )
        assertEquals(expectedData, eventCaptor.firstValue.eventData)
    }
}
