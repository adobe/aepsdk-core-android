package com.adobe.marketing.mobile.services;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface AppContextService {

    /**
     * Set the Android {@link Application}, which enables the SDK get the app {@code Context},
     * register a {@link Application.ActivityLifecycleCallbacks}
     * to monitor the lifecycle of the app and get the {@link android.app.Activity} on top of the screen.
     * <p>
     * NOTE: This method should be called right after the app starts, so it gives the SDK all the
     * contexts it needed.
     *
     * @param application the Android {@link Application} instance. It should not be null.
     */
    void setApplication(@NonNull final Application application);

    /**
     * Get the global {@link Application} object of the current process.
     * <p>
     * NOTE: {@link #setApplication(Application)} must be called before calling this method.
     *
     * @return the current {@code Application}, or null if no {@code Application} was set or
     * the {@code Application} process was destroyed.
     */
    @Nullable Application getApplication();

    /**
     * Returns the current {@code Activity}
     *
     * @return the current {@code Activity}
     */
    @Nullable Activity getCurrentActivity();

    /**
     * Returns the current {@code Context}
     *
     * @return the current {@code Context}
     */
    @Nullable Context getApplicationContext();

    /**
     * Get the current application state.
     *
     * @return AppState the current application state
     */
    @NonNull AppState getAppState();
}
