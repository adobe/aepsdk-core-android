/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util;

import com.adobe.marketing.mobile.services.HttpConnecting;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

public class MockConnection implements HttpConnecting {

	public MockConnection(final int responseCode, final String responseBody, final String errorBody) {
		this(responseCode, responseBody, errorBody, null);
	}

	public MockConnection(
		final int responseCode,
		final String responseBody,
		final String errorBody,
		final Map<String, String> headers
	) {
		mockGetResponseCode = responseCode;
		mockResponseBody = responseBody;
		mockErrorBody = errorBody;
		mockGetResponsePropertyValues = headers;
	}

	public int getInputStreamCalledTimes = 0;
	private String mockResponseBody;

	@Override
	public InputStream getInputStream() {
		getInputStreamCalledTimes += 1;

		if (mockResponseBody == null) {
			return null;
		}

		return new ByteArrayInputStream(mockResponseBody.getBytes());
	}

	public int getErrorStreamCalledTimes = 0;
	private String mockErrorBody;

	@Override
	public InputStream getErrorStream() {
		getErrorStreamCalledTimes += 1;

		if (mockErrorBody == null) {
			return null;
		}

		return new ByteArrayInputStream(mockErrorBody.getBytes());
	}

	public int getResponseCodeCalledTimes = 0;
	private int mockGetResponseCode = 0;

	@Override
	public int getResponseCode() {
		getResponseCodeCalledTimes += 1;
		return mockGetResponseCode;
	}

	@Override
	public String getResponseMessage() {
		return null;
	}

	private Map<String, String> mockGetResponsePropertyValues;

	@Override
	public String getResponsePropertyValue(final String value) {
		if (mockGetResponsePropertyValues == null) {
			return null;
		}

		return mockGetResponsePropertyValues.get(value);
	}

	public int closeCalledTimes = 0;

	@Override
	public void close() {
		closeCalledTimes += 1;
	}
}
