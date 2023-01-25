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

import com.adobe.marketing.mobile.internal.CoreConstants;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.StringUtils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Utility class for {@code String} related encoding methods */
public final class StringEncoder {

    private static final String LOG_TAG = "StringEncoder";
    private static final char[] hexArray = "0123456789abcdef".toCharArray();
    private static final int ALL_BITS_ENABLED = 0xFF;
    private static final int SHIFT_BY = 4;
    private static final int HEX_CHAR_MULTIPLIER = 0x0F;
    private static final int HEX_RADIX = 16;
    private static final int PRIME = 0x1000193; // 16777619 as hex
    private static final int OFFSET = 0x811c9dc5; // 2166136261 as hex
    private static final int LSB_8_MASK = 0xFF;
    private static final char[] BYTE_TO_HEX =
            ("000102030405060708090A0B0C0D0E0F"
                            + "101112131415161718191A1B1C1D1E1F"
                            + "202122232425262728292A2B2C2D2E2F"
                            + "303132333435363738393A3B3C3D3E3F"
                            + "404142434445464748494A4B4C4D4E4F"
                            + "505152535455565758595A5B5C5D5E5F"
                            + "606162636465666768696A6B6C6D6E6F"
                            + "707172737475767778797A7B7C7D7E7F"
                            + "808182838485868788898A8B8C8D8E8F"
                            + "909192939495969798999A9B9C9D9E9F"
                            + "A0A1A2A3A4A5A6A7A8A9AAABACADAEAF"
                            + "B0B1B2B3B4B5B6B7B8B9BABBBCBDBEBF"
                            + "C0C1C2C3C4C5C6C7C8C9CACBCCCDCECF"
                            + "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF"
                            + "E0E1E2E3E4E5E6E7E8E9EAEBECEDEEEF"
                            + "F0F1F2F3F4F5F6F7F8F9FAFBFCFDFEFF")
                    .toCharArray();

    private static final int OXFF = 0xFF;

    private StringEncoder() {}

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
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Error while attempting to encode a string (%s)" + ex);
        }

        return hash;
    }

    /**
     * Converts the given {@code String} into a decimal representation of the signed 2's complement
     * FNV1a 32-bit hash.
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
     *
     * <p>https://en.wikipedia.org/wiki/Fowler%E2%80%93Noll%E2%80%93Vo_hash_function Online
     * validator - https://md5calc.com/hash/fnv1a32?str=
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

    /**
     * Converts a string to hexadecimal notation
     *
     * @param originalString the string for which hexadecimal value is to be computed
     * @return hexadecimal representation of the string if valid, null otherwise
     */
    public static String getHexString(final String originalString) {
        if (StringUtils.isNullOrEmpty(originalString)) {
            return null;
        }

        byte[] bytes = originalString.getBytes(StandardCharsets.UTF_8);

        final int bytesLength = bytes.length;
        final char[] chars = new char[bytesLength << 1];
        int hexIndex;
        int index = 0;
        int offset = 0;

        while (offset < bytesLength) {
            hexIndex = (bytes[offset++] & OXFF) << 1;
            chars[index++] = BYTE_TO_HEX[hexIndex++];
            chars[index++] = BYTE_TO_HEX[hexIndex];
        }

        return new String(chars);
    }

    /**
     * Decodes a hexadecimal string
     *
     * @param hexString the hexadecimal string to be decoded
     * @return decoded hexadecimal string if valid, null otherwise
     */
    public static String hexToString(final String hexString) {
        if (hexString == null || hexString.length() <= 0 || hexString.length() % 2 != 0) {
            return null;
        }

        final int length = hexString.length();
        byte[] data = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            final int radix = 16;
            final int fourDigit = 4;
            data[i / 2] =
                    (byte)
                            ((Character.digit(hexString.charAt(i), radix) << fourDigit)
                                    + Character.digit(hexString.charAt(i + 1), radix));
        }

        String decodedString = null;

        decodedString = new String(data, StandardCharsets.UTF_8);

        return decodedString;
    }

    /**
     * Computes the sha2 hash for the string
     *
     * @param input the string for which sha2 hash is to be computed
     * @return sha2 hash result if the string is valid, null otherwise
     */
    public static String sha2hash(final String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        try {
            final MessageDigest messagedigest = MessageDigest.getInstance("SHA-256");
            messagedigest.update(input.getBytes(StandardCharsets.UTF_8));
            final byte[] messageDigest = messagedigest.digest();
            final StringBuilder sha2HexBuilder = new StringBuilder();

            for (byte aMessageDigest : messageDigest) {
                StringBuilder hexString =
                        new StringBuilder(Integer.toHexString(LSB_8_MASK & aMessageDigest));

                while (hexString.length() < 2) {
                    hexString.insert(0, "0");
                }

                sha2HexBuilder.append(hexString);
            }

            return sha2HexBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Failed to create sha2 hash " + e);
        }

        return null;
    }
}
