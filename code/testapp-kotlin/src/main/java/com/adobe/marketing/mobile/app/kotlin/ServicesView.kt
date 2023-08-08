/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 */
package com.adobe.marketing.mobile.app.kotlin

import android.app.Activity
import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.adobe.marketing.mobile.app.kotlin.ui.theme.AepsdkcoreandroidTheme
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ui.AlertSetting
import com.adobe.marketing.mobile.services.ui.MessageSettings
import com.adobe.marketing.mobile.services.ui.NotificationSetting
import com.adobe.marketing.mobile.services.ui.vnext.InAppMessage
import com.adobe.marketing.mobile.services.ui.vnext.Presentable
import com.adobe.marketing.mobile.services.ui.vnext.PresentationError
import com.adobe.marketing.mobile.services.ui.vnext.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageEventListener
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageSettings
import java.io.InputStream
import java.util.*
import kotlin.concurrent.schedule


private val uiService = ServiceProvider.getInstance().uiService

@Composable
fun ServicesView(navController: NavHostController) {
    Column(Modifier.padding(8.dp)) {
        Button(onClick = {
            navController.navigate(NavRoutes.HomeView.route)
        }) {
            Text(text = "Home")
        }
        Spacer(modifier = Modifier.size(10.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            elevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "UI Service")
                Button(onClick = {
                    showAlert()
                }) {
                    Text(text = "ALERT")
                }
                Button(onClick = {
                    openUrl()
                }) {
                    Text(text = "OPEN URL")
                }
                Button(onClick = {
                    //TODO: not showing
                    showLocalNotification()
                }) {
                    Text(text = "LOCAL NOTIFICATION")
                }

                Button(onClick = {
                    showFloatingButton()
                }) {
                    Text(text = "FLOATING BUTTON (5s)")
                }

                Button(onClick = {
                    //TODO: not showing
                    showFullScreenMessage()
                }) {
                    Text(text = "FULL SCREEN MESSAGE")
                }


                Button(onClick = {
                    //TODO: not showing
                    showVNextMessage()
                }) {
                    Text(text = "V NEXT FULL SCREEN MESSAGE")
                }

            }
        }

    }
}

private fun showAlert() {
    uiService.showAlert(
        AlertSetting.build(
            "title",
            "message",
            "positive",
            "negative"
        ), null
    )
}

private fun openUrl() {
    uiService.showUrl("https://adobe.com")
}

private fun showLocalNotification() {
    uiService.showLocalNotification(
        NotificationSetting.build(
            "id",
            "Content",
            System.currentTimeMillis() / 1000,
            0,
            "myscheme://link",
            null,
            "sound.wav",
            "title"
        )
    )
}

private fun showFloatingButton() {
    val floatingButton = uiService.createFloatingButton(null)
    floatingButton.display()
    Timer("SettingUp", false).schedule(2000) {
        floatingButton.remove()
    }
}

private fun showFullScreenMessage() {
    uiService.createFullscreenMessage(
        "xx",
        null,
        false,
        MessageSettings()
    )
}

private fun showVNextMessage() {
    val iamSettings = InAppMessageSettings.Builder()
        .backdropOpacity(0.5f)
        .backgroundColor("#000000")
        .cornerRadius(10f)
        .displayAnimation(InAppMessageSettings.MessageAnimation.BOTTOM)
        .dismissAnimation(InAppMessageSettings.MessageAnimation.TOP)
        .height(60)
        .width(80)
        .horizontalAlignment(InAppMessageSettings.MessageAlignment.CENTER)
        .shouldTakeOverUi(true)
        .content(HTML_TEXT_SAMPLE)
        .build()
    val inAppMessageEventListener : InAppMessageEventListener = object : InAppMessageEventListener {
        override fun onBackPressed(message: Presentable<InAppMessage>) {

        }

        override fun onUrlLoading(message: Presentable<InAppMessage>, url: String): Boolean {
            return true
        }

        override fun onShow(presentable: Presentable<InAppMessage>) {

        }

        override fun onHide(presentable: Presentable<InAppMessage>) {

        }

        override fun onDismiss(presentable: Presentable<InAppMessage>) {

        }

        override fun onError(presentable: Presentable<InAppMessage>, error: PresentationError) {

        }

    }

    val iam = InAppMessage(inAppMessageEventListener, iamSettings)
    val pup = object : PresentationUtilityProvider {
        override fun getApplication(): Application {
            return ServiceProvider.getInstance().appContextService.application!!
        }

        override fun getCurrentActivity(): Activity? {
            return ServiceProvider.getInstance().appContextService.currentActivity
        }

        override fun getCachedContent(cacheName: String, key: String): InputStream? {
            return null
        }

    }


    val presentable = ServiceProvider.getInstance().newUIService.create(iam, pup)
    presentable.show()
}

@Preview(showBackground = true)
@Composable
fun DefaultPreviewForServicesView() {
    AepsdkcoreandroidTheme {
        ServicesView(rememberNavController())
    }
}


val HTML_TEXT_SAMPLE = "<html>\n" +
        "<head>\n" +
        "<title>A Large HTML Page</title>\n" +
        "</head>\n" +
        "<body>\n" +
        "\n" +
        "<h1>This is a large HTML page</h1>\n" +
        "\n" +
        "<p>This page contains a lot of text, images, and other content.</p>\n" +
        "\n" +
        "<img src=\"image.jpg\" alt=\"A picture of a cat\">\n" +
        "\n" +
        "<ul>\n" +
        "<li>Item 1</li>\n" +
        "<li>Item 2</li>\n" +
        "<li>Item 3</li>\n" +
        "</ul>\n" +
        "\n" +
        "<table>\n" +
        "<tr>\n" +
        "<th>Column 1</th>\n" +
        "<th>Column 2</th>\n" +
        "<th>Column 3</th>\n" +
        "</tr>\n" +
        "<tr>\n" +
        "<td>Row 1, Column 1</td>\n" +
        "<td>Row 1, Column 2</td>\n" +
        "<td>Row 1, Column 3</td>\n" +
        "</tr>\n" +
        "<tr>\n" +
        "<td>Row 2, Column 1</td>\n" +
        "<td>Row 2, Column 2</td>\n" +
        "<td>Row 2, Column 3</td>\n" +
        "</tr>\n" +
        "</table>\n" +
        "\n" +
        "<p>This is the end of the large HTML page.</p>\n" +
        "\n" +
        "</body>\n" +
        "</html>"