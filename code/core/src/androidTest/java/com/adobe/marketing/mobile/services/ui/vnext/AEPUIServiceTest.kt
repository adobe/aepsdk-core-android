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

import android.app.Activity
import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import com.adobe.marketing.mobile.services.ui.vnext.floatingbutton.FloatingButtonEventListener
import com.adobe.marketing.mobile.services.ui.vnext.floatingbutton.FloatingButtonPresentable
import com.adobe.marketing.mobile.services.ui.vnext.floatingbutton.FloatingButtonSettings
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.io.InputStream

/**
 * Tests for [AEPUIService] for cases where Android specific api's are used in the implementation
 * which cannot be mocked in unit tests.
 */
class AEPUIServiceTest {

    @Test
    fun testCreate_FloatingButton() {
        // Setup
        val aepUiService = AEPUIService()
        val floatingButtonSettings: FloatingButtonSettings = FloatingButtonSettings.Builder().build()
        val floatingButtonEventListener: FloatingButtonEventListener = object : FloatingButtonEventListener {
            override fun onTapDetected(presentable: Presentable<FloatingButton>) {}
            override fun onPanDetected(presentable: Presentable<FloatingButton>) {}
            override fun onShow(presentable: Presentable<FloatingButton>) {}
            override fun onHide(presentable: Presentable<FloatingButton>) {}
            override fun onDismiss(presentable: Presentable<FloatingButton>) {}
            override fun onError(
                presentable: Presentable<FloatingButton>,
                error: PresentationError
            ) {}
        }

        val presentationUtilityProvider = object : PresentationUtilityProvider {
            override fun getApplication(): Application {
                return InstrumentationRegistry.getInstrumentation().context.applicationContext as Application
            }

            override fun getCurrentActivity(): Activity? {
                return InstrumentationRegistry.getInstrumentation().context as Activity
            }

            override fun getCachedContent(cacheName: String, key: String): InputStream? {
                return null
            }
        }

        val floatingButtonPresentation = FloatingButton(floatingButtonSettings, floatingButtonEventListener)

        // Test
        val floatingButtonPresentable = aepUiService.create(floatingButtonPresentation, presentationUtilityProvider)

        // Verify
        assertNotNull(floatingButtonPresentable)
        assertTrue(floatingButtonPresentable is FloatingButtonPresentable)
        assertEquals(floatingButtonPresentation, floatingButtonPresentable.getPresentation())
        assertEquals(floatingButtonSettings, floatingButtonPresentable.getPresentation().settings)
        assertEquals(floatingButtonEventListener, floatingButtonPresentable.getPresentation().eventListener)
        assertNotNull(floatingButtonPresentable.getPresentation().eventListener)
    }
}
