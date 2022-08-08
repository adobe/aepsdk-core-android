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

import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.adobe.marketing.mobile.Assertions.assertMapContains;
import static com.adobe.marketing.mobile.EventAssertions.assertEvent;
import static com.adobe.marketing.mobile.EventAssertions.assertEventDataContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LifecycleModuleTest extends SystemTest {

	//private static final String ADDITIONAL_CONTEXT_DATA = "additionalcontextdata";
	private static final String APP_ID                  = "appid";
	private static final String CARRIER_NAME            = "carriername";
	//private static final String CRASH_EVENT             = "crashevent";
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
	//private static final String LAUNCHES_SINCE_UPGRADE  = "launchessinceupgrade";
	//private static final String LIFECYCLE_ACTION_KEY    = "action";
	private static final String LIFECYCLE_CONTEXT_DATA  = "lifecyclecontextdata";
	private static final String DATA                    = "data";
	private static final String XDM                     = "xdm";
	//private static final String LIFECYCLE_PAUSE         = "pause";
	//private static final String LIFECYCLE_START         = "start";
	private static final String LOCALE                  = "locale";
	private static final String MAX_SESSION_LENGTH      = "maxsessionlength";
	private static final String MONTHLY_ENGAGED_EVENT   = "monthlyenguserevent";
	private static final String OPERATING_SYSTEM        = "osversion";
	private static final String PREVIOUS_SESSION_LENGTH = "prevsessionlength";
	private static final String PREVIOUS_APPID		  = "previousappid";
	private static final String PREVIOUS_OS			  = "previousosversion";
	//private static final String PREVIOUS_SESSION_PAUSE_TIMESTAMP = "previoussessionpausetimestampmillis";
	//private static final String PREVIOUS_SESSION_START_TIMESTAMP = "previoussessionstarttimestampmillis";
	private static final String RUN_MODE                = "runmode";
	//private static final String SESSION_EVENT           = "sessionevent";
	private static final String SESSION_START_TIMESTAMP = "starttimestampmillis";
	private static final String UPGRADE_EVENT           = "upgradeevent";
	private static final long   MAX_SESSION_LENGTH_SECONDS = TimeUnit.DAYS.toSeconds(7);

	private MockSystemInfoService mockSystemInfoService;
	private String dayOfWeek;
	private String hourOfDay;
	private String dayMonthYearDate;
	private long currentTimestampMillis;

	private LifecycleInternal lifecycleInternal;
	Map<String, Object> environmentMap, deviceMap;

	@Before
	public void beforeEach() {
		eventHub.ignoreAllStateChangeEvents();

		try {
			eventHub.registerModule(LifecycleExtension.class);
			eventHub.finishModulesRegistration(new AdobeCallback<Void>() {
				@Override
				public void call(Void aVoid) {
					SystemTest.eventHubLatch.countDown();
				}
			});
			SystemTest.eventHubLatch.await(1, TimeUnit.SECONDS);

		} catch (Exception e) {
			e.printStackTrace();
		}

		lifecycleInternal = new LifecycleInternal(eventHub);

		mockSystemInfoService = platformServices.getMockSystemInfoService();

		environmentMap = new HashMap<String, Object>();
		environmentMap.put("carrier", "mockMobileCarrier");
		environmentMap.put("operatingSystemVersion", "mockOSVersion");
		environmentMap.put("operatingSystem", "mockOSName");
		environmentMap.put("type", "application");
		environmentMap.put("_dc", new HashMap<String, Object>() {
			{
				put("language", "en-US");
			}
		});

		deviceMap = new HashMap<String, Object>() {
			{
				put("model", "mockDeviceName");
				put("modelNumber", "mockDeviceBuildId");
				put("type", "mobile");
				put("manufacturer", "mockDeviceManufacturer");

			}
		};

		initTimestamps();
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

	private void startThenStopLifecycle(long startTimestampMillis, long intervalMillis, Map<String, String> contextData) {
		lifecycleInternal.startLifecycle(startTimestampMillis, contextData);
		lifecycleInternal.pauseLifecycle(startTimestampMillis + intervalMillis);
	}

	@SuppressWarnings("all")
	private void startThenStopLifecycle(long intervalSeconds, Map<String, String> contextData) {
		startThenStopLifecycle(currentTimestampMillis, intervalSeconds, contextData);
	}

	@Test
	public void testLifecycle__When__Start__Then__DispatchLifecycleContextDataUpdate_InstallEvent() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		eventHub.setExpectedEventCount(1);

		lifecycleInternal.configureLifecycle(2);
		startThenStopLifecycle(currentTimestampMillis, 3, null);

		List<Event> events = eventHub.getEvents();
		assertEquals(1, events.size());

		Event lifecycleEvent = events.get(0);
		assertEvent(lifecycleEvent, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT);
		Map<String, String> expectedContextData = new HashMap<String, String>() {
			{
				put(INSTALL_EVENT, "InstallEvent");
				put(LAUNCH_EVENT, "LaunchEvent");
				put(INSTALL_DATE, dayMonthYearDate);
				put(HOUR_OF_DAY, hourOfDay);
				put(DAY_OF_WEEK, dayOfWeek);
				put(LAUNCHES, "1");
				put(OPERATING_SYSTEM, "mockOSName mockOSVersion");
				put(LOCALE, "en-US");
				put(DEVICE_RESOLUTION, "0x0");
				put(CARRIER_NAME, "mockMobileCarrier");
				put(DEVICE_NAME, "mockDeviceName");
				put(APP_ID, "mockAppName mockAppVersion (mockAppVersionCode)");
				put(RUN_MODE, "Application");
				put(DAILY_ENGAGED_EVENT, "DailyEngUserEvent");
				put(MONTHLY_ENGAGED_EVENT, "MonthlyEngUserEvent");
			}
		};
		assertEquals(expectedContextData, lifecycleEvent.getData().optStringMap(LIFECYCLE_CONTEXT_DATA, null));
		assertEquals(expectedContextData, lifecycleInternal.getLatestLifecycleSharedState().optStringMap(LIFECYCLE_CONTEXT_DATA,
					 null));
	}

	@Test
	public void
	testLifecycleV2__When__Start__Then__DispatchLifecycleApplicationLaunchEvent_When__Pause__Then__DispatchLifecycleApplicationClose() {
		//setup
		Map<String, Object> applicationMap = new HashMap<String, Object>() {
			{
				put("id", "mockAppPackageName");
				put("name", "mockAppName");
				put("isLaunch", true);
				put("isInstall", true);
				put("version", "mockAppVersion (mockAppVersionCode)");
			}
		};

		Map<String, Object> expectedXDMLaunchData = new HashMap<String, Object>();
		expectedXDMLaunchData.put("environment", environmentMap);
		expectedXDMLaunchData.put("eventType", LifecycleV2Constants.XDMEventType.APP_LAUNCH);
		expectedXDMLaunchData.put("application", applicationMap);
		expectedXDMLaunchData.put("device", deviceMap);
		expectedXDMLaunchData.put("timestamp", LifecycleUtil.dateTimeISO8601String(new Date(currentTimestampMillis)));

		Map<String, Object> expectedXDMCloseData = new HashMap<String, Object>();
		expectedXDMCloseData.put("eventType", LifecycleV2Constants.XDMEventType.APP_CLOSE);
		expectedXDMCloseData.put("application", new HashMap<String, Object>() {
			{
				put("isClose", true);
				put("closeType", "close");
				put("sessionLength", 1);
			}
		});
		expectedXDMCloseData.put("timestamp", LifecycleUtil.dateTimeISO8601String(new Date(currentTimestampMillis + 1000)));


		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT);
		eventHub.setExpectedEventCount(2);

		lifecycleInternal.configureLifecycle(2);

		// test
		startThenStopLifecycle(currentTimestampMillis, 1000, null);
		waitForThreadsWithFailIfTimedOut(1000);
		List<Event> events = eventHub.getEvents(2);
		assertEquals(2, events.size());

		// verify
		Event lifecycleApplicationLaunchEvent = events.get(0);
		Event lifecycleApplicationCloseEvent = events.get(1);
		assertEvent(lifecycleApplicationLaunchEvent, EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		assertEvent(lifecycleApplicationCloseEvent, EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);

		assertEquals(expectedXDMLaunchData, lifecycleApplicationLaunchEvent.getEventData().get(XDM));
		assertNull(lifecycleApplicationLaunchEvent.getEventData().get(DATA));
		assertEquals(expectedXDMCloseData, lifecycleApplicationCloseEvent.getEventData().get(XDM));
	}

	@Test
	public void
	testLifecycleV2__When__Start__Then__DispatchLifecycleApplicationLaunchEvent_When__WithFreeFormData() {
		//setup
		Map<String, Object> applicationMap = new HashMap<String, Object>() {
			{
				put("id", "mockAppPackageName");
				put("name", "mockAppName");
				put("isLaunch", true);
				put("isInstall", true);
				put("version", "mockAppVersion (mockAppVersionCode)");
			}
		};

		Map<String, Object> expectedXDMLaunchData = new HashMap<String, Object>();
		expectedXDMLaunchData.put("environment", environmentMap);
		expectedXDMLaunchData.put("eventType", LifecycleV2Constants.XDMEventType.APP_LAUNCH);
		expectedXDMLaunchData.put("application", applicationMap);
		expectedXDMLaunchData.put("device", deviceMap);
		expectedXDMLaunchData.put("timestamp", LifecycleUtil.dateTimeISO8601String(new Date(currentTimestampMillis)));

		Map<String, String> freeFormData = new HashMap<String, String>();
		freeFormData.put("key1", "value1");
		freeFormData.put("key2", "value2");

		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT);
		eventHub.setExpectedEventCount(2);

		lifecycleInternal.configureLifecycle(2);

		// test
		startThenStopLifecycle(currentTimestampMillis, 1000, freeFormData);
		waitForThreadsWithFailIfTimedOut(1000);
		List<Event> events = eventHub.getEvents(2);
		assertEquals(2, events.size());

		// verify
		Event lifecycleApplicationLaunchEvent = events.get(0);
		assertEvent(lifecycleApplicationLaunchEvent, EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);

		assertEquals(expectedXDMLaunchData, lifecycleApplicationLaunchEvent.getEventData().get(XDM));
		assertEquals(freeFormData, lifecycleApplicationLaunchEvent.getEventData().get(DATA));
	}

	@Test
	public void
	testLifecycleV2__When__SecondLaunch_VersionNumberChanged__Then__GetApplicationLaunchEvent__withIsUpgradeTrue() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT);

		Map<String, Object> applicationMap = new HashMap<String, Object>() {
			{
				put("id", "mockAppPackageName");
				put("name", "mockAppName");
				put("isLaunch", true);
				put("isUpgrade", true);
				put("version", "newVersion (mockAppVersionCode)");
			}
		};

		Map<String, Object> expectedXDMLaunchData = new HashMap<String, Object>();
		expectedXDMLaunchData.put("environment", environmentMap);
		expectedXDMLaunchData.put("eventType", LifecycleV2Constants.XDMEventType.APP_LAUNCH);
		expectedXDMLaunchData.put("application", applicationMap);
		expectedXDMLaunchData.put("device", deviceMap);
		expectedXDMLaunchData.put("timestamp", LifecycleUtil.dateTimeISO8601String(new Date(currentTimestampMillis + 5000)));

		Map<String, String> freeFormData = new HashMap<String, String>();
		freeFormData.put("key1", "value1");
		freeFormData.put("key2", "value2");

		eventHub.setExpectedEventCount(2);
		lifecycleInternal.configureLifecycle(2);
		mockSystemInfoService.applicationVersion = "previousVersion";
		startThenStopLifecycle(2000, null);
		List<Event> events = eventHub.getEvents(2); // to wait for both launch and close events
		eventHub.clearEvents();

		eventHub.setExpectedEventCount(2);
		mockSystemInfoService.applicationVersion = "newVersion";
		startThenStopLifecycle(currentTimestampMillis + 5000, 2000, freeFormData);

		// verify
		events = eventHub.getEvents(2);
		assertEquals(2, events.size());
		Event lifecycleApplicationLaunchEvent = events.get(0);
		assertEvent(lifecycleApplicationLaunchEvent, EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);

		assertEquals(expectedXDMLaunchData, lifecycleApplicationLaunchEvent.getEventData().get(XDM));
		assertEquals(freeFormData, lifecycleApplicationLaunchEvent.getEventData().get(DATA));
	}

	@Test
	public void
	testLifecycleV2__When__SecondLaunch_VersionNumberNotChanged__Then__GetApplicationLaunchEvent() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT);

		Map<String, Object> applicationMap = new HashMap<String, Object>() {
			{
				put("id", "mockAppPackageName");
				put("name", "mockAppName");
				put("isLaunch", true);
				put("version", "mockAppVersion (mockAppVersionCode)");
			}
		};

		Map<String, Object> expectedXDMLaunchData = new HashMap<String, Object>();
		expectedXDMLaunchData.put("environment", environmentMap);
		expectedXDMLaunchData.put("eventType", LifecycleV2Constants.XDMEventType.APP_LAUNCH);
		expectedXDMLaunchData.put("application", applicationMap);
		expectedXDMLaunchData.put("device", deviceMap);
		expectedXDMLaunchData.put("timestamp", LifecycleUtil.dateTimeISO8601String(new Date(currentTimestampMillis + 5000)));

		eventHub.setExpectedEventCount(2);
		lifecycleInternal.configureLifecycle(2);
		startThenStopLifecycle(2000, null);
		List<Event> events = eventHub.getEvents(2); // to wait for both launch and close events
		eventHub.clearEvents();

		eventHub.setExpectedEventCount(2);
		startThenStopLifecycle(currentTimestampMillis + 5000, 2000, null);

		// verify
		events = eventHub.getEvents(2);
		assertEquals(2, events.size());
		Event lifecycleApplicationLaunchEvent = events.get(0);
		assertEvent(lifecycleApplicationLaunchEvent, EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);

		assertEquals(expectedXDMLaunchData, lifecycleApplicationLaunchEvent.getEventData().get(XDM));
	}

	@Test
	public void testLifecycle__When__Start__Then__DispatchLifecycleContextDataUpdate_SecondLaunchEvent() throws Exception {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		eventHub.setExpectedEventCount(1);

		lifecycleInternal.configureLifecycle(1);
		startThenStopLifecycle(currentTimestampMillis, 5, null);
		waitForThreadsWithFailIfTimedOut(1000);

		List<Event> events = eventHub.getEvents();
		assertEquals(1, events.size());
		events.clear();

		Thread.sleep(1050);
		eventHub.setExpectedEventCount(1);

		startThenStopLifecycle(currentTimestampMillis + 1050, 1, null);
		waitForThreadsWithFailIfTimedOut(1000);
		events = eventHub.getEvents();
		assertEquals(1, events.size());

		Event lifecycleEvent = events.get(0);
		assertEvent(lifecycleEvent, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT);
		Map<String, String> expectedContextData = new HashMap<String, String>() {
			{
				put(LAUNCH_EVENT, "LaunchEvent");
				put(HOUR_OF_DAY, hourOfDay);
				put(DAY_OF_WEEK, dayOfWeek);
				put(LAUNCHES, "2");
				put(OPERATING_SYSTEM, "mockOSName mockOSVersion");
				put(LOCALE, "en-US");
				put(DEVICE_RESOLUTION, "0x0");
				put(CARRIER_NAME, "mockMobileCarrier");
				put(DEVICE_NAME, "mockDeviceName");
				put(PREVIOUS_APPID, "mockAppName mockAppVersion (mockAppVersionCode)");
				put(APP_ID, "mockAppName mockAppVersion (mockAppVersionCode)");
				put(PREVIOUS_OS, "mockOSName mockOSVersion");
				put(RUN_MODE, "Application");
				put(DAYS_SINCE_FIRST_LAUNCH, "0");
				put(DAYS_SINCE_LAST_LAUNCH, "0");
				put(IGNORED_SESSION_LENGTH, "0");
			}
		};
		assertEquals(expectedContextData, lifecycleEvent.getData().optStringMap(LIFECYCLE_CONTEXT_DATA, null));
		assertEquals(expectedContextData, lifecycleInternal.getLatestLifecycleSharedState().optStringMap(LIFECYCLE_CONTEXT_DATA,
					 null));
	}

	@Test
	public void testLifecycle__When__Start__Then__ShouldDispatchEvents() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);

		eventHub.setExpectedEventCount(2);
		lifecycleInternal.configureLifecycle(2);
		startThenStopLifecycle(currentTimestampMillis, 3, null);

		List<Event> events = eventHub.getEvents();
		assertEquals(1, events.size());
		assertEvent(events.get(0), EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT);
	}

	@Test
	public void testLifecycle__When__FirstLaunch__Then__GetInstallEvent() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		eventHub.setExpectedEventCount(1);

		lifecycleInternal.configureLifecycle(2);
		startThenStopLifecycle(currentTimestampMillis, 3, null);

		List<Event> events = eventHub.getEvents();
		assertEquals(1, events.size());

		Event lifecycleResponseContentEvent = events.get(0);
		assertEventDataContains(lifecycleResponseContentEvent, LIFECYCLE_CONTEXT_DATA);
		Map<String, String> contextData = lifecycleResponseContentEvent.getData().optStringMap(LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(contextData, INSTALL_EVENT, "InstallEvent");
		Map<String, String> lifecycleSharedState = lifecycleInternal.getLatestLifecycleSharedState().optStringMap(
					LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(lifecycleSharedState, INSTALL_EVENT, "InstallEvent");
	}

	@Test
	public void testLifecycle__When__SecondLaunch_AfterSessionTimeout__Then__GetLaunchEvent() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		eventHub.setExpectedEventCount(1);
		lifecycleInternal.configureLifecycle(2);
		startThenStopLifecycle(2000, null);
		eventHub.clearEvents();

		eventHub.setExpectedEventCount(1);
		startThenStopLifecycle(currentTimestampMillis + 4000, 2000, null);

		List<Event> events = eventHub.getEvents();
		assertEquals(1, events.size());
		Event lifecycleResponseContentEvent = events.get(0);
		assertEventDataContains(lifecycleResponseContentEvent, LIFECYCLE_CONTEXT_DATA);
		Map<String, String> contextData = lifecycleResponseContentEvent.getData().optStringMap(LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(contextData, LAUNCH_EVENT, "LaunchEvent");
		assertMapContains(contextData, LAUNCHES, "2");
		lifecycleInternal.getLatestLifecycleSharedState();
		Map<String, String> lifecycleSharedState = lifecycleInternal.getLatestLifecycleSharedState().optStringMap(
					LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(lifecycleSharedState, LAUNCH_EVENT, "LaunchEvent");
		assertMapContains(lifecycleSharedState, LAUNCHES, "2");
	}

	@Test
	public void testLifecycle__When__SecondLaunch_OverMaxSessionTime__Then__GetLaunchEvent_WithIgnoredSessionLength() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		eventHub.setExpectedEventCount(1);

		long firstSessionStartTimeMillis = currentTimestampMillis;
		long firstSessionPauseTimeMillis = firstSessionStartTimeMillis + TimeUnit.DAYS.toMillis(8);
		final long secondSessionStartTimeMillis = firstSessionPauseTimeMillis + TimeUnit.SECONDS.toMillis(3);
		long secondSessionPauseTimeMillis = secondSessionStartTimeMillis + TimeUnit.SECONDS.toMillis(2);

		lifecycleInternal.configureLifecycle(2);
		lifecycleInternal.startLifecycle(firstSessionStartTimeMillis, null);
		lifecycleInternal.pauseLifecycle(firstSessionPauseTimeMillis);
		eventHub.clearEvents();

		eventHub.setExpectedEventCount(1);
		lifecycleInternal.startLifecycle(secondSessionStartTimeMillis, null);
		lifecycleInternal.pauseLifecycle(secondSessionPauseTimeMillis);

		List<Event> events = eventHub.getEvents();
		assertEquals(1, events.size());

		Event lifecycleResponseContentEvent = events.get(0);
		assertEventDataContains(lifecycleResponseContentEvent, LIFECYCLE_CONTEXT_DATA);
		Map<String, String> contextData = lifecycleResponseContentEvent.getData().optStringMap(
											  LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(contextData, LAUNCH_EVENT, "LaunchEvent");
		assertMapContains(contextData, IGNORED_SESSION_LENGTH, "691200");
		assertMapContains(contextData, LAUNCHES, "2");
		Map<String, String> lifecycleSharedState = lifecycleInternal.getLatestLifecycleSharedState().optStringMap(
					LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(lifecycleSharedState, LAUNCH_EVENT, "LaunchEvent");
		assertMapContains(lifecycleSharedState, IGNORED_SESSION_LENGTH, "691200");
		assertMapContains(lifecycleSharedState, LAUNCHES, "2");
	}

	@Test
	public void testLifecycle__When__SecondLaunch_BeforeSessionTimeout__Then__GetNoLifecycleHit() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);

		eventHub.setExpectedEventCount(5);
		lifecycleInternal.configureLifecycle(2);
		startThenStopLifecycle(currentTimestampMillis, 2000, null);
		eventHub.clearEvents();

		eventHub.setExpectedEventCount(5);
		startThenStopLifecycle(currentTimestampMillis + 3000, 2000, null);

		List<Event> events = eventHub.getEvents();
		assertEquals(0, events.size());
	}

	@Test
	public void testLifecycle__When__SecondLaunch_VersionNumberChanged__Then__GetUpgradeEvent() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		eventHub.setExpectedEventCount(1);
		lifecycleInternal.configureLifecycle(2);
		mockSystemInfoService.applicationVersion = "previousVersion";
		mockSystemInfoService.operatingSystemName = "previousSystemName";
		startThenStopLifecycle(2000, null);
		eventHub.clearEvents();

		eventHub.setExpectedEventCount(1);
		mockSystemInfoService.applicationVersion = "newVersion";
		startThenStopLifecycle(currentTimestampMillis + 5000, 2000, null);

		List<Event> events = eventHub.getEvents();
		assertEquals(1, events.size());
		Event lifecycleResponseContentEvent = events.get(0);

		assertEventDataContains(lifecycleResponseContentEvent, LIFECYCLE_CONTEXT_DATA);
		Map<String, String> contextData = lifecycleResponseContentEvent.getData().optStringMap(
											  LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(contextData, UPGRADE_EVENT, "UpgradeEvent");
		assertMapContains(contextData, LAUNCH_EVENT, "LaunchEvent");
		assertMapContains(contextData, PREVIOUS_APPID, "mockAppName previousVersion (mockAppVersionCode)");
		assertMapContains(contextData, PREVIOUS_OS, "previousSystemName mockOSVersion");
		assertMapContains(contextData, LAUNCHES, "2");
		Map<String, String> lifecycleSharedState = lifecycleInternal.getLatestLifecycleSharedState().optStringMap(
					LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(lifecycleSharedState, UPGRADE_EVENT, "UpgradeEvent");
		assertMapContains(lifecycleSharedState, LAUNCH_EVENT, "LaunchEvent");
		assertMapContains(lifecycleSharedState, LAUNCHES, "2");
	}

	@Test
	public void testLifecycle__When__SecondLaunch__Then__CorrectNumberOfLaunches() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		eventHub.setExpectedEventCount(1);
		lifecycleInternal.configureLifecycle(2);
		startThenStopLifecycle(currentTimestampMillis, 2000, null);
		eventHub.clearEvents();

		eventHub.setExpectedEventCount(1);
		startThenStopLifecycle(currentTimestampMillis + 5000, 2000, null);

		List<Event> events = eventHub.getEvents();
		assertEquals(1, events.size());

		Event lifecycleResponseContentEvent = events.get(0);
		assertEventDataContains(lifecycleResponseContentEvent, LIFECYCLE_CONTEXT_DATA);
		Map<String, String> contextData = lifecycleResponseContentEvent.getData().optStringMap(
											  LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(contextData, LAUNCHES, "2");
	}

	@Test
	public void
	testLifecycle__When__SecondLaunch_SessionTimeLessThanMaxSessionLength__Then__ShouldContainPrevSessionLength() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		eventHub.setExpectedEventCount(1);
		lifecycleInternal.configureLifecycle(2);
		startThenStopLifecycle(currentTimestampMillis, 2000, null);
		eventHub.clearEvents();

		eventHub.setExpectedEventCount(1);
		startThenStopLifecycle(currentTimestampMillis + 5000, 2000, null);

		List<Event> events = eventHub.getEvents();
		assertEquals(1, events.size());

		Event lifecycleResponseContentEvent = events.get(0);
		assertEventDataContains(lifecycleResponseContentEvent, LIFECYCLE_CONTEXT_DATA);
		Map<String, String> contextData = lifecycleResponseContentEvent.getData().optStringMap(LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(contextData, PREVIOUS_SESSION_LENGTH, "2");
		Map<String, String> lifecycleSharedState = lifecycleInternal.getLatestLifecycleSharedState().optStringMap(
					LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(lifecycleSharedState, PREVIOUS_SESSION_LENGTH, "2");
	}

	@Test
	public void testLifecycle__When__SecondLaunch_ThreeDaysAfterInstall__Then__DaysSinceFirstUseIs3() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		eventHub.setExpectedEventCount(1);

		long firstSessionStartTime = currentTimestampMillis;
		long firstSessionPauseTime = firstSessionStartTime + TimeUnit.DAYS.toMillis(3);
		final long secondSessionStartTime = firstSessionPauseTime + 3000;
		long secondSessionPauseTime = secondSessionStartTime + 2000;

		lifecycleInternal.configureLifecycle(2);
		lifecycleInternal.startLifecycle(firstSessionStartTime, null);
		lifecycleInternal.pauseLifecycle(firstSessionPauseTime);
		eventHub.clearEvents();

		eventHub.setExpectedEventCount(1);
		lifecycleInternal.startLifecycle(secondSessionStartTime, null);
		lifecycleInternal.pauseLifecycle(secondSessionPauseTime);

		List<Event> events = eventHub.getEvents();
		assertEquals(1, events.size());

		Event lifecycleResponseContentEvent = events.get(0);
		assertEventDataContains(lifecycleResponseContentEvent, LIFECYCLE_CONTEXT_DATA);
		Map<String, String> contextData = lifecycleResponseContentEvent.getData().optStringMap(
											  LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(contextData, DAYS_SINCE_FIRST_LAUNCH, "3");
		Map<String, String> lifecycleSharedState = lifecycleInternal.getLatestLifecycleSharedState().optStringMap(
					LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(lifecycleSharedState, DAYS_SINCE_FIRST_LAUNCH, "3");
	}

	@Test
	public void testLifecycle__When__SecondLaunch_ThreeDaysAfterLastUse__Then__DaysSinceLastUseIs3() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		eventHub.setExpectedEventCount(1);

		long firstSessionStartTime = currentTimestampMillis;
		long firstSessionPauseTime = firstSessionStartTime + 2000;

		lifecycleInternal.configureLifecycle(2);
		lifecycleInternal.startLifecycle(firstSessionStartTime, null);
		lifecycleInternal.pauseLifecycle(firstSessionPauseTime);
		eventHub.clearEvents();

		eventHub.setExpectedEventCount(1);

		final long secondSessionStartTime = firstSessionPauseTime + TimeUnit.DAYS.toMillis(3);
		long secondSessionPauseTime = secondSessionStartTime + 2000;
		lifecycleInternal.startLifecycle(secondSessionStartTime, null);
		lifecycleInternal.pauseLifecycle(secondSessionPauseTime);

		List<Event> events = eventHub.getEvents();
		assertEquals(1, events.size());

		Event lifecycleResponseContentEvent = events.get(0);
		assertEventDataContains(lifecycleResponseContentEvent, LIFECYCLE_CONTEXT_DATA);
		Map<String, String> contextData = lifecycleResponseContentEvent.getData().optStringMap(LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(contextData, DAYS_SINCE_LAST_LAUNCH, "3");
	}

	@Test
	public void testLifecycle__When__ThreeDaysAfterUpgrade__Then__DaysSinceLastUpgradeIs3() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);

		long firstSessionStartTime = currentTimestampMillis;
		long firstSessionPauseTime = firstSessionStartTime + TimeUnit.SECONDS.toMillis(2);
		final long secondSessionStartTime = firstSessionPauseTime + TimeUnit.SECONDS.toMillis(3);
		long secondSessionPauseTime = secondSessionStartTime + TimeUnit.SECONDS.toMillis(2);
		final long thirdSessionStartTime = secondSessionPauseTime + TimeUnit.DAYS.toMillis(3);
		long thirdSessionPauseTime = thirdSessionStartTime + TimeUnit.SECONDS.toMillis(2);

		eventHub.setExpectedEventCount(1);
		lifecycleInternal.configureLifecycle(2);
		mockSystemInfoService.applicationVersion = "previousVersion";
		lifecycleInternal.startLifecycle(firstSessionStartTime, null);
		lifecycleInternal.pauseLifecycle(firstSessionPauseTime);
		eventHub.clearEvents();

		eventHub.setExpectedEventCount(1);
		mockSystemInfoService.applicationVersion = "newVersion";
		lifecycleInternal.startLifecycle(secondSessionStartTime, null);
		lifecycleInternal.pauseLifecycle(secondSessionPauseTime);
		eventHub.clearEvents();

		eventHub.setExpectedEventCount(1);
		lifecycleInternal.startLifecycle(thirdSessionStartTime, null);
		lifecycleInternal.pauseLifecycle(thirdSessionPauseTime);

		List<Event> events = eventHub.getEvents();
		assertEquals(1, events.size());

		Event lifecycleResponseContentEvent = events.get(0);
		assertEventDataContains(lifecycleResponseContentEvent, LIFECYCLE_CONTEXT_DATA);
		Map<String, String> contextData = lifecycleResponseContentEvent.getData().optStringMap(
											  LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(contextData, DAYS_SINCE_LAST_UPGRADE, "3");
	}

	@Test
	public void testLifecycle__When__NotConfigured__Then__ShouldQueueEvents() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		eventHub.setExpectedEventCount(1);

		startThenStopLifecycle(currentTimestampMillis, 3, null);

		List<Event> events = eventHub.getEvents();
		assertEquals(0, events.size());
	}

	@Test
	public void testLifecycle__When__ConfiguredAfterSession__Then__ShouldProcessQueuedEvents() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		eventHub.ignoreAllStateChangeEvents();
		eventHub.setExpectedEventCount(6);
		startThenStopLifecycle(currentTimestampMillis, 3000, new HashMap<String, String>() {
			{
				put("session0", "session0");
			}
		});
		startThenStopLifecycle(currentTimestampMillis + 7000, 3000, new HashMap<String, String>() {
			{
				put("session1", "session1");
			}
		});
		startThenStopLifecycle(currentTimestampMillis + 13000, 3000, new HashMap<String, String>() {
			{
				put("session2", "session2");
			}
		});
		eventHub.clearEvents();
		eventHub.setExpectedEventCount(3);

		lifecycleInternal.configureLifecycle(2);
		List<Event> events = eventHub.getEvents();
		assertEquals(3, events.size());

		Event lifecycleResponseContentEventSession0 = events.get(0);
		assertEventDataContains(lifecycleResponseContentEventSession0, LIFECYCLE_CONTEXT_DATA);
		Map<String, String> contextData = lifecycleResponseContentEventSession0.getData().optStringMap(LIFECYCLE_CONTEXT_DATA,
										  null);
		assertMapContains(contextData, "session0", "session0");

		Event lifecycleResponseContentEventSession1 = events.get(1);
		assertEventDataContains(lifecycleResponseContentEventSession1, LIFECYCLE_CONTEXT_DATA);
		contextData = lifecycleResponseContentEventSession1.getData().optStringMap(
						  LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(contextData, "session1", "session1");

		Event lifecycleResponseContentEventSession2 = events.get(2);
		assertEventDataContains(lifecycleResponseContentEventSession2, LIFECYCLE_CONTEXT_DATA);
		contextData = lifecycleResponseContentEventSession2.getData().optStringMap(
						  LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(contextData, "session2", "session2");
		Map<String, String> lifecycleSharedState = lifecycleInternal.getLatestLifecycleSharedState().optStringMap(
					LIFECYCLE_CONTEXT_DATA, null);
		assertMapContains(lifecycleSharedState, "session2", "session2");
	}

	@Test
	public void testLifecycle__When__Start__ContainSessionStartTimestamp__InstallEvent() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		eventHub.setExpectedEventCount(1);

		lifecycleInternal.configureLifecycle(2);
		startThenStopLifecycle(currentTimestampMillis, 3, null);

		List<Event> events = eventHub.getEvents();
		assertEquals(1, events.size());

		Event lifecycleEvent = events.get(0);
		assertEvent(lifecycleEvent, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT);


		long expectedSessionStart = currentTimestampMillis / 1000;
		assertEquals(expectedSessionStart, lifecycleEvent.getData().optLong(SESSION_START_TIMESTAMP, 0));
		assertEquals(expectedSessionStart, lifecycleInternal.getLatestLifecycleSharedState().optLong(SESSION_START_TIMESTAMP,
					 0));

		assertEquals(MAX_SESSION_LENGTH_SECONDS, lifecycleEvent.getData().optLong(MAX_SESSION_LENGTH, 0));
		assertEquals(MAX_SESSION_LENGTH_SECONDS, lifecycleInternal.getLatestLifecycleSharedState().optLong(MAX_SESSION_LENGTH,
					 0));
	}

	@Test
	public void
	testLifecycle__When__SecondLaunch__PauseDurationLessThanMaxSessionLength__ContainSessionStartTimestamp() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		eventHub.setExpectedEventCount(1);
		lifecycleInternal.configureLifecycle(10);
		startThenStopLifecycle(currentTimestampMillis, 2000, null);
		eventHub.clearEvents();

		eventHub.setExpectedEventCount(1);
		startThenStopLifecycle(currentTimestampMillis + 5000, 2000, null);

		List<Event> events = eventHub.getEvents();
		// No lifecycle response content.
		assertEquals(0, events.size());

		//  PreviousSessionStart  + PauseDuration
		long expectedSessionStart = (currentTimestampMillis + 3000) / 1000;
		assertEquals(expectedSessionStart, lifecycleInternal.getLatestLifecycleSharedState().optLong(SESSION_START_TIMESTAMP,
					 0));
		assertEquals(MAX_SESSION_LENGTH_SECONDS, lifecycleInternal.getLatestLifecycleSharedState().optLong(MAX_SESSION_LENGTH,
					 0));
	}

	@Test
	public void
	testLifecycle__When__SecondLaunch__PauseDurationMoreThanMaxSessionLength__ContainSessionStartTimestamp() {
		eventHub.ignoreEvents(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
		eventHub.ignoreEvents(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
		eventHub.setExpectedEventCount(1);
		lifecycleInternal.configureLifecycle(5);
		startThenStopLifecycle(currentTimestampMillis, 2000, null);
		eventHub.clearEvents();

		eventHub.setExpectedEventCount(1);
		startThenStopLifecycle(currentTimestampMillis + 8000, 2000, null);

		List<Event> events = eventHub.getEvents();
		// No lifecycle response content.
		assertEquals(1, events.size());
		assertEquals(1, events.size());

		Event lifecycleEvent = events.get(0);
		assertEvent(lifecycleEvent, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT);

		// 2nd session start time
		long expectedSessionStart = (currentTimestampMillis + 8000) / 1000;
		assertEquals(expectedSessionStart, lifecycleEvent.getData().optLong(SESSION_START_TIMESTAMP, 0));
		assertEquals(expectedSessionStart, lifecycleInternal.getLatestLifecycleSharedState().optLong(SESSION_START_TIMESTAMP,
					 0));

		assertEquals(MAX_SESSION_LENGTH_SECONDS, lifecycleEvent.getData().optLong(MAX_SESSION_LENGTH, 0));
		assertEquals(MAX_SESSION_LENGTH_SECONDS, lifecycleInternal.getLatestLifecycleSharedState().optLong(MAX_SESSION_LENGTH,
					 0));
	}
}
