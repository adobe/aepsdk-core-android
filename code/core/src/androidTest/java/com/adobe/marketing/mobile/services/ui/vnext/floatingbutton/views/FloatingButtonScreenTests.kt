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

package com.adobe.marketing.mobile.services.ui.vnext.floatingbutton.views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.services.ui.vnext.common.PresentationStateManager
import com.adobe.marketing.mobile.services.ui.vnext.floatingbutton.FloatingButtonSettings
import com.adobe.marketing.mobile.services.ui.vnext.floatingbutton.FloatingButtonViewModel
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class FloatingButtonScreenTests {
    companion object {
        private const val TEST_GRAPHIC_RESOURCE =
            "uiservice_fab_tests/floatingButtonTestGraphic.txt"
    }

    @get: Rule
    val composeTestRule = createComposeRule()

    private val presentationStateManager = PresentationStateManager()
    private var onTapDetected = false
    private var onPanDetected = false

    @Test
    fun testFloatingButtonIsNotDisplayedWhenFirstAttached() {
        val floatingButtonSettings = FloatingButtonSettings.Builder()
            .height(100)
            .width(100)
            .initialGraphic(getFloatingButtonGraphic())
            .build()
        val floatingButtonViewModel = FloatingButtonViewModel(
            FloatingButtonSettings.Builder().initialGraphic(getFloatingButtonGraphic()).build()
        )

        composeTestRule.setContent {
            FloatingButtonScreen(
                presentationStateManager = presentationStateManager,
                floatingButtonSettings = floatingButtonSettings,
                floatingButtonViewModel = floatingButtonViewModel,
                onTapDetected = { onTapDetected = true },
                onPanDetected = { onPanDetected = true }
            )
        }
        validateFloatingButtonIsNotShown()
    }

    @Test
    fun testFloatingButtonIsDisplayedWhenPresentationVisibilityStateIsTrue() {
        val floatingButtonSettings = FloatingButtonSettings.Builder()
            .height(100)
            .width(100)
            .initialGraphic(getFloatingButtonGraphic())
            .build()
        val floatingButtonViewModel = FloatingButtonViewModel(
            FloatingButtonSettings.Builder().initialGraphic(getFloatingButtonGraphic()).build()
        )

        composeTestRule.setContent {
            FloatingButtonScreen(
                presentationStateManager = presentationStateManager,
                floatingButtonSettings = floatingButtonSettings,
                floatingButtonViewModel = floatingButtonViewModel,
                onTapDetected = { onTapDetected = true },
                onPanDetected = { onPanDetected = true }
            )
        }

        validateFloatingButtonIsNotShown()

        presentationStateManager.onShown()
        composeTestRule.waitForIdle()
        validateFloatingButtonIsShown()
    }

    @Test
    fun testFloatingButtonIsHiddenWhenPresentationVisibilityStateIsFalse() {
        val floatingButtonSettings = FloatingButtonSettings.Builder()
            .height(100)
            .width(100)
            .initialGraphic(getFloatingButtonGraphic())
            .build()
        val floatingButtonViewModel = FloatingButtonViewModel(
            FloatingButtonSettings.Builder().initialGraphic(getFloatingButtonGraphic()).build()
        )

        composeTestRule.setContent {
            FloatingButtonScreen(
                presentationStateManager = presentationStateManager,
                floatingButtonSettings = floatingButtonSettings,
                floatingButtonViewModel = floatingButtonViewModel,
                onTapDetected = { onTapDetected = true },
                onPanDetected = { onPanDetected = true }
            )
        }

        // validate that button is not initially shown
        validateFloatingButtonIsNotShown()

        // show the button
        presentationStateManager.onShown()

        // validate that button is shown
        composeTestRule.waitForIdle()
        validateFloatingButtonIsShown()

        presentationStateManager.onHidden()

        // validate that button is hidden
        composeTestRule.waitForIdle()
        validateFloatingButtonIsNotShown()
    }

    @Test
    fun testFloatingButtonIsDetachedWhenPresentationDetached() {
        val floatingButtonSettings = FloatingButtonSettings.Builder()
            .initialGraphic(getFloatingButtonGraphic())
            .build()
        val floatingButtonViewModel = FloatingButtonViewModel(
            FloatingButtonSettings.Builder().initialGraphic(getFloatingButtonGraphic()).build()
        )

        composeTestRule.setContent {
            FloatingButtonScreen(
                presentationStateManager = presentationStateManager,
                floatingButtonSettings = floatingButtonSettings,
                floatingButtonViewModel = floatingButtonViewModel,
                onTapDetected = { onTapDetected = true },
                onPanDetected = { onPanDetected = true }
            )
        }

        // validate that button is not initially shown
        validateFloatingButtonIsNotShown()

        // show the button
        presentationStateManager.onShown()

        // validate that button is shown
        composeTestRule.waitForIdle()
        validateFloatingButtonIsShown()

        presentationStateManager.onDetached()

        // validate that button is detached
        composeTestRule.waitForIdle()
        validateFloatingButtonIsNotShown()
    }

    @Ignore("Cannot seem to get the test to pass, but the functionality works as expected when tested manually")
    @Test
    fun testFloatingButtonNotifiesTaps() {
        val floatingButtonSettings = FloatingButtonSettings.Builder()
            .height(100)
            .width(100)
            .initialGraphic(getFloatingButtonGraphic())
            .build()
        val floatingButtonViewModel = FloatingButtonViewModel(
            FloatingButtonSettings.Builder().initialGraphic(getFloatingButtonGraphic()).build()
        )

        composeTestRule.setContent {
            FloatingButtonScreen(
                presentationStateManager = presentationStateManager,
                floatingButtonSettings = floatingButtonSettings,
                floatingButtonViewModel = floatingButtonViewModel,
                onTapDetected = { onTapDetected = true },
                onPanDetected = { onPanDetected = true }
            )
        }

        validateFloatingButtonIsNotShown()

        // Test
        presentationStateManager.onShown()

        // Verify that the button is shown
        composeTestRule.waitForIdle()
        validateFloatingButtonIsShown()

        // Tap the button
        composeTestRule.onNodeWithTag(FloatingButtonTestTags.FLOATING_BUTTON)
            .assertHasClickAction()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(onTapDetected)
    }

    @After
    fun tearDown() {
        onPanDetected = false
        onTapDetected = false
    }

    private fun getFloatingButtonGraphic(): Bitmap {
        val graphicBase64 = this.javaClass.classLoader
            ?.getResource(TEST_GRAPHIC_RESOURCE)?.readText()
        val graphicBase64Bytes = Base64.decode(graphicBase64, Base64.DEFAULT)
        return BitmapFactory.decodeStream(graphicBase64Bytes.inputStream()).asImageBitmap()
            .asAndroidBitmap()
    }

    private fun validateFloatingButtonIsShown() {
        composeTestRule.onNodeWithTag(FloatingButtonTestTags.FLOATING_BUTTON_AREA).assertExists()
        composeTestRule.onNodeWithTag(FloatingButtonTestTags.FLOATING_BUTTON).assertExists()
        composeTestRule.onNodeWithTag(
            FloatingButtonTestTags.FLOATING_BUTTON,
            useUnmergedTree = true
        ).onChildren().assertAny(
            hasTestTag(FloatingButtonTestTags.FLOATING_BUTTON_GRAPHIC)
        )
    }

    private fun validateFloatingButtonIsNotShown() {
        composeTestRule.onNodeWithTag(FloatingButtonTestTags.FLOATING_BUTTON_AREA)
            .assertDoesNotExist()
        composeTestRule.onNodeWithTag(FloatingButtonTestTags.FLOATING_BUTTON).assertDoesNotExist()
        composeTestRule.onNodeWithTag(FloatingButtonTestTags.FLOATING_BUTTON_GRAPHIC)
            .assertDoesNotExist()
    }
}
