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
import java.util.HashMap;
import java.util.Map;

/** Represents an item that can be cached using {@link CacheService} */
public class CacheEntry {

    /** The input stream which provides the data to be cached */
    private final InputStream data;

    /** The expiration for this item */
    private final CacheExpiry expiry;

    /** The metadata associated with this cache entry */
    private final Map<String, String> metadata;

    public CacheEntry(
            @NonNull final InputStream data,
            @NonNull final CacheExpiry expiry,
            @Nullable final Map<String, String> metadata) {
        this.data = data;
        this.expiry = expiry;
        this.metadata = metadata == null ? new HashMap<>() : new HashMap<>(metadata);
    }

    @NonNull public InputStream getData() {
        return data;
    }

    @NonNull public CacheExpiry getExpiry() {
        return expiry;
    }

    @Nullable public Map<String, String> getMetadata() {
        return metadata;
    }
}
