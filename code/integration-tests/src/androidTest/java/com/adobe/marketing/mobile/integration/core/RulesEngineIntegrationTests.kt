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

package com.adobe.marketing.mobile.integration.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.SDKHelper
import com.adobe.marketing.mobile.Signal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RulesEngineIntegrationTests {

    companion object {
        const val TEST_APP_ID = "appId"
        const val TEST_RULES_RESOURCE = "rules_integration.zip"
        const val WAIT_TIME_MILLIS_SHORT = 500L
    }

    @Before
    fun setup() {
        SDKHelper.initializeSDK(listOf(Signal.EXTENSION))
        SDKHelper.setupConfiguration(TEST_APP_ID, mapOf("global.privacy" to "optedin"), TEST_RULES_RESOURCE)
    }

    @After
    fun cleanup() {
        SDKHelper.resetSDK()
    }

    @Test
    fun testDispatchConsequence() {
        val capturedEvents = mutableListOf<Event>()

        val eventLatch = CountDownLatch(1)
        MobileCore.registerEventListener(
            "test.type.consequence",
            "test.source.consequence") {
                    capturedEvents.add(it)
                    eventLatch.countDown()
                }

        val eventData = mapOf("xdm" to "test data")
        val event = Event.Builder("Test Event Trigger", "test.type.trigger", "test.source.trigger")
            .setEventData(eventData)
            .build()
        MobileCore.dispatchEvent(event)

        assertTrue(eventLatch.await(WAIT_TIME_MILLIS_SHORT, TimeUnit.MILLISECONDS))
        assertTrue(capturedEvents.size == 1)
        assertEquals(eventData, capturedEvents[0].eventData)
        assertEquals(event.uniqueIdentifier, capturedEvents[0].parentID)
    }

    @Test
    fun testDispatchConsequence_eventTriggersTwoConsequences() {
        val capturedEvents = mutableListOf<Event>()

        val eventLatch = CountDownLatch(2)
        MobileCore.registerEventListener(
            "test.type.consequence",
            "test.source.consequence"){
            capturedEvents.add(it)
            eventLatch.countDown()
        }

        // Test
        val eventData = mapOf("dispatch" to "yes") // This is to trigger the consequence
        val event = Event.Builder("Test Event Trigger", "test.type.trigger", "test.source.trigger")
            .setEventData(eventData)
            .build()
        MobileCore.dispatchEvent(event)

        // Verify
        assertTrue(eventLatch.await(WAIT_TIME_MILLIS_SHORT, TimeUnit.MILLISECONDS))
        // One consequence event corresponding to source & type match and one for the data match
        assertEquals(2, capturedEvents.size)
        capturedEvents.forEach { e ->
            assertEquals("yes", e.eventData?.get("dispatch"))
            assertEquals(event.uniqueIdentifier, e.parentID)
        }
    }

    @Test
    fun testDispatchConsequenceChainDoesNotLoop() {
        val capturedEvents = mutableListOf<Event>()

        val consequenceEventLatch1 = CountDownLatch(1)
        MobileCore.registerEventListener(
            "test.type.consequence",
            "test.source.consequence"){
            capturedEvents.add(it)
            consequenceEventLatch1.countDown()
        }

        val consequenceEventLatch2 = CountDownLatch(1)
        MobileCore.registerEventListener(
            "test.type.consequence.2",
            "test.source.consequence.2") {
            capturedEvents.add(it)
            consequenceEventLatch2.countDown()
        }

        val consequenceEventLatch3 = CountDownLatch(1)
        MobileCore.registerEventListener(
            "test.type.consequence.3",
            "test.source.consequence.3") {
            capturedEvents.add(it)
            consequenceEventLatch3.countDown()
        }

        // Test
        val event = Event.Builder(
            "Test Event Trigger",
            "test.type.trigger",
            "test.source.trigger"
        )
            .setEventData(
                mapOf("chain" to "yes")
            )
            .build()

        MobileCore.dispatchEvent(event)

        // Verify

        assertTrue(consequenceEventLatch1.await(WAIT_TIME_MILLIS_SHORT, TimeUnit.MILLISECONDS))
        // Should not dispatch due to chain limit = 1
        assertFalse(consequenceEventLatch2.await(WAIT_TIME_MILLIS_SHORT, TimeUnit.MILLISECONDS))
        assertFalse(consequenceEventLatch3.await(WAIT_TIME_MILLIS_SHORT, TimeUnit.MILLISECONDS))
        assertTrue(capturedEvents.size == 1)
        capturedEvents.forEach { e ->
            assertEquals("yes", e.eventData?.get("chain"))
        }
    }

    @Test
    fun testAttachData_dispatchesEventWithAttachedKeys() {
        val capturedEvents = mutableListOf<Event>()

        val eventLatch = CountDownLatch(1)
        MobileCore.registerEventListener(
            "test.type.consequence",
            "test.source.consequence") {
            capturedEvents.add(it)
            eventLatch.countDown()
        }

        // Test
        val eventData = mapOf("attach" to "yes") // "attach" triggers the condition
        val event = Event.Builder("Test Event Trigger", "test.type.trigger", "test.source.trigger")
            .setEventData(eventData)
            .build()
        MobileCore.dispatchEvent(event)

        // Verify
        assertTrue(eventLatch.await(WAIT_TIME_MILLIS_SHORT, TimeUnit.MILLISECONDS))
        assertEquals(1, capturedEvents.size)
        val expectedEventData = mapOf("attach" to "yes", "attachedKey" to "attachedValue")
        capturedEvents.forEach { e ->
            assertEquals(expectedEventData, e.eventData)
        }
    }

    @Test
    fun testModifyData_dispatchesEventWithChangedValue() {
        val capturedEvents = mutableListOf<Event>()

        val eventLatch = CountDownLatch(1)
        MobileCore.registerEventListener(
            "test.type.consequence",
            "test.source.consequence") {
            capturedEvents.add(it)
            eventLatch.countDown()
        }

        // Test
        val eventData = mapOf("modify" to "yes", "keyToModify" to "originalValue") // "modify" triggers the condition
        val event = Event.Builder("Test Event Trigger", "test.type.trigger", "test.source.trigger")
            .setEventData(eventData)
            .build()
        MobileCore.dispatchEvent(event)

        // Verify
        assertTrue(eventLatch.await(WAIT_TIME_MILLIS_SHORT, TimeUnit.MILLISECONDS))
        assertEquals(1, capturedEvents.size)

        val expectedEventData = mapOf("modify" to "yes", "keyToModify" to "modifiedValue")
        capturedEvents.forEach { e ->
            assertEquals(expectedEventData, e.eventData)
        }
    }
}