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
    class MockExtensionInvalidConstructor(api: ExtensionApi, name: String?): Extension(api) {
        override fun getName(): String {
            return MockExtensionInvalidConstructor::javaClass.name
        }
    }

    class MockExtensionInitFailure(api: ExtensionApi): Extension(api) {
        init {
            throw Exception("Init Exception")
        }

        override fun getName(): String {
            return MockExtensionInitFailure::javaClass.name
        }
    }

    class MockExtensionNullName(api: ExtensionApi): Extension(api) {
        override fun getName(): String? {
            return null
        }
    }

    class MockExtensionNameException(api: ExtensionApi): Extension(api) {
        override fun getName(): String {
            throw Exception()
        }
    }

    class MockExtensionKotlin(api: ExtensionApi): Extension(api) {
        override fun getName(): String {
            return MockExtensionKotlin::javaClass.name
        }
    }
}

internal class EventHubTests {

    // Helper to register extensions
    fun registerExtension(extensionClass: Class<out Extension>): EventHubError {
        var ret: EventHubError = EventHubError.unknown;

        val latch = CountDownLatch(1)
        EventHub.shared.registerExtension(extensionClass) { error ->
            ret = error
            latch.countDown()
        }
        if (!latch.await(1, TimeUnit.SECONDS)) throw Exception("Timeout registering extension");
        return ret
    }

    fun unregisterExtension(extensionClass: Class<out Extension>): EventHubError {
        var ret: EventHubError = EventHubError.unknown;

        val latch = CountDownLatch(1)
        EventHub.shared.unregisterExtension(extensionClass) { error ->
            ret = error
            latch.countDown()
        }
        if (!latch.await(1, TimeUnit.SECONDS)) throw Exception("Timeout unregistering extension");
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
        assertEquals(EventHubError.none, ret)

        ret = registerExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.none, ret)
    }

    @Test
    fun testRegisterExtensionFailure_DuplicateExtension() {
        registerExtension(MockExtension::class.java)

        var ret = registerExtension(MockExtension::class.java)
        assertEquals(EventHubError.duplicateExtensionName, ret)
    }

    @Test
    fun testRegisterExtensionFailure_ExtensionInitialization() {
        var ret = registerExtension(MockExtensions.MockExtensionInitFailure::class.java)
        assertEquals(EventHubError.extensionInitializationFailure, ret)

        ret = registerExtension(MockExtensions.MockExtensionInvalidConstructor::class.java)
        assertEquals(EventHubError.extensionInitializationFailure, ret)
    }

    @Test
    fun testRegisterExtensionFailure_InvalidExceptionName() {
        var ret = registerExtension(MockExtensions.MockExtensionNullName::class.java)
        assertEquals(EventHubError.invalidExtensionName, ret)

        ret = registerExtension(MockExtensions.MockExtensionNameException::class.java)
        assertEquals(EventHubError.invalidExtensionName, ret)
    }


    @Test
    fun testUnregisterExtensionSuccess() {
        registerExtension(MockExtensions.MockExtensionKotlin::class.java)

        var ret = unregisterExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.none, ret)
    }

    @Test
    fun testUnregisterExtensionFailure() {
        var ret = unregisterExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.extensionNotRegistered, ret)
    }

    @Test
    fun testRegisterAfterUnregister() {
        registerExtension(MockExtensions.MockExtensionKotlin::class.java)

        var ret = unregisterExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.none, ret)

        ret = registerExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.none, ret)
    }

}