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

package com.adobe.marketing.mobile.services.ui.common

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.compose.ui.platform.ComposeView
import androidx.test.core.app.ApplicationProvider
import com.adobe.marketing.mobile.internal.util.ActivityCompatOwnerUtils
import com.adobe.marketing.mobile.services.ui.InAppMessage
import com.adobe.marketing.mobile.services.ui.Presentation
import com.adobe.marketing.mobile.services.ui.PresentationDelegate
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Tests for [AEPPresentable] that require mocking of Android framework classes.
 */
class AEPPresentableTests {

    @Test
    fun test_getThemedContextWhenNewThemeCreationFails() {
        // setup
        val context = mock(Context::class.java)
        val presentationUtilityProvider = mock(PresentationUtilityProvider::class.java)
        val presentationDelegate = mock(PresentationDelegate::class.java)
        val appLifecycleProvider = mock(AppLifecycleProvider::class.java)
        val presentationStateManager = mock(PresentationStateManager::class.java)
        val activityCompatOwnerUtils = mock(ActivityCompatOwnerUtils::class.java)
        val mainScope = mock(CoroutineScope::class.java)
        val presentationObserver = mock(PresentationObserver::class.java)
        val presentation = mock(InAppMessage::class.java)

        val presentable = SampleAEPPresentable(
            presentation,
            presentationUtilityProvider,
            presentationDelegate,
            appLifecycleProvider,
            presentationStateManager,
            activityCompatOwnerUtils,
            mainScope,
            presentationObserver
        )

        val resources = mock(android.content.res.Resources::class.java)
        `when`(context.resources).thenReturn(resources)
        `when`(resources.newTheme()).thenThrow(RuntimeException("Resources.newTheme() invocation failure."))

        // test
        val themedContext = presentable.getThemedContext(context)

        // verify
        assertFalse(themedContext is ContextThemeWrapper)
        assertEquals(context, themedContext) // No wrapping should occur if theme creation fails
    }

    @Test
    fun test_getThemedContextWhenApplyStyleFails() {
        // setup
        val context = mock(Context::class.java)
        val presentationUtilityProvider = mock(PresentationUtilityProvider::class.java)
        val presentationDelegate = mock(PresentationDelegate::class.java)
        val appLifecycleProvider = mock(AppLifecycleProvider::class.java)
        val presentationStateManager = mock(PresentationStateManager::class.java)
        val activityCompatOwnerUtils = mock(ActivityCompatOwnerUtils::class.java)
        val mainScope = mock(CoroutineScope::class.java)
        val presentationObserver = mock(PresentationObserver::class.java)
        val presentation = mock(InAppMessage::class.java)

        val presentable = SampleAEPPresentable(
            presentation,
            presentationUtilityProvider,
            presentationDelegate,
            appLifecycleProvider,
            presentationStateManager,
            activityCompatOwnerUtils,
            mainScope,
            presentationObserver
        )

        val resources = mock(android.content.res.Resources::class.java)
        `when`(context.resources).thenReturn(resources)
        val theme = mock(android.content.res.Resources.Theme::class.java)
        `when`(resources.newTheme()).thenReturn(theme)

        `when`(theme.applyStyle(anyInt(), eq(true))).thenThrow(RuntimeException("Theme.applyStyle() invocation failure."))

        // test
        val themedContext = presentable.getThemedContext(context)
        assertFalse(themedContext is ContextThemeWrapper)
        assertEquals(context, themedContext) // No wrapping should occur if theme creation fails
    }

    @Test
    fun test_getThemedContextSucceedsWithThemeOverride() {
        // setup
        val context = ApplicationProvider.getApplicationContext<Context>()
        val presentationUtilityProvider = mock(PresentationUtilityProvider::class.java)
        val presentationDelegate = mock(PresentationDelegate::class.java)
        val appLifecycleProvider = mock(AppLifecycleProvider::class.java)
        val presentationStateManager = mock(PresentationStateManager::class.java)
        val activityCompatOwnerUtils = mock(ActivityCompatOwnerUtils::class.java)
        val mainScope = mock(CoroutineScope::class.java)
        val presentationObserver = mock(PresentationObserver::class.java)
        val presentation = mock(InAppMessage::class.java)

        val presentable = SampleAEPPresentable(
            presentation,
            presentationUtilityProvider,
            presentationDelegate,
            appLifecycleProvider,
            presentationStateManager,
            activityCompatOwnerUtils,
            mainScope,
            presentationObserver
        )

        // test
        val themedContext = presentable.getThemedContext(context)
        assertTrue(themedContext is ContextThemeWrapper)
        val themedContextWrapper = themedContext as ContextThemeWrapper
        val background = themedContextWrapper.theme.obtainStyledAttributes(intArrayOf(android.R.attr.background))
        assertTrue(background.hasValue(0))
        assertTrue(background.peekValue(0).resourceId == android.R.color.transparent)
    }

    /**
     * A sample implementation of [AEPPresentable]. Make an effort to keep this presentation type
     * agnostic to avoid test pollution.
     */
    internal class SampleAEPPresentable(
        private val presentation: InAppMessage,
        presentationUtilityProvider: PresentationUtilityProvider,
        presentationDelegate: PresentationDelegate?,
        appLifecycleProvider: AppLifecycleProvider,
        presentationStateManager: PresentationStateManager,
        activityCompatOwnerUtils: ActivityCompatOwnerUtils,
        mainScope: CoroutineScope,
        presentationObserver: PresentationObserver,
    ) : AEPPresentable<InAppMessage>(
        presentation,
        presentationUtilityProvider,
        presentationDelegate,
        appLifecycleProvider,
        presentationStateManager,
        activityCompatOwnerUtils,
        mainScope,
        presentationObserver
    ) {
        override fun getPresentation(): InAppMessage {
            return presentation
        }

        override fun getContent(activityContext: Context): ComposeView {
            return ComposeView(activityContext)
        }

        override fun gateDisplay(): Boolean {
            return true
        }

        override fun hasConflicts(visiblePresentations: List<Presentation<*>>): Boolean {
            return false
        }
    }
}
