/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 ******************************************************************************/

package com.adobe.marketing.mobile;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class IdentityHitSchemaTest {
	private static final String   COL_REQUESTS_ID					= "ID";
	private static final String   COL_REQUESTS_URL					= "URL";
	private static final String   COL_REQUESTS_CONFIG_SSL			= "SSL";
	private static final String   COL_REQUESTS_PAIR_ID				= "PAIR_ID";
	private static final String   COL_REQUESTS_EVENT_NUMBER			= "EVENT_NUMBER";
	private static final String   COL_REQUESTS_TIMESTAMP			= "TIMESTAMP";

	@Test
	public void testGenerateDataMap() {
		IdentityHitSchema schema = new IdentityHitSchema();
		IdentityHit newHit = new IdentityHit();
		newHit.identifier = "id";
		newHit.configSSL = true;
		newHit.url = "url";
		newHit.timestamp = 123;
		newHit.eventNumber = 10;
		newHit.pairId = "pairID";

		Map<String, Object> values = schema.generateDataMap(newHit);
		assertFalse(values.containsKey(COL_REQUESTS_ID));
		assertEquals("pairID", values.get(COL_REQUESTS_PAIR_ID));
		assertEquals(10, values.get(COL_REQUESTS_EVENT_NUMBER));
		assertEquals(true, values.get(COL_REQUESTS_CONFIG_SSL));
		assertEquals(123L, values.get(COL_REQUESTS_TIMESTAMP));
		assertEquals("url", values.get(COL_REQUESTS_URL));
	}
}
