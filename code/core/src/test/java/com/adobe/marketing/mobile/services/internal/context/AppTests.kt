/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services.internal.context

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.net.ConnectivityManager
import com.adobe.marketing.mobile.services.AppState
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner.Silent
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

@RunWith(Silent::class)
class AppTests {
    @Mock
    private lateinit var mockApplication: Application
    @Mock
    private lateinit var mockContext: Context
    @Mock
    private lateinit var mockActivity: Activity
    @Mock
    private lateinit var mockConnectivityManager: ConnectivityManager

    private lateinit var activityLifecycleCallback: KArgumentCaptor<Application.ActivityLifecycleCallbacks>
    private lateinit var componentCallback: KArgumentCaptor<ComponentCallbacks2>

    @Before
    fun beforeEach() {
        MockitoAnnotations.openMocks(this)
        activityLifecycleCallback = argumentCaptor<Application.ActivityLifecycleCallbacks>()
        componentCallback = argumentCaptor<ComponentCallbacks2>()

        `when`(mockApplication.applicationContext).thenReturn(mockContext)
        `when`(mockApplication.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager)

        App.setApplication(mockApplication)
        verify(mockApplication).registerActivityLifecycleCallbacks(activityLifecycleCallback.capture())
        verify(mockApplication).registerComponentCallbacks(componentCallback.capture())
    }

    @After
    fun afterEach() {
        App.reset()
    }

    @Test
    fun `should return application`() {
        assertEquals(mockApplication, App.application)
    }

    @Test
    fun `should return application context`() {
        assertEquals(mockContext, App.applicationContext)
    }

    @Test
    fun `should return valid connection manager`() {
        assertEquals(mockConnectivityManager, App.connectivityManager)
    }

    @Test
    fun `should return unknown app state on launch`() {
        assertEquals(AppState.UNKNOWN, App.appState)
    }

    @Test
    fun `should transition to foreground state when activity is resumed`() {
        activityLifecycleCallback.firstValue.onActivityResumed(mockActivity)
        assertEquals(AppState.FOREGROUND, App.appState)
    }

    @Test
    fun `should transition to background state on memory trim event`() {
        componentCallback.firstValue.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)
        assertEquals(AppState.BACKGROUND, App.appState)
    }

    @Test
    fun `should retain unknown state for unexpected trim memory value`() {
        componentCallback.firstValue.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN - 1)
        assertEquals(AppState.UNKNOWN, App.appState)
    }

    @Test
    fun `should correctly handle app state transitions`() {
        assertEquals(AppState.UNKNOWN, App.appState)

        activityLifecycleCallback.firstValue.onActivityResumed(mockActivity)
        assertEquals(AppState.FOREGROUND, App.appState)

        activityLifecycleCallback.firstValue.onActivityPaused(mockActivity)
        assertEquals(AppState.FOREGROUND, App.appState)

        componentCallback.firstValue.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)
        assertEquals(AppState.BACKGROUND, App.appState)

        activityLifecycleCallback.firstValue.onActivityResumed(mockActivity)
        assertEquals(AppState.FOREGROUND, App.appState)
    }

    @Test
    fun `should return null value for current activity before activity callbacks`() {
        assertNull(App.currentActivity)
    }

    @Test
    fun `should return current activity`() {
        assertNull(App.currentActivity)

        activityLifecycleCallback.firstValue.onActivityResumed(mockActivity)
        assertEquals(mockActivity, App.currentActivity)

        val secondActivity = mock(Activity::class.java)
        activityLifecycleCallback.firstValue.onActivityResumed(secondActivity)
        assertEquals(secondActivity, App.currentActivity)
    }

    @Test
    fun `should call activity callbacks on activity resumed`() {
        App.registerActivityLifecycleCallbacks(object : App.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                assertEquals(activity, mockActivity)
            }

            override fun onActivityPaused(activity: Activity) {
                fail()
            }
        })

        activityLifecycleCallback.firstValue.onActivityResumed(mockActivity)
    }

    @Test
    fun `should call activity callbacks on activity paused`() {
        App.registerActivityLifecycleCallbacks(object : App.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                fail()
            }

            override fun onActivityPaused(activity: Activity) {
                assertEquals(activity, mockActivity)
            }
        })

        activityLifecycleCallback.firstValue.onActivityPaused(mockActivity)
    }
}
