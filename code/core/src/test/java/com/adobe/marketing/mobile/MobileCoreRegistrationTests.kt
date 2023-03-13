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
import com.adobe.marketing.mobile.extensions.Sample1
import com.adobe.marketing.mobile.extensions.Sample1Kt
import com.adobe.marketing.mobile.extensions.Sample2
import com.adobe.marketing.mobile.extensions.Sample2Extension
import com.adobe.marketing.mobile.extensions.Sample2Kt
import com.adobe.marketing.mobile.extensions.Sample2KtExtension
import com.adobe.marketing.mobile.internal.eventhub.EventHub
import com.adobe.marketing.mobile.internal.eventhub.EventHubError
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MobileCoreRegistrationTests {

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
            api.registerEventListener(EventType.WILDCARD, EventSource.WILDCARD) {
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
            api.registerEventListener(EventType.WILDCARD, EventSource.WILDCARD) {
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
        MobileCore.sdkInitializedWithContext = AtomicBoolean(false)
        EventHub.shared = EventHub()
    }

    @After
    fun cleanup() {
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
    fun testScenario1_SameClass() {
        val latch = CountDownLatch(2)
        val capturedIds = mutableSetOf<String>()
        val callback = object : AdobeCallbackWithError<String> {
            override fun call(id: String) {
                capturedIds.add(id)
                latch.countDown()
            }
            override fun fail(error: AdobeError?) {}
        }

        MobileCore.setApplication(mock(Application::class.java))

        val extensions = listOf(
            Sample1::class.java,
            Sample1Kt::class.java
        )

        MobileCore.registerExtensions(extensions) {
            Sample1.getTrackingIdentifier(callback)
            Sample1Kt.getTrackingIdentifier(callback)
        }

        assertTrue { latch.await(1000, TimeUnit.MILLISECONDS) }
        assertEquals(setOf("Sample1_ID", "Sample1Kt_ID"), capturedIds)
    }

    @Test
    fun testScenario2_DifferentClasses() {
        val latch = CountDownLatch(2)
        val capturedIds = mutableSetOf<String>()
        val callback = object : AdobeCallbackWithError<String> {
            override fun call(id: String) {
                capturedIds.add(id)
                latch.countDown()
            }
            override fun fail(error: AdobeError?) {}
        }

        MobileCore.setApplication(mock(Application::class.java))

        val extensions = listOf(
            Sample2Extension::class.java,
            Sample2KtExtension::class.java
        )

        MobileCore.registerExtensions(extensions) {
            Sample2.getTrackingIdentifier(callback)
            Sample2Kt.getTrackingIdentifier(callback)
        }

        assertTrue { latch.await(1000, TimeUnit.MILLISECONDS) }
        assertEquals(setOf("Sample2_ID", "Sample2Kt_ID"), capturedIds)
    }

    @Test
    fun testRegisterExtensionsSimple() {
        val latch = CountDownLatch(1)
        MockExtension.registrationClosure = { latch.countDown() }

        MobileCore.setApplication(mock(Application::class.java))
        MobileCore.registerExtensions(listOf(MockExtension::class.java)) {}

        assertTrue { latch.await(1, TimeUnit.SECONDS) }
    }

    @Test
    fun testRegisterExtensionsSimpleMultiple() {
        val latch = CountDownLatch(2)
        MockExtension.registrationClosure = { latch.countDown() }
        MockExtension2.registrationClosure = { latch.countDown() }

        val extensions: List<Class<out Extension>> = listOf(
            MockExtension::class.java,
            MockExtension2::class.java
        )

        MobileCore.setApplication(mock(Application::class.java))
        MobileCore.registerExtensions(extensions) {}

        assertTrue { latch.await(1, TimeUnit.SECONDS) }
    }

    @Test
    fun testRegisterExtensionsWithSlowExtension() {
        val latch = CountDownLatch(2)
        MockExtension.registrationClosure = { latch.countDown() }
        MockExtension2.registrationClosure = { latch.countDown() }

        MockExtensionWithSlowInit.initWaitTimeMS = 2000

        val extensions: List<Class<out Extension>> = listOf(
            MockExtensionWithSlowInit::class.java,
            MockExtension::class.java,
            MockExtension2::class.java
        )

        MobileCore.setApplication(mock(Application::class.java))
        MobileCore.registerExtensions(extensions) {}

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
        val latch = CountDownLatch(3)
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
        MobileCore.start {
            latch.countDown()
        }

        assertTrue { latch.await(1000, TimeUnit.SECONDS) }
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
}
