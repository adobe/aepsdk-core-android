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

public class E2EAndroidNetworkService extends E2ETestableNetworkService {

	public E2EAndroidNetworkService(final SystemInfoService systemInfoService) {
		super();
	}

	@Override
	public HttpConnection connectUrl(String url, HttpCommand command, byte[] connectPayload,
									 Map<String, String> requestProperty,
									 int connectTimeout, int readTimeout) {
		return processAsyncNetworkRequest(
				   new NetworkRequest(url, command, connectPayload, requestProperty,
									  connectTimeout, readTimeout, null, NetworkRequestType.SYNC));
	}

	@Override
	public void connectUrlAsync(String url, HttpCommand command, byte[] connectPayload, Map<String, String> requestProperty,
								int connectTimeout, int readTimeout, Callback resultCallback) {
		processAsyncNetworkRequest(
			new NetworkRequest(url, command, connectPayload, requestProperty,
							   connectTimeout, readTimeout, resultCallback, NetworkRequestType.ASYNC));
	}

}
