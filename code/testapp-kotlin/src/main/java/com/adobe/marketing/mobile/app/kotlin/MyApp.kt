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
package com.adobe.marketing.mobile.app.kotlin

import android.app.Application
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.Identity
import com.adobe.marketing.mobile.Lifecycle
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.Signal
import com.adobe.marketing.mobile.app.kotlin.extension.PerfExtension

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MobileCore.setApplication(this)
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        // MobileCore.configureWithAppID("YOUR_APP_ID")
        val extensions = listOf(Identity.EXTENSION, Signal.EXTENSION, Lifecycle.EXTENSION, PerfExtension::class.java)
        MobileCore.registerExtensions(extensions) {

        }
    }

}
