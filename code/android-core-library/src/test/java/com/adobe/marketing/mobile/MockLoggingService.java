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

public class MockLoggingService implements LoggingService {

	private LoggingCallback traceCallback;
	private LoggingCallback debugCallback;
	private LoggingCallback warningCallback;
	private LoggingCallback errorCallback;

	public void setTraceCallback(final LoggingCallback callback) {
		this.traceCallback = callback;
	}

	public void setDebugCallback(final LoggingCallback callback) {
		this.debugCallback = callback;
	}

	public void setWarningCallback(final LoggingCallback callback) {
		this.warningCallback = callback;
	}

	public void setErrorCallback(final LoggingCallback callback) {
		this.errorCallback = callback;
	}

	@Override
	public void trace(String tag, String message) {
		traceCallback.call(tag, message);
	}

	@Override
	public void debug(String tag, String message) {
		debugCallback.call(tag, message);
	}

	@Override
	public void warning(String tag, String message) {
		warningCallback.call(tag, message);
	}

	@Override
	public void error(String tag, String message) {
		errorCallback.call(tag, message);
	}

	interface LoggingCallback {
		void call(String tag, String message);
	}
}
