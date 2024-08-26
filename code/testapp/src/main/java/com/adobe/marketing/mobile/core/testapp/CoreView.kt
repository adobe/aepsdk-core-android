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

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.MobilePrivacyStatus
import com.adobe.marketing.mobile.core.testapp.ui.theme.AEPSDKCoreAndroidTheme
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider

@Composable
fun CoreView(navController: NavHostController) {
    var appId by remember { mutableStateOf("your-appId") }
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
                MobileCore.configureWithFileInAssets("ADBMobileConfig_custom.json")
            }) {
                Text(text = "configureWithFileInAssets()")
            }
            Surface(
                border = BorderStroke(1.dp, Color.Gray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(PaddingValues(8.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = appId,
                        onValueChange = { appId = it },
                        label = { Text("appId") }
                    )
                    Button(onClick = {
                        MobileCore.configureWithAppID(appId)
                    }) {
                        Text(text = "configureWithAppID(\"appId\")")
                    }
                }
            }

            Button(onClick = {
                updateConfiguration()
            }) {
                Text(text = "updateConfiguration")
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
                // The bundled rule is configured to triggers a postback for the following condition: a trackAction event with the action type 'bundled_trigger_postback'.
                MobileCore.trackAction("bundled_trigger_postback", null)
            }) {
                Text(text = "Trigger bundled rule consequence(postback)")
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
                MobileCore.setLogLevel(LoggingMode.WARNING)
            }) {
                Text(text = "setLogLevel(LogLevel.WARNING)")
            }
            Button(onClick = {
                MobileCore.setLogLevel(LoggingMode.ERROR)
            }) {
                Text(text = "setLogLevel(LogLevel.ERROR)")
            }
            Button(onClick = {
                showAlert("Log Level: ${MobileCore.getLogLevel()}")
            }) {
                Text(text = "getLogLevel")
            }
            Button(onClick = {
                Log.trace(
                        "CoreView",
                        "testapp",
                        "This is a %s log",
                        "verbose"
                )
            }) {
                Text(text = "Log.verbose")
            }
            Button(onClick = {
                Log.debug(
                        "CoreView",
                        "testapp",
                        "This is a %s log",
                        "debug"
                )
            }) {
                Text(text = "Log.debug")
            }
            Button(onClick = {
                Log.warning(
                        "CoreView",
                        "testapp",
                        "This is a %s log",
                        "warning"
                )
            }) {
                Text(text = "Log.warning")
            }
            Button(onClick = {
                Log.error(
                        "CoreView",
                        "testapp",
                        "This is a %s log",
                        "error"
                )
            }) {
                Text(text = "Log.error")
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
    Toast.makeText(
        ServiceProvider.getInstance().appContextService.applicationContext,
        "Core Version: ${MobileCore.extensionVersion()}",
        Toast.LENGTH_SHORT
    ).show()
}

private fun updateConfiguration() {
    MobileCore.updateConfiguration(mapOf("custom_key" to "custom_value"))
}

private fun clearUpdatedConfiguration() {
    MobileCore.clearUpdatedConfiguration()
}


@Preview(showBackground = true)
@Composable
fun DefaultPreviewForCoreView() {
    AEPSDKCoreAndroidTheme {
        CoreView(rememberNavController())
    }
}
