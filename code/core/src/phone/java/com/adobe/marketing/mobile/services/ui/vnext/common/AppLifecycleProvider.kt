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

package com.adobe.marketing.mobile.services.ui.vnext.common

import android.app.Activity
import android.app.Application
import android.os.Bundle

internal class AppLifecycleProvider private constructor() {

    companion object {
        val INSTANCE  by lazy { AppLifecycleProvider() }
    }

    internal interface AppLifecycleListener {
        fun onActivityResumed(activity: Activity)
        fun onActivityDestroyed(activity: Activity)
    }

    private val listeners: MutableSet<AppLifecycleListener> = mutableSetOf()
    private var started = false

    @Synchronized
    internal fun start(app: Application) {
        if(started) {
            return
        }
        started = true
        app.registerActivityLifecycleCallbacks(InternalAppLifecycleListener(this))

    }

    internal fun registerListener(listener: AppLifecycleListener) {
        listeners.add(listener)
    }

    internal fun unregisterListener(listener: AppLifecycleListener) {
        listeners.remove(listener)
    }

    private class InternalAppLifecycleListener(private val appLifecycleProvider: AppLifecycleProvider) :
        Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            appLifecycleProvider.listeners.forEach { listener -> listener.onActivityResumed(activity) }
        }

        override fun onActivityDestroyed(activity: Activity) {
            appLifecycleProvider.listeners.forEach { listener ->
                listener.onActivityDestroyed(
                    activity
                )
            }
        }

        // No op methods
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    }
}
