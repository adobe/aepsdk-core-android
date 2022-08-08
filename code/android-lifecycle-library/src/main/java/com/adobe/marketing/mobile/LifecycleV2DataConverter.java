/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2021 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 * *************************************************************************/

package com.adobe.marketing.mobile;

/**
 * Helper class that converts standard data types to XDM data types for Lifecycle metrics
 */
class LifecycleV2DataConverter {

	/**
	 * Converts {@link SystemInfoService.DeviceType} value to {@link XDMLifecycleDeviceTypeEnum}.
	 * If new values are added to the {@link XDMLifecycleDeviceTypeEnum} this helper needs to be updated as well.
	 *
	 * @param deviceType the type of the device returned by the {@link SystemInfoService}
	 * @return the device type enum value, null if the provided {@code deviceType} is null or the default {@link XDMLifecycleDeviceTypeEnum#MOBILE}
	 * 		   if the value is unknown
	 */
	static XDMLifecycleDeviceTypeEnum toDeviceTypeEnum(final SystemInfoService.DeviceType deviceType) {
		if (deviceType == null) {
			return null;
		}

		switch (deviceType) {
			case PHONE:
				return XDMLifecycleDeviceTypeEnum.MOBILE;

			case TABLET:
				return XDMLifecycleDeviceTypeEnum.TABLET;

			default:
				return XDMLifecycleDeviceTypeEnum.MOBILE; // if unknown, default is Mobile
		}
	}

	/**
	 * Converts {@link SystemInfoService#getRunMode()} String to {@link XDMLifecycleEnvironmentTypeEnum}. It currently only supports the
	 * {@link XDMLifecycleEnvironmentTypeEnum#APPLICATION} type and requires updates once the SystemInfoService is updated to support
	 * multiple values.
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
