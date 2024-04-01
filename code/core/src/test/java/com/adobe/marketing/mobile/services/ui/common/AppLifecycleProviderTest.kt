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

import android.app.Activity
import android.app.Application
import android.os.Bundle
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import kotlin.test.assertNotNull

class AppLifecycleProviderTest {

    @Mock
    private lateinit var mockApplication: Application

    @Mock
    private lateinit var mockActivity: Activity

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `Test that #start registers an InternalAppLifecycleListener with the Application`() {
        // test
        AppLifecycleProvider.INSTANCE.start(mockApplication)

        // verify
        val lifecycleCallbackCaptor = argumentCaptor<Application.ActivityLifecycleCallbacks>()
        verify(mockApplication).registerActivityLifecycleCallbacks(lifecycleCallbackCaptor.capture())

        val capturedCallback = lifecycleCallbackCaptor.firstValue
        assertNotNull(capturedCallback)
    }

    @Test
    fun `Test that #start invoked multiple times does not register multiple InternalAppLifecycleListeners with the Application`() {
        // test
        AppLifecycleProvider.INSTANCE.start(mockApplication)
        AppLifecycleProvider.INSTANCE.start(mockApplication)
        AppLifecycleProvider.INSTANCE.start(mockApplication)

        // verify
        val lifecycleCallbackCaptor = argumentCaptor<Application.ActivityLifecycleCallbacks>()
        verify(
            mockApplication,
            times(1)
        ).registerActivityLifecycleCallbacks(lifecycleCallbackCaptor.capture())

        val capturedCallback = lifecycleCallbackCaptor.firstValue
        assertNotNull(capturedCallback)
    }

    @Test
    fun `Test that registerListeners are notified onActivityResumed calls`() {
        // setup
        AppLifecycleProvider.INSTANCE.start(mockApplication)
        val lifecycleCallbackCaptor = argumentCaptor<Application.ActivityLifecycleCallbacks>()
        verify(
            mockApplication,
            times(1)
        ).registerActivityLifecycleCallbacks(lifecycleCallbackCaptor.capture())
        val registeredActivityLifecycleCallback = lifecycleCallbackCaptor.firstValue
        val listener1: AppLifecycleProvider.AppLifecycleListener =
            Mockito.mock(AppLifecycleProvider.AppLifecycleListener::class.java)
        val listener2: AppLifecycleProvider.AppLifecycleListener =
            Mockito.mock(AppLifecycleProvider.AppLifecycleListener::class.java)

        // test
        AppLifecycleProvider.INSTANCE.registerListener(listener1)
        AppLifecycleProvider.INSTANCE.registerListener(listener2)
        registeredActivityLifecycleCallback.onActivityResumed(mockActivity)

        // verify
        verify(listener1, times(1)).onActivityResumed(mockActivity)
        verify(listener2, times(1)).onActivityResumed(mockActivity)
    }

    @Test
    fun `Test that registerListeners are notified onActivityDestroyed calls`() {
        // setup
        AppLifecycleProvider.INSTANCE.start(mockApplication)
        val lifecycleCallbackCaptor = argumentCaptor<Application.ActivityLifecycleCallbacks>()
        verify(
            mockApplication,
            times(1)
        ).registerActivityLifecycleCallbacks(lifecycleCallbackCaptor.capture())
        val registeredActivityLifecycleCallback = lifecycleCallbackCaptor.firstValue
        val listener1: AppLifecycleProvider.AppLifecycleListener =
            Mockito.mock(AppLifecycleProvider.AppLifecycleListener::class.java)
        val listener2: AppLifecycleProvider.AppLifecycleListener =
            Mockito.mock(AppLifecycleProvider.AppLifecycleListener::class.java)

        // test
        AppLifecycleProvider.INSTANCE.registerListener(listener1)
        AppLifecycleProvider.INSTANCE.registerListener(listener2)
        registeredActivityLifecycleCallback.onActivityDestroyed(mockActivity)

        // verify
        verify(listener1, times(1)).onActivityDestroyed(mockActivity)
        verify(listener2, times(1)).onActivityDestroyed(mockActivity)
    }

    @Test
    fun `Test that unregisterListeners are not notified on activity callbacks`() {
        // setup
        AppLifecycleProvider.INSTANCE.stop(mockApplication)
        AppLifecycleProvider.INSTANCE.start(mockApplication)
        val lifecycleCallbackCaptor = argumentCaptor<Application.ActivityLifecycleCallbacks>()
        verify(
            mockApplication,
            times(1)
        ).registerActivityLifecycleCallbacks(lifecycleCallbackCaptor.capture())
        val registeredActivityLifecycleCallback = lifecycleCallbackCaptor.firstValue
        val listener1: AppLifecycleProvider.AppLifecycleListener =
            Mockito.mock(AppLifecycleProvider.AppLifecycleListener::class.java)
        val listener2: AppLifecycleProvider.AppLifecycleListener =
            Mockito.mock(AppLifecycleProvider.AppLifecycleListener::class.java)

        // test
        AppLifecycleProvider.INSTANCE.registerListener(listener1)
        AppLifecycleProvider.INSTANCE.registerListener(listener2)
        registeredActivityLifecycleCallback.onActivityResumed(mockActivity)

        AppLifecycleProvider.INSTANCE.unregisterListener(listener1)
        registeredActivityLifecycleCallback.onActivityDestroyed(mockActivity)

        // verify
        verify(listener1, times(1)).onActivityResumed(mockActivity)
        verify(listener2, times(1)).onActivityResumed(mockActivity)
        verify(listener1, times(0)).onActivityDestroyed(mockActivity)
        verify(listener2, times(1)).onActivityDestroyed(mockActivity)
    }

    @Test
    fun `Test that other activity lifecycle methods are no-ops`() {
        // setup
        AppLifecycleProvider.INSTANCE.start(mockApplication)
        val lifecycleCallbackCaptor = argumentCaptor<Application.ActivityLifecycleCallbacks>()
        verify(
            mockApplication,
            times(1)
        ).registerActivityLifecycleCallbacks(lifecycleCallbackCaptor.capture())
        val registeredActivityLifecycleCallback = lifecycleCallbackCaptor.firstValue
        val listener1: AppLifecycleProvider.AppLifecycleListener =
            Mockito.mock(AppLifecycleProvider.AppLifecycleListener::class.java)

        // test
        AppLifecycleProvider.INSTANCE.registerListener(listener1)
        registeredActivityLifecycleCallback.onActivityStarted(mockActivity)
        registeredActivityLifecycleCallback.onActivityPaused(mockActivity)
        registeredActivityLifecycleCallback.onActivityStopped(mockActivity)
        registeredActivityLifecycleCallback.onActivitySaveInstanceState(mockActivity, Bundle())
        registeredActivityLifecycleCallback.onActivityCreated(mockActivity, Bundle())

        // verify
        verifyNoMoreInteractions(listener1)
    }

    @After
    fun tearDown() {
        reset(mockApplication)
        reset(mockActivity)
        AppLifecycleProvider.INSTANCE.stop(mockApplication)
    }
}
