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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.HttpsURLConnection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HttpConnectingHandlerTests {

    @Mock private HttpsURLConnection httpsURLConnection;

    private URL url;

    @Before
    public void setup() throws Exception {
        // Since URL is a final class - it cannot be mocked by Mockito.
        // Hence, create an instance of URL that will allow us to pass out mock URLConnection
        // upon calling openConnection().
        URLStreamHandler handler =
                new URLStreamHandler() {
                    @Override
                    protected URLConnection openConnection(URL url) {
                        return httpsURLConnection;
                    }
                };
        url = new URL("http", "www.adobe.com", 80, "mock", handler);

        Mockito.when(httpsURLConnection.getURL()).thenReturn(url);
    }

    @Test
    public void testSetCommand_Get_Method() throws IOException {
        // Setup
        final HttpMethod testMethod = HttpMethod.GET;
        final AtomicBoolean actualDoOutputValue = new AtomicBoolean(false);
        Mockito.doAnswer(
                        invocation -> {
                            actualDoOutputValue.set((boolean) invocation.getArguments()[0]);
                            return null;
                        })
                .when(httpsURLConnection)
                .setDoOutput(ArgumentMatchers.anyBoolean());
        // Test
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        boolean result = connectionHandler.setCommand(testMethod);
        // Verify
        Assert.assertTrue(result);
        Assert.assertFalse(actualDoOutputValue.get());
        Assert.assertEquals(testMethod.name(), connectionHandler.command.toString());
    }

    @Test
    public void testSetCommand_Post_Method() throws IOException {
        // Setup
        final HttpMethod testMethod = HttpMethod.POST;
        final AtomicBoolean actualDoOutputValue = new AtomicBoolean(false);
        Mockito.doAnswer(
                        invocation -> {
                            actualDoOutputValue.set((boolean) invocation.getArguments()[0]);
                            return null;
                        })
                .when(httpsURLConnection)
                .setDoOutput(ArgumentMatchers.anyBoolean());
        // Test
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        boolean result = connectionHandler.setCommand(HttpMethod.POST);
        // Verify
        Assert.assertTrue(result);
        Assert.assertTrue(actualDoOutputValue.get());
        Assert.assertEquals(testMethod.name(), connectionHandler.command.toString());
    }

    @Test
    public void testSetCommand_Invalid_Method() throws IOException {
        // Setup
        final AtomicBoolean actualDoOutputValue = new AtomicBoolean(false);
        // Test
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        boolean result = connectionHandler.setCommand(null);
        // Verify
        Assert.assertFalse(result);
        // The Connection Handler will fallback to the default command which is GET
        Assert.assertFalse(actualDoOutputValue.get());
        Assert.assertEquals(HttpConnectionHandler.Command.GET, connectionHandler.command);
    }

    @Test
    public void testSetCommand_Protocol_Exception() throws IOException {
        // Setup
        final HttpMethod testMethod = HttpMethod.GET;
        final AtomicBoolean actualDoOutputValue = new AtomicBoolean(false);
        Mockito.doThrow(new ProtocolException("mockException"))
                .when(httpsURLConnection)
                .setRequestMethod(ArgumentMatchers.anyString());
        // Test
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        boolean result = connectionHandler.setCommand(testMethod);
        // Verify
        Assert.assertFalse(result);
        // The Connection Handler will fallback to the default command which is GET
        Assert.assertFalse(actualDoOutputValue.get());
        Assert.assertEquals(HttpConnectionHandler.Command.GET, connectionHandler.command);
    }

    @Test
    public void testSetRequestProperty_null_Parameter() throws IOException {
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        connectionHandler.setRequestProperty(null);
        Assert.assertEquals(0, connectionHandler.httpsUrlConnection.getRequestProperties().size());
    }

    @Test
    public void testSetRequestProperty_Empty_Map_Parameter() throws IOException {
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        connectionHandler.setRequestProperty(new HashMap<>());
        Assert.assertEquals(0, connectionHandler.httpsUrlConnection.getRequestProperties().size());
    }

    @Test
    public void testSetRequestProperty_Valid_Map_Parameter() throws IOException {
        final HashMap<String, String> requestPropertyList = new HashMap<>();
        Mockito.doAnswer(
                        invocation -> {
                            String key = (String) invocation.getArguments()[0];
                            String value = (String) invocation.getArguments()[1];
                            requestPropertyList.put(key, value);
                            return null;
                        })
                .when(httpsURLConnection)
                .setRequestProperty(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        HashMap<String, String> testPropertyList =
                new HashMap<String, String>() {
                    {
                        put("key1", "value1");
                        put("key2", "value2");
                    }
                };
        connectionHandler.setRequestProperty(testPropertyList);
        Assert.assertEquals(testPropertyList.size(), requestPropertyList.size());
        Assert.assertEquals("value1", requestPropertyList.get("key1"));
        Assert.assertEquals("value2", requestPropertyList.get("key2"));
    }

    @Test
    public void testSetRequestProperty_MultipleCalls() throws IOException {
        final HashMap<String, String> requestPropertyList = new HashMap<>();
        Mockito.doAnswer(
                        invocation -> {
                            String key = (String) invocation.getArguments()[0];
                            String value = (String) invocation.getArguments()[1];
                            requestPropertyList.put(key, value);
                            return null;
                        })
                .when(httpsURLConnection)
                .setRequestProperty(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        HashMap<String, String> testPropertyList =
                new HashMap<String, String>() {
                    {
                        put("key1", "value1");
                        put("key2", "value2");
                    }
                };
        // Valid Map input
        connectionHandler.setRequestProperty(testPropertyList);
        // Empty Map
        connectionHandler.setRequestProperty(new HashMap<>());
        Assert.assertEquals(testPropertyList.size(), requestPropertyList.size());
        Assert.assertEquals("value1", requestPropertyList.get("key1"));
        Assert.assertEquals("value2", requestPropertyList.get("key2"));
    }

    @Test
    public void testSetRequestProperty_Cumulative_Operations() throws IOException {
        final HashMap<String, String> requestPropertyList = new HashMap<>();
        Mockito.doAnswer(
                        invocation -> {
                            String key = (String) invocation.getArguments()[0];
                            String value = (String) invocation.getArguments()[1];
                            requestPropertyList.put(key, value);
                            return null;
                        })
                .when(httpsURLConnection)
                .setRequestProperty(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        HashMap<String, String> testPropertyList =
                new HashMap<String, String>() {
                    {
                        put("key1", "value1");
                        put("key2", "value2");
                    }
                };
        // Valid Map input
        connectionHandler.setRequestProperty(testPropertyList);
        HashMap<String, String> testPropertyList1 =
                new HashMap<String, String>() {
                    {
                        put("key3", "value3");
                        put("key1", "value4");
                    }
                };
        // Valid Map Input
        connectionHandler.setRequestProperty(testPropertyList1);
        // Verify
        Assert.assertEquals(3, requestPropertyList.size());
        Assert.assertEquals("value4", requestPropertyList.get("key1"));
        Assert.assertEquals("value2", requestPropertyList.get("key2"));
        Assert.assertEquals("value3", requestPropertyList.get("key3"));
    }

    @Test
    public void testConnect() throws IOException {
        // Setup
        final AtomicBoolean connectCalled = new AtomicBoolean(false);
        Mockito.doAnswer(
                        invocation -> {
                            connectCalled.set(true);
                            return null;
                        })
                .when(httpsURLConnection)
                .connect();
        // Test
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        HttpConnection connection = (HttpConnection) connectionHandler.connect(null);
        // Verify
        Assert.assertTrue(connectCalled.get());
        Assert.assertNotNull(connection);
    }

    @Test
    public void testConnect_IOException() throws IOException {
        // Setup
        Mockito.doThrow(new IOException("mockException")).when(httpsURLConnection).connect();
        // Test
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        HttpConnection connection = (HttpConnection) connectionHandler.connect(null);
        // Verify
        Assert.assertNotNull(connection);
    }

    @Test
    public void testConnect_Post_Command_With_Null_Payload() throws IOException {
        // Setup
        final AtomicBoolean connectCalled = new AtomicBoolean(false);
        final AtomicInteger payloadLength = new AtomicInteger(0);
        Mockito.doAnswer(
                        invocation -> {
                            connectCalled.set(true);
                            return null;
                        })
                .when(httpsURLConnection)
                .connect();
        // Output Stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // Test
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        connectionHandler.setCommand(HttpMethod.POST);
        // POST with no payload
        HttpConnection connection = (HttpConnection) connectionHandler.connect(null);
        // Verify
        Assert.assertTrue(connectCalled.get());
        Assert.assertNotNull(connection);
        Assert.assertEquals(0, payloadLength.get());
        Assert.assertEquals(0, bos.size());
    }

    @Test
    public void testConnect_Post_Command_With_Valid_Payload() throws IOException {
        // Setup
        final AtomicBoolean connectCalled = new AtomicBoolean(false);
        final AtomicInteger payloadLength = new AtomicInteger(0);
        String payload = "testPayload";
        Mockito.doAnswer(
                        invocation -> {
                            connectCalled.set(true);
                            return null;
                        })
                .when(httpsURLConnection)
                .connect();
        Mockito.doAnswer(
                        invocation -> {
                            payloadLength.set((int) invocation.getArguments()[0]);
                            return null;
                        })
                .when(httpsURLConnection)
                .setFixedLengthStreamingMode(ArgumentMatchers.anyInt());
        // Output Stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Mockito.when(httpsURLConnection.getOutputStream()).thenReturn(bos);
        // Test
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        connectionHandler.setCommand(HttpMethod.POST);
        // POST with no payload
        HttpConnection connection = (HttpConnection) connectionHandler.connect(payload.getBytes());
        // Verify
        Assert.assertTrue(connectCalled.get());
        Assert.assertNotNull(connection);
        Assert.assertEquals(payload.length(), payloadLength.get());
        Assert.assertEquals(payload.length(), bos.size());
        Assert.assertEquals(payload, new String(bos.toByteArray()));
    }

    @Test
    public void testConnect_Post_Command_When_IOException_Thrown() throws IOException {
        // Setup
        Mockito.doThrow(new IOException("mockException")).when(httpsURLConnection).connect();
        // Test
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        connectionHandler.setCommand(HttpMethod.POST);
        HttpConnection connection = (HttpConnection) connectionHandler.connect(null);
        // Verify
        Assert.assertNotNull(connection);
    }

    @Test
    public void testSetReadTimeout() throws IOException {
        final AtomicInteger readTimeoutValue = new AtomicInteger(0);
        Mockito.doAnswer(
                        invocation -> {
                            readTimeoutValue.set((int) invocation.getArguments()[0]);
                            return null;
                        })
                .when(httpsURLConnection)
                .setReadTimeout(ArgumentMatchers.anyInt());
        // Test
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        connectionHandler.setReadTimeout(100);
        // Verify
        Assert.assertEquals(100, readTimeoutValue.get());
    }

    @Test
    public void testSetConnectTimeout() throws IOException {
        final AtomicInteger connectTimeoutValue = new AtomicInteger(0);
        Mockito.doAnswer(
                        invocation -> {
                            connectTimeoutValue.set((int) invocation.getArguments()[0]);
                            return null;
                        })
                .when(httpsURLConnection)
                .setConnectTimeout(ArgumentMatchers.anyInt());
        // Test
        HttpConnectionHandler connectionHandler = new HttpConnectionHandler(url);
        connectionHandler.setConnectTimeout(100);
        // Verify
        Assert.assertEquals(100, connectTimeoutValue.get());
    }
}
