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

package com.adobe.marketing.mobile.launch.rulesengine.download;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.services.HttpConnecting;
import com.adobe.marketing.mobile.services.HttpMethod;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.NetworkCallback;
import com.adobe.marketing.mobile.services.NetworkRequest;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.caching.CacheEntry;
import com.adobe.marketing.mobile.services.caching.CacheExpiry;
import com.adobe.marketing.mobile.services.caching.CacheResult;
import com.adobe.marketing.mobile.util.StreamUtils;
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
 * Facilitates the download and caching of rules from a url as well as an asset bundled with the
 * app.
 */
public class RulesLoader {

    private static final String TAG = "RulesLoader";

    private static final int DEFAULT_CONNECTION_TIMEOUT_MS = 10000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 10000;

    static final String HTTP_HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
    static final String HTTP_HEADER_IF_NONE_MATCH = "If-None-Match";
    static final String HTTP_HEADER_LAST_MODIFIED = "Last-Modified";
    static final String HTTP_HEADER_ETAG = "ETag";

    /** The cache name used for storing the downloaded results. */
    private final String cacheName;

    private final RulesZipProcessingHelper rulesZipProcessingHelper;

    public RulesLoader(@NonNull final String cacheName) {
        this(cacheName, new RulesZipProcessingHelper());
    }

    @VisibleForTesting
    RulesLoader(
            @NonNull final String cacheName,
            @NonNull final RulesZipProcessingHelper rulesZipProcessingHelper) {
        if (StringUtils.isNullOrEmpty(cacheName))
            throw new IllegalArgumentException("Name cannot be null or empty");

        this.cacheName = cacheName;
        this.rulesZipProcessingHelper = rulesZipProcessingHelper;
    }

    /**
     * Loads rules from the {@code url} and invokes {@code callback} with the extracted rules.
     * Additionally, the extracted content is cached in cache bucket with name {@code name} and
     * {@code url} as the key in {@code CacheService}.
     *
     * @param url the url from which the compressed rules are to be downloaded
     * @param callback the callback that will be invoked with the result of the download
     */
    public void loadFromUrl(
            @NonNull final String url, @NonNull final AdobeCallback<RulesLoadResult> callback) {
        if (!UrlUtils.isValidUrl(url)) {
            Log.trace(TAG, cacheName, "Provided download url: %s is null or empty. ", url);
            callback.call(new RulesLoadResult(null, RulesLoadResult.Reason.INVALID_SOURCE));
            return;
        }

        final CacheResult cacheResult =
                ServiceProvider.getInstance().getCacheService().get(cacheName, url);

        final NetworkRequest networkRequest =
                new NetworkRequest(
                        url,
                        HttpMethod.GET,
                        null,
                        extractHeadersFromCache(cacheResult),
                        DEFAULT_CONNECTION_TIMEOUT_MS,
                        DEFAULT_READ_TIMEOUT_MS);

        final NetworkCallback networkCallback =
                response -> {
                    final RulesLoadResult result = handleDownloadResponse(url, response);

                    if (response != null) {
                        response.close();
                    }

                    callback.call(result);
                };

        ServiceProvider.getInstance()
                .getNetworkService()
                .connectAsync(networkRequest, networkCallback);
    }

    /**
     * Loads rules from an asset bundled with the app and returns the extracted rules. Additionally,
     * the extracted content is cached in cache bucket with name {@code RulesLoader.getCacheName()}
     * and {@code assetName} as the key in {@code CacheService}.
     *
     * @param assetName the asset name from where the rules must be fetched
     * @return {@code RulesDownloadResult} indicating the result of the load operation.
     */
    @NonNull public RulesLoadResult loadFromAsset(@NonNull final String assetName) {
        if (StringUtils.isNullOrEmpty(assetName)) {
            new RulesLoadResult(null, RulesLoadResult.Reason.INVALID_SOURCE);
        }

        final InputStream bundledRulesStream =
                ServiceProvider.getInstance().getDeviceInfoService().getAsset(assetName);
        if (bundledRulesStream == null) {
            Log.trace(TAG, cacheName, "Provided asset: %s is invalid.", assetName);
            return new RulesLoadResult(null, RulesLoadResult.Reason.INVALID_SOURCE);
        }

        return extractRules(assetName, bundledRulesStream, new HashMap<>());
    }

    /**
     * Loads rules that were previously cached via {@code loadFromAsset()} or {@code loadFromUrl}
     *
     * @param key the asset name or url that was previously used for loading and storing rules via
     *     {@code loadFromAsset()} or {@code loadFromUrl}
     * @return {@code RulesDownloadResult} indicating the result of the load operation.
     */
    @NonNull public RulesLoadResult loadFromCache(@NonNull final String key) {
        if (StringUtils.isNullOrEmpty(key)) {
            return new RulesLoadResult(null, RulesLoadResult.Reason.INVALID_SOURCE);
        }

        final CacheResult cacheResult =
                ServiceProvider.getInstance().getCacheService().get(cacheName, key);
        if (cacheResult == null) {
            return new RulesLoadResult(null, RulesLoadResult.Reason.NO_DATA);
        }

        return new RulesLoadResult(
                StreamUtils.readAsString(cacheResult.getData()), RulesLoadResult.Reason.SUCCESS);
    }

    /**
     * Gets the cache name that will be used for storing and retrieving the rules when using
     * operations of this class.
     *
     * @return cache name that will be used for storing and retrieving the rules.
     */
    @NonNull public String getCacheName() {
        return cacheName;
    }

    private RulesLoadResult handleDownloadResponse(
            final String url, final HttpConnecting response) {

        if (response == null) {
            Log.trace(TAG, cacheName, "Received null response.");
            return new RulesLoadResult(null, RulesLoadResult.Reason.NO_DATA);
        }

        switch (response.getResponseCode()) {
            case HttpURLConnection.HTTP_OK:
                return extractRules(
                        url, response.getInputStream(), extractMetadataFromResponse(response));
            case HttpURLConnection.HTTP_NOT_MODIFIED:
                return new RulesLoadResult(null, RulesLoadResult.Reason.NOT_MODIFIED);
            case HttpURLConnection.HTTP_NOT_FOUND:
            default:
                Log.trace(
                        TAG,
                        cacheName,
                        "Received download response: %s",
                        response.getResponseCode());
                return new RulesLoadResult(null, RulesLoadResult.Reason.NO_DATA);
        }
    }

    /**
     * Responsible for reading and extracting {@code zipContentStream} and returning a {@code
     * RulesDownloadResult} with rules. if successful. If the extraction is unsuccessful, returns a
     * {@code RulesDownloadResult} with the error reason.
     *
     * @param key the key that will be used for e
     * @param zipContentStream the zip stream that will need to be processed
     * @param metadata any metadata associated with the zipContentStream
     */
    private RulesLoadResult extractRules(
            final String key,
            final InputStream zipContentStream,
            final Map<String, String> metadata) {
        if (zipContentStream == null) {
            Log.debug(TAG, cacheName, "Zip content stream is null");
            return new RulesLoadResult(null, RulesLoadResult.Reason.NO_DATA);
        }

        // Attempt to create a temporary directory for copying the zipContentStream
        if (!rulesZipProcessingHelper.createTemporaryRulesDirectory(key)) {
            Log.debug(
                    TAG,
                    cacheName,
                    "Cannot access application cache directory to create temp dir.");
            return new RulesLoadResult(null, RulesLoadResult.Reason.CANNOT_CREATE_TEMP_DIR);
        }

        // Copy the content of zipContentStream into the previously created temporary folder
        if (!rulesZipProcessingHelper.storeRulesInTemporaryDirectory(key, zipContentStream)) {
            Log.debug(TAG, cacheName, "Cannot read response content into temp dir.");
            return new RulesLoadResult(null, RulesLoadResult.Reason.CANNOT_STORE_IN_TEMP_DIR);
        }

        // Extract the rules zip
        final String rules = rulesZipProcessingHelper.unzipRules(key);
        if (rules == null) {
            Log.debug(TAG, cacheName, "Failed to extract rules response zip into temp dir.");
            return new RulesLoadResult(null, RulesLoadResult.Reason.ZIP_EXTRACTION_FAILED);
        }

        // Cache the extracted contents
        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream(rules.getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.never(),
                        metadata);
        final boolean cached =
                ServiceProvider.getInstance().getCacheService().set(cacheName, key, cacheEntry);
        if (!cached) {
            Log.debug(TAG, cacheName, "Could not cache rules from source %s", key);
        }

        // Delete the temporary directory created for processing
        rulesZipProcessingHelper.deleteTemporaryDirectory(key);

        return new RulesLoadResult(rules, RulesLoadResult.Reason.SUCCESS);
    }

    /**
     * Extracts the response properties (like {@code HTTP_HEADER_ETAG} , {@code
     * HTTP_HEADER_LAST_MODIFIED} that are useful as cache metadata.
     *
     * @param response the {@code HttpConnecting} from where the response properties should be
     *     extracted from
     * @return a map of metadata keys and their values as obrained from the {@code response}
     */
    private HashMap<String, String> extractMetadataFromResponse(final HttpConnecting response) {
        final HashMap<String, String> metadata = new HashMap<>();

        final String lastModifiedProp =
                response.getResponsePropertyValue(HTTP_HEADER_LAST_MODIFIED);
        final Date lastModifiedDate =
                TimeUtils.parseRFC2822Date(
                        lastModifiedProp, TimeZone.getTimeZone("GMT"), Locale.US);
        final String lastModifiedMetadata =
                lastModifiedDate == null
                        ? String.valueOf(new Date(0L).getTime())
                        : String.valueOf(lastModifiedDate.getTime());
        metadata.put(HTTP_HEADER_LAST_MODIFIED, lastModifiedMetadata);

        final String eTagProp = response.getResponsePropertyValue(HTTP_HEADER_ETAG);
        metadata.put(HTTP_HEADER_ETAG, eTagProp == null ? "" : eTagProp);

        return metadata;
    }

    /**
     * Creates http headers for conditional fetching, based on the metadata of the {@code
     * CacheResult} provided.
     *
     * @param cacheResult the cache result whose metadata should be used for finding headers
     * @return a map of headers (HTTP_HEADER_IF_MODIFIED_SINCE, HTTP_HEADER_IF_NONE_MATCH) that can
     *     be used while fetching any modified content.
     */
    private Map<String, String> extractHeadersFromCache(final CacheResult cacheResult) {
        final Map<String, String> headers = new HashMap<>();
        if (cacheResult == null) {
            return headers;
        }

        final Map<String, String> metadata = cacheResult.getMetadata();
        final String eTag = metadata == null ? "" : metadata.get(HTTP_HEADER_ETAG);
        headers.put(HTTP_HEADER_IF_NONE_MATCH, eTag != null ? eTag : "");

        // Last modified in cache metadata is stored in epoch string. So Convert it to RFC-2822 date
        // format.
        final String lastModified =
                metadata == null ? null : metadata.get(HTTP_HEADER_LAST_MODIFIED);
        long lastModifiedEpoch;
        try {
            lastModifiedEpoch = lastModified != null ? Long.parseLong(lastModified) : 0L;
        } catch (final NumberFormatException e) {
            lastModifiedEpoch = 0L;
        }

        final String ifModifiedSince =
                TimeUtils.getRFC2822Date(lastModifiedEpoch, TimeZone.getTimeZone("GMT"), Locale.US);
        headers.put(HTTP_HEADER_IF_MODIFIED_SINCE, ifModifiedSince);
        return headers;
    }
}
