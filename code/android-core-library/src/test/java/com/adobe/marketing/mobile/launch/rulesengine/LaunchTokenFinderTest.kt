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
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.internal.eventhub.EventHub
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class LaunchTokenFinderTest {

    private lateinit var extensionApi: ExtensionApi

    @Before
    fun setup() {
        extensionApi = Mockito.mock(ExtensionApi::class.java)
    }

    @Test
    fun `LaunchTokenFinder should return null on empty input string`() {
        // setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("")
        // verify
        assertNull(result)
    }

    @Test
    fun `get should return Event Type on valid Event`() {
        // setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~type")
        // verify
        assertEquals("com.adobe.eventType.analytics", result)
    }

    @Test
    fun `get should return Event Source on valid Event`() {
        // setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~source")
        // verify
        assertEquals("com.adobe.eventSource.requestContent", result)
    }

    @Test
    fun `get should return current unix timestamp on valid event`() {
        // setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~timestampu")
        // verify
        assertNotNull(result)
    }

    @Test
    fun `get should return current ISO8601 timestamp on valid event`() {
        // setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~timestampz")
        // verify
        assertNotNull(result)
    }

    @Test
    fun `get should return current ISO8601 date timezone on valid event`() {
        // setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~timestampp")
        // verify
        assertNotNull(result)
    }

    @Test
    fun `get should return current sdk version on valid event`() {
        // reset eventhub
        EventHub.shared = EventHub()

        // setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~sdkver")
        // verify
        assertEquals(MobileCore.extensionVersion(), result)
    }

    @Test
    fun `get should return random cachebust on valid event`() {
        // setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~cachebust") as? String
        // verify
        if (result != null) {
            assertTrue(result.toInt() < 100000000)
        }
    }

    @Test
    fun `get should return all string variables on valid event encoded in url format`() {
        // setup
        val testEventData = mapOf(
            "key1" to "value 1",
            "key8" to null
        )
        val testEvent = getDefaultEvent(testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~all_url")
        // verify
        assertEquals("key1=value%201", result)
    }

    @Test
    fun `get should return all numeric variables on valid event encoded in url format`() {
        // setup
        val testEventData = mapOf(
            "key3" to 123,
            "key4" to -456L
        )
        val testEvent = getDefaultEvent(testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~all_url")
        // verify
        assertTrue("key3=123&key4=-456" == result || "key4=-456&key3=123" == result)
    }

    @Test
    fun `get should return all boolean variables on valid event encoded in url format`() {
        // setup
        val testEventData = mapOf(
            "key2" to true,
            "key5" to -123.456
        )
        val testEvent = getDefaultEvent(testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~all_url")
        // verify
        assertTrue("key2=true&key5=-123.456" == result || "key5=-123.456&key2=true" == result)
    }

    @Test
    fun `get should return all list variables on valid event encoded in url format`() {
        // setup
        val testEventData = mapOf(
            "key6" to listOf("String1", "String2")
        )
        val testEvent = getDefaultEvent(testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~all_url")
        // verify
        assertEquals("key6=String1%2CString2", result)
    }

    @Test
    fun `get should return all map variables on valid event encoded in url format`() {
        // setup
        val testEventData = mapOf(
            "key7" to mapOf(
                "innerKey1" to "inner val1",
                "innerKey2" to "innerVal2"
            )
        )
        val testEvent = getDefaultEvent(testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~all_url")
        // verify
        assertTrue("key7.innerKey1=inner%20val1&key7.innerKey2=innerVal2" == result || "key7.innerKey2=innerVal2&key7.innerKey1=inner%20val1" == result)
    }

    @Test
    fun `get should return empty string on event with no event data for url`() {
        // setup
        val testEvent = getDefaultEvent(null)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~all_url")
        // verify
        assertEquals("", result)
    }

    /*@Test
    @Throws(JSONException::class)
    fun get_ReturnsJson_When_KeyPrefixIsAllJson() {
        //setup
        val testEventData = EventData()
        testEventData.putString("key1", "value1")
        testEventData.putBoolean("key2", true)
        testEventData.putInteger("key3", 123)
        testEventData.putLong("key4", -456L)
        testEventData.putDouble("key5", -123.456)
        testEventData.putNull("key6")
        val stringList: MutableList<String?> = ArrayList()
        stringList.add("String1")
        stringList.add("String2")
        testEventData.putStringList("key7", stringList)
        val stringMap: MutableMap<String?, String?> = HashMap()
        stringMap["key22"] = "22"
        testEventData.putStringMap("key8", stringMap)
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        //test
        val result = launchTokenFinder.get("~all_json")
        val resultObj = JSONObject(result as String)
        val expectedObj = JSONObject("{\"key1\":\"value1\",\"key2\":true,\"key5\":-123.456,\"key6\":null,\"key3\":123,\"key4\":-456,\"key7\":[\"String1\",\"String2\"],\"key8\":{\"key22\":\"22\"}}")

        //verify
        assertEquals(expectedObj, resultObj)
    } */

    @Test
    fun `get should return empty json on event with no event data for json`() {
        // setup
        val testEvent = getDefaultEvent(null)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~all_json")
        // verify
        assertEquals("", result)
    }

    @Test
    fun `get should return nested value from shared state of the module on valid event`() {
        // setup
        val testEvent = getDefaultEvent(null)
        val lcData = mapOf("analytics.contextData" to mutableMapOf("akey" to "avalue"))
        Mockito.`when`(
            extensionApi.getSharedState(
                Mockito.eq("com.adobe.marketing.mobile.Analytics"),
                Mockito.any(),
                Mockito.anyBoolean(),
                Mockito.any()
            )
        ).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                lcData
            )
        )
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result =
            launchTokenFinder.get("~state.com.adobe.marketing.mobile.Analytics/analytics.contextData.akey")
        // verify
        assertEquals("avalue", result)
    }

    @Test
    fun `get should return null when top level key is used from shared state of the module on valid event`() {
        // setup
        val testEvent = getDefaultEvent(null)
        val lcData = mapOf("analytics.contextData" to mapOf("akey" to "avalue"))
        Mockito.`when`(
            extensionApi.getSharedState(
                Mockito.eq("com.adobe.marketing.mobile.Analytics"),
                Mockito.any(),
                Mockito.anyBoolean(),
                Mockito.any()
            )
        ).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                lcData
            )
        )
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~state.com.adobe.marketing.mobile.Analytics/analytics")
        // verify
        assertNull(result)
    }

    @Test
    fun `get should return shared state list of the module on valid event`() {
        // setup
        val testEvent = getDefaultEvent(null)
        val lcData = mapOf("visitoridslist" to listOf("vid1", "vid2"))
        Mockito.`when`(
            extensionApi.getSharedState(
                Mockito.eq("com.adobe.marketing.mobile.identity"),
                Mockito.any(),
                Mockito.anyBoolean(),
                Mockito.any()
            )
        ).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                lcData
            )
        )
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result =
            launchTokenFinder.get("~state.com.adobe.marketing.mobile.identity/visitoridslist")
        // verify
        assertEquals(listOf("vid1", "vid2"), result)
    }

    @Test
    fun `get should return null when key does not have shared state name`() {
        // setup
        val testEvent = getDefaultEvent(null)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~state.")
        // verify
        assertNull(result)
    }

    @Test
    fun `get should return null when key does not have shared state key name`() {
        // setup
        val testEvent = getDefaultEvent(null)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("~state.com.adobe.marketing.mobile.Analytics/")
        // verify
        assertNull(result)
    }

    @Test
    fun `get should return null when key does not have valid format`() {
        // setup
        val testEvent = getDefaultEvent(null)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result =
            launchTokenFinder.get("~state.com.adobe/.marketing.mobile.Analytics/analytics.contextData.akey")
        // verify
        assertNull(result)
    }

    @Test
    fun `get should return null when key does not exist in shared state`() {
        // setup
        val testEvent = getDefaultEvent(null)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result =
            launchTokenFinder.get("~state.com.adobe.marketing.mobile.Analytics/analytics.contextData.akey")
        // verify
        assertNull(result)
    }

    @Test
    fun `get should return value of the key from event data on valid event`() {
        // setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("key1")
        // verify
        assertEquals("value1", result)
    }

    // TODO change if we decide to keep event data as null instead of empty map by default
    @Test
    fun `get should return empty string when event data is null on valid event`() {
        // setup
        val testEvent = getDefaultEvent(null)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("key1")
        // verify
        assertEquals("", result)
    }

    @Test
    fun `get should return null when key does not exist in event data on valid event`() {
        // setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("abc")
        // verify
        assertNull(result)
    }

    @Test
    fun `get should return null when value for the key in event data is null on valid event`() {
        // setup
        val testEvent = getDefaultEvent(mapOf("key1" to null))
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("key1")
        // verify
        assertNull(result)
    }

    @Test
    fun `get should return list on list value`() {
        // setup
        val testEventData = mapOf("key6" to listOf("String1", "String2"))
        val testEvent = getDefaultEvent(testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("key6")
        // verify
        assertEquals(listOf("String1", "String2"), result)
    }

    @Test
    fun get_ReturnsMap_When_KeyIsNotSpecialKeyAndValueIsEmptyMap() {
        // setup
        val testEventData = mapOf("key1" to emptyMap<String, Any>())
        val testEvent = getDefaultEvent(testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("key1")
        // verify
        assertEquals(
            "get should return empty map on empty map value",
            emptyMap<String, Any>(),
            result
        )
    }

    @Test
    fun `get should return null on top level key`() {
        // setup
        val testEventData = mapOf("key1" to mapOf("innerKey1" to "inner val1"))
        val testEvent = getDefaultEvent(testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("key1")
        // verify
        assertNull(result)
    }

    @Test
    fun `get should return nested value for valid flattened key on a valid event`() {
        // setup
        val testEventData = mapOf(
            "key7" to mapOf(
                "innerKey1" to "inner val1",
                "innerKey2" to "innerVal2"
            )
        )
        val testEvent = getDefaultEvent(testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, extensionApi)
        // test
        val result = launchTokenFinder.get("key7.innerKey1")
        // verify
        assertEquals("inner val1", result)
    }

    private fun getDefaultEvent(eventData: Map<String, Any?>?): Event {
        return Event.Builder(
            "TEST",
            "com.adobe.eventType.analytics",
            "com.adobe.eventSource.requestContent"
        ).setEventData(eventData).build()
    }

    private fun getDefaultEvent(): Event {
        val testEventData = mapOf("key1" to "value1")
        return getDefaultEvent(testEventData)
    }
}
