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
import java.io.InputStream;
import java.util.Map;

/** Represents an item that is retrieved from cache using {@code CacheService} */
public interface CacheResult {
    /**
     * Gets the content of the item that is retrieved from the cache.
     *
     * @return the content of the item that was cached
     */
    @Nullable InputStream getData();

    /**
     * Gets the expiry of the item that is retrieved from the cache.
     *
     * @return expiry of the item that was cached
     */
    @NonNull CacheExpiry getExpiry();

    /**
     * Gets the metadata of the item that is retrieved from the cache. Note that this metadata may
     * also contain additional keys used internally by {@code CacheService} to facilitate caching.
     *
     * @return metadata of the item provided when it was cached.
     */
    @Nullable Map<String, String> getMetadata();
}
