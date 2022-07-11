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

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DeviceInfoServiceTests {

    private DeviceInfoService deviceInfoService;

    @Before
    public void setup() {
        deviceInfoService = new DeviceInfoService();
    }

    @Test
    public void testWhenNullContext() {
        ServiceProvider.getInstance().setContext(null);
        assertNull(deviceInfoService.getActiveLocale());
        assertNull(deviceInfoService.getDisplayInformation());
        assertEquals(DeviceInforming.DeviceType.UNKNOWN, deviceInfoService.getDeviceType());
        assertNull(deviceInfoService.getMobileCarrierName());
        assertEquals(DeviceInforming.ConnectionStatus.UNKNOWN,deviceInfoService.getNetworkConnectionStatus());
        assertNull(deviceInfoService.getApplicationCacheDir());
        assertNull(deviceInfoService.getAsset("testFileName"));
        assertNull(deviceInfoService.getPropertyFromManifest("key"));
        assertNull(deviceInfoService.getApplicationName());
        assertNull(deviceInfoService.getApplicationPackageName());
        assertNull(deviceInfoService.getApplicationVersion());
        assertNull(deviceInfoService.getApplicationVersionCode());
        assertNull(deviceInfoService.getApplicationBaseDir());
        assertEquals("en-US", deviceInfoService.getLocaleString());
    }
}
