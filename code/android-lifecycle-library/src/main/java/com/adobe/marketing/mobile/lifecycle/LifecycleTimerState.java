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

import java.util.Timer;
import java.util.TimerTask;

/**
 * Encapsulates a {@link Timer} object and provides API to start/cancel the timer or check whether the timer is running.
 */
class LifecycleTimerState {
    private final static String LOG_TAG = "TimerState";
    private boolean isTimerRunning;
    private long timeout;
    private TimerTask timerTask;
    private Timer timer;
    private AdobeCallback<Boolean> callback;
    private final String debugName;
    private final Object timerMutex;

    /**
     * Constructor
     * @param debugName a {@link String} used as log tag
     */
    LifecycleTimerState(final String debugName) {
        this.timeout = 0;
        this.isTimerRunning = false;
        this.debugName = debugName;
        this.timerMutex = new Object();
    }

    /**
     * Checks if the timer is still running.
     *
     * @return a {@code boolean} indicates whether there is a timer and it is still running
     */
    boolean isTimerRunning() {

        synchronized (timerMutex) {
            return timerTask != null && isTimerRunning;
        }
    }

    /**
     * Starts the timer with the given {@code long} timeout value, and call the {@code AdobeCallback<Boolean>}
     * if the timer was not canceled before timeout.
     *
     * @param timeout {@code long} timeout value for the timer
     * @param callback the {@code AdobeCallback<Boolean>} to be invoked once times out
     */
    void startTimer(final long timeout, final AdobeCallback<Boolean> callback) {
        synchronized (timerMutex) {

            if (timerTask != null) {
                Log.debug(LOG_TAG, "Timer has already started.");
                return;
            }


            this.timeout = timeout;
            this.isTimerRunning = true;
            this.callback = callback;

            try {
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        LifecycleTimerState.this.isTimerRunning = false;

                        if (LifecycleTimerState.this.callback != null) {
                            LifecycleTimerState.this.callback.call(true);
                        }
                    }
                };
                timer = new Timer(this.debugName);
                timer.schedule(timerTask, timeout);
                Log.trace(LOG_TAG,  String.format("%s timer scheduled having timeout %s ms", this.debugName, this.timeout));
            } catch (Exception e) {
                Log.warning(LOG_TAG, String.format("Error creating %s timer, failed with error: (%s)", this.debugName, e));
            }
        }
    }

    /**
     * Cancels the timer and sets the state back to normal.
     */
    void cancel() {
        synchronized (timerMutex) {
            if (timer != null) {
                try {
                    timer.cancel();
                    Log.trace(LOG_TAG, String.format("%s timer was canceled", this.debugName));
                } catch (Exception e) {
                    Log.warning(LOG_TAG, String.format("Error cancelling %s timer, failed with error: (%s)", this.debugName, e));
                }

                timerTask = null;
            }

            // set is running to false regardless of whether the timer is null or not
            this.isTimerRunning = false;
        }
    }
}