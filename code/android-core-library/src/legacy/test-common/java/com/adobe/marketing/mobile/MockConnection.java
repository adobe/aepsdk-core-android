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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

public class MockConnection implements NetworkService.HttpConnection {
	String response;
	int responseCode;
	String responseMessage;
	Map<String, String> responseProperties;

	public MockConnection(final String response, final int responseCode, final String responseMessage,
						  final Map<String, String> responseProperties) {
		this.response = response;
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.responseProperties = responseProperties;
	}

	public InputStream getInputStream() {
		return response != null ? getInputStreamFromString(response) : getInputStreamFromString(responseMessage);
	}

	@Override
	public InputStream getErrorStream() {
		return null;
	}

	public int getResponseCode() {
		return this.responseCode;
	}

	public String getResponseMessage() {
		return this.responseMessage;
	}
	public String getResponsePropertyValue(final String responsePropertyKey) {
		return responseProperties.get(responsePropertyKey);
	}

	public boolean closeWasCalled;
	public void close() {
		closeWasCalled = true;
	}

	private InputStream getInputStreamFromString(final String responseMessage) {
		if (responseMessage == null) {
			return null;
		}

		return new ByteArrayInputStream(responseMessage.getBytes(Charset.forName("UTF-8")));
	}
}
