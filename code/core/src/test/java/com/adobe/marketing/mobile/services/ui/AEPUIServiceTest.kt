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

package com.adobe.marketing.mobile.services.ui

import android.app.Application
import android.graphics.Bitmap
import com.adobe.marketing.mobile.services.ui.alert.AlertEventListener
import com.adobe.marketing.mobile.services.ui.alert.AlertPresentable
import com.adobe.marketing.mobile.services.ui.alert.AlertSettings
import com.adobe.marketing.mobile.services.ui.floatingbutton.FloatingButtonEventListener
import com.adobe.marketing.mobile.services.ui.floatingbutton.FloatingButtonPresentable
import com.adobe.marketing.mobile.services.ui.floatingbutton.FloatingButtonSettings
import com.adobe.marketing.mobile.services.ui.message.InAppMessageEventListener
import com.adobe.marketing.mobile.services.ui.message.InAppMessagePresentable
import com.adobe.marketing.mobile.services.ui.message.InAppMessageSettings
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AEPUIServiceTest {

    private val aepUiService = AEPUIService()

    @Mock private lateinit var mockPresentationUtilityProvider: PresentationUtilityProvider

    @Mock private lateinit var mockApplication: Application

    @Mock private lateinit var mockBitmap: Bitmap

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(mockPresentationUtilityProvider.getApplication()).thenReturn(mockApplication)
    }

    @Test
    fun `Test #create InAppMessage`() {
        val iamSettings: InAppMessageSettings = InAppMessageSettings.Builder().build()
        val iamEventListener: InAppMessageEventListener = mock(InAppMessageEventListener::class.java)
        val iamPresentation = InAppMessage(iamSettings, iamEventListener)
        val messagePresentable = aepUiService.create(iamPresentation, mockPresentationUtilityProvider)

        assertNotNull(messagePresentable)
        assertTrue(messagePresentable is InAppMessagePresentable)
        assertEquals(iamPresentation, messagePresentable.getPresentation())
        assertEquals(iamSettings, messagePresentable.getPresentation().settings)
        assertEquals(iamEventListener, messagePresentable.getPresentation().eventListener)
        assertNotNull(messagePresentable.getPresentation().eventListener)
        assertNotNull(messagePresentable.getPresentation().eventHandler)
    }

    @Test
    fun `Test #create FloatingButton`() {
        val floatingButtonSettings: FloatingButtonSettings = FloatingButtonSettings.Builder().initialGraphic(mockBitmap).build()
        val floatingButtonEventListener: FloatingButtonEventListener = mock(
            FloatingButtonEventListener::class.java
        )
        val floatingButtonPresentation = FloatingButton(floatingButtonSettings, floatingButtonEventListener)
        val floatingButtonPresentable = aepUiService.create(floatingButtonPresentation, mockPresentationUtilityProvider)

        assertNotNull(floatingButtonPresentable)
        assertTrue(floatingButtonPresentable is FloatingButtonPresentable)
        assertEquals(floatingButtonPresentation, floatingButtonPresentable.getPresentation())
        assertEquals(floatingButtonSettings, floatingButtonPresentable.getPresentation().settings)
        assertEquals(floatingButtonEventListener, floatingButtonPresentable.getPresentation().eventListener)
        assertNotNull(floatingButtonPresentable.getPresentation().eventListener)
        assertNotNull(floatingButtonPresentable.getPresentation().eventHandler)
    }

    @Test
    fun `Test #create Alert`() {
        val alertSettings: AlertSettings = AlertSettings.Builder().positiveButtonText("").build()
        val alertEventListener: AlertEventListener = mock(AlertEventListener::class.java)
        val alertPresentation = Alert(alertSettings, alertEventListener)
        val alertPresentable = aepUiService.create(alertPresentation, mockPresentationUtilityProvider)

        assertNotNull(alertPresentable)
        assertTrue(alertPresentable is AlertPresentable)
        assertEquals(alertPresentation, alertPresentable.getPresentation())
        assertEquals(alertSettings, alertPresentable.getPresentation().settings)
        assertEquals(alertEventListener, alertPresentable.getPresentation().eventListener)
        assertNotNull(alertPresentable.getPresentation().eventListener)
    }
}
