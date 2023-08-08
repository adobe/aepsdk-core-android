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
import android.content.Context
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceConstants
import com.adobe.marketing.mobile.services.ui.vnext.Presentable
import com.adobe.marketing.mobile.services.ui.vnext.Presentation
import com.adobe.marketing.mobile.services.ui.vnext.PresentationDelegate
import com.adobe.marketing.mobile.services.ui.vnext.PresentationUtilityProvider
import java.util.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal abstract class BasePresentable<T : Presentation<T>> :
    Presentable<T>,
    AppLifecycleProvider.AppLifecycleListener {
    companion object {
        private const val LOG_SOURCE = "BasePresentable"
    }

    private val contentIdentifier: Int = Random().nextInt()
    private val presentation: Presentation<T>
    private val presentationUtilityProvider: PresentationUtilityProvider
    private val presentationDelegate: PresentationDelegate?
    private val mainScope: CoroutineScope
    private val appLifecycleProvider: AppLifecycleProvider
    protected val visibilityStateManager: VisibilityStateManager

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
        VisibilityStateManager(),
        CoroutineScope(Dispatchers.Main)
    )

    internal constructor(
        presentation: Presentation<T>,
        presentationUtilityProvider: PresentationUtilityProvider,
        presentationDelegate: PresentationDelegate?,
        appLifecycleProvider: AppLifecycleProvider,
        presentableStateManager: VisibilityStateManager,
        mainScope: CoroutineScope
    ) {
        this.presentation = presentation
        this.presentationUtilityProvider = presentationUtilityProvider
        this.presentationDelegate = presentationDelegate
        this.appLifecycleProvider = appLifecycleProvider
        this.visibilityStateManager = presentableStateManager
        this.mainScope = mainScope
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
                return@launch
            }

            if (gateDisplay()) { // TODO :should also include PresentationObserver gate
                val canShow = (presentationDelegate?.canShow(this@BasePresentable) ?: true)
                if (!canShow) return@launch
            }

            if (currentState == Presentable.State.DETACHED) {
                appLifecycleProvider.registerListener(this@BasePresentable)
            }

            val currentActivity: Activity? = presentationUtilityProvider.getCurrentActivity()
            if (currentActivity == null) {
                Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_SOURCE,
                    "Current activity is null. Cannot show presentable."
                )
                return@launch
            }

            // Show the presentable on the current activity
            show(currentActivity)
            Log.debug(
                ServiceConstants.LOG_TAG,
                LOG_SOURCE,
                "Show finished. Presentable is now visible."
            )

            // Notify listener and delegate about the presentable being shown
            presentation.listener.onShow(this@BasePresentable)
            presentationDelegate?.onShow(this@BasePresentable)
        }
    }

    override fun dismiss() {
        mainScope.launch {
            val currentState = getState()
            if (currentState == Presentable.State.DETACHED) {
                Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_SOURCE,
                    "Presentable is already hidden. Ignoring dismiss request."
                )
                return@launch
            }

            appLifecycleProvider.unregisterListener(this@BasePresentable)

            val currentActivity: Activity? = presentationUtilityProvider.getCurrentActivity()
            if (currentActivity == null) {
                Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_SOURCE,
                    "Current activity is null. Cannot dismiss presentable."
                )
                return@launch
            }

            // Remove the presentable from the current activity
            dismiss(currentActivity)

            // Notify listeners and delegate about the presentable being dismissed
            presentation.listener.onDismiss(this@BasePresentable)
            presentationDelegate?.onDismiss(this@BasePresentable)
        }
    }

    override fun hide() {
        mainScope.launch {
            if (getState() == Presentable.State.DETACHED || getState() == Presentable.State.HIDDEN) {
                Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_SOURCE,
                    "Presentable is already hidden. Ignoring hide request."
                )
                return@launch
            }

            // Hide the presentable from the current activity. Note that unlike show() and dismiss()
            // which perform actions on the view itself, hide() only modifies the presentable state
            // resulting in composable to be hidden.
            visibilityStateManager.onHidden()

            // Notify listeners
            presentation.listener.onHide(this@BasePresentable)
            presentationDelegate?.onHide(this@BasePresentable)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        Log.debug(
            ServiceConstants.LOG_TAG,
            LOG_SOURCE,
            "On Activity Resumed."
        )
        mainScope.launch {
            // When an activity is resumed, attach the presentable if it is already shown.
            if (getState() != Presentable.State.VISIBLE) {
                return@launch
            }
            Log.debug(
                ServiceConstants.LOG_TAG,
                LOG_SOURCE,
                "Attaching to $activity."
            )
            attach(activity)
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.debug(
            ServiceConstants.LOG_TAG,
            LOG_SOURCE,
            "On Activity destroyed. Detaching from $activity"
        )

        mainScope.launch { detach(activity) }
    }

    override fun getState(): Presentable.State {
        return visibilityStateManager.presentableState.value
    }

    /**
     * Returns the ComposeView associated with the presentable. This ComposeView is used to
     * render the UI of the presentable by attaching it to the content view of the activity.
     */
    abstract fun getContent(context: Context): ComposeView
    abstract fun gateDisplay(): Boolean

    @MainThread
    internal fun show(activity: Activity) {
        attach(activity)
        // Change the state after the view is attached to the activity.
        // This will trigger the recomposition of the view and result in any animations to work properly.
        visibilityStateManager.onShown()
    }

    @VisibleForTesting
    @MainThread
    internal fun attach(activityToAttach: Activity) {
        val rootViewGroup = activityToAttach.findViewById<ViewGroup>(android.R.id.content)
        val existingComposeView: ComposeView? = activityToAttach.findViewById(contentIdentifier)
        if (existingComposeView != null) {
            Log.debug(
                ServiceConstants.LOG_TAG,
                LOG_SOURCE,
                "Compose view already exists. Showing it instead of creating a new one."
            )
            // do not create a new compose view. make it visible if hidden
            return
        }

        // Fetch a new content view from the presentable
        val composeView: ComposeView = getContent(activityToAttach)
        composeView.id = contentIdentifier
        rootViewGroup.addView(composeView)
        Log.trace(ServiceConstants.LOG_TAG, LOG_SOURCE, "Attached to $composeView to $activityToAttach.")
    }

    @VisibleForTesting
    @MainThread
    fun dismiss(activity: Activity) {
        // Change the state before the view is detached from the activity.
        // This will trigger the recomposition of the view and result in any animations to work properly.
        visibilityStateManager.onDetached()
        detach(activity)
    }

    @VisibleForTesting
    @MainThread
    internal fun detach(activityToDetach: Activity) {
        val rootViewGroup = activityToDetach.findViewById<ViewGroup>(android.R.id.content)
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
        Log.trace(ServiceConstants.LOG_TAG, LOG_SOURCE, "detached to $existingComposeView from $activityToDetach.")
    }
}
internal class VisibilityStateManager {
    private val _presentableState = mutableStateOf(Presentable.State.DETACHED)
    val presentableState: State<Presentable.State> = _presentableState

    fun onShown() {
        _presentableState.value = Presentable.State.VISIBLE
    }

    fun onHidden() {
        _presentableState.value = Presentable.State.HIDDEN
    }

    fun onDetached() {
        _presentableState.value = Presentable.State.DETACHED
    }
}