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

import androidx.compose.ui.unit.dp
import com.adobe.marketing.mobile.services.ui.message.InAppMessageSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageOffsetMapperTest {

    @Test
    fun `Test #getHorizontalOffset with LEFT alignment and +ve offset`() {
        // setup
        val horizontalAlignment = InAppMessageSettings.MessageAlignment.LEFT
        val offsetPercent = 10
        val screenWidthDp = 100.dp

        // test
        val result = MessageOffsetMapper.getHorizontalOffset(horizontalAlignment, offsetPercent, screenWidthDp)

        // verify
        assertEquals(10.dp, result)
    }

    @Test
    fun `Test #getHorizontalOffset with LEFT alignment and -ve offset`() {
        // setup
        val horizontalAlignment = InAppMessageSettings.MessageAlignment.LEFT
        val offsetPercent = -10
        val screenWidthDp = 100.dp

        // test
        val result = MessageOffsetMapper.getHorizontalOffset(horizontalAlignment, offsetPercent, screenWidthDp)

        // verify
        assertEquals(-10.dp, result)
    }

    @Test
    fun `Test #getHorizontalOffset with RIGHT alignment and +ve offset`() {
        // setup
        val horizontalAlignment = InAppMessageSettings.MessageAlignment.RIGHT
        val offsetPercent = 10
        val screenWidthDp = 100.dp

        // test
        val result = MessageOffsetMapper.getHorizontalOffset(horizontalAlignment, offsetPercent, screenWidthDp)

        // verify
        assertEquals(-10.dp, result)
    }

    @Test
    fun `Test #getHorizontalOffset with RIGHT alignment and -ve offset`() {
        // setup
        val horizontalAlignment = InAppMessageSettings.MessageAlignment.RIGHT
        val offsetPercent = -10
        val screenWidthDp = 100.dp

        // test
        val result = MessageOffsetMapper.getHorizontalOffset(horizontalAlignment, offsetPercent, screenWidthDp)

        // verify
        assertEquals(10.dp, result)
    }

    @Test
    fun `Test #getHorizontalOffset with CENTER alignment`() {
        // setup
        val horizontalAlignment = InAppMessageSettings.MessageAlignment.CENTER
        val offsetPercent = 10
        val screenWidthDp = 100.dp

        // test
        val result = MessageOffsetMapper.getHorizontalOffset(horizontalAlignment, offsetPercent, screenWidthDp)

        // verify
        assertEquals(0.dp, result)
    }

    @Test
    fun `Test #getVerticalOffset with TOP alignment and +ve offset`() {
        // setup
        val verticalAlignment = InAppMessageSettings.MessageAlignment.TOP
        val offsetPercent = 10
        val screenHeightDp = 100.dp

        // test
        val result = MessageOffsetMapper.getVerticalOffset(verticalAlignment, offsetPercent, screenHeightDp)

        // verify
        assertEquals(10.dp, result)
    }

    @Test
    fun `Test #getVerticalOffset with TOP alignment and -ve offset`() {
        // setup
        val verticalAlignment = InAppMessageSettings.MessageAlignment.TOP
        val offsetPercent = -10
        val screenHeightDp = 100.dp

        // test
        val result = MessageOffsetMapper.getVerticalOffset(verticalAlignment, offsetPercent, screenHeightDp)

        // verify
        assertEquals(-10.dp, result)
    }

    @Test
    fun `Test #getVerticalOffset with BOTTOM alignment and +ve offset`() {
        // setup
        val verticalAlignment = InAppMessageSettings.MessageAlignment.BOTTOM
        val offsetPercent = 10
        val screenHeightDp = 100.dp

        // test
        val result = MessageOffsetMapper.getVerticalOffset(verticalAlignment, offsetPercent, screenHeightDp)

        // verify
        assertEquals(-10.dp, result)
    }

    @Test
    fun `Test #getVerticalOffset with BOTTOM alignment and -ve offset`() {
        // setup
        val verticalAlignment = InAppMessageSettings.MessageAlignment.BOTTOM
        val offsetPercent = -10
        val screenHeightDp = 100.dp

        // test
        val result = MessageOffsetMapper.getVerticalOffset(verticalAlignment, offsetPercent, screenHeightDp)

        // verify
        assertEquals(10.dp, result)
    }

    @Test
    fun `Test #getVerticalOffset with CENTER alignment`() {
        // setup
        val verticalAlignment = InAppMessageSettings.MessageAlignment.CENTER
        val offsetPercent = 10
        val screenHeightDp = 100.dp

        // test
        val result = MessageOffsetMapper.getVerticalOffset(verticalAlignment, offsetPercent, screenHeightDp)

        // verify
        assertEquals(0.dp, result)
    }

    @Test
    fun `Test #getVerticalOffset with invalid vertical alignment`() {
        // setup
        val verticalAlignment = InAppMessageSettings.MessageAlignment.RIGHT
        val offsetPercent = 10
        val screenHeightDp = 100.dp

        // test
        val result = MessageOffsetMapper.getVerticalOffset(verticalAlignment, offsetPercent, screenHeightDp)

        // verify
        assertEquals(0.dp, result)
    }

    @Test
    fun `Test #getHorizontalOffset with invalid horizontal alignment`() {
        // setup
        val horizontalAlignment = InAppMessageSettings.MessageAlignment.TOP
        val offsetPercent = 10
        val screenWidthDp = 100.dp

        // test
        val result = MessageOffsetMapper.getHorizontalOffset(horizontalAlignment, offsetPercent, screenWidthDp)

        // verify
        assertEquals(0.dp, result)
    }
}
