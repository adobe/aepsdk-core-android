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
package com.adobe.marketing.mobile.integration.lifecycle

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.adobe.marketing.mobile.*
import com.adobe.marketing.mobile.integration.ConfigurationMonitor
import com.adobe.marketing.mobile.integration.MonitorExtension
import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ServiceProviderModifier
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import java.io.InputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private typealias NetworkMonitor = (url: String) -> Unit

class LifecycleIntegrationTests {

    companion object {
        private var networkMonitor: NetworkMonitor? = null

        @BeforeClass
        @JvmStatic
        fun setupClass() {
            overrideNetworkService()
        }

        private fun overrideNetworkService() {
            ServiceProvider.getInstance().networkService = Networking { request, callback ->
                var connection: HttpConnecting = MockedHttpConnecting(null)
                with(request.url) {
                    when {
                        startsWith("https://adobe.com") && contains("rules_lifecycle.zip") -> {
                            connection = MockedHttpConnecting("rules_lifecycle")
                        }
                    }
                }
                if (callback != null) {
                    callback.call(connection)
                } else {
                    // If no callback is passed by the client, close the connection.
                    connection.close()
                }
                networkMonitor?.let { it(request.url) }
            }

        }
    }

    @Before
    fun setup() {
        networkMonitor = null
        SDKHelper.resetSDK()
        clearSharedPreference()

        val countDownLatch = CountDownLatch(1)
        MobileCore.setApplication(ApplicationProvider.getApplicationContext())
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        MobileCore.registerExtensions(
                listOf(
                        Lifecycle.EXTENSION,
                        Identity.EXTENSION,
                        Signal.EXTENSION,
                        MonitorExtension::class.java
                )
        )
        {
            countDownLatch.countDown()
        }
        Assert.assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    private fun clearSharedPreference() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPreference = context.getSharedPreferences("AdobeMobile_Lifecycle", 0)
        val editor = sharedPreference.edit()
        editor.clear()
        editor.commit()
    }

    @Test(timeout = 10000)
    fun testInstall() {
        // setup
        val countDownLatch = CountDownLatch(1)
        var postbackUrl = ""
        networkMonitor = { url ->
            if (url.startsWith("https://www.lifecycle.com")) {
                postbackUrl = url
                // verify
                countDownLatch.countDown()
            }
        }
        MobileCore.updateConfiguration(
                mapOf(
                        "lifecycle.sessionTimeout" to 300,
                        "global.privacy" to "optedin",
                        "rules.url" to "https://adobe.com/rules_lifecycle.zip"
                )
        )
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()

        // test
        MobileCore.lifecycleStart(null)
        countDownLatch.await()
        Assert.assertTrue(postbackUrl.contains("installevent=InstallEvent"))
    }

    @Test(timeout = 10000)
    fun testLaunch() {
        // setup
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
                mapOf(
                        "lifecycle.sessionTimeout" to 1,
                        "global.privacy" to "optedin",
                        "rules.url" to "https://adobe.com/rules_lifecycle.zip"
                )
        )
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()

        networkMonitor = { url ->
            if (url.startsWith("https://www.lifecycle.com")) {
                countDownLatch.countDown()
            }
        }

        MobileCore.lifecycleStart(null)
        Assert.assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))

        MobileCore.lifecyclePause()
        Thread.sleep(2000)

        // restart
        SDKHelper.resetSDK()
        ServiceProviderModifier.reset()
        overrideNetworkService()

        MobileCore.setApplication(ApplicationProvider.getApplicationContext())
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        val countDownLatchSecondLifecycleStart = CountDownLatch(1)
        var postbackUrl = ""
        networkMonitor = { url ->
            if (url.startsWith("https://www.lifecycle.com")) {
                // verify
                postbackUrl = url
                countDownLatchSecondLifecycleStart.countDown()
            }
        }
        val countDownLatchSecondLaunch = CountDownLatch(1)
        MobileCore.registerExtensions(
                listOf(
                        Lifecycle.EXTENSION,
                        Identity.EXTENSION,
                        Signal.EXTENSION,
                        MonitorExtension::class.java
                )
        ) {
            countDownLatchSecondLaunch.countDown()
        }

        MobileCore.updateConfiguration(
                mapOf(
                        "lifecycle.sessionTimeout" to 1,
                        "global.privacy" to "optedin",
                        "rules.url" to "https://adobe.com/rules_lifecycle.zip"
                )
        )
        val configurationLatch2 = CountDownLatch(1)
        configurationAwareness { configurationLatch2.countDown() }
        configurationLatch2.await()

        // test
        MobileCore.lifecycleStart(null)
        Assert.assertTrue(countDownLatchSecondLaunch.await(1, TimeUnit.SECONDS))
        countDownLatchSecondLifecycleStart.await()
        Assert.assertTrue(postbackUrl.contains("installevent=&"))
        Assert.assertTrue(postbackUrl.contains("launchevent=LaunchEvent"))
    }

    @Test(timeout = 10000)
    fun testCrash() {
        // setup
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
                mapOf(
                        "lifecycle.sessionTimeout" to 1,
                        "global.privacy" to "optedin",
                        "rules.url" to "https://adobe.com/rules_lifecycle.zip"
                )
        )
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()

        networkMonitor = { url ->
            if (url.startsWith("https://www.lifecycle.com")) {
                countDownLatch.countDown()
            }
        }

        MobileCore.lifecycleStart(null)
        Assert.assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))

        // restart
        SDKHelper.resetSDK()
        ServiceProviderModifier.reset()
        overrideNetworkService()

        MobileCore.setApplication(ApplicationProvider.getApplicationContext())
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        val countDownLatchSecondLifecycleStart = CountDownLatch(1)
        var postbackUrl = ""
        networkMonitor = { url ->
            if (url.startsWith("https://www.lifecycle.com")) {
                // verify
                postbackUrl = url
                countDownLatchSecondLifecycleStart.countDown()
            }
        }
        val countDownLatchSecondLaunch = CountDownLatch(1)
        MobileCore.registerExtensions(
                listOf(
                        Lifecycle.EXTENSION,
                        Identity.EXTENSION,
                        Signal.EXTENSION,
                        MonitorExtension::class.java
                )
        ) {
            countDownLatchSecondLaunch.countDown()
        }

        MobileCore.updateConfiguration(
                mapOf(
                        "lifecycle.sessionTimeout" to 1,
                        "global.privacy" to "optedin",
                        "rules.url" to "https://adobe.com/rules_lifecycle.zip"
                )
        )
        val configurationLatch2 = CountDownLatch(1)
        configurationAwareness { configurationLatch2.countDown() }
        configurationLatch2.await()

        // test
        MobileCore.lifecycleStart(null)
        Assert.assertTrue(countDownLatchSecondLaunch.await(1, TimeUnit.SECONDS))
        countDownLatchSecondLifecycleStart.await()
        Assert.assertTrue(postbackUrl.contains("installevent=&"))
        Assert.assertTrue(postbackUrl.contains("launchevent=LaunchEvent"))
        Assert.assertTrue(postbackUrl.contains("crashevent=CrashEvent"))
    }

    @Test(timeout = 10000)
    fun testAdditionalContextData() {
        val countDownLatch = CountDownLatch(1)
        var postbackUrl = ""
        networkMonitor = { url ->
            if (url.startsWith("https://www.lifecycle.com")) {
                postbackUrl = url
                countDownLatch.countDown()
            }
        }
        MobileCore.updateConfiguration(
                mapOf(
                        "lifecycle.sessionTimeout" to 1,
                        "global.privacy" to "optedin",
                        "rules.url" to "https://adobe.com/rules_lifecycle.zip"
                )
        )
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()

        MobileCore.lifecycleStart(mapOf("key" to "value"))
        countDownLatch.await()
        Assert.assertTrue(postbackUrl.contains("key=value"))
    }

    @Test(timeout = 10000)
    fun testSessionContinue() {
        // setup
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
                mapOf(
                        "lifecycle.sessionTimeout" to 10,
                        "global.privacy" to "optedin",
                        "rules.url" to "https://adobe.com/rules_lifecycle.zip"
                )
        )
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        configurationLatch.await()

        var postbackUrl = ""
        networkMonitor = { url ->
            if (url.startsWith("https://www.lifecycle.com")) {
                postbackUrl = url
                countDownLatch.countDown()
            }
        }

        MobileCore.lifecycleStart(null)
        Assert.assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))
        Assert.assertTrue(postbackUrl.contains("installevent=InstallEvent"))

        MobileCore.lifecyclePause()
        Thread.sleep(2000)

        // restart
        SDKHelper.resetSDK()
        ServiceProviderModifier.reset()
        overrideNetworkService()

        MobileCore.setApplication(ApplicationProvider.getApplicationContext())
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        val countDownLatchSecondLifecycleStart = CountDownLatch(1)
        networkMonitor = { url ->
            if (url.startsWith("https://www.lifecycle.com")) {
                // verify
                countDownLatchSecondLifecycleStart.countDown()
            }
        }
        val countDownLatchSecondLaunch = CountDownLatch(1)
        MobileCore.registerExtensions(
                listOf(
                        Lifecycle.EXTENSION,
                        Identity.EXTENSION,
                        Signal.EXTENSION,
                        MonitorExtension::class.java
                )
        ) {
            countDownLatchSecondLaunch.countDown()
        }

        MobileCore.updateConfiguration(
                mapOf(
                        "lifecycle.sessionTimeout" to 10,
                        "global.privacy" to "optedin",
                        "rules.url" to "https://adobe.com/rules_lifecycle.zip"
                )
        )
        val configurationLatch2 = CountDownLatch(1)
        configurationAwareness { configurationLatch2.countDown() }
        configurationLatch2.await()

        // test
        MobileCore.lifecycleStart(null)
        Assert.assertTrue(countDownLatchSecondLaunch.await(1, TimeUnit.SECONDS))
        Assert.assertFalse(countDownLatchSecondLifecycleStart.await(1, TimeUnit.SECONDS))
    }

    private fun configurationAwareness(callback: ConfigurationMonitor) {
        MonitorExtension.configurationAwareness(callback)
    }
}


private class MockedHttpConnecting(val rulesFileName: String?) : HttpConnecting {
    var rulesStream: InputStream? = null

    override fun getInputStream(): InputStream? {
        if (rulesFileName != null) {
            rulesStream = this::class.java.classLoader?.getResource("${rulesFileName}.zip")
                    ?.openStream()!!
            return rulesStream
        }
        return null
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