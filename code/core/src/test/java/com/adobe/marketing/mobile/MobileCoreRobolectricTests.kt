/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile

import android.app.Application
import android.content.Context
import android.os.UserManager
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.internal.context.App
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class MobileCoreRobolectricTests {

    @Before
    fun setup() {
        MobileCore.resetSDK()
        App.reset()
    }

    @Test
    @Config(sdk = [23])
    fun `test setApplication when the device doesn't support direct boot mode`() {
        var app = mock(Application::class.java)
        var userManager = mock(UserManager::class.java)
        `when`(app.getSystemService(Context.USER_SERVICE)).thenReturn(userManager)

        MobileCore.setApplication(app)
        assertEquals(app, ServiceProvider.getInstance().appContextService.application)
    }

    @Test
    @Config(sdk = [24])
    fun `test setApplication when the app is not configured to run in direct boot mode`() {
        var app = mock(Application::class.java)
        var userManager = mock(UserManager::class.java)
        `when`(userManager.isUserUnlocked).thenReturn(true)
        `when`(app.getSystemService(Context.USER_SERVICE)).thenReturn(userManager)

        MobileCore.setApplication(app)

        verify(userManager, times(1)).isUserUnlocked
        assertEquals(app, ServiceProvider.getInstance().appContextService.application)
    }

    @Test
    @Config(sdk = [24])
    fun `test setApplication when the app is launched in direct boot mode`() {
        var app = mock(Application::class.java)
        var userManager = mock(UserManager::class.java)
        `when`(userManager.isUserUnlocked).thenReturn(false)
        `when`(app.getSystemService(Context.USER_SERVICE)).thenReturn(userManager)

        MobileCore.setApplication(app)

        verify(userManager, times(1)).isUserUnlocked
        assertNull(ServiceProvider.getInstance().appContextService.application)
    }
}
