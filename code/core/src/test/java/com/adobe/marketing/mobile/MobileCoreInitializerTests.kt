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

package com.adobe.marketing.mobile

import android.app.Activity
import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.adobe.marketing.mobile.internal.configuration.ConfigurationExtension
import com.adobe.marketing.mobile.internal.eventhub.EventHub
import com.adobe.marketing.mobile.services.AppContextService
import com.adobe.marketing.mobile.services.DataStoring
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.internal.context.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class MobileCoreInitializerTests {

    private class TestExtension1(api: ExtensionApi) : Extension(api) {
        override fun getName() = "TestExtension1"
    }

    private class TestExtension2(api: ExtensionApi) : Extension(api) {
        override fun getName() = "TestExtension2"
    }

    @Mock
    private lateinit var mockedEventHub: EventHub

    @Mock
    private lateinit var application: Application

    @Mock
    private lateinit var lifecycle: Lifecycle

    @Mock
    private lateinit var extensionDiscovery: ExtensionDiscovery

    private lateinit var mobileCoreInitializer: MobileCoreInitializer

    var lastCapturedLifecycleObserver: DefaultLifecycleObserver? = null
        get() {
            var lifecycleCallback = argumentCaptor<LifecycleObserver>()
            verify(lifecycle).addObserver(lifecycleCallback.capture())
            return lifecycleCallback.lastValue as? DefaultLifecycleObserver
        }

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        App.reset()
        EventHub.shared = mockedEventHub

        val lifecycleOwner = mock(LifecycleOwner::class.java)
        `when`(lifecycleOwner.lifecycle).thenReturn(lifecycle)

        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)

        mobileCoreInitializer = MobileCoreInitializer(
            scope = testScope,
            mainDispatcher = testDispatcher,
            isUserUnlocked = { true },
            lifecycleOwner = lifecycleOwner,
            extensionDiscovery = extensionDiscovery
        )
    }

    @After
    fun teardown() {
        reset(mockedEventHub)
    }

    @Test
    fun `test lifecycle tracker`() {
        val contextData = mapOf("key" to "value")
        val lifecycleTracker = LifecycleTracker(contextData)
        val lifecycleOwner = mock(LifecycleOwner::class.java)
        Mockito.mockStatic(MobileCore::class.java).use { mockedStatic ->
            lifecycleTracker.onResume(lifecycleOwner)
            mockedStatic.verify({ MobileCore.lifecycleStart(contextData) }, Mockito.times(1))

            lifecycleTracker.onPause(lifecycleOwner)
            mockedStatic.verify({ MobileCore.lifecyclePause() }, Mockito.times(1))
        }
    }

    @Test
    fun `test launch info collector`() {
        val launchInfoCollector = LaunchInfoCollector()
        val activity = mock(Activity::class.java)

        Mockito.mockStatic(MobileCore::class.java).use { mockedStatic ->
            launchInfoCollector.onActivityResumed(activity)
            mockedStatic.verify({ MobileCore.collectLaunchInfo(activity) }, Mockito.times(1))
        }
    }

    @Test
    fun `test setApplication sets application context`() {
        mobileCoreInitializer.setApplication(application)
        assertEquals(application, ServiceProvider.getInstance().appContextService.application)
    }

    @Test
    fun `test setApplication is ignored the second time`() {
        mobileCoreInitializer.setApplication(application)
        assertEquals(application, ServiceProvider.getInstance().appContextService.application)

        val application1 = mock(Application::class.java)
        mobileCoreInitializer.setApplication(application1)
        assertEquals(application, ServiceProvider.getInstance().appContextService.application)
    }

    @Test
    fun `test registerExtensions is ignored without setApplication`() {
        val extensions: List<Class<out Extension>> = listOf(
            TestExtension1::class.java,
            TestExtension2::class.java
        )
        mobileCoreInitializer.registerExtensions(extensions) {}

        verify(mockedEventHub, never()).registerExtensions(any(), any())
    }

    @Test
    fun `test registerExtensions`() {
        mobileCoreInitializer.setApplication(application)

        val extensions: List<Class<out Extension>> = listOf(
            TestExtension1::class.java,
            TestExtension2::class.java
        )
        mobileCoreInitializer.registerExtensions(extensions) {}

        val expected =
            mutableSetOf<Class<out Extension>>(ConfigurationExtension::class.java).apply {
                addAll(extensions)
            }

        verify(mockedEventHub, times(1)).registerExtensions(eq(expected), any())
    }

    @Test
    fun `test registerExtensions multiple times`() {
        mobileCoreInitializer.setApplication(application)

        mobileCoreInitializer.registerExtensions(listOf<Class<out Extension>>(TestExtension1::class.java)) {}

        val expected1 = mutableSetOf(ConfigurationExtension::class.java, TestExtension1::class.java)
        verify(mockedEventHub, times(1)).registerExtensions(eq(expected1), any())
        clearInvocations(mockedEventHub)

        mobileCoreInitializer.registerExtensions(listOf<Class<out Extension>>(TestExtension2::class.java)) {}

        val expected2 = mutableSetOf(ConfigurationExtension::class.java, TestExtension2::class.java)
        verify(mockedEventHub, times(1)).registerExtensions(eq(expected2), any())
    }

    @Test
    fun `test initialize enables automatic lifecycle tracking`() {
        val options = InitOptions.configureWithAppID("appID")
        mobileCoreInitializer.initialize(application, options, null)
        assertNotNull(lastCapturedLifecycleObserver)

        Mockito.mockStatic(MobileCore::class.java).use { mockedStatic ->
            lastCapturedLifecycleObserver?.onResume(mock(LifecycleOwner::class.java))
            mockedStatic.verify({ MobileCore.lifecycleStart(null) }, Mockito.times(1))

            lastCapturedLifecycleObserver?.onPause(mock(LifecycleOwner::class.java))
            mockedStatic.verify({ MobileCore.lifecyclePause() }, Mockito.times(1))
        }
    }

    @Test
    fun `test initialize enables automatic lifecycle tracking with context data`() {
        val options = InitOptions.configureWithAppID("appID")
        val contextData = mapOf("key" to "value")
        options.lifecycleAdditionalContextData = contextData
        mobileCoreInitializer.initialize(application, options, null)

        Mockito.mockStatic(MobileCore::class.java).use { mockedStatic ->
            assertNotNull(lastCapturedLifecycleObserver)
            lastCapturedLifecycleObserver?.onResume(mock(LifecycleOwner::class.java))
            mockedStatic.verify({ MobileCore.lifecycleStart(eq(contextData)) }, Mockito.times(1))

            lastCapturedLifecycleObserver?.onPause(mock(LifecycleOwner::class.java))
            mockedStatic.verify({ MobileCore.lifecyclePause() }, Mockito.times(1))
        }
    }

    @Test
    fun `test init options copies context data`() {
        val contextData = mutableMapOf("key" to "value")

        val options = InitOptions.configureWithAppID("appID")
        options.lifecycleAdditionalContextData = contextData

        // Mutate the context data after creating init options.
        contextData["key1"] = "value1"

        mobileCoreInitializer.initialize(application, options, null)

        Mockito.mockStatic(MobileCore::class.java).use { mockedStatic ->
            assertNotNull(lastCapturedLifecycleObserver)

            val expectedContextData = mutableMapOf("key" to "value")
            lastCapturedLifecycleObserver?.onResume(mock(LifecycleOwner::class.java))
            mockedStatic.verify(
                { MobileCore.lifecycleStart(eq(expectedContextData)) },
                Mockito.times(1)
            )

            lastCapturedLifecycleObserver?.onPause(mock(LifecycleOwner::class.java))
            mockedStatic.verify({ MobileCore.lifecyclePause() }, Mockito.times(1))
        }
    }

    @Test
    fun `test initialize disables automatic lifecycle tracking`() {
        val options = InitOptions.configureWithAppID("appID").apply {
            lifecycleAutomaticTrackingEnabled = false
        }
        mobileCoreInitializer.initialize(application, options, null)

        verify(lifecycle, never()).addObserver(any())
    }

    @Test
    fun `test initialize registers extensions automatically`() {
        val extensions = listOf(TestExtension1::class.java, TestExtension2::class.java)
        `when`(extensionDiscovery.getExtensions(any())).thenReturn(extensions)

        val options = InitOptions.configureWithAppID("appId")
        mobileCoreInitializer.initialize(application, options, null)

        val expected =
            mutableSetOf<Class<out Extension>>(ConfigurationExtension::class.java).apply {
                addAll(extensions)
            }
        verify(mockedEventHub, times(1)).registerExtensions(eq(expected), any())
    }

    @Test
    fun `test initialize calls config with appId`() {
        Mockito.mockStatic(MobileCore::class.java).use { mockedStatic ->
            val options = InitOptions.configureWithAppID("appId")
            mobileCoreInitializer.initialize(application, options, null)

            mockedStatic.verify({ MobileCore.configureWithAppID(eq("appId")) }, Mockito.times(1))
        }
    }

    @Test
    fun `test initialize configures with file path`() {
        Mockito.mockStatic(MobileCore::class.java).use { mockedStatic ->
            val options = InitOptions.configureWithFileInPath("filePath")
            mobileCoreInitializer.initialize(application, options, null)

            mockedStatic.verify(
                { MobileCore.configureWithFileInPath(eq("filePath")) },
                Mockito.times(1)
            )
        }
    }

    @Test
    fun `test initialize configures with file in assets`() {
        Mockito.mockStatic(MobileCore::class.java).use { mockedStatic ->
            val options = InitOptions.configureWithFileInAssets("fileInAssets")
            mobileCoreInitializer.initialize(application, options, null)

            mockedStatic.verify(
                { MobileCore.configureWithFileInAssets(eq("fileInAssets")) },
                Mockito.times(1)
            )
        }
    }

    @Test
    fun `test initialize ignored 2nd time`() {
        val options = InitOptions.configureWithAppID("appID")
        mobileCoreInitializer.initialize(application, options, null)
        Mockito.mockStatic(MobileCore::class.java).use { mockedStatic ->
            mobileCoreInitializer.initialize(application, options, null)

            mockedStatic.verifyNoInteractions()
        }
    }

    @Test
    fun `test setApplication ignored after initialize`() {
        val options = InitOptions.configureWithAppID("appID")
        mobileCoreInitializer.initialize(application, options, null)
        assertEquals(application, ServiceProvider.getInstance().appContextService.application)

        val application1 = mock(Application::class.java)
        mobileCoreInitializer.setApplication(application1)
        assertEquals(application, ServiceProvider.getInstance().appContextService.application)
    }

    @Test
    fun `test extension registration happens after event hub initialization and migration completes`() {
        mobileCoreInitializer = MobileCoreInitializer(
            scope = CoroutineScope(Dispatchers.IO),
            mainDispatcher = UnconfinedTestDispatcher(),
            isUserUnlocked = { true },
            lifecycleOwner = mock(LifecycleOwner::class.java),
            extensionDiscovery = extensionDiscovery
        )

        var latch = CountDownLatch(1)
        var orderOfCalls = mutableListOf<String>()

        `when`(mockedEventHub.initializeEventHistory()).doAnswer {
            Thread.sleep(500)
            orderOfCalls.add("initializeEventHistory")
            Unit
        }

        `when`(mockedEventHub.registerExtensions(any(), any())).doAnswer {
            orderOfCalls.add("registerExtensions")
            latch.countDown()
            Unit
        }

        mobileCoreInitializer.setApplication(application)
        mobileCoreInitializer.registerExtensions(listOf<Class<out Extension>>(TestExtension1::class.java)) {}
        assertEquals(listOf(), orderOfCalls)

        assertTrue { latch.await(1000, TimeUnit.MILLISECONDS) }
        assertEquals(listOf("initializeEventHistory", "registerExtensions"), orderOfCalls)
    }
    @Test
    fun `test V5LegacyCleaner handles exception from dataStoreService`() {
        // Setup
        // 1. Create mocks
        val mockServiceProvider = mock(ServiceProvider::class.java)
        val mockDataStoreService = mock(DataStoring::class.java)
        val mockNamedCollection = mock(NamedCollection::class.java)
        val mockAppContextService = mock(AppContextService::class.java)

        // 2. Mock static ServiceProvider.getInstance()
        val mockedStaticServiceProvider = Mockito.mockStatic(ServiceProvider::class.java)
        mockedStaticServiceProvider.`when`<Any> { ServiceProvider.getInstance() }.thenReturn(mockServiceProvider)

        // 3. Setup the dataStoreService to return our mock
        `when`(mockServiceProvider.dataStoreService).thenReturn(mockDataStoreService)
        `when`(mockServiceProvider.appContextService).thenReturn(mockAppContextService)

        // 4. Make getNamedCollection return our mock named collection
        `when`(mockDataStoreService.getNamedCollection("ADBMobileServices")).thenReturn(mockNamedCollection)

        // 5. Make removeAll() throw an exception
        `when`(mockNamedCollection.removeAll()).thenThrow(RuntimeException("Test exception"))

        // Test - invoke the cleanup method

        try {
            mobileCoreInitializer.setApplication(application)
            // If we reach here, the exception was handled properly
        } catch (e: Exception) {
            fail("V5LegacyCleaner.cleanup() throws exceptions, it should be caught.")
        } finally {
            // Clean up static mock
            mockedStaticServiceProvider.close()
        }
    }
}
