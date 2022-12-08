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

package com.adobe.marketing.mobile.identity

import com.adobe.marketing.mobile.services.HttpConnecting
import java.io.InputStream

internal open class DefaultHttpConnecting : HttpConnecting {
    override fun getInputStream(): InputStream? {
        return null
    }

    override fun getErrorStream(): InputStream? {
        return null
    }

    override fun getResponseCode(): Int {
        return 0
    }

    override fun getResponseMessage(): String {
        return ""
    }

    override fun getResponsePropertyValue(responsePropertyKey: String?): String {
        return ""
    }

    override fun close() {}
}
