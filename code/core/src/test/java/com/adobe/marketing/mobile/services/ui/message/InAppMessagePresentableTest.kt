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
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.ui.common.AppLifecycleProvider
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
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
}
