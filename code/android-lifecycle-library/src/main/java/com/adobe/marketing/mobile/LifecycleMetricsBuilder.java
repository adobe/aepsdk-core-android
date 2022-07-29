/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Systems Incorporated
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * LifecycleMetricsBuilder
 * <p>
 * The responsibility of LifecycleMetrics Builder is to build a map that contains the
 * various pieces of lifecycle data. This builder has the ability to add all install, launch, and
 * upgrade data to this map and return it when build method is called.
 */
final class LifecycleMetricsBuilder {
	private static final String SELF_LOG_TAG = "LifecycleMetricsBuilder";
	private final DateFormat sdfDate = new SimpleDateFormat("M/d/yyyy", Locale.US);
	private Map<String, String> lifecycleData;
	private SystemInfoService systemInfoService;
	private LocalStorageService.DataStore lifecycleDataStore;
	private long timestampInSeconds;

	LifecycleMetricsBuilder(final SystemInfoService systemInfoService, final LocalStorageService.DataStore dataStore,
							final long timestampInSeconds) {
		this.lifecycleData = new HashMap<String, String>();
		this.systemInfoService = systemInfoService;
		this.lifecycleDataStore = dataStore;
		this.timestampInSeconds = timestampInSeconds;

		if (dataStore == null) {
			Log.debug(LifecycleConstants.LOG_TAG, "%s - %s (Data Store), while creating LifecycleExtension Metrics Builder.",
					  SELF_LOG_TAG, Log.UNEXPECTED_NULL_VALUE);
		}

		if (systemInfoService == null) {
			Log.debug(LifecycleConstants.LOG_TAG,
					  "%s - %s (System Info Services), while creating LifecycleExtension Metrics Builder", SELF_LOG_TAG,
					  Log.UNEXPECTED_NULL_VALUE);
		}
	}

	/**
	 * Returns the context data map that was built
	 *
	 * @return the context data map that was built
	 */
	Map<String, String> build() {
		return lifecycleData;
	}

	/**
	 * Adds all install data key value pairs to the lifecycle data map and updates the install date in persistence
	 *
	 * @return the builder itself
	 */
	LifecycleMetricsBuilder addInstallData() {
		Log.trace(LifecycleConstants.LOG_TAG, "%s - Adding install data to lifecycle data map", SELF_LOG_TAG);

		lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.DAILY_ENGAGED_EVENT,
						  LifecycleConstants.ContextDataValues.DAILY_ENG_USER_EVENT);
		lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.MONTHLY_ENGAGED_EVENT,
						  LifecycleConstants.ContextDataValues.MONTHLY_ENG_USER_EVENT);
		lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.INSTALL_EVENT,
						  LifecycleConstants.ContextDataValues.INSTALL_EVENT);
		lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.INSTALL_DATE, stringFromTimestamp(timestampInSeconds));

		return this;
	}

	/**
	 * Adds all launch data key value pairs to the lifecycle data map
	 *
	 * @return the builder itself
	 */
	LifecycleMetricsBuilder addLaunchData() {
		Log.trace(LifecycleConstants.LOG_TAG, "%s - Adding launch data to the lifecycle data map", SELF_LOG_TAG);

		if (lifecycleDataStore == null) {
			return this;
		}

		long lastLaunchDate = lifecycleDataStore.getLong(LifecycleConstants.DataStoreKeys.LAST_USED_DATE, 0);
		long firstLaunchDate = lifecycleDataStore.getLong(LifecycleConstants.DataStoreKeys.INSTALL_DATE, 0);
		Calendar calendarCurrentTimestamp = calendarFromTimestampInSeconds(timestampInSeconds);
		Calendar calendarPersistedTimestamp = calendarFromTimestampInSeconds(lastLaunchDate);

		int daysSinceLastUse = calculateDaysBetween(lastLaunchDate, timestampInSeconds);
		int daysSinceFirstUse = calculateDaysBetween(firstLaunchDate, timestampInSeconds);

		if (calendarCurrentTimestamp.get(Calendar.MONTH) != calendarPersistedTimestamp.get(Calendar.MONTH)
				|| calendarCurrentTimestamp.get(Calendar.YEAR) != calendarPersistedTimestamp.get(Calendar.YEAR)) {
			lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.DAILY_ENGAGED_EVENT,
							  LifecycleConstants.ContextDataValues.DAILY_ENG_USER_EVENT);
			lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.MONTHLY_ENGAGED_EVENT,
							  LifecycleConstants.ContextDataValues.MONTHLY_ENG_USER_EVENT);
		} else if (calendarCurrentTimestamp.get(Calendar.DAY_OF_MONTH) != calendarPersistedTimestamp.get(
					   Calendar.DAY_OF_MONTH)) {
			lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.DAILY_ENGAGED_EVENT,
							  LifecycleConstants.ContextDataValues.DAILY_ENG_USER_EVENT);
		}

		if (daysSinceLastUse >= 0) {
			lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.DAYS_SINCE_LAST_LAUNCH,
							  Integer.toString(daysSinceLastUse));
		}

		if (daysSinceFirstUse >= 0) {
			lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.DAYS_SINCE_FIRST_LAUNCH,
							  Integer.toString(daysSinceFirstUse));
		}

		return this;
	}

	/**
	 * Adds all generic data key value pairs to the lifecycle data map (e.g. day of week, hour of day, no of launches)
	 *
	 * @return the builder itself
	 */
	LifecycleMetricsBuilder addGenericData() {
		Log.trace(LifecycleConstants.LOG_TAG, "%s - Adding generic data to the lifecycle data map", SELF_LOG_TAG);

		if (lifecycleDataStore != null) {
			int launches = lifecycleDataStore.getInt(LifecycleConstants.DataStoreKeys.LAUNCHES, -1);

			if (launches != -1) {
				lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.LAUNCHES, Integer.toString(launches));
			}
		}

		Calendar calendarCurrentTimestamp = calendarFromTimestampInSeconds(timestampInSeconds);

		// first day of week is Sunday, days will have indexes [1-7]
		lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.DAY_OF_WEEK,
						  Integer.toString(calendarCurrentTimestamp.get(Calendar.DAY_OF_WEEK)));
		lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.HOUR_OF_DAY,
						  Integer.toString(calendarCurrentTimestamp.get(Calendar.HOUR_OF_DAY)));
		lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.LAUNCH_EVENT,
						  LifecycleConstants.ContextDataValues.LAUNCH_EVENT);

		return this;
	}

	/**
	 * Adds all upgrade data key value pairs to the lifecycle data map if upgrade parameter is true
	 *
	 * @param upgrade boolean specifying if this was an upgrade or not
	 * @return the builder itself
	 */
	LifecycleMetricsBuilder addUpgradeData(final boolean upgrade) {
		Log.trace(LifecycleConstants.LOG_TAG, "%s - Adding upgrade data to lifecycle data map", SELF_LOG_TAG);

		if (upgrade) {
			lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.UPGRADE_EVENT,
							  LifecycleConstants.ContextDataValues.UPGRADE_EVENT);
		}

		if (lifecycleDataStore == null) {
			return this;
		}

		long upgradeDate = lifecycleDataStore.getLong(LifecycleConstants.DataStoreKeys.UPGRADE_DATE, 0L);

		if (upgrade) {
			lifecycleDataStore.setLong(LifecycleConstants.DataStoreKeys.UPGRADE_DATE, timestampInSeconds);
			lifecycleDataStore.setInt(LifecycleConstants.DataStoreKeys.LAUNCHES_AFTER_UPGRADE, 0);
		} else if (upgradeDate > 0) {
			int daysSinceUpgrade = calculateDaysBetween(upgradeDate, timestampInSeconds);
			int launchesAfterUpgrade = lifecycleDataStore.getInt(LifecycleConstants.DataStoreKeys.LAUNCHES_AFTER_UPGRADE, 0) + 1;
			lifecycleDataStore.setInt(LifecycleConstants.DataStoreKeys.LAUNCHES_AFTER_UPGRADE, launchesAfterUpgrade);

			lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.LAUNCHES_SINCE_UPGRADE, Integer.toString(
								  launchesAfterUpgrade));

			if (daysSinceUpgrade >= 0) {
				lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.DAYS_SINCE_LAST_UPGRADE,
								  Integer.toString(daysSinceUpgrade));
			}
		}

		return this;
	}

	/**
	 * Adds crash info to the lifecycle data map if previousSessionCrash is true
	 *
	 * @param previousSessionCrash specifies if there was a crash in the previous session
	 * @return the builder itself
	 */
	LifecycleMetricsBuilder addCrashData(final boolean previousSessionCrash) {
		Log.trace(LifecycleConstants.LOG_TAG, "%s - Adding crash data to lifecycle data map", SELF_LOG_TAG);

		if (previousSessionCrash) {
			lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.CRASH_EVENT,
							  LifecycleConstants.ContextDataValues.CRASH_EVENT);
		}

		return this;
	}

	/**
	 * Adds all core data key value pairs to the lifecycle data map. If some of the values are null, the corresponding keys
	 * will not be added in the lifecycle data map.
	 *
	 * @return the builder itself
	 */
	LifecycleMetricsBuilder addCoreData() {
		Log.trace(LifecycleConstants.LOG_TAG, "%s - Adding core data to lifecycle data map", SELF_LOG_TAG);

		if (systemInfoService == null) {
			return this;
		}

		final String deviceName = systemInfoService.getDeviceName();

		if (!StringUtils.isNullOrEmpty(deviceName)) {
			lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.DEVICE_NAME, deviceName);
		}

		final String carrierName = systemInfoService.getMobileCarrierName();

		if (!StringUtils.isNullOrEmpty(carrierName)) {
			lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.CARRIER_NAME, carrierName);
		}

		final String appId = getApplicationIdentifier();

		if (!StringUtils.isNullOrEmpty(appId)) {
			lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.APP_ID, appId);
		}

		final String operatingSystem = systemInfoService.getOperatingSystemName() + " " +
									   systemInfoService.getOperatingSystemVersion();

		if (!StringUtils.isNullOrEmpty(operatingSystem)) {
			lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.OPERATING_SYSTEM, operatingSystem);
		}

		final String resolution = getResolution();

		if (!StringUtils.isNullOrEmpty(resolution)) {
			lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.DEVICE_RESOLUTION, resolution);
		}

		final String locale = LifecycleUtil.formatLocale(systemInfoService.getActiveLocale());

		if (!StringUtils.isNullOrEmpty(locale)) {
			lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.LOCALE, locale);
		}

		final String runMode = systemInfoService.getRunMode();

		if (!StringUtils.isNullOrEmpty(runMode)) {
			lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.RUN_MODE, runMode);
		}

		return this;
	}

	/**
	 * Calculates the number of days between two times provided
	 *
	 * @param startTimestampInSeconds the starting date
	 * @param endTimestampInSeconds   the end date
	 * @return the resulted number of days
	 */
	private int calculateDaysBetween(final long startTimestampInSeconds, final long endTimestampInSeconds) {
		if (startTimestampInSeconds < LifecycleConstants.WRONG_EPOCH_MAX_LENGTH_SECONDS
				|| endTimestampInSeconds < LifecycleConstants.WRONG_EPOCH_MAX_LENGTH_SECONDS) {
			Log.debug(LifecycleConstants.LOG_TAG,
					  "%s - Invalid timestamp - startTimestampInSeconds (%d), endTimestampInSeconds (%d)", SELF_LOG_TAG,
					  startTimestampInSeconds, endTimestampInSeconds);
			return -1;
		}

		Calendar calendarStartDate = calendarFromTimestampInSeconds(startTimestampInSeconds);
		Calendar calendarEndDate = calendarFromTimestampInSeconds(endTimestampInSeconds);

		int yearsDifference = calendarEndDate.get(Calendar.YEAR) - calendarStartDate.get(Calendar.YEAR);
		int daysDifference = calendarEndDate.get(Calendar.DAY_OF_YEAR) - calendarStartDate.get(Calendar.DAY_OF_YEAR);
		int startYear = calendarStartDate.get(Calendar.YEAR);
		int endYear = calendarEndDate.get(Calendar.YEAR);

		if (yearsDifference == 0) {
			return daysDifference;
		} else {
			int daysInYearsDifference = 0;
			final int leapYearDays = 366;
			final int otherYearDays = 365;
			GregorianCalendar gregorianCalendar = new GregorianCalendar();

			for (int year = startYear; year < endYear; year++) {
				if (gregorianCalendar.isLeapYear(year)) {
					daysInYearsDifference += leapYearDays;
				} else {
					daysInYearsDifference += otherYearDays;
				}
			}

			return daysDifference + daysInYearsDifference;
		}
	}

	private Calendar calendarFromTimestampInSeconds(final long timestampInSeconds) {

		Calendar calendarStartDate = Calendar.getInstance();
		calendarStartDate.setTimeInMillis(TimeUnit.SECONDS.toMillis(timestampInSeconds));
		return calendarStartDate;

	}

	private String stringFromTimestamp(final long timestampInSeconds) {
		synchronized (sdfDate) {
			return sdfDate.format(TimeUnit.SECONDS.toMillis(timestampInSeconds));
		}
	}

	/**
	 * Generates the Application ID string from Application name, version and version code
	 *
	 * @return string representation of the Application ID
	 */
	private String getApplicationIdentifier() {
		if (systemInfoService == null) {
			return null;
		}

		final String applicationName = systemInfoService.getApplicationName();
		final String applicationVersion = systemInfoService.getApplicationVersion();
		final String applicationVersionCode = systemInfoService.getApplicationVersionCode();
		return String.format("%s%s%s",
							 applicationName,
							 !StringUtils.isNullOrEmpty(applicationVersion) ? String.format(" %s", applicationVersion) : "",
							 !StringUtils.isNullOrEmpty(applicationVersionCode) ? String.format(" (%s)", applicationVersionCode) : "");
	}

	/**
	 * Generates the resolution string from DisplayInformationInterface
	 *
	 * @return string representation of the resolution
	 */
	private String getResolution() {
		if (systemInfoService == null) {
			return null;
		}

		SystemInfoService.DisplayInformation displayInfo = systemInfoService.getDisplayInformation();

		if (displayInfo == null) {
			Log.debug(LifecycleConstants.LOG_TAG, "%s - Failed to get resolution (DisplayInformation was null)", SELF_LOG_TAG);
			return null;
		}

		return String.format(Locale.US, "%dx%d", displayInfo.getWidthPixels(), displayInfo.getHeightPixels());
	}
}
