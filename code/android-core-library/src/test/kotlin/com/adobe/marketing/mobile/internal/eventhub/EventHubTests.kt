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

package com.adobe.marketing.mobile.internal.eventhub

import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import org.junit.Before
import org.junit.Test
import java.lang.Exception
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

private object MockExtensions {
    class MockExtensionInvalidConstructor(api: ExtensionApi, name: String?) : Extension(api) {
        override fun getName(): String {
            return MockExtensionInvalidConstructor::javaClass.name
        }
    }

    class MockExtensionInitFailure(api: ExtensionApi) : Extension(api) {
        init {
            throw Exception("Init Exception")
        }

        override fun getName(): String {
            return MockExtensionInitFailure::javaClass.name
        }
    }

    class MockExtensionNullName(api: ExtensionApi) : Extension(api) {
        override fun getName(): String? {
            return null
        }
    }

    class MockExtensionNameException(api: ExtensionApi) : Extension(api) {
        override fun getName(): String {
            throw Exception()
        }
    }

    class MockExtensionKotlin(api: ExtensionApi) : Extension(api) {
        override fun getName(): String {
            return MockExtensionKotlin::javaClass.name
        }
    }
}

internal class EventHubTests {

    // Helper to register extensions
    fun registerExtension(extensionClass: Class<out Extension>): EventHubError {
        var ret: EventHubError = EventHubError.Unknown

        val latch = CountDownLatch(1)
        EventHub.shared.registerExtension(extensionClass) { error ->
            ret = error
            latch.countDown()
        }
        if (!latch.await(1, TimeUnit.SECONDS)) throw Exception("Timeout registering extension")
        return ret
    }

    fun unregisterExtension(extensionClass: Class<out Extension>): EventHubError {
        var ret: EventHubError = EventHubError.Unknown

        val latch = CountDownLatch(1)
        EventHub.shared.unregisterExtension(extensionClass) { error ->
            ret = error
            latch.countDown()
        }
        if (!latch.await(1, TimeUnit.SECONDS)) throw Exception("Timeout unregistering extension")
        return ret
    }

    @Before
    fun setup() {
        EventHub.shared.shutdown()
        EventHub.shared = EventHub()
    }

    @Test
    fun testRegisterExtensionSuccess() {

        var ret = registerExtension(MockExtension::class.java)
        assertEquals(EventHubError.None, ret)

        ret = registerExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.None, ret)
    }

    @Test
    fun testRegisterExtensionFailure_DuplicateExtension() {
        registerExtension(MockExtension::class.java)

        var ret = registerExtension(MockExtension::class.java)
        assertEquals(EventHubError.DuplicateExtensionName, ret)
    }

    @Test
    fun testRegisterExtensionFailure_ExtensionInitialization() {
        var ret = registerExtension(MockExtensions.MockExtensionInitFailure::class.java)
        assertEquals(EventHubError.ExtensionInitializationFailure, ret)

        ret = registerExtension(MockExtensions.MockExtensionInvalidConstructor::class.java)
        assertEquals(EventHubError.ExtensionInitializationFailure, ret)
    }

    @Test
    fun testRegisterExtensionFailure_InvalidExceptionName() {
        var ret = registerExtension(MockExtensions.MockExtensionNullName::class.java)
        assertEquals(EventHubError.InvalidExtensionName, ret)

        ret = registerExtension(MockExtensions.MockExtensionNameException::class.java)
        assertEquals(EventHubError.InvalidExtensionName, ret)
    }

    @Test
    fun testUnregisterExtensionSuccess() {
        registerExtension(MockExtensions.MockExtensionKotlin::class.java)

        var ret = unregisterExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.None, ret)
    }

    @Test
    fun testUnregisterExtensionFailure() {
        var ret = unregisterExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.ExtensionNotRegistered, ret)
    }

    @Test
    fun testRegisterAfterUnregister() {
        registerExtension(MockExtensions.MockExtensionKotlin::class.java)

        var ret = unregisterExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.None, ret)

        ret = registerExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.None, ret)
    }
}
