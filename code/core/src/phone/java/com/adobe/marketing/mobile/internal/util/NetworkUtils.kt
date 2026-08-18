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

@file:JvmName("NetworkUtils")
package com.adobe.marketing.mobile.internal.util

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.adobe.marketing.mobile.services.NetworkConnectionInfo

/**
 * Checks if the network is configured to reach the general Internet.
 *
 * @param connectivityManager the [ConnectivityManager] to use to check the network status
 * @return `true` if the network is configured to reach the general Internet, `false` otherwise
 */
@JvmName("isInternetAvailable")
internal fun isInternetAvailable(connectivityManager: ConnectivityManager): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // The getActiveNetwork() API was introduced in API version 23.
        val network = connectivityManager.activeNetwork ?: return false
        val activeCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        return networkInfo.isConnected
    }
}

/**
 * Reads a point-in-time snapshot of the device's network connection state from the given
 * [connectivityManager].
 *
 * Mirrors [isInternetAvailable] for the availability signal and additionally derives the dominant
 * interface type, the metered ("expensive") flag, and the data-restriction ("constrained") flag.
 *
 * @param connectivityManager the [ConnectivityManager] to read the network state from
 * @return a [NetworkConnectionInfo] describing the current connection; an unavailable snapshot with
 *     [NetworkConnectionInfo.InterfaceType.UNKNOWN] when no usable path exists
 */
@JvmName("getNetworkConnectionInfo")
internal fun getNetworkConnectionInfo(connectivityManager: ConnectivityManager): NetworkConnectionInfo {
    val unavailable = NetworkConnectionInfo(
        false,
        NetworkConnectionInfo.InterfaceType.UNKNOWN,
        false,
        false
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // The getActiveNetwork() / NetworkCapabilities path was introduced in API version 23.
        val network = connectivityManager.activeNetwork ?: return unavailable
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return unavailable
        val available = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (!available) {
            return unavailable
        }

        val interfaceType = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                NetworkConnectionInfo.InterfaceType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                NetworkConnectionInfo.InterfaceType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
                NetworkConnectionInfo.InterfaceType.WIRED_ETHERNET
            else -> NetworkConnectionInfo.InterfaceType.OTHER
        }

        // A metered link is the Android analog of iOS `isExpensive` - the network is billed by
        // usage (cellular, hotspot, etc.). NET_CAPABILITY_NOT_METERED is present on unmetered paths.
        val isExpensive = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

        return NetworkConnectionInfo(true, interfaceType, isDataSaverEnabled(connectivityManager), isExpensive)
    } else {
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo ?: return unavailable
        @Suppress("DEPRECATION")
        if (!networkInfo.isConnected) {
            return unavailable
        }

        @Suppress("DEPRECATION")
        val interfaceType = when (networkInfo.type) {
            ConnectivityManager.TYPE_WIFI -> NetworkConnectionInfo.InterfaceType.WIFI
            ConnectivityManager.TYPE_MOBILE -> NetworkConnectionInfo.InterfaceType.CELLULAR
            ConnectivityManager.TYPE_ETHERNET -> NetworkConnectionInfo.InterfaceType.WIRED_ETHERNET
            else -> NetworkConnectionInfo.InterfaceType.OTHER
        }

        // Pre-M has no per-path metered capability; treat cellular as metered as a best effort.
        val isExpensive = interfaceType == NetworkConnectionInfo.InterfaceType.CELLULAR
        return NetworkConnectionInfo(true, interfaceType, isDataSaverEnabled(connectivityManager), isExpensive)
    }
}

/**
 * Returns whether the user has enabled Data Saver and it is actively restricting background data -
 * the Android analog of iOS Low Data Mode ("constrained"). Requires API version 24; returns `false`
 * on older devices where the API is unavailable.
 */
private fun isDataSaverEnabled(connectivityManager: ConnectivityManager): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // getRestrictBackgroundStatus() was introduced in API version 24.
        return connectivityManager.restrictBackgroundStatus ==
            ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
    }
    return false
}
