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

package com.adobe.marketing.mobile.integration.core

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.MobilePrivacyStatus
import com.adobe.marketing.mobile.SDKHelper
import com.adobe.marketing.mobile.Signal
import com.adobe.marketing.mobile.integration.MockNetworkResponse
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import junit.framework.TestCase
import org.json.JSONObject
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfigurationIntegrationTests {
    companion object {
        const val TEST_RULES_URL = "https://rules.com/rules.zip"
        const val TEST_APP_ID = "appId"
        const val TEST_CONFIG_URL = "https://assets.adobedtm.com/${TEST_APP_ID}.json"
        const val TEST_RULES_RESOURCE = "rules_configuration_tests.zip"
        const val WAIT_TIME_MILLIS = 500L
    }

    @Before
    fun setup() {
        // Setup with only configuration extension
        SDKHelper.resetSDK()
        initializeSDK()
    }

    @Test
    fun testConfigureWithFilePath() {
        // Setup with unknown privacy
        MobileCore.setPrivacyStatus(MobilePrivacyStatus.UNKNOWN)
        validatePrivacyStatus(MobilePrivacyStatus.UNKNOWN)

        // Test config with file path that contains privacy opted out.
        val configFilePath = ConfigurationIntegrationTests::class.java.classLoader?.getResource(
            "ADBMobileConfig-OptedOut.json"
        )!!.path

        MobileCore.configureWithFileInPath(configFilePath)

        // Verify
        validatePrivacyStatus(MobilePrivacyStatus.UNKNOWN)
    }

    @Test
    fun testConfigureWithAppId() {
        val configUrlValidationLatch = CountDownLatch(1)
        val rulesUrlValidationLatch = CountDownLatch(1)

        // Sets up a network service to respond with a mock config and rules
        setupNetworkService(
            mapOf(
                "global.privacy" to "optedin",
                "rules.url" to "https://rules.com/rules.zip"
            ),
            TEST_RULES_RESOURCE
        ) {
            when (it) {
                TEST_CONFIG_URL -> configUrlValidationLatch.countDown()
                TEST_RULES_URL -> rulesUrlValidationLatch.countDown()
            }
        }

        MobileCore.configureWithAppID(TEST_APP_ID)

        // Verify
        Assert.assertTrue(configUrlValidationLatch.await(WAIT_TIME_MILLIS, TimeUnit.MILLISECONDS))
        Assert.assertTrue(rulesUrlValidationLatch.await(WAIT_TIME_MILLIS, TimeUnit.MILLISECONDS))

        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)
    }

    @Test
    fun testUpdateConfiguration() {
        val configUrlValidationLatch = CountDownLatch(1)
        val rulesUrlValidationLatch = CountDownLatch(1)

        // Setup with initial privacy status
        setupNetworkService(
            mapOf(
                "global.privacy" to "optedin"
            ), TEST_RULES_RESOURCE
        ) {
            when (it) {
                TEST_CONFIG_URL -> configUrlValidationLatch.countDown()
                TEST_RULES_URL -> rulesUrlValidationLatch.countDown()
            }
        }


        MobileCore.configureWithAppID(TEST_APP_ID)
        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)

        // Test
        MobileCore.updateConfiguration(mapOf("global.privacy" to "optedout"))

        // Verify
        validatePrivacyStatus(MobilePrivacyStatus.OPT_OUT)
    }

    @Test
    fun testConfigurationHonorsEnvironment() {
        val configUrlValidationLatch = CountDownLatch(1)
        val rulesUrlValidationLatch = CountDownLatch(1)

        // Setup with different privacy values for dev and prod
        setupNetworkService(
            mapOf(
                "global.privacy" to "optedin",
                "__dev__global.privacy" to "unknown",
                "build.environment" to "dev"
            ), TEST_RULES_RESOURCE
        ) {
            when (it) {
                TEST_CONFIG_URL -> configUrlValidationLatch.countDown()
                TEST_RULES_URL -> rulesUrlValidationLatch.countDown()
            }
        }

        // Test
        MobileCore.configureWithAppID(TEST_APP_ID)

        // Verify
        validatePrivacyStatus(MobilePrivacyStatus.UNKNOWN)

        // Switch environment to prod
        MobileCore.updateConfiguration(mapOf("build.environment" to "prod"))

        // Verify
        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)
    }

    @Test
    fun testConfigurationIsRetainedOnSubsequentLaunch() {
        val configUrlValidationLatch = CountDownLatch(1)
        val rulesUrlValidationLatch = CountDownLatch(1)

        setupNetworkService(
            mapOf(
                "global.privacy" to "optedin"
            ), TEST_RULES_RESOURCE
        ) {
            when (it) {
                TEST_CONFIG_URL -> configUrlValidationLatch.countDown()
                TEST_RULES_URL -> rulesUrlValidationLatch.countDown()
            }
        }

        // Test
        MobileCore.configureWithAppID(TEST_APP_ID)

        // Verify
        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)

        //Simulate shut down and initialize SDK again
        SDKHelper.resetSDK()
        initializeSDK()

        // Verify configuration is retained
        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)
    }

    @Test
    fun testClearUpdatedConfiguration() {
        val configUrlValidationLatch = CountDownLatch(1)
        val rulesUrlValidationLatch = CountDownLatch(1)

        // Setup with opted in privacy
        setupNetworkService(
            mapOf(
                "global.privacy" to "optedin"
            ), TEST_RULES_RESOURCE
        ) {
            when (it) {
                TEST_CONFIG_URL -> configUrlValidationLatch.countDown()
                TEST_RULES_URL -> rulesUrlValidationLatch.countDown()
            }
        }

        MobileCore.configureWithAppID(TEST_APP_ID)
        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)

        // Update configuration with privacy status to be opted out
        MobileCore.updateConfiguration(mapOf("global.privacy" to "optedout"))

        // Test
        MobileCore.clearUpdatedConfiguration()

        // Verify
        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)
    }

    @Test
    fun testClearUpdatedConfigurationWAfterSetPrivacyStatus() {
        val configUrlValidationLatch = CountDownLatch(1)
        val rulesUrlValidationLatch = CountDownLatch(1)

        // Setup with opted in privacy
        setupNetworkService(
            mapOf(
                "global.privacy" to "optedin"
            ), TEST_RULES_RESOURCE
        ) {
            when (it) {
                TEST_CONFIG_URL -> configUrlValidationLatch.countDown()
                TEST_RULES_URL -> rulesUrlValidationLatch.countDown()
            }
        }

        MobileCore.configureWithAppID(TEST_APP_ID)
        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)

        // Change privacy status to be opted out
        MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT)

        // Test
        MobileCore.clearUpdatedConfiguration()

        // Verify
        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)
    }

    @After
    fun cleanup() {
        SDKHelper.resetSDK()
    }

    private fun initializeSDK() {
        val initializationLatch = CountDownLatch(1)

        MobileCore.setApplication(ApplicationProvider.getApplicationContext())
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        MobileCore.registerExtensions(listOf(Signal.EXTENSION)) {
            initializationLatch.countDown()
        }

        TestCase.assertTrue(initializationLatch.await(WAIT_TIME_MILLIS, TimeUnit.MILLISECONDS))
    }

    private fun setupNetworkService(
        mockConfigResponse: Map<String, String>,
        mockRulesResource: String,
        urlMonitor: (String) -> Unit
    ) {
        ServiceProvider.getInstance().networkService = Networking { request, callback ->
            var connection: MockNetworkResponse? = null
            when (request.url) {
                TEST_CONFIG_URL -> {
                    val configStream =
                        JSONObject(mockConfigResponse).toString().byteInputStream()
                    connection = MockNetworkResponse(
                        HttpURLConnection.HTTP_OK,
                        "OK",
                        emptyMap(),
                        configStream,
                        urlMonitor
                    )
                }

                TEST_RULES_URL -> {
                    val rulesStream =
                        this::class.java.classLoader?.getResource(
                            mockRulesResource
                        )
                            ?.openStream()!!
                    connection = MockNetworkResponse(
                        HttpURLConnection.HTTP_OK, "OK", emptyMap(), rulesStream, urlMonitor
                    )
                }
            }

            if (callback != null && connection != null) {
                callback.call(connection)
            }
            connection?.urlMonitor?.invoke(request.url)
            connection?.close()
        }
    }

    private fun validatePrivacyStatus(expectedPrivacyStatus: MobilePrivacyStatus) {
        var actualPrivacyStatus: MobilePrivacyStatus? = null
        val waitLatch = CountDownLatch(1)
        MobileCore.getPrivacyStatus {
            actualPrivacyStatus = it
            waitLatch.countDown()
        }
        Assert.assertTrue(waitLatch.await(WAIT_TIME_MILLIS, TimeUnit.MILLISECONDS))
        Assert.assertEquals(expectedPrivacyStatus, actualPrivacyStatus)
    }
}
