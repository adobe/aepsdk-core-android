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

package com.adobe.marketing.mobile.lifecycle;

import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.services.DeviceInforming;
import java.util.Locale;

class LifecycleTestHelper {

    public static void initDeviceInfoService(DeviceInforming deviceInfoService) {
        when(deviceInfoService.getApplicationName()).thenReturn("TEST_APPLICATION_NAME");
        when(deviceInfoService.getApplicationVersion()).thenReturn("1.1");
        when(deviceInfoService.getDeviceName()).thenReturn("deviceName");
        when(deviceInfoService.getApplicationVersionCode()).thenReturn("12345");
        when(deviceInfoService.getDisplayInformation())
                .thenReturn(
                        new DeviceInforming.DisplayInformation() {
                            @Override
                            public int getWidthPixels() {
                                return 100;
                            }

                            @Override
                            public int getHeightPixels() {
                                return 100;
                            }

                            @Override
                            public int getDensityDpi() {
                                return 500;
                            }
                        });
        when(deviceInfoService.getDeviceBuildId()).thenReturn("TEST_PLATFORM");
        when(deviceInfoService.getOperatingSystemName()).thenReturn("TEST_OS");
        when(deviceInfoService.getOperatingSystemVersion()).thenReturn("5.55");
        when(deviceInfoService.getMobileCarrierName()).thenReturn("TEST_CARRIER");
        when(deviceInfoService.getActiveLocale()).thenReturn(new Locale("en", "US"));
        when(deviceInfoService.getRunMode()).thenReturn("APPLICATION");
    }
}
