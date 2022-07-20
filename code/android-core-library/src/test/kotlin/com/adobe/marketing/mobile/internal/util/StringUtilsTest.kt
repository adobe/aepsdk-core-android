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
package com.adobe.marketing.mobile.internal.util

import java.io.ByteArrayInputStream
import java.io.InputStream
import org.junit.Assert
import org.junit.Test

class StringUtilsTest {

    @Test
    fun testClassIsWellDefined() {
        try {
            TestHelper.assertUtilityClassWellDefined(StringUtils::class.java)
        } catch (e: Exception) {
            Assert.fail("StringUtils class is not well defined, throwing exception $e")
        }
    }

    // ==============================================================================================================
    // static String getSha1HashedString(final String inputString)
    // ==============================================================================================================
    @Test
    @Throws(Exception::class)
    fun testGetSha1HashedString_when_inputStringIsNull_then_returnNull() {
        // setup
        val inputString: String? = null

        // test
        val result = StringEncoder.getSha1HashedString(inputString)

        // verify
        Assert.assertNull("result should be null when input is null", result)
    }

    @Test
    @Throws(Exception::class)
    fun testGetSha1HashedString_when_inputStringIsEmpty_then_returnNull() {
        // setup
        val inputString = ""

        // test
        val result = StringEncoder.getSha1HashedString(inputString)

        // verify
        Assert.assertNull("result should be null when input is null", result)
    }

    @Test
    @Throws(Exception::class)
    fun testGetSha1HashedString_when_inputStringIsValid_then_returnCorrectSha1HashedValue() {
        // setup
        val inputString = "BIGSOFTHANDS"

        // test
        val result = StringEncoder.getSha1HashedString(inputString)

        // verify
        // results tested against http://www.sha1hash.com/
        Assert.assertEquals(
            "result should have correct sha1 hash result",
            "185fdf9fa24b073d1b3bfc724c6dd77825130607",
            result
        )
    }

    @Test
    fun testStringIsUrl_when_validUrl() {
        Assert.assertTrue(StringUtils.stringIsUrl("http://is.valid.url/path?key=value&abc=def"))
    }

    @Test
    fun testStringIsUrl_when_invalidUrl() {
        Assert.assertFalse(StringUtils.stringIsUrl("wrong.url"))
    }

    @Test
    fun testStringIsUrl_when_emptyUrl() {
        Assert.assertFalse(StringUtils.stringIsUrl(""))
    }

    @Test
    fun testStringIsUrl_when_nullUrl() {
        Assert.assertFalse(StringUtils.stringIsUrl(null))
    }

    @Test
    fun testIsNullOrEmpty_when_nullInputString() {
        Assert.assertTrue(StringUtils.isNullOrEmpty(null))
    }

    @Test
    fun testIsNullOrEmpty_when_emptyInputString() {
        Assert.assertTrue(StringUtils.isNullOrEmpty(""))
    }

    @Test
    fun testIsNullOrEmpty_when_validInputString() {
        Assert.assertFalse(StringUtils.isNullOrEmpty("non empty string"))
    }

    @Test
    fun testIsNullOrEmpty_when_whitespacesInputString() {
        Assert.assertTrue(StringUtils.isNullOrEmpty("        "))
    }

    @Test
    fun testStreamToString_when_nullInputStream() {
        Assert.assertNull(StringUtils.streamToString(null))
    }

    @Test
    @Throws(Exception::class)
    fun testStreamToString_when_validInputStream() {
        val stream: InputStream =
            ByteArrayInputStream("myTestExample".toByteArray(charset("UTF-8")))
        Assert.assertEquals("myTestExample", StringUtils.streamToString(stream))
    }
}
