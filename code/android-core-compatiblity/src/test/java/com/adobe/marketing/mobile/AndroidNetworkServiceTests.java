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


import com.adobe.marketing.mobile.services.ServiceProvider;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static com.adobe.marketing.mobile.NetworkService.HttpCommand.GET;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AndroidNetworkServiceTests {
	private AndroidNetworkService androidNetworkService;

	@Before
	public void setup() {
		androidNetworkService = new AndroidNetworkService(ServiceProvider.getInstance().getNetworkService());
	}

	@Test
	public void testDoConnection_Null_URL() {
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl(null, null, null, null, 10, 10);
		Assert.assertNull(connection);
	}

	@Test
	public void testDoConnection_Valid_Url_null_Command() {
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl("http://www.adobe.com",
				null, null, null, 10, 10);
		Assert.assertNull(connection);
	}

	@Test
	public void testDoConnection_Insecure_Url_Get_Command() {
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl("http://www.adobe.com",
				GET, null, null, 10, 10);
		Assert.assertNull(connection);
	}

	@Test
	public void testDoConnection_Valid_Url_https_Get_Command() {
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl("https://www.adobe.com",
				GET, null, null, 10, 10);
		Assert.assertNotNull(connection);
	}

	@Test
	public void testDoConnection_Valid_Url_unsupported_protocol_Get_Command() {
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl("ssh://www.adobe.com",
				GET, null, null, 10, 10);
		Assert.assertNull(connection);
	}

	@Test
	public void testDoConnection_Valid_Url_Valid_RequestProperty() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("testing", "header");
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl("https://www.adobe.com",
				GET, null, properties, 10, 10);
		Assert.assertNotNull(connection);
	}

}
