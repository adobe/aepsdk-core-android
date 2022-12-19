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

package com.adobe.marketing.mobile.services;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface AppContextService {
    /**
     * Set the Android {@link Application}, which enables the SDK get the app {@code Context},
     * register a {@link Application.ActivityLifecycleCallbacks} to monitor the lifecycle of the app
     * and get the {@link android.app.Activity} on top of the screen.
     *
     * <p>NOTE: This method should be called right after the app starts, so it gives the SDK all the
     * contexts it needed.
     *
     * @param application the Android {@link Application} instance. It should not be null.
     */
    void setApplication(@NonNull final Application application);

    /**
     * Get the global {@link Application} object of the current process.
     *
     * <p>NOTE: {@link #setApplication(Application)} must be called before calling this method.
     *
     * @return the current {@code Application}, or null if no {@code Application} was set or the
     *     {@code Application} process was destroyed.
     */
    @Nullable Application getApplication();

    /**
     * Returns the current {@code Activity}
     *
     * @return the current {@code Activity}
     */
    @Nullable Activity getCurrentActivity();

    /**
     * Returns the application {@code Context}
     *
     * @return the application {@code Context}
     */
    @Nullable Context getApplicationContext();

    /**
     * Get the current application state.
     *
     * @return AppState the current application state
     */
    @NonNull AppState getAppState();
}
