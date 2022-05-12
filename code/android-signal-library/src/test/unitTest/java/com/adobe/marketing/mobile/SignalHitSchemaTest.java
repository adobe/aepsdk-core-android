/* ***********************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/

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
