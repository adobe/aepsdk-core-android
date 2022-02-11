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
 
// package com.adobe.marketing.mobile;

// import java.io.BufferedOutputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.OutputStream;
// import java.net.HttpURLConnection;
// import java.net.MalformedURLException;
// import java.net.URL;
// import java.util.Map;
// import com.adobe.marketing.mobile.AndroidNetworkServiceOverrider.HTTPConnectionPerformer;
// import com.adobe.marketing.mobile.AndroidNetworkServiceOverrider.Connecting;

// import org.junit.After;
// import org.junit.Before;

// // Intended to run all existing AndroidNetworkServiceTests with a replacement network handler.
// // (hence the extension of AndroidNetworkServiceTests).
// public class AndroidNetworkServiceOverriderTests extends AndroidNetworkServiceTests {
// 	@Override
// 	@Before
// 	public void setup() {
// 		AndroidNetworkServiceOverrider.setHTTPConnectionPerformer(new TestConnectionPerformer());
// 		super.setup();
// 	}

// 	@After
// 	public void teardown() {
// 		AndroidNetworkServiceOverrider.setHTTPConnectionPerformer(null);
// 	}

// 	// this is the custom network stack used for these tests
// 	private static class TestConnectionPerformer extends HTTPConnectionPerformer {
// 		private final String LOG_TAG = "TestConnectionPerformer";

// 		TestConnectionPerformer() {

// 		}

// 		@Override
// 		public boolean shouldOverride(final String url, final  String method) {
// 			Log.trace(LOG_TAG, "shouldOverride called for " + method + " request against " + url);
// 			return true;
// 		}

// 		@Override
// 		public Connecting connect(final String url, final String method, final byte[] payload,
// 								  final  Map<String, String> headers,
// 								  final int connectionTimeoutSeconds, final int readTimeoutSeconds) {
// 			Log.debug(LOG_TAG, "Overrode network connection to " + method + " for " + url);

// 			try {
// 				final URL dest = new URL(url);
// 				final HttpURLConnection con = (HttpURLConnection) dest.openConnection();
// 				con.setReadTimeout(readTimeoutSeconds * 1000);
// 				con.setConnectTimeout(connectionTimeoutSeconds * 1000);

// 				con.setRequestMethod(method);

// 				if (headers != null) {

// 					for (final Map.Entry<String, String> entry : headers.entrySet()) {
// 						con.setRequestProperty(entry.getKey(), entry.getValue());
// 					}

// 				}

// 				if (method.equals("POST") && payload != null) {
// 					con.setFixedLengthStreamingMode(payload.length);
// 				}

// 				con.connect();

// 				if (method.equals("POST") && payload != null) {
// 					final OutputStream os = new BufferedOutputStream(con.getOutputStream());
// 					os.write(payload);
// 					os.flush();
// 					os.close();
// 				}

// 				final InputStream inputStream = con.getInputStream();
// 				final InputStream errorStream = con.getErrorStream();
// 				final int responseCode = con.getResponseCode();
// 				final String responseMessage = con.getResponseMessage();

// 				return new Connecting() {
// 					@Override
// 					public InputStream getInputStream() {
// 						Log.debug(LOG_TAG, "Input stream retrieved.");

// 						return inputStream;
// 					}

// 					@Override
// 					public InputStream getErrorStream() {
// 						return errorStream;
// 					}

// 					@Override
// 					public int getResponseCode() {
// 						Log.debug(LOG_TAG, "Response code(%d) retrieved.", responseCode);

// 						return responseCode;
// 					}

// 					@Override
// 					public String getResponseMessage() {
// 						Log.debug(LOG_TAG, "Response message(%s) retrieved. ", responseMessage);

// 						return responseMessage;
// 					}

// 					@Override
// 					public String getResponsePropertyValue(String responsePropertyKey) {
// 						final String responseHeaderValue = con.getHeaderField(responsePropertyKey);
// 						Log.debug(LOG_TAG, "Response header value (%s) retrieved for key (%s)", responseHeaderValue, responsePropertyKey);
// 						return responseHeaderValue;
// 					}

// 					@Override
// 					public void close() {
// 						Log.debug(LOG_TAG, "Connection closed.");

// 						if (inputStream != null) {
// 							try {
// 								inputStream.close();
// 							} catch (final Exception ex) {
// 								Log.error(LOG_TAG, "Error closing input stream(%s).", ex.getLocalizedMessage());
// 							}
// 						}

// 						con.disconnect();
// 					}
// 				};


// 			} catch (final MalformedURLException ex) {
// 				Log.error(LOG_TAG, "Invalid URL (%s)", ex.getLocalizedMessage());
// 				return CONNECTION_ERROR_URL;

// 			} catch (final IOException ex) {
// 				Log.error(LOG_TAG, "Unexpected exception in custom network override(%s)", ex.getLocalizedMessage());
// 				return CONNECTION_ERROR_IO;
// 			}
// 		}
// 	}
// }
