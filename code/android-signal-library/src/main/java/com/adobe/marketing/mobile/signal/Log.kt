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

//TODO: TBD- remove this class if we add the similar logging APIs to Core.
internal object Log {
    @JvmStatic
    fun trace(source: String?, message: String?) {
        MobileCore.log(LoggingMode.VERBOSE, source, message)
    }

    @JvmStatic
    fun debug(source: String?, message: String?) {
        MobileCore.log(LoggingMode.DEBUG, source, message)
    }

    @JvmStatic
    fun warning(source: String?, message: String?) {
        MobileCore.log(LoggingMode.WARNING, source, message)
    }

    @JvmStatic
    fun error(source: String?, message: String?) {
        MobileCore.log(LoggingMode.ERROR, source, message)
    }
}