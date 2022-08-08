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

package com.adobe.marketing.mobile.internal.utility;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

public class UrlUtilitiesTest {

    @Test
    public void urlEncodeWithNoEncodedNeeded() {
        assertEquals(UrlUtilities.urlEncode("thisisateststring"), "thisisateststring");
    }

    @Test
    public void urlEncodeWithSpaces() {
        assertEquals(UrlUtilities.urlEncode("this is a test string"), "this%20is%20a%20test%20string");
    }

    @Test
    public void urlEncodeStartsWithSpace() {
        assertEquals(UrlUtilities.urlEncode(" afterspace"), "%20afterspace");
    }

    @Test
    public void urlEncodeOnlyUnicode() {
        assertEquals(UrlUtilities.urlEncode("网"), "%E7%BD%91");
    }

    @Test
    public void urlEncodeStartsWithUnicode() {
        assertEquals(UrlUtilities.urlEncode("网test"), "%E7%BD%91test");
    }

    @Test
    public void urlEncodeEndsWithUnicode() {
        assertEquals(UrlUtilities.urlEncode("test网"), "test%E7%BD%91");
    }

    @Test
    public void urlEncodeBlankString() {
        assertEquals(UrlUtilities.urlEncode(""), "");
    }

    @Test
    public void urlEncodeDeathString() {
        assertEquals(UrlUtilities.urlEncode("~!@#$%^&*()-+=|}{][\\/.<,>"),
                "~%21%40%23%24%25%5E%26%2A%28%29-%2B%3D%7C%7D%7B%5D%5B%5C%2F.%3C%2C%3E");
    }

    @Test
    public void testURLEncodeNull() {
        Assert.assertNull(UrlUtilities.urlEncode(null));
    }
    
}
