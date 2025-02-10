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

package com.adobe.marketing.mobile.internal.util;

/** Utility class for {@code String} related encoding methods */
public final class StringEncoder {

    private StringEncoder() {}

    /**
     * Computes the sha2 hash for the string.
     *
     * <p>Some extensions are incorrectly using this internal utility. To address this, the
     * functionality has been moved to a public utility class, and this internal class will be
     * removed in version 4.x.
     *
     * @param input the string for which sha2 hash is to be computed
     * @return sha2 hash result if the string is valid, null otherwise
     * @deprecated This method will be removed in version 4.x.
     */
    public static String sha2hash(final String input) {
        return com.adobe.marketing.mobile.util.StringEncoder.sha2hash(input);
    }
}
