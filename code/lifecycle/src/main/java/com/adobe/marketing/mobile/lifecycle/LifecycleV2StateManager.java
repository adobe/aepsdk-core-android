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

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.services.Log;

/**
 * Manager for current app session state updates for the XDM scenario based on the start/pause
 * Lifecycle events. For standard, non-XDM scenarios, see {@link LifecycleSession}.
 */
class LifecycleV2StateManager {

    private static final String SELF_LOG_TAG = "LifecycleV2StateManager";
    // timer used for waiting for pause-start consecutive updates, which can be encountered while
    // tracking
    // the app lifecycle background/foreground changes.
    private final LifecycleTimerState updateTimer;
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
        this.updateTimer = new LifecycleTimerState("ADBLifecycleStateManager");
    }

    /**
     * Updates {@code currentState} if needed and returns the status of the update operation through
     * the provided callback.
     *
     * <p>Expected scenarios: If this is the first start update in the session, the state gets
     * updated immediately. If this is a start -> pause update, a timer of {@link
     * LifecycleV2Constants#STATE_UPDATE_TIMEOUT_MILLIS} will start to wait for any other immediate
     * updates (start/pause) that would indicate the session is not over yet. If an update is
     * received before the timer expired:
     *
     * <ul>
     *   <li>start - would cancel the timer and ignore the update (session continues)
     *   <li>pause - would reset the timer as the session should end based on the last pause update
     * </ul>
     *
     * If no other update is received the pause update is marked as completed.
     *
     * <p>Consecutive start updates are being ignored as the session continues.
     *
     * @param newState (required) the new state that needs to be updated
     * @param callback (required) completion callback to be invoked with the status of the update
     *     once the operation is complete
     */
    void updateState(final State newState, final AdobeCallback<Boolean> callback) {
        if (callback == null || newState == null) {
            return;
        }

        synchronized (stateUpdatesMutex) {
            if (updateTimer.isTimerRunning()) {
                if (State.START.equals(newState)) {
                    Log.trace(
                            LifecycleConstants.LOG_TAG,
                            SELF_LOG_TAG,
                            "Consecutive pause-start state update detected, ignoring.");
                    cancelTimer();
                    callback.call(false);
                } else if (State.PAUSE.equals(newState)) {
                    Log.trace(
                            LifecycleConstants.LOG_TAG,
                            SELF_LOG_TAG,
                            "New pause state update received while waiting, restarting the count.");
                    restartTimer(newState, callback);
                }

                return;
            }

            if (this.currentState == newState) {
                Log.trace(
                        LifecycleConstants.LOG_TAG,
                        SELF_LOG_TAG,
                        "Consecutive %s state update received, ignoring.",
                        currentState);
                callback.call(false);
                return;
            }

            if (State.PAUSE.equals(newState)) {
                Log.trace(
                        LifecycleConstants.LOG_TAG,
                        SELF_LOG_TAG,
                        "New pause state update received, waiting for %s (ms) before updating.",
                        LifecycleV2Constants.STATE_UPDATE_TIMEOUT_MILLIS);
                startTimer(newState, callback);
            } else {
                Log.trace(
                        LifecycleConstants.LOG_TAG,
                        SELF_LOG_TAG,
                        "New start state update received.");
                currentState = newState;
                callback.call(true);
            }
        }
    }

    /**
     * Starts the {@code updateTimer} with the new state.
     *
     * @param newState (required) the new state that needs to be updated
     * @param callback (required) completion callback to be invoked with the status of the update
     *     once the operation is complete
     */
    private void startTimer(final State newState, final AdobeCallback<Boolean> callback) {
        cancelableCallback = callback;
        updateTimer.startTimer(
                LifecycleV2Constants.STATE_UPDATE_TIMEOUT_MILLIS,
                complete -> {
                    // no other event interrupted this timer, proceed with processing the new state
                    synchronized (stateUpdatesMutex) {
                        currentState = newState;
                        updateTimer.cancel();
                        callback.call(true);
                        cancelableCallback = null;
                    }
                });
    }

    /**
     * Cancels and restarts the {@code updateTimer} with the new state.
     *
     * @param newState (required) the new state that needs to be updated
     * @param callback (required) completion callback to be invoked with the status of the update
     *     once the operation is complete
     */
    private void restartTimer(final State newState, final AdobeCallback<Boolean> callback) {
        cancelTimer();
        startTimer(newState, callback);
    }

    /**
     * Cancels the running {@code updateTimer} and calls the cancelable callback with false if
     * needed.
     */
    private void cancelTimer() {
        if (cancelableCallback != null) {
            cancelableCallback.call(false);
            cancelableCallback = null;
        }

        updateTimer.cancel();
    }
}
