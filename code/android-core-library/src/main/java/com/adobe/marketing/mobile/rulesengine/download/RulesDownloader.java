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

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.HttpConnecting;
import com.adobe.marketing.mobile.services.HttpMethod;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.NetworkCallback;
import com.adobe.marketing.mobile.services.NetworkRequest;
import com.adobe.marketing.mobile.services.Networking;
import com.adobe.marketing.mobile.services.caching.CacheEntry;
import com.adobe.marketing.mobile.services.caching.CacheExpiry;
import com.adobe.marketing.mobile.services.caching.CacheResult;
import com.adobe.marketing.mobile.services.caching.CacheService;
import com.adobe.marketing.mobile.util.StringUtils;
import com.adobe.marketing.mobile.util.TimeUtils;
import com.adobe.marketing.mobile.util.UrlUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Facilitates the download and caching of rules from a url as well as an asset bundled with the app.
 */
public class RulesDownloader {
    private static final String TAG = "RulesDownloader";

    private static final int DEFAULT_CONNECTION_TIMEOUT_MS = 10000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 10000;

    static final String HTTP_HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
    static final String HTTP_HEADER_IF_NONE_MATCH = "If-None-Match";
    static final String HTTP_HEADER_LAST_MODIFIED = "Last-Modified";
    static final String HTTP_HEADER_ETAG = "ETag";

    /**
     * The cache name used for storing the downloaded results.
     */
    private final String cacheName;

    private final Networking networkService;
    private final CacheService cacheService;
    private final DeviceInforming deviceInfoService;
    private final RulesZipProcessingHelper rulesZipProcessingHelper;


    public RulesDownloader(@NonNull final String cacheName,
                           @NonNull final Networking networkService,
                           @NonNull final CacheService cacheService,
                           @NonNull final DeviceInforming deviceInfoService) {
        this(cacheName, networkService, cacheService, deviceInfoService, new RulesZipProcessingHelper(deviceInfoService));

    }

    @VisibleForTesting
    RulesDownloader(@NonNull final String cacheName,
                    @NonNull final Networking networkService,
                    @NonNull final CacheService cacheService,
                    @NonNull final DeviceInforming deviceInfoService,
                    @NonNull final RulesZipProcessingHelper rulesZipProcessingHelper) {

        if (StringUtils.isNullOrEmpty(cacheName))
            throw new IllegalArgumentException("Name cannot be null or empty");

        this.cacheName = cacheName;
        this.networkService = networkService;
        this.cacheService = cacheService;
        this.deviceInfoService = deviceInfoService;
        this.rulesZipProcessingHelper = rulesZipProcessingHelper;
    }

    /**
     * Loads rules from the {@code url} and invokes {@code callback} with the extracted rules.
     * Additionally, the extracted content is cached in cache bucket with name {@code name} and {@code url} as the key
     * in {@code CacheService}.
     *
     * @param url      the url from which the compressed rules are to be downloaded
     * @param callback the callback that will be invoked with the result of the download
     */
    public void load(@NonNull final String url,
                     @NonNull final RulesDownloadCallback callback) {
        if (!UrlUtils.isValidUrl(url)) {
            Log.trace(TAG, cacheName, "Provided download url: %s is null or empty. ", url);
            callback.call(new RulesDownloadResult(null, RulesDownloadResult.Reason.INVALID_SOURCE));
            return;
        }

        final CacheResult cacheResult = cacheService.get(cacheName, url);

        final NetworkRequest networkRequest = new NetworkRequest(
                url,
                HttpMethod.GET,
                null,
                extractHeadersFromCache(cacheResult),
                DEFAULT_CONNECTION_TIMEOUT_MS,
                DEFAULT_READ_TIMEOUT_MS
        );

        final NetworkCallback networkCallback = response -> {
            final RulesDownloadResult result = handleDownloadResponse(url, response);
            callback.call(result);
        };

        networkService.connectAsync(networkRequest, networkCallback);
    }

    /**
     * Loads rules from an asset bundled with the app and returns the extracted rules.
     * Additionally, the extracted content is cached in cache bucket with name {@code RulesDownloader.name}
     * and {@code assetName} as the key in {@code CacheService}.
     *
     * @param assetName the asset name from where the rules must be fetched
     * @return {@code RulesDownloadResult} indicating the result of the download.
     */
    @NonNull
    public RulesDownloadResult load(@NonNull final String assetName) {
        if (StringUtils.isNullOrEmpty(assetName)) {
            new RulesDownloadResult(null, RulesDownloadResult.Reason.INVALID_SOURCE);
        }

        final InputStream bundledRulesStream = deviceInfoService.getAsset(assetName);
        if (bundledRulesStream == null) {
            Log.trace(TAG, cacheName, "Provided asset: %s is invalid.", assetName);
            return new RulesDownloadResult(null, RulesDownloadResult.Reason.INVALID_SOURCE);
        }

        return extractRules(assetName, bundledRulesStream, new HashMap<>());
    }

    @NonNull
    public String getCacheName() {
        return cacheName;
    }

    private RulesDownloadResult handleDownloadResponse(final String url, final HttpConnecting response) {
        switch (response.getResponseCode()) {
            case HttpURLConnection.HTTP_OK:
                return extractRules(url, response.getInputStream(), extractMetadataFromResponse(response));

            case HttpURLConnection.HTTP_NOT_MODIFIED:
                return new RulesDownloadResult(null, RulesDownloadResult.Reason.NOT_MODIFIED);

            case HttpURLConnection.HTTP_NOT_FOUND:
            default:
                Log.trace(TAG, cacheName, "Received download response: %s", response.getResponseCode());
                return new RulesDownloadResult(null, RulesDownloadResult.Reason.NO_DATA);
        }
    }

    /**
     * Responsible for reading and extracting {@code zipContentStream} and returning a {@code RulesDownloadResult}
     *  with rules. if successful. If the extraction is unsuccessful, returns a {@code RulesDownloadResult} with the
     *  error reason.
     *
     * @param key              the key that will be used for e
     * @param zipContentStream the zip stream that will need to be processed
     * @param metadata         any metadata associated with the zipContentStream
     */
    private RulesDownloadResult extractRules(final String key,
                                             final InputStream zipContentStream,
                                             final Map<String, String> metadata) {

        if (zipContentStream == null) {
            Log.trace(TAG, cacheName, "Zip content stream is null");
            return new RulesDownloadResult(null, RulesDownloadResult.Reason.NO_DATA);
        }

        // Attempt to create a temporary directory for copying the zipContentStream
        if (!rulesZipProcessingHelper.createTemporaryRulesDirectory(key)) {
            Log.trace(TAG, cacheName, "Cannot access application cache directory to create temp dir.");
            return new RulesDownloadResult(null, RulesDownloadResult.Reason.CANNOT_CREATE_TEMP_DIR);
        }

        // Copy the content of zipContentStream into the previously created temporary folder
        if (!rulesZipProcessingHelper.storeRulesInTemporaryDirectory(key, zipContentStream)) {
            Log.trace(TAG, cacheName, "Cannot read response content into temp dir.");
            return new RulesDownloadResult(null, RulesDownloadResult.Reason.CANNOT_STORE_IN_TEMP_DIR);
        }

        // Extract the rules zip
        final String rules = rulesZipProcessingHelper.unzipRules(key);
        if (rules == null) {
            Log.trace(TAG, cacheName, "Failed to extract rules response zip into temp dir.");
            return new RulesDownloadResult(null, RulesDownloadResult.Reason.ZIP_EXTRACTION_FAILED);
        }

        // Cache the extracted contents
        final CacheEntry cacheEntry = new CacheEntry(new ByteArrayInputStream(rules.getBytes(StandardCharsets.UTF_8)),
                CacheExpiry.never(), metadata);
        final boolean cached = cacheService.set(cacheName, key, cacheEntry);
        if (!cached) {
            Log.trace(TAG, cacheName, "Could not cache rules from source %s", key);
        }

        // Delete the temporary directory created for processing
        rulesZipProcessingHelper.deleteTemporaryDirectory(key);

        return new RulesDownloadResult(rules, RulesDownloadResult.Reason.SUCCESS);
    }

    /**
     * Extracts the response properties (like {@code HTTP_HEADER_ETAG} , {@code HTTP_HEADER_LAST_MODIFIED}
     * that are useful as cache metadata.
     *
     * @param response the {@code HttpConnecting} from where the response properties should be extracted from
     * @return a map of metadata keys and their values as obrained from the {@code response}
     */
    private HashMap<String, String> extractMetadataFromResponse(final HttpConnecting response) {
        final HashMap<String, String> metadata = new HashMap<>();

        final String lastModifiedProp = response.getResponsePropertyValue(HTTP_HEADER_LAST_MODIFIED);
        final Date lastModifiedDate = TimeUtils.parseRFC2822Date(
                lastModifiedProp, TimeZone.getTimeZone("GMT"), Locale.US);
        final String lastModifiedMetadata = lastModifiedDate == null
                ? String.valueOf(new Date(0L).getTime())
                : String.valueOf(lastModifiedDate.getTime());
        metadata.put(HTTP_HEADER_LAST_MODIFIED, lastModifiedMetadata);

        final String eTagProp = response.getResponsePropertyValue(HTTP_HEADER_ETAG);
        metadata.put(HTTP_HEADER_ETAG, eTagProp == null ? "" : eTagProp);

        return metadata;
    }

    /**
     * Creates http headers for conditional fetching, based on the metadata of the
     * {@code CacheResult} provided.
     *
     * @param cacheResult the cache result whose metadata should be used for finding headers
     * @return a map of headers (HTTP_HEADER_IF_MODIFIED_SINCE, HTTP_HEADER_IF_NONE_MATCH)
     * that can be used while fetching any modified content.
     */
    private Map<String, String> extractHeadersFromCache(final CacheResult cacheResult) {
        final Map<String, String> headers = new HashMap<>();
        if (cacheResult == null) {
            return headers;
        }

        final Map<String, String> metadata = cacheResult.getMetadata();
        final String eTag = metadata == null ? "" : metadata.get(HTTP_HEADER_ETAG);
        headers.put(HTTP_HEADER_IF_NONE_MATCH, eTag != null ? eTag : "");

        // Last modified in cache metadata is stored in epoch string. So Convert it to RFC-2822 date format.
        final String lastModified = metadata == null ? null : metadata.get(HTTP_HEADER_LAST_MODIFIED);
        final long lastModifiedEpoch = lastModified != null ? Long.parseLong(lastModified) : 0L;
        final String ifModifiedSince = TimeUtils.getRFC2822Date(lastModifiedEpoch,
                TimeZone.getTimeZone("GMT"), Locale.US);
        headers.put(HTTP_HEADER_IF_MODIFIED_SINCE, ifModifiedSince);
        return headers;
    }
}
