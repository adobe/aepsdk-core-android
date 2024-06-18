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
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertFalse

@RunWith(RobolectricTestRunner::class)
class MobileCoreRobolectricTests {

    @Before
    fun setup() {
        MobileCore.sdkInitializedWithContext = AtomicBoolean(false)
    }

    @Test
    @Config(sdk = [23])
    fun testA() {
        val app = RuntimeEnvironment.application as Application
        val mockedEventHub = Mockito.mock(EventHub::class.java)
        Mockito.mockStatic(UserManagerCompat::class.java).use { mockedStaticUserManagerCompat ->
            mockedStaticUserManagerCompat.`when`<Any> { UserManagerCompat.isUserUnlocked(Mockito.any()) }
                .thenReturn(false)
            MobileCore.setApplication(app)
            mockedStaticUserManagerCompat.verify({ UserManagerCompat.isUserUnlocked(Mockito.any()) }, never())
        }
        verify(mockedEventHub, never()).executeInEventHubExecutor(any())
        assertTrue(MobileCore.sdkInitializedWithContext.get())
    }

    @Test
    @Config(sdk = [24])
    fun testB() {
        val app = RuntimeEnvironment.application as Application
        val mockedEventHub = Mockito.mock(EventHub::class.java)
        EventHub.shared = mockedEventHub
        Mockito.mockStatic(UserManagerCompat::class.java).use { mockedStaticUserManagerCompat ->
            mockedStaticUserManagerCompat.`when`<Any> { UserManagerCompat.isUserUnlocked(Mockito.any()) }.thenReturn(true)
            MobileCore.setApplication(app)
            mockedStaticUserManagerCompat.verify({ UserManagerCompat.isUserUnlocked(Mockito.any()) }, times(1))
        }
        verify(mockedEventHub, times(1)).executeInEventHubExecutor(any())
        assertTrue(MobileCore.sdkInitializedWithContext.get())
    }

    @Test
    @Config(sdk = [24])
    fun testc() {
        val app = RuntimeEnvironment.application as Application
        val mockedEventHub = Mockito.mock(EventHub::class.java)
        Mockito.mockStatic(UserManagerCompat::class.java).use { mockedStaticUserManagerCompat ->
            mockedStaticUserManagerCompat.`when`<Any> { UserManagerCompat.isUserUnlocked(Mockito.any()) }.thenReturn(false)
            MobileCore.setApplication(app)
            mockedStaticUserManagerCompat.verify({ UserManagerCompat.isUserUnlocked(Mockito.any()) }, times(1))
        }
        verify(mockedEventHub, never()).executeInEventHubExecutor(any())
        assertFalse(MobileCore.sdkInitializedWithContext.get())
    }
}
