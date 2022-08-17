package com.adobe.marketing.mobile.app.kotlin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun HomeView(navController: NavHostController) {
    Column(Modifier.padding(8.dp)) {
        Button(onClick = {
            navController.navigate(NavRoutes.CoreView.route)
        }) {
            Text(text = "Core")
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