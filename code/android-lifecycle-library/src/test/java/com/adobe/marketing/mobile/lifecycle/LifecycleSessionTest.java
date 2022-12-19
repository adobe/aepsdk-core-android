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

package com.adobe.marketing.mobile.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.services.NamedCollection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LifecycleSessionTest {

    private static final String DATASTORE_KEY_START_DATE = "SessionStart";
    private static final String DATASTORE_KEY_LAUNCHES = "Launches";
    private static final String DATASTORE_KEY_LAST_VERSION = "LastVersion";
    private static final String DATASTORE_KEY_PAUSE_DATE = "PauseDate";
    private static final String DATASTORE_KEY_SUCCESSFUL_CLOSE = "SuccessfulClose";
    private static final String DATASTORE_KEY_OS_VERSION = "OsVersion";
    private static final String DATASTORE_KEY_APP_ID = "AppId";
    public static final String DATA_STORE_KEY_LAST_KNOWN_TIMESTAMP = "LastKnownTimestamp";

    public static final String CONTEXT_DATA_KEY_PREV_SESSION_LENGTH = "prevsessionlength";
    public static final String CONTEXT_DATA_KEY_IGNORED_SESSION_LENGTH = "ignoredsessionlength";

    private static final String CORE_DATA_KEY_OPERATING_SYSTEM = "osversion";
    private static final String CORE_DATA_KEY_APP_ID = "appid";

    private LifecycleSession session;

    @Mock NamedCollection dataStore;

    private final int sessionTimeoutInSeconds = (int) TimeUnit.MINUTES.toSeconds(5);
    private long currentTimestampInSeconds;
    private long timestampOneMinEarlierInSeconds;
    private long timestampTenMinEarlierInSeconds;
    private final Map<String, String> coreData = new HashMap<>();

    @Before
    public void beforeEach() {
        session = new LifecycleSession(dataStore);
        initTimestamps();
    }

    private void initTimestamps() {
        currentTimestampInSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        timestampOneMinEarlierInSeconds = currentTimestampInSeconds - TimeUnit.MINUTES.toSeconds(1);
        timestampTenMinEarlierInSeconds =
                currentTimestampInSeconds - TimeUnit.MINUTES.toSeconds(10);
    }

    @Test
    public void testStart_FirstLaunchSession() {
        when(dataStore.getBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), anyBoolean()))
                .thenReturn(true);
        LifecycleSession.SessionInfo previousSessionInfo =
                session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);
        assertEquals(0, previousSessionInfo.getPauseTimestampInSeconds());
        assertEquals(0, previousSessionInfo.getStartTimestampInSeconds());
        assertFalse(previousSessionInfo.isCrash());

        ArgumentCaptor<Long> startDateCaptor = ArgumentCaptor.forClass(Long.class);
        verify(dataStore, times(1))
                .setLong(eq(DATASTORE_KEY_START_DATE), startDateCaptor.capture());
        assertEquals(currentTimestampInSeconds, startDateCaptor.getValue().longValue());

        ArgumentCaptor<Boolean> successfulCloseCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(dataStore, times(1))
                .setBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), successfulCloseCaptor.capture());
        assertFalse(successfulCloseCaptor.getValue());

        verify(dataStore, times(1)).remove(DATASTORE_KEY_PAUSE_DATE);
        verify(dataStore, never()).setString(eq(DATASTORE_KEY_LAST_VERSION), anyString());
    }

    @Test
    public void testStart_verifyAppIdAndOsVersion() {
        final String osVersion = "android 4.0";
        final String appId = "app-id-1234";

        when(dataStore.getBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), anyBoolean()))
                .thenReturn(true);

        coreData.put(CORE_DATA_KEY_OPERATING_SYSTEM, osVersion);
        coreData.put(CORE_DATA_KEY_APP_ID, appId);
        session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);

        ArgumentCaptor<String> osVersionCaptor = ArgumentCaptor.forClass(String.class);
        verify(dataStore, times(1))
                .setString(eq(DATASTORE_KEY_OS_VERSION), osVersionCaptor.capture());
        assertEquals(osVersion, osVersionCaptor.getValue());

        ArgumentCaptor<String> appIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(dataStore, times(1)).setString(eq(DATASTORE_KEY_APP_ID), appIdCaptor.capture());
        assertEquals(appId, appIdCaptor.getValue());
    }

    @Test
    public void testStart_NullDataStore() {
        session = new LifecycleSession(null);
        LifecycleSession.SessionInfo previousSessionInfo =
                session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);

        verifyNoInteractions(dataStore);
        assertNull(previousSessionInfo);
    }

    @Test
    public void testStart_LifecycleHasAlreadyRun() {
        when(dataStore.getBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), anyBoolean()))
                .thenReturn(true);
        session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);
        LifecycleSession.SessionInfo previousSessionInfo =
                session.start(
                        currentTimestampInSeconds + TimeUnit.MINUTES.toSeconds(6),
                        sessionTimeoutInSeconds,
                        coreData);

        ArgumentCaptor<Long> startDateCaptor = ArgumentCaptor.forClass(Long.class);
        verify(dataStore, times(1))
                .setLong(eq(DATASTORE_KEY_START_DATE), startDateCaptor.capture());
        assertEquals(currentTimestampInSeconds, startDateCaptor.getValue().longValue());

        ArgumentCaptor<Boolean> successfulCloseCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(dataStore, times(1))
                .setBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), successfulCloseCaptor.capture());
        assertFalse(successfulCloseCaptor.getValue());

        verify(dataStore, times(1)).remove(DATASTORE_KEY_PAUSE_DATE);

        ArgumentCaptor<Integer> launchesCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(dataStore, times(1)).setInt(eq(DATASTORE_KEY_LAUNCHES), launchesCaptor.capture());
        assertEquals(1, launchesCaptor.getValue().intValue());

        assertNull(previousSessionInfo);
    }

    @Test
    public void testStart_ResumeSession() {
        long previousSessionStartTimestamp = currentTimestampInSeconds;
        long previousSessionPauseTimestamp =
                currentTimestampInSeconds + TimeUnit.MINUTES.toSeconds(2);
        long newSessionStartTimestamp = currentTimestampInSeconds + TimeUnit.MINUTES.toSeconds(6);
        long previousSessionPauseTime = newSessionStartTimestamp - previousSessionPauseTimestamp;

        when(dataStore.getLong(eq(DATASTORE_KEY_START_DATE), anyLong()))
                .thenReturn(currentTimestampInSeconds);
        when(dataStore.getLong(eq(DATASTORE_KEY_PAUSE_DATE), anyLong()))
                .thenReturn(previousSessionPauseTimestamp);
        when(dataStore.getBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), anyBoolean()))
                .thenReturn(true);

        LifecycleSession.SessionInfo previousSessionInfo =
                session.start(newSessionStartTimestamp, sessionTimeoutInSeconds, coreData);

        ArgumentCaptor<Long> startDateCaptor = ArgumentCaptor.forClass(Long.class);
        verify(dataStore, times(1))
                .setLong(eq(DATASTORE_KEY_START_DATE), startDateCaptor.capture());
        assertEquals(
                previousSessionStartTimestamp + previousSessionPauseTime,
                startDateCaptor.getValue().longValue());

        ArgumentCaptor<Boolean> successfulCloseCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(dataStore, times(1))
                .setBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), successfulCloseCaptor.capture());
        assertFalse(successfulCloseCaptor.getValue());

        verify(dataStore, times(1)).remove(DATASTORE_KEY_PAUSE_DATE);

        assertNull(previousSessionInfo);
    }

    @Test
    public void testStart_SessionExpired() {
        long previousSessionStartTimestamp = currentTimestampInSeconds;
        long previousSessionPauseTimestamp =
                currentTimestampInSeconds + TimeUnit.MINUTES.toSeconds(2);
        long newSessionStartTimestamp =
                previousSessionPauseTimestamp + TimeUnit.MINUTES.toSeconds(6);

        when(dataStore.getLong(eq(DATASTORE_KEY_START_DATE), anyLong()))
                .thenReturn(currentTimestampInSeconds);
        when(dataStore.getLong(eq(DATASTORE_KEY_PAUSE_DATE), anyLong()))
                .thenReturn(previousSessionPauseTimestamp);
        when(dataStore.getBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), anyBoolean()))
                .thenReturn(true);
        when(dataStore.getInt(eq(DATASTORE_KEY_LAUNCHES), anyInt())).thenReturn(1);

        LifecycleSession.SessionInfo previousSessionInfo =
                session.start(newSessionStartTimestamp, sessionTimeoutInSeconds, coreData);

        ArgumentCaptor<Long> startDateCaptor = ArgumentCaptor.forClass(Long.class);
        verify(dataStore, times(1))
                .setLong(eq(DATASTORE_KEY_START_DATE), startDateCaptor.capture());
        assertEquals(newSessionStartTimestamp, startDateCaptor.getValue().longValue());

        ArgumentCaptor<Boolean> successfulCloseCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(dataStore, times(1))
                .setBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), successfulCloseCaptor.capture());
        assertFalse(successfulCloseCaptor.getValue());

        verify(dataStore, times(1)).remove(DATASTORE_KEY_PAUSE_DATE);

        ArgumentCaptor<Integer> launchesCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(dataStore, times(1)).setInt(eq(DATASTORE_KEY_LAUNCHES), launchesCaptor.capture());
        assertEquals(2, launchesCaptor.getValue().intValue());

        assertEquals(
                previousSessionStartTimestamp, previousSessionInfo.getStartTimestampInSeconds());
        assertEquals(
                previousSessionPauseTimestamp, previousSessionInfo.getPauseTimestampInSeconds());
        assertFalse(previousSessionInfo.isCrash());
    }

    @Test
    public void testPause_Happy() {
        session.pause(currentTimestampInSeconds);

        ArgumentCaptor<Long> pauseDateCaptor = ArgumentCaptor.forClass(Long.class);
        verify(dataStore, times(1))
                .setLong(eq(DATASTORE_KEY_PAUSE_DATE), pauseDateCaptor.capture());
        assertEquals(currentTimestampInSeconds, pauseDateCaptor.getValue().longValue());

        ArgumentCaptor<Boolean> successfulCloseCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(dataStore, times(1))
                .setBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), successfulCloseCaptor.capture());
        assertTrue(successfulCloseCaptor.getValue());
    }

    @Test
    public void testPause_NullDataStore() {
        session = new LifecycleSession(null);
        session.pause(currentTimestampInSeconds);

        verifyNoInteractions(dataStore);
    }

    @Test
    public void testGetSessionData_NotANewSession() {
        session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);

        LifecycleSession.SessionInfo previousSessionInfo =
                new LifecycleSession.SessionInfo(
                        timestampTenMinEarlierInSeconds, timestampOneMinEarlierInSeconds, false);
        Map<String, String> sessionData =
                session.getSessionData(
                        currentTimestampInSeconds, sessionTimeoutInSeconds, previousSessionInfo);

        assertEquals(Collections.EMPTY_MAP, sessionData);
    }

    @Test
    public void testGetSessionData_NullDataStore() {
        session = new LifecycleSession(null);
        session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);

        LifecycleSession.SessionInfo previousSessionInfo =
                new LifecycleSession.SessionInfo(
                        timestampTenMinEarlierInSeconds, timestampOneMinEarlierInSeconds, false);
        Map<String, String> sessionData =
                session.getSessionData(
                        currentTimestampInSeconds, sessionTimeoutInSeconds, previousSessionInfo);

        assertEquals(Collections.EMPTY_MAP, sessionData);
    }

    @Test
    public void testGetSessionData_DroppedSession() {
        dataStore.setLong(DATA_STORE_KEY_LAST_KNOWN_TIMESTAMP, timestampTenMinEarlierInSeconds);
        session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);

        LifecycleSession.SessionInfo previousSessionInfo =
                new LifecycleSession.SessionInfo(
                        currentTimestampInSeconds - TimeUnit.DAYS.toSeconds(8),
                        timestampTenMinEarlierInSeconds,
                        false);
        Map<String, String> sessionData =
                session.getSessionData(
                        currentTimestampInSeconds, sessionTimeoutInSeconds, previousSessionInfo);

        Map<String, String> expectedSessionData =
                new HashMap<String, String>() {
                    {
                        put(CONTEXT_DATA_KEY_IGNORED_SESSION_LENGTH, "690600");
                    }
                };
        assertEquals(expectedSessionData, sessionData);
    }

    @Test
    public void testGetSessionData_PreviousSessionValid() {
        dataStore.setLong(DATA_STORE_KEY_LAST_KNOWN_TIMESTAMP, timestampTenMinEarlierInSeconds);
        session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);

        LifecycleSession.SessionInfo previousSessionInfo =
                new LifecycleSession.SessionInfo(
                        currentTimestampInSeconds - TimeUnit.DAYS.toSeconds(5),
                        timestampTenMinEarlierInSeconds,
                        false);
        Map<String, String> sessionData =
                session.getSessionData(
                        currentTimestampInSeconds, sessionTimeoutInSeconds, previousSessionInfo);

        Map<String, String> expectedSessionData =
                new HashMap<String, String>() {
                    {
                        put(CONTEXT_DATA_KEY_PREV_SESSION_LENGTH, "431400");
                    }
                };
        assertEquals(expectedSessionData, sessionData);
    }

    @Test
    public void testGetSessionData_PreviousSessionInfoNull() {
        session.start(currentTimestampInSeconds, sessionTimeoutInSeconds, coreData);

        Map<String, String> sessionData =
                session.getSessionData(currentTimestampInSeconds, sessionTimeoutInSeconds, null);

        assertEquals(Collections.EMPTY_MAP, sessionData);
    }
}
