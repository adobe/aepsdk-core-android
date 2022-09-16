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
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.services.ServiceProvider
import java.lang.ref.WeakReference

/**
 * The [App] holds some variables related to the current application, including app [Context],
 * the current [Activity]. Also provides the method to get the orientation of the device, and the
 * methods to get and set icons for notifications.
 */

internal object App {

    private const val DATASTORE_NAME = "ADOBE_MOBILE_APP_STATE"
    private const val SMALL_ICON_RESOURCE_ID_KEY = "SMALL_ICON_RESOURCE_ID"
    private const val LARGE_ICON_RESOURCE_ID_KEY = "LARGE_ICON_RESOURCE_ID"

    @Volatile
    private var applicationContext: WeakReference<Context>? = null

    @Volatile
    private var currentActivity: WeakReference<Activity>? = null

    @Volatile
    private var application: WeakReference<Application>? = null

    @Volatile
    private var smallIconResourceID = -1

    @Volatile
    private var largeIconResourceID = -1

    /**
     * Registers `Application.ActivityLifecycleCallbacks` to the `Application` instance,
     * and the context variable.
     *
     * @param application               the current `Application`
     * @param onActivityResumed invoked when ActivityLifecycleCallbacks.onActivityResumed() is called
     */
    // TODO: we can refactor the "onActivityResumed" parameter to a LifecycleListener interface later when we want to listen to other app actions.
    @JvmName("initializeApp")
    internal fun initializeApp(
        application: Application,
        onActivityResumed: SimpleCallback<Activity>?
    ) {
        this.setApplication(application)
        AppLifecycleListener.instance
            ?.registerActivityLifecycleCallbacks(application, onActivityResumed)
        applicationContext = WeakReference(application)
    }

    fun getApplication(): Application? {
        return application?.get()
    }

    @VisibleForTesting
    @JvmName("setApplication")
    internal fun setApplication(application: Application) {
        this.application = WeakReference(application)
    }

    @JvmName("getAppContext")
    internal fun getAppContext(): Context? {
        return applicationContext?.get()
    }

    // TODO: this method is called in LocalNotificationHandler class, we can make it package private when cleaning up the LocalNotificationHandler class.
    @JvmName("setAppContext")
    internal fun setAppContext(appContext: Context?) {
        val context = appContext?.applicationContext
        if (context != null) {
            this.applicationContext = WeakReference(context)
        }
    }

    @JvmName("getCurrentActivity")
    internal fun getCurrentActivity(): Activity? {
        return this.currentActivity?.get()
    }

    @JvmName("setCurrentActivity")
    internal fun setCurrentActivity(activity: Activity?) {
        if (activity == null) {
            this.currentActivity = null
        } else {
            this.currentActivity = WeakReference(activity)
        }
    }

    /**
     * Returns the `Context` which was set either by `setApplication` or `setAppContext`.
     *
     * @return the current `Context`
     */
    /**
     * Sets the `context` variable.
     *
     * @param context the current `Context`
     */

    /**
     * Returns the `Activity` which was set by `setCurrentActivity`.
     *
     * @return the current `Activity`
     */
    /**
     * Sets the  `activity` variable and also update the  `context` variable
     *
     * @param activity the current `Activity`
     */

    /**
     * Sets the resource Id for small icon.
     *
     * @param resourceID the resource Id of the icon
     */
    @JvmName("setSmallIconResourceID")
    internal fun setSmallIconResourceID(resourceID: Int) {
        smallIconResourceID = resourceID
        val dataStore = ServiceProvider.getInstance().dataStoreService.getNamedCollection(
            DATASTORE_NAME
        )
        dataStore?.setInt(SMALL_ICON_RESOURCE_ID_KEY, smallIconResourceID)
    }

    /**
     * Returns the resource Id for small icon if it was set by `setSmallIconResourceID`.
     *
     * @return a `int` value if it has been set, otherwise -1
     */
    @JvmName("getSmallIconResourceID")
    internal fun getSmallIconResourceID(): Int {
        if (smallIconResourceID == -1) {
            val dataStore = ServiceProvider.getInstance().dataStoreService.getNamedCollection(
                DATASTORE_NAME
            )
            if (dataStore != null) {
                smallIconResourceID = dataStore.getInt(SMALL_ICON_RESOURCE_ID_KEY, -1)
            }
        }
        return smallIconResourceID
    }

    /**
     * Sets the resource Id for large icon.
     *
     * @param resourceID the resource Id of the icon
     */
    @JvmName("setLargeIconResourceID")
    internal fun setLargeIconResourceID(resourceID: Int) {
        largeIconResourceID = resourceID
        val dataStore = ServiceProvider.getInstance().dataStoreService.getNamedCollection(
            DATASTORE_NAME
        )
        dataStore?.setInt(LARGE_ICON_RESOURCE_ID_KEY, largeIconResourceID)
    }

    /**
     * Returns the resource Id for large icon if it was set by `setLargeIconResourceID`.
     *
     * @return a `int` value if it has been set, otherwise -1
     */
    @JvmName("getLargeIconResourceID")
    internal fun getLargeIconResourceID(): Int {
        if (largeIconResourceID == -1) {
            val dataStore = ServiceProvider.getInstance().dataStoreService.getNamedCollection(
                DATASTORE_NAME
            )
            if (dataStore != null) {
                largeIconResourceID = dataStore.getInt(LARGE_ICON_RESOURCE_ID_KEY, -1)
            }
        }
        return largeIconResourceID
    }

    @JvmName("resetInstance")
    internal fun resetInstance() {
        applicationContext = null
        currentActivity = null
        application = null
        smallIconResourceID = -1
        largeIconResourceID = -1
    }
}
