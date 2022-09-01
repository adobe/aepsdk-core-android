package com.example.benchmark

import android.app.Application
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.adobe.marketing.mobile.MobileCore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class CoreExtensionBenchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val app =
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Test
    fun startEventHub() {
        benchmarkRule.measureRepeated {
            val latch = CountDownLatch(1)
            MobileCore.setApplication(app)
            MobileCore.start {
                latch.countDown()
            }
            latch.await(50, TimeUnit.MILLISECONDS)
        }
    }

}