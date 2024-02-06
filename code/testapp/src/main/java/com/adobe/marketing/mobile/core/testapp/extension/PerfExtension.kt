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
package com.adobe.marketing.mobile.core.testapp.extension

import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.services.Log

class PerfExtension(extensionApi: ExtensionApi) : Extension(extensionApi) {
    override fun getName(): String {
        return "PerfExtension"
    }

    companion object {
        private var count = 0
        private var monitor: RulesConsequenceMonitor? = null

        fun recount(rulesConsequenceMonitor: RulesConsequenceMonitor) {
            synchronized(this) {
                monitor = rulesConsequenceMonitor
                count = 0
            }
        }

        fun rulesConsequenceEventCaught() {
            synchronized(this) {
                count++
                monitor?.let { it(count) }
            }
        }

        fun consequenceCount(): Int {
            synchronized(this) {
                return count
            }
        }
    }


    override fun onRegistered() {
        super.onRegistered()
        api.registerEventListener(EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT) { event ->
            if (event.name.trim() == "Rules Consequence Event") {
                rulesConsequenceEventCaught()
                Log.error("x", "a", "================= ${consequenceCount()}")
            }
        }
    }
}

typealias RulesConsequenceMonitor = (count: Int) -> Unit