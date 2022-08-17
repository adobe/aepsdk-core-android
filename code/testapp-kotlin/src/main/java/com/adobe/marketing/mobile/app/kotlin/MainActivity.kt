package com.adobe.marketing.mobile.app.kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adobe.marketing.mobile.app.kotlin.ui.theme.AepsdkcoreandroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AepsdkcoreandroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
//                    Greeting("Android")
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

        composable(NavRoutes.SignalView.route) {
            SignalView(navController = navController)
        }
        composable(NavRoutes.LifecycleView.route) {
            LifecycleView(navController = navController)
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AepsdkcoreandroidTheme {
        Greeting("Android")
    }
}