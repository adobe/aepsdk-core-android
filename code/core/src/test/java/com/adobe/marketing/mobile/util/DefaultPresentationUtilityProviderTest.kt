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

package com.adobe.marketing.mobile.util

import android.app.Activity
import android.app.Application
import com.adobe.marketing.mobile.services.AppContextService
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheResult
import com.adobe.marketing.mobile.services.caching.CacheService
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.uri.UriOpening
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

class DefaultPresentationUtilityProviderTest {

    @Mock private lateinit var mockServiceProvider: ServiceProvider

    @Mock private lateinit var mockApplication: Application

    @Mock private lateinit var mockAppContextService: AppContextService

    @Mock private lateinit var mockActivity: Activity

    @Mock private lateinit var mockCacheService: CacheService

    @Mock private lateinit var mockUriOpening: UriOpening

    private lateinit var mockedStaticServiceProvider: MockedStatic<ServiceProvider>
    private lateinit var defaultPresentationUtilityProvider: PresentationUtilityProvider

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockedStaticServiceProvider = Mockito.mockStatic(ServiceProvider::class.java)
        mockedStaticServiceProvider.`when`<Any> { ServiceProvider.getInstance() }
            .thenReturn(mockServiceProvider)
        `when`(mockServiceProvider.appContextService).thenReturn(mockAppContextService)
        `when`(mockServiceProvider.cacheService).thenReturn(mockCacheService)
        `when`(mockServiceProvider.uriService).thenReturn(mockUriOpening)

        defaultPresentationUtilityProvider = DefaultPresentationUtilityProvider()
    }

    @Test
    fun `Test #getApplication proxies AppContextService`() {
        `when`(mockAppContextService.application).thenReturn(mockApplication)
        // test
        val application = defaultPresentationUtilityProvider.getApplication()

        // verify
        verify(mockAppContextService).application
        assertEquals(mockApplication, application)
    }

    @Test
    fun `Test #getCurrentActivity proxies AppContextService`() {
        `when`(mockAppContextService.currentActivity).thenReturn(mockActivity)
        // test
        val currentActivity = defaultPresentationUtilityProvider.getCurrentActivity()

        // verify
        verify(mockAppContextService).currentActivity
        assertEquals(mockActivity, currentActivity)
    }

    @Test
    fun `Test #getCachedContent proxies CacheService`() {
        val testCacheName = "testCacheName"
        val testCacheKey = "testCacheKey"
        val mockCacheResult = Mockito.mock(CacheResult::class.java)
        `when`(mockCacheResult.data).thenReturn("value".byteInputStream())
        `when`(mockCacheService.get(testCacheName, testCacheKey)).thenReturn(mockCacheResult)
        // test
        val cachedContent =
            defaultPresentationUtilityProvider.getCachedContent(testCacheName, testCacheKey)

        // verify
        verify(mockCacheService).get(testCacheName, testCacheKey)
        assertEquals(mockCacheResult.data, cachedContent)
    }

    @Test
    fun `Test #openUri proxies UriOpening`() {
        val testUri = "http://www.adobe.com"
        // test
        defaultPresentationUtilityProvider.openUri(testUri)

        // verify
        verify(mockUriOpening).openUri(testUri)
    }

    @After
    fun tearDown() {
        mockedStaticServiceProvider.close()
    }
}
