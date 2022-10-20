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
import org.junit.Ignore
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
                IdentityExtension::class.java,
                MonitorExtension::class.java
            )
        ) {
            countDownLatch.countDown()
        }
        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    private fun restartExtension() {
        networkMonitor = null

        SDKHelper.resetSDK()

        MobileCore.setApplication(ApplicationProvider.getApplicationContext())
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        val countDownLatch = CountDownLatch(1)
        MobileCore.registerExtensions(listOf(IdentityExtension::class.java)) {
            countDownLatch.countDown()
        }
        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test(timeout = 10000)
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


    @Test(timeout = 10000)
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
        assertTrue(countDownLatch.await(500, TimeUnit.MILLISECONDS))

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

        assertTrue(countDownLatchSecondLaunch.await(500, TimeUnit.MILLISECONDS))
        countDownLatchSecondNetworkMonitor.await()
    }

    @Test(timeout = 10000)
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

    @Test(timeout = 10000)
    @Ignore
    //TODO: getUrlVariables feature depends on Analytics shared state, will re-enable it once Analytics 2.0 migration is done.
    fun testGetUrlVariables() {
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
            countDownLatch.countDown()
        }

        countDownLatch.await()
    }

    @Test
    @Ignore
    //TODO: appendUrl feature depends on Analytics shared state, will re-enable it once Analytics 2.0 migration is done.
    fun testAppendTo() {
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
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @Test(timeout = 10000)
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
        assertTrue(countDownLatch.await(500, TimeUnit.MILLISECONDS))
    }

    @Ignore
    @Test(timeout = 10000)
    //TODO: enable this test once MobileCore.getSdkIdentities event handling is implemented in the new Configuration extension
    fun testGetSdkIdentities() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
        MobileCore.setAdvertisingIdentifier("adid")
        Identity.syncIdentifiers(mapOf("id1" to "value1"))
        MobileCore.getSdkIdentities { identityString ->
            assertNotNull(identityString)
            assertTrue(identityString.contains("DSID_20915"))
            assertTrue(identityString.contains("id1"))
            assertTrue(identityString.contains("imsOrgID"))
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @Test(timeout = 10000)
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

    @Test(timeout = 10000)
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

    @Test(timeout = 10000)
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

    @Test(timeout = 10000)
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

    @Test(timeout = 10000)
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
        assertTrue(countDownLatch.await(500, TimeUnit.MILLISECONDS))
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
        val secondMid = loadStoreMid()
        assertNotEquals("", secondMid)
    }

    @Test(timeout = 10000)
    fun test_getIdentifiers_afterExtensionRestart_restoresIdentifiers() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
        networkMonitor = { url ->
            if (url.contains("d_cid_ic=idType3%01id3%011")) {
                assertTrue(url.contains("https://test.com/id"))
                assertTrue(url.contains("d_orgid=orgid"))
                assertTrue(url.contains("d_cid_ic=ab%E7%8B%AC%E8%A7%92%E5%85%BD%01%E7%8B%AC%E8%A7%92%E5%85%BD%011"))
                assertTrue(url.contains("d_cid_ic=idType1%01id1%011"))
                assertTrue(url.contains("d_cid_ic=anotherIdType%01value1001%011"))
                countDownLatch.countDown()
            }
        }
        val vidList = mapOf(
            "idType1" to "id1",
            "idType2" to "",
            "idType3" to "id3",
            "idType4" to null,
            "ab独角兽" to "独角兽",
            "anotherIdType" to "value1001"
        )
        Identity.syncIdentifiers(
            vidList,
            VisitorID.AuthenticationState.AUTHENTICATED
        )
        countDownLatch.await()

        restartExtension()

        val countDownLatchGetter = CountDownLatch(1)
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
        Identity.getIdentifiers { identifiers ->
            assertNotNull(identifiers)
            assertEquals(4, identifiers.size)
            for (vid in identifiers) {
                assertEquals(vidList[vid.idType], vid.id)
                assertEquals(VisitorID.AuthenticationState.AUTHENTICATED, vid.authenticationState)
            }
            countDownLatchGetter.countDown()
        }
        countDownLatchGetter.await()
    }

    @Test(timeout = 10000)
    fun test_getIdentifiers_afterExtensionRestart_removesVisitorIdsWithDuplicatedIdTypes() {
        val countDownLatch = CountDownLatch(7)
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
        networkMonitor = { url ->
            if (url.contains("idType0") || url.contains("idType1") || url.contains("anotherIdType")) {
                countDownLatch.countDown()
            }
        }

        Identity.syncIdentifier("idType0", "value0", VisitorID.AuthenticationState.LOGGED_OUT)
        Identity.syncIdentifier("idType1", "value1", VisitorID.AuthenticationState.AUTHENTICATED)
        Identity.syncIdentifier("anotherIdType", "value1000", VisitorID.AuthenticationState.UNKNOWN)
        Identity.syncIdentifier("idType1", "value2", VisitorID.AuthenticationState.LOGGED_OUT)
        Identity.syncIdentifier("idType1", "value3", VisitorID.AuthenticationState.UNKNOWN)
        Identity.syncIdentifier(
            "anotherIdType",
            "value1001",
            VisitorID.AuthenticationState.AUTHENTICATED
        )
        Identity.syncIdentifier(
            "anotherIdType",
            "value1002",
            VisitorID.AuthenticationState.LOGGED_OUT
        )
        countDownLatch.await()

        val countDownLatchGetter = CountDownLatch(3)
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
        Identity.getIdentifiers { identifiers ->
            assertNotNull(identifiers)
            assertEquals(3, identifiers.size)
            for (vid in identifiers) {
                when (vid.idType) {
                    "idType0" -> {
                        assertEquals("value0", vid.id)
                        assertEquals(
                            VisitorID.AuthenticationState.LOGGED_OUT,
                            vid.authenticationState
                        )
                        countDownLatchGetter.countDown()
                    }
                    "idType1" -> {
                        assertEquals("value3", vid.id)
                        assertEquals(
                            VisitorID.AuthenticationState.UNKNOWN,
                            vid.authenticationState
                        )
                        countDownLatchGetter.countDown()
                    }
                    "anotherIdType" -> {
                        assertEquals("value1002", vid.id)
                        assertEquals(
                            VisitorID.AuthenticationState.LOGGED_OUT,
                            vid.authenticationState
                        )
                        countDownLatchGetter.countDown()
                    }
                }
            }

        }
        countDownLatchGetter.await()
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
