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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for the {@link Networking#isNetworkAvailable()} and {@link
 * Networking#networkConnectionInfo()} default (protocol-extension equivalent) behavior and the
 * custom-override mechanism available to callers. These are pure-JVM tests exercising the interface
 * contract itself; the device-backed production behavior is covered in {@code NetworkServiceTests}.
 */
public class NetworkingIsAvailableTests {

    /**
     * A minimal conformer that does NOT implement the new methods. It receives the interface's
     * default implementations for free - source-compatible with all pre-existing {@link Networking}
     * conformers across AEP extension repos.
     */
    private static class MinimalNetworkingConformer implements Networking {
        @Override
        public void connectAsync(final NetworkRequest request, final NetworkCallback callback) {}
    }

    /** A conformer implementing custom availability and connection-info logic. */
    private static class CustomNetworking implements Networking {
        boolean overrideValue;
        int connectAsyncCallCount = 0;

        CustomNetworking(final boolean value) {
            this.overrideValue = value;
        }

        @Override
        public void connectAsync(final NetworkRequest request, final NetworkCallback callback) {
            connectAsyncCallCount++;
        }

        @Override
        public boolean isNetworkAvailable() {
            return overrideValue;
        }
    }

    // MARK: - Default implementation

    @Test
    public void testDefaultImpl_conservativeInitialState_returnsFalse() {
        // Without access to the device connectivity layer, the interface default is conservative:
        // report unavailable rather than assume connectivity.
        assertFalse(new MinimalNetworkingConformer().isNetworkAvailable());
    }

    @Test
    public void testDefaultImpl_connectionInfo_returnsUnavailableWithUnknownType() {
        NetworkConnectionInfo info = new MinimalNetworkingConformer().networkConnectionInfo();
        assertFalse(info.isAvailable());
        assertEquals(NetworkConnectionInfo.InterfaceType.UNKNOWN, info.getInterfaceType());
        assertFalse(info.isConstrained());
        assertFalse(info.isExpensive());
    }

    @Test
    public void testDefaultImpl_connectionInfo_isAvailable_consistentWithIsNetworkAvailable() {
        MinimalNetworkingConformer conformer = new MinimalNetworkingConformer();
        assertEquals(
                conformer.isNetworkAvailable(), conformer.networkConnectionInfo().isAvailable());
    }

    @Test
    public void testDefaultImpl_repeatedReads_returnConsistentResult() {
        MinimalNetworkingConformer conformer = new MinimalNetworkingConformer();
        for (int i = 0; i < 1000; i++) {
            assertFalse(
                    "Call " + i + " returned an inconsistent result.",
                    conformer.isNetworkAvailable());
        }
    }

    // MARK: - Custom override

    @Test
    public void testCustomOverride_satisfiedPath_returnsTrue() {
        assertTrue(new CustomNetworking(true).isNetworkAvailable());
    }

    @Test
    public void testCustomOverride_unsatisfiedPath_returnsFalse() {
        assertFalse(new CustomNetworking(false).isNetworkAvailable());
    }

    // MARK: - Transport behavior is unchanged

    @Test
    public void testConnectAsync_isCallableIndependentlyOfIsNetworkAvailable() {
        // isNetworkAvailable() is a passive query only. It must NOT intercept, block, or alter how
        // connectAsync is dispatched.
        CustomNetworking custom = new CustomNetworking(false);
        NetworkRequest request =
                new NetworkRequest("https://example.com", HttpMethod.GET, null, null, 10, 10);
        custom.connectAsync(request, null);
        assertEquals(1, custom.connectAsyncCallCount);
    }

    // MARK: - NetworkConnectionInfo data class

    @Test
    public void testNetworkConnectionInfo_retainsAllProvidedValues() {
        NetworkConnectionInfo info =
                new NetworkConnectionInfo(
                        true, NetworkConnectionInfo.InterfaceType.CELLULAR, true, true);
        assertTrue(info.isAvailable());
        assertEquals(NetworkConnectionInfo.InterfaceType.CELLULAR, info.getInterfaceType());
        assertTrue(info.isConstrained());
        assertTrue(info.isExpensive());
    }
}
