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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MockLifecycleV2MetricsBuilder extends LifecycleV2MetricsBuilder {
	Map<String, Object> mockAppLaunchXDMData = new HashMap<String, Object>();
	int buildAppLaunchXDMDataCalledTimes = 0;
	List<Object> buildAppLaunchXDMDataLastParams = new ArrayList<Object>();
	Map<String, Object> mockAppCloseXDMData = new HashMap<String, Object>();
	int buildAppCloseXDMDataCalledTimes = 0;
	List<Object> buildAppCloseXDMDataLastParams = new ArrayList<Object>();


	MockLifecycleV2MetricsBuilder(SystemInfoService systemInfoService) {
		super(systemInfoService);
	}

	@Override
	Map<String, Object> buildAppLaunchXDMData(final long launchTimestampSec,
			final boolean isInstall,
			final boolean isUpgrade) {
		buildAppLaunchXDMDataCalledTimes++;
		buildAppLaunchXDMDataLastParams = new ArrayList<Object>();
		buildAppLaunchXDMDataLastParams.add(launchTimestampSec);
		buildAppLaunchXDMDataLastParams.add(isInstall);
		buildAppLaunchXDMDataLastParams.add(isUpgrade);
		return mockAppLaunchXDMData;

	}

	@Override
	Map<String, Object> buildAppCloseXDMData(final long launchTimestampSec,
			final long closeTimestampSec,
			final long fallbackCloseEventTimestampSec,
			final boolean isCloseUnknown) {
		buildAppCloseXDMDataCalledTimes++;
		buildAppCloseXDMDataLastParams = new ArrayList<Object>();
		buildAppCloseXDMDataLastParams.add(launchTimestampSec);
		buildAppCloseXDMDataLastParams.add(closeTimestampSec);
		buildAppCloseXDMDataLastParams.add(fallbackCloseEventTimestampSec);
		buildAppCloseXDMDataLastParams.add(isCloseUnknown);

		return mockAppCloseXDMData;
	}

}
