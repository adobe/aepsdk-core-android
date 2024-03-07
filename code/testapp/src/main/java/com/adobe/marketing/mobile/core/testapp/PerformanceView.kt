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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.core.testapp.extension.PerfExtension

@Composable
fun PerformanceView(navController: NavHostController) {
    val result = remember {
        mutableStateOf("")
    }
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
            Text(result.value)
            Button(onClick = {
                result.value = ""
                PerfExtension.recount { count ->
                    if (count == 999) {
                        result.value = "$count consequence events caught"
                    }
                }

                for (i in 0..99) {
                    MobileCore.dispatchEvent(
                        Event.Builder(
                            "mock event - $i",
                            "com.adobe.eventType.generic.track",
                            "com.adobe.eventSource.requestContent"
                        ).build()
                    )
                }

            }) {
                Text(text = "EvaluateRules")
            }
            Button(onClick = {

            }) {
                Text(text = "Evaluate Rules xx")
            }

        }
    }
}