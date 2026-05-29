/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.internal.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * Utility class to attach and detach an [ActivityCompatOwner] to an activity.
 * Exists to mock and make tests easier to write for the calling component.
 * This component has to be manually tested due the internal threading and lifecycle management
 * of the Android SDK.
 */
internal class ActivityCompatOwnerUtils {

    /**
     * Attaches an [ActivityCompatOwner] to the given activity if it does not already have a lifecycle owner.
     * @param activityToAttach the activity to attach the [ActivityCompatOwner] to
     */
    internal fun attachActivityCompatOwner(activityToAttach: Activity) {
        val decorView = activityToAttach.window.decorView

        if (decorView.findViewTreeLifecycleOwner() != null) {
            // If the activity already has a lifecycle owner, then we don't need to attach a new one
            return
        }

        val proxyLifeCycleOwner = ActivityCompatOwner()
        proxyLifeCycleOwner.onCreate()
        // Bind the proxy's lifecycle to the host activity. This advances the proxy from CREATED
        // to RESUMED (the host's current state at attach time, since presentables are only
        // attached to a foregrounded activity) and starts mirroring future lifecycle transitions
        // of the host. Without this, the proxy lifecycle would remain at CREATED, which leaves
        // Compose's WindowRecomposer suspended (no recompositions / animations / pointer-driven
        // UI updates after the initial draw) and disables every BackHandler callback registered
        // against the proxy LifecycleOwner.
        proxyLifeCycleOwner.bindToHostLifecycle(activityToAttach)
        proxyLifeCycleOwner.attachToView(decorView)
    }

    /**
     * Detaches the [ActivityCompatOwner] from the given activity if it has one.
     * @param activityToDetach the activity to detach the [ActivityCompatOwner] from
     */
    internal fun detachActivityCompatOwner(activityToDetach: Activity) {
        val decorView = activityToDetach.window.decorView

        // If the activity's lifecycle owner is not a ActivityCompatOwner, then there is nothing
        // to detach
        val lifecycleOwner = decorView.findViewTreeLifecycleOwner()
        if (lifecycleOwner !is ActivityCompatOwner) {
            return
        }

        lifecycleOwner.detachFromView(decorView)
        lifecycleOwner.onDestroy()
    }
}

/**
 * A proxy lifecycle owner that is used to attach to an activity's view tree to which a compose view
 * from the SDK is being attached.
 * This is required to provide a lifecycle owner to the view tree of an activity that inherits
 * from android.app.Activity which does not provide a lifecycle owner by default. Not doing so will
 * result in a crash when trying to attach a compose view. Android recommends using inheriting from
 * AppCompatActivity or similar which provides a lifecycle owner by default (irrespective of whether
 * compose is being used or not). This is a best effort to provide a lifecycle owner when this is not
 * the case.
 */
internal class ActivityCompatOwner :
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    OnBackPressedDispatcherOwner {

    // LifecycleOwner methods
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    // ViewModelStore methods
    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore
        get() = store

    // SavedStateRegistry methods
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    // OnBackPressedDispatcherOwner methods
    private val dispatcher = OnBackPressedDispatcher {}
    override val onBackPressedDispatcher: OnBackPressedDispatcher
        get() = dispatcher

    // Reference to the host Activity whose lifecycle this proxy mirrors. Held only while bound
    // so that [onDestroy] can unregister the lifecycle callbacks. Cleared on detach to avoid
    // leaking the Activity.
    private var hostActivity: Activity? = null

    // ActivityLifecycleCallbacks instance used to mirror the host's lifecycle transitions onto
    // this proxy. Non-null only between [bindToHostLifecycle] and [onDestroy].
    private var hostLifecycleCallbacks: Application.ActivityLifecycleCallbacks? = null

    /**
     * Trigger the ON_CREATE lifecycle event for this [ActivityCompatOwner].
     */
    internal fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    /**
     * Binds this proxy's lifecycle to [activity]. Advances the proxy through ON_START and
     * ON_RESUME (the host's current state at attach time) and registers an
     * [Application.ActivityLifecycleCallbacks] listener so that subsequent lifecycle
     * transitions of the host activity are mirrored onto this proxy.
     *
     * Must be called on the main thread, after [onCreate] and before [attachToView].
     *
     * @param activity the host activity whose lifecycle this proxy should mirror
     */
    internal fun bindToHostLifecycle(activity: Activity) {
        hostActivity = activity
        // Presentables are only attached to a foregrounded activity, so the host is at RESUMED
        // by contract at the time of attach. Advance the proxy to match.
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val callbacks = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                if (activity === hostActivity) {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
                }
            }

            override fun onActivityResumed(activity: Activity) {
                if (activity === hostActivity) {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
                }
            }

            override fun onActivityPaused(activity: Activity) {
                if (activity === hostActivity) {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                }
            }

            override fun onActivityStopped(activity: Activity) {
                if (activity === hostActivity) {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                }
            }

            // Unused — proxy ON_CREATE is driven explicitly by [onCreate], and ON_DESTROY by
            // [onDestroy] (called from ActivityCompatOwnerUtils.detachActivityCompatOwner). The
            // host activity being destroyed independently is already handled by AEPPresentable's
            // own activity-lifecycle wiring, which calls detach on this proxy.
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        }
        activity.application?.registerActivityLifecycleCallbacks(callbacks)
        hostLifecycleCallbacks = callbacks
    }

    /**
     * Trigger the ON_DESTROY lifecycle event for this [ActivityCompatOwner]. Also unregisters
     * the host lifecycle callbacks (if any) and releases the reference to the host activity.
     *
     * [LifecycleRegistry.handleLifecycleEvent] with ON_DESTROY auto-traverses any intermediate
     * states (e.g. RESUMED → STARTED → CREATED → DESTROYED), dispatching ON_PAUSE / ON_STOP /
     * ON_DESTROY to observers in order, so we do not emit those events manually.
     */
    internal fun onDestroy() {
        hostLifecycleCallbacks?.let { callbacks ->
            hostActivity?.application?.unregisterActivityLifecycleCallbacks(callbacks)
        }
        hostLifecycleCallbacks = null
        hostActivity = null
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }

    /**
     * Attaches this [ActivityCompatOwner] to the given view.
     * @param view the view to attach this [ActivityCompatOwner] to
     */
    internal fun attachToView(view: View?) {
        view?.apply {
            setViewTreeLifecycleOwner(this@ActivityCompatOwner)
            setViewTreeViewModelStoreOwner(this@ActivityCompatOwner)
            setViewTreeSavedStateRegistryOwner(this@ActivityCompatOwner)
            setViewTreeOnBackPressedDispatcherOwner(this@ActivityCompatOwner)
        }
    }

    /**
     * Detaches this [ActivityCompatOwner] from the given view.
     * @param view the view to detach this [ActivityCompatOwner] from
     */
    internal fun detachFromView(view: View?) {
        view?.apply {
            setViewTreeLifecycleOwner(null)
            setViewTreeViewModelStoreOwner(null)
            setViewTreeSavedStateRegistryOwner(null)
        }
    }
}
