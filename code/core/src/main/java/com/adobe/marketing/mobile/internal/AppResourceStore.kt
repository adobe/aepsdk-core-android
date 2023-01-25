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

package com.adobe.marketing.mobile.internal

import com.adobe.marketing.mobile.services.ServiceProvider

internal object AppResourceStore {
    private const val DATASTORE_NAME = "ADOBE_MOBILE_APP_STATE"
    private const val SMALL_ICON_RESOURCE_ID_KEY = "SMALL_ICON_RESOURCE_ID"
    private const val LARGE_ICON_RESOURCE_ID_KEY = "LARGE_ICON_RESOURCE_ID"

    @Volatile
    private var smallIconResourceID = -1

    @Volatile
    private var largeIconResourceID = -1

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
}
