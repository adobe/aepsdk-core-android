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

import com.adobe.marketing.mobile.internal.util.UrlEncoder.urlEncode
import org.junit.Assert
import org.junit.Test

class UrlEncoderTests {
    @Test
    fun urlEncodeWithNoEncodedNeeded() {
        Assert.assertEquals(urlEncode("thisisateststring"), "thisisateststring")
    }

    @Test
    fun urlEncodeWithSpaces() {
        Assert.assertEquals(urlEncode("this is a test string"), "this%20is%20a%20test%20string")
    }

    @Test
    fun urlEncodeStartsWithSpace() {
        Assert.assertEquals(urlEncode(" afterspace"), "%20afterspace")
    }

    @Test
    fun urlEncodeOnlyUnicode() {
        Assert.assertEquals(urlEncode("网"), "%E7%BD%91")
    }

    @Test
    fun urlEncodeStartsWithUnicode() {
        Assert.assertEquals(urlEncode("网test"), "%E7%BD%91test")
    }

    @Test
    fun urlEncodeEndsWithUnicode() {
        Assert.assertEquals(urlEncode("test网"), "test%E7%BD%91")
    }

    @Test
    fun urlEncodeBlankString() {
        Assert.assertEquals(urlEncode(""), "")
    }

    @Test
    fun urlEncodeDeathString() {
        Assert.assertEquals(
            urlEncode("~!@#$%^&*()-+=|}{][\\/.<,>"),
            "~%21%40%23%24%25%5E%26%2A%28%29-%2B%3D%7C%7D%7B%5D%5B%5C%2F.%3C%2C%3E"
        )
    }

    @Test
    fun testURLEncodeNull() {
        Assert.assertNull(urlEncode(null))
    }
}
