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

import android.app.Activity
import android.app.Application
import android.content.Context

class MockAppContextService() : AppContextService {
    var application_: Application? = null
    var currentActivity_: Activity? = null
    var appContext_: Context? = null
    var appState_: AppState = AppState.UNKNOWN

    override fun setApplication(application: Application) {
        application_ = application
    }

    override fun getApplication(): Application? {
        return application_
    }

    override fun getCurrentActivity(): Activity? {
        return currentActivity_
    }

    override fun getApplicationContext(): Context? {
        return appContext_
    }

    override fun getAppState(): AppState {
        return appState_
    }
}