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

package com.adobe.marketing.mobile.services.ui.vnext.message.mapping

import androidx.compose.ui.Alignment
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageSettings

internal object MessageAlignmentMapper {

    private val verticalAlignmentMap: Map<InAppMessageSettings.MessageAlignment, Alignment.Vertical> =
        mapOf(
            InAppMessageSettings.MessageAlignment.TOP to Alignment.Top,
            InAppMessageSettings.MessageAlignment.BOTTOM to Alignment.Bottom,
            InAppMessageSettings.MessageAlignment.CENTER to Alignment.CenterVertically
            // Vertical alignment is not supported for LEFT and RIGHT

        )

    private val horizontalAlignmentMap: Map<InAppMessageSettings.MessageAlignment, Alignment.Horizontal> =
        mapOf(
            InAppMessageSettings.MessageAlignment.LEFT to Alignment.Start,
            InAppMessageSettings.MessageAlignment.RIGHT to Alignment.End,
            InAppMessageSettings.MessageAlignment.CENTER to Alignment.CenterHorizontally
            // Horizontal alignment is not supported for TOP and BOTTOM
        )

    internal fun getVerticalAlignment(alignment: InAppMessageSettings.MessageAlignment): Alignment.Vertical {
        return verticalAlignmentMap[alignment] ?: Alignment.CenterVertically
    }

    internal fun getHorizontalAlignment(alignment: InAppMessageSettings.MessageAlignment): Alignment.Horizontal {
        return horizontalAlignmentMap[alignment] ?: Alignment.CenterHorizontally
    }
}
