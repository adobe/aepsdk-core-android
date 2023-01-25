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

import android.content.Context;
import android.content.SharedPreferences;

/** Implementation of {@link DataStoring} service */
class LocalDataStoreService implements DataStoring {

    private static final String TAG = LocalDataStoreService.class.getSimpleName();

    @Override
    public NamedCollection getNamedCollection(final String collectionName) {
        if (collectionName == null || collectionName.isEmpty()) {
            Log.error(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format(
                            "Failed to create an instance of NamedCollection with name - %s: the"
                                    + " collection name is null or empty.",
                            collectionName));
            return null;
        }

        Context appContext =
                ServiceProvider.getInstance().getAppContextService().getApplicationContext();

        if (appContext == null) {
            Log.error(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format(
                            "Failed to create an instance of NamedCollection with name - %s: the"
                                    + " ApplicationContext is null",
                            collectionName));
            return null;
        }

        SharedPreferences sharedPreferences = appContext.getSharedPreferences(collectionName, 0);
        SharedPreferences.Editor sharedPreferencesEditor = null;

        if (sharedPreferences != null) {
            sharedPreferencesEditor = sharedPreferences.edit();
        }

        if (sharedPreferences == null || sharedPreferencesEditor == null) {
            Log.error(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Failed to create a valid SharedPreferences object or SharedPreferences.Editor"
                            + " object");
            return null;
        }

        return new SharedPreferencesNamedCollection(sharedPreferences, sharedPreferencesEditor);
    }
}
