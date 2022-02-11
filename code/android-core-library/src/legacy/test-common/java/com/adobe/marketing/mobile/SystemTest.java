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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

@Ignore
@SuppressWarnings("all")
public class SystemTest {
	private static final String LOG_TAG = "SystemTest";
	protected MockEventHubModuleTest     eventHub;
	protected ModuleTestPlatformServices platformServices;
	protected static CountDownLatch eventHubLatch = new CountDownLatch(1);
	protected static int EVENTHUB_WAIT_MS = 50;

	List<String> whitelistedThreads = new ArrayList<String>();

	{
		whitelistedThreads.add("pool"); // used for threads that execute the listeners code
		whitelistedThreads.add("ADB"); // module internal threads
	}

	public SystemTest() {}

	@Before
	public void beforeEachSystemTest() {
		platformServices = new ModuleTestPlatformServices();
		eventHub = new MockEventHubModuleTest("eventhub", platformServices);
		Log.setLoggingService(platformServices.getLoggingService());
		Log.setLogLevel(LoggingMode.DEBUG);
	}

	@After
	public void afterEachSystemTest() {
		eventHub.clearEvents();
		eventHub.clearIgnoredEventFilters();
		eventHub.shutdown();
		eventHub = null;
	}

	/**
	 * Waits for all the whitelisted threads to finish or fails the test after timeoutMillis if some of them are still running
	 * when the timer expires. If timeoutMillis is 0, a default timeout will be set = 1000ms
	 *
	 * @param timeoutMillis max waiting time
	 */
	protected void waitForThreadsWithFailIfTimedOut(long timeoutMillis) {

		long startTime = System.currentTimeMillis();
		long timeoutTestMillis = timeoutMillis > 0 ? timeoutMillis : 1000;
		long sleepTime = timeoutTestMillis < 50 ? timeoutTestMillis : 50;

		sleep(100);
		Set<Thread> threadSet = getEligibleThreads();

		while (threadSet.size() > 0 && ((System.currentTimeMillis() - startTime) < timeoutTestMillis)) {
			Log.debug(LOG_TAG, "Still waiting for %s thread(s)", threadSet.size());

			for (Thread t : threadSet) {

				Log.debug(LOG_TAG, "Waiting for thread " + t.getName() + " (" + t.getId() + ")");
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

				if (timedOut) {
					Log.debug(LOG_TAG, "Timeout out waiting for thread " + t.getName() + " (" + t.getId() + ")");
					//The wait timedout
					//Do we need to add a boolean parameter for an optional fail?
					Assert.fail("Timed out waiting for thread " + t.getName() + " (" + t.getId() + ")");
				} else {
					Log.debug(LOG_TAG, "Done waiting for thread " + t.getName() + " (" + t.getId() + ")");
				}
			}

			threadSet = getEligibleThreads();
		}
	}

	/**
	 * Get an instance of {@link File} for resource from the resource directory, that matches the name supplied.
	 * <p>
	 *	The resource directory for the unit tests is <b>{projectroot}/unitTests/resource</b>
	 * </p>
	 *
	 *
	 * @param resourceName The name of the resource.
	 * @return A File instance, if the resource was found in the resource directory. null otherwise.
	 */
	public File getResource(String resourceName) {
		File resourceFile = null;
		URL resource = getClass().getClassLoader().getResource(resourceName);

		if (resource != null) {
			resourceFile = new File(resource.getFile());
		}

		return resourceFile;
	}


	/**
	 * Retrieves all the whitelisted threads that are still running
	 * @return set of running tests
	 */
	private Set<Thread> getEligibleThreads() {
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Set<Thread> eligibleThreads = new HashSet<Thread>();

		for (Thread t : threadSet) {
			if (isAppThread(t) && !t.getState().equals(Thread.State.WAITING) && !t.getState().equals(Thread.State.TERMINATED)
					&& !t.getState().equals(Thread.State.TIMED_WAITING)) {
				eligibleThreads.add(t);
			}
		}

		return eligibleThreads;
	}

	/**
	 * Checks if current thread is not a daemon and its name starts with one of the whitelisted thread names specified here
	 * {@link #whitelistedThreads}
	 *
	 * @param t current thread to verify
	 * @return true if it is a known thread, false otherwise
	 */
	private boolean isAppThread(Thread t) {
		if (t.isDaemon()) {
			return false;
		}

		for (String prefix : whitelistedThreads) {
			if (t.getName().startsWith(prefix)) {
				return true;
			}
		}

		return false;
	}

	protected void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
