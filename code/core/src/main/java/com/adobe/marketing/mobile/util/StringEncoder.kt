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

package com.adobe.marketing.mobile.util

import com.adobe.marketing.mobile.internal.CoreConstants
import com.adobe.marketing.mobile.services.Log
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object StringEncoder {
    private const val LOG_TAG = "StringEncoder"
    private const val LSB_8_MASK = 0xFF

    /**
     * Computes the sha2 hash for the string
     *
     * @param input the string for which sha2 hash is to be computed
     * @return sha2 hash result if the string is valid, null otherwise
     */
    @JvmStatic
    fun sha2hash(input: String?): String? {
        if (input.isNullOrEmpty()) {
            return null
        }

        try {
            val messageDigest = MessageDigest.getInstance("SHA-256").apply {
                update(input.toByteArray(StandardCharsets.UTF_8))
            }.digest()

            val sha2HexBuilder = StringBuilder()
            for (aMessageDigest in messageDigest) {
                val hexString = String.format("%02x", LSB_8_MASK and aMessageDigest.toInt())
                sha2HexBuilder.append(hexString)
            }
            return sha2HexBuilder.toString()
        } catch (e: NoSuchAlgorithmException) {
            Log.debug(
                CoreConstants.LOG_TAG, LOG_TAG,
                "Failed to create SHA-256 hash for input: '$input', Error: $e"
            )
        }

        return null
    }
}
