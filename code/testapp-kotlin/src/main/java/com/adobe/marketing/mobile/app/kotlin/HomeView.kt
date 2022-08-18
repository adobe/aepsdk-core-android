package com.adobe.marketing.mobile.app.kotlin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
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
fun HomeView(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            navController.navigate(NavRoutes.CoreView.route)
        }) {
            Text(text = "Core")
        }
        Button(onClick = {
            navController.navigate(NavRoutes.ServicesView.route)
        }) {
            Text(text = "Service")
        }
        Button(onClick = {
            navController.navigate(NavRoutes.SignalView.route)
        }) {
            Text(text = "Signal")
        }
        Button(onClick = {
            navController.navigate(NavRoutes.LifecycleView.route)
        }) {
            Text(text = "Lifecycle")
        }

    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreviewForHomeView() {
    AepsdkcoreandroidTheme {
        HomeView(rememberNavController())
    }
}