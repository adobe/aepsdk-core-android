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
package com.adobe.marketing.mobile

import android.app.Application
import com.adobe.marketing.mobile.extensions.Sample1
import com.adobe.marketing.mobile.extensions.Sample1Kt
import com.adobe.marketing.mobile.extensions.Sample2
import com.adobe.marketing.mobile.extensions.Sample2Extension
import com.adobe.marketing.mobile.extensions.Sample2Kt
import com.adobe.marketing.mobile.extensions.Sample2KtExtension
import com.adobe.marketing.mobile.internal.eventhub.EventHub
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MobileCoreRegistrationTests {

    @Before
    fun setup() {
        MobileCore.sdkInitializedWithContext = AtomicBoolean(false)
        EventHub.shared = EventHub()
    }

    @After
    fun cleanup() {
        EventHub.shared.shutdown()
    }

    @Test
    fun testScenario1_SameClass() {
        val latch = CountDownLatch(2)
        val capturedIds = mutableSetOf<String>()
        val callback = object : AdobeCallbackWithError<String> {
            override fun call(id: String) {
                capturedIds.add(id)
                latch.countDown()
            }
            override fun fail(error: AdobeError?) {}
        }

        MobileCore.setApplication(mock(Application::class.java))

        val extensions = listOf(
            Sample1::class.java,
            Sample1Kt::class.java
        )

        MobileCore.registerExtensions(extensions) {
            Sample1.getTrackingIdentifier(callback)
            Sample1Kt.getTrackingIdentifier(callback)
        }

        assertTrue { latch.await(1000, TimeUnit.MILLISECONDS) }
        assertEquals(setOf("Sample1_ID", "Sample1Kt_ID"), capturedIds)
    }

    @Test
    fun testScenario2_DifferentClasses() {
        val latch = CountDownLatch(2)
        val capturedIds = mutableSetOf<String>()
        val callback = object : AdobeCallbackWithError<String> {
            override fun call(id: String) {
                capturedIds.add(id)
                latch.countDown()
            }
            override fun fail(error: AdobeError?) {}
        }

        MobileCore.setApplication(mock(Application::class.java))

        val extensions = listOf(
            Sample2Extension::class.java,
            Sample2KtExtension::class.java
        )

        MobileCore.registerExtensions(extensions) {
            Sample2.getTrackingIdentifier(callback)
            Sample2Kt.getTrackingIdentifier(callback)
        }

        assertTrue { latch.await(1000, TimeUnit.MILLISECONDS) }
        assertEquals(setOf("Sample2_ID", "Sample2Kt_ID"), capturedIds)
    }
}
