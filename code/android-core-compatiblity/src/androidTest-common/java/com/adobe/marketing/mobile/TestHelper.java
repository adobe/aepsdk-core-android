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

import android.content.Context;

import com.adobe.marketing.mobile.services.internal.context.App;

import junit.framework.Assert;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class TestHelper {

	private static final String LOG_TAG = TestHelper.class.getSimpleName();
	private static final int TEST_DEFAULT_TIMEOUT_MS = 1000;
	private static final int TEST_DEFAULT_SLEEP_MS = 50;
	private static final int TEST_INITIAL_SLEEP_MS = 100;


	List<String> whitelistedThreads = new ArrayList<String>();

	{
		whitelistedThreads.add("pool"); // used for threads that execute the listeners code
		whitelistedThreads.add("ADB"); // module internal threads
	}

	public void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setAudienceServer() {
		HashMap<String, Object> data = new HashMap<String, Object> ();
		data.put("audience.server", "audience.com");
		MobileCore.updateConfiguration(data);
	}

	public void setAnalyticsReferrerTimeout(int analyticsReferrerTimeout) {
		HashMap<String, Object> data = new HashMap<String, Object> ();
		data.put("analytics.referrerTimeout", analyticsReferrerTimeout);
		MobileCore.updateConfiguration(data);
	}

	public void setLifecycleTimeout(int lifecycleTimeout) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("lifecycle.sessionTimeout", lifecycleTimeout);
		MobileCore.updateConfiguration(data);
	}

	public void setPrivacyStatus(String privacyStatus) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("global.privacy", privacyStatus);
		MobileCore.updateConfiguration(data);
	}

	public void setOfflineEnabled(boolean offlineTracking) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("analytics.offlineEnabled", offlineTracking);
		MobileCore.updateConfiguration(data);
	}

	public void setupForTntIdTest() {
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("acquisition.server", "fingerprinter.com");
		data.put("acquisition.appid", "testAppId");
		data.put("analytics.server", null);
		data.put("analytics.rsids", "rsid1,rsid2");
		data.put("analytics.referrerTimeout", 0);
		data.put("analytics.offlineEnabled", false);
		data.put("analytics.backdatePreviousSessionInfo", true);
		data.put("lifecycle.sessionTimeout", 1);
		data.put("global.privacy", "optedin");
		data.put("identity.adidEnabled", true);
		data.put("global.ssl", true);
		data.put("global.timezone", "PDT");
		data.put("global.timeoneOffset", -420);
		data.put("experienceCloud.org", "972C898555E9F7BC7F000101@AdobeOrg");
		data.put("experienceCloud.server", null);
		data.put("rulesEngine.url", null);
		data.put("messaging.url", null);
		data.put("target.clientCode", "adobeobumobile5target");
		data.put("target.timeout", 5);
		MobileCore.updateConfiguration(data);
	}

	public void setBackdateSessionInfo__OfflineTracking__LifecycleTimeout(boolean sessionInfoChoice,
			boolean offlineTrackingChoice, int lifecycleTimeout) {
		HashMap<String, Object> data = new HashMap<String, Object> ();
		data.put("analytics.backdatePreviousSessionInfo", sessionInfoChoice);
		data.put("analytics.offlineEnabled", offlineTrackingChoice);
		data.put("lifecycle.sessionTimeout", lifecycleTimeout);
		MobileCore.updateConfiguration(data);
		sleep(1000);
	}


	public void cleanCache(Context defaultContext) {
		boolean success;
		System.out.println("Cleaning cache");
		File cache = defaultContext.getCacheDir();
		File filesDir = new File(cache.getParent() + "/files");

		if (defaultContext.equals(null)) {
			System.out.println("Default context is null, cannot clean cache\n");
			return;
		}

		if (cache.exists() && cache.list().length != 0) {
			String[] children = cache.list();

			for (String s : children) {
				if (s != null && s.equals("adbdownloadcache")) {
					success = deleteDir(new File(cache, s));

					if (success) {
						System.out.println("Inside cleanCache: Child folder " + s + " DELETED\n");
					} else {
						System.out.println("Inside cleanCache, unable to delete " + s);
					}
				}
			}
		}

		if (filesDir.exists()) {
			success = deleteDir(filesDir);

			if (success) {
				System.out.println("Inside cleanCache: Directory " + filesDir.getName() + " DELETED\n");
			} else {
				System.out.println("Inside cleanCache, unable to delete " + filesDir.getName());
			}
		}
	}

	public boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();

			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));

				if (!success) {
					return false;
				}
			}
		}

		return dir.delete();
	}

	public static void writeLocalFile(String filepath, String contents) throws IOException, JSONException {

		File file = new File(filepath);
		file.getParentFile().mkdirs();
		file.createNewFile();

		PrintWriter out = null;

		try {
			out = new PrintWriter(file.getAbsolutePath());
			out.println(contents);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}


	public static SimpleDateFormat createRFC2822Formatter() {
		final String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
		final SimpleDateFormat rfc2822formatter = new SimpleDateFormat(pattern, Locale.US);
		rfc2822formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return rfc2822formatter;
	}

	public static void cleanLocalStorage() {
		AndroidLocalStorageService localStorageService = new AndroidLocalStorageService();
		localStorageService.getDataStore("AAMDataStore").removeAll();
		localStorageService.getDataStore("AdobeMobile_ConfigState").removeAll();
		localStorageService.getDataStore("AnalyticsDataStorage").removeAll();
		localStorageService.getDataStore("AdobeMobile_Lifecycle").removeAll();
		localStorageService.getDataStore("Acquisition").removeAll();
		localStorageService.getDataStore("visitorIDServiceDataStore").removeAll();
		localStorageService.getDataStore("ADOBEMOBILE_TARGET").removeAll();
	}

	void setResponseForAudienceManagerSignalWithData(String json) {
		Map<String, String> headers = new HashMap<>();
		SimpleDateFormat simpleDateFormat = createRFC2822Formatter();
		headers.put("Last-Modified", simpleDateFormat.format(new Date()));
		E2ETestableNetworkService.NetworkResponse networkResponse = new E2ETestableNetworkService.NetworkResponse(json, 200,
				headers);
		//		testableNetworkService.setDefaultResponse(networkResponse);
	}

	public void setupRulesZip(E2ETestableNetworkService testableNetworkService, String path) {
		String zipMatcher = "http://configServer.com/rules.zip";
		E2ERequestMatcher networkMatcher = new E2ERequestMatcher(zipMatcher);
		InputStream zipFile = null;

		try {
			zipFile = App.INSTANCE.getAppContext().getAssets().open(path);
		} catch (IOException e) {
			e.printStackTrace();
		}

		SimpleDateFormat simpleDateFormat = TestHelper.createRFC2822Formatter();
		Map<String, String> headers = new HashMap<>();
		headers.put("Last-Modified", simpleDateFormat.format(new Date()));
		E2ETestableNetworkService.NetworkResponse networkResponse = new E2ETestableNetworkService.NetworkResponse(
			zipFile,
			200, headers);
		testableNetworkService.setResponse(networkMatcher, networkResponse);

		Map<String, Object> rulesData = new HashMap<String, Object>();
		rulesData.put("rules.url", "http://configServer.com/rules.zip");
		MobileCore.updateConfiguration(rulesData);
		//wait for rules json download request to complete before proceeding
		testableNetworkService.waitAndGetCount(1);
		testableNetworkService.resetTestableNetworkService();
	}

	/**
	 * Waits for all the whitelisted threads to finish or fails the test after timeoutMillis if some of them are still running
	 * when the timer expires. If timeoutMillis is 0, a default timeout will be set = 1000ms
	 *
	 * @param timeoutMillis max waiting time
	 */
	protected void waitForThreadsWithFailIfTimedOut(final long timeoutMillis) {

		long startTime = System.currentTimeMillis();
		long timeoutTestMillis = timeoutMillis > 0 ? timeoutMillis : TEST_DEFAULT_TIMEOUT_MS;
		long sleepTime = timeoutTestMillis < TEST_DEFAULT_SLEEP_MS ? timeoutTestMillis : TEST_DEFAULT_SLEEP_MS;

		sleep(TEST_INITIAL_SLEEP_MS);
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
	private boolean isAppThread(final Thread t) {
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


	private void sleep(final long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}