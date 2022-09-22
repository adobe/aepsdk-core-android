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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

import com.adobe.marketing.mobile.TestHelper;
import com.adobe.marketing.mobile.internal.util.StringEncoder;
import com.adobe.marketing.mobile.util.StreamUtils;
import com.adobe.marketing.mobile.util.StringUtils;
import com.adobe.marketing.mobile.util.UrlUtils;

public class UrlUtilsTests {

    @Test
    public void testClassIsWellDefined() {
        try {
            TestHelper.assertUtilityClassWellDefined(UrlUtils.class);
        } catch (Exception e) {
            fail("StringUtils class is not well defined, throwing exception " + e);
        }
    }

    @Test
    public void testStringIsUrl_when_validUrl() {
        assertTrue(UrlUtils.stringIsUrl("http://is.valid.url/path?key=value&abc=def"));
    }

    @Test
    public void testStringIsUrl_when_invalidUrl() {
        assertFalse(UrlUtils.stringIsUrl("wrong.url"));
    }

    @Test
    public void testStringIsUrl_when_emptyUrl() {
        assertFalse(UrlUtils.stringIsUrl(""));
    }

    @Test
    public void testStringIsUrl_when_nullUrl() {
        assertFalse(UrlUtils.stringIsUrl(null));
    }
}

