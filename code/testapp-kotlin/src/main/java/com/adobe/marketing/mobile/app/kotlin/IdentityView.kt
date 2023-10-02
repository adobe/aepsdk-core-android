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

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.adobe.marketing.mobile.*
import com.adobe.marketing.mobile.services.ServiceProvider
import java.lang.StringBuilder

@Composable
fun IdentityView(navController: NavHostController) {
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
                MobileCore.setAdvertisingIdentifier("aid_001")
            }) {
                Text(text = "setAdvertisingIdentifier")
            }
            Button(onClick = {
                MobileCore.setPushIdentifier("push_id_001")
            }) {
                Text(text = "setPushIdentifier")
            }
            Button(onClick = {
                Identity.syncIdentifiers(
                    mapOf(
                        "idType1" to "idValue1",
                        "idType2" to "idValue2"
                    ), VisitorID.AuthenticationState.AUTHENTICATED
                )
            }) {
                Text(text = "syncIdentifiers")
            }

            Button(onClick = {
                Identity.getExperienceCloudId { showAlert("getExperienceCloudId: $it") }
            }) {
                Text(text = "getExperienceCloudId")
            }
            Button(onClick = {
                Identity.getIdentifiers { vidList ->
                    val stringBuilder = StringBuilder()
                    vidList.forEach {
                        stringBuilder.append(
                            """
                                
                            ${it.id}
                            ${it.idType}
                            ${it.idOrigin}
                            ${it.authenticationState}
                            
                        """.trimIndent()
                        )
                    }

                    showAlert("getIdentifiers: \n $stringBuilder")
                }
            }) {
                Text(text = "getIdentifiers")
            }
            Button(onClick = {
                Identity.getUrlVariables { showAlert("getUrlVariables: $it") }
            }) {
                Text(text = "getUrlVariables")
            }
            Button(onClick = {
                Identity.appendVisitorInfoForURL("https://example.com") { urlWithAdobeVisitorInfo ->
                    showAlert("appendVisitorInfoForURL: $urlWithAdobeVisitorInfo")
                }
            }) {
                Text(text = "appendVisitorInfoForURL")
            }

        }

    }
}

private fun showCoreVersion() {
    Toast.makeText(
        ServiceProvider.getInstance().appContextService.applicationContext,
        "Core version: ${MobileCore.extensionVersion()}",
        Toast.LENGTH_SHORT
    )
        .show()
}