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
import java.util.Map;

/**
 * Represents a component that provides metadata about file content.
 */
public interface MetadataProvider {

    String HTTP_HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
    String HTTP_HEADER_IF_RANGE = "If-Range";
    String HTTP_HEADER_RANGE = "Range";
    String HTTP_HEADER_LAST_MODIFIED = "Last-Modified";
    String ETAG = "ETag";

    /**
     * Retrieves metadata for the [file] provided for the purpose of
     * conditionally fetching content from the remote url.
     *
     * @param file the [File] for which metadata is needed
     * @return the metadata of the [file] if it is valid;
     *         empty map if no metadata is needed,
     *         null if metadata cannot be computed
     */
    @Nullable
    Map<String, String> getMetadata(@NonNull File file);
}
