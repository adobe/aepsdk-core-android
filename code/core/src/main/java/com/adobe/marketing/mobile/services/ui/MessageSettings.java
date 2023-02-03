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
 * The class defines the layout and behavior of an in-app message.
 *
 * <p>These settings are customizable in a Messaging extension message: width: Width of the view in
 * which the message is displayed. Represented in percentage of the total screen width. height:
 * Height of the view in which the message is displayed. Represented in percentage of the total
 * screen height. verticalAlign: Defines the vertical alignment of the message. The alignments can
 * be seen in the MessageAlignment enum. horizontalAlign: Defines the horizontal alignment of the
 * message. The alignments can be seen in the MessageAlignment enum. verticalInset: Defines the
 * vertical inset respective to the verticalAlign. Represented in percentage of the total screen
 * height. horizontalInset: Defines the horizontal inset respective to the horizontalAlign.
 * Represented in percentage of the total screen width. uiTakeover: If true, a displayed message
 * will prevent the user from other UI interactions. displayAnimation: Defines the animation to be
 * used when the message is displayed. The animations can be seen in the MessageAnimation enum.
 * dismissAnimation: Defines the animation to be used when the message is dismissed. The animations
 * can be seen in the MessageAnimation enum. backdropColor: Defines the color of the backdrop shown
 * when a uiTakeover message is displayed. backdropOpacity: Defines the opacity of the backdrop
 * shown when a uiTakeover message is displayed. cornerRadius: Defines the angle to use when
 * rounding the message's webview. gestures: A mapping of gestures and their associated behaviors.
 * The gestures can be seen in the MessageGesture enum. The behavior string is handled by the
 * FullscreenMessageDelegate's overrideUrlLoad function.
 */
public class MessageSettings {

    /** Enum representing Message alignment. */
    public enum MessageAlignment {
        CENTER,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
    }

    /** Enum representing Message animations. */
    public enum MessageAnimation {
        NONE,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        CENTER,
        FADE,
    }

    /** Enum representing Message gestures. */
    public enum MessageGesture {
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

    private Object parent;
    private int width;
    private int height;
    private MessageAlignment verticalAlign;
    private MessageAlignment horizontalAlign;
    private int verticalInset;
    private int horizontalInset;
    private boolean uiTakeover;
    private MessageAnimation displayAnimation;
    private MessageAnimation dismissAnimation;
    private String backdropColor;
    private float backdropOpacity;
    private float cornerRadius;
    private Map<MessageGesture, String> gestures;

    /**
     * Returns the object that owns the message created using these settings.
     *
     * @return an {@code Object} which owns the {@link FullscreenMessage} created using this {@link
     *     MessageSettings} object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(final Object parent) {
        this.parent = parent;
    }

    /**
     * Returns the width of the view in which the message is displayed. Represented in percentage of
     * the total screen width.
     *
     * @return An {@code int} containing the percentage width of the view in which the message is
     *     displayed
     */
    public int getWidth() {
        return this.width;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    /**
     * Returns the height of the view in which the message is displayed. Represented in percentage
     * of the total screen height.
     *
     * @return An {@code int} containing the height of the view in which the message is displayed
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Sets the height of the view in which the message is displayed.
     *
     * @param height An {@code int} containing the height of the view in which the message is
     *     displayed
     */
    public void setHeight(final int height) {
        this.height = height;
    }

    /**
     * Returns the vertical alignment of the message. See {@link MessageAlignment}.
     *
     * @return A {@code MessageAlignment} value for the vertical alignment
     */
    public MessageAlignment getVerticalAlign() {
        return this.verticalAlign;
    }

    /**
     * Sets the vertical alignment of the message.
     *
     * @param verticalAlign A {@code MessageAlignment} value for the vertical alignment
     */
    public void setVerticalAlign(final MessageAlignment verticalAlign) {
        this.verticalAlign = verticalAlign;
    }

    /**
     * Returns the horizontal alignment of the message. See {@link MessageAlignment}.
     *
     * @return A {@code MessageAlignment} value for the horizontal alignment
     */
    public MessageAlignment getHorizontalAlign() {
        return this.horizontalAlign;
    }

    /**
     * Sets the horizontal alignment of the message. See {@link MessageAlignment}.
     *
     * @param horizontalAlign A {@code MessageAlignment} value for the horizontal alignment
     */
    public void setHorizontalAlign(final MessageAlignment horizontalAlign) {
        this.horizontalAlign = horizontalAlign;
    }

    /**
     * Returns the vertical inset respective to the vertical alignment. Represented in percentage of
     * the total screen height.
     *
     * @return An {@code int} containing the vertical inset percentage
     */
    public int getVerticalInset() {
        return this.verticalInset;
    }

    /**
     * Sets the vertical inset respective to the vertical alignment. Represented in percentage of
     * the total screen height.
     *
     * @param verticalInset An {@code int} containing the vertical inset percentage
     */
    public void setVerticalInset(final int verticalInset) {
        this.verticalInset = verticalInset;
    }

    /**
     * Returns the horizontal inset respective to the `horizontal alignment. Represented in
     * percentage of the total screen width.
     *
     * @return An {@code int} containing the horizontal inset percentage
     */
    public int getHorizontalInset() {
        return this.horizontalInset;
    }

    /**
     * Sets the horizontal inset respective to the `horizontal alignment. Represented in percentage
     * of the total screen width.
     *
     * @param horizontalInset An {@code int} containing the horizontal inset percentage
     */
    public void setHorizontalInset(final int horizontalInset) {
        this.horizontalInset = horizontalInset;
    }

    /**
     * Returns a boolean which if true, will prevent the user from other UI interactions if a
     * message is currently displayed.
     *
     * @return A {@code boolean} value which if true, will prevent the user from other UI
     *     interactions if a message is currently displayed.
     */
    public boolean getUITakeover() {
        return this.uiTakeover;
    }

    /**
     * Sets a boolean which if true, will prevent the user from other UI interactions if a message
     * is currently displayed.
     *
     * @param uiTakeover A {@code boolean} value which if true, will prevent the user from other UI
     *     interactions if a message is currently displayed.
     */
    public void setUiTakeover(final boolean uiTakeover) {
        this.uiTakeover = uiTakeover;
    }

    /**
     * Returns the animation to be used when the message is displayed. See {@link MessageAnimation}.
     *
     * @return A {@code MessageAnimation} value for the display animation
     */
    public MessageAnimation getDisplayAnimation() {
        return this.displayAnimation;
    }

    /**
     * Sets the animation to be used when the message is displayed. See {@link MessageAnimation}.
     *
     * @param displayAnimation A {@code MessageAnimation} value for the display animation
     */
    public void setDisplayAnimation(final MessageAnimation displayAnimation) {
        this.displayAnimation = displayAnimation;
    }

    /**
     * Returns the animation to be used when the message is dismissed. See {@link MessageAnimation}.
     *
     * @return A {@code MessageAnimation} value for the dismiss animation
     */
    public MessageAnimation getDismissAnimation() {
        return this.dismissAnimation;
    }

    /**
     * Sets the animation to be used when the message is dismissed. See {@link MessageAnimation}.
     *
     * @param dismissAnimation A {@code MessageAnimation} value for the dismiss animation
     */
    public void setDismissAnimation(final MessageAnimation dismissAnimation) {
        this.dismissAnimation = dismissAnimation;
    }

    /**
     * Returns the color of the backdrop shown when a uiTakeover message is displayed.
     *
     * @return A {@code String} containing the HTML hex string color code to use for the backdrop
     */
    public String getBackdropColor() {
        return this.backdropColor;
    }

    /**
     * Sets the color of the backdrop shown when a uiTakeover message is displayed.
     *
     * @param backdropColor A {@code String} containing the HTML hex string color code to use for
     *     the backdrop
     */
    public void setBackdropColor(final String backdropColor) {
        this.backdropColor = backdropColor;
    }

    /**
     * Returns the opacity of the backdrop shown when a uiTakeover message is displayed.
     *
     * @return A {@code float} containing the backdrop opacity percentage with 0.0 being fully
     *     transparent and 1.0 being fully opaque
     */
    public float getBackdropOpacity() {
        return this.backdropOpacity;
    }

    /**
     * Sets the opacity of the backdrop shown when a uiTakeover message is displayed.
     *
     * @param backdropOpacity A {@code float} containing the backdrop opacity percentage with 0.0
     *     being fully transparent and 1.0 being fully opaque
     */
    public void setBackdropOpacity(final float backdropOpacity) {
        this.backdropOpacity = backdropOpacity;
    }

    /**
     * Returns the angle to use when rounding the message's webview.
     *
     * @return A {@code float} containing the corner radius angle to use when rounding the message
     *     corners
     */
    public float getCornerRadius() {
        return this.cornerRadius;
    }

    /**
     * Sets the angle to use when rounding the message's webview.
     *
     * @param cornerRadius A {@code float} containing the corner radius angle to use when rounding
     *     the message corners
     */
    public void setCornerRadius(final float cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    /**
     * Returns a mapping of gestures and their associated behaviors.
     *
     * @return A {@code Map<MessageGesture, String} containing gestures and their associated
     *     behaviors
     */
    public Map<MessageGesture, String> getGestures() {
        return this.gestures;
    }

    /**
     * Sets a mapping of gestures and their associated behaviors.
     *
     * @param gestures A {@code Map<MessageGesture, String} containing gestures and their associated
     *     behaviors
     */
    public void setGestures(final Map<MessageGesture, String> gestures) {
        this.gestures = gestures;
    }
}
