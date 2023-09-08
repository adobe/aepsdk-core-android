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

package com.adobe.marketing.mobile.services.ui.vnext.alert

import junit.framework.TestCase.assertEquals
import org.junit.Test
import kotlin.test.fail

class AlertSettingsTest {
    companion object {
        private const val TITLE = "title"
        private const val MESSAGE = "message"
        private const val POSITIVE_BUTTON_TEXT = "positiveButtonText"
        private const val NEGATIVE_BUTTON_TEXT = "negativeButtonText"
    }

    @Test
    fun `Test AlertSettings with happy values`() {
        val alertSettings = AlertSettings.Builder()
            .title(TITLE)
            .message(MESSAGE)
            .positiveButtonText(POSITIVE_BUTTON_TEXT)
            .negativeButtonText(NEGATIVE_BUTTON_TEXT)
            .build()
        assertEquals(TITLE, alertSettings.title)
        assertEquals(MESSAGE, alertSettings.message)
        assertEquals(POSITIVE_BUTTON_TEXT, alertSettings.positiveButtonText)
        assertEquals(NEGATIVE_BUTTON_TEXT, alertSettings.negativeButtonText)
    }

    @Test
    fun `Test AlertSettings with no positive button text`() {
        val alertSettings = AlertSettings.Builder()
            .title(TITLE)
            .message(MESSAGE)
            .negativeButtonText(NEGATIVE_BUTTON_TEXT)
            .build()
        assertEquals(TITLE, alertSettings.title)
        assertEquals(MESSAGE, alertSettings.message)
        assertEquals(null, alertSettings.positiveButtonText)
        assertEquals(NEGATIVE_BUTTON_TEXT, alertSettings.negativeButtonText)
    }

    @Test
    fun `Test AlertSettings with no negative button text`() {
        val alertSettings = AlertSettings.Builder()
            .title(TITLE)
            .message(MESSAGE)
            .positiveButtonText(POSITIVE_BUTTON_TEXT)
            .build()
        assertEquals(TITLE, alertSettings.title)
        assertEquals(MESSAGE, alertSettings.message)
        assertEquals(POSITIVE_BUTTON_TEXT, alertSettings.positiveButtonText)
        assertEquals(null, alertSettings.negativeButtonText)
    }

    @Test
    fun `Test AlertSettings fails when neither buttons are provided`() {
        try {
            AlertSettings.Builder()
                .title(TITLE)
                .message(MESSAGE)
                .build()
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("At least one button must be defined.", e.message)
        }
    }
}
