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

package com.adobe.marketing.mobile.internal.util

/**
 * Convert map to a decimal FNV1a 32-bit hash. If a mask is provided, only use keys in the provided mask and alphabetize their order.
 *
 * @param map the [Map] to be converted to FNV1a 32-bit hash
 * @param masks contain keys to be hashed.
 * @return the decimal FNV1a 32-bit hash.
 */
internal fun convertMapToFnv1aHash(map: Map<String, Any?>?, masks: Array<String>?): Long {
    return map?.fnv1a32(masks) ?: 0L
}
