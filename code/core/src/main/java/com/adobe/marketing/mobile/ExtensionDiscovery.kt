/*
  Copyright 2025 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.adobe.marketing.mobile.internal.CoreConstants
import com.adobe.marketing.mobile.services.Log

/**
 * Responsible for discovering and retrieving a list of [Extension] classes
 * that are bundled with the app.
 */
internal class ExtensionDiscovery {

    companion object {
        private const val SERVICE_NAME = "com.adobe.marketing.mobile.ExtensionDiscoveryService"
        private const val LOG_TAG = "ExtensionDiscovery"
    }

    fun getExtensions(context: Context): List<Class<out Extension>> {
        val extensions = mutableSetOf<Class<out Extension>>()

        try {
            val serviceInfo = context.packageManager.getServiceInfo(
                ComponentName(context, SERVICE_NAME), PackageManager.GET_META_DATA
            )

            serviceInfo.metaData?.let { metadata ->
                metadata.keySet().forEach { key ->
                    try {
                        val extensionClass = Class.forName(key) as? Class<out Extension>
                        if (extensionClass != null) {
                            extensions.add(extensionClass)
                            Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Discovered extension: $key bundled with the app.")
                        } else {
                            Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Class $key is not a valid Extension.")
                        }
                    } catch (e: ClassNotFoundException) {
                        Log.error(CoreConstants.LOG_TAG, LOG_TAG, "Failed to load extension class $key - ${e.message}.")
                    }
                }
            } ?: Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "No metadata found for service $SERVICE_NAME.")
        } catch (e: PackageManager.NameNotFoundException) {
            Log.warning(CoreConstants.LOG_TAG, LOG_TAG, "Service $SERVICE_NAME not found.")
        } catch (e: Exception) {
            Log.warning(CoreConstants.LOG_TAG, LOG_TAG, "Error $e during extension discovery.")
        }

        Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Found ${extensions.size} extensions.")
        return extensions.toList()
    }
}
