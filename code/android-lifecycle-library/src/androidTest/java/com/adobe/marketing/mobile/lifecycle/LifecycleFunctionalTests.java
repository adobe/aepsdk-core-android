/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 *//*

// TODO refactor by replacing Mockito with TestableExtensionRuntime
package com.adobe.marketing.mobile;

import org.codehaus.plexus.util.cli.Arg;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.services.DataStoring;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.services.ServiceProviderTestHelper;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LifecycleFunctionalTests {

	@Mock
	ExtensionApi extensionApi;

	@Mock
	DataStoring dataStoring;

	@Mock
	NamedCollection lifecycleDataStore;

	@Mock
	DeviceInforming deviceInfoService;

	private long currentTimestampInSeconds;
	private long currentTimestampInMilliSeconds;
	private long timestampOneSecEarlierInSeconds;
	private long timestampTenMinEarlierInSeconds;
	private LifecycleExtension lifecycle;

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
	private static final String CONTEXT_DATA_KEY_INSTALL_EVENT = "installevent";
	private static final String CONTEXT_DATA_KEY_DAILY_ENGAGED_EVENT = "dailyenguserevent";
	private static final String CONTEXT_DATA_KEY_MONTHLY_ENGAGED_EVENT = "monthlyenguserevent";
	private static final String CONTEXT_DATA_KEY_UPGRADE_EVENT = "upgradeevent";
	private static final String CONTEXT_DATA_KEY_IGNORED_SESSION_LENGTH = "ignoredsessionlength";
	private static final String CONTEXT_DATA_KEY_INSTALL_DATE            = "installdate";

	private static final String CONTEXT_DATA_VALUE_LAUNCH_EVENT          = "LaunchEvent";
	private static final String CONTEXT_DATA_VALUE_CRASH_EVENT          = "CrashEvent";
	private static final String CONTEXT_DATA_VALUE_INSTALL_EVENT = "InstallEvent";
	private static final String CONTEXT_DATA_VALUE_DAILY_ENG_USER_EVENT = "DailyEngUserEvent";
	private static final String CONTEXT_DATA_VALUE_MONTHLY_ENG_USER_EVENT = "MonthlyEngUserEvent";

	private static final String SESSION_START_TIMESTAMP = "starttimestampmillis";
	private static final String MAX_SESSION_LENGTH      = "maxsessionlength";
	private static final long MAX_SESSION_LENGTH_SECONDS = TimeUnit.DAYS.toSeconds(7);
	private static final String LIFECYCLE_CONTEXT_DATA  = "lifecyclecontextdata";
	private static final String SESSION_EVENT           = "sessionevent";
	private static final String PREVIOUS_SESSION_PAUSE_TIMESTAMP = "previoussessionpausetimestampmillis";
	private static final String PREVIOUS_SESSION_START_TIMESTAMP = "previoussessionstarttimestampmillis";
	private static final String ADDITIONAL_CONTEXT_DATA = "additionalcontextdata";

	private static final String LIFECYCLE_ACTION_KEY = "action";
	private static final String LIFECYCLE_PAUSE = "pause";
	private static final String LIFECYCLE_START = "start";

	private static final String LIFECYCLE_CONFIG_SESSION_TIMEOUT = "lifecycle.sessionTimeout";
	private static final String CONFIGURATION_MODULE_NAME = "com.adobe.module.configuration";

	private static final String IDENTITY_MODULE_NAME = "com.adobe.module.identity";
	private static final String ADVERTISING_IDENTIFIER = "advertisingidentifier";

	private static final String EVENT_TYPE_GENERIC_LIFECYCLE = "com.adobe.eventType.generic.lifecycle";
	private static final String EVENT_TYPE_LIFECYCLE = "com.adobe.eventType.lifecycle";
	private static final String EVENT_SOURCE_REQUEST_CONTENT = "com.adobe.eventSource.requestContent";


	@Before
	public void beforeEach() {
		ServiceProviderTestHelper.setDeviceInfoService(deviceInfoService);
		ServiceProviderTestHelper.setDataStoring(dataStoring);
		when(dataStoring.getNamedCollection(eq("AdobeMobile_Lifecycle"))).thenReturn(lifecycleDataStore);
		LifecycleTestHelper.initDeviceInfoService(deviceInfoService);

		Map<String, Object> configurationSharedState = new HashMap<>();
		configurationSharedState.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 200L);
		when(extensionApi.getSharedState(
				eq(CONFIGURATION_MODULE_NAME),
				any(),
				eq(false),
				eq(SharedStateResolution.ANY)
		)).thenReturn(new SharedStateResult(SharedStateStatus.SET, configurationSharedState));

		lifecycle = new LifecycleExtension(extensionApi);
		initTimestamps();
	}

	private void initTimestamps() {
		currentTimestampInMilliSeconds = System.currentTimeMillis();
		currentTimestampInSeconds = TimeUnit.MILLISECONDS.toSeconds(currentTimestampInMilliSeconds);
		timestampOneSecEarlierInSeconds = currentTimestampInSeconds - 1;
		timestampTenMinEarlierInSeconds = currentTimestampInSeconds - TimeUnit.MINUTES.toSeconds(10);
	}

	@Test
	public void readyForEvent_ConfigurationSharedStateSet() {
		Event event = new Event.Builder("Lifecycle_queueEvent_Happy",
				EVENT_TYPE_GENERIC_LIFECYCLE,
				EVENT_SOURCE_REQUEST_CONTENT)
				.setEventNumber(1)
				.build();

		assertTrue(lifecycle.readyForEvent(event));
	}

	@Test
	public void readyForEvent_ConfigurationSharedStateNotSet() {
		// set config shared state to pending
		when(extensionApi.getSharedState(
				eq(CONFIGURATION_MODULE_NAME),
				any(),
				eq(false),
				eq(SharedStateResolution.ANY)
		)).thenReturn(new SharedStateResult(SharedStateStatus.PENDING, new HashMap<>()));

		Event event = new Event.Builder("Lifecycle_queueEvent_Happy",
				EVENT_TYPE_GENERIC_LIFECYCLE,
				EVENT_SOURCE_REQUEST_CONTENT)
				.setEventNumber(1)
				.build();

		assertFalse(lifecycle.readyForEvent(event));
	}

	@Test
	public void handleLifecycleRequestEvent_LifecycleStart_DeviceInfo() {
		Event lifecycleStartEvent = createStartEvent(null, currentTimestampInMilliSeconds);

		lifecycle.handleLifecycleRequestEvent(lifecycleStartEvent);

		ArgumentCaptor<Event> dispatchEventCaptor = ArgumentCaptor.forClass(Event.class);
		verify(extensionApi, times(2)).dispatch(dispatchEventCaptor.capture());
		Map<String, String> dispatchEventContextData = (Map<String, String>) dispatchEventCaptor.getAllValues().get(0).getEventData().get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("100x100", dispatchEventContextData.get(CONTEXT_DATA_KEY_DEVICE_RESOLUTION));
		assertEquals("TEST_CARRIER", dispatchEventContextData.get(CONTEXT_DATA_KEY_CARRIER_NAME));
		assertEquals("TEST_OS 5.55",  dispatchEventContextData.get(CONTEXT_DATA_KEY_OPERATING_SYSTEM));
		assertEquals("deviceName",  dispatchEventContextData.get(CONTEXT_DATA_KEY_DEVICE_NAME));
		assertEquals("en-US",  dispatchEventContextData.get(CONTEXT_DATA_KEY_LOCALE));
		assertEquals("APPLICATION",  dispatchEventContextData.get(CONTEXT_DATA_KEY_RUN_MODE));
		assertEquals("TEST_APPLICATION_NAME 1.1 (12345)",  dispatchEventContextData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));

		ArgumentCaptor<Map<String, Object>> lifecycleStateCaptor = ArgumentCaptor.forClass(Map.class);
		verify(extensionApi, times(1)).createSharedState(lifecycleStateCaptor.capture(), any());
		Map<String, String> sharedStateContextData = (Map<String, String>) lifecycleStateCaptor.getValue().get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("100x100", sharedStateContextData.get(CONTEXT_DATA_KEY_DEVICE_RESOLUTION));
		assertEquals("TEST_CARRIER", sharedStateContextData.get(CONTEXT_DATA_KEY_CARRIER_NAME));
		assertEquals("TEST_OS 5.55",  sharedStateContextData.get(CONTEXT_DATA_KEY_OPERATING_SYSTEM));
		assertEquals("deviceName",  sharedStateContextData.get(CONTEXT_DATA_KEY_DEVICE_NAME));
		assertEquals("en-US",  sharedStateContextData.get(CONTEXT_DATA_KEY_LOCALE));
		assertEquals("APPLICATION",  sharedStateContextData.get(CONTEXT_DATA_KEY_RUN_MODE));
		assertEquals("TEST_APPLICATION_NAME 1.1 (12345)",  sharedStateContextData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
	}

	@Test
	public void handleLifecycleRequestEvent_LifecycleStart_FirstLaunch() {
		// Sunday, January 8, 2017 7:32:48.301 AM
		long startTimeInMillis = 1483889568301L;
		Event lifecycleStartEvent = createStartEvent(null, startTimeInMillis);

		lifecycle.handleLifecycleRequestEvent(lifecycleStartEvent);

		ArgumentCaptor<Event> dispatchEventCaptor = ArgumentCaptor.forClass(Event.class);
		verify(extensionApi, times(2)).dispatch(dispatchEventCaptor.capture());
		assertEquals(LIFECYCLE_START, dispatchEventCaptor.getAllValues().get(0).getEventData().get(SESSION_EVENT));
		assertEquals(TimeUnit.MILLISECONDS.toSeconds(startTimeInMillis), dispatchEventCaptor.getAllValues().get(0).getEventData().get(SESSION_START_TIMESTAMP));
		assertEquals(MAX_SESSION_LENGTH_SECONDS, dispatchEventCaptor.getAllValues().get(0).getEventData().get(MAX_SESSION_LENGTH));
		assertEquals(0L, dispatchEventCaptor.getAllValues().get(0).getEventData().get(PREVIOUS_SESSION_START_TIMESTAMP));
		assertEquals(0L, dispatchEventCaptor.getAllValues().get(0).getEventData().get(PREVIOUS_SESSION_PAUSE_TIMESTAMP));
		Map<String, String> dispatchEventContextData = (Map<String, String>) dispatchEventCaptor.getAllValues().get(0).getEventData().get(LIFECYCLE_CONTEXT_DATA);
		assertEquals(CONTEXT_DATA_VALUE_INSTALL_EVENT, dispatchEventContextData.get(CONTEXT_DATA_KEY_INSTALL_EVENT));
		assertEquals(CONTEXT_DATA_VALUE_LAUNCH_EVENT, dispatchEventContextData.get(CONTEXT_DATA_KEY_EVENT_LAUNCH));
		assertEquals(CONTEXT_DATA_VALUE_DAILY_ENG_USER_EVENT,  dispatchEventContextData.get(CONTEXT_DATA_KEY_DAILY_ENGAGED_EVENT));
		assertEquals(CONTEXT_DATA_VALUE_MONTHLY_ENG_USER_EVENT,  dispatchEventContextData.get(CONTEXT_DATA_KEY_MONTHLY_ENGAGED_EVENT));
		assertEquals("1/8/2017",  dispatchEventContextData.get(CONTEXT_DATA_KEY_INSTALL_DATE));
		assertEquals("1", dispatchEventContextData.get(CONTEXT_DATA_KEY_DAY_OF_WEEK));
		assertEquals("7", dispatchEventContextData.get(CONTEXT_DATA_KEY_HOUR_OF_DAY));

		ArgumentCaptor<Map<String, Object>> lifecycleStateCaptor = ArgumentCaptor.forClass(Map.class);
		verify(extensionApi, times(1)).createSharedState(lifecycleStateCaptor.capture(), any());
		assertEquals(TimeUnit.MILLISECONDS.toSeconds(startTimeInMillis), lifecycleStateCaptor.getValue().get(SESSION_START_TIMESTAMP));
		assertEquals(MAX_SESSION_LENGTH_SECONDS, lifecycleStateCaptor.getValue().get(MAX_SESSION_LENGTH));
		Map<String, String> sharedStateContextData = (Map<String, String>) lifecycleStateCaptor.getValue().get(LIFECYCLE_CONTEXT_DATA);
		assertEquals(CONTEXT_DATA_VALUE_INSTALL_EVENT, sharedStateContextData.get(CONTEXT_DATA_KEY_INSTALL_EVENT));
		assertEquals(CONTEXT_DATA_VALUE_LAUNCH_EVENT, sharedStateContextData.get(CONTEXT_DATA_KEY_EVENT_LAUNCH));
		assertEquals(CONTEXT_DATA_VALUE_DAILY_ENG_USER_EVENT,  sharedStateContextData.get(CONTEXT_DATA_KEY_DAILY_ENGAGED_EVENT));
		assertEquals(CONTEXT_DATA_VALUE_MONTHLY_ENG_USER_EVENT,  sharedStateContextData.get(CONTEXT_DATA_KEY_MONTHLY_ENGAGED_EVENT));
		assertEquals("1/8/2017",  sharedStateContextData.get(CONTEXT_DATA_KEY_INSTALL_DATE));
		assertEquals("1", sharedStateContextData.get(CONTEXT_DATA_KEY_DAY_OF_WEEK));
		assertEquals("7", sharedStateContextData.get(CONTEXT_DATA_KEY_HOUR_OF_DAY));

		ArgumentCaptor<Integer> launchesCaptor = ArgumentCaptor.forClass(Integer.class);
		verify(lifecycleDataStore, times(1)).setInt(eq(DATASTORE_KEY_LAUNCHES), launchesCaptor.capture());
		assertEquals(1, launchesCaptor.getValue().intValue());

		ArgumentCaptor<Long> installDateCaptor = ArgumentCaptor.forClass(Long.class);
		verify(lifecycleDataStore, times(1)).setLong(eq(DATASTORE_KEY_INSTALL_DATE), installDateCaptor.capture());
		assertEquals(TimeUnit.MILLISECONDS.toSeconds(startTimeInMillis), installDateCaptor.getValue().longValue());
	}

	@Test
	public void handleLifecycleRequestEvent_LifecycleStart_AdditionalData() {
		Map<String, String> additionalData = new HashMap<>();
		additionalData.put("testKey", "testVal");
		Event lifecycleStartEvent = createStartEvent(additionalData, currentTimestampInMilliSeconds);

		lifecycle.handleLifecycleRequestEvent(lifecycleStartEvent);

		ArgumentCaptor<Event> dispatchEventCaptor = ArgumentCaptor.forClass(Event.class);
		verify(extensionApi, times(2)).dispatch(dispatchEventCaptor.capture());
		Map<String, String> dispatchEventContextData = (Map<String, String>) dispatchEventCaptor.getAllValues().get(0).getEventData().get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("testVal", dispatchEventContextData.get("testKey"));
		verify(extensionApi, times(1)).createSharedState(anyMap(), any());
	}

	@Test
	public void handleLifecycleRequestEvent_StartPauseStart() {
		Map<String, Object> configurationSharedState = new HashMap<>();
		configurationSharedState.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 30L);
		when(extensionApi.getSharedState(
				eq(CONFIGURATION_MODULE_NAME),
				any(),
				eq(false),
				eq(SharedStateResolution.ANY)
		)).thenReturn(new SharedStateResult(SharedStateStatus.SET, configurationSharedState));

		Event startEvent1 = createStartEvent(null, 1660115186);
		Event pauseEvent1 = createPauseEvent(1660115186L + 10000);
		Event startEvent2 = createStartEvent(null, 1660115186 + 20000);

		lifecycle.handleLifecycleRequestEvent(startEvent1);
		when(lifecycleDataStore.getLong(eq(LifecycleConstants.DataStoreKeys.START_DATE), anyLong())).thenReturn(TimeUnit.MILLISECONDS.toSeconds(1660115186));
		lifecycle.handleLifecycleRequestEvent(pauseEvent1);
		when(lifecycleDataStore.getBoolean(eq(LifecycleConstants.DataStoreKeys.SUCCESSFUL_CLOSE), anyBoolean())).thenReturn(true);
		when(lifecycleDataStore.getLong(eq(LifecycleConstants.DataStoreKeys.PAUSE_DATE), anyLong())).thenReturn(TimeUnit.MILLISECONDS.toSeconds(1660115186) + 10);
		lifecycle.handleLifecycleRequestEvent(startEvent2);

		verify(extensionApi, times(2)).dispatch(any());

		ArgumentCaptor<Map<String, Object>> lifecycleStateCaptor = ArgumentCaptor.forClass(Map.class);
		verify(extensionApi, times(2)).createSharedState(lifecycleStateCaptor.capture(), any());
		assertEquals(TimeUnit.MILLISECONDS.toSeconds(1660115186), lifecycleStateCaptor.getAllValues().get(0).get(SESSION_START_TIMESTAMP));
		assertEquals(0L, lifecycleStateCaptor.getAllValues().get(1).get(SESSION_START_TIMESTAMP));
	}

	@Test
	public void handleLifecycleRequestEvent_StartPauseStart_OverTimeout() {
		Map<String, Object> configurationSharedState = new HashMap<>();
		configurationSharedState.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 30L);
		when(extensionApi.getSharedState(
				eq(CONFIGURATION_MODULE_NAME),
				any(),
				eq(false),
				eq(SharedStateResolution.ANY)
		)).thenReturn(new SharedStateResult(SharedStateStatus.SET, configurationSharedState));

		Event startEvent1 = createStartEvent(null, 1660115186);
		Event startEvent2 = createStartEvent(null, 1660115186 + 40);

		lifecycle.handleLifecycleRequestEvent(startEvent1);
		when(lifecycleDataStore.getBoolean(eq(LifecycleConstants.DataStoreKeys.SUCCESSFUL_CLOSE), anyBoolean())).thenReturn(true);
		when(lifecycleDataStore.getLong(eq(LifecycleConstants.DataStoreKeys.PAUSE_DATE), anyLong())).thenReturn(1660115186L + 10);
		lifecycle.handleLifecycleRequestEvent(startEvent2);

		ArgumentCaptor<Event> dispatchEventCaptor = ArgumentCaptor.forClass(Event.class);
		verify(extensionApi, times(4)).dispatch(dispatchEventCaptor.capture());
		assertEquals(LIFECYCLE_START, dispatchEventCaptor.getAllValues().get(3).getEventData().get(SESSION_EVENT));
		assertEquals(1660115186 + 40, dispatchEventCaptor.getAllValues().get(3).getEventData().get(SESSION_START_TIMESTAMP));
		assertEquals(MAX_SESSION_LENGTH_SECONDS, dispatchEventCaptor.getAllValues().get(3).getEventData().get(MAX_SESSION_LENGTH));
		assertEquals(1660115186, dispatchEventCaptor.getAllValues().get(3).getEventData().get(PREVIOUS_SESSION_START_TIMESTAMP));
		assertEquals(1660115186 + 10, dispatchEventCaptor.getAllValues().get(3).getEventData().get(PREVIOUS_SESSION_PAUSE_TIMESTAMP));

		ArgumentCaptor<Map<String, Object>> lifecycleStateCaptor = ArgumentCaptor.forClass(Map.class);
		verify(extensionApi, times(2)).createSharedState(lifecycleStateCaptor.capture(), any());
		assertEquals(TimeUnit.MILLISECONDS.toSeconds(1660115186), lifecycleStateCaptor.getAllValues().get(0).get(SESSION_START_TIMESTAMP));
		assertEquals(TimeUnit.MILLISECONDS.toSeconds(1660115186 + 40), lifecycleStateCaptor.getAllValues().get(1).get(SESSION_START_TIMESTAMP));

		ArgumentCaptor<Integer> launchesCaptor = ArgumentCaptor.forClass(Integer.class);
		verify(lifecycleDataStore, times(2)).setInt(eq(DATASTORE_KEY_LAUNCHES), launchesCaptor.capture());
		assertEquals(1, launchesCaptor.getAllValues().get(0).intValue());
		assertEquals(2, launchesCaptor.getAllValues().get(1).intValue());
	}

	@Test
	public void handleLifecycleRequestEvent_LifecyclePause() {
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(LIFECYCLE_ACTION_KEY, LIFECYCLE_PAUSE);
		Event lifecyclePauseEvent = new Event.Builder(null, EVENT_TYPE_LIFECYCLE, EVENT_SOURCE_REQUEST_CONTENT)
				.setTimestamp(currentTimestampInMilliSeconds)
				.setEventData(eventData)
				.setEventNumber(1)
				.build();

		lifecycle.handleLifecycleRequestEvent(lifecyclePauseEvent);

		ArgumentCaptor<Boolean> successfulCloseCaptor = ArgumentCaptor.forClass(Boolean.class);
		verify(lifecycleDataStore, times(1)).setBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), successfulCloseCaptor.capture());
		assertTrue(successfulCloseCaptor.getValue());

		ArgumentCaptor<Long> pauseDateCaptor = ArgumentCaptor.forClass(Long.class);
		verify(lifecycleDataStore, times(1)).setLong(eq(DATASTORE_KEY_PAUSE_DATE) ,pauseDateCaptor.capture());
		assertEquals(currentTimestampInSeconds, pauseDateCaptor.getValue().longValue());
	}

	@Test
	public void handleLifecycleRequestEvent_NullEventData() {
		Event lifecycleRequestEvent = new Event.Builder(null, EVENT_TYPE_LIFECYCLE, EVENT_SOURCE_REQUEST_CONTENT)
				.setTimestamp(currentTimestampInMilliSeconds)
				.setEventData(null)
				.setEventNumber(1)
				.build();
		lifecycle.handleLifecycleRequestEvent(lifecycleRequestEvent);
		verify(lifecycleDataStore, never()).setBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), anyBoolean());
		verify(lifecycleDataStore, never()).setLong(eq(DATASTORE_KEY_PAUSE_DATE), anyLong());
	}

	@Test
	public void handleLifecycleRequestEvent_InvalidEventData() {
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(LIFECYCLE_ACTION_KEY, "invalid_action");
		Event lifecycleRequestEvent = new Event.Builder(null, EVENT_TYPE_LIFECYCLE, EVENT_SOURCE_REQUEST_CONTENT)
				.setTimestamp(currentTimestampInMilliSeconds)
				.setEventData(eventData)
				.setEventNumber(1)
				.build();
		lifecycle.handleLifecycleRequestEvent(lifecycleRequestEvent);

		verify(lifecycleDataStore, never()).setBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), anyBoolean());
		verify(lifecycleDataStore, never()).setLong(eq(DATASTORE_KEY_PAUSE_DATE), anyLong());
	}

	@Test
	public void handleEventHubBootEvent_Happy() {
		Event eventHubBootEvent = new Event.Builder(null, EventType.TYPE_HUB, EventSource.TYPE_BOOTED)
				.setTimestamp(currentTimestampInMilliSeconds)
				.build();
		lifecycle.handleEventHubBootEvent(eventHubBootEvent);
		ArgumentCaptor<Map<String, Object>> lifecycleStateCaptor = ArgumentCaptor.forClass(Map.class);
		ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

		verify(extensionApi, times(1)).createSharedState(lifecycleStateCaptor.capture(), eventCaptor.capture());

		assertEquals(eventHubBootEvent, eventCaptor.getValue());
		assertEquals(0L, lifecycleStateCaptor.getValue().get(SESSION_START_TIMESTAMP));
		assertEquals(MAX_SESSION_LENGTH_SECONDS, lifecycleStateCaptor.getValue().get(MAX_SESSION_LENGTH));

		Map<String, String> actualContextData = (Map<String, String>) lifecycleStateCaptor.getValue().get(LIFECYCLE_CONTEXT_DATA);
		assertEquals("100x100", actualContextData.get(CONTEXT_DATA_KEY_DEVICE_RESOLUTION));
		assertEquals("TEST_CARRIER", actualContextData.get(CONTEXT_DATA_KEY_CARRIER_NAME));
		assertEquals("TEST_OS 5.55",  actualContextData.get(CONTEXT_DATA_KEY_OPERATING_SYSTEM));
		assertEquals("deviceName",  actualContextData.get(CONTEXT_DATA_KEY_DEVICE_NAME));
		assertEquals("en-US",  actualContextData.get(CONTEXT_DATA_KEY_LOCALE));
		assertEquals("APPLICATION",  actualContextData.get(CONTEXT_DATA_KEY_RUN_MODE));
		assertEquals("TEST_APPLICATION_NAME 1.1 (12345)",  actualContextData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
		assertNotNull(actualContextData.get(CONTEXT_DATA_KEY_DAY_OF_WEEK));
		assertNotNull(actualContextData.get(CONTEXT_DATA_KEY_HOUR_OF_DAY));
		assertNull(actualContextData.get(CONTEXT_DATA_KEY_INSTALL_EVENT));
	}

	@Test
	public void getAdvertisingIdentifier_NullEvent() {
		assertNull(lifecycle.getAdvertisingIdentifier(null));
	}

	@Test
	public void getAdvertisingIdentifier_Happy() {
		Map<String, Object> identitySharedState = new HashMap<>();
		identitySharedState.put(ADVERTISING_IDENTIFIER, "testAdid");
		when(extensionApi.getSharedState(
				eq(IDENTITY_MODULE_NAME),
				any(),
				eq(false),
				eq(SharedStateResolution.ANY)
		)).thenReturn(new SharedStateResult(SharedStateStatus.SET, identitySharedState));

		assertEquals("testAdid", lifecycle.getAdvertisingIdentifier(Event.SHARED_STATE_OLDEST));
	}

	private Event createStartEvent(final Map<String, String> additionalData, final long timestamp) {
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(LIFECYCLE_ACTION_KEY,
				LIFECYCLE_START);
		eventData.put(ADDITIONAL_CONTEXT_DATA, additionalData);
		return new Event.Builder(null, EVENT_TYPE_LIFECYCLE, EVENT_SOURCE_REQUEST_CONTENT)
				.setTimestamp(timestamp)
				.setEventData(eventData)
				.setEventNumber(1)
				.build();
	}

	private Event createPauseEvent( final long timestamp) {
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(LIFECYCLE_ACTION_KEY,
				LIFECYCLE_PAUSE);
		return new Event.Builder(null, EVENT_TYPE_LIFECYCLE, EVENT_SOURCE_REQUEST_CONTENT)
				.setTimestamp(timestamp)
				.setEventData(eventData)
				.setEventNumber(1)
				.build();
	}
}
*/
