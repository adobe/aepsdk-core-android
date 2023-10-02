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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.adobe.marketing.mobile.app.kotlin.ui.theme.AepsdkcoreandroidTheme
import com.adobe.marketing.mobile.app.kotlin.uiservices.alert.AlertCard
import com.adobe.marketing.mobile.app.kotlin.uiservices.alert.AlertCreator
import com.adobe.marketing.mobile.app.kotlin.uiservices.floatingbutton.FloatingButtonCard
import com.adobe.marketing.mobile.app.kotlin.uiservices.floatingbutton.FloatingButtonCreator
import com.adobe.marketing.mobile.app.kotlin.uiservices.inappmessage.InAppMessageCard
import com.adobe.marketing.mobile.app.kotlin.uiservices.inappmessage.InAppMessageCreator
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import java.io.InputStream

@Composable
fun ServicesView(navController: NavHostController) {
    Column(Modifier.padding(8.dp)) {
        Button(onClick = {
            navController.navigate(NavRoutes.HomeView.route)
        }) {
            Text(text = "Home")
        }
        Spacer(modifier = Modifier.size(10.dp))

        InAppMessageDemo()
        AlertDemo()
        FloatingButtonDemo()
    }
}

@Composable
internal fun InAppMessageDemo() {
    val inAppMessage = InAppMessageCreator.create()
    InAppMessageCard(iamPresentable = inAppMessage)
}


@Composable
internal fun AlertDemo() {
    val alert = AlertCreator.create()
    AlertCard(alertPresentable = alert)
}

@Composable
internal fun FloatingButtonDemo() {
    val context = LocalContext.current
    val floatingButton = FloatingButtonCreator(context).create()
    FloatingButtonCard(floatingButtonPresentable = floatingButton)
}


@Preview(showBackground = true)
@Composable
fun DefaultPreviewForServicesView() {
    AepsdkcoreandroidTheme {
        ServicesView(rememberNavController())
    }
}