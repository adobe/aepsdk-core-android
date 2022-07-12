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
import static junit.framework.TestCase.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NetworkService.class, ServiceProvider.class})
public class NetworkServiceTests {

    private NetworkService networkService;

    @Mock
    ServiceProvider serviceProvider;

    @Mock
    DeviceInfoService deviceInfoService;

    @Mock
    private HttpConnectionHandler httpConnectionHandler;

    @Before
    public void setup() throws Exception {
        networkService = new NetworkService(currentThreadExecutorService());
        PowerMockito.mockStatic(ServiceProvider.class);
        PowerMockito.when(ServiceProvider.getInstance()).thenReturn(serviceProvider);
        PowerMockito.when(ServiceProvider.getInstance().getDeviceInfoService()).thenReturn(deviceInfoService);
        PowerMockito.whenNew(HttpConnectionHandler.class).withAnyArguments().thenReturn(httpConnectionHandler);
        Mockito.when(httpConnectionHandler.setCommand(Mockito.any(HttpMethod.class))).thenReturn(true);
    }

    @Test
    public void testConnectAsync_NullUrl() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        networkService.connectAsync(
                new NetworkRequest(null, null, null, null, 0, 0),
                connection -> {
                    assertNull(connection);
                    latch.countDown();
                });
                latch.await();
        Mockito.verifyZeroInteractions(httpConnectionHandler);
    }

    @Test
    public void testConnectAsync_NonHttpsUrl() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        networkService.connectAsync(
                new NetworkRequest("http://www.adobe.com", null, null, null, 0, 0),
                connection -> {
                    assertNull(connection);
                    latch.countDown();
                });
        latch.await();
        Mockito.verifyZeroInteractions(httpConnectionHandler);
    }

    @Test
    public void testConnectAsync_MalformedUrl() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        networkService.connectAsync(
                new NetworkRequest("://www.adobehttps.com", null, null, null, 0, 0),
                connection -> {
                    assertNull(connection);
                    latch.countDown();
                });
        latch.await();
        Mockito.verifyZeroInteractions(httpConnectionHandler);
    }

    @Test
    public void testConnectAsync_UnsupportedUrlProtocol() throws InterruptedException {
        final CountDownLatch latch1 = new CountDownLatch(1);
        networkService.connectAsync(
                new NetworkRequest("http://www.adobehttps.com", null, null, null, 0, 0),
                connection -> {
                    assertNull(connection);
                    latch1.countDown();
                });
        latch1.await();
        Mockito.verifyZeroInteractions(httpConnectionHandler);

        final CountDownLatch latch2 = new CountDownLatch(1);
        networkService.connectAsync(
                new NetworkRequest("ssh://www.adobehttps.com", null, null, null, 0, 0),
                connection -> {
                    assertNull(connection);
                    latch2.countDown();
                });
        latch2.await();
        Mockito.verifyZeroInteractions(httpConnectionHandler);
    }

    @Test
    public void testConnectAsync_DefaultHeaders() throws InterruptedException {
        final String mockDefaultUserAgent = "mock default user agent";
        final String mockLocaleString = "mock locale string";
        PowerMockito.when(ServiceProvider.getInstance().getDeviceInfoService().getDefaultUserAgent()).thenReturn(mockDefaultUserAgent);
        PowerMockito.when(ServiceProvider.getInstance().getDeviceInfoService().getLocaleString()).thenReturn(mockLocaleString);
        final CountDownLatch latch = new CountDownLatch(1);
        networkService.connectAsync(
                new NetworkRequest("https://www.adobe.com", HttpMethod.GET, null, null, 10, 10),
                connection -> {
                    latch.countDown();

                });
        latch.await();
        Mockito.verify(httpConnectionHandler, Mockito.times(1)).connect(Mockito.any());

        final ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(httpConnectionHandler).setRequestProperty(captor.capture());
        assertEquals(mockDefaultUserAgent, captor.getValue().get("User-Agent"));
        assertEquals(mockLocaleString, captor.getValue().get("Accept-Language"));
    }

    @Test
    public void testConnectAsync_WithHeader() throws InterruptedException {
        final String mockDefaultUserAgent = "mock default user agent";
        final String mockLocaleString = "mock locale string";
        PowerMockito.when(ServiceProvider.getInstance().getDeviceInfoService().getDefaultUserAgent()).thenReturn(mockDefaultUserAgent);
        PowerMockito.when(ServiceProvider.getInstance().getDeviceInfoService().getLocaleString()).thenReturn(mockLocaleString);
        Map<String, String> properties = new HashMap<>();
        properties.put("testing", "header");

        final CountDownLatch latch = new CountDownLatch(1);
        networkService.connectAsync(
                new NetworkRequest("https://www.adobe.com", HttpMethod.GET, null, properties, 10, 10),
                connection -> {
                    latch.countDown();

                });
        latch.await();
        Mockito.verify(httpConnectionHandler, Mockito.times(1)).connect(Mockito.any());

        final ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(httpConnectionHandler).setRequestProperty(captor.capture());
        assertEquals(mockDefaultUserAgent, captor.getValue().get("User-Agent"));
        assertEquals(mockLocaleString, captor.getValue().get("Accept-Language"));
        assertEquals("header", captor.getValue().get("testing"));
    }

    @Test
    public void testConnectAsync_GetMethod() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        networkService.connectAsync(
                new NetworkRequest("https://www.adobe.com", HttpMethod.GET, null, null, 10, 5),
                connection -> {
                    latch.countDown();

                });
        latch.await();
        Mockito.verify(httpConnectionHandler, Mockito.times(1)).connect(Mockito.any());

        final ArgumentCaptor<HttpMethod> httpMethodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        Mockito.verify(httpConnectionHandler).setCommand(httpMethodCaptor.capture());
        assertEquals(HttpMethod.GET, httpMethodCaptor.getValue());
    }

    @Test
    public void testConnectAsync_PostMethod() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        networkService.connectAsync(
                new NetworkRequest("https://www.adobe.com", HttpMethod.POST, null, null, 10, 5),
                connection -> {
                    latch.countDown();

                });
        latch.await();
        Mockito.verify(httpConnectionHandler, Mockito.times(1)).connect(Mockito.any());

        final ArgumentCaptor<HttpMethod> httpMethodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        Mockito.verify(httpConnectionHandler).setCommand(httpMethodCaptor.capture());
        assertEquals(HttpMethod.POST, httpMethodCaptor.getValue());
    }

    @Test
    public void testConnectAsync_ConnectAndReadTimeout() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        networkService.connectAsync(
                new NetworkRequest("https://www.adobe.com", HttpMethod.GET, null, null, 10, 5),
                connection -> {
                    latch.countDown();
                });
        latch.await();

        final ArgumentCaptor<Integer> connectTimeoutCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(httpConnectionHandler).setConnectTimeout(connectTimeoutCaptor.capture());
        assertEquals(10*1000, (int)connectTimeoutCaptor.getValue());

        final ArgumentCaptor<Integer> readTimeoutCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(httpConnectionHandler).setReadTimeout(readTimeoutCaptor.capture());
        assertEquals(5*1000, (int)readTimeoutCaptor.getValue());
    }

    @Test
    public void testConnectAsync_WithEmptyBody() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        networkService.connectAsync(
                new NetworkRequest("https://www.adobe.com", HttpMethod.GET, new byte[] {}, null, 10, 10),
                connection -> {
                    latch.countDown();

                });
        latch.await();
        Mockito.verify(httpConnectionHandler, Mockito.times(1)).connect(Mockito.any());

        final ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(httpConnectionHandler).connect((byte[]) bodyCaptor.capture());
        assertEquals(0, ((byte[]) bodyCaptor.getValue()).length);
    }

    @Test
    public void testConnectAsync_WithBody() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        networkService.connectAsync(
                new NetworkRequest("https://www.adobe.com", HttpMethod.GET, new byte[] {12, 34}, null, 10, 10),
                connection -> {
                    latch.countDown();

                });
        latch.await();
        Mockito.verify(httpConnectionHandler, Mockito.times(1)).connect(Mockito.any());

        final ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(httpConnectionHandler).connect((byte[]) bodyCaptor.capture());
        assertEquals(2, ((byte[]) bodyCaptor.getValue()).length);
        assertEquals(12, ((byte[]) bodyCaptor.getValue())[0]);
        assertEquals(34, ((byte[]) bodyCaptor.getValue())[1]);
    }

    private static ExecutorService currentThreadExecutorService() {

        // Handler for tasks that runs the task directly in the calling thread of the execute method
        ThreadPoolExecutor.CallerRunsPolicy callerRunsPolicy =
                new ThreadPoolExecutor.CallerRunsPolicy();

        return new ThreadPoolExecutor(0, 1, 0L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), callerRunsPolicy) {
            @Override
            public void execute(Runnable command) {
                // Executes task in the caller's thread
                callerRunsPolicy.rejectedExecution(command, this);
            }
        };
    }

}
