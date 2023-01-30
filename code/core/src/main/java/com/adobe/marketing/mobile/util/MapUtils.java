/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util;

import androidx.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/** Utility class for Maps */
public final class MapUtils {

    private MapUtils() {}

    /**
     * Checks if the provided {@code map} is null or it has no element
     *
     * @param map the {@code map} to be verified
     * @return true if null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(@Nullable final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Adds {@code key}/{@code value} to {@code map} if {@code value} is not null or an empty
     * string, map or collection.
     *
     * @param map collection to put {@code values} mapped to {@code key} if {@code values} is
     *     non-null and contains at least one entry
     * @param key key used to map {@code value} in {@code map}
     * @param value an object to add to {@code map} if not null or empty
     */
    public static void putIfNotEmpty(
            @Nullable final Map<String, Object> map,
            @Nullable final String key,
            @Nullable final Object value) {

        if (map == null || key == null || value == null) {
            return;
        }

        boolean isValueEmpty = false;
        if (value instanceof String) {
            isValueEmpty = ((String) value).isEmpty();
        } else if (value instanceof Map<?, ?>) {
            isValueEmpty = ((Map<?, ?>) value).isEmpty();
        } else if (value instanceof Collection<?>) {
            isValueEmpty = ((Collection<?>) value).isEmpty();
        }

        if (!isValueEmpty) {
            map.put(key, value);
        }
    }
}
