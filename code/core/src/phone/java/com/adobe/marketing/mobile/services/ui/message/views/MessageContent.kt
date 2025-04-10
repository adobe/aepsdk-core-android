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

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceConstants
import com.adobe.marketing.mobile.services.ui.message.InAppMessageEventHandler
import com.adobe.marketing.mobile.services.ui.message.InAppMessagePresentable
import com.adobe.marketing.mobile.services.ui.message.InAppMessageSettings
import java.nio.charset.StandardCharsets

/**
 * Represents the content of the InAppMessage. Holds a WebView that loads the HTML content of the message
 * @param modifier [Modifier] to apply to the content (AndroidView holding the WebView)
 * @param inAppMessageSettings [InAppMessageSettings] settings for the InAppMessage
 * @param onCreated callback to be invoked when the WenView that this composable holds is created
 */
@Composable
internal fun MessageContent(
    modifier: Modifier,
    inAppMessageSettings: InAppMessageSettings,
    inAppMessageEventHandler: InAppMessageEventHandler,
    onHeightReceived: (Int) -> Unit,
    onCreated: (WebView) -> Unit
) {

    AndroidView(
        factory = {
            WebView(it).apply {
                Log.debug(
                    ServiceConstants.LOG_TAG,
                    "MessageContent",
                    "Creating MessageContent"
                )

                // Needed to force the HTML to be rendered within bounds of the AndroidView
                // allowing HTML content with "overflow" css to work properly
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                inAppMessageEventHandler.handleJavascriptMessage("inAppContentHeightHandler") { message ->
                    try {
                        val height = message.toIntOrNull() ?: 0
                        onHeightReceived(height)
                    } catch (e: Exception) {
                        Log.error(
                            ServiceConstants.LOG_TAG,
                            "MessageContent",
                            "Error parsing height", e
                        )
                    }
                }
                // call on created before loading the content. This is to ensure that the webview script
                // handlers and interfaces are ready to be used
                onCreated(this)

                loadDataWithBaseURL(
                    InAppMessagePresentable.BASE_URL,
                    inAppMessageSettings.content,
                    InAppMessagePresentable.TEXT_HTML_MIME_TYPE,
                    StandardCharsets.UTF_8.name(),
                    null
                )
            }
        },
        modifier = modifier
            .clip(RoundedCornerShape(inAppMessageSettings.cornerRadius.dp))
            .testTag(MessageTestTags.MESSAGE_CONTENT)
    )
}
