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

package com.adobe.marketing.mobile;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.services.ServiceProvider;

import java.lang.ref.WeakReference;

/**
 * The {@link App} holds some variables related to the current application, including app {@link Context},
 * the current {@link Activity}. Also provides the method to get the orientation of the device, and the
 * methods to get and set icons for notifications.
 */
class App {

    private static final String DATASTORE_NAME = "ADOBE_MOBILE_APP_STATE";
    private static final String SMALL_ICON_RESOURCE_ID_KEY = "SMALL_ICON_RESOURCE_ID";
    private static final String LARGE_ICON_RESOURCE_ID_KEY = "LARGE_ICON_RESOURCE_ID";
    private static volatile Context appContext;
    private static volatile WeakReference<Activity> currentActivity;
    private static volatile WeakReference<Application> application;
    private static volatile int smallIconResourceID = -1;
    private static volatile int largeIconResourceID = -1;

    private App() {
    }

    /**
     * Registers {@code Application.ActivityLifecycleCallbacks} to the {@code Application} instance,
     * and the context variable.
     *
     * @param app the current {@code Application}
     */
    static void setApplication(Application app) {
        if (application != null && application.get() != null) {
            return;
        }

        application = new WeakReference<Application>(app);
        AppLifecycleListener.getInstance().registerActivityLifecycleCallbacks(app);
        setAppContext(app);
        ServiceProvider.getInstance().setContext(app);
    }

    static Application getApplication() {
        return application != null ? application.get() : null;
    }

    /**
     * Sets the {@code context} variable.
     *
     * @param context the current {@code Context}
     */
    static void setAppContext(Context context) {
        appContext = context != null ? context.getApplicationContext() : null;
    }

    /**
     * Returns the {@code Context} which was set either by {@code setApplication} or {@code setAppContext}.
     *
     * @return the current {@code Context}
     */
    static Context getAppContext() {
        return appContext;
    }

    /**
     * Sets the  {@code activity} variable and also update the  {@code context} variable
     *
     * @param activity the current {@code Activity}
     */
    static void setCurrentActivity(Activity activity) {
        if (activity == null) {
            return;
        }

        currentActivity = new WeakReference<Activity>(activity);
        setAppContext(activity);
        ServiceProvider.getInstance().setCurrentActivity(currentActivity.get());
    }

    /**
     * Returns the {@code Activity} which was set by {@code setCurrentActivity}.
     *
     * @return the current {@code Activity}
     */
    static Activity getCurrentActivity() {
        if (currentActivity == null) {
            return null;
        }

        return currentActivity.get();
    }

    /**
     * Returns the current orientation of the device.
     *
     * @return a {@code int} value indicates the orientation. 0 for unknown, 1 for portrait and 2 for landscape
     */
    static int getCurrentOrientation() {
        if (currentActivity == null || currentActivity.get() == null) {
            return 0; //neither landscape nor portrait
        }

        return currentActivity.get().getResources().getConfiguration().orientation;
    }

    /**
     * Returns the resource Id for small icon if it was set by {@code setSmallIconResourceID}.
     *
     * @return a {@code int} value if it has been set, otherwise -1
     */
    static int getSmallIconResourceID() {
        if (smallIconResourceID == -1) {
            NamedCollection dataStore = ServiceProvider.getInstance().getDataStoreService().getNamedCollection(DATASTORE_NAME);

            if (dataStore != null) {
                smallIconResourceID = dataStore.getInt(SMALL_ICON_RESOURCE_ID_KEY, -1);
            }
        }

        return smallIconResourceID;
    }

    /**
     * Sets the resource Id for small icon.
     *
     * @param resourceID the resource Id of the icon
     */
    static void setSmallIconResourceID(int resourceID) {
        smallIconResourceID = resourceID;
        NamedCollection dataStore = ServiceProvider.getInstance().getDataStoreService().getNamedCollection(DATASTORE_NAME);

        if (dataStore != null) {
            dataStore.setInt(SMALL_ICON_RESOURCE_ID_KEY, smallIconResourceID);
        }
    }

    /**
     * Returns the resource Id for large icon if it was set by {@code setLargeIconResourceID}.
     *
     * @return a {@code int} value if it has been set, otherwise -1
     */
    static int getLargeIconResourceID() {
        if (largeIconResourceID == -1) {
            NamedCollection dataStore = ServiceProvider.getInstance().getDataStoreService().getNamedCollection(DATASTORE_NAME);
            if (dataStore != null) {
                largeIconResourceID = dataStore.getInt(LARGE_ICON_RESOURCE_ID_KEY, -1);
            }
        }

        return largeIconResourceID;
    }

    /**
     * Sets the resource Id for large icon.
     *
     * @param resourceID the resource Id of the icon
     */
    static void setLargeIconResourceID(int resourceID) {
        largeIconResourceID = resourceID;
        NamedCollection dataStore = ServiceProvider.getInstance().getDataStoreService().getNamedCollection(DATASTORE_NAME);
        if (dataStore != null) {
            dataStore.setInt(LARGE_ICON_RESOURCE_ID_KEY, largeIconResourceID);
        }
    }

    /**
     * For testing.
     * Clear this App of all held variables and object references.
     * Method clears the {@link Application}, {@link Context}, {@link Activity},
     * notification icon resources and clears the icons in local persistent storage.
     */
    static void clearAppResources() {
        if (appContext != null) {
            appContext = null;
        }

        if (application != null) {
            Application app = application.get();

            if (app != null) {
                app.unregisterActivityLifecycleCallbacks(AppLifecycleListener.getInstance());
            }

            application.clear();
            application = null;
        }

        if (currentActivity != null) {
            currentActivity.clear();
            currentActivity = null;
        }

        NamedCollection dataStore = ServiceProvider.getInstance().getDataStoreService().getNamedCollection(DATASTORE_NAME);
        if (dataStore != null) {
            dataStore.remove(SMALL_ICON_RESOURCE_ID_KEY);
            dataStore.remove(LARGE_ICON_RESOURCE_ID_KEY);
        }

        smallIconResourceID = -1;
        largeIconResourceID = -1;
    }

}
