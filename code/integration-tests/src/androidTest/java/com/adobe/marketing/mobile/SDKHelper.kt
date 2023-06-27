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
package com.adobe.marketing.mobile

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.adobe.marketing.mobile.integration.MockNetworkResponse
import com.adobe.marketing.mobile.integration.core.EventHistoryIntegrationTests
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import org.json.JSONObject
import org.junit.Assert
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object SDKHelper {
    @JvmStatic
    fun resetSDK(clearPreferences: Boolean = true) {
        MobileCore.resetSDK()
        if (clearPreferences) {
            clearSharedPreference()
        }
    }

    private const val CONFIG_URL_PREFIX = "https://assets.adobedtm.com"
    private const val TEST_RULES_URL = "https://rules.com/rules.zip"
    private const val CONFIG_RULES_KEY = "rules.url"
    private const val WAIT_TIME_MILLIS = 1000L
    private const val CONFIGURATION_DATA_STORE = "AdobeMobile_ConfigState"
    private const val LIFECYCLE_DATA_STORE = "AdobeMobile_Lifecycle"


    fun setupNetworkService(
        configURL: String,
        mockConfigResponse: Map<String, String>,
        rulesURL: String?,
        mockRulesResource: String?,
        urlMonitor: (String) -> Unit
    ) {
        ServiceProvider.getInstance().networkService = Networking { request, callback ->
            var connection: MockNetworkResponse? = null
            when (request.url) {
                configURL -> {
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

                rulesURL -> {
                    val rulesStream =
                        this::class.java.classLoader?.getResource(
                            mockRulesResource
                        )
                            ?.openStream()!!
                    connection = MockNetworkResponse(
                        HttpURLConnection.HTTP_OK, "OK", emptyMap(), rulesStream, urlMonitor
                    )
                }
                else -> {
                    connection = MockNetworkResponse(HttpURLConnection.HTTP_NOT_FOUND, "NOT FOUND", emptyMap(), "".byteInputStream(), urlMonitor)
                }
            }

            if (callback != null && connection != null) {
                callback.call(connection)
            }
            connection?.urlMonitor?.invoke(request.url)
            connection?.close()
        }
    }

    fun setupConfiguration(appId: String,
                           mockConfigResponse: Map<String, String>,
                           mockRulesResource: String? = null,
                           waitTime: Long = WAIT_TIME_MILLIS ) {
        val configUrlValidationLatch = CountDownLatch(1)
        val rulesUrlValidationLatch = CountDownLatch(1)

        val configURL = "$CONFIG_URL_PREFIX/$appId.json"
        var configWithRules = mockConfigResponse.toMutableMap()
        var rulesURL: String? = null
        if (mockRulesResource != null) {
            configWithRules[CONFIG_RULES_KEY] = TEST_RULES_URL
            rulesURL = TEST_RULES_URL
        }
        setupNetworkService(
            configURL,
            configWithRules,
            rulesURL,
            mockRulesResource
        ) {
            when (it) {
                configURL -> configUrlValidationLatch.countDown()
                rulesURL -> rulesUrlValidationLatch.countDown()
            }
        }

        MobileCore.configureWithAppID(appId)
        var ret = configUrlValidationLatch.await(waitTime, TimeUnit.MILLISECONDS)
        if (ret && rulesURL != null) {
            ret = rulesUrlValidationLatch.await(waitTime, TimeUnit.MILLISECONDS)
        }
        Assert.assertTrue(ret)
    }

    fun initializeSDK(extensions: List<Class<out Extension>>, waitTime: Long = WAIT_TIME_MILLIS) {
        val initializationLatch = CountDownLatch(1)

        MobileCore.setApplication(ApplicationProvider.getApplicationContext())
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        MobileCore.registerExtensions(extensions) {
            initializationLatch.countDown()
        }

        Assert.assertTrue(
            initializationLatch.await(
                waitTime,
                TimeUnit.MILLISECONDS
            )
        )
    }

    private fun clearSharedPreference() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val lifecycleSharedPreference = context.getSharedPreferences(LIFECYCLE_DATA_STORE, 0)
        lifecycleSharedPreference.edit().clear().commit()

        val configSharedPreference = context.getSharedPreferences(CONFIGURATION_DATA_STORE, 0)
        configSharedPreference.edit().clear().commit()
    }
}