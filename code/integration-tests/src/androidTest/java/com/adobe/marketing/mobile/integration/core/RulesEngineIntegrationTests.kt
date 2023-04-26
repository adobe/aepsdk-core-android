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

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.SDKHelper
import com.adobe.marketing.mobile.Signal
import com.adobe.marketing.mobile.integration.MockNetworkResponse
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RulesEngineIntegrationTests {

    companion object {
        const val TEST_RULES_URL = "https://rules.com/rules.zip"
        const val TEST_APP_ID = "appId"
        const val TEST_CONFIG_URL = "https://assets.adobedtm.com/${TEST_APP_ID}.json"
        const val TEST_RULES_RESOURCE = "rules_integration.zip"
        const val WAIT_TIME_MILLIS_LONG = 500L
        const val WAIT_TIME_MILLIS_SHORT = 100L
    }

    @Before
    fun setup() {
        // Setup with only configuration extension
        SDKHelper.resetSDK()
        Thread.sleep(WAIT_TIME_MILLIS_LONG)

        val initializationLatch = CountDownLatch(1)
        val configUrlValidationLatch = CountDownLatch(1)
        val rulesUrlValidationLatch = CountDownLatch(1)

        // Sets up a network service to respond with a mock config and rules
        setupNetworkService(
            mapOf(
                "global.privacy" to "optedin",
                "rules.url" to "https://rules.com/rules.zip"
            ),
            TEST_RULES_RESOURCE
        ) {
            when (it) {
                TEST_CONFIG_URL -> configUrlValidationLatch.countDown()
                TEST_RULES_URL -> rulesUrlValidationLatch.countDown()
            }
        }

        MobileCore.setApplication(ApplicationProvider.getApplicationContext())
        MobileCore.setLogLevel(LoggingMode.VERBOSE)

        // Start event processing by registering extensions. Just register one extension because,
        // at least one extension is required for the completion callback to be invoked.
        MobileCore.registerExtensions(mutableListOf(Signal.EXTENSION)) {
            // Wait for the registration to complete
            initializationLatch.countDown()

            // Now configure with a app id to simulate mock config and rule download
            MobileCore.configureWithAppID(TEST_APP_ID)
        }

        assertTrue(initializationLatch.await(WAIT_TIME_MILLIS_LONG, TimeUnit.MILLISECONDS))
        // Validate that the configuration url is hit
        assertTrue(configUrlValidationLatch.await(WAIT_TIME_MILLIS_LONG, TimeUnit.MILLISECONDS))
        // Validate that the rules url from config is hit
        assertTrue(rulesUrlValidationLatch.await(WAIT_TIME_MILLIS_LONG, TimeUnit.MILLISECONDS))
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

    @After
    fun cleanup() {
        SDKHelper.resetSDK()
    }

    private fun setupNetworkService(
        mockConfigResponse: Map<String, String>,
        mockRulesResource: String,
        urlMonitor: (String) -> Unit
    ) {
        ServiceProvider.getInstance().networkService = Networking { request, callback ->
            var connection: MockNetworkResponse? = null
            when (request.url) {
                TEST_CONFIG_URL -> {
                    val configStream =
                        JSONObject(mockConfigResponse).toString().byteInputStream()
                    connection = MockNetworkResponse(
                        HttpURLConnection.HTTP_OK,
                        "OK",
                        emptyMap(),
                        configStream,
                        urlMonitor
                    )
                }

                TEST_RULES_URL -> {
                    val rulesStream =
                        this::class.java.classLoader?.getResource(
                            mockRulesResource
                        )
                            ?.openStream()!!
                    connection = MockNetworkResponse(
                        HttpURLConnection.HTTP_OK, "OK", emptyMap(), rulesStream, urlMonitor
                    )
                }
            }

            if (callback != null && connection != null) {
                callback.call(connection)
            }
            connection?.urlMonitor?.invoke(request.url)
            connection?.close()
        }
    }
}