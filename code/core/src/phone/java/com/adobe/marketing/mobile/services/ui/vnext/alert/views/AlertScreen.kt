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

package com.adobe.marketing.mobile.services.ui.vnext.alert.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.DialogProperties
import com.adobe.marketing.mobile.services.ui.vnext.alert.AlertSettings
import com.adobe.marketing.mobile.services.ui.vnext.common.PresentationStateManager

/**
 * Composable function to display an alert dialog.
 * @param presentationStateManager [PresentationStateManager] to manage the visibility state of the alert dialog.
 * @param alertSettings [AlertSettings] to configure the alert dialog.
 * @param onPositiveResponse callback to be invoked when the user clicks on the positive button.
 * @param onNegativeResponse callback to be invoked when the user clicks on the negative button.
 * @param onBackPressed callback to be invoked when the user performs back navigation.
 */
@Composable
internal fun AlertScreen(
    presentationStateManager: PresentationStateManager,
    alertSettings: AlertSettings,
    onPositiveResponse: () -> Unit,
    onNegativeResponse: () -> Unit,
    onBackPressed: () -> Unit
) {
    AnimatedVisibility(
        visibleState = presentationStateManager.visibilityState,
        enter = fadeIn()
    ) {
        AlertDialog(
            title = {
                Text(
                    text = alertSettings.title,
                    modifier = Modifier.testTag(AlertTestTags.TITLE_TEXT)
                )
            },
            text = {
                Text(
                    text = alertSettings.message,
                    modifier = Modifier.testTag(AlertTestTags.MESSAGE_TEXT)
                )
            },
            confirmButton = {
                alertSettings.positiveButtonText?.let { positiveButtonText ->
                    TextButton(
                        onClick = { onPositiveResponse() },
                        modifier = Modifier.testTag(AlertTestTags.POSITIVE_BUTTON)
                    ) {
                        Text(text = positiveButtonText)
                    }
                }
            },
            dismissButton = {
                alertSettings.negativeButtonText?.let { negativeButtonText ->
                    TextButton(
                        onClick = { onNegativeResponse() },
                        modifier = Modifier.testTag(AlertTestTags.NEGATIVE_BUTTON)
                    ) {
                        Text(text = negativeButtonText)
                    }
                }
            },
            onDismissRequest = {
                // Slightly roundabout way to dismiss the dialog! DialogProperties determine when a
                // dismiss request is received. They are set to dismiss on back press and not any clicks outside.
                // So all dismiss requests are from back press.
                onBackPressed()
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        )
    }
}
