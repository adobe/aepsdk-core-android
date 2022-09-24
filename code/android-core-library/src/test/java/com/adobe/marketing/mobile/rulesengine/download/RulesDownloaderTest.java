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

package com.adobe.marketing.mobile.rulesengine.download;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import static java.util.Collections.emptyMap;

import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.HttpConnecting;
import com.adobe.marketing.mobile.services.HttpMethod;
import com.adobe.marketing.mobile.services.NetworkCallback;
import com.adobe.marketing.mobile.services.NetworkRequest;
import com.adobe.marketing.mobile.services.Networking;
import com.adobe.marketing.mobile.services.caching.CacheEntry;
import com.adobe.marketing.mobile.services.caching.CacheExpiry;
import com.adobe.marketing.mobile.services.caching.CacheResult;
import com.adobe.marketing.mobile.services.caching.CacheService;
import com.adobe.marketing.mobile.test.util.FileTestHelper;
import com.adobe.marketing.mobile.util.TimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class RulesDownloaderTest {
    private static final String VALID_URL = "https://assets.adobedtm.com/1234";
    private static final String RULES_TEST_CACHE_NAME = "TestRulesCache";
    private static final String SAMPLE_ETAG = "sampleETAG";
    private static final long SAMPLE_LAST_MODIFIED_MS = 50000L;
    private static final String SAMPLE_LAST_MODIFIED_RFC2822 =
            TimeUtils.getRFC2822Date(SAMPLE_LAST_MODIFIED_MS, TimeZone.getTimeZone("GMT"), Locale.US);

    private RulesDownloader rulesDownloader;

    @Mock
    private Networking mockNetworkService;

    @Mock
    private CacheService mockCacheService;

    @Mock
    private DeviceInforming mockDeviceInfoService;

    private File mockCacheDir;
    private File mockRulesZip;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mockCacheDir = new File(this.getClass().getClassLoader().getResource("").getPath()
                + File.separator + "TestCache");
        mockCacheDir.mkdirs();

        when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(mockCacheDir);

        rulesDownloader = new RulesDownloader(RULES_TEST_CACHE_NAME, mockNetworkService,
                mockCacheService, mockDeviceInfoService);
    }

    @Test
    public void testRemoteDownload_InvalidURL() {
        final RulesDownloadCallback mockCallback = mock(RulesDownloadCallback.class);

        rulesDownloader.load("not a url", mockCallback);

        verifyNoInteractions(mockNetworkService);
        verifyNoInteractions(mockCacheService);

        final ArgumentCaptor<RulesDownloadResult> resultCaptor = ArgumentCaptor.forClass(RulesDownloadResult.class);
        verify(mockCallback, times(1)).call(resultCaptor.capture());
        final RulesDownloadResult capturedResult = resultCaptor.getValue();
        assertNotNull(capturedResult);
        assertEquals(RulesDownloadResult.Reason.INVALID_SOURCE, capturedResult.getReason());
        assertNull(capturedResult.getData());
    }

    @Test
    public void testRemoteDownload_Happy_NoCachedEntryForKey() throws FileNotFoundException {
        mockRulesZip = prepareMockZipFile("rules_zip_happy/ADBMobileConfig-rules.zip");

        // Setup to return null when fetching from cache
        final RulesDownloadCallback mockCallback = mock(RulesDownloadCallback.class);
        when(mockCacheService.get(RULES_TEST_CACHE_NAME, VALID_URL)).thenReturn(null);

        // Simulate a mock network response.
        final HttpConnecting mockResponse = mock(HttpConnecting.class);
        when(mockResponse.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(mockResponse.getInputStream()).thenReturn(new FileInputStream(mockRulesZip));
        when(mockResponse.getResponsePropertyValue(RulesDownloader.HTTP_HEADER_ETAG)).thenReturn(SAMPLE_ETAG);
        when(mockResponse.getResponsePropertyValue(RulesDownloader.HTTP_HEADER_LAST_MODIFIED)).thenReturn(
                TimeUtils.getRFC2822Date(SAMPLE_LAST_MODIFIED_MS, TimeZone.getTimeZone("GMT"), Locale.US)
        );

        doAnswer(
                invocation -> {
                    final NetworkCallback callback = invocation.getArgument(1);
                    callback.call(mockResponse);
                    return null;
                }
        ).when(mockNetworkService).connectAsync(any(NetworkRequest.class), any(NetworkCallback.class));

        final NetworkRequest expectedNetworkRequest = new NetworkRequest(
                VALID_URL,
                HttpMethod.GET,
                null,
                emptyMap(),
                10000,
                10000
        );

        // Test
        rulesDownloader.load(VALID_URL, mockCallback);

        verify(mockCacheService).get(RULES_TEST_CACHE_NAME, VALID_URL);
        final ArgumentCaptor<NetworkRequest> networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest.class);
        verify(mockNetworkService, times(1)).connectAsync(networkRequestCaptor.capture(), any());
        verifyNetworkRequestParams(expectedNetworkRequest, networkRequestCaptor.getValue());

        final ArgumentCaptor<RulesDownloadResult> resultCaptor = ArgumentCaptor.forClass(RulesDownloadResult.class);
        verify(mockCallback, times(1)).call(resultCaptor.capture());

        final RulesDownloadResult capturedResult = resultCaptor.getValue();
        assertNotNull(capturedResult);
        assertEquals(RulesDownloadResult.Reason.SUCCESS, capturedResult.getReason());
        assertNotNull(capturedResult.getData());

        ArgumentCaptor<CacheEntry> cacheEntryArgumentCaptor = ArgumentCaptor.forClass(CacheEntry.class);
        verify(mockCacheService).set(eq(RULES_TEST_CACHE_NAME), eq(VALID_URL), cacheEntryArgumentCaptor.capture());
        final CacheEntry capturedCacheEntry = cacheEntryArgumentCaptor.getValue();
        assertNull(capturedCacheEntry.getExpiry().getExpiration());
        assertNotNull(capturedCacheEntry.getMetadata());
        assertEquals(SAMPLE_ETAG, capturedCacheEntry.getMetadata().get(RulesDownloader.HTTP_HEADER_ETAG));
        assertEquals(String.valueOf(SAMPLE_LAST_MODIFIED_MS), capturedCacheEntry.getMetadata().get(RulesDownloader.HTTP_HEADER_LAST_MODIFIED));
    }

    @Test
    public void testRemoteDownload_Happy_CachedFileExists() throws FileNotFoundException {
        mockRulesZip = prepareMockZipFile("rules_zip_happy/ADBMobileConfig-rules.zip");

        final RulesDownloadCallback mockCallback = mock(RulesDownloadCallback.class);

        // Setup to return a valid cache result
        final CacheResult mockCacheResult = mock(CacheResult.class);
        when(mockCacheResult.getData()).thenReturn(new FileInputStream(mockRulesZip));
        when(mockCacheResult.getExpiry()).thenReturn(CacheExpiry.never());
        final HashMap<String, String> mockCacheMetadata = new HashMap<>();
        mockCacheMetadata.put(RulesDownloader.HTTP_HEADER_ETAG, SAMPLE_ETAG);
        mockCacheMetadata.put(RulesDownloader.HTTP_HEADER_LAST_MODIFIED, String.valueOf(SAMPLE_LAST_MODIFIED_MS));
        when(mockCacheResult.getMetadata()).thenReturn(mockCacheMetadata);
        when(mockCacheService.get(RULES_TEST_CACHE_NAME, VALID_URL)).thenReturn(mockCacheResult);

        final HttpConnecting mockResponse = mock(HttpConnecting.class);
        when(mockResponse.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(mockResponse.getInputStream()).thenReturn(new FileInputStream(mockRulesZip));

        doAnswer(
                invocation -> {
                    final NetworkCallback callback = invocation.getArgument(1);
                    callback.call(mockResponse);
                    return null;
                }
        ).when(mockNetworkService).connectAsync(any(NetworkRequest.class), any(NetworkCallback.class));

        final HashMap<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put(RulesDownloader.HTTP_HEADER_IF_NONE_MATCH, SAMPLE_ETAG);
        expectedHeaders.put(RulesDownloader.HTTP_HEADER_IF_MODIFIED_SINCE, SAMPLE_LAST_MODIFIED_RFC2822);

        final NetworkRequest expectedNetworkRequest = new NetworkRequest(
                VALID_URL,
                HttpMethod.GET,
                null,
                expectedHeaders,
                10000,
                10000
        );

        // Test
        rulesDownloader.load(VALID_URL, mockCallback);

        verify(mockCacheService).get(RULES_TEST_CACHE_NAME, VALID_URL);
        final ArgumentCaptor<NetworkRequest> networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest.class);
        verify(mockNetworkService, times(1)).connectAsync(networkRequestCaptor.capture(), any());
        verifyNetworkRequestParams(expectedNetworkRequest, networkRequestCaptor.getValue());

        final ArgumentCaptor<RulesDownloadResult> resultCaptor = ArgumentCaptor.forClass(RulesDownloadResult.class);
        verify(mockCallback, times(1)).call(resultCaptor.capture());

        final RulesDownloadResult capturedResult = resultCaptor.getValue();
        assertNotNull(capturedResult);
        assertEquals(RulesDownloadResult.Reason.SUCCESS, capturedResult.getReason());
        assertNotNull(capturedResult.getData());
        verify(mockCacheService).set(eq(RULES_TEST_CACHE_NAME), eq(VALID_URL), any());
    }

    @Test
    public void testRemoteDownload_UnExtractableZipFile() throws FileNotFoundException {
        mockRulesZip = prepareMockZipFile("rules_zip_invalid/ADBMobileConfig-rules.zip");

        final RulesDownloadCallback mockCallback = mock(RulesDownloadCallback.class);
        when(mockCacheService.get(RULES_TEST_CACHE_NAME, VALID_URL)).thenReturn(null);

        final HttpConnecting mockResponse = mock(HttpConnecting.class);
        when(mockResponse.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(mockResponse.getInputStream()).thenReturn(new FileInputStream(mockRulesZip));

        doAnswer(
                invocation -> {
                    final NetworkCallback callback = invocation.getArgument(1);
                    callback.call(mockResponse);
                    return null;
                }
        ).when(mockNetworkService).connectAsync(any(NetworkRequest.class), any(NetworkCallback.class));


        final NetworkRequest expectedNetworkRequest = new NetworkRequest(
                VALID_URL,
                HttpMethod.GET,
                null,
                emptyMap(),
                10000,
                10000
        );

        rulesDownloader.load(VALID_URL, mockCallback);

        verify(mockCacheService).get(RULES_TEST_CACHE_NAME, VALID_URL);
        final ArgumentCaptor<NetworkRequest> networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest.class);
        verify(mockNetworkService, times(1)).connectAsync(networkRequestCaptor.capture(), any());
        verifyNetworkRequestParams(expectedNetworkRequest, networkRequestCaptor.getValue());

        final ArgumentCaptor<RulesDownloadResult> resultCaptor = ArgumentCaptor.forClass(RulesDownloadResult.class);
        verify(mockCallback, times(1)).call(resultCaptor.capture());

        final RulesDownloadResult capturedResult = resultCaptor.getValue();
        assertNotNull(capturedResult);
        assertEquals(RulesDownloadResult.Reason.ZIP_EXTRACTION_FAILED, capturedResult.getReason());
        assertNull(capturedResult.getData());
    }

    @Test
    public void testRemoteDownload_ZipFileCannotBeCopied() throws IOException {
        final InputStream mockInputStream = mock(InputStream.class);
        when(mockInputStream.read(any())).thenThrow(new SecurityException());

        final RulesDownloadCallback mockCallback = mock(RulesDownloadCallback.class);
        when(mockCacheService.get(RULES_TEST_CACHE_NAME, VALID_URL)).thenReturn(null);

        final HttpConnecting mockResponse = mock(HttpConnecting.class);
        when(mockResponse.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(mockResponse.getInputStream()).thenReturn(mockInputStream);

        doAnswer(
                invocation -> {
                    final NetworkCallback callback = invocation.getArgument(1);
                    callback.call(mockResponse);
                    return null;
                }
        ).when(mockNetworkService).connectAsync(any(NetworkRequest.class), any(NetworkCallback.class));


        final NetworkRequest expectedNetworkRequest = new NetworkRequest(
                VALID_URL,
                HttpMethod.GET,
                null,
                emptyMap(),
                10000,
                10000
        );

        rulesDownloader.load(VALID_URL, mockCallback);

        verify(mockCacheService).get(RULES_TEST_CACHE_NAME, VALID_URL);
        final ArgumentCaptor<NetworkRequest> networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest.class);
        verify(mockNetworkService, times(1)).connectAsync(networkRequestCaptor.capture(), any());
        verifyNetworkRequestParams(expectedNetworkRequest, networkRequestCaptor.getValue());

        final ArgumentCaptor<RulesDownloadResult> resultCaptor = ArgumentCaptor.forClass(RulesDownloadResult.class);
        verify(mockCallback, times(1)).call(resultCaptor.capture());

        final RulesDownloadResult capturedResult = resultCaptor.getValue();
        assertNotNull(capturedResult);
        assertEquals(RulesDownloadResult.Reason.CANNOT_STORE_IN_TEMP_DIR, capturedResult.getReason());
        assertNull(capturedResult.getData());
    }

    @Test
    public void testRemoteDownload_InvalidZipContents() throws FileNotFoundException {
        mockRulesZip = prepareMockZipFile("rules_zip_invalid_content/ADBMobileConfig-rules.zip");
        final RulesDownloadCallback mockCallback = mock(RulesDownloadCallback.class);
        when(mockCacheService.get(RULES_TEST_CACHE_NAME, VALID_URL)).thenReturn(null);

        final HttpConnecting mockResponse = mock(HttpConnecting.class);
        when(mockResponse.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(mockResponse.getInputStream()).thenReturn(new FileInputStream(mockRulesZip));

        doAnswer(
                invocation -> {
                    final NetworkCallback callback = invocation.getArgument(1);
                    callback.call(mockResponse);
                    return null;
                }
        ).when(mockNetworkService).connectAsync(any(NetworkRequest.class), any(NetworkCallback.class));


        final NetworkRequest expectedNetworkRequest = new NetworkRequest(
                VALID_URL,
                HttpMethod.GET,
                null,
                emptyMap(),
                10000,
                10000
        );

        rulesDownloader.load(VALID_URL, mockCallback);

        final ArgumentCaptor<NetworkRequest> networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest.class);
        verify(mockNetworkService, times(1)).connectAsync(networkRequestCaptor.capture(), any());
        verifyNetworkRequestParams(expectedNetworkRequest, networkRequestCaptor.getValue());

        final ArgumentCaptor<RulesDownloadResult> resultCaptor = ArgumentCaptor.forClass(RulesDownloadResult.class);
        verify(mockCallback, times(1)).call(resultCaptor.capture());

        final RulesDownloadResult capturedResult = resultCaptor.getValue();
        assertNotNull(capturedResult);
        assertEquals(RulesDownloadResult.Reason.ZIP_EXTRACTION_FAILED, capturedResult.getReason());
        assertNull(capturedResult.getData());
    }

    @Test
    public void testRemoteDownload_Response_HTTP_NOT_MODIFIED() throws FileNotFoundException {
        mockRulesZip = prepareMockZipFile("rules_zip_happy/ADBMobileConfig-rules.zip");
        // Setup to return a valid cache result
        final CacheResult mockCacheResult = mock(CacheResult.class);
        when(mockCacheResult.getData()).thenReturn(new FileInputStream(mockRulesZip));
        when(mockCacheResult.getExpiry()).thenReturn(CacheExpiry.never());

        final HashMap<String, String> mockCacheMetadata = new HashMap<>();
        mockCacheMetadata.put(RulesDownloader.HTTP_HEADER_ETAG, SAMPLE_ETAG);
        mockCacheMetadata.put(RulesDownloader.HTTP_HEADER_LAST_MODIFIED, String.valueOf(SAMPLE_LAST_MODIFIED_MS));
        when(mockCacheResult.getMetadata()).thenReturn(mockCacheMetadata);
        when(mockCacheService.get(RULES_TEST_CACHE_NAME, VALID_URL)).thenReturn(mockCacheResult);

        final HttpConnecting mockResponse = mock(HttpConnecting.class);
        when(mockResponse.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_MODIFIED);
        when(mockResponse.getInputStream()).thenReturn(null);

        doAnswer(
                invocation -> {
                    final NetworkCallback callback = invocation.getArgument(1);
                    callback.call(mockResponse);
                    return null;
                }
        ).when(mockNetworkService).connectAsync(any(NetworkRequest.class), any(NetworkCallback.class));

        final HashMap<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put(RulesDownloader.HTTP_HEADER_IF_NONE_MATCH, SAMPLE_ETAG);
        expectedHeaders.put(RulesDownloader.HTTP_HEADER_IF_MODIFIED_SINCE, SAMPLE_LAST_MODIFIED_RFC2822);

        final NetworkRequest expectedNetworkRequest = new NetworkRequest(
                VALID_URL,
                HttpMethod.GET,
                null,
                expectedHeaders,
                10000,
                10000
        );

        // Test
        final RulesDownloadCallback mockCallback = mock(RulesDownloadCallback.class);
        rulesDownloader.load(VALID_URL, mockCallback);

        final ArgumentCaptor<NetworkRequest> networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest.class);
        verify(mockNetworkService, times(1)).connectAsync(networkRequestCaptor.capture(), any());
        verifyNetworkRequestParams(expectedNetworkRequest, networkRequestCaptor.getValue());

        final ArgumentCaptor<RulesDownloadResult> resultCaptor = ArgumentCaptor.forClass(RulesDownloadResult.class);
        verify(mockCallback, times(1)).call(resultCaptor.capture());

        final RulesDownloadResult capturedResult = resultCaptor.getValue();
        assertNotNull(capturedResult);
        assertEquals(RulesDownloadResult.Reason.NOT_MODIFIED, capturedResult.getReason());
        assertNull(capturedResult.getData());
    }

    @Test
    public void testRemoteDownload_Response_HTTP_NOT_FOUND() throws FileNotFoundException {
        final RulesDownloadCallback mockCallback = mock(RulesDownloadCallback.class);
        when(mockCacheService.get(RULES_TEST_CACHE_NAME, VALID_URL)).thenReturn(null);

        final HttpConnecting mockResponse = mock(HttpConnecting.class);
        when(mockResponse.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(mockResponse.getInputStream()).thenReturn(null);

        doAnswer(
                invocation -> {
                    final NetworkCallback callback = invocation.getArgument(1);
                    callback.call(mockResponse);
                    return null;
                }
        ).when(mockNetworkService).connectAsync(any(NetworkRequest.class), any(NetworkCallback.class));

        final NetworkRequest expectedNetworkRequest = new NetworkRequest(
                VALID_URL,
                HttpMethod.GET,
                null,
                emptyMap(),
                10000,
                10000
        );

        rulesDownloader.load(VALID_URL, mockCallback);

        verify(mockCacheService).get(RULES_TEST_CACHE_NAME, VALID_URL);
        final ArgumentCaptor<NetworkRequest> networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest.class);
        verify(mockNetworkService, times(1)).connectAsync(networkRequestCaptor.capture(), any());
        verifyNetworkRequestParams(expectedNetworkRequest, networkRequestCaptor.getValue());

        final ArgumentCaptor<RulesDownloadResult> resultCaptor = ArgumentCaptor.forClass(RulesDownloadResult.class);
        verify(mockCallback, times(1)).call(resultCaptor.capture());

        final RulesDownloadResult capturedResult = resultCaptor.getValue();
        assertNotNull(capturedResult);
        assertEquals(RulesDownloadResult.Reason.NO_DATA, capturedResult.getReason());
        assertNull(capturedResult.getData());
    }

    @Test
    public void testLocalAssetDownload_Happy() throws FileNotFoundException {
        final String assetName = "ADBMobileConfig-rules.zip";
        mockRulesZip = prepareMockZipFile("rules_zip_happy/ADBMobileConfig-rules.zip");
        when(mockDeviceInfoService.getAsset(assetName)).thenReturn(new FileInputStream(mockRulesZip));

        final RulesDownloadResult rulesDownloadResult = rulesDownloader.load(assetName);
        assertEquals(RulesDownloadResult.Reason.SUCCESS, rulesDownloadResult.getReason());
        assertNotNull(rulesDownloadResult.getData());
    }

    @Test
    public void testLocalAssetDownload_EmptyAssetName() throws FileNotFoundException {
        when(mockDeviceInfoService.getAsset(any())).thenReturn(null);

        final RulesDownloadResult rulesDownloadResult = rulesDownloader.load("");
        assertEquals(RulesDownloadResult.Reason.INVALID_SOURCE, rulesDownloadResult.getReason());
        assertNull(rulesDownloadResult.getData());
    }

    @Test
    public void testLocalAssetDownload_NoAssetFile() throws FileNotFoundException {
        when(mockDeviceInfoService.getAsset(any())).thenReturn(null);

        final RulesDownloadResult rulesDownloadResult = rulesDownloader.load("someNonExistentAsset");
        assertEquals(RulesDownloadResult.Reason.INVALID_SOURCE, rulesDownloadResult.getReason());
        assertNull(rulesDownloadResult.getData());
    }

    @Test
    public void testLocalAssetDownload_UnExtractableRulesZip() throws FileNotFoundException {
        final String assetName = "rules_zip_invalid/ADBMobileConfig-rules.zip";
        mockRulesZip = prepareMockZipFile(assetName);
        when(mockDeviceInfoService.getAsset(assetName)).thenReturn(new FileInputStream(mockRulesZip));

        final RulesDownloadResult rulesDownloadResult = rulesDownloader.load(assetName);
        assertEquals(RulesDownloadResult.Reason.ZIP_EXTRACTION_FAILED, rulesDownloadResult.getReason());
        assertNull(rulesDownloadResult.getData());
    }

    @Test
    public void testLocalAssetDownload_InvalidContentRulesZip() throws FileNotFoundException {
        final String assetName = "rules_zip_invalid_content/ADBMobileConfig-rules.zip";
        mockRulesZip = prepareMockZipFile(assetName);
        when(mockDeviceInfoService.getAsset(assetName)).thenReturn(new FileInputStream(mockRulesZip));

        final RulesDownloadResult rulesDownloadResult = rulesDownloader.load(assetName);
        assertEquals(RulesDownloadResult.Reason.ZIP_EXTRACTION_FAILED, rulesDownloadResult.getReason());
        assertNull(rulesDownloadResult.getData());
    }

    @Test
    public void testLocalAssetDownload_CannotWriteToCacheDir() throws FileNotFoundException {
        final String assetName = "rules_zip_happy/ADBMobileConfig-rules.zip";
        mockRulesZip = prepareMockZipFile(assetName);
        when(mockDeviceInfoService.getAsset(assetName)).thenReturn(new FileInputStream(mockRulesZip));
        mockCacheDir.setWritable(false);

        final RulesDownloadResult rulesDownloadResult = rulesDownloader.load(assetName);
        assertEquals(RulesDownloadResult.Reason.CANNOT_CREATE_TEMP_DIR, rulesDownloadResult.getReason());
        assertNull(rulesDownloadResult.getData());
    }

    private void verifyNetworkRequestParams(final NetworkRequest expectedNetworkRequest,
                                            final NetworkRequest actualNetworkRequest) {
        assertEquals(expectedNetworkRequest.getUrl(), actualNetworkRequest.getUrl());
        assertEquals(expectedNetworkRequest.getMethod(), actualNetworkRequest.getMethod());
        assertEquals(expectedNetworkRequest.getBody(), actualNetworkRequest.getBody());
        assertEquals(expectedNetworkRequest.getConnectTimeout(), actualNetworkRequest.getConnectTimeout());
        assertEquals(expectedNetworkRequest.getReadTimeout(), actualNetworkRequest.getReadTimeout());
        assertEquals(expectedNetworkRequest.getHeaders(), actualNetworkRequest.getHeaders());
    }

    @After
    public void tearDown() throws Exception {
        mockCacheDir.setWritable(true);
        mockCacheDir.setReadable(true);

        FileTestHelper.deleteFile(mockCacheDir, true);
    }

    private File prepareMockZipFile(final String zipFileResourcePath) {
        try {
            return new File(this.getClass().getClassLoader().getResource(zipFileResourcePath).getPath());
        } catch (final Exception e) {
            return null;
        }
    }
}