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

package com.adobe.marketing.mobile.services.ui.floatingbutton.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.adobe.marketing.mobile.services.ui.floatingbutton.FloatingButtonSettings
import kotlin.math.roundToInt

/**
 * Represents the floating button view.
 * @param settings the settings for the floating button
 * @param graphic the graphic to display on the floating button
 * @param offset the offset of the floating button from the top left corner of the screen
 * @param onClick the action to take when the floating button is clicked
 */
@Composable
internal fun FloatingButton(
    settings: FloatingButtonSettings,
    graphic: State<ImageBitmap>,
    offset: Offset = Offset.Unspecified,
    onClick: () -> Unit,
    onDragFinished: (Offset) -> Unit
) {
    // Floating button draggable area dimensions
    val heightDp = with(LocalConfiguration.current) { mutableStateOf(screenHeightDp.dp) }
    val widthDp = with(LocalConfiguration.current) { mutableStateOf(screenWidthDp.dp) }

    // Floating button dimensions
    val fbHeightDp: Dp = remember { settings.height.dp }
    val fbWidthDp: Dp = remember { settings.width.dp }
    val padding: Dp = remember { 4.dp }

    // Calculate initial offset of the floating button. The default offset is the top right corner
    // of the screen. If the offset is specified, then use that value instead.
    val widthPx = with(LocalDensity.current) { remember { widthDp.value.toPx() } }
    val fbWidthPx = with(LocalDensity.current) { remember { fbWidthDp.toPx() } }
    val paddingPx = with(LocalDensity.current) { remember { padding.toPx() } }
    val correctedOffset = remember {
        if (offset == Offset.Unspecified) {
            Offset(
                widthPx - fbWidthPx - paddingPx,
                0f
            )
        } else {
            offset
        }
    }

    // Tracks the offset of the floating button as a result of dragging
    val offsetState = remember { mutableStateOf(correctedOffset) }

    // The draggable area for the floating button
    Box(
        modifier = Modifier
            .height(heightDp.value)
            .width(widthDp.value)
            .testTag(FloatingButtonTestTags.FLOATING_BUTTON_AREA)
    ) {
        FloatingActionButton(
            modifier = Modifier
                .height(fbHeightDp)
                .width(fbWidthDp)
                .padding(padding)
                .wrapContentSize()
                .offset {
                    IntOffset(
                        offsetState.value.x.roundToInt(),
                        offsetState.value.y.roundToInt()
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            onDragFinished(offsetState.value)
                        }
                    ) { change, dragAmount ->
                        change.consumeAllChanges()
                        // Calculate new offset as a result of dragging
                        val newX = (offsetState.value.x + dragAmount.x)
                        val newY = (offsetState.value.y + dragAmount.y)

                        // Update the offset state with the new calculated offset while ensuring
                        // that the floating button stays within the draggable area.
                        // Offset values are in pixels, so we need to convert Dp to Px
                        offsetState.value = Offset(
                            newX.coerceIn(0f, (widthDp.value - fbWidthDp).toPx()),
                            newY.coerceIn(0f, (heightDp.value - fbHeightDp).toPx())
                        )
                    }
                }
                .testTag(FloatingButtonTestTags.FLOATING_BUTTON),
            // Remove the default elevation and background color of the floating button to ensure
            // that the floating button graphic is the only thing that is displayed without any
            // additional white background or shadow
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
            onClick = { onClick() },
            shape = RoundedCornerShape(settings.cornerRadius.dp),
            backgroundColor = Color.Transparent
        ) {
            // Represents the floating button graphic
            Image(
                bitmap = graphic.value,
                contentDescription = "Floating Button",
                modifier = Modifier
                    .background(Color.Transparent)
                    .wrapContentSize()
                    .testTag(FloatingButtonTestTags.FLOATING_BUTTON_GRAPHIC)
            )
        }
    }
}
