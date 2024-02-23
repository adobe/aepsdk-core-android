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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.adobe.marketing.mobile.core.testapp.ui.theme.AEPSDKCoreAndroidTheme
import com.adobe.marketing.mobile.core.testapp.ui.alert.AlertCard
import com.adobe.marketing.mobile.core.testapp.ui.alert.AlertCreator
import com.adobe.marketing.mobile.core.testapp.ui.floatingbutton.FloatingButtonCard
import com.adobe.marketing.mobile.core.testapp.ui.floatingbutton.FloatingButtonCreator
import com.adobe.marketing.mobile.core.testapp.ui.inappmessage.InAppMessageCard
import com.adobe.marketing.mobile.core.testapp.ui.inappmessage.InAppMessageCreator
import com.adobe.marketing.mobile.services.HttpMethod
import com.adobe.marketing.mobile.services.NetworkRequest
import com.adobe.marketing.mobile.services.ServiceProvider

//val inAppMessage = InAppMessageCreator.create()
//val alert = AlertCreator.create()
//val floatingButton =
//    FloatingButtonCreator(ServiceProvider.getInstance().appContextService.applicationContext!!).create()

@Composable
fun ServicesView(navController: NavHostController) {
    Column(Modifier.padding(8.dp)) {
        Button(onClick = {
            navController.navigate(NavRoutes.HomeView.route)
        }) {
            Text(text = "Home")
        }
        Spacer(modifier = Modifier.size(10.dp))
        Button(onClick = {
            val request = NetworkRequest("https://www.adobe.com", HttpMethod.GET, null, null, 5000,5000)
            ServiceProvider.getInstance().networkService?.connectAsync(request){ connection ->
                val status = if (connection != null) "valid connection" else "null connection"
                showAlert("Privacy Status: $status")
            }
        }) {
            Text(text = "NetworkService")
        }

//        InAppMessageCard(iamPresentable = inAppMessage)
//        AlertCard(alertPresentable = alert)
//        FloatingButtonCard(floatingButtonPresentable = floatingButton)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreviewForServicesView() {
    AEPSDKCoreAndroidTheme {
        ServicesView(rememberNavController())
    }
}