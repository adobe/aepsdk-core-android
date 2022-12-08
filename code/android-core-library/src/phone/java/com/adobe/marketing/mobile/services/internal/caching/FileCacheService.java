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

package com.adobe.marketing.mobile.services.internal.caching;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceConstants;
import com.adobe.marketing.mobile.services.caching.CacheEntry;
import com.adobe.marketing.mobile.services.caching.CacheExpiry;
import com.adobe.marketing.mobile.services.caching.CacheResult;
import com.adobe.marketing.mobile.services.caching.CacheService;
import java.io.File;
import java.util.Date;
import java.util.Map;

/** A {@code CacheService} that uses file storage as the cache store. */
public class FileCacheService implements CacheService {

    private static final String TAG = "FileCacheService";
    static final String ROOT_CACHE_DIR_NAME = "aepsdkcache";

    private final CacheFileManager cacheFileManager;

    public FileCacheService() {
        this.cacheFileManager = new CacheFileManager(ROOT_CACHE_DIR_NAME);
    }

    /**
     * Creates or updates the key-value pair in the cache. Stores the content and its metadata as
     * separate files internally in the cache directory supplied by {@code
     * DeviceInforming#getApplicationCacheDir}
     *
     * @param cacheName name of the bucket where the cache entry is to be created
     * @param key key for the cache entry
     * @param value the value that is to be associated with {@code key}
     * @return true if the value for the key was created or updated; false otherwise.
     */
    @Override
    public boolean set(
            @NonNull final String cacheName,
            @NonNull final String key,
            @NonNull final CacheEntry value) {
        // Create the bucket if necessary
        final File cacheBucket = cacheFileManager.createCache(cacheName);
        if (cacheBucket == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Could not set value for key: [%s] in cache: [%s]." + "Cache creation failed.");
            return false;
        }

        return cacheFileManager.createCacheFile(cacheName, key, value);
    }

    /**
     * Retrieves the value associated with the key being queried. Note that the {@code CacheResult}
     * returned may have additional metadata internally used by the {@code FileCacheService}.
     *
     * @param cacheName name of the bucket where the entry is to be fetched from
     * @param key key for the cache entry
     * @return the {@code FileCacheResult} associated with the key if present; null otherwise.
     */
    @Nullable @Override
    public CacheResult get(@NonNull final String cacheName, @NonNull final String key) {
        final File cacheFile = cacheFileManager.getCacheFile(cacheName, key);

        if (cacheFile == null) return null;

        final Map<String, String> cacheMetadata = cacheFileManager.getCacheMetadata(cacheName, key);

        // We lost the metadata/metadata is corrupt, but have an entry - so there must have been an
        // error.
        // Remove the cache entry as well and return null.
        if (cacheMetadata == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Could not find metadata for key: [%s] in cache: [%s].");
            remove(cacheName, key);
            return null;
        }

        final String expiryFromMetadata =
                cacheMetadata.get(FileCacheResult.METADATA_KEY_EXPIRY_IN_MILLIS);
        final CacheExpiry expiry = getExpiryFromEpoch(expiryFromMetadata);

        if (expiry.isExpired()) {
            // If the cache entry has expired by the time it was fetched, remove it
            // and return null.
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Cache entry for key: [%s] in cache: [%s] has expired.");
            remove(cacheName, key);
            return null;
        }

        return new FileCacheResult(cacheFile, expiry, cacheMetadata);
    }

    /**
     * Removes the key and entry associated with it from {@code cacheName}.
     *
     * @param cacheName name of the bucket where the entry is to be fetched from
     * @param key the key for the cache entry that is to be removed
     * @return true if the removal was successful; false otherwise.
     */
    @Override
    public boolean remove(@NonNull final String cacheName, @NonNull final String key) {
        return cacheFileManager.deleteCacheFile(cacheName, key);
    }

    /**
     * Translates the epochString provided into a {@code CacheExpiry}
     *
     * @param epochString the epoch to use for populating {@code CacheExpiry.expiry}
     * @return a valid CacheExpiry at epoch provided by {@code epochString} if valid; an expired
     *     CacheExpiry otherwise.
     */
    private CacheExpiry getExpiryFromEpoch(final String epochString) {
        try {
            return (epochString == null)
                    ? CacheExpiry.never()
                    : CacheExpiry.at(new Date(Long.parseLong(epochString)));
        } catch (final NumberFormatException e) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Failed to parse expiry from stored metadata. Marking as expired");
            return CacheExpiry.at(new Date(0));
        }
    }
}
