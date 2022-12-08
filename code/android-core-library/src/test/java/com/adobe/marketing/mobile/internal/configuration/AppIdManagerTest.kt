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

package com.adobe.marketing.mobile.internal.configuration

import com.adobe.marketing.mobile.services.DataStoring
import com.adobe.marketing.mobile.services.DeviceInforming
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.ServiceProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

class AppIdManagerTest {
    @Mock
    private lateinit var mockDataStoreService: DataStoring

    @Mock
    private lateinit var mockDeviceInfoService: DeviceInforming

    @Mock
    private lateinit var mockNamedCollection: NamedCollection

    @Mock
    private lateinit var mockServiceProvider: ServiceProvider

    private lateinit var mockedStaticServiceProvider: MockedStatic<ServiceProvider>

    private lateinit var appIdManager: AppIdManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        `when`(mockDataStoreService.getNamedCollection(ConfigurationStateManager.DATASTORE_KEY)).thenReturn(
            mockNamedCollection
        )

        mockedStaticServiceProvider = Mockito.mockStatic(ServiceProvider::class.java)
        mockedStaticServiceProvider.`when`<Any> { ServiceProvider.getInstance() }.thenReturn(mockServiceProvider)
        `when`(mockServiceProvider.dataStoreService).thenReturn(mockDataStoreService)
        `when`(mockServiceProvider.deviceInfoService).thenReturn(mockDeviceInfoService)

        appIdManager = AppIdManager()
    }

    @Test
    fun `Get AppID favors persistence over manifest`() {
        `when`(
            mockNamedCollection.getString(
                ConfigurationStateManager.PERSISTED_APPID,
                null
            )
        ).thenReturn("PersistedAppID")

        `when`(mockDeviceInfoService.getPropertyFromManifest(ConfigurationStateManager.CONFIG_MANIFEST_APPID_KEY)).thenReturn(
            "ManifestAppID"
        )

        val appId = appIdManager.loadAppId()
        assertEquals("PersistedAppID", appId)
    }

    @Test
    fun `Get AppID gets from manifest when nothing is persisted`() {
        val manifestAppID = "ManifestAppID"
        `when`(
            mockNamedCollection.getString(
                ConfigurationStateManager.PERSISTED_APPID,
                null
            )
        ).thenReturn(null)

        `when`(mockDeviceInfoService.getPropertyFromManifest(ConfigurationStateManager.CONFIG_MANIFEST_APPID_KEY)).thenReturn(
            manifestAppID
        )

        val appId = appIdManager.loadAppId()
        assertEquals("ManifestAppID", appId)

        verify(mockNamedCollection).setString(
            ConfigurationStateManager.PERSISTED_APPID,
            manifestAppID
        )
    }

    @Test
    fun `Save AppID to Persistence - sets PERSISTED_APPID in data store`() {
        appIdManager.saveAppIdToPersistence("AppId")
        verify(mockNamedCollection).setString(ConfigurationStateManager.PERSISTED_APPID, "AppId")
    }

    @Test
    fun `Save AppID to Persistence - does not allow empty appID in data store`() {
        val emptyAppID = "  "
        appIdManager.saveAppIdToPersistence(emptyAppID)
        verify(mockNamedCollection, never()).setString(
            ConfigurationStateManager.PERSISTED_APPID,
            emptyAppID
        )
    }

    @Test
    fun `Remove AppID to Persistence - removes PERSISTED_APPID from data store`() {
        appIdManager.removeAppIdFromPersistence()
        verify(mockNamedCollection).remove(ConfigurationStateManager.PERSISTED_APPID)
    }

    @After
    fun teardown() {
        mockedStaticServiceProvider.close()
    }
}
