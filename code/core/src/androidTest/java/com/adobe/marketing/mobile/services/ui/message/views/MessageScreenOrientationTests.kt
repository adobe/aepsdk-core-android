/*
  Copyright 2024 Adobe. All rights reserved.
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.adobe.marketing.mobile.services.ui.RestrictedConfigActivity
import com.adobe.marketing.mobile.services.ui.common.PresentationStateManager
import com.adobe.marketing.mobile.services.ui.message.InAppMessageSettings
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * AEPPresentable handles the orientation changes of a presentable by detaching and reattaching
 * the presentable from the activity by listening to Application.ActivityLifecycleCallbacks.
 * However, the test cannot be isolated to listen to ApplicationLifecycle changes at the screen level.
 * So, the tests in this class are done on the MessageScreen which is attached to an activity that
 * restricts orientation/screen size changes. So the rotation will not destroy the activity but it
 * is expected that the MessageScreen will be recomposed to fit the new screen dimensions.
 * While this is not the ideal test, it is the best we can do to test the
 * orientation changes of the message screen along with [MessageScreenTests.testMessageScreenIsRestoredOnConfigurationChange]
 * which tests the configuration changes of the message screen.
 */
@RunWith(AndroidJUnit4::class)
class MessageScreenOrientationTests {
    @get: Rule
    val composeTestRule = createAndroidComposeRule<RestrictedConfigActivity>()

    private var onCreatedCalled = false
    private var onDisposedCalled = false
    private var onBackPressed = false
    private val detectedGestures = mutableListOf<InAppMessageSettings.MessageGesture>()
    private val presentationStateManager = PresentationStateManager()

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

    // ----------------------------------------------------------------------------------------------
    // Test cases for orientation changes
    // ----------------------------------------------------------------------------------------------
    @Test
    fun testMessageScreenIsRestoredOnOrientationChange() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val uiDevice = UiDevice.getInstance(instrumentation)

        val heightPercentage = 95
        val widthPercentage = 60
        val settings = InAppMessageSettings.Builder()
            .height(heightPercentage)
            .width(widthPercentage)
            .content(HTML_TEXT_SAMPLE)
            .build()

        val contentViewHeightDp = mutableStateOf(0.dp)
        val contentViewWidthDp = mutableStateOf(0.dp)
        val contentHeightDp = mutableStateOf(0.dp)
        val contentWidthDp = mutableStateOf(0.dp)
        composeTestRule.setContent { // setting our composable as content for test
            // Get the screen dimensions
            LocalConfiguration.current.run {
                val activity = LocalContext.current as Activity
                val contentView = activity.findViewById<View>(android.R.id.content)
                contentViewHeightDp.value = with(LocalDensity.current) { contentView.height.toDp() }
                contentViewWidthDp.value = with(LocalDensity.current) { contentView.width.toDp() }
                contentHeightDp.value = ((contentViewHeightDp.value * settings.height) / 100)
                contentWidthDp.value = ((contentViewWidthDp.value * settings.width) / 100)
            }

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
        MessageScreenTestHelper.validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = false
        )

        // Verify that the message content is resized to fit the screen
        val contentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .getUnclippedBoundsInRoot()
        val (expectedInitialHeight, expectedInitialWidth) = calculateDimensions(
            contentViewHeightDp.value,
            contentViewWidthDp.value,
            contentHeightDp.value,
            heightPercentage,
            widthPercentage
        )

        MessageScreenTestHelper.validateViewSize(
            contentBounds,
            expectedInitialHeight,
            expectedInitialWidth
        )

        assertTrue(onCreatedCalled)
        assertFalse(onDisposedCalled)
        assertFalse(onBackPressed)
        assertTrue(detectedGestures.isEmpty())
        resetState()

        // Rotate the device to landscape
        uiDevice.setOrientationLandscape()

        // Wait for the device to stabilize
        uiDevice.waitForIdle()
        composeTestRule.waitForIdle()

        // Verify that the message content is resized to fit the new orientation
        MessageScreenTestHelper.validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = false
        )
        val landscapeContentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .getUnclippedBoundsInRoot()
        val (expectedLandscapeHeight, expectedLandscapeWidth) = calculateDimensions(
            contentViewHeightDp.value,
            contentViewWidthDp.value,
            contentHeightDp.value,
            heightPercentage,
            widthPercentage
        )

        MessageScreenTestHelper.validateViewSize(
            landscapeContentBounds,
            expectedLandscapeHeight,
            expectedLandscapeWidth
        )

        // onCreated should not be called again due to orientation change restrictions
        assertFalse(onCreatedCalled)
        assertFalse(onDisposedCalled)
        assertFalse(onBackPressed)
        assertTrue(detectedGestures.isEmpty())
        resetState()

        // Rotate the device back to its original orientation
        uiDevice.setOrientationNatural()

        // Wait for the device to stabilize
        uiDevice.waitForIdle()
        composeTestRule.waitForIdle()
        MessageScreenTestHelper.validateMessageAppeared(
            composeTestRule = composeTestRule,
            withBackdrop = false,
            clipped = false
        )

        val naturalContentBounds = composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT)
            .getUnclippedBoundsInRoot()

        // Verify that the message content is restored to its original size
        val (expectedNaturalHeight, expectedNaturalWidth) = calculateDimensions(
            contentViewHeightDp.value,
            contentViewWidthDp.value,
            contentHeightDp.value,
            heightPercentage,
            widthPercentage
        )

        MessageScreenTestHelper.validateViewSize(
            naturalContentBounds,
            expectedNaturalHeight,
            expectedNaturalWidth
        )
    }

    /**
     * Calculates the expected height and width of the message content based on the screen dimensions
     * If the height exceeds what is allowed by the activity (due to actionbar), it takes
     * up the full height of the activity
     * @param screenHeightDp the screen height in dp
     * @param screenWidthDp the screen width in dp
     * @param activityHeightDp the height of the activity in dp
     * @param heightPercentage the percentage of the screen height the message content should take
     * @param widthPercentage the percentage of the screen width the message content should take
     * @return a pair of the expected height and width of the message content
     */
    private fun calculateDimensions(
        screenHeightDp: Dp,
        screenWidthDp: Dp,
        activityHeightDp: Dp,
        heightPercentage: Int,
        widthPercentage: Int
    ): Pair<Dp, Dp> {
        val expectedHeight = if ((screenHeightDp * (heightPercentage / 100f)) > activityHeightDp) {
            activityHeightDp
        } else {
            screenHeightDp * (heightPercentage / 100f)
        }
        val expectedWidth = screenWidthDp * (widthPercentage / 100f)

        return Pair(expectedHeight, expectedWidth)
    }

    private fun resetState() {
        onCreatedCalled = false
        onDisposedCalled = false
        onBackPressed = false
        detectedGestures.clear()
    }

    @After
    fun tearDown() {
        resetState()
    }
}
