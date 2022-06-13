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
import com.adobe.marketing.mobile.launch.rulesengine.json.JSONRulesParser
import com.adobe.marketing.mobile.test.utility.readTestResources
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.any
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(ExtensionApi::class, MobileCore::class)
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
        extensionApi = PowerMockito.mock(ExtensionApi::class.java)
        PowerMockito.mockStatic(MobileCore::class.java)
        launchRulesEngine = LaunchRulesEngine(extensionApi)
        launchRulesConsequence = LaunchRulesConsequence(launchRulesEngine, extensionApi)
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
        PowerMockito.`when`(extensionApi.getSharedEventState(ArgumentMatchers.anyString(), any(), any())).thenReturn(
            mapOf(
                "lifecyclecontextdata" to mapOf(
                    "carriername" to "AT&T"
                )
            )
        )
        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(defaultEvent)

        // / Then: no consequence event will be dispatched
        PowerMockito.verifyStatic(MobileCore::class.java, never())
        MobileCore.dispatchEvent(any(), any())

        val attachedData = processedEvent?.eventData?.get("attached_data") as Map<*, *>

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
        PowerMockito.`when`(extensionApi.getSharedEventState(ArgumentMatchers.anyString(), any(), any())).thenReturn(
            mapOf(
                "lifecyclecontextdata" to mapOf(
                    "carriername" to "AT&T"
                )
            )
        )
        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(defaultEvent)

        // / Then: no consequence event will be dispatched
        PowerMockito.verifyStatic(MobileCore::class.java, never())
        MobileCore.dispatchEvent(any(), any())

        // / Then: no data should not be attached to original launch event
        val attachedData = processedEvent?.eventData?.get("attached_data")
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
        PowerMockito.`when`(extensionApi.getSharedEventState(ArgumentMatchers.anyString(), any(), any())).thenReturn(
            mapOf(
                "lifecyclecontextdata" to mapOf(
                    "carriername" to "AT&T",
                    "launches" to 2
                )
            )
        )
        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(defaultEvent)

        // / Then: no consequence event will be dispatched
        PowerMockito.verifyStatic(MobileCore::class.java, never())
        MobileCore.dispatchEvent(any(), any())

        // / Then: "launchevent" should be removed from event data
        val lifecycleContextData = processedEvent?.eventData?.get("lifecyclecontextdata") as Map<*, *>
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
        PowerMockito.`when`(extensionApi.getSharedEventState(ArgumentMatchers.anyString(), any(), any())).thenReturn(
            mapOf(
                "lifecyclecontextdata" to mapOf(
                    "carriername" to "AT&T",
                    "launches" to 2
                )
            )
        )
        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(defaultEvent)

        // / Then: no consequence event will be dispatched
        PowerMockito.verifyStatic(MobileCore::class.java, never())
        MobileCore.dispatchEvent(any(), any())

        // / Then: "launchevent" should not be removed from event data
        val lifecycleContextData = processedEvent?.eventData?.get("lifecyclecontextdata") as Map<*, *>
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

        val event = Event.Builder("Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch")
            .setEventData(mapOf("xdm" to "test data"))
            .build()
        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event)

        // / Then: One consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        PowerMockito.verifyStatic(MobileCore::class.java, times(1))
        MobileCore.dispatchEvent(dispatchedEventCaptor.capture(), any())
        assertEquals("com.adobe.eventtype.edge", dispatchedEventCaptor.value.type)
        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEventCaptor.value.source)
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
        val event = Event.Builder("Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch")
            .setEventData(null)
            .build()

        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event)

        // / Then: One consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        PowerMockito.verifyStatic(MobileCore::class.java, times(1))
        MobileCore.dispatchEvent(dispatchedEventCaptor.capture(), any())
        assertEquals("com.adobe.eventtype.edge", dispatchedEventCaptor.value.type)
        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEventCaptor.value.source)
        assertEquals(mapOf(), dispatchedEventCaptor.value.eventData)

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
        val event = Event.Builder("Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch")
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event)

        // / Then: One consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        PowerMockito.verifyStatic(MobileCore::class.java, times(1))
        MobileCore.dispatchEvent(dispatchedEventCaptor.capture(), any())
        assertEquals("com.adobe.eventtype.edge", dispatchedEventCaptor.value.type)
        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEventCaptor.value.source)
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
        val event = Event.Builder("Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch")
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event)

        // / Then: One consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        PowerMockito.verifyStatic(MobileCore::class.java, times(1))
        MobileCore.dispatchEvent(dispatchedEventCaptor.capture(), any())
        assertEquals("com.adobe.eventtype.edge", dispatchedEventCaptor.value.type)
        assertEquals("com.adobe.eventsource.requestcontent", dispatchedEventCaptor.value.source)
        assertEquals(mapOf(), dispatchedEventCaptor.value.eventData)

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
        val event = Event.Builder("Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch")
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event)

        // / Then: No consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        PowerMockito.verifyStatic(MobileCore::class.java, never())
        MobileCore.dispatchEvent(dispatchedEventCaptor.capture(), any())

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
        val event = Event.Builder("Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch")
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event)

        // / Then: No consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        PowerMockito.verifyStatic(MobileCore::class.java, never())
        MobileCore.dispatchEvent(dispatchedEventCaptor.capture(), any())

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
        val event = Event.Builder("Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch")
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event)

        // / Then: No consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        PowerMockito.verifyStatic(MobileCore::class.java, never())
        MobileCore.dispatchEvent(dispatchedEventCaptor.capture(), any())

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
        val event = Event.Builder("Application Launch",
            "com.adobe.eventType.lifecycle",
            "com.adobe.eventSource.applicationLaunch")
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event)

        // / Then: No consequence event will be dispatched
        val dispatchedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        PowerMockito.verifyStatic(MobileCore::class.java, never())
        MobileCore.dispatchEvent(dispatchedEventCaptor.capture(), any())

        // verify original event is unchanged
        assertEquals(event, processedEvent)
    }

    @Test
    fun `Test Chained Dispatch Events`() {
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
        val event = Event.Builder("Edge Request",
            "com.adobe.eventType.edge",
            "com.adobe.eventSource.requestContent")
            .setEventData(mapOf("xdm" to "test data"))
            .build()

        val processedEvent = launchRulesConsequence.evaluateRulesConsequence(event)

        // Process original event; dispatch chain count = 0
        val dispatchedEventCaptor: ArgumentCaptor<Event> = ArgumentCaptor.forClass(Event::class.java)
        PowerMockito.verifyStatic(MobileCore::class.java, times(1))
        MobileCore.dispatchEvent(dispatchedEventCaptor.capture(), any())

        // Process dispatched event; dispatch chain count = 1
        // Expect dispatch to not be called max allowed chained events is 1
        val secondDispatchEvent = launchRulesConsequence.evaluateRulesConsequence(dispatchedEventCaptor.value)
        PowerMockito.verifyStatic(MobileCore::class.java, times(1))
        MobileCore.dispatchEvent(any(), any())
    }

    private fun resetRulesEngine(rulesFileName: String) {
        val json = readTestResources(rulesFileName)
        val rules = json?.let { JSONRulesParser.parse(it) }
        launchRulesEngine.replaceRules(rules)
    }
}
