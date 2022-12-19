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
import androidx.annotation.VisibleForTesting;
import com.adobe.marketing.mobile.internal.util.FileUtils;
import com.adobe.marketing.mobile.internal.util.StringEncoder;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceConstants;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.caching.CacheEntry;
import com.adobe.marketing.mobile.util.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/** Helper class for {@code FileCacheService} used for managing cache files and their metadata. */
class CacheFileManager {

    private static final String TAG = "CacheFileManager";

    @VisibleForTesting static final String CACHE_METADATA_FILE_EXT = "_metadata.txt";

    private final String rootCacheDirName;

    CacheFileManager(@NonNull final String rootCacheDirName) {
        this.rootCacheDirName = rootCacheDirName;
    }

    /**
     * Creates a directory with name {@code cacheName} inside the {@code cacheRoot} folder of {@code
     * deviceInfoService.getApplicationCacheDir()}
     *
     * @param cacheName the name of the folder to act as a cache bucket.
     * @return a {@code File} handle for the newly created/existing bucket; null if the directory
     *     creation fails.
     */
    File createCache(@NonNull final String cacheName) {
        if (StringUtils.isNullOrEmpty(cacheName)) return null;

        final File appCacheDir =
                ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir();
        if (!FileUtils.isWritableDirectory(appCacheDir)) {
            Log.debug(ServiceConstants.LOG_TAG, TAG, "App cache directory is not writable.");
            return null;
        }

        final File cacheBucket =
                new File(appCacheDir, rootCacheDirName + File.separator + cacheName);
        if (!cacheBucket.exists() && !cacheBucket.mkdirs()) {
            Log.debug(ServiceConstants.LOG_TAG, TAG, "Cannot create cache bucket.");
            return null;
        }

        return cacheBucket;
    }

    /**
     * Retrieves a cache file for {@code key} from {@code cacheName} directory.
     *
     * @param cacheName the cache sub-folder within {@code cacheRoot} where cache file stored for
     *     key should be fetched from
     * @param key the key for which the cache file should be retrieved
     * @return the cache file for {@code key} if present; null otherwise.
     */
    File getCacheFile(@NonNull final String cacheName, @NonNull final String key) {
        final String entryLocation = getCacheLocation(cacheName, key);
        if (entryLocation == null) return null;

        final File entryHandle = new File(entryLocation);
        if (entryHandle.exists()) {
            return entryHandle;
        } else {
            return null;
        }
    }

    /**
     * Retrieves a cache metadata file for {@code key} from {@code cacheName} directory.
     *
     * @param cacheName the cache sub-folder within {@code cacheRoot} where the metadata for cache
     *     file stored for key should be fetched from
     * @param key the key for which the cache file metadata should be retrieved
     * @return the cache metadata for cache file stored for {@code key} if present; null otherwise.
     */
    Map<String, String> getCacheMetadata(
            @NonNull final String cacheName, @NonNull final String key) {
        if (!canProcess(cacheName, key)) {
            return null;
        }

        final String metaDataLocation = getMetadataLocation(cacheName, key);
        if (metaDataLocation == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Metadata location for" + "cache name: [%s], cache key [%s] is null.",
                    cacheName,
                    key);
            return null;
        }

        final String metadataJson = FileUtils.readAsString(new File(metaDataLocation));
        if (metadataJson == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Metadata stored for" + "cache name: [%s], cache key [%s] is null.",
                    cacheName,
                    key);
            return null;
        }

        try {
            final Map<String, String> metadata = new HashMap<>();
            final JSONObject contentJson = new JSONObject(new JSONTokener(metadataJson));
            Iterator<String> jsonKeys = contentJson.keys();
            while (jsonKeys.hasNext()) {
                final String jsonKey = jsonKeys.next();
                final String jsonValue = contentJson.optString(jsonKey);
                metadata.put(jsonKey, jsonValue);
            }
            return metadata;
        } catch (final JSONException e) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Cannot create cache metadata for"
                            + "cache name: [%s], cache key: [%s] due to %s",
                    cacheName,
                    key,
                    e.getMessage());
            return null;
        }
    }

    /**
     * Creates a cache file and a cache metadata file based on {@code cacheEntry} to be retrievable
     * via {@code key}. These files are stored inside {@code cacheRoot/cacheName} dir.
     *
     * @param cacheName the cache sub-folder within {@code cacheRoot} where the cache file and
     *     metadata are stored
     * @param key the key for which the cacheEntry should be linked to.
     * @param cacheEntry the cache entry based on which the cache file should be created
     * @return true if the cache file and metadata file are created successfully; false otherwise.
     */
    boolean createCacheFile(
            @NonNull final String cacheName,
            @NonNull final String key,
            @NonNull final CacheEntry cacheEntry) {
        if (!canProcess(cacheName, key)) {
            return false;
        }

        // Attempt to save the cache file.
        final String entryLocation = getCacheLocation(cacheName, key);

        if (entryLocation == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Entry location for " + "cache name: [%s], cache key [%s] is null.",
                    cacheName,
                    key);
            return false;
        }

        final boolean saved =
                FileUtils.readInputStreamIntoFile(
                        new File(entryLocation), cacheEntry.getData(), false);

        if (!saved) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Failed to save cache file for " + "cache name: [%s], cache key [%s].",
                    cacheName,
                    key);
            return false;
        }

        // Attempt to save metadata
        final String metaDataLocation = getMetadataLocation(cacheName, key);
        final boolean metadataSaved =
                createCacheMetadataFile(cacheEntry, entryLocation, metaDataLocation);

        if (!metadataSaved) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Failed to save metadata for" + "cache name: [%s], cache key [%s].",
                    cacheName,
                    key);
            // Metadata save failed. Delete the cache file previously saved.
            final File savedCacheFile = new File(entryLocation);
            FileUtils.deleteFile(savedCacheFile, true);
            return false;
        }
        return true;
    }

    /**
     * Creates a metadata file based on {@code cacheEntry} to be retrievable via {@code key}.
     *
     * @param cacheEntry the cache entry based on which the metadata file should be created
     * @param metadataFilePath the location where the metadata file should be placed
     * @return true if the cache file and metadata file are created successfully; false otherwise.
     */
    private boolean createCacheMetadataFile(
            final CacheEntry cacheEntry,
            final String entryLocation,
            final String metadataFilePath) {
        if (cacheEntry == null || StringUtils.isNullOrEmpty(metadataFilePath)) {
            return false;
        }

        final Map<String, String> metadata = new HashMap<>();
        metadata.put(FileCacheResult.METADATA_KEY_PATH_TO_FILE, entryLocation);

        final Date expiry = cacheEntry.getExpiry().getExpiration();
        if (expiry != null) {
            metadata.put(
                    FileCacheResult.METADATA_KEY_EXPIRY_IN_MILLIS,
                    String.valueOf(expiry.getTime()));
        }

        if (cacheEntry.getMetadata() != null) {
            metadata.putAll(cacheEntry.getMetadata());
        }

        try {
            final JSONObject metadataJson = new JSONObject(metadata);
            final InputStream metadataStream =
                    new ByteArrayInputStream(
                            metadataJson.toString().getBytes(StandardCharsets.UTF_8));
            return FileUtils.readInputStreamIntoFile(
                    new File(metadataFilePath), metadataStream, false);
        } catch (final Exception exception) {
            Log.debug(ServiceConstants.LOG_TAG, TAG, "Cannot create cache metadata %s", exception);
            return false;
        }
    }

    /**
     * Deletes the cache file and a cache metadata file associated with {@code key}.
     *
     * @param cacheName the cache sub-folder from which files associated with the key need to be
     *     deleted
     * @param key the key for which the cache file and cache metadata file should be deleted
     * @return true if the cache file and metadata file were deleted successfully; false otherwise.
     */
    boolean deleteCacheFile(final String cacheName, final String key) {
        if (!canProcess(cacheName, key)) {
            return false;
        }

        final File cacheFileToDelete = getCacheFile(cacheName, key);

        if (cacheFileToDelete == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG, TAG, "Cannot delete cache file. No file to delete.");
            return true;
        }

        if (!FileUtils.deleteFile(cacheFileToDelete, true)) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Failed to delete cache file for " + "cache name [%s], key: [%s]",
                    cacheName,
                    key);
            return false;
        }

        final String cacheFileMetadataPath = getMetadataLocation(cacheName, key);
        if (cacheFileMetadataPath != null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Failed to delete cache metadata file for " + "cache name [%s], key: [%s]",
                    cacheName,
                    key);
            FileUtils.deleteFile(new File(cacheFileMetadataPath), true);
        }

        return true;
    }

    @VisibleForTesting
    String getCacheLocation(final String cacheName, final String key) {
        if (!canProcess(cacheName, key)) {
            return null;
        }

        final String hash = StringEncoder.sha2hash(key);
        return (ServiceProvider.getInstance()
                        .getDeviceInfoService()
                        .getApplicationCacheDir()
                        .getPath()
                + File.separator
                + rootCacheDirName
                + File.separator
                + cacheName
                + File.separator
                + hash);
    }

    @VisibleForTesting
    String getMetadataLocation(final String cacheName, final String key) {
        if (!canProcess(cacheName, key)) {
            return null;
        }

        return getCacheLocation(cacheName, key) + CACHE_METADATA_FILE_EXT;
    }

    private boolean canProcess(final String cacheName, final String key) {
        return !StringUtils.isNullOrEmpty(cacheName) && !StringUtils.isNullOrEmpty(key);
    }
}
