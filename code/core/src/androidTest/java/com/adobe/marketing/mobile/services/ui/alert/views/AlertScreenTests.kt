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

package com.adobe.marketing.mobile.services.ui.alert.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import com.adobe.marketing.mobile.services.ui.alert.AlertSettings
import com.adobe.marketing.mobile.services.ui.common.PresentationStateManager
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AlertScreenTests {
    @get: Rule
    val composeTestRule = createComposeRule()

    private val presentationStateManager = PresentationStateManager()
    private var onPositiveButtonClicked = false
    private var onNegativeButtonClicked = false
    private var onBackPressed = false

    @Before
    fun setUp() {
    }

    @Test
    fun testAlertScreenNotDisplayedWhenFirstAttached() {
        // Setup
        val alertSettings = AlertSettings.Builder()
            .title("My Title")
            .message("My Message")
            .positiveButtonText("Confirm")
            .negativeButtonText("Cancel")
            .build()

        composeTestRule.setContent {
            AlertScreen(
                presentationStateManager = presentationStateManager,
                alertSettings = alertSettings,
                onPositiveResponse = { onPositiveButtonClicked = true },
                onNegativeResponse = { onNegativeButtonClicked = true },
                onBackPressed = { onBackPressed = true }
            )
        }

        // Test
        composeTestRule.onNodeWithTag(AlertTestTags.TITLE_TEXT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(AlertTestTags.MESSAGE_TEXT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(AlertTestTags.POSITIVE_BUTTON).assertDoesNotExist()
        composeTestRule.onNodeWithTag(AlertTestTags.NEGATIVE_BUTTON).assertDoesNotExist()
    }

    @Test
    fun testAlertScreenIsDisplayedWhenPresentationVisibilityStateIsTrue() {
        // Setup
        val alertSettings = AlertSettings.Builder()
            .title("My Title")
            .message("My Message")
            .positiveButtonText("Confirm")
            .negativeButtonText("Cancel")
            .build()

        composeTestRule.setContent {
            AlertScreen(
                presentationStateManager = presentationStateManager,
                alertSettings = alertSettings,
                onPositiveResponse = { onPositiveButtonClicked = true },
                onNegativeResponse = { onNegativeButtonClicked = true },
                onBackPressed = { onBackPressed = true }
            )
        }

        // Test
        presentationStateManager.onShown()

        composeTestRule.onNodeWithTag(AlertTestTags.TITLE_TEXT).assertExists().assertIsDisplayed()
            .assertTextContains("My Title")
        composeTestRule.onNodeWithTag(AlertTestTags.MESSAGE_TEXT).assertExists().assertIsDisplayed()
            .assertTextContains("My Message")
        composeTestRule.onNodeWithTag(AlertTestTags.POSITIVE_BUTTON).assertExists()
            .assertTextContains("Confirm")
        composeTestRule.onNodeWithTag(AlertTestTags.NEGATIVE_BUTTON).assertExists()
            .assertTextContains("Cancel")
    }

    @Test
    fun testAlertScreenDisplaysRightNumberOfButtonsWhenPresentationVisibilityStateIsTrue() {
        // Setup
        val alertSettings = AlertSettings.Builder()
            .title("My Title")
            .message("My Message")
            .positiveButtonText("Confirm") // No negative button
            .build()

        composeTestRule.setContent {
            AlertScreen(
                presentationStateManager = presentationStateManager,
                alertSettings = alertSettings,
                onPositiveResponse = { onPositiveButtonClicked = true },
                onNegativeResponse = { onNegativeButtonClicked = true },
                onBackPressed = { onBackPressed = true }
            )
        }

        // Test
        presentationStateManager.onShown()

        composeTestRule.onNodeWithTag(AlertTestTags.TITLE_TEXT).assertExists().assertIsDisplayed()
            .assertTextContains("My Title")
        composeTestRule.onNodeWithTag(AlertTestTags.MESSAGE_TEXT).assertExists().assertIsDisplayed()
            .assertTextContains("My Message")
        composeTestRule.onNodeWithTag(AlertTestTags.POSITIVE_BUTTON).assertExists()
            .assertIsDisplayed().assertTextContains("Confirm")
        composeTestRule.onNodeWithTag(AlertTestTags.NEGATIVE_BUTTON).assertDoesNotExist()
    }

    @Test
    fun testAlertScreenNotifiesOnPositiveResponseWhenPositiveButtonIsClicked() {
        // Setup
        val alertSettings = AlertSettings.Builder()
            .title("My Title")
            .message("My Message")
            .positiveButtonText("Confirm")
            .negativeButtonText("Cancel")
            .build()

        composeTestRule.setContent {
            AlertScreen(
                presentationStateManager = presentationStateManager,
                alertSettings = alertSettings,
                onPositiveResponse = { onPositiveButtonClicked = true },
                onNegativeResponse = { onNegativeButtonClicked = true },
                onBackPressed = { onBackPressed = true }
            )
        }

        // Test
        presentationStateManager.onShown()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(AlertTestTags.TITLE_TEXT).assertExists().assertIsDisplayed()
            .assertTextContains("My Title")
        composeTestRule.onNodeWithTag(AlertTestTags.MESSAGE_TEXT).assertExists().assertIsDisplayed()
            .assertTextContains("My Message")
        composeTestRule.onNodeWithTag(AlertTestTags.POSITIVE_BUTTON).assertExists()
            .assertTextContains("Confirm")
        composeTestRule.onNodeWithTag(AlertTestTags.NEGATIVE_BUTTON).assertExists()
            .assertTextContains("Cancel")

        // Click the positive button
        composeTestRule.onNode(hasTestTag(AlertTestTags.POSITIVE_BUTTON)).assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        // verify
        assert(onPositiveButtonClicked)
    }

    @Test
    fun testAlertScreenNotifiesOnNegativeResponseWhenNegativeButtonIsClicked() {
        // Setup
        val alertSettings = AlertSettings.Builder()
            .title("My Title")
            .message("My Message")
            .positiveButtonText("Confirm")
            .negativeButtonText("Cancel")
            .build()

        composeTestRule.setContent {
            AlertScreen(
                presentationStateManager = presentationStateManager,
                alertSettings = alertSettings,
                onPositiveResponse = { onPositiveButtonClicked = true },
                onNegativeResponse = { onNegativeButtonClicked = true },
                onBackPressed = { onBackPressed = true }
            )
        }

        // Test
        presentationStateManager.onShown()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(AlertTestTags.TITLE_TEXT).assertExists().assertIsDisplayed()
            .assertTextContains("My Title")
        composeTestRule.onNodeWithTag(AlertTestTags.MESSAGE_TEXT).assertExists().assertIsDisplayed()
            .assertTextContains("My Message")
        composeTestRule.onNodeWithTag(AlertTestTags.POSITIVE_BUTTON).assertExists()
            .assertTextContains("Confirm")
        composeTestRule.onNodeWithTag(AlertTestTags.NEGATIVE_BUTTON).assertExists()
            .assertTextContains("Cancel")

        // Click the negative button
        composeTestRule.onNode(hasTestTag(AlertTestTags.NEGATIVE_BUTTON)).assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        // verify
        assert(onNegativeButtonClicked)
    }

    @Test
    fun testAlertScreenNotifiesOnBackPressedWhenBackButtonIsClicked() {
        // Setup
        val alertSettings = AlertSettings.Builder()
            .title("My Title")
            .message("My Message")
            .positiveButtonText("Confirm")
            .negativeButtonText("Cancel")
            .build()

        composeTestRule.setContent {
            AlertScreen(
                presentationStateManager = presentationStateManager,
                alertSettings = alertSettings,
                onPositiveResponse = { onPositiveButtonClicked = true },
                onNegativeResponse = { onNegativeButtonClicked = true },
                onBackPressed = { onBackPressed = true }
            )
        }

        // Test
        presentationStateManager.onShown()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(AlertTestTags.TITLE_TEXT).assertExists().assertIsDisplayed()
            .assertTextContains("My Title")
        composeTestRule.onNodeWithTag(AlertTestTags.MESSAGE_TEXT).assertExists().assertIsDisplayed()
            .assertTextContains("My Message")
        composeTestRule.onNodeWithTag(AlertTestTags.POSITIVE_BUTTON).assertExists()
            .assertTextContains("Confirm")
        composeTestRule.onNodeWithTag(AlertTestTags.NEGATIVE_BUTTON).assertExists()
            .assertTextContains("Cancel")

        // Press back button
        Espresso.pressBack()
        Espresso.onIdle()

        // verify that onBackPressed is called
        assertTrue(onBackPressed)
    }

    @Test
    fun testAlertScreenIsInvisibleWhenPresentationStateIsHidden() {
        // Setup
        val alertSettings = AlertSettings.Builder()
            .title("My Title")
            .message("My Message")
            .positiveButtonText("Confirm")
            .negativeButtonText("Cancel")
            .build()

        composeTestRule.setContent {
            AlertScreen(
                presentationStateManager = presentationStateManager,
                alertSettings = alertSettings,
                onPositiveResponse = { onPositiveButtonClicked = true },
                onNegativeResponse = { onNegativeButtonClicked = true },
                onBackPressed = { onBackPressed = true }
            )
        }

        // show the alert initially
        presentationStateManager.onShown()

        // verify that the alert is visible
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(AlertTestTags.TITLE_TEXT).assertExists().assertIsDisplayed()
            .assertTextContains("My Title")
        composeTestRule.onNodeWithTag(AlertTestTags.MESSAGE_TEXT).assertExists().assertIsDisplayed()
            .assertTextContains("My Message")
        composeTestRule.onNodeWithTag(AlertTestTags.POSITIVE_BUTTON).assertExists()
            .assertIsDisplayed().assertTextContains("Confirm")
        composeTestRule.onNodeWithTag(AlertTestTags.NEGATIVE_BUTTON).assertExists()
            .assertIsDisplayed().assertTextContains("Cancel")

        // hide the alert
        presentationStateManager.onHidden()

        // verify that the alert is not visible
        composeTestRule.onNodeWithTag(AlertTestTags.TITLE_TEXT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(AlertTestTags.MESSAGE_TEXT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(AlertTestTags.POSITIVE_BUTTON).assertDoesNotExist()
        composeTestRule.onNodeWithTag(AlertTestTags.NEGATIVE_BUTTON).assertDoesNotExist()
    }

    @Test
    fun testAlertScreenIsInvisibleWhenPresentationStateIsDismissed() {
        // Setup
        val alertSettings = AlertSettings.Builder()
            .title("My Title")
            .message("My Message")
            .positiveButtonText("Confirm")
            .negativeButtonText("Cancel")
            .build()

        composeTestRule.setContent {
            AlertScreen(
                presentationStateManager = presentationStateManager,
                alertSettings = alertSettings,
                onPositiveResponse = { onPositiveButtonClicked = true },
                onNegativeResponse = { onNegativeButtonClicked = true },
                onBackPressed = { onBackPressed = true }
            )
        }

        // show the alert initially
        presentationStateManager.onShown()

        // verify that the alert is visible
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(AlertTestTags.TITLE_TEXT).assertExists().assertIsDisplayed()
            .assertTextContains("My Title")
        composeTestRule.onNodeWithTag(AlertTestTags.MESSAGE_TEXT).assertExists().assertIsDisplayed()
            .assertTextContains("My Message")
        composeTestRule.onNodeWithTag(AlertTestTags.POSITIVE_BUTTON).assertExists()
            .assertIsDisplayed().assertTextContains("Confirm")
        composeTestRule.onNodeWithTag(AlertTestTags.NEGATIVE_BUTTON).assertExists()
            .assertIsDisplayed().assertTextContains("Cancel")

        // hide the alert
        presentationStateManager.onDetached()

        // verify that the alert is not visible
        composeTestRule.onNodeWithTag(AlertTestTags.TITLE_TEXT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(AlertTestTags.MESSAGE_TEXT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(AlertTestTags.POSITIVE_BUTTON).assertDoesNotExist()
        composeTestRule.onNodeWithTag(AlertTestTags.NEGATIVE_BUTTON).assertDoesNotExist()
    }

    @After
    fun tearDown() {
        onBackPressed = false
        onPositiveButtonClicked = false
        onNegativeButtonClicked = false
    }
}
