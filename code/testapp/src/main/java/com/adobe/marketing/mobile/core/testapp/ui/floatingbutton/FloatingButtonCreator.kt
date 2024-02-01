package com.adobe.marketing.mobile.core.testapp.ui.floatingbutton

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.adobe.marketing.mobile.core.testapp.R
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ui.FloatingButton
import com.adobe.marketing.mobile.services.ui.Presentable
import com.adobe.marketing.mobile.services.ui.PresentationError
import com.adobe.marketing.mobile.services.ui.floatingbutton.FloatingButtonEventListener
import com.adobe.marketing.mobile.services.ui.floatingbutton.FloatingButtonSettings
import com.adobe.marketing.mobile.util.DefaultPresentationUtilityProvider

class FloatingButtonCreator(context: Context) {

    private val drawable: Drawable =
        ContextCompat.getDrawable(context, R.drawable.ic_floating_button)!!

    private val floatingButtonSettings = FloatingButtonSettings.Builder()
        .initialGraphic(drawable.toBitmap())
        .cornerRadius(10f)
        .build()

    private val floatingButtonEventListener = object : FloatingButtonEventListener {
        override fun onTapDetected(presentable: Presentable<FloatingButton>) {}
        override fun onPanDetected(presentable: Presentable<FloatingButton>) {}
        override fun onShow(presentable: Presentable<FloatingButton>) {}
        override fun onHide(presentable: Presentable<FloatingButton>) {}
        override fun onDismiss(presentable: Presentable<FloatingButton>) {}
        override fun onError(presentable: Presentable<FloatingButton>, error: PresentationError) {}
    }

    fun create(): Presentable<FloatingButton> = ServiceProvider.getInstance().uiService.create(
        FloatingButton(
            floatingButtonSettings,
            floatingButtonEventListener
        ), DefaultPresentationUtilityProvider()
    )
}