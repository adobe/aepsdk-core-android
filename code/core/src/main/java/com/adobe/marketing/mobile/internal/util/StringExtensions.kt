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

package com.adobe.marketing.mobile.internal.util

/**
 * Computes the signed 2's complement FNV-1a 32-bit hash for the given string input.
 *
 * <p>https://en.wikipedia.org/wiki/Fowler%E2%80%93Noll%E2%80%93Vo_hash_function Online
 * validator - https://md5calc.com/hash/fnv1a32?str=
 *
 * @param input The string to be hashed.
 * @return A 32-bit signed integer representing the FNV-1a hash of the input string.
 */
private fun computeFnv1aHash(input: String?): Int {
    val ALL_BITS_ENABLED = 0xFF
    val PRIME = 0x1000193 // 16777619 as hex
    val OFFSET = -0x7ee3623b // 2166136261 as hex

    if (input == null) {
        return 0
    }

    var hash = if (input.trim().isEmpty()) 0 else OFFSET
    val bytes = input.toByteArray()

    for (aByte in bytes) {
        hash = hash xor (aByte.toInt() and ALL_BITS_ENABLED)
        hash *= PRIME
    }

    return hash
}

/**
 * Extension function that computes the decimal FNV-1a 32-bit hash for a string.
 *
 * @return The decimal representation of the FNV-1a 32-bit hash.
 */
internal fun String?.fnv1a32(): Long {
    return computeFnv1aHash(this).toUInt().toLong()
}
