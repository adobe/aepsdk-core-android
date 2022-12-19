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

import static org.junit.Assert.*;

import java.util.Map;
import org.junit.Test;

public class UrlUtilsTests {

    @Test
    public void testIsValidUrl_when_validUrl() {
        assertTrue(UrlUtils.isValidUrl("http://is.valid.url/path?key=value&abc=def"));
    }

    @Test
    public void testIsValidUrl_when_invalidUrl() {
        assertFalse(UrlUtils.isValidUrl("wrong.url"));
    }

    @Test
    public void testIsValidUrl_when_emptyUrl() {
        assertFalse(UrlUtils.isValidUrl(""));
    }

    @Test
    public void testIsValidUrl_when_nullUrl() {
        assertFalse(UrlUtils.isValidUrl(null));
    }

    @Test
    public void test_extractQueryParameters_happy() {
        Map<String, String> map =
                UrlUtils.extractQueryParameters("abc://123.com/query?k1=v1&k2=v2");
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));
    }

    @Test
    public void test_extractQueryParameters_invalidUri() {
        Map<String, String> map = UrlUtils.extractQueryParameters("abc:query?k1=v1&k2=v2");
        assertNull(map);
    }

    @Test
    public void test_extractQueryParameters_emptyParameter1() {
        Map<String, String> map = UrlUtils.extractQueryParameters("abc://123.com/query?");
        assertNotNull(map);
        assertEquals(0, map.size());
    }

    @Test
    public void test_extractQueryParameters_emptyParameter2() {
        Map<String, String> map = UrlUtils.extractQueryParameters("abc://123.com/query");
        assertNotNull(map);
        assertEquals(0, map.size());
    }

    @Test
    public void test_extractQueryParameters_emptyValue() {
        Map<String, String> map = UrlUtils.extractQueryParameters("abc://123.com/query?k1=v1&k2=");
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("v1", map.get("k1"));
    }

    @Test
    public void test_extractQueryParameters_emptyKey() {
        Map<String, String> map = UrlUtils.extractQueryParameters("abc://123.com/query?k1=v1&=v2");
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("v1", map.get("k1"));
    }

    @Test
    public void test_extractQueryParameters_keyWithSpecialCharacter() {
        Map<String, String> map =
                UrlUtils.extractQueryParameters("abc://123.com/query?k1.k11=v1&=v2");
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("v1", map.get("k1.k11"));
    }
}
