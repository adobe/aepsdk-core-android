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
package com.adobe.marketing.mobile.core.testapp

import android.app.Application
import android.util.Log
import com.adobe.marketing.mobile.InitOptions
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        val options = InitOptions()
        MobileCore.initialize(this, options) {
            Log.i("MyApp", "AEP SDK initialized")
        }
        // The test app uses bundled config. Uncomment this and change the app ID for testing the mobile tags property.
        // MobileCore.initialize(this, "YOUR_APP_ID")


    }

}
