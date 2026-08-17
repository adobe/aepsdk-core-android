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

package com.adobe.marketing.mobile.services;

import android.net.ConnectivityManager;
import androidx.annotation.NonNull;
import com.adobe.marketing.mobile.internal.util.NetworkUtils;

public interface Networking {
    /**
     * Initiates an asynchronous network connection
     *
     * @param request {@link NetworkRequest} used for connection
     * @param callback {@link NetworkCallback} that will receive the {@link HttpConnecting} instance
     *     after the connection has been made; if the current network is unavailable, callback will
     *     be invoked immediately with a null connection.
     */
    void connectAsync(final NetworkRequest request, final NetworkCallback callback);

    /**
     * Returns whether network-bound SDK work should proceed right now. This default implementation
     * checks device-level connectivity only (via {@code ConnectivityManager}) and is provided to
     * every {@link Networking} conformer, including custom ones, so existing conformers remain
     * source-compatible without implementing it. Override this to implement custom logic - for
     * example, pinging your own backend's health endpoint instead of relying on device connectivity
     * alone.
     *
     * @return {@code true} when a usable network path exists
     */
    default boolean isNetworkAvailable() {
        final ConnectivityManager connectivityManager =
                ServiceProvider.getInstance().getAppContextService().getConnectivityManager();
        if (connectivityManager == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    "Networking",
                    "ConnectivityManager instance is null. Unable to check network availability.");
            // Conservative fallback: report unavailable when connectivity cannot be determined.
            return false;
        }
        return NetworkUtils.isInternetAvailable(connectivityManager);
    }

    /**
     * Returns a point-in-time snapshot of the device's network connection state. This default
     * implementation reads from the device {@code ConnectivityManager} synchronously - no HTTP
     * request is made and the call returns immediately - and is provided to every {@link
     * Networking} conformer, including custom ones. Override this to supply your own values (for
     * example, from a proprietary reachability library).
     *
     * <p>Use {@link #isNetworkAvailable()} for a simple yes/no guard. Use {@code
     * networkConnectionInfo()} when you need richer signal - interface type, data-restriction mode,
     * or metered-link detection - to make payload-sizing or deferral decisions.
     *
     * @return the current {@link NetworkConnectionInfo}; never {@code null}
     */
    @NonNull default NetworkConnectionInfo networkConnectionInfo() {
        final ConnectivityManager connectivityManager =
                ServiceProvider.getInstance().getAppContextService().getConnectivityManager();
        if (connectivityManager == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    "Networking",
                    "ConnectivityManager instance is null. Unable to determine network connection"
                            + " info.");
            // Conservative fallback: report an unavailable snapshot with an unknown interface type.
            return new NetworkConnectionInfo(
                    false, NetworkConnectionInfo.InterfaceType.UNKNOWN, false, false);
        }
        return NetworkUtils.getNetworkConnectionInfo(connectivityManager);
    }
}
