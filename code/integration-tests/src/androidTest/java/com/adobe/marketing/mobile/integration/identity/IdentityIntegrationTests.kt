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
package com.adobe.marketing.mobile.integration.identity

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.*
import com.adobe.marketing.mobile.identity.IdentityExtension
import com.adobe.marketing.mobile.integration.ConfigurationMonitor
import com.adobe.marketing.mobile.integration.MonitorExtension
import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ServiceProviderModifier
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private typealias NetworkMonitor = (url: String) -> Unit

@RunWith(AndroidJUnit4::class)
class IdentityIntegrationTests {
    companion object {
        private const val TEST_TIMEOUT : Long = 1000
        private var networkMonitor: NetworkMonitor? = null

        @BeforeClass
        @JvmStatic
        fun setupClass() {
            overrideNetworkService()
        }

        private fun overrideNetworkService() {
            ServiceProvider.getInstance().networkService = Networking { request, callback ->
                networkMonitor?.let { it(request.url) }
                callback.call(object : HttpConnecting {
                    override fun getInputStream(): InputStream? {
                        return null
                    }

                    override fun getErrorStream(): InputStream? {
                        return null
                    }

                    override fun getResponseCode(): Int {
                        return HttpURLConnection.HTTP_REQ_TOO_LONG
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


    private fun clearSharedPreference() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPreference = context.getSharedPreferences("visitorIDServiceDataStore", 0)
        val editor = sharedPreference.edit()
        editor.clear()
        editor.commit()
    }


    @Before
    fun setup() {
        networkMonitor = null
        SDKHelper.resetSDK()

        MobileCore.setApplication(ApplicationProvider.getApplicationContext())

        clearSharedPreference()

        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        val countDownLatch = CountDownLatch(1)
        MobileCore.registerExtensions(
            listOf(
                Identity.EXTENSION,
                MonitorExtension::class.java,
                Analytics.EXTENSION
            )
        ) {
            countDownLatch.countDown()
        }
        assertTrue(countDownLatch.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS))
    }

    @Test(timeout = TEST_TIMEOUT)
    fun testSyncIdentifiers() {
        val countDownLatch = CountDownLatch(1)
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }

        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )

        configurationLatch.await()
        networkMonitor = { url ->
            if (url.contains("d_cid_ic=id1%01value1%011")) {
                assertTrue(url.contains("https://test.com/id"))
                assertTrue(url.contains("d_orgid=orgid"))
                countDownLatch.countDown()
            }
        }
        Identity.syncIdentifiers(
            mapOf("id1" to "value1"),
            VisitorID.AuthenticationState.AUTHENTICATED
        )
        countDownLatch.await()
    }


    @Test(timeout = TEST_TIMEOUT)
    fun testIdentitySendsForceSyncRequestOnEveryLaunch() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )

        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()

        networkMonitor = { url ->
            if (url.contains("d_cid_ic=id1%01value1%011")) {
                assertTrue(url.contains("https://test.com/id"))
                assertTrue(url.contains("d_orgid=orgid"))
                assertTrue(url.contains("d_mid="))
                countDownLatch.countDown()
            }
        }
        Identity.syncIdentifiers(
            mapOf("id1" to "value1"),
            VisitorID.AuthenticationState.AUTHENTICATED
        )
        assertTrue(countDownLatch.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS))

        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPreference = context.getSharedPreferences("visitorIDServiceDataStore", 0)
        sharedPreference.all.entries.forEach { entry ->
            Log.d("integration_test", "${entry.key} - ${entry.value}")
        }

        SDKHelper.resetSDK()
        ServiceProviderModifier.reset()

        overrideNetworkService()

        MobileCore.setApplication(ApplicationProvider.getApplicationContext())
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        val countDownLatchSecondNetworkMonitor = CountDownLatch(1)
        networkMonitor = { url ->
            if (url.contains("https://test.com/id")) {
                assertTrue(url.contains("d_orgid=orgid"))
                assertTrue(url.contains("d_mid="))
                countDownLatchSecondNetworkMonitor.countDown()
            }
        }
        val countDownLatchSecondLaunch = CountDownLatch(1)
        MobileCore.registerExtensions(
            listOf(
                IdentityExtension::class.java,
                MonitorExtension::class.java
            )
        ) {
            countDownLatchSecondLaunch.countDown()
        }

        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )

        val configurationLatch2 = CountDownLatch(1)
        configurationAwareness { configurationLatch2.countDown() }
        configurationLatch2.await()

        assertTrue(countDownLatchSecondLaunch.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS))
        countDownLatchSecondNetworkMonitor.await()
    }

    @Test(timeout = TEST_TIMEOUT)
    fun testOptedout() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedout"
            )
        )
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()
        networkMonitor = { url ->
            if (url.contains("d_cid_ic=id1%01value1%010")) {
                countDownLatch.countDown()
            }
        }
        Identity.syncIdentifiers(mapOf("id1" to "value1"))
        assertFalse(countDownLatch.await(200, TimeUnit.MILLISECONDS))
    }

    @Test(timeout = TEST_TIMEOUT)
    fun testGetUrlVariables_whenValidAnalyticsIds_includesAnalyticsIdsInReturnedUrl() {
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )

        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()

        Analytics.setVisitorIdentifier("testVid")
        val analyticsLatch = CountDownLatch(1)
        Analytics.getVisitorIdentifier{ id ->
            analyticsLatch.countDown()
        }
        analyticsLatch.await()

        val countDownLatch = CountDownLatch(1)
        Identity.getUrlVariables { variables ->
            assertNotNull(variables)
            assertTrue(variables.contains("TS"))
            assertTrue(variables.contains("MCMID"))
            assertTrue(variables.contains("MCORGID"))
            assertTrue(variables.contains("adobe_aa_vid=testVid"))
            countDownLatch.countDown()
        }

        countDownLatch.await()
    }

    @Test(timeout = TEST_TIMEOUT)
    fun testGetUrlVariables_whenNoAnalyticsIds_returnsUrlWithIdentityInfo() {
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )

        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()

        val countDownLatch = CountDownLatch(1)
        Identity.getUrlVariables { variables ->
            assertNotNull(variables)
            assertTrue(variables.contains("TS"))
            assertTrue(variables.contains("MCMID"))
            assertTrue(variables.contains("MCORGID"))
            assertFalse(variables.contains("adobe_aa_vid"))
            assertFalse(variables.contains("MCAID"))
            countDownLatch.countDown()
        }

        countDownLatch.await()
    }

    @Test(timeout = TEST_TIMEOUT)
    fun testAppendTo_whenValidAnalyticsIds_includesAnalyticsIdsInReturnedUrl() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )

        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()
        Analytics.setVisitorIdentifier("testVid")
        val analyticsLatch = CountDownLatch(1)
        Analytics.getVisitorIdentifier{ id ->
            analyticsLatch.countDown()
        }
        analyticsLatch.await()

        Identity.appendVisitorInfoForURL("https://adobe.com") { url ->
            assertNotNull(url)
            assertTrue(url.contains("TS"))
            assertTrue(url.contains("MCMID"))
            assertTrue(url.contains("MCORGID"))
            assertTrue(url.contains("adobe_aa_vid=testVid"))
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @Test
    fun testAppendTo_whenNoAnalyticsIds_returnsUrlWithIdentityInfo() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()

        Identity.appendVisitorInfoForURL("https://adobe.com") { url ->
            assertNotNull(url)
            assertTrue(url.contains("TS"))
            assertTrue(url.contains("MCMID"))
            assertTrue(url.contains("MCORGID"))
            assertFalse(url.contains("adobe_aa_vid"))
            assertFalse(url.contains("MCAID"))
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @Test(timeout = TEST_TIMEOUT)
    fun testGetExperienceCloudId() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()
        Identity.getExperienceCloudId { ecid ->
            assertTrue(ecid.isNotEmpty())
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @Test(timeout = TEST_TIMEOUT)
    fun testGetSdkIdentities() {
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )

        Analytics.setVisitorIdentifier("testVid")
        val analyticsLatch = CountDownLatch(1)
        Analytics.getVisitorIdentifier {
            analyticsLatch.countDown()
        }
        MobileCore.setAdvertisingIdentifier("adid")
        Identity.syncIdentifiers(mapOf("id1" to "value1"))
        val identityLatch = CountDownLatch(1)
        var ecid : String? = null
        Identity.getExperienceCloudId{
            ecid = it
            identityLatch.countDown()
        }
        identityLatch.await()
        analyticsLatch.await()

        val countDownLatch = CountDownLatch(1)
        var receivedSDKIdentities : String? = null
        MobileCore.getSdkIdentities { identityString ->
            receivedSDKIdentities = identityString
            countDownLatch.countDown()
        }
        countDownLatch.await()

        assertNotNull(receivedSDKIdentities)
        assertNotNull(ecid)
        val assertMessage = "Received SDKIdentities: " + receivedSDKIdentities
        assertTrue(assertMessage, receivedSDKIdentities?.contains("{\"namespace\":\"DSID_20914\",\"value\":\"adid\",\"type\":\"integrationCode\"}") ?: false)
        assertTrue(assertMessage, receivedSDKIdentities?.contains("{\"namespace\":\"id1\",\"value\":\"value1\",\"type\":\"integrationCode\"}") ?: false)
        assertTrue(assertMessage, receivedSDKIdentities?.contains("\"companyContexts\":[{\"namespace\":\"imsOrgID\",\"value\":\"orgid\"}]") ?: false)
        assertTrue(assertMessage, receivedSDKIdentities?.contains("{\"namespace\":\"4\",\"value\":\""+ ecid + "\",\"type\":\"namespaceId\"}") ?: false)
        assertTrue(assertMessage, receivedSDKIdentities?.contains("testVid") ?: false)
    }

    @Test(timeout = TEST_TIMEOUT)
    fun testGetIdentifiers() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()
        Identity.syncIdentifier("type1", "id1", VisitorID.AuthenticationState.AUTHENTICATED)
        Identity.getIdentifiers { identifiers ->
            assertNotNull(identifiers)
            assertEquals(1, identifiers.size)
            val first = identifiers[0]
            assertEquals(VisitorID.AuthenticationState.AUTHENTICATED, first.authenticationState)
            assertEquals("d_cid_ic", first.idOrigin)
            assertEquals("id1", first.id)
            assertEquals("type1", first.idType)
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @Test(timeout = TEST_TIMEOUT)
    fun testGetIdentifiers_returnsEmptyList_whenNoIds() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()
        Identity.getIdentifiers { identifiers ->
            assertNotNull(identifiers)
            assertEquals(0, identifiers.size)
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @Test(timeout = TEST_TIMEOUT)
    fun testSetPushIdentifier() {
        val countDownLatchNetworkMonitor = CountDownLatch(1)
        networkMonitor = { url ->
            if (url.contains("20919")) {
                assertTrue(url.contains("d_cid=20919%019516258b6230afdd93cf0cd07b8dd845"))
                countDownLatchNetworkMonitor.countDown()
            }
        }
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()
        MobileCore.setPushIdentifier("9516258b6230afdd93cf0cd07b8dd845")
        countDownLatchNetworkMonitor.await()
    }

    @Test(timeout = TEST_TIMEOUT)
    fun testSetAdvertisingIdentifier() {
        val countDownLatchNetworkMonitor = CountDownLatch(1)
        networkMonitor = { url ->
            if (url.contains("DSID_20914")) {
                assertTrue(url.contains("d_cid_ic=DSID_20914%01adid%011"))
                countDownLatchNetworkMonitor.countDown()
            }
        }
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()
        MobileCore.setAdvertisingIdentifier("adid")
        countDownLatchNetworkMonitor.await()
    }

    @Test(timeout = TEST_TIMEOUT)
    fun testResetIdentities() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()
        networkMonitor = { url ->
            if (url.contains("https://test.com/id")) {
                assertTrue(url.contains("d_orgid=orgid"))
                assertTrue(url.contains("d_mid="))
                countDownLatch.countDown()
            }
        }
        assertTrue(countDownLatch.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS))
        val firstMid = loadStoreMid()
        assertNotEquals("", firstMid)

        val countDownLatchSecondNetworkMonitor = CountDownLatch(1)
        networkMonitor = { url ->
            if (url.contains("https://test.com/id")) {
                assertTrue(url.contains("d_orgid=orgid"))
                assertTrue(url.contains("d_mid="))
                assertFalse(url.contains(firstMid))
                countDownLatchSecondNetworkMonitor.countDown()
            }
        }
        MobileCore.resetIdentities()
        countDownLatchSecondNetworkMonitor.await()
        Thread.sleep(20)
        val secondMid = loadStoreMid()
        assertNotEquals("", secondMid)
    }

    private fun loadStoreMid(): String {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPreference = context.getSharedPreferences("visitorIDServiceDataStore", 0)
        sharedPreference.all.entries.forEach { entry ->
            Log.d("integration_test", "${entry.key} - ${entry.value}")
        }
        return sharedPreference.getString("ADOBEMOBILE_PERSISTED_MID", "")!!
    }

    private fun configurationAwareness(callback: ConfigurationMonitor) {
        MonitorExtension.configurationAwareness(callback)
    }

}
