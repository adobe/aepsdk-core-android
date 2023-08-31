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

package com.adobe.marketing.mobile.services.ui.vnext

import android.app.Application
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageEventListener
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessagePresentable
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageSettings
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AEPUIServiceTest {

    private val aepUiService = AEPUIService()

    @Mock private lateinit var mockPresentationUtilityProvider: PresentationUtilityProvider

    @Mock private lateinit var mockApplication: Application

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(mockPresentationUtilityProvider.getApplication()).thenReturn(mockApplication)
    }

    @Test
    fun `Test #create InAppMessage`() {
        val iamSettings: InAppMessageSettings = InAppMessageSettings.Builder().build()
        val iamEventListener: InAppMessageEventListener = Mockito.mock(InAppMessageEventListener::class.java)
        val iamPresentation = InAppMessage(iamSettings, iamEventListener)
        val messagePresentable = aepUiService.create(iamPresentation, mockPresentationUtilityProvider)

        assertNotNull(messagePresentable)
        assertTrue(messagePresentable is InAppMessagePresentable)
        assertEquals(iamPresentation, messagePresentable.getPresentation())
        assertEquals(iamSettings, messagePresentable.getPresentation().settings)
        assertEquals(iamEventListener, messagePresentable.getPresentation().eventListener)
        assertNotNull(messagePresentable.getPresentation().eventListener)
    }
}
