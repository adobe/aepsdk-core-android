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

package com.adobe.marketing.mobile.services.ui.floatingbutton

import android.graphics.Bitmap
import java.lang.IllegalArgumentException

/**
 * Settings for the FloatingButton
 * @param height height of the button in dp
 * @param width width of the button in dp
 * @param initialGraphic initial graphic to be displayed on the button
 * @param cornerRadius corner radius of the button in dp
 */
class FloatingButtonSettings private constructor(
    val height: Int,
    val width: Int,
    val initialGraphic: Bitmap,
    val cornerRadius: Float
) {
    class Builder {
        private var height: Int = 56 // default height per material design spec
        private var width: Int = 56 // default width per material design spec
        private var initialGraphic: Bitmap? = null
        private var cornerRadius: Float = 5f

        /**
         * Sets the height of the floating button.
         * @param height height of the button in dp
         */
        fun height(height: Int) = apply { this.height = height }

        /**
         * Sets the width in of the floating button.
         * @param width width of the button in dp
         */
        fun width(width: Int) = apply { this.width = width }

        /**
         * Sets the corner radius of the floating button.
         * @param cornerRadius corner radius of the button in dp
         */
        fun cornerRadius(cornerRadius: Float) = apply { this.cornerRadius = cornerRadius }

        /**
         * Sets the initial graphic to be displayed on the floating button.
         * @param initialGraphic initial graphic to be displayed on the button
         */
        fun initialGraphic(initialGraphic: Bitmap) =
            apply { this.initialGraphic = initialGraphic }

        fun build() = run {
            initialGraphic?.let { graphic ->
                FloatingButtonSettings(height, width, graphic, cornerRadius)
            } ?: throw IllegalArgumentException("Initial graphic must be set")
        }
    }
}
