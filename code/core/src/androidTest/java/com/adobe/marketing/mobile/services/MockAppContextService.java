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

public class MockAppContextService implements AppContextService {

    public Application application;
    public Activity currentActivity;
    public Context appContext;
    public AppState appState = AppState.UNKNOWN;

    @Override
    public void setApplication(@NonNull Application application) {
        this.application = application;
    }

    @Nullable @Override
    public Application getApplication() {
        return application;
    }

    @Nullable @Override
    public Activity getCurrentActivity() {
        return currentActivity;
    }

    @Nullable @Override
    public Context getApplicationContext() {
        return appContext;
    }

    @NonNull @Override
    public AppState getAppState() {
        return appState;
    }
}
