// TODO: update this test after another PR is merged in: https://github.com/adobe/aepsdk-core-android/pull/149
// /*
//  Copyright 2022 Adobe. All rights reserved.
//  This file is licensed to you under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License. You may obtain a copy
//  of the License at http://www.apache.org/licenses/LICENSE-2.0
//  Unless required by applicable law or agreed to in writing, software distributed under
//  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
//  OF ANY KIND, either express or implied. See the License for the specific language
//  governing permissions and limitations under the License.
// */
// package com.adobe.marketing.mobile
//
// import android.app.Application
// import org.junit.After
// import org.junit.Before
// import org.junit.Ignore
// import org.junit.Test
// import org.junit.runner.RunWith
// import org.mockito.ArgumentCaptor
// import org.mockito.ArgumentMatchers.any
// import org.mockito.Mock
// import org.mockito.Mockito
// import org.powermock.api.mockito.PowerMockito
// import org.powermock.core.classloader.annotations.PrepareForTest
// import org.powermock.modules.junit4.PowerMockRunner
// import org.powermock.reflect.Whitebox
// import kotlin.test.assertEquals
// import kotlin.test.assertNotNull
// import kotlin.test.assertNull
//
// @RunWith(PowerMockRunner::class)
// @PrepareForTest(DataMarshaller::class, MobileCore::class)
// class MobileCoreTests {
//
//    @Mock
//    private lateinit var eventHub: EventHub
//
//    @Mock
//    private lateinit var application: Application
//
//    @Mock
//    private lateinit var loggingService: LoggingService
//
//    @Mock
//    private lateinit var extensionErrorCallback: ExtensionErrorCallback<ExtensionError>
//
//    @Mock
//    private lateinit var adobeCallback: AdobeCallback<Event>
//
//    @Mock
//    private lateinit var adobeCallbackWithError: AdobeCallbackWithError<Event>
//
//    private lateinit var dataMarshaller: DataMarshaller
//
//    @Before
//    fun setup() {
//        Mockito.reset(eventHub)
//        Mockito.reset(application)
//        Mockito.reset(loggingService)
//        Mockito.reset(extensionErrorCallback)
//        Mockito.reset(adobeCallback)
//        Mockito.reset(adobeCallbackWithError)
//        Whitebox.setInternalState(MobileCore::class.java, "eventHub", eventHub)
//        Whitebox.setInternalState(MobileCore::class.java, "startActionCalled", false)
//        dataMarshaller = PowerMockito.mock(DataMarshaller::class.java)
//        PowerMockito.mock(MobileCore::class.java)
//    }
//
//    @After
//    fun teardown() {
//    }
//
//    @Test
//    fun `test TrackState()`() {
//        MobileCore.trackState(
//            "state",
//            mapOf(
//                "key" to "value"
//            )
//        )
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("Analytics Track", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.generic.track", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(
//            mapOf(
//                "state" to "state",
//                "contextdata" to mapOf(
//                    "key" to "value"
//                )
//            ),
//            dispatchedEvent.eventData
//        )
//    }
//
//    @Test
//    fun `test TrackState() with no ContextData`() {
//        MobileCore.trackState("state", null)
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("Analytics Track", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.generic.track", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(
//            mapOf(
//                "state" to "state",
//                "contextdata" to emptyMap<String, Any>()
//            ),
//            dispatchedEvent.eventData
//        )
//    }
//
//    @Test
//    fun `test trackAction()`() {
//        MobileCore.trackAction(
//            "action",
//            mapOf(
//                "key" to "value"
//            )
//        )
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("Analytics Track", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.generic.track", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(
//            mapOf(
//                "action" to "action",
//                "contextdata" to mapOf(
//                    "key" to "value"
//                )
//            ),
//            dispatchedEvent.eventData
//        )
//    }
//
//    @Test
//    fun `test trackAction() with no ContextData`() {
//        MobileCore.trackAction("action", null)
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("Analytics Track", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.generic.track", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(
//            mapOf(
//                "action" to "action",
//                "contextdata" to emptyMap<String, Any>()
//            ),
//            dispatchedEvent.eventData
//        )
//    }
//
//    @Test
//    fun `test collectPii()`() {
//        MobileCore.collectPii(
//            mapOf(
//                "key" to "value"
//            )
//        )
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("CollectPII", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.generic.pii", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(
//            mapOf(
//                "contextdata" to mapOf(
//                    "key" to "value"
//                )
//            ),
//            dispatchedEvent.eventData
//        )
//    }
//
//    @Test
//    fun `test collectPii() without data`() {
//        MobileCore.collectPii(null)
//        Mockito.verify(eventHub, Mockito.times(0)).dispatch(any())
//    }
//
//    @Test
//    fun `test setAdvertisingIdentifier()`() {
//        MobileCore.setAdvertisingIdentifier("advid")
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("SetAdvertisingIdentifier", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.generic.identity", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(
//            mapOf(
//                "advertisingidentifier" to "advid"
//            ),
//            dispatchedEvent.eventData
//        )
//    }
//
//    @Test
//    fun `test setPushIdentifier()`() {
//        MobileCore.setAdvertisingIdentifier("pushid")
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("SetAdvertisingIdentifier", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.generic.identity", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(
//            mapOf(
//                "advertisingidentifier" to "pushid"
//            ),
//            dispatchedEvent.eventData
//        )
//    }
//
//    @Test
//    fun `test lifecycleStart()`() {
//        MobileCore.lifecycleStart(
//            mapOf(
//                "key" to "value"
//            )
//        )
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("LifecycleResume", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.generic.lifecycle", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(
//
//            mapOf(
//                "action" to "start",
//                "additionalcontextdata" to mapOf(
//                    "key" to "value"
//                )
//            ),
//            dispatchedEvent.eventData
//        )
//    }
//
//    @Test
//    fun `test lifecyclePause()`() {
//        MobileCore.lifecyclePause()
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("LifecyclePause", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.generic.lifecycle", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(
//            mapOf(
//                "action" to "pause"
//            ),
//            dispatchedEvent.eventData
//        )
//    }
//
//    @Test
//    fun `test setApplication()`() {
//        MobileCore.setApplication(application)
//        // TODO: xxxx
//    }
//
//    @Test
//    fun `test setLogLevel() & getLogLevel()`() {
//        MobileCore.setLogLevel(LoggingMode.ERROR)
//        assertEquals(LoggingMode.ERROR, MobileCore.getLogLevel())
//        MobileCore.setLogLevel(LoggingMode.WARNING)
//        assertEquals(LoggingMode.WARNING, MobileCore.getLogLevel())
//        MobileCore.setLogLevel(LoggingMode.DEBUG)
//        assertEquals(LoggingMode.DEBUG, MobileCore.getLogLevel())
//        MobileCore.setLogLevel(LoggingMode.VERBOSE)
//        assertEquals(LoggingMode.VERBOSE, MobileCore.getLogLevel())
//    }
//
//    @Test
//    fun `test log() - VERBOSE`() {
//        MobileCore.setLogLevel(LoggingMode.VERBOSE)
//        val logTag = "log_tag"
//        Log.setLoggingService(loggingService)
//        MobileCore.log(LoggingMode.VERBOSE, logTag, "verbose logs")
//        val logCaptor = ArgumentCaptor.forClass(
//            String::class.java
//        )
//        val tagCaptor = ArgumentCaptor.forClass(
//            String::class.java
//        )
//        Mockito.verify(loggingService, Mockito.times(1))
//            .trace(tagCaptor.capture(), logCaptor.capture())
//        assertEquals(logTag, tagCaptor.value)
//        assertEquals("verbose logs", logCaptor.value)
//    }
//
//    @Test
//    fun `test log() - DEBUG`() {
//        MobileCore.setLogLevel(LoggingMode.VERBOSE)
//        val logTag = "log_tag"
//        Log.setLoggingService(loggingService)
//        MobileCore.log(LoggingMode.DEBUG, logTag, "debug logs")
//        val logCaptor = ArgumentCaptor.forClass(
//            String::class.java
//        )
//        val tagCaptor = ArgumentCaptor.forClass(
//            String::class.java
//        )
//        Mockito.verify(loggingService, Mockito.times(1))
//            .debug(tagCaptor.capture(), logCaptor.capture())
//        assertEquals(logTag, tagCaptor.value)
//        assertEquals("debug logs", logCaptor.value)
//    }
//
//    @Test
//    fun `test log() - WARNING`() {
//        MobileCore.setLogLevel(LoggingMode.VERBOSE)
//        val logTag = "log_tag"
//        Log.setLoggingService(loggingService)
//        MobileCore.log(LoggingMode.WARNING, logTag, "warning logs")
//        val logCaptor = ArgumentCaptor.forClass(
//            String::class.java
//        )
//        val tagCaptor = ArgumentCaptor.forClass(
//            String::class.java
//        )
//        Mockito.verify(loggingService, Mockito.times(1))
//            .warning(tagCaptor.capture(), logCaptor.capture())
//        assertEquals(logTag, tagCaptor.value)
//        assertEquals("warning logs", logCaptor.value)
//    }
//
//    @Test
//    fun `test log() - ERROR`() {
//        MobileCore.setLogLevel(LoggingMode.VERBOSE)
//        val logTag = "log_tag"
//        Log.setLoggingService(loggingService)
//        MobileCore.log(LoggingMode.ERROR, logTag, "error logs")
//        val logCaptor = ArgumentCaptor.forClass(
//            String::class.java
//        )
//        val tagCaptor = ArgumentCaptor.forClass(
//            String::class.java
//        )
//        Mockito.verify(loggingService, Mockito.times(1))
//            .error(tagCaptor.capture(), logCaptor.capture())
//        assertEquals(logTag, tagCaptor.value)
//        assertEquals("error logs", logCaptor.value)
//    }
//
//    @Test
//    fun `test log() with filtered out logs`() {
//        MobileCore.setLogLevel(LoggingMode.ERROR)
//        val logTag = "log_tag"
//        Log.setLoggingService(loggingService)
//        MobileCore.log(LoggingMode.VERBOSE, logTag, "verbose logs")
//        MobileCore.log(LoggingMode.DEBUG, logTag, "debug logs")
//        MobileCore.log(LoggingMode.WARNING, logTag, "verbose logs")
//        MobileCore.log(LoggingMode.ERROR, logTag, "error logs")
//        Mockito.verify(loggingService, Mockito.times(0)).trace(any(), any())
//        Mockito.verify(loggingService, Mockito.times(0)).debug(any(), any())
//        Mockito.verify(loggingService, Mockito.times(0)).warning(any(), any())
//        val logCaptor = ArgumentCaptor.forClass(
//            String::class.java
//        )
//        val tagCaptor = ArgumentCaptor.forClass(
//            String::class.java
//        )
//        Mockito.verify(loggingService, Mockito.times(1))
//            .error(tagCaptor.capture(), logCaptor.capture())
//        assertEquals(logTag, tagCaptor.value)
//        assertEquals("error logs", logCaptor.value)
//    }
//
//    @Test
//    fun `test log() without logging acceptor`() {
//        MobileCore.setLogLevel(LoggingMode.ERROR)
//        val logTag = "log_tag"
//        Log.setLoggingService(null)
//        MobileCore.log(LoggingMode.VERBOSE, logTag, "verbose logs")
//        MobileCore.log(LoggingMode.DEBUG, logTag, "debug logs")
//        MobileCore.log(LoggingMode.WARNING, logTag, "verbose logs")
//        MobileCore.log(LoggingMode.ERROR, logTag, "error logs")
//    }
//
//    @Test
//    fun `test extensionVersion()`() {
//        MobileCore.setWrapperType(WrapperType.NONE)
//        MobileCore.extensionVersion()
//        Mockito.verify(eventHub, Mockito.times(1)).sdkVersion
//    }
//
//    @Test
//    fun `test extensionVersion() - RN`() {
//        MobileCore.setWrapperType(WrapperType.REACT_NATIVE)
//        MobileCore.extensionVersion()
//        val captor = ArgumentCaptor.forClass(
//            WrapperType::class.java
//        )
//        Mockito.verify(eventHub, Mockito.times(1)).wrapperType = captor.capture()
//        assertEquals(WrapperType.REACT_NATIVE, captor.value)
//    }
//
//    @Test
//    fun `test collectMessageInfo()`() {
//        MobileCore.collectMessageInfo(
//            mapOf(
//                "key" to "value"
//            )
//        )
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("CollectData", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.os", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.generic.data", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(mapOf("key" to "value"), dispatchedEvent.eventData)
//    }
//
//    @Test
//    fun `test collectMessageInfo() with null data`() {
//        MobileCore.collectMessageInfo(null)
//        Mockito.verify(eventHub, Mockito.times(0)).dispatch(any())
//    }
//
//    @Test
//    fun `test collectMessageInfo() without empty data`() {
//        MobileCore.collectMessageInfo(emptyMap<String, Any>())
//        Mockito.verify(eventHub, Mockito.times(0)).dispatch(any())
//    }
//
//    @Test
//    fun `test resetIdentities()`() {
//        MobileCore.resetIdentities()
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("Reset Identities Request", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestreset", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.generic.identity", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//    }
//
//    @Test
//    fun `test dispatchEvent() without callback`() {
//        val event = Event.Builder(
//            "event",
//            "com.adobe.eventType.lifecycle",
//            "com.adobe.eventSource.responseContent"
//        ).setEventData(
//            mapOf(
//                "lifecyclecontextdata" to mapOf(
//                    "launchevent" to "LaunchEvent"
//                )
//            )
//        ).build()
//        MobileCore.dispatchEvent(event, null)
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertEquals(event, dispatchedEvent)
//    }
//
//    @Test
//    fun `test dispatchEvent() with callback`() {
//        MobileCore.dispatchEvent(null, extensionErrorCallback)
//        Mockito.verify(eventHub, Mockito.times(0)).dispatch(any())
//        val captor = ArgumentCaptor.forClass(
//            ExtensionError::class.java
//        )
//        Mockito.verify(extensionErrorCallback, Mockito.timeout(1000).times(1))
//            .error(captor.capture())
//        assertEquals(ExtensionError.EVENT_NULL, captor.value)
//    }
//
//    @Test
//    fun `test collectLaunchInfo()`() {
//        val data = mapOf(
//            "key" to "value"
//        )
//
//        PowerMockito.whenNew(DataMarshaller::class.java).withNoArguments()
//            .thenReturn(dataMarshaller)
//        PowerMockito.`when`(dataMarshaller.data)
//            .thenReturn(data)
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        MobileCore.collectLaunchInfo(null)
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("CollectData", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.os", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.generic.data", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(mapOf("key" to "value"), dispatchedEvent.eventData)
//    }
//
//    @Test
//    fun `test collectLaunchInfo() without data in Activity`() {
//        PowerMockito.whenNew(DataMarshaller::class.java).withNoArguments()
//            .thenReturn(dataMarshaller)
//        PowerMockito.`when`(dataMarshaller.data)
//            .thenReturn(emptyMap())
//        MobileCore.collectLaunchInfo(null)
//        Mockito.verify(eventHub, Mockito.times(0)).dispatch(any())
//    }
//
//    @Test
//    fun `test clearUpdatedConfiguration()`() {
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        MobileCore.clearUpdatedConfiguration()
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("Clear updated configuration", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.configuration", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(mapOf("config.clearUpdates" to true), dispatchedEvent.eventData)
//    }
//
//    @Test
//    fun `test configureWithAppID()`() {
//        val appId = "id_123"
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        MobileCore.configureWithAppID(appId)
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("Configure with AppID", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.configuration", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(mapOf("config.appId" to appId), dispatchedEvent.eventData)
//    }
//
//    @Test
//    fun `test configureWithFileInAssets()`() {
//        val path = "path/to/the/bundled/configuration"
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        MobileCore.configureWithFileInAssets(path)
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("Configure with FilePath", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.configuration", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(mapOf("config.assetFile" to path), dispatchedEvent.eventData)
//    }
//
//    @Test
//    fun `test configureWithFileInPath()`() {
//        val path = "path/to/the/bundled/configuration"
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        MobileCore.configureWithFileInPath(path)
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("Configure with FilePath", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.configuration", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(mapOf("config.filePath" to path), dispatchedEvent.eventData)
//    }
//
//    @Test
//    @Ignore
//    fun `test dispatchEventWithResponseCallback()`() {
//        // TODO: will test dispatchEventWithResponseCallback() after we have the redesigned OneTimeListener and EventListener APIs
//    }
//
//    @Test
//    @Ignore
//    fun `test registerEventListener()`() {
//        // TODO: will test dispatchEventWithResponseCallback() after we have the redesigned OneTimeListener and EventListener APIs
//    }
//
//    @Test
//    @Ignore
//    fun `test getPrivacyStatus()`() {
//        // TODO: will test dispatchEventWithResponseCallback() after we have the redesigned OneTimeListener and EventListener APIs
//    }
//
//    @Test
//    fun `test setPrivacyStatus()`() {
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT)
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("Configuration Update", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.configuration", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(
//            mapOf(
//                "config.update" to mapOf(
//                    "global.privacy" to MobilePrivacyStatus.OPT_OUT.value
//                )
//            ),
//            dispatchedEvent.eventData
//        )
//    }
//
//    @Test
//    fun `test updateConfiguration()`() {
//        val configurations = mapOf(
//            "key" to "value"
//        )
//        val captor = ArgumentCaptor.forClass(
//            Event::class.java
//        )
//        MobileCore.updateConfiguration(configurations)
//        Mockito.verify(eventHub, Mockito.times(1)).dispatch(captor.capture())
//        val dispatchedEvent = captor.value
//        assertNotNull(dispatchedEvent)
//        assertEquals("Configuration Update", dispatchedEvent.name)
//        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEvent.source)
//        assertEquals("com.adobe.eventtype.configuration", dispatchedEvent.type)
//        assertNull(dispatchedEvent.pairID)
//        assertNotNull(dispatchedEvent.responsePairID)
//        assertNotNull(dispatchedEvent.uniqueIdentifier)
//        assertNotNull(dispatchedEvent.eventNumber)
//        assertEquals(
//            mapOf(
//                "config.update" to mapOf(
//                    "key" to "value"
//                )
//            ),
//            dispatchedEvent.eventData
//        )
//    }
// }
