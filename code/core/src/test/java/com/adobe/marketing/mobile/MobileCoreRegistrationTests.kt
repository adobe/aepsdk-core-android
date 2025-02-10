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
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue

class MobileCoreRegistrationTests {

    private class MockExtension(extensionApi: ExtensionApi) : Extension(extensionApi) {
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

    private class MockExtension2(extensionApi: ExtensionApi) : Extension(extensionApi) {
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

    private class MockExtensionWithSlowInit(extensionApi: ExtensionApi) : Extension(extensionApi) {
        companion object {
            var initWaitTimeMS: Long = 0
            var registrationClosure: (() -> Unit)? = null

            fun reset() {
                initWaitTimeMS = 0
                registrationClosure = null
            }
        }

        init {
            Thread.sleep(initWaitTimeMS)
        }

        override fun getName(): String = "SlowMockExtension"

        override fun onRegistered() {
            registrationClosure?.invoke()
        }
    }

    private fun resetExtensions() {
        MockExtension.reset()
        MockExtension2.reset()
        MockExtensionWithSlowInit.reset()
    }

    @Before
    fun setup() {
        MobileCore.resetSDK()
        resetExtensions()
    }

    @Test
    fun `register single extension`() {
        val extensionRegistrationLatch = CountDownLatch(1)
        MockExtension.registrationClosure = { extensionRegistrationLatch.countDown() }

        MobileCore.setApplication(mock(Application::class.java))
        val coreRegistrationLatch = CountDownLatch(1)
        MobileCore.registerExtensions(listOf(MockExtension::class.java)) {
            coreRegistrationLatch.countDown()
        }

        assertTrue { extensionRegistrationLatch.await(1, TimeUnit.SECONDS) }
        assertTrue { coreRegistrationLatch.await(1, TimeUnit.SECONDS) }
    }

    @Test
    fun `register multiple extensions`() {
        val extensionRegistrationLatch = CountDownLatch(2)
        MockExtension.registrationClosure = { extensionRegistrationLatch.countDown() }
        MockExtension2.registrationClosure = { extensionRegistrationLatch.countDown() }

        val extensions: List<Class<out Extension>> = listOf(
            MockExtension::class.java,
            MockExtension2::class.java
        )

        MobileCore.setApplication(mock(Application::class.java))
        val coreRegistrationLatch = CountDownLatch(1)
        MobileCore.registerExtensions(extensions) {
            coreRegistrationLatch.countDown()
        }

        assertTrue { extensionRegistrationLatch.await(1, TimeUnit.SECONDS) }
        assertTrue { coreRegistrationLatch.await(1, TimeUnit.SECONDS) }
    }

    @Test
    fun `registering slow extension should not block other extensions`() {
        val fastExtensionLatch = CountDownLatch(2)
        MockExtension.registrationClosure = { fastExtensionLatch.countDown() }
        MockExtension2.registrationClosure = { fastExtensionLatch.countDown() }

        val latch = CountDownLatch(2)
        MockExtensionWithSlowInit.registrationClosure = { latch.countDown() }
        MockExtensionWithSlowInit.initWaitTimeMS = 2000

        val extensions: List<Class<out Extension>> = listOf(
            MockExtensionWithSlowInit::class.java,
            MockExtension::class.java,
            MockExtension2::class.java
        )

        MobileCore.setApplication(mock(Application::class.java))
        MobileCore.registerExtensions(extensions) {
            latch.countDown()
        }

        // Extension 1 and 2 will initialize first
        assertTrue { fastExtensionLatch.await(1, TimeUnit.SECONDS) }
        // registerExtensions will complete after all extensions are registered
        assertTrue { latch.await(4, TimeUnit.SECONDS) }
    }

    @Test
    fun `register duplicate extension`() {
        val latch = CountDownLatch(1)
        MobileCore.setApplication(mock(Application::class.java))
        MobileCore.registerExtensions(listOf(MockExtension::class.java, MockExtension::class.java)) {
            latch.countDown()
        }
        assertTrue { latch.await(1000, TimeUnit.SECONDS) }
    }

    @Test
    fun `dispatch event after registration`() {
        val latch = CountDownLatch(1)
        MockExtension.eventReceivedClosure = {
            if (it.name == "test-event") {
                latch.countDown()
            }
        }

        MobileCore.setApplication(mock(Application::class.java))
        MobileCore.registerExtensions(listOf(MockExtension::class.java)) {}

        val event = Event.Builder("test-event", "analytics", "requestContent").build()
        MobileCore.dispatchEvent(event)
        assertTrue { latch.await(1, TimeUnit.SECONDS) }
    }

    @Test
    fun `dispatch event before registration`() {
        val latch = CountDownLatch(1)
        MockExtension.eventReceivedClosure = {
            if (it.name == "test-event") {
                System.err.println("Received....")
                latch.countDown()
            }
        }

        val event = Event.Builder("test-event", "analytics", "requestContent").build()
        MobileCore.dispatchEvent(event)

        MobileCore.setApplication(mock(Application::class.java))
        MobileCore.registerExtensions(listOf(MockExtension::class.java)) {}
        assertTrue { latch.await(2, TimeUnit.SECONDS) }
    }

    @Test
    fun `dispatch event to multiple extensions after registration`() {
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
        MobileCore.registerExtensions(listOf(MockExtension::class.java, MockExtension2::class.java)) {}

        val event = Event.Builder("test-event", "analytics", "requestContent").build()
        MobileCore.dispatchEvent(event)

        assertTrue { latch.await(1, TimeUnit.SECONDS) }
    }

    @Test
    fun `dispatch event to multiple extensions before registration`() {
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
        MobileCore.registerExtensions(listOf(MockExtension::class.java, MockExtension2::class.java)) {
            latch.countDown()
        }

        assertTrue { latch.await(2, TimeUnit.SECONDS) }
    }
}
