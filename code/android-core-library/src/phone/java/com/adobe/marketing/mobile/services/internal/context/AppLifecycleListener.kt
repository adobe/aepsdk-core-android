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
package com.adobe.marketing.mobile.services.internal.context

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
import android.content.res.Configuration
import android.os.Bundle
import com.adobe.marketing.mobile.CoreConstants
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.internal.context.App.setCurrentActivity
import java.util.ArrayList

/**
 * Implement [Application.ActivityLifecycleCallbacks] to detect whether the app is in foreground.
 *
 *
 * The order of lifecycle callbacks when [Activity] A starts `Activity` B:
 *
 *  1. Activity A's onPause() method executes
 *  1. Activity B's onCreate(), onStart(), and onResume() methods execute in sequence. (Activity B now has user focus.)
 *  1. Then, if Activity A is no longer visible on screen, its onStop() method executes.
 *
 * So based on this sequence, we start a timer with `BACKGROUND_TRANSITION_DELAY_MILLIS` when onPause() of Activity A is called, which will set
 * the app state to background. So if it is a true app close, then we will have the correct app state
 * after 500ms.
 * And if there is Activity B gets started right after Activity A paused, then we will cancel the timer,
 * so the app state will remain foreground.
 */
internal class AppLifecycleListener private constructor() :
    Application.ActivityLifecycleCallbacks,
    ComponentCallbacks2 {
    /**
     * Gets the current app state.
     *
     * @return the current app state
     */
    @Volatile
    var appState = AppState.UNKNOWN
        private set
    private val appStateListeners: MutableList<AppStateListener>
    private var onActivityResumed: SimpleCallback<Activity>? = null

    /**
     * Registers `this` as the activity lifecycle callback for the `Application`.
     *
     * @param application       the [Application] of the app
     * @param onActivityResumed invoked when ActivityLifecycleCallbacks.onActivityResumed() is called
     */
    fun registerActivityLifecycleCallbacks(
        application: Application?,
        onActivityResumed: SimpleCallback<Activity>?
    ) {
        if (application != null && !registered) {
            application.registerActivityLifecycleCallbacks(this)
            application.registerComponentCallbacks(this)
            registered = true
            this.onActivityResumed = onActivityResumed
        } else {
            Log.error(
                CoreConstants.CORE_EXTENSION_NAME,
                LOG_TAG,
                "The given Application instance is null."
            )
        }
    }

    /**
     * Registers a `AppStateListener` which will gets called when the app state changes.
     *
     * @param listener the [AppStateListener] to receive app state change events
     */
    fun registerListener(listener: AppStateListener) {
        appStateListeners.add(listener)
    }

    /**
     * Unregisters a `AppStateListener`.
     *
     * @param listener the [AppStateListener] to unregister
     */
    fun unregisterListener(listener: AppStateListener) {
        appStateListeners.remove(listener)
    }

    override fun onActivityResumed(activity: Activity) {
        setForegroundIfNeeded()
        if (onActivityResumed != null) {
            onActivityResumed!!.call(activity)
        }
        setCurrentActivity(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        // do nothing
    }

    private fun notifyListeners() {
        for (listener in appStateListeners) {
            if (appState == AppState.FOREGROUND) {
                listener.onForeground()
            } else if (appState == AppState.BACKGROUND) {
                listener.onBackground()
            }
        }
    }

    private fun setForegroundIfNeeded() {
        if (appState == AppState.FOREGROUND) {
            return
        }

        appState = AppState.FOREGROUND
        notifyListeners()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // do nothing
    }

    override fun onActivityStarted(activity: Activity) {
        // do nothing
    }

    override fun onActivityStopped(activity: Activity) {
        // do nothing
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // do nothing
    }

    override fun onActivityDestroyed(activity: Activity) {
        // do nothing
    }

    override fun onConfigurationChanged(paramConfiguration: Configuration) {
        // do nothing
    }

    override fun onLowMemory() {
        // do nothing
    }

    override fun onTrimMemory(level: Int) {
        // https://developer.android.com/reference/android/content/ComponentCallbacks2.html#TRIM_MEMORY_UI_HIDDEN
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            if (appState == AppState.BACKGROUND) {
                return
            }

            appState = AppState.BACKGROUND
            notifyListeners()
        }
    }

    companion object {
        private const val LOG_TAG = "AppLifecycleListener"

        /**
         * Singleton. Get the [AppLifecycleListener] instance.
         *
         * @return [AppLifecycleListener] Singleton
         */
        @JvmStatic
        @get:Synchronized
        var instance: AppLifecycleListener? = null
            get() {
                if (field == null) {
                    field = AppLifecycleListener()
                }
                return field
            }
            private set
        private var registered = false
    }

    /**
     * Private constructor.
     */
    init {
        appStateListeners = ArrayList()
    }
}
