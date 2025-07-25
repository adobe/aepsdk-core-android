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

import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.AdobeError
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventHistoryResult
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.launch.rulesengine.json.JSONRulesParser
import com.adobe.marketing.mobile.rulesengine.RulesEngine
import com.adobe.marketing.mobile.test.util.readTestResources
import com.adobe.marketing.mobile.util.DataReader
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(MockitoJUnitRunner.Silent::class)
class LaunchRulesEngineModuleTests {
    private lateinit var extensionApi: ExtensionApi

    private lateinit var launchRulesEngine: LaunchRulesEngine

    private val defaultEvent = Event.Builder(
        "event",
        "com.adobe.eventType.lifecycle",
        "com.adobe.eventSource.responseContent"
    ).setEventData(
        mapOf(
            "lifecyclecontextdata" to mapOf(
                "launchevent" to "LaunchEvent"
            )
        )
    ).build()

    @Before
    fun setup() {
        extensionApi = mock(ExtensionApi::class.java)
        launchRulesEngine = LaunchRulesEngine("TestLaunchRulesEngine", extensionApi)
    }

    @Test
    fun `Test group condition`() {
        val json = readTestResources("rules_module_tests/rules_testGroupLogicalOperators.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "carriername" to "AT&T"
                        )
                    )
                )
            )

        val matchedConsequence = launchRulesEngine.evaluateEvent(defaultEvent)
        // val matchedRules = launchRulesEngine.process(defaultEvent)
        assertEquals(1, matchedConsequence.size)
        assertEquals("pb", matchedConsequence[0].type)
    }

    @Test
    fun `Test group condition with key embedded inside list`() {
        val json = readTestResources("rules_module_tests/rules_testGroupLogicalOperatorsWithKeysEmbeddedInList.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        val contentCardDismissEvent = Event.Builder(
            "contentCardDismiss",
            EventType.EDGE,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "xdm" to mapOf(
                    "eventType" to "decisioning.propositionDismiss",
                    "_experience" to mapOf(
                        "decisioning" to mapOf(
                            "propositions" to listOf(
                                mapOf(
                                    "scopeDetails" to mapOf(
                                        "activity" to mapOf(
                                            "id" to "a43122c4-bf19-499f-b507-087a028d1769#fa035681-15ce-488e-859e-200bb2ca90ac"
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ).build()
        val matchedConsequence = launchRulesEngine.evaluateEvent(contentCardDismissEvent)
        assertEquals(1, matchedConsequence.size)
        assertEquals("schema", matchedConsequence[0].type)
    }

    @Test
    fun `Test historical condition any search type success`() {
        val captor = argumentCaptor<AdobeCallbackWithError<Array<EventHistoryResult>>>()
        Mockito.`when`(extensionApi.getHistoricalEvents(any(), Mockito.anyBoolean(), captor.capture()))
            .doAnswer {
                captor.firstValue.call(arrayOf(EventHistoryResult(1)))
            }

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)

        val json = readTestResources("rules_module_tests/rules_testHistoryAny.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        assertEquals(1, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test historical condition any search type database failure `() {
        val captor = argumentCaptor<AdobeCallbackWithError<Array<EventHistoryResult>>>()
        Mockito.`when`(extensionApi.getHistoricalEvents(any(), Mockito.anyBoolean(), captor.capture()))
            .doAnswer {
                captor.firstValue.call(arrayOf(EventHistoryResult(-1)))
            }

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)

        val json = readTestResources("rules_module_tests/rules_testHistoryAny.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test historical condition any search type event not found `() {
        val captor = argumentCaptor<AdobeCallbackWithError<Array<EventHistoryResult>>>()
        Mockito.`when`(extensionApi.getHistoricalEvents(any(), Mockito.anyBoolean(), captor.capture()))
            .doAnswer {
                captor.firstValue.call(arrayOf(EventHistoryResult(0)))
            }

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)

        val json = readTestResources("rules_module_tests/rules_testHistoryAny.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test historical condition ordered search type success`() {
        val captor = argumentCaptor<AdobeCallbackWithError<Array<EventHistoryResult>>>()
        Mockito.`when`(extensionApi.getHistoricalEvents(any(), Mockito.anyBoolean(), captor.capture()))
            .doAnswer {
                captor.firstValue.call(arrayOf(EventHistoryResult(1), EventHistoryResult(1)))
            }

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)

        val json = readTestResources("rules_module_tests/rules_testHistoryOrdered.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        assertEquals(1, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test historical condition ordered search type database failure`() {
        val captor = argumentCaptor<AdobeCallbackWithError<Array<EventHistoryResult>>>()
        Mockito.`when`(extensionApi.getHistoricalEvents(any(), Mockito.anyBoolean(), captor.capture()))
            .doAnswer {
                captor.firstValue.fail(AdobeError.DATABASE_ERROR)
            }

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)

        val json = readTestResources("rules_module_tests/rules_testHistoryOrdered.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test historical condition ordered search type event not found`() {
        val captor = argumentCaptor<AdobeCallbackWithError<Array<EventHistoryResult>>>()
        Mockito.`when`(extensionApi.getHistoricalEvents(any(), Mockito.anyBoolean(), captor.capture()))
            .doAnswer {
                captor.firstValue.call(arrayOf(EventHistoryResult(0), EventHistoryResult(1)))
            }

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)

        val json = readTestResources("rules_module_tests/rules_testHistoryOrdered.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test historical condition mostRecent search type success`() {
        val captor = argumentCaptor<AdobeCallbackWithError<Array<EventHistoryResult>>>()
        Mockito.`when`(extensionApi.getHistoricalEvents(any(), Mockito.anyBoolean(), captor.capture()))
            .doAnswer {
                // Simulate two historical results:
                // - First has an older newestOccurrence
                // - Second is the most recent (index 1 should match the rule's "value": 1)
                captor.firstValue.call(
                    arrayOf(
                        EventHistoryResult(1, oldestOccurrence = null, newestOccurrence = 1000L),
                        EventHistoryResult(1, oldestOccurrence = null, newestOccurrence = 2000L)
                    )
                )
            }

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)

        val json = readTestResources("rules_module_tests/rules_testHistoryOrdered.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        assertEquals(1, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test historical condition mostRecent search type database failure`() {
        val captor = argumentCaptor<AdobeCallbackWithError<Array<EventHistoryResult>>>()
        Mockito.`when`(extensionApi.getHistoricalEvents(any(), Mockito.anyBoolean(), captor.capture()))
            .doAnswer {
                captor.firstValue.call(
                    arrayOf(
                        EventHistoryResult(-1, oldestOccurrence = null, newestOccurrence = null),
                        EventHistoryResult(0, oldestOccurrence = null, newestOccurrence = null)
                    )
                )
            }

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)

        val json = readTestResources("rules_module_tests/rules_testHistoryMostRecent.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test historical condition mostRecent search type failure callback`() {
        val captor = argumentCaptor<AdobeCallbackWithError<Array<EventHistoryResult>>>()
        Mockito.`when`(extensionApi.getHistoricalEvents(any(), Mockito.anyBoolean(), captor.capture()))
            .doAnswer {
                captor.firstValue.fail(AdobeError.DATABASE_ERROR)
            }

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)

        val json = readTestResources("rules_module_tests/rules_testHistoryMostRecent.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test historical condition mostRecent search type event not found`() {
        val captor = argumentCaptor<AdobeCallbackWithError<Array<EventHistoryResult>>>()
        Mockito.`when`(extensionApi.getHistoricalEvents(any(), Mockito.anyBoolean(), captor.capture()))
            .doAnswer {
                captor.firstValue.call(
                    arrayOf(
                        EventHistoryResult(0, oldestOccurrence = null, newestOccurrence = null),
                        EventHistoryResult(1, oldestOccurrence = null, newestOccurrence = 100L)
                    )
                )
            }

        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)

        val json = readTestResources("rules_module_tests/rules_testHistoryMostRecent.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)

        assertEquals(1, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test matcher condition (co) - negative `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherCo.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "carriername" to "Verizon"
                        )
                    )
                )
            )
        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test matcher condition (co) - positive `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherCo.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "carriername" to "AT&T"
                        )
                    )
                )
            )
        val matchedConsequences = launchRulesEngine.evaluateEvent(defaultEvent)
        assertEquals(1, matchedConsequences.size)
        assertEquals("pb", matchedConsequences[0].type)
    }

    @Test
    fun `Test matcher condition (ge) - negative `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherGe.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "launches" to 1
                        )
                    )
                )
            )
        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test matcher condition (ge) - positive `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherGe.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "launches" to 2
                        )
                    )
                )
            )
        val matchedConsequences = launchRulesEngine.evaluateEvent(defaultEvent)
        assertEquals(1, matchedConsequences.size)
        assertEquals("pb", matchedConsequences[0].type)
    }

    @Test
    fun `Test matcher condition (gt) - negative `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherGt.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "launches" to 2
                        )
                    )
                )
            )
        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test matcher condition (gt) - positive `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherGt.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "launches" to 3
                        )
                    )
                )
            )
        val matchedConsequences = launchRulesEngine.evaluateEvent(defaultEvent)
        assertEquals(1, matchedConsequences.size)
        assertEquals("pb", matchedConsequences[0].type)
    }

    @Test
    fun `Test matcher condition (gt) with different types - String vs Int `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherGt_2_types.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "launches" to 2
                        )
                    )
                )
            )
        val matchedConsequences = launchRulesEngine.evaluateEvent(defaultEvent)
        assertEquals(1, matchedConsequences.size)
        assertEquals("pb", matchedConsequences[0].type)
    }

    @Test
    fun `Test matcher condition (le) - negative `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherLe.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "launches" to 3
                        )
                    )
                )
            )
        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test matcher condition (le) - positive `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherLe.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "launches" to 2
                        )
                    )
                )
            )
        val matchedConsequences = launchRulesEngine.evaluateEvent(defaultEvent)
        assertEquals(1, matchedConsequences.size)
        assertEquals("pb", matchedConsequences[0].type)
    }

    @Test
    fun `Test matcher condition (lt) - negative `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherLt.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "launches" to 2
                        )
                    )
                )
            )
        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test matcher condition (lt) - positive `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherLt.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "launches" to 1
                        )
                    )
                )
            )
        val matchedConsequences = launchRulesEngine.evaluateEvent(defaultEvent)
        assertEquals(1, matchedConsequences.size)
        assertEquals("pb", matchedConsequences[0].type)
    }

    @Test
    fun `Test matcher condition (nc) - negative `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherNc.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "carriername" to "AT&T"
                        )
                    )
                )
            )
        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test matcher condition (nc) - positive `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherNc.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "carriername" to "Verizon"
                        )
                    )
                )
            )
        val matchedConsequences = launchRulesEngine.evaluateEvent(defaultEvent)
        assertEquals(1, matchedConsequences.size)
        assertEquals("pb", matchedConsequences[0].type)
    }

    @Test
    fun `Test matcher condition (ne) - negative `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherNe.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "carriername" to "AT&T"
                        )
                    )
                )
            )
        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test matcher condition (ne) - positive `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherNe.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "carriername" to "Verizon"
                        )
                    )
                )
            )
        val matchedConsequences = launchRulesEngine.evaluateEvent(defaultEvent)
        assertEquals(1, matchedConsequences.size)
        assertEquals("pb", matchedConsequences[0].type)
    }

    @Test
    fun `Test matcher condition multiple Values (ne) - negative `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherNe_multipleValues.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "carriername" to "AT&T"
                        )
                    )
                )
            )
        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test matcher condition multiple Values (ne) - positive `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherNe_multipleValues.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "carriername" to "T-Mobile"
                        )
                    )
                )
            )
        val matchedConsequences = launchRulesEngine.evaluateEvent(defaultEvent)
        assertEquals(1, matchedConsequences.size)
        assertEquals("pb", matchedConsequences[0].type)
    }

    @Test
    fun `Test matcher condition (nx) - negative `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherNx.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "carriername" to "AT&T"
                        )
                    )
                )
            )
        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test matcher condition (nx) - positive `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherNx.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "key" to "value"
                        )
                    )
                )
            )
        val matchedConsequences = launchRulesEngine.evaluateEvent(defaultEvent)
        assertEquals(1, matchedConsequences.size)
        assertEquals("pb", matchedConsequences[0].type)
    }

    @Test
    fun `Test matcher condition - with different types`() {
        val json =
            readTestResources("rules_module_tests/rules_testMatcherWithDifferentTypesOfParameters.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "launches" to 3
                        )
                    )
                )
            )
        val matchedConsequences = launchRulesEngine.evaluateEvent(defaultEvent)
        assertEquals(1, matchedConsequences.size)
        assertEquals("pb", matchedConsequences[0].type)
    }

    @Test
    fun `Test transformer`() {
        val json = readTestResources("rules_module_tests/rules_testTransform.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        val matchedConsequences = launchRulesEngine.evaluateEvent(
            Event.Builder(
                "event",
                "com.adobe.eventType.lifecycle",
                "com.adobe.eventSource.responseContent"
            ).setEventData(
                mapOf(
                    "lifecyclecontextdata" to mapOf(
                        "numberString" to "3",
                        "booleanValue" to true,
                        "intValue" to 5,
                        "doubleValue" to 10.3
                    )
                )
            ).build()
        )

        assertEquals(1, matchedConsequences.size)
        assertEquals("url", matchedConsequences[0].type)
    }

    @Test
    fun `Test add rules`() {
        val firstJson = readTestResources("rules_happy/rules.json")
        val secondJson = readTestResources("rules_module_tests/consequence_rules_1.json")
        assertNotNull(firstJson)
        assertNotNull(secondJson)
        val rules = JSONRulesParser.parse(firstJson, extensionApi)
        val newRules = JSONRulesParser.parse(secondJson, extensionApi)
        assertNotNull(rules)
        assertNotNull(newRules)
        launchRulesEngine.replaceRules(rules)
        assertEquals(2, launchRulesEngine.rules.size)
        launchRulesEngine.addRules(newRules)
        assertEquals(4, launchRulesEngine.rules.size)
    }

    @Test
    fun `Cache incoming events if rules are not set`() {
        repeat(100) {
            launchRulesEngine.processEvent(
                Event.Builder("event-$it", "type", "source").build()
            )
        }
        assertEquals(100, launchRulesEngine.cachedEventCount)
    }

    @Test
    fun `Reprocess cached events when rules are ready`() {
        repeat(10) {
            launchRulesEngine.processEvent(
                Event.Builder("event-$it", "type", "source").build()
            )
        }
        assertEquals(10, launchRulesEngine.cachedEventCount)
        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        launchRulesEngine.replaceRules(listOf())

        verify(extensionApi, Mockito.times(1)).dispatch(eventCaptor.capture())
        assertNotNull(eventCaptor.firstValue)
        assertEquals("TestLaunchRulesEngine", eventCaptor.firstValue.name)
        assertEquals(EventType.RULES_ENGINE, eventCaptor.firstValue.type)
        assertEquals(EventSource.REQUEST_RESET, eventCaptor.firstValue.source)

        launchRulesEngine.processEvent(eventCaptor.firstValue)

        assertEquals(0, launchRulesEngine.cachedEventCount)
    }

    @Test
    fun `Do not reprocess cached events on a reset event from a different engine`() {
        repeat(10) {
            launchRulesEngine.processEvent(
                Event.Builder("event-$it", "type", "source").build()
            )
        }
        assertEquals(10, launchRulesEngine.cachedEventCount)

        // simulate incidence of reset event with a different name
        val differentEngineResetEvent = Event.Builder("SomeOtherEngineName", EventType.RULES_ENGINE, EventSource.REQUEST_RESET)
            .setEventData(mapOf(LaunchRulesEngine.RULES_ENGINE_NAME to "SomeOtherEngineName"))
            .build()
        launchRulesEngine.processEvent(differentEngineResetEvent)

        // 10 cached events and this unmatched event is treated as any other event
        assertEquals(11, launchRulesEngine.cachedEventCount)

        // incidence of reset event with correct name
        val thisEngineResetEvent = Event.Builder("TestLaunchRulesEngine", EventType.RULES_ENGINE, EventSource.REQUEST_RESET)
            .setEventData(mapOf(LaunchRulesEngine.RULES_ENGINE_NAME to "TestLaunchRulesEngine"))
            .build()
        launchRulesEngine.processEvent(thisEngineResetEvent)
        assertEquals(0, launchRulesEngine.cachedEventCount)
    }

    @Test
    fun `Reprocess cached events in the right order`() {
        val mockRulesEngine: RulesEngine<LaunchRule> = mock(RulesEngine::class.java) as RulesEngine<LaunchRule>
        val mockLaunchRulesConsequence = mock(LaunchRulesConsequence::class.java)
        val launchRulesEngine = LaunchRulesEngine(
            "TestLaunchRulesEngine",
            extensionApi,
            mockRulesEngine,
            mockLaunchRulesConsequence
        )

        repeat(10) {
            launchRulesEngine.processEvent(
                Event.Builder("event-$it", "type", "source").build()
            )
        }
        assertEquals(10, launchRulesEngine.cachedEventCount)

        Mockito.reset(mockLaunchRulesConsequence)

        // simulate reset event
        launchRulesEngine.replaceRules(listOf())

        val resetEventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(extensionApi, Mockito.times(1)).dispatch(resetEventCaptor.capture())
        val capturedResetRulesEvent = resetEventCaptor.firstValue
        assertNotNull(capturedResetRulesEvent)
        assertEquals(EventType.RULES_ENGINE, capturedResetRulesEvent.type)
        assertEquals(EventSource.REQUEST_RESET, capturedResetRulesEvent.source)
        assertEquals(
            "TestLaunchRulesEngine",
            DataReader.optString(capturedResetRulesEvent.eventData, LaunchRulesEngine.RULES_ENGINE_NAME, "")
        )

        // simulate incidence of reset rules event above
        launchRulesEngine.processEvent(capturedResetRulesEvent)

        val processedEventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockLaunchRulesConsequence, Mockito.times(11))
            .process(processedEventCaptor.capture(), org.mockito.kotlin.any())
        assertEquals(11, processedEventCaptor.allValues.size)

        // 0 - 10 events to be processed are cached events in order
        for (index in 0 until processedEventCaptor.allValues.size - 1) {
            assertEquals("event-$index", processedEventCaptor.allValues[index].name)
        }
        // final 11 th event to be processed is the reset event
        assertEquals(capturedResetRulesEvent, processedEventCaptor.allValues[10])

        // verify that all cached events are cleared
        assertEquals(0, launchRulesEngine.cachedEventCount)
    }

    @Test
    fun `Test evaluateConsequence when consequences are present`() {
        val json = readTestResources("rules_module_tests/rules_testEvaluateConsequenceWithValidConsequences.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "key" to "value"
                        )
                    )
                )
            )

        val matchedConsequences: List<RuleConsequence> = launchRulesEngine.evaluateEvent(defaultEvent)
        assertEquals(3, matchedConsequences.size)
        for (consequence in matchedConsequences) {
            assertEquals("ajoInbound", consequence.type)
            assertEquals("ajoFeedItem", consequence.detail["type"])
        }
    }

    @Test
    fun `Test evaluateConsequence when no consequences are present`() {
        val json = readTestResources("rules_module_tests/rules_testEvaluateConsequenceWithNoConsequences.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "key" to "value"
                        )
                    )
                )
            )

        val matchedConsequences: List<RuleConsequence> = launchRulesEngine.evaluateEvent(defaultEvent)
        assertEquals(0, matchedConsequences.size)
    }

    @Test
    fun `Do nothing on null rule replacement`() {
        repeat(10) {
            launchRulesEngine.processEvent(
                Event.Builder("event-$it", "type", "source").build()
            )
        }
        assertEquals(10, launchRulesEngine.cachedEventCount)
        launchRulesEngine.replaceRules(null)
        Mockito.verifyNoInteractions(extensionApi)
        assertEquals(10, launchRulesEngine.cachedEventCount)
    }

    @Test
    fun `Test matcher condition multiple Values (nc) - negative `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherNc_multipleValues.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "carriername" to "AT&T"
                        )
                    )
                )
            )
        assertEquals(0, launchRulesEngine.evaluateEvent(defaultEvent).size)
    }

    @Test
    fun `Test matcher condition multiple Values (nc) - positive `() {
        val json = readTestResources("rules_module_tests/rules_testMatcherNc_multipleValues.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        Mockito.`when`(extensionApi.getSharedState(anyString(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(
                SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "lifecyclecontextdata" to mapOf(
                            "carriername" to "T-Mobile"
                        )
                    )
                )
            )
        val matchedConsequences = launchRulesEngine.evaluateEvent(defaultEvent)
        assertEquals(1, matchedConsequences.size)
        assertEquals("pb", matchedConsequences[0].type)
    }
}
