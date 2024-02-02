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

package com.adobe.marketing.mobile.services.ui.message.views

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performGesture
import androidx.compose.ui.test.swipeWithVelocity
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.services.ui.common.PresentationStateManager
import com.adobe.marketing.mobile.services.ui.message.InAppMessageSettings
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageScreenTests {
    @get: Rule
    val composeTestRule = createComposeRule()

    private var onCreatedCalled = false
    private var onDisposedCalled = false
    private var onBackPressed = false
    private val detectedGestures = mutableListOf<InAppMessageSettings.MessageGesture>()
    private val presentationStateManager = PresentationStateManager()

    private val acceptedGestures = mutableMapOf(
        "swipeUp" to "adbinapp://dismiss",
        "swipeRight" to "adbinapp://dismiss",
        "swipeLeft" to "adbinapp://dismiss",
        "tapBackground" to "adbinapp://dismiss"
    )

    private val HTML_TEXT_SAMPLE = "<html>\n" +
        "<head>\n" +
        "<title>A Sample HTML Page</title>\n" +
        "</head>\n" +
        "<body>\n" +
        "\n" +
        "<h1>This is a sample HTML page</h1>\n" +
        "\n" +
        "</body>\n" +
        "</html>"

    @Test
    fun testMessageScreenIsNotDisplayedWhenFirstAttached() {
        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = getSettings(false),
                onCreated = { onCreatedCalled = true },
                onDisposed = { onDisposedCalled = true },
                onGestureDetected = { gesture -> detectedGestures.add(gesture) },
                onBackPressed = { onBackPressed = true }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).assertDoesNotExist()
    }

    @Test
    fun testMessageScreenIsDisplayedWhenPresentationVisibilityStateIsTrue() {
        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = getSettings(false),
                onCreated = { onCreatedCalled = true },
                onDisposed = { onDisposedCalled = true },
                onGestureDetected = { gesture -> detectedGestures.add(gesture) },
                onBackPressed = { onBackPressed = true }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).assertDoesNotExist()

        // Change the state of the presentation state manager to shown to display the message
        presentationStateManager.onShown()
        composeTestRule.waitForIdle()
        validateMessageAppeared(false)

        assertTrue(onCreatedCalled)
        assertFalse(onDisposedCalled)
        assertFalse(onBackPressed)
        assertTrue(detectedGestures.isEmpty())
    }

    @Test
    fun testMessageScreenIsRemovedWhenPresentationVisibilityStateChangesToFalse() {
        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = getSettings(false),
                onCreated = { onCreatedCalled = true },
                onDisposed = { onDisposedCalled = true },
                onGestureDetected = { gesture -> detectedGestures.add(gesture) },
                onBackPressed = { onBackPressed = true }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).assertDoesNotExist()

        // Change the state of the presentation state manager to shown to display the message
        presentationStateManager.onShown()
        composeTestRule.waitForIdle()
        validateMessageAppeared(false)

        // Change the state of the presentation state manager to hidden to remove the message
        presentationStateManager.onHidden()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_BACKDROP).assertDoesNotExist()

        assertTrue(onCreatedCalled)
        assertTrue(onDisposedCalled)
        assertFalse(onBackPressed)
        assertTrue(detectedGestures.isEmpty())
    }

    @Test
    fun testMessageScreenHasBackdropWhenUITakeOverIsEnabled() {
        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = getSettings(true),
                onCreated = { onCreatedCalled = true },
                onDisposed = { onDisposedCalled = true },
                onGestureDetected = { gesture -> detectedGestures.add(gesture) },
                onBackPressed = { onBackPressed = true }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).assertDoesNotExist()

        // Change the state of the presentation state manager to shown to display the message
        presentationStateManager.onShown()
        composeTestRule.waitForIdle()
        validateMessageAppeared(true)

        assertTrue(onCreatedCalled)
        assertFalse(onDisposedCalled)
        assertFalse(onBackPressed)
        assertTrue(detectedGestures.isEmpty())
    }

    @Test
    fun testMessageScreenRegistersBackPressWhenBackPressed() {
        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = getSettings(true),
                onCreated = { onCreatedCalled = true },
                onDisposed = { onDisposedCalled = true },
                onGestureDetected = { gesture -> detectedGestures.add(gesture) },
                onBackPressed = { onBackPressed = true }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).assertDoesNotExist()

        // Change the state of the presentation state manager to shown to display the message
        presentationStateManager.onShown()
        composeTestRule.waitForIdle()
        validateMessageAppeared(true)

        // Press back
        Espresso.pressBack()
        Espresso.onIdle()

        assertTrue(onCreatedCalled)
        assertFalse(onDisposedCalled)
        assertTrue(onBackPressed)
        assertTrue(detectedGestures.isEmpty())
    }

    @Test
    fun testMessageScreenRegistersAcceptedGesturesWhenUiTakeOverIsEnabled() {
        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = getSettings(true),
                onCreated = { onCreatedCalled = true },
                onDisposed = { onDisposedCalled = true },
                onGestureDetected = { gesture -> detectedGestures.add(gesture) },
                onBackPressed = { onBackPressed = true }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).assertDoesNotExist()

        // Change the state of the presentation state manager to shown to display the message
        presentationStateManager.onShown()
        composeTestRule.waitForIdle()
        validateMessageAppeared(true)
        // Swipe gestures
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).performGesture {
            // create a swipe right gesture
            swipeWithVelocity(
                start = Offset(100f, 10f),
                end = Offset(800f, 10f),
                endVelocity = 1000f
            )
        }
        composeTestRule.waitForIdle()

        assertTrue(onCreatedCalled)
        assertFalse(onDisposedCalled)
        assertFalse(onBackPressed)
        assertTrue(detectedGestures.contains(InAppMessageSettings.MessageGesture.SWIPE_RIGHT))
    }

    @Test
    fun testMessageScreenRegistersAcceptedGesturesWhenUiTakeOverIsDisabled() {
        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = getSettings(false),
                onCreated = { onCreatedCalled = true },
                onDisposed = { onDisposedCalled = true },
                onGestureDetected = { gesture -> detectedGestures.add(gesture) },
                onBackPressed = { onBackPressed = true }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).assertDoesNotExist()

        // Change the state of the presentation state manager to shown to display the message
        presentationStateManager.onShown()
        composeTestRule.waitForIdle()
        validateMessageAppeared(false)

        // Swipe gestures
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).performGesture {
            // create a swipe right gesture
            swipeWithVelocity(
                start = Offset(100f, 10f),
                end = Offset(800f, 10f),
                endVelocity = 1000f
            )
        }
        composeTestRule.waitForIdle()

        assertTrue(onCreatedCalled)
        assertFalse(onDisposedCalled)
        assertFalse(onBackPressed)
        assertTrue(detectedGestures.contains(InAppMessageSettings.MessageGesture.SWIPE_RIGHT))
    }

    @Test
    fun testMessageScreenDoesNotRegisterUnAcceptedGestures() {
        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = getSettings(true),
                onCreated = { onCreatedCalled = true },
                onDisposed = { onDisposedCalled = true },
                onGestureDetected = { gesture -> detectedGestures.add(gesture) },
                onBackPressed = { onBackPressed = true }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).assertDoesNotExist()

        // Change the state of the presentation state manager to shown to display the message
        presentationStateManager.onShown()
        composeTestRule.waitForIdle()
        validateMessageAppeared(true)

        // Swipe gestures
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).performGesture {
            // create a swipe down gesture
            swipeWithVelocity(
                start = Offset(0f, 10f),
                end = Offset(0f, 600f),
                endVelocity = 1000f
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_BACKDROP).performGesture {
            click(
                Offset(100f, 10f)
            )
        }
        composeTestRule.waitForIdle()

        assertTrue(onCreatedCalled)
        assertFalse(onDisposedCalled)
        assertFalse(onBackPressed)
        assertTrue(detectedGestures.contains(InAppMessageSettings.MessageGesture.TAP_BACKGROUND))
        assertFalse(detectedGestures.contains(InAppMessageSettings.MessageGesture.SWIPE_DOWN))
    }

    @After
    fun tearDown() {
        composeTestRule.waitForIdle()
        onBackPressed = false
        onCreatedCalled = false
        onDisposedCalled = false
        detectedGestures.clear()
    }

    private fun validateMessageAppeared(withBackdrop: Boolean) {
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).assertExists().assertIsDisplayed()
        if (withBackdrop) {
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_BACKDROP).assertExists().assertIsDisplayed()
                .assertIsDisplayed()
        } else {
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_BACKDROP).assertDoesNotExist()
        }
    }

    private fun getSettings(withUiTakeOver: Boolean): InAppMessageSettings {
        return InAppMessageSettings.Builder()
            .backdropOpacity(0.5f)
            .backgroundColor("#000000")
            .cornerRadius(10f)
            .displayAnimation(InAppMessageSettings.MessageAnimation.BOTTOM)
            .dismissAnimation(InAppMessageSettings.MessageAnimation.TOP)
            .height(60)
            .width(80)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.CENTER)
            .shouldTakeOverUi(withUiTakeOver)
            .content(HTML_TEXT_SAMPLE)
            .gestureMap(acceptedGestures)
            .build()
    }
}
