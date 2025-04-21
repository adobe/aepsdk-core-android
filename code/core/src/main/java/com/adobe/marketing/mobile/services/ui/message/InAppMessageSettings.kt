/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services.ui.message

/**
 * An immutable class that holds the settings and configuration for the InAppMessage.
 *
 * @param content the HTML content for the message
 * @param width the width of the message as a percentage of the screen width
 * @param maxWidth the maximum width of the message displayed in pixels
 * @param height the height of the message as a percentage of the screen height
 * @param verticalInset the vertical inset of the message. This is the padding from the top and bottom of the screen
 * expressed as a percentage of the screen height
 * @param horizontalInset the horizontal inset of the message. This is the padding from the left and right of the screen
 * expressed as a percentage of the screen width
 * @param verticalAlignment the vertical alignment of the message on the screen
 * @param horizontalAlignment the horizontal alignment of the message on the screen
 * @param displayAnimation the animation to use when displaying the message
 * @param dismissAnimation the animation to use when dismissing the message
 * @param backdropColor the color of the backdrop behind the message. This the color behind the message when the message is taking over the UI.
 * @param backdropOpacity the opacity of the backdrop behind the message. This the opacity behind the message when the message is taking over the UI.
 * @param cornerRadius the corner radius of the message
 * @param shouldTakeOverUi whether interactions with the elements outside the message should be disabled.
 * Should be set to true if the message should take over the UI, false otherwise.
 * @param assetMap a map of asset names to asset URLs
 * @param gestureMap a map of gestures to the names of the actions to be performed when the gesture is detected
 */
class InAppMessageSettings private constructor(
    val content: String,
    val width: Int,
    val maxWidth: Int,
    val height: Int,
    val verticalInset: Int,
    val horizontalInset: Int,
    val verticalAlignment: MessageAlignment,
    val horizontalAlignment: MessageAlignment,
    val displayAnimation: MessageAnimation,
    val dismissAnimation: MessageAnimation,
    val backdropColor: String,
    val backdropOpacity: Float,
    val cornerRadius: Float,
    val shouldTakeOverUi: Boolean,
    val assetMap: Map<String, String>,
    val gestureMap: Map<MessageGesture, String>,
    val fitToContent: Boolean
) {
    /** Enum representing Message alignment.  */
    enum class MessageAlignment {
        CENTER, LEFT, RIGHT, TOP, BOTTOM
    }

    /** Enum representing Message animations.  */
    enum class MessageAnimation {
        NONE, LEFT, RIGHT, TOP, BOTTOM, CENTER, FADE
    }

    enum class MessageGesture(private val gestureName: String) {
        SWIPE_UP("swipeUp"),
        SWIPE_DOWN("swipeDown"),
        SWIPE_LEFT("swipeLeft"),
        SWIPE_RIGHT("swipeRight"),
        TAP_BACKGROUND("tapBackground");

        internal companion object {
            private val gestureToEnumMap: Map<String, MessageGesture> =
                values().associateBy { it.gestureName }

            internal fun getGestureForName(gestureName: String): MessageGesture? =
                gestureToEnumMap[gestureName]
        }
    }

    /**
     * Builder class for [InAppMessageSettings].
     */
    class Builder {
        private var content: String = ""
        private var width: Int = 100
        // If no maxWidth is provided, use Int.MAX_VALUE to ensure its value will not be used
        // because messageWidth = min(contentViewWidth * width / 100, maxWidth)
        private var maxWidth: Int = Int.MAX_VALUE
        private var height: Int = 100
        private var verticalInset: Int = 0
        private var horizontalInset: Int = 0
        private var verticalAlignment: MessageAlignment = MessageAlignment.CENTER
        private var horizontalAlignment: MessageAlignment = MessageAlignment.CENTER
        private var displayAnimation: MessageAnimation = MessageAnimation.NONE
        private var dismissAnimation: MessageAnimation = MessageAnimation.NONE
        private var backgroundColor: String = "#000000"
        private var backdropOpacity: Float = 0.0f
        private var cornerRadius: Float = 0.0f
        private var shouldTakeOverUi: Boolean = false
        private var assetMap: MutableMap<String, String> = mutableMapOf()
        private var gestures: MutableMap<MessageGesture, String> = mutableMapOf()
        private var fitToContent: Boolean = false

        /**
         * Sets the HTML content for the message.
         * @param content the HTML content for the message
         */
        fun content(content: String) = apply { this.content = content }

        /**
         * Sets the width of the message as a percentage of the screen width.
         * @param width the width of the message as a percentage of the screen width
         */
        fun width(width: Int) = apply { this.width = clipToPercent(width) }

        /**
         * Sets the maximum width of the message displayed in pixels.
         * @param maxWidth the maximum width of the message in pixels
         */
        fun maxWidth(maxWidth: Int) = apply { this.maxWidth = maxWidth }

        /**
         * Sets the height of the message as a percentage of the screen height.
         * @param height the height of the message as a percentage of the screen height
         */
        fun height(height: Int) = apply { this.height = clipToPercent(height) }

        /**
         * Sets the vertical inset of the message. This is the padding from the top and bottom
         * of the screen as a percentage of the screen height.
         * @param verticalInset the vertical inset of the message
         */
        fun verticalInset(verticalInset: Int) = apply { this.verticalInset = clipToPercent(verticalInset, true) }

        /**
         * Sets the horizontal inset of the message. This is the padding from the left and right
         * of the screen as a percentage of the screen width.
         */
        fun horizontalInset(horizontalInset: Int) = apply { this.horizontalInset = clipToPercent(horizontalInset, true) }

        /**
         * Sets the vertical alignment of the message on the screen.
         * @param verticalAlignment the vertical [MessageAlignment] of the message on the screen
         */
        fun verticalAlignment(verticalAlignment: MessageAlignment) =
            apply { this.verticalAlignment = verticalAlignment }

        /**
         * Sets the horizontal alignment of the message on the screen.
         * @param horizontalAlignment the horizontal [MessageAlignment] of the message on the screen
         */
        fun horizontalAlignment(horizontalAlignment: MessageAlignment) =
            apply { this.horizontalAlignment = horizontalAlignment }

        /**
         * Sets the animation to use when displaying the message.
         * @param displayAnimation the [MessageAnimation] to use when displaying the message
         */
        fun displayAnimation(displayAnimation: MessageAnimation) =
            apply { this.displayAnimation = displayAnimation }

        /**
         * Sets the animation to use when dismissing the message.
         * @param dismissAnimation the [MessageAnimation] to use when dismissing the message
         */
        fun dismissAnimation(dismissAnimation: MessageAnimation) =
            apply { this.dismissAnimation = dismissAnimation }

        /**
         * Sets the color of the backdrop behind the message. This is the color behind the message when the message is taking over the UI.
         * @param backgroundColor the hex color of the backdrop behind the message.
         */
        fun backgroundColor(backgroundColor: String) =
            apply { this.backgroundColor = backgroundColor }

        /**
         * Sets the opacity of the backdrop behind the message. This is the opacity behind the message when the message is taking over the UI.
         * @param backdropOpacity the opacity of the backdrop behind the message. This should be a value between 0.0f (transparent) and 1.0f(opaque).
         */
        fun backdropOpacity(backdropOpacity: Float) =
            apply { this.backdropOpacity = backdropOpacity }

        /**
         * Sets the corner radius of the message.
         * @param cornerRadius the corner radius of the message
         */
        fun cornerRadius(cornerRadius: Float) = apply { this.cornerRadius = cornerRadius }

        /**
         * Configures whether the message should take over the UI.
         * @param shouldTakeOverUi whether the message should take over the UI. Should be set to true if the message should take over the UI, false otherwise.
         */
        fun shouldTakeOverUi(shouldTakeOverUi: Boolean) =
            apply { this.shouldTakeOverUi = shouldTakeOverUi }

        /**
         * Configures whether the message set to fit to content.
         * @param fitToContent whether the message should fit to the content. Should be set to true if the message should fit to content, false otherwise.
         */
        fun shouldFitToContent(fitToContent: Boolean) =
            apply { this.fitToContent = fitToContent }

        /**
         * Sets the asset map for the message. This is a map of asset names to asset URLs.
         * @param assetMap the asset map for the message
         */
        fun assetMap(assetMap: Map<String, String>) =
            apply { this.assetMap = assetMap.toMutableMap() }

        /**
         * Sets the gesture map for the message. This is a map of gesture names
         * (as defined by [InAppMessageSettings.MessageGesture]'s) to gesture actions.
         * @param gestureMap the gesture map for the message
         */
        fun gestureMap(gestureMap: Map<String, String>) =
            apply {
                for ((key, value) in gestureMap) {
                    val gesture: MessageGesture? = MessageGesture.getGestureForName(key)
                    gesture?.let {
                        this@Builder.gestures[gesture] = value
                    }
                }
            }

        fun build() = InAppMessageSettings(
            content,
            width,
            maxWidth,
            height,
            verticalInset,
            horizontalInset,
            verticalAlignment,
            horizontalAlignment,
            displayAnimation,
            dismissAnimation,
            backgroundColor,
            backdropOpacity,
            cornerRadius,
            shouldTakeOverUi,
            assetMap,
            gestures,
            fitToContent
        )

        private companion object {
            /**
             * Clips a value to a percentage. This is used to ensure that values are between -100 and 100
             * when negative values are allowed and between 0 and 100 when negative values are not allowed.
             * @param toClip the value to clip
             * @param allowNegative whether negative values are allowed
             * @return the clipped value
             */
            fun clipToPercent(toClip: Int, allowNegative: Boolean = false) = when {
                !allowNegative && toClip <= 0 -> 0
                toClip <= -100 -> -100
                toClip >= 100 -> 100
                else -> toClip
            }
        }
    }
}
