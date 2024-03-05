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

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

object Retry {
    internal fun <T> createExecutor(
        config: RetryConfig = RetryConfig.Builder<Any?>().build()
    ): Executor<T> {
        // keep a weak reference inside Retry??
        return ExecutorImpl(config)
    }

    private class ExecutorImpl<T>(val config: RetryConfig) : Executor<T> {

        val DEFAULT_RANDOIZED_FACTOR = 0.5
        var retryOnResultFunction: (T?) -> Boolean = { _: T? -> false }
        var retryOnExceptionFunction: (Exception) -> Boolean = { _: Exception -> false }
        var resolveThrowableFunction: (Throwable) -> T? = { _: Throwable -> null }
        var retryIntervalOnResultFunction: (T?) -> Long = { _: T? -> 0 }
        var monitorRetryFunction: ((attempts: Int, lastIntervalWithJitter: Long) -> Unit)? = null

        // TODO: this is the jitter formula used by Polly, will do more investigation and change it later
        private fun randomize(current: Double, randomizationFactor: Double): Double {
            val delta = randomizationFactor * current
            val min = current - delta
            val max = current + delta
            return min + Math.random() * (max - min + 1)
        }

        override suspend fun execute(block: suspend () -> T?): T? {
            val initialInterval = config.initialInterval
            val intervalFunction = config.intervalFunction
            val maxInterval = config.maxInterval
            val maxAttempts = config.maxAttempts
            val executionTimeoutInMilliseconds = config.executionTimeoutInMilliseconds
            val useJitter = config.useJitter
            var attempt = 0
            var lastInterval = initialInterval
            while (true) {
                attempt++
                try {
                    val result: T? = withTimeout(executionTimeoutInMilliseconds) {
                        block()
                    }
                    if (!retryOnResultFunction(result)) {
                        return result
                    }
                } catch (e: Throwable) {
                    when (e) {
                        is TimeoutCancellationException -> {
                            // continue to retry
                        }
                        is Exception -> {
                            if (!retryOnExceptionFunction(e)) {
                                return null
                            }
                        }
                        else -> return resolveThrowableFunction(e)
                    }
                }

                if (attempt >= maxAttempts) {
                    return null
                }

                lastInterval = if (lastInterval >= maxInterval) {
                    maxInterval
                } else {
                    intervalFunction(initialInterval, attempt, lastInterval)
                }

                if (useJitter) {
                    val intervalWithJitter = randomize(lastInterval.toDouble(), DEFAULT_RANDOIZED_FACTOR).toLong()
                    delay(intervalWithJitter)
                    monitorRetryFunction?.invoke(attempt, intervalWithJitter)
                } else {
                    delay(lastInterval)
                    monitorRetryFunction?.invoke(attempt, lastInterval)
                }
            }
        }

        override fun retryOnException(block: (Exception) -> Boolean): Executor<T> {
            retryOnExceptionFunction = block
            return this
        }

        override fun retryOnResult(block: (T?) -> Boolean): Executor<T> {
            retryOnResultFunction = block
            return this
        }

        override fun retryIntervalOnResult(block: (T?) -> Long): Executor<T> {
            retryIntervalOnResultFunction = block
            return this
        }

        override fun resolveThrowable(block: (Throwable) -> T?): Executor<T> {
            resolveThrowableFunction = block
            return this
        }

        override fun cancel() {}
        override fun monitorRetry(block: (attempts: Int, lastIntervalWithJitter: Long) -> Unit): Executor<T> {
            monitorRetryFunction = block
            return this
        }
    }
}
