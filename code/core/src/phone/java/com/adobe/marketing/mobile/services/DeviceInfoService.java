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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import com.adobe.marketing.mobile.internal.util.NetworkUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Locale;

/** Implementation of {@link DeviceInforming} service */
class DeviceInfoService implements DeviceInforming {

    private static final String LOG_TAG = "DeviceInfoService";
    private static final String CANONICAL_PLATFORM_NAME = "android";
    private static final String ANDROID_OS_STRING = "Android";

    private static final String UNEXPECTED_NULL_VALUE = "Unexpected Null Value";

    DeviceInfoService() {}

    /**
     * Returns the currently selected / active locale value with respect to the application context.
     *
     * @return A {@link Locale} value, if available, null otherwise
     */
    public Locale getActiveLocale() {
        final Context context = getApplicationContext();

        if (context == null) {
            return null;
        }

        return getLocaleFromResources(context.getResources());
    }

    /**
     * Returns the currently selected / active locale value on the device settings as set by the
     * user.
     *
     * @return A {@link Locale} value, if available, null otherwise
     */
    @Override
    public Locale getSystemLocale() {
        return getLocaleFromResources(Resources.getSystem());
    }

    @Override
    public DisplayInformation getDisplayInformation() {
        final Context context = getApplicationContext();

        if (context == null) {
            return null;
        }

        Resources resources = context.getResources();

        if (resources == null) {
            return null;
        }

        return new DisplayInfoService(resources.getDisplayMetrics());
    }

    @Override
    public int getCurrentOrientation() {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            return 0; // neither landscape nor portrait
        }

        return currentActivity.getResources().getConfiguration().orientation;
    }

    @Override
    public String getCanonicalPlatformName() {
        return CANONICAL_PLATFORM_NAME;
    }

    /**
     * Returns the string representation of the operating system name.
     *
     * @return Operating system name {@link String}.
     */
    @Override
    public String getOperatingSystemName() {
        return ANDROID_OS_STRING;
    }

    /**
     * Returns the string representation of the operating system version.
     *
     * @return Operating system version {@link String}.
     */
    public String getOperatingSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * Returns the device brand.
     *
     * @return {@code String} containing the consumer-visible brand name
     */
    @Override
    public String getDeviceBrand() {
        return Build.BRAND;
    }

    @Override
    public String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * Returns a human readable device name.
     *
     * @return Device name {@link String} if available. null otherwise.
     */
    public String getDeviceName() {
        return Build.MODEL;
    }

    /**
     * Returns name of the industrial design for the device
     *
     * @return {@code String} containing the name of the industrial design
     */
    @Override
    public String getDevice() {
        return Build.DEVICE;
    }

    @Override
    public DeviceType getDeviceType() {
        final Context context = getApplicationContext();
        final double MIN_TABLET_INCHES = 6.5d;

        if (context == null) {
            return DeviceInforming.DeviceType.UNKNOWN;
        }

        final Resources resources = context.getResources();

        if (resources == null) {
            return DeviceInforming.DeviceType.UNKNOWN;
        }

        final int uiMode = resources.getConfiguration().uiMode;

        if ((uiMode & Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_WATCH) {
            return DeviceType.WATCH;
        }

        final DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        final float yInches = displayMetrics.heightPixels / displayMetrics.ydpi;
        final float xInches = displayMetrics.widthPixels / displayMetrics.xdpi;
        final double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);

        return diagonalInches >= MIN_TABLET_INCHES
                ? DeviceInforming.DeviceType.TABLET
                : DeviceInforming.DeviceType.PHONE;
    }

    /**
     * Returns a string that identifies a particular device OS build. This value may be present on
     * Android devices, with a value like "M4-rc20". The value is platform dependent and platform
     * specific.
     *
     * @return {@link String} Build ID string if available. null otherwise.
     */
    public String getDeviceBuildId() {
        return Build.ID;
    }

    @Override
    public String getMobileCarrierName() {
        final Context context = getApplicationContext();

        if (context == null) {
            return null;
        }

        TelephonyManager telephonyManager =
                ((TelephonyManager) context.getSystemService(Application.TELEPHONY_SERVICE));
        return telephonyManager != null ? telephonyManager.getNetworkOperatorName() : null;
    }

    @Override
    public ConnectionStatus getNetworkConnectionStatus() {
        ConnectivityManager connectivityManager =
                ServiceProvider.getInstance().getAppContextService().getConnectivityManager();
        if (connectivityManager == null) {
            return ConnectionStatus.UNKNOWN;
        }

        return NetworkUtils.isInternetAvailable(connectivityManager)
                ? ConnectionStatus.CONNECTED
                : ConnectionStatus.DISCONNECTED;
    }

    @Override
    public boolean registerOneTimeNetworkConnectionActiveListener(
            final NetworkConnectionActiveListener listener) {
        return false;
    }

    @Override
    public String getRunMode() {
        return "Application";
    }

    @Override
    public String getDeviceUniqueId() {
        Context context = getApplicationContext();

        if (context == null) {
            return null;
        }

        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Returns the default platform/device user agent value
     *
     * @return {@link String} containing the default user agent
     */
    @Override
    public String getDefaultUserAgent() {
        final String unknown = "unknown";
        final String operatingSystemNameWithVersion =
                this.getOperatingSystemName() + " " + this.getOperatingSystemVersion();
        final String operatingSystem =
                isNullOrEmpty(operatingSystemNameWithVersion)
                        ? unknown
                        : operatingSystemNameWithVersion;
        final String locale = getLocaleString();
        final String localeString = isNullOrEmpty(locale) ? unknown : locale;
        final String deviceName =
                isNullOrEmpty(this.getDeviceName()) ? unknown : this.getDeviceName();
        final String deviceBuildId =
                isNullOrEmpty(this.getDeviceBuildId()) ? unknown : this.getDeviceBuildId();

        return String.format(
                "Mozilla/5.0 (Linux; U; %s; %s; %s Build/%s)",
                operatingSystem, localeString, deviceName, deviceBuildId);
    }

    @Override
    public File getApplicationCacheDir() {
        final Context context = getApplicationContext();

        if (context == null) {
            return null;
        }

        return context.getCacheDir();
    }

    @Override
    public InputStream getAsset(final String fileName) {
        final Context context = getApplicationContext();

        if (isNullOrEmpty(fileName) || context == null) {
            return null;
        }

        InputStream inputStream = null;

        Resources resources = context.getResources();

        if (resources == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format(
                            "%s (Resources), unable to read (%s) from the the assets folder.",
                            UNEXPECTED_NULL_VALUE, fileName));
            return null;
        }

        AssetManager assetManager = resources.getAssets();

        if (assetManager == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format(
                            "%s (AssetManager), unable to read (%s) from the the assets folder.",
                            UNEXPECTED_NULL_VALUE, fileName));
            return null;
        }

        try {
            inputStream = assetManager.open(fileName);
        } catch (IOException e) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format(
                            "Unable to read (%s) from the the assets folder. (%s)", fileName, e));
        }

        return inputStream;
    }

    @Override
    public String getPropertyFromManifest(final String propertyKey) {
        final Context context = getApplicationContext();

        if (isNullOrEmpty(propertyKey) || context == null) {
            return null;
        }

        String propertyValue = null;

        PackageManager packageManager = context.getPackageManager();

        if (packageManager == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format(
                            "%s (Package Manager), unable to read property for key (%s).",
                            UNEXPECTED_NULL_VALUE, propertyKey));
            return null;
        }

        try {
            ApplicationInfo ai =
                    packageManager.getApplicationInfo(
                            context.getPackageName(), PackageManager.GET_META_DATA);

            if (ai == null) {
                Log.debug(
                        ServiceConstants.LOG_TAG,
                        LOG_TAG,
                        String.format(
                                "%s (Application info), unable to read property for key (%s).",
                                UNEXPECTED_NULL_VALUE, propertyKey));
                return null;
            }

            Bundle bundle = ai.metaData;

            if (bundle == null) {
                Log.debug(
                        ServiceConstants.LOG_TAG,
                        LOG_TAG,
                        String.format(
                                "%s (ApplicationInfo's metaData), unable to read property for key"
                                        + " (%s).",
                                UNEXPECTED_NULL_VALUE, propertyKey));
                return null;
            }

            propertyValue = bundle.getString(propertyKey);
        } catch (Exception e) {
            // In rare cases, package manager throws run time exception.
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format(
                            "Unable to read property for key (%s). Exception - (%s)",
                            propertyKey, e));
        }

        return propertyValue;
    }

    @Override
    public String getApplicationName() {
        final Context context = getApplicationContext();

        if (context == null) {
            return null;
        }

        String appName = null;

        try {
            PackageManager packageManager = context.getPackageManager();

            if (packageManager == null) {
                return null;
            }

            ApplicationInfo applicationInfo =
                    packageManager.getApplicationInfo(context.getPackageName(), 0);

            if (applicationInfo != null) {
                appName = (String) packageManager.getApplicationLabel(applicationInfo);
            }
        } catch (Exception e) {
            // In rare cases, package manager throws run time exception.
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format("PackageManager couldn't find application name (%s)", e));
        }

        return appName;
    }

    @Override
    public String getApplicationPackageName() {
        final Context context = getApplicationContext();

        if (context == null) {
            return null;
        }

        return context.getPackageName();
    }

    @Override
    public String getApplicationVersion() {
        PackageInfo packageInfo = getPackageInfo();
        return packageInfo != null ? packageInfo.versionName : null;
    }

    @Override
    public String getApplicationVersionCode() {
        PackageInfo packageInfo = getPackageInfo();

        if (packageInfo == null) {
            return null;
        }

        Locale locale = Locale.US;

        final int buildVersion = Build.VERSION.SDK_INT;
        int versionCode = 0;

        if (buildVersion >= Build.VERSION_CODES.P) { // 28
            try {
                Method method = packageInfo.getClass().getDeclaredMethod("getLongVersionCode");
                long longVersion = (Long) method.invoke(packageInfo);
                // getLongVersionCode contains versionCode in the lower 32bits and versionCodeMajor
                // in the higher 32 bits. Casting to int will give us the lower 32 bits.
                versionCode = (int) longVersion;
            } catch (Exception e) {
                Log.debug(
                        ServiceConstants.LOG_TAG,
                        LOG_TAG,
                        String.format("Failed to get app version code, (%s)", e));
            }
        } else {
            versionCode = packageInfo.versionCode;
        }

        return versionCode > 0 ? String.format(locale, "%d", versionCode) : null;
    }

    @Override
    public File getApplicationBaseDir() {
        final Context context = getApplicationContext();

        if (context == null) {
            return null;
        }

        ApplicationInfo applicationInfo = context.getApplicationInfo();

        if (applicationInfo == null) {
            return null;
        }

        return new File(context.getApplicationInfo().dataDir);
    }

    /**
     * Returns active locale's value in string format. The default value is en-US
     *
     * @return Locale as {@code String}
     */
    public String getLocaleString() {
        Locale localeValue = this.getActiveLocale();

        if (localeValue == null) {
            localeValue = Locale.US;
        }

        String result = localeValue.getLanguage();
        final String countryCode = localeValue.getCountry();

        if (!countryCode.isEmpty()) {
            result += "-" + countryCode;
        }

        return result;
    }

    /**
     * Returns the preferred locale value from the Resources object.
     *
     * @return A {@link Locale} value, if available, null otherwise
     */
    private Locale getLocaleFromResources(final Resources resources) {
        if (resources == null) {
            return null;
        }

        final Configuration configuration = resources.getConfiguration();

        if (configuration == null) {
            return null;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return configuration.locale;
        } else {
            return configuration.getLocales().get(0);
        }
    }

    /**
     * Checks if a {@code String} is null, empty or it only contains whitespaces.
     *
     * @param str the {@link String} that we want to check
     * @return {@code boolean} with the evaluation result
     */
    private boolean isNullOrEmpty(final String str) {
        return str == null || str.trim().isEmpty();
    }

    private Context getApplicationContext() {
        return ServiceProvider.getInstance().getAppContextService().getApplicationContext();
    }

    private Activity getCurrentActivity() {
        return ServiceProvider.getInstance().getAppContextService().getCurrentActivity();
    }

    private PackageInfo getPackageInfo() {
        final Context context = getApplicationContext();

        if (context == null) {
            return null;
        }

        try {
            PackageManager packageManager = context.getPackageManager();

            if (packageManager == null) {
                return null;
            }

            return packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (Exception e) {
            // In rare cases, package manager throws run time exception.
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format(
                            "PackageManager couldn't find application version (%s)",
                            e.getLocalizedMessage()));
            return null;
        }
    }
}
