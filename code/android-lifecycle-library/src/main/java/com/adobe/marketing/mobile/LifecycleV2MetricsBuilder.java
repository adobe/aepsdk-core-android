/* ************************************************************************
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
 **************************************************************************/

package com.adobe.marketing.mobile;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * XDMLifecycleMetricsBuilder collects metrics in XDM format, to be sent as two XDM events for mobile application launch and application close.
 * Refer to the Mobile App Lifecycle Details mixin, which includes:
 * <ul>
 *     <li>XDM Environment datatype</li>
 *     <li>XMD Device datatype</li>
 *     <li>XDM Application datatype</li>
 * </ul>
 */
class LifecycleV2MetricsBuilder {
	private static final String SELF_LOG_TAG = "LifecycleV2MetricsBuilder";
	private final SystemInfoService systemInfoService;
	private XDMLifecycleDevice xdmDeviceInfo;
	private XDMLifecycleEnvironment xdmEnvironmentInfo;

	/**
	 * Constructor for the Lifecycle metrics builder in XDM format
	 *
	 * @param systemInfoService {@link SystemInfoService} instance to be used for collecting the metrics
	 */
	LifecycleV2MetricsBuilder(final SystemInfoService systemInfoService) {
		this.systemInfoService = systemInfoService;

		if (systemInfoService == null) {
			Log.debug(LifecycleConstants.LOG_TAG, "%s - %s (System Info Services), while creating XDMLifecycleMetricsBuilder.",
					  SELF_LOG_TAG,
					  Log.UNEXPECTED_NULL_VALUE);
		}
	}

	/**
	 * Builds the data required for the XDM Application Launch event, including {@link XDMLifecycleApplication},
	 * {@link XDMLifecycleEnvironment} and {@link XDMLifecycleDevice} info.
	 *
	 * @param launchTimestampMillis the app launch timestamp (milliseconds)
	 * @param isInstall indicates if this is an app install
	 * @param isUpgrade indicates if this is an app upgrade
	 *
	 * @return serialized data in {@link Map} format
	 */
	Map<String, Object> buildAppLaunchXDMData(final long launchTimestampMillis, final boolean isInstall,
			final boolean isUpgrade) {
		XDMLifecycleMobileDetails appLaunchXDMData = new XDMLifecycleMobileDetails();
		appLaunchXDMData.setApplication(computeAppLaunchData(isInstall, isUpgrade));
		appLaunchXDMData.setDevice(computeDeviceData());
		appLaunchXDMData.setEnvironment(computeEnvironmentData());
		appLaunchXDMData.setEventType(LifecycleV2Constants.XDMEventType.APP_LAUNCH);
		appLaunchXDMData.setTimestamp(new Date(launchTimestampMillis));

		return appLaunchXDMData.serializeToXdm();
	}

	/**
	 * Builds the data required for the XDM Application Close event, including {@link XDMLifecycleApplication}.
	 *
	 * @param launchTimestampMillis the app launch timestamp (milliseconds)
	 * @param closeTimestampMillis the app close timestamp (milliseconds)
	 * @param fallbackCloseEventTimestampMillis the timestamp to be used as xdm.timestamp for the Close event when {@code closeTimestampMillis} is invalid or 0
	 * @param isCloseUnknown indicates if this is a regular or abnormal close event
	 * @return serialized data in {@link Map} format
	 */
	Map<String, Object> buildAppCloseXDMData(final long launchTimestampMillis,
			final long closeTimestampMillis,
			final long fallbackCloseEventTimestampMillis,
			final boolean isCloseUnknown) {

		XDMLifecycleMobileDetails appCloseXDMData = new XDMLifecycleMobileDetails();
		appCloseXDMData.setApplication(computeAppCloseData(launchTimestampMillis, closeTimestampMillis, isCloseUnknown));
		appCloseXDMData.setEventType(LifecycleV2Constants.XDMEventType.APP_CLOSE);

		long unwrappedCloseTimestamp = closeTimestampMillis > 0 ? closeTimestampMillis : fallbackCloseEventTimestampMillis;
		appCloseXDMData.setTimestamp(new Date(unwrappedCloseTimestamp));

		return  appCloseXDMData.serializeToXdm();
	}

	/**
	 * Computes general application information as well as details related to the type of launch (install, upgrade, regular launch)
	 *
	 * @param isInstall indicates if this is an app install
	 * @param isUpgrade indicates if this is an app upgrade
	 * @return this {@link XDMLifecycleApplication} with the launch information
	 */
	private XDMLifecycleApplication computeAppLaunchData(final boolean isInstall, final boolean isUpgrade) {

		XDMLifecycleApplication xdmApplicationInfoLaunch = new XDMLifecycleApplication();

		xdmApplicationInfoLaunch.setIsLaunch(true);

		if (isInstall) {
			xdmApplicationInfoLaunch.setIsInstall(true);
		} else if (isUpgrade) {
			xdmApplicationInfoLaunch.setIsUpgrade(true);
		}

		if (systemInfoService == null) {
			Log.debug(LifecycleConstants.LOG_TAG,
					  "%s - Unable to add XDM Application data for app launch due to SystemInfoService being not initialized.",
					  SELF_LOG_TAG);
			return xdmApplicationInfoLaunch;
		}

		xdmApplicationInfoLaunch.setName(systemInfoService.getApplicationName());
		xdmApplicationInfoLaunch.setId(systemInfoService.getApplicationPackageName());
		xdmApplicationInfoLaunch.setVersion(getAppVersion());

		return xdmApplicationInfoLaunch;
	}

	/**
	 * Computes metrics related to the type of close event. The session length is computed based on the launch and close timestamp values.
	 * The {@code closeTimestampMillis} corresponds to the pause event timestamp in normal scenarios or to the last known
	 * close event in case of an abnormal application close.
	 *
	 * @param launchTimestampMillis the app launch timestamp (milliseconds)
	 * @param closeTimestampMillis the app close timestamp (milliseconds)
	 * @param isCloseUnknown indicates if this is a regular or abnormal close event
	 * @return this {@link XDMLifecycleApplication}
	 */
	private XDMLifecycleApplication computeAppCloseData(final long launchTimestampMillis,
			final long closeTimestampMillis,
			final boolean isCloseUnknown) {
		XDMLifecycleApplication xdmApplicationInfoClose = new XDMLifecycleApplication();
		xdmApplicationInfoClose.setIsClose(true);
		xdmApplicationInfoClose.setCloseType(isCloseUnknown ? XDMLifecycleCloseTypeEnum.UNKNOWN :
											 XDMLifecycleCloseTypeEnum.CLOSE);
		// Session Length is defined in seconds
		xdmApplicationInfoClose.setSessionLength(computeSessionLengthSeconds(launchTimestampMillis, closeTimestampMillis));

		return xdmApplicationInfoClose;
	}

	/**
	 * Returns information related to the running environment. This data is computed once, when it is first used, then
	 * returned from cache.
	 *
	 * @return {@link XDMLifecycleEnvironment}
	 */
	private XDMLifecycleEnvironment computeEnvironmentData() {
		if (xdmEnvironmentInfo != null) {
			return xdmEnvironmentInfo;
		}

		if (systemInfoService == null) {
			Log.debug(LifecycleConstants.LOG_TAG,
					  "%s - Unable to add XDM Environment data due to SystemInfoService being not initialized.",
					  SELF_LOG_TAG);
			return null;
		}

		xdmEnvironmentInfo = new XDMLifecycleEnvironment();
		xdmEnvironmentInfo.setCarrier(systemInfoService.getMobileCarrierName());
		xdmEnvironmentInfo.setType(LifecycleV2DataConverter.toEnvironmentTypeEnum(systemInfoService.getRunMode()));
		xdmEnvironmentInfo.setOperatingSystem(systemInfoService.getOperatingSystemName());
		xdmEnvironmentInfo.setOperatingSystemVersion(systemInfoService.getOperatingSystemVersion());
		xdmEnvironmentInfo.setLanguage(LifecycleUtil.formatLocale(systemInfoService.getActiveLocale()));

		return xdmEnvironmentInfo;
	}

	/**
	 * Returns information related to the running environment. This data is computed once, when it is first used, then
	 * returned from cache.
	 *
	 * @return {@link XDMLifecycleDevice}
	 */
	private XDMLifecycleDevice computeDeviceData() {
		if (xdmDeviceInfo != null) {
			return xdmDeviceInfo;
		}

		if (systemInfoService == null) {
			Log.debug(LifecycleConstants.LOG_TAG,
					  "%s - Unable to add XDM Device data due to SystemInfoService being not initialized.", SELF_LOG_TAG);
			return null;
		}

		xdmDeviceInfo = new XDMLifecycleDevice();
		SystemInfoService.DisplayInformation displayInfo = systemInfoService.getDisplayInformation();

		if (displayInfo != null) {
			// absolute width/height of the device
			xdmDeviceInfo.setScreenWidth(displayInfo.getWidthPixels());
			xdmDeviceInfo.setScreenHeight(displayInfo.getHeightPixels());
		}

		xdmDeviceInfo.setType(LifecycleV2DataConverter.toDeviceTypeEnum(systemInfoService.getDeviceType()));
		xdmDeviceInfo.setModel(systemInfoService.getDeviceName());
		xdmDeviceInfo.setModelNumber(systemInfoService.getDeviceBuildId());
		xdmDeviceInfo.setManufacturer(systemInfoService.getDeviceManufacturer());

		return xdmDeviceInfo;
	}

	/**
	 * Returns the application version in the format appVersion (versionCode). Example: 2.3 (10)
	 *
	 * @return the app version as a {@link String} formatted in the specified format.
	 */
	private String getAppVersion() {
		if (systemInfoService == null) {
			return null;
		}

		final String applicationVersion = systemInfoService.getApplicationVersion();
		final String applicationVersionCode = systemInfoService.getApplicationVersionCode();
		return String.format("%s%s",
							 !StringUtils.isNullOrEmpty(applicationVersion) ? String.format("%s", applicationVersion) : "",
							 !StringUtils.isNullOrEmpty(applicationVersionCode) ? String.format(" (%s)", applicationVersionCode) : "");
	}

	/**
	 * Computes the session length based on the previous app session launch and close timestamp.
	 * The returned session length is in seconds.
	 * If the session length is larger than can be represented by an Integer, then 0 is returned.
	 *
	 * @param launchTimestampMillis last known app launch timestamp (milliseconds)
	 * @param closeTimestampMillis last known app close timestamp (milliseconds)
	 * @return the session length (seconds) or 0 if the session length could not be computed
	 */
	private int computeSessionLengthSeconds(final long launchTimestampMillis, final long closeTimestampMillis) {
		long sessionLength = 0;

		if (launchTimestampMillis > 0 && closeTimestampMillis > 0 && closeTimestampMillis > launchTimestampMillis) {
			sessionLength = closeTimestampMillis - launchTimestampMillis;
		}

		long sessionLengthSeconds = TimeUnit.MILLISECONDS.toSeconds(sessionLength);

		return sessionLengthSeconds <= Integer.MAX_VALUE ? (int) sessionLengthSeconds : 0;
	}
}
