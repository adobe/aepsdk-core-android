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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.MobilePrivacyStatus
import com.adobe.marketing.mobile.app.kotlin.ui.theme.AepsdkcoreandroidTheme
import com.adobe.marketing.mobile.services.ServiceProvider

@Composable
fun CoreView(navController: NavHostController) {
    Column(Modifier.padding(8.dp)) {
        Button(onClick = {
            navController.navigate(NavRoutes.HomeView.route)
        }) {
            Text(text = "Home")
        }
        Spacer(modifier = Modifier.size(10.dp))
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                showCoreVersion()
            }) {
                Text(text = "extensionVersion")
            }
            Button(onClick = {
                updateConfiguration()
            }) {
                Text(text = "updateConfiguration(optedout)")
            }
            Button(onClick = {
                clearUpdatedConfiguration()
            }) {
                Text(text = "clearUpdatedConfiguration")
            }
            Button(onClick = {
                MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_IN)
            }) {
                Text(text = "setPrivacyStatus(OptIn)")
            }
            Button(onClick = {
                MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT)
            }) {
                Text(text = "setPrivacyStatus(OptOut)")
            }
            Button(onClick = {
                MobileCore.getPrivacyStatus { status ->
                    showAlert("Privacy Status: $status")
                }

            }) {
                Text(text = "getPrivacyStatus")
            }
            Button(onClick = {
                MobileCore.setLogLevel(LoggingMode.VERBOSE)
            }) {
                Text(text = "setLogLevel(LogLevel.VERBOSE)")
            }
            Button(onClick = {
                MobileCore.setLogLevel(LoggingMode.DEBUG)
            }) {
                Text(text = "setLogLevel(LogLevel.DEBUG)")
            }
            Button(onClick = {
                MobileCore.log(LoggingMode.VERBOSE, "VERBOSE_TAG", "This is a VERBOSE message.")
            }) {
                Text(text = "log (VERBOSE)")
            }

            Button(onClick = {
                showAlert("Log Level: ${MobileCore.getLogLevel()}")
            }) {
                Text(text = "getLogLevel")
            }
            Button(onClick = {
                com.adobe.marketing.mobile.services.Log.debug(
                    "extension-name",
                    "kotlin-app",
                    "This is a %s log",
                    "debug"
                )
            }) {
                Text(text = "Log.debug")
            }
            Button(onClick = {
                MobileCore.setPushIdentifier("ABC")
            }) {
                Text(text = "setPushIdentifier")
            }
            Button(onClick = {
                MobileCore.setAdvertisingIdentifier("XYZ")
            }) {
                Text(text = "setAdvertisingIdentifier")
            }
            Button(onClick = {
                MobileCore.getSdkIdentities { json ->
                    showAlert("Identities: $json")
                }
            }) {
                Text(text = "getSdkIdentities")
            }
            Button(onClick = {
                MobileCore.collectPii(mapOf("key" to "value"))
            }) {
                Text(text = "collectPii")
            }
            Button(onClick = {
                MobileCore.trackAction("action", mapOf("key" to "value"))
            }) {
                Text(text = "trackAction")
            }
            Button(onClick = {
                MobileCore.trackState("state", mapOf("key" to "value"))
            }) {
                Text(text = "trackState")
            }
            Button(onClick = {
                MobileCore.resetIdentities()
            }) {
                Text(text = "resetIdentities")
            }

        }

    }

}

private fun showCoreVersion() {
//    ServiceProvider.getInstance().uiService.showAlert(
//        AlertSetting.build(
//            "show core version",
//            "core: ${MobileCore.extensionVersion()}",
//            "OK",
//            "Cancel"
//        ), null
//    )
}

private fun updateConfiguration() {
    registerEventListener(
        "com.adobe.eventType.configuration",
        "com.adobe.eventSource.requestContent"
    ) { event ->
        showAlert(event)
    }

    MobileCore.updateConfiguration(mapOf("'global.privacy" to "optedout"))
}

private fun clearUpdatedConfiguration() {
    registerEventListener(
        "com.adobe.eventType.configuration",
        "com.adobe.eventSource.requestContent"
    ) { event ->
        showAlert(event)
    }
    MobileCore.clearUpdatedConfiguration()
}


@Preview(showBackground = true)
@Composable
fun DefaultPreviewForCoreView() {
    AepsdkcoreandroidTheme {
        CoreView(rememberNavController())
    }
}