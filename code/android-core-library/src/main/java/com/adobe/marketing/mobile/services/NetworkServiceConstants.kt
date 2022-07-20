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

package com.adobe.marketing.mobile.services

object NetworkServiceConstants {
    val RECOVERABLE_ERROR_CODES = intArrayOf(408, 504, 503)
    val HTTP_SUCCESS_CODES = (200..299).toList().toIntArray()

    object Headers {
        const val IF_MODIFIED_SINCE = "If-Modified-Since"
        const val IF_NONE_MATCH = "If-None-Match"
        const val LAST_MODIFIED = "Last-Modified"
        const val ETAG = "Etag"
        const val CONTENT_TYPE = "Content-Type"
    }

    object HeaderValues {
        const val CONTENT_TYPE_URL_ENCODED = "application/x-www-form-urlencoded"
    }
}
