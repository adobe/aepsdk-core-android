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

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.rules.ActivityScenarioRule

internal object MessageScreenTestHelper {

    /**
     * Validates the size of the view bounds with the given height and width
     */
    internal fun validateViewSize(viewBounds: DpRect, height: Dp, width: Dp) {
        viewBounds.height.assertIsEqualTo(height, "failed", Dp(16f))
        viewBounds.width.assertIsEqualTo(width, "failed", Dp(2f))
    }

    /**
     * Validates the bounds of the view with the given top, bottom, left and right values
     */
    internal fun validateBounds(viewBounds: DpRect, top: Dp, bottom: Dp, left: Dp, right: Dp) {
        viewBounds.top.assertIsEqualTo(top, "failed", Dp(2f))
        viewBounds.bottom.assertIsEqualTo(bottom, "failed", Dp(2f))
        viewBounds.left.assertIsEqualTo(left, "failed", Dp(2f))
        viewBounds.right.assertIsEqualTo(right, "failed", Dp(2f))
    }

    /**
     * Validates the message content with the given backdrop and clipped values
     * @param composeTestRule the compose test rule
     * @param withBackdrop whether the backdrop is present
     * @param clipped whether the message is expected to be clipped
     */
    internal fun <T : ComponentActivity> validateMessageAppeared(
        composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<T>, T>,
        withBackdrop: Boolean,
        clipped: Boolean
    ) {
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_FRAME).assertExists().also {
            if (!clipped) it.assertIsDisplayed()
        }
        composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_CONTENT).assertExists().also {
            if (!clipped) it.assertIsDisplayed()
        }
        if (withBackdrop) {
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_BACKDROP).assertExists().also {
                if (!clipped) it.assertIsDisplayed()
            }
        } else {
            composeTestRule.onNodeWithTag(MessageTestTags.MESSAGE_BACKDROP).assertDoesNotExist()
        }
    }
}
