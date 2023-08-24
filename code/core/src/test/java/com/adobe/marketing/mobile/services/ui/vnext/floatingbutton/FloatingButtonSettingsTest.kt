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

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.fail

class FloatingButtonSettingsTest {
    @Test
    fun `Test that floating button applies default settings when no settings are provided`() {
        // setup & test
        val floatingButtonSettings = FloatingButtonSettings.Builder().build()

        // test
        assertEquals(56, floatingButtonSettings.height)
        assertEquals(56, floatingButtonSettings.width)
        assertEquals(5f, floatingButtonSettings.cornerRadius)
        assertNull(floatingButtonSettings.initialGraphic)
    }

    @Test
    fun `Test that floating button honors partial settings`() {
        // setup & test
        val floatingButtonSettings = FloatingButtonSettings.Builder()
            .height(100)
            .width(100)
            .build()

        // verify
        assertEquals(100, floatingButtonSettings.height)
        assertEquals(100, floatingButtonSettings.width)
        assertEquals(5f, floatingButtonSettings.cornerRadius)
        assertNull(floatingButtonSettings.initialGraphic)
    }

    @Test
    fun `Test that floating button honors all settings`() {
        val graphicBase64 = this.javaClass.classLoader?.getResource("uiservice_fab_unit_tests/floatingButtonTestGraphic.txt")?.readText()
            ?: fail("Failed to load test graphic")

        // setup & test
        val floatingButtonSettings = FloatingButtonSettings.Builder()
            .height(100)
            .width(100)
            .cornerRadius(10f)
            .initialGraphic(graphicBase64.byteInputStream())
            .build()

        // verify
        assertEquals(100, floatingButtonSettings.height)
        assertEquals(100, floatingButtonSettings.width)
        assertEquals(10f, floatingButtonSettings.cornerRadius)
        assertNotNull(floatingButtonSettings.initialGraphic)
        floatingButtonSettings.initialGraphic?.let {
            assertEquals(graphicBase64, String(it.readBytes()))
        }
    }
}
