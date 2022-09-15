/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2017 Adobe
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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class IdentityObjectTest extends BaseTest {

	IdentityResponseObject identityResponseObject;

	@Before
	public void setup() throws Exception {
		super.beforeEach();
		identityResponseObject = new IdentityResponseObject();
	}

	@Test
	public void testSetBlob_Should_SetBlob() {
		identityResponseObject.blob = "blob_this";
		assertEquals("blob_this", identityResponseObject.blob);
	}

	@Test
	public void testSetMid_Should_SetMid() {
		identityResponseObject.mid = "mid_this";
		assertEquals("mid_this", identityResponseObject.mid);
	}

	@Test
	public void testSetHint_Should_SetHint() {
		identityResponseObject.hint = "hint_this";
		assertEquals("hint_this", identityResponseObject.hint);
	}

	@Test
	public void testSetError_Should_SetError() {
		identityResponseObject.error = "error_this";
		assertEquals("error_this", identityResponseObject.error);
	}

	@Test
	public void testSetTTl_Should_SetTTL() {
		identityResponseObject.ttl = 100;
		assertEquals(100, identityResponseObject.ttl);
	}

	@Test
	public void testSetOptOutList_Should_SetOptOut() {

		ArrayList<String> list = new ArrayList<String>() {
			{
				add("abc");
				add("def");
			}
		};

		identityResponseObject.optOutList = list;
		assertEquals(2, identityResponseObject.optOutList.size());
	}



}


