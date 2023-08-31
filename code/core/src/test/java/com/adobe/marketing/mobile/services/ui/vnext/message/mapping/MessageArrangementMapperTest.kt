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
import org.junit.Test
import kotlin.test.assertEquals

class MessageArrangementMapperTest {

    @Test
    fun `Test #getVerticalArrangement with all MessageAlignments`() {
        assertEquals(Arrangement.Top, MessageArrangementMapper.getVerticalArrangement(InAppMessageSettings.MessageAlignment.TOP))
        assertEquals(Arrangement.Bottom, MessageArrangementMapper.getVerticalArrangement(InAppMessageSettings.MessageAlignment.BOTTOM))
        assertEquals(Arrangement.Center, MessageArrangementMapper.getVerticalArrangement(InAppMessageSettings.MessageAlignment.CENTER))
        assertEquals(Arrangement.Center, MessageArrangementMapper.getVerticalArrangement(InAppMessageSettings.MessageAlignment.LEFT))
        assertEquals(Arrangement.Center, MessageArrangementMapper.getVerticalArrangement(InAppMessageSettings.MessageAlignment.RIGHT))
    }

    @Test
    fun `Test #getHorizontalArrangement with all MessageAlignments`() {
        assertEquals(Arrangement.Start, MessageArrangementMapper.getHorizontalArrangement(InAppMessageSettings.MessageAlignment.LEFT))
        assertEquals(Arrangement.End, MessageArrangementMapper.getHorizontalArrangement(InAppMessageSettings.MessageAlignment.RIGHT))
        assertEquals(Arrangement.Center, MessageArrangementMapper.getHorizontalArrangement(InAppMessageSettings.MessageAlignment.CENTER))
        assertEquals(Arrangement.Center, MessageArrangementMapper.getHorizontalArrangement(InAppMessageSettings.MessageAlignment.TOP))
        assertEquals(Arrangement.Center, MessageArrangementMapper.getHorizontalArrangement(InAppMessageSettings.MessageAlignment.BOTTOM))
    }
}
