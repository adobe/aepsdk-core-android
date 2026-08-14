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

import androidx.annotation.NonNull;

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
     * Returns whether network-bound SDK work should proceed right now. The production
     * implementation checks device-level connectivity only (via {@code ConnectivityManager}).
     * Override this in a custom {@link Networking} conformer (see: overriding {@code
     * NetworkService} via {@code ServiceProvider.getInstance().setNetworkService(...)}) to
     * implement custom logic - for example, pinging your own backend's health endpoint instead of
     * relying on device connectivity alone.
     *
     * <p>This default implementation returns {@code false} - a conservative fallback for conformers
     * that do not have access to the device connectivity layer. The default {@code NetworkService}
     * overrides it with the real device-backed check.
     *
     * @return {@code true} when a usable network path exists
     */
    default boolean isNetworkAvailable() {
        return false;
    }

    /**
     * Returns a point-in-time snapshot of the device's network connection state.
     *
     * <p>The production implementation reads from the device {@code ConnectivityManager}
     * synchronously - no HTTP request is made and the call returns immediately. Override this in a
     * custom {@link Networking} conformer to supply your own values (for example, from a
     * proprietary reachability library).
     *
     * <p>Use {@link #isNetworkAvailable()} for a simple yes/no guard. Use {@code
     * networkConnectionInfo()} when you need richer signal - interface type, data-restriction mode,
     * or metered-link detection - to make payload-sizing or deferral decisions.
     *
     * <p>This default implementation returns an unavailable snapshot with {@link
     * NetworkConnectionInfo.InterfaceType#UNKNOWN} - a conservative fallback for conformers that do
     * not have access to the device connectivity layer. The default {@code NetworkService}
     * overrides it with the real device-backed snapshot.
     *
     * @return the current {@link NetworkConnectionInfo}; never {@code null}
     */
    @NonNull default NetworkConnectionInfo networkConnectionInfo() {
        return new NetworkConnectionInfo(
                false, NetworkConnectionInfo.InterfaceType.UNKNOWN, false, false);
    }
}
