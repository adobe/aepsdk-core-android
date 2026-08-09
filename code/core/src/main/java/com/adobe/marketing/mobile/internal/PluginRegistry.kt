/*
  Copyright 2026 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/
package com.adobe.marketing.mobile.internal

import com.adobe.marketing.mobile.plugin.IAepPlugin
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Process-wide holder for registered [IAepPlugin] implementations. Not public API - access goes
 * through the [com.adobe.marketing.mobile.MobileCore] facade (`addPlugins` / `getPlugin`).
 *
 * Thread-safe: registration and resolution may race with inbound work such as push handling.
 */
object PluginRegistry {

    private val plugins = CopyOnWriteArrayList<IAepPlugin>()

    /** Registers a plugin. Idempotent per instance; registration order is preserved. */
    fun addPlugin(plugin: IAepPlugin) {
        plugins.addIfAbsent(plugin)
    }

    /**
     * @return the first registered plugin assignable to [type], or `null` when none is registered.
     */
    fun <T : IAepPlugin> getPlugin(type: Class<T>): T? {
        for (plugin in plugins) {
            if (type.isInstance(plugin)) {
                return type.cast(plugin)
            }
        }
        return null
    }
}
