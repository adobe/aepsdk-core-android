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

import com.adobe.marketing.mobile.lifecycle.LifecycleConstants.DataStoreKeys;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.NamedCollection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for managing lifecycle sessions for standard, non-XDM scenarios. Persists start, pause, and
 * end timestamps for lifecycle sessions. Also generates session context data for Analytics
 * reporting
 */
class LifecycleSession {

    private static final String SELF_LOG_TAG = "LifecycleSession";
    private final NamedCollection dataStore;
    private boolean lifecycleHasRun;

    LifecycleSession(final NamedCollection dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Start a new lifecycle session. Returns a SessionInfo object containing the previous session's
     * data if it is a new session Returns null if the previous session is resumed, or if lifecycle
     * has already run
     *
     * @param startTimestampInSeconds long session start time (in seconds)
     * @param sessionTimeoutInSeconds int session timeout (in seconds)
     * @param coreData core data generated from LifecycleMetricsBuilder
     * @return SessionInfo object containing previous session's data
     */
    SessionInfo start(
            final long startTimestampInSeconds,
            final long sessionTimeoutInSeconds,
            final Map<String, String> coreData) {
        if (lifecycleHasRun) {
            return null;
        }

        if (dataStore == null) {
            Log.debug(
                    LifecycleConstants.LOG_TAG,
                    SELF_LOG_TAG,
                    "Failed to start session, %s (persisted data)",
                    Log.UNEXPECTED_NULL_VALUE);
            return null;
        }

        lifecycleHasRun = true;

        final long previousSessionStartTimeInSeconds =
                dataStore.getLong(DataStoreKeys.START_DATE, 0L);
        final long previousSessionPauseTimeInSeconds =
                dataStore.getLong(DataStoreKeys.PAUSE_DATE, 0L);
        final boolean previousSessionCrashed =
                !dataStore.getBoolean(DataStoreKeys.SUCCESSFUL_CLOSE, true);

        // if we have a pause date, check to see if pausedTime is less than the session timeout
        // threshold
        if (previousSessionPauseTimeInSeconds > 0) {
            final long pausedTimeInSecond =
                    startTimestampInSeconds - previousSessionPauseTimeInSeconds;

            if (pausedTimeInSecond < sessionTimeoutInSeconds
                    && previousSessionStartTimeInSeconds > 0) {
                // handle sessions that did not time out by removing paused time from session
                // do this by adding the paused time the session start time
                dataStore.setLong(
                        DataStoreKeys.START_DATE,
                        previousSessionStartTimeInSeconds + pausedTimeInSecond);

                // clear lifecycle flags
                dataStore.setBoolean(DataStoreKeys.SUCCESSFUL_CLOSE, false);
                dataStore.remove(DataStoreKeys.PAUSE_DATE);
                return null;
            }
        }

        dataStore.setLong(DataStoreKeys.START_DATE, startTimestampInSeconds);
        dataStore.remove(DataStoreKeys.PAUSE_DATE);
        dataStore.setBoolean(DataStoreKeys.SUCCESSFUL_CLOSE, false);

        final int launches = dataStore.getInt(DataStoreKeys.LAUNCHES, 0) + 1;
        dataStore.setInt(DataStoreKeys.LAUNCHES, launches);

        dataStore.setString(
                DataStoreKeys.OS_VERSION,
                coreData.get(LifecycleConstants.EventDataKeys.Lifecycle.OPERATING_SYSTEM));
        dataStore.setString(
                DataStoreKeys.APP_ID,
                coreData.get(LifecycleConstants.EventDataKeys.Lifecycle.APP_ID));

        Log.trace(LifecycleConstants.LOG_TAG, SELF_LOG_TAG, "New lifecycle session started");
        return new SessionInfo(
                previousSessionStartTimeInSeconds,
                previousSessionPauseTimeInSeconds,
                previousSessionCrashed);
    }

    /**
     * Pause current lifecycle session
     *
     * @param pauseTimestampInSeconds pause timestamp (in seconds)
     */
    void pause(final long pauseTimestampInSeconds) {
        if (dataStore == null) {
            Log.debug(
                    LifecycleConstants.LOG_TAG,
                    SELF_LOG_TAG,
                    "Failed to pause session, %s (persisted data)",
                    Log.UNEXPECTED_NULL_VALUE);
            return;
        }

        dataStore.setBoolean(DataStoreKeys.SUCCESSFUL_CLOSE, true);
        dataStore.setLong(DataStoreKeys.PAUSE_DATE, pauseTimestampInSeconds);

        Log.trace(LifecycleConstants.LOG_TAG, SELF_LOG_TAG, "Lifecycle session paused");
        // reset lifecycle flag
        lifecycleHasRun = false;
    }

    /**
     * Gets session length data (used for Analytics reporting)
     *
     * @param startTimestampInSeconds session start timestamp (in seconds)
     * @param sessionTimeoutInSeconds session timeout (in seconds)
     * @param previousSessionInfo SessionInfo object containing previous session's data
     * @return {@code Map<String, String>} session length context data
     */
    Map<String, String> getSessionData(
            final long startTimestampInSeconds,
            final long sessionTimeoutInSeconds,
            final LifecycleSession.SessionInfo previousSessionInfo) {
        Map<String, String> sessionContextData = new HashMap<>();

        if (dataStore == null) {
            Log.debug(
                    LifecycleConstants.LOG_TAG,
                    SELF_LOG_TAG,
                    "%s (data store), Failed to get session length data",
                    Log.UNEXPECTED_NULL_VALUE);
            return sessionContextData;
        }

        if (previousSessionInfo == null) {
            Log.debug(
                    LifecycleConstants.LOG_TAG,
                    SELF_LOG_TAG,
                    "%s (previous session info), Failed to get session length data",
                    Log.UNEXPECTED_NULL_VALUE);
            return sessionContextData;
        }

        final long timeSincePauseInSeconds =
                startTimestampInSeconds - previousSessionInfo.getPauseTimestampInSeconds();
        final long lastSessionTimeSeconds =
                previousSessionInfo.getPauseTimestampInSeconds()
                        - previousSessionInfo.getStartTimestampInSeconds();

        // if we have not exceeded our timeout, bail
        if (timeSincePauseInSeconds < sessionTimeoutInSeconds) {
            return sessionContextData;
        }

        // verify our session time is valid
        if (lastSessionTimeSeconds > 0
                && lastSessionTimeSeconds < LifecycleConstants.MAX_SESSION_LENGTH_SECONDS) {
            sessionContextData.put(
                    LifecycleConstants.EventDataKeys.Lifecycle.PREVIOUS_SESSION_LENGTH,
                    Long.toString(lastSessionTimeSeconds));
        } else {
            // data is out of bounds, still record it in context data but put it in a different key
            sessionContextData.put(
                    LifecycleConstants.EventDataKeys.Lifecycle.IGNORED_SESSION_LENGTH,
                    Long.toString(lastSessionTimeSeconds));
        }

        return sessionContextData;
    }

    /** Container for lifecycle session information */
    static class SessionInfo {

        private final long startTimestampInSeconds;
        private final long pauseTimestampInSeconds;
        private final boolean isCrash;

        SessionInfo(
                final long startTimestampInSeconds,
                final long pauseTimestampInSeconds,
                final boolean isCrash) {
            this.startTimestampInSeconds = startTimestampInSeconds;
            this.pauseTimestampInSeconds = pauseTimestampInSeconds;
            this.isCrash = isCrash;
        }

        long getStartTimestampInSeconds() {
            return startTimestampInSeconds;
        }

        long getPauseTimestampInSeconds() {
            return pauseTimestampInSeconds;
        }

        boolean isCrash() {
            return isCrash;
        }
    }
}
