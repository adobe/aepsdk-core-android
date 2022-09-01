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

package com.adobe.marketing.mobile;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.adobe.marketing.mobile.internal.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Locale;

class AndroidSystemInfoService implements SystemInfoService {

	private static final String LOG_TAG = "AndroidSystemInfoService";
	private static final String ANDROID_OS_STRING = "Android";
	private static final String CANONICAL_PLATFORM_NAME = "android";
	static final String CORE_VERSION = MobileCore.extensionVersion();

	@Override
	public File getApplicationBaseDir() {
		Context context = App.getAppContext();

		if (context == null) {
			return null;
		}

		ApplicationInfo applicationInfo = context.getApplicationInfo();

		if (applicationInfo == null) {
			return null;
		}

		return new File(context.getApplicationInfo().dataDir);
	}

	@Override
	public File getApplicationCacheDir() {
		Context context = App.getAppContext();

		if (context == null) {
			return null;
		}

		return context.getCacheDir();
	}

	@Override
	public InputStream getAsset(String fileName) {
		Context context = App.getAppContext();

		if (StringUtils.isNullOrEmpty(fileName) || context == null) {
			return null;
		}

		InputStream inputStream = null;

		Resources resources = context.getResources();

		if (resources == null) {
			Log.debug(LOG_TAG, "%s (Resources), unable to read (%s) from the the assets folder.", Log.UNEXPECTED_NULL_VALUE,
					  fileName);
			return null;
		}

		AssetManager assetManager = resources.getAssets();

		if (assetManager == null) {
			Log.debug(LOG_TAG, "%s (AssetManager), unable to read (%s) from the the assets folder.", Log.UNEXPECTED_NULL_VALUE,
					  fileName);
			return null;
		}

		try {
			inputStream = assetManager.open(fileName);
		} catch (IOException e) {
			Log.warning(LOG_TAG, "Unable to read (%s) from the the assets folder. (%s)", fileName, e);
		}

		return inputStream;
	}

	@Override
	public String getProperty(String propertyKey) {
		Context context = App.getAppContext();

		if (StringUtils.isNullOrEmpty(propertyKey) || context == null) {
			return null;
		}

		String propertyValue = null;

		PackageManager packageManager = context.getPackageManager();

		if (packageManager == null) {
			Log.debug(LOG_TAG, "%s (Package Manager), unable to read property for key (%s).", Log.UNEXPECTED_NULL_VALUE,
					  propertyKey);
			return null;
		}

		try {
			ApplicationInfo ai = packageManager.getApplicationInfo(context.getPackageName(),
								 PackageManager.GET_META_DATA);

			if (ai == null) {
				Log.debug(LOG_TAG, "%s (Application info), unable to read property for key (%s).", Log.UNEXPECTED_NULL_VALUE,
						  propertyKey);
				return  null;
			}

			Bundle bundle = ai.metaData;

			if (bundle == null) {
				Log.debug(LOG_TAG, "%s (ApplicationInfo's metaData), unable to read property for key (%s).", Log.UNEXPECTED_NULL_VALUE,
						  propertyKey);
				return null;
			}

			propertyValue = bundle.getString(propertyKey);
		} catch (PackageManager.NameNotFoundException e) {
			Log.warning(LOG_TAG, "Unable to read property for key (%s). Exception - (%s)", propertyKey, e);
		}

		return propertyValue;
	}

	@Override
	public String getApplicationName() {
		Context context = App.getAppContext();

		if (context == null) {
			return null;
		}

		String appName = null;

		try {
			PackageManager packageManager = context.getPackageManager();

			if (packageManager == null) {
				return null;
			}

			ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);

			if (applicationInfo != null) {
				appName = (String) packageManager.getApplicationLabel(applicationInfo);
			}
		} catch (PackageManager.NameNotFoundException e) {
			Log.warning(LOG_TAG, "PackageManager couldn't find application name (%s)", e);
		}

		return appName;
	}

	@Override
	public String getApplicationPackageName() {
		Context context = App.getAppContext();

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

		if (buildVersion >= 28) {
			try {
				Method method = packageInfo.getClass().getDeclaredMethod("getLongVersionCode");
				long longVersion = (Long)method.invoke(packageInfo);
				// getLongVersionCode contains versionCode in the lower 32bits and versionCodeMajor
				// in the higher 32 bits. Casting to int will give us the lower 32 bits.
				versionCode = (int)longVersion;
			} catch (Exception e) {
				Log.warning(LOG_TAG, "Failed to get app version code, (%s)", e);
			}
		} else {
			versionCode = packageInfo.versionCode;
		}

		return versionCode > 0 ? String.format(locale, "%d", versionCode) : null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Locale getActiveLocale() {
		Context context = App.getAppContext();

		if (context == null) {
			return null;
		}

		Resources resources = context.getResources();

		if (resources == null) {
			return null;
		}

		Configuration configuration = resources.getConfiguration();

		if (configuration == null) {
			return null;
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
			return configuration.locale;
		} else {
			return configuration.getLocales().get(0);
		}
	}

	@Override
	public DisplayInformation getDisplayInformation() {
		Context context = App.getAppContext();

		if (context == null) {
			return null;
		}

		Resources resources = context.getResources();

		if (resources == null) {
			return null;
		}

		return new AndroidDisplayInformation(resources.getDisplayMetrics());
	}

	@Override
	public int getCurrentOrientation() {
		return App.getCurrentOrientation();
	}

	@Override
	public String getCanonicalPlatformName() {
		return CANONICAL_PLATFORM_NAME;
	}

	@Override
	public String getOperatingSystemName() {
		return ANDROID_OS_STRING;
	}

	@Override
	public String getOperatingSystemVersion() {
		return Build.VERSION.RELEASE;
	}

	@Override
	public String getDeviceManufacturer() {
		return Build.MANUFACTURER;
	}

	@Override
	public String getDeviceName() {
		return Build.MODEL;
	}

	@Override
	public DeviceType getDeviceType() {
		final double MIN_TABLET_INCHES = 6.5d;
		final Context context = App.getAppContext();

		if (context == null) {
			return DeviceType.UNKNOWN;
		}

		final Resources resources = context.getResources();

		if (resources == null) {
			return DeviceType.UNKNOWN;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
			final int uiMode = resources.getConfiguration().uiMode;

			if ((uiMode & Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_WATCH) {
				return DeviceType.WATCH;
			}
		}

		final DisplayMetrics displayMetrics = resources.getDisplayMetrics();

		final float yInches = displayMetrics.heightPixels / displayMetrics.ydpi;
		final float xInches = displayMetrics.widthPixels / displayMetrics.xdpi;
		final double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);

		return diagonalInches >= MIN_TABLET_INCHES ? DeviceType.TABLET : DeviceType.PHONE;
	}

	@Override
	public String getDeviceBuildId() {
		return Build.ID;
	}

	@Override
	public String getRunMode() {
		return "Application";
	}

	@Override
	public String getMobileCarrierName() {
		Context context = App.getAppContext();

		if (context == null) {
			return null;
		}

		TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Application.TELEPHONY_SERVICE));
		return telephonyManager != null ? telephonyManager.getNetworkOperatorName() : null;
	}

	@Override
	public ConnectionStatus getNetworkConnectionStatus() {
		Context context = App.getAppContext();

		if (context == null) {
			return ConnectionStatus.UNKNOWN;
		}

		try {
			// We have a context so ask the system for an instance of ConnectivityManager
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

			if (connectivityManager != null) {
				// We have an instance of ConnectivityManager so now we can ask for the active network info
				NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

				if (activeNetworkInfo != null) {
					// At this point we have everything that we need to accurately determine the current connectivity status
					return activeNetworkInfo.isAvailable() && activeNetworkInfo.isConnected() ?
						   ConnectionStatus.CONNECTED : ConnectionStatus.DISCONNECTED;
				} else {
					// Per Android documentation getActiveNetworkInfo() will return null if no default network is currently active.
					// If no default network is currently active we can assume that we don't have connectivity.
					Log.debug(LOG_TAG,
							  "Unable to determine connectivity status due to there being no default network currently active");
				}
			} else {
				Log.warning(LOG_TAG,
							"Unable to determine connectivity status due to the system service requested being unrecognized");
			}
		} catch (NullPointerException e) {
			Log.warning(LOG_TAG, "Unable to determine connectivity status due to an unexpected error (%s)",
						e.getLocalizedMessage());
		} catch (SecurityException e) {
			Log.error(LOG_TAG, "Unable to access connectivity status due to a security error (%s)", e.getLocalizedMessage());
		} catch (Exception e) {
			Log.warning(LOG_TAG, "Unable to access connectivity status due to an unexpected error (%s)",
						e.getLocalizedMessage());
		}

		return ConnectionStatus.UNKNOWN;
	}

	@Override
	public boolean registerOneTimeNetworkConnectionActiveListener(final NetworkConnectionActiveListener listener) {
		try {
			final Context context = App.getAppContext();

			if (context == null) {
				Log.debug(LOG_TAG,
						  "%s (application context), registerOneTimeNetworkConnectionActiveListener did not register.",
						  Log.UNEXPECTED_NULL_VALUE);
				return false;
			}

			// We have a context so ask the system for an instance of ConnectivityManager
			final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
						Context.CONNECTIVITY_SERVICE);

			if (connectivityManager == null) {
				Log.debug(LOG_TAG,
						  "%s (Connectivity Manager), registerOneTimeNetworkConnectionActiveListener did not register.",
						  Log.UNEXPECTED_NULL_VALUE);
				return false;
			}

			// Depending on the api version, there are a few ways track connectivity in Android:
			// (a) ConnectivityManager.CONNECTIVITY_ACTION (added v1, deprecated v28)
			// (b) ConnectivityManager.registerDefaultNetworkCallback (added v24)
			//
			// For maximum compatibility, the following implementation follows method (a).
			final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
				@Override
				public final void onReceive(final Context context, final Intent intent) {
					if (getNetworkConnectionStatus() != ConnectionStatus.CONNECTED) {
						return;
					}

					try {
						context.unregisterReceiver(this);
						listener.onActive();
					} catch (final Exception e) {
						Log.warning(LOG_TAG, "registerOneTimeNetworkConnectionActiveListener: Unexpected error while invoking callback (%s)",
									e.getLocalizedMessage());
					}
				}
			};
			context.registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
			return true;
		} catch (final Exception e) {
			Log.warning(LOG_TAG, "registerOneTimeNetworkConnectionActiveListener: Unexpected error while registering listener (%s)",
						e.getLocalizedMessage());
			return false;
		}
	}

	@Override
	public String getSdkVersion() {
		return CORE_VERSION;
	}

	@Override
	public String getCoreVersion() {
		return CORE_VERSION;
	}

	@Override
	public String getDefaultUserAgent() {
		final String unknown = "unknown";
		final String operatingSystemNameWithVersion = this.getOperatingSystemName() + " " + this.getOperatingSystemVersion();
		final String operatingSystem = StringUtils.isNullOrEmpty(operatingSystemNameWithVersion) ? unknown :
									   operatingSystemNameWithVersion;
		final String locale = getLocaleString();
		final String localeString = StringUtils.isNullOrEmpty(locale) ? unknown : locale;
		final String deviceName = StringUtils.isNullOrEmpty(this.getDeviceName()) ? unknown : this.getDeviceName();
		final String deviceBuildId = StringUtils.isNullOrEmpty(this.getDeviceBuildId()) ? unknown : this.getDeviceBuildId();

		return String.format("Mozilla/5.0 (Linux; U; %s; %s; %s Build/%s)", operatingSystem,
							 localeString, deviceName, deviceBuildId);

	}

	@Override
	public String getLocaleString() {
		Locale localeValue = this.getActiveLocale();

		if (localeValue == null) {
			localeValue = Locale.US;
		}

		String result = localeValue.getLanguage();
		String countryCode = localeValue.getCountry();

		if (!countryCode.isEmpty()) {
			result += "-" + countryCode;
		}

		return result;
	}

	private PackageInfo getPackageInfo() {
		Context context = App.getAppContext();

		if (context == null) {
			return null;
		}

		try {
			PackageManager packageManager = context.getPackageManager();

			if (packageManager == null) {
				return null;
			}

			return packageManager.getPackageInfo(context.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			Log.warning(LOG_TAG, "PackageManager couldn't find application version (%s)", e.getLocalizedMessage());
			return null;
		}
	}
}