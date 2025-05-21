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
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRuleTransformer.createTransforming
import com.adobe.marketing.mobile.launch.rulesengine.json.JSONRulesParser
import com.adobe.marketing.mobile.rulesengine.ConditionEvaluator
import com.adobe.marketing.mobile.rulesengine.RulesEngine
import com.adobe.marketing.mobile.test.util.readTestResources
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.any
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.reset
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner.Silent::class)
class LaunchRulesConsequenceTests {

    private lateinit var extensionApi: ExtensionApi

    // private lateinit var launchRulesEngine: LaunchRulesEngine
    private lateinit var rulesEngine: RulesEngine<LaunchRule>
    private lateinit var launchRulesConsequence: LaunchRulesConsequence
    private var defaultEvent = Event.Builder(
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
        rulesEngine = RulesEngine(
            ConditionEvaluator(ConditionEvaluator.Option.CASE_INSENSITIVE),
            createTransforming()
        )
        launchRulesConsequence = LaunchRulesConsequence(extensionApi)
    }

    @Test
    fun `Test Attach Data`() {
        // / Given: a launch rule to attach data to event

        //    ---------- attach data rule ----------
        //        "eventdata": {
        //            "attached_data": {
        //                "key1": "value1",
        //                "launches": "{%~state.com.adobe.module.lifecycle/lifecyclecontextdata.launches%}"
        //            }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testAttachData.json")

        // / When: evaluating a launch event

        //    ------------ launch event ------------
        //        "eventdata": {
        //            "lifecyclecontextdata": {
        //                "launchevent": "LaunchEvent"
        //            }
        //        }
        //    --------------------------------------
        `when`(extensionApi.getSharedState(anyString(), any(), anyBoolean(), any())).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "lifecyclecontextdata" to mapOf(
                        "carriername" to "AT&T"
                    )
                )
            )
        )

        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(defaultEvent, extensionApi))
        val processedEvent =
            launchRulesConsequence.process(defaultEvent, matchedRules)

        // / Then: no consequence event will be dispatched
        verify(extensionApi, never()).dispatch(any())

        val attachedData = processedEvent.eventData?.get("attached_data") as Map<*, *>

        // / Then: ["key1": "value1"] should be attached to above launch event
        assertEquals("value1", attachedData["key1"])

        // / Then: should not get "launches" value from (lifecycle) shared state
        assertEquals("", attachedData["launches"])
    }

    @Test
    fun `Test Attach Data Array`() {
        // / Given: a launch rule to attach data to event

        //    ---------- attach data rule ----------
        //        "eventdata": {
        //          "attached_data_array": [
        //            "{%~state.com.adobe.module.lifecycle/lifecyclecontextdata.carriername%}",
        //            "testStringTopLevel",
        //            {
        //                "testDictKey": "testVal",
        //                "osversionNested": "{%~state.com.adobe.module.lifecycle/lifecyclecontextdata.osversion%}"
        //                "numbers": 123
        //
        //            }, [
        //                "{%~state.com.adobe.module.lifecycle/lifecyclecontextdata.osversion%}",
        //                "testStringInsideNestedArray"
        //            ]
        //          ]
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testAttachData_array.json")

        // / When: evaluating a launch event

        //    ------------ launch event ------------
        //        "eventdata": {
        //            "lifecyclecontextdata": {
        //                "launchevent": "LaunchEvent"
        //            }
        //        }
        //    --------------------------------------
        `when`(extensionApi.getSharedState(anyString(), any(), anyBoolean(), any())).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "lifecyclecontextdata" to mapOf(
                        "carriername" to "AT&T",
                        "osversion" to "27"
                    )
                )
            )
        )

        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(defaultEvent, extensionApi))
        val processedEvent =
            launchRulesConsequence.process(defaultEvent, matchedRules)

        // / Then: no consequence event will be dispatched
        verify(extensionApi, never()).dispatch(any())

        val attachedDataArray = processedEvent.eventData?.get("attached_data_array") as List<*>

        // / Then: "{%~state.com.adobe.module.lifecycle/lifecyclecontextdata.carriername%}" should be replaced with "AT&T"
        assertEquals("AT&T", attachedDataArray[0])

        // / Then: "testStringTopLevel" should not be changed
        assertEquals("testStringTopLevel", attachedDataArray[1])

        // / Then: the nested map should be handled correctly
        val nestedMap = attachedDataArray[2] as Map<*, *>

        assertEquals("testVal", nestedMap["testDictKey"] as String)
        assertEquals("27", nestedMap["osversionNested"] as String)
        assertTrue { nestedMap["number"] is Int }
        assertEquals(123, nestedMap["number"] as Int)

        // / Then: the data array should be handled correctly
        val dataArray = attachedDataArray[3] as List<*>
        assertEquals("27", dataArray[0] as String)
        assertEquals("testStringInsideNestedArray", dataArray[1] as String)
    }

    @Test
    fun `Test Attach Data Invalid Json`() {
        // / Given: a launch rule to attach data to event

        //    ---------- attach data rule ----------
        //        "eventdata_xyz": {
        //            "attached_data": {
        //                "key1": "value1",
        //                "launches": "{%~state.com.adobe.module.lifecycle/lifecyclecontextdata.launches%}"
        //            }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testAttachData_invalidJson.json")

        // / When: evaluating a launch event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(defaultEvent, extensionApi))
        val processedEvent =
            launchRulesConsequence.process(defaultEvent, matchedRules)

        // / Then: no consequence event will be dispatched
        verify(extensionApi, never()).dispatch(any())

        // / Then: no data should not be attached to original launch event
        val attachedData = processedEvent.eventData?.get("attached_data")
        assertNull(attachedData)
    }

    @Test
    fun `Test Modify Data`() {
        // / Given: a launch rule to modify event data

        //    ---------- modify data rule ----------
        //        "eventdata": {
        //            "lifecyclecontextdata": {
        //                "launches": "{%~state.com.adobe.module.lifecycle/lifecyclecontextdata.launches%}",
        //                "launchevent": null
        //            }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testModifyData.json")

        // / When: evaluating a launch event

        //    ------------ launch event ------------
        //        "eventdata": {
        //            "lifecyclecontextdata": {
        //                "launchevent": "LaunchEvent"
        //            }
        //        }
        //    --------------------------------------
        `when`(extensionApi.getSharedState(anyString(), any(), anyBoolean(), any())).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "lifecyclecontextdata" to mapOf(
                        "carriername" to "AT&T",
                        "launches" to 2
                    )
                )
            )
        )

        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(defaultEvent, extensionApi))
        val processedEvent =
            launchRulesConsequence.process(defaultEvent, matchedRules)

        // / Then: no consequence event will be dispatched
        verify(extensionApi, never()).dispatch(any())

        // / Then: "launchevent" should be removed from event data
        val lifecycleContextData =
            processedEvent.eventData?.get("lifecyclecontextdata") as Map<*, *>
        assertNull(lifecycleContextData["launchevent"])

        // / Then: should get "launches" value from (lifecycle) shared state
        assertEquals("2", lifecycleContextData["launches"])
    }

    @Test
    fun `Test Modify Data Invalid Json`() {
        // / Given: a launch rule to modify event data

        //    ---------- modify data rule ----------
        //        "eventdata_xyz": {
        //            "lifecyclecontextdata": {
        //                "launches": "{%~state.com.adobe.module.lifecycle/lifecyclecontextdata.launches%}",
        //                "launchevent": null
        //            }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testModifyData_invalidJson.json")

        // / When: evaluating a launch event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(defaultEvent, extensionApi))
        val processedEvent =
            launchRulesConsequence.process(defaultEvent, matchedRules)

        // / Then: no consequence event will be dispatched
        verify(extensionApi, never()).dispatch(any())

        // / Then: "launchevent" should not be removed from event data
        val lifecycleContextData =
            processedEvent.eventData?.get("lifecyclecontextdata") as Map<*, *>
        assertNotNull(lifecycleContextData["launchevent"])
        assertNull(lifecycleContextData["launches"])
    }

    @Test
    fun `Test Dispatch Event Copy`() {
        // / Given: a launch rule to dispatch an event which copies the triggering event data

        //    ---------- dispatch event rule ----------
        //        "detail": {
        //          "type" : "com.adobe.eventType.edge",
        //          "source" : "com.adobe.eventSource.requestContent",
        //          "eventdataaction" : "copy"
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testDispatchEventCopy.json")

        val event = Event.Builder(
            "Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch"
        )
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        val processedEvent = launchRulesConsequence.process(event, matchedRules)

        // / Then: One consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
        assertEquals("com.adobe.eventType.edge", dispatchedEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.requestContent", dispatchedEventCaptor.value.source)
        assertEquals(event.eventData, dispatchedEventCaptor.value.eventData)
        assertEquals(event.uniqueIdentifier, dispatchedEventCaptor.value.parentID)

        // verify original event is unchanged
        assertEquals(event, processedEvent)
    }

    @Test
    fun `Test Dispatch Event Copy No Event  Data`() {
        // / Given: a launch rule to dispatch an event which copies the triggering event data

        //    ---------- dispatch event rule ----------
        //        "detail": {
        //          "type" : "com.adobe.eventType.edge",
        //          "source" : "com.adobe.eventSource.requestContent",
        //          "eventdataaction" : "copy"
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testDispatchEventCopy.json")

        val event = Event.Builder(
            "Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch"
        )
            .setEventData(null)
            .build()

        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        val processedEvent = launchRulesConsequence.process(event, matchedRules)

        // / Then: One consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
        assertEquals("com.adobe.eventType.edge", dispatchedEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.requestContent", dispatchedEventCaptor.value.source)
        assertEquals(null, dispatchedEventCaptor.value.eventData)
        assertEquals(event.uniqueIdentifier, dispatchedEventCaptor.value.parentID)

        // verify original event is unchanged
        assertEquals(event, processedEvent)
    }

    @Test
    fun `Test Dispatch Event Copy New Event Data`() {
        // / Given: a launch rule to dispatch an event which adds new event data

        //    ---------- dispatch event rule ----------
        //        "detail": {
        //          "type" : "com.adobe.eventType.edge",
        //          "source" : "com.adobe.eventSource.requestContent",
        //          "eventdataaction" : "new",
        //          "eventdata" : {
        //            "key" : "value",
        //            "key.subkey" : "subvalue",
        //            "launches": "{%~state.com.adobe.module.lifecycle/lifecyclecontextdata.launches%}",
        //          }
        //        }
        //    --------------------------------------

        resetRulesEngine("rules_module_tests/consequence_rules_testDispatchEventNewData.json")

        val event = Event.Builder(
            "Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch"
        )
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        val processedEvent = launchRulesConsequence.process(event, matchedRules)

        // / Then: One consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
        assertEquals("com.adobe.eventType.edge", dispatchedEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.requestContent", dispatchedEventCaptor.value.source)
        assertEquals("value", dispatchedEventCaptor.value.eventData["key"])
        assertEquals("subvalue", dispatchedEventCaptor.value.eventData["key.subkey"])
        assertEquals(event.uniqueIdentifier, dispatchedEventCaptor.value.parentID)

        // verify original event is unchanged
        assertEquals(event, processedEvent)
    }

    @Test
    fun `Test Dispatch Event Copy New No Event Data`() {
        // / Given: a launch rule to dispatch an event which adds new event event data, but none is configured

        //    ---------- dispatch event rule ----------
        //        "detail": {
        //          "type" : "com.adobe.eventType.edge",
        //          "source" : "com.adobe.eventSource.requestContent",
        //          "eventdataaction" : "new"
        //        }
        //    --------------------------------------

        resetRulesEngine("rules_module_tests/consequence_rules_testDispatchEventNewNoData.json")

        val event = Event.Builder(
            "Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch"
        )
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        val processedEvent = launchRulesConsequence.process(event, matchedRules)

        // / Then: One consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
        assertEquals("com.adobe.eventType.edge", dispatchedEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.requestContent", dispatchedEventCaptor.value.source)
        assertEquals(null, dispatchedEventCaptor.value.eventData)
        assertEquals(event.uniqueIdentifier, dispatchedEventCaptor.value.parentID)

        // verify original event is unchanged
        assertEquals(event, processedEvent)
    }

    @Test
    fun `Test Dispatch Event Invalid Action `() {
        // / Given: a launch rule to dispatch an event with invalid action

        //    ---------- dispatch event rule ----------
        //        "detail": {
        //          "type" : "com.adobe.eventType.edge",
        //          "source" : "com.adobe.eventSource.requestContent",
        //          "eventdataaction" : "invalid",
        //        }
        //    --------------------------------------

        resetRulesEngine("rules_module_tests/consequence_rules_testDispatchEventInvalidAction.json")
        val event = Event.Builder(
            "Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch"
        )
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        val processedEvent = launchRulesConsequence.process(event, matchedRules)

        // / Then: No consequence event will be dispatched
        verify(extensionApi, never()).dispatch(any())

        // verify original event is unchanged
        assertEquals(event, processedEvent)
    }

    @Test
    fun `Test Dispatch Event No Action `() {
        // / Given: a launch rule to dispatch an event with no action specified in details

        //    ---------- dispatch event rule ----------
        //        "detail": {
        //          "type" : "com.adobe.eventType.edge",
        //          "source" : "com.adobe.eventSource.requestContent"
        //        }
        //    --------------------------------------

        resetRulesEngine("rules_module_tests/consequence_rules_testDispatchEventNoAction.json")
        val event = Event.Builder(
            "Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch"
        )
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        val processedEvent = launchRulesConsequence.process(event, matchedRules)

        // / Then: No consequence event will be dispatched
        verify(extensionApi, never()).dispatch(any())

        // verify original event is unchanged
        assertEquals(event, processedEvent)
    }

    @Test
    fun `Test Dispatch Event No Type `() {
        // / Given: a launch rule to dispatch an event with no type specified in details

        //    ---------- dispatch event rule ----------
        //        "detail": {
        //          "source" : "com.adobe.eventSource.requestContent",
        //          "eventdataaction" : "copy"
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testDispatchEventNoType.json")
        val event = Event.Builder(
            "Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch"
        )
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        val processedEvent = launchRulesConsequence.process(event, matchedRules)

        // / Then: No consequence event will be dispatched
        verify(extensionApi, never()).dispatch(any())

        // verify original event is unchanged
        assertEquals(event, processedEvent)
    }

    @Test
    fun `Test Dispatch Event No Source`() {
        // / Given: a launch rule to dispatch an event with no source specified in details

        //    ---------- dispatch event rule ----------
        //        "detail": {
        //          "type" : "com.adobe.eventType.edge",
        //          "eventdataaction" : "copy"
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testDispatchEventNoSource.json")
        val event = Event.Builder(
            "Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch"
        )
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        val processedEvent = launchRulesConsequence.process(event, matchedRules)

        // / Then: No consequence event will be dispatched
        verify(extensionApi, never()).dispatch(any())

        // verify original event is unchanged
        assertEquals(event, processedEvent)
    }

    @Test
    fun `Test Dispatch Event chained Dispatch Events`() {
        // / Given: a launch rule to dispatch an event with the same type and source which triggered the consequence

        //    ---------- dispatch event rule condition ----------
        //        "conditions": [
        //        {
        //          "type": "matcher",
        //          "definition": {
        //            "key": "~type",
        //            "matcher": "eq",
        //            "values": [
        //              "com.adobe.eventType.edge"
        //            ]
        //          }
        //        },
        //        {
        //          "type": "matcher",
        //          "definition": {
        //            "key": "~source",
        //            "matcher": "eq",
        //            "values": [
        //              "com.adobe.eventSource.requestContent"
        //            ]
        //          }
        //        }
        //      ]
        //    ---------- dispatch event rule consequence ----------
        //        "detail": {
        //           "type" : "com.adobe.eventType.edge",
        //           "source" : "com.adobe.eventSource.requestContent",
        //           "eventdataaction" : "copy"
        //         }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testDispatchEventChain.json")

        val event = Event.Builder(
            "Edge Request",
            "com.adobe.eventType.edge",
            "com.adobe.eventSource.requestContent"
        )
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        // Process original event; dispatch chain count = 0
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
        assertEquals(event.uniqueIdentifier, dispatchedEventCaptor.value.parentID)

        // Process dispatched event; dispatch chain count = 1
        // Expect dispatch to not be called max allowed chained events is 1
        val matchedRulesDDispatchedEvent =
            rulesEngine.evaluate(LaunchTokenFinder(dispatchedEventCaptor.value, extensionApi))
        launchRulesConsequence.process(
            dispatchedEventCaptor.value,
            matchedRulesDDispatchedEvent
        )
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
    }

    @Test
    fun `Test Dispatch Event multiple Processing of Same Original Event`() {
        // Given: a launch rule to dispatch an event with the same type and source which triggered the consequence

        //    ---------- dispatch event rule condition ----------
        //        "conditions": [
        //        {
        //          "type": "matcher",
        //          "definition": {
        //            "key": "~type",
        //            "matcher": "eq",
        //            "values": [
        //              "com.adobe.eventType.edge"
        //            ]
        //          }
        //        },
        //        {
        //          "type": "matcher",
        //          "definition": {
        //            "key": "~source",
        //            "matcher": "eq",
        //            "values": [
        //              "com.adobe.eventSource.requestContent"
        //            ]
        //          }
        //        }
        //      ]
        //    ---------- dispatch event rule consequence ----------
        //        "detail": {
        //           "type" : "com.adobe.eventType.edge",
        //           "source" : "com.adobe.eventSource.requestContent",
        //           "eventdataaction" : "copy"
        //         }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testDispatchEventChain.json")

        val event = Event.Builder(
            "Edge Request",
            "com.adobe.eventType.edge",
            "com.adobe.eventSource.requestContent"
        )
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        // Process original event; dispatch chain count = 0
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
        assertEquals(event.uniqueIdentifier, dispatchedEventCaptor.value.parentID)

        // reset calls to extension api for exclusively verifying next dispatch
        reset(extensionApi)

        // Process dispatched event; dispatch chain count = 1
        // Expect dispatch to fail as max allowed chained events is 1
        val matchedRulesDispatchedEvent =
            rulesEngine.evaluate(LaunchTokenFinder(dispatchedEventCaptor.value, extensionApi))
        launchRulesConsequence.process(
            dispatchedEventCaptor.value,
            matchedRulesDispatchedEvent
        )
        verify(extensionApi, times(0)).dispatch(any())

        // reset calls to extension api for exclusively verifying next dispatch
        reset(extensionApi)

        // Process dispatched event; dispatch chain count = 1
        // Expect event to be processed as if first time
        launchRulesConsequence.process(event, matchedRules)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
        assertEquals(event.uniqueIdentifier, dispatchedEventCaptor.value.parentID)

        // reset calls to extension api for exclusively verifying next dispatch
        reset(extensionApi)

        // Process dispatched event; dispatch chain count = 1
        // Expect dispatch to fail as max allowed chained events is 1
        val matchedRulesDispatchedEvent2 =
            rulesEngine.evaluate(LaunchTokenFinder(dispatchedEventCaptor.value, extensionApi))
        launchRulesConsequence.process(
            dispatchedEventCaptor.value,
            matchedRulesDispatchedEvent2
        )
        verify(extensionApi, times(0)).dispatch(any())
    }

    @Test
    fun `Test Dispatch Event multiple Processing of Same Dispatched Event`() {
        // Given: a launch rule to dispatch an event with the same type and source which triggered the consequence

        //    ---------- dispatch event rule condition ----------
        //        "conditions": [
        //        {
        //          "type": "matcher",
        //          "definition": {
        //            "key": "~type",
        //            "matcher": "eq",
        //            "values": [
        //              "com.adobe.eventType.edge"
        //            ]
        //          }
        //        },
        //        {
        //          "type": "matcher",
        //          "definition": {
        //            "key": "~source",
        //            "matcher": "eq",
        //            "values": [
        //              "com.adobe.eventSource.requestContent"
        //            ]
        //          }
        //        }
        //      ]
        //    ---------- dispatch event rule consequence ----------
        //        "detail": {
        //           "type" : "com.adobe.eventType.edge",
        //           "source" : "com.adobe.eventSource.requestContent",
        //           "eventdataaction" : "copy"
        //         }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testDispatchEventChain.json")

        val event = Event.Builder(
            "Edge Request",
            "com.adobe.eventType.edge",
            "com.adobe.eventSource.requestContent"
        )
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        // Process original event; dispatch chain count = 0
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
        assertEquals(event.uniqueIdentifier, dispatchedEventCaptor.value.parentID)

        // reset calls to extension api for exclusively verifying next dispatch
        reset(extensionApi)

        // Process dispatched event; dispatch chain count = 1
        // Expect dispatch to fail as max allowed chained events is 1
        val matchedRulesDispatchedEvent =
            rulesEngine.evaluate(LaunchTokenFinder(dispatchedEventCaptor.value, extensionApi))
        launchRulesConsequence.process(
            dispatchedEventCaptor.value,
            matchedRulesDispatchedEvent
        )
        verify(extensionApi, times(0)).dispatch(any())

        // reset calls to extension api for exclusively verifying next dispatch
        reset(extensionApi)

        // Process dispatched event; dispatch chain count = 1
        // Expect event to be processed as if first time
        launchRulesConsequence.process(
            dispatchedEventCaptor.value,
            matchedRulesDispatchedEvent
        )
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())

        // reset calls to extension api for exclusively verifying next dispatch
        reset(extensionApi)

        // Process dispatched event; dispatch chain count = 1
        // Expect dispatch to fail as max allowed chained events is 1
        val matchedRulesDispatchedEvent2 =
            rulesEngine.evaluate(LaunchTokenFinder(dispatchedEventCaptor.value, extensionApi))
        launchRulesConsequence.process(
            dispatchedEventCaptor.value,
            matchedRulesDispatchedEvent2
        )
        verify(extensionApi, times(0)).dispatch(any())
    }

    @Test
    fun `Test Dispatch Event interleaved Chained Dispatched Event`() {
        // Given: two launch rules with the same consequence but different event triggers

        //    ---------- dispatch event rule 1 condition ----------
        //        "conditions": [
        //        {
        //          "type": "matcher",
        //          "definition": {
        //            "key": "~type",
        //            "matcher": "eq",
        //            "values": [
        //              "com.adobe.eventType.edge"
        //            ]
        //          }
        //        },
        //        {
        //          "type": "matcher",
        //          "definition": {
        //            "key": "~source",
        //            "matcher": "eq",
        //            "values": [
        //              "com.adobe.eventSource.requestContent"
        //            ]
        //          }
        //        }
        //      ]
        //    ---------- dispatch event rule 1 consequence ----------
        //        "detail": {
        //           "type" : "com.adobe.eventType.edge",
        //           "source" : "com.adobe.eventSource.requestContent",
        //           "eventdataaction" : "copy"
        //         }
        //    --------------------------------------

        //    ---------- dispatch event rule 2 condition ----------
        //        "conditions": [
        //       {
        //         "type": "matcher",
        //         "definition": {
        //           "key": "~type",
        //           "matcher": "eq",
        //           "values": [
        //             "com.adobe.eventType.lifecycle"
        //           ]
        //         }
        //       },
        //       {
        //         "type": "matcher",
        //         "definition": {
        //           "key": "~source",
        //           "matcher": "eq",
        //           "values": [
        //             "com.adobe.eventSource.applicationLaunch"
        //           ]
        //         }
        //       }
        //     ]
        //    ---------- dispatch event rule 2 consequence ----------
        //        "detail": {
        //           "type" : "com.adobe.eventType.edge",
        //           "source" : "com.adobe.eventSource.requestContent",
        //           "eventdataaction" : "copy"
        //         }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testDispatchEventChain.json")

        // Then: dispatch event to trigger rule 1
        val eventEdgeRequest = Event.Builder(
            "Edge Request",
            "com.adobe.eventType.edge",
            "com.adobe.eventSource.requestContent"
        )
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        // Then: dispatch event to trigger rule 2
        val eventLaunch = Event.Builder(
            "Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch"
        )
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        // Process original event; dispatch chain count = 0
        val matchedRulesEdgeRequestEvent =
            rulesEngine.evaluate(LaunchTokenFinder(eventEdgeRequest, extensionApi))
        launchRulesConsequence.process(
            eventEdgeRequest,
            matchedRulesEdgeRequestEvent
        )
        val dispatchedEventCaptor1: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor1.capture())
        assertEquals(eventEdgeRequest.uniqueIdentifier, dispatchedEventCaptor1.value.parentID)

        // Process launch event
        val matchedRulesLaunchEvent =
            rulesEngine.evaluate(LaunchTokenFinder(eventLaunch, extensionApi))
        launchRulesConsequence.process(eventLaunch, matchedRulesLaunchEvent)
        val dispatchedEventCaptor2: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(2)).dispatch(dispatchedEventCaptor2.capture())
        assertEquals(eventLaunch.uniqueIdentifier, dispatchedEventCaptor2.value.parentID)

        // reset calls to extension api for exclusively verifying next dispatch
        reset(extensionApi)

        // Process first dispatched event; dispatch chain count = 1
        // Expect dispatch to fail as max allowed chained events is 1
        val matchedRulesDispatchEvent1 =
            rulesEngine.evaluate(LaunchTokenFinder(dispatchedEventCaptor1.value, extensionApi))
        launchRulesConsequence.process(
            dispatchedEventCaptor1.value,
            matchedRulesDispatchEvent1
        )

        // Process second dispatched event; dispatch chain count = 1
        // Expect dispatch to fail as max allowed chained events is 1
        val matchedRulesDispatchEvent2 =
            rulesEngine.evaluate(LaunchTokenFinder(dispatchedEventCaptor2.value, extensionApi))
        launchRulesConsequence.process(
            dispatchedEventCaptor2.value,
            matchedRulesDispatchEvent2
        )
        verify(extensionApi, times(0)).dispatch(any())
    }

    @Test
    fun `Test Dispatch Event processed Event matches multiple Dispatch Consequences`() {
        // Given: two launch rules with the same consequence but different conditions

        //    ---------- dispatch event rule 1 condition ----------
        //        "conditions": [
        //        {
        //          "type": "matcher",
        //          "definition": {
        //            "key": "~type",
        //            "matcher": "eq",
        //            "values": [
        //              "com.adobe.eventType.edge"
        //            ]
        //          }
        //        },
        //        {
        //          "type": "matcher",
        //          "definition": {
        //            "key": "~source",
        //            "matcher": "eq",
        //            "values": [
        //              "com.adobe.eventSource.requestContent"
        //            ]
        //          }
        //        }
        //      ]
        //    ---------- dispatch event rule 1 consequence ----------
        //        "detail": {
        //           "type" : "com.adobe.eventType.edge",
        //           "source" : "com.adobe.eventSource.requestContent",
        //           "eventdataaction" : "copy"
        //         }
        //    --------------------------------------

        //    ---------- dispatch event rule 2 condition ----------
        //        "conditions": [
        //          {
        //            "type": "matcher",
        //            "definition": {
        //              "key": "dispatch",
        //              "matcher": "eq",
        //              "values": [
        //                "yes"
        //              ]
        //            }
        //          }
        //        ]
        //    ---------- dispatch event rule 2 consequence ----------
        //        "detail": {
        //           "type" : "com.adobe.eventType.edge",
        //           "source" : "com.adobe.eventSource.requestContent",
        //           "eventdataaction" : "copy"
        //         }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testDispatchEventChain.json")

        // Then:  dispatch event which will trigger two launch rules
        val event = Event.Builder(
            "Edge Request",
            "com.adobe.eventType.edge",
            "com.adobe.eventSource.requestContent"
        )
            .setEventData(mapOf("dispatch" to "yes"))
            .build()

        // Process original event, expect 2 dispatched events
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(2)).dispatch(dispatchedEventCaptor.capture())
        assertEquals(event.uniqueIdentifier, dispatchedEventCaptor.allValues[0].parentID)
        assertEquals(event.uniqueIdentifier, dispatchedEventCaptor.allValues[1].parentID)

        // reset calls to extension api for exclusively verifying next dispatch
        reset(extensionApi)

        // Process dispatched event 1, expect 0 dispatch events
        // chain count = 1, which is max chained events
        val matchedRulesDispatchEvent1 =
            rulesEngine.evaluate(
                LaunchTokenFinder(
                    dispatchedEventCaptor.allValues[0],
                    extensionApi
                )
            )
        launchRulesConsequence.process(
            dispatchedEventCaptor.allValues[0],
            matchedRulesDispatchEvent1
        )
        verify(extensionApi, times(0)).dispatch(any())

        // Process dispatched event 2, expect 0 dispatch events
        // chain count = 1, which is max chained events
        val matchedRulesDispatchEvent2 =
            rulesEngine.evaluate(
                LaunchTokenFinder(
                    dispatchedEventCaptor.allValues[1],
                    extensionApi
                )
            )
        launchRulesConsequence.process(
            dispatchedEventCaptor.allValues[1],
            matchedRulesDispatchEvent2
        )
        verify(extensionApi, times(0)).dispatch(any())
    }

    @Test
    fun `Test Url Encode`() {
        // Given:         {
        //          "id": "RC48ef3f5e83c84405a3da6cc5128c090c",
        //          "type": "url",
        //          "detail": {
        //            "url": "http://www.adobe.com/a={%urlenc(~state.com.adobe.module.lifecycle/lifecyclecontextdata.carriername)%}"
        //          }
        //        }

        resetRulesEngine("rules_module_tests/consequence_rules_testUrlenc.json")

        `when`(extensionApi.getSharedState(anyString(), any(), anyBoolean(), any())).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "lifecyclecontextdata" to mapOf(
                        "carriername" to "x y"
                    )
                )
            )
        )

        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(defaultEvent, extensionApi))
        launchRulesConsequence.process(defaultEvent, matchedRules)

        val consequenceEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(consequenceEventCaptor.capture())

        assertEquals("com.adobe.eventType.rulesEngine", consequenceEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.responseContent", consequenceEventCaptor.value.source)
        val data = consequenceEventCaptor.value.eventData?.get("triggeredconsequence") as Map<*, *>?
        val detail = data?.get("detail") as Map<*, *>?
        assertEquals("url", data?.get("type"))
        assertEquals("http://www.adobe.com/a=x%20y", detail?.get("url"))
    }

    @Test
    fun `Test Url Encode Invalid Fn Name`() {
        // Given:
        //    {
        //      "id": "RC48ef3f5e83c84405a3da6cc5128c090c",
        //      "type": "url",
        //      "detail": {
        //        "url": "http://www.adobe.com/a={%urlenc1(~state.com.adobe.module.lifecycle/lifecyclecontextdata.carriername)%}"
        //      }
        //    }
        resetRulesEngine("rules_module_tests/consequence_rules_testUrlenc_invalidFnName.json")

        `when`(extensionApi.getSharedState(anyString(), any(), anyBoolean(), any())).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "lifecyclecontextdata" to mapOf(
                        "carriername" to "x y"
                    )
                )
            )
        )

        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(defaultEvent, extensionApi))
        launchRulesConsequence.process(defaultEvent, matchedRules)

        val consequenceEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(consequenceEventCaptor.capture())

        assertEquals("com.adobe.eventType.rulesEngine", consequenceEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.responseContent", consequenceEventCaptor.value.source)
        val data = consequenceEventCaptor.value.eventData?.get("triggeredconsequence") as Map<*, *>?
        val detail = data?.get("detail") as Map<*, *>?
        assertEquals("url", data?.get("type"))
        assertEquals("http://www.adobe.com/a=x y", detail?.get("url"))
    }

    @Test
    fun `Test Schema Event History Insert Operation When detail id doesn't exist`() {
        // Given: a launch rule with an invalid event history operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": {
        //              "operation": "insert",
        //              "content": {
        //                  "key1": "value1",
        //                  "key2": "value2"
        //              }
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaNoDetailId.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When detail id Is Null`() {
        // Given: a launch rule with an invalid event history operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": null,
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": {
        //              "operation": "insert",
        //              "content": {
        //                  "key1": "value1",
        //                  "key2": "value2"
        //              }
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaNullDetailId.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched due to lookup failure
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When detail id Is Empty`() {
        // Given: a launch rule with an invalid event history operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "",
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": {
        //              "operation": "insert",
        //              "content": {
        //                  "key1": "value1",
        //                  "key2": "value2"
        //              }
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEmptyDetailId.json")

        `when`(extensionApi.recordHistoricalEvent(any(), any<AdobeCallbackWithError<Boolean>>())).thenAnswer {
            val callback = it.arguments[1] as AdobeCallbackWithError<Boolean>
            callback.call(true)
        }

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should be recorded and dispatched
        val recordedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        val recordResultCaptor: ArgumentCaptor<AdobeCallbackWithError<Boolean>> =
            ArgumentCaptor.forClass(AdobeCallbackWithError::class.java) as ArgumentCaptor<AdobeCallbackWithError<Boolean>>

        verify(extensionApi).recordHistoricalEvent(
            recordedEventCaptor.capture(),
            recordResultCaptor.capture()
        )

        // Verify recorded event properties
        assertEquals("Dispatch Consequence Result", recordedEventCaptor.value.name)
        assertEquals("com.adobe.eventType.rulesEngine", recordedEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.responseContent", recordedEventCaptor.value.source)
        assertEquals(
            mapOf("key1" to "value1", "key2" to "value2"),
            recordedEventCaptor.value.eventData
        )

        // Verify event dispatch after successful recording
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
        assertEquals(recordedEventCaptor.value, dispatchedEventCaptor.value)
    }

    @Test
    fun `Test Schema Event History Insert Operation When detail schema doesn't exist`() {
        // Given: a launch rule with an invalid event history operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "data": {
        //              "operation": "insert",
        //              "content": {
        //                  "key1": "value1"
        //              }
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaNoDetailSchema.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When detail schema Is Null`() {
        // Given: a launch rule with an invalid event history operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": null,
        //          "data": {
        //              "operation": "insert",
        //              "content": {
        //                  "key1": "value1"
        //              }
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaNullDetailSchema.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When detail schema Is Empty`() {
        // Given: a launch rule with an invalid event history operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "",
        //          "data": {
        //              "operation": "insert",
        //              "content": {
        //                  "key1": "value1"
        //              }
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEmptyDetailSchema.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When detail schema Is Invalid`() {
        // Given: a launch rule with an invalid event history operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "https://ns.adobe.com/personalization/invalidSchema",
        //          "data": {
        //              "operation": "insert",
        //              "content": {
        //                  "key1": "value1"
        //              }
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaInvalidDetailSchema.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When detail data doesn't exist`() {
        // Given: a launch rule with an invalid event history operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryNoSchemaDetailData.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When detail data Is Null`() {
        // Given: a launch rule with an invalid event history operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": null
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryNullSchemaDetailData.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When detail data Is Empty`() {
        // Given: a launch rule with an invalid event history operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": {}
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryEmptySchemaDetailData.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When Operation doesn't exist`() {
        // Given: a launch rule with an invalid event history operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": {
        //              "content": {
        //                  "key1": "value1"
        //              }
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryNoOperation.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When Operation Is Null`() {
        // Given: a launch rule with an invalid event history operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": {
        //              "operation": null,
        //              "content": {
        //                  "key1": "value1"
        //              }
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryNullOperation.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When Operation Is Empty`() {
        // Given: a launch rule with an invalid event history operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": {
        //              "operation": "",
        //              "content": {
        //                  "key1": "value1"
        //              }
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryEmptyOperation.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When Operation Is Invalid`() {
        // Given: a launch rule with an invalid event history operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": {
        //              "operation": "invalid_operation",
        //              "content": {
        //                  "key1": "value1"
        //              }
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryInvalidOperation.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When Content doesn't exist`() {
        // Given: a launch rule with an event history insert operation but missing content
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": {
        //              "operation": "insert"
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryInsertNoContent.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When Content Is Null`() {
        // Given: a launch rule with an event history insert operation but missing content
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": {
        //              "operation": "insert",
        //              "content": null
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryInsertNullContent.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When Content Is Empty`() {
        // Given: a launch rule with an event history insert operation but missing content
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": {
        //              "operation": "insert",
        //              "content": {}
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryInsertEmptyContent.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation`() {
        // Given: a launch rule with an event history insert operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": {
        //              "operation": "insert",
        //              "content": {
        //                  "key1": "value1",
        //                  "key2": "value2"
        //              }
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryInsert.json")

        `when`(extensionApi.recordHistoricalEvent(any(), any<AdobeCallbackWithError<Boolean>>())).thenAnswer {
            val callback = it.arguments[1] as AdobeCallbackWithError<Boolean>
            callback.call(true)
        }

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should be recorded and dispatched
        val recordedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        val recordResultCaptor: ArgumentCaptor<AdobeCallbackWithError<Boolean>> =
            ArgumentCaptor.forClass(AdobeCallbackWithError::class.java) as ArgumentCaptor<AdobeCallbackWithError<Boolean>>

        verify(extensionApi).recordHistoricalEvent(
            recordedEventCaptor.capture(),
            recordResultCaptor.capture()
        )

        // Verify recorded event properties
        assertEquals("Dispatch Consequence Result", recordedEventCaptor.value.name)
        assertEquals("com.adobe.eventType.rulesEngine", recordedEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.responseContent", recordedEventCaptor.value.source)
        assertEquals(mapOf("key1" to "value1", "key2" to "value2"), recordedEventCaptor.value.eventData)

        // Verify event dispatch after successful recording
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
        assertEquals(recordedEventCaptor.value, dispatchedEventCaptor.value)
    }

    @Test
    fun `Test Schema Event History Insert Operation with token replacement`() {
        // Given: a launch rule with an event history insert operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": {
        //              "operation": "insert",
        //              "content": {
        //                  "key1": "value1",
        //                  "key2": "{%~type%}",
        //                  "key3": "{%~state.com.adobe.module.testModule/testKey%}"
        //              }
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryInsertWithTokens.json")

        `when`(extensionApi.getSharedState(anyString(), any(), anyBoolean(), any())).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "testKey" to "testValue"
                )
            )
        )

        `when`(extensionApi.recordHistoricalEvent(any(), any<AdobeCallbackWithError<Boolean>>())).thenAnswer {
            val callback = it.arguments[1] as AdobeCallbackWithError<Boolean>
            callback.call(true)
        }

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should be recorded and dispatched
        val recordedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        val recordResultCaptor: ArgumentCaptor<AdobeCallbackWithError<Boolean>> =
            ArgumentCaptor.forClass(AdobeCallbackWithError::class.java) as ArgumentCaptor<AdobeCallbackWithError<Boolean>>

        verify(extensionApi).recordHistoricalEvent(
            recordedEventCaptor.capture(),
            recordResultCaptor.capture()
        )

        // Verify recorded event properties
        assertEquals("Dispatch Consequence Result", recordedEventCaptor.value.name)
        assertEquals("com.adobe.eventType.rulesEngine", recordedEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.responseContent", recordedEventCaptor.value.source)
        assertEquals(
            mapOf("key1" to "value1", "key2" to "com.adobe.eventType.generic", "key3" to "testValue"),
            recordedEventCaptor.value.eventData
        )

        // Verify event dispatch after successful recording
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
        assertEquals(recordedEventCaptor.value, dispatchedEventCaptor.value)
    }

    @Test
    fun `Test Schema Event History InsertIfNotExists Operation When Event Does Not Exist`() {
        // Given: a launch rule with an event history insertIfNotExists operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": {
        //              "operation": "insertIfNotExists",
        //              "content": {
        //                  "key1": "value1",
        //                  "key2": "value2"
        //              }
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryInsertIfNotExists.json")

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // Mock getHistoricalEvents to return 0 (event doesn't exist)
        `when`(
            extensionApi.getHistoricalEvents(
                any(),
                anyBoolean(),
                any<AdobeCallbackWithError<Array<EventHistoryResult>>>()
            )
        ).thenAnswer {
            val callback = it.arguments[2] as AdobeCallbackWithError<Array<EventHistoryResult>>
            callback.call(arrayOf(EventHistoryResult(0)))
        }

        `when`(extensionApi.recordHistoricalEvent(any(), any<AdobeCallbackWithError<Boolean>>())).thenAnswer {
            val callback = it.arguments[1] as AdobeCallbackWithError<Boolean>
            callback.call(true)
        }

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should be recorded and dispatched
        val recordedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        val recordResultCaptor: ArgumentCaptor<AdobeCallbackWithError<Boolean>> =
            ArgumentCaptor.forClass(AdobeCallbackWithError::class.java) as ArgumentCaptor<AdobeCallbackWithError<Boolean>>

        verify(extensionApi, times(1)).recordHistoricalEvent(
            recordedEventCaptor.capture(),
            recordResultCaptor.capture()
        )

        // Verify recorded event properties
        assertEquals("Dispatch Consequence Result", recordedEventCaptor.value.name)
        assertEquals("com.adobe.eventType.rulesEngine", recordedEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.responseContent", recordedEventCaptor.value.source)
        assertEquals(mapOf("key1" to "value1", "key2" to "value2"), recordedEventCaptor.value.eventData)

        // Verify event dispatch after successful recording
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi).dispatch(dispatchedEventCaptor.capture())
        assertEquals(recordedEventCaptor.value, dispatchedEventCaptor.value)
    }

    @Test
    fun `Test Schema Event History InsertIfNotExists Operation When Event Exists`() {
        // Given: a launch rule with an event history insertIfNotExists operation
        //    ---------- schema event rule ----------
        //        "detail": {
        //          "id": "test-id",
        //          "schema": "https://ns.adobe.com/personalization/eventHistoryOperation",
        //          "data": {
        //              "operation": "insertIfNotExists",
        //              "content": {
        //                  "key1": "value1",
        //                  "key2": "value2"
        //              }
        //          }
        //        }
        //    --------------------------------------
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryInsertIfNotExists.json")

        // Mock getHistoricalEvents to return 1 (event exists)
        `when`(
            extensionApi.getHistoricalEvents(
                any(),
                anyBoolean(),
                any<AdobeCallbackWithError<Array<EventHistoryResult>>>()
            )
        ).thenAnswer {
            val callback = it.arguments[2] as AdobeCallbackWithError<Array<EventHistoryResult>>
            callback.call(arrayOf(EventHistoryResult(1)))
        }

        `when`(extensionApi.recordHistoricalEvent(any(), any<AdobeCallbackWithError<Boolean>>())).thenAnswer {
            val callback = it.arguments[1] as AdobeCallbackWithError<Boolean>
            callback.call(true)
        }

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched since it already exists
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History InsertIfNotExists Operation When EventHistory lookup times out`() {
        // Given: a launch rule with an event history insert operation
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryInsertIfNotExists.json")

        // Mock recordHistoricalEvent to simulate failed recording
        `when`(extensionApi.getHistoricalEvents(any(), anyBoolean(), any<AdobeCallbackWithError<Array<EventHistoryResult>>>()))
            .thenThrow(RuntimeException("Failed to lookup event history"))

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched due to lookup failure
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History InsertIfNotExists Operation When EventHistory lookup has negative count`() {
        // Given: a launch rule with an event history insert operation
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryInsertIfNotExists.json")

        // Mock recordHistoricalEvent to simulate failed recording
        `when`(
            extensionApi.getHistoricalEvents(
                any(),
                anyBoolean(),
                any<AdobeCallbackWithError<Array<EventHistoryResult>>>()
            )
        ).thenAnswer {
            val callback = it.arguments[2] as AdobeCallbackWithError<Array<EventHistoryResult>>
            callback.call(arrayOf(EventHistoryResult(-1)))
        }

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched due to lookup failure
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History InsertIfNotExists Operation When EventHistory lookup fails`() {
        // Given: a launch rule with an event history insert operation
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryInsertIfNotExists.json")

        // Mock recordHistoricalEvent to simulate failed recording
        `when`(
            extensionApi.getHistoricalEvents(
                any(),
                anyBoolean(),
                any<AdobeCallbackWithError<Array<EventHistoryResult>>>()
            )
        ).thenAnswer {
            val callback = it.arguments[2] as AdobeCallbackWithError<Array<EventHistoryResult>>
            callback.fail(AdobeError.DATABASE_ERROR)
        }

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should not be recorded or dispatched due to lookup failure
        verify(extensionApi, never()).recordHistoricalEvent(any(), any())
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When recordHistoricalEvent returns false`() {
        // Given: a launch rule with an event history insert operation
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryInsert.json")

        // Mock recordHistoricalEvent to simulate failed recording
        `when`(extensionApi.recordHistoricalEvent(any(), any())).thenAnswer {
            val callback = it.arguments[1] as AdobeCallbackWithError<Boolean>
            callback.call(false)
        }

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should be recorded and dispatched
        val recordedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        val recordResultCaptor: ArgumentCaptor<AdobeCallbackWithError<Boolean>> =
            ArgumentCaptor.forClass(AdobeCallbackWithError::class.java) as ArgumentCaptor<AdobeCallbackWithError<Boolean>>

        // Then: Event recordHistoricalEvent is called
        verify(extensionApi, times(1)).recordHistoricalEvent(
            recordedEventCaptor.capture(),
            recordResultCaptor.capture()
        )

        // Verify properties passed to recordHistoricalEvent
        assertEquals("Dispatch Consequence Result", recordedEventCaptor.value.name)
        assertEquals("com.adobe.eventType.rulesEngine", recordedEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.responseContent", recordedEventCaptor.value.source)

        // Event not dispatched due to recordHistoricalEvent failure
        verify(extensionApi, never()).dispatch(any())
    }

    @Test
    fun `Test Schema Event History Insert Operation When recordHistoricalEvent fails`() {
        // Given: a launch rule with an event history insert operation
        resetRulesEngine("rules_module_tests/consequence_rules_testSchemaEventHistoryInsert.json")

        // Mock recordHistoricalEvent to simulate failed recording
        `when`(extensionApi.recordHistoricalEvent(any(), any())).thenAnswer {
            val callback = it.arguments[1] as AdobeCallbackWithError<Boolean>
            callback.fail(AdobeError.DATABASE_ERROR)
        }

        val event = Event.Builder(
            "Test Event",
            "com.adobe.eventType.generic",
            "com.adobe.eventSource.requestContent"
        ).build()

        // When: evaluating the event
        val matchedRules = rulesEngine.evaluate(LaunchTokenFinder(event, extensionApi))
        launchRulesConsequence.process(event, matchedRules)

        // Then: Event should be recorded and dispatched
        val recordedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        val recordResultCaptor: ArgumentCaptor<AdobeCallbackWithError<Boolean>> =
            ArgumentCaptor.forClass(AdobeCallbackWithError::class.java) as ArgumentCaptor<AdobeCallbackWithError<Boolean>>

        // Then: Event recordHistoricalEvent is called
        verify(extensionApi, times(1)).recordHistoricalEvent(
            recordedEventCaptor.capture(),
            recordResultCaptor.capture()
        )

        // Verify properties passed to recordHistoricalEvent
        assertEquals("Dispatch Consequence Result", recordedEventCaptor.value.name)
        assertEquals("com.adobe.eventType.rulesEngine", recordedEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.responseContent", recordedEventCaptor.value.source)

        // Event not dispatched due to recordHistoricalEvent failure
        verify(extensionApi, never()).dispatch(any())
    }

    private fun resetRulesEngine(rulesFileName: String) {
        val json = readTestResources(rulesFileName)
        val rules = json?.let { JSONRulesParser.parse(it, extensionApi) }
        rulesEngine.replaceRules(rules)
        reset(extensionApi)
    }
}
