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

package com.adobe.marketing.mobile.util;

public final class StringUtils {

    private StringUtils() {}

    /**
     * Checks if a {@code String} is null, empty or it only contains whitespaces.
     *
     * <p>A {@code null} reference is treated the same as an empty or whitespace-only string.
     *
     * @param str the {@link String} that we want to check
     * @return {@code boolean} with the evaluation result
     */
    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Checks if a {@code String} is non-null and contains at least one non-whitespace
     * character. This is the inverse of {@link #isNullOrEmpty(String)}.
     *
     * @param str the {@link String} that we want to check
     * @return {@code boolean} with the evaluation result
     */
    static boolean isNotNullOrEmpty(final String str) {
        return !isNullOrEmpty(str);
    }
}
