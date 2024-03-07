package com.adobe.marketing.mobile.core.testapp.ui.alert

import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ui.Alert
import com.adobe.marketing.mobile.services.ui.Presentable
import com.adobe.marketing.mobile.services.ui.PresentationError
import com.adobe.marketing.mobile.services.ui.alert.AlertEventListener
import com.adobe.marketing.mobile.services.ui.alert.AlertSettings
import com.adobe.marketing.mobile.util.DefaultPresentationUtilityProvider

object AlertCreator {
    private val alertSettings = AlertSettings.Builder()
        .positiveButtonText("OK")
        .negativeButtonText("Cancel")
        .title("Why did the JS developer go broke?")
        .message("They used up all their cache!")
        .build()

    private val alertEventListener = object : AlertEventListener {
        override fun onPositiveResponse(alert: Presentable<Alert>) {}
        override fun onNegativeResponse(alert: Presentable<Alert>) {}
        override fun onShow(presentable: Presentable<Alert>) {}
        override fun onHide(presentable: Presentable<Alert>) {}
        override fun onDismiss(presentable: Presentable<Alert>) {}
        override fun onError(presentable: Presentable<Alert>, error: PresentationError) {}
    }

    fun create(): Presentable<Alert> = ServiceProvider.getInstance().uiService.create(
        Alert(alertSettings, alertEventListener), DefaultPresentationUtilityProvider())
}