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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class LifecycleExtensionTest extends BaseTest {

	private long currentTimestampInSeconds;
	private long currentTimestampInMilliSeconds;
	private long timestampOneSecEarlierInSeconds;
	private long timestampTenMinEarlierInSeconds;
	private long timestampOneHourEarlierInSeconds;
	private long timestampOneDayEarlierSeconds;
	private String dayOfWeek;
	private String hourOfDay;
	private String mockAppName = "TEST_APPLICATION_NAME";
	private String mockAppVersion = "1.1";
	private LifecycleExtension lifecycle;
	private LocalStorageService.DataStore lifecycleDataStore;
	private MockSystemInfoService mockSystemInfoService;

	private static final String DATASTORE_KEY_LIFECYCLE_DATA = "LifecycleData";
	private static final String DATASTORE_KEY_START_DATE = "SessionStart";
	private static final String DATASTORE_KEY_INSTALL_DATE = "InstallDate";
	private static final String DATASTORE_KEY_LAST_USED_DATE = "LastDateUsed";
	private static final String DATASTORE_KEY_LAUNCHES = "Launches";
	private static final String DATASTORE_KEY_LAST_VERSION = "LastVersion";
	private static final String DATASTORE_KEY_PAUSE_DATE = "PauseDate";
	private static final String DATASTORE_KEY_SUCCESSFUL_CLOSE = "SuccessfulClose";
	private static final String DATASTORE_KEY_OS_VERSION = "OsVersion";
	private static final String DATASTORE_KEY_APP_ID = "AppId";

	private static final String CONTEXT_DATA_KEY_EVENT_LAUNCH            = "launchevent";
	private static final String CONTEXT_DATA_KEY_CRASH_EVENT             = "crashevent";
	private static final String CONTEXT_DATA_KEY_LAUNCHES                = "launches";
	private static final String CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH = "dayssincefirstuse";
	private static final String CONTEXT_DATA_KEY_DAYS_SINCE_LAST_LAUNCH  = "dayssincelastuse";
	private static final String CONTEXT_DATA_KEY_HOUR_OF_DAY             = "hourofday";
	private static final String CONTEXT_DATA_KEY_DAY_OF_WEEK             = "dayofweek";
	private static final String CONTEXT_DATA_KEY_OPERATING_SYSTEM        = "osversion";
	private static final String CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER  = "appid";
	private static final String CONTEXT_DATA_KEY_DEVICE_NAME             = "devicename";
	private static final String CONTEXT_DATA_KEY_DEVICE_RESOLUTION       = "resolution";
	private static final String CONTEXT_DATA_KEY_CARRIER_NAME            = "carriername";
	private static final String CONTEXT_DATA_KEY_LOCALE                  = "locale";
	private static final String CONTEXT_DATA_KEY_RUN_MODE                = "runmode";
	private static final String CONTEXT_DATA_KEY_PREVIOUS_SESSION_LENGTH = "prevsessionlength";
	private static final String CONTEXT_DATA_VALUE_LAUNCH_EVENT          = "LaunchEvent";
	private static final String CONTEXT_DATA_KEY_PREVIOUS_OS_VERSION     = "previousosversion";
	private static final String CONTEXT_DATA_KEY_PREVIOUS_APP_ID         = "previousappid";
	private static final String EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA = "additionalcontextdata";


	@Before
	public void beforeEach() {
		super.beforeEach();

		eventHub.createSharedState("com.adobe.module.configuration",
								   Event.SHARED_STATE_OLDEST.getEventNumber(),
								   new EventData().putInteger("lifecycle.sessionTimeout", 200)
								  );
		lifecycle = new LifecycleExtension(eventHub, platformServices);
		lifecycleDataStore = platformServices.getLocalStorageService().getDataStore("AdobeMobile_Lifecycle");
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

		initTimestamps();
	}

	private void initTimestamps() {
		currentTimestampInMilliSeconds = System.currentTimeMillis();
		currentTimestampInSeconds = TimeUnit.MILLISECONDS.toSeconds(currentTimestampInMilliSeconds);
		timestampOneSecEarlierInSeconds = currentTimestampInSeconds - 1;
		timestampTenMinEarlierInSeconds = currentTimestampInSeconds - TimeUnit.MINUTES.toSeconds(10);
		timestampOneHourEarlierInSeconds = currentTimestampInSeconds - TimeUnit.HOURS.toSeconds(1);
		timestampOneDayEarlierSeconds = currentTimestampInSeconds - TimeUnit.DAYS.toSeconds(1);
		final Date currentDate = new Date(currentTimestampInMilliSeconds);

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(currentTimestampInMilliSeconds);
		int dayOfWeekNumber = cal.get(Calendar.DAY_OF_WEEK);
		dayOfWeek = Integer.toString(dayOfWeekNumber);

		DateFormat hourOfDayDateFormat = new SimpleDateFormat("H");
		hourOfDay = hourOfDayDateFormat.format(currentDate);
	}

	@After
	public void afterEach() {
		super.afterEach();
		lifecycleDataStore.removeAll();
	}

	@Test
	public void start_Happy() {
		lifecycleDataStore.setLong(DATASTORE_KEY_PAUSE_DATE, timestampOneSecEarlierInSeconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_START_DATE, timestampTenMinEarlierInSeconds);
		lifecycleDataStore.setString(DATASTORE_KEY_LAST_VERSION, mockAppVersion);
		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliSeconds)
		.setEventNumber(1)
		.build();
		lifecycle.start(testEvent, new EventData(), false);
		assertEquals(timestampTenMinEarlierInSeconds + 1, lifecycleDataStore.getLong(DATASTORE_KEY_START_DATE, 0));
		assertFalse(lifecycleDataStore.getBoolean(DATASTORE_KEY_SUCCESSFUL_CLOSE, false));
		assertFalse(lifecycleDataStore.contains(DATASTORE_KEY_PAUSE_DATE));
		assertEquals(mockAppVersion, lifecycleDataStore.getString(DATASTORE_KEY_LAST_VERSION, ""));
	}



	@Test
	public void start_PreviousSessionCrashed() {

		final String osVersion = "ios 10.2";
		final String appId = "app_id_123";
		lifecycleDataStore.setLong(DATASTORE_KEY_INSTALL_DATE, timestampOneDayEarlierSeconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_START_DATE, timestampTenMinEarlierInSeconds);
		lifecycleDataStore.setString(DATASTORE_KEY_LAST_VERSION, mockAppVersion);
		lifecycleDataStore.setBoolean(DATASTORE_KEY_SUCCESSFUL_CLOSE, false);
		lifecycleDataStore.setString(DATASTORE_KEY_OS_VERSION, osVersion);
		lifecycleDataStore.setString(DATASTORE_KEY_APP_ID, appId);
		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliSeconds)
		.setEventNumber(1)
		.build();

		lifecycle.start(testEvent, new EventData(), false);

		assertTrue(lifecycle.getContextData().containsKey(CONTEXT_DATA_KEY_CRASH_EVENT));
		assertEquals(lifecycle.getContextData().get(CONTEXT_DATA_KEY_PREVIOUS_OS_VERSION), osVersion);
		assertEquals(lifecycle.getContextData().get(CONTEXT_DATA_KEY_PREVIOUS_APP_ID), appId);
	}

	@Test
	public void start_PreviousAppId() {

		final String osVersion = "ios 10.2";
		final String appId = "app_id_123";
		lifecycleDataStore.setLong(DATASTORE_KEY_INSTALL_DATE, timestampOneDayEarlierSeconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_START_DATE, timestampTenMinEarlierInSeconds);
		lifecycleDataStore.setString(DATASTORE_KEY_LAST_VERSION, mockAppVersion);
		lifecycleDataStore.setBoolean(DATASTORE_KEY_SUCCESSFUL_CLOSE, true);
		lifecycleDataStore.setString(DATASTORE_KEY_OS_VERSION, osVersion);
		lifecycleDataStore.setString(DATASTORE_KEY_APP_ID, appId);
		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliSeconds)
		.setEventNumber(1)
		.build();

		lifecycle.start(testEvent, new EventData(), false);

		assertTrue(lifecycle.getContextData().containsKey(CONTEXT_DATA_KEY_EVENT_LAUNCH));
		assertEquals(lifecycle.getContextData().get(CONTEXT_DATA_KEY_PREVIOUS_OS_VERSION), osVersion);
		assertEquals(lifecycle.getContextData().get(CONTEXT_DATA_KEY_PREVIOUS_APP_ID), appId);
	}



	@Test
	public void start_AppResume_VersionUpgrade_NoLifecycleInMemory() {
		lifecycleDataStore.setLong(DATASTORE_KEY_PAUSE_DATE, timestampOneSecEarlierInSeconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_START_DATE, timestampTenMinEarlierInSeconds);
		lifecycleDataStore.setString(DATASTORE_KEY_LAST_VERSION, "1.2");
		final String mockLifecycleMapJson = "{\"appid\":\"NEW_APP_ID\"}";
		lifecycleDataStore.setString(DATASTORE_KEY_LIFECYCLE_DATA, mockLifecycleMapJson);
		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliSeconds)
		.setEventNumber(1)
		.build();
		lifecycle.start(testEvent, new EventData(), true);
		Map<String, String> lifecycleData = lifecycle.getContextData();
		assertEquals("NEW_APP_ID", lifecycleData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
	}

	@Test
	public void start_AppResume_VersionUpgrade_LifecycleIsInMemory() {
		lifecycleDataStore.getLong(DATASTORE_KEY_PAUSE_DATE, timestampOneSecEarlierInSeconds);
		lifecycleDataStore.getLong(DATASTORE_KEY_START_DATE, timestampTenMinEarlierInSeconds);
		lifecycleDataStore.getString(DATASTORE_KEY_LAST_VERSION, "1.2");
		Map<String, String> mockLifecycleData = new HashMap<String, String>();
		mockLifecycleData.put(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER, "A DIFFERENT APP ID");
		lifecycle.updateContextData(mockLifecycleData, 0);
		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliSeconds)
		.setEventNumber(1)
		.build();
		lifecycle.start(testEvent, new EventData(), true);

		Map<String, String> lifecycleData = lifecycle.getContextData();
		assertEquals("TEST_APPLICATION_NAME 1.1 (12345)", lifecycleData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
	}

	@Test
	public void start_AppResume_VersionsAreSame() {
		lifecycleDataStore.setLong(DATASTORE_KEY_PAUSE_DATE, timestampOneSecEarlierInSeconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_START_DATE, timestampTenMinEarlierInSeconds);
		lifecycleDataStore.setString(DATASTORE_KEY_LAST_VERSION, mockAppVersion);
		final String mockLifecycleMapJson = "{\"appid\":\"" + mockAppName + " " + mockAppVersion + " " + "\"}";
		lifecycleDataStore.setString(DATASTORE_KEY_LIFECYCLE_DATA, mockLifecycleMapJson);
		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliSeconds)
		.setEventNumber(1)
		.build();
		lifecycle.start(testEvent, new EventData(), true);
		Map<String, String> lifecycleData = lifecycle.getContextData();
		assertEquals(mockAppName + " " + mockAppVersion + " ",
					 lifecycleData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
	}

	@Test
	public void start_OverTimeout() {
		lifecycleDataStore.setLong(DATASTORE_KEY_PAUSE_DATE, timestampTenMinEarlierInSeconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_START_DATE, timestampOneHourEarlierInSeconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_INSTALL_DATE, timestampOneDayEarlierSeconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_LAST_USED_DATE, timestampTenMinEarlierInSeconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_INSTALL_DATE, timestampOneDayEarlierSeconds);
		lifecycleDataStore.setBoolean(DATASTORE_KEY_SUCCESSFUL_CLOSE, true);
		lifecycleDataStore.setString(DATASTORE_KEY_LAST_VERSION, mockAppVersion);
		lifecycleDataStore.setString("ADB_LIFETIME_VALUE", "5");
		Map<String, String> additionalData = new HashMap<String, String>();
		additionalData.put("testKey1", "testVal1");

		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliSeconds)
		.setData(new EventData().putStringMap(LifecycleConstants.EventDataKeys.Lifecycle.ADDITIONAL_CONTEXT_DATA,
											  additionalData))
		.setEventNumber(1)
		.build();
		lifecycle.start(testEvent, new EventData(), false);
		Map<String, String> expectedContextData = new HashMap<String, String>();
		expectedContextData.put(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER, "TEST_APPLICATION_NAME 1.1 (12345)");
		expectedContextData.put(CONTEXT_DATA_KEY_DEVICE_RESOLUTION, "100x100");
		expectedContextData.put(CONTEXT_DATA_KEY_CARRIER_NAME, "TEST_CARRIER");
		expectedContextData.put(CONTEXT_DATA_KEY_OPERATING_SYSTEM, "TEST_OS");
		expectedContextData.put(CONTEXT_DATA_KEY_DEVICE_NAME, "deviceName");
		expectedContextData.put(CONTEXT_DATA_KEY_DAY_OF_WEEK, dayOfWeek);
		expectedContextData.put(CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH, "1");
		expectedContextData.put(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_LAUNCH, "0");
		expectedContextData.put(CONTEXT_DATA_KEY_HOUR_OF_DAY, hourOfDay);
		expectedContextData.put(CONTEXT_DATA_KEY_LAUNCHES, "1");
		expectedContextData.put(CONTEXT_DATA_KEY_OPERATING_SYSTEM, "TEST_OS 5.55");
		expectedContextData.put(CONTEXT_DATA_KEY_EVENT_LAUNCH, CONTEXT_DATA_VALUE_LAUNCH_EVENT);
		expectedContextData.put(CONTEXT_DATA_KEY_LOCALE, "en-US");
		expectedContextData.put(CONTEXT_DATA_KEY_RUN_MODE, "Application");
		expectedContextData.put(CONTEXT_DATA_KEY_PREVIOUS_SESSION_LENGTH, "3000");
		expectedContextData.putAll(additionalData);

		assertEquals(1, lifecycleDataStore.getInt(DATASTORE_KEY_LAUNCHES, 0));
		assertEquals(currentTimestampInSeconds, lifecycleDataStore.getLong(DATASTORE_KEY_LAST_USED_DATE, 0));
		assertEquals(currentTimestampInSeconds, lifecycleDataStore.getLong(DATASTORE_KEY_START_DATE, 0));
		assertEquals(mockAppVersion, lifecycleDataStore.getString(DATASTORE_KEY_LAST_VERSION, null));
		assertFalse(lifecycleDataStore.getBoolean(DATASTORE_KEY_SUCCESSFUL_CLOSE, true));
		assertFalse(lifecycleDataStore.contains(DATASTORE_KEY_PAUSE_DATE));

		assertEquals(expectedContextData, lifecycle.getContextData());
	}

	@Test
	public void start_OverTimeout_AdditionalData() {
		lifecycleDataStore.setLong(DATASTORE_KEY_PAUSE_DATE, timestampTenMinEarlierInSeconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_START_DATE, timestampOneHourEarlierInSeconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_LAST_USED_DATE, timestampTenMinEarlierInSeconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_INSTALL_DATE, timestampOneDayEarlierSeconds);
		lifecycleDataStore.setBoolean(DATASTORE_KEY_SUCCESSFUL_CLOSE, true);
		lifecycleDataStore.setString("ADB_LIFETIME_VALUE", "5");
		lifecycleDataStore.setString(DATASTORE_KEY_LAST_VERSION, mockAppVersion);

		Map<String, String> additionalContextData = new HashMap<String, String>();
		additionalContextData.put("TEST_KEY1", "TEXT_VAL1");

		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliSeconds)
		.setData(new EventData().putStringMap(EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA,
											  additionalContextData))
		.setEventNumber(1)
		.build();
		lifecycle.start(testEvent, new EventData(), false);

		Map<String, String> expectedContextData = new HashMap<String, String>(additionalContextData);
		expectedContextData.put(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER, mockAppName);
		expectedContextData.put(CONTEXT_DATA_KEY_DEVICE_RESOLUTION, "100x100");
		expectedContextData.put(CONTEXT_DATA_KEY_CARRIER_NAME, "TEST_CARRIER");
		expectedContextData.put(CONTEXT_DATA_KEY_OPERATING_SYSTEM, "TEST_OS");
		expectedContextData.put(CONTEXT_DATA_KEY_DEVICE_NAME, "TEST_PLATFORM");
		expectedContextData.put(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER, mockAppName);
		expectedContextData.put(CONTEXT_DATA_KEY_DAY_OF_WEEK, dayOfWeek);
		expectedContextData.put(CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH, "1");
		expectedContextData.put(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_LAUNCH, "0");
		expectedContextData.put(CONTEXT_DATA_KEY_HOUR_OF_DAY, hourOfDay);
		expectedContextData.put(CONTEXT_DATA_KEY_LAUNCHES, "1");
		expectedContextData.put(CONTEXT_DATA_KEY_EVENT_LAUNCH, CONTEXT_DATA_VALUE_LAUNCH_EVENT);

		assertEquals(currentTimestampInSeconds, lifecycleDataStore.getLong(DATASTORE_KEY_LAST_USED_DATE, 0));
		assertEquals(currentTimestampInSeconds, lifecycleDataStore.getLong(DATASTORE_KEY_START_DATE, 0));
		assertEquals(mockAppVersion, lifecycleDataStore.getString(DATASTORE_KEY_LAST_VERSION, mockAppVersion));
		assertFalse(lifecycleDataStore.getBoolean(DATASTORE_KEY_SUCCESSFUL_CLOSE, true));
		assertFalse(lifecycleDataStore.contains(DATASTORE_KEY_PAUSE_DATE));
	}

	@Test
	public void pause_Happy() {
		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliSeconds)
		.setEventNumber(1)
		.build();
		lifecycle.pause(testEvent);
		assertTrue(lifecycleDataStore.getBoolean(DATASTORE_KEY_SUCCESSFUL_CLOSE, false));
		assertEquals(currentTimestampInSeconds, lifecycleDataStore.getLong(DATASTORE_KEY_PAUSE_DATE, 0));
	}

	@Test
	public void updateContextData_Happy() {
		Map<String, String> testMap = new HashMap<String, String>();
		testMap.put("updateContextDataTestKey", "updateContextDataTestValue");
		lifecycle.updateContextData(testMap, 0);
		Map<String, String> returnData = lifecycle.getContextData();
		assertEquals(returnData.get("updateContextDataTestKey"), "updateContextDataTestValue");
	}

	@Test
	public void updateContextData_NullParameter() {
		Map<String, String> testMap = new HashMap<String, String>();
		testMap.put("updateContextDataTestKey", "updateContextDataTestValue");
		lifecycle.updateContextData(testMap, 0);

		lifecycle.updateContextData(null, 0);

		Map<String, String> returnData = lifecycle.getContextData();
		assertEquals(returnData.get("updateContextDataTestKey"), "updateContextDataTestValue");
	}

	@Test
	public void removeContextData() {
		Map<String, String> testMap = new HashMap<String, String>();
		testMap.put("removeContextDataTestKey", "removeContextDataTestValue");
		lifecycle.updateContextData(testMap, 0);
		testMap.put("removeContextDataTestKey", null);
		lifecycle.updateContextData(testMap, 0);

		Map<String, String> returnData = lifecycle.getContextData();
		assertEquals(returnData.get("removeContextDataTestKey"), (null));
	}

	@Test
	public void checkForApplicationUpgrade_AppUpgrade_ExistingLifecycleDataNull() {
		Map<String, String> mockLifecycleData = new HashMap<String, String>();
		lifecycle.updateContextData(mockLifecycleData, 0);

		Map<String, String> coreData = new HashMap<String, String>();
		lifecycle.checkForApplicationUpgrade(coreData);

		Map<String, String> lifecycleData = lifecycle.getContextData();
		assertEquals(Collections.EMPTY_MAP, lifecycleData);
	}

	@Test
	public void checkForApplicationUpgrade_AppUpgrade_ExistingLifecycleDataEmpty() {
		Map<String, String> mockLifecycleData = new HashMap<String, String>();
		lifecycle.updateContextData(mockLifecycleData, 0);

		Map<String, String> coreData = new HashMap<String, String>();
		lifecycle.checkForApplicationUpgrade(coreData);

		Map<String, String> lifecycleData = lifecycle.getContextData();
		assertEquals(mockLifecycleData.size(), lifecycleData.size());
	}

	@Test
	public void checkForApplicationUpgrade_AppUpgrade_MapIsInMemory() {
		Map<String, String> mockLifecycleData = new HashMap<String, String>();
		mockLifecycleData.put(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER, "NEW_APP_ID");
		lifecycle.updateContextData(mockLifecycleData, 0);

		Map<String, String> coreData = new HashMap<String, String>();
		lifecycle.checkForApplicationUpgrade(coreData);

		Map<String, String> lifecycleData = lifecycle.getContextData();
		assertEquals("NEW_APP_ID", lifecycleData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
	}

	@Test
	public void checkForApplicationUpgrade_AppUpgrade_MapNotInMemory() {
		final String mockLifecycleMapJson = " {\"appid\":\"NEW_APP_ID\"}";
		lifecycleDataStore.setString(DATASTORE_KEY_LIFECYCLE_DATA, mockLifecycleMapJson);
		lifecycle.updateContextData(new HashMap<String, String>(), 0);

		Map<String, String> coreData = new HashMap<String, String>();
		lifecycle.checkForApplicationUpgrade(coreData);

		Map<String, String> lifecycleData = lifecycle.getContextData();
		assertEquals("NEW_APP_ID", lifecycleData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
	}

	@Test
	public void checkForApplicationUpgrade_AppUpgrade_MapInMemoryIsEmpty() {
		final String mockLifecycleMapJson = "{\"appid\":\"NEW_APP_ID\"}";
		lifecycleDataStore.setString(DATASTORE_KEY_LIFECYCLE_DATA, mockLifecycleMapJson);

		lifecycle.updateContextData(new HashMap<String, String>(), 0);

		Map<String, String> coreData = new HashMap<String, String>();
		lifecycle.checkForApplicationUpgrade(coreData);

		Map<String, String> lifecycleData = lifecycle.getContextData();
		assertEquals("NEW_APP_ID", lifecycleData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
	}

	@Test
	public void checkForApplicationUpgrade_AppUpgrade_Happy() {
		final String mockLifecycleMapJson = "{\"appid\":\"OLD_APP_ID\"}";
		lifecycleDataStore.setString(DATASTORE_KEY_LIFECYCLE_DATA, mockLifecycleMapJson);
		lifecycleDataStore.setString(DATASTORE_KEY_INSTALL_DATE, "mockDate");
		lifecycleDataStore.setString(DATASTORE_KEY_LAST_VERSION, "oldVersion");
		mockSystemInfoService.applicationVersion = "newVersion";

		Map<String, String> coreData = new HashMap<String, String>();
		coreData.put("appid", "NEW_APP_ID");
		lifecycle.checkForApplicationUpgrade(coreData);

		Map<String, String> lifecycleData = lifecycle.getContextData();
		assertEquals("NEW_APP_ID",
					 lifecycleData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
	}

	@Test
	public void queueEvent_Happy() {
		// set config shared state to avoid processing queued events for assertions
		eventHub.createSharedState("com.adobe.module.configuration",
								   1, EventHub.SHARED_STATE_PENDING);

		Event event = new Event.Builder("Lifecycle_queueEvent_Happy", EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setEventNumber(1)
		.build();
		lifecycle.queueEvent(event);
		lifecycle.queueEvent(event);

		assertEquals(2, lifecycle.getEventQueue().size());
	}

	@Test
	public void queueEvent_NullEvent() {
		lifecycle.queueEvent(null);

		assertEquals(0, lifecycle.getEventQueue().size());
	}

	@Test
	public void queueEvent_Happy_LifecycleStart() {
		lifecycleDataStore.setLong(DATASTORE_KEY_PAUSE_DATE, timestampOneSecEarlierInSeconds);
		lifecycleDataStore.setLong(DATASTORE_KEY_START_DATE, timestampTenMinEarlierInSeconds);
		lifecycleDataStore.setString(DATASTORE_KEY_LAST_VERSION, mockAppVersion);
		Event lifecycleStartEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliSeconds)
		.setData(new EventData().putString("action", "start"))
		.setEventNumber(1)
		.build();

		lifecycle.queueEvent(lifecycleStartEvent);

		assertEquals(timestampTenMinEarlierInSeconds + 1, lifecycleDataStore.getLong(DATASTORE_KEY_START_DATE, 0));
		assertFalse(lifecycleDataStore.getBoolean(DATASTORE_KEY_SUCCESSFUL_CLOSE, false));
		assertFalse(lifecycleDataStore.contains(DATASTORE_KEY_PAUSE_DATE));
		assertEquals(mockAppVersion, lifecycleDataStore.getString(DATASTORE_KEY_LAST_VERSION, ""));
	}

	@Test
	public void queueEvent_Happy_LifecyclePause() {
		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliSeconds)
		.setData(new EventData().putString("action", "pause"))
		.setEventNumber(1)
		.build();
		lifecycle.queueEvent(testEvent);

		assertTrue(lifecycleDataStore.getBoolean(DATASTORE_KEY_SUCCESSFUL_CLOSE, false));
		assertEquals(currentTimestampInSeconds, lifecycleDataStore.getLong(DATASTORE_KEY_PAUSE_DATE, 0));
	}

	@Test
	public void queueEvent_NullEventData() {
		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliSeconds)
		.setData(null)
		.setEventNumber(1)
		.build();
		lifecycle.queueEvent(testEvent);

		assertFalse(lifecycleDataStore.contains(DATASTORE_KEY_SUCCESSFUL_CLOSE));
		assertFalse(lifecycleDataStore.contains(DATASTORE_KEY_PAUSE_DATE));
	}

	@Test
	public void queueEvent_InvalidEventData() {
		Event testEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.REQUEST_CONTENT)
		.setTimestamp(currentTimestampInMilliSeconds)
		.setData(new EventData().putString("lifecycle.action", "invalid_action"))
		.setEventNumber(1)
		.build();
		lifecycle.queueEvent(testEvent);

		assertFalse(lifecycleDataStore.contains(DATASTORE_KEY_SUCCESSFUL_CLOSE));
		assertFalse(lifecycleDataStore.contains(DATASTORE_KEY_PAUSE_DATE));
	}

	@Test
	public void getAdvertisingIdentifier_NullEvent() {
		assertNull(lifecycle.getAdvertisingIdentifier(null));
	}

	@Test
	public void getAdvertisingIdentifier_Happy() {
		eventHub.setSharedState("com.adobe.module.identity",
								new EventData().putString("advertisingidentifier", "testAdid")
							   );

		assertEquals("testAdid", lifecycle.getAdvertisingIdentifier(Event.SHARED_STATE_OLDEST));
	}
}
