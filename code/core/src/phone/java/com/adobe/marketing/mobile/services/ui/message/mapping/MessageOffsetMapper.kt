/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services.ui.message.mapping

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.adobe.marketing.mobile.services.ui.message.InAppMessageSettings

/**
 * A mapper to calculate the horizontal and vertical offset for the InAppMessage based
 * on the alignment and offset percent.
 */
internal object MessageOffsetMapper {

    /**
     * Returns the horizontal offset for the InAppMessage based on the alignment and offset percent
     * relative to the screen width.
     *
     * LEFT alignment with +ve offset will move the message to the right.
     * LEFT alignment with -ve offset will move the message to the left.
     * RIGHT alignment with +ve offset will move the message to the left.
     * RIGHT alignment with -ve offset will move the message to the right.
     *
     * @param horizontalAlignment the horizontal alignment of the message
     * @param offsetPercent the offset percent of the message
     * @param screenWidthDp the screen width in dp
     * @return the horizontal offset in dp
     */
    internal fun getHorizontalOffset(
        horizontalAlignment: InAppMessageSettings.MessageAlignment,
        offsetPercent: Int,
        screenWidthDp: Dp
    ): Dp {
        val offset = ((offsetPercent * screenWidthDp.value) / 100).dp

        return when (horizontalAlignment) {
            InAppMessageSettings.MessageAlignment.LEFT -> offset
            InAppMessageSettings.MessageAlignment.RIGHT -> -offset
            InAppMessageSettings.MessageAlignment.CENTER -> 0.dp
            else -> 0.dp
        }
    }

    /**
     * Returns the vertical offset for the InAppMessage based on the alignment and offset percent
     * relative to the screen height.
     *
     * TOP alignment with +ve offset will move the message down.
     * TOP alignment with -ve offset will move the message up.
     * BOTTOM alignment with +ve offset will move the message up.
     * BOTTOM alignment with -ve offset will move the message down.
     *
     * @param verticalAlignment the vertical alignment of the message
     * @param offsetPercent the offset percent of the message
     * @param screenHeightDp the screen height in dp
     * @return the vertical offset in dp
     */
    internal fun getVerticalOffset(
        verticalAlignment: InAppMessageSettings.MessageAlignment,
        offsetPercent: Int,
        screenHeightDp: Dp
    ): Dp {
        val offset = ((offsetPercent * screenHeightDp.value) / 100).dp

        return when (verticalAlignment) {
            InAppMessageSettings.MessageAlignment.TOP -> offset
            InAppMessageSettings.MessageAlignment.BOTTOM -> -offset
            InAppMessageSettings.MessageAlignment.CENTER -> 0.dp
            else -> 0.dp
        }
    }
}
