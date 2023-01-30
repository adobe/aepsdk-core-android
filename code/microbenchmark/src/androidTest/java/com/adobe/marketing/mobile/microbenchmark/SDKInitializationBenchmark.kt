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
package com.adobe.marketing.mobile.microbenchmark

import android.app.Application
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.adobe.marketing.mobile.Identity
import com.adobe.marketing.mobile.Lifecycle
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.SDKHelper
import com.adobe.marketing.mobile.Signal
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Benchmark, which will execute on an Android device.
 *
 * The body of [BenchmarkRule.measureRepeated] is measured in a loop, and Studio will
 * output the result. Modify your code to see how it affects performance.
 */
@RunWith(AndroidJUnit4::class)
class SDKInitializationBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun startEventHubAndRegisterConfiguration() {
        val appContext =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        benchmarkRule.measureRepeated {
            val countDownLatch = CountDownLatch(1)
            MobileCore.setApplication(appContext)
            MobileCore.setLogLevel(LoggingMode.VERBOSE)
            MobileCore.registerExtensions(listOf()) {
                countDownLatch.countDown()
            }
            countDownLatch.await(100, TimeUnit.MILLISECONDS)
            SDKHelper.resetSDK()
        }
    }

    @Test
    fun startEventHubAndRegisterMultipleExtensions() {
        val appContext = ApplicationProvider.getApplicationContext() as Application
        benchmarkRule.measureRepeated {
            val countDownLatch = CountDownLatch(1)
            MobileCore.setApplication(appContext)
            MobileCore.setLogLevel(LoggingMode.VERBOSE)
            MobileCore.registerExtensions(
                listOf(
                    Lifecycle.EXTENSION,
                    Signal.EXTENSION,
                    Identity.EXTENSION
                )
            ) {
                countDownLatch.countDown()
            }
            countDownLatch.await(100, TimeUnit.MILLISECONDS)
            SDKHelper.resetSDK()
        }
    }
}