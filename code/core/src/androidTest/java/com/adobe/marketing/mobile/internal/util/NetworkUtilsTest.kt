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

package com.adobe.marketing.mobile.internal.util

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class NetworkUtilsTest {

    @Mock
    private lateinit var mockedConnectivityManager: ConnectivityManager

    @Mock
    private lateinit var mockedNetworkCapabilities: NetworkCapabilities

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }
    @Test
    fun internetAvailable_whenActiveNetworkHasInternetCapability_returnsTrue() {
        `when`(mockedConnectivityManager.activeNetwork).thenReturn(mock(Network::class.java))
        `when`(mockedConnectivityManager.getNetworkCapabilities(mockedConnectivityManager.activeNetwork)).thenReturn(mockedNetworkCapabilities)
        `when`(mockedNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true)

        assertTrue(isInternetAvailable(mockedConnectivityManager))
    }

    @Test
    fun internetNotAvailable_whenActiveNetworkHasNoInternetCapability_returnsFalse() {
        `when`(mockedConnectivityManager.activeNetwork).thenReturn(mock(Network::class.java))
        `when`(mockedConnectivityManager.getNetworkCapabilities(mockedConnectivityManager.activeNetwork)).thenReturn(mockedNetworkCapabilities)
        `when`(mockedNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(false)

        assertFalse(isInternetAvailable(mockedConnectivityManager))
    }

    @Test
    fun internetNotAvailable_whenActiveNetworkIsNull_returnsFalse() {
        `when`(mockedConnectivityManager.activeNetwork).thenReturn(null)
        Mockito.reset(mockedConnectivityManager)

        assertFalse(isInternetAvailable(mockedConnectivityManager))
    }
}
