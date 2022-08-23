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

import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.Log
import com.adobe.marketing.mobile.services.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class SignalHitProcessor : HitProcessing {
    private val networkService: Networking

    companion object {
        private const val CLASS_NAME = "SignalHitProcessor"
        private const val HIT_QUEUE_RETRY_TIME_SECONDS = 30
    }

    constructor() {
        this.networkService = ServiceProvider.getInstance().networkService
    }

    @VisibleForTesting
    internal constructor(networkService: Networking) {
        this.networkService = networkService
    }

    override fun retryInterval(entity: DataEntity?): Int {
        return HIT_QUEUE_RETRY_TIME_SECONDS
    }

    override fun processHit(entity: DataEntity?): Boolean {
        if (entity == null) {
            Log.warning(SignalConstants.LOG_TAG, "$CLASS_NAME - Drop this data entity as it is null.")
            return true
        }
        val request = buildNetworkRequest(entity) ?: run {
            Log.warning(
                SignalConstants.LOG_TAG,
                "$CLASS_NAME - Drop this data entity as it's not able to convert it to a valid Signal request: ${entity.data}"
            )
            return true
        }
        val countDownLatch = CountDownLatch(1)
        var result = false
        networkService.connectAsync(request) { connection ->
            if (connection == null) {
                countDownLatch.countDown()
                return@connectAsync
            }
            val responseCode = connection.responseCode
            result = when (responseCode) {
                in SignalConstants.HTTP_SUCCESS_CODES -> {
                    Log.debug(
                        SignalConstants.LOG_TAG,
                        "$CLASS_NAME - Signal request (${request.url}) successfully sent."
                    )
                    true
                }
                in SignalConstants.RECOVERABLE_ERROR_CODES -> {
                    Log.debug(
                        SignalConstants.LOG_TAG,
                        "$CLASS_NAME - Signal request failed with recoverable error ($result).Will retry sending the request (${request.url}) later."
                    )
                    false
                }
                else -> {
                    Log.warning(
                        SignalConstants.LOG_TAG,
                        "$CLASS_NAME - Signal request (${request.url}) failed with unrecoverable error ($result)."
                    )
                    true
                }
            }
            countDownLatch.countDown()
        }
        countDownLatch.await((request.connectTimeout + 1).toLong(), TimeUnit.SECONDS)
        return result
    }

    private fun buildNetworkRequest(entity: DataEntity): NetworkRequest? {
        val signalDataEntity = SignalHit.from(entity)
        if (signalDataEntity.url.isEmpty()) {
            Log.warning(
                SignalConstants.LOG_TAG,
                "$CLASS_NAME - Failed to build Signal request (URL is null)."
            )
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