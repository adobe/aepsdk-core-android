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

package com.adobe.marketing.mobile.services.ui.common

import com.adobe.marketing.mobile.services.ui.Alert
import com.adobe.marketing.mobile.services.ui.InAppMessage
import org.junit.After
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PresentationObserverTest {

    @Test
    fun `Test #onPresentationVisible adds the given Presentation to the visible list`() {
        // setup
        val alert = mock(Alert::class.java)
        `when`(alert.id).thenReturn("alertUUID")

        val inAppMessage = mock(InAppMessage::class.java)
        `when`(inAppMessage.id).thenReturn("inAppMessageUUID")

        // test
        PresentationObserver.INSTANCE.onPresentationVisible(alert)
        PresentationObserver.INSTANCE.onPresentationVisible(inAppMessage)

        // verify
        val visible = PresentationObserver.INSTANCE.getVisiblePresentations()
        assertTrue(visible.contains(alert))
    }

    @Test
    fun `Test #onPresentationVisible allows multiple Presentations of same kind`() {
        // setup
        val alert = mock(Alert::class.java)
        `when`(alert.id).thenReturn("alertUUID_1")

        val anotherAlert = mock(Alert::class.java)
        `when`(anotherAlert.id).thenReturn("alertUUID_2")

        // test
        PresentationObserver.INSTANCE.onPresentationVisible(alert)
        PresentationObserver.INSTANCE.onPresentationVisible(anotherAlert)

        // verify that PresentationObserver contains both the alerts
        val visible = PresentationObserver.INSTANCE.getVisiblePresentations()
        assertTrue(visible.contains(alert))
        assertTrue(visible.contains(anotherAlert))
    }

    @Test
    fun `test #onPresentation is a no-op when presentation already visible`() {
        // setup
        val alert = mock(Alert::class.java)
        `when`(alert.id).thenReturn("alertUUID")

        // test
        PresentationObserver.INSTANCE.onPresentationVisible(alert)
        PresentationObserver.INSTANCE.onPresentationVisible(alert)

        // verify
        val visible = PresentationObserver.INSTANCE.getVisiblePresentations()
        assertTrue(visible.contains(alert))
        assertEquals(1, visible.size)
    }

    @Test
    fun `Test #onPresentationInvisible removes the presentation from visible list`() {
        // setup
        val alert = mock(Alert::class.java)
        `when`(alert.id).thenReturn("alertUUID")

        val inAppMessage = mock(InAppMessage::class.java)
        `when`(inAppMessage.id).thenReturn("inAppMessageUUID")

        // test
        PresentationObserver.INSTANCE.onPresentationVisible(alert)
        PresentationObserver.INSTANCE.onPresentationVisible(inAppMessage)

        PresentationObserver.INSTANCE.onPresentationInvisible(alert)

        // verify
        val visible = PresentationObserver.INSTANCE.getVisiblePresentations()
        assertTrue(visible.contains(inAppMessage))
        assertFalse(visible.contains(alert))
    }

    @Test
    fun `Test #onPresentationInvisible is a no-op when presentation already invisible`() {
        // setup
        val alert = mock(Alert::class.java)
        `when`(alert.id).thenReturn("alertUUID")

        // test
        PresentationObserver.INSTANCE.onPresentationVisible(alert)
        PresentationObserver.INSTANCE.onPresentationInvisible(alert)
        PresentationObserver.INSTANCE.onPresentationInvisible(alert)

        // verify
        val visible = PresentationObserver.INSTANCE.getVisiblePresentations()
        assertFalse(visible.contains(alert))
        assertEquals(0, visible.size)
    }

    @After
    fun tearDown() {
        val presentations = PresentationObserver.INSTANCE.getVisiblePresentations()
        // reset the visible presentations
        presentations.forEach {
            PresentationObserver.INSTANCE.onPresentationInvisible(it)
        }
    }
}
