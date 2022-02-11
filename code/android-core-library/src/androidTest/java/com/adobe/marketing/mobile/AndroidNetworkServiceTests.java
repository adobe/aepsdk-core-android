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


import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import android.annotation.SuppressLint;
import android.support.test.InstrumentationRegistry;

import com.adobe.marketing.mobile.services.ServiceProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import static com.adobe.marketing.mobile.NetworkService.HttpCommand.GET;
import static com.adobe.marketing.mobile.NetworkService.HttpCommand.POST;

public class AndroidNetworkServiceTests {

	private AndroidNetworkService androidNetworkService;

	private static String DEFAULT_HERMETIC_SERVER = "sj1010010101041.corp.adobe.com";

	@SuppressLint({"BadHostnameVerifier", "TrustAllX509TrustManager"})
	@Before
	public void setup() {
		//trust all CAs - removes the need to manually trust the hermetic server cert
		try {
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[] {new X509TrustManager() {
					public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
					public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
					public X509Certificate[] getAcceptedIssuers() {
						return new X509Certificate[0];
					}
				}
			}, null);
			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

			ServiceProvider.getInstance().setContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
		} catch (Exception e) {
			e.printStackTrace();
		}

		androidNetworkService = new AndroidNetworkService(ServiceProvider.getInstance().getNetworkService());
	}

	private String getHermeticServer(boolean ssl) {
		String server = System.getProperty("HERMETIC_SERVER") != null ? System.getProperty("HERMETIC_SERVER") :
						DEFAULT_HERMETIC_SERVER;
		String hermeticServer = ssl ? "https://" : "http://";
		hermeticServer += server;
		return hermeticServer;
	}

	@Test
	public void testConnectUrl__When_Valid_Url_Get_Command_Used__Then_Connection_Successful() {
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl(getHermeticServer(true),
				GET, null, null, 10000, 10000);
		Assert.assertNotNull(connection);
		Assert.assertEquals(200, connection.getResponseCode());
		connection.close();
	}

	@Test
	public void testConnectUrl__When_Valid_Url_Post_Command_Used__Then_Connection_Successful() {
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl(getHermeticServer(true),
				POST, null, null, 10000, 10000);
		Assert.assertNotNull(connection);
		Assert.assertEquals(200, connection.getResponseCode());
		connection.close();
	}

	@Test
	public void testConnectUrl__When_Valid_Url_Post_Command_Used_With_Payload__Then_Payload_Sent_Successfully() throws
		IOException {
		String mockData = "mockData";
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl(getHermeticServer(
					true) + "/test/setNextResponse", POST, mockData.getBytes(), null, 10000, 10000);
		Assert.assertNotNull(connection);
		Assert.assertEquals(200, connection.getResponseCode());
		connection = androidNetworkService.connectUrl(getHermeticServer(true) + "/test/getHits/",
					 GET, null, null, 10000, 10000);
		InputStream inputStream = connection.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line = reader.readLine();
		Assert.assertEquals(mockData, line);
		connection.close();
	}

	@Test
	public void
	testConnectUrl__When_Valid_Url_And_https_Protocol_Post_Command_Used_With_Payload__Then_Payload_Sent_Successfully()
	throws IOException {
		String mockData = "mockData";
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl(getHermeticServer(
					true) + "/test/setNextResponse", POST, mockData.getBytes(), null, 10000, 10000);
		Assert.assertNotNull(connection);
		Assert.assertEquals(200, connection.getResponseCode());
		connection = androidNetworkService.connectUrl(getHermeticServer(true) + "/test/getHits/",
					 GET, null, null, 10000, 10000);
		InputStream inputStream = connection.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line = reader.readLine();
		Assert.assertEquals(mockData, line);
		connection.close();
	}

	@Test
	public void testConnectUrl__When_InvalidURL__Then_Return_null_Connection() throws IOException {
		String mockData = "mockData";
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl("ssh://mock",
				POST, mockData.getBytes(), null, 10000, 10000);
		Assert.assertNull(connection);
	}

	@Test
	public void testConnectUrl__When_Connection_Causes_IOException__Then_Return_Valid_Connection() throws IOException {
		String mockData = "mockData";
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl("https://mock_does_not_exist",
				POST, mockData.getBytes(), null, 10000, 10000);
		Assert.assertNotNull(connection);
		Assert.assertEquals(-1, connection.getResponseCode());
		Assert.assertEquals(null, connection.getResponseMessage());
	}

	@Test
	public void
	testConnectUrlAsync__When_Valid_Url_And_https_Protocol_Post_Command_Used_With_Payload__Then_Payload_Sent_Successfully()
	throws Exception {
		String mockData = "mockData";
		final CountDownLatch latch = new CountDownLatch(1);
		androidNetworkService.connectUrlAsync(getHermeticServer(true) + "/test/setNextResponse",
											  POST, mockData.getBytes(),
		null, 10000, 10000, new NetworkService.Callback() {
			@Override
			public void call(NetworkService.HttpConnection connection) {
				latch.countDown();
				connection.close();
			}
		});
		latch.await();
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl(getHermeticServer(true) + "/test/getHits/",
				GET, null, null, 10000, 10000);
		InputStream inputStream = connection.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line = reader.readLine();
		Assert.assertEquals(mockData, line);
		connection.close();
	}

	@Test
	public void
	testConnectUrlAsync__When_Valid_Url_And_http_Protocol_Post_Command_Used_With_Payload__Then_Payload_Sent_Successfully()
	throws Exception {
		String mockData = "mockData";
		final CountDownLatch latch = new CountDownLatch(1);
		androidNetworkService.connectUrlAsync(getHermeticServer(true) + "/test/setNextResponse",
											  POST, mockData.getBytes(),
		null, 10000, 10000, new NetworkService.Callback() {
			@Override
			public void call(NetworkService.HttpConnection connection) {
				latch.countDown();
				connection.close();
			}
		});
		latch.await();
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl(getHermeticServer(true) + "/test/getHits/",
				GET, null, null, 10000, 10000);
		InputStream inputStream = connection.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line = reader.readLine();
		Assert.assertEquals(mockData, line);
		connection.close();
	}

	@Test
	public void testConnectUrlAsync__When_Valid_Url_And_http_Protocol_Get_Command_Used__Then_Connection_Successful() throws
		Exception {
		final AtomicBoolean connectionSuccess = new AtomicBoolean(false);
		final CountDownLatch latch = new CountDownLatch(1);
		androidNetworkService.connectUrlAsync(getHermeticServer(true) + "/test/setNextResponse",
		GET, null, null, 10000, 10000, new NetworkService.Callback() {
			@Override
			public void call(NetworkService.HttpConnection connection) {
				connectionSuccess.set(connection != null && connection.getResponseCode() == 200);
				latch.countDown();
				connection.close();
			}
		});
		latch.await();
		Assert.assertTrue(connectionSuccess.get());
	}

	@Test
	public void testConnectUrl__When_ValidUrl_ValidRequestProperties__Then_Connection_Successful() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("testing", "header");
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl(getHermeticServer(true),
				GET, null, properties, 10000, 10000);
		Assert.assertNotNull(connection);
		Assert.assertEquals(200, connection.getResponseCode());
		connection.close();
	}

	@Test
	public void testConnectUrl__When_ValidUrl_OverwrittenDefaultHeaders__Then_Connection_Successful() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 5.1.1; ro; Nexus 5 Build/LMY48B)");
		properties.put("Accept-Language", "ro");
		NetworkService.HttpConnection connection = androidNetworkService.connectUrl(getHermeticServer(true),
				GET, null, properties, 10000, 10000);
		Assert.assertNotNull(connection);
		Assert.assertEquals(200, connection.getResponseCode());
		connection.close();
	}
}
