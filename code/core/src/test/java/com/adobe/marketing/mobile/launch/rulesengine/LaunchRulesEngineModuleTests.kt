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
import com.adobe.marketing.mobile.EventHistoryRequest
import com.adobe.marketing.mobile.EventHistoryResultHandler
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.internal.eventhub.history.EventHistory
import com.adobe.marketing.mobile.launch.rulesengine.json.JSONRulesParser
import com.adobe.marketing.mobile.test.util.readTestResources
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
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
        extensionApi = Mockito.mock(ExtensionApi::class.java)
        launchRulesEngine = LaunchRulesEngine(extensionApi)
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
        val matchedRules = launchRulesEngine.process(defaultEvent)
        assertEquals(1, matchedRules.size)
        assertEquals(1, matchedRules[0].consequenceList.size)
        assertEquals("pb", matchedRules[0].consequenceList[0].type)
    }

    // TODO: https://github.com/adobe/aepsdk-core-android/issues/150
    // MobileCore.getEventHistory() will be removed, will update the test after that.
    @Ignore
    @Test
    fun `Test historical condition`() {
        val eventHistory = object : EventHistory {
            override fun recordEvent(event: Event?, handler: EventHistoryResultHandler<Boolean>?) {
                TODO("Not yet implemented")
            }

            override fun getEvents(
                eventHistoryRequests: Array<out EventHistoryRequest>?,
                enforceOrder: Boolean,
                handler: EventHistoryResultHandler<Int>?
            ) {
                handler?.call(1)
            }

            override fun deleteEvents(
                eventHistoryRequests: Array<out EventHistoryRequest>?,
                handler: EventHistoryResultHandler<Int>?
            ) {
                TODO("Not yet implemented")
            }
        }
//        PowerMockito.mockStatic(MobileCore::class.java)
//        PowerMockito.`when`(MobileCore.getEventHistory()).thenReturn(eventHistory)
        val json = readTestResources("rules_module_tests/rules_testHistory.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        assertEquals(1, launchRulesEngine.process(defaultEvent).size)
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
        assertEquals(0, launchRulesEngine.process(defaultEvent).size)
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
        val matchedRules = launchRulesEngine.process(defaultEvent)
        assertEquals(1, matchedRules.size)
        assertEquals(1, matchedRules[0].consequenceList.size)
        assertEquals("pb", matchedRules[0].consequenceList[0].type)
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
        assertEquals(0, launchRulesEngine.process(defaultEvent).size)
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
        val matchedRules = launchRulesEngine.process(defaultEvent)
        assertEquals(1, matchedRules.size)
        assertEquals(1, matchedRules[0].consequenceList.size)
        assertEquals("pb", matchedRules[0].consequenceList[0].type)
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
        assertEquals(0, launchRulesEngine.process(defaultEvent).size)
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
        val matchedRules = launchRulesEngine.process(defaultEvent)
        assertEquals(1, matchedRules.size)
        assertEquals(1, matchedRules[0].consequenceList.size)
        assertEquals("pb", matchedRules[0].consequenceList[0].type)
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
        val matchedRules = launchRulesEngine.process(defaultEvent)
        assertEquals(1, matchedRules.size)
        assertEquals(1, matchedRules[0].consequenceList.size)
        assertEquals("pb", matchedRules[0].consequenceList[0].type)
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
        assertEquals(0, launchRulesEngine.process(defaultEvent).size)
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
        val matchedRules = launchRulesEngine.process(defaultEvent)
        assertEquals(1, matchedRules.size)
        assertEquals(1, matchedRules[0].consequenceList.size)
        assertEquals("pb", matchedRules[0].consequenceList[0].type)
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
        assertEquals(0, launchRulesEngine.process(defaultEvent).size)
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
        val matchedRules = launchRulesEngine.process(defaultEvent)
        assertEquals(1, matchedRules.size)
        assertEquals(1, matchedRules[0].consequenceList.size)
        assertEquals("pb", matchedRules[0].consequenceList[0].type)
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
        assertEquals(0, launchRulesEngine.process(defaultEvent).size)
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
        val matchedRules = launchRulesEngine.process(defaultEvent)
        assertEquals(1, matchedRules.size)
        assertEquals(1, matchedRules[0].consequenceList.size)
        assertEquals("pb", matchedRules[0].consequenceList[0].type)
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
        assertEquals(0, launchRulesEngine.process(defaultEvent).size)
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
        val matchedRules = launchRulesEngine.process(defaultEvent)
        assertEquals(1, matchedRules.size)
        assertEquals(1, matchedRules[0].consequenceList.size)
        assertEquals("pb", matchedRules[0].consequenceList[0].type)
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
        assertEquals(0, launchRulesEngine.process(defaultEvent).size)
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
        val matchedRules = launchRulesEngine.process(defaultEvent)
        assertEquals(1, matchedRules.size)
        assertEquals(1, matchedRules[0].consequenceList.size)
        assertEquals("pb", matchedRules[0].consequenceList[0].type)
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
        val matchedRules = launchRulesEngine.process(defaultEvent)
        assertEquals(1, matchedRules.size)
    }

    @Test
    fun `Test transformer`() {
        val json = readTestResources("rules_module_tests/rules_testTransform.json")
        assertNotNull(json)
        val rules = JSONRulesParser.parse(json, extensionApi)
        assertNotNull(rules)
        launchRulesEngine.replaceRules(rules)
        val matchedRules = launchRulesEngine.process(
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
        assertEquals(1, matchedRules.size)
        assertEquals(1, matchedRules[0].consequenceList.size)
        assertEquals("url", matchedRules[0].consequenceList[0].type)
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
}
