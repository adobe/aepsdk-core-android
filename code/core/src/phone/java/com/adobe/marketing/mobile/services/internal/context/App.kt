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
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.services.AppContextService
import com.adobe.marketing.mobile.services.AppState
import java.lang.ref.WeakReference

/**
 * The [App] holds some variables related to the current application, including app [Context],
 * the current [Activity].
 */
internal object App : AppContextService {

    @Volatile
    private var application: WeakReference<Application>? = null

    @Volatile
    private var applicationContext: WeakReference<Context>? = null

    @Volatile
    private var connectivityManager: ConnectivityManager? = null

    private var activityTracker = ActivityTracker()
    private var appStateTracker = AppStateTracker()

    // Activity lifecycle callbacks registered with the Android Application instance
    private var systemActivityLifecycleCallbacks: InternalActivityLifecycleCallbacks? = null

    /**
     * Sets the application context and registers the necessary lifecycle and component callbacks.
     * @param application The application instance to be set.
     */
    override fun setApplication(application: Application) {
        if (this.application?.get() != null) return

        this.application = WeakReference(application)
        application.applicationContext?.let {
            this.applicationContext = WeakReference(it)
        }
        this.connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        val listeners = listOf(
            activityTracker,
            appStateTracker
        )
        systemActivityLifecycleCallbacks = InternalActivityLifecycleCallbacks(listeners)
        application.registerActivityLifecycleCallbacks(systemActivityLifecycleCallbacks)
        application.registerComponentCallbacks(systemActivityLifecycleCallbacks)
    }

    /**
     * Gets the application instance that was previously set.
     * @return The application instance or null if it was not set.
     */
    override fun getApplication(): Application? = application?.get()

    /**
     * Gets the application context that was previously set.
     * @return The application context or null if it was not set.
     */
    override fun getApplicationContext(): Context? = applicationContext?.get()

    /**
     * Gets the current activity.
     * @return The current [Activity] or null if no activity is currently tracked.
     */
    override fun getCurrentActivity(): Activity? = activityTracker.currentActivity?.get()

    /**
     * Gets the current app state (e.g., foreground or background).
     * @return The current [AppState] of the app.
     */
    override fun getAppState(): AppState = appStateTracker.appState

    /**
     * Gets the connectivity manager for managing network connections.
     * @return The [ConnectivityManager] instance or null if not available.
     */
    override fun getConnectivityManager(): ConnectivityManager? = connectivityManager

    /**
     * Registers activity lifecycle callbacks to receive notifications of activity state changes.
     * @param callback The [ActivityLifecycleCallbacks] to be registered.
     */
    fun registerActivityLifecycleCallbacks(callback: ActivityLifecycleCallbacks) {
        activityTracker.activityCallbacks.add(callback)
    }

    @VisibleForTesting
    fun reset() {
        application?.get()?.let {
            it.unregisterActivityLifecycleCallbacks(systemActivityLifecycleCallbacks)
            it.unregisterComponentCallbacks(systemActivityLifecycleCallbacks)
        }
        application = null
        applicationContext = null
        systemActivityLifecycleCallbacks = null
        connectivityManager = null

        activityTracker = ActivityTracker()
        appStateTracker = AppStateTracker()
    }

    /**
     * Interface for listening to activity lifecycle events.
     * This is used by core components to observe and respond to activity lifecycle changes
     * (e.g., activity resumed, paused, etc.) within the application.
     */
    interface ActivityLifecycleCallbacks {
        fun onActivityResumed(activity: Activity) {}
        fun onActivityPaused(activity: Activity) {}
    }

    /**
     * Internal interface used by various components inside App class.
     */
    private interface InternalActivityLifecycleListener : ActivityLifecycleCallbacks {
        fun onTrimMemory(level: Int) {}
    }

    /**
     * Tracks the app state (e.g., FOREGROUND or BACKGROUND) based on lifecycle events.
     * This value is currently used by Analytics extension.
     */
    class AppStateTracker : InternalActivityLifecycleListener {
        @Volatile
        var appState = AppState.UNKNOWN

        override fun onActivityResumed(activity: Activity) {
            appState = AppState.FOREGROUND
        }

        override fun onTrimMemory(level: Int) {
            // https://developer.android.com/reference/android/content/ComponentCallbacks2.html#TRIM_MEMORY_UI_HIDDEN
            if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
                appState = AppState.BACKGROUND
            }
        }
    }

    /**
     * Tracks the current activity and notifies listeners when an activity resumes/pauses.
     */
    class ActivityTracker : InternalActivityLifecycleListener {
        @Volatile
        var currentActivity: WeakReference<Activity>? = null

        @Volatile
        var activityCallbacks: MutableList<ActivityLifecycleCallbacks> = mutableListOf()

        override fun onActivityResumed(activity: Activity) {
            currentActivity = WeakReference(activity)
            activityCallbacks.forEach {
                it.onActivityResumed(activity)
            }
        }

        override fun onActivityPaused(activity: Activity) {
            activityCallbacks.forEach {
                it.onActivityPaused(activity)
            }
        }
    }

    private class InternalActivityLifecycleCallbacks(private val listeners: List<InternalActivityLifecycleListener>) : Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
        override fun onActivityResumed(activity: Activity) {
            listeners.forEach { it.onActivityResumed(activity) }
        }

        override fun onActivityPaused(activity: Activity) {
            listeners.forEach { it.onActivityPaused(activity) }
        }

        override fun onTrimMemory(level: Int) {
            listeners.forEach { it.onTrimMemory(level) }
        }

        // no-op
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
        override fun onConfigurationChanged(newConfig: Configuration) {}
        override fun onLowMemory() {}
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
    }
}
