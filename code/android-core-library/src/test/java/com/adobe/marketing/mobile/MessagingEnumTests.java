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

import static org.junit.Assert.assertEquals;

import com.adobe.marketing.mobile.services.ui.MessageSettings.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessagingEnumTests {

	@Test
	public void testMessageAlignmentEnumValueOf() {
		// setup
		MessageAlignment center = MessageAlignment.valueOf("CENTER");
		MessageAlignment left = MessageAlignment.valueOf("LEFT");
		MessageAlignment right = MessageAlignment.valueOf("RIGHT");
		MessageAlignment top = MessageAlignment.valueOf("TOP");
		MessageAlignment bottom = MessageAlignment.valueOf("BOTTOM");

		// verify
		assertEquals(center, MessageAlignment.CENTER);
		assertEquals(left, MessageAlignment.LEFT);
		assertEquals(right, MessageAlignment.RIGHT);
		assertEquals(top, MessageAlignment.TOP);
		assertEquals(bottom, MessageAlignment.BOTTOM);
	}

	@Test
	public void testMessageAnimationEnumValueOf() {
		// setup
		MessageAnimation none = MessageAnimation.valueOf("NONE");
		MessageAnimation left = MessageAnimation.valueOf("LEFT");
		MessageAnimation right = MessageAnimation.valueOf("RIGHT");
		MessageAnimation top = MessageAnimation.valueOf("TOP");
		MessageAnimation bottom = MessageAnimation.valueOf("BOTTOM");
		MessageAnimation center = MessageAnimation.valueOf("CENTER");
		MessageAnimation fade = MessageAnimation.valueOf("FADE");

		// verify
		assertEquals(none, MessageAnimation.NONE);
		assertEquals(left, MessageAnimation.LEFT);
		assertEquals(right, MessageAnimation.RIGHT);
		assertEquals(top, MessageAnimation.TOP);
		assertEquals(bottom, MessageAnimation.BOTTOM);
		assertEquals(center, MessageAnimation.CENTER);
		assertEquals(fade, MessageAnimation.FADE);
	}

	@Test
	public void testMessageGestureEnumValueOf() {
		// setup
		MessageGesture up = MessageGesture.valueOf("SWIPE_UP");
		MessageGesture down = MessageGesture.valueOf("SWIPE_DOWN");
		MessageGesture left = MessageGesture.valueOf("SWIPE_LEFT");
		MessageGesture right = MessageGesture.valueOf("SWIPE_RIGHT");
		MessageGesture background_tap = MessageGesture.valueOf("BACKGROUND_TAP");

		// verify
		assertEquals(up, MessageGesture.SWIPE_UP);
		assertEquals(down, MessageGesture.SWIPE_DOWN);
		assertEquals(left, MessageGesture.SWIPE_LEFT);
		assertEquals(right, MessageGesture.SWIPE_RIGHT);
		assertEquals(background_tap, MessageGesture.BACKGROUND_TAP);
	}

	@Test
	public void testMessageGestureEnumToString() {
		// verify
		assertEquals(MessageGesture.SWIPE_UP, MessageGesture.get("swipeUp"));
		assertEquals(MessageGesture.SWIPE_DOWN, MessageGesture.get("swipeDown"));
		assertEquals(MessageGesture.SWIPE_LEFT, MessageGesture.get("swipeLeft"));
		assertEquals(MessageGesture.SWIPE_RIGHT, MessageGesture.get("swipeRight"));
		assertEquals(MessageGesture.BACKGROUND_TAP, MessageGesture.get("backgroundTap"));
	}
}
