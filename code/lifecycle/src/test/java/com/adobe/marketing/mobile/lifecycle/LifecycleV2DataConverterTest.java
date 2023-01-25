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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.adobe.marketing.mobile.services.DeviceInforming;
import org.junit.Test;

public class LifecycleV2DataConverterTest {

    @Test
    public void testToDeviceTypeEnum_whenPhone_returnsMobile() {
        assertEquals(
                XDMLifecycleDeviceTypeEnum.MOBILE,
                LifecycleV2DataConverter.toDeviceTypeEnum(DeviceInforming.DeviceType.PHONE));
    }

    @Test
    public void testToDeviceTypeEnum_whenTablet_returnsTablet() {
        assertEquals(
                XDMLifecycleDeviceTypeEnum.TABLET,
                LifecycleV2DataConverter.toDeviceTypeEnum(DeviceInforming.DeviceType.TABLET));
    }

    @Test
    public void testToDeviceTypeEnum_whenUnknown_returnsMobile() {
        assertEquals(
                XDMLifecycleDeviceTypeEnum.MOBILE,
                LifecycleV2DataConverter.toDeviceTypeEnum(DeviceInforming.DeviceType.UNKNOWN));
    }

    @Test
    public void testToDeviceTypeEnum_whenNull_returnsNull() {
        assertNull(LifecycleV2DataConverter.toDeviceTypeEnum(null));
    }

    @Test
    public void testToEnvironmentTypeEnum_whenApplication_returnsApplication() {
        assertEquals(
                XDMLifecycleEnvironmentTypeEnum.APPLICATION,
                LifecycleV2DataConverter.toEnvironmentTypeEnum("application"));
    }

    @Test
    public void testToEnvironmentTypeEnum_whenNull_returnsNull() {
        assertNull(LifecycleV2DataConverter.toEnvironmentTypeEnum(null));
    }

    @Test
    public void testToEnvironmentTypeEnum_whenOther_returnsNull() {
        assertNull(LifecycleV2DataConverter.toEnvironmentTypeEnum("other"));
    }
}
