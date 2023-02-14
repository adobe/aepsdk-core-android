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

package com.adobe.marketing.mobile.services

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceInfoServiceTests {
    private lateinit var deviceInfoService: DeviceInforming

    @Before
    fun setup() {
        ServiceProvider.getInstance().appContextService.setApplication(ApplicationProvider.getApplicationContext())
        deviceInfoService = ServiceProvider.getInstance().deviceInfoService
    }

    @Test
    fun testWhenNullContext() {
        ServiceProviderModifier.setAppContextService(MockAppContextService())
        deviceInfoService = ServiceProvider.getInstance().deviceInfoService
        assertNull(deviceInfoService.activeLocale)
        assertNull(deviceInfoService.displayInformation)
        assertEquals(
            DeviceInforming.DeviceType.UNKNOWN,
            deviceInfoService.deviceType
        )
        assertNull(deviceInfoService.mobileCarrierName)
        assertEquals(
            DeviceInforming.ConnectionStatus.UNKNOWN,
            deviceInfoService.networkConnectionStatus
        )
        assertNull(deviceInfoService.applicationCacheDir)
        assertNull(deviceInfoService.getAsset("testFileName"))
        assertNull(deviceInfoService.getPropertyFromManifest("key"))
        assertNull(deviceInfoService.applicationName)
        assertNull(deviceInfoService.applicationPackageName)
        assertNull(deviceInfoService.applicationVersion)
        assertNull(deviceInfoService.applicationVersionCode)
        assertNull(deviceInfoService.applicationBaseDir)
        assertEquals("en-US", deviceInfoService.localeString)
        assertNull(deviceInfoService.deviceUniqueId)
    }

    @Test
    fun testDisplayInfoService() {
        assertTrue(deviceInfoService.displayInformation.densityDpi > 0)
        assertTrue(deviceInfoService.displayInformation.heightPixels > 0)
        assertTrue(deviceInfoService.displayInformation.widthPixels > 0)
    }
}
