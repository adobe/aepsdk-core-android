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

package com.adobe.marketing.mobile.services

import java.io.File

/**
 * Represents a contract that allows creating, retrieving, deleting cache files.
 * Additionally provides some constants for various metadata keys that can be used for
 * creating and tracking cache files.
 */
interface CacheFileService {
    companion object {
        const val METADATA_KEY_LAST_MODIFIED_EPOCH = "last-modified-epoch"
        const val METADATA_KEY_ETAG = "etag"
    }

    /**
     * Responsible for creating a new cache file in the specified sub directory inside the default
     * application cache directory.
     *
     * @param key the handle for tracking the cache file. If this is null file will not be created
     * @param metadata the metadata associated with the file, that will be used to name the cache file
     * @param cacheDir optional sub directory within the application cache directory where the  file needs to be created
     *
     * @return a valid [File] representing the new cache file created, if successful; null if creation fails.
     *
     */
    fun createCacheFile(key: String, metadata: Map<String, String>, cacheDir: String?): File?

    /**
     * Returns the file corresponding to the provided key (within the [cacheDir] if provided).
     *
     * @param key The handle originally used to create the cache file which is being searched for
     * @param cacheDir Optional directory name where the file cached is to be found
     * @param ignorePartial If true, then the cached file, if found, will be returned even if the file is
     *                      not marked complete
     * @return The cached file if one is found, null otherwise.
     */
    fun getCacheFile(key: String, cacheDir: String?, ignorePartial: Boolean): File?

    /**
     * Deletes the file cached for the handle given. This will delete the file even if it has not been marked complete.
     *
     * @param key The handle to use for retrieving the cached file
     * @param cacheDir Optional directory name where the file was cached.
     * @return true - if the file was found and deleted;
     *         false otherwise.
     */
    fun deleteCacheFile(key: String, cacheDir: String?): Boolean

    /**
     * Marks the file provided as complete, regarding it as fully processed and not partial anymore.
     * @param cacheFile the file to be marked complete
     *
     * @return the completed cache file if marking complete was successful, null otherwise.
     */
    fun markComplete(cacheFile: File?): File?

    /**
     * Retrieves the value of [metadataKey] from the [cacheFilePath] provided
     *
     * @param metadataKey the key to fetch from the cache file path name
     * @param cacheFilePath the file path of the cache file (as returned by [createCacheFile])
     *
     * @return value of the requested metadata key if it exists, null otherwise.
     */
    fun getMetadata(metadataKey: String, cacheFilePath: String?): String?

    /**
     *
     */
    fun getBaseFilePath(key: String, cacheDir: String?): String?
}
