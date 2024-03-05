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

import androidx.annotation.VisibleForTesting

interface Executor <T> {
    suspend fun execute(block: suspend () -> T?): T?
    fun retryOnException(block: (Exception) -> Boolean): Executor<T>
    fun retryOnResult(block: (T?) -> Boolean): Executor<T>
    fun retryIntervalOnResult(block: (T?) -> Long): Executor<T>
    fun resolveThrowable(block: (Throwable) -> T?): Executor<T>
    fun cancel()
    @VisibleForTesting
    fun monitorRetry(block: (attempts: Int, lastIntervalWithJitter: Long) -> Unit): Executor<T>
}
