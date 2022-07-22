package com.adobe.marketing.mobile

import com.adobe.marketing.mobile.services.PersistentHitQueue
import com.adobe.marketing.mobile.signal.SignalExtension
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class SignalExtensionProtectedMethodsTests {
    private lateinit var signalExtension: Extension
    private lateinit var extensionApi: ExtensionApi

    companion object {
        @BeforeClass
        @JvmStatic
        fun setupClass() {
            mockConstruction(PersistentHitQueue::class.java) { mock, _ ->
                doNothing().`when`(mock).beginProcessing()
            }
        }
    }

    @Before
    fun setup() {
        extensionApi = mock(ExtensionApi::class.java)
        signalExtension = SignalExtension(extensionApi)
    }

    @Test
    fun `Test SignalExtension registration `() {

        signalExtension.onRegistered()

        val eventTypeCaptor = ArgumentCaptor.forClass(
            String::class.java
        )
        val eventSourceCaptor = ArgumentCaptor.forClass(
            String::class.java
        )
        val listenerCaptor = ArgumentCaptor.forClass(
            ExtensionEventListener::class.java
        )

        verify(extensionApi, times(2)).registerEventListener(
            eventTypeCaptor.capture(),
            eventSourceCaptor.capture(),
            listenerCaptor.capture()
        )

        // TODO: need to change it to the actual string after Core exposed all EventType/EventSource constants
        assertEquals("", eventTypeCaptor.allValues[0])
        assertEquals("", eventTypeCaptor.allValues[1])
        assertEquals("", eventSourceCaptor.allValues[0])
        assertEquals("", eventSourceCaptor.allValues[1])
        assertNotNull(listenerCaptor.allValues[0])
        assertNotNull(listenerCaptor.allValues[1])
    }

    @Test
    fun `Test getName() `() {
        assertEquals("com.adobe.module.signal", signalExtension.name)
    }

    @Test
    fun `Test getFriendlyName() `() {
        assertEquals("Signal", signalExtension.friendlyName)
    }

    @Test
    fun `Test getVersion() `() {
        assertEquals("1.0.4", signalExtension.version)
    }

    @Test
    fun `Test readyForEvent() with null Event `() {
        assertFalse(signalExtension.readyForEvent(null))
    }

    @Test
    fun `Test readyForEvent() when configuration is not ready `() {
        assertFalse(signalExtension.readyForEvent(null))
    }

}