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
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeWithVelocity
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.services.ui.common.PresentationStateManager
import com.adobe.marketing.mobile.services.ui.message.DefaultInAppMessageEventHandler
import com.adobe.marketing.mobile.services.ui.message.InAppMessageSettings
import com.adobe.marketing.mobile.services.ui.message.views.MessageScreenTestHelper.validateBounds
import com.adobe.marketing.mobile.services.ui.message.views.MessageScreenTestHelper.validateMessageAppeared
import com.adobe.marketing.mobile.services.ui.message.views.MessageScreenTestHelper.validateViewSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.After
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
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = false
        )

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
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = false
        )

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
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = true,
            clipped = false
        )

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
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = true,
            clipped = false
        )

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
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = true,
            clipped = false
        )
        // Swipe gestures
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).performTouchInput {
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
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = false
        )

        // Swipe gestures
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).performTouchInput {
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
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = true,
            clipped = false
        )

        // Swipe gestures
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).assertIsDisplayed()
            .performTouchInput {
                // create a swipe down gesture
                swipeWithVelocity(
                    start = Offset(0f, 10f),
                    end = Offset(0f, 600f),
                    endVelocity = 1000f
                )
            }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_BACKDROP).assertIsDisplayed()
            .performTouchInput {
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
    // Test cases for configuration changes
    // ----------------------------------------------------------------------------------------------
    @Test
    fun testMessageScreenIsRestoredOnConfigurationChange() {
        val restorationTester = StateRestorationTester(composeTestRule)
        restorationTester.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = getSettings(false),
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = false
        )

        assertTrue(onCreatedCalled)
        assertFalse(onDisposedCalled)
        assertFalse(onBackPressed)
        assertTrue(detectedGestures.isEmpty())

        // Simulate recreation of the composable when activity is recreated on orientation change
        restorationTester.emulateSavedInstanceStateRestore()

        composeTestRule.waitForIdle()
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = false
        )
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

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        var messageContentWidthDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)
            messageContentWidthDp = ((contentViewWidthDp * settings.width) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = false
        )

        // Message Frame all the available height and width allowed by the parent (in this case ComponentActivity)
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        validateViewSize(frameBounds, contentViewHeightDp, contentViewWidthDp)

        // Message Content(WebView) is 100% of height and width, as allowed by the activity
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .getUnclippedBoundsInRoot()
        validateViewSize(contentBounds, messageContentHeightDp, messageContentWidthDp)
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

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = false
        )

        // Message Frame all the available height and width allowed by the parent (in this case ComponentActivity)
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        validateViewSize(frameBounds, contentViewHeightDp, contentViewWidthDp)

        // Message Content(WebView) is 95% of the screen height and heightPercent% of the screen width.
        // If the height exceeds what is allowed by the activity (due to actionbar), it takes up the full height of the activity
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .getUnclippedBoundsInRoot()
        if ((contentViewHeightDp * (heightPercentage / 100f)) > messageContentHeightDp) {
            validateViewSize(
                contentBounds,
                messageContentHeightDp,
                contentViewWidthDp * (widthPercentage / 100f)
            )
        } else {
            validateViewSize(
                contentBounds,
                contentViewHeightDp * (heightPercentage / 100f),
                contentViewWidthDp * (widthPercentage / 100f)
            )
        }
    }

    // ----------------------------------------------------------------------------------------------
    // Test cases for alignment
    // ----------------------------------------------------------------------------------------------
    @Test
    fun testMessageScreenAlignmentWhenTopAligned() {
        val heightPercent = 60
        val widthPercent = 80
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(widthPercent)
            .verticalAlignment(InAppMessageSettings.MessageAlignment.TOP)
            .build()

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = false
        )

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getBoundsInRoot()
        val contentBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getBoundsInRoot()
        val horizontalContentPaddingDp = (contentViewWidthDp * (100 - widthPercent).toFloat() / 100f) / 2

        // Frame is same size as its parent so bounds should match the root bounds
        validateBounds(
            frameBounds,
            rootBounds.top,
            rootBounds.bottom,
            rootBounds.left,
            rootBounds.right
        )

        // Content is top aligned vertically and centered horizontally and takes 80% of screen width
        validateBounds(
            contentBounds,
            rootBounds.top, // top bound is same as the top bound of root
            rootBounds.top + messageContentHeightDp, // bottom bound is top bound plus content height
            rootBounds.left + horizontalContentPaddingDp, // left bound is left bound of root plus content padding
            rootBounds.right - horizontalContentPaddingDp // right bound is right bound of root minus content padding
        )
    }

    @Test
    fun testMessageScreenAlignmentWhenBottomAligned() {
        val heightPercent = 60
        val widthPercent = 80
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(widthPercent)
            .verticalAlignment(InAppMessageSettings.MessageAlignment.BOTTOM)
            .build()

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = false
        )

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getBoundsInRoot()
        val contentBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getBoundsInRoot()
        val horizontalContentPaddingDp = (contentViewWidthDp * (100 - widthPercent).toFloat() / 100f) / 2

        // Frame is same size as its parent so bounds should match the root bounds
        validateBounds(
            frameBounds,
            rootBounds.top,
            rootBounds.bottom,
            rootBounds.left,
            rootBounds.right
        )

        // Content is bottom aligned vertically and centered horizontally and takes 80% of screen width
        validateBounds(
            contentBounds,
            rootBounds.bottom - messageContentHeightDp, // top bound is bottom bound minus content height
            rootBounds.bottom, // bottom bound is same as the bottom bound of root
            rootBounds.left + horizontalContentPaddingDp, // left bound is left bound of root plus content padding
            rootBounds.right - horizontalContentPaddingDp // right bound is right bound of root minus content padding
        )
    }

    @Test
    fun testMessageScreenAlignmentWhenLeftAligned() {
        val heightPercent = 60
        val widthPercent = 80
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(widthPercent)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.LEFT)
            .build()

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        var messageContentWidthDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)
            messageContentWidthDp = ((contentViewWidthDp * settings.width) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = false
        )

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getBoundsInRoot()
        val contentBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getBoundsInRoot()

        // Frame is same size as its parent so bounds should match the root bounds
        validateBounds(
            frameBounds,
            rootBounds.top,
            rootBounds.bottom,
            rootBounds.left,
            rootBounds.right
        )

        // Content is center aligned vertically and left aligned horizontally and takes heightPercent% of screen height
        validateBounds(
            contentBounds,
            rootBounds.top + ((contentViewHeightDp - messageContentHeightDp) / 2), // top bound is half of the difference between the activity height and the content height
            rootBounds.bottom - ((contentViewHeightDp - messageContentHeightDp) / 2), // top bound is the remaining half of the difference between the activity height and the content height
            rootBounds.left, // left bound is same as left bound of the root
            rootBounds.left + messageContentWidthDp // right bound is left bound plus width of content
        )
    }

    @Test
    fun testMessageScreenAlignmentWhenRightAligned() {
        val heightPercent = 60
        val widthPercent = 80
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(widthPercent)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.RIGHT)
            .build()

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        var messageContentWidthDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)
            messageContentWidthDp = ((contentViewWidthDp * settings.width) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = false
        )

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getBoundsInRoot()
        val contentBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getBoundsInRoot()

        // Frame is same size as its parent so bounds should match the root bounds
        validateBounds(
            frameBounds,
            rootBounds.top,
            rootBounds.bottom,
            rootBounds.left,
            rootBounds.right
        )

        // Content is center aligned vertically and right aligned horizontally and takes heightPercent% of screen height
        validateBounds(
            contentBounds,
            rootBounds.top + ((contentViewHeightDp - messageContentHeightDp) / 2), // top bound is top bound of root plus half of the difference between the activity height and the content height
            rootBounds.bottom - ((contentViewHeightDp - messageContentHeightDp) / 2), // bottom bound is bottom bound of root plys the remaining half of the difference between the activity height and the content height
            rootBounds.right - messageContentWidthDp, // left bound is right bound minus width of content
            rootBounds.right // right bound is same as right bound of the root
        )
    }

    @Test
    fun testMessageScreenAlignmentWhenBottomRightAligned() {
        val heightPercent = 60
        val widthPercent = 80
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(widthPercent)
            .verticalAlignment(InAppMessageSettings.MessageAlignment.BOTTOM)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.RIGHT)
            .build()

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        var messageContentWidthDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)
            messageContentWidthDp = ((contentViewWidthDp * settings.width) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = false
        )

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getBoundsInRoot()
        val contentBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).getBoundsInRoot()

        // Frame is same size as its parent so bounds should match the root bounds
        validateBounds(
            frameBounds,
            rootBounds.top,
            rootBounds.bottom,
            rootBounds.left,
            rootBounds.right
        )

        // Content is center aligned vertically and right aligned horizontally and takes heightPercent% of screen height
        validateBounds(
            contentBounds,
            rootBounds.bottom - messageContentHeightDp, // top bound is bottom bound minus content height
            rootBounds.bottom, // bottom bound is same as the bottom bound of root
            rootBounds.right - messageContentWidthDp, // left bound is right bound minus width of content
            rootBounds.right // right bound is same as right bound of the root
        )
    }

    // ----------------------------------------------------------------------------------------------
    // Test cases for alignment with insets
    // ----------------------------------------------------------------------------------------------
    @Test
    fun testMessageScreenAlignmentWhenTopAlignedWithPositiveInset() {
        val heightPercent = 60
        val widthPercent = 80
        val offsetPercent = 20
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(widthPercent)
            .verticalAlignment(InAppMessageSettings.MessageAlignment.TOP)
            .verticalInset(offsetPercent)
            .build()

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = true
        )

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .getUnclippedBoundsInRoot()
        val horizontalContentPaddingDp = (contentViewWidthDp * (100 - widthPercent).toFloat() / 100f) / 2
        val offsetDp = messageContentHeightDp * offsetPercent.toFloat() / 100f

        // Frame top and bottom bounds are offset downwards by the inset value from the top
        validateBounds(
            frameBounds,
            rootBounds.top + offsetDp,
            rootBounds.bottom + offsetDp,
            rootBounds.left,
            rootBounds.right
        )

        // Content is top aligned vertically and offset downwards and centered horizontally and takes 80% of screen width
        validateBounds(
            contentBounds,
            rootBounds.top + offsetDp, // top bound is offset downwards by the inset value from the top bound of the root
            rootBounds.top + messageContentHeightDp + offsetDp, // bottom bound is the top bound of the root plus content height offset downwards by the inset value
            rootBounds.left + horizontalContentPaddingDp, // left bound is left bound of root plus content padding
            rootBounds.right - horizontalContentPaddingDp // right bound is right bound of root minus content padding
        )
    }

    @Test
    fun testMessageScreenAlignmentWhenTopAlignedWithNegativeInset() {
        val heightPercent = 60
        val widthPercent = 80
        val offsetPercent = 20
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(widthPercent)
            .verticalAlignment(InAppMessageSettings.MessageAlignment.TOP)
            .verticalInset(-offsetPercent)
            .build()

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = true
        )

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .getUnclippedBoundsInRoot()
        val horizontalContentPaddingDp = (contentViewWidthDp * (100 - widthPercent).toFloat() / 100f) / 2
        val offsetDp = messageContentHeightDp * offsetPercent.toFloat() / 100f

        // Frame top and bottom bounds are offset upwards by the inset value from the top
        validateBounds(
            frameBounds,
            rootBounds.top - offsetDp,
            rootBounds.bottom - offsetDp,
            rootBounds.left,
            rootBounds.right
        )

        // Content is top aligned vertically and offset upwards and centered horizontally and takes 80% of screen width
        validateBounds(
            contentBounds,
            rootBounds.top - offsetDp, // top bound is offset upwards by the inset value from the top bound of the root
            rootBounds.top + messageContentHeightDp - offsetDp, // bottom bound is the top bound of the root plus content height offset upwards by the inset value
            rootBounds.left + horizontalContentPaddingDp, // left bound is left bound of root plus content padding
            rootBounds.right - horizontalContentPaddingDp // right bound is right bound of root minus content padding
        )
    }

    @Test
    fun testMessageScreenAlignmentWhenBottomAlignedWithPositiveInset() {
        val heightPercent = 60
        val widthPercent = 80
        val offsetPercent = 20
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(widthPercent)
            .verticalAlignment(InAppMessageSettings.MessageAlignment.BOTTOM)
            .verticalInset(offsetPercent)
            .build()

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = true
        )

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .getUnclippedBoundsInRoot()
        val horizontalContentPaddingDp = (contentViewWidthDp * (100 - widthPercent).toFloat() / 100f) / 2
        val offsetDp = messageContentHeightDp * offsetPercent.toFloat() / 100f

        // Frame top and bottom bounds are offset upwards by the inset value from the bottom
        validateBounds(
            frameBounds,
            rootBounds.top - offsetDp,
            rootBounds.bottom - offsetDp,
            rootBounds.left,
            rootBounds.right
        )

        // Content is bottom aligned vertically and offset upwards and centered horizontally and takes 80% of screen width
        validateBounds(
            contentBounds,
            rootBounds.bottom - messageContentHeightDp - offsetDp, // top bound is the bottom bound of the root minus content height offset upwards by the inset value
            rootBounds.bottom - offsetDp, // bottom bound is the bottom bound of the root offset upwards by the inset value
            rootBounds.left + horizontalContentPaddingDp, // left bound is left bound of root plus content padding
            rootBounds.right - horizontalContentPaddingDp // right bound is right bound of root minus content padding
        )
    }

    @Test
    fun testMessageScreenAlignmentWhenBottomAlignedWithNegativeInset() {
        val heightPercent = 60
        val widthPercent = 80
        val offsetPercent = 20
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(widthPercent)
            .verticalAlignment(InAppMessageSettings.MessageAlignment.BOTTOM)
            .verticalInset(-offsetPercent)
            .build()

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = true
        )

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .getUnclippedBoundsInRoot()
        val horizontalContentPaddingDp = (contentViewWidthDp * (100 - widthPercent).toFloat() / 100f) / 2
        val offsetDp = messageContentHeightDp * offsetPercent.toFloat() / 100f

        // Frame top and bottom bounds are offset downwards by the inset value from the bottom
        validateBounds(
            frameBounds,
            rootBounds.top + offsetDp,
            rootBounds.bottom + offsetDp,
            rootBounds.left,
            rootBounds.right
        )

        // Content is bottom aligned vertically and offset upwards and centered horizontally and takes 80% of screen width
        validateBounds(
            contentBounds,
            rootBounds.bottom - messageContentHeightDp + offsetDp, // top bound is the bottom bound of the root minus content height offset downwards by the inset value
            rootBounds.bottom + offsetDp, // bottom bound is the bottom bound of the root offset downwards by the inset value
            rootBounds.left + horizontalContentPaddingDp, // left bound is left bound of root plus content padding
            rootBounds.right - horizontalContentPaddingDp // right bound is right bound of root minus content padding
        )
    }

    @Test
    fun testMessageScreenAlignmentWhenLeftAlignedWithPositiveInset() {
        val heightPercent = 60
        val widthPercent = 80
        val offsetPercent = 20
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(widthPercent)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.LEFT)
            .horizontalInset(offsetPercent)
            .build()

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        var messageContentWidthDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)
            messageContentWidthDp = ((contentViewWidthDp * settings.width) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = true
        )

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .getUnclippedBoundsInRoot()
        val verticalContentPaddingDp = (contentViewHeightDp - messageContentHeightDp) / 2
        val offsetDp = messageContentWidthDp * offsetPercent.toFloat() / 100f

        // Frame left and right bounds are offset leftwards by the inset value from the left
        validateBounds(
            frameBounds,
            rootBounds.top,
            rootBounds.bottom,
            rootBounds.left + offsetDp,
            rootBounds.right + offsetDp
        )

        // Content is center aligned vertically and left aligned horizontally offset leftwards and takes 60% of screen height
        validateBounds(
            contentBounds,
            rootBounds.top + verticalContentPaddingDp, // top bound is the top round of the root plus content padding
            rootBounds.bottom - verticalContentPaddingDp, // bottom bound is the bottom bound of the root minus content padding
            rootBounds.left + offsetDp, // left bound is the left bound of the root offset leftwards by the inset value
            rootBounds.left + messageContentWidthDp + offsetDp // right bound is left bound of root plus content width offset leftwards by the inset value
        )
    }

    @Test
    fun testMessageScreenAlignmentWhenLeftAlignedWithNegativeInset() {
        val heightPercent = 60
        val widthPercent = 80
        val offsetPercent = 20
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(widthPercent)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.LEFT)
            .horizontalInset(-offsetPercent)
            .build()

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        var messageContentWidthDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)
            messageContentWidthDp = ((contentViewWidthDp * settings.width) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = true
        )

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .getUnclippedBoundsInRoot()
        val verticalContentPaddingDp = (contentViewHeightDp - messageContentHeightDp) / 2
        val offsetDp = messageContentWidthDp * offsetPercent.toFloat() / 100f

        // Frame left and right bounds are offset rightwards by the inset value from the left
        validateBounds(
            frameBounds,
            rootBounds.top,
            rootBounds.bottom,
            rootBounds.left - offsetDp,
            rootBounds.right - offsetDp
        )

        // Content is center aligned vertically and left aligned horizontally offset leftwards and takes 60% of screen height
        validateBounds(
            contentBounds,
            rootBounds.top + verticalContentPaddingDp, // top bound is the top round of the root plus content padding
            rootBounds.bottom - verticalContentPaddingDp, // bottom bound is the bottom bound of the root minus content padding
            rootBounds.left - offsetDp, // left bound is the left bound of the root offset rightwards by the inset value
            rootBounds.left + messageContentWidthDp - offsetDp // right bound is left bound of root plus content width offset rightwards by the inset value
        )
    }

    @Test
    fun testMessageScreenAlignmentWhenRightAlignedWithPositiveInset() {
        val heightPercent = 60
        val widthPercent = 80
        val offsetPercent = 20
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(widthPercent)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.RIGHT)
            .horizontalInset(offsetPercent)
            .build()

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        var messageContentWidthDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)
            messageContentWidthDp = ((contentViewWidthDp * settings.width) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = true
        )

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .getUnclippedBoundsInRoot()
        val verticalContentPaddingDp = (contentViewHeightDp - messageContentHeightDp) / 2
        val offsetDp = messageContentWidthDp * offsetPercent.toFloat() / 100f

        // Frame left and right bounds are offset leftwards by the inset value from the right
        validateBounds(
            frameBounds,
            rootBounds.top,
            rootBounds.bottom,
            rootBounds.left - offsetDp,
            rootBounds.right - offsetDp
        )

        // Content is center aligned vertically and right aligned horizontally offset leftwards and takes 60% of screen height
        validateBounds(
            contentBounds,
            rootBounds.top + verticalContentPaddingDp, // top bound is the top round of the root plus content padding
            rootBounds.bottom - verticalContentPaddingDp, // bottom bound is the bottom bound of the root minus content padding
            rootBounds.right - messageContentWidthDp - offsetDp, // left bound is the right bound of the root minus content width offset leftwards by the inset value
            rootBounds.right - offsetDp // right bound is right bound of root offset leftwards by the inset value
        )
    }

    @Test
    fun testMessageScreenAlignmentWhenRightAlignedWithNegativeInset() {
        val heightPercent = 60
        val widthPercent = 80
        val offsetPercent = 20
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(widthPercent)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.RIGHT)
            .horizontalInset(-offsetPercent)
            .build()

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        var messageContentWidthDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)
            messageContentWidthDp = ((contentViewWidthDp * settings.width) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = true
        )

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .getUnclippedBoundsInRoot()
        val verticalContentPaddingDp = (contentViewHeightDp - messageContentHeightDp) / 2
        val offsetDp = messageContentWidthDp * offsetPercent.toFloat() / 100f

        // Frame left and right bounds are offset rightwards by the inset value from the right
        validateBounds(
            frameBounds,
            rootBounds.top,
            rootBounds.bottom,
            rootBounds.left + offsetDp,
            rootBounds.right + offsetDp
        )

        // Content is center aligned vertically and right aligned horizontally offset leftwards and takes 60% of screen height
        validateBounds(
            contentBounds,
            rootBounds.top + verticalContentPaddingDp, // top bound is the top round of the root plus content padding
            rootBounds.bottom - verticalContentPaddingDp, // bottom bound is the bottom bound of the root minus content padding
            rootBounds.right - messageContentWidthDp + offsetDp, // left bound is the right bound of the root minus content width offset rightwards by the inset value
            rootBounds.right + offsetDp // right bound is right bound of root offset rightwards by the inset value
        )
    }

    @Test
    fun testMessageScreenAlignmentWhenBottomAlignedWithPositiveInsetAndRightAlignedWithNegativeInset() {
        val heightPercent = 60
        val widthPercent = 80
        val offsetPercent = 20
        val settings = InAppMessageSettings.Builder()
            .content(HTML_TEXT_SAMPLE)
            .height(heightPercent)
            .width(widthPercent)
            .verticalAlignment(InAppMessageSettings.MessageAlignment.BOTTOM)
            .verticalInset(offsetPercent)
            .horizontalAlignment(InAppMessageSettings.MessageAlignment.RIGHT)
            .horizontalInset(-offsetPercent)
            .build()

        var contentViewHeightDp = 0.dp
        var contentViewWidthDp = 0.dp
        var messageContentHeightDp = 0.dp
        var messageContentWidthDp = 0.dp
        composeTestRule.setContent { // setting our composable as content for test
            val activity = LocalContext.current as Activity
            val contentView = activity.findViewById<View>(android.R.id.content)
            contentViewHeightDp = with(LocalDensity.current) { contentView.height.toDp() }
            contentViewWidthDp = with(LocalDensity.current) { contentView.width.toDp() }
            messageContentHeightDp = ((contentViewHeightDp * settings.height) / 100)
            messageContentWidthDp = ((contentViewWidthDp * settings.width) / 100)

            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = settings,
                inAppMessageEventHandler = DefaultInAppMessageEventHandler(
                    scriptHandlers = mutableMapOf(),
                    mainScope = CoroutineScope(Dispatchers.Default)
                ),
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
        validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = true
        )

        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val frameBounds =
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).getUnclippedBoundsInRoot()
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .getUnclippedBoundsInRoot()
        val heightOffsetDp = messageContentHeightDp * offsetPercent.toFloat() / 100f
        val widthOffsetDp = messageContentWidthDp * offsetPercent.toFloat() / 100f

        // Frame top and bottom  bounds are offset upwards by the inset from the bottom
        // left and right bounds are offset rightwards by the inset value from the right
        validateBounds(
            frameBounds,
            rootBounds.top - heightOffsetDp,
            rootBounds.bottom - heightOffsetDp,
            rootBounds.left + widthOffsetDp,
            rootBounds.right + widthOffsetDp
        )

        // Content is bottom aligned vertically and offset upwards and centered horizontally and takes 80% of screen width
        validateBounds(
            contentBounds,
            rootBounds.bottom - messageContentHeightDp - heightOffsetDp, // top bound is the bottom bound of the root minus content height offset upwards by the inset value
            rootBounds.bottom - heightOffsetDp, // bottom bound is the bottom bound of the root offset upwards by the inset value
            rootBounds.right - messageContentWidthDp + widthOffsetDp, // left bound is the right bound of the root minus content width offset rightwards by the inset value
            rootBounds.right + widthOffsetDp // right bound is right bound of root offset rightwards by the inset value
        )
    }

    @After
    fun tearDown() {
        composeTestRule.waitForIdle()
        onBackPressed = false
        onCreatedCalled = false
        onDisposedCalled = false
        detectedGestures.clear()
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
