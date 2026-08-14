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
import com.adobe.marketing.mobile.services.NetworkConnectionInfo
import org.junit.Assert.assertEquals
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

    @Test
    fun connectionInfo_whenActiveNetworkIsNull_returnsUnavailableUnknown() {
        `when`(mockedConnectivityManager.activeNetwork).thenReturn(null)

        val info = getNetworkConnectionInfo(mockedConnectivityManager)
        assertFalse(info.isAvailable)
        assertEquals(NetworkConnectionInfo.InterfaceType.UNKNOWN, info.interfaceType)
        assertFalse(info.isConstrained)
        assertFalse(info.isExpensive)
    }

    @Test
    fun connectionInfo_whenNoInternetCapability_returnsUnavailableUnknown() {
        stubCapabilities()
        `when`(mockedNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(false)

        val info = getNetworkConnectionInfo(mockedConnectivityManager)
        assertFalse(info.isAvailable)
        assertEquals(NetworkConnectionInfo.InterfaceType.UNKNOWN, info.interfaceType)
    }

    @Test
    fun connectionInfo_whenWifiUnmetered_returnsWifiAvailableNotExpensive() {
        stubCapabilities()
        `when`(mockedNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true)
        `when`(mockedNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(true)
        `when`(mockedNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)).thenReturn(true)

        val info = getNetworkConnectionInfo(mockedConnectivityManager)
        assertTrue(info.isAvailable)
        assertEquals(NetworkConnectionInfo.InterfaceType.WIFI, info.interfaceType)
        assertFalse(info.isExpensive)
    }

    @Test
    fun connectionInfo_whenCellularMetered_returnsCellularAvailableExpensive() {
        stubCapabilities()
        `when`(mockedNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true)
        `when`(mockedNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(true)
        `when`(mockedNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)).thenReturn(false)

        val info = getNetworkConnectionInfo(mockedConnectivityManager)
        assertTrue(info.isAvailable)
        assertEquals(NetworkConnectionInfo.InterfaceType.CELLULAR, info.interfaceType)
        assertTrue(info.isExpensive)
    }

    @Test
    fun connectionInfo_whenEthernet_returnsWiredEthernet() {
        stubCapabilities()
        `when`(mockedNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true)
        `when`(mockedNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)).thenReturn(true)

        val info = getNetworkConnectionInfo(mockedConnectivityManager)
        assertTrue(info.isAvailable)
        assertEquals(NetworkConnectionInfo.InterfaceType.WIRED_ETHERNET, info.interfaceType)
    }

    @Test
    fun connectionInfo_whenNoKnownTransport_returnsOther() {
        stubCapabilities()
        `when`(mockedNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true)

        val info = getNetworkConnectionInfo(mockedConnectivityManager)
        assertTrue(info.isAvailable)
        assertEquals(NetworkConnectionInfo.InterfaceType.OTHER, info.interfaceType)
    }

    private fun stubCapabilities() {
        `when`(mockedConnectivityManager.activeNetwork).thenReturn(mock(Network::class.java))
        `when`(mockedConnectivityManager.getNetworkCapabilities(mockedConnectivityManager.activeNetwork)).thenReturn(mockedNetworkCapabilities)
    }
}
