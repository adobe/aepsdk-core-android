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

package com.adobe.marketing.mobile.services.ui.common

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.platform.ComposeView
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceConstants
import com.adobe.marketing.mobile.services.ui.AlreadyDismissed
import com.adobe.marketing.mobile.services.ui.AlreadyHidden
import com.adobe.marketing.mobile.services.ui.AlreadyShown
import com.adobe.marketing.mobile.services.ui.ConflictingPresentation
import com.adobe.marketing.mobile.services.ui.DelegateGateNotMet
import com.adobe.marketing.mobile.services.ui.NoActivityToDetachFrom
import com.adobe.marketing.mobile.services.ui.NoAttachableActivity
import com.adobe.marketing.mobile.services.ui.Presentable
import com.adobe.marketing.mobile.services.ui.Presentation
import com.adobe.marketing.mobile.services.ui.PresentationDelegate
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.Random

/**
 * Base implementation of [Presentable] interface. Abstracts the common functionality needed by all presentables
 * for their lifecycle and listener management.
 */
internal abstract class AEPPresentable<T : Presentation<T>> :
    Presentable<T>,
    AppLifecycleProvider.AppLifecycleListener {
    companion object {
        private const val LOG_SOURCE = "AEPPresentable"
    }

    private val presentation: Presentation<T>
    private val presentationUtilityProvider: PresentationUtilityProvider
    private val presentationDelegate: PresentationDelegate?
    private val mainScope: CoroutineScope
    private val appLifecycleProvider: AppLifecycleProvider
    private val presentationObserver: PresentationObserver
    private val activityCompatOwnerUtils: ActivityCompatOwnerUtils
    protected val presentationStateManager: PresentationStateManager

    @VisibleForTesting internal val contentIdentifier: Int = Random().nextInt()

    /**
     * @param presentation the [Presentation] to be used by this [Presentable]
     * @param presentationUtilityProvider the [PresentationUtilityProvider] to be used to fetch components needed by this [Presentable]
     * @param presentationDelegate the [PresentationDelegate] for notifying the application of [Presentation] lifecycle events
     * @param appLifecycleProvider the [AppLifecycleProvider] to be used to register for app lifecycle events
     */
    constructor(
        presentation: Presentation<T>,
        presentationUtilityProvider: PresentationUtilityProvider,
        presentationDelegate: PresentationDelegate?,
        appLifecycleProvider: AppLifecycleProvider
    ) : this(
        presentation,
        presentationUtilityProvider,
        presentationDelegate,
        appLifecycleProvider,
        PresentationStateManager(),
        ActivityCompatOwnerUtils(),
        CoroutineScope(Dispatchers.Main),
        PresentationObserver.INSTANCE
    )

    /**
     * @param presentation the [Presentation] to be used by this [Presentable]
     * @param presentationUtilityProvider the [PresentationUtilityProvider] to be used to fetch components needed by this [Presentable]
     * @param presentationDelegate the [PresentationDelegate] for notifying the application of [Presentation] lifecycle events
     * @param appLifecycleProvider the [AppLifecycleProvider] to be used to register for app lifecycle events
     * @param presentationStateManager the [PresentationStateManager] to be used to manage the state of this [Presentable]
     * @param mainScope the [CoroutineScope] to be used to launch coroutines on the main thread
     */
    @VisibleForTesting
    internal constructor(
        presentation: Presentation<T>,
        presentationUtilityProvider: PresentationUtilityProvider,
        presentationDelegate: PresentationDelegate?,
        appLifecycleProvider: AppLifecycleProvider,
        presentationStateManager: PresentationStateManager,
        activityCompatOwnerUtils: ActivityCompatOwnerUtils,
        mainScope: CoroutineScope,
        presentationObserver: PresentationObserver
    ) {
        this.presentation = presentation
        this.presentationUtilityProvider = presentationUtilityProvider
        this.presentationDelegate = presentationDelegate
        this.appLifecycleProvider = appLifecycleProvider
        this.presentationStateManager = presentationStateManager
        this.activityCompatOwnerUtils = activityCompatOwnerUtils
        this.mainScope = mainScope
        this.presentationObserver = presentationObserver
    }

    override fun show() {
        mainScope.launch {
            val currentState = getState()
            if (currentState == Presentable.State.VISIBLE) {
                Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_SOURCE,
                    "Presentable is already shown. Ignoring show request."
                )
                presentation.listener.onError(this@AEPPresentable, AlreadyShown)
                return@launch
            }

            val currentActivity: Activity? = presentationUtilityProvider.getCurrentActivity()
            if (currentActivity == null) {
                Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_SOURCE,
                    "Current activity is null. Cannot show presentable."
                )
                presentation.listener.onError(this@AEPPresentable, NoAttachableActivity)
                return@launch
            }

            val hasConflicts = hasConflicts(presentationObserver.getVisiblePresentations())
            if (hasConflicts) {
                Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_SOURCE,
                    "Presentable has conflicts with other visible presentations. Ignoring show request."
                )
                presentation.listener.onError(this@AEPPresentable, ConflictingPresentation)
                return@launch
            }

            // If all basic conditions are met, check with the delegate if the presentable can be shown
            if (gateDisplay()) {
                val canShow = (presentationDelegate?.canShow(this@AEPPresentable) ?: true)
                if (!canShow) {
                    presentation.listener.onError(this@AEPPresentable, DelegateGateNotMet)
                    return@launch
                }
            }

            // Show the presentable on the current activity
            show(currentActivity)

            // Register for app lifecycle events. AppLifecycleProvider maintains a set of listeners, so
            // registering multiple times is safe
            appLifecycleProvider.registerListener(this@AEPPresentable)

            // At this point show(currentActivity) would have attached the view if one existed, or just bailed out
            // if the view already a part of the view hierarchy. In either case, notify the listeners and
            // delegate about the presentable being shown
            presentation.listener.onShow(this@AEPPresentable)
            presentationDelegate?.onShow(this@AEPPresentable)
            presentationObserver.onPresentationVisible(getPresentation())
        }
    }

    override fun dismiss() {
        mainScope.launch {
            // unregister for app lifecycle events
            appLifecycleProvider.unregisterListener(this@AEPPresentable)

            if (getState() == Presentable.State.DETACHED) {
                Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_SOURCE,
                    "Presentable is already detached. Ignoring dismiss request."
                )
                presentation.listener.onError(this@AEPPresentable, AlreadyDismissed)
                return@launch
            }

            val currentActivity: Activity? = presentationUtilityProvider.getCurrentActivity()
            if (currentActivity == null) {
                Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_SOURCE,
                    "Current activity is null. Cannot dismiss presentable."
                )
                presentation.listener.onError(this@AEPPresentable, NoActivityToDetachFrom)
                return@launch
            }

            // Remove the presentable from the current activity.
            dismiss(currentActivity)

            // At this point dismiss(currentActivity) would have detached the view if one existed, or just bailed out
            // if the view was never a part of the view hierarchy. In either case, notify the listeners and
            // delegate about the presentable being dismissed
            presentation.listener.onDismiss(this@AEPPresentable)
            presentationDelegate?.onDismiss(this@AEPPresentable)
            presentationObserver.onPresentationInvisible(getPresentation())
        }
    }

    override fun hide() {
        mainScope.launch {
            if (getState() != Presentable.State.VISIBLE) {
                Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_SOURCE,
                    "Presentable is already hidden. Ignoring hide request."
                )
                presentation.listener.onError(this@AEPPresentable, AlreadyHidden)
                return@launch
            }

            // Hide the presentable from the current activity. Note that unlike show() and dismiss()
            // which perform actions on the view itself, hide() only modifies the presentable state
            // resulting in composable to be hidden.
            presentationStateManager.onHidden()

            // Notify listeners
            presentation.listener.onHide(this@AEPPresentable)
            presentationDelegate?.onHide(this@AEPPresentable)
            presentationObserver.onPresentationInvisible(getPresentation())
        }
    }

    override fun onActivityResumed(activity: Activity) {
        // When an activity associated with the host application is resumed, attach the ComposeView to it.
        // Do not change the state of the presentable it because this is an implicit attachment.
        mainScope.launch {
            // When an activity is resumed, attach the presentable only if it is already shown.
            if (getState() != Presentable.State.VISIBLE) {
                return@launch
            }
            attach(activity)
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        // When an activity associated with the host application is destroyed, detach the ComposeView from it.
        // Do not change the state of the presentable it because this is an implicit detachment.
        mainScope.launch { detach(activity) }
    }

    override fun getState(): Presentable.State {
        return presentationStateManager.presentableState.value
    }

    /**
     * Waits for the exit animation to complete before performing any cleanup.
     * Subclasses can override this method to wait for the exit animation to complete before invoking
     * [onAnimationComplete]. Default implementation is to not wait for any exit animation to complete.
     * @param onAnimationComplete the callback to be invoked after the exit animation is complete.
     */
    protected open fun awaitExitAnimation(onAnimationComplete: () -> Unit) {
        // Default implementation is to not wait for any exit animation to complete.
        onAnimationComplete()
    }

    /**
     * Fetches the [ComposeView] associated with the presentable. This ComposeView is used to
     * render the UI of the presentable by attaching it to the content view of the activity.
     * @param activityContext The context associated with the activity.
     */
    abstract fun getContent(activityContext: Context): ComposeView

    /**
     * Determines whether the [Presentable] should be shown or not after consulting the [PresentationDelegate].
     * @return true if the presentable should be shown, false otherwise.
     */
    abstract fun gateDisplay(): Boolean

    /**
     * Determines whether the presentable has any conflicts with the given list of visible presentations
     * on the screen.
     * @param visiblePresentations list of visible presentations on the screen.
     */
    abstract fun hasConflicts(visiblePresentations: List<Presentation<*>>): Boolean

    /**
     * Makes the presentable visible on the given activity by attaching it to the activity's content view.
     * Also changes the state of the presentable.
     * @param activity The activity to attach the presentable to.
     */
    @MainThread
    private fun show(activity: Activity) {
        attach(activity)
        // Change the state after the view is attached to the activity.
        // This will trigger the recomposition of the view and result in any animations to be seen after attaching.
        presentationStateManager.onShown()
    }

    /**
     * Attaches the presentable content with identifier [contentIdentifier] to the given activity.
     * The caller of this method is responsible for changing the state of the presentable before or after calling this method.
     * This method will always result in the presentable being attached to the activity's content view (absent exceptions),
     * either by virtue of the view already being a part of the view hierarchy or by creating a new view and
     * attaching it to the activity's content view.
     * @param activityToAttach The activity to attach the presentable to.
     */
    @MainThread
    private fun attach(activityToAttach: Activity) {
        val existingComposeView: View? = activityToAttach.findViewById(contentIdentifier)

        if (existingComposeView != null) {
            Log.debug(
                ServiceConstants.LOG_TAG,
                LOG_SOURCE,
                "Compose view already exists with id: $contentIdentifier. Showing it instead of creating a new one."
            )
            return
        }

        activityCompatOwnerUtils.attachActivityCompatOwner(activityToAttach)

        // Fetch a new content view from the presentable
        val composeView: ComposeView = getContent(activityToAttach)
        composeView.id = contentIdentifier
        val rootViewGroup = activityToAttach.findViewById<ViewGroup>(android.R.id.content)
        rootViewGroup.addView(composeView)
        Log.trace(
            ServiceConstants.LOG_TAG,
            LOG_SOURCE,
            "Attached $contentIdentifier to $activityToAttach."
        )
    }

    /**
     * Removes the presentable content from the given activity and changes the state of the presentable.
     * @param activity The activity to detach the presentable from.
     */
    @MainThread
    private fun dismiss(activity: Activity) {
        // Change the state before the view is detached from the activity.
        // This will trigger the recomposition of the view and result in any animations.
        presentationStateManager.onDetached()

        // If the presentable has an exit animation, wait for it to complete before detaching the ComposeView.
        // This is done to ensure that the ComposeView is not detached before the exit animation completes.
        // This is important because the exit animation may be a part of the ComposeView itself and detaching
        // the ComposeView before the animation completes will result in the animation being cancelled.
        awaitExitAnimation { detach(activity) }
    }

    /**
     * Detaches the presentable content (previously fetched via [getContent]) with identifier [contentIdentifier] from the given activity.
     * The caller of this method is responsible for changing the state of the presentable before or after
     * calling this method.
     * This method will always result in the presentable being detached from the activity's content view (absent exceptions),
     * either by virtue of the view already being absent from the view hierarchy or by removing the existing view.
     * @param activityToDetach The activity to detach the presentable from.
     */
    @MainThread
    private fun detach(activityToDetach: Activity) {
        val rootViewGroup: ViewGroup = activityToDetach.findViewById(android.R.id.content)
        val existingComposeView: ComposeView? = activityToDetach.findViewById(contentIdentifier)
        if (existingComposeView == null) {
            Log.debug(
                ServiceConstants.LOG_TAG,
                LOG_SOURCE,
                "Compose view does not exist. Nothing to detach."
            )
            return
        }
        existingComposeView.removeAllViews()
        rootViewGroup.removeView(existingComposeView)
        activityCompatOwnerUtils.detachActivityCompatOwner(activityToDetach)
        Log.trace(ServiceConstants.LOG_TAG, LOG_SOURCE, "Detached ${contentIdentifier}from $activityToDetach.")
    }
}

/**
 * Responsible for tracking the currently visible presentations. Methods of this class should be invoked
 * from the same thread across presentables to ensure serialization of the operations. Since this is managed
 * from [AEPPresentable], this guarantee is already provided (due to calls from main thread). This class should
 * be moved to the API layer and appropriate serialization mechanisms should be added in case we want to add the
 * ability to have custom implementation of [Presentable]s.
 */
@VisibleForTesting
internal class PresentationObserver private constructor() {

    companion object {
        internal val INSTANCE by lazy { PresentationObserver() }
    }

    /**
     * A map of presentation IDs to weak references of the presentation.
     * This map is used to keep track of the currently visible presentations.
     */
    private val visiblePresentations: MutableMap<String, WeakReference<Presentation<*>>> = mutableMapOf()

    /**
     * Called when a presentation becomes visible.
     * @param presentation The presentation that became visible.
     */
    @VisibleForTesting
    @MainThread
    internal fun onPresentationVisible(presentation: Presentation<*>) {
        visiblePresentations[presentation.id] = WeakReference(presentation)
    }

    /**
     * Called when a presentation becomes invisible from the screen either due to being hidden or dismissed.
     * @param presentation The presentation that became invisible from the screen.
     */
    @VisibleForTesting
    @MainThread
    internal fun onPresentationInvisible(presentation: Presentation<*>) {
        visiblePresentations.remove(presentation.id)
    }

    /**
     * Returns the list of currently visible presentations.
     * @return The list of currently visible presentations.
     */
    @VisibleForTesting
    @MainThread
    internal fun getVisiblePresentations(): List<Presentation<*>> {
        // Use this opportunity to clean up the map of any lost references
        val lostRefs = visiblePresentations.filterValues { it.get() == null }.keys
        lostRefs.forEach { visiblePresentations.remove(it) }

        // Return the list of visible presentations
        return visiblePresentations.values.mapNotNull { it.get() }
    }
}
