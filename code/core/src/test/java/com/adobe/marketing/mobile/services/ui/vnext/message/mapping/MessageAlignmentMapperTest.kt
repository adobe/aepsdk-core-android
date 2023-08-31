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
import org.junit.Test
import kotlin.test.assertEquals

class MessageAlignmentMapperTest {

    @Test
    fun `Test #getVerticalAlignment with all MessageAlignments`() {
        assertEquals(Alignment.Top, MessageAlignmentMapper.getVerticalAlignment(InAppMessageSettings.MessageAlignment.TOP))
        assertEquals(Alignment.Bottom, MessageAlignmentMapper.getVerticalAlignment(InAppMessageSettings.MessageAlignment.BOTTOM))
        assertEquals(Alignment.CenterVertically, MessageAlignmentMapper.getVerticalAlignment(InAppMessageSettings.MessageAlignment.CENTER))
        assertEquals(Alignment.CenterVertically, MessageAlignmentMapper.getVerticalAlignment(InAppMessageSettings.MessageAlignment.LEFT))
        assertEquals(Alignment.CenterVertically, MessageAlignmentMapper.getVerticalAlignment(InAppMessageSettings.MessageAlignment.RIGHT))
    }

    @Test
    fun `Test #getHorizontalAlignment with all MessageAlignments`() {
        assertEquals(Alignment.Start, MessageAlignmentMapper.getHorizontalAlignment(InAppMessageSettings.MessageAlignment.LEFT))
        assertEquals(Alignment.End, MessageAlignmentMapper.getHorizontalAlignment(InAppMessageSettings.MessageAlignment.RIGHT))
        assertEquals(Alignment.CenterHorizontally, MessageAlignmentMapper.getHorizontalAlignment(InAppMessageSettings.MessageAlignment.CENTER))
        assertEquals(Alignment.CenterHorizontally, MessageAlignmentMapper.getHorizontalAlignment(InAppMessageSettings.MessageAlignment.TOP))
        assertEquals(Alignment.CenterHorizontally, MessageAlignmentMapper.getHorizontalAlignment(InAppMessageSettings.MessageAlignment.BOTTOM))
    }
}
