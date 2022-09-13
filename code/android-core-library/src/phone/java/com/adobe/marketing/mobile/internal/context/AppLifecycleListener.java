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

package com.adobe.marketing.mobile.internal.context;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.res.Configuration;

import androidx.annotation.Nullable;

import com.adobe.marketing.mobile.CoreConstants;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.services.Log;

/**
 * Implement {@link Application.ActivityLifecycleCallbacks} to detect whether the app is in foreground.
 * <p>
 * The order of lifecycle callbacks when {@link Activity} A starts {@code Activity} B:
 * <ol>
 *     <li>Activity A's onPause() method executes</li>
 *     <li>Activity B's onCreate(), onStart(), and onResume() methods execute in sequence. (Activity B now has user focus.)</li>
 *     <li>Then, if Activity A is no longer visible on screen, its onStop() method executes.</li>
 * </ol>
 * So based on this sequence, we start a timer with {@code BACKGROUND_TRANSITION_DELAY_MILLIS} when onPause() of Activity A is called, which will set
 * the app state to background. So if it is a true app close, then we will have the correct app state
 * after 500ms.
 * And if there is Activity B gets started right after Activity A paused, then we will cancel the timer,
 * so the app state will remain foreground.
 */
public class AppLifecycleListener implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    private final static String LOG_TAG = "AppLifecycleListener";
    private static AppLifecycleListener instance;
    private static boolean registered;

    private volatile AppState appState = AppState.UNKNOWN;
    private List<AppStateListener> appStateListeners;
    private final AtomicBoolean isInBackground = new AtomicBoolean(true);
    private SimpleCallback<Activity> onActivityResumed;

    /**
     * Singleton. Get the {@link AppLifecycleListener} instance.
     *
     * @return {@link AppLifecycleListener} Singleton
     */
    public static synchronized AppLifecycleListener getInstance() {
        if (instance == null) {
            instance = new AppLifecycleListener();
        }

        return instance;
    }

    /**
     * Private constructor.
     */
    private AppLifecycleListener() {
        appStateListeners = new ArrayList<>();
    }

    /**
     * Registers {@code this} as the activity lifecycle callback for the {@code Application}.
     *
     * @param application       the {@link Application} of the app
     * @param onActivityResumed invoked when ActivityLifecycleCallbacks.onActivityResumed() is called
     */
    void registerActivityLifecycleCallbacks(Application application, @Nullable SimpleCallback<Activity> onActivityResumed) {
        if (application != null && !registered) {
            application.registerActivityLifecycleCallbacks(this);
            application.registerComponentCallbacks(this);
            registered = true;
            this.onActivityResumed = onActivityResumed;
        } else {
            Log.error(CoreConstants.CORE_EXTENSION_NAME, LOG_TAG, "The given Application instance is null.");
        }
    }

    /**
     * Gets the current app state.
     *
     * @return the current app state
     */
    public AppState getAppState() {
        return appState;
    }

    /**
     * Registers a {@code AppStateListener} which will gets called when the app state changes.
     *
     * @param listener the {@link AppStateListener} to receive app state change events
     */
    public void registerListener(AppStateListener listener) {
        appStateListeners.add(listener);
    }

    /**
     * Unregisters a {@code AppStateListener}.
     *
     * @param listener the {@link AppStateListener} to unregister
     */
    public void unregisterListener(AppStateListener listener) {
        appStateListeners.remove(listener);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        setForegroundIfNeeded();
        if (onActivityResumed != null) {
            onActivityResumed.call(activity);
        }
        App.setCurrentActivity(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // do nothing
    }

    private void notifyListeners() {
        for (AppStateListener listener : appStateListeners) {
            if (appState == AppState.FOREGROUND) {
                listener.onForeground();
            } else if (appState == AppState.BACKGROUND) {
                listener.onBackground();
            }
        }
    }

    private void setForegroundIfNeeded() {
        boolean isBackground = isInBackground.compareAndSet(true, false);

        if (isBackground) {
            appState = AppState.FOREGROUND;
            notifyListeners();
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // do nothing
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // do nothing
    }

    @Override
    public void onActivityStopped(Activity activity) {
        // do nothing
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // do nothing
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // do nothing
    }

    @Override
    public final void onConfigurationChanged(Configuration paramConfiguration) {
        // do nothing
    }

    @Override
    public final void onLowMemory() {
        // do nothing
    }

    @Override
    public void onTrimMemory(int level) {
        // https://developer.android.com/reference/android/content/ComponentCallbacks2.html#TRIM_MEMORY_UI_HIDDEN
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            boolean isForeground = isInBackground.compareAndSet(false, true);

            if (isForeground) {
                appState = AppState.BACKGROUND;
                notifyListeners();
            }
        }
    }

}
