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

package com.adobe.marketing.mobile;

import static com.adobe.marketing.mobile.LifecycleConstants.MAX_SESSION_LENGTH_SECONDS;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.ServiceProviderTestHelper;

@RunWith(AndroidJUnit4.class)
public class LifecycleFunctionalTest {

	private TestableExtensionApi mockExtensionApi;
    private MockDeviceInfoService mockDeviceInfoService;
	private NamedCollection lifecycleDataStore;
	private LifecycleExtension lifecycleExtension;

	private static final String ADDITIONAL_CONTEXT_DATA = "additionalcontextdata";
	private static final String APP_ID                  = "appid";
	private static final String CARRIER_NAME            = "carriername";
	private static final String CRASH_EVENT             = "crashevent";
	private static final String DAILY_ENGAGED_EVENT     = "dailyenguserevent";
	private static final String DAY_OF_WEEK             = "dayofweek";
	private static final String DAYS_SINCE_FIRST_LAUNCH = "dayssincefirstuse";
	private static final String DAYS_SINCE_LAST_LAUNCH  = "dayssincelastuse";
	private static final String DAYS_SINCE_LAST_UPGRADE = "dayssincelastupgrade";
	private static final String DEVICE_NAME             = "devicename";
	private static final String DEVICE_RESOLUTION       = "resolution";
	private static final String HOUR_OF_DAY             = "hourofday";
	private static final String IGNORED_SESSION_LENGTH  = "ignoredsessionlength";
	private static final String INSTALL_DATE            = "installdate";
	private static final String INSTALL_EVENT           = "installevent";
	private static final String LAUNCH_EVENT            = "launchevent";
	private static final String LAUNCHES                = "launches";
	private static final String LIFECYCLE_ACTION_KEY    = "action";
	private static final String LIFECYCLE_CONTEXT_DATA  = "lifecyclecontextdata";
	private static final String LIFECYCLE_PAUSE         = "pause";
	private static final String LIFECYCLE_START         = "start";
	private static final String LOCALE                  = "locale";
	private static final String MAX_SESSION_LENGTH      = "maxsessionlength";
	private static final String MONTHLY_ENGAGED_EVENT   = "monthlyenguserevent";
	private static final String OPERATING_SYSTEM        = "osversion";
	private static final String PREVIOUS_SESSION_LENGTH = "prevsessionlength";
	private static final String PREVIOUS_APPID		  = "previousappid";
	private static final String PREVIOUS_OS			  = "previousosversion";
	private static final String PREVIOUS_SESSION_PAUSE_TIMESTAMP = "previoussessionpausetimestampmillis";
	private static final String PREVIOUS_SESSION_START_TIMESTAMP = "previoussessionstarttimestampmillis";
	private static final String RUN_MODE                = "runmode";
	private static final String SESSION_EVENT           = "sessionevent";
	private static final String SESSION_START_TIMESTAMP = "starttimestampmillis";
	private static final String UPGRADE_EVENT           = "upgradeevent";
	private static final String DATA_STORE_NAME           = "AdobeMobile_Lifecycle";
	private static final String LIFECYCLE_CONFIG_SESSION_TIMEOUT = "lifecycle.sessionTimeout";

	private String dayOfWeek;
	private String hourOfDay;
	private String dayMonthYearDate;
	private long currentTimestampMillis;

	@Before
	public void beforeEach() {
		setupMockDeviceInfoService();
		ServiceProvider.getInstance().setContext(InstrumentationRegistry.getInstrumentation().getContext());
		ServiceProviderTestHelper.setDeviceInfoService(mockDeviceInfoService);
		lifecycleDataStore = ServiceProvider.getInstance().getDataStoreService().getNamedCollection(DATA_STORE_NAME);

		mockExtensionApi = new TestableExtensionApi();
		lifecycleExtension = new LifecycleExtension(mockExtensionApi);
		lifecycleExtension.onRegistered();
		mockExtensionApi.resetDispatchedEventAndCreatedSharedState();
		mockExtensionApi.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		mockExtensionApi.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		lifecycleDataStore.removeAll();
		initTimestamps();

		Map<String, Object> configurationMap = new HashMap<>();
		configurationMap.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 30L);
		mockExtensionApi.simulateSharedState("com.adobe.module.configuration", SharedStateStatus.SET, configurationMap);
	}

	private void setupMockDeviceInfoService() {
		mockDeviceInfoService = new MockDeviceInfoService();
		mockDeviceInfoService.applicationName = "TEST_APPLICATION_NAME";
		mockDeviceInfoService.applicationVersion = "1.1";
		mockDeviceInfoService.deviceName = "deviceName";
		mockDeviceInfoService.applicationVersionCode = "12345";
		mockDeviceInfoService.displayInformation = new DeviceInforming.DisplayInformation() {
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
				return 100;
			}
		};
		mockDeviceInfoService.deviceBuildId = "TEST_PLATFORM";
		mockDeviceInfoService.operatingSystemName = "TEST_OS";
		mockDeviceInfoService.operatingSystemVersion = "5.55";
		mockDeviceInfoService.mobileCarrierName = "TEST_CARRIER";
		mockDeviceInfoService.activeLocale = new Locale("en", "US");
		mockDeviceInfoService.runMode = "APPLICATION";
	}

	private void initTimestamps() {
		currentTimestampMillis = System.currentTimeMillis();
		final Date currentDate = new Date(currentTimestampMillis);

		dayOfWeek = getDayOfWeek(currentTimestampMillis);

		DateFormat hourOfDayDateFormat = new SimpleDateFormat("H");
		hourOfDay = hourOfDayDateFormat.format(currentDate);

		DateFormat dayMonthYearFormat = new SimpleDateFormat("M/d/yyyy");
		dayMonthYearDate = dayMonthYearFormat.format(currentDate);
	}

	private String getDayOfWeek(long timestampMillis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestampMillis);
		int dayOfWeekNumber = cal.get(Calendar.DAY_OF_WEEK);
		return Integer.toString(dayOfWeekNumber);
	}

	@Test
	public void testLifecycle__When__Start__Then__DispatchLifecycleContextDataUpdate_InstallEvent() {
		// test
		mockExtensionApi.simulateComingEvent(createStartEvent(null, currentTimestampMillis));

		// verify
		List<Event> events = mockExtensionApi.dispatchedEvents;
		assertEquals(1, events.size());

		List<Map<String, Object>> sharedStateList = mockExtensionApi.createdSharedState;
		assertEquals(1, sharedStateList.size());

		Event lifecycleEvent = events.get(0);
		assertEquals(EventType.LIFECYCLE, lifecycleEvent.getType());
		assertEquals(EventSource.RESPONSE_CONTENT, lifecycleEvent.getSource());
		assertEquals(0L, lifecycleEvent.getEventData().get(PREVIOUS_SESSION_START_TIMESTAMP));
		assertEquals(0L, lifecycleEvent.getEventData().get(PREVIOUS_SESSION_PAUSE_TIMESTAMP));
		assertEquals(MAX_SESSION_LENGTH_SECONDS, lifecycleEvent.getEventData().get(MAX_SESSION_LENGTH));
		assertEquals(TimeUnit.MILLISECONDS.toSeconds(currentTimestampMillis), lifecycleEvent.getEventData().get(SESSION_START_TIMESTAMP));
		assertEquals(LIFECYCLE_START, lifecycleEvent.getEventData().get(SESSION_EVENT));

		Map<String, Object> lifecycleSharedState = sharedStateList.get(0);
		assertEquals(MAX_SESSION_LENGTH_SECONDS, lifecycleSharedState.get(MAX_SESSION_LENGTH));
		assertEquals(TimeUnit.MILLISECONDS.toSeconds(currentTimestampMillis), lifecycleSharedState.get(SESSION_START_TIMESTAMP));

		Map<String, String> expectedContextData = new HashMap<String, String>() {
			{
				put(INSTALL_EVENT, "InstallEvent");
				put(LAUNCH_EVENT, "LaunchEvent");
				put(INSTALL_DATE, dayMonthYearDate);
				put(HOUR_OF_DAY, hourOfDay);
				put(DAY_OF_WEEK, dayOfWeek);
				put(LAUNCHES, "1");
				put(OPERATING_SYSTEM, "TEST_OS 5.55");
				put(LOCALE, "en-US");
				put(DEVICE_RESOLUTION, "100x100");
				put(CARRIER_NAME, "TEST_CARRIER");
				put(DEVICE_NAME, "deviceName");
				put(APP_ID, "TEST_APPLICATION_NAME 1.1 (12345)");
				put(RUN_MODE, "APPLICATION");
				put(DAILY_ENGAGED_EVENT, "DailyEngUserEvent");
				put(MONTHLY_ENGAGED_EVENT, "MonthlyEngUserEvent");
			}
		};
		assertEquals(expectedContextData, lifecycleEvent.getEventData().get(LIFECYCLE_CONTEXT_DATA));
		assertEquals(expectedContextData, sharedStateList.get(0).get(LIFECYCLE_CONTEXT_DATA));
		assertEquals(TimeUnit.MILLISECONDS.toSeconds(currentTimestampMillis), lifecycleDataStore.getLong("InstallDate", 0L));
	}

	@Test
	public void testLifecycle__When__Start__WithAdditionalData__Then__DispatchLifecycleContextDataUpdateWithAdditionalData() {
		// test
		Map<String, String> additionalContextData = new HashMap<>();
		additionalContextData.put("testKey", "testVal");
		mockExtensionApi.simulateComingEvent(createStartEvent(additionalContextData, currentTimestampMillis));

		// verify
		List<Event> events = mockExtensionApi.dispatchedEvents;
		assertEquals(1, events.size());

		List<Map<String, Object>> sharedStateList = mockExtensionApi.createdSharedState;
		assertEquals(1, sharedStateList.size());

		Map<String, String> lifecycleContextData = (Map<String, String>) events.get(0).getEventData().get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("testVal", lifecycleContextData.get("testKey"));
	}

	@Test
	public void testLifecycle__When__SecondLaunch_BeforeSessionTimeout__Then__GetNoLifecycleHit() {
		// setup
		long firstSessionStartTimeMillis = currentTimestampMillis;
		long firstSessionPauseTimeMillis = firstSessionStartTimeMillis +  TimeUnit.SECONDS.toMillis(10);
		long secondSessionStartTimeMillis = firstSessionStartTimeMillis + TimeUnit.SECONDS.toMillis(20);

		// test
		mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTimeMillis));
		mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTimeMillis));
		mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTimeMillis));

		// verify
		assertEquals(1, mockExtensionApi.dispatchedEvents.size());
		assertEquals(2, mockExtensionApi.createdSharedState.size());

		assertEquals(TimeUnit.MILLISECONDS.toSeconds(firstSessionStartTimeMillis), mockExtensionApi.createdSharedState.get(0).get(SESSION_START_TIMESTAMP));
		assertEquals(TimeUnit.MILLISECONDS.toSeconds(firstSessionStartTimeMillis) + 10, mockExtensionApi.createdSharedState.get(1).get(SESSION_START_TIMESTAMP));
	}

	@Test
	public void testLifecycle__When__Start__Then__DispatchLifecycleContextDataUpdate_SecondLaunchEvent() {
		// setup
		long firstSessionStartTimeMillis = currentTimestampMillis;
		long firstSessionPauseTimeMillis = firstSessionStartTimeMillis + 100L;
		long secondSessionStartTimeMillis = firstSessionStartTimeMillis + TimeUnit.SECONDS.toMillis(40);

		// test
		mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTimeMillis));
		mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTimeMillis));
		mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTimeMillis));

		// verify
		assertEquals(2, mockExtensionApi.dispatchedEvents.size());
		assertEquals(2, mockExtensionApi.createdSharedState.size());

		Map<String, String> expectedContextData = new HashMap<String, String>() {
			{
				put(LAUNCH_EVENT, "LaunchEvent");
				put(HOUR_OF_DAY, hourOfDay);
				put(DAY_OF_WEEK, dayOfWeek);
				put(LAUNCHES, "2");
				put(PREVIOUS_OS, "TEST_OS 5.55");
				put(OPERATING_SYSTEM, "TEST_OS 5.55");
				put(LOCALE, "en-US");
				put(DEVICE_RESOLUTION, "100x100");
				put(CARRIER_NAME, "TEST_CARRIER");
				put(DEVICE_NAME, "deviceName");
				put(PREVIOUS_APPID, "TEST_APPLICATION_NAME 1.1 (12345)");
				put(APP_ID, "TEST_APPLICATION_NAME 1.1 (12345)");
				put(RUN_MODE, "APPLICATION");
				put(DAYS_SINCE_FIRST_LAUNCH, "0");
				put(DAYS_SINCE_LAST_LAUNCH, "0");
				put(IGNORED_SESSION_LENGTH, "0");
			}
		};
		assertEquals(expectedContextData, mockExtensionApi.dispatchedEvents.get(1).getEventData().get(LIFECYCLE_CONTEXT_DATA));
		assertEquals(expectedContextData, mockExtensionApi.createdSharedState.get(1).get(LIFECYCLE_CONTEXT_DATA));
	}

	@Test
	public void testLifecycle__When__SecondLaunch_AfterSessionTimeout__Then__GetLaunchEvent() {
		// setup
		long firstSessionStartTimeMillis = currentTimestampMillis;
		long firstSessionPauseTimeMillis = firstSessionStartTimeMillis +  TimeUnit.SECONDS.toMillis(10);
		long secondSessionStartTimeMillis = firstSessionStartTimeMillis + TimeUnit.SECONDS.toMillis(40);

		// test
		mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTimeMillis));
		mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTimeMillis));
		mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTimeMillis));

		// verify
		assertEquals(2, mockExtensionApi.dispatchedEvents.size());
		assertEquals(2, mockExtensionApi.createdSharedState.size());

		Map<String, String> lifecycleContextData1 = (Map<String, String>) mockExtensionApi.dispatchedEvents.get(0).getEventData().get(LIFECYCLE_CONTEXT_DATA);
		Map<String, String> lifecycleContextData2 = (Map<String, String>) mockExtensionApi.dispatchedEvents.get(1).getEventData().get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("1", lifecycleContextData1.get(LAUNCHES));
		assertEquals("2", lifecycleContextData2.get(LAUNCHES));

		assertEquals(TimeUnit.MILLISECONDS.toSeconds(firstSessionStartTimeMillis),
				mockExtensionApi.dispatchedEvents.get(1).getEventData().get(PREVIOUS_SESSION_START_TIMESTAMP));
		assertEquals(TimeUnit.MILLISECONDS.toSeconds(firstSessionPauseTimeMillis),
				mockExtensionApi.dispatchedEvents.get(1).getEventData().get(PREVIOUS_SESSION_PAUSE_TIMESTAMP));
		assertEquals(MAX_SESSION_LENGTH_SECONDS, mockExtensionApi.dispatchedEvents.get(1).getEventData().get(MAX_SESSION_LENGTH));
		assertEquals(LIFECYCLE_START, mockExtensionApi.dispatchedEvents.get(1).getEventData().get(SESSION_EVENT));

		assertEquals(TimeUnit.MILLISECONDS.toSeconds(firstSessionStartTimeMillis), mockExtensionApi.createdSharedState.get(0).get(SESSION_START_TIMESTAMP));
		assertEquals(TimeUnit.MILLISECONDS.toSeconds(secondSessionStartTimeMillis), mockExtensionApi.createdSharedState.get(1).get(SESSION_START_TIMESTAMP));

		assertEquals("10", lifecycleContextData2.get(PREVIOUS_SESSION_LENGTH));
		Map<String, String> lifecycleSharedSate = (Map<String, String>) mockExtensionApi.createdSharedState.get(1).get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("10", lifecycleSharedSate.get(PREVIOUS_SESSION_LENGTH));

	}

	@Test
	public void testLifecycle__When__Crash() {
		// setup
		Map<String, Object> configurationMap = new HashMap<>();
		configurationMap.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 1L);

		TestableExtensionApi mockExtensionApi2 = new TestableExtensionApi();
		mockExtensionApi2.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		mockExtensionApi2.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		mockExtensionApi2.simulateSharedState("com.adobe.module.configuration", SharedStateStatus.SET, configurationMap);

		// test
		mockExtensionApi.simulateComingEvent(createStartEvent(null, currentTimestampMillis));

		LifecycleExtension lifecycleSession2 = new LifecycleExtension(mockExtensionApi2);
		lifecycleSession2.onRegistered();
		mockExtensionApi2.simulateComingEvent(createStartEvent(null, currentTimestampMillis));

		// verify
		assertEquals(1, mockExtensionApi2.dispatchedEvents.size());

		Map<String, String> lifecycleContextData = (Map<String, String>) mockExtensionApi2.dispatchedEvents.get(0).getEventData().get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("CrashEvent", lifecycleContextData.get(CRASH_EVENT));
	}

	@Test
	public void testLifecycle__When__SecondLaunch_OverMaxSessionTime__Then__GetLaunchEvent_WithIgnoredSessionLength() {
		// setup
		long firstSessionStartTimeMillis = currentTimestampMillis;
		long firstSessionPauseTimeMillis = firstSessionStartTimeMillis + TimeUnit.DAYS.toMillis(8);
		long secondSessionStartTimeMillis = firstSessionPauseTimeMillis + TimeUnit.SECONDS.toMillis(40);

		// test
		mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTimeMillis));
		mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTimeMillis));
		mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTimeMillis));

		// verify
		assertEquals(2, mockExtensionApi.dispatchedEvents.size());
		assertEquals(2, mockExtensionApi.createdSharedState.size());

		Map<String, String> lifecycleContextData = (Map<String, String>) mockExtensionApi.dispatchedEvents.get(1).getEventData().get(LIFECYCLE_CONTEXT_DATA);
		assertEquals(String.valueOf(TimeUnit.DAYS.toSeconds(8)), lifecycleContextData.get(IGNORED_SESSION_LENGTH));
		Map<String, String> lifecycleSharedState = (Map<String, String>) mockExtensionApi.createdSharedState.get(1).get(LIFECYCLE_CONTEXT_DATA);
		assertEquals(String.valueOf(TimeUnit.DAYS.toSeconds(8)), lifecycleSharedState.get(IGNORED_SESSION_LENGTH));
		assertEquals(MAX_SESSION_LENGTH_SECONDS, mockExtensionApi.dispatchedEvents.get(1).getEventData().get(MAX_SESSION_LENGTH));
		assertEquals(MAX_SESSION_LENGTH_SECONDS, mockExtensionApi.createdSharedState.get(1).get(MAX_SESSION_LENGTH));
	}

	@Test
	public void testLifecycle__When__SecondLaunch_VersionNumberChanged__Then__GetUpgradeEvent() {
		// setup
		long firstSessionStartTimeMillis = currentTimestampMillis;
		long firstSessionPauseTimeMillis = firstSessionStartTimeMillis + TimeUnit.SECONDS.toMillis(10);
		long secondSessionStartTimeMillis = firstSessionPauseTimeMillis + TimeUnit.DAYS.toMillis(1);

		Map<String, Object> configurationMap = new HashMap<>();
		configurationMap.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 30L);

		TestableExtensionApi mockExtensionApi2 = new TestableExtensionApi();
		mockExtensionApi2.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		mockExtensionApi2.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		mockExtensionApi2.simulateSharedState("com.adobe.module.configuration", SharedStateStatus.SET, configurationMap);

		// test
		mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTimeMillis));
		mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTimeMillis));

		mockDeviceInfoService.applicationVersion = "1.2";
		LifecycleExtension lifecycleExtension2 = new LifecycleExtension(mockExtensionApi2);
		lifecycleExtension2.onRegistered();
		mockExtensionApi2.simulateComingEvent(createStartEvent(null, secondSessionStartTimeMillis));

		// verify
		Map<String, String> lifecycleContextData = (Map<String, String>) mockExtensionApi2.dispatchedEvents.get(0).getEventData().get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("UpgradeEvent", lifecycleContextData.get(UPGRADE_EVENT));
		assertEquals("TEST_APPLICATION_NAME 1.1 (12345)", lifecycleContextData.get(PREVIOUS_APPID));
		assertEquals("TEST_APPLICATION_NAME 1.2 (12345)", lifecycleContextData.get(APP_ID));
		assertEquals("2", lifecycleContextData.get(LAUNCHES));
		assertEquals("LaunchEvent", lifecycleContextData.get(LAUNCH_EVENT));
		assertEquals("LaunchEvent", lifecycleContextData.get(LAUNCH_EVENT));
		Map<String, String> lifecycleSharedState = (Map<String, String>) mockExtensionApi2.createdSharedState.get(0).get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("UpgradeEvent", lifecycleSharedState.get(UPGRADE_EVENT));

		assertEquals(TimeUnit.MILLISECONDS.toSeconds(firstSessionStartTimeMillis), lifecycleDataStore.getLong("InstallDate", 0L));
		assertEquals(TimeUnit.MILLISECONDS.toSeconds(secondSessionStartTimeMillis), lifecycleDataStore.getLong("UpgradeDate", 0L));
	}

	@Test
	public void testLifecycle__When__SecondLaunch_ThreeDaysAfterInstall__Then__DaysSinceFirstUseIs3() {
		// setup
		long firstSessionStartTime = currentTimestampMillis;
		long firstSessionPauseTime = firstSessionStartTime + TimeUnit.DAYS.toMillis(3);
		long secondSessionStartTime = firstSessionPauseTime + TimeUnit.SECONDS.toMillis(30);

		// test
		mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTime));
		mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTime));
		mockExtensionApi.resetDispatchedEventAndCreatedSharedState();
		mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTime));

		// verify
		Map<String, String> lifecycleContextData = (Map<String, String>) mockExtensionApi.dispatchedEvents.get(0).getEventData().get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("3", lifecycleContextData.get(DAYS_SINCE_FIRST_LAUNCH));
		assertEquals("DailyEngUserEvent", lifecycleContextData.get(DAILY_ENGAGED_EVENT));
		assertNull(lifecycleContextData.get(MONTHLY_ENGAGED_EVENT));
		Map<String, String> lifecycleSharedState = (Map<String, String>) mockExtensionApi.createdSharedState.get(0).get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("3", lifecycleSharedState.get(DAYS_SINCE_FIRST_LAUNCH));
		assertEquals("DailyEngUserEvent", lifecycleSharedState.get(DAILY_ENGAGED_EVENT));
		assertNull(lifecycleSharedState.get(MONTHLY_ENGAGED_EVENT));
	}

	@Test
	public void testLifecycle__When__SecondLaunch_ThreeDaysAfterLastUse__Then__DaysSinceLastUseIs3() {
		// setup
		long firstSessionStartTime = currentTimestampMillis;
		long firstSessionPauseTime = firstSessionStartTime + TimeUnit.SECONDS.toMillis(20);
		long secondSessionStartTime = firstSessionPauseTime + TimeUnit.DAYS.toMillis(3);

		// test
		mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTime));
		mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTime));
		mockExtensionApi.resetDispatchedEventAndCreatedSharedState();
		mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTime));

		// verify
		Map<String, String> lifecycleContextData = (Map<String, String>) mockExtensionApi.dispatchedEvents.get(0).getEventData().get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("3", lifecycleContextData.get(DAYS_SINCE_LAST_LAUNCH));
		Map<String, String> lifecycleSharedState = (Map<String, String>) mockExtensionApi.createdSharedState.get(0).get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("3", lifecycleSharedState.get(DAYS_SINCE_LAST_LAUNCH));
	}

	@Test
	public void testLifecycle__When__ThreeDaysAfterUpgrade__Then__DaysSinceLastUpgradeIs3() {
		// setup
		long firstSessionStartTimeMillis = currentTimestampMillis;
		long firstSessionPauseTimeMillis = firstSessionStartTimeMillis + TimeUnit.SECONDS.toMillis(20);
		long secondSessionStartTimeMillis = firstSessionPauseTimeMillis + TimeUnit.SECONDS.toMillis(30);
		long secondSessionPauseTimeMillis = secondSessionStartTimeMillis + TimeUnit.SECONDS.toMillis(20);
		long thirdSessionStartTimeMillis = secondSessionPauseTimeMillis + TimeUnit.DAYS.toMillis(3);

		// test
		mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTimeMillis));
		mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTimeMillis));
		mockExtensionApi.resetDispatchedEventAndCreatedSharedState();

		mockDeviceInfoService.applicationVersion = "1.2";
		mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTimeMillis));
		mockExtensionApi.simulateComingEvent(createPauseEvent(secondSessionPauseTimeMillis));
		mockExtensionApi.resetDispatchedEventAndCreatedSharedState();

		mockExtensionApi.simulateComingEvent(createStartEvent(null, thirdSessionStartTimeMillis));

		// verify
		Map<String, String> lifecycleContextData = (Map<String, String>) mockExtensionApi.dispatchedEvents.get(0).getEventData().get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("3", lifecycleContextData.get(DAYS_SINCE_LAST_UPGRADE));
		Map<String, String> lifecycleSharedState = (Map<String, String>) mockExtensionApi.createdSharedState.get(0).get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("3", lifecycleSharedState.get(DAYS_SINCE_LAST_UPGRADE));
	}

	@Test
	public void testLifecycle__When__SecondLaunch_OneMonthAfterInstall__Then__MonthlyEngUserEvent() {
		// setup
		long firstSessionStartTime = currentTimestampMillis;
		long firstSessionPauseTime = firstSessionStartTime + TimeUnit.SECONDS.toMillis(30);
		long secondSessionStartTime = firstSessionPauseTime + TimeUnit.DAYS.toMillis(30);

		// test
		mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTime));
		mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTime));
		mockExtensionApi.resetDispatchedEventAndCreatedSharedState();
		mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTime));

		// verify
		Map<String, String> lifecycleContextData = (Map<String, String>) mockExtensionApi.dispatchedEvents.get(0).getEventData().get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("MonthlyEngUserEvent", lifecycleContextData.get(MONTHLY_ENGAGED_EVENT));
		Map<String, String> lifecycleSharedState = (Map<String, String>) mockExtensionApi.createdSharedState.get(0).get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("MonthlyEngUserEvent", lifecycleSharedState.get(MONTHLY_ENGAGED_EVENT));
	}

	@Test
	public void testLifecycle__When__ForceCloseAndRestartWithinSessionTimeout_ThenRestorePreviousSession() {
		// setup
		long firstSessionStartTime = currentTimestampMillis;
		long firstSessionPauseTime = firstSessionStartTime + 100;

		Map<String, Object> configurationMap = new HashMap<>();
		configurationMap.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 30L);

		TestableExtensionApi mockExtensionApi2 = new TestableExtensionApi();
		mockExtensionApi2.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		mockExtensionApi2.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		mockExtensionApi2.simulateSharedState("com.adobe.module.configuration", SharedStateStatus.SET, configurationMap);

		// test
		mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTime));
		mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTime));

		LifecycleExtension lifecycleSession2 = new LifecycleExtension(mockExtensionApi2);
		lifecycleSession2.onRegistered();
		Event bootEvent = new Event.Builder(null, EventType.HUB, EventSource.BOOTED)
				.build();
		mockExtensionApi2.simulateComingEvent(bootEvent);

		// verify
		assertEquals(0, mockExtensionApi2.dispatchedEvents.size());
		assertEquals(1, mockExtensionApi2.createdSharedState.size());

		Map<String, String> expectedContextData = new HashMap<String, String>() {
			{
				put(INSTALL_EVENT, "InstallEvent");
				put(LAUNCH_EVENT, "LaunchEvent");
				put(INSTALL_DATE, dayMonthYearDate);
				put(HOUR_OF_DAY, hourOfDay);
				put(DAY_OF_WEEK, dayOfWeek);
				put(LAUNCHES, "1");
				put(OPERATING_SYSTEM, "TEST_OS 5.55");
				put(LOCALE, "en-US");
				put(DEVICE_RESOLUTION, "100x100");
				put(CARRIER_NAME, "TEST_CARRIER");
				put(DEVICE_NAME, "deviceName");
				put(APP_ID, "TEST_APPLICATION_NAME 1.1 (12345)");
				put(RUN_MODE, "APPLICATION");
				put(DAILY_ENGAGED_EVENT, "DailyEngUserEvent");
				put(MONTHLY_ENGAGED_EVENT, "MonthlyEngUserEvent");
			}
		};
		assertEquals(expectedContextData, mockExtensionApi2.createdSharedState.get(0).get(LIFECYCLE_CONTEXT_DATA));
	}

	private Event createStartEvent(final Map<String, String> additionalData, final long timestamp) {
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(LIFECYCLE_ACTION_KEY,
				LIFECYCLE_START);
		eventData.put(ADDITIONAL_CONTEXT_DATA, additionalData);
		return new Event.Builder(null, EventType.GENERIC_LIFECYCLE, EventSource.REQUEST_CONTENT)
				.setTimestamp(timestamp)
				.setEventData(eventData)
				.build();
	}

	private Event createPauseEvent(final long timestamp) {
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(LIFECYCLE_ACTION_KEY,
				LIFECYCLE_PAUSE);
		return new Event.Builder(null, EventType.GENERIC_LIFECYCLE, EventSource.REQUEST_CONTENT)
				.setTimestamp(timestamp)
				.setEventData(eventData)
				.build();
	}

}
