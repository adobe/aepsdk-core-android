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

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class FakeLoggingService implements LoggingService {
	private List<String> log;
	private boolean ignoreRegisteringErrors;

	FakeLoggingService() {
		this.log = new ArrayList<String>();
	}

	@Override
	public void trace(String tag, String message) {
		log("Trace", tag, message);
	}

	@Override
	public void debug(String tag, String message) {
		log("Debug", tag, message);
	}

	@Override
	public void warning(String tag, String message) {
		log("Warning", tag, message);
	}

	@Override
	public void error(String tag, String message) {
		log("Error", tag, message);
	}

	private void log(String prefix, String tag, String message) {
		String logLine = prefix + ": " + tag + " - " + message;

		if (ignoreRegisteringErrors &&
				(message.startsWith("Failed to register listener for class") ||
				 message.startsWith("Failed to create dispatcher for class"))) {
			return;
		}

		System.out.println(logLine);
		log.add(logLine);
	}

	boolean containsTraceLog(String tag, String message) {
		return log.contains("Trace: " + tag + " - " + message);
	}

	boolean containsDebugLog(String tag, String message) {
		return log.contains("Debug: " + tag + " - " + message);
	}

	boolean containsWarningLog(String tag, String message) {
		return log.contains("Warning: " + tag + " - " + message);
	}

	boolean containsErrorLog(String tag, String message) {
		return log.contains("Error: " + tag + " - " + message);
	}

	void clearLog() {
		this.log = new ArrayList<String>();
	}

	void setIgnoreRegisteringErrors() {
		ignoreRegisteringErrors = true;
	}
}
