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

import java.util.Map;

/**
 * The Android implementation of {@link MessageSettings}.
 * The {@link AEPMessageSettings} define the layout and behavior of an in-app message. The settings also define how a user can
 * interact with the {@link AEPMessage}.
 * <p>
 * The {@code AEPMessageSettings} uses a builder pattern for construction. The parent object can only be set during initialization.
 */
class AEPMessageSettings implements MessageSettings {
    private final Object parent;
    private final int width;
    private final int height;
    private final MessageAlignment verticalAlign;
    private final MessageAlignment horizontalAlign;
    private final int verticalInset;
    private final int horizontalInset;
    private final boolean uiTakeover;
    private final MessageAnimation displayAnimation;
    private MessageAnimation dismissAnimation;
    private final String backdropColor;
    private final float backdropOpacity;
    private final float cornerRadius;
    private final Map<MessageGesture, String> gestures;

    private AEPMessageSettings(final Builder builder) {
        this.parent = builder.parent;
        this.width = builder.width;
        this.height = builder.height;
        this.verticalAlign = builder.verticalAlign;
        this.horizontalAlign = builder.horizontalAlign;
        this.verticalInset = builder.verticalInset;
        this.horizontalInset = builder.horizontalInset;
        this.uiTakeover = builder.uiTakeover;
        this.displayAnimation = builder.displayAnimation;
        this.dismissAnimation = builder.dismissAnimation;
        this.backdropColor = builder.backdropColor;
        this.backdropOpacity = builder.backdropOpacity;
        this.cornerRadius = builder.cornerRadius;
        this.gestures = builder.gestures;
    }

    @Override
    public Object getParent() {
        return parent;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public MessageAlignment getVerticalAlign() {
        return verticalAlign;
    }

    @Override
    public MessageAlignment getHorizontalAlign() {
        return horizontalAlign;
    }

    @Override
    public int getVerticalInset() {
        return verticalInset;
    }

    @Override
    public int getHorizontalInset() {
        return horizontalInset;
    }

    @Override
    public boolean getUITakeover() {
        return uiTakeover;
    }

    @Override
    public MessageAnimation getDisplayAnimation() {
        return displayAnimation;
    }

    @Override
    public MessageAnimation getDismissAnimation() {
        return dismissAnimation;
    }

    @Override
    public void setDismissAnimation(MessageAnimation dismissAnimation) {
        this.dismissAnimation = dismissAnimation;
    }

    @Override
    public String getBackdropColor() {
        return backdropColor;
    }

    @Override
    public float getBackdropOpacity() {
        return backdropOpacity;
    }

    @Override
    public float getCornerRadius() {
        return cornerRadius;
    }

    @Override
    public Map<MessageGesture, String> getGestures() {
        return gestures;
    }

    /**
     * The builder responsible for creating {@link AEPMessageSettings} objects.
     */
    public static class Builder implements MessageSettings.MessageSettingsBuilder {
        private final Object parent;
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
         * Constructor.
         *
         * @param parent The {@code Object} that owns the message created using these settings.
         */
        public Builder(final Object parent) {
            this.parent = parent;
        }

        /**
         * Sets the width of the message.
         * <p>
         * The width of the view in which the message is displayed. Represented in percentage of the total screen width.
         *
         * @param width {@code int} the value of the message's width
         */
        @Override
        public Builder setWidth(final int width) {
            this.width = width;
            return this;
        }

        /**
         * Sets the height of the message.
         * <p>
         * The height of the view in which the message is displayed. Represented in percentage of the total screen height.
         *
         * @param height {@code int} the value of the message's height
         */
        @Override
        public Builder setHeight(final int height) {
            this.height = height;
            return this;
        }

        /**
         * Sets the vertical alignment of the message.
         * <p>
         * See the {@link MessageAlignment} enum for the possible alignment values.
         *
         * @param verticalAlign {@code MessageAlignment} the enum value
         */
        @Override
        public Builder setVerticalAlign(final MessageAlignment verticalAlign) {
            this.verticalAlign = verticalAlign;
            return this;
        }

        /**
         * Sets the horizontal alignment of the message.
         * <p>
         * See the {@link MessageAlignment} enum for the possible alignment values.
         *
         * @param horizontalAlign {@code MessageAlignment} the enum value
         */
        @Override
        public Builder setHorizontalAlign(final MessageAlignment horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            return this;
        }

        /**
         * Sets the vertical inset of the message.
         * <p>
         * Defines the vertical inset respective to the verticalAlign. Represented in percentage of the total screen height.
         *
         * @param verticalInset {@code int} the value of the message's vertical inset
         */
        @Override
        public Builder setVerticalInset(final int verticalInset) {
            this.verticalInset = verticalInset;
            return this;
        }

        /**
         * Sets the horizontal inset of the message.
         * <p>
         * Defines the horizontal inset respective to the horizontalAlign. Represented in percentage of the total screen width.
         *
         * @param horizontalInset {@code int} the value of the message's horizontal inset
         */
        @Override
        public Builder setHorizontalInset(final int horizontalInset) {
            this.horizontalInset = horizontalInset;
            return this;
        }

        /**
         * Sets the UI takeover status of the message.
         * <p>
         * If true, a displayed message will prevent the user from performing other UI interactions.
         *
         * @param uiTakeover {@code boolean} the value of the message's ui takeover status
         */
        @Override
        public Builder setUiTakeover(final boolean uiTakeover) {
            this.uiTakeover = uiTakeover;
            return this;
        }

        /**
         * Sets the display animation of the message.
         * <p>
         * See the {@link MessageAnimation} enum for the possible animation values.
         *
         * @param animation {@code MessageAnimation} the enum value
         */
        @Override
        public Builder setDisplayAnimation(final MessageAnimation animation) {
            this.displayAnimation = animation;
            return this;
        }

        /**
         * Sets the dismiss animation of the message.
         * <p>
         * See the {@link MessageAnimation} enum for the possible animation values.
         *
         * @param animation {@code MessageAnimation} enum value
         */
        @Override
        public Builder setDismissAnimation(final MessageAnimation animation) {
            this.dismissAnimation = animation;
            return this;
        }

        /**
         * Sets the backdrop color of the message.
         * <p>
         * Defines the color of the backdrop shown when a UI takeover message is displayed.
         *
         * @param color {@code String} the HTML color code value
         */
        @Override
        public Builder setBackdropColor(final String color) {
            this.backdropColor = color;
            return this;
        }

        /**
         * Sets the backdrop opacity of the message.
         * <p>
         * Defines the opacity of the backdrop shown when a UI takeover message is displayed.
         * The value ranges from 0.0 (invisible) to 1.0 (completely opaque).
         *
         * @param opacity {@code float} the value of the opacity
         */
        @Override
        public Builder setBackdropOpacity(final float opacity) {
            this.backdropOpacity = opacity;
            return this;
        }

        /**
         * Sets the corner radius angle of the message.
         * <p>
         * Defines the angle to use when rounding the message's webview.
         *
         * @param cornerRadius {@code float} the value of the angle
         */
        @Override
        public Builder setCornerRadius(final float cornerRadius) {
            this.cornerRadius = cornerRadius;
            return this;
        }

        /**
         * Sets the {@link MessageGesture} map containing key value pairs of gestures and behaviors.
         *
         * @param gestures A {@code Map<MessageGesture, String>} of gestures and their associated behaviors. The gestures can be seen in the {@link MessageGesture} enum.
         *                 The behavior string is handled by the {@link FullscreenMessageDelegate#overrideUrlLoad} function.
         */
        @Override
        public Builder setGestures(Map<MessageGesture, String> gestures) {
            this.gestures = gestures;
            return this;
        }

        /**
         * Builds the {@link AEPMessageSettings} object.
         *
         * @return a {@code AEPMessageSettings} object
         */
        @Override
        public AEPMessageSettings build() {
            return new AEPMessageSettings(this);
        }
    }
}
