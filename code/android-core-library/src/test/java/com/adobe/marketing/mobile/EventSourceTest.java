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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class EventSourceTest {

	@Test
	public void getEventSource_ShouldReturnNullWhenNameIsNullOrEmpty() {
		assertNull(EventSource.get(null));
		assertNull(EventSource.get(""));
		assertNull(EventSource.get("  "));
	}


	@Test
	public void getEventSource_ShouldReturnTheSameObjectWhenNameIsSame() {
		assertSame(EventSource.get("test"), EventSource.get("test"));
	}

	@Test
	public void getEventSource_ShouldBeCaseInsensitive() {
		assertSame(EventSource.get("TEST"), EventSource.get("test"));
	}


	@Test
	public void getName() {
		final EventSource testSource1 = EventSource.get("TEST");
		final EventSource testSource2 = EventSource.get("test");
		assertNotNull(testSource1);
		assertNotNull(testSource2);
		assertEquals("test", testSource1.getName());
		assertEquals("test", testSource2.getName());
	}

}
