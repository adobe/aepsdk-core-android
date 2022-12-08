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
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.ExtensionApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(MockitoJUnitRunner.Silent::class)
class LaunchRulesEvaluatorTests {

    @Mock
    private lateinit var launchRulesEngine: LaunchRulesEngine

    private lateinit var extensionApi: ExtensionApi
    private lateinit var launchRulesEvaluator: LaunchRulesEvaluator

    @Before
    fun setup() {
        extensionApi = Mockito.mock(ExtensionApi::class.java)
        launchRulesEvaluator = LaunchRulesEvaluator("", launchRulesEngine, extensionApi)
    }

    @Test
    fun `Cache incoming events if rules are not set`() {
        repeat(100) {
            launchRulesEvaluator.process(
                Event.Builder("event-$it", "type", "source").build()
            )
        }
        assertEquals(100, launchRulesEvaluator.getCachedEventCount())
    }

    @Test
    fun `Reprocess cached events when rules are ready`() {
        repeat(10) {
            launchRulesEvaluator.process(
                Event.Builder("event-$it", "type", "source").build()
            )
        }
        assertEquals(10, launchRulesEvaluator.getCachedEventCount())
        val eventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        launchRulesEvaluator.replaceRules(listOf())
        verify(extensionApi, Mockito.times(1)).dispatch(eventCaptor.capture())
        assertNotNull(eventCaptor.value)
        assertEquals(EventType.RULES_ENGINE, eventCaptor.value.type)
        assertEquals(EventSource.REQUEST_RESET, eventCaptor.value.source)
        launchRulesEvaluator.process(eventCaptor.value)
        assertEquals(0, launchRulesEvaluator.getCachedEventCount())
    }

    @Test
    fun `Reprocess cached events in the right order`() {
        repeat(10) {
            launchRulesEvaluator.process(
                Event.Builder("event-$it", "type", "source").build()
            )
        }
        assertEquals(10, launchRulesEvaluator.getCachedEventCount())
        val eventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        launchRulesEvaluator.replaceRules(listOf())
        verify(extensionApi, Mockito.times(1)).dispatch(eventCaptor.capture())
        assertNotNull(eventCaptor.value)
        assertEquals(EventType.RULES_ENGINE, eventCaptor.value.type)
        assertEquals(EventSource.REQUEST_RESET, eventCaptor.value.source)
        Mockito.reset(launchRulesEngine)
        launchRulesEvaluator.process(eventCaptor.value)
        val cachedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        verify(launchRulesEngine, Mockito.times(11)).process(cachedEventCaptor.capture())
        assertEquals(11, cachedEventCaptor.allValues.size)
        // 0th event will be current processed event
        for (index in 1 until cachedEventCaptor.allValues.size) {
            assertEquals("event-${index - 1}", cachedEventCaptor.allValues[index].name)
        }
        assertEquals(0, launchRulesEvaluator.getCachedEventCount())
    }

    @Test
    fun `Do nothing if set null rule`() {
        repeat(10) {
            launchRulesEvaluator.process(
                Event.Builder("event-$it", "type", "source").build()
            )
        }
        assertEquals(10, launchRulesEvaluator.getCachedEventCount())
        launchRulesEvaluator.replaceRules(null)
        Mockito.verifyNoInteractions(extensionApi)
        assertEquals(10, launchRulesEvaluator.getCachedEventCount())
    }
}
