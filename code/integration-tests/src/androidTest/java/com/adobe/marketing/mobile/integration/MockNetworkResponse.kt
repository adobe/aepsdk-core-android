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

package com.adobe.marketing.mobile.integration

import com.adobe.marketing.mobile.services.HttpConnecting
import java.io.InputStream

internal class MockNetworkResponse(
    private val code: Int,
    private val message: String,
    private val headers: Map<String, String>,
    private val body: InputStream,
    val urlMonitor: (String) -> Unit
) : HttpConnecting {

    override fun getInputStream(): InputStream {
        return body
    }

    override fun getErrorStream(): InputStream? {
        return null
    }

    override fun getResponseCode(): Int {
        return code
    }

    override fun getResponseMessage(): String {
        return message
    }

    override fun getResponsePropertyValue(responsePropertyKey: String): String? {
        return headers[responsePropertyKey]
    }

    override fun close() {
        body.close()
    }
}
