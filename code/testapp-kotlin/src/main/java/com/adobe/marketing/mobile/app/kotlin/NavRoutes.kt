package com.adobe.marketing.mobile.app.kotlin

sealed class NavRoutes(val route: String) {
    object HomeView : NavRoutes("home")
    object CoreView : NavRoutes("core")
    object SignalView : NavRoutes("signal")
    object LifecycleView : NavRoutes("lifecycle")
}