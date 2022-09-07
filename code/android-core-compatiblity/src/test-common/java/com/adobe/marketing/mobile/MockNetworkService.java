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

import java.util.Map;

public class MockNetworkService implements NetworkService {

	public String connectUrlParametersUrl;
	public HttpCommand connectUrlParametersCommand;
	public byte[] connectUrlParametersConnectPayload;
	public Map<String, String> connectUrlParametersRequestProperty;
	public long connectUrlParametersConnectTimeout;
	public long connectUrlParametersReadTimeout;
	public HttpConnection connectUrlReturnValue;
	public boolean connectUrlWasCalled;

	@Override
	public HttpConnection connectUrl(final String url, final HttpCommand command, final byte[] connectPayload,
									 final Map<String, String> requestProperty,
									 final int connectTimeout, final int readTimeout) {
		this.connectUrlParametersUrl = url;
		this.connectUrlParametersCommand = command;
		this.connectUrlParametersConnectPayload = connectPayload;
		this.connectUrlParametersRequestProperty = requestProperty;
		this.connectUrlParametersConnectTimeout = connectTimeout;
		this.connectUrlParametersReadTimeout = readTimeout;
		this.connectUrlWasCalled = true;
		return connectUrlReturnValue;
	}

	public String connectUrlAsyncParametersUrl;
	public HttpCommand connectUrlAsyncParametersCommand;
	public byte[] connectUrlAsyncParametersConnectPayload;
	public Map<String, String> connectUrlAsyncParametersRequestProperty;
	public long connectUrlAsyncParametersConnectTimeout;
	public long connectUrlAsyncParametersReadTimeout;
	public Callback connectUrlAsyncParametersResultCallback;
	public boolean connectUrlAsyncWasCalled;
	public HttpConnection connectUrlAsyncCallbackParametersConnection;

	@Override
	public void connectUrlAsync(final String url, final HttpCommand command, final byte[] connectPayload,
								final Map<String, String> requestProperty,
								final int connectTimeout, final int readTimeout, final Callback resultCallback) {
		this.connectUrlAsyncParametersUrl = url;
		this.connectUrlAsyncParametersCommand = command;
		this.connectUrlAsyncParametersConnectPayload = connectPayload;
		this.connectUrlAsyncParametersRequestProperty = requestProperty;
		this.connectUrlAsyncParametersConnectTimeout = connectTimeout;
		this.connectUrlAsyncParametersReadTimeout = readTimeout;
		this.connectUrlAsyncParametersResultCallback = resultCallback;

		if (connectUrlAsyncCallbackParametersConnection != null && resultCallback != null) {
			resultCallback.call(connectUrlAsyncCallbackParametersConnection);
		}

		this.connectUrlAsyncWasCalled = true;
	}
}
