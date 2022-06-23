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
package com.adobe.marketing.mobile.services;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class NetworkServiceTests {

    private NetworkService networkService;

    @Before
    public void setup() {
        networkService = new NetworkService();
    }

    @Test
    public void testConnectAsync_NullUrl() {
        networkService.connectAsync(
                new NetworkRequest(null, null, null, null, 0, 0),
                TestCase::assertNull);
    }

    @Test
    public void testConnectAsync_NonHttpsUrl() {
        networkService.connectAsync(
                new NetworkRequest("http://www.adobe.com", null, null, null, 0, 0),
                TestCase::assertNull);
    }

    @Test
    public void testConnectAsync_MalformedUrl()
    {
        networkService.connectAsync(
                new NetworkRequest("://www.adobehttps.com", null, null, null, 0, 0),
                TestCase::assertNull);
    }

    @Test
    public void testConnectAsync_UnsupportedUrlProtocol()
    {
        networkService.connectAsync(
                new NetworkRequest("http://www.adobehttps.com", null, null, null, 0, 0),
                TestCase::assertNull);

        networkService.connectAsync(
                new NetworkRequest("ssh://www.adobehttps.com", null, null, null, 0, 0),
                TestCase::assertNull);
    }

    @Test
    public void testConnectAsync_GetMethod()
    {
        networkService.connectAsync(
                new NetworkRequest("https://www.adobe.com", HttpMethod.GET, null, null, 10, 10),
                connection -> {
                    assertNotNull(connection);
                    assertEquals(200, connection.getResponseCode());
                });
    }

    @Test
    public void testConnectAsync_PostMethod()
    {
        networkService.connectAsync(
                new NetworkRequest("https://www.adobe.com", HttpMethod.GET, null, null, 10, 10),
                connection -> {
                    assertNotNull(connection);
                    assertEquals(200, connection.getResponseCode());
                });
    }

    @Test
    public void testConnectAsync_WithBody()
    {
        networkService.connectAsync(
                new NetworkRequest("https://www.adobe.com", HttpMethod.GET, new byte[] {}, null, 10, 10),
                connection -> {
                    assertNotNull(connection);
                    assertEquals(200, connection.getResponseCode());
                });
    }

    @Test
    public void testConnectAsync_WithHeader()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("testing", "header");
        networkService.connectAsync(
                new NetworkRequest("https://www.adobe.com", HttpMethod.GET, null, properties, 10, 10),
                connection -> {
                    assertNotNull(connection);
                    assertEquals(200, connection.getResponseCode());
                });
    }

}
