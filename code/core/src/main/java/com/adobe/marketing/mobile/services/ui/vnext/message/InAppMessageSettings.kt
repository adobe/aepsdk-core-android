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

package com.adobe.marketing.mobile.services.ui.vnext.message

class InAppMessageSettings private constructor(
    val content: String,
    val width: Int,
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
    val assetMap: Map<String, String>

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
        BACKGROUND_TAP("backgroundTap");

        companion object {
            private val gestureToEnumMap: Map<String, MessageGesture> =
                values().associateBy { it.gestureName }

            fun forName(gestureName: String): MessageGesture? {
                return gestureToEnumMap[gestureName]
            }
        }
    }

    class Builder {
        private var content: String = ""
        private var width: Int = 100
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

        fun content(content: String) = apply { this.content = content }

        fun width(width: Int) = apply { this.width = width }

        fun height(height: Int) = apply { this.height = height }

        fun verticalInset(verticalInset: Int) = apply { this.verticalInset = verticalInset }

        fun horizontalInset(horizontalInset: Int) = apply { this.horizontalInset = horizontalInset }

        fun verticalAlignment(verticalAlignment: MessageAlignment) =
            apply { this.verticalAlignment = verticalAlignment }

        fun horizontalAlignment(horizontalAlignment: MessageAlignment) =
            apply { this.horizontalAlignment = horizontalAlignment }

        fun displayAnimation(displayAnimation: MessageAnimation) =
            apply { this.displayAnimation = displayAnimation }

        fun dismissAnimation(dismissAnimation: MessageAnimation) =
            apply { this.dismissAnimation = dismissAnimation }

        fun backgroundColor(backgroundColor: String) =
            apply { this.backgroundColor = backgroundColor }

        fun backdropOpacity(backdropOpacity: Float) =
            apply { this.backdropOpacity = backdropOpacity }

        fun cornerRadius(cornerRadius: Float) = apply { this.cornerRadius = cornerRadius }

        fun shouldTakeOverUi(shouldTakeOverUi: Boolean) =
            apply { this.shouldTakeOverUi = shouldTakeOverUi }

        fun assetMap(assetMap: MutableMap<String, String>) =
            apply { this.assetMap = assetMap }

        fun build() = InAppMessageSettings(
            content,
            width,
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
            assetMap
        )
    }
}
