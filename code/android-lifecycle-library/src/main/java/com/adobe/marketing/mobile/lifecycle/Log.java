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
package com.adobe.marketing.mobile.lifecycle;

import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;

// Todo remove this class once it's exposed from core
class Log {
    public static void trace(String source, String message) {
        MobileCore.log(LoggingMode.VERBOSE, source, message);
    }
    public static void debug(String source, String message) {
        MobileCore.log(LoggingMode.DEBUG, source, message);
    }
    public static void warning(String source, String message) {
        MobileCore.log(LoggingMode.WARNING, source, message);
    }
    public static void error(String source, String message) {
        MobileCore.log(LoggingMode.ERROR, source, message);
    }
}
