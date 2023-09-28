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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.adobe.marketing.mobile.app.kotlin.ui.theme.AepsdkcoreandroidTheme
import com.adobe.marketing.mobile.services.ServiceProvider
import java.util.Timer
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
            }
        }

    }
}

private fun showAlert() {
//    uiService.showAlert(
//        AlertSetting.build(
//            "title",
//            "message",
//            "positive",
//            "negative"
//        ), null
//    )
}

private fun openUrl() {
//    uiService.showUrl("https://adobe.com")
}

private fun showLocalNotification() {
//    uiService.showLocalNotification(
//        NotificationSetting.build(
//            "id",
//            "Content",
//            System.currentTimeMillis() / 1000,
//            0,
//            "myscheme://link",
//            null,
//            "sound.wav",
//            "title"
//        )
//    )
}

private fun showFloatingButton() {
//    val floatingButton = uiService.createFloatingButton(null)
//    floatingButton.display()
//    Timer("SettingUp", false).schedule(2000) {
//        floatingButton.remove()
//    }
}

private fun showFullScreenMessage() {
//    uiService.createFullscreenMessage(
//        "xx",
//        null,
//        false,
//        MessageSettings()
//    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreviewForServicesView() {
    AepsdkcoreandroidTheme {
        ServicesView(rememberNavController())
    }
}