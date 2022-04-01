package com.adobe.marketing.mobile

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import org.junit.Assert.*

class LaunchTokenFinderTest: BaseTest() {

    private lateinit var configuration: TestableConfigurationExtension

    @Before
    @Throws(MissingPlatformServicesException::class)
    fun setup() {
        super.beforeEach()
        configuration = TestableConfigurationExtension(eventHub, platformServices)
    }

    @Test
    fun get_ReturnsNull_When_KeyIsEmpty() {
        //setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration, platformServices)
        //test
        val result = launchTokenFinder.get("")
        //verify
        assertNull("get should return null on empty input string", result)
    }

    @Test
    fun get_ReturnsEventType_When_KeyIsType() {
        //setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration, platformServices)
        //test
        val result = launchTokenFinder.get("~type")
        //verify
        assertEquals("get should return Event Type on valid Event", "com.adobe.eventtype.analytics", result)
    }

    @Test
    fun get_ReturnsEventSource_When_KeyIsSource() {
        //setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration, platformServices)
        //test
        val result = launchTokenFinder.get("~source")
        //verify
        assertEquals("get should return Event Source on valid Event", "com.adobe.eventsource.requestcontent", result)
    }

    @Test
    fun get_ReturnsCurrentUnixTimestamp_When_KeyPrefixIsTimestampu() {
        //setup
        val testEvent = getDefaultEvent()!!
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration!!,
                platformServices)
        //test
        val result = launchTokenFinder.get("~timestampu")
        //verify
        assertEquals("get should return current unix timestamp on valid event", TimeUtil.getUnixTimeInSeconds().toString(), result)
    }

    @Test
    fun get_ReturnsCurrentISO8601Timestamp_When_KeyPrefixIsTimestampz() {
        //setup
        val testEvent = getDefaultEvent()!!
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration!!,
                platformServices)
        //test
        val result = launchTokenFinder.get("~timestampz")
        //verify
        assertEquals("get should return current ISO8601 timestamp on valid event", TimeUtil.getIso8601Date(), result)
    }

    @Test
    fun get_ReturnsCurrentIso8601DateTimeZone_When_KeyPrefixIsTimestampp() {
        //setup
        val testEvent = getDefaultEvent()!!
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration!!,
                platformServices)
        //test
        val result = launchTokenFinder.get("~timestampp")
        //verify
        assertEquals("get should return current ISO8601 date timezone on valid event",
                TimeUtil.getIso8601DateTimeZoneISO8601(), result)
    }

    @Test
    fun expandKey_ReturnsCurrentSdkVersion_When_KeyPrefixIsSdkVersion() {
        //setup
        val testEvent = getDefaultEvent()!!
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration!!,
                platformServices)
        //test
        val result = launchTokenFinder.get("~sdkver")
        //verify
        assertEquals("get should return current sdk version on valid event", "mockSdkVersion", result)
    }

    @Test
    fun get_ReturnsRandomNumber_When_KeyPrefixIsCachebust() {
        //setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("~cachebust") as? String
        //verify
        try {
            if (result != null) {
                assertTrue("get should return random cachebust on valid event", result.toInt() < 100000000)
            }
        } catch (ex: VariantException) {
        }
    }

    @Test
    fun get_ReturnsUrlEncoded_When_KeyPrefixIsAllUrlStringOrNull() {
        //setup
        val testEventData = EventData()
        testEventData.putString("key1", "value 1")
        testEventData.putNull("key8")
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("~all_url")
        //verify
        assertEquals("get should return all variables on valid event encoded in url format",
                "&key1=value%201",
                result)
    }

    @Test
    fun get_ReturnsUrlEncoded_When_KeyPrefixIsAllUrlAndEventDataIsIntOrLong() {
        //setup
        val testEventData = EventData()
        testEventData.putInteger("key3", 123)
        testEventData.putLong("key4", -456L)
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("~all_url")
        //verify
        assertTrue("get should return all list variables on valid event encoded in url format",
                "&key3=123&key4=-456" == result || "&key4=-456&key3=123" == result)
    }

    @Test
    fun get_ReturnsUrlEncoded_When_KeyPrefixIsAllUrlAndEventDataIsDoubleOrBoolean() {
        //setup
        val testEventData = EventData()
        testEventData.putBoolean("key2", true)
        testEventData.putDouble("key5", -123.456)
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("~all_url")
        //verify
        assertTrue("get should return all list variables on valid event encoded in url format",
                "&key2=true&key5=-123.456" == result || "&key5=-123.456&key2=true" == result)
    }

    @Test
    fun get_ReturnsUrlEncoded_When_KeyPrefixIsAllUrlAndEventDataIsList() {
        //setup
        val testEventData = EventData()
        val stringList: MutableList<String> = ArrayList()
        stringList.add("String1")
        stringList.add("String2")
        testEventData.putStringList("key6", stringList)
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("~all_url")
        //verify
        assertEquals("get should return all list variables on valid event encoded in url format",
                "&key6=String1%2CString2",
                result)
    }

    @Test
    fun get_ReturnsUrlEncoded_When_KeyPrefixIsAllUrlAndEventDataIsMap() {
        //setup
        val testEventData = EventData()
        val stringMap: MutableMap<String?, String?> = HashMap()
        stringMap["innerKey1"] = "inner val1"
        stringMap["innerKey2"] = "innerVal2"
        testEventData.putStringMap("key7", stringMap)
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("~all_url")
        //verify
        assertTrue("get should return all map variables on valid event encoded in url format",
                "&key7.innerKey1=inner%20val1&key7.innerKey2=innerVal2" == result || "&key7.innerKey2=innerVal2&key7.innerKey1=inner%20val1" == result)
    }

    @Test
    fun get_ReturnsEmptyString_When_KeyPrefixIsAllUrlEventDataIsNull() {
        //setup
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, null)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("~all_url")
        //verify
        assertEquals("get should return empty string on event with no event data", "", result)
    }

    /* @Test
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
        val launchTokenFinder = LaunchTokenFinder(testEvent!!, configuration!!,
                platformServices)
        //test
        val result = launchTokenFinder.get("~all_json")
        val resultObj = JSONObject(result as String)
        val expectedObj = JSONObject("{\"key1\":\"value1\",\"key2\":true,\"key5\":-123.456,\"key6\":null,\"key3\":123,\"key4\":-456,\"key7\":[\"String1\",\"String2\"],\"key8\":{\"key22\":\"22\"}}")

        //verify
        assertTrue("get should return all variables on valid event encoded in json format",
                expectedObj.similar(resultObj))
    } */

    @Test
    fun get_ReturnsEmptyString_When_KeyPrefixIsAllJsonEventDataIsNull() {
        //setup
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, null)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("~all_json")
        //verify
        assertEquals("get should return empty string on event with no event data", "", result)
    }

    @Test
    fun get_ReturnsSharedStateKey_When_KeyPrefixIsState() {
        //setup
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, null)
        val lcdata = EventData()
        val lifecycleSharedState: MutableMap<String?, String?> = HashMap()
        lifecycleSharedState["akey"] = "avalue"
        lcdata.putStringMap("analytics.contextData", lifecycleSharedState)
        eventHub.setSharedState("com.adobe.marketing.mobile.Analytics", lcdata)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("~state.com.adobe.marketing.mobile.Analytics/analytics.contextData.akey")
        //verify
        assertEquals("get should return shared state of the module on valid event", "avalue", result)
    }

    @Test
    fun get_ReturnsSharedStateList_When_KeyPrefixIsStateAndValueIsList() {
        //setup
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, null)
        val lcdata = EventData()
        val identitySharedState: MutableList<String?> = ArrayList()
        identitySharedState.add("vid1")
        identitySharedState.add("vid2")
        lcdata.putStringList("visitoridslist", identitySharedState)
        eventHub.setSharedState("com.adobe.marketing.mobile.identity", lcdata)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("~state.com.adobe.marketing.mobile.identity/visitoridslist")
        //verify
        assertEquals("get should return shared state list of the module on valid event", identitySharedState, result)
    }

    @Test
    fun get_ReturnsSharedStateMap_When_KeyPrefixIsStateAndValueIsMap() {
        //setup
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, null)
        val lcdata = EventData()
        val lifecycleSharedState: MutableMap<String?, String?> = HashMap()
        lifecycleSharedState["akey"] = "avalue"
        lcdata.putStringMap("analytics.contextData", lifecycleSharedState)
        eventHub.setSharedState("com.adobe.marketing.mobile.Analytics", lcdata)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("~state.com.adobe.marketing.mobile.Analytics/analytics.contextData")
        //verify
        assertEquals("get should return shared state of the module on valid event", lifecycleSharedState, result)
    }

    @Test
    fun get_ReturnsNull_When_KeyPrefixIsStateAndMissingSharedStateKeyName() {
        //setup
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, null)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("~state.")
        //verify
        assertNull("get should return null when key does not have shared state name", result)
    }

    @Test
    fun get_ReturnsNull_When_KeyPrefixIsStateAndMissingKeyName() {
        //setup
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, null)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("~state.com.adobe.marketing.mobile.Analytics/")
        //verify
        assertNull("get should return null when key does not have shared state key name", result)
    }

    @Test
    fun get_ReturnsNull_When_KeyPrefixIsStateAndIncorrectFormat() {
        //setup
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, null)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("~state.com.adobe/.marketing.mobile.Analytics/analytics.contextData.akey")
        //verify
        assertNull("get should return null when key does not have valid format", result)
    }

    @Test
    fun get_ReturnsNull_When_KeyPrefixIsStateAndKeyNotExist() {
        //setup
        val testEventData = EventData()
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("~state.com.adobe.marketing.mobile.Analytics/analytics.contextData.akey")
        //verify
        assertNull("get should return null when key does not exist in shared state", result)
    }

    @Test
    fun get_ReturnsEventDataValue_When_KeyIsNotSpecialKey() {
        //setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("key1")
        //verify
        assertEquals("get should return value of the key from event data on valid event", "value1", result)
    }

    @Test
    fun get_ReturnsEmptyString_When_KeyIsNotSpecialKeyAndEventDataIsNull() {
        //setup
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, null)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("key1")
        //verify
        assertEquals("get should return empty string when event data is null on valid event", "", result)
    }

    @Test
    fun get_ReturnsNull_When_KeyIsNotSpecialKeyAndDoesNotExist() {
        //setup
        val testEvent = getDefaultEvent()
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("abc")
        //verify
        assertNull("get should return null when key does not exist in event data on valid event", result)
    }

    @Test
    fun get_ReturnsNull_When_KeyIsNotSpecialKeyAndValueIsNull() {
        //setup
        val testEventData = EventData()
        testEventData.putNull("key1")
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("key1")
        //verify
        assertNull("get should return null when value for the key in event data is null on valid event", result)
    }

    @Test
    fun get_ReturnsList_When_KeyIsNotSpecialKeyAndValueIsList() {
        //setup
        val testEventData = EventData()
        val stringList: MutableList<String?> = ArrayList()
        stringList.add("String1")
        stringList.add("String2")
        testEventData.putStringList("key6", stringList)
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("key6")
        //verify
        assertEquals("get should return empty string on list variant", stringList, result)
    }

    /* @Test
    fun get_ReturnsMap_When_KeyIsNotSpecialKeyAndValueIsEmptyMap() {
        //setup
        val testEventData = EventData()
        testEventData.putVariantMap("key1", HashMap())
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent!!, configuration!!,
                platformServices)
        //test
        val result = launchTokenFinder.get("key1")
        //verify
        assertEquals("get should return empty map on empty map variant", HashMap(), result)
    } */

    @Test
    fun get_ReturnsMap_When_KeyIsNotSpecialKeyAndValueIsMap() {
        //setup
        val testEventData = EventData()
        val stringMap: MutableMap<String?, String?> = HashMap()
        stringMap["innerKey1"] = "inner val1"
        testEventData.putStringMap("key1", stringMap)
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("key1")
        //verify
        assertEquals("get should return map on map variant", stringMap, result)
    }

    @Test
    fun get_ReturnsNestedValue_When_KeyIsFlattenedNestedKey() {
        //setup
        val testEventData = EventData()
        val stringMap: MutableMap<String?, String?> = HashMap()
        stringMap["innerKey1"] = "inner val1"
        stringMap["innerKey2"] = "innerVal2"
        testEventData.putStringMap("key7", stringMap)
        val testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData)
        val launchTokenFinder = LaunchTokenFinder(testEvent, configuration,
                platformServices)
        //test
        val result = launchTokenFinder.get("key7.innerKey1")
        //verify
        assertEquals("get should return nested value for valid flattened key on a valid event", "inner val1", result)
    }

    private fun getEvent(type: EventType?, source: EventSource?, eventData: EventData?): Event {
        return Event.Builder("TEST", type, source)
                .setData(eventData).build()
    }

    private fun getDefaultEvent(): Event {
        val testEventData = EventData()
        testEventData.putString("key1", "value1")
        return getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData)
    }
}