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

import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 * Utility class for {@code String} related encoding methods
 */
final public class StringEncoder {

    private static final String LOG_TAG = "StringEncoder";
    private static final char[] hexArray = "0123456789abcdef".toCharArray();
    private static final int ALL_BITS_ENABLED = 0xFF;
    private static final int SHIFT_BY = 4;
    private static final int HEX_CHAR_MULTIPLIER = 0x0F;
    private static final int HEX_RADIX = 16;
    private static final int PRIME = 0x1000193; // 16777619 as hex
    private static final int OFFSET = 0x811c9dc5; // 2166136261 as hex

    private StringEncoder() {
    }

    public static String getSha1HashedString(final String inputString) {
        if (inputString == null || inputString.isEmpty()) {
            return null;
        }

        String hash = null;

        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = inputString.getBytes(StandardCharsets.UTF_8);
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            char[] hexChars = new char[bytes.length * 2];

            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & ALL_BITS_ENABLED;
                hexChars[j * 2] = hexArray[v >>> SHIFT_BY];
                hexChars[j * 2 + 1] = hexArray[v & HEX_CHAR_MULTIPLIER];
            }

            hash = new String(hexChars);
        } catch (final NoSuchAlgorithmException ex) {
            MobileCore.log(LoggingMode.DEBUG, LOG_TAG, "ADBMobile - error while attempting to encode a string (%s)" + ex);
        }

        return hash;
    }

    /**
     * Converts the given {@code String} into a decimal representation of the signed 2's complement FNV1a 32-bit hash.
     *
     * @param inputString {@code String} containing to be hashed
     * @return a {@link long} containing the decimal FNV1a 32-bit hash
     */
    public static long convertStringToDecimalHash(final String inputString) {
        final int hash = getFnv1aHash(inputString);
        // convert signed 2's complement hash to hex string
        final String hexHash = Integer.toHexString(hash);

        return Long.parseLong(hexHash, HEX_RADIX);
    }

    /**
     * Converts the given {@code String} to a signed 2's complement FNV1a 32-bit hash.
     * <p>
     * https://en.wikipedia.org/wiki/Fowler%E2%80%93Noll%E2%80%93Vo_hash_function
     * Online validator - https://md5calc.com/hash/fnv1a32?str=
     *
     * @param input {@code String} to be hashed
     * @return a {@code int} containing the signed 2's complement FNV1a 32-bit hash
     */
    private static int getFnv1aHash(final String input) {
        int hash = (input == null || input.trim().isEmpty()) ? 0 : OFFSET;
        final byte[] bytes = input.getBytes();

        for (byte aByte : bytes) {
            hash ^= (aByte & ALL_BITS_ENABLED);
            hash *= PRIME;
        }

        return hash;
    }
}