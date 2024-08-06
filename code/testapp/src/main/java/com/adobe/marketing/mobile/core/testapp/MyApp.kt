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
import androidx.core.os.UserManagerCompat
import com.adobe.marketing.mobile.Identity
import com.adobe.marketing.mobile.Lifecycle
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.Signal
import com.adobe.marketing.mobile.core.testapp.extension.PerfExtension
import com.adobe.marketing.mobile.internal.eventhub.Tenant

class MyApp : Application() {
    private val LAUNCH_ENVIRONMENT_FILE_ID = "94f571f308d5/bc09a100649b/launch-6df8e3eea690-development"

    companion object {
        val partnerTenant = Tenant.Id("partner")
    }
    override fun onCreate() {
        super.onCreate()
        Log.i("MyApp", "Application.onCreate() - start to initialize Adobe SDK. UserManagerCompat.isUserUnlocked(): ${UserManagerCompat.isUserUnlocked(this)}")
        MobileCore.setApplication(this)
        MobileCore.setLogLevel(LoggingMode.VERBOSE)

        // The test app uses bundled config. Uncomment this and change the app ID for testing the mobile tags property.
        MobileCore.configureWithAppID(LAUNCH_ENVIRONMENT_FILE_ID)
        val extensions = listOf(
            Identity.EXTENSION,
            Signal.EXTENSION,
            Lifecycle.EXTENSION,
            PerfExtension::class.java
        )
        // Default tenant
        MobileCore.registerExtensions(extensions) {}

//         Initializing a new tenant. Only extensions which are tenant aware will be initialized for this instance.

        val partnerLaunchEnvironmentID = "94f571f308d5/39273f51e930/launch-00ac4ce72151-development"
//        MobileCore.setApplication(this, partnerTenant)
        MobileCore.registerExtensions(extensions, partnerTenant) {
            MobileCore.configureWithAppID(partnerLaunchEnvironmentID, partnerTenant)
        }

    }

}
