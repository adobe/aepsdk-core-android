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

import androidx.compose.foundation.layout.Arrangement
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageSettings

internal object MessageArrangementMapper {

    private val horizontalArrangementMap: Map<InAppMessageSettings.MessageAlignment, Arrangement.Horizontal> = mapOf(
        InAppMessageSettings.MessageAlignment.LEFT to Arrangement.Start,
        InAppMessageSettings.MessageAlignment.RIGHT to Arrangement.End,
        InAppMessageSettings.MessageAlignment.CENTER to Arrangement.Center
        // Horizontal alignment is not supported for TOP and BOTTOM
    )

    private val verticalArrangementMap: Map<InAppMessageSettings.MessageAlignment, Arrangement.Vertical> = mapOf(
        InAppMessageSettings.MessageAlignment.TOP to Arrangement.Top,
        InAppMessageSettings.MessageAlignment.BOTTOM to Arrangement.Bottom,
        InAppMessageSettings.MessageAlignment.CENTER to Arrangement.Center
        // Vertical alignment is not supported for LEFT and RIGHT
    )

    internal fun getHorizontalArrangement(alignment: InAppMessageSettings.MessageAlignment): Arrangement.Horizontal {
        return horizontalArrangementMap[alignment] ?: Arrangement.Center
    }

    internal fun getVerticalArrangement(alignment: InAppMessageSettings.MessageAlignment): Arrangement.Vertical {
        return verticalArrangementMap[alignment] ?: Arrangement.Center
    }
}
