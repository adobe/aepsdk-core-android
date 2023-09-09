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
import junit.framework.TestCase.assertTrue
import org.junit.Test

class FloatingButtonViewModelTest {
    companion object {
        private const val TEST_GRAPHIC_RESOURCE =
            "uiservice_fab_tests/floatingButtonTestGraphic.txt"
        private const val TEST_GRAPHIC_RESOURCE_UPDATE =
            "uiservice_fab_tests/floatingButtonTestGraphicUpdate.txt"
    }

    @Test
    fun testOnGraphicUpdateChangesTheFloatingButtonGraphic() {
        val expectedBitmap: Bitmap = getFloatingButtonGraphic(TEST_GRAPHIC_RESOURCE)
        val floatingButtonViewModel = FloatingButtonViewModel(
            FloatingButtonSettings
                .Builder()
                .initialGraphic(getFloatingButtonGraphic(TEST_GRAPHIC_RESOURCE))
                .build()
        )
        assertTrue(expectedBitmap.asImageBitmap().asAndroidBitmap().sameAs(floatingButtonViewModel.currentGraphic.value.asAndroidBitmap()))

        val updatedBitmap: Bitmap = getFloatingButtonGraphic(TEST_GRAPHIC_RESOURCE_UPDATE)
        floatingButtonViewModel.onGraphicUpdate(updatedBitmap)
        assertTrue(updatedBitmap.asImageBitmap().asAndroidBitmap().sameAs(floatingButtonViewModel.currentGraphic.value.asAndroidBitmap()))
    }

    @Test
    fun testOnPositionUpdatedWhenOffsetsAreOutOfBounds() {
        val floatingButtonViewModel = FloatingButtonViewModel(
            FloatingButtonSettings
                .Builder()
                .initialGraphic(getFloatingButtonGraphic(TEST_GRAPHIC_RESOURCE))
                .build()
        )
        // verify initial state
        assertEquals(Offset.Unspecified, floatingButtonViewModel.landscapeOffSet)
        assertEquals(Offset.Unspecified, floatingButtonViewModel.portraitOffSet)

        // offsets with out of bounds values
        val offsetList = listOf(
            Offset(-10f, -1f),
            Offset(10f, -1f),
            Offset(-1f, 10f)
        )

        offsetList.forEach { offset ->
            // update landscape offsets with out of bounds values
            floatingButtonViewModel.onPositionUpdate(offset, Configuration.ORIENTATION_LANDSCAPE)
            // verify offsets are not updated
            assertEquals(Offset.Unspecified, floatingButtonViewModel.landscapeOffSet)

            // update portrait offsets with out of bounds values
            floatingButtonViewModel.onPositionUpdate(offset, Configuration.ORIENTATION_PORTRAIT)
            // verify offsets are not updated
            assertEquals(Offset.Unspecified, floatingButtonViewModel.portraitOffSet)
        }
    }

    @Test
    fun testOnPositionUpdatedWhenOrientationIsPortrait() {
        // setup
        val floatingButtonViewModel = FloatingButtonViewModel(
            FloatingButtonSettings
                .Builder()
                .initialGraphic(getFloatingButtonGraphic(TEST_GRAPHIC_RESOURCE))
                .build()
        )
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
        val floatingButtonViewModel = FloatingButtonViewModel(
            FloatingButtonSettings
                .Builder()
                .initialGraphic(getFloatingButtonGraphic(TEST_GRAPHIC_RESOURCE))
                .build()
        )
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
        val floatingButtonViewModel = FloatingButtonViewModel(
            FloatingButtonSettings
                .Builder()
                .initialGraphic(getFloatingButtonGraphic(TEST_GRAPHIC_RESOURCE))
                .build()
        )
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

    private fun getFloatingButtonGraphic(resourcePath: String): Bitmap {
        val graphicBase64 = this.javaClass.classLoader
            ?.getResource(resourcePath)?.readText()
        val graphicBase64Bytes = Base64.decode(graphicBase64, Base64.DEFAULT)
        return BitmapFactory.decodeStream(graphicBase64Bytes.inputStream()).asImageBitmap()
            .asAndroidBitmap()
    }
}
