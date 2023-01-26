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

import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.util.StringUtils;

/** Helper class that converts standard data types to XDM data types for Lifecycle metrics */
class LifecycleV2DataConverter {

    /**
     * Converts {@link DeviceInforming.DeviceType} value to {@link XDMLifecycleDeviceTypeEnum}. If
     * new values are added to the {@link XDMLifecycleDeviceTypeEnum} this helper needs to be
     * updated as well.
     *
     * @param deviceType the type of the device returned by the {@link DeviceInforming}
     * @return the device type enum value, null if the provided {@code deviceType} is null or the
     *     default {@link XDMLifecycleDeviceTypeEnum#MOBILE} if the value is unknown
     */
    static XDMLifecycleDeviceTypeEnum toDeviceTypeEnum(
            final DeviceInforming.DeviceType deviceType) {
        if (deviceType == null) {
            return null;
        }

        // todo: watch to be added once included in the xdm enum
        if (deviceType == DeviceInforming.DeviceType.TABLET) {
            return XDMLifecycleDeviceTypeEnum.TABLET;
        }
        return XDMLifecycleDeviceTypeEnum.MOBILE; // if unknown, default is Mobile
    }

    /**
     * Converts {@link DeviceInforming#getRunMode()} String to {@link
     * XDMLifecycleEnvironmentTypeEnum}. It currently only supports the {@link
     * XDMLifecycleEnvironmentTypeEnum#APPLICATION} type and requires updates once the
     * SystemInfoService is updated to support multiple values.
     *
     * @param runMode the device run mode
     * @return the environment type enum value, or null if unknown
     */
    static XDMLifecycleEnvironmentTypeEnum toEnvironmentTypeEnum(final String runMode) {
        if (StringUtils.isNullOrEmpty(runMode) || !"application".equalsIgnoreCase(runMode)) {
            return null;
        }

        return XDMLifecycleEnvironmentTypeEnum.APPLICATION;
    }
}
