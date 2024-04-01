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

package com.adobe.marketing.mobile.services.ui.floatingbutton

import android.graphics.Bitmap
import com.adobe.marketing.mobile.services.ui.FloatingButton
import com.adobe.marketing.mobile.services.ui.InAppMessage
import com.adobe.marketing.mobile.services.ui.PresentationDelegate
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.ui.common.AppLifecycleProvider
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class FloatingButtonPresentableTest {

    @Mock
    private lateinit var mockFloatingButton: FloatingButton

    @Mock
    private lateinit var mockPresentationDelegate: PresentationDelegate

    @Mock
    private lateinit var mockPresentationUtilityProvider: PresentationUtilityProvider

    @Mock
    private lateinit var mockAppLifecycleProvider: AppLifecycleProvider

    @Mock
    private lateinit var mockFloatingButtonViewModel: FloatingButtonViewModel

    @Mock
    private lateinit var mockFloatingButtonSettings: FloatingButtonSettings

    private lateinit var floatingButtonPresentable: FloatingButtonPresentable

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        `when`(mockFloatingButton.settings).thenReturn(mockFloatingButtonSettings)
        `when`(mockFloatingButtonSettings.initialGraphic).thenReturn(mock(Bitmap::class.java))

        floatingButtonPresentable = FloatingButtonPresentable(
            mockFloatingButton,
            mockFloatingButtonViewModel,
            mockPresentationDelegate,
            mockPresentationUtilityProvider,
            mockAppLifecycleProvider
        )

        verify(mockFloatingButtonViewModel).onGraphicUpdate(mockFloatingButtonSettings.initialGraphic)
    }

    @Test
    fun `Test #gateDisplay`() {
        assertFalse(floatingButtonPresentable.gateDisplay())
    }

    @Test
    fun `Test #getPresentation`() {
        assertEquals(mockFloatingButton, floatingButtonPresentable.getPresentation())
    }

    @Test
    fun `Test #hasConflicts`() {
        assertFalse(
            floatingButtonPresentable.hasConflicts(
                listOf(mock(FloatingButton::class.java), mock(InAppMessage::class.java))
            )
        )

        assertFalse(floatingButtonPresentable.hasConflicts(listOf(mock(FloatingButton::class.java))))
        assertFalse(floatingButtonPresentable.hasConflicts(listOf(mock(InAppMessage::class.java))))
    }
}
