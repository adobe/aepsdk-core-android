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

import com.adobe.marketing.mobile.*
import com.adobe.marketing.mobile.services.*
import com.adobe.marketing.mobile.util.DataReader
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.io.*
import java.net.URL
import java.util.*
import java.util.concurrent.CountDownLatch


@RunWith(MockitoJUnitRunner.Silent::class)
class IdentityExtensionTests {
    @Mock
    private lateinit var mockedExtensionApi: ExtensionApi

    @Mock
    private lateinit var mockedNamedCollection: NamedCollection

    @Mock
    private lateinit var mockedHitQueue: HitQueuing

    @Mock
    private lateinit var mockedConnection: HttpConnecting

    @Before
    fun setup() {
        Mockito.reset(mockedExtensionApi)
        Mockito.reset(mockedNamedCollection)
        Mockito.reset(mockedHitQueue)
        Mockito.reset(mockedConnection)
    }

    private fun initializeSpiedIdentityExtension(): IdentityExtension {
        val identityExtension =
            IdentityExtension(mockedExtensionApi, mockedNamedCollection, mockedHitQueue)
        return Mockito.spy(identityExtension)
    }

    @Test
    fun `get extension name`() {
        val identityExtension = initializeSpiedIdentityExtension()
        assertEquals("com.adobe.module.identity", identityExtension.name)
    }

    @Test
    fun `get extension friendlyName`() {
        val identityExtension = initializeSpiedIdentityExtension()
        assertEquals("Identity", identityExtension.friendlyName)
    }

    @Test
    fun `get extension version`() {
        val identityExtension = initializeSpiedIdentityExtension()
        assertEquals("2.0.0", identityExtension.version)
    }

    @Test
    fun `onUnregistered() should close HitQueue`() {
        val identityExtension = initializeSpiedIdentityExtension()
        identityExtension.onUnregistered()
        verify(mockedHitQueue, times(1)).close()
    }

    @Test
    @Ignore
    fun `onRegistered() should load cached data and create a shared state`() {
        val identityExtension = initializeSpiedIdentityExtension()
        identityExtension.onRegistered()
    }

    @Test
    fun `readyForEvent() should return false if no valid configuration shared state`() {
        val identityExtension = initializeSpiedIdentityExtension()
        identityExtension.onRegistered()

        Mockito.`when`(
            mockedExtensionApi.getSharedState(any(), any(), anyOrNull(), any())
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                return@thenAnswer SharedStateResult(SharedStateStatus.SET, emptyMap())
            }
            return@thenAnswer null
        }
        assertFalse(
            identityExtension.readyForEvent(
                Event.Builder("event", "type", "source").build()
            )
        )
    }

    @Test
    fun `readyForEvent() should return false if configuration is not registered`() {
        val identityExtension = initializeSpiedIdentityExtension()
        identityExtension.onRegistered()

        Mockito.`when`(
            mockedExtensionApi.getSharedState(any(), any(), anyOrNull(), any())
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                return@thenAnswer null
            }
            return@thenAnswer null
        }
        assertFalse(
            identityExtension.readyForEvent(
                Event.Builder("event", "type", "source").build()
            )
        )
    }

    @Test(timeout = 10000)
    fun `readyForEvent() should return false for appendUrl and urlVars events if Analytics extension is not registered`() {
        val identityExtension = initializeSpiedIdentityExtension()
        identityExtension.onRegistered()
        val countDownLatch = CountDownLatch(2)
        Mockito.`when`(
            mockedExtensionApi.getSharedState(any(), any(), anyOrNull(), any())
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                return@thenAnswer SharedStateResult(
                    SharedStateStatus.SET, mapOf(
                        "experienceCloud.org" to "orgid"
                    )
                )
            }
            if ("com.adobe.module.analytics" === extension) {
                countDownLatch.countDown()
                return@thenAnswer null
            }
            return@thenAnswer null
        }
        assertFalse(
            identityExtension.readyForEvent(
                Event.Builder("event", "type", "source").setEventData(
                    mapOf(
                        "baseurl" to true
                    )
                ).build()
            )
        )
        assertFalse(
            identityExtension.readyForEvent(
                Event.Builder("event", "type", "source").setEventData(
                    mapOf(
                        "urlvariables" to true
                    )
                ).build()
            )
        )
        countDownLatch.await()
    }

    @Test
    fun `readyForEvent() should trigger one forceSync event `() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        Mockito.`when`(
            mockedExtensionApi.getSharedState(any(), any(), anyOrNull(), any())
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                return@thenAnswer SharedStateResult(
                    SharedStateStatus.SET, mapOf(
                        "experienceCloud.org" to "orgid"
                    )
                )
            }
            return@thenAnswer null
        }
        spiedIdentityExtension.readyForEvent(Event.Builder("event", "type", "source").build())

        spiedIdentityExtension.readyForEvent(Event.Builder("event", "type", "source").build())
        verify(spiedIdentityExtension, times(1)).forceSyncIdentifiers(any())
    }

    @Test
    fun `forceSyncIdentifiers() should still create a shared state if OPTED_OUT`() {
        val identityExtension = initializeSpiedIdentityExtension()
        identityExtension.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT)
        identityExtension.forceSyncIdentifiers(Event.Builder("event", "type", "source").build())
        verify(mockedExtensionApi, times(1)).createSharedState(any(), anyOrNull())
    }

    @Test
    fun `readyForSyncIdentifiers() for sync event - happy `() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()

        spiedIdentityExtension.onRegistered()

        Mockito.`when`(
            mockedExtensionApi.getSharedState(any(), any(), anyOrNull(), any())
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                return@thenAnswer SharedStateResult(
                    SharedStateStatus.SET, mapOf(
                        "experienceCloud.org" to "orgid"
                    )
                )
            }
            return@thenAnswer null
        }
        assertTrue(
            spiedIdentityExtension.readyForEvent(
                Event.Builder("event", "type", "source").setEventData(mapOf("issyncevent" to true))
                    .build()
            )
        )
        verify(spiedIdentityExtension, times(1)).readyForSyncIdentifiers(any())
    }

    @Test
    fun `readyForSyncIdentifiers() for sync event - no valid orgId - return false`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()

        spiedIdentityExtension.onRegistered()

        Mockito.`when`(
            mockedExtensionApi.getSharedState(any(), any(), anyOrNull(), any())
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                return@thenAnswer SharedStateResult(
                    SharedStateStatus.SET, mapOf(
                        "invalid key - experienceCloud.org" to "orgid"
                    )
                )
            }
            return@thenAnswer null
        }
        assertFalse(
            spiedIdentityExtension.readyForEvent(
                Event.Builder("event", "type", "source").setEventData(mapOf("issyncevent" to true))
                    .build()
            )
        )
        verify(spiedIdentityExtension, times(2)).isSyncEvent(any())
    }

    @Test
    fun `readyForSyncIdentifiers() - appendUrlEvent - happy`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()

        spiedIdentityExtension.onRegistered()

        Mockito.`when`(
            mockedExtensionApi.getSharedState(any(), any(), anyOrNull(), any())
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                return@thenAnswer SharedStateResult(
                    SharedStateStatus.SET, mapOf(
                        "invalid key - experienceCloud.org" to "orgid"
                    )
                )
            }
            if ("com.adobe.module.analytics" === extension) {
                return@thenAnswer SharedStateResult(
                    SharedStateStatus.SET, mapOf(
                        "vid" to "fake_vid"
                    )
                )
            }
            return@thenAnswer null
        }
        assertTrue(
            spiedIdentityExtension.readyForEvent(
                Event.Builder("event", "type", "source").setEventData(mapOf("baseurl" to "url"))
                    .build()
            )
        )
        verify(spiedIdentityExtension, times(1)).isAppendUrlEvent(any())
    }

    @Test
    fun `readyForSyncIdentifiers() - appendUrlEvent - invalid analytics`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()

        spiedIdentityExtension.onRegistered()

        Mockito.`when`(
            mockedExtensionApi.getSharedState(any(), any(), anyOrNull(), any())
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                return@thenAnswer SharedStateResult(
                    SharedStateStatus.SET, mapOf(
                        "invalid key - experienceCloud.org" to "orgid"
                    )
                )
            }
            return@thenAnswer null
        }
        assertFalse(
            spiedIdentityExtension.readyForEvent(
                Event.Builder("event", "type", "source").setEventData(mapOf("baseurl" to "url"))
                    .build()
            )
        )
        verify(spiedIdentityExtension, times(1)).isAppendUrlEvent(any())
    }

    @Test
    fun `readyForSyncIdentifiers() - getUrlVarsEvent - happy`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()

        spiedIdentityExtension.onRegistered()

        Mockito.`when`(
            mockedExtensionApi.getSharedState(any(), any(), anyOrNull(), any())
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                return@thenAnswer SharedStateResult(
                    SharedStateStatus.SET, mapOf(
                        "invalid key - experienceCloud.org" to "orgid"
                    )
                )
            }
            if ("com.adobe.module.analytics" === extension) {
                return@thenAnswer SharedStateResult(
                    SharedStateStatus.SET, mapOf(
                        "vid" to "fake_vid"
                    )
                )
            }
            return@thenAnswer null
        }
        assertTrue(
            spiedIdentityExtension.readyForEvent(
                Event.Builder("event", "type", "source").setEventData(mapOf("urlvariables" to true))
                    .build()
            )
        )
        verify(spiedIdentityExtension, times(1)).isGetUrlVarsEvent(any())
    }

    @Test
    fun `readyForSyncIdentifiers() - getUrlVarsEvent - invalid analytics`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()

        spiedIdentityExtension.onRegistered()

        Mockito.`when`(
            mockedExtensionApi.getSharedState(any(), any(), anyOrNull(), any())
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                return@thenAnswer SharedStateResult(
                    SharedStateStatus.SET, mapOf(
                        "invalid key - experienceCloud.org" to "orgid"
                    )
                )
            }
            return@thenAnswer null
        }
        assertFalse(
            spiedIdentityExtension.readyForEvent(
                Event.Builder("event", "type", "source").setEventData(mapOf("urlvariables" to true))
                    .build()
            )
        )
        verify(spiedIdentityExtension, times(1)).isGetUrlVarsEvent(any())
    }

    @Test
    fun `handleConfiguration() - configuration is null`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()

        spiedIdentityExtension.handleConfiguration(null)

        verify(spiedIdentityExtension, never()).processPrivacyChange(any(), any())
        verify(spiedIdentityExtension, never()).updateLatestValidConfiguration(any())
    }

    @Test
    fun `handleConfiguration() - experienceCloud_server not exists - return default value`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()

        spiedIdentityExtension.handleConfiguration(
            Event.Builder("event", "type", "source").setEventData(
                mapOf(
                    "experienceCloud.org" to "orgId",
                    "global.privacy" to "optedin"
                )
            ).build()
        )
        assertEquals(
            "dpm.demdex.net",
            spiedIdentityExtension.latestValidConfig.marketingCloudServer
        )
    }

    @Test
    fun `loadVariablesFromPersistentData() - shouldn't crash if NamedCollection is null`() {
        val identityExtension =
            IdentityExtension(mockedExtensionApi, null, mockedHitQueue)
        identityExtension.loadVariablesFromPersistentData()
    }

    @Test
    fun `handleConfiguration() - configuration's event data is null`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()

        spiedIdentityExtension.handleConfiguration(Event.Builder("event", "type", "source").build())

        verify(spiedIdentityExtension, never()).processPrivacyChange(any(), any())
        verify(spiedIdentityExtension, never()).updateLatestValidConfiguration(any())
    }

    @Test
    fun `handleIdentityRequestReset() - event is null`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.handleIdentityRequestReset(null)

        verify(spiedIdentityExtension, never()).processIdentityRequest(any())
    }

    @Test
    fun `handleIdentityRequestReset() - OptedOut`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()

        spiedIdentityExtension.handleConfiguration(
            Event.Builder("event", "type", "source").setEventData(
                mapOf(
                    "global.privacy" to "optedout"
                )
            ).build()
        )
        spiedIdentityExtension.handleIdentityRequestReset(
            Event.Builder("event", "type", "source").build()
        )

        verify(spiedIdentityExtension, never()).processIdentityRequest(any())
    }

    // ==============================================================================================================
    // 	void handleAnalyticsResponseIdentity()
    // ==============================================================================================================


    @Test
    fun `handleAnalyticsResponseIdentity() - event is null`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.handleAnalyticsResponseIdentity(null)

        verify(spiedIdentityExtension, never()).processIdentityRequest(any())
    }

    @Test
    fun `handleAnalyticsResponseIdentity() - NamedCollection is null`() {
        val identityExtension =
            IdentityExtension(mockedExtensionApi, null, mockedHitQueue)
        val spiedIdentityExtension = Mockito.spy(identityExtension)
        spiedIdentityExtension.handleAnalyticsResponseIdentity(null)

        verify(spiedIdentityExtension, never()).processIdentityRequest(any())
    }

    @Test
    fun `handleAnalyticsResponseIdentity() - eventData is null`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.handleAnalyticsResponseIdentity(
            Event.Builder(
                "event",
                "type",
                "source"
            ).build()
        )

        verify(spiedIdentityExtension, never()).processIdentityRequest(any())
    }

    @Test
    fun `handleAnalyticsResponseIdentity() - namedCollection is null`() {
        val identityExtension =
            IdentityExtension(mockedExtensionApi, null, mockedHitQueue)
        val spiedIdentityExtension = Mockito.spy(identityExtension)
        spiedIdentityExtension.handleAnalyticsResponseIdentity(
            Event.Builder(
                "event",
                "type",
                "source"
            ).setEventData(
                mapOf(
                    "key" to "value"
                )
            ).build()
        )

        verify(spiedIdentityExtension, never()).processIdentityRequest(any())
    }

    @Test
    fun `handleAnalyticsResponseIdentity() - no aid`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.handleAnalyticsResponseIdentity(
            Event.Builder(
                "event",
                "type",
                "source"
            ).setEventData(
                mapOf(
                    "invalid_aid" to "iddddd"
                )
            ).build()
        )

        verify(spiedIdentityExtension, never()).processIdentityRequest(any())
    }

    @Test
    fun `handleAnalyticsResponseIdentity() - aid is synced`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        Mockito.`when`(mockedNamedCollection.contains(any())).thenAnswer { invocation ->
            val key = invocation.arguments[0] as? String
            when (key) {
                "ADOBEMOBILE_AID_SYNCED" -> {
                    return@thenAnswer true
                }
            }
            return@thenAnswer false
        }

        spiedIdentityExtension.handleAnalyticsResponseIdentity(
            Event.Builder(
                "event",
                "type",
                "source"
            ).setEventData(
                mapOf(
                    "aid" to "iddddd"
                )
            ).build()
        )

        verify(spiedIdentityExtension, never()).processIdentityRequest(any())
    }

    @Test
    fun `handleAnalyticsResponseIdentity() - happy`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        Mockito.`when`(mockedNamedCollection.contains(any())).thenAnswer { invocation ->
            val key = invocation.arguments[0] as? String
            when (key) {
                "ADOBEMOBILE_AID_SYNCED" -> {
                    return@thenAnswer false
                }
            }
            return@thenAnswer true
        }

        val countDownLatch = CountDownLatch(1)

        doAnswer { invocation ->
            val key = invocation.arguments[0] as? String
            if ("ADOBEMOBILE_AID_SYNCED" === key) {
                countDownLatch.countDown()
            }
        }.`when`(mockedNamedCollection).setBoolean(any(), any())

        spiedIdentityExtension.handleAnalyticsResponseIdentity(
            Event.Builder(
                "event",
                "type",
                "source"
            ).setEventData(
                mapOf(
                    "aid" to "iddddd"
                )
            ).build()
        )
        countDownLatch.await()
        val eventCaptor = ArgumentCaptor.forClass(Event::class.java)
        verify(spiedIdentityExtension, times(1)).processIdentityRequest(eventCaptor.capture())
        val event = eventCaptor.value
        assertNotNull(event.eventData)
        assertTrue(event.eventData.contains("forcesync"))
        assertTrue(event.eventData.contains("visitoridentifiers"))
        assertTrue(event.eventData.contains("authenticationstate"))
        assertTrue(event.eventData.contains("issyncevent"))
    }


    @Test
    fun `processAudienceResponse() - event is null`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        var retrievedConfiguration = false
        Mockito.`when`(
            mockedExtensionApi.getSharedState(any(), any(), anyOrNull(), any())
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                retrievedConfiguration = true
                return@thenAnswer null
            }
            return@thenAnswer null
        }
        spiedIdentityExtension.processAudienceResponse(null)
        assertFalse(retrievedConfiguration)
    }

    @Test
    fun `processAudienceResponse() - eventData is null`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        var retrievedConfiguration = false
        Mockito.`when`(
            mockedExtensionApi.getSharedState(any(), any(), anyOrNull(), any())
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                retrievedConfiguration = true
                return@thenAnswer null
            }
            return@thenAnswer null
        }
        spiedIdentityExtension.processAudienceResponse(
            Event.Builder(
                "event",
                "type",
                "source"
            ).build()
        )
        assertFalse(retrievedConfiguration)
    }

    @Test
    fun `processAudienceResponse() - invalid eventData`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        var retrievedConfiguration = false
        Mockito.`when`(
            mockedExtensionApi.getSharedState(any(), any(), anyOrNull(), any())
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                retrievedConfiguration = true
                return@thenAnswer null
            }
            return@thenAnswer null
        }
        spiedIdentityExtension.processAudienceResponse(
            Event.Builder(
                "event",
                "type",
                "source"
            ).setEventData(
                mapOf(
                    "invalid_optedouthitsent" to "x"
                )
            ).build()
        )
        assertFalse(retrievedConfiguration)
    }

    @Test
    fun `processAudienceResponse() - optedouthitsent is true`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        var retrievedConfiguration = false
        Mockito.`when`(
            mockedExtensionApi.getSharedState(any(), any(), anyOrNull(), any())
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                retrievedConfiguration = true
                return@thenAnswer null
            }
            return@thenAnswer null
        }
        spiedIdentityExtension.processAudienceResponse(
            Event.Builder(
                "event",
                "type",
                "source"
            ).setEventData(
                mapOf(
                    "optedouthitsent" to true
                )
            ).build()
        )
        assertFalse(retrievedConfiguration)
    }

    @Test
    fun `processAudienceResponse() - happy`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        Mockito.`when`(
            mockedExtensionApi.getSharedState(any(), any(), anyOrNull(), any())
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            if ("com.adobe.module.configuration" === extension) {
                return@thenAnswer SharedStateResult(
                    SharedStateStatus.SET, mapOf(
                        "global.privacy" to "optedout"
                    )
                )
            }
            return@thenAnswer null
        }
        spiedIdentityExtension.processAudienceResponse(
            Event.Builder(
                "event",
                "type",
                "source"
            ).setEventData(
                mapOf(
                    "optedouthitsent" to false
                )
            ).build()
        )
        verify(spiedIdentityExtension, times(1)).sendOptOutHit(any())
    }

    @Test(timeout = 10000)
    fun `sendOptOutHit() - happy (200)`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        val state = ConfigurationSharedStateIdentity()
        spiedIdentityExtension.mid = "123455"
        Mockito.`when`(mockedConnection.responseCode).thenReturn(200)
        val countDownLatch = CountDownLatch(1)
        ServiceProvider.getInstance().networkService = Networking { request, callback ->
            assertEquals(2, request.readTimeout)
            assertEquals(2, request.connectTimeout)
            callback.call(mockedConnection)
            countDownLatch.countDown()
        }
        state.getConfigurationProperties(
            mapOf(
                "experienceCloud.org" to "orgId"
            )
        )
        spiedIdentityExtension.sendOptOutHit(state)
        countDownLatch.await()
        verify(mockedConnection, times(1)).close()
    }

    @Test(timeout = 10000)
    fun `sendOptOutHit() - bad request (400)`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        val state = ConfigurationSharedStateIdentity()
        spiedIdentityExtension.mid = "123455"
        Mockito.`when`(mockedConnection.responseCode).thenReturn(400)
        val countDownLatch = CountDownLatch(1)
        ServiceProvider.getInstance().networkService = Networking { request, callback ->
            assertEquals(2, request.readTimeout)
            assertEquals(2, request.connectTimeout)
            callback.call(mockedConnection)
            countDownLatch.countDown()
        }
        state.getConfigurationProperties(
            mapOf(
                "experienceCloud.org" to "orgId"
            )
        )
        spiedIdentityExtension.sendOptOutHit(state)
        countDownLatch.await()
        verify(mockedConnection, times(1)).close()
    }

    @Test(timeout = 10000)
    fun `sendOptOutHit() - null connection`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        val state = ConfigurationSharedStateIdentity()
        spiedIdentityExtension.mid = "123455"
        Mockito.`when`(mockedConnection.responseCode).thenReturn(200)
        val countDownLatch = CountDownLatch(1)
        ServiceProvider.getInstance().networkService = Networking { request, callback ->
            assertEquals(2, request.readTimeout)
            assertEquals(2, request.connectTimeout)
            callback.call(null)
            countDownLatch.countDown()
        }
        state.getConfigurationProperties(
            mapOf(
                "experienceCloud.org" to "orgId"
            )
        )
        spiedIdentityExtension.sendOptOutHit(state)
        countDownLatch.await()
        verify(mockedConnection, never()).close()
    }

    @Test
    fun `handleSyncIdentifiers() - null configuration`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        assertTrue(
            spiedIdentityExtension.handleSyncIdentifiers(
                Event.Builder("event", "type", "source").build(), null
            )
        )
        verify(spiedIdentityExtension, never()).extractIdentifiers(any())
    }

    @Test
    fun `handleSyncIdentifiers() - cached state is OPTED_OUT`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT)
        val state = ConfigurationSharedStateIdentity()
        assertTrue(
            spiedIdentityExtension.handleSyncIdentifiers(
                Event.Builder("event", "type", "source").build(), state
            )
        )

        verify(spiedIdentityExtension, never()).extractIdentifiers(any())
    }

    @Test
    fun `handleSyncIdentifiers() - latest state is OPTED_OUT`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        val state = ConfigurationSharedStateIdentity()
        state.getConfigurationProperties(
            mapOf(
                "experienceCloud.org" to "orgid",
                "global.privacy" to "optedout"

            )
        )
        assertTrue(
            spiedIdentityExtension.handleSyncIdentifiers(
                Event.Builder("event", "type", "source").build(), state
            )
        )

        verify(spiedIdentityExtension, never()).extractIdentifiers(any())
    }

    @Test
    fun `handleSyncIdentifiers() - null event`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        val state = ConfigurationSharedStateIdentity()
        state.getConfigurationProperties(
            mapOf(
                "experienceCloud.org" to "orgid",
                "global.privacy" to "optedout"

            )
        )
        assertTrue(spiedIdentityExtension.handleSyncIdentifiers(null, state))

        verify(spiedIdentityExtension, never()).extractIdentifiers(any())
    }

    // ==============================================================================================================
    // 	void networkResponseLoaded(final HashMap<String, String> result, final String pairID, final int stateVersion)
    // ==============================================================================================================

    @Test
    fun testNetworkResponseLoaded_Happy() {
        val responseObject = IdentityResponseObject()
        responseObject.blob = "blobvalue"
        responseObject.mid = "1234567890"
        responseObject.ttl = 222222
        responseObject.hint = "region"

        val countDownLatch = CountDownLatch(1)
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.lastSync = 0
        spiedIdentityExtension.mid = responseObject.mid
        doAnswer { invocation ->
            val event = invocation.arguments[0] as? Event
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertEquals(spiedIdentityExtension.lastSync, eventData?.get("lastsync"))
            assertEquals(true, eventData?.get("updatesharedstate"))
            assertEquals("blobvalue", eventData?.get("blob"))
            assertEquals("region", eventData?.get("locationhint"))
            assertEquals(spiedIdentityExtension.mid, eventData?.get("mid"))
            countDownLatch.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        spiedIdentityExtension.networkResponseLoaded(
            responseObject,
            Event.Builder(
                "event",
                "type",
                "source"
            ).build()
        )
        countDownLatch.await()
        assertTrue(spiedIdentityExtension.lastSync > 0)
        verify(spiedIdentityExtension, times(1)).savePersistently()
    }

    @Test
    fun testNetworkResponseLoaded_WithError() {
        // setup
        val responseObject = IdentityResponseObject()
        responseObject.blob = "blobvalue"
        responseObject.mid = "1234567890"
        responseObject.ttl = 222222
        responseObject.hint = "region"
        responseObject.error = "this is an error message"

        val countDownLatch = CountDownLatch(1)
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.lastSync = 0
        spiedIdentityExtension.mid = responseObject.mid
        doAnswer { invocation ->
            val event = invocation.arguments[0] as? Event
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertEquals(spiedIdentityExtension.lastSync, eventData?.get("lastsync"))
            assertEquals(spiedIdentityExtension.mid, eventData?.get("mid"))
            countDownLatch.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        spiedIdentityExtension.networkResponseLoaded(
            responseObject,
            Event.Builder(
                "event",
                "type",
                "source"
            ).build()
        )
        countDownLatch.await()
        assertTrue(spiedIdentityExtension.lastSync > 0)
        verify(spiedIdentityExtension, times(1)).savePersistently()
    }

    @Test
    fun testNetworkResponseLoaded_WhenOptOut() {
        // setup
        val responseObject = IdentityResponseObject()
        responseObject.blob = "blobvalue"
        responseObject.mid = "1234567890"
        responseObject.ttl = 222222
        responseObject.hint = "region"

        val countDownLatch = CountDownLatch(1)
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.lastSync = 0
        spiedIdentityExtension.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT)
        doAnswer { invocation ->
            val event = invocation.arguments[0] as? Event
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertEquals(spiedIdentityExtension.lastSync, eventData?.get("lastsync"))
            assertFalse(eventData?.contains("mid") == true)
            countDownLatch.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        // test
        spiedIdentityExtension.networkResponseLoaded(
            responseObject,
            Event.Builder(
                "event",
                "type",
                "source"
            ).build()
        )

        countDownLatch.await()
        assertTrue(spiedIdentityExtension.lastSync > 0)
        verify(spiedIdentityExtension, never()).savePersistently()
    }

    @Test
    fun testNetworkResponseLoaded_WhenChangedBlob_setUpdateSharedStateTrue() {

        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.mid = "1234567890"
        spiedIdentityExtension.lastSync = 0
        spiedIdentityExtension.setBlob("beforeBlob")
        spiedIdentityExtension.setLocationHint("9")
        spiedIdentityExtension.setPrivacyStatus(MobilePrivacyStatus.OPT_IN)


        val responseObject = IdentityResponseObject()
        responseObject.blob = "afterBlob"
        responseObject.hint = "9"
        responseObject.mid = "1234567890"

        val countDownLatch = CountDownLatch(1)
        doAnswer { invocation ->
            val event = invocation.arguments[0] as? Event
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertEquals("afterBlob", eventData?.get("blob"))
            assertEquals("9", eventData?.get("locationhint"))
            assertTrue(eventData?.get("updatesharedstate") == true)
            countDownLatch.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        // test
        spiedIdentityExtension.networkResponseLoaded(
            responseObject,
            Event.Builder(
                "event",
                "type",
                "source"
            ).build()
        )

        countDownLatch.await()
        assertTrue(spiedIdentityExtension.lastSync > 0)
        verify(spiedIdentityExtension, times(1)).savePersistently()
    }

    @Test
    fun testNetworkResponseLoaded_WhenChangedLocationHint_setUpdateSharedStateTrue() {
        val responseObject = IdentityResponseObject()
        responseObject.blob = "blob"
        responseObject.hint = "9"
        responseObject.mid = "1234567890"

        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.mid = "1234567890"
        spiedIdentityExtension.setBlob("blob")
        spiedIdentityExtension.setLocationHint("5")
        spiedIdentityExtension.setPrivacyStatus(MobilePrivacyStatus.OPT_IN)

        val countDownLatch = CountDownLatch(1)
        doAnswer { invocation ->
            val event = invocation.arguments[0] as? Event
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertEquals("blob", eventData?.get("blob"))
            assertEquals("9", eventData?.get("locationhint"))
            assertTrue(eventData?.get("updatesharedstate") == true)
            countDownLatch.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        // test
        spiedIdentityExtension.networkResponseLoaded(
            responseObject,
            Event.Builder(
                "event",
                "type",
                "source"
            ).build()
        )

        countDownLatch.await()
        assertTrue(spiedIdentityExtension.lastSync > 0)
        verify(spiedIdentityExtension, times(1)).savePersistently()
    }

    @Test
    fun testNetworkResponseLoaded_WhenChangedLocationHintFromNull_setUpdateSharedStateTrue() {

        val responseObject = IdentityResponseObject()
        responseObject.blob = "blob"
        responseObject.hint = "9"
        responseObject.mid = "1234567890"

        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.mid = "1234567890"
        spiedIdentityExtension.setBlob("blob")
        spiedIdentityExtension.setLocationHint(null)
        spiedIdentityExtension.setPrivacyStatus(MobilePrivacyStatus.OPT_IN)

        val countDownLatch = CountDownLatch(1)
        doAnswer { invocation ->
            val event = invocation.arguments[0] as? Event
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertEquals("blob", eventData?.get("blob"))
            assertEquals("9", eventData?.get("locationhint"))
            assertTrue(eventData?.get("updatesharedstate") == true)
            countDownLatch.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        // test
        spiedIdentityExtension.networkResponseLoaded(
            responseObject,
            Event.Builder(
                "event",
                "type",
                "source"
            ).build()
        )

        countDownLatch.await()
        assertTrue(spiedIdentityExtension.lastSync > 0)
        verify(spiedIdentityExtension, times(1)).savePersistently()
    }

    @Test
    fun testNetworkResponseLoaded_WhenChangedBlobFromNull_setUpdateSharedStateTrue() {

        val responseObject = IdentityResponseObject()
        responseObject.blob = "afterBlob"
        responseObject.hint = "9"
        responseObject.mid = "1234567890"

        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.mid = "1234567890"
        spiedIdentityExtension.setBlob(null)
        spiedIdentityExtension.setLocationHint("9")
        spiedIdentityExtension.setPrivacyStatus(MobilePrivacyStatus.OPT_IN)

        val countDownLatch = CountDownLatch(1)
        doAnswer { invocation ->
            val event = invocation.arguments[0] as? Event
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertEquals("afterBlob", eventData?.get("blob"))
            assertEquals("9", eventData?.get("locationhint"))
            assertTrue(eventData?.get("updatesharedstate") == true)
            countDownLatch.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        // test
        spiedIdentityExtension.networkResponseLoaded(
            responseObject,
            Event.Builder(
                "event",
                "type",
                "source"
            ).build()
        )

        countDownLatch.await()
        assertTrue(spiedIdentityExtension.lastSync > 0)
        verify(spiedIdentityExtension, times(1)).savePersistently()
    }

    @Test
    fun testNetworkResponseLoaded_WhenChangedLocationHintToNull_setUpdateSharedStateTrue() {

        val responseObject = IdentityResponseObject()
        responseObject.blob = "blob"
        responseObject.hint = null
        responseObject.mid = "1234567890"

        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.mid = "1234567890"
        spiedIdentityExtension.setBlob("blob")
        spiedIdentityExtension.setLocationHint("9")
        spiedIdentityExtension.setPrivacyStatus(MobilePrivacyStatus.OPT_IN)

        val countDownLatch = CountDownLatch(1)
        doAnswer { invocation ->
            val event = invocation.arguments[0] as? Event
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertEquals("blob", eventData?.get("blob"))
            assertEquals(null, eventData?.get("locationhint"))
            assertTrue(eventData?.get("updatesharedstate") == true)
            countDownLatch.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        // test
        spiedIdentityExtension.networkResponseLoaded(
            responseObject,
            Event.Builder(
                "event",
                "type",
                "source"
            ).build()
        )

        countDownLatch.await()
        assertTrue(spiedIdentityExtension.lastSync > 0)
        verify(spiedIdentityExtension, times(1)).savePersistently()
    }

    @Test
    fun testNetworkResponseLoaded_WhenChangedBlobToNull_setUpdateSharedStateTrue() {

        val responseObject = IdentityResponseObject()
        responseObject.blob = null
        responseObject.hint = "9"
        responseObject.mid = "1234567890"

        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.mid = "1234567890"
        spiedIdentityExtension.setBlob("blob")
        spiedIdentityExtension.setLocationHint("9")
        spiedIdentityExtension.setPrivacyStatus(MobilePrivacyStatus.OPT_IN)

        val countDownLatch = CountDownLatch(1)
        doAnswer { invocation ->
            val event = invocation.arguments[0] as? Event
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertEquals(null, eventData?.get("blob"))
            assertEquals("9", eventData?.get("locationhint"))
            assertTrue(eventData?.get("updatesharedstate") == true)
            countDownLatch.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        // test
        spiedIdentityExtension.networkResponseLoaded(
            responseObject,
            Event.Builder(
                "event",
                "type",
                "source"
            ).build()
        )

        countDownLatch.await()
        assertTrue(spiedIdentityExtension.lastSync > 0)
        verify(spiedIdentityExtension, times(1)).savePersistently()
    }

    @Test
    fun testNetworkResponseLoaded_WhenErrorAndGeneratedNewMID_setUpdateSharedStateTrue() {
        val responseObject = IdentityResponseObject()
        responseObject.error = "some error"

        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.mid = null
        spiedIdentityExtension.setBlob("blob")
        spiedIdentityExtension.setLocationHint("9")
        spiedIdentityExtension.setPrivacyStatus(MobilePrivacyStatus.OPT_IN)

        val countDownLatch = CountDownLatch(1)
        doAnswer { invocation ->
            val event = invocation.arguments[0] as? Event
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertEquals("blob", eventData?.get("blob"))
            assertEquals("9", eventData?.get("locationhint"))
            assertTrue(eventData?.get("updatesharedstate") == true)
            assertNotNull(eventData?.get("mid"))
            countDownLatch.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        // test
        spiedIdentityExtension.networkResponseLoaded(
            responseObject,
            Event.Builder(
                "event",
                "type",
                "source"
            ).build()
        )

        countDownLatch.await()
        assertTrue(spiedIdentityExtension.lastSync > 0)
        verify(spiedIdentityExtension, times(1)).savePersistently()
    }

    @Test
    fun testNetworkResponseLoaded_WhenErrorAndNotGeneratedNewMID_doesNotSetUpdateSharedState() {
        val responseObject = IdentityResponseObject()
        responseObject.error = "some error"

        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.mid = "123456"
        spiedIdentityExtension.setBlob("blob")
        spiedIdentityExtension.setLocationHint("9")
        spiedIdentityExtension.setPrivacyStatus(MobilePrivacyStatus.OPT_IN)

        val countDownLatch = CountDownLatch(1)
        doAnswer { invocation ->
            val event = invocation.arguments[0] as? Event
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertEquals("blob", eventData?.get("blob"))
            assertEquals("9", eventData?.get("locationhint"))
            assertFalse(eventData?.contains("updatesharedstate") == true)
            assertEquals("123456", eventData?.get("mid"))
            countDownLatch.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        // test
        spiedIdentityExtension.networkResponseLoaded(
            responseObject,
            Event.Builder(
                "event",
                "type",
                "source"
            ).build()
        )

        countDownLatch.await()
        assertTrue(spiedIdentityExtension.lastSync > 0)
        verify(spiedIdentityExtension, times(1)).savePersistently()
    }

    @Test
    fun testNetworkResponseLoaded_WhenUnchangedBlobLocationHint_doesNotSetUpdateSharedState() {
        val responseObject = IdentityResponseObject()
        responseObject.blob = "blob"
        responseObject.hint = "9"
        responseObject.mid = "123456"

        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.mid = "123456"
        spiedIdentityExtension.setBlob("blob")
        spiedIdentityExtension.setLocationHint("9")
        spiedIdentityExtension.setPrivacyStatus(MobilePrivacyStatus.OPT_IN)

        val countDownLatch = CountDownLatch(1)
        doAnswer { invocation ->
            val event = invocation.arguments[0] as? Event
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertEquals("blob", eventData?.get("blob"))
            assertEquals("9", eventData?.get("locationhint"))
            assertFalse(eventData?.contains("updatesharedstate") == true)
            assertEquals("123456", eventData?.get("mid"))
            countDownLatch.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        // test
        spiedIdentityExtension.networkResponseLoaded(
            responseObject,
            Event.Builder(
                "event",
                "type",
                "source"
            ).build()
        )

        countDownLatch.await()
        assertTrue(spiedIdentityExtension.lastSync > 0)
        verify(spiedIdentityExtension, times(1)).savePersistently()
    }

    //    // This situation can usually happen if a network response is handled at the same time as the resetIdentities request.
    @Test
    fun testNetworkResponseLoaded_MismatchECID() {
        // setup
        val responseObject = IdentityResponseObject()
        responseObject.blob = "blobvalue"
        responseObject.mid = "1234567890"
        responseObject.ttl = 222222
        responseObject.hint = "region"

        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        spiedIdentityExtension.lastSync = 0
        spiedIdentityExtension.mid = "123"

        val countDownLatch = CountDownLatch(1)
        doAnswer { invocation ->
            val event = invocation.arguments[0] as? Event
            val eventData = event?.eventData
            assertNotNull(eventData)
            assertFalse(eventData?.contains("updatesharedstate") == true)
            assertEquals("123", eventData?.get("mid"))
            countDownLatch.countDown()
        }.`when`(mockedExtensionApi).dispatch(any())
        // test
        spiedIdentityExtension.networkResponseLoaded(
            responseObject,
            Event.Builder(
                "event",
                "type",
                "source"
            ).build()
        )
        countDownLatch.await()
        assertTrue(spiedIdentityExtension.lastSync > 0)
        verify(spiedIdentityExtension, times(1)).savePersistently()
    }

    @Test
    fun `handleNetworkResponseMap() - IdentityResponseObject is null`() {
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        assertFalse(spiedIdentityExtension.handleNetworkResponseMap(null))
    }

    @Test
    fun `handleNetworkResponseMap() - OPT_OUT list is not null or empty`() {
        val responseObject = IdentityResponseObject()
        responseObject.optOutList = listOf("a", "b")
        val spiedIdentityExtension = initializeSpiedIdentityExtension()
        assertFalse(spiedIdentityExtension.handleNetworkResponseMap(responseObject))
        verify(spiedIdentityExtension, times(1)).handleIdentityConfigurationUpdateEvent(any())
    }

    // ==============================================================================================================
    // 	List<VisitorID> convertVisitorIdsStringToVisitorIDObjects(final String idString)
    // ==============================================================================================================
    @Test
    fun convertVisitorIdsStringToVisitorIDObjects_ConvertStringToVisitorIDsCorrectly() {
        val visitorIdString =
            "d_cid_ic=loginidhash%0197717%010&d_cid_ic=xboxlivehash%011629158955%011&d_cid_ic" +
                    "=psnidhash%011144032295%012&d_cid=pushid%01testPushId%011"
        val visitorIDList = listOf(
            VisitorID(
                "d_cid_ic", "loginidhash", "97717",
                VisitorID.AuthenticationState.UNKNOWN
            ),
            VisitorID(
                "d_cid_ic", "xboxlivehash", "1629158955",
                VisitorID.AuthenticationState.AUTHENTICATED
            ),
            VisitorID(
                "d_cid_ic", "psnidhash", "1144032295",
                VisitorID.AuthenticationState.LOGGED_OUT
            ),
            VisitorID(
                "d_cid", "pushid", "testPushId",
                VisitorID.AuthenticationState.AUTHENTICATED
            )
        )
        val visitorIds =
            IdentityExtension.convertVisitorIdsStringToVisitorIDObjects(visitorIdString)
        assertEquals(visitorIds, visitorIDList)
    }

    @Test
    fun `convertVisitorIdsStringToVisitorIDObjects() - bad string format - 1`() {
        val visitorIdString =
            "there is no equals sign & there is no equals sign "
        val visitorIds =
            IdentityExtension.convertVisitorIdsStringToVisitorIDObjects(visitorIdString)
        assertEquals(0, visitorIds.size)
    }

    @Test
    fun `convertVisitorIdsStringToVisitorIDObjects() - bad string format - 2`() {
        val visitorIdString =
            "d_cid_ic=loginidhash%0197717%010&d_cid_ic="
        val visitorIds =
            IdentityExtension.convertVisitorIdsStringToVisitorIDObjects(visitorIdString)
        assertEquals(1, visitorIds.size)
    }

    @Test
    fun `convertVisitorIdsStringToVisitorIDObjects() - bad string format - 3`() {
        val visitorIdString =
            "d_cid_ic=loginidhash%0197717&d_cid_ic="
        val visitorIds =
            IdentityExtension.convertVisitorIdsStringToVisitorIDObjects(visitorIdString)
        assertEquals(0, visitorIds.size)
    }

    @Test
    fun `convertVisitorIdsStringToVisitorIDObjects() - bad string format - 4`() {
        val visitorIdString =
            "d_cid_ic=loginidhash%0197717%01x&d_cid_ic="
        val visitorIds =
            IdentityExtension.convertVisitorIdsStringToVisitorIDObjects(visitorIdString)
        assertEquals(0, visitorIds.size)
    }

    @Test
    fun testConvertVisitorIdsStringToVisitorIDObjects_OneVisitorId_Works() {
        // setup
        val visitorIds = listOf(
            VisitorID(
                IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType", "customIdValue",
                VisitorID.AuthenticationState.AUTHENTICATED
            )
        )
        val visitorIdsString = stringFromVisitorIdList(visitorIds)

        // test
        val returnedIds =
            IdentityExtension.convertVisitorIdsStringToVisitorIDObjects(visitorIdsString)

        // verify
        assertNotNull(returnedIds)
        assertEquals(1, returnedIds.size)
        assertEquals(visitorIds, returnedIds)
    }

    @Test
    fun testConvertVisitorIdsStringToVisitorIDObjects_TwoVisitorIds_Works() {
        // setup
        val visitorIds = listOf(
            VisitorID(
                IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType", "customIdValue",
                VisitorID.AuthenticationState.AUTHENTICATED
            ),
            VisitorID(
                IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType2", "customIdValue2",
                VisitorID.AuthenticationState.UNKNOWN
            )
        )
        val visitorIdsString = stringFromVisitorIdList(visitorIds)

        // test
        val returnedIds =
            IdentityExtension.convertVisitorIdsStringToVisitorIDObjects(visitorIdsString)

        // verify
        assertNotNull(returnedIds)
        assertEquals(2, returnedIds.size)
        assertEquals(visitorIds, returnedIds)
    }

    @Test
    fun testConvertVisitorIdsStringToVisitorIDObjects_EqualsInValue_Works() {
        // setup
        val visitorIds = listOf(
            VisitorID(
                IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType",
                "customIdValue==withEquals", VisitorID.AuthenticationState.AUTHENTICATED
            )
        )
        val visitorIdsString = stringFromVisitorIdList(visitorIds)

        // test
        val returnedIds =
            IdentityExtension.convertVisitorIdsStringToVisitorIDObjects(visitorIdsString)

        // verify
        assertNotNull(returnedIds)
        assertEquals(1, returnedIds.size)
        assertEquals(visitorIds, returnedIds)
    }

    @Test
    fun testConvertVisitorIdsStringToVisitorIDObjects_TwoVisitorIdsOneInvalid_ReturnsOnlyValidOne() {
        // setup
        val visitorIds = listOf(
            VisitorID(
                IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType", "value1",
                VisitorID.AuthenticationState.AUTHENTICATED
            ),
            VisitorID(
                IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType", "",
                VisitorID.AuthenticationState.LOGGED_OUT
            )
        )

        val visitorIdsString = stringFromVisitorIdList(visitorIds)

        // test
        val returnedIds =
            IdentityExtension.convertVisitorIdsStringToVisitorIDObjects(visitorIdsString)

        // verify
        assertNotNull(returnedIds)
        assertEquals(returnedIds.size, 1)
        val visitorID = returnedIds[0]
        assertEquals(IdentityTestConstants.UrlKeys.VISITOR_ID, visitorID.idOrigin)
        assertEquals("customIdType", visitorID.idType)
        assertEquals("value1", visitorID.id)
        assertEquals(VisitorID.AuthenticationState.AUTHENTICATED, visitorID.authenticationState)
    }

    @Test
    fun testConvertVisitorIdsStringToVisitorIDObjects__removesDuplicatedIdTypes() {
        // setup
        val visitorIds = listOf(
            VisitorID(
                IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType", "value1",
                VisitorID.AuthenticationState.AUTHENTICATED
            ),
            VisitorID(
                IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType", "value2",
                VisitorID.AuthenticationState.LOGGED_OUT
            )
        )
        val visitorIdsString: String = stringFromVisitorIdList(visitorIds)

        // test
        val returnedIds =
            IdentityExtension.convertVisitorIdsStringToVisitorIDObjects(visitorIdsString)

        // verify
        assertNotNull(returnedIds)
        assertEquals(1, returnedIds.size)
        val visitorID = returnedIds[0]
        assertEquals(IdentityTestConstants.UrlKeys.VISITOR_ID, visitorID.idOrigin)
        assertEquals("customIdType", visitorID.idType)
        assertEquals("value2", visitorID.id)
        assertEquals(VisitorID.AuthenticationState.LOGGED_OUT, visitorID.authenticationState)
    }

    @Test
    @Throws(Exception::class)
    fun appendVisitorInfoForURL_Should_HandleAllURLs() {
        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(getResource("SampleURLTestSet.tab"))
        } catch (ex: Exception) {
            fail("Could not read the sample urls file!")
        }
        val buf = BufferedReader(InputStreamReader(inputStream))
        var line: String? = buf.readLine()
        val sb = java.lang.StringBuilder()
        var testComponents: Array<String?>
        var lineNumber = 0
        while (line != null) {
            lineNumber++
            if (lineNumber == 1 || line == "") {
                line = buf.readLine()
                continue
            }
            sb.append(line).append("\n")
            testComponents = line.split("\t".toRegex()).toTypedArray()
            val testURL = testComponents[0]
            val idPayload = java.lang.StringBuilder(testComponents[1])
            val expectedResult = testComponents[2]

            val spiedIdentityExtension = initializeSpiedIdentityExtension()

            val configurationSharedStateIdentity = ConfigurationSharedStateIdentity()
            configurationSharedStateIdentity.getConfigurationProperties(
                mapOf(
                    "experienceCloud.org" to "orgid"
                )
            )

            val countDownLatch = CountDownLatch(1)
            doAnswer { invocation ->
                val event = invocation.arguments[0] as? Event
                val eventData = event?.eventData
                assertNotNull(eventData)
                val returnURL: String = DataReader.optString(eventData, "updatedurl", "")
                assertEquals(expectedResult, returnURL)

                countDownLatch.countDown()
            }.`when`(mockedExtensionApi).dispatch(any())

            spiedIdentityExtension.appendVisitorInfoForURL(
                testURL,
                null,
                configurationSharedStateIdentity,
                mapOf(
                    "aid" to "test-aid",
                    "vid" to "test-vid"
                ),
                idPayload
            )
            countDownLatch.await()
            line = buf.readLine()
        }
    }

    private fun getResource(resourceName: String?): File? {
        var resourceFile: File? = null
        val resource: URL? = this.javaClass.classLoader.getResource(resourceName)
        if (resource != null) {
            resourceFile = File(resource.getFile())
        }
        return resourceFile
    }

    private fun stringFromVisitorIdList(visitorIDs: List<VisitorID>?): String {
        if (visitorIDs == null) {
            return ""
        }
        val customerIdString = StringBuilder()
        for (visitorID in visitorIDs) {
            customerIdString.append("&")
            customerIdString.append(IdentityConstants.UrlKeys.VISITOR_ID)
            customerIdString.append("=")
            customerIdString.append(visitorID.idType)
            customerIdString.append(IdentityTestConstants.Defaults.CID_DELIMITER)
            if (visitorID.id != null) {
                customerIdString.append(visitorID.id)
            }
            customerIdString.append(IdentityTestConstants.Defaults.CID_DELIMITER)
            customerIdString.append(visitorID.authenticationState.value)
        }
        return customerIdString.toString()
    }

}