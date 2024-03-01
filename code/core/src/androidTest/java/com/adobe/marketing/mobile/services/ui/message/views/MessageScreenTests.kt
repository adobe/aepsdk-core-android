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

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Insets
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performGesture
import androidx.compose.ui.test.swipeWithVelocity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.services.ui.common.PresentationStateManager
import com.adobe.marketing.mobile.services.ui.message.InAppMessageSettings
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageScreenTests {
    @get: Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

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

    // ----------------------------------------------------------------------------------------------
    // Test cases for ui take over
    // ----------------------------------------------------------------------------------------------
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

    // ----------------------------------------------------------------------------------------------
    // Test cases for gestures
    // ----------------------------------------------------------------------------------------------
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
    fun testMessageScreenDoesNotRegisterUnacceptedGestures() {
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

    // ----------------------------------------------------------------------------------------------
    // Test cases for orientation changes
    // ----------------------------------------------------------------------------------------------
    @Test
    fun testMessageScreenIsRestoredOnOrientationChange() {
        val restorationTester = StateRestorationTester(composeTestRule)
        restorationTester.setContent { // setting our composable as content for test
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

        // Simulate recreation of the composable when happens when activity is recreated on orientation change
        restorationTester.emulateSavedInstanceStateRestore()

        composeTestRule.waitForIdle()
        validateMessageAppeared(false)
    }

    // ----------------------------------------------------------------------------------------------
    // Test cases for sizing
    // ----------------------------------------------------------------------------------------------
    @Test
    fun testMessageScreenSizeWhenWidthAndHeightSetToMax() {
        val settings = InAppMessageSettings.Builder()
            .height(100)
            .width(100)
            .content(HTML_TEXT_SAMPLE)
            .build()

        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
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

        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME)
            .assertHeightIsEqualTo(composeTestRule.onRoot().getBoundsInRoot().height)
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .assertHeightIsEqualTo(composeTestRule.onRoot().getBoundsInRoot().height)
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME)
            .assertWidthIsEqualTo(composeTestRule.onRoot().getBoundsInRoot().width)
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .assertWidthIsEqualTo(composeTestRule.onRoot().getBoundsInRoot().width)

        val activityHeightDp = getActivityHeightInDp(composeTestRule.activity)
        val screenWidthDp = getScreenWidthInDp(composeTestRule.activity)

        // Message Frame all the available height and width allowed by the parent (in this case ComponentActivity)
        val frameBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        frameBounds.width.assertIsEqualTo(screenWidthDp, "failed", Dp(2f))
        frameBounds.height.assertIsEqualTo(activityHeightDp, "failed", Dp(2f))

        // Message Content(WebView) is 100% of height and width, as allowed by the activity
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getUnclippedBoundsInRoot()
        contentBounds.height.assertIsEqualTo(activityHeightDp, "failed", Dp(2f))
        contentBounds.width.assertIsEqualTo(screenWidthDp, "failed", Dp(2f))
    }

    @Test
    fun testMessageScreenSizeWhenWidthAndHeightSetToSpecificPercentage() {
        val heightPercentage = 95
        val widthPercentage = 60
        val settings = InAppMessageSettings.Builder()
            .height(heightPercentage)
            .width(widthPercentage)
            .content(HTML_TEXT_SAMPLE)
            .build()

        composeTestRule.setContent {
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
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

        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME)
            .assertHeightIsEqualTo(composeTestRule.onRoot().getBoundsInRoot().height)
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME)
            .assertWidthIsEqualTo(composeTestRule.onRoot().getBoundsInRoot().width)

        // WebView height is calculated based on the screen height and width
        val screenHeightDp = getScreenHeightInDp(composeTestRule.activity)
        val activityHeightDp = getActivityHeightInDp(composeTestRule.activity)
        val screenWidthDp = getScreenWidthInDp(composeTestRule.activity)

        // Message Frame all the available height and width allowed by the parent (in this case ComponentActivity)
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        frameBounds.height.assertIsEqualTo(activityHeightDp, "failed", Dp(2f))
        frameBounds.width.assertIsEqualTo(screenWidthDp, "failed", Dp(2f))

        // Message Content(WebView) is 95% of the screen height and 60% of the screen width.
        // If the height exceeds what is allowed by the activity (due to actionbar), it takes up the full height of the activity
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .getUnclippedBoundsInRoot()
        if ((screenHeightDp * (heightPercentage / 100f)) > activityHeightDp) {
            contentBounds.height.assertIsEqualTo(activityHeightDp, "failed", Dp(2f))
        } else {
            contentBounds.height.assertIsEqualTo(screenHeightDp * (heightPercentage / 100f), "failed", Dp(2f))
        }
        contentBounds.width.assertIsEqualTo(screenWidthDp * (widthPercentage / 100f), "failed", Dp(2f))
    }

    // ----------------------------------------------------------------------------------------------
    // Test cases for alignment
    // ----------------------------------------------------------------------------------------------
    @Test
    fun testMessageScreenAlignmentWhenTopAligned() {
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(60)
            .width(80)
            .verticalAlignment(InAppMessageSettings.MessageAlignment.TOP)
            .build()

        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
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

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getBoundsInRoot()
        assertEquals(rootBounds.top, frameBounds.top)
        assertEquals(rootBounds.bottom, frameBounds.bottom)
        assertEquals(rootBounds.top, contentBounds.top)
        assertTrue(rootBounds.bottom > contentBounds.bottom)
    }

    @Test
    fun testMessageScreenAlignmentWhenBottomAligned() {
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(60)
            .width(80)
            .verticalAlignment(InAppMessageSettings.MessageAlignment.BOTTOM)
            .build()

        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
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

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getBoundsInRoot()
        assertEquals(rootBounds.top, frameBounds.top)
        assertEquals(rootBounds.bottom, frameBounds.bottom)
        assertTrue(rootBounds.top < contentBounds.top)
        assertEquals(rootBounds.bottom, contentBounds.bottom)
    }

    @Test
    fun testMessageScreenAlignmentWhenLeftAligned() {
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(60)
            .width(80)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.LEFT)
            .build()

        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
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

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getBoundsInRoot()
        rootBounds.left.assertIsEqualTo(frameBounds.left, "failed", Dp(2f))
        rootBounds.right.assertIsEqualTo(frameBounds.right, "failed", Dp(2f))
        rootBounds.left.assertIsEqualTo(contentBounds.left, "failed", Dp(2f))
        assertTrue(rootBounds.right > contentBounds.right)
    }

    @Test
    fun testMessageScreenAlignmentWhenRightAligned() {
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(60)
            .width(80)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.RIGHT)
            .build()

        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
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

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getBoundsInRoot()
        rootBounds.left.assertIsEqualTo(frameBounds.left, "failed", Dp(2f))
        rootBounds.right.assertIsEqualTo(frameBounds.right, "failed", Dp(2f))
        assertTrue(rootBounds.left < contentBounds.left)
        rootBounds.right.assertIsEqualTo(contentBounds.right, "failed", Dp(2f))
    }

    // ----------------------------------------------------------------------------------------------
    // Test cases for alignment with insets
    // ----------------------------------------------------------------------------------------------
    @Test
    fun testMessageScreenAlignmentWhenTopAlignedWithPositiveInset() {
        val offsetPercent = 20
        val heightPercent = 60
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(80)
            .verticalAlignment(InAppMessageSettings.MessageAlignment.TOP)
            .verticalInset(offsetPercent)
            .build()

        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
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
        validateMessageExistsWhenClipped(false)

        val frameBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getUnclippedBoundsInRoot()
        val screenHeightDp = getScreenHeightInDp(composeTestRule.activity)
        val activityHeightDp = getActivityHeightInDp(composeTestRule.activity)
        val offsetDp = screenHeightDp * offsetPercent.toFloat() / 100f
        val contentHeightDp = screenHeightDp * heightPercent.toFloat() / 100f

        // Frame and content is offset downwards from the top edge of the activity by the inset value
        frameBounds.top.assertIsEqualTo(offsetDp, "failed", Dp(2f))
        frameBounds.bottom.assertIsEqualTo(activityHeightDp + offsetDp, "failed", Dp(2f))
        contentBounds.top.assertIsEqualTo(offsetDp, "failed", Dp(2f))
        contentBounds.bottom.assertIsEqualTo(contentHeightDp + offsetDp, "failed", Dp(2f))
    }

    @Test
    fun testMessageScreenAlignmentWhenTopAlignedWithNegativeInset() {
        val offsetPercent = 20
        val heightPercent = 60
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(80)
            .verticalAlignment(InAppMessageSettings.MessageAlignment.TOP)
            .verticalInset(-offsetPercent)
            .build()

        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
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
        validateMessageExistsWhenClipped(false)

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getUnclippedBoundsInRoot()
        val screenHeightDp = getScreenHeightInDp(composeTestRule.activity)
        val activityHeightDp = getActivityHeightInDp(composeTestRule.activity)
        val offsetDp = screenHeightDp * offsetPercent.toFloat() / 100f
        val contentHeightDp = screenHeightDp * heightPercent.toFloat() / 100f

        // Frame and content is offset upwards from the top edge of the activity by the inset value, leading to clipping
        frameBounds.top.assertIsEqualTo(-offsetDp, "failed", Dp(2f))
        frameBounds.bottom.assertIsEqualTo(activityHeightDp - offsetDp, "failed", Dp(2f))
        contentBounds.top.assertIsEqualTo(-offsetDp, "failed", Dp(2f))
        contentBounds.bottom.assertIsEqualTo(contentHeightDp - offsetDp, "failed", Dp(2f))
    }

    @Test
    fun testMessageScreenAlignmentWhenBottomAlignedWithPositiveInset() {
        val offsetPercent = 20
        val heightPercent = 60
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(80)
            .verticalAlignment(InAppMessageSettings.MessageAlignment.BOTTOM)
            .verticalInset(offsetPercent)
            .build()

        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
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
        validateMessageExistsWhenClipped(false)

        val frameBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getUnclippedBoundsInRoot()
        val screenHeightDp = getScreenHeightInDp(composeTestRule.activity)
        val activityHeightDp = getActivityHeightInDp(composeTestRule.activity)
        val offsetDp = screenHeightDp * offsetPercent.toFloat() / 100f
        val contentHeightDp = screenHeightDp * heightPercent.toFloat() / 100f

        // Frame and content is offset upwards from the bottom edge of the activity by the inset value
        frameBounds.top.assertIsEqualTo(-offsetDp, "failed", Dp(2f))
        frameBounds.bottom.assertIsEqualTo(activityHeightDp - offsetDp, "failed", Dp(2f))
        contentBounds.top.assertIsEqualTo(activityHeightDp - contentHeightDp - offsetDp, "failed", Dp(2f))
        contentBounds.bottom.assertIsEqualTo(activityHeightDp - offsetDp, "failed", Dp(2f))
    }

    @Test
    fun testMessageScreenAlignmentWhenBottomAlignedWithNegativeInset() {
        val offsetPercent = 20
        val heightPercent = 60
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(80)
            .verticalAlignment(InAppMessageSettings.MessageAlignment.BOTTOM)
            .verticalInset(-offsetPercent)
            .build()

        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
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
        validateMessageExistsWhenClipped(false)

        val frameBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getUnclippedBoundsInRoot()
        val screenHeightDp = getScreenHeightInDp(composeTestRule.activity)
        val activityHeightDp = getActivityHeightInDp(composeTestRule.activity)
        val offsetDp = screenHeightDp * offsetPercent.toFloat() / 100f
        val contentHeightDp = screenHeightDp * heightPercent.toFloat() / 100f

        // Frame and content is offset upwards from the bottom edge of the activity by the inset value
        frameBounds.top.assertIsEqualTo(offsetDp, "failed", Dp(2f))
        frameBounds.bottom.assertIsEqualTo(activityHeightDp + offsetDp, "failed", Dp(2f))
        contentBounds.top.assertIsEqualTo(activityHeightDp - contentHeightDp + offsetDp, "failed", Dp(2f))
        contentBounds.bottom.assertIsEqualTo(activityHeightDp + offsetDp, "failed", Dp(2f))
    }

    @Test
    fun testMessageScreenAlignmentWhenLeftAlignedWithPositiveInset() {
        val offsetPercent = 20
        val widthPercent = 90
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(60)
            .width(widthPercent)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.LEFT)
            .horizontalInset(offsetPercent)
            .build()

        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
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
        validateMessageExistsWhenClipped(false)

        val frameBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getUnclippedBoundsInRoot()
        val screenWidthDp = getScreenWidthInDp(composeTestRule.activity)
        val activityWidthDp = getActivityWidthInDp(composeTestRule.activity)
        val offsetDp = screenWidthDp * offsetPercent.toFloat() / 100f
        val contentWidthDp = screenWidthDp * widthPercent.toFloat() / 100f

        // Frame and content is offset rightwards from the left edge of the activity by the inset value
        frameBounds.left.assertIsEqualTo(offsetDp, "failed", Dp(2f))
        frameBounds.right.assertIsEqualTo(activityWidthDp + offsetDp, "failed", Dp(2f))
        contentBounds.left.assertIsEqualTo(offsetDp, "failed", Dp(2f))
        contentBounds.right.assertIsEqualTo(contentWidthDp + offsetDp, "failed", Dp(2f))
    }

    @Test
    fun testMessageScreenAlignmentWhenLeftAlignedWithNegativeInset() {
        val offsetPercent = 20
        val widthPercent = 90
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(60)
            .width(widthPercent)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.LEFT)
            .horizontalInset(-offsetPercent)
            .build()

        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
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
        validateMessageExistsWhenClipped(false)

        val frameBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getUnclippedBoundsInRoot()
        val screenWidthDp = getScreenWidthInDp(composeTestRule.activity)
        val activityWidthDp = getActivityWidthInDp(composeTestRule.activity)
        val offsetDp = screenWidthDp * offsetPercent.toFloat() / 100f
        val contentWidthDp = screenWidthDp * widthPercent.toFloat() / 100f

        // Frame and content is offset leftwards from the left edge of the activity by the inset value, leading to clipping
        frameBounds.left.assertIsEqualTo(-offsetDp, "failed", Dp(2f))
        frameBounds.right.assertIsEqualTo(activityWidthDp - offsetDp, "failed", Dp(2f))
        contentBounds.left.assertIsEqualTo(-offsetDp, "failed", Dp(2f))
        contentBounds.right.assertIsEqualTo(contentWidthDp - offsetDp, "failed", Dp(2f))
    }

    @Test
    fun testMessageScreenAlignmentWhenRightAlignedWithPositiveInset() {
        val offsetPercent = 20
        val widthPercent = 90
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(60)
            .width(widthPercent)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.RIGHT)
            .horizontalInset(offsetPercent)
            .build()

        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
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
        validateMessageExistsWhenClipped(false)

        val frameBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getUnclippedBoundsInRoot()
        val screenWidthDp = getScreenWidthInDp(composeTestRule.activity)
        val activityWidthDp = getActivityWidthInDp(composeTestRule.activity)
        val offsetDp = screenWidthDp * offsetPercent.toFloat() / 100f
        val contentWidthDp = screenWidthDp * widthPercent.toFloat() / 100f

        // Frame and content is offset leftwards from the right edge of the activity by the inset value
        frameBounds.left.assertIsEqualTo(-offsetDp, "failed", Dp(2f))
        frameBounds.right.assertIsEqualTo(activityWidthDp - offsetDp, "failed", Dp(2f))
        contentBounds.left.assertIsEqualTo(activityWidthDp - contentWidthDp - offsetDp, "failed", Dp(2f))
        contentBounds.right.assertIsEqualTo(activityWidthDp - offsetDp, "failed", Dp(2f))
    }

    @Test
    fun testMessageScreenAlignmentWhenRightAlignedWithNegativeInset() {
        val offsetPercent = 20
        val widthPercent = 90
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(60)
            .width(widthPercent)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.RIGHT)
            .horizontalInset(-offsetPercent)
            .build()

        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
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
        validateMessageExistsWhenClipped(false)

        val frameBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getUnclippedBoundsInRoot()
        val screenWidthDp = getScreenWidthInDp(composeTestRule.activity)
        val activityWidthDp = getActivityWidthInDp(composeTestRule.activity)
        val offsetDp = screenWidthDp * offsetPercent.toFloat() / 100f
        val contentWidthDp = screenWidthDp * widthPercent.toFloat() / 100f

        // Frame and content is offset rightwards from the right edge of the activity by the inset value, leading to clipping
        frameBounds.left.assertIsEqualTo(offsetDp, "failed", Dp(2f))
        frameBounds.right.assertIsEqualTo(activityWidthDp + offsetDp, "failed", Dp(2f))
        contentBounds.left.assertIsEqualTo(activityWidthDp - contentWidthDp + offsetDp, "failed", Dp(2f))
        contentBounds.right.assertIsEqualTo(activityWidthDp + offsetDp, "failed", Dp(2f))
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
        } else {
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_BACKDROP).assertDoesNotExist()
        }
    }

    private fun validateMessageExistsWhenClipped(withBackdrop: Boolean) {
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).assertExists()
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).assertExists()
        if (withBackdrop) {
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_BACKDROP).assertExists()
        } else {
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_BACKDROP).assertDoesNotExist()
        }
    }

    private fun getSettings(withUiTakeOver: Boolean): InAppMessageSettings {
        return InAppMessageSettings.Builder()
            // .backdropOpacity(0.5f)
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

    private fun getScreenWidthInDp(activity: Activity): Dp {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val bounds: Rect = windowMetrics.bounds
            val insets: Insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars()
            )
            val density = activity.resources.displayMetrics.density
            if (activity.resources.configuration.orientation
                == Configuration.ORIENTATION_LANDSCAPE &&
                activity.resources.configuration.smallestScreenWidthDp < 600
            ) { // landscape and phone
                val navigationBarSize: Int = insets.right + insets.left
                Dp((bounds.width() - navigationBarSize) / density)
            } else { // portrait or tablet
                Dp(bounds.width() / density)
            }
        } else {
            val outMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(outMetrics)
            val density = outMetrics.density
            Dp(outMetrics.widthPixels / density)
        }
    }

    private fun getScreenHeightInDp(activity: Activity): Dp {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val bounds: Rect = windowMetrics.bounds
            val insets: Insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars()
            )
            val density = activity.resources.displayMetrics.density
            if (activity.resources.configuration.orientation
                == Configuration.ORIENTATION_LANDSCAPE &&
                activity.resources.configuration.smallestScreenWidthDp < 600
            ) { // landscape and phone
                Dp(bounds.height() / density)
            } else { // portrait or tablet
                val navigationBarSize: Int = insets.bottom
                Dp((bounds.height() - navigationBarSize) / density)
            }
        } else {
            val outMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(outMetrics)
            val density = outMetrics.density
            Dp(outMetrics.heightPixels / density)
        }
    }

    private fun getActivityHeightInDp(activity: Activity): Dp {
        val density = activity.resources.displayMetrics.density
        val activityRoot = activity.window.decorView.findViewById<View>(android.R.id.content)
        if (activityRoot != null) {
            return Dp(activityRoot.height.toFloat() / density)
        }
        return Dp(0f)
    }

    private fun getActivityWidthInDp(activity: Activity): Dp {
        val density = activity.resources.displayMetrics.density
        val activityRoot = activity.window.decorView.findViewById<View>(android.R.id.content)
        if (activityRoot != null) {
            return Dp(activityRoot.width.toFloat() / density)
        }
        return Dp(0f)
    }
}
