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

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HttpConnectingTests {

    @Mock private HttpURLConnection httpURLConnection;

    @Mock private InputStream inputStream;

    @Mock private InputStream errorStream;

    @Test
    public void testGetInputStream_Valid_InputStream() throws IOException {
        // Setup
        InputStream is = new ByteArrayInputStream("mock data".getBytes());
        when(httpURLConnection.getInputStream()).thenReturn(is);
        HttpConnection connection = new HttpConnection(httpURLConnection);
        // Test
        InputStream connectionInputStream = connection.getInputStream();
        // Verify
        BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(connectionInputStream));
        String line = bufferedReader.readLine();
        Assert.assertEquals("mock data", line);
    }

    @Test
    public void testGetInputStream_InputStream_Is_null() throws IOException {
        // Setup
        when(httpURLConnection.getInputStream()).thenReturn(null);
        HttpConnection connection = new HttpConnection(httpURLConnection);
        // Test
        InputStream connectionInputStream = connection.getInputStream();
        // Verify
        Assert.assertNull(connectionInputStream);
    }

    @Test
    public void testGetInputStream_IOException() throws IOException {
        // Setup
        when(httpURLConnection.getInputStream()).thenThrow(new IOException("Mock Exception"));
        HttpConnection connection = new HttpConnection(httpURLConnection);
        // Test
        InputStream connectionInputStream = connection.getInputStream();
        // Verify
        Assert.assertNull(connectionInputStream);
    }

    @Test
    public void testGetResponseCode_ValidResponseCode() throws IOException {
        // Setup
        when(httpURLConnection.getResponseCode()).thenReturn(200);
        HttpConnection connection = new HttpConnection(httpURLConnection);
        // Test
        int responseCode = connection.getResponseCode();
        // Verify
        Assert.assertEquals(200, responseCode);
    }

    @Test
    public void testGetResponseCode_IOException() throws IOException {
        // Setup
        when(httpURLConnection.getResponseCode()).thenThrow(new IOException("mock exception"));
        HttpConnection connection = new HttpConnection(httpURLConnection);
        // Test
        int responseCode = connection.getResponseCode();
        // Verify
        Assert.assertEquals(-1, responseCode);
    }

    @Test
    public void testGetResponseMessage_ValidMessage() throws IOException {
        // Setup
        String message = "mock message";
        when(httpURLConnection.getResponseMessage()).thenReturn(message);
        HttpConnection connection = new HttpConnection(httpURLConnection);
        // Test
        String responseMessage = connection.getResponseMessage();
        // Verify
        Assert.assertEquals(message, responseMessage);
    }

    @Test
    public void testGetResponseMessage_Message_Is_null() throws IOException {
        // Setup
        when(httpURLConnection.getResponseMessage()).thenReturn(null);
        HttpConnection connection = new HttpConnection(httpURLConnection);
        // Test
        String responseMessage = connection.getResponseMessage();
        // Verify
        Assert.assertNull(responseMessage);
    }

    @Test
    public void testGetResponseMessage_IOException() throws IOException {
        // Setup
        when(httpURLConnection.getResponseMessage()).thenThrow(new IOException("Mock Exception"));
        HttpConnection connection = new HttpConnection(httpURLConnection);
        // Test
        String responseMessage = connection.getResponseMessage();
        // Verify
        Assert.assertNull(responseMessage);
    }

    @Test
    public void testGetResponsePropertyValue_ValidKey() throws IOException {
        // Setup
        when(httpURLConnection.getHeaderField("key")).thenReturn("value");
        HttpConnection connection = new HttpConnection(httpURLConnection);
        // Test
        String value = connection.getResponsePropertyValue("key");
        // Verify
        Assert.assertEquals("value", value);
    }

    @Test
    public void testGetResponsePropertyValue_Key_Is_Invalid() throws IOException {
        // Setup
        HttpConnection connection = new HttpConnection(httpURLConnection);
        // Test
        String value = connection.getResponsePropertyValue("nonexistent_key");
        // Verify
        Assert.assertNull(value);
    }

    @Test
    public void testClose() throws IOException {
        Mockito.reset(inputStream);
        Mockito.reset(errorStream);
        // Setup
        final AtomicBoolean controlValue = new AtomicBoolean(false);
        doAnswer(
                        invocation -> {
                            controlValue.set(true);
                            return null;
                        })
                .when(httpURLConnection)
                .disconnect();
        when(httpURLConnection.getInputStream()).thenReturn(inputStream);
        when(httpURLConnection.getErrorStream()).thenReturn(errorStream);
        HttpConnection connection = new HttpConnection(httpURLConnection);
        // Test
        connection.close();
        // Verify
        Assert.assertTrue(controlValue.get());
        verify(inputStream, times(1)).close();
        verify(errorStream, times(1)).close();
    }
}
