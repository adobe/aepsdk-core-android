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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class VisitorIDTests {
	private FakePlatformServices services;

	@Before
	public void setup() {
		services = new FakePlatformServices();
		com.adobe.marketing.mobile.services.Log.setLogLevel(LoggingMode.VERBOSE);
	}

	@Test
	public void testVisitorIDAuthenticationState_FromInteger() {
		assertEquals(VisitorID.AuthenticationState.UNKNOWN, VisitorID.AuthenticationState.fromInteger(0));
		assertEquals(VisitorID.AuthenticationState.AUTHENTICATED, VisitorID.AuthenticationState.fromInteger(1));
		assertEquals(VisitorID.AuthenticationState.LOGGED_OUT, VisitorID.AuthenticationState.fromInteger(2));
		assertEquals(VisitorID.AuthenticationState.UNKNOWN, VisitorID.AuthenticationState.fromInteger(100));
	}

	@Test
	public void testVisitorIDAuthenticationState_GetValue() {
		assertEquals(0, VisitorID.AuthenticationState.UNKNOWN.getValue());
		assertEquals(1, VisitorID.AuthenticationState.AUTHENTICATED.getValue());
		assertEquals(2, VisitorID.AuthenticationState.LOGGED_OUT.getValue());
	}

	@Test
	public void testVisitorIdConstructor_Happy() {
		VisitorID visitorID = new VisitorID("testOrigin", "testType", "testId", VisitorID.AuthenticationState.AUTHENTICATED);
		assertEquals("testId", visitorID.getId());
		assertEquals("testOrigin", visitorID.getIdOrigin());
		assertEquals("testType", visitorID.getIdType());
		assertEquals(VisitorID.AuthenticationState.AUTHENTICATED, visitorID.getAuthenticationState());
	}

	@Test
	public void testVisitorIdConstructor_When_IdTypeContainsNotAllowedCharacters() {
		VisitorID visitorID = new VisitorID("testOrigin", "test+_Type().is*#Awesome$@&^", "testId",
											VisitorID.AuthenticationState.AUTHENTICATED);
		assertEquals("testId", visitorID.getId());
		assertEquals("testOrigin", visitorID.getIdOrigin());
		assertEquals("test_Type.isAwesome", visitorID.getIdType());
		assertEquals(VisitorID.AuthenticationState.AUTHENTICATED, visitorID.getAuthenticationState());
	}

	@Test(expected = IllegalStateException.class)
	public void testVisitorIdConstructor_When_NullIdType_ThrowsIllegalException() {
		new VisitorID("testOrigin", null, "testId", VisitorID.AuthenticationState.AUTHENTICATED);
	}

	@Test(expected = IllegalStateException.class)
	public void testVisitorIdConstructor_When_EmptyIdType_ThrowsIllegalException() {
		new VisitorID("testOrigin", "", "testId", VisitorID.AuthenticationState.AUTHENTICATED);
	}

	@Ignore
	@Test
	public void testVisitorIdConstructor_When_NullId_LogWarning() {
		new VisitorID("testOrigin", "testIdType", null, VisitorID.AuthenticationState.AUTHENTICATED);
		assertTrue(services.fakeLoggingService.containsDebugLog("VisitorID",
				   "The custom VisitorID should not have null/empty id, this VisitorID will be ignored"));
	}

	@Ignore
	@Test
	public void testVisitorIdConstructor_When_EmptyId_LogWarning() {
		new VisitorID("testOrigin", "testIdType", "", VisitorID.AuthenticationState.AUTHENTICATED);
		assertTrue(services.fakeLoggingService.containsDebugLog("VisitorID",
				   "The custom VisitorID should not have null/empty id, this VisitorID will be ignored"));
	}

	@Test
	public void testVisitorId_WithANullObject_NotEqual() {
		VisitorID visitorID1 = new VisitorID("testOrigin", "testType", "testId", VisitorID.AuthenticationState.LOGGED_OUT);
		VisitorID visitorID2 = null;
		assertFalse(visitorID1.equals(visitorID2));
	}

	@Test
	public void testVisitorId_TwoIds_Equal() {
		VisitorID visitorID1 = new VisitorID("testOrigin", "testType", "testId", VisitorID.AuthenticationState.LOGGED_OUT);
		VisitorID visitorID2 = new VisitorID("testOrigin", "testType", "testId", VisitorID.AuthenticationState.LOGGED_OUT);
		assertTrue(visitorID1.equals(visitorID2));
	}

	@Test
	public void testVisitorId_TwoOrigins_Equal() {
		VisitorID visitorID1 = new VisitorID("testOriginOne", "testType", "testId", VisitorID.AuthenticationState.LOGGED_OUT);
		VisitorID visitorID2 = new VisitorID("testOriginTwo", "testType", "testId", VisitorID.AuthenticationState.LOGGED_OUT);
		assertTrue(visitorID1.equals(visitorID2));
	}


	@Test
	public void testVisitorId_TwoIds_SameInstance_Equal() {
		VisitorID visitorID1 = new VisitorID("testOrigin", "testType", "testId", VisitorID.AuthenticationState.LOGGED_OUT);
		assertTrue(visitorID1.equals(visitorID1));
	}

	@Test
	public void testVisitorId_TwoIdTypes_NotEqual() {
		VisitorID visitorID1 = new VisitorID("testOrigin", "testType", "testId", VisitorID.AuthenticationState.LOGGED_OUT);
		VisitorID visitorID2 = new VisitorID("testOrigin", "otherType", "testId", VisitorID.AuthenticationState.LOGGED_OUT);
		assertFalse(visitorID1.equals(visitorID2));
	}

	@Test
	public void testVisitorId_OneId_AnotherObject_NotEqual() {
		VisitorID visitorID1 = new VisitorID("testOrigin", "testType", "testId", VisitorID.AuthenticationState.LOGGED_OUT);
		assertFalse(visitorID1.equals("someTesting"));
	}

	@Test
	public void testVisitorId_TwoDifferentIds_NotEqual() {
		VisitorID visitorID1 = new VisitorID("testOrigin", "testType", "testId", VisitorID.AuthenticationState.LOGGED_OUT);
		VisitorID visitorID2 = new VisitorID("testOrigin", "testType", "otherTestId", VisitorID.AuthenticationState.LOGGED_OUT);
		assertFalse(visitorID1.equals(visitorID2));
	}

	@Test
	public void testVisitorId_TwoNullIds_Equal() {
		VisitorID visitorID1 = new VisitorID("testOrigin", "testType", null, VisitorID.AuthenticationState.LOGGED_OUT);
		VisitorID visitorID2 = new VisitorID("testOrigin", "testType", null, VisitorID.AuthenticationState.LOGGED_OUT);
		assertTrue(visitorID1.equals(visitorID2));
	}

	@Test
	public void testVisitorId_TwoIds_FirstOneNullId_NotEqual() {
		VisitorID visitorID1 = new VisitorID("testOrigin", "testType", null, VisitorID.AuthenticationState.LOGGED_OUT);
		VisitorID visitorID2 = new VisitorID("testOrigin", "testType", "testId", VisitorID.AuthenticationState.LOGGED_OUT);
		assertFalse(visitorID1.equals(visitorID2));
	}
	@Test
	public void testVisitorId_TwoIds_SecondOneNullId_NotEqual() {
		VisitorID visitorID1 = new VisitorID("testOrigin", "testType", "testId", VisitorID.AuthenticationState.LOGGED_OUT);
		VisitorID visitorID2 = new VisitorID("testOrigin", "testType", null, VisitorID.AuthenticationState.LOGGED_OUT);
		assertFalse(visitorID1.equals(visitorID2));
	}
}
