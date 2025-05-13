/*
  Copyright 2025 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services.ui.message.views

import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.services.ui.InAppMessage
import com.adobe.marketing.mobile.services.ui.PresentationDelegate
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.ui.common.AppLifecycleProvider
import com.adobe.marketing.mobile.services.ui.message.InAppMessagePresentable
import com.adobe.marketing.mobile.services.ui.message.InAppMessageSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.nio.charset.StandardCharsets

/**
 * Pseudo-unit tests for [InAppMessagePresentable] that rely on mocking Android framework
 * components.
 * See also [InAppMessagePresentableTest] for unit tests that do not rely on Android framework
 */
@RunWith(AndroidJUnit4::class)
internal class InAppMessagePresentableTests {

    @Mock
    private lateinit var mockInAppMessage: InAppMessage

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(mockInAppMessage.settings).thenReturn(InAppMessageSettings.Builder().build())
    }

    @Test
    fun testInAppMessagePresentableDefaultWebViewSettings() {
        // setup
        val context = ApplicationProvider.getApplicationContext<Context>()
        val presentationDelegate = mock(PresentationDelegate::class.java)
        val presentationUtilityProvider = mock(PresentationUtilityProvider::class.java)
        val appLifecycleProvider = mock(AppLifecycleProvider::class.java)
        val mainScope = CoroutineScope(Dispatchers.Main)

        // Create a mock WebView
        val webView = mock(WebView::class.java)
        val webSettings = mock(WebSettings::class.java)
        `when`(webView.settings).thenReturn(webSettings)

        // Create an instance of InAppMessagePresentable
        val presentable = InAppMessagePresentable(
            inAppMessage = mockInAppMessage,
            presentationDelegate = presentationDelegate,
            presentationUtilityProvider = presentationUtilityProvider,
            appLifecycleProvider = appLifecycleProvider,
            mainScope = mainScope
        )

        presentable.applyWebViewSettings(webView)

        // Verify that the default settings were applied to the WebView
        verify(webSettings).javaScriptEnabled = true
        verify(webSettings).allowFileAccess = false
        verify(webSettings).domStorageEnabled = true
        verify(webSettings).layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        verify(webSettings).defaultTextEncodingName = StandardCharsets.UTF_8.name()
        verify(webSettings).mediaPlaybackRequiresUserGesture = false
        verify(webSettings, times(0)).databaseEnabled = anyBoolean()
    }
}
