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


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.fail;


public abstract class BaseTest {

	MockEventHubUnitTest eventHub;
	FakePlatformServices platformServices;

	void beforeEach() {
		platformServices = new FakePlatformServices();
		eventHub = new MockEventHubUnitTest("UnitTest", platformServices);
	}

	void afterEach() {
	}

	Map<String, String> getURLQueryParameters(final String urlString) {
		final Map<String, String> parameters = new HashMap<String, String>();
		URL url;

		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return parameters;
		}

		String queryString = url.getQuery();
		final String[] paramArray = queryString.split("&");

		for (String currentParam : paramArray) {
			// quick out in case this entry is null or empty string
			if (currentParam == null || currentParam.length() <= 0) {
				continue;
			}

			final String[] currentParamArray = currentParam.split("=", 2);

			// don't need to check the key, because if the param is not null or empty, we will have
			// at least one entry in the array with a length > 0
			// we do need to check for a second value, as there will only be a second entry in this
			// array if there is at least one '=' character in currentParam
			if (currentParamArray.length == 1 || (currentParamArray.length == 2 && currentParamArray[1].isEmpty())) {
				continue;
			}

			final String key = currentParamArray[0];
			final String value = currentParamArray[1];
			parameters.put(key, value);
		}

		return parameters;
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
	File getResource(String resourceName) {
		File resourceFile = null;
		URL resource = getClass().getClassLoader().getResource(resourceName);

		if (resource != null) {
			resourceFile = new File(resource.getFile());
		}

		return resourceFile;
	}

	File copyResource(String resourceName, File destinationDirectory, String destinationResourceName) throws IOException {
		File resourceFile = null;
		InputStream resource = getClass().getClassLoader().getResourceAsStream(resourceName);

		if (resource != null) {
			resourceFile = new File(destinationDirectory, destinationResourceName);

			FileOutputStream fos = null;

			try {
				byte[] buffer = new byte[4096];
				int n = 0;
				fos = new FileOutputStream(resourceFile);

				while ((n = resource.read(buffer)) != -1) {
					fos.write(buffer, 0, n);
				}
			} finally {
				if (fos != null) {
					fos.close();
				}

				resource.close();
			}
		}

		return resourceFile;
	}


	/**
	 * Waits for given executor to execute all the submitted tasks. Exits with failure after the default 5secs
	 * @param executorService executor that we want to wait for
	 */
	void waitForExecutor(final ExecutorService executorService) throws Exception {
		waitForExecutor(executorService, 5);
	}

	/**
	 * Waits for given executor to execute all the submitted tasks. Exits with failure after the given timeout expires
	 * @param executorService executor that we want to wait for
	 * @param timeoutSec how long you want to wait for the execution before it fails
	 */
	void waitForExecutor(final ExecutorService executorService, final long timeoutSec) throws Exception {
		Future<?> future = executorService.submit(new Runnable() {
			@Override
			public void run() {
				// Fake task to check the execution termination
			}
		});

		try {
			future.get(timeoutSec, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			fail(String.format("Executor took longer than %s (sec)", timeoutSec));
		}
	}

	/**
	 * Waits for given executor to execute all the submitted tasks. Exits without failure after the given timeout expires
	 * @param executorService executor that we want to wait for
	 * @param timeoutSec how long you want to wait for the execution before it fails
	 */
	void waitForExecutorWithoutFailing(final ExecutorService executorService, final long timeoutSec) throws Exception {
		Future<?> future = executorService.submit(new Runnable() {
			@Override
			public void run() {
				// Fake task to check the execution termination
			}
		});

		try {
			future.get(timeoutSec, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
		}
	}
}
