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

inline fun retryConfig(config: RetryConfig.Builder<Any?>.() -> Unit): RetryConfig {
    return RetryConfig.Builder<Any?>().apply(config).build()
}

typealias NextInterval = (initialInterval: Long, attempt: Int, lastInterval: Long) -> Long

fun fixedWaitInterval(): NextInterval {
    return { _, _, lastInterval -> lastInterval }
}

fun exponentialWaitInterval(): NextInterval {
    return { _, _, lastInterval -> lastInterval * 2 }
}

fun fibonacciWaitInterval(): NextInterval {
    return { initialInterval, attempt, _ ->
        fibonacci(
            attempt,
            initialInterval,
            initialInterval + 1
        )
    }
}

private tailrec fun fibonacci(n: Int, a: Long = 0, b: Long = 1): Long =
    when (n) {
        0 -> a
        1 -> b
        else -> fibonacci(n - 1, b, a + b)
    }

fun linearWaitInterval(): NextInterval {
    return { initialInterval, _, lastInterval -> lastInterval + initialInterval }
}

class RetryConfig private constructor() {
    internal var intervalFunction: NextInterval = fixedWaitInterval()
        private set
    var initialInterval: Long = 1000L
        private set

    var maxInterval: Long = 1000000L // 1000000 milliseconds = 16 minutes and 40 seconds
        private set
    var maxAttempts: Int = -1
        private set
    var useJitter: Boolean = false
        private set

    var executionTimeoutInMilliseconds: Long = 10000L
        private set
    class Builder<T> {
        val config = RetryConfig()

        fun build(): RetryConfig {
            return config
        }

        fun intervalFunction(
            intervalFunction: NextInterval,
            initialInterval: Long = 1000L,
            maxInterval: Long = 0
        ) {
            config.intervalFunction = intervalFunction
            config.initialInterval = initialInterval
            if (maxInterval > 0) {
                config.maxInterval = maxInterval
            }
        }

        fun maxAttempts(attempts: Int) {
            config.maxAttempts = attempts
        }

        fun useJitter(flag: Boolean) {
            config.useJitter = flag
        }

        fun executionTimeoutInMilliseconds(timeout: Long) {
            config.executionTimeoutInMilliseconds = timeout
        }
    }
}
