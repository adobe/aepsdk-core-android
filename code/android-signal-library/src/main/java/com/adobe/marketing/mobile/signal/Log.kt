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

import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.LoggingMode

//TODO: TBD- will remove this after we add the similar logging APIs to Core.
internal object Log {
    @JvmStatic
    fun trace(source: String?, format: String?, vararg params: Any?) {
        MobileCore.log(LoggingMode.VERBOSE, source, String.format(format!!, *params))
    }

    @JvmStatic
    fun debug(source: String?, format: String?, vararg params: Any?) {
        MobileCore.log(LoggingMode.DEBUG, source, String.format(format!!, *params))
    }

    @JvmStatic
    fun warning(source: String?, format: String?, vararg params: Any?) {
        MobileCore.log(LoggingMode.WARNING, source, String.format(format!!, *params))
    }

    fun error(source: String?, format: String?, vararg params: Any?) {
        MobileCore.log(LoggingMode.ERROR, source, String.format(format!!, *params))
    }
}