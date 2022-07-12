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
package com.adobe.marketing.mobile.services.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Interface defining a MessageSettings object which defines the layout and behavior of an in-app message.
 *
 * These settings are customizable in a Messaging extension message:
 * width: Width of the view in which the message is displayed. Represented in percentage of the total screen width.
 * height: Height of the view in which the message is displayed. Represented in percentage of the total screen height.
 * verticalAlign: Defines the vertical alignment of the message. The alignments can be seen in the MessageAlignment enum.
 * horizontalAlign: Defines the horizontal alignment of the message. The alignments can be seen in the MessageAlignment enum.
 * verticalInset: Defines the vertical inset respective to the verticalAlign. Represented in percentage of the total screen height.
 * horizontalInset: Defines the horizontal inset respective to the horizontalAlign. Represented in percentage of the total screen width.
 * uiTakeover: If true, a displayed message will prevent the user from other UI interactions.
 * displayAnimation: Defines the animation to be used when the message is displayed. The animations can be seen in the MessageAnimation enum.
 * dismissAnimation: Defines the animation to be used when the message is dismissed. The animations can be seen in the MessageAnimation enum.
 * backdropColor: Defines the color of the backdrop shown when a uiTakeover message is displayed.
 * backdropOpacity: Defines the opacity of the backdrop shown when a uiTakeover message is displayed.
 * cornerRadius: Defines the angle to use when rounding the message's webview.
 * gestures: A mapping of gestures and their associated behaviors. The gestures can be seen in the MessageGesture enum. The behavior string is
 * 			 handled by the FullscreenMessageDelegate's overrideUrlLoad function.
 */
public interface MessageSettings {
	/**
	 * Enum representing Message alignment.
	 */
	enum MessageAlignment {
		CENTER,
		LEFT,
		RIGHT,
		TOP,
		BOTTOM
	}

	/**
	 * Enum representing Message animations.
	 */
	enum MessageAnimation {
		NONE,
		LEFT,
		RIGHT,
		TOP,
		BOTTOM,
		CENTER,
		FADE
	}

	/**
	 * Enum representing Message gestures.
	 */
	enum MessageGesture {
		SWIPE_UP("swipeUp"),
		SWIPE_DOWN("swipeDown"),
		SWIPE_LEFT("swipeLeft"),
		SWIPE_RIGHT("swipeRight"),
		BACKGROUND_TAP("backgroundTap");

		private String name;
		private static final Map<String, MessageGesture> gestureStringToGestureEnumMap;

		// initialize the string to gesture map
		static {
			final Map<String, MessageGesture> map = new HashMap<String, MessageGesture>();

			for (MessageGesture gesture : MessageGesture.values()) {
				map.put(gesture.toString(), gesture);
			}

			gestureStringToGestureEnumMap = Collections.unmodifiableMap(map);
		}

		MessageGesture(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		public static MessageGesture get(final String name) {
			return gestureStringToGestureEnumMap.get(name);
		}
	}

	/**
	 * Returns the object that owns the message created using these settings.
	 * @return an {@code Object} which owns the {@link FullscreenMessage} created using this {@link MessageSettings} object.
	 */
	Object getParent();

	/**
	 * Returns the width of the view in which the message is displayed.
	 * Represented in percentage of the total screen width.
	 * @return An {@code int} containing the percentage width of the view in which the message is displayed
	 */
	int getWidth();

	/**
	 * Returns the height of the view in which the message is displayed.
	 * Represented in percentage of the total screen height.
	 * @return An {@code int} containing the height of the view in which the message is displayed
	 */
	int getHeight();

	/**
	 * Returns the vertical alignment of the message.
	 * See {@link MessageAlignment}.
	 * @return A {@code MessageAlignment} value for the vertical alignment
	 */
	MessageAlignment getVerticalAlign();

	/**
	 * Returns the horizontal alignment of the message.
	 * See {@link MessageAlignment}.
	 * @return A {@code MessageAlignment} value for the horizontal alignment
	 */
	MessageAlignment getHorizontalAlign();

	/**
	 * Returns the vertical inset respective to the vertical alignment.
	 * Represented in percentage of the total screen height.
	 * @return An {@code int} containing the vertical inset percentage
	 */
	int getVerticalInset();

	/**
	 * Returns the horizontal inset respective to the `horizontal alignment.
	 * Represented in percentage of the total screen width.
	 * @return An {@code int} containing the horizontal inset percentage
	 */
	int getHorizontalInset();

	/**
	 * Returns a boolean which if true, will prevent the user from other UI interactions if a message is currently displayed.
	 * @return A {@code boolean} value which if true, will prevent the user from other UI interactions if a message is currently displayed.
	 */
	boolean getUITakeover();

	/**
	 * Returns the animation to be used when the message is displayed.
	 * See {@link MessageAnimation}.
	 * @return A {@code MessageAnimation} value for the display animation
	 */
	MessageAnimation getDisplayAnimation();

	/**
	 * Returns the animation to be used when the message is dismissed.
	 * See {@link MessageAnimation}.
	 * @return A {@code MessageAnimation} value for the dismiss animation
	 */
	MessageAnimation getDismissAnimation();

	/**
	 * Set the animation to be used when the message is dismissed.
	 * See {@link MessageAnimation}.
	 */
	void setDismissAnimation(MessageAnimation dismissAnimation);

	/**
	 * Returns the color of the backdrop shown when a uiTakeover message is displayed.
	 * @return A {@code String} containing the HTML hex string color code to use for the backdrop
	 */
	String getBackdropColor();

	/**
	 * Returns the opacity of the backdrop shown when a uiTakeover message is displayed.
	 * @return A {@code float} containing the backdrop opacity percentage with 0.0 being fully transparent
	 * and 1.0 being fully opaque
	 */
	float getBackdropOpacity();

	/**
	 * Returns the angle to use when rounding the message's webview.
	 * @return A {@code float} containing the corner radius angle to use when rounding the message corners
	 */
	float getCornerRadius();

	/**
	 * Returns a mapping of gestures and their associated behaviors.
	 * @return A {@code Map<MessageGesture, String} containing gestures and their associated behaviors
	 */
	Map<MessageGesture, String> getGestures();

	/**
	 * Interface defining a MessageSettingsBuilder which is used for creating new MessageSettings objects.
	 * The parent can only be set in the constructor of the MessageSettingsBuilder implementation.
	 */
	interface MessageSettingsBuilder {
		MessageSettingsBuilder setWidth(final int width);

		MessageSettingsBuilder setHeight(final int height);

		MessageSettingsBuilder setVerticalAlign(final MessageAlignment verticalAlign);

		MessageSettingsBuilder setHorizontalAlign(final MessageAlignment horizontalAlign);

		MessageSettingsBuilder setVerticalInset(final int verticalInset);

		MessageSettingsBuilder setHorizontalInset(final int horizontalInset);

		MessageSettingsBuilder setUiTakeover(final boolean uiTakeover);

		MessageSettingsBuilder setDisplayAnimation(final MessageAnimation animation);

		MessageSettingsBuilder setDismissAnimation(final MessageAnimation animation);

		MessageSettingsBuilder setBackdropColor(final String color);

		MessageSettingsBuilder setBackdropOpacity(final float opacity);

		MessageSettingsBuilder setCornerRadius(final float cornerRadius);

		MessageSettingsBuilder setGestures(final Map <MessageGesture, String> gestures);

		MessageSettings build();
	}
}
