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

package com.adobe.marketing.mobile.signal.internal

import com.adobe.marketing.mobile.services.DataEntity
import org.json.JSONObject

internal class SignalHit(
    val url: String,
    val body: String,
    val contentType: String,
    private val timeout: Int
) {
    internal fun toDataEntity(): DataEntity {
        val map = mapOf<String, Any>(
            URL to url,
            BODY to body,
            CONTENT_TYPE to contentType,
            TIME_OUT to timeout
        )
        val json = try {
            JSONObject(map).toString()
        } catch (e: Exception) {
            EMPTY_JSON
        }
        return DataEntity(json)
    }

    companion object {
        private const val URL = "url"
        private const val BODY = "body"
        private const val CONTENT_TYPE = "contentType"
        private const val TIME_OUT = "timeout"
        private const val EMPTY_JSON = ""

        internal fun from(dataEntity: DataEntity): SignalHit {
            val json = dataEntity.data ?: EMPTY_JSON
            val jsonObject = try {
                JSONObject(json)
            } catch (e: Exception) {
                JSONObject()
            }
            return SignalHit(
                jsonObject.optString(URL),
                jsonObject.optString(BODY),
                jsonObject.optString(CONTENT_TYPE),
                jsonObject.optInt(TIME_OUT, 0)
            )
        }
    }

    @JvmSynthetic
    internal fun timeout(default: Int): Int {
        return if (timeout > 0) timeout else default
    }
}
