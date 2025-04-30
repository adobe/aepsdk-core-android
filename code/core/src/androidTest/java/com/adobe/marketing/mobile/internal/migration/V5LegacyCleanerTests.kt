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

package com.adobe.marketing.mobile.internal.migration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.adobe.marketing.mobile.internal.migration.MigrationConstants.V5
import com.adobe.marketing.mobile.services.MockAppContextService
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ServiceProviderModifier
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class V5LegacyCleanerTests {

    @Before
    fun setup() {
        // Set up application context service
        val context = ApplicationProvider.getApplicationContext<Context>()
        val mockAppContextService = MockAppContextService().apply {
            appContext = context
        }
        ServiceProviderModifier.setAppContextService(mockAppContextService)

        // Clear any existing data
        ServiceProvider.getInstance().dataStoreService.getNamedCollection(V5.MobileServices.DATASTORE_NAME)?.removeAll()
    }

    @After
    fun tearDown() {
        // Clean up test data
        ServiceProvider.getInstance().dataStoreService.getNamedCollection(V5.MobileServices.DATASTORE_NAME)?.removeAll()
    }

    @Test
    fun testCleanup_RemovesDataFromMobileServicesDataStore() {
        // Setup - add some test data
        val dataStore = ServiceProvider.getInstance().dataStoreService.getNamedCollection(V5.MobileServices.DATASTORE_NAME)
        dataStore?.setString("testKey", "testValue")

        // Verify data was set
        Assert.assertEquals("testValue", dataStore?.getString("testKey", null))

        // Test - invoke the cleanup method
        V5LegacyCleaner.cleanup()

        // Verify - data was removed
        Assert.assertNull(dataStore?.getString("testKey", null))
    }

    @Test
    fun testCleanup_HandlesEmptyDataStore() {
        // Setup - ensure data store is empty
        val dataStore = ServiceProvider.getInstance().dataStoreService.getNamedCollection(V5.MobileServices.DATASTORE_NAME)
        dataStore?.removeAll()

        // Test - invoke the cleanup method
        V5LegacyCleaner.cleanup()

        // Verify - no exceptions thrown
    }
}
