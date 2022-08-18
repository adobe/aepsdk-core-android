package com.adobe.marketing.mobile.app.kotlin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.adobe.marketing.mobile.app.kotlin.ui.theme.AepsdkcoreandroidTheme
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ui.AlertSetting
import com.adobe.marketing.mobile.services.ui.NotificationSetting
import java.util.*
import kotlin.concurrent.schedule


private val uiService = ServiceProvider.getInstance().uiService

@Composable
fun ServicesView(navController: NavHostController) {
    Column(Modifier.padding(8.dp)) {
        Button(onClick = {
            navController.navigate(NavRoutes.HomeView.route)
        }) {
            Text(text = "Home")
        }
        Spacer(modifier = Modifier.size(10.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            elevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "UI Service")
                Button(onClick = {
                    showAlert()
                }) {
                    Text(text = "ALERT")
                }
                Button(onClick = {
                    openUrl()
                }) {
                    Text(text = "OPEN URL")
                }
                Button(onClick = {
                    //TODO: not showing
                    showLocalNotification()
                }) {
                    Text(text = "LOCAL NOTIFICATION")
                }

                Button(onClick = {
//                navController.navigate(NavRoutes.HomeView.route)
                }) {
                    Text(text = "FLOATING BUTTON (5s)")
                }


            }
        }

    }
}

private fun showAlert() {
    uiService.showAlert(
        AlertSetting.build(
            "title",
            "message",
            "positive",
            "negative"
        ), null
    )
}

private fun openUrl() {
    uiService.showUrl("https://adobe.com")
}

private fun showLocalNotification() {
    uiService.showLocalNotification(
        NotificationSetting.build(
            "id",
            "Content",
            System.currentTimeMillis() / 1000,
            0,
            "myscheme://link",
            null,
            "sound.wav",
            "title"
        )
    )
}

private fun showFloatingButton() {
    val floatingButton = uiService.createFloatingButton(null)
    floatingButton.display()
    Timer("SettingUp", false).schedule(500) {
//        doSomething()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreviewForServicesView() {
    AepsdkcoreandroidTheme {
        ServicesView(rememberNavController())
    }
}