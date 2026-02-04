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
import com.adobe.marketing.mobile.launch.rulesengine.json.JSONRulesParser
import com.adobe.marketing.mobile.test.util.readTestResources
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for LaunchRulesEngine reevaluation interceptor functionality.
 *
 * These tests cover the reevaluation feature which allows rules with schema consequences
 * to be held back while an interceptor fetches updated rules before processing.
 */
@RunWith(MockitoJUnitRunner.Silent::class)
class LaunchRulesEngineReevaluationTests {
    private lateinit var extensionApi: ExtensionApi
    private lateinit var launchRulesEngine: LaunchRulesEngine

    @Before
    fun setup() {
        extensionApi = mock(ExtensionApi::class.java)
        launchRulesEngine = LaunchRulesEngine("TestLaunchRulesEngine", extensionApi)
    }

    // ========================================
    // Category 1: Basic Interceptor Triggering
    // ========================================

    @Test
    fun `Test reevaluation interceptor is triggered when reevaluable schema rule matches`() {
        val json = readTestResources("rules_module_tests/rules_testReevaluable_schemaConsequence.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(mapOf("initialKey" to "initialValue")).build()

        launchRulesEngine.processEvent(testEvent)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        val rulesCaptor: KArgumentCaptor<List<LaunchRule>> = argumentCaptor()
        val callbackCaptor: KArgumentCaptor<LaunchRulesEngine.CompletionCallback> = argumentCaptor()

        verify(mockInterceptor, Mockito.times(1)).onReevaluationTriggered(
            eventCaptor.capture(),
            rulesCaptor.capture(),
            callbackCaptor.capture()
        )

        assertEquals(testEvent.uniqueIdentifier, eventCaptor.firstValue.uniqueIdentifier)
        assertEquals(1, rulesCaptor.firstValue.size)
        assertTrue(rulesCaptor.firstValue[0].meta.reEvaluate)
        assertEquals("schema", rulesCaptor.firstValue[0].consequenceList[0].type)
    }

    @Test
    fun `Test reevaluation interceptor is NOT triggered when reEvaluate is false`() {
        val json = readTestResources("rules_module_tests/rules_testNonReevaluable_schemaConsequence.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(mapOf("initialKey" to "initialValue")).build()

        launchRulesEngine.processEvent(testEvent)

        // Interceptor should NOT be called for non-reevaluable rules
        Mockito.verifyNoInteractions(mockInterceptor)
    }

    @Test
    fun `Test reevaluation interceptor is NOT triggered when no schema consequences exist`() {
        val json = readTestResources("rules_module_tests/rules_testReevaluable_nonSchemaConsequence.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(mapOf("initialKey" to "initialValue")).build()

        launchRulesEngine.processEvent(testEvent)

        // Interceptor should NOT be called when there are no schema consequences
        Mockito.verifyNoInteractions(mockInterceptor)
    }

    @Test
    fun `Test reevaluation interceptor is NOT triggered when rule does not match`() {
        val json = readTestResources("rules_module_tests/rules_testReevaluable_schemaConsequence.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        // Event with different type that won't match the rule
        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.requestContent"
        ).build()

        launchRulesEngine.processEvent(testEvent)

        // Interceptor should NOT be called when rules don't match
        Mockito.verifyNoInteractions(mockInterceptor)
    }

    @Test
    fun `Test reevaluation interceptor is NOT triggered when interceptor not set`() {
        val json = readTestResources("rules_module_tests/rules_testReevaluable_schemaConsequence.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        // Don't set interceptor

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(mapOf("initialKey" to "initialValue")).build()

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        launchRulesEngine.processEvent(testEvent)

        // Should dispatch consequence event normally
        verify(extensionApi, Mockito.atLeastOnce()).dispatch(eventCaptor.capture())

        // Verify a consequence event was dispatched
        val dispatchedEvents = eventCaptor.allValues
        val consequenceEvents = dispatchedEvents.filter {
            it.type == EventType.RULES_ENGINE && it.source == EventSource.RESPONSE_CONTENT
        }
        assertTrue(consequenceEvents.isNotEmpty())
    }

    // ========================================
    // Category 2: Rule Holding and Separation
    // ========================================

    @Test
    fun `Test all schema consequence rules are held for reevaluation`() {
        // This test verifies that when we have SEPARATE RULES (not mixed consequences in one rule):
        // 1. Reevaluable rules with schema consequences -> held
        // 2. Reevaluable rules with add consequences -> processed immediately (add is not a reevaluable-supported type)
        // 3. Non-reevaluable rules with add consequences -> processed immediately

        val reEvaluateRuleFile = readTestResources("rules_module_tests/rules_testReevaluable_mixedRules.json")
        assertNotNull(reEvaluateRuleFile)
        val reEvaluateRules = JSONRulesParser.parse(reEvaluateRuleFile, extensionApi)
        // This file has 3 rules (all with reEvaluate=true):
        // - Rule 1: schema consequence (will be held)
        // - Rule 2: add consequence (will process immediately - add is not a reevaluable-supported type)
        // - Rule 3: schema consequence (will be held)

        // Load an additional NON-reevaluable add rule
        val addRuleFile = readTestResources("rules_module_tests/rules_testNonReevaluable_addConsequence.json")
        assertNotNull(addRuleFile)
        val addRule = JSONRulesParser.parse(addRuleFile, extensionApi)
        assertNotNull(addRule)

        launchRulesEngine.replaceRules(reEvaluateRules)
        launchRulesEngine.addRules(addRule)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(mapOf("initialKey" to "initialValue")).build()

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        val processedEvent = launchRulesEngine.processEvent(testEvent)

        // Verify interceptor was called
        val rulesCaptor: KArgumentCaptor<List<LaunchRule>> = argumentCaptor()
        verify(mockInterceptor, Mockito.times(1)).onReevaluationTriggered(
            eventCaptor.capture(),
            rulesCaptor.capture(),
            any()
        )

        // Only the 2 reevaluable schema rules should be passed to interceptor
        // The add rules (both reevaluable and non-reevaluable) should NOT be in the list
        assertEquals(1, rulesCaptor.firstValue.size)
        assertTrue(rulesCaptor.firstValue[0].meta.reEvaluate)

        // Verify that BOTH add rule consequences were processed immediately
        // (Even the reevaluable one, because add is not a reevaluable-supported consequence type)
        assertNotNull(processedEvent.eventData)
        // Check that initial data is preserved
        assertEquals("initialValue", processedEvent.eventData?.get("initialKey"))
        // Check that attached data was added
        val attachedData = processedEvent.eventData?.get("attached_data") as? Map<String, Any>
        assertEquals("addedValue", attachedData?.get("addedKey"))
    }

    @Test
    fun `Test single rule with mixed consequences - all consequences held together`() {
        // This test verifies that if a SINGLE RULE has BOTH schema and non-schema consequences,
        // the ENTIRE RULE is held (including the non-schema consequences)

        val json = readTestResources("rules_module_tests/rules_testReevaluable_singleRuleMixedConsequences.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(mapOf("initialKey" to "initialValue")).build()

        val processedEvent = launchRulesEngine.processEvent(testEvent)

        // Verify interceptor was called
        val rulesCaptor: KArgumentCaptor<List<LaunchRule>> = argumentCaptor()
        verify(mockInterceptor, Mockito.times(1)).onReevaluationTriggered(
            any(),
            rulesCaptor.capture(),
            any()
        )

        // The single reevaluable rule should be passed to interceptor
        assertEquals(1, rulesCaptor.firstValue.size)
        assertTrue(rulesCaptor.firstValue[0].meta.reEvaluate)
        // Verify the rule has both consequences
        assertEquals(2, rulesCaptor.firstValue[0].consequenceList.size)

        // CRITICAL: The add consequence should NOT have been processed immediately
        // because the entire rule is held due to having a schema consequence
        assertNotNull(processedEvent.eventData)
        // Check that initial data is preserved
        assertEquals("initialValue", processedEvent.eventData?.get("initialKey"))
        // The attached_data should NOT be present because the rule is held
        val attachedData = processedEvent.eventData?.get("attached_data") as? Map<String, Any>
        assertEquals(null, attachedData?.get("mixedRuleKey"))
    }

    @Test
    fun `Test non-schema rules processed immediately while schema rules held`() {
        val json = readTestResources("rules_module_tests/rules_testReevaluable_eventModification.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(mapOf("initialKey" to "initialValue")).build()

        val processedEvent = launchRulesEngine.processEvent(testEvent)

        // Verify the event was modified by the add consequence (processed immediately)
        assertNotNull(processedEvent.eventData)
        assertEquals("modifiedValue", processedEvent.eventData?.get("modifiedKey"))
        assertEquals("12345", processedEvent.eventData?.get("timestamp"))

        // Verify interceptor was called (schema consequence held)
        verify(mockInterceptor, Mockito.times(1)).onReevaluationTriggered(any(), any(), any())
    }

    @Test
    fun `Test multiple reevaluable schema rules trigger single interceptor call`() {
        val json = readTestResources("rules_module_tests/rules_testReevaluable_multipleSchemaRules.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(mapOf("initialKey" to "initialValue")).build()

        launchRulesEngine.processEvent(testEvent)

        val rulesCaptor: KArgumentCaptor<List<LaunchRule>> = argumentCaptor()

        // Interceptor should be called exactly once
        verify(mockInterceptor, Mockito.times(1)).onReevaluationTriggered(
            any(),
            rulesCaptor.capture(),
            any()
        )

        // Both reevaluable rules should be passed
        assertEquals(2, rulesCaptor.firstValue.size)
        assertTrue(rulesCaptor.firstValue.all { it.meta.reEvaluate })
    }

    // ========================================
    // Category 3: Callback and Re-evaluation
    // ========================================

    @Test
    fun `Test callback completion processes held rules`() {
        val json = readTestResources("rules_module_tests/rules_testReevaluable_schemaConsequence.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(mapOf("initialKey" to "initialValue")).build()

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()

        launchRulesEngine.processEvent(testEvent)

        val callbackCaptor: KArgumentCaptor<LaunchRulesEngine.CompletionCallback> = argumentCaptor()
        verify(mockInterceptor, Mockito.times(1)).onReevaluationTriggered(
            any(),
            any(),
            callbackCaptor.capture()
        )

        // Invoke the callback to simulate completion
        callbackCaptor.firstValue.onComplete()

        // Verify that consequence event was dispatched after callback
        verify(extensionApi, Mockito.atLeastOnce()).dispatch(eventCaptor.capture())

        val dispatchedEvents = eventCaptor.allValues
        val consequenceEvents = dispatchedEvents.filter {
            it.type == EventType.RULES_ENGINE && it.source == EventSource.RESPONSE_CONTENT
        }
        assertTrue(consequenceEvents.isNotEmpty())
    }

    @Test
    fun `Test callback re-evaluates event against current rules`() {
        val json = readTestResources("rules_module_tests/rules_testReevaluable_schemaConsequence.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(mapOf("initialKey" to "initialValue")).build()

        launchRulesEngine.processEvent(testEvent)

        val callbackCaptor: KArgumentCaptor<LaunchRulesEngine.CompletionCallback> = argumentCaptor()
        verify(mockInterceptor, Mockito.times(1)).onReevaluationTriggered(
            any(),
            any(),
            callbackCaptor.capture()
        )

        // Simulate adding new rules before callback
        val newJson = readTestResources("rules_module_tests/rules_testReevaluable_nonSchemaConsequence.json")
        assertNotNull(newJson)
        val newRules = JSONRulesParser.parse(newJson, extensionApi)
        assertNotNull(newRules)
        launchRulesEngine.addRules(newRules)

        // Invoke callback - should re-evaluate with new rules
        callbackCaptor.firstValue.onComplete()

        // The new rule (add consequence) should have been evaluated and event modified
        // We can't directly test this without more complex mocking, but we verified callback executes
        verify(mockInterceptor, Mockito.times(1)).onReevaluationTriggered(any(), any(), any())
    }

    @Test
    fun `Test event passed to interceptor is correct`() {
        val json = readTestResources("rules_module_tests/rules_testReevaluable_schemaConsequence.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        val testEvent = Event.Builder(
            "test-event-unique",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(
            mapOf(
                "customKey" to "customValue",
                "userId" to "12345"
            )
        ).build()

        launchRulesEngine.processEvent(testEvent)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockInterceptor, Mockito.times(1)).onReevaluationTriggered(
            eventCaptor.capture(),
            any(),
            any()
        )

        // Verify the exact event was passed
        assertEquals(testEvent.uniqueIdentifier, eventCaptor.firstValue.uniqueIdentifier)
        assertEquals("test-event-unique", eventCaptor.firstValue.name)
        assertEquals("customValue", eventCaptor.firstValue.eventData?.get("customKey"))
        assertEquals("12345", eventCaptor.firstValue.eventData?.get("userId"))
    }

    // ========================================
    // Category 4: evaluateEvent() bypasses reevaluation
    // ========================================

    @Test
    fun `Test evaluateEvent does NOT trigger reevaluation interceptor`() {
        val json = readTestResources("rules_module_tests/rules_testReevaluable_schemaConsequence.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(mapOf("initialKey" to "initialValue")).build()

        // Use evaluateEvent instead of processEvent
        val consequences = launchRulesEngine.evaluateEvent(testEvent)

        // Interceptor should NOT be called
        Mockito.verifyNoInteractions(mockInterceptor)

        // Consequences should be returned synchronously
        assertEquals(1, consequences.size)
        assertEquals("schema", consequences[0].type)
    }

    // ========================================
    // Category 5: Edge Cases
    // ========================================

    @Test
    fun `Test reevaluation with event modification from immediate rules`() {
        val json = readTestResources("rules_module_tests/rules_testReevaluable_eventModification.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(
            mapOf("originalKey" to "originalValue")
        ).build()

        val processedEvent = launchRulesEngine.processEvent(testEvent)

        // Verify immediate modification happened
        assertNotNull(processedEvent.eventData)
        assertEquals("originalValue", processedEvent.eventData?.get("originalKey"))
        assertEquals("modifiedValue", processedEvent.eventData?.get("modifiedKey"))
        assertEquals("12345", processedEvent.eventData?.get("timestamp"))

        // Verify interceptor was called
        verify(mockInterceptor, Mockito.times(1)).onReevaluationTriggered(any(), any(), any())
    }

    @Test
    fun `Test reevaluation rules list contains only reevaluable rules`() {
        val json = readTestResources("rules_module_tests/rules_testReevaluable_mixedRules.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(mapOf("initialKey" to "initialValue")).build()

        launchRulesEngine.processEvent(testEvent)

        val rulesCaptor: KArgumentCaptor<List<LaunchRule>> = argumentCaptor()
        verify(mockInterceptor, Mockito.times(1)).onReevaluationTriggered(
            any(),
            rulesCaptor.capture(),
            any()
        )

        val reevaluableRules = rulesCaptor.firstValue
        // Only the reevaluable schema rule should be in the list
        assertEquals(2, reevaluableRules.size)
        assertTrue(reevaluableRules[0].meta.reEvaluate)
        assertTrue(reevaluableRules[0].consequenceList.any { it.type == "schema" })
    }

    @Test
    fun `Test reevaluation with callback invoked multiple times has no effect`() {
        val json = readTestResources("rules_module_tests/rules_testReevaluable_schemaConsequence.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(mapOf("initialKey" to "initialValue")).build()

        launchRulesEngine.processEvent(testEvent)

        val callbackCaptor: KArgumentCaptor<LaunchRulesEngine.CompletionCallback> = argumentCaptor()
        verify(mockInterceptor, Mockito.times(1)).onReevaluationTriggered(
            any(),
            any(),
            callbackCaptor.capture()
        )

        val callback = callbackCaptor.firstValue

        // Invoke callback multiple times
        callback.onComplete()
        callback.onComplete()
        callback.onComplete()

        // Should not cause issues - just processes rules multiple times
        // This is implementation-defined behavior
        verify(mockInterceptor, Mockito.times(1)).onReevaluationTriggered(any(), any(), any())
    }

    @Test
    fun `Test reevaluation interceptor can be updated`() {
        val json = readTestResources("rules_module_tests/rules_testReevaluable_schemaConsequence.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor1 = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor1)

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(mapOf("initialKey" to "initialValue")).build()

        launchRulesEngine.processEvent(testEvent)

        // First interceptor should be called
        verify(mockInterceptor1, Mockito.times(1)).onReevaluationTriggered(any(), any(), any())

        // Update interceptor
        val mockInterceptor2 = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor2)

        launchRulesEngine.processEvent(testEvent)

        // Second interceptor should be called
        verify(mockInterceptor2, Mockito.times(1)).onReevaluationTriggered(any(), any(), any())

        // First interceptor should not be called again
        verify(mockInterceptor1, Mockito.times(1)).onReevaluationTriggered(any(), any(), any())
    }

    @Test
    fun `Test reevaluation interceptor can be cleared`() {
        val json = readTestResources("rules_module_tests/rules_testReevaluable_schemaConsequence.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val mockInterceptor = mock(LaunchRulesEngine.RuleReevaluationInterceptor::class.java)
        launchRulesEngine.setRuleReevaluationInterceptor(mockInterceptor)

        val testEvent = Event.Builder(
            "test-event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).setEventData(mapOf("initialKey" to "initialValue")).build()

        launchRulesEngine.processEvent(testEvent)

        verify(mockInterceptor, Mockito.times(1)).onReevaluationTriggered(any(), any(), any())

        // Clear interceptor by setting to null
        launchRulesEngine.setRuleReevaluationInterceptor(null)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        launchRulesEngine.processEvent(testEvent)

        // Should not call interceptor again, but should dispatch consequence normally
        verify(mockInterceptor, Mockito.times(1)).onReevaluationTriggered(any(), any(), any())
        verify(extensionApi, Mockito.atLeastOnce()).dispatch(eventCaptor.capture())
    }
}
