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
import androidx.annotation.Nullable;

import com.adobe.marketing.mobile.services.caching.CacheExpiry;
import com.adobe.marketing.mobile.services.caching.CacheResult;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * A holder for the downloaded data and result returned by {@code RulesDownloader}
 */
public class RulesDownloadResult {
    private final String data;
    private final Reason reason;

    /**
     * Represents the reason why the downloaded data is returned as such
     */
    public enum Reason {
        /**
         * The requested download source is invalid. Example: Invalid url, non-existent asset
         */
        INVALID_SOURCE,

        /**
         * Extraction of the rules zip file failed.
         */
        ZIP_EXTRACTION_FAILED,

        /**
         * Content cannot be written into the cache directoy used by {@code RulesDownloader}
         */
        CANNOT_CREATE_TEMP_DIR,

        /**
         * Content cannot be written into a temp directory used by {@code RulesDownloader}
         */
        CANNOT_STORE_IN_TEMP_DIR,

        /**
         * Requested content has not been modified at source.
         */
        NOT_MODIFIED,

        /**
         * The requested rules were not found.
         */
        NO_DATA,

        SUCCESS
    }

    public RulesDownloadResult(@Nullable final String data,
                               @NonNull final Reason reason) {
        this.data = data;
        this.reason = reason;
    }

    @Nullable
    public String getData() {
        return data;
    }

    @NonNull
    public Reason getReason() {
        return reason;
    }
}