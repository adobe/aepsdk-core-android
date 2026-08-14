/*
  Copyright 2026 Adobe. All rights reserved.
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

/**
 * A point-in-time snapshot of the device's network connection state, returned by {@link
 * Networking#networkConnectionInfo()}.
 *
 * <p>All properties are read from the device {@code ConnectivityManager} synchronously - no HTTP
 * request is made and the call returns immediately.
 *
 * <p>Usage in an AEP extension:
 *
 * <pre>{@code
 * NetworkConnectionInfo info = ServiceProvider.getInstance().getNetworkService().networkConnectionInfo();
 * if (!info.isAvailable()) {
 *     return;
 * }
 *
 * if (info.isConstrained()) {
 *     sendMinimalPayload();   // Data Saver: respect the user's data preference
 * } else if (info.isExpensive()) {
 *     scheduleForLater();     // Cellular / hotspot: defer non-critical work
 * } else {
 *     sendFullPayload();      // Wi-Fi / wired: full payload
 * }
 * }</pre>
 */
public final class NetworkConnectionInfo {

    /** The dominant network interface type on the current path. */
    public enum InterfaceType {
        /** Wi-Fi (802.11). */
        WIFI,
        /** Cellular (LTE, 5G, etc.). */
        CELLULAR,
        /** Wired Ethernet. */
        WIRED_ETHERNET,
        /** Another interface type (loopback, VPN, etc.). */
        OTHER,
        /** Path is unavailable or the connectivity state could not be determined. */
        UNKNOWN,
    }

    private final boolean isAvailable;
    private final InterfaceType interfaceType;
    private final boolean isConstrained;
    private final boolean isExpensive;

    /**
     * Creates a snapshot of the device's network connection state.
     *
     * @param isAvailable {@code true} when the device has a usable network path
     * @param interfaceType the dominant {@link InterfaceType} active on the current path; never
     *     {@code null}. Pass {@link InterfaceType#UNKNOWN} when {@code isAvailable} is {@code
     *     false}
     * @param isConstrained {@code true} when the user has enabled a data-restriction mode (e.g.
     *     Android Data Saver)
     * @param isExpensive {@code true} when the path uses a metered link (cellular, hotspot, etc.)
     */
    public NetworkConnectionInfo(
            final boolean isAvailable,
            @NonNull final InterfaceType interfaceType,
            final boolean isConstrained,
            final boolean isExpensive) {
        this.isAvailable = isAvailable;
        this.interfaceType = interfaceType;
        this.isConstrained = isConstrained;
        this.isExpensive = isExpensive;
    }

    /**
     * Returns whether the device has a usable network path - equivalent to {@link
     * Networking#isNetworkAvailable()}.
     *
     * @return {@code true} when a usable network path exists
     */
    public boolean isAvailable() {
        return isAvailable;
    }

    /**
     * Returns the dominant interface type active on the current path.
     *
     * <p>When {@link #isAvailable()} is {@code false} this returns {@link InterfaceType#UNKNOWN}.
     *
     * @return the current {@link InterfaceType}; never {@code null}
     */
    @NonNull public InterfaceType getInterfaceType() {
        return interfaceType;
    }

    /**
     * Returns whether the user has enabled a data-restriction mode (e.g. Android Data Saver).
     *
     * <p>Respect this the same way you would honor background-data restrictions: skip prefetch,
     * send minimal payloads, defer analytics batches.
     *
     * @return {@code true} when a data-restriction mode is active
     */
    public boolean isConstrained() {
        return isConstrained;
    }

    /**
     * Returns whether the path uses a metered link - cellular, a personal hotspot, or similar.
     *
     * <p>Use this to decide whether to defer large uploads or analytics batches.
     *
     * @return {@code true} when the current path is metered
     */
    public boolean isExpensive() {
        return isExpensive;
    }
}
