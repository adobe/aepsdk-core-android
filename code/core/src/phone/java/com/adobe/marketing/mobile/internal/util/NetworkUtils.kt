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
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Checks if the network is configured to reach the general Internet.
 *
 * @param connectivityManager the [ConnectivityManager] to use to check the network status
 * @return `true` if the network is configured to reach the general Internet, `false` otherwise
 */
fun isInternetAvailable(connectivityManager: ConnectivityManager): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // The getActiveNetwork() API was introduced in API version 23.
        val network = connectivityManager.activeNetwork
        val activeCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        return networkInfo.isConnected
    }
}
