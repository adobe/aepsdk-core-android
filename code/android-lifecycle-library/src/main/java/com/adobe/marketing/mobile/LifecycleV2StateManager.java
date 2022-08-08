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

/**
 * Manager for current app session state updates for the XDM scenario based on the start/pause Lifecycle events.
 * For standard, non-XDM scenarios, see {@link LifecycleSession}.
 */
class LifecycleV2StateManager {
	private static final String SELF_LOG_TAG = "LifecycleV2StateManager";
	// timer used for waiting for pause-start consecutive updates, which can be encountered while tracking
	// the app lifecycle background/foreground changes.
	private final TimerState updateTimer;
	// protects concurrent access to updateTimer and state related params
	private final Object stateUpdatesMutex = new Object();
	private State currentState;
	private AdobeCallback<Boolean> cancelableCallback;

	enum State {
		START("start"),
		PAUSE("pause");

		private final String value;

		State(final String value) {
			this.value = value;
		}
	}

	LifecycleV2StateManager() {
		this.updateTimer = new TimerState("ADBLifecycleStateManager");
	}

	/**
	 * Updates {@code currentState} if needed and returns the status of the update operation through the provided callback.
	 *
	 * Expected scenarios:
	 * If this is the first start update in the session, the state gets updated immediately.
	 * If this is a start -> pause update, a timer of {@link LifecycleV2Constants#STATE_UPDATE_TIMEOUT_MILLIS} will start to wait for
	 * any other immediate updates (start/pause) that would indicate the session is not over yet. If an update is received
	 * before the timer expired:
	 * <ul>
	 * 		<li> start - would cancel the timer and ignore the update (session continues)</li>
	 *  	<li> pause - would reset the timer as the session should end based on the last pause update </li>
	 * </ul>
	 * If no other update is received the pause update is marked as completed.
	 *
	 * Consecutive start updates are being ignored as the session continues.
	 *
	 * @param newState (required) the new state that needs to be updated
	 * @param callback (required) completion callback to be invoked with the status of the update once the operation is complete
	 */
	void updateState(final State newState, final AdobeCallback<Boolean> callback) {
		if (callback == null || newState == null) {
			return;
		}

		synchronized (stateUpdatesMutex) {
			if (updateTimer.isTimerRunning()) {
				if (State.START.equals(newState)) {
					Log.trace(LifecycleConstants.LOG_TAG, "%s - Consecutive pause-start state update detected, ignoring.",
							  SELF_LOG_TAG);
					cancelTimer();
					callback.call(false);
				} else if (State.PAUSE.equals(newState)) {
					Log.trace(LifecycleConstants.LOG_TAG, "%s - New pause state update received while waiting, restarting the count.",
							  SELF_LOG_TAG);
					restartTimer(newState, callback);
				}

				return;
			}

			if (this.currentState == newState) {
				Log.trace(LifecycleConstants.LOG_TAG, "%s - Consecutive %s state update received, ignoring.",
						  SELF_LOG_TAG, currentState);
				callback.call(false);
				return;
			}

			if (State.PAUSE.equals(newState)) {
				Log.trace(LifecycleConstants.LOG_TAG, "%s - New pause state update received, waiting for %s (ms) before updating.",
						  SELF_LOG_TAG, LifecycleV2Constants.STATE_UPDATE_TIMEOUT_MILLIS);
				startTimer(newState, callback);

			} else {
				Log.trace(LifecycleConstants.LOG_TAG, "%s - New start state update received.",
						  SELF_LOG_TAG);
				currentState = newState;
				callback.call(true);
			}
		}
	}

	/**
	 * Starts the {@code updateTimer} with the new state.
	 *
	 * @param newState (required) the new state that needs to be updated
	 * @param callback (required) completion callback to be invoked with the status of the update once the operation is complete
	 */
	private void startTimer(final State newState, final AdobeCallback<Boolean> callback) {
		cancelableCallback = callback;
		updateTimer.startTimer(LifecycleV2Constants.STATE_UPDATE_TIMEOUT_MILLIS, new AdobeCallback<Boolean>() {
			@Override
			public void call(final Boolean complete) {
				// no other event interrupted this timer, proceed with processing the new state
				synchronized (stateUpdatesMutex) {
					currentState = newState;
					updateTimer.cancel();
					callback.call(true);
					cancelableCallback = null;
				}
			}
		});
	}

	/**
	 * Cancels and restarts the {@code updateTimer} with the new state.
	 *
	 * @param newState (required) the new state that needs to be updated
	 * @param callback (required) completion callback to be invoked with the status of the update once the operation is complete
	 */
	private void restartTimer(final State newState, final AdobeCallback<Boolean> callback) {
		cancelTimer();
		startTimer(newState, callback);
	}

	/**
	 * Cancels the running {@code updateTimer} and calls the cancelable callback with false if needed.
	 */
	private void cancelTimer() {
		if (cancelableCallback != null) {
			cancelableCallback.call(false);
			cancelableCallback = null;
		}

		updateTimer.cancel();
	}
}
