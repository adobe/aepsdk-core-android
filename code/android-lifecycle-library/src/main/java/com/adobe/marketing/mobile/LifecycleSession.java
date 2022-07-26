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

import com.adobe.marketing.mobile.LifecycleConstants.DataStoreKeys;
import com.adobe.marketing.mobile.LocalStorageService.DataStore;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for managing lifecycle sessions for standard, non-XDM scenarios.
 * Persists start, pause, and end timestamps for lifecycle sessions.
 * Also generates session context data for Analytics reporting
 */
class LifecycleSession {

	private static final String SELF_LOG_TAG = "LifecycleSession";
	private final DataStore dataStore;
	private       boolean   lifecycleHasRun;

	LifecycleSession(final LocalStorageService.DataStore dataStore) {
		this.dataStore = dataStore;
	}

	/**
	 * Start a new lifecycle session.
	 * Returns a SessionInfo object containing the previous session's data if it is a new session
	 * Returns null if the previous session is resumed, or if lifecycle has already run
	 *
	 * @param startTimestampInSeconds long session start time (in seconds)
	 * @param sessionTimeoutInSeconds int session timeout (in seconds)
	 * @param coreData core data generated from LifecycleMetricsBuilder
	 *
	 * @return SessionInfo object containing previous session's data
	 */
	SessionInfo start(final long startTimestampInSeconds, final long sessionTimeoutInSeconds,
					  final Map<String, String> coreData) {
		if (lifecycleHasRun) {
			return null;
		}

		if (dataStore == null) {
			Log.debug(LifecycleConstants.LOG_TAG, "%s - Failed to start session, %s (persisted data)", SELF_LOG_TAG,
					  Log.UNEXPECTED_NULL_VALUE);
			return null;
		}

		lifecycleHasRun = true;

		final long previousSessionStartTimeInSeconds = dataStore.getLong(DataStoreKeys.START_DATE, 0L);
		final long previousSessionPauseTimeInSeconds = dataStore.getLong(DataStoreKeys.PAUSE_DATE, 0L);
		final boolean previousSessionCrashed = !dataStore.getBoolean(DataStoreKeys.SUCCESSFUL_CLOSE, true);

		// if we have a pause date, check to see if pausedTime is less than the session timeout threshold
		if (previousSessionPauseTimeInSeconds > 0) {
			final long pausedTimeInSecond = startTimestampInSeconds - previousSessionPauseTimeInSeconds;

			if (pausedTimeInSecond < sessionTimeoutInSeconds && previousSessionStartTimeInSeconds > 0) {
				// handle sessions that did not time out by removing paused time from session
				// do this by adding the paused time the session start time
				dataStore.setLong(DataStoreKeys.START_DATE, previousSessionStartTimeInSeconds + pausedTimeInSecond);

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

		dataStore.setString(DataStoreKeys.OS_VERSION,
							coreData.get(LifecycleConstants.EventDataKeys.Lifecycle.OPERATING_SYSTEM));
		dataStore.setString(DataStoreKeys.APP_ID, coreData.get(LifecycleConstants.EventDataKeys.Lifecycle.APP_ID));

		Log.trace(LifecycleConstants.LOG_TAG, "%s - New lifecycle session started", SELF_LOG_TAG);
		return new SessionInfo(previousSessionStartTimeInSeconds, previousSessionPauseTimeInSeconds, previousSessionCrashed);
	}

	/**
	 * Pause current lifecycle session
	 *
	 * @param pauseTimestampInSeconds pause timestamp (in seconds)
	 */
	void pause(final long pauseTimestampInSeconds) {
		if (dataStore == null) {
			Log.debug(LifecycleConstants.LOG_TAG, "%s - Failed to pause session, %s (persisted data)", SELF_LOG_TAG,
					  Log.UNEXPECTED_NULL_VALUE);
			return;
		}

		dataStore.setBoolean(DataStoreKeys.SUCCESSFUL_CLOSE, true);
		dataStore.setLong(DataStoreKeys.PAUSE_DATE, pauseTimestampInSeconds);

		Log.trace(LifecycleConstants.LOG_TAG, "%s - Lifecycle session paused", SELF_LOG_TAG);
		// reset lifecycle flag
		lifecycleHasRun = false;
	}

	/**
	 * Gets session length data (used for Analytics reporting)
	 *
	 * @param startTimestampInSeconds session start timestamp (in seconds)
	 * @param sessionTimeoutInSeconds session timeout (in seconds)
	 * @param previousSessionInfo  SessionInfo object containing previous session's data
	 *
	 * @return {@code Map<String, String>} session length context data
	 */
	Map<String, String> getSessionData(final long startTimestampInSeconds,
									   final long sessionTimeoutInSeconds,
									   final LifecycleSession.SessionInfo previousSessionInfo) {
		Map<String, String> sessionContextData = new HashMap<String, String>();

		if (dataStore == null) {
			Log.debug(LifecycleConstants.LOG_TAG, "%s - %s (data store), Failed to get session length data", SELF_LOG_TAG,
					  Log.UNEXPECTED_NULL_VALUE);
			return sessionContextData;
		}

		if (previousSessionInfo == null) {
			Log.debug(LifecycleConstants.LOG_TAG, "%s - %s (previous session info), Failed to get session length data",
					  SELF_LOG_TAG, Log.UNEXPECTED_NULL_VALUE);
			return sessionContextData;
		}

		final long timeSincePauseInSeconds = startTimestampInSeconds - previousSessionInfo.getPauseTimestampInSeconds();
		final long lastSessionTimeSeconds = previousSessionInfo.getPauseTimestampInSeconds() -
											previousSessionInfo.getStartTimestampInSeconds();

		// if we have not exceeded our timeout, bail
		if (timeSincePauseInSeconds < sessionTimeoutInSeconds) {
			return sessionContextData;
		}

		// verify our session time is valid
		if (lastSessionTimeSeconds > 0 && lastSessionTimeSeconds < LifecycleConstants.MAX_SESSION_LENGTH_SECONDS) {
			sessionContextData.put(LifecycleConstants.EventDataKeys.Lifecycle.PREVIOUS_SESSION_LENGTH,
								   Long.toString(lastSessionTimeSeconds));
		} else {
			// data is out of bounds, still record it in context data but put it in a different key
			sessionContextData.put(LifecycleConstants.EventDataKeys.Lifecycle.IGNORED_SESSION_LENGTH,
								   Long.toString(lastSessionTimeSeconds));
		}

		return sessionContextData;
	}

	/**
	 * Container for lifecycle session information
	 */
	static class SessionInfo {
		private final long    startTimestampInSeconds;
		private final long    pauseTimestampInSeconds;
		private final boolean isCrash;

		SessionInfo(final long startTimestampInSeconds, final long pauseTimestampInSeconds, final boolean isCrash) {
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
