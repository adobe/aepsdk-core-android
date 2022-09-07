package com.adobe.marketing.mobile.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalMetricApi::class)
class RulesEngineBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun simpleViewClick() {
        var firstStart = true
        benchmarkRule.measureRepeated(
            packageName = "com.adobe.marketing.mobile.app.kotlin",
            metrics = listOf(TraceSectionMetric("RulesTrace")),
            compilationMode = CompilationMode.Full(),
            startupMode = null,
            iterations = 5,
            setupBlock = {
                if (firstStart) {
                    startActivityAndWait()
                    device.findObject(By.text("PerformanceTest")).click()
                    firstStart = false
                }
            }
        ) {
            device.findObject(By.text("EvaluateRules")).click()
            waitForTextShown("999 consequence events caught")
        }
    }
    private fun MacrobenchmarkScope.waitForTextShown(text: String) {
        check(device.wait(Until.hasObject(By.text(text)), 1000)) {
            "View showing '$text' not found after waiting 1000 ms."
        }
    }
}