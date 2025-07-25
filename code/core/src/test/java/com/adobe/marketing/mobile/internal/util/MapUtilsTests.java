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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class MapUtilsTests {

    // FNV1a 32-bit hash tests
    // basic smoke tests for comparison with iOS
    @Test
    public void testGetFnv1aHash_String_Smoke() {
        // setup
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("key", "value");
                    }
                };

        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, null);
        // verify flattened map string "key:value"
        final long expectedHash = 4007910315L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_Integer_Smoke() {
        // setup
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("key", 552);
                    }
                };
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, null);
        // verify flattened map string "key:552"
        final long expectedHash = 874166902L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_Boolean_Smoke() {
        // setup
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("key", false);
                    }
                };
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, null);
        // verify flattened map string "key:false"
        final long expectedHash = 138493769L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_AsciiSorted_Smoke() {
        // setup
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("key", "value");
                        put("number", 1234);
                        put("UpperCase", "abc");
                        put("_underscore", "score");
                    }
                };
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, null);
        // verify flattened map string "UpperCase:abc_underscore:scorekey:valuenumber:1234"
        final long expectedHash = 960895195L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_NoMask_Happy() {
        // setup
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("aaa", "1");
                        put("zzz", true);
                    }
                };
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, null);
        // verify flattened map string "aaa:1zzz:true"
        final long expectedHash = 3251025831L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_WithMask_Happy() {
        // setup
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("aaa", "1");
                        put("c", 2);
                        put("m", 1.11);
                        put("zzz", true);
                    }
                };
        final String[] mask = new String[] {"c", "m"};
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, mask);
        // verify flattened map string "c:2m:1.11"
        final long expectedHash = 2718815288L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_ArrayOfMaps() {
        // setup
        final Map<String, Object> map1 =
                new HashMap<String, Object>() {
                    {
                        put("aaa", "1");
                        put("zzz", true);
                    }
                };
        final Map<String, Object> map2 =
                new HashMap<String, Object>() {
                    {
                        put("number", 123);
                        put("double", 1.5);
                    }
                };
        final List<Map<String, Object>> list = new ArrayList<>();
        list.add(map1);
        list.add(map2);
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("key", list);
                    }
                };
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, null);
        // verify flattened map string "key.0.aaa:1key.0.zzz:truekey.1.double:1.5key.1.number:123"
        final long expectedHash = 2410759527L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_ArrayOfLists() {
        // setup
        final List<Object> innerList =
                new ArrayList<Object>() {
                    {
                        add("aaa");
                        add("zzz");
                        add(111);
                    }
                };
        final List<Object> innerList2 =
                new ArrayList<Object>() {
                    {
                        add("2");
                    }
                };
        final List<Object> list = new ArrayList<>();
        list.add(innerList);
        list.add(innerList2);
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("key", list);
                    }
                };
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, null);
        // verify flattened map string "key.0.0:aaakey.0.1:zzzkey.0.2:111key.1.0:2"
        final long expectedHash = 2441202563L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_WithNestedMap() {
        // setup
        final Map<String, String> innerMap =
                new HashMap<String, String>() {
                    {
                        put("bbb", "5");
                        put("hhh", "false");
                    }
                };
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("aaa", "1");
                        put("zzz", true);
                        put("inner", innerMap);
                    }
                };
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, null);
        // verify flattened map string "aaa:1inner.bbb:5inner.hhh:falsezzz:true"
        final long expectedHash = 4230384023L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_WithNestedMapContainingNestedMap() {
        // setup
        final Map<String, String> secondInnerMap =
                new HashMap<String, String>() {
                    {
                        put("ccc", "10");
                        put("iii", "1.1");
                    }
                };
        final Map<String, Object> innerMap =
                new HashMap<String, Object>() {
                    {
                        put("bbb", 5);
                        put("hhh", false);
                        put("secondInner", secondInnerMap);
                    }
                };
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("aaa", "1");
                        put("zzz", true);
                        put("inner", innerMap);
                    }
                };
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, null);
        // verify flattened map string
        // "aaa:1inner.bbb:5inner.hhh:falseinner.secondInner.ccc:10inner.secondInner.iii:1.1zzz:true"
        final long expectedHash = 1786696518L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_WithEmptyMask() {
        // setup
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("a", "1");
                        put("b", "2");
                    }
                };
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, new String[] {});
        // verify flattened map string "a:1b:2"
        final long expectedHash = 0L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_WithMaskMatchingNoKeys() {
        // setup
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("a", "1");
                        put("b", "2");
                    }
                };
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, new String[] {"c", "d"});
        // verify 0 / no hash generated due to mask keys not being present in the map
        final long expectedHash = 0;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_NullAndEmptyMapValuesPresent_WithAllKeysInMask() {
        // setup
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("a", "1");
                        put("b", "2");
                        put("c", "");
                        put("d", null);
                    }
                };
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, new String[] {"a", "b", "c", "d"});
        // verify flattened map string "a:1b:2"
        final long expectedHash = 3371500665L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void
            testGetFnv1aHash_NullAndEmptyMapValuesPresent_WithKeysForNullAndEmptyValuesInMask() {
        // setup
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("a", "1");
                        put("b", "2");
                        put("c", "");
                        put("d", null);
                    }
                };
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, new String[] {"c", "d"});
        // verify 0 / no hash generated due to mask keys being present for null and empty values
        final long expectedHash = 0;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_NullAndEmptyMapValuesPresentInInnerMap_WithAllKeysInMask() {
        // setup
        final Map<String, Object> inner =
                new HashMap<String, Object>() {
                    {
                        put("a", "1");
                        put("b", "2");
                        put("c", "");
                        put("d", null);
                    }
                };
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("inner", inner);
                    }
                };
        // test
        final long hash =
                MapUtilsKt.convertMapToFnv1aHash(map, new String[] {"inner.a", "inner.b"});
        // verify flattened map string "inner.a:1inner.b:2"
        final long expectedHash = 3328417429L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_NullAndEmptyMapValuesPresentInMultipleNestedInnerMaps() {
        // setup
        final Map<String, Object> nestedNestedInner =
                new HashMap<String, Object>() {
                    {
                        put("k", "5");
                        put("l", "6");
                        put("m", "");
                        put("n", null);
                    }
                };
        final Map<String, Object> nestedInner =
                new HashMap<String, Object>() {
                    {
                        put("f", "3");
                        put("g", "4");
                        put("h", "");
                        put("i", null);
                        put("nestedNestedInner", nestedNestedInner);
                    }
                };
        final Map<String, Object> inner =
                new HashMap<String, Object>() {
                    {
                        put("a", "1");
                        put("b", "2");
                        put("c", "");
                        put("d", null);
                        put("nestedInner", nestedInner);
                    }
                };
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("inner", inner);
                    }
                };
        // test
        final long hash =
                MapUtilsKt.convertMapToFnv1aHash(
                        map,
                        new String[] {
                            "inner.a",
                            "inner.b",
                            "inner.nestedInner.g",
                            "inner.nestedInner.nestedNestedInner.l"
                        });
        // verify flattened map string
        // "inner.a:1inner.b:2inner.nestedInner.g:4inner.nestedInner.nestedNestedInner.l:6"
        final long expectedHash = 4160127196L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_NullAndEmptyMapValuesPresent_WithNoMask() {
        // setup
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("a", "1");
                        put("b", "2");
                        put("c", "");
                        put("d", null);
                        put("e", 3);
                        put("f", "4");
                    }
                };
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, null);
        // verify flattened map string "a:1b:2e:3f:4"
        final long expectedHash = 3916945161L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_NoMask_VerifyEventDataMapSortedWithCaseSensitivity() {
        // setup
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("a", "1");
                        put("A", "2");
                        put("ba", "3");
                        put("Ba", "4");
                        put("Z", "5");
                        put("z", "6");
                        put("r", "7");
                        put("R", "8");
                        put("bc", "9");
                        put("Bc", "10");
                        put("1", 1);
                        put("222", 222);
                    }
                };
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, null);
        // verify flattened map string "1:1222:222A:2Ba:4Bc:10R:8Z:5a:1ba:3bc:9r:7z:6"
        final long expectedHash = 2933724447L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_WithMask_VerifyEventDataMapSortedWithCaseSensitivity() {
        // setup
        final Map<String, Object> map =
                new HashMap<String, Object>() {
                    {
                        put("a", "1");
                        put("A", "2");
                        put("ba", "3");
                        put("Ba", "4");
                        put("Z", "5");
                        put("z", "6");
                        put("r", "7");
                        put("R", "8");
                        put("bc", "9");
                        put("Bc", "10");
                        put("1", 1);
                        put("222", 222);
                    }
                };
        // test
        final long hash =
                MapUtilsKt.convertMapToFnv1aHash(
                        map, new String[] {"A", "a", "ba", "Ba", "bc", "Bc", "1"});
        // verify flattened map string "1:1A:2Ba:4Bc:10a:1ba:3bc:9"
        final long expectedHash = 3344627991L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_EmptyMap() {
        // setup
        final Map<String, Object> map = new HashMap<>();
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, null);
        // verify empty map returns 0 hash
        final long expectedHash = 0L;
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testGetFnv1aHash_NullMap() {
        // setup
        final Map<String, Object> map = null;
        // test
        final long hash = MapUtilsKt.convertMapToFnv1aHash(map, null);
        // verify null map returns 0 hash
        final long expectedHash = 0L;
        assertEquals(expectedHash, hash);
    }
}
