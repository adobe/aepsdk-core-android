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
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.launch.rulesengine.json.JSONRulesParser
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(MockitoJUnitRunner.Silent::class)
class LaunchRulesConsequenceTests {

    private lateinit var extensionApi: ExtensionApi
    private lateinit var launchRulesEngine: LaunchRulesEngine
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
        launchRulesEngine = LaunchRulesEngine(extensionApi)
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

        val matchedRules = launchRulesEngine.process(defaultEvent)
        val processedEvent =
            launchRulesConsequence.evaluateRulesConsequence(defaultEvent, matchedRules)

        // / Then: no consequence event will be dispatched
        verify(extensionApi, never()).dispatch(any())

        val attachedData = processedEvent.eventData?.get("attached_data") as Map<*, *>

        // / Then: ["key1": "value1"] should be attached to above launch event
        assertEquals("value1", attachedData["key1"])

        // / Then: should not get "launches" value from (lifecycle) shared state
        assertEquals("", attachedData["launches"])
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
        val matchedRules = launchRulesEngine.process(defaultEvent)
        val processedEvent =
            launchRulesConsequence.evaluateRulesConsequence(defaultEvent, matchedRules)

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

        val matchedRules = launchRulesEngine.process(defaultEvent)
        val processedEvent =
            launchRulesConsequence.evaluateRulesConsequence(defaultEvent, matchedRules)

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
        val matchedRules = launchRulesEngine.process(defaultEvent)
        val processedEvent =
            launchRulesConsequence.evaluateRulesConsequence(defaultEvent, matchedRules)

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

        val matchedRules = launchRulesEngine.process(event)
        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)

        // / Then: One consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
        assertEquals("com.adobe.eventType.edge", dispatchedEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.requestContent", dispatchedEventCaptor.value.source)
        assertEquals(event.eventData, dispatchedEventCaptor.value.eventData)

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

        val matchedRules = launchRulesEngine.process(event)
        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)

        // / Then: One consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
        assertEquals("com.adobe.eventType.edge", dispatchedEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.requestContent", dispatchedEventCaptor.value.source)
        assertEquals(null, dispatchedEventCaptor.value.eventData)

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

        val matchedRules = launchRulesEngine.process(event)
        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)

        // / Then: One consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
        assertEquals("com.adobe.eventType.edge", dispatchedEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.requestContent", dispatchedEventCaptor.value.source)
        assertEquals("value", dispatchedEventCaptor.value.eventData["key"])
        assertEquals("subvalue", dispatchedEventCaptor.value.eventData["key.subkey"])

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

        val matchedRules = launchRulesEngine.process(event)
        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)

        // / Then: One consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())
        assertEquals("com.adobe.eventType.edge", dispatchedEventCaptor.value.type)
        assertEquals("com.adobe.eventSource.requestContent", dispatchedEventCaptor.value.source)
        assertEquals(null, dispatchedEventCaptor.value.eventData)

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

        val matchedRules = launchRulesEngine.process(event)
        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)

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

        val matchedRules = launchRulesEngine.process(event)
        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)

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

        val matchedRules = launchRulesEngine.process(event)
        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)

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

        val matchedRules = launchRulesEngine.process(event)
        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)

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
        val matchedRules = launchRulesEngine.process(event)
        launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())

        // Process dispatched event; dispatch chain count = 1
        // Expect dispatch to not be called max allowed chained events is 1
        val matchedRulesDDispatchedEvent = launchRulesEngine.process(dispatchedEventCaptor.value)
        launchRulesConsequence.evaluateRulesConsequence(
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
        val matchedRules = launchRulesEngine.process(event)
        launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())

        // Process dispatched event; dispatch chain count = 1
        // Expect dispatch to fail as max allowed chained events is 1
        val matchedRulesDispatchedEvent = launchRulesEngine.process(dispatchedEventCaptor.value)
        launchRulesConsequence.evaluateRulesConsequence(
            dispatchedEventCaptor.value,
            matchedRulesDispatchedEvent
        )
        verify(extensionApi, times(1)).dispatch(any())

        // Process dispatched event; dispatch chain count = 1
        // Expect event to be processed as if first time
        launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)
        verify(extensionApi, times(2)).dispatch(dispatchedEventCaptor.capture())

        // Process dispatched event; dispatch chain count = 1
        // Expect dispatch to fail as max allowed chained events is 1
        val matchedRulesDispatchedEvent2 = launchRulesEngine.process(dispatchedEventCaptor.value)
        launchRulesConsequence.evaluateRulesConsequence(
            dispatchedEventCaptor.value,
            matchedRulesDispatchedEvent2
        )
        verify(extensionApi, times(2)).dispatch(any())
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
        val matchedRules = launchRulesEngine.process(event)
        launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture())

        // Process dispatched event; dispatch chain count = 1
        // Expect dispatch to fail as max allowed chained events is 1
        val matchedRulesDispatchedEvent = launchRulesEngine.process(dispatchedEventCaptor.value)
        launchRulesConsequence.evaluateRulesConsequence(
            dispatchedEventCaptor.value,
            matchedRulesDispatchedEvent
        )
        verify(extensionApi, times(1)).dispatch(any())

        // Process dispatched event; dispatch chain count = 1
        // Expect event to be processed as if first time
        launchRulesConsequence.evaluateRulesConsequence(
            dispatchedEventCaptor.value,
            matchedRulesDispatchedEvent
        )
        verify(extensionApi, times(2)).dispatch(dispatchedEventCaptor.capture())

        // Process dispatched event; dispatch chain count = 1
        // Expect dispatch to fail as max allowed chained events is 1
        val matchedRulesDispatchedEvent2 = launchRulesEngine.process(dispatchedEventCaptor.value)
        launchRulesConsequence.evaluateRulesConsequence(
            dispatchedEventCaptor.value,
            matchedRulesDispatchedEvent2
        )
        verify(extensionApi, times(2)).dispatch(any())
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
        val matchedRulesEdgeRequestEvent = launchRulesEngine.process(eventEdgeRequest)
        launchRulesConsequence.evaluateRulesConsequence(
            eventEdgeRequest,
            matchedRulesEdgeRequestEvent
        )
        val dispatchedEventCaptor1: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor1.capture())

        // Process launch event
        val matchedRulesLaunchEvent = launchRulesEngine.process(eventLaunch)
        launchRulesConsequence.evaluateRulesConsequence(eventLaunch, matchedRulesLaunchEvent)
        val dispatchedEventCaptor2: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(2)).dispatch(dispatchedEventCaptor2.capture())

        // Process first dispatched event; dispatch chain count = 1
        // Expect dispatch to fail as max allowed chained events is 1
        val matchedRulesDispatchEvent1 = launchRulesEngine.process(dispatchedEventCaptor1.value)
        launchRulesConsequence.evaluateRulesConsequence(
            dispatchedEventCaptor1.value,
            matchedRulesDispatchEvent1
        )
        verify(extensionApi, times(2)).dispatch(any())

        // Process second dispatched event; dispatch chain count = 1
        // Expect dispatch to fail as max allowed chained events is 1
        val matchedRulesDispatchEvent2 = launchRulesEngine.process(dispatchedEventCaptor2.value)
        launchRulesConsequence.evaluateRulesConsequence(
            dispatchedEventCaptor2.value,
            matchedRulesDispatchEvent2
        )
        verify(extensionApi, times(2)).dispatch(any())
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
        val matchedRules = launchRulesEngine.process(event)
        launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)
        val dispatchedEventCaptor: ArgumentCaptor<Event> =
            ArgumentCaptor.forClass(Event::class.java)
        verify(extensionApi, times(2)).dispatch(dispatchedEventCaptor.capture())

        // Process dispatched event 1, expect 0 dispatch events
        // chain count = 1, which is max chained events
        val matchedRulesDispatchEvent1 =
            launchRulesEngine.process(dispatchedEventCaptor.allValues[0])
        launchRulesConsequence.evaluateRulesConsequence(
            dispatchedEventCaptor.allValues[0],
            matchedRulesDispatchEvent1
        )
        verify(extensionApi, times(2)).dispatch(any())

        // Process dispatched event 2, expect 0 dispatch events
        // chain count = 1, which is max chained events
        val matchedRulesDispatchEvent2 =
            launchRulesEngine.process(dispatchedEventCaptor.allValues[1])
        launchRulesConsequence.evaluateRulesConsequence(
            dispatchedEventCaptor.allValues[1],
            matchedRulesDispatchEvent2
        )
        verify(extensionApi, times(2)).dispatch(any())
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

        val matchedRules = launchRulesEngine.process(defaultEvent)
        launchRulesConsequence.evaluateRulesConsequence(defaultEvent, matchedRules)

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

        val matchedRules = launchRulesEngine.process(defaultEvent)
        launchRulesConsequence.evaluateRulesConsequence(defaultEvent, matchedRules)

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

    private fun resetRulesEngine(rulesFileName: String) {
        val json = readTestResources(rulesFileName)
        val rules = json?.let { JSONRulesParser.parse(it, extensionApi) }
        launchRulesEngine.replaceRules(rules)
    }
}
