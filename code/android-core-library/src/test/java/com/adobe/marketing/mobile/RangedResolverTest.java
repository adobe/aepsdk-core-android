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
import org.junit.Test;

import static com.adobe.marketing.mobile.EventHub.*;
import static org.junit.Assert.*;

public class RangedResolverTest {

	private RangedResolver<EventData> resolver;

	private final EventData ZERO = new EventData();
	private final EventData ONE = new EventData();
	private final EventData TWO = new EventData();
	private final EventData THREE = new EventData();
	private final EventData FOUR = new EventData();
	private final EventData FIVE = new EventData();
	private final EventData TEN = new EventData();


	@Before
	public void beforeEach() {
		resolver = new RangedResolver<EventData>(
			SHARED_STATE_PENDING,
			SHARED_STATE_INVALID,
			SHARED_STATE_NEXT,
			SHARED_STATE_PREV);
	}


	@Test
	public void constructorTest() {
		assertNotNull("Unable to create versioned object", resolver);
	}

	@Test
	public void addVersionTest() {
		resolver.add(0, ZERO);
		assertTrue(resolver.add(5, FIVE));
	}

	@Test
	public void addMultipleVersionsTest() {
		assertTrue(resolver.add(0, ZERO));
		assertTrue(resolver.add(1, ONE));
		assertTrue(resolver.add(2, TWO));
		assertTrue(resolver.add(3, THREE));
		assertTrue(resolver.add(4, FOUR));
	}

	@Test
	public void addSameVersionTest() {
		assertTrue(resolver.add(0, ZERO));
		assertFalse(resolver.add(0, ONE));
	}

	@Test
	public void addAndRetrieveVersionTestSingle() {
		assertTrue(resolver.add(0, ZERO));
		assertEquals("reading object failed", ZERO, resolver.resolve(5));
	}

	@Test
	public void addAndRetrieveVersionTestMultiple() {
		assertTrue(resolver.add(0, ZERO));
		resolver.add(3, THREE);
		resolver.add(5, FIVE);
		assertEquals("reading object version 3 failed", THREE, resolver.resolve(3));
		assertEquals("reading object version 5 failed", FIVE, resolver.resolve(5));
		assertEquals("reading object version 9 failed", FIVE, resolver.resolve(9));
		assertEquals("reading object version 320 failed", FIVE, resolver.resolve(350));
	}

	@Test(timeout = 1000)
	public void modifyVersionTest() {
		assertTrue(resolver.add(0, SHARED_STATE_PENDING));
		assertEquals("reading initial set(null) of version 0 failed", SHARED_STATE_PENDING, resolver.resolve(0));
		assertTrue(resolver.update(0, ZERO));
		assertEquals("reading updated value(\"notnull\") of version 0 failed", ZERO, resolver
					 .resolve(0));
	}

	@Test(timeout = 1000)
	public void recursiveUpdateTest() {
		assertTrue(resolver.add(0, SHARED_STATE_PENDING));

		assertTrue(resolver.add(5, FIVE));
		assertEquals("reading initial state of node5 failed", FIVE, resolver.resolve(5));
		assertFalse(resolver.update(10, TEN));
		assertFalse(resolver.update(3, THREE));
		assertFalse(resolver.update(-1, ONE));
		assertFalse(resolver.update(5, TEN)); // only update PENDING
		assertTrue(resolver.update(0, ZERO));
		assertEquals("reading new state of node5 failed", FIVE, resolver.resolve(5));
		assertEquals("reading new state of node5 failed", ZERO, resolver.resolve(0));
	}

	@Test
	public void createPreviousVersionTest() {
		assertTrue(resolver.add(10, TEN));
		assertFalse("able to add version 5 after version 10", resolver.add(5, FIVE));
	}

	@Test
	public void validateOpenEndedRangeStartTest() {
		assertTrue(resolver.add(10, TEN));
		assertEquals("open ended range start failed (5 did not resolve when 10 was first object)",
					 TEN, resolver.resolve(5));
	}

	@Test
	public void validateOpenEndedRangeEndTest() {
		assertTrue(resolver.add(10, TEN));
		assertEquals("open ended range end failed (100 did not resolve when 10 was last ojbect)",
					 TEN, resolver.resolve(100));
	}

	@Test
	public void validateNullAcceptedTest() {
		assertTrue(resolver.add(10, SHARED_STATE_PENDING));
		org.junit.Assert.assertNull(resolver.resolve(10));
	}

	@Test
	public void resolvesDirectVersionAccessTest() {
		assertTrue(resolver.add(0, ZERO));
		assertTrue(resolver.add(1, ONE));
		assertTrue(resolver.add(2, TWO));
		assertTrue(resolver.add(3, THREE));
		assertTrue(resolver.add(4, FOUR));
		assertTrue(resolver.add(5, FIVE));
		assertTrue(resolver.add(10, TEN));

		assertEquals("retrieval for version 0 failed", ZERO, resolver.resolve(0));
		assertEquals("retrieval for version 1 failed", ONE, resolver.resolve(1));
		assertEquals("retrieval for version 2 failed", TWO, resolver.resolve(2));
		assertEquals("retrieval for version 3 failed", THREE, resolver.resolve(3));
		assertEquals("retrieval for version 4 failed", FOUR, resolver.resolve(4));
		assertEquals("retrieval for version 5 failed", FIVE, resolver.resolve(5));
		assertEquals("retrieval for version 10 failed", TEN, resolver.resolve(10));

	}

	@Test
	public void addWorksWithSpecialStates() {
		assertTrue(resolver.add(1, ONE));
		assertTrue(resolver.add(3, SHARED_STATE_PENDING));
		assertTrue(resolver.add(4, SHARED_STATE_INVALID));
		assertFalse(resolver.add(5, SHARED_STATE_NEXT));
		assertFalse(resolver.add(6, SHARED_STATE_PREV));
	}

	@Test
	public void updateWorksWithSpecialStates() {
		assertTrue(resolver.add(0, SHARED_STATE_PENDING));
		assertTrue(resolver.add(1, ONE));
		assertTrue(resolver.add(2, SHARED_STATE_PENDING));
		assertTrue(resolver.add(4, FOUR));
		assertTrue(resolver.add(5, SHARED_STATE_PENDING));

		assertTrue(resolver.update(2, SHARED_STATE_INVALID));
		assertTrue(resolver.update(0, SHARED_STATE_NEXT));
		assertFalse(resolver.update(5, SHARED_STATE_PENDING)); // update PENDING with PENDING returns false
		assertTrue(resolver.update(5, SHARED_STATE_PREV));

		assertFalse(resolver.update(2, SHARED_STATE_INVALID));
		assertFalse(resolver.update(0, SHARED_STATE_PENDING));
		assertFalse(resolver.update(5, FIVE));
	}

	@Test
	public void getWorksWithSpecialStates() {

		assertTrue(resolver.add(2, TWO));
		assertTrue(resolver.add(3, SHARED_STATE_PENDING));
		assertTrue(resolver.update(3, SHARED_STATE_PREV));
		assertTrue(resolver.add(4, SHARED_STATE_PENDING));
		assertTrue(resolver.update(4, SHARED_STATE_NEXT));
		assertTrue(resolver.add(5, SHARED_STATE_PENDING));
		assertTrue(resolver.update(5, SHARED_STATE_PREV));
		assertTrue(resolver.add(7, SHARED_STATE_INVALID));

		assertEquals(TWO, resolver.resolve(0));
		assertEquals(TWO, resolver.resolve(1));
		assertEquals(TWO, resolver.resolve(2));
		assertEquals(TWO, resolver.resolve(3));
		assertEquals(SHARED_STATE_INVALID, resolver.resolve(4));
		assertEquals(SHARED_STATE_INVALID, resolver.resolve(5));
		assertEquals(SHARED_STATE_INVALID, resolver.resolve(6));
		assertEquals(SHARED_STATE_INVALID, resolver.resolve(7));

	}

	@Test
	public void getWorksWithNext() {
		assertTrue(resolver.add(0, ZERO));
		assertTrue(resolver.add(1, SHARED_STATE_PENDING));
		assertTrue(resolver.add(2, TWO));
		assertTrue(resolver.add(3, SHARED_STATE_PENDING));
		assertTrue(resolver.add(4, FOUR));

		assertEquals(ZERO, resolver.resolve(0));
		assertEquals(SHARED_STATE_PENDING, resolver.resolve(1));
		assertEquals(TWO, resolver.resolve(2));
		assertEquals(SHARED_STATE_PENDING, resolver.resolve(3));
		assertEquals(FOUR, resolver.resolve(4));
		assertEquals(FOUR, resolver.resolve(5));

		assertTrue(resolver.update(1, SHARED_STATE_NEXT));
		assertTrue(resolver.update(3, SHARED_STATE_NEXT));

		assertEquals(ZERO, resolver.resolve(0));
		assertEquals(TWO, resolver.resolve(1));
		assertEquals(TWO, resolver.resolve(2));
		assertEquals(FOUR, resolver.resolve(3));
		assertEquals(FOUR, resolver.resolve(4));
		assertEquals(FOUR, resolver.resolve(5));

	}

	@Test
	public void getWorksWithPrev() {
		assertTrue(resolver.add(0, ZERO));
		assertTrue(resolver.add(1, SHARED_STATE_PENDING));
		assertTrue(resolver.add(2, TWO));
		assertTrue(resolver.add(3, SHARED_STATE_PENDING));
		assertTrue(resolver.add(4, FOUR));

		assertEquals(ZERO, resolver.resolve(0));
		assertEquals(SHARED_STATE_PENDING, resolver.resolve(1));
		assertEquals(TWO, resolver.resolve(2));
		assertEquals(SHARED_STATE_PENDING, resolver.resolve(3));
		assertEquals(FOUR, resolver.resolve(4));
		assertEquals(FOUR, resolver.resolve(5));

		assertTrue(resolver.update(1, SHARED_STATE_PREV));
		assertTrue(resolver.update(3, SHARED_STATE_PREV));

		assertEquals(ZERO, resolver.resolve(0));
		assertEquals(ZERO, resolver.resolve(1));
		assertEquals(TWO, resolver.resolve(2));
		assertEquals(TWO, resolver.resolve(3));
		assertEquals(FOUR, resolver.resolve(4));
		assertEquals(FOUR, resolver.resolve(5));

	}

	@Test
	public void getWorksWithNoStates() {
		assertEquals(SHARED_STATE_PENDING, resolver.resolve(0));
		assertEquals(SHARED_STATE_PENDING, resolver.resolve(3));

	}

	@Test
	public void getWorksWithPreviousAndNext() {
		// back of list has implicit NEXT state
		assertTrue(resolver.add(3, SHARED_STATE_PENDING));
		assertTrue(resolver.add(5, FIVE));
		assertTrue(resolver.update(3, SHARED_STATE_PREV));

		assertEquals(FIVE, resolver.resolve(3));

	}

	@Test
	public void getWorksWithPreviousEdgeCase() {
		assertTrue(resolver.add(0, SHARED_STATE_PENDING));
		assertTrue(resolver.add(3, SHARED_STATE_PENDING));
		assertTrue(resolver.add(5, FIVE));
		assertTrue(resolver.update(0, SHARED_STATE_PREV));
		assertTrue(resolver.update(3, SHARED_STATE_PREV));

		// back of list has implicit NEXT state
		assertEquals(FIVE, resolver.resolve(3));

	}

	@Test
	public void pendingStateIsNull() {
		assertTrue(resolver.add(2, SHARED_STATE_PENDING));
		assertEquals(null, resolver.resolve(2));
		assertTrue(resolver.add(3, null));
		assertEquals(SHARED_STATE_PENDING, resolver.resolve(3));

	}

	@Test
	public void containsWorksEmptyContainer() {
		assertFalse(resolver.containsValidState());
	}

	@Test
	public void containsWorksPendingState() {
		assertTrue(resolver.add(0, SHARED_STATE_PENDING));
		assertTrue(resolver.containsValidState());
	}

	@Test
	public void containsWorksDataState() {
		assertTrue(resolver.add(0, ZERO));
		assertTrue(resolver.containsValidState());
	}

	@Test
	public void containsWorksNextState() {
		assertTrue(resolver.add(0, SHARED_STATE_PENDING));
		assertTrue(resolver.update(0, SHARED_STATE_NEXT));
		assertFalse(resolver.containsValidState());
	}

	@Test
	public void containsWorksPrevState() {
		assertTrue(resolver.add(0, SHARED_STATE_PENDING));
		assertTrue(resolver.update(0, SHARED_STATE_PREV));
		assertFalse(resolver.containsValidState());
	}

	@Test
	public void containsWorksInvalidState() {
		assertTrue(resolver.add(0, SHARED_STATE_INVALID));
		assertFalse(resolver.containsValidState());
	}

	@Test
	public void containsWorksMultipleInvalid() {
		assertTrue(resolver.add(0, SHARED_STATE_PENDING));
		assertTrue(resolver.update(0, SHARED_STATE_PREV));
		assertTrue(resolver.add(3, SHARED_STATE_INVALID));
		assertTrue(resolver.add(4, SHARED_STATE_PENDING));
		assertTrue(resolver.update(4, SHARED_STATE_NEXT));
		assertTrue(resolver.add(5, SHARED_STATE_INVALID));
		assertTrue(resolver.add(10, SHARED_STATE_INVALID));
		assertFalse(resolver.containsValidState());
	}

	@Test
	public void containsWorksMultipleValid() {
		assertTrue(resolver.add(0, SHARED_STATE_PENDING));
		assertTrue(resolver.add(2, SHARED_STATE_INVALID));
		assertTrue(resolver.add(3, THREE));
		assertTrue(resolver.add(4, SHARED_STATE_PENDING));
		assertTrue(resolver.update(4, SHARED_STATE_NEXT));
		assertTrue(resolver.add(5, SHARED_STATE_INVALID));
		assertTrue(resolver.add(10, SHARED_STATE_INVALID));
		assertTrue(resolver.containsValidState());
	}

}
