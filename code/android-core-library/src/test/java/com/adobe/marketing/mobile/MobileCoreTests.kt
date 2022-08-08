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

import android.app.Application
import com.adobe.marketing.mobile.internal.eventhub.EventHub
import com.adobe.marketing.mobile.internal.eventhub.EventHubConstants
import com.adobe.marketing.mobile.internal.eventhub.EventHubError
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MobileCoreTests {

    class MockExtension(extensionApi: ExtensionApi) : Extension(extensionApi) {
        companion object {
            var registrationClosure: (() -> Unit)? = null
            var unregistrationClosure: (() -> Unit)? = null
            var eventReceivedClosure: ((Event) -> Unit)? = null

            fun reset() {
                registrationClosure = null
                unregistrationClosure = null
                eventReceivedClosure = null
            }
        }

        override fun getName(): String = "MockExtension"

        override fun onRegistered() {
            api.registerEventListener(EventType.TYPE_WILDCARD, EventSource.TYPE_WILDCARD) {
                eventReceivedClosure?.invoke(it)
            }

            registrationClosure?.invoke()
        }

        override fun onUnregistered() {
            unregistrationClosure?.invoke()
        }
    }

    class MockExtension2(extensionApi: ExtensionApi) : Extension(extensionApi) {
        companion object {
            var registrationClosure: (() -> Unit)? = null
            var unregistrationClosure: (() -> Unit)? = null
            var eventReceivedClosure: ((Event) -> Unit)? = null

            fun reset() {
                registrationClosure = null
                unregistrationClosure = null
                eventReceivedClosure = null
            }
        }

        override fun getName(): String = "MockExtension2"

        override fun onRegistered() {
            api.registerEventListener(EventType.TYPE_WILDCARD, EventSource.TYPE_WILDCARD) {
                eventReceivedClosure?.invoke(it)
            }

            registrationClosure?.invoke()
        }

        override fun onUnregistered() {
            unregistrationClosure?.invoke()
        }
    }

    class MockExtensionWithSlowInit(extensionApi: ExtensionApi) : Extension(extensionApi) {
        companion object {
            var initWaitTimeMS: Long = 0
            var registrationClosure: (() -> Unit)? = null
        }

        init {
            Thread.sleep(initWaitTimeMS)
        }

        override fun getName(): String = "SlowMockExtension"

        override fun onRegistered() {
            registrationClosure?.invoke()
        }
    }

    @Before
    fun setup() {
        MockExtension.reset()
        EventHub.shared = EventHub()
        MobileCore.sdkInitializedWithContext = AtomicBoolean(false)
    }

    @After
    fun teardown() {
        EventHub.shared.shutdown()
    }

    private fun registerExtension(extensionClass: Class<out Extension>): EventHubError {
        var ret: EventHubError = EventHubError.Unknown

        val latch = CountDownLatch(1)
        EventHub.shared.registerExtension(extensionClass) { error ->
            ret = error
            latch.countDown()
        }
        if (!latch.await(1, TimeUnit.SECONDS)) throw Exception("Timeout registering extension")
        return ret
    }

    @Test
    fun testRegisterExtensionsSimple() {
        val latch = CountDownLatch(1)
        MockExtension.registrationClosure = { latch.countDown() }

        MobileCore.setApplication(mock(Application::class.java))
        MobileCore.registerExtension(MockExtension::class.java) {}

        assertTrue { latch.await(1, TimeUnit.SECONDS) }
    }

    @Test
    fun testRegisterExtensionsSimpleMultiple() {
        val latch = CountDownLatch(2)
        MockExtension.registrationClosure = { latch.countDown() }
        MockExtension2.registrationClosure = { latch.countDown() }

        MobileCore.setApplication(mock(Application::class.java))
        MobileCore.registerExtension(MockExtension::class.java) {}
        MobileCore.registerExtension(MockExtension2::class.java) {}

        assertTrue { latch.await(1, TimeUnit.SECONDS) }
    }

    @Test
    fun testRegisterExtensionsWithSlowExtension() {
        val latch = CountDownLatch(2)
        MockExtension.registrationClosure = { latch.countDown() }
        MockExtension2.registrationClosure = { latch.countDown() }

        MockExtensionWithSlowInit.initWaitTimeMS = 2000

        MobileCore.setApplication(mock(Application::class.java))
        MobileCore.registerExtension(MockExtensionWithSlowInit::class.java) {}
        MobileCore.registerExtension(MockExtension::class.java) {}
        MobileCore.registerExtension(MockExtension2::class.java) {}

        assertTrue { latch.await(1, TimeUnit.SECONDS) }
    }

    @Test
    fun testRegisterExtensionsSimpleEventDispatch() {
        val latch = CountDownLatch(1)
        MockExtension.eventReceivedClosure = {
            if (it.name == "test-event") {
                latch.countDown()
            }
        }

        MobileCore.setApplication(mock(Application::class.java))
        MobileCore.registerExtension(MockExtension::class.java) {}
        MobileCore.start {}

        val event = Event.Builder("test-event", "analytics", "requestContent").build()
        MobileCore.dispatchEvent(event)
        assertTrue { latch.await(1, TimeUnit.SECONDS) }
    }

    @Test
    fun testRegisterExtensionsDispatchEventBeforeRegister() {
        val latch = CountDownLatch(1)
        MockExtension.eventReceivedClosure = {
            if (it.name == "test-event") {
                latch.countDown()
            }
        }

        val event = Event.Builder("test-event", "analytics", "requestContent").build()
        MobileCore.dispatchEvent(event)

        MobileCore.setApplication(mock(Application::class.java))
        MobileCore.registerExtension(MockExtension::class.java) {}
        MobileCore.start {}

        assertTrue { latch.await(1, TimeUnit.SECONDS) }
    }

    @Test
    fun testRegisterMultipleExtensionsSimpleEventDispatch() {
        val latch = CountDownLatch(2)
        MockExtension.eventReceivedClosure = {
            if (it.name == "test-event") {
                latch.countDown()
            }
        }
        MockExtension2.eventReceivedClosure = {
            if (it.name == "test-event") {
                latch.countDown()
            }
        }

        MobileCore.setApplication(mock(Application::class.java))
        MobileCore.registerExtension(MockExtension::class.java) {}
        MobileCore.registerExtension(MockExtension2::class.java) {}
        MobileCore.start {}

        val event = Event.Builder("test-event", "analytics", "requestContent").build()
        MobileCore.dispatchEvent(event)

        assertTrue { latch.await(1, TimeUnit.SECONDS) }
    }

    @Test
    fun testRegisterMultipleExtensionsDispatchEventBeforeRegister() {
        val latch = CountDownLatch(2)
        MockExtension.eventReceivedClosure = {
            if (it.name == "test-event") {
                latch.countDown()
            }
        }
        MockExtension2.eventReceivedClosure = {
            if (it.name == "test-event") {
                latch.countDown()
            }
        }

        val event = Event.Builder("test-event", "analytics", "requestContent").build()
        MobileCore.dispatchEvent(event)

        MobileCore.setApplication(mock(Application::class.java))
        MobileCore.registerExtension(MockExtension::class.java) {}
        MobileCore.registerExtension(MockExtension2::class.java) {}
        MobileCore.start {}

        assertTrue { latch.await(10, TimeUnit.SECONDS) }
    }

    @Test
    fun testRegisterSameExtensionTwice() {
        val capturedErrors = mutableListOf<ExtensionError>()

        MobileCore.setApplication(mock(Application::class.java))
        MobileCore.registerExtension(MockExtension::class.java) {
            capturedErrors.add(it)
        }
        MobileCore.registerExtension(MockExtension::class.java) {
            capturedErrors.add(it)
        }

        Thread.sleep(500)
        assertEquals(mutableListOf(ExtensionError.DUPLICATE_NAME), capturedErrors)
    }

    @Test
    fun testDispatchEventSimple() {
        val event = Event.Builder("test", "analytics", "requestContent").build()

        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        registerExtension(MockExtension::class.java)
        EventHub.shared.getExtensionContainer(MockExtension::class.java)
            ?.registerEventListener(event.type, event.source) {
                capturedEvents.add(it)
                latch.countDown()
            }

        EventHub.shared.start()

        // test
        MobileCore.dispatchEvent(event)

        assertTrue { latch.await(1, TimeUnit.SECONDS) }
        assertEquals(event, capturedEvents[0])
    }

    // / Tests that the response callback is invoked when the trigger event is dispatched
    @Test
    fun testDispatchEventWithResponseCallbackSimple() {
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
        // setup
        val event1 = Event.Builder("test", "analytics", "requestContent").build()
        val event2 = Event.Builder("test", "analytics", "requestContent").build()
        val unexpectedEvent = Event.Builder("", "wrong", "wrong").build()

        EventHub.shared.start()

        val latch = CountDownLatch(2)
        val capturedEvents = mutableListOf<Event>()

        MobileCore.registerEventListener(
            "analytics", "requestContent",
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
        // setup
        val event1 = Event.Builder("test", "analytics", "requestContent").build()
        val event2 = Event.Builder("test", "analytics", "requestContent").build()
        val unexpectedEvent = Event.Builder("", "wrong", "wrong").build()

        EventHub.shared.start()

        val latch1 = CountDownLatch(2)
        val capturedEvents1 = mutableListOf<Event>()
        MobileCore.registerEventListener(
            "analytics", "requestContent",
            object : AdobeCallbackWithError<Event> {
                override fun call(value: Event) {
                    capturedEvents1.add(value)
                    latch1.countDown()
                }

                override fun fail(error: AdobeError?) {
                    latch1.countDown()
                }
            }
        )

        val latch2 = CountDownLatch(2)
        val capturedEvents2 = mutableListOf<Event>()
        MobileCore.registerEventListener(
            "analytics", "requestContent",
            object : AdobeCallbackWithError<Event> {
                override fun call(value: Event) {
                    capturedEvents2.add(value)
                    latch2.countDown()
                }

                override fun fail(error: AdobeError?) {
                    latch2.countDown()
                }
            }
        )

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
        assertEquals(EventHubConstants.VERSION_NUMBER, MobileCore.extensionVersion())
    }

    // Tests that no wrapper tag is appended when the wrapper type is none
    @Test
    fun testSetWrapperTypeNone() {
        MobileCore.setWrapperType(WrapperType.NONE)
        assertEquals(EventHubConstants.VERSION_NUMBER, MobileCore.extensionVersion())
    }

    // / Tests that the React Native wrapper tag is appended
    @Test
    fun testSetWrapperTypeReactNative() {
        MobileCore.setWrapperType(WrapperType.REACT_NATIVE)
        assertEquals(EventHubConstants.VERSION_NUMBER + "-R", MobileCore.extensionVersion())
    }

    // / Tests that the Flutter wrapper tag is appended
    @Test
    fun testSetWrapperTypeFlutter() {
        MobileCore.setWrapperType(WrapperType.FLUTTER)
        assertEquals(EventHubConstants.VERSION_NUMBER + "-F", MobileCore.extensionVersion())
    }

    // / Tests that the Cordova wrapper tag is appended
    @Test
    fun testSetWrapperTypeCordova() {
        MobileCore.setWrapperType(WrapperType.CORDOVA)
        assertEquals(EventHubConstants.VERSION_NUMBER + "-C", MobileCore.extensionVersion())
    }

    // / Tests that the Unity wrapper tag is appended
    @Test
    fun testSetWrapperTypeUnity() {
        MobileCore.setWrapperType(WrapperType.UNITY)
        assertEquals(EventHubConstants.VERSION_NUMBER + "-U", MobileCore.extensionVersion())
    }

    // / Tests that the Xamarin wrapper tag is appended
    @Test
    fun testSetWrapperTypeXamarin() {
        MobileCore.setWrapperType(WrapperType.XAMARIN)
        assertEquals(EventHubConstants.VERSION_NUMBER + "-X", MobileCore.extensionVersion())
    }

    // / Tests that the log level in the Log class is updated to verbose
    @Test
    fun testSetLogLevelVerbose() {
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        assertEquals(LoggingMode.VERBOSE, Log.getLogLevel())
    }

    // MARK: setLogLevel(...) tests
    // / Tests that the log level in the Log class is updated to debug
    @Test
    fun testSetLogLevelDebug() {
        MobileCore.setLogLevel(LoggingMode.DEBUG)
        assertEquals(LoggingMode.DEBUG, Log.getLogLevel())
    }

    // / Tests that the log level in the Log class is updated to warning
    @Test
    fun testSetLogLevelWarning() {
        MobileCore.setLogLevel(LoggingMode.WARNING)
        assertEquals(LoggingMode.WARNING, Log.getLogLevel())
    }

    // / Tests that the log level in the Log class is updated to error
    @Test
    fun testSetLogLevelError() {
        MobileCore.setLogLevel(LoggingMode.ERROR)
        assertEquals(LoggingMode.ERROR, Log.getLogLevel())
    }

    // MARK: collectMessageInfo(...) tests
    // / When message info is empty no event should be dispatched
    @Test
    fun testCollectMessageInfoEmpty() {
        registerExtension(MockExtension::class.java)

        val latch = CountDownLatch(1)
        EventHub.shared.getExtensionContainer(MockExtension::class.java)
            ?.registerEventListener(EventType.TYPE_GENERIC_DATA, EventSource.TYPE_OS) {
                latch.countDown()
            }
        EventHub.shared.start()

        MobileCore.collectMessageInfo(HashMap())

        assertFalse {
            latch.await(1, TimeUnit.SECONDS)
        }
    }

    // / When message info is not empty we should dispatch an event
    @Test
    fun testCollectMessageInfoWithData() {
        // setup
        registerExtension(MockExtension::class.java)
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)
            ?.registerEventListener(EventType.TYPE_GENERIC_DATA, EventSource.TYPE_OS) {
                capturedEvents.add(it)
                latch.countDown()
            }
        EventHub.shared.start()

        val messageInfo = mapOf("testKey" to "testVal")
        MobileCore.collectMessageInfo(messageInfo)

        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }
        assertEquals(messageInfo, capturedEvents[0].eventData)
    }

    // MARK: collectLaunchInfo(...) tests
    // / When launch info is empty no event should be dispatched
    @Test
    fun testCollectLaunchInfoEmpty() {
        registerExtension(MockExtension::class.java)

        val latch = CountDownLatch(1)
        EventHub.shared.getExtensionContainer(MockExtension::class.java)
            ?.registerEventListener(EventType.TYPE_GENERIC_DATA, EventSource.TYPE_OS) {
                latch.countDown()
            }
        EventHub.shared.start()

        MobileCore.collectLaunchInfo(null)

        assertFalse {
            latch.await(1, TimeUnit.SECONDS)
        }
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
//        EventHub.shared.getExtensionContainer(MockExtension::class.java)?.registerEventListener(EventType.TYPE_GENERIC_DATA, EventSource.TYPE_OS) {
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
        registerExtension(MockExtension::class.java)

        val latch = CountDownLatch(1)
        EventHub.shared.getExtensionContainer(MockExtension::class.java)
            ?.registerEventListener(EventType.TYPE_GENERIC_DATA, EventSource.TYPE_OS) {
                latch.countDown()
            }
        EventHub.shared.start()

        MobileCore.collectPii(HashMap())

        assertFalse {
            latch.await(1, TimeUnit.SECONDS)
        }
    }

    @Test
    fun testCollectPiiInfoWithData() {
        // setup
        registerExtension(MockExtension::class.java)
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)
            ?.registerEventListener(EventType.TYPE_GENERIC_PII, EventSource.TYPE_REQUEST_CONTENT) {
                capturedEvents.add(it)
                latch.countDown()
            }
        EventHub.shared.start()

        val piiInfo = mapOf("testKey" to "testVal")
        MobileCore.collectPii(piiInfo)

        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }
        val expectedData = mapOf(CoreConstants.EventDataKeys.Signal.SIGNAL_CONTEXT_DATA to piiInfo)
        assertEquals(expectedData, capturedEvents[0].eventData)
    }

    // MARK: setAdvertisingIdentifier(...) tests
    // / Tests that when setAdvertisingIdentifier is called that we dispatch an event with the advertising identifier in the event data
    @Test
    fun testSetAdvertisingIdentifierHappy() {
        // setup
        registerExtension(MockExtension::class.java)
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)
            ?.registerEventListener(
                EventType.TYPE_GENERIC_IDENTITY,
                EventSource.TYPE_REQUEST_CONTENT
            ) {
                capturedEvents.add(it)
                latch.countDown()
            }
        EventHub.shared.start()

        MobileCore.setAdvertisingIdentifier("test-ad-id")

        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER to "test-ad-id")
        assertEquals(expectedData, capturedEvents[0].eventData)
    }

    // / Tests that when nil is passed to setAdvertisingId that we convert it to an empty string since swift cannot hold nil in a dict
    @Test
    fun testSetAdvertisingIdentifierNil() {
        // setup
        registerExtension(MockExtension::class.java)
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)
            ?.registerEventListener(
                EventType.TYPE_GENERIC_IDENTITY,
                EventSource.TYPE_REQUEST_CONTENT
            ) {
                capturedEvents.add(it)
                latch.countDown()
            }
        EventHub.shared.start()

        MobileCore.setAdvertisingIdentifier(null)

        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER to null)
        assertEquals(expectedData, capturedEvents[0].eventData)
    }

    // MARK: setPushIdentifier(...) tests
    // / Tests that when setPushIdentifier is called that we dispatch an event with the push identifier in the event data
    @Test
    fun testSetPushIdentifierHappy() {
        // setup
        registerExtension(MockExtension::class.java)
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)
            ?.registerEventListener(
                EventType.TYPE_GENERIC_IDENTITY,
                EventSource.TYPE_REQUEST_CONTENT
            ) {
                capturedEvents.add(it)
                latch.countDown()
            }
        EventHub.shared.start()

        MobileCore.setPushIdentifier("test-push-id")

        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Identity.PUSH_IDENTIFIER to "test-push-id")
        assertEquals(expectedData, capturedEvents[0].eventData)
    }

    // / Tests that when setPushIdentifier is called that we dispatch an event with the push identifier in the event data and that an empty push id is handled properly
    @Test
    fun testSetPushIdentifierNil() {
        // setup
        registerExtension(MockExtension::class.java)
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)
            ?.registerEventListener(
                EventType.TYPE_GENERIC_IDENTITY,
                EventSource.TYPE_REQUEST_CONTENT
            ) {
                capturedEvents.add(it)
                latch.countDown()
            }
        EventHub.shared.start()

        MobileCore.setPushIdentifier(null)

        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }
        val expectedData = mapOf(CoreConstants.EventDataKeys.Identity.PUSH_IDENTIFIER to null)
        assertEquals(expectedData, capturedEvents[0].eventData)
    }

    // MARK: Configuration methods
    // / Tests that a configuration request content event is dispatched with the appId
    @Test
    fun testConfigureWithAppId() {
        // setup
        registerExtension(MockExtension::class.java)
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)?.registerEventListener(
            EventType.TYPE_CONFIGURATION,
            EventSource.TYPE_REQUEST_CONTENT
        ) {
            capturedEvents.add(it)
            latch.countDown()
        }
        EventHub.shared.start()

        val appId = "test-app-id"
        MobileCore.configureWithAppID(appId)

        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to appId)
        assertEquals(expectedData, capturedEvents[0].eventData)
    }

    // / Tests that a configuration request content event is dispatched with the filePath
    @Test
    fun testConfigureWithFilePath() {
        // setup
        registerExtension(MockExtension::class.java)
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)?.registerEventListener(
            EventType.TYPE_CONFIGURATION,
            EventSource.TYPE_REQUEST_CONTENT
        ) {
            capturedEvents.add(it)
            latch.countDown()
        }
        EventHub.shared.start()

        val filePath = "test-file-path"
        MobileCore.configureWithFileInPath(filePath)

        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH to filePath)
        assertEquals(expectedData, capturedEvents[0].eventData)
    }

    // / Tests that a configuration request content event is dispatched with the fileAssets
    @Test
    fun testConfigureWithFileAssets() {
        // setup
        registerExtension(MockExtension::class.java)
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)?.registerEventListener(
            EventType.TYPE_CONFIGURATION,
            EventSource.TYPE_REQUEST_CONTENT
        ) {
            capturedEvents.add(it)
            latch.countDown()
        }
        EventHub.shared.start()

        val assertPath = "test-asset-path"
        MobileCore.configureWithFileInAssets(assertPath)

        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE to assertPath)
        assertEquals(expectedData, capturedEvents[0].eventData)
    }

    // / Tests that a configuration request content event is dispatched with the updated dict
    @Test
    fun testUpdateConfiguration() {
        // setup
        registerExtension(MockExtension::class.java)
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)?.registerEventListener(
            EventType.TYPE_CONFIGURATION,
            EventSource.TYPE_REQUEST_CONTENT
        ) {
            capturedEvents.add(it)
            latch.countDown()
        }
        EventHub.shared.start()

        val updateDict = mapOf("testKey" to "testVal")
        MobileCore.updateConfiguration(updateDict)

        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG to updateDict)
        assertEquals(expectedData, capturedEvents[0].eventData)
    }

    // / Tests that a configuration request content event is dispatched with the true value for a revert
    @Test
    fun testClearUpdateConfiguration() {
        // setup
        registerExtension(MockExtension::class.java)
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)?.registerEventListener(
            EventType.TYPE_CONFIGURATION,
            EventSource.TYPE_REQUEST_CONTENT
        ) {
            capturedEvents.add(it)
            latch.countDown()
        }
        EventHub.shared.start()

        MobileCore.clearUpdatedConfiguration()

        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_CLEAR_UPDATED_CONFIG to true)
        assertEquals(expectedData, capturedEvents[0].eventData)
    }

    // / Tests that set privacy status dispatches a configuration request content event with the new privacy status
    @Test
    fun testSetPrivacy() {
        // setup
        registerExtension(MockExtension::class.java)
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)?.registerEventListener(
            EventType.TYPE_CONFIGURATION,
            EventSource.TYPE_REQUEST_CONTENT
        ) {
            capturedEvents.add(it)
            latch.countDown()
        }
        EventHub.shared.start()

        MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_IN)

        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }
        val privacyDict =
            mapOf(CoreConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY to MobilePrivacyStatus.OPT_IN.value)
        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG to privacyDict)
        assertEquals(expectedData, capturedEvents[0].eventData)
    }

    // / Tests that get privacy status dispatches an event of configuration request content with the correct retrieve config data
    @Test
    fun testGetPrivacy() {
        // setup
        registerExtension(MockExtension::class.java)
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)?.registerEventListener(
            EventType.TYPE_CONFIGURATION,
            EventSource.TYPE_REQUEST_CONTENT
        ) {
            capturedEvents.add(it)
            latch.countDown()
        }
        EventHub.shared.start()

        MobileCore.getPrivacyStatus { }

        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }

        val expectedData =
            mapOf(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_RETRIEVE_CONFIG to true)
        assertEquals(expectedData, capturedEvents[0].eventData)
    }

    // / Tests that getSdkIdentities dispatches a configuration request identity event
    @Test
    fun testGetSdkIdentities() {
        // setup
        registerExtension(MockExtension::class.java)
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)?.registerEventListener(
            EventType.TYPE_CONFIGURATION,
            EventSource.TYPE_REQUEST_IDENTITY
        ) {
            capturedEvents.add(it)
            latch.countDown()
        }
        EventHub.shared.start()

        MobileCore.getSdkIdentities {}

        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }
    }

    // / Tests that resetIdentities dispatches an generic identity event
    @Test
    fun testResetIdentities() {
        // setup
        registerExtension(MockExtension::class.java)
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)?.registerEventListener(
            EventType.TYPE_GENERIC_IDENTITY,
            EventSource.TYPE_REQUEST_RESET
        ) {
            capturedEvents.add(it)
            latch.countDown()
        }
        EventHub.shared.start()

        MobileCore.resetIdentities()

        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }

        assertEquals(capturedEvents.size, 1)
    }

    // Track methods
    @Test
    fun testTrackAction() {
        registerExtension(MockExtension::class.java)

        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)?.registerEventListener(
            EventType.TYPE_GENERIC_TRACK,
            EventSource.TYPE_REQUEST_CONTENT
        ) {
            capturedEvents.add(it)
            latch.countDown()
        }
        EventHub.shared.start()

        val contextData = mapOf("testKey" to "testVal")
        val action = "myAction"

        // test
        MobileCore.trackAction(action, contextData)
        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }

        val expectedData = mapOf(
            CoreConstants.EventDataKeys.Analytics.TRACK_ACTION to action,
            CoreConstants.EventDataKeys.Analytics.CONTEXT_DATA to contextData,
        )
        assertEquals(expectedData, capturedEvents[0].eventData)
    }

    @Test
    fun testTrackState() {
        registerExtension(MockExtension::class.java)

        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Event>()
        EventHub.shared.getExtensionContainer(MockExtension::class.java)?.registerEventListener(
            EventType.TYPE_GENERIC_TRACK,
            EventSource.TYPE_REQUEST_CONTENT
        ) {
            capturedEvents.add(it)
            latch.countDown()
        }
        EventHub.shared.start()

        val contextData = mapOf("testKey" to "testVal")
        val state = "myState"

        // test
        MobileCore.trackState(state, contextData)
        assertTrue {
            latch.await(1, TimeUnit.SECONDS)
        }

        val expectedData = mapOf(
            CoreConstants.EventDataKeys.Analytics.TRACK_STATE to state,
            CoreConstants.EventDataKeys.Analytics.CONTEXT_DATA to contextData,
        )
        assertEquals(expectedData, capturedEvents[0].eventData)
    }
}
