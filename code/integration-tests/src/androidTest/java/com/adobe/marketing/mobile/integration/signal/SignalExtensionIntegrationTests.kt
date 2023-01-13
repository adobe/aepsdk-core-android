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
package com.adobe.marketing.mobile.integration.signal

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.SDKHelper
import com.adobe.marketing.mobile.Signal
import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private typealias NetworkMonitor = (url: String) -> Unit

@RunWith(AndroidJUnit4::class)
class SignalExtensionIntegrationTests {

    companion object {
        private var networkMonitor: NetworkMonitor? = null

        @BeforeClass
        @JvmStatic
        fun setupClass() {
            val appContext =
                InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

            val countDownLatch = CountDownLatch(1)
            SDKHelper.resetSDK()
            ServiceProvider.getInstance().networkService = Networking { request, callback ->
                var connection: HttpConnecting? = null
                with(request.url) {
                    when {
                        startsWith("https://adobe.com") && contains("rules_signal.zip") -> {
                            connection = MockedHttpConnecting("rules_signal")
                        }
                        startsWith("https://adobe.com") && contains("rules_pii.zip") -> {
                            connection = MockedHttpConnecting("rules_pii")
                        }
                    }
                }
                if (callback != null && connection != null) {
                    callback.call(connection)
                } else {
                    // If no callback is passed by the client, close the connection.
                    connection?.close()
                }
                networkMonitor?.let { it(request.url) }
            }

            MobileCore.setApplication(appContext)
            MobileCore.setLogLevel(LoggingMode.VERBOSE)
            MobileCore.registerExtensions(listOf(Signal.EXTENSION)) {
                countDownLatch.countDown()
            }
            countDownLatch.await(100, TimeUnit.MILLISECONDS)

        }
    }

    @Before
    fun setUP() {

    }

    @Test
    fun testGetRequest() {
        val countDownLatch = CountDownLatch(1)
        networkMonitor = { url ->
            if (url == "https://www.signal.com?name=testGetRequest") {
                countDownLatch.countDown()
            }
        }
        MobileCore.updateConfiguration(
            mapOf(
                "global.privacy" to "optedin",
                "rules.url" to "https://adobe.com/rules_signal.zip"
            )
        )
        Thread.sleep(10)
        MobileCore.dispatchEvent(
            Event.Builder("Test", "type", "source")
                .setEventData(
                    mapOf(
                        "name" to "testGetRequest"
                    )
                ).build()
        )
        assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))
    }

    @Test
    fun testPostRequest() {
        val countDownLatch = CountDownLatch(1)
        networkMonitor = { url ->
            if (url == "https://www.signal.com?name=testPostRequest") {
                countDownLatch.countDown()
            }
        }
        MobileCore.updateConfiguration(
            mapOf(
                "global.privacy" to "optedin",
                "rules.url" to "https://adobe.com/rules_signal.zip"
            )
        )
        Thread.sleep(10)
        MobileCore.dispatchEvent(
            Event.Builder("Test", "type", "source")
                .setEventData(
                    mapOf(
                        "name" to "testPostRequest"
                    )
                ).build()
        )
        assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))
    }

    @Test
    fun testOptedOut() {
        var signalRequestCaught = false
        val countDownLatch = CountDownLatch(1)
        networkMonitor = { url ->
            if (url.startsWith("https://www.signal.com")) {
                signalRequestCaught = true
            }
        }
        MobileCore.updateConfiguration(
            mapOf(
                "global.privacy" to "optedout",
                "rules.url" to "https://adobe.com/rules_signal.zip"
            )
        )
        Thread.sleep(10)
        MobileCore.dispatchEvent(
            Event.Builder("Test", "type", "source")
                .setEventData(
                    mapOf(
                        "name" to "testPostRequest"
                    )
                ).build()
        )
        MobileCore.dispatchEvent(
            Event.Builder("Test", "type", "source")
                .setEventData(
                    mapOf(
                        "name" to "testGetRequest"
                    )
                ).build()
        )
        assertFalse(countDownLatch.await(10, TimeUnit.MILLISECONDS))
        assertFalse(signalRequestCaught)
    }

    @Test
    fun testPii() {
        val countDownLatch = CountDownLatch(1)
        networkMonitor = { url ->
            if (url == "https://www.pii.com?name=aep") {
                countDownLatch.countDown()
            }
        }
        MobileCore.updateConfiguration(
            mapOf(
                "global.privacy" to "optedin",
                "rules.url" to "https://adobe.com/rules_pii.zip"
            )
        )
        Thread.sleep(100)
        MobileCore.collectPii(mapOf("name" to "aep"))
        assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))
    }

}

private class MockedHttpConnecting(val rulesFileName: String) : HttpConnecting {
    var rulesStream: InputStream? = null

    override fun getInputStream(): InputStream? {
        rulesStream = this::class.java.classLoader?.getResource("${rulesFileName}.zip")
            ?.openStream()!!
        return rulesStream
    }

    override fun getErrorStream(): InputStream? {
        return null
    }

    override fun getResponseCode(): Int {
        return 200
    }

    override fun getResponseMessage(): String {
        return ""
    }

    override fun getResponsePropertyValue(responsePropertyKey: String?): String {
        return ""
    }

    override fun close() {
        rulesStream?.close()
    }

}
