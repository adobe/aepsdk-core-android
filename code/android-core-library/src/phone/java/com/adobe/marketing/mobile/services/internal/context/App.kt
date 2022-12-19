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
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.services.AppContextService
import com.adobe.marketing.mobile.services.AppState
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * The [App] holds some variables related to the current application, including app [Context],
 * the current [Activity]. Also provides the method to get the orientation of the device, and the
 * methods to get and set icons for notifications.
 */

internal object App : AppContextService, Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    @Volatile
    private var application: WeakReference<Application>? = null

    @Volatile
    private var applicationContext: WeakReference<Context>? = null

    @Volatile
    private var currentActivity: WeakReference<Activity>? = null

    @Volatile
    private var appState = AppState.UNKNOWN

    private var onActivityResumed: SimpleCallback<Activity>? = null

    private var appStateListeners: ConcurrentLinkedQueue<AppStateListener> = ConcurrentLinkedQueue()

    // AppContextService overrides
    override fun setApplication(application: Application) {
        if (this.application?.get() != null) {
            return
        }

        this.application = WeakReference(application)
        setAppContext(application)
        registerActivityLifecycleCallbacks(application)
    }

    override fun getApplication(): Application? {
        return application?.get()
    }

    override fun getApplicationContext(): Context? {
        return applicationContext?.get()
    }

    override fun getCurrentActivity(): Activity? {
        return this.currentActivity?.get()
    }

    override fun getAppState(): AppState {
        return appState
    }

    // Android Lifecycle overrides
    override fun onActivityResumed(activity: Activity) {
        setAppState(AppState.FOREGROUND)
        onActivityResumed?.let {
            it.call(activity)
        }
        setCurrentActivity(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        // do nothing
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
            setAppState(AppState.BACKGROUND)
        }
    }

    // Internal methods called current from Core.
    @JvmName("setAppContext")
    internal fun setAppContext(appContext: Context?) {
        val context = appContext?.applicationContext
        if (context != null) {
            this.applicationContext = WeakReference(context)
        }
    }

    @JvmName("setCurrentActivity")
    internal fun setCurrentActivity(activity: Activity?) {
        this.currentActivity = if (activity != null) { WeakReference(activity) } else { null }
    }

    @JvmName("registerActivityResumedListener")
    internal fun registerActivityResumedListener(resumedListener: SimpleCallback<Activity>) {
        onActivityResumed = resumedListener
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

    // This method is used only for testing
    @VisibleForTesting
    @JvmName("resetInstance")
    internal fun resetInstance() {
        application?.get()?.let {
            unregisterActivityLifecycleCallbacks(it)
        }
        applicationContext = null
        currentActivity = null
        application = null

        appStateListeners = ConcurrentLinkedQueue()
        appState = AppState.UNKNOWN
    }

    private fun setAppState(state: AppState) {
        if (appState == state) {
            return
        }

        appState = state
        notifyAppStateListeners()
    }

    private fun notifyAppStateListeners() {
        for (listener in appStateListeners) {
            if (appState == AppState.FOREGROUND) {
                listener.onForeground()
            } else if (appState == AppState.BACKGROUND) {
                listener.onBackground()
            }
        }
    }

    /**
     * Registers `this` as the activity lifecycle callback for the `Application`.
     *
     * @param application       the [Application] of the app
     * @param onActivityResumed invoked when ActivityLifecycleCallbacks.onActivityResumed() is called
     */
    private fun registerActivityLifecycleCallbacks(
        application: Application
    ) {
        application.registerActivityLifecycleCallbacks(this)
        application.registerComponentCallbacks(this)
    }

    /**
     * Unregisters `this` as the activity lifecycle callback for the `Application`.
     *
     * @param application       the [Application] of the app
     */
    private fun unregisterActivityLifecycleCallbacks(
        application: Application
    ) {
        application.unregisterActivityLifecycleCallbacks(this)
        application.unregisterComponentCallbacks(this)
    }
}
