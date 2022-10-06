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
package com.adobe.marketing.mobile.integration

import com.adobe.marketing.mobile.*

internal typealias ConfigurationMonitor = (firstValidConfiguration: Map<String, Any>) -> Unit

internal class MonitorExtension(extensionApi: ExtensionApi) : Extension(extensionApi) {
    companion object {
        private var configurationMonitor: ConfigurationMonitor? = null
        internal fun configurationAwareness(callback: ConfigurationMonitor) {
            configurationMonitor = callback
        }
    }

    override fun getName(): String {
        return "MonitorExtension"
    }

    override fun onRegistered() {
        api.registerEventListener(EventType.WILDCARD, EventSource.WILDCARD) { event ->
            val result = api.getSharedState(
                "com.adobe.module.configuration",
                event,
                false,
                SharedStateResolution.LAST_SET
            )
            val configuration = result?.value
            configuration?.let {
                configurationMonitor?.let { it(configuration) }
            }
        }
    }
}