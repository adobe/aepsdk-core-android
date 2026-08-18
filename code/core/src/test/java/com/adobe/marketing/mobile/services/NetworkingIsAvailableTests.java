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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import android.net.ConnectivityManager;
import com.adobe.marketing.mobile.internal.util.NetworkUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for the {@link Networking#isNetworkAvailable()} and {@link
 * Networking#networkConnectionInfo()} default implementations and the custom-override mechanism
 * available to callers. The defaults delegate to the real {@code ConnectivityManager}-backed check
 * (via {@link NetworkUtils}) for every conformer, including custom ones that don't override them -
 * these tests prove that, rather than assuming a hardcoded fallback value.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class NetworkingIsAvailableTests {

    @Mock AppContextService appContextService;

    @Mock ConnectivityManager connectivityManager;

    @Before
    public void setup() {
        ServiceProvider.getInstance().setAppContextService(appContextService);
        when(appContextService.getConnectivityManager()).thenReturn(null);
    }

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

    // MARK: - Default implementation reflects the real ConnectivityManager state

    @Test
    public void testDefaultImpl_reflectsConnectivityManagerAvailable_returnsTrue() {
        try (MockedStatic<NetworkUtils> ignored = Mockito.mockStatic(NetworkUtils.class)) {
            when(appContextService.getConnectivityManager()).thenReturn(connectivityManager);
            when(NetworkUtils.isInternetAvailable(connectivityManager)).thenReturn(true);

            assertTrue(
                    "Default impl must reflect the real ConnectivityManager state, not a hardcoded"
                            + " value.",
                    new MinimalNetworkingConformer().isNetworkAvailable());
        }
    }

    @Test
    public void testDefaultImpl_reflectsConnectivityManagerUnavailable_returnsFalse() {
        try (MockedStatic<NetworkUtils> ignored = Mockito.mockStatic(NetworkUtils.class)) {
            when(appContextService.getConnectivityManager()).thenReturn(connectivityManager);
            when(NetworkUtils.isInternetAvailable(connectivityManager)).thenReturn(false);

            assertFalse(new MinimalNetworkingConformer().isNetworkAvailable());
        }
    }

    @Test
    public void testDefaultImpl_nullConnectivityManager_returnsFalse() {
        // Conservative fallback only when connectivity cannot be determined at all.
        assertFalse(new MinimalNetworkingConformer().isNetworkAvailable());
    }

    @Test
    public void testDefaultImpl_connectionInfo_reflectsConnectivityManagerState() {
        try (MockedStatic<NetworkUtils> ignored = Mockito.mockStatic(NetworkUtils.class)) {
            when(appContextService.getConnectivityManager()).thenReturn(connectivityManager);
            NetworkConnectionInfo expected =
                    new NetworkConnectionInfo(
                            true, NetworkConnectionInfo.InterfaceType.WIFI, false, false);
            when(NetworkUtils.getNetworkConnectionInfo(connectivityManager)).thenReturn(expected);

            NetworkConnectionInfo result = new MinimalNetworkingConformer().networkConnectionInfo();
            assertEquals(expected.isAvailable(), result.isAvailable());
            assertEquals(expected.getInterfaceType(), result.getInterfaceType());
        }
    }

    @Test
    public void
            testDefaultImpl_connectionInfo_nullConnectivityManager_returnsUnavailableWithUnknownType() {
        NetworkConnectionInfo info = new MinimalNetworkingConformer().networkConnectionInfo();
        assertFalse(info.isAvailable());
        assertEquals(NetworkConnectionInfo.InterfaceType.UNKNOWN, info.getInterfaceType());
        assertFalse(info.isConstrained());
        assertFalse(info.isExpensive());
    }

    @Test
    public void testDefaultImpl_connectionInfo_isAvailable_consistentWithIsNetworkAvailable() {
        try (MockedStatic<NetworkUtils> ignored = Mockito.mockStatic(NetworkUtils.class)) {
            when(appContextService.getConnectivityManager()).thenReturn(connectivityManager);
            when(NetworkUtils.isInternetAvailable(connectivityManager)).thenReturn(true);
            when(NetworkUtils.getNetworkConnectionInfo(connectivityManager))
                    .thenReturn(
                            new NetworkConnectionInfo(
                                    true, NetworkConnectionInfo.InterfaceType.WIFI, false, false));

            MinimalNetworkingConformer conformer = new MinimalNetworkingConformer();
            assertEquals(
                    conformer.isNetworkAvailable(),
                    conformer.networkConnectionInfo().isAvailable());
        }
    }

    @Test
    public void testDefaultImpl_repeatedReads_returnConsistentResult() {
        try (MockedStatic<NetworkUtils> ignored = Mockito.mockStatic(NetworkUtils.class)) {
            when(appContextService.getConnectivityManager()).thenReturn(connectivityManager);
            when(NetworkUtils.isInternetAvailable(any())).thenReturn(true);

            MinimalNetworkingConformer conformer = new MinimalNetworkingConformer();
            for (int i = 0; i < 1000; i++) {
                assertTrue(
                        "Call " + i + " returned an inconsistent result.",
                        conformer.isNetworkAvailable());
            }
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
