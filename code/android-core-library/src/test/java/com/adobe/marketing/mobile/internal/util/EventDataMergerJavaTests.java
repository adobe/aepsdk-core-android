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

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class EventDataMergerJavaTests {

    @Test
    public void testMerge() {
        Map<String, Object> innerMap1 =
                new HashMap<String, Object>() {
                    {
                        put("key", "oldValue");
                        put("toBeDeleted", "value");
                    }
                };
        Map<String, Object> innerMap2 =
                new HashMap<String, Object>() {
                    {
                        put("newKey", "newValue");
                        put("toBeDeleted", null);
                    }
                };
        Map<String, Object> toMap =
                new HashMap<String, Object>() {
                    {
                        put("nested", innerMap1);
                    }
                };
        Map<String, Object> fromMap =
                new HashMap<String, Object>() {
                    {
                        put("nested", innerMap2);
                    }
                };
        Map<String, Object> mergedMap = EventDataMerger.merge(fromMap, toMap, true);
        Map<String, Object> innerMap = (Map<String, Object>) mergedMap.get("nested");
        assertEquals(2, innerMap.size());
        assertEquals("oldValue", innerMap.get("key"));
        assertEquals("newValue", innerMap.get("newKey"));
    }
}
