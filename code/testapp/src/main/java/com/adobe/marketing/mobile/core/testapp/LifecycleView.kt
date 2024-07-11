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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.Signal
import com.adobe.marketing.mobile.core.testapp.ui.theme.AEPSDKCoreAndroidTheme
import kotlinx.coroutines.delay

@Composable
fun LifecycleView(navController: NavHostController) {
    var showLifecycleEvent by remember { mutableStateOf(false) }
    LaunchedEffect(showLifecycleEvent) {
        if (showLifecycleEvent) {
            delay(1000)
            showAlert("Lifecycle event: ${SDKObserver.getLatestLifecycleEvent()?.toString()}")
            showLifecycleEvent = false
        }
    }
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
            Text(text = "Signal extension version - ${Signal.extensionVersion()}")
            Button(onClick = {
                SDKObserver.clearLatestLifecycleEvent()
                MobileCore.lifecycleStart(null)
                showLifecycleEvent = true
            }) {
                Text(text = "lifecycleStart")
            }
            Button(onClick = {
                SDKObserver.clearLatestLifecycleEvent()
                MobileCore.lifecycleStart(mapOf("key" to "value"))
                showLifecycleEvent = true
            }) {
                Text(text = "lifecycleStart(contextData)")
            }
            Button(onClick = {
                SDKObserver.clearLatestLifecycleEvent()
                MobileCore.lifecyclePause()
                showLifecycleEvent = true
            }) {
                Text(text = "lifecyclePause")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreviewForCoreViewForLifecycleView() {
    AEPSDKCoreAndroidTheme {
        LifecycleView(rememberNavController())
    }
}