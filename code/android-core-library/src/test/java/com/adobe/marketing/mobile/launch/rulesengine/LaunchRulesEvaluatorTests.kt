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
package com.adobe.marketing.mobile.launch.rulesengine

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.MobileCore
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.reflect.Whitebox

@RunWith(PowerMockRunner::class)
@PrepareForTest(ExtensionApi::class, MobileCore::class)
class LaunchRulesEvaluatorTests {

    @Mock
    private lateinit var launchRulesEngine: LaunchRulesEngine

    private lateinit var extensionApi: ExtensionApi
    private lateinit var launchRulesEvaluator: LaunchRulesEvaluator
    private val cachedEvents: MutableList<Event> = mutableListOf()

    @Before
    fun setup() {
        extensionApi = PowerMockito.mock(ExtensionApi::class.java)
        PowerMockito.mockStatic(MobileCore::class.java)
        launchRulesEvaluator = LaunchRulesEvaluator("", launchRulesEngine, extensionApi)
        Whitebox.setInternalState(launchRulesEvaluator, "cachedEvents", cachedEvents)
    }

    @Test
    fun `Process a null event`() {
        assertNull(launchRulesEvaluator.process(null))
        verify(launchRulesEngine, never()).process(any())
        assertEquals(0, cachedEvents.size)
    }

    @Test
    fun `Cache incoming events if rules are not set`() {
        repeat(100) {
            launchRulesEvaluator.process(
                Event.Builder("event-$it", "type", "source").build()
            )
        }
        assertEquals(100, cachedEvents.size)
    }

    @Test
    fun `Clear cached events if reached the limit`() {
        repeat(101) {
            launchRulesEvaluator.process(
                Event.Builder("event-$it", "type", "source").build()
            )
        }
        assertEquals(0, cachedEvents.size)
    }

    @Test
    fun `Reprocess cached events when rules are ready`() {
        repeat(10) {
            launchRulesEvaluator.process(
                Event.Builder("event-$it", "type", "source").build()
            )
        }
        assertEquals(10, cachedEvents.size)
        val eventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        BDDMockito.given(MobileCore.dispatchEvent(eventCaptor.capture(), any())).willReturn(true)
        launchRulesEvaluator.replaceRules(listOf())
        assertNotNull(eventCaptor.value)
        assertEquals("com.adobe.eventtype.rulesengine", eventCaptor.value.type)
        assertEquals("com.adobe.eventtype.rulesengine", eventCaptor.value.type)
        assertEquals("com.adobe.eventsource.requestreset", eventCaptor.value.source)
        launchRulesEvaluator.process(eventCaptor.value)
        assertEquals(0, cachedEvents.size)
    }

    @Test
    fun `Reprocess cached events in the right order`() {
        repeat(10) {
            launchRulesEvaluator.process(
                Event.Builder("event-$it", "type", "source").build()
            )
        }
        assertEquals(10, cachedEvents.size)
        val eventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        BDDMockito.given(MobileCore.dispatchEvent(eventCaptor.capture(), any())).willReturn(true)
        launchRulesEvaluator.replaceRules(listOf())
        assertNotNull(eventCaptor.value)
        assertEquals("com.adobe.eventtype.rulesengine", eventCaptor.value.type)
        assertEquals("com.adobe.eventsource.requestreset", eventCaptor.value.source)
        Mockito.reset(launchRulesEngine)
        launchRulesEvaluator.process(eventCaptor.value)
        val cachedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        verify(launchRulesEngine, Mockito.times(10)).process(cachedEventCaptor.capture())
        assertEquals(10, cachedEventCaptor.allValues.size)
        cachedEventCaptor.allValues.forEachIndexed { index, element ->
            assertEquals("event-$index", element.name)
        }
        assertEquals(0, cachedEvents.size)
    }

    @Test
    fun `Do nothing if set null rule`() {
        repeat(10) {
            launchRulesEvaluator.process(
                Event.Builder("event-$it", "type", "source").build()
            )
        }
        assertEquals(10, cachedEvents.size)
        launchRulesEvaluator.replaceRules(null)
        PowerMockito.verifyNoMoreInteractions(MobileCore::class.java)
        assertEquals(10, cachedEvents.size)
    }
}
