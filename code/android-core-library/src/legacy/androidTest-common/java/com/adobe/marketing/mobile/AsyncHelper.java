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

import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AsyncHelper {
	private List<String> whitelistedThreads = new ArrayList<String>();

	{
		whitelistedThreads.add("pool"); // used for threads that execute the listeners code
		whitelistedThreads.add("ADB"); // module internal threads
	}

	/**
	 * Waits for all the whitelisted threads to finish or fails the test after timeoutMillis if some
	 * of them are still running when the timer expires (only if failOnTimeout is true). If timeoutMillis
	 * is 0, a default timeout will be set = 1000ms
	 *
	 * @param timeoutMillis max waiting time
	 * @param failOnTimeout fails the test this method timeouts waiting for all the threads and this param is true
	 */
	public void waitForAppThreads(final long timeoutMillis, final boolean failOnTimeout) {
		final long defaultTimeout = 1000;
		final long defaultSleepTime = 50;
		long startTime = System.currentTimeMillis();
		long timeoutTestMillis = timeoutMillis > 0 ? timeoutMillis : defaultTimeout;
		long sleepTime = timeoutTestMillis < defaultSleepTime ? timeoutTestMillis : defaultSleepTime;

		sleep(100);
		Set<Thread> threadSet = getEligibleThreads();

		while (threadSet.size() > 0 && ((System.currentTimeMillis() - startTime) < timeoutTestMillis)) {
			for (Thread t : threadSet) {
				boolean done = false;
				boolean timedOut = false;

				while (!done && !timedOut) {
					if (t.getState().equals(Thread.State.TERMINATED)
							|| t.getState().equals(Thread.State.TIMED_WAITING)
							|| t.getState().equals(Thread.State.WAITING)) {
						//Cannot use the join() API since we use a cached thread pool, which
						//means that we keep idle threads around for 60secs (default timeout).
						done = true;
					} else {
						//blocking
						sleep(sleepTime);
						timedOut = (System.currentTimeMillis() - startTime) > timeoutTestMillis;
					}
				}

				if (timedOut && failOnTimeout) {
					Assert.fail(String.format("Timed out waiting for thread %s (%s)", t.getName(), t.getId()));
				}
			}

			threadSet = getEligibleThreads();
		}
	}


	/**
	 * Retrieves all the whitelisted threads that are still running
	 * @return set of running tests
	 */
	private Set<Thread> getEligibleThreads() {
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Set<Thread> eligibleThreads = new HashSet<Thread>();

		for (Thread t : threadSet) {
			if (isAppThread(t) && !t.getState().equals(Thread.State.WAITING)
					&& !t.getState().equals(Thread.State.TERMINATED)
					&& !t.getState().equals(Thread.State.TIMED_WAITING)) {
				eligibleThreads.add(t);
			}
		}

		return eligibleThreads;
	}

	/**
	 * Checks if current thread is not a daemon and its name starts with one of the whitelisted thread
	 * names specified here {@link #whitelistedThreads}
	 *
	 * @param thread current thread to verify
	 * @return true if it is a known thread, false otherwise
	 */
	private boolean isAppThread(Thread thread) {
		if (thread.isDaemon()) {
			return false;
		}

		for (String prefix : whitelistedThreads) {
			if (thread.getName().startsWith(prefix)) {
				return true;
			}
		}

		return false;
	}

	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
