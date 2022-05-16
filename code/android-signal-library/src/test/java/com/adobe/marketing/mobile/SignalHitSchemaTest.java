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

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SignalHitSchemaTest {
	private static final String HIT_ID_COL_NAME = "ID";
	private static final int HIT_ID_COL_INDEX = 0;
	private static final String HIT_URL_COL_NAME = "URL";
	private static final int HIT_URL_COL_INDEX = 1;
	private static final String HIT_TIMESTAMP_COL_NAME = "TIMESTAMP";
	private static final int HIT_TIMESTAMP_COL_INDEX = 2;
	private static final String HIT_POSTBODY_COL_NAME = "POSTBODY";
	private static final int HIT_POSTBODY_COL_INDEX = 3;
	private static final String HIT_CONTENTTYPE_COL_NAME = "CONTENTTYPE";
	private static final int HIT_CONTENTTYPE_COL_INDEX = 4;
	private static final String HIT_TIMEOUT_COL_NAME = "TIMEOUT";
	private static final int HIT_TIMEOUT_COL_INDEX = 5;

	@Test
	public void testGenerateDataMap() {
		SignalHitSchema schema = new SignalHitSchema();
		SignalHit newHit = new SignalHit();
		newHit.identifier = "id";
		newHit.body = "body";
		newHit.contentType = "Get";
		newHit.url = "url";
		newHit.timestamp = 123;
		newHit.timeout = 5;
		Map<String, Object> values = schema.generateDataMap(newHit);
		assertFalse(values.containsKey(HIT_ID_COL_NAME));
		assertEquals("body", values.get(HIT_POSTBODY_COL_NAME));
		assertEquals(123L, values.get(HIT_TIMESTAMP_COL_NAME));
		assertEquals(5, values.get(HIT_TIMEOUT_COL_NAME));
		assertEquals("Get", values.get(HIT_CONTENTTYPE_COL_NAME));
		assertEquals("url", values.get(HIT_URL_COL_NAME));
	}
}
