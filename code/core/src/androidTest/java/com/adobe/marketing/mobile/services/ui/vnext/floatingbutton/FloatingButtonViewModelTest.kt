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

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FloatingButtonViewModelTest {
    companion object {
        private const val TEST_GRAPHIC_RESOURCE =
            "uiservice_fab_tests/floatingButtonTestGraphic.txt"
    }

    @Test
    fun testOnGraphicUpdateChangesTheFloatingButtonGraphic() {
        val graphicBase64 = this.javaClass.classLoader
            ?.getResource(TEST_GRAPHIC_RESOURCE)?.readText()
        val graphicBase64Bytes = Base64.decode(graphicBase64, Base64.DEFAULT)
        val expectedBitmap: Bitmap =
            BitmapFactory.decodeStream(graphicBase64Bytes.inputStream()).asImageBitmap()
                .asAndroidBitmap()

        runTest {
            val floatingButtonViewModel =
                FloatingButtonViewModel(CoroutineScope(Dispatchers.Unconfined))
            assertTrue(
                floatingButtonViewModel.currentGraphic.value.asAndroidBitmap()
                    .sameAs(FloatingButtonViewModel.NO_GRAPHIC.asAndroidBitmap())
            )

            val graphicToUpdateWith = graphicBase64?.byteInputStream()
            assertNotNull(graphicToUpdateWith)
            floatingButtonViewModel.onGraphicUpdate(graphicToUpdateWith!!)

            assertNotNull(floatingButtonViewModel.currentGraphic.value)
            assertTrue(
                floatingButtonViewModel.currentGraphic.value.asAndroidBitmap()
                    .sameAs(expectedBitmap)
            )
        }
    }

    @Test
    fun testGraphicUpdateWhenUpdatedGraphicIsEmpty() {
        val graphicBase64 = ""
        runTest {
            val floatingButtonViewModel =
                FloatingButtonViewModel(CoroutineScope(Dispatchers.Unconfined))
            assertTrue(
                floatingButtonViewModel.currentGraphic.value.asAndroidBitmap()
                    .sameAs(FloatingButtonViewModel.NO_GRAPHIC.asAndroidBitmap())
            )

            val graphicToUpdateWith = graphicBase64.byteInputStream()
            floatingButtonViewModel.onGraphicUpdate(graphicToUpdateWith)

            assertTrue(
                floatingButtonViewModel.currentGraphic.value.asAndroidBitmap()
                    .sameAs(FloatingButtonViewModel.NO_GRAPHIC.asAndroidBitmap())
            )
        }
    }

    @Test
    fun testGraphicUpdateWhenUpdatedGraphicIsBlank() {
        val graphicBase64 = "                 "
        runTest {
            val floatingButtonViewModel =
                FloatingButtonViewModel(CoroutineScope(Dispatchers.Unconfined))
            assertTrue(
                floatingButtonViewModel.currentGraphic.value.asAndroidBitmap()
                    .sameAs(FloatingButtonViewModel.NO_GRAPHIC.asAndroidBitmap())
            )

            val graphicToUpdateWith = graphicBase64.byteInputStream()
            floatingButtonViewModel.onGraphicUpdate(graphicToUpdateWith)

            assertTrue(
                floatingButtonViewModel.currentGraphic.value.asAndroidBitmap()
                    .sameAs(FloatingButtonViewModel.NO_GRAPHIC.asAndroidBitmap())
            )
        }
    }

    @Test
    fun testGraphicUpdateDoesNotUpdateWhenUpdatedGraphicIsInputCannotBeDecoded() {
        val graphicBase64 = "~~~~~~"
        runTest {
            val floatingButtonViewModel =
                FloatingButtonViewModel(CoroutineScope(Dispatchers.Unconfined))
            assertTrue(
                floatingButtonViewModel.currentGraphic.value.asAndroidBitmap()
                    .sameAs(FloatingButtonViewModel.NO_GRAPHIC.asAndroidBitmap())
            )

            val graphicToUpdateWith = graphicBase64.byteInputStream()
            floatingButtonViewModel.onGraphicUpdate(graphicToUpdateWith)
            assertTrue(
                floatingButtonViewModel.currentGraphic.value.asAndroidBitmap()
                    .sameAs(FloatingButtonViewModel.NO_GRAPHIC.asAndroidBitmap())
            )
        }
    }

    @Test
    fun testOnPositionUpdatedWhenOffsetsAreOutOfBounds() {
        val floatingButtonViewModel = FloatingButtonViewModel(CoroutineScope(Dispatchers.Unconfined))
        // verify initial state
        assertEquals(Offset.Unspecified, floatingButtonViewModel.landscapeOffSet)
        assertEquals(Offset.Unspecified, floatingButtonViewModel.portraitOffSet)

        // update landscape offsets with out of bounds values
        floatingButtonViewModel.onPositionUpdate(Offset(-10f, -1f), Configuration.ORIENTATION_LANDSCAPE)
        floatingButtonViewModel.onPositionUpdate(Offset(10f, -1f), Configuration.ORIENTATION_LANDSCAPE)
        floatingButtonViewModel.onPositionUpdate(Offset(-1f, 10f), Configuration.ORIENTATION_LANDSCAPE)

        // update portrait offsets with out of bounds values
        floatingButtonViewModel.onPositionUpdate(Offset(-10f, -1f), Configuration.ORIENTATION_PORTRAIT)
        floatingButtonViewModel.onPositionUpdate(Offset(10f, -1f), Configuration.ORIENTATION_PORTRAIT)
        floatingButtonViewModel.onPositionUpdate(Offset(-1f, 10f), Configuration.ORIENTATION_PORTRAIT)

        // verify offsets are not updated
        assertEquals(Offset.Unspecified, floatingButtonViewModel.portraitOffSet)
        assertEquals(Offset.Unspecified, floatingButtonViewModel.landscapeOffSet)
    }

    @Test
    fun testOnPositionUpdatedWhenOrientationIsPortrait() {
        // setup
        val floatingButtonViewModel = FloatingButtonViewModel(CoroutineScope(Dispatchers.Unconfined))
        assertEquals(Offset.Unspecified, floatingButtonViewModel.landscapeOffSet)
        assertEquals(Offset.Unspecified, floatingButtonViewModel.portraitOffSet)

        // update portrait offset
        floatingButtonViewModel.onPositionUpdate(Offset(10f, 50f), Configuration.ORIENTATION_PORTRAIT)
        assertEquals(Offset(10f, 50f), floatingButtonViewModel.portraitOffSet)

        // landscape offset should not be updated
        assertEquals(Offset.Unspecified, floatingButtonViewModel.landscapeOffSet)
    }

    @Test
    fun testOnPositionUpdatedWhenOrientationIsLandscape() {
        // setup
        val floatingButtonViewModel = FloatingButtonViewModel(CoroutineScope(Dispatchers.Unconfined))
        assertEquals(Offset.Unspecified, floatingButtonViewModel.landscapeOffSet)
        assertEquals(Offset.Unspecified, floatingButtonViewModel.portraitOffSet)

        // update landscape offset
        floatingButtonViewModel.onPositionUpdate(Offset(10f, 50f), Configuration.ORIENTATION_LANDSCAPE)
        assertEquals(Offset(10f, 50f), floatingButtonViewModel.landscapeOffSet)

        // portrait offset should not be updated
        assertEquals(Offset.Unspecified, floatingButtonViewModel.portraitOffSet)
    }

    @Test
    fun testOnPositionUpdatedRetainsOrientationSpecificOffsets() {
        // setup
        val floatingButtonViewModel = FloatingButtonViewModel(CoroutineScope(Dispatchers.Unconfined))
        assertEquals(Offset.Unspecified, floatingButtonViewModel.landscapeOffSet)
        assertEquals(Offset.Unspecified, floatingButtonViewModel.portraitOffSet)

        // update landscape offset
        floatingButtonViewModel.onPositionUpdate(Offset(10f, 50f), Configuration.ORIENTATION_LANDSCAPE)
        // update portrait offset
        floatingButtonViewModel.onPositionUpdate(Offset(20f, 60f), Configuration.ORIENTATION_PORTRAIT)

        // verify offsets are retained
        assertEquals(Offset(10f, 50f), floatingButtonViewModel.landscapeOffSet)
        assertEquals(Offset(20f, 60f), floatingButtonViewModel.portraitOffSet)

        // update landscape offset
        floatingButtonViewModel.onPositionUpdate(Offset(30f, 70f), Configuration.ORIENTATION_LANDSCAPE)
        // update portrait offset
        floatingButtonViewModel.onPositionUpdate(Offset(40f, 80f), Configuration.ORIENTATION_PORTRAIT)

        // verify offsets are retained
        assertEquals(Offset(30f, 70f), floatingButtonViewModel.landscapeOffSet)
        assertEquals(Offset(40f, 80f), floatingButtonViewModel.portraitOffSet)
    }
}
