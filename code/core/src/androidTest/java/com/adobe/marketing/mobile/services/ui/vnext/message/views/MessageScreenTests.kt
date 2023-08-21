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

package com.adobe.marketing.mobile.services.ui.vnext.message.views

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performGesture
import androidx.compose.ui.test.swipeDown
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.services.ui.vnext.common.PresentationStateManager
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageSettings
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageScreenTests {
    @get: Rule
    val composeTestRule = createComposeRule() // compose rule is required to get access to the composable component
    val HTML_TEXT_SAMPLE = "<html>\n" +
        "<head>\n" +
        "<title>A Sample HTML Page</title>\n" +
        "</head>\n" +
        "<body>\n" +
        "\n" +
        "<h1>This is a sample HTML page</h1>\n" +
        "\n" +
        "</body>\n" +
        "</html>"

    private val inAppMessageSettings = InAppMessageSettings.Builder()
        .backdropOpacity(0.5f)
        .backgroundColor("#000000")
        .cornerRadius(10f)
        .displayAnimation(InAppMessageSettings.MessageAnimation.BOTTOM)
        .dismissAnimation(InAppMessageSettings.MessageAnimation.TOP)
        .height(60)
        .width(80)
        .horizontalAlignment(InAppMessageSettings.MessageAlignment.CENTER)
        .shouldTakeOverUi(false)
        .content(HTML_TEXT_SAMPLE)
        .gestureMap(mapOf("swipeDown" to "adbinapp://dismiss"))
        .build()

    private var onCreatedCalled = false
    private var onDisposedCalled = false
    private var onBackPressed = false
    private val detectedGestures = mutableListOf<InAppMessageSettings.MessageGesture>()
    private val presentationStateManager = PresentationStateManager()

    @Before
    fun setUp() {
        composeTestRule.setContent { // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = inAppMessageSettings,
                onCreated = { onCreatedCalled = true },
                onDisposed = { onDisposedCalled = true },
                onGestureDetected = { gesture -> detectedGestures.add(gesture) },
                onBackPressed = { onBackPressed = true }
            )
        }
    }

    @Test
    fun testMessage() {
        presentationStateManager.onShown()
        composeTestRule.waitForIdle()
        assertTrue(onCreatedCalled)
        composeTestRule.onNodeWithTag("MessageFrame").assertExists()

        composeTestRule.onNodeWithTag("MessageFrame").performGesture {
            swipeDown()
        }

        composeTestRule.waitForIdle()
        assertTrue(detectedGestures.contains(InAppMessageSettings.MessageGesture.SWIPE_DOWN))

        presentationStateManager.onDetached()
        composeTestRule.waitForIdle()

        assertTrue(onDisposedCalled)
    }

    @After
    fun tearDown() {
    }
}
