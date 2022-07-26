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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LifecycleSessionTest extends BaseTest {

	public static final String CONTEXT_DATA_KEY_PREV_SESSION_LENGTH    = "prevsessionlength";
	public static final String CONTEXT_DATA_KEY_IGNORED_SESSION_LENGTH = "ignoredsessionlength";
	public static final String DATA_STORE_KEY_LAST_KNOWN_TIMESTAMP     = "LastKnownTimestamp";

	private static final String CORE_DATA_KEY_OPERATING_SYSTEM = "osversion";
	private static final String CORE_DATA_KEY_APP_ID = "appid";

	private static final String CONTEXT_DATA_KEY_OS_VERSION = "OsVersion";
	private static final String CONTEXT_DATA_KEY_APP_ID = "AppId";



	private LifecycleSession session;
	private FakeDataStore    dataStore;
	private int sessionTimeoutInSeconds = (int) TimeUnit.MINUTES.toSeconds(5);
	private long currentTimestampInSeconds;
	private long timestampOneMinEarlierInSeconds;
	private long timestampTenMinEarlierInSeconds;
	private Map<String, String> coreData = new HashMap<String, String>();

	@Before
	public void beforeEach() {
		FakePlatformServices platformServices = new FakePlatformServices();
		final MockSystemInfoService mockSystemInfoService = (MockSystemInfoService) platformServices
				.getSystemInfoService();
		mockSystemInfoService.applicationVersion = "version";
		FakeLocalStorageService localStorageService = (FakeLocalStorageService) platformServices
				.getLocalStorageService();
		dataStore = (FakeDataStore) localStorageService.getDataStore("lifecycle");
		session = new LifecycleSession(dataStore);
		initTimestamps();
	}

	private void initTimestamps() {
		currentTimestampInSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		timestampOneMinEarlierInSeconds = currentTimestampInSeconds - TimeUnit.MINUTES.toSeconds(1);
		timestampTenMinEarlierInSeconds = currentTimestampInSeconds - TimeUnit.MINUTES.toSeconds(10);
	}

	@Test
	public void start_FirstLaunchSession() {
		LifecycleSession.SessionInfo previousSessionInfo = session.start(currentTimestampInSeconds, sessionTimeoutInSeconds,
				coreData);

		assertEquals(currentTimestampInSeconds, dataStore.getLong(LifecycleConstants.DataStoreKeys.START_DATE, 0));
		assertFalse(dataStore.contains(LifecycleConstants.DataStoreKeys.PAUSE_DATE));
		assertFalse(dataStore.getBoolean(LifecycleConstants.DataStoreKeys.SUCCESSFUL_CLOSE, true));
		assertEquals(1, dataStore.getInt(LifecycleConstants.DataStoreKeys.LAUNCHES, 0));

		assertEquals(0, previousSessionInfo.getPauseTimestampInSeconds());
		assertEquals(0, previousSessionInfo.getStartTimestampInSeconds());
		assertFalse(previousSessionInfo.isCrash());
	}

	@Test
	public void start_verifyAppIdAndOsVersion() {
		final String osVersion = "android 4.0";
		final String appId = "app-id-1234";

		coreData.put(CORE_DATA_KEY_OPERATING_SYSTEM, osVersion);
		coreData.put(CORE_DATA_KEY_APP_ID, appId);
		LifecycleSession.SessionInfo previousSessionInfo = session.start(currentTimestampInSeconds, sessionTimeoutInSeconds,
				coreData);

		assertEquals(dataStore.getString(CONTEXT_DATA_KEY_OS_VERSION, ""), osVersion);
		assertEquals(dataStore.getString(CONTEXT_DATA_KEY_APP_ID, ""), appId);
	}

	@Test
	public void start_NullDataStore() {
		session = new LifecycleSession(null);
		LifecycleSession.SessionInfo previousSessionInfo = session.start(currentTimestampInSeconds, sessionTimeoutInSeconds,
				coreData);

		assertFalse(dataStore.contains(LifecycleConstants.DataStoreKeys.START_DATE));
		assertFalse(dataStore.contains(LifecycleConstants.DataStoreKeys.PAUSE_DATE));
		assertFalse(dataStore.contains(LifecycleConstants.DataStoreKeys.LAST_VERSION));
		assertFalse(dataStore.contains(LifecycleConstants.DataStoreKeys.SUCCESSFUL_CLOSE));
		assertFalse(dataStore.contains(LifecycleConstants.DataStoreKeys.LAUNCHES));
		assertFalse(dataStore.contains(LifecycleConstants.DataStoreKeys.LAST_USED_DATE));
		assertNull(previousSessionInfo);
	}

	@Test
	public void start_LifecycleHasAlreadyRun() {
		session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);
		LifecycleSession.SessionInfo previousSessionInfo = session.start(currentTimestampInSeconds + TimeUnit.MINUTES
				.toSeconds(
					6),
				sessionTimeoutInSeconds, coreData);

		assertEquals(currentTimestampInSeconds, dataStore.getLong(LifecycleConstants.DataStoreKeys.START_DATE, 0));
		assertFalse(dataStore.contains(LifecycleConstants.DataStoreKeys.PAUSE_DATE));
		assertFalse(dataStore.getBoolean(LifecycleConstants.DataStoreKeys.SUCCESSFUL_CLOSE, true));
		assertEquals(1, dataStore.getInt(LifecycleConstants.DataStoreKeys.LAUNCHES, 0));

		assertNull(previousSessionInfo);
	}

	@Test
	public void start_NullSystemInfoService() {
		session = new LifecycleSession(dataStore);
		LifecycleSession.SessionInfo previousSessionInfo = session.start(currentTimestampInSeconds, sessionTimeoutInSeconds,
				coreData);

		assertEquals(currentTimestampInSeconds, dataStore.getLong(LifecycleConstants.DataStoreKeys.START_DATE, 0));
		assertFalse(dataStore.contains(LifecycleConstants.DataStoreKeys.PAUSE_DATE));
		assertFalse(dataStore.contains(LifecycleConstants.DataStoreKeys.LAST_VERSION));
		assertFalse(dataStore.getBoolean(LifecycleConstants.DataStoreKeys.SUCCESSFUL_CLOSE, true));
		assertEquals(1, dataStore.getInt(LifecycleConstants.DataStoreKeys.LAUNCHES, 0));

		assertEquals(0, previousSessionInfo.getPauseTimestampInSeconds());
		assertEquals(0, previousSessionInfo.getStartTimestampInSeconds());
		assertFalse(previousSessionInfo.isCrash());
	}

	@Test
	public void start_ResumeSession() {
		long previousSessionStartTimestamp = currentTimestampInSeconds;
		long previousSessionPauseTimestamp = currentTimestampInSeconds + TimeUnit.MINUTES.toSeconds(2);
		long newSessionStartTimestamp = currentTimestampInSeconds + TimeUnit.MINUTES.toSeconds(6);
		long previousSessionPauseTime = newSessionStartTimestamp - previousSessionPauseTimestamp;
		session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);
		session.pause(previousSessionPauseTimestamp);
		LifecycleSession.SessionInfo previousSessionInfo = session.start(newSessionStartTimestamp,
				sessionTimeoutInSeconds, coreData);

		assertEquals(previousSessionStartTimestamp + previousSessionPauseTime,
					 dataStore.getLong(LifecycleConstants.DataStoreKeys.START_DATE, 0));
		assertFalse(dataStore.contains(LifecycleConstants.DataStoreKeys.PAUSE_DATE));
		assertFalse(dataStore.getBoolean(LifecycleConstants.DataStoreKeys.SUCCESSFUL_CLOSE, true));

		assertNull(previousSessionInfo);
	}

	@Test
	public void start_SessionExpired() {
		long previousSessionStartTimestamp = currentTimestampInSeconds;
		long previousSessionPauseTimestamp = currentTimestampInSeconds + TimeUnit.MINUTES.toSeconds(2);
		long newSessionStartTimestamp = previousSessionPauseTimestamp + TimeUnit.MINUTES.toSeconds(6);
		session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);
		session.pause(previousSessionPauseTimestamp);
		LifecycleSession.SessionInfo previousSessionInfo = session.start(newSessionStartTimestamp,
				sessionTimeoutInSeconds, coreData);

		assertEquals(newSessionStartTimestamp, dataStore.getLong(LifecycleConstants.DataStoreKeys.START_DATE, 0));
		assertFalse(dataStore.contains(LifecycleConstants.DataStoreKeys.PAUSE_DATE));
		assertFalse(dataStore.getBoolean(LifecycleConstants.DataStoreKeys.SUCCESSFUL_CLOSE, true));
		assertEquals(2, dataStore.getInt(LifecycleConstants.DataStoreKeys.LAUNCHES, 0));

		assertEquals(previousSessionStartTimestamp, previousSessionInfo.getStartTimestampInSeconds());
		assertEquals(previousSessionPauseTimestamp, previousSessionInfo.getPauseTimestampInSeconds());
		assertFalse(previousSessionInfo.isCrash());
	}

	@Test
	public void pause_Happy() {
		session.pause(currentTimestampInSeconds);

		assertEquals(currentTimestampInSeconds, dataStore.getLong(LifecycleConstants.DataStoreKeys.PAUSE_DATE, 0));
		assertTrue(dataStore.getBoolean(LifecycleConstants.DataStoreKeys.SUCCESSFUL_CLOSE, false));
	}

	@Test
	public void pause_NullDataStore() {
		session = new LifecycleSession(null);
		session.pause(currentTimestampInSeconds);

		assertFalse(dataStore.contains(LifecycleConstants.DataStoreKeys.PAUSE_DATE));
		assertFalse(dataStore.contains(LifecycleConstants.DataStoreKeys.SUCCESSFUL_CLOSE));
	}

	@Test
	public void getSessionData_NotANewSession() {
		session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);

		LifecycleSession.SessionInfo previousSessionInfo = new LifecycleSession.SessionInfo
		(timestampTenMinEarlierInSeconds,
		 timestampOneMinEarlierInSeconds,
		 false);
		Map<String, String> sessionData = session.getSessionData(currentTimestampInSeconds, sessionTimeoutInSeconds,
										  previousSessionInfo);

		assertEquals(Collections.EMPTY_MAP, sessionData);
	}

	@Test
	public void getSessionData_NullDataStore() {
		session = new LifecycleSession(null);
		session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);

		LifecycleSession.SessionInfo previousSessionInfo = new LifecycleSession.SessionInfo
		(timestampTenMinEarlierInSeconds,
		 timestampOneMinEarlierInSeconds,
		 false);
		Map<String, String> sessionData = session.getSessionData(currentTimestampInSeconds, sessionTimeoutInSeconds,
										  previousSessionInfo);

		assertEquals(Collections.EMPTY_MAP, sessionData);
	}

	@Test
	public void getSessionData_DroppedSession() {
		dataStore.setLong(DATA_STORE_KEY_LAST_KNOWN_TIMESTAMP, timestampTenMinEarlierInSeconds);
		session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);

		LifecycleSession.SessionInfo previousSessionInfo = new LifecycleSession.SessionInfo(
			currentTimestampInSeconds - TimeUnit.DAYS.toSeconds(8),
			timestampTenMinEarlierInSeconds,
			false);
		Map<String, String> sessionData = session.getSessionData(currentTimestampInSeconds, sessionTimeoutInSeconds,
										  previousSessionInfo);

		Map<String, String> expectedSessionData = new HashMap<String, String>() {
			{
				put(CONTEXT_DATA_KEY_IGNORED_SESSION_LENGTH, "690600");
			}
		};
		assertEquals(expectedSessionData, sessionData);
	}

	@Test
	public void getSessionData_PreviousSessionValid() {
		dataStore.setLong(DATA_STORE_KEY_LAST_KNOWN_TIMESTAMP, timestampTenMinEarlierInSeconds);
		session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);

		LifecycleSession.SessionInfo previousSessionInfo = new LifecycleSession.SessionInfo(
			currentTimestampInSeconds - TimeUnit.DAYS.toSeconds(5),
			timestampTenMinEarlierInSeconds,
			false);
		Map<String, String> sessionData = session.getSessionData(currentTimestampInSeconds, sessionTimeoutInSeconds,
										  previousSessionInfo);

		Map<String, String> expectedSessionData = new HashMap<String, String>() {
			{
				put(CONTEXT_DATA_KEY_PREV_SESSION_LENGTH, "431400");
			}
		};
		assertEquals(expectedSessionData, sessionData);
	}

	@Test
	public void getSessionData_PreviousSessionInfoNull() {
		session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);

		Map<String, String> sessionData = session.getSessionData(currentTimestampInSeconds, sessionTimeoutInSeconds, null);

		assertEquals(Collections.EMPTY_MAP, sessionData);
	}

}
