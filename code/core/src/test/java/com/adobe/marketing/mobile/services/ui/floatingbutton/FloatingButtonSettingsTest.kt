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

package com.adobe.marketing.mobile.services.ui.floatingbutton

import android.graphics.Bitmap
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertNotNull
import kotlin.test.fail

class FloatingButtonSettingsTest {

    @Mock
    private lateinit var mockBitMap: Bitmap

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `Test that floating button throws when no graphic is provided`() {
        try {
            // setup & test
            val floatingButtonSettings = FloatingButtonSettings.Builder().build()
            fail("FloatingButtonSettings should throw when no graphic is provided")
        } catch (e: IllegalArgumentException) {
            // verify success
        }
    }

    @Test
    fun `Test that floating button applies default settings when no size settings are provided`() {
        // setup & test
        val floatingButtonSettings =
            FloatingButtonSettings.Builder().initialGraphic(mockBitMap).build()

        // verify
        assertEquals(56, floatingButtonSettings.height)
        assertEquals(56, floatingButtonSettings.width)
        assertEquals(5f, floatingButtonSettings.cornerRadius)
        assertNotNull(floatingButtonSettings.initialGraphic)
    }

    @Test
    fun `Test that floating button honors partial settings`() {
        // setup & test
        val floatingButtonSettings = FloatingButtonSettings.Builder()
            .height(100)
            .width(100)
            .initialGraphic(mockBitMap)
            .build()

        // verify
        assertEquals(100, floatingButtonSettings.height)
        assertEquals(100, floatingButtonSettings.width)
        assertEquals(5f, floatingButtonSettings.cornerRadius)
        assertEquals(mockBitMap, floatingButtonSettings.initialGraphic)
    }

    @Test
    fun `Test that floating button honors all settings`() {
        // setup & test
        val floatingButtonSettings = FloatingButtonSettings.Builder()
            .height(100)
            .width(100)
            .cornerRadius(10f)
            .initialGraphic(mockBitMap)
            .build()

        // verify
        assertEquals(100, floatingButtonSettings.height)
        assertEquals(100, floatingButtonSettings.width)
        assertEquals(10f, floatingButtonSettings.cornerRadius)
        assertNotNull(floatingButtonSettings.initialGraphic)
        floatingButtonSettings.initialGraphic.sameAs(mockBitMap)
    }
}
