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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adobe.marketing.mobile.core.testapp.ui.theme.AEPSDKCoreAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AEPSDKCoreAndroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    NavigationInit()
                }
            }
        }
    }
}

@Composable
fun NavigationInit() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.HomeView.route,
    ) {
        composable(NavRoutes.HomeView.route) {
            HomeView(navController = navController)
        }

        composable(NavRoutes.CoreView.route) {
            CoreView(navController = navController)
        }

        composable(NavRoutes.ServicesView.route) {
            ServicesView(navController = navController)
        }

        composable(NavRoutes.SignalView.route) {
            SignalView(navController = navController)
        }

        composable(NavRoutes.IdentityView.route) {
            IdentityView(navController = navController)
        }

        composable(NavRoutes.LifecycleView.route) {
            LifecycleView(navController = navController)
        }

        composable(NavRoutes.PerformanceView.route) {
            PerformanceView(navController = navController)
        }
    }
}
