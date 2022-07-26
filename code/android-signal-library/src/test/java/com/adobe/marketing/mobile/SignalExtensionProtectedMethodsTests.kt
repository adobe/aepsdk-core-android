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