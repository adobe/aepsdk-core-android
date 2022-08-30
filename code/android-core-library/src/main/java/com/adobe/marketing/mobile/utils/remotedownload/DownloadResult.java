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
package com.adobe.marketing.mobile.utils.remotedownload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

/**
 * A holder for the downloaded data returned by the network request via {@code RemoteDownloader}
 */
public class DownloadResult {
    private final File data;
    private final Reason reason;

    /**
     * Represents the reason why the downloaded data is returned as such
     */
    public enum Reason {
        INVALID_URL,
        RESPONSE_PROCESSING_FAILED,
        CANNOT_WRITE_TO_CACHE_DIR,
        NOT_MODIFIED,
        NO_DATA,
        SUCCESS
    }

    public DownloadResult(@Nullable final File file,
                          @NonNull final Reason reason) {
        this.data = file;
        this.reason = reason;
    }

    public File getData() {
        return data;
    }

    public Reason getReason() {
        return reason;
    }
}