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

import java.io.File;
import java.io.InputStream;
import java.util.Locale;

public interface DeviceInforming {
    /** Represents the possible network connection status for the Adobe SDK. */
    enum ConnectionStatus {
        /** Network connectivity exists. */
        CONNECTED,
        /** No Network connectivity. */
        DISCONNECTED,
        /** Unknown when unable to access the connectivity status. */
        UNKNOWN,
    }

    /** Represents the possible device types. */
    enum DeviceType {
        PHONE,
        TABLET,
        WATCH,
        UNKNOWN,
    }

    interface NetworkConnectionActiveListener {
        /** Invoked when the connection has become active. */
        void onActive();
    }

    interface DisplayInformation {
        /**
         * Returns absolute width of the available display size in pixels.
         *
         * @return width in pixels if available. -1 otherwise.
         */
        int getWidthPixels();

        /**
         * Returns absolute height of the available display size in pixels.
         *
         * @return height in pixels if available. -1 otherwise.
         */
        int getHeightPixels();

        /**
         * Returns the screen dots-per-inch
         *
         * @return dpi if available. -1 otherwise.
         */
        int getDensityDpi();
    }

    /**
     * Returns the directory which can be used as a application data directory.
     *
     * @return A {@link File} representing the application data directory, or null if not available
     *     on the platform
     */
    File getApplicationBaseDir();

    /**
     * Returns active locale's value in string format. The default value is en-US
     *
     * @return Locale as {@code String}
     */
    String getLocaleString();

    /**
     * Returns the default platform/device user agent value
     *
     * @return {@link String} containing the default user agent
     */
    String getDefaultUserAgent();

    /**
     * Returns the application specific cache directory. The application will be able to read and
     * write to the directory, but there is no guarantee made as to the persistence of the data (it
     * may be deleted by the system when storage is required).
     *
     * @return A {@link File} representing the application cache directory, or null if not available
     *     on the platform.
     */
    File getApplicationCacheDir();

    /**
     * Open the requested asset returns an InputStream to read its contents.
     *
     * @param fileName asset's name which is to be retrieved
     * @return an {@link InputStream} to read file's content, or null if not available on the
     *     platform
     */
    InputStream getAsset(String fileName);

    /**
     * Returns the property value specific to the key from the manifest file.
     *
     * @param resourceKey resource key
     * @return A {@link String} value of the requested property, or null if there is no value
     *     defined for the key
     */
    String getPropertyFromManifest(String resourceKey);

    /**
     * Returns the application name.
     *
     * @return A {@link String} representing Application name if available, null otherwise
     */
    String getApplicationName();

    /**
     * Returns the application package name.
     *
     * @return A {@link String} representing Application package name if available, null otherwise
     */
    String getApplicationPackageName();

    /**
     * Returns the application version.
     *
     * @return A {@link String} representing Application version if available, null otherwise
     */
    String getApplicationVersion();

    /**
     * Returns the application version code as a string.
     *
     * @return application version code formatted as {@link String} using the active locale, if
     *     available. null otherwise
     */
    String getApplicationVersionCode();

    /**
     * Returns the currently selected / active locale value (as set by the user on the system).
     *
     * @return A {@link Locale} value, if available, null otherwise
     */
    Locale getActiveLocale();

    /**
     * Returns information about the display hardware, as returned by the underlying OS.
     *
     * @return {@link DeviceInforming.DisplayInformation} instance, or null if application context
     *     is null
     * @see DeviceInforming.DisplayInformation
     */
    DeviceInforming.DisplayInformation getDisplayInformation();

    /**
     * Returns the current screen orientation
     *
     * @return a {@code int} value indicates the orientation. 0 for unknown, 1 for portrait and 2
     *     for landscape
     */
    int getCurrentOrientation();

    /**
     * Returns the string representation of the operating system name.
     *
     * @return Operating system name {@link String}.
     */
    String getOperatingSystemName();

    /**
     * Returns the string representation of the canonical platform name.
     *
     * @return Platform name {@link String}.
     */
    String getCanonicalPlatformName();

    /**
     * Returns the string representation of the operating system version.
     *
     * @return Operating system version {@link String}.
     */
    String getOperatingSystemVersion();

    /**
     * Returns the device brand.
     *
     * @return {@code String} containing the consumer-visible brand name
     */
    String getDeviceBrand();

    /**
     * The device manufacturer's name.
     *
     * @return Device manufacturer name {@link String} if available. null otherwise
     */
    String getDeviceManufacturer();

    /**
     * Returns the device name.
     *
     * @return {@link String} Device name if available, null otherwise
     */
    String getDeviceName();

    /**
     * Returns name of the industrial design for the device
     *
     * @return {@code String} containing the name of the industrial design
     */
    String getDevice();

    /**
     * Returns the device type.
     *
     * <ul>
     *   <li>If {@link DeviceInforming.DeviceType#PHONE}, for a Phone type device
     *   <li>If {@link DeviceInforming.DeviceType#TABLET}, for a Tablet type device
     *   <li>If {@link DeviceInforming.DeviceType#WATCH}, for a Watch type device
     *   <li>If {@link DeviceInforming.DeviceType#UNKNOWN}, when device type cannot be determined
     * </ul>
     *
     * @return {@link DeviceInforming.DeviceType} representing the device type
     * @see DeviceInforming.DeviceType
     */
    DeviceInforming.DeviceType getDeviceType();

    /**
     * Returns a string that identifies a particular device OS build. This value may be present on
     * Android devices, with a value like "M4-rc20". The value is platform dependent and platform
     * specific.
     *
     * @return {@link String} Build ID string if available. null otherwise
     */
    String getDeviceBuildId();

    /**
     * Returns the device's mobile carrier name.
     *
     * @return A {@link String} representing the carrier name. null if this value is not available
     */
    String getMobileCarrierName();

    /**
     * Indicates whether network connectivity exists and it is possible to establish connections and
     * pass data.
     *
     * <p>Always call this before attempting to perform data transactions.
     *
     * <ul>
     *   <li>If {@link DeviceInforming.ConnectionStatus#CONNECTED}, if we have network connectivity.
     *   <li>If {@link DeviceInforming.ConnectionStatus#DISCONNECTED}, if do not have network
     *       connectivity.
     *   <li>If {@link DeviceInforming.ConnectionStatus#UNKNOWN}, if unable to determine the network
     *       connectivity.
     * </ul>
     *
     * @return {@link DeviceInforming.ConnectionStatus} representing the current network status
     * @see DeviceInforming.ConnectionStatus
     */
    DeviceInforming.ConnectionStatus getNetworkConnectionStatus();

    /**
     * Invokes a callback when the network connection status changes.
     *
     * @param listener {@link DeviceInforming.NetworkConnectionActiveListener} listener that will
     *     get invoked once when the connection status changes.
     * @see #getNetworkConnectionStatus()
     * @return whether the registration was successful
     */
    boolean registerOneTimeNetworkConnectionActiveListener(
            DeviceInforming.NetworkConnectionActiveListener listener);

    /**
     * Returns a string that identifies the SDK running mode, e.g. Application, Extension.
     *
     * @return {@link String} containing running mode
     */
    String getRunMode();

    /**
     * Get unique identifier for device.
     *
     * @return {@code String} containing the device UUID or null if application {@code Context} is
     *     null
     */
    String getDeviceUniqueId();
}
