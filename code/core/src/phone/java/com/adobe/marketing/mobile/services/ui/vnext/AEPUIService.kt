package com.adobe.marketing.mobile.services.ui.vnext

import com.adobe.marketing.mobile.services.ui.vnext.common.AppLifecycleProvider
import com.adobe.marketing.mobile.services.ui.vnext.message.MessagePresentable

class AEPUIService : UIService {
    private var presentationDelegate: PresentationDelegate? = null

    @Suppress("UNCHECKED_CAST")
    override fun <T : Presentation<*>> create(
        presentation: T,
        presentationUtilityProvider: PresentationUtilityProvider
    ): Presentable<T> {

        // start the app lifecycle provider if not started
        AppLifecycleProvider.INSTANCE.start(presentationUtilityProvider.getApplication())

        when (presentation) {

            is InAppMessage -> {
                return MessagePresentable(
                    presentation,
                    presentationDelegate,
                    presentationUtilityProvider,
                    AppLifecycleProvider.INSTANCE
                ) as Presentable<T>
            }
            else -> {
                throw IllegalArgumentException("Presentation type not supported")
            }
        }
    }

    override fun setPresentationDelegate(presentationDelegate: PresentationDelegate) {
        this.presentationDelegate = presentationDelegate
    }
}