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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.MobilePrivacyStatus
import com.adobe.marketing.mobile.SDKHelper
import com.adobe.marketing.mobile.Signal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfigurationIntegrationTests {
    companion object {
        const val TEST_APP_ID = "appId"
        const val TEST_RULES_RESOURCE = "rules_configuration_tests.zip"
        const val WAIT_TIME_MILLIS = 5000L
    }

    @Before
    fun setup() {
        SDKHelper.initializeSDK(listOf(Signal.EXTENSION))
    }

    @After
    fun cleanup() {
        SDKHelper.resetSDK()
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
        SDKHelper.setupConfiguration(TEST_APP_ID, mapOf("global.privacy" to "optedin"), TEST_RULES_RESOURCE)

        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)
    }

    @Test
    fun testUpdateConfiguration() {
        SDKHelper.setupConfiguration(TEST_APP_ID, mapOf("global.privacy" to "optedin"), TEST_RULES_RESOURCE)

        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)

        // Test
        MobileCore.updateConfiguration(mapOf("global.privacy" to "optedout"))

        // Verify
        validatePrivacyStatus(MobilePrivacyStatus.OPT_OUT)
    }

    @Test
    fun testConfigurationHonorsEnvironment() {
        SDKHelper.setupConfiguration(TEST_APP_ID, mapOf(
            "global.privacy" to "optedin",
            "__dev__global.privacy" to "unknown",
            "build.environment" to "dev"
        ), TEST_RULES_RESOURCE)

        // Verify
        validatePrivacyStatus(MobilePrivacyStatus.UNKNOWN)

        // Switch environment to prod
        MobileCore.updateConfiguration(mapOf("build.environment" to "prod"))

        // Verify
        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)
    }

    @Test
    fun testConfigurationIsRetainedOnSubsequentLaunch() {
        SDKHelper.setupConfiguration(TEST_APP_ID, mapOf("global.privacy" to "optedin"), TEST_RULES_RESOURCE)

        // Verify
        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)

        //Simulate shut down and initialize SDK again
        SDKHelper.resetSDK(false)
        SDKHelper.initializeSDK(listOf(Signal.EXTENSION))

        // Verify configuration is retained
        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)
    }

    @Test
    fun testClearUpdatedConfiguration() {
        SDKHelper.setupConfiguration(TEST_APP_ID, mapOf("global.privacy" to "optedin"), TEST_RULES_RESOURCE)
        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)

        // Update configuration with privacy status to be opted out
        MobileCore.updateConfiguration(mapOf("global.privacy" to "optedout"))

        // Test
        MobileCore.clearUpdatedConfiguration()

        // Verify
        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)
    }

    @Test
    fun testClearUpdatedConfigurationAfterSetPrivacyStatus() {
        SDKHelper.setupConfiguration(TEST_APP_ID, mapOf("global.privacy" to "optedin"), TEST_RULES_RESOURCE)
        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)

        // Change privacy status to be opted out
        MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT)

        // Test
        MobileCore.clearUpdatedConfiguration()

        // Verify
        validatePrivacyStatus(MobilePrivacyStatus.OPT_IN)
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
