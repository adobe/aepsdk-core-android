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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AndroidLoggingServiceTests {

	private static final String ADB_MOBILE_LOG_TAG = "AdobeExperienceSDK";
	private static final String LOGCAT_COMMAND = "logcat -v tag -d " + ADB_MOBILE_LOG_TAG + ":* *:S";
	private static final int LOG_READ_MAX_RETRIES = 5;

	private LoggingService loggingService;

	@Before
	public void setup() {
		loggingService = new AndroidLoggingService();
	}

	private String readLogcat() {
		Process process = null;
		StringBuilder log = new StringBuilder();

		try {
			process = Runtime.getRuntime().exec(LOGCAT_COMMAND);
			BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(process.getInputStream()));
			String line = "";

			while ((line = bufferedReader.readLine()) != null) {
				log.append(line);
			}
		} catch (IOException e) {
		}

		if (process != null) {
			process.destroy();
		}

		return log.toString();
	}

	/**
	 * Asserts that the {@code regexToMatch} was a successful match.
	 *
	 * <p>
	 * Retries to read the logcat {@code maxAttempts} times, with a sleep of 500ms between retries.
	 *
	 * @param maxAttempts how many times to retry reading system logs
	 */
	private void assertLogLineMatch(final int maxAttempts, final String regexToMatch) {
		String log = readLogcat();
		assertTrue(maxAttempts >= 0);
		int attempt = 0;

		boolean found = false;

		while (!found && attempt < maxAttempts) {
			sleep(500);
			log = readLogcat();
			found = log.matches(regexToMatch);
			attempt++;
		}

		assertTrue("The expected log was not found!", found);
	}

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception e) {
		}
	}

	@Test
	public void logWarning() {
		//Test
		loggingService.warning("testTag", "abc");
		//Verify
		String expectedLogText = "testTag - abc";
		assertLogLineMatch(LOG_READ_MAX_RETRIES, ".*" + "W.?" + ADB_MOBILE_LOG_TAG + ": " + expectedLogText);
	}

	@Test
	public void logWarning_Null_Tag() {
		//Test
		loggingService.warning(null, "Log line with null tag");
		//Verify
		String expectedLogText = "null - Log line with null tag";
		assertLogLineMatch(LOG_READ_MAX_RETRIES, ".*" + "W.?" + ADB_MOBILE_LOG_TAG + ": " + expectedLogText);
	}

	@Test
	public void logWarning_Null_Log_Line() {
		//Test
		loggingService.warning(null, null);
		//Verify
		String expectedLogText = "null - null";
		assertLogLineMatch(LOG_READ_MAX_RETRIES, ".*" + "W.?" + ADB_MOBILE_LOG_TAG + ": " + expectedLogText);
	}

	@Test
	public void logError() {
		//Test
		loggingService.error("testErrorTag", "Error Log");
		//Verify
		String expectedLogText = "testErrorTag - Error Log";
		assertLogLineMatch(LOG_READ_MAX_RETRIES, ".*" + "E.?" + ADB_MOBILE_LOG_TAG + ": " + expectedLogText);
	}

	@Test
	public void logError_Null_Tag() {
		//Test
		loggingService.error(null, "Error Log Null Tag");
		//Verify
		String expectedLogText = "null - Error Log Null Tag";
		assertLogLineMatch(LOG_READ_MAX_RETRIES, ".*" + "E.?" + ADB_MOBILE_LOG_TAG + ": " + expectedLogText);
	}

	@Test
	public void logError_Null_Logline() {
		//Test
		loggingService.error(null, null);
		//Verify
		String expectedLogText = "null - null";
		assertLogLineMatch(LOG_READ_MAX_RETRIES, ".*" + "E.?" + ADB_MOBILE_LOG_TAG + ": " + expectedLogText);
	}

	@Test
	public void logDebug() {
		//Test
		loggingService.debug("testDebugTag", "Debug Log");
		//Verify
		String expectedLogText = "testDebugTag - Debug Log";
		assertLogLineMatch(LOG_READ_MAX_RETRIES, ".*" + "D.?" + ADB_MOBILE_LOG_TAG + ": " + expectedLogText);
	}

	@Test
	public void logDebug_Null_tag() {
		//Test
		loggingService.debug(null, "Debug Log Null Tag");
		//Verify
		String expectedLogText = "null - Debug Log Null Tag";
		assertLogLineMatch(LOG_READ_MAX_RETRIES, ".*" + "D.?" + ADB_MOBILE_LOG_TAG + ": " + expectedLogText);
	}

	@Test
	public void logDebug_Null_LogLine() {
		//Test
		loggingService.debug(null, null);
		//Verify
		String expectedLogText = "null - null";
		assertLogLineMatch(LOG_READ_MAX_RETRIES, ".*" + "D.?" + ADB_MOBILE_LOG_TAG + ": " + expectedLogText);
	}

	@Test
	public void logTrace() {
		//Test
		loggingService.trace("testTraceTag", "Trace Log");
		//Verify
		String expectedLogText = "testTraceTag - Trace Log";
		assertLogLineMatch(LOG_READ_MAX_RETRIES, ".*" + "V.?" + ADB_MOBILE_LOG_TAG + ": " + expectedLogText);
	}

	@Test
	public void logTrace_Null_tag() {
		//Test
		loggingService.trace(null, "Trace Log Null Tag");
		//Verify
		String expectedLogText = "null - Trace Log Null Tag";
		assertLogLineMatch(LOG_READ_MAX_RETRIES, ".*" + "V.?" + ADB_MOBILE_LOG_TAG + ": " + expectedLogText);
	}

	@Test
	public void logTrace_Null_LogLine() {
		//Test
		loggingService.trace(null, null);
		//Verify
		String expectedLogText = "null - null";
		assertLogLineMatch(LOG_READ_MAX_RETRIES, ".*" + "V.?" + ADB_MOBILE_LOG_TAG + ": " + expectedLogText);
	}
}
