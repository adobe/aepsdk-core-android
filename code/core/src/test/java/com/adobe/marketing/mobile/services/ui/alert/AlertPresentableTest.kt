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

package com.adobe.marketing.mobile.services.ui.alert

import com.adobe.marketing.mobile.services.ui.Alert
import com.adobe.marketing.mobile.services.ui.FloatingButton
import com.adobe.marketing.mobile.services.ui.InAppMessage
import com.adobe.marketing.mobile.services.ui.PresentationDelegate
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.ui.common.AppLifecycleProvider
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AlertPresentableTest {

    @Mock
    private lateinit var mockAlert: Alert

    @Mock
    private lateinit var mockPresentationDelegate: PresentationDelegate

    @Mock
    private lateinit var mockPresentationUtilityProvider: PresentationUtilityProvider

    @Mock
    private lateinit var mockAppLifecycleProvider: AppLifecycleProvider

    private lateinit var alertPresentable: AlertPresentable

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        alertPresentable = AlertPresentable(mockAlert, mockPresentationDelegate, mockPresentationUtilityProvider, mockAppLifecycleProvider)
    }

    @Test
    fun `Test #gateDisplay`() {
        assertFalse(alertPresentable.gateDisplay())
    }

    @Test
    fun `Test #getPresentation`() {
        assertEquals(mockAlert, alertPresentable.getPresentation())
    }

    @Test
    fun `Test #hasConflicts`() {
        assertTrue(
            alertPresentable.hasConflicts(
                listOf(
                    mock(Alert::class.java),
                    mock(InAppMessage::class.java),
                    mock(FloatingButton::class.java)
                )
            )
        )

        assertFalse(
            alertPresentable.hasConflicts(
                listOf(
                    mock(FloatingButton::class.java)
                )
            )
        )

        assertTrue(
            alertPresentable.hasConflicts(
                listOf(
                    mock(InAppMessage::class.java)
                )
            )
        )

        assertTrue(
            alertPresentable.hasConflicts(
                listOf(
                    mock(Alert::class.java)
                )
            )
        )
    }
}
