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

package com.adobe.marketing.mobile.identity

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.VisitorID
import com.adobe.marketing.mobile.services.DataEntity
import com.adobe.marketing.mobile.services.HitQueuing
import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private typealias NetworkMonitor = (url: String) -> Unit

@RunWith(MockitoJUnitRunner.Silent::class)
class IdentityFunctionalTests {

    companion object {
        private val FRESH_INSTALL_WITHOUT_CACHE: Map<String, Any> = emptyMap()

        private var networkMonitor: NetworkMonitor? = null

        @BeforeClass
        @JvmStatic
        fun setupClass() {
            ServiceProvider.getInstance().networkService = Networking { request, callback ->
                networkMonitor?.let { it(request.url) }
                callback.call(object : HttpConnecting {
                    override fun getInputStream(): InputStream? {
                        val json = """
                            {
                                "d_mid":"32392347938908875026252848914224372728",
                                "id_sync_ttl":604800,
                                "d_blob":"hmk_Lq6TPIBMW925SPhw3Q",
                                "dcs_region":9,
                                "d_ottl":7200,
                                "ibs":[],
                                "subdomain":"obumobile5",
                                "tid":"d47JfAKTTsU="
                            }
                        """.trimIndent()
                        return json.byteInputStream(Charsets.UTF_8)
                    }

                    override fun getErrorStream(): InputStream? {
                        return null
                    }

                    override fun getResponseCode(): Int {
                        return HttpURLConnection.HTTP_OK
                    }

                    override fun getResponseMessage(): String {
                        return ""
                    }

                    override fun getResponsePropertyValue(responsePropertyKey: String?): String {
                        return ""
                    }

                    override fun close() {}
                })
            }
        }
    }

    @Volatile
    var counter = 0

    private lateinit var mockedPersistentData: Map<String, Any>

    @Mock
    private lateinit var mockedExtensionApi: ExtensionApi

    @Mock
    private lateinit var mockedNamedCollection: NamedCollection

    @Mock
    private lateinit var mockedHitQueue: HitQueuing

    @Before
    fun setUp() {
        reset(mockedExtensionApi)
        reset(mockedNamedCollection)
        reset(mockedHitQueue)
    }

    private fun initializeIdentityExtensionWithPreset(
        cachedData: Map<String, Any>,
        configuration: Map<String, Any>
    ): IdentityExtension {
        `when`(
            mockedExtensionApi.getSharedState(
                any(),
                any(),
                anyOrNull(),
                any()
            )
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                return@thenAnswer SharedStateResult(SharedStateStatus.SET, configuration)
            }
            if ("com.adobe.module.analytics" === extension) {
                return@thenAnswer SharedStateResult(
                    SharedStateStatus.SET,
                    mapOf(
                        "vid" to "fake_vid"
                    )
                )
            }
            return@thenAnswer null
        }

        mockedPersistentData = cachedData
        `when`(mockedNamedCollection.getString(anyString(), anyString())).thenAnswer { invocation ->
            val key = invocation.arguments[0] as? String
            return@thenAnswer mockedPersistentData[key]
        }

        val identityExtension =
            IdentityExtension(mockedExtensionApi, mockedNamedCollection, mockedHitQueue)
        identityExtension.onRegistered()
        val countDownLatch = CountDownLatch(2)
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            val hit = IdentityHit.fromDataEntity(entity)
            if (hit != null) {
                assertTrue(hit.url.contains("https://test.com/id?"))
                IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
            }
            return@thenAnswer true
        }
        networkMonitor = { url ->
            if (url.contains("https://test.com/id?")) {
                countDownLatch.countDown()
            }
        }

        identityExtension.readyForEvent(Event.Builder("event", "type", "source").build())

        val eventCaptor = ArgumentCaptor.forClass(Event::class.java)
        verify(mockedExtensionApi, atLeast(1)).dispatch(eventCaptor.capture())
        eventCaptor.allValues.forEach { event ->
            assertNotNull(event.eventData)
            if (event.eventData.contains("forcesync") && event.eventData.contains("issyncevent") && event.eventData.contains(
                    "issyncevent"
                )
            ) {
                identityExtension.processIdentityRequest(event)
            }
        }

        //        countDownLatch.await()
        assertTrue(countDownLatch.await(500, TimeUnit.MILLISECONDS))
        reset(mockedHitQueue)

        return identityExtension
    }

    private fun initializeIdentityExtensionWithoutValidConfiguration(cachedData: Map<String, Any>): IdentityExtension {
        `when`(
            mockedExtensionApi.getSharedState(
                any(),
                any(),
                anyOrNull(),
                any()
            )
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                return@thenAnswer null
            }
            return@thenAnswer null
        }

        mockedPersistentData = cachedData
        `when`(mockedNamedCollection.getString(any(), any())).thenAnswer { invocation ->
            val key = invocation.arguments[0] as? String
            return@thenAnswer mockedPersistentData[key]
        }

        val identityExtension =
            IdentityExtension(mockedExtensionApi, mockedNamedCollection, mockedHitQueue)
        identityExtension.onRegistered()

        identityExtension.readyForEvent(Event.Builder("event", "type", "source").build())
        reset(mockedHitQueue)

        return identityExtension
    }

    @Test(timeout = 10000)
    fun test_syncIdentifier_validateQueryParams_happy() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        // setup
        val countDownLatch = CountDownLatch(2)
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            val hit = IdentityHit.fromDataEntity(entity)
            if (hit != null) {
                assertTrue(hit.url.contains("https://test.com/id?"))
                assertTrue(hit.url.contains("d_cid_ic=idTypeSYNC"))
                assertTrue(hit.url.contains("idValueSYNC"))
                assertTrue(hit.url.contains("d_ver=2"))
                assertTrue(hit.url.contains("d_orgid=orgid"))
                IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
            }
            return@thenAnswer true
        }

        networkMonitor = { url ->
            if (url.contains("https://test.com/id?")) {
                assertTrue(url.contains("d_mid"))
                assertTrue(url.contains("d_cid_ic=idTypeSYNC"))
                assertTrue(url.contains("idValueSYNC"))
                assertTrue(url.contains("d_ver=2"))
                assertTrue(url.contains("d_orgid=orgid"))
                countDownLatch.countDown()
            }
        }

        // test
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "visitoridentifiers" to mapOf<String, Any>(
                        "idTypeSYNC" to "idValueSYNC"
                    ),
                    "authenticationstate" to VisitorID.AuthenticationState.AUTHENTICATED.value,
                    "forcesync" to false,
                    "issyncevent" to true
                )
            ).build()
        )
        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun test_syncIdentifiers_validateQueryParams_happy() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        // setup
        val countDownLatch = CountDownLatch(2)
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            val hit = IdentityHit.fromDataEntity(entity)
            if (hit != null) {
                assertTrue(hit.url.contains("https://test.com/id?"))
                assertTrue(hit.url.contains("d_cid_ic=ghi%01jkl%011"))
                assertTrue(hit.url.contains("d_cid_ic=123%01456%011"))
                assertTrue(hit.url.contains("d_cid_ic=abc%01def%011"))
                assertTrue(hit.url.contains("d_ver=2"))
                assertTrue(hit.url.contains("d_orgid=orgid"))
//                assertTrue(hit.url.contains("d_blob=hmk_Lq6TPIBMW925SPhw3Q"))
                IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
            }
            return@thenAnswer true
        }

        networkMonitor = { url ->
            if (url.contains("https://test.com/id?")) {
                assertTrue(url.contains("https://test.com/id?"))
                assertTrue(url.contains("d_cid_ic=ghi%01jkl%011"))
                assertTrue(url.contains("d_cid_ic=123%01456%011"))
                assertTrue(url.contains("d_cid_ic=abc%01def%011"))
                assertTrue(url.contains("d_ver=2"))
                assertTrue(url.contains("d_orgid=orgid"))
//                assertTrue(url.contains("d_blob=hmk_Lq6TPIBMW925SPhw3Q"))
                countDownLatch.countDown()
            }
        }

        // test
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "visitoridentifiers" to mapOf<String, Any>(
                        "abc" to "def",
                        "123" to "456",
                        "ghi" to "jkl"
                    ),
                    "authenticationstate" to VisitorID.AuthenticationState.AUTHENTICATED.value,
                    "forcesync" to false,
                    "issyncevent" to true
                )
            ).build()
        )
        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun test_setAdvertisingIdentifier_validateIDFA_happy() {
        // setup
        val rand = Random()
        val randomString = (rand.nextInt(10000000) + 1).toString()
        val testAdvertisingId = "TestAdvertisingID$randomString"

        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )

        val countDownLatch = CountDownLatch(2)
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            val hit = IdentityHit.fromDataEntity(entity)
            if (hit != null) {
                assertTrue(hit.url.contains("https://test.com/id?"))
                assertTrue(hit.url.contains(testAdvertisingId))
                IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
            }
            return@thenAnswer true
        }

        networkMonitor = { url ->
            if (url.contains("https://test.com/id?")) {
                assertTrue(url.contains(testAdvertisingId))
                countDownLatch.countDown()
            }
        }

        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.generic.identity",
                "com.adobe.eventSource.requestContent"
            ).setEventData(
                mapOf(
                    "advertisingidentifier" to testAdvertisingId
                )
            ).build()
        )

        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun test_syncIdentifiers_nullAndEmptyIdTypeAndIdentifier_ValidateQueryParams() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )

        // setup
        val countDownLatch = CountDownLatch(2)
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            val hit = IdentityHit.fromDataEntity(entity)
            if (hit != null) {
                assertTrue(hit.url.contains("https://test.com/id?"))
                assertTrue(hit.url.contains("d_cid_ic=keya%01value1%011"))
                assertTrue(hit.url.contains("d_cid_ic=keyf%01value6%011"))
                assertFalse(hit.url.contains("keyb"))
                assertFalse(hit.url.contains("value3"))
                assertFalse(hit.url.contains("value4"))
                assertFalse(hit.url.contains("keye"))
//                assertTrue(hit.url.contains("d_blob=hmk_Lq6TPIBMW925SPhw3Q"))
                IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
            }
            return@thenAnswer true
        }

        networkMonitor = { url ->
            if (url.contains("https://test.com/id?")) {
                assertTrue(url.contains("d_cid_ic=keya%01value1%011"))
                assertTrue(url.contains("d_cid_ic=keyf%01value6%011"))
                assertFalse(url.contains("keyb"))
                assertFalse(url.contains("value3"))
                assertFalse(url.contains("value4"))
                assertFalse(url.contains("keye"))
//                assertTrue(url.contains("d_blob=hmk_Lq6TPIBMW925SPhw3Q"))
                countDownLatch.countDown()
            }
        }

        // test
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "visitoridentifiers" to mapOf<String?, Any?>(
                        "keya" to "value1",
                        "keyb" to "",
                        "" to "value3",
                        null to "value4",
                        "keye" to null,
                        "keyf" to "value6"
                    ),
                    "authenticationstate" to VisitorID.AuthenticationState.AUTHENTICATED.value,
                    "forcesync" to false,
                    "issyncevent" to true
                )
            ).build()
        )
        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun test_setAdvertisingIdentifier_valueChanged_syncCallsSentForValidValues() {
        // setup
        val rand = Random()
        val testAdvertisingId1 = "TestAdvertisingID" + (rand.nextInt(10000000) + 1).toString()
        val testAdvertisingId2 = "TestAdvertisingID" + (rand.nextInt(10000000) + 1).toString()

        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        val countDownLatch = CountDownLatch(3)
        counter = 0
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            counter++
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            val hit = IdentityHit.fromDataEntity(entity)
            if (hit != null) {
                when (counter) {
                    1 -> {
                        assertTrue(hit.url.contains("https://test.com/id?"))
                        assertTrue(hit.url.contains(testAdvertisingId1))
                        IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
                    }
                    2 -> {
                        assertTrue(hit.url.contains("https://test.com/id?"))
                        assertTrue(hit.url.contains("DSID_20914&"))
                        IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
                    }
                    3 -> {
                        assertTrue(hit.url.contains("https://test.com/id?"))
                        assertTrue(hit.url.contains(testAdvertisingId2))
                        IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
                    }
                }
            }
            return@thenAnswer true
        }

        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.generic.identity",
                "com.adobe.eventSource.requestContent"
            ).setEventData(
                mapOf(
                    "advertisingidentifier" to testAdvertisingId1
                )
            ).build()
        )
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.generic.identity",
                "com.adobe.eventSource.requestContent"
            ).setEventData(
                mapOf(
                    "advertisingidentifier" to null
                )
            ).build()
        )
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.generic.identity",
                "com.adobe.eventSource.requestContent"
            ).setEventData(
                mapOf(
                    "advertisingidentifier" to ""
                )
            ).build()
        )
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.generic.identity",
                "com.adobe.eventSource.requestContent"
            ).setEventData(
                mapOf(
                    "advertisingidentifier" to testAdvertisingId2
                )
            ).build()
        )

        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun test_setAdvertisingIdentifier_sameValueTwice_syncsOnlyOnce() {
        // setup
        val rand = Random()
        val randomString = (rand.nextInt(10000000) + 1).toString()
        val testAdvertisingId = "TestAdvertisingID$randomString"

        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )

        val countDownLatch1 = CountDownLatch(1)
        val countDownLatch2 = CountDownLatch(1)
        counter = 0
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            counter++
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            val hit = IdentityHit.fromDataEntity(entity)
            when (counter) {
                1 -> {
                    if (hit != null) {
                        assertTrue(hit.url.contains("https://test.com/id?"))
                        assertTrue(hit.url.contains(testAdvertisingId))
                        IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch1.countDown() }
                    }
                }
                2 -> {
                    if (hit != null) {
                        assertTrue(hit.url.contains("https://test.com/id?"))
                        assertTrue(hit.url.contains(testAdvertisingId))
                        countDownLatch2.countDown()
                    }
                }
            }

            return@thenAnswer true
        }

        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.generic.identity",
                "com.adobe.eventSource.requestContent"
            ).setEventData(
                mapOf(
                    "advertisingidentifier" to testAdvertisingId
                )
            ).build()
        )

        countDownLatch1.await()

        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.generic.identity",
                "com.adobe.eventSource.requestContent"
            ).setEventData(
                mapOf(
                    "advertisingidentifier" to testAdvertisingId
                )
            ).build()
        )

        assertFalse(countDownLatch2.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test(timeout = 10000)
    fun test_setAdvertisingIdentifier_sameValueTwice_getIdentifiersReturnsOne() {
        val rand = Random()
        val randomString = (rand.nextInt(10000000) + 1).toString()
        val testAdvertisingId = "TestAdvertisingID$randomString"

        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        val countDownLatch = CountDownLatch(1)
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
            countDownLatch.countDown()
            return@thenAnswer true
        }

        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.generic.identity",
                "com.adobe.eventSource.requestContent"
            ).setEventData(
                mapOf(
                    "advertisingidentifier" to testAdvertisingId
                )
            ).build()
        )

        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.generic.identity",
                "com.adobe.eventSource.requestContent"
            ).setEventData(
                mapOf(
                    "advertisingidentifier" to testAdvertisingId
                )
            ).build()
        )

        countDownLatch.await()
        val countDownLatchGetter = CountDownLatch(1)
        doAnswer { invocation ->
            val event: Event? = invocation.arguments[0] as Event?
            val eventData = event?.eventData
            assertNotNull(eventData)
            val jsonObject = JSONObject(eventData)
            val jsonArray = jsonObject.getJSONArray("visitoridslist")
            assertEquals(1, jsonArray.length())
            val visitorId1 = jsonArray.getJSONObject(0)
            assertEquals("d_cid_ic", visitorId1.getString("ID_ORIGIN"))
            assertEquals(testAdvertisingId, visitorId1.getString("ID"))
            assertEquals("DSID_20914", visitorId1.getString("ID_TYPE"))
            assertEquals(1, visitorId1.getInt("STATE"))
            countDownLatchGetter.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).build()
        )
        countDownLatchGetter.await()
    }

    @Test(timeout = 10000)
    fun test_setAdvertisingIdentifier_newValue_getIdentifiersReturnsOne() {
        val rand = Random()
        val testAdvertisingId1 = "TestAdvertisingID" + (rand.nextInt(10000000) + 1).toString()
        val testAdvertisingId2 = "TestAdvertisingID" + (rand.nextInt(10000000) + 1).toString()

        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        val countDownLatch = CountDownLatch(2)
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }

            return@thenAnswer true
        }

        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.generic.identity",
                "com.adobe.eventSource.requestContent"
            ).setEventData(
                mapOf(
                    "advertisingidentifier" to testAdvertisingId1
                )
            ).build()
        )

        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.generic.identity",
                "com.adobe.eventSource.requestContent"
            ).setEventData(
                mapOf(
                    "advertisingidentifier" to testAdvertisingId2
                )
            ).build()
        )

        countDownLatch.await()
        val countDownLatchGetter = CountDownLatch(1)
        doAnswer { invocation ->
            val event: Event? = invocation.arguments[0] as Event?
            val eventData = event?.eventData
            assertNotNull(eventData)
            val jsonObject = JSONObject(eventData)
            val jsonArray = jsonObject.getJSONArray("visitoridslist")
            assertEquals(1, jsonArray.length())
            val visitorId1 = jsonArray.getJSONObject(0)
            assertEquals("d_cid_ic", visitorId1.getString("ID_ORIGIN"))
            assertEquals(testAdvertisingId2, visitorId1.getString("ID"))
            assertEquals("DSID_20914", visitorId1.getString("ID_TYPE"))
            assertEquals(1, visitorId1.getInt("STATE"))
            countDownLatchGetter.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).build()
        )
        countDownLatchGetter.await()
    }

    @Test(timeout = 10000)
    fun test_getExperienceCloudId_verifyValidMidRetrieval_happy() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )

        val countDownLatchGetter = CountDownLatch(1)
        doAnswer { invocation ->
            val event: Event? = invocation.arguments[0] as Event?
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertTrue(eventData?.get("mid").toString().isNotEmpty())
            countDownLatchGetter.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).build()
        )
        countDownLatchGetter.await()
    }

    @Test(timeout = 10000)
    fun test_getExperienceCloudId_withoutValidConfiguration() {
        val identityExtension =
            initializeIdentityExtensionWithoutValidConfiguration(mapOf("ADOBEMOBILE_PERSISTED_MID" to "mid_1"))

        val countDownLatchGetter = CountDownLatch(1)
        doAnswer { invocation ->
            val event: Event? = invocation.arguments[0] as Event?
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertEquals("mid_1", eventData?.get("mid").toString())
            countDownLatchGetter.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).build()
        )
        countDownLatchGetter.await()
    }

    @Test(timeout = 10000)
    fun test_getIdentifiers_validateReturnedIdentifiers_happy() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        // setup
        val countDownLatch = CountDownLatch(1)
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            val hit = IdentityHit.fromDataEntity(entity)
            if (hit != null) {
                IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
            }
            return@thenAnswer true
        }
        val vidList = mapOf<String, Any>(
            "abc" to "def",
            "123" to "456",
            "ghi" to "jkl"
        )
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "visitoridentifiers" to vidList,
                    "authenticationstate" to VisitorID.AuthenticationState.AUTHENTICATED.value,
                    "forcesync" to false,
                    "issyncevent" to true
                )
            ).build()
        )
        countDownLatch.await()
        val countDownLatchGetter = CountDownLatch(1)
        doAnswer { invocation ->
            val event: Event? = invocation.arguments[0] as Event?
            val eventData = event?.eventData
            assertNotNull(eventData)
            val jsonObject = JSONObject(eventData)
            val jsonArray = jsonObject.getJSONArray("visitoridslist")
            assertEquals(3, jsonArray.length())
            for (i in 0 until jsonArray.length()) {
                val vid = jsonArray.getJSONObject(i)
                assertEquals(vidList[vid.getString("ID_TYPE")], vid.getString("ID"))
                assertEquals(1, vid.getInt("STATE"))
            }
            countDownLatchGetter.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).build()
        )
        countDownLatchGetter.await()
    }

    @Test(timeout = 10000)
    fun test_getIdentifiers_invalidIdentifiers_validateReturnedIdentifiers() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        // setup
        val countDownLatch = CountDownLatch(1)
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            val hit = IdentityHit.fromDataEntity(entity)
            if (hit != null) {
                IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
            }
            return@thenAnswer true
        }
        val vidList = mapOf<String?, Any?>(
            "keya" to "value1",
            "keyb" to "",
            "" to "value3",
            null to "value4",
            "keye" to null,
            "keyf" to "value6"
        )
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "visitoridentifiers" to vidList,
                    "authenticationstate" to VisitorID.AuthenticationState.AUTHENTICATED.value,
                    "forcesync" to false,
                    "issyncevent" to true
                )
            ).build()
        )
        countDownLatch.await()
        val countDownLatchGetter = CountDownLatch(1)
        doAnswer { invocation ->
            val event: Event? = invocation.arguments[0] as Event?
            val eventData = event?.eventData
            assertNotNull(eventData)
            val jsonObject = JSONObject(eventData)
            val jsonArray = jsonObject.getJSONArray("visitoridslist")
            assertEquals(2, jsonArray.length())
            for (i in 0 until jsonArray.length()) {
                val vid = jsonArray.getJSONObject(i)
                assertEquals(vidList[vid.getString("ID_TYPE")], vid.getString("ID"))
                assertEquals(1, vid.getInt("STATE"))
            }
            countDownLatchGetter.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).build()
        )
        countDownLatchGetter.await()
    }

    @Test(timeout = 10000)
    fun test_appendToUrl_verifyExperienceCloudIdentifierPresentInUrl() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        val countDownLatchGetter = CountDownLatch(1)
        doAnswer { invocation ->
            val event: Event? = invocation.arguments[0] as Event?
            val eventData = event?.eventData
            assertNotNull(eventData)
            val updatedUrl = eventData?.get("updatedurl") as String
            assertTrue(updatedUrl.contains("TS%3D"))
            assertTrue(updatedUrl.contains("MCMID%3D"))
            assertTrue(updatedUrl.contains("MCORGID%3Dorgid"))
            countDownLatchGetter.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "baseurl" to "http://testURL"
                )
            ).build()
        )
        countDownLatchGetter.await()
    }

    @Test(timeout = 10000)
    fun test_appendToUrl_passNullUrl_returnsNull() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        val countDownLatchGetter = CountDownLatch(1)
        doAnswer { invocation ->
            val event: Event? = invocation.arguments[0] as Event?
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertNull(eventData?.get("updatedurl"))
            countDownLatchGetter.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "baseurl" to null
                )
            ).build()
        )
        countDownLatchGetter.await()
    }

    @Test(timeout = 10000)
    fun test_appendToUrl_passEmptyUrl_returnsEmpty() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        val countDownLatchGetter = CountDownLatch(1)
        doAnswer { invocation ->
            val event: Event? = invocation.arguments[0] as Event?
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertTrue("" === eventData?.get("updatedurl"))
            countDownLatchGetter.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "baseurl" to ""
                )
            ).build()
        )
        countDownLatchGetter.await()
    }

    @Test(timeout = 10000)
    fun test_appendToUrl_passInvalidUrl_returnsAppendedParameters() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        val countDownLatchGetter = CountDownLatch(1)
        doAnswer { invocation ->
            val event: Event? = invocation.arguments[0] as Event?
            val eventData = event?.eventData
            assertNotNull(eventData)
            val updatedUrl = eventData?.get("updatedurl") as String
            assertTrue(updatedUrl.contains("invalid <url> ^^string%"))
            assertTrue(updatedUrl.contains("TS%3D"))
            assertTrue(updatedUrl.contains("MCMID%3D"))
            assertTrue(updatedUrl.contains("MCORGID%3Dorgid"))
            countDownLatchGetter.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "baseurl" to "invalid <url> ^^string%"
                )
            ).build()
        )
        countDownLatchGetter.await()
    }

    @Test(timeout = 10000)
    fun test_setPushIdentifier_SyncsNewValue() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )

        val token = "D52DB39EEE21395B2B67B895FC478301CE6E936D82521E095902A5E0F57EE0B3"
        val newToken = "D52DB39EEE21395B2B67B895FC478301CE6E936D82521E095902A5E0F57EE1A3"
        val countDownLatch = CountDownLatch(3)
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            val hit = IdentityHit.fromDataEntity(entity)
            if (hit != null) {
                IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
            }
            return@thenAnswer true
        }

        counter = 0
        networkMonitor = { url ->
            if (url.contains("https://test.com/id?")) {
                counter++
                when (counter) {
                    1 -> {
                        assertTrue(url.contains("d_cid=20919%01D52DB39EEE21395B2B67B895FC478301CE6E936D82521E095902A5E0F57EE0B3"))
                        countDownLatch.countDown()
                    }
                    2 -> {
                        assertTrue(url.contains("d_cid=20919%01D52DB39EEE21395B2B67B895FC478301CE6E936D82521E095902A5E0F57EE1A3"))
                        countDownLatch.countDown()
                    }
                }
            }
        }

        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.generic.identity",
                "com.adobe.eventSource.requestContent"
            ).setEventData(
                mapOf(
                    "pushidentifier" to token
                )
            ).build()
        )
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.generic.identity",
                "com.adobe.eventSource.requestContent"
            ).setEventData(
                mapOf(
                    "pushidentifier" to newToken
                )
            ).build()
        )

        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun test_getUrlVariables_VerifyMarketingCloudIdentifiersPresentInVariables() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        val countDownLatch = CountDownLatch(1)

        doAnswer { invocation ->
            val event: Event? = invocation.arguments[0] as Event?
            val eventData = event?.eventData
            assertNotNull(eventData)
            val urlVars = eventData?.get("urlvariables") as String
            assertTrue(urlVars.contains("TS%3D"))
            assertTrue(urlVars.contains("MCMID%3D"))
            assertTrue(urlVars.contains("MCORGID%3Dorgid"))
            assertFalse(urlVars.contains("?"))
            assertFalse(urlVars.contains("MCAID"))
            assertTrue(urlVars.contains("adobe_aa_vid=fake_vid"))
            countDownLatch.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())

        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestContent"
            ).setEventData(
                mapOf(
                    "urlvariables" to true
                )
            ).build()
        )
        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun test_getUrlVariables_setPrivacyOptOut_verifyOrgIdPresentInVariables() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        val countDownLatch = CountDownLatch(1)
        networkMonitor = { url ->
            if (url.contains("https://test.com/demoptout.jpg?")) {
                assertTrue(url.contains("d_mid="))
                assertTrue(url.contains("d_orgid=orgid"))
                countDownLatch.countDown()
            }
        }

        identityExtension.handleConfiguration(
            Event.Builder(
                "event",
                "com.adobe.eventType.configuration",
                "com.adobe.eventSource.responseContent"
            ).setEventData(
                mapOf(
                    "experienceCloud.org" to "orgid",
                    "experienceCloud.server" to "test.com",
                    "global.privacy" to "optedout"
                )
            ).build()
        )
        countDownLatch.await()
        val countDownLatchGetter = CountDownLatch(1)

        doAnswer { invocation ->
            val event: Event? = invocation.arguments[0] as Event?
            val eventData = event?.eventData
            assertNotNull(eventData)
            val urlVars = eventData?.get("urlvariables") as String
            assertTrue(urlVars.contains("TS%3D"))
            assertFalse(urlVars.contains("MCMID%3D"))
            assertTrue(urlVars.contains("MCORGID%3Dorgid"))
            assertFalse(urlVars.contains("?"))
            assertFalse(urlVars.contains("MCAID"))
            assertTrue(urlVars.contains("adobe_aa_vid=fake_vid"))
            countDownLatchGetter.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())

        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestContent"
            ).setEventData(
                mapOf(
                    "urlvariables" to true
                )
            ).build()
        )
        countDownLatchGetter.await()
    }

    @Test(timeout = 10000)
    fun test_syncIdentifier_firstWithNullIdentifier_doesNotCrash() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        // setup
        val countDownLatch = CountDownLatch(2)
        val countDownLatchSecond = CountDownLatch(1)
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            val hit = IdentityHit.fromDataEntity(entity)
            if (hit != null) {
                IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
            }
            return@thenAnswer true
        }
        counter = 0
        networkMonitor = { url ->
            counter++
            when (counter) {
                1 -> {
                    assertTrue(url.contains("https://test.com/id?"))
                    assertTrue(url.contains("d_mid"))
                    assertTrue(url.contains("d_cid_ic=test1234%01ca%01"))
                    assertTrue(url.contains("ca"))
                    assertTrue(url.contains("d_ver=2"))
                    assertTrue(url.contains("d_orgid=orgid"))
                    countDownLatch.countDown()
                }
                2 -> {
                    countDownLatchSecond.countDown()
                }
            }
        }

        // test
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "visitoridentifiers" to mapOf<String, Any?>(
                        "test1234" to null
                    ),
                    "authenticationstate" to VisitorID.AuthenticationState.AUTHENTICATED.value,
                    "forcesync" to false,
                    "issyncevent" to true
                )
            ).build()
        )
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "visitoridentifiers" to mapOf<String, Any?>(
                        "test1234" to "ca"
                    ),
                    "authenticationstate" to VisitorID.AuthenticationState.AUTHENTICATED.value,
                    "forcesync" to false,
                    "issyncevent" to true
                )
            ).build()
        )
        countDownLatch.await()
        assertFalse(countDownLatchSecond.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test(timeout = 10000)
    fun test_syncIdentifier_withTwoNullIdentifiers_doesNotCrash() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        // setup
        val countDownLatch = CountDownLatch(2)
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            val hit = IdentityHit.fromDataEntity(entity)
            if (hit != null) {
                IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
            }
            return@thenAnswer true
        }
        networkMonitor = {
            countDownLatch.countDown()
        }

        // test
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "visitoridentifiers" to mapOf<String, Any?>(
                        "test1234" to null
                    ),
                    "authenticationstate" to VisitorID.AuthenticationState.AUTHENTICATED.value,
                    "forcesync" to false,
                    "issyncevent" to true
                )
            ).build()
        )
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "visitoridentifiers" to mapOf<String, Any?>(
                        "test1234" to null
                    ),
                    "authenticationstate" to VisitorID.AuthenticationState.AUTHENTICATED.value,
                    "forcesync" to false,
                    "issyncevent" to true
                )
            ).build()
        )
        assertFalse(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test(timeout = 10000)
    fun test_syncIdentifier_withSecondNullIdentifier_doesNotCrash() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        // setup
        val countDownLatch = CountDownLatch(2)
        val countDownLatchSecond = CountDownLatch(1)
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            val hit = IdentityHit.fromDataEntity(entity)
            if (hit != null) {
                IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
            }
            return@thenAnswer true
        }
        counter = 0
        networkMonitor = { url ->
            counter++
            when (counter) {
                1 -> {
                    assertTrue(url.contains("https://test.com/id?"))
                    assertTrue(url.contains("d_mid"))
                    assertTrue(url.contains("d_cid_ic=test1234%01ca%01"))
                    assertTrue(url.contains("ca"))
                    assertTrue(url.contains("d_ver=2"))
                    assertTrue(url.contains("d_orgid=orgid"))
                    countDownLatch.countDown()
                }
                2 -> countDownLatchSecond.countDown()
            }
        }

        // test
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "visitoridentifiers" to mapOf<String, Any?>(
                        "test1234" to "ca"
                    ),
                    "authenticationstate" to VisitorID.AuthenticationState.AUTHENTICATED.value,
                    "forcesync" to false,
                    "issyncevent" to true
                )
            ).build()
        )
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "visitoridentifiers" to mapOf<String, Any?>(
                        "test1234" to null
                    ),
                    "authenticationstate" to VisitorID.AuthenticationState.AUTHENTICATED.value,
                    "forcesync" to false,
                    "issyncevent" to true
                )
            ).build()
        )
        countDownLatch.await()
        assertFalse(countDownLatchSecond.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test(timeout = 10000)
    fun test_syncIdentifier_withSecondNullIdentifier_clearsIdentifier() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        // setup
        val countDownLatch = CountDownLatch(2)
        val countDownLatchSecond = CountDownLatch(1)
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            val hit = IdentityHit.fromDataEntity(entity)
            if (hit != null) {
                IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
            }
            return@thenAnswer true
        }
        counter = 0
        networkMonitor = {
            counter++
            when (counter) {
                1 -> {
                    countDownLatch.countDown()
                }
                2 -> {
                    countDownLatchSecond.countDown()
                }
            }
        }

        // test
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "visitoridentifiers" to mapOf<String, Any?>(
                        "test1234" to "ca"
                    ),
                    "authenticationstate" to VisitorID.AuthenticationState.AUTHENTICATED.value,
                    "forcesync" to false,
                    "issyncevent" to true
                )
            ).build()
        )
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).setEventData(
                mapOf(
                    "visitoridentifiers" to mapOf<String, Any?>(
                        "test1234" to null
                    ),
                    "authenticationstate" to VisitorID.AuthenticationState.AUTHENTICATED.value,
                    "forcesync" to false,
                    "issyncevent" to true
                )
            ).build()
        )
        countDownLatch.await()
        assertFalse(countDownLatchSecond.await(1000, TimeUnit.MILLISECONDS))

        val countDownLatchGetter = CountDownLatch(1)
        doAnswer { invocation ->
            val event: Event? = invocation.arguments[0] as Event?
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertNull(eventData?.get("visitoridslist"))
            countDownLatchGetter.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        identityExtension.processIdentityRequest(
            Event.Builder(
                "event",
                "com.adobe.eventType.identity",
                "com.adobe.eventSource.requestIdentity"
            ).build()
        )
        countDownLatchGetter.await()
    }

    @Test(timeout = 10000)
    fun test_whenPrivacyChangedToOptout_sendsOptOutRequest() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        val countDownLatch = CountDownLatch(1)
        networkMonitor = { url ->
            if (url.contains("https://test.com/demoptout.jpg?")) {
                assertTrue(url.contains("d_mid="))
                assertTrue(url.contains("d_orgid=orgid"))
                countDownLatch.countDown()
            }
        }

        identityExtension.handleConfiguration(
            Event.Builder(
                "event",
                "com.adobe.eventType.configuration",
                "com.adobe.eventSource.responseContent"
            ).setEventData(
                mapOf(
                    "experienceCloud.org" to "orgid",
                    "experienceCloud.server" to "test.com",
                    "global.privacy" to "optedout"
                )
            ).build()
        )
        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun test_whenPrivacyChangedToOptoutThenOptoutAgain_sendsOptOutRequestOnce() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        val countDownLatch = CountDownLatch(1)
        val countDownLatchSecond = CountDownLatch(1)
        counter = 0
        networkMonitor = { url ->
            counter++
            if (counter == 2) {
                countDownLatchSecond.countDown()
            }
            if (url.contains("https://test.com/demoptout.jpg?")) {
                assertTrue(url.contains("d_mid="))
                assertTrue(url.contains("d_orgid=orgid"))
                countDownLatch.countDown()
            }
        }

        val event = Event.Builder(
            "event",
            "com.adobe.eventType.configuration",
            "com.adobe.eventSource.responseContent"
        ).setEventData(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedout"
            )
        ).build()
        identityExtension.handleConfiguration(event)
        identityExtension.handleConfiguration(event)
        identityExtension.handleConfiguration(event)

        countDownLatch.await()
        assertFalse(countDownLatchSecond.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test(timeout = 10000)
    fun test_whenPrivacyChangedToOptoutOptinOptout_sendsOptOutRequestTwice() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        val countDownLatchForOptedOut = CountDownLatch(2)
        networkMonitor = { url ->
            if (url.contains("https://test.com/demoptout.jpg?")) {
                assertTrue(url.contains("d_mid="))
                assertTrue(url.contains("d_orgid=orgid"))
                countDownLatchForOptedOut.countDown()
            }
        }
        val eventCaptor = ArgumentCaptor.forClass(Event::class.java)
        doAnswer { invocation ->
            val event = invocation.arguments[0] as? Event ?: return@doAnswer null
            assertNotNull(event.eventData)
            if (event.eventData.contains("forcesync") && event.eventData.contains("issyncevent")) {
                identityExtension.processIdentityRequest(event)
            }
            return@doAnswer null
        }.`when`(mockedExtensionApi).dispatch(any())
        verify(mockedExtensionApi, atLeast(1)).dispatch(eventCaptor.capture())

        val optedoutEvent = Event.Builder(
            "event",
            "com.adobe.eventType.configuration",
            "com.adobe.eventSource.responseContent"
        ).setEventData(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedout"
            )
        ).build()
        identityExtension.handleConfiguration(optedoutEvent)
        identityExtension.handleConfiguration(
            Event.Builder(
                "event",
                "com.adobe.eventType.configuration",
                "com.adobe.eventSource.responseContent"
            ).setEventData(
                mapOf(
                    "experienceCloud.org" to "orgid",
                    "experienceCloud.server" to "test.com",
                    "global.privacy" to "optedin"
                )
            ).build()
        )

        identityExtension.handleConfiguration(optedoutEvent)

        countDownLatchForOptedOut.await()
    }

    @Test(timeout = 10000)
    fun test_whenPrivacyChangedToUnknownThenOptout_sendsOptOutRequestOnce() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        val countDownLatch = CountDownLatch(1)
        val countDownLatchSecond = CountDownLatch(1)
        counter = 0
        networkMonitor = { url ->
            counter++
            if (counter == 2) {
                countDownLatchSecond.countDown()
            }
            if (url.contains("https://test.com/demoptout.jpg?")) {
                assertTrue(url.contains("d_mid="))
                assertTrue(url.contains("d_orgid=orgid"))
                countDownLatch.countDown()
            }
        }

        identityExtension.handleConfiguration(
            Event.Builder(
                "event",
                "com.adobe.eventType.configuration",
                "com.adobe.eventSource.responseContent"
            ).setEventData(
                mapOf(
                    "experienceCloud.org" to "orgid",
                    "experienceCloud.server" to "test.com",
                    "global.privacy" to "optunknown"
                )
            ).build()
        )
        identityExtension.handleConfiguration(
            Event.Builder(
                "event",
                "com.adobe.eventType.configuration",
                "com.adobe.eventSource.responseContent"
            ).setEventData(
                mapOf(
                    "experienceCloud.org" to "orgid",
                    "experienceCloud.server" to "test.com",
                    "global.privacy" to "optedout"
                )
            ).build()
        )

        countDownLatch.await()
        assertFalse(countDownLatchSecond.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test(timeout = 10000)
    fun test_handleIdentityResponseIdentityForSharedState_null_event() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        reset(mockedExtensionApi)
        identityExtension.handleIdentityResponseIdentityForSharedState(null)
        verify(mockedExtensionApi, never()).createSharedState(any(), any())
    }

    @Test(timeout = 10000)
    fun test_handleIdentityResponseIdentityForSharedState_null_eventData() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        reset(mockedExtensionApi)
        identityExtension.handleIdentityResponseIdentityForSharedState(
            Event.Builder("event", "type", "source").build()
        )
        verify(mockedExtensionApi, never()).createSharedState(any(), any())
    }

    @Test(timeout = 10000)
    fun test_handleIdentityResponseIdentityForSharedState_invalid_eventData() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        reset(mockedExtensionApi)
        identityExtension.handleIdentityResponseIdentityForSharedState(
            Event.Builder("event", "type", "source")
                .setEventData(
                    mapOf(
                        "invalid_updatesharedstate" to true
                    )
                ).build()
        )
        verify(mockedExtensionApi, never()).createSharedState(any(), any())
    }

    @Test(timeout = 10000)
    fun test_handleIdentityResponseIdentityForSharedState_happy() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )
        reset(mockedExtensionApi)
        val spiedIdentityExtension = Mockito.spy(identityExtension)

        spiedIdentityExtension.handleIdentityResponseIdentityForSharedState(
            Event.Builder("event", "type", "source")
                .setEventData(
                    mapOf(
                        "updatesharedstate" to true
                    )
                ).build()
        )
        verify(spiedIdentityExtension, times(1)).packageEventData()
        verify(mockedExtensionApi, times(1)).createSharedState(any(), any())
    }

    @Test(timeout = 10000)
    fun test_testResetIdentities() {
        val configuration = mapOf(
            "experienceCloud.org" to "orgid",
            "experienceCloud.server" to "test.com",
            "global.privacy" to "optedin"
        )
        val identityExtension = initializeIdentityExtensionWithPreset(
            FRESH_INSTALL_WITHOUT_CACHE,
            configuration
        )

        val oldMid = identityExtension.mid
        val countDownLatch = CountDownLatch(2)
        `when`(mockedHitQueue.queue(any())).thenAnswer { invocation ->
            val entity: DataEntity? = invocation.arguments[0] as DataEntity?
            if (entity == null) {
                fail("DataEntity is null.")
                return@thenAnswer false
            }
            val hit = IdentityHit.fromDataEntity(entity)
            if (hit != null) {
                IdentityHitsProcessing(identityExtension).processHit(entity) { countDownLatch.countDown() }
            }
            return@thenAnswer true
        }
        networkMonitor = { url ->
            if (url.contains("https://test.com/id?")) {
                assertTrue(url.contains("d_mid="))
                assertTrue(url.contains("d_orgid=orgid"))
                assertTrue(url.contains("d_ver=2"))
                assertTrue(oldMid.isNotEmpty())
                assertFalse(url.contains(oldMid))
                countDownLatch.countDown()
            }
        }

        identityExtension.handleIdentityRequestReset(
            Event.Builder(
                "event",
                "com.adobe.eventType.generic.identity",
                "com.adobe.eventSource.requestReset"
            ).build()
        )

        countDownLatch.await()
    }
}
