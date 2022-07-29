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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class LifecycleV2ExtensionTest extends BaseTest {
	private long currentTimestampInMilliseconds;
	private long timestampTenMinEarlierInMilliseconds;
	private long timestampOneHourEarlierInMilliseconds;
	private long timestampOneDayEarlierMilliseconds;
	private String mockAppName = "TEST_APPLICATION_NAME";
	private String mockAppVersion = "1.1";
	private String mockInitAppVersion = "1.0";
	private LifecycleV2Extension lifecycleV2;
	private LocalStorageService.DataStore lifecycleDataStore;
	private MockSystemInfoService mockSystemInfoService;
	private MockLifecycleV2MetricsBuilder mockBuilder;
	private MockLifecycleExtension mockLifecycleExtension;
	private LifecycleV2DispatcherApplicationState lifecycleV2DispatcherApplicationState;

	private static final String DATASTORE_KEY_INSTALL_DATE = "InstallDate";
	private static final String DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS = "v2AppStartTimestampMillis";
	private static final String DATASTORE_KEY_APP_PAUSE_TIMESTAMP_MILLIS = "v2AppPauseTimestampMillis";
	private static final String DATASTORE_KEY_APP_CLOSE_TIMESTAMP_MILLIS = "v2AppCloseTimestampMillis";
	private static final String DATASTORE_KEY_LAST_APP_VERSION = "v2LastAppVersion";
	private static final String EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA = "additionalcontextdata";

	private static final long STATE_UPDATE_TIMEOUT_MILLIS				 = 2000;
	private static final long CLOSE_TIMESTAMP_OFFSET_MILLIS         	 = 2000;

	@Before
	public void beforeEach() {
		super.beforeEach();

		PlatformServices fakePlatformServices = new FakePlatformServices();
		EventHub mockEventHub = new EventHub("testEventHub", fakePlatformServices);
		mockLifecycleExtension = new MockLifecycleExtension(mockEventHub, fakePlatformServices);
		lifecycleV2DispatcherApplicationState = new LifecycleV2DispatcherApplicationState(mockEventHub, mockLifecycleExtension);

		eventHub.createSharedState("com.adobe.module.configuration",
								   Event.SHARED_STATE_OLDEST.getEventNumber(),
								   new EventData().putInteger("lifecycle.sessionTimeout", 200)
								  );
		mockSystemInfoService = platformServices.getMockSystemInfoService();
		mockSystemInfoService.applicationName = "appName";
		mockSystemInfoService.applicationVersion = mockAppVersion;
		mockSystemInfoService.deviceName = "deviceName";
		mockSystemInfoService.applicationVersionCode = "12345";
		mockSystemInfoService.displayInformation = new SystemInfoService.DisplayInformation() {
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
		};
		mockSystemInfoService.deviceBuildId = "TEST_PLATFORM";
		mockSystemInfoService.operatingSystemName = "TEST_OS";
		mockSystemInfoService.operatingSystemVersion = "5.55";
		mockSystemInfoService.mobileCarrierName = "TEST_CARRIER";
		mockSystemInfoService.activeLocale = new Locale("en", "US");
		mockSystemInfoService.applicationName = mockAppName;

		lifecycleDataStore = platformServices.getLocalStorageService().getDataStore("AdobeMobile_Lifecycle");
		lifecycleV2 = new LifecycleV2Extension(lifecycleDataStore, mockSystemInfoService,
											   lifecycleV2DispatcherApplicationState);

		initTimestamps();
	}

	private void initTimestamps() {
		currentTimestampInMilliseconds = System.currentTimeMillis();
		timestampTenMinEarlierInMilliseconds = currentTimestampInMilliseconds - TimeUnit.MINUTES.toMillis(10000);
		timestampOneHourEarlierInMilliseconds = currentTimestampInMilliseconds - TimeUnit.HOURS.toMillis(1000);
		timestampOneDayEarlierMilliseconds = currentTimestampInMilliseconds - TimeUnit.DAYS.toMillis(1000);
	}

	@After
	public void afterEach() {
		super.afterEach();
		lifecycleDataStore.removeAll();
	}

	// ------------ Test XDM related methods ------------

	@Test
	public void start_happy_regularLaunch() {
		mockPersistence(timestampOneHourEarlierInMilliseconds, timestampTenMinEarlierInMilliseconds,
						timestampTenMinEarlierInMilliseconds,
						false, false);
		mockBuilder = new MockLifecycleV2MetricsBuilder(mockSystemInfoService);
		lifecycleV2 = new LifecycleV2Extension(lifecycleDataStore, mockSystemInfoService, lifecycleV2DispatcherApplicationState,
											   mockBuilder);

		Map<String, String> additionalContextData = new HashMap<String, String>();
		additionalContextData.put("TEST_KEY1", "TEXT_VAL1");

		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliseconds)
		.setData(new EventData().putStringMap(EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA, additionalContextData))
		.build();
		lifecycleV2.start(testEvent, false);

		assertEquals(currentTimestampInMilliseconds, lifecycleDataStore.getLong(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS, 0));
		assertEquals(1, mockBuilder.buildAppLaunchXDMDataCalledTimes);
		assertEquals(currentTimestampInMilliseconds, mockBuilder.buildAppLaunchXDMDataLastParams.get(0));
		assertEquals(false, mockBuilder.buildAppLaunchXDMDataLastParams.get(1));
		assertEquals(false, mockBuilder.buildAppLaunchXDMDataLastParams.get(2));

		assertEquals(0, mockBuilder.buildAppCloseXDMDataCalledTimes);
	}

	@Test
	public void start_consecutiveStartEvents_updatesOnlyFirstTime() {
		mockPersistence(timestampOneHourEarlierInMilliseconds, timestampTenMinEarlierInMilliseconds,
						timestampTenMinEarlierInMilliseconds,
						false, true);
		mockBuilder = new MockLifecycleV2MetricsBuilder(mockSystemInfoService);
		lifecycleV2 = new LifecycleV2Extension(lifecycleDataStore, mockSystemInfoService, lifecycleV2DispatcherApplicationState,
											   mockBuilder);

		Map<String, String> additionalContextData = new HashMap<String, String>();
		additionalContextData.put("TEST_KEY1", "TEXT_VAL1");

		long timestampTwoSecondsEarlierMilliseconds = currentTimestampInMilliseconds - 2000;
		Event testEvent1 = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(timestampTwoSecondsEarlierMilliseconds)
		.setData(new EventData().putStringMap(EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA, additionalContextData))
		.build();
		lifecycleV2.start(testEvent1, false);

		Event testEvent2 = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliseconds)
		.setData(new EventData().putStringMap(EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA, additionalContextData))
		.build();
		lifecycleV2.start(testEvent2, false);


		assertEquals(timestampTwoSecondsEarlierMilliseconds,
					 lifecycleDataStore.getLong(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS, 0));
		assertEquals(1, mockBuilder.buildAppLaunchXDMDataCalledTimes);
		assertEquals(1, mockBuilder.buildAppLaunchXDMDataCalledTimes);
		assertEquals(currentTimestampInMilliseconds - 2000, mockBuilder.buildAppLaunchXDMDataLastParams.get(0));
		assertEquals(false, mockBuilder.buildAppLaunchXDMDataLastParams.get(1));
		assertEquals(true, mockBuilder.buildAppLaunchXDMDataLastParams.get(2));

		assertEquals(0, mockBuilder.buildAppCloseXDMDataCalledTimes);
	}

	@Test
	public void start_onInstall_doesNotBuildCloseEvent() {
		mockBuilder = new MockLifecycleV2MetricsBuilder(mockSystemInfoService);
		lifecycleV2 = new LifecycleV2Extension(lifecycleDataStore, mockSystemInfoService, lifecycleV2DispatcherApplicationState,
											   mockBuilder);

		Map<String, String> additionalContextData = new HashMap<String, String>();
		additionalContextData.put("TEST_KEY1", "TEXT_VAL1");

		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliseconds)
		.setData(new EventData().putStringMap(EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA, additionalContextData))
		.build();
		lifecycleV2.start(testEvent, true);

		assertEquals(currentTimestampInMilliseconds, lifecycleDataStore.getLong(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS, 0));
		assertEquals(1, mockBuilder.buildAppLaunchXDMDataCalledTimes);
		assertEquals(0, mockBuilder.buildAppCloseXDMDataCalledTimes);
	}

	@Test
	public void start_onCloseUnknown_missingPause_buildsCloseEvent() {
		lifecycleDataStore.setLong(DATASTORE_KEY_INSTALL_DATE, timestampOneDayEarlierMilliseconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS, timestampOneHourEarlierInMilliseconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_APP_CLOSE_TIMESTAMP_MILLIS, timestampTenMinEarlierInMilliseconds);
		mockBuilder = new MockLifecycleV2MetricsBuilder(mockSystemInfoService);
		lifecycleV2 = new LifecycleV2Extension(lifecycleDataStore, mockSystemInfoService, lifecycleV2DispatcherApplicationState,
											   mockBuilder);

		Map<String, String> additionalContextData = new HashMap<String, String>();
		additionalContextData.put("TEST_KEY1", "TEXT_VAL1");

		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliseconds)
		.setData(new EventData().putStringMap(EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA, additionalContextData))
		.build();
		lifecycleV2.start(testEvent, false);

		assertEquals(currentTimestampInMilliseconds, lifecycleDataStore.getLong(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS, 0));
		assertEquals(1, mockBuilder.buildAppLaunchXDMDataCalledTimes);

		assertEquals(1, mockBuilder.buildAppCloseXDMDataCalledTimes);
		assertEquals(timestampOneHourEarlierInMilliseconds, mockBuilder.buildAppCloseXDMDataLastParams.get(0));
		assertEquals(timestampTenMinEarlierInMilliseconds + CLOSE_TIMESTAMP_OFFSET_MILLIS,
					 mockBuilder.buildAppCloseXDMDataLastParams.get(1));
		assertEquals(currentTimestampInMilliseconds - 1000,
					 mockBuilder.buildAppCloseXDMDataLastParams.get(2)); // backdated start time
		assertEquals(true, mockBuilder.buildAppCloseXDMDataLastParams.get(3));
	}

	@Test
	public void start_onCloseUnknown_missingStart_buildsCloseEvent() {
		lifecycleDataStore.setLong(DATASTORE_KEY_INSTALL_DATE, timestampOneDayEarlierMilliseconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_APP_PAUSE_TIMESTAMP_MILLIS, timestampTenMinEarlierInMilliseconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_APP_CLOSE_TIMESTAMP_MILLIS, timestampTenMinEarlierInMilliseconds);
		mockBuilder = new MockLifecycleV2MetricsBuilder(mockSystemInfoService);
		lifecycleV2 = new LifecycleV2Extension(lifecycleDataStore, mockSystemInfoService, lifecycleV2DispatcherApplicationState,
											   mockBuilder);

		Map<String, String> additionalContextData = new HashMap<String, String>();
		additionalContextData.put("TEST_KEY1", "TEXT_VAL1");

		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliseconds)
		.setData(new EventData().putStringMap(EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA, additionalContextData))
		.build();
		lifecycleV2.start(testEvent, false);

		assertEquals(currentTimestampInMilliseconds, lifecycleDataStore.getLong(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS, 0));
		assertEquals(1, mockBuilder.buildAppLaunchXDMDataCalledTimes);

		assertEquals(1, mockBuilder.buildAppCloseXDMDataCalledTimes);
		assertEquals(0L, mockBuilder.buildAppCloseXDMDataLastParams.get(0));
		assertEquals(timestampTenMinEarlierInMilliseconds + CLOSE_TIMESTAMP_OFFSET_MILLIS,
					 mockBuilder.buildAppCloseXDMDataLastParams.get(1));
		assertEquals(currentTimestampInMilliseconds - 1000,
					 mockBuilder.buildAppCloseXDMDataLastParams.get(2)); // backdated start time
		assertEquals(true, mockBuilder.buildAppCloseXDMDataLastParams.get(3));
	}

	@Test
	public void start_onCloseUnknown_startAfterPause_buildsCloseEvent() {
		mockPersistence(timestampTenMinEarlierInMilliseconds, timestampOneHourEarlierInMilliseconds,
						timestampTenMinEarlierInMilliseconds,
						false, false);
		mockBuilder = new MockLifecycleV2MetricsBuilder(mockSystemInfoService);
		lifecycleV2 = new LifecycleV2Extension(lifecycleDataStore, mockSystemInfoService, lifecycleV2DispatcherApplicationState,
											   mockBuilder);

		Map<String, String> additionalContextData = new HashMap<String, String>();
		additionalContextData.put("TEST_KEY1", "TEXT_VAL1");

		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliseconds)
		.setData(new EventData().putStringMap(EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA, additionalContextData))
		.build();
		lifecycleV2.start(testEvent, false);

		assertEquals(currentTimestampInMilliseconds, lifecycleDataStore.getLong(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS, 0));
		assertEquals(1, mockBuilder.buildAppLaunchXDMDataCalledTimes);

		assertEquals(1, mockBuilder.buildAppCloseXDMDataCalledTimes);
		assertEquals(timestampTenMinEarlierInMilliseconds, mockBuilder.buildAppCloseXDMDataLastParams.get(0));
		assertEquals(timestampTenMinEarlierInMilliseconds + CLOSE_TIMESTAMP_OFFSET_MILLIS,
					 mockBuilder.buildAppCloseXDMDataLastParams.get(1));
		assertEquals(currentTimestampInMilliseconds - 1000,
					 mockBuilder.buildAppCloseXDMDataLastParams.get(2)); // backdated start time
		assertEquals(true, mockBuilder.buildAppCloseXDMDataLastParams.get(3));
	}

	@Test
	public void pause_Happy() throws Exception {
		mockPersistence(timestampTenMinEarlierInMilliseconds, timestampOneDayEarlierMilliseconds,
						timestampOneDayEarlierMilliseconds, false,
						false);
		mockBuilder = new MockLifecycleV2MetricsBuilder(mockSystemInfoService);
		lifecycleV2 = new LifecycleV2Extension(lifecycleDataStore, mockSystemInfoService, lifecycleV2DispatcherApplicationState,
											   mockBuilder);

		lifecycleDataStore.setLong(DATASTORE_KEY_APP_PAUSE_TIMESTAMP_MILLIS, timestampTenMinEarlierInMilliseconds);
		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliseconds)
		.build();
		lifecycleV2.pause(testEvent);

		Thread.sleep(STATE_UPDATE_TIMEOUT_MILLIS + 10);
		assertEquals(currentTimestampInMilliseconds, lifecycleDataStore.getLong(DATASTORE_KEY_APP_PAUSE_TIMESTAMP_MILLIS, 0));
		assertEquals(0, mockBuilder.buildAppLaunchXDMDataCalledTimes);
		assertEquals(1, mockBuilder.buildAppCloseXDMDataCalledTimes);

		assertEquals(timestampTenMinEarlierInMilliseconds, mockBuilder.buildAppCloseXDMDataLastParams.get(0));
		assertEquals(currentTimestampInMilliseconds, mockBuilder.buildAppCloseXDMDataLastParams.get(1));
		assertEquals(currentTimestampInMilliseconds, mockBuilder.buildAppCloseXDMDataLastParams.get(2));
		assertEquals(false, mockBuilder.buildAppCloseXDMDataLastParams.get(3));
	}

	@Test
	public void pause_consecutivePauseEvents_updatesLastTime() throws Exception {
		mockPersistence(timestampTenMinEarlierInMilliseconds, timestampOneDayEarlierMilliseconds,
						timestampOneDayEarlierMilliseconds, false,
						false);
		mockBuilder = new MockLifecycleV2MetricsBuilder(mockSystemInfoService);
		lifecycleV2 = new LifecycleV2Extension(lifecycleDataStore, mockSystemInfoService, lifecycleV2DispatcherApplicationState,
											   mockBuilder);

		long timestampTwoSecondsEarlierMilliseconds = currentTimestampInMilliseconds - 2000;
		Event testEvent1 = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(timestampTwoSecondsEarlierMilliseconds)
		.build();
		lifecycleV2.pause(testEvent1);
		Event testEvent2 = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliseconds)
		.build();
		lifecycleV2.pause(testEvent2);

		Thread.sleep(STATE_UPDATE_TIMEOUT_MILLIS + 10);
		assertEquals(currentTimestampInMilliseconds, lifecycleDataStore.getLong(DATASTORE_KEY_APP_PAUSE_TIMESTAMP_MILLIS, 0));
		assertEquals(0, mockBuilder.buildAppLaunchXDMDataCalledTimes);
		assertEquals(1, mockBuilder.buildAppCloseXDMDataCalledTimes);

		assertEquals(timestampTenMinEarlierInMilliseconds, mockBuilder.buildAppCloseXDMDataLastParams.get(0));
		assertEquals(currentTimestampInMilliseconds, mockBuilder.buildAppCloseXDMDataLastParams.get(1));
		assertEquals(currentTimestampInMilliseconds, mockBuilder.buildAppCloseXDMDataLastParams.get(2));
		assertEquals(false, mockBuilder.buildAppCloseXDMDataLastParams.get(3));
	}

	@Test
	public void updateLastKnownTimestamp_Happy() {
		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliseconds)
		.build();
		lifecycleV2.updateLastKnownTimestamp(testEvent);
		assertEquals(currentTimestampInMilliseconds, lifecycleDataStore.getLong(DATASTORE_KEY_APP_CLOSE_TIMESTAMP_MILLIS, 0));
	}

	private void mockPersistence(final long startTimeSec, final long pauseTimeSec, final long closeTimeSec,
								 final boolean isInstall, final boolean isUpgrade) {
		lifecycleDataStore.setLong(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS, startTimeSec);
		lifecycleDataStore.setLong(DATASTORE_KEY_APP_PAUSE_TIMESTAMP_MILLIS, pauseTimeSec);
		lifecycleDataStore.setLong(DATASTORE_KEY_APP_CLOSE_TIMESTAMP_MILLIS, closeTimeSec);

		if (!isInstall) {
			lifecycleDataStore.setLong(DATASTORE_KEY_INSTALL_DATE, timestampOneDayEarlierMilliseconds);
			lifecycleDataStore.setString(DATASTORE_KEY_LAST_APP_VERSION, mockInitAppVersion);
		}

		if (!isUpgrade) {
			lifecycleDataStore.setString(DATASTORE_KEY_LAST_APP_VERSION, mockAppVersion);
		}
	}

}
