/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util

import android.app.Activity
import android.app.Application
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import java.io.InputStream

/**
 * Default implementation of [PresentationUtilityProvider] that uses the [ServiceProvider]
 * to relay the calls to the appropriate services.
 */
class DefaultPresentationUtilityProvider : PresentationUtilityProvider {
    override fun getApplication(): Application? {
        return ServiceProvider.getInstance().appContextService.application
    }

    override fun getCurrentActivity(): Activity? {
        return ServiceProvider.getInstance().appContextService.currentActivity
    }

    override fun getCachedContent(cacheName: String, key: String): InputStream? {
        val cacheResult = ServiceProvider.getInstance().cacheService.get(cacheName, key)
        return cacheResult?.data
    }

    override fun openUri(uri: String): Boolean {
        return ServiceProvider.getInstance().uriService.openUri(uri)
    }
}
