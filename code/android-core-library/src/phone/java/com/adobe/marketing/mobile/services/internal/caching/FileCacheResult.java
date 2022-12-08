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
import com.adobe.marketing.mobile.services.caching.CacheExpiry;
import com.adobe.marketing.mobile.services.caching.CacheResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

/** Represents a result returned by {@link FileCacheService} */
class FileCacheResult implements CacheResult {

    private final File fileContent;
    private final CacheExpiry cacheExpiry;
    private final Map<String, String> metadata;
    static final String METADATA_KEY_EXPIRY_IN_MILLIS = "expiryInMillis";
    static final String METADATA_KEY_PATH_TO_FILE = "pathToFile";

    public FileCacheResult(
            @NonNull final File data,
            @NonNull final CacheExpiry cacheExpiry,
            @Nullable final Map<String, String> metadata) {
        this.fileContent = data;
        this.cacheExpiry = cacheExpiry;
        this.metadata = metadata;
    }

    @Override
    public InputStream getData() {
        try {
            return new FileInputStream(fileContent);
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull @Override
    public CacheExpiry getExpiry() {
        return cacheExpiry;
    }

    @Nullable @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }
}
