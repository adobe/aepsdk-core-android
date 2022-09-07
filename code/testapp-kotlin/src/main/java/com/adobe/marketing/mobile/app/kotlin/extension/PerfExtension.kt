package com.adobe.marketing.mobile.app.kotlin.extension

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