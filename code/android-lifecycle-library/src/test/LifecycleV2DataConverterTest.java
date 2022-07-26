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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LifecycleV2DataConverterTest {

	@Test
	public void testToDeviceTypeEnum_whenPhone_returnsMobile() {
		assertEquals(XDMLifecycleDeviceTypeEnum.MOBILE,
					 LifecycleV2DataConverter.toDeviceTypeEnum(SystemInfoService.DeviceType.PHONE));
	}

	@Test
	public void testToDeviceTypeEnum_whenTablet_returnsTablet() {
		assertEquals(XDMLifecycleDeviceTypeEnum.TABLET,
					 LifecycleV2DataConverter.toDeviceTypeEnum(SystemInfoService.DeviceType.TABLET));
	}

	@Test
	public void testToDeviceTypeEnum_whenUnknown_returnsMobile() {
		assertEquals(XDMLifecycleDeviceTypeEnum.MOBILE,
					 LifecycleV2DataConverter.toDeviceTypeEnum(SystemInfoService.DeviceType.UNKNOWN));
	}

	@Test
	public void testToDeviceTypeEnum_whenNull_returnsNull() {
		assertNull(LifecycleV2DataConverter.toDeviceTypeEnum(null));
	}

	@Test
	public void testToEnvironmentTypeEnum_whenApplication_returnsApplication() {
		assertEquals(XDMLifecycleEnvironmentTypeEnum.APPLICATION,
					 LifecycleV2DataConverter.toEnvironmentTypeEnum("application"));
	}

	@Test
	public void testToEnvironmentTypeEnum_whenNull_returnsNull() {
		assertNull(LifecycleV2DataConverter.toEnvironmentTypeEnum("other"));
	}
}
