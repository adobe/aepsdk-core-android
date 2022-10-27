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
package com.adobe.marketing.mobile.integration.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.services.DeviceInforming
import com.adobe.marketing.mobile.services.MockAppContextService
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ServiceProviderModifier
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceInfoServiceTests {
    private var deviceInfoService: DeviceInforming? = null

    @Before
    fun setup() {
        ServiceProviderModifier.setAppContextService(MockAppContextService());
        deviceInfoService = ServiceProvider.getInstance().deviceInfoService
    }

    @Test
    fun testWhenNullContext() {
        TestCase.assertNull(deviceInfoService!!.activeLocale)
        TestCase.assertNull(deviceInfoService!!.displayInformation)
        TestCase.assertEquals(
            DeviceInforming.DeviceType.UNKNOWN,
            deviceInfoService!!.deviceType
        )
        TestCase.assertNull(deviceInfoService!!.mobileCarrierName)
        TestCase.assertEquals(
            DeviceInforming.ConnectionStatus.UNKNOWN,
            deviceInfoService!!.networkConnectionStatus
        )
        TestCase.assertNull(deviceInfoService!!.applicationCacheDir)
        TestCase.assertNull(deviceInfoService!!.getAsset("testFileName"))
        TestCase.assertNull(deviceInfoService!!.getPropertyFromManifest("key"))
        TestCase.assertNull(deviceInfoService!!.applicationName)
        TestCase.assertNull(deviceInfoService!!.applicationPackageName)
        TestCase.assertNull(deviceInfoService!!.applicationVersion)
        TestCase.assertNull(deviceInfoService!!.applicationVersionCode)
        TestCase.assertNull(deviceInfoService!!.applicationBaseDir)
        TestCase.assertEquals("en-US", deviceInfoService!!.localeString)
        TestCase.assertNull(deviceInfoService!!.deviceUniqueId)
    }
}