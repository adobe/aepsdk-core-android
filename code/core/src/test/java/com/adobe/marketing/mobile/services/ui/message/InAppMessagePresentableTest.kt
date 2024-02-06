/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services.ui.message

import android.webkit.WebSettings
import android.webkit.WebView
import com.adobe.marketing.mobile.services.ui.Alert
import com.adobe.marketing.mobile.services.ui.FloatingButton
import com.adobe.marketing.mobile.services.ui.InAppMessage
import com.adobe.marketing.mobile.services.ui.PresentationDelegate
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.ui.common.AppLifecycleProvider
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InAppMessagePresentableTest {

    @Mock
    private lateinit var mockInAppMessage: InAppMessage

    @Mock
    private lateinit var mockPresentationDelegate: PresentationDelegate

    @Mock
    private lateinit var mockPresentationUtilityProvider: PresentationUtilityProvider

    @Mock
    private lateinit var mockAppLifecycleProvider: AppLifecycleProvider

    @Mock
    private lateinit var mockScope: CoroutineScope

    @Mock
    private lateinit var mockWebView: WebView

    @Mock
    private lateinit var mockWebViewSettings: WebSettings

    private lateinit var inAppMessagePresentable: InAppMessagePresentable

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        inAppMessagePresentable = InAppMessagePresentable(
            mockInAppMessage,
            mockPresentationDelegate,
            mockPresentationUtilityProvider,
            mockAppLifecycleProvider,
            mockScope
        )
    }

    @Test
    fun `Test #gateDisplay`() {
        assertTrue(inAppMessagePresentable.gateDisplay())
    }

    @Test
    fun `Test #hasConflicts`() {
        assertTrue(
            inAppMessagePresentable.hasConflicts(
                listOf(
                    mock(InAppMessage::class.java),
                    mock(Alert::class.java)
                )
            )
        )

        assertTrue(inAppMessagePresentable.hasConflicts(listOf(mock(InAppMessage::class.java))))
        assertTrue(inAppMessagePresentable.hasConflicts(listOf(mock(Alert::class.java))))
        assertFalse(inAppMessagePresentable.hasConflicts(listOf(mock(FloatingButton::class.java))))
    }

    @Test
    fun `Test that WebView is applied with default settings by DefaultInAppMessageEventHandler`() {
        `when`(mockWebView.settings).thenReturn(mockWebViewSettings)
        `when`(mockInAppMessage.settings).thenReturn(InAppMessageSettings.Builder().build())

        // Test that the default settings are applied to the WebView
        inAppMessagePresentable.inAppMessageEventHandler.defaultWebViewSettingsApplier(mockWebView)

        // Verify that the default settings are applied to the WebView
        verify(mockWebViewSettings).javaScriptEnabled = true
        verify(mockWebViewSettings).allowFileAccess = false
        verify(mockWebViewSettings).domStorageEnabled = true
        verify(mockWebViewSettings).layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL

        verify(mockWebViewSettings).defaultTextEncodingName = "UTF-8"
        verify(mockWebViewSettings).mediaPlaybackRequiresUserGesture = false
        verify(mockWebViewSettings).databaseEnabled = true

        verify(mockWebView).isVerticalScrollBarEnabled = true
        verify(mockWebView).isHorizontalScrollBarEnabled = true
        verify(mockWebView).isScrollbarFadingEnabled = true
        verify(mockWebView).scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY
        verify(mockWebView).setBackgroundColor(0)
    }
}
