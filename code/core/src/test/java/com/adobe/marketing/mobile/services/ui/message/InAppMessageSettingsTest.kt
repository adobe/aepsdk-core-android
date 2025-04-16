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

package com.adobe.marketing.mobile.services.ui.message

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InAppMessageSettingsTest {

    @Test
    fun `Test InAppMessageSettings with default params`() {
        val iamSettings = InAppMessageSettings.Builder().build()
        assertEquals("", iamSettings.content)
        assertEquals(100, iamSettings.width)
        assertEquals(100, iamSettings.height)
        assertEquals(0, iamSettings.verticalInset)
        assertEquals(0, iamSettings.horizontalInset)
        assertEquals(InAppMessageSettings.MessageAlignment.CENTER, iamSettings.verticalAlignment)
        assertEquals(InAppMessageSettings.MessageAlignment.CENTER, iamSettings.horizontalAlignment)
        assertEquals(InAppMessageSettings.MessageAnimation.NONE, iamSettings.displayAnimation)
        assertEquals(InAppMessageSettings.MessageAnimation.NONE, iamSettings.dismissAnimation)
        assertEquals("#000000", iamSettings.backdropColor)
        assertEquals(0.0f, iamSettings.backdropOpacity)
        assertEquals(0.0f, iamSettings.cornerRadius)
        assertFalse(iamSettings.shouldTakeOverUi)
        assertTrue(iamSettings.assetMap.isEmpty())
        assertTrue(iamSettings.gestureMap.isEmpty())
    }

    @Test
    fun `Test InAppMessageSettings with full height and width params`() {
        val iamSettings = InAppMessageSettings.Builder()
            .height(100)
            .width(100)
            .build()
        assertEquals("", iamSettings.content)
        assertEquals(100, iamSettings.width)
        assertEquals(100, iamSettings.height)
        assertEquals(0, iamSettings.verticalInset)
        assertEquals(0, iamSettings.horizontalInset)
        assertEquals(InAppMessageSettings.MessageAlignment.CENTER, iamSettings.verticalAlignment)
        assertEquals(InAppMessageSettings.MessageAlignment.CENTER, iamSettings.horizontalAlignment)
        assertEquals(InAppMessageSettings.MessageAnimation.NONE, iamSettings.displayAnimation)
        assertEquals(InAppMessageSettings.MessageAnimation.NONE, iamSettings.dismissAnimation)
        assertEquals("#000000", iamSettings.backdropColor)
        assertEquals(0.0f, iamSettings.backdropOpacity)
        assertEquals(0.0f, iamSettings.cornerRadius)
        assertFalse(iamSettings.shouldTakeOverUi)
        assertTrue(iamSettings.assetMap.isEmpty())
        assertTrue(iamSettings.gestureMap.isEmpty())
    }

    @Test
    fun `Test InAppMessageSettings with height and width params needing clipping`() {
        val iamSettings = InAppMessageSettings.Builder()
            .height(123)
            .width(123)
            .build()
        assertEquals("", iamSettings.content)
        assertEquals(100, iamSettings.width)
        assertEquals(100, iamSettings.height)
        assertEquals(0, iamSettings.verticalInset)
        assertEquals(0, iamSettings.horizontalInset)
        assertEquals(InAppMessageSettings.MessageAlignment.CENTER, iamSettings.verticalAlignment)
        assertEquals(InAppMessageSettings.MessageAlignment.CENTER, iamSettings.horizontalAlignment)
        assertEquals(InAppMessageSettings.MessageAnimation.NONE, iamSettings.displayAnimation)
        assertEquals(InAppMessageSettings.MessageAnimation.NONE, iamSettings.dismissAnimation)
        assertEquals("#000000", iamSettings.backdropColor)
        assertEquals(0.0f, iamSettings.backdropOpacity)
        assertEquals(0.0f, iamSettings.cornerRadius)
        assertFalse(iamSettings.shouldTakeOverUi)
        assertTrue(iamSettings.assetMap.isEmpty())
        assertTrue(iamSettings.gestureMap.isEmpty())
    }

    @Test
    fun `Test InAppMessageSettings with negative insets needing clipping`() {
        val iamSettings = InAppMessageSettings.Builder()
            .height(50)
            .width(50)
            .horizontalInset(-20)
            .verticalInset(-10)
            .build()
        assertEquals("", iamSettings.content)
        assertEquals(50, iamSettings.width)
        assertEquals(50, iamSettings.height)
        assertEquals(-10, iamSettings.verticalInset)
        assertEquals(-20, iamSettings.horizontalInset)
        assertEquals(InAppMessageSettings.MessageAlignment.CENTER, iamSettings.verticalAlignment)
        assertEquals(InAppMessageSettings.MessageAlignment.CENTER, iamSettings.horizontalAlignment)
        assertEquals(InAppMessageSettings.MessageAnimation.NONE, iamSettings.displayAnimation)
        assertEquals(InAppMessageSettings.MessageAnimation.NONE, iamSettings.dismissAnimation)
        assertEquals("#000000", iamSettings.backdropColor)
        assertEquals(0.0f, iamSettings.backdropOpacity)
        assertEquals(0.0f, iamSettings.cornerRadius)
        assertFalse(iamSettings.shouldTakeOverUi)
        assertTrue(iamSettings.assetMap.isEmpty())
        assertTrue(iamSettings.gestureMap.isEmpty())
    }

    @Test
    fun `Test InAppMessageSettings with negative height, width needing clipping`() {
        val iamSettings = InAppMessageSettings.Builder()
            .height(-50)
            .width(-50)
            .build()
        assertEquals("", iamSettings.content)
        assertEquals(0, iamSettings.width)
        assertEquals(0, iamSettings.height)
        assertEquals(0, iamSettings.verticalInset)
        assertEquals(0, iamSettings.horizontalInset)
        assertEquals(InAppMessageSettings.MessageAlignment.CENTER, iamSettings.verticalAlignment)
        assertEquals(InAppMessageSettings.MessageAlignment.CENTER, iamSettings.horizontalAlignment)
        assertEquals(InAppMessageSettings.MessageAnimation.NONE, iamSettings.displayAnimation)
        assertEquals(InAppMessageSettings.MessageAnimation.NONE, iamSettings.dismissAnimation)
        assertEquals("#000000", iamSettings.backdropColor)
        assertEquals(0.0f, iamSettings.backdropOpacity)
        assertEquals(0.0f, iamSettings.cornerRadius)
        assertFalse(iamSettings.shouldTakeOverUi)
        assertTrue(iamSettings.assetMap.isEmpty())
        assertTrue(iamSettings.gestureMap.isEmpty())
    }

    @Test
    fun `Test InAppMessageSettings with exceeding +ve insets and dimensions needing clipping`() {
        val iamSettings = InAppMessageSettings.Builder()
            .height(173)
            .width(233)
            .horizontalInset(220)
            .verticalInset(220)
            .build()
        assertEquals("", iamSettings.content)
        assertEquals(100, iamSettings.width)
        assertEquals(100, iamSettings.height)
        assertEquals(100, iamSettings.verticalInset)
        assertEquals(100, iamSettings.horizontalInset)
        assertEquals(InAppMessageSettings.MessageAlignment.CENTER, iamSettings.verticalAlignment)
        assertEquals(InAppMessageSettings.MessageAlignment.CENTER, iamSettings.horizontalAlignment)
        assertEquals(InAppMessageSettings.MessageAnimation.NONE, iamSettings.displayAnimation)
        assertEquals(InAppMessageSettings.MessageAnimation.NONE, iamSettings.dismissAnimation)
        assertEquals("#000000", iamSettings.backdropColor)
        assertEquals(0.0f, iamSettings.backdropOpacity)
        assertEquals(0.0f, iamSettings.cornerRadius)
        assertFalse(iamSettings.shouldTakeOverUi)
        assertTrue(iamSettings.assetMap.isEmpty())
        assertTrue(iamSettings.gestureMap.isEmpty())
    }

    @Test
    fun `Test InAppMessageSettings with exceeding -ve insets and dimensions needing clipping`() {
        val iamSettings = InAppMessageSettings.Builder()
            .height(-173)
            .width(-233)
            .horizontalInset(-220)
            .verticalInset(-220)
            .build()
        assertEquals("", iamSettings.content)
        assertEquals(0, iamSettings.width)
        assertEquals(0, iamSettings.height)
        assertEquals(-100, iamSettings.verticalInset)
        assertEquals(-100, iamSettings.horizontalInset)
        assertEquals(InAppMessageSettings.MessageAlignment.CENTER, iamSettings.verticalAlignment)
        assertEquals(InAppMessageSettings.MessageAlignment.CENTER, iamSettings.horizontalAlignment)
        assertEquals(InAppMessageSettings.MessageAnimation.NONE, iamSettings.displayAnimation)
        assertEquals(InAppMessageSettings.MessageAnimation.NONE, iamSettings.dismissAnimation)
        assertEquals("#000000", iamSettings.backdropColor)
        assertEquals(0.0f, iamSettings.backdropOpacity)
        assertEquals(0.0f, iamSettings.cornerRadius)
        assertFalse(iamSettings.shouldTakeOverUi)
        assertTrue(iamSettings.assetMap.isEmpty())
        assertTrue(iamSettings.gestureMap.isEmpty())
    }

    @Test
    fun `Test InAppMessageSettings with custom params`() {
        val iamSettings = InAppMessageSettings.Builder()
            .content("<html><head><body>Hi</body></head></html>")
            .height(80)
            .width(80)
            .verticalInset(10)
            .horizontalInset(10)
            .verticalAlignment(InAppMessageSettings.MessageAlignment.TOP)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.LEFT)
            .displayAnimation(InAppMessageSettings.MessageAnimation.FADE)
            .dismissAnimation(InAppMessageSettings.MessageAnimation.TOP)
            .backgroundColor("#FFFFFF")
            .backdropOpacity(0.5f)
            .cornerRadius(10.0f)
            .shouldTakeOverUi(true)
            .setFitToContent(true)
            .assetMap(mapOf("key1" to "value1"))
            .gestureMap(mapOf("swipeUp" to "adbinapp//dismiss"))
            .build()

        assertEquals("<html><head><body>Hi</body></head></html>", iamSettings.content)
        assertEquals(80, iamSettings.width)
        assertEquals(80, iamSettings.height)
        assertEquals(10, iamSettings.verticalInset)
        assertEquals(10, iamSettings.horizontalInset)
        assertEquals(InAppMessageSettings.MessageAlignment.TOP, iamSettings.verticalAlignment)
        assertEquals(InAppMessageSettings.MessageAlignment.LEFT, iamSettings.horizontalAlignment)
        assertEquals(InAppMessageSettings.MessageAnimation.FADE, iamSettings.displayAnimation)
        assertEquals(InAppMessageSettings.MessageAnimation.TOP, iamSettings.dismissAnimation)
        assertEquals("#FFFFFF", iamSettings.backdropColor)
        assertEquals(0.5f, iamSettings.backdropOpacity)
        assertEquals(10.0f, iamSettings.cornerRadius)
        assertTrue(iamSettings.shouldTakeOverUi)
        assertTrue(iamSettings.fitToContent)
        assertEquals("value1", iamSettings.assetMap["key1"])
        assertEquals(
            mapOf(InAppMessageSettings.MessageGesture.SWIPE_UP to "adbinapp//dismiss"),
            iamSettings.gestureMap
        )
    }

    @Test
    fun `Test InAppMessageSettings with unknown gestures`() {
        val testGestureMap = mapOf(
            "swipeUp" to "adbinapp//dismiss",
            "swipeDown" to "adbinapp//dismiss",
            "doubleTap" to "adbinapp//dismiss" // not supported
        )

        val iamSettings = InAppMessageSettings.Builder()
            .gestureMap(testGestureMap)
            .build()

        val expectedGestureMap = mapOf(
            InAppMessageSettings.MessageGesture.SWIPE_UP to "adbinapp//dismiss",
            InAppMessageSettings.MessageGesture.SWIPE_DOWN to "adbinapp//dismiss"
        )
        assertEquals(expectedGestureMap, iamSettings.gestureMap)
    }
}
