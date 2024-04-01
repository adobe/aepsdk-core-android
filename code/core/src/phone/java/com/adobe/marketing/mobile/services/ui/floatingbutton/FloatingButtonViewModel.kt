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

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * A simple view model for the FloatingButton screen. Maintains the state for the Floating Button and has
 * auxiliary methods for updating the state. Responsible for ensuring the state of the button is
 * maintained and updated across orientation changes.
 */
internal class FloatingButtonViewModel(settings: FloatingButtonSettings) {
    companion object {
        private const val LOG_TAG = "FloatingButtonViewModel"
    }

    // Current graphic as a state to be displayed on the floating button.
    // ImageBitmap should at least have a size of 1x1 to be created.
    private val _currentGraphic: MutableState<ImageBitmap> = mutableStateOf(settings.initialGraphic.asImageBitmap())
    internal val currentGraphic: State<ImageBitmap> = _currentGraphic

    // Offsets of the floating button in landscape and portrait mode
    internal var landscapeOffSet: Offset = Offset.Unspecified
    internal var portraitOffSet: Offset = Offset.Unspecified

    /**
     * Updates the current graphic of the floating button.
     * @param graphic the new content of the floating button
     */
    internal fun onGraphicUpdate(graphic: Bitmap) {
        _currentGraphic.value = graphic.asImageBitmap()
    }

    /**
     * Updates the current position of the floating button.
     * @param offset the new position of the floating button
     * @param orientation the orientation ([Configuration.ORIENTATION_LANDSCAPE] or [Configuration.ORIENTATION_PORTRAIT])
     *        of the device
     */
    internal fun onPositionUpdate(offset: Offset, orientation: Int) {
        if (offset.x < 0 || offset.y < 0) {
            return
        }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            landscapeOffSet = offset
        } else {
            portraitOffSet = offset
        }
    }
}
