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
package com.adobe.marketing.mobile.core.testapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.Signal
import com.adobe.marketing.mobile.core.testapp.ui.theme.AEPSDKCoreAndroidTheme

@Composable
fun SignalView(navController: NavHostController) {
    var url by remember { mutableStateOf("https://www.adobe.com") }

    Column(Modifier.padding(8.dp)) {
        Button(onClick = {
            navController.navigate(NavRoutes.HomeView.route)
        }) {
            Text(text = "Home")
        }
        Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Example of the malformed URL: https://www.adobe.com:_80/", fontSize = 10.sp)
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL") }
            )
            Button(onClick = {
                MobileCore.dispatchEvent(
                    Event.Builder("consequence event", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).setEventData(
                    mapOf(
                        "triggeredconsequence" to mapOf(
                            "type" to "pii",
                            "detail" to mapOf(
                                "timeout" to 0,
                                "templateurl" to url
                            )
                        )
                    )
                ).build())

            }) {
                Text(text = "post back")
            }
            // openURL is covered by automation test, we can enable it later if needed for customer issue verification
            Button(onClick = {},enabled = false) {
                Text(text = "open URL")
            }
            Text(text = "Signal extension version - ${Signal.extensionVersion()}")
        }

    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreviewForSignalView() {
    AEPSDKCoreAndroidTheme {
        SignalView(rememberNavController())
    }
}