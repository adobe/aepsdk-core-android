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

package com.adobe.marketing.mobile.services.uri

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceConstants
import com.adobe.marketing.mobile.services.ServiceProvider

/**
 * Represents a component capable of opening URIs.
 */
internal class UriService : UriOpening {
    internal companion object {
        private const val LOG_TAG = "UriService"
    }

    private var uriHandler: URIHandler? = null

    override fun openUri(uri: String): Boolean {
        if (uri.isNullOrBlank()) {
            Log.debug(ServiceConstants.LOG_TAG, LOG_TAG, "Cannot open URI. URI is empty.")
            return false
        }

        val currentActivity: Activity = ServiceProvider.getInstance().appContextService.currentActivity
            ?: kotlin.run {
                Log.debug(ServiceConstants.LOG_TAG, LOG_TAG, "Cannot open URI: $uri. No current activity found.")
                return false
            }

        val configuredDestination: Intent? = uriHandler?.getURIDestination(uri)

        return try {
            val intent = configuredDestination ?: Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(uri) }
            currentActivity.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.debug(ServiceConstants.LOG_TAG, LOG_TAG, "Failed to open URI: $uri")
            false
        }
    }

    override fun setUriHandler(handler: URIHandler) {
        uriHandler = handler
    }
}
