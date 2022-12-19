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

package com.adobe.marketing.mobile.services.caching;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Represents a component that facilitates caching. */
public interface CacheService {
    /**
     * Creates or updates the key-value pair in the cache.
     *
     * @param cacheName name of the cache where the cache entry is to be created
     * @param key key for the item to be cached
     * @param value the value that is to be associated with {@code key}
     */
    boolean set(
            @NonNull final String cacheName,
            @NonNull final String key,
            @NonNull final CacheEntry value);

    /**
     * Retrieves the item associated with the key from the cache.
     *
     * @param cacheName name of the cache where the key should be queried from
     * @param key key for the cache entry
     * @return a valid cache entry for the key if one exists; null otherwise.
     */
    @Nullable CacheResult get(@NonNull final String cacheName, @NonNull final String key);

    /**
     * Removes the item associated with the key from the cache.
     *
     * @param cacheName name of the cache where the item is to be removed from
     * @param key the key for the item that is to be removed
     * @return true if the item has been removed; false otherwise.
     */
    boolean remove(@NonNull final String cacheName, @NonNull final String key);
}
