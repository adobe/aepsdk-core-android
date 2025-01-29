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
import androidx.core.os.UserManagerCompat
import com.adobe.marketing.mobile.internal.eventhub.EventHub
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.internal.context.App
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
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
        // Android supports direct boot mode on API level 24 and above
        val app = RuntimeEnvironment.application as Application
        val mockedEventHub = Mockito.mock(EventHub::class.java)
        Mockito.mockStatic(UserManagerCompat::class.java).use { mockedStaticUserManagerCompat ->
            mockedStaticUserManagerCompat.`when`<Any> { UserManagerCompat.isUserUnlocked(Mockito.any()) }
                .thenReturn(false)
            MobileCore.setApplication(app)
            mockedStaticUserManagerCompat.verify(
                { UserManagerCompat.isUserUnlocked(Mockito.any()) },
                never()
            )
        }
        assertEquals(app, ServiceProvider.getInstance().appContextService.application)
    }

    @Test
    @Config(sdk = [24])
    fun `test setApplication when the app is not configured to run in direct boot mode`() {
        val app = RuntimeEnvironment.application as Application
        val mockedEventHub = Mockito.mock(EventHub::class.java)
        EventHub.shared = mockedEventHub
        Mockito.mockStatic(UserManagerCompat::class.java).use { mockedStaticUserManagerCompat ->
            // when initializing SDK, the app is not in direct boot mode (device is unlocked)
            mockedStaticUserManagerCompat.`when`<Any> { UserManagerCompat.isUserUnlocked(Mockito.any()) }.thenReturn(true)
            MobileCore.setApplication(app)
            mockedStaticUserManagerCompat.verify({ UserManagerCompat.isUserUnlocked(Mockito.any()) }, times(1))
        }
        assertEquals(app, ServiceProvider.getInstance().appContextService.application)
    }

    @Test
    @Config(sdk = [24])
    fun `test setApplication when the app is launched in direct boot mode`() {
        val app = RuntimeEnvironment.application as Application
        val mockedEventHub = Mockito.mock(EventHub::class.java)
        Mockito.mockStatic(UserManagerCompat::class.java).use { mockedStaticUserManagerCompat ->
            // when initializing SDK, the app is in direct boot mode (device is still locked)
            mockedStaticUserManagerCompat.`when`<Any> { UserManagerCompat.isUserUnlocked(Mockito.any()) }.thenReturn(false)
            MobileCore.setApplication(app)
            mockedStaticUserManagerCompat.verify({ UserManagerCompat.isUserUnlocked(Mockito.any()) }, times(1))
        }
        assertNull(ServiceProvider.getInstance().appContextService.application)
    }
}
