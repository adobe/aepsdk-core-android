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

package com.adobe.marketing.mobile;

import java.util.*;

import static org.junit.Assert.*;

@SuppressWarnings("unchecked")
public class Assertions {

	public static void assertMapSubset(Map map, Map expectedSubset) {
		assertTrue(map.entrySet().containsAll(expectedSubset.entrySet()));
	}

	public static void assertMapContains(Map map, Object key) {
		assertTrue("Map does not contain key: " + key, map.containsKey(key));
	}

	public static void assertMapNotContains(Map map, Object key) {
		assertTrue("Map contains key: " + key, map.containsKey(key));
	}

	public static void assertMapContains(Map map, Object key, Object value) {
		assertTrue("Map does not contain key: " + key, map.containsKey(key));
		assertEquals("Expected value for key " + key + ": " + value + ", got: " + map.get(key),
					 value, map.get(key));
	}

}
