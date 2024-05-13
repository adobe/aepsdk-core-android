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

import com.adobe.marketing.mobile.services.ui.Alert
import com.adobe.marketing.mobile.services.ui.FloatingButton
import com.adobe.marketing.mobile.services.ui.InAppMessage
import com.adobe.marketing.mobile.services.ui.PresentationDelegate
import com.adobe.marketing.mobile.services.ui.PresentationListener
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.ui.common.AppLifecycleProvider
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import kotlin.test.assertEquals
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
    private lateinit var mockInAppMessageEventListener: InAppMessageEventListener

    @Mock
    private lateinit var mockScope: CoroutineScope

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

        `when`(mockInAppMessage.eventListener).thenReturn(mockInAppMessageEventListener)
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
    fun `Test #handleInAppUri invokes InAppMessageEventListener first`() {
        val uri = "adbinapp://dismiss?interaction=customInteraction"
        `when`(mockInAppMessageEventListener.onUrlLoading(inAppMessagePresentable, uri)).thenReturn(
            true
        )

        assertTrue(inAppMessagePresentable.handleInAppUri(uri))

        verify(mockInAppMessageEventListener, times(1)).onUrlLoading(inAppMessagePresentable, uri)
        verify(mockPresentationUtilityProvider, times(0)).openUri(uri)

        val presentationContentCaptor = argumentCaptor<PresentationListener.PresentationContent>()
        verify(mockPresentationDelegate).onContentLoaded(
            eq(inAppMessagePresentable),
            presentationContentCaptor.capture()
        )
        assertTrue(presentationContentCaptor.firstValue is PresentationListener.PresentationContent.UrlContent)
        assertEquals(
            (presentationContentCaptor.firstValue as PresentationListener.PresentationContent.UrlContent).url,
            uri
        )
    }

    @Test
    fun `Test that #handleInAppUri attempts to open uri if InAppMessageEventListener does not handle it`() {
        val uri = "adbinapp://dismiss?interaction=customInteraction"
        `when`(mockInAppMessageEventListener.onUrlLoading(inAppMessagePresentable, uri)).thenReturn(
            false
        )
        `when`(mockPresentationUtilityProvider.openUri(uri)).thenReturn(true)

        assertTrue(inAppMessagePresentable.handleInAppUri(uri))
        verify(mockInAppMessageEventListener, times(1)).onUrlLoading(inAppMessagePresentable, uri)
        verify(mockPresentationUtilityProvider, times(1)).openUri(uri)

        val presentationContentCaptor = argumentCaptor<PresentationListener.PresentationContent>()
        verify(mockPresentationDelegate).onContentLoaded(
            eq(inAppMessagePresentable),
            presentationContentCaptor.capture()
        )
        assertTrue(presentationContentCaptor.firstValue is PresentationListener.PresentationContent.UrlContent)
        assertEquals(
            (presentationContentCaptor.firstValue as PresentationListener.PresentationContent.UrlContent).url,
            uri
        )
    }

    @Test
    fun `Test that #handleInAppUri returns false the uri cannot be handled by uri opening or the listener `() {
        val uri = "adbinapp://dismiss?interaction=customInteraction"
        `when`(mockInAppMessageEventListener.onUrlLoading(inAppMessagePresentable, uri)).thenReturn(
            false
        )
        `when`(mockPresentationUtilityProvider.openUri(uri)).thenReturn(false)

        assertFalse(inAppMessagePresentable.handleInAppUri(uri))
        verify(mockInAppMessageEventListener, times(1)).onUrlLoading(inAppMessagePresentable, uri)
        verify(mockPresentationUtilityProvider, times(1)).openUri(uri)
        verify(mockPresentationDelegate, times(0)).onContentLoaded(any(), any())
    }
}
