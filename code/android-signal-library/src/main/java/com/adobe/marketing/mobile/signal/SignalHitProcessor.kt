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
package com.adobe.marketing.mobile.signal

import com.adobe.marketing.mobile.services.*
import java.util.concurrent.CountDownLatch

internal class SignalHitProcessor : HitProcessing {

    companion object {
        private const val TAG = "SignalHitProcessor"
        private const val HIT_QUEUE_RETRY_TIME_SECONDS = 30
        private val networkService = ServiceProvider.getInstance().networkService
    }

    override fun retryInterval(entity: DataEntity?): Int {
        return HIT_QUEUE_RETRY_TIME_SECONDS
    }

    override fun processHit(entity: DataEntity?): Boolean {
        val request = buildNetworkRequest(entity) ?: run {
            // TODO: logs
            return false
        }
        val countDownLatch = CountDownLatch(1)
        var result = false
        networkService.connectAsync(request) { connection ->
            val responseCode = connection.responseCode
            result = when (responseCode) {
                in SignalConstants.HTTP_SUCCESS_CODES -> {
                    // TODO: logs
                    true
                }
                in SignalConstants.RECOVERABLE_ERROR_CODES -> {
                    // TODO: logs
                    false
                }
                else -> {
                    // TODO: logs
                    false
                }
            }
            countDownLatch.countDown()
        }
        countDownLatch.await()
        return result
    }

    private fun buildNetworkRequest(entity: DataEntity?): NetworkRequest? {
        if (entity == null) {
            // TODO: logs
            return null
        }
        val signalDataEntity = SignalConsequence.from(entity)
        if (signalDataEntity.url.isEmpty()) {
            // TODO: logs
            return null
        }
        val timeoutRaw = signalDataEntity.timeout(0)
        val timeout = if (timeoutRaw > 0) timeoutRaw else SignalConstants.DEFAULT_NETWORK_TIMEOUT
        val postBody = signalDataEntity.body
        val httpMethod =
                if (postBody.isEmpty()) HttpMethod.GET else HttpMethod.POST
        val contentType = signalDataEntity.contentType
        val header =
                if (contentType.isEmpty())
                    emptyMap()
                else
                    mapOf(SignalConstants.NETWORK_REQUEST_HEATER_CONTENT_TYPE to contentType)
        return NetworkRequest(
                signalDataEntity.url,
                httpMethod,
                postBody.toByteArray(Charsets.UTF_8),
                header,
                timeout,
                timeout
        )
    }
}