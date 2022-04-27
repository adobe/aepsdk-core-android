package com.adobe.marketing.mobile.launch.rulesengine

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.rulesengine.RulesEngine

class LaunchRulesEngine {
    private val rulesEngineMutex = Any()
    var rules: List<LaunchRule>? = null
    val rulesEngine:RulesEngine<LaunchRule>
    constructor()
    init {
        rulesEngine = RulesEngine<LaunchRule>()
    }


    fun replaceRules(rules: List<LaunchRule>) {
        synchronized(rulesEngineMutex) {
            this.rules = rules
        }
    }

    fun process(event: Event): Event {
        synchronized(rulesEngineMutex) {
            rules?.forEach {

            }
        }
        return event
    }
}