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

package com.adobe.marketing.mobile.services.ui.vnext.floatingbutton

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 * A simple view model for the FloatingButton screen. Maintains the state for the Floating Button and has
 * auxiliary methods for updating the state. Responsible for ensuring the state of the button is
 * maintained and updated across orientation changes.
 */
internal class FloatingButtonViewModel(private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
    companion object {
        private const val LOG_TAG = "FloatingButtonViewModel"
        internal val NO_GRAPHIC = ImageBitmap(1, 1)
    }

    // Current graphic as a state to be displayed on the floating button.
    // ImageBitmap should at least have a size of 1x1 to be created.
    private val _currentGraphic: MutableState<ImageBitmap> = mutableStateOf(NO_GRAPHIC)
    internal val currentGraphic: State<ImageBitmap> = _currentGraphic

    // Offsets of the floating button in landscape and portrait mode
    internal var landscapeOffSet: Offset = Offset.Unspecified
    internal var portraitOffSet: Offset = Offset.Unspecified

    /**
     * Updates the current graphic of the floating button.
     * @param graphic the new content of the floating button
     */
    internal fun onGraphicUpdate(graphic: InputStream) {
        coroutineScope.launch {
            try {
                val graphicString = readAsString(graphic)

                if (graphicString.isNullOrBlank()) {
                    Log.debug(ServiceConstants.LOG_TAG, LOG_TAG, "Failed to update graphic. Graphic is null or blank.")
                    return@launch
                }

                val backgroundImage = Base64.decode(graphicString, Base64.DEFAULT) ?: kotlin.run {
                    Log.debug(ServiceConstants.LOG_TAG, LOG_TAG, "Failed to update graphic. Cannot decode graphic as Base64.")
                    return@launch
                }

                val bitmap: Bitmap = BitmapFactory.decodeStream(backgroundImage.inputStream()) ?: kotlin.run {
                    Log.debug(ServiceConstants.LOG_TAG, LOG_TAG, "Failed to update graphic. Cannot decode graphic stream.")
                    return@launch
                }

                _currentGraphic.value = bitmap.asImageBitmap()
            } catch (e: Exception) {
                Log.debug(ServiceConstants.LOG_TAG, LOG_TAG, "Failed to update floating button graphic.", e)
            }
        }
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

    /**
     * Reads the contents of `InputStream` as String
     *
     * @param inputStream [InputStream] to read
     * @return [String] representation of the input stream
     */
    private fun readAsString(inputStream: InputStream?): String? {
        if (inputStream == null) {
            return null
        }
        val buffer = ByteArrayOutputStream()
        val data = ByteArray(1024)
        var bytesRead: Int
        return try {
            while (inputStream.read(data, 0, data.size).also { bytesRead = it } != -1) {
                buffer.write(data, 0, bytesRead)
            }
            val byteArray = buffer.toByteArray()
            String(byteArray, StandardCharsets.UTF_8)
        } catch (ex: IOException) {
            Log.debug(ServiceConstants.LOG_TAG, LOG_TAG, "Error reading input stream as string.", ex)
            null
        } finally {
            try {
                inputStream.close()
                buffer.close()
            } catch (ex: IOException) {
                Log.debug(ServiceConstants.LOG_TAG, LOG_TAG, "Error closing buffer.", ex)
            }
        }
    }
}
