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
import com.adobe.marketing.mobile.Identity
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.VisitorID
import com.adobe.marketing.mobile.identity.IdentityExtension
import com.adobe.marketing.mobile.integration.EventHubProxy
import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
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
                        return HttpURLConnection.HTTP_UNAVAILABLE
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

    @Before
    fun setup() {
        networkMonitor = null
        MobileCore.setApplication(ApplicationProvider.getApplicationContext())
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPreference = context.getSharedPreferences("visitorIDServiceDataStore", 0)
        val editor = sharedPreference.edit()
        editor.clear()
        editor.commit()
        EventHubProxy.resetEventhub()
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
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
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
        EventHubProxy.resetEventhub()
        Thread.sleep(100)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPreference = context.getSharedPreferences("visitorIDServiceDataStore", 0)
        sharedPreference.all.entries.forEach { entry ->
            Log.d("integration_test", "${entry.key} - ${entry.value}")
        }
        //ADOBEMOBILE_PERSISTED_MID
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
        MobileCore.registerExtensions(listOf(IdentityExtension::class.java)) {
            countDownLatchSecondLaunch.countDown()
        }
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
        assertTrue(countDownLatchSecondLaunch.await(100, TimeUnit.MILLISECONDS))
        countDownLatchSecondNetworkMonitor.await()
    }

    @Ignore
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
        //TODO: why we send out this request event if SDK is optedout??
        networkMonitor = { url ->
            if (url.contains("d_cid_ic=id1%01value1%010")) {
                countDownLatch.countDown()
            }
        }
        Identity.syncIdentifiers(mapOf("id1" to "value1"))
        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun testGetUrlVariables() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
        Identity.getUrlVariables { variables ->
            assertNotNull(variables)
            assertTrue(variables.contains("TS"))
            assertTrue(variables.contains("MCMID"))
            assertTrue(variables.contains("MCORGID"))
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun testAppendTo() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
            mapOf(
                "experienceCloud.org" to "orgid",
                "experienceCloud.server" to "test.com",
                "global.privacy" to "optedin"
            )
        )
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

        MobileCore.resetIdentities()

        val countDownLatchSecondNetworkMonitor = CountDownLatch(1)
        networkMonitor = { url ->
            if (url.contains("https://test.com/id")) {
                assertTrue(url.contains("d_orgid=orgid"))
                assertTrue(url.contains("d_mid="))
                assertFalse(url.contains(firstMid))
                countDownLatchSecondNetworkMonitor.countDown()
            }
        }
        countDownLatchSecondNetworkMonitor.await()
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

}
