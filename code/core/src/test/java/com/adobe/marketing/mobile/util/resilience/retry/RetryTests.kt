/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util.resilience.retry

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.lang.Exception
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class RetryTests {

    private val customIntervalFunction = { initialInterval: Long, attempt: Int, lastInterval: Long ->
        val nextInterval = initialInterval * attempt
        if (nextInterval > lastInterval) {
            lastInterval
        } else {
            nextInterval
        }
    }
    @Test(timeout = 1000L)
    fun testRetry_basic() {
        val config = retryConfig {
            maxAttempts(3)
            intervalFunction(fixedWaitInterval(), 10L)
        }
//        val defaultConfig = retryConfig {}
        val executionTime = kotlin.system.measureTimeMillis {
            runBlocking {
                val executor = Retry.createExecutor<String?>(config)
                    .retryOnException {
                        return@retryOnException true
                    }
                var counter = 0
                val result = executor.execute {
                    if (counter > 0) {
                        return@execute "Hello World"
                    }
                    counter++
                    delay(10L)
                    throw Exception("Test failed.")
                }
                assertEquals("Hello World", result)
            }
        }
        assertTrue(executionTime < 100L)
    }

    @Test(timeout = 5000L)
    fun testRetry_retryConfig_maxAttempts() {
        val executionTime = kotlin.system.measureTimeMillis {
            runBlocking {
                val executor = Retry.createExecutor<String?>(
                    retryConfig {
                        maxAttempts(3)
                        intervalFunction(fixedWaitInterval(), 20L)
                    }
                )
                    .retryOnException {
                        return@retryOnException true
                    }
                var counter = 0
                val result = executor.execute {
                    counter++
                    delay(10L)
                    throw Exception("Test failed.")
                }
                assertNull(result)
                assertEquals(3, counter)
            }
        }
        assertTrue(executionTime < 200L)
    }

    @Test(timeout = 5000L)
    fun testRetry_retryConfig_useJitter() {
        var firstInterval = 0L
        var secondInterval = 0L
        val executor = Retry.createExecutor<String?>(
            retryConfig {
                maxAttempts(3)
                useJitter(true)
                intervalFunction(fixedWaitInterval(), 10L)
            }
        )
            .retryOnException {
                return@retryOnException true
            }
            .monitorRetry { attempts, lastIntervalWithJitter ->
                when (attempts) {
                    1 -> firstInterval = lastIntervalWithJitter
                    2 -> secondInterval = lastIntervalWithJitter
                }
            }
        val executionTime = kotlin.system.measureTimeMillis {
            runBlocking {
                var counter = 0
                val result = executor.execute {
                    counter++
                    delay(10L)
                    throw Exception("Test failed.")
                }
                assertNull(result)
                assertEquals(3, counter)
            }
        }
        assertTrue(executionTime < 100L)
        assertNotEquals(firstInterval, secondInterval)
        assertTrue(firstInterval in 5..15)
        assertTrue(secondInterval in 5..15)
    }

    @Test(timeout = 5000L)
    fun testRetry_retryConfig_executionTimeoutInMilliseconds() {
        var firstInterval = 0L
        var secondInterval = 0L
        val executor = Retry.createExecutor<String?>(
            retryConfig {
                maxAttempts(3)
                executionTimeoutInMilliseconds(10L)
                intervalFunction(fixedWaitInterval(), 10L)
            }
        )
            .retryOnException {
                return@retryOnException true
            }
            .monitorRetry { attempts, lastIntervalWithJitter ->
                when (attempts) {
                    1 -> firstInterval = lastIntervalWithJitter
                    2 -> secondInterval = lastIntervalWithJitter
                }
            }
        val executionTime = kotlin.system.measureTimeMillis {
            runBlocking {
                var counter = 0
                val result = executor.execute {
                    counter++
                    delay(50L)
                    fail("Test failed.")
                }
                assertNull(result)
                assertEquals(3, counter)
            }
        }
        assertTrue(executionTime < 100L)
        assertTrue { firstInterval > 0L }
        assertEquals(firstInterval, secondInterval)
    }

    @Test(timeout = 5000L)
    fun testRetry_retryConfig_intervalFunction_fixedWaitInterval() {
        val executor = Retry.createExecutor<String?>(
            retryConfig {
                maxAttempts(2)
                intervalFunction(fixedWaitInterval(), 1000L)
            }
        )
            .retryOnException {
                return@retryOnException true
            }

        val executionTime = kotlin.system.measureTimeMillis {
            runBlocking {
                var counter = 0
                val result = executor.execute {
                    counter++
                    delay(100L)
                    throw Exception("Test failed.")
                }
                assertNull(result)
                assertEquals(2, counter)
            }
        }
        assertTrue(executionTime < 2000L)
    }
}
