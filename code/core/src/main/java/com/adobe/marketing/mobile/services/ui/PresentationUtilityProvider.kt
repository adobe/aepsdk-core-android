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

package com.adobe.marketing.mobile.services.ui

import android.app.Activity
import android.app.Application
import java.io.InputStream

/**
 * The PresentationUtilityProvider is used to provide the necessary utilities for the UI SDK to function
 */
interface PresentationUtilityProvider {
    /**
     * Retrieves the [Application] instance for the host application.
     *
     * @return the [Application] instance for the host application
     */
    fun getApplication(): Application?

    /**
     * Retrieves the current activity being shown to the user.
     *
     * @return the current activity being shown to the user if one exists, null otherwise
     */
    fun getCurrentActivity(): Activity?

    /**
     * Retrieves any cached content for the given cache name and key.
     * @param cacheName the name of the cache to retrieve content from
     * @param key the key of the content to retrieve
     * @return an [InputStream] containing the cached content if it exists, null otherwise.
     */
    fun getCachedContent(cacheName: String, key: String): InputStream?

    /**
     * Opens the given [uri].
     * @param uri the URI to open
     * @return true if the URI was opened successfully, false otherwise.
     */
    fun openUri(uri: String): Boolean
}
