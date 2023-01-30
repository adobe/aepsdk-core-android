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

package com.adobe.marketing.mobile.internal.configuration

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.SharedStateResolution
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.VisitorID
import com.adobe.marketing.mobile.internal.util.VisitorIDSerializer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
import kotlin.test.assertEquals

class MobileIdentitiesProviderTest {
    @Mock
    private lateinit var mockExtensionApi: ExtensionApi

    @Mock
    private lateinit var mockEvent: Event

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `Get SDK Identifiers when all shared states are available`() {
        setAnalyticsSharedState()
        setAudienceSharedState()
        setTargetSharedState()
        setConfigurationSharedState()
        setIdentitySharedState()

        val collectedSDKIdentities =
            MobileIdentitiesProvider.collectSdkIdentifiers(mockEvent, mockExtensionApi)
        val expectedIdentifiers = "{" +
            "\"companyContexts\":" +
            // Configuration
            "[{\"namespace\":\"imsOrgID\",\"value\":\"test_orgid\"}]," +
            "\"users\":" +
            "[{\"userIDs\":" +
            // Analytics
            "[{\"namespace\":\"AVID\",\"type\":\"integrationCode\",\"value\":\"test_aid\"}," +
            "{\"namespace\":\"vid\",\"type\":\"analytics\",\"value\":\"test_vid\"}," +
            // Audience
            "{\"namespace\":\"test_dpid\",\"type\":\"namespaceId\",\"value\":\"test_dpuuid\"}," +
            "{\"namespace\":\"0\",\"type\":\"namespaceId\",\"value\":\"test_uuid\"}," +
            // Identity
            "{\"namespace\":\"4\",\"type\":\"namespaceId\",\"value\":\"test_mid\"}," +
            "{\"namespace\":\"type1\",\"type\":\"integrationCode\",\"value\":\"id1\"}," +
            "{\"namespace\":\"type2\",\"type\":\"integrationCode\",\"value\":\"id2\"}," +
            "{\"namespace\":\"DSID_20914\",\"type\":\"integrationCode\",\"value\":\"test_advertisingId\"}," +
            "{\"namespace\":\"20919\",\"type\":\"integrationCode\",\"value\":\"test_pushId\"}," +
            // Target
            "{\"namespace\":\"tntid\",\"type\":\"target\",\"value\":\"test_tntid\"}," +
            "{\"namespace\":\"3rdpartyid\",\"type\":\"target\",\"value\":\"test_thirdpartyid\"}]}]}"

        assertEquals(expectedIdentifiers, collectedSDKIdentities)
    }

    @Test
    fun `Get SDK Identifiers when only Configuration shared state is available`() {
        setConfigurationSharedState()

        val collectedSDKIdentities =
            MobileIdentitiesProvider.collectSdkIdentifiers(mockEvent, mockExtensionApi)
        val expectedIdentifiers = "{" +
            "\"companyContexts\":" +
            // Configuration
            "[{\"namespace\":\"imsOrgID\",\"value\":\"test_orgid\"}]" +
            "}"

        assertEquals(expectedIdentifiers, collectedSDKIdentities)
    }

    @Test
    fun `Get SDK Identifiers when only Identity shared state is available`() {
        setIdentitySharedState()

        val collectedSDKIdentities =
            MobileIdentitiesProvider.collectSdkIdentifiers(mockEvent, mockExtensionApi)
        val expectedIdentifiers = "{" +
            "\"users\":" +
            "[{\"userIDs\":" +
            // Identity
            "[{\"namespace\":\"4\",\"type\":\"namespaceId\",\"value\":\"test_mid\"}," +
            "{\"namespace\":\"type1\",\"type\":\"integrationCode\",\"value\":\"id1\"}," +
            "{\"namespace\":\"type2\",\"type\":\"integrationCode\",\"value\":\"id2\"}," +
            "{\"namespace\":\"DSID_20914\",\"type\":\"integrationCode\",\"value\":\"test_advertisingId\"}," +
            "{\"namespace\":\"20919\",\"type\":\"integrationCode\",\"value\":\"test_pushId\"}" +
            "]}]}"

        assertEquals(expectedIdentifiers, collectedSDKIdentities)
    }

    @Test
    fun `Get SDK Identifiers when only Audience shared state is available`() {
        setAudienceSharedState()

        val collectedSDKIdentities =
            MobileIdentitiesProvider.collectSdkIdentifiers(mockEvent, mockExtensionApi)
        val expectedIdentifiers = "{" +
            "\"users\":" +
            "[{\"userIDs\":" +
            // Audeince
            "[{\"namespace\":\"test_dpid\",\"type\":\"namespaceId\",\"value\":\"test_dpuuid\"}," +
            "{\"namespace\":\"0\",\"type\":\"namespaceId\",\"value\":\"test_uuid\"}" +
            "]}]}"

        assertEquals(expectedIdentifiers, collectedSDKIdentities)
    }

    @Test
    fun `Get SDK Identifiers when only Analytics shared state is available`() {
        setAnalyticsSharedState()

        val collectedSDKIdentities =
            MobileIdentitiesProvider.collectSdkIdentifiers(mockEvent, mockExtensionApi)
        val expectedIdentifiers = "{" +
            "\"users\":" +
            "[{\"userIDs\":" +
            "[{\"namespace\":\"AVID\",\"type\":\"integrationCode\",\"value\":\"test_aid\"}," +
            "{\"namespace\":\"vid\",\"type\":\"analytics\",\"value\":\"test_vid\"}" +
            "]}]}"

        assertEquals(expectedIdentifiers, collectedSDKIdentities)
    }

    @Test
    fun `Get SDK Identifiers when only Target shared state is available`() {
        setTargetSharedState()

        val collectedSDKIdentities =
            MobileIdentitiesProvider.collectSdkIdentifiers(mockEvent, mockExtensionApi)
        val expectedIdentifiers = "{" +
            "\"users\":" +
            "[{\"userIDs\":" +
            // Target
            "[{\"namespace\":\"tntid\",\"type\":\"target\",\"value\":\"test_tntid\"}," +
            "{\"namespace\":\"3rdpartyid\",\"type\":\"target\",\"value\":\"test_thirdpartyid\"}]}]}"

        assertEquals(expectedIdentifiers, collectedSDKIdentities)
    }

    @Test
    fun `Get SDK Identifiers when Analytics and Target states are pending`() {
        setAudienceSharedState()
        setConfigurationSharedState()
        setIdentitySharedState()
        `when`(
            mockExtensionApi.getSharedState(
                MobileIdentitiesProvider.SharedStateKeys.Target.EXTENSION_NAME,
                mockEvent,
                false,
                SharedStateResolution.ANY
            )
        ).thenReturn(SharedStateResult(SharedStateStatus.PENDING, null))
        `when`(
            mockExtensionApi.getSharedState(
                MobileIdentitiesProvider.SharedStateKeys.Analytics.EXTENSION_NAME,
                mockEvent,
                false,
                SharedStateResolution.ANY
            )
        ).thenReturn(SharedStateResult(SharedStateStatus.PENDING, null))

        val collectedSDKIdentities = MobileIdentitiesProvider.collectSdkIdentifiers(mockEvent, mockExtensionApi)
        val expectedIdentifiers = "{" +
            "\"companyContexts\":" +
            // Configuration
            "[{\"namespace\":\"imsOrgID\",\"value\":\"test_orgid\"}]," +
            "\"users\":" +
            "[{\"userIDs\":" +
            // Audience
            "[{\"namespace\":\"test_dpid\",\"type\":\"namespaceId\",\"value\":\"test_dpuuid\"}," +
            "{\"namespace\":\"0\",\"type\":\"namespaceId\",\"value\":\"test_uuid\"}," +
            // Identity
            "{\"namespace\":\"4\",\"type\":\"namespaceId\",\"value\":\"test_mid\"}," +
            "{\"namespace\":\"type1\",\"type\":\"integrationCode\",\"value\":\"id1\"}," +
            "{\"namespace\":\"type2\",\"type\":\"integrationCode\",\"value\":\"id2\"}," +
            "{\"namespace\":\"DSID_20914\",\"type\":\"integrationCode\",\"value\":\"test_advertisingId\"}," +
            "{\"namespace\":\"20919\",\"type\":\"integrationCode\",\"value\":\"test_pushId\"}]}]}"

        assertEquals(expectedIdentifiers, collectedSDKIdentities)
    }

    @Test
    fun `Get SDK Identifiers when all states are pending`() {
        `when`(
            mockExtensionApi.getSharedState(
                anyString(),
                eq(mockEvent),
                eq(false),
                eq(SharedStateResolution.ANY)
            )
        ).thenReturn(SharedStateResult(SharedStateStatus.PENDING, null))

        val collectedSDKIdentities = MobileIdentitiesProvider.collectSdkIdentifiers(mockEvent, mockExtensionApi)
        val expectedIdentifiers = "{}"

        assertEquals(expectedIdentifiers, collectedSDKIdentities)
    }

    @Test
    fun `Get SDK Identifiers when no states are available`() {
        `when`(
            mockExtensionApi.getSharedState(
                anyString(),
                eq(mockEvent),
                eq(false),
                eq(SharedStateResolution.ANY)
            )
        ).thenReturn(SharedStateResult(SharedStateStatus.NONE, null))

        val collectedSDKIdentities = MobileIdentitiesProvider.collectSdkIdentifiers(mockEvent, mockExtensionApi)
        val expectedIdentifiers = "{}"

        assertEquals(expectedIdentifiers, collectedSDKIdentities)
    }

    @After
    fun tearDown() {
    }

    private fun setIdentitySharedState() {
        val visitor1 = VisitorID("origin1", "type1", "id1", VisitorID.AuthenticationState.AUTHENTICATED)
        val visitor2 = VisitorID("origin2", "type2", "id2", VisitorID.AuthenticationState.LOGGED_OUT)
        val visitor3 = VisitorID("origin2", "DSID_20914", "test_advertisingId", VisitorID.AuthenticationState.LOGGED_OUT)

        val visitorIDs = listOf(
            VisitorIDSerializer.convertVisitorId(visitor1),
            VisitorIDSerializer.convertVisitorId(visitor2),
            VisitorIDSerializer.convertVisitorId(visitor3)
        )

        `when`(
            mockExtensionApi.getSharedState(
                MobileIdentitiesProvider.SharedStateKeys.Identity.EXTENSION_NAME,
                mockEvent,
                false,
                SharedStateResolution.ANY
            )
        ).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf<String, Any?>(
                    "mid" to "test_mid",
                    "advertisingidentifier" to "test_advertisingId",
                    "pushidentifier" to "test_pushId",
                    "visitoridslist" to visitorIDs
                )
            )
        )
    }

    private fun setConfigurationSharedState() {
        `when`(
            mockExtensionApi.getSharedState(
                MobileIdentitiesProvider.SharedStateKeys.Configuration.EXTENSION_NAME,
                mockEvent,
                false,
                SharedStateResolution.ANY
            )
        ).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf("experienceCloud.org" to "test_orgid")
            )
        )
    }

    private fun setTargetSharedState() {
        `when`(
            mockExtensionApi.getSharedState(
                MobileIdentitiesProvider.SharedStateKeys.Target.EXTENSION_NAME,
                mockEvent,
                false,
                SharedStateResolution.ANY
            )
        ).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "tntid" to "test_tntid",
                    "thirdpartyid" to "test_thirdpartyid"
                )
            )
        )
    }

    private fun setAudienceSharedState() {
        `when`(
            mockExtensionApi.getSharedState(
                MobileIdentitiesProvider.SharedStateKeys.Audience.EXTENSION_NAME,
                mockEvent,
                false,
                SharedStateResolution.ANY
            )
        ).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "dpuuid" to "test_dpuuid",
                    "dpid" to "test_dpid",
                    "uuid" to "test_uuid"
                )
            )
        )
    }

    private fun setAnalyticsSharedState() {
        `when`(
            mockExtensionApi.getSharedState(
                MobileIdentitiesProvider.SharedStateKeys.Analytics.EXTENSION_NAME,
                mockEvent,
                false,
                SharedStateResolution.ANY
            )
        ).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "aid" to "test_aid",
                    "vid" to "test_vid"
                )
            )
        )
    }
}
