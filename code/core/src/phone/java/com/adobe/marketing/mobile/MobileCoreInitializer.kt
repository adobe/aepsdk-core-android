/*
  Copyright 2025 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.UserManager
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.adobe.marketing.mobile.internal.CoreConstants
import com.adobe.marketing.mobile.internal.configuration.ConfigurationExtension
import com.adobe.marketing.mobile.internal.eventhub.EventHub
import com.adobe.marketing.mobile.internal.migration.V4Migrator
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.internal.context.App
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

internal class MobileCoreInitializer(
    private val scope: CoroutineScope,
    private val mainDispatcher: CoroutineDispatcher,
    private val lifecycleOwner: LifecycleOwner,
    private val extensionDiscovery: ExtensionDiscovery,
    private val isUserUnlocked: (Application) -> Boolean
) {
    companion object {
        const val LOG_TAG: String = "MobileCoreInitializer"

        private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.warning(CoreConstants.LOG_TAG, LOG_TAG, "Caught exception - ${throwable.message}")
        }

        private val isUserUnlocked: (Application) -> Boolean = { application: Application ->
            // https://issuetracker.google.com/issues/146535388
            if (Build.VERSION.SDK_INT >= 24) {
                val userManager = application.getSystemService(Context.USER_SERVICE) as? UserManager
                // User manager should never be null.
                userManager?.isUserUnlocked ?: true
            } else {
                true
            }
        }

        @JvmField
        var INSTANCE = MobileCoreInitializer(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler),
            mainDispatcher = Dispatchers.Main.immediate,
            lifecycleOwner = ProcessLifecycleOwner.get(),
            extensionDiscovery = ExtensionDiscovery(),
            isUserUnlocked = isUserUnlocked
        )
    }

    private val setApplicationCalled = AtomicBoolean(false)
    private val initializeCalled = AtomicBoolean(false)
    // Using this mutex to guard migration and EventHistory initialization, ensuring they complete
    // in a background thread before any extensions are registered.
    private val mutex = Mutex()

    // Hold a reference to cleanup between tests.
    private var lifecycleObserver: DefaultLifecycleObserver? = null

    fun initialize(
        application: Application,
        initOptions: InitOptions,
        completionCallback: AdobeCallback<*>?
    ) {
        if (initializeCalled.getAndSet(true)) {
            Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "initialize failed - ignoring as it was already called.")
            return
        }

        setApplication(application)

        when (val config = initOptions.config) {
            is ConfigType.AppID -> MobileCore.configureWithAppID(config.appID)
            is ConfigType.FileInPath -> MobileCore.configureWithFileInPath(config.filePath)
            is ConfigType.FileInAssets -> MobileCore.configureWithFileInAssets(config.filePath)
            else -> Unit
        }

        // Enable automatic lifecycle tracking if specified
        if (initOptions.lifecycleAutomaticTrackingEnabled) {
            scope.launch(mainDispatcher) {
                lifecycleObserver = LifecycleTracker(initOptions.lifecycleAdditionalContextData).also {
                    lifecycleOwner.lifecycle.addObserver(it)
                }
            }
        }

        // Automatically discovers and registers extensions.
        // Extension discovery is performed in a background coroutine for efficiency.
        scope.launch {
            extensionDiscovery.getExtensions(application).also {
                registerExtensions(it, completionCallback)
            }
        }
    }

    private fun handleAndroid8BugWorkaround() {
        // AMSDK-8502
        // Workaround to prevent a crash happening on Android 8.0/8.1 related to
        // TimeZoneNamesImpl
        // https://issuetracker.google.com/issues/110848122
        if (Build.VERSION.SDK_INT == VERSION_CODES.O || Build.VERSION.SDK_INT == VERSION_CODES.O_MR1) {
            try {
                Date().toString()
            } catch (e: AssertionError) {
                // Ignore AssertionError
            } catch (e: Exception) {
                // Ignore other exceptions
            }
        }
    }

    fun setApplication(application: Application) {
        if (!isUserUnlocked(application)) {
            Log.error(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "setApplication failed - device is in direct boot mode, SDK will not be" +
                    " initialized."
            )
            return
        }
        handleAndroid8BugWorkaround()

        if (setApplicationCalled.getAndSet(true)) {
            Log.debug(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "setApplication failed - ignoring as setApplication was already called."
            )
            return
        }

        ServiceProvider.getInstance().appContextService.setApplication(application)
        App.registerActivityLifecycleCallbacks(LaunchInfoCollector())
        scope.launch {
            mutex.withLock {
                try {
                    val v4Migrator = V4Migrator()
                    v4Migrator.migrate()
                } catch (e: Exception) {
                    Log.error(CoreConstants.LOG_TAG, LOG_TAG, "Migration from V4 SDK failed with error - ${e.localizedMessage}")
                }

                EventHub.shared.initializeEventHistory()
            }
        }
    }

    fun registerExtensions(extensions: List<Class<out Extension>?>?, completionCallback: AdobeCallback<*>?) {
        if (!setApplicationCalled.get()) {
            Log.error(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "Failed to registerExtensions - setApplication not called"
            )
            return
        }

        val extensionsToRegister = mutableSetOf<Class<out Extension>>(ConfigurationExtension::class.java)
        extensions?.filterNotNull()?.let {
            extensionsToRegister.addAll(it)
        }

        scope.launch {
            mutex.withLock {
                EventHub.shared.registerExtensions(extensionsToRegister) {
                    completionCallback?.call(null)
                }
            }
        }
    }

    @VisibleForTesting
    fun reset() {
        // Reset Lifecycle Observer if present
        lifecycleObserver?.let {
            scope.launch(mainDispatcher) {
                lifecycleOwner.lifecycle.removeObserver(it)
            }
        }

        setApplicationCalled.set(false)
        initializeCalled.set(false)
    }
}

/**
 * Responsible for tracking the application lifecycle and automatically invoking lifecycle APIs.
 */
internal class LifecycleTracker(private val additionalContextData: Map<String, String>?) : DefaultLifecycleObserver {
    override fun onResume(owner: LifecycleOwner) {
        Log.debug(CoreConstants.LOG_TAG, MobileCoreInitializer.LOG_TAG, "Lifecycle tracking - onResume")
        MobileCore.lifecycleStart(additionalContextData)
    }

    override fun onPause(owner: LifecycleOwner) {
        Log.debug(CoreConstants.LOG_TAG, MobileCoreInitializer.LOG_TAG, "Lifecycle tracking - onPause")
        MobileCore.lifecyclePause()
    }
}

/**
 * This class listens to the activity lifecycle and invokes the `MobileCore.collectLaunchInfo` method
 * when an activity enters the Resumed state.
 * The callback is invoked synchronously from the registered [Application.ActivityLifecycleCallbacks].
 */
internal class LaunchInfoCollector : App.ActivityLifecycleCallbacks {
    override fun onActivityResumed(activity: Activity) {
        MobileCore.collectLaunchInfo(activity)
    }
}
