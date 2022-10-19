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

    //TODO: This test not working because of this benchmark library issue: https://issuetracker.google.com/issues/238350808
    // The trace file is generated, but the benchmark library can't read it -> /storage/emulated/0/Android/data/com.adobe.marketing.mobile.benchmark/cache/RulesEngineBenchmark_simpleViewClick_iter000_2022-10-19-02-13-45.perfetto-trace
    // As a workaround, we can manually download the above trace file and upload it to https://ui.perfetto.dev/
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
//            Thread.sleep(500)
        }
    }

    private fun MacrobenchmarkScope.waitForTextShown(text: String) {
        check(device.wait(Until.hasObject(By.text(text)), 1000)) {
            "View showing '$text' not found after waiting 1000 ms."
        }
    }
}