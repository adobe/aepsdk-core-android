package com.adobe.marketing.mobile.app.kotlin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.adobe.marketing.mobile.app.kotlin.ui.theme.AepsdkcoreandroidTheme

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
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
            }) {
                Text(text = "extensionVersion")
            }
            Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
            }) {
                Text(text = "updateConfiguration")
            }
            Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
            }) {
                Text(text = "clearUpdatedConfiguration")
            }
            Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
            }) {
                Text(text = "setPrivacyStatus(OptIn)")
            }
            Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
            }) {
                Text(text = "getPrivacyStatus")
            }
            Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
            }) {
                Text(text = "log")
            }
            Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
            }) {
                Text(text = "setLogLevel(LogLevel.VERBOSE)")
            }
            Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
            }) {
                Text(text = "getLogLevel")
            }
            Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
            }) {
                Text(text = "setPushIdentifier")
            }
            Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
            }) {
                Text(text = "setAdvertisingIdentifier")
            }
            Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
            }) {
                Text(text = "getSdkIdentities")
            }
            Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
            }) {
                Text(text = "collectPii")
            }
            Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
            }) {
                Text(text = "trackAction")
            }
            Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
            }) {
                Text(text = "trackState")
            }
            Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
            }) {
                Text(text = "resetIdentities")
            }

        }

    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreviewForCoreView() {
    AepsdkcoreandroidTheme {
        CoreView(rememberNavController())
    }
}