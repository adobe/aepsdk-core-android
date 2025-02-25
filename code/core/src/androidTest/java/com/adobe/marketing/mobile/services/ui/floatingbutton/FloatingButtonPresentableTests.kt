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

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import com.adobe.marketing.mobile.services.ui.FloatingButton
import com.adobe.marketing.mobile.services.ui.PresentationDelegate
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.ui.common.AppLifecycleProvider
import com.adobe.marketing.mobile.services.ui.floatingbutton.FloatingButtonPresentable
import com.adobe.marketing.mobile.services.ui.floatingbutton.FloatingButtonSettings
import com.adobe.marketing.mobile.services.ui.floatingbutton.FloatingButtonViewModel
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class FloatingButtonPresentableTests {
    @Mock
    private lateinit var mockFloatingButton: FloatingButton

    @Mock
    private lateinit var mockFloatingButtonSettings: FloatingButtonSettings

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Mock the settings and initial graphic
        `when`(mockFloatingButton.settings).thenReturn(mockFloatingButtonSettings)
        `when`(mockFloatingButtonSettings.initialGraphic).thenReturn(mock(android.graphics.Bitmap::class.java))
    }

    @Test
    fun test_getContentReturnsComposeViewWithThemeWrapper() = runTest {
        // setup
        val context = ApplicationProvider.getApplicationContext<Context>()
        val floatingButtonViewModel = mock(FloatingButtonViewModel::class.java)
        val presentationDelegate = mock(PresentationDelegate::class.java)
        val presentationUtilityProvider = mock(PresentationUtilityProvider::class.java)
        val appLifecycleProvider = mock(AppLifecycleProvider::class.java)
        val mainScope = CoroutineScope(Dispatchers.Main)

        val presentable = FloatingButtonPresentable(
            mockFloatingButton,
            floatingButtonViewModel,
            presentationDelegate,
            presentationUtilityProvider,
            appLifecycleProvider,
            mainScope
        )

        // test
        val composeView = presentable.getContent(context)

        // verify
        assertTrue(composeView.context is ContextThemeWrapper)
        val themedContext = composeView.context as ContextThemeWrapper
        val theme = themedContext.theme
        // get android.background from the theme
        val background = theme.obtainStyledAttributes(intArrayOf(android.R.attr.background))
        // Verify that the background attribute is added
        assertTrue(background.hasValue(0))
        // Verify that the background attribute is transparent
        assertTrue(background.peekValue(0).resourceId == android.R.color.transparent)
    }
}
