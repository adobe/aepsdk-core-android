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

import static org.junit.Assert.*;

public class EventTypeTest {

	@Test
	public void getEventType_ShouldReturnNullWhenNameIsNullOrEmpty() {
		assertNull(EventType.get(null));
		assertNull(EventType.get(""));
		assertNull(EventType.get("  "));
	}

	@Test
	public void getEventType_ShouldReturnTheSameObjectWhenNameIsSame() {
		assertSame(EventType.get("test"), EventType.get("test"));
	}

	@Test
	public void getEventType_ShouldBeCaseInsensitive() {
		assertSame(EventType.get("TEST"), EventType.get("test"));
	}

	@Test
	public void getName() {
		final EventType testType1 = EventType.get("TEST");
		final EventType testType2 = EventType.get("test");
		assertNotNull(testType1);
		assertNotNull(testType2);
		assertEquals("test", testType1.getName());
		assertEquals("test", testType2.getName());
	}
}
