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

package com.adobe.marketing.mobile.services.uri

import android.app.Activity
import android.content.Intent
import com.adobe.marketing.mobile.services.AppContextService
import com.adobe.marketing.mobile.services.ServiceProvider
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import kotlin.test.assertNotNull

class UriServiceTest {

    private lateinit var uriService: UriService

    @Mock
    private lateinit var mockCurrentActivity: Activity

    @Mock
    private lateinit var mockServiceProvider: ServiceProvider

    @Mock
    private lateinit var mockAppContextService: AppContextService

    @Mock
    private lateinit var mockUriHandler: UriHandler

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        `when`(mockServiceProvider.appContextService).thenReturn(mockAppContextService)
        uriService = UriService(mockServiceProvider)
    }

    @Test
    fun `Test that #openURI returns false when uri is empty`() {
        // setup
        val uri = ""

        // test
        val result = uriService.openUri(uri)

        // verify
        assertFalse(result)
    }

    @Test
    fun `Test that #openURI returns false when current activity is null`() {
        // setup
        val uri = "https://www.adobe.com"
        `when`(mockAppContextService.currentActivity).thenReturn(null)

        // test
        val result = uriService.openUri(uri)

        // verify
        assertFalse(result)
    }

    @Test
    fun `Test that #openURI returns false when uri is blank`() {
        // setup
        val uri = " "

        // test
        val result = uriService.openUri(uri)

        // verify
        assertFalse(result)
    }

    @Test
    fun `Test that #openURI returns true when uri is valid`() {
        // setup
        val uri = "https://www.adobe.com"
        `when`(mockAppContextService.currentActivity).thenReturn(mockCurrentActivity)

        // test
        val result = uriService.openUri(uri)

        // verify
        assertTrue(result)
        val intentArgumentCaptor = argumentCaptor<Intent>()
        verify(mockCurrentActivity).startActivity(intentArgumentCaptor.capture())
        assertNotNull(intentArgumentCaptor.firstValue)
    }

    @Test
    fun `Test that #openURI honors intent configured by URIHandler`() {
        // setup
        val uri = "https://www.adobe.com"

        // setup mock UriHandler to return an intent for the uri
        val intent = Intent(Intent.ACTION_VIEW).apply { setPackage("some.random.package.name") }
        `when`(mockAppContextService.currentActivity).thenReturn(mockCurrentActivity)
        `when`(mockUriHandler.getUriDestination(uri)).thenReturn(intent)
        uriService.setUriHandler(mockUriHandler)

        // test
        val result = uriService.openUri(uri)

        // verify
        assertTrue(result)
        verify(mockCurrentActivity).startActivity(intent)
    }

    @Test
    fun `Test that openURI returns falls back to default handling when no URIHandler is set`() {
        // setup
        val uri = "https://www.adobe.com"
        `when`(mockAppContextService.currentActivity).thenReturn(mockCurrentActivity)
        // setup mock UriHandler to return null intent for the uri
        `when`(mockUriHandler.getUriDestination(uri)).thenReturn(null)

        // test
        val result = uriService.openUri(uri)

        // verify
        assertTrue(result)
        verify(mockCurrentActivity).startActivity(any<Intent>())
    }
}
