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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;

import com.adobe.marketing.mobile.services.internal.context.App;
import com.adobe.marketing.mobile.services.ServiceProvider;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;

class AndroidSystemInfoService implements SystemInfoService {

	private static final String LOG_TAG = "AndroidSystemInfoService";
	private static final String ANDROID_OS_STRING = "Android";
	private static final String CANONICAL_PLATFORM_NAME = "android";
	static final String CORE_VERSION = MobileCore.extensionVersion();

	@Override
	public File getApplicationBaseDir() {
		return ServiceProvider.getInstance().getDeviceInfoService().getApplicationBaseDir();
	}

	@Override
	public File getApplicationCacheDir() {
		return ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir();
	}

	@Override
	public InputStream getAsset(String fileName) {
		return ServiceProvider.getInstance().getDeviceInfoService().getAsset(fileName);
	}

	@Override
	public String getProperty(String propertyKey) {
		return ServiceProvider.getInstance().getDeviceInfoService().getPropertyFromManifest(propertyKey);
	}

	@Override
	public String getApplicationName() {
		return ServiceProvider.getInstance().getDeviceInfoService().getApplicationName();
	}

	@Override
	public String getApplicationPackageName() {
		return ServiceProvider.getInstance().getDeviceInfoService().getApplicationPackageName();
	}

	@Override
	public String getApplicationVersion() {
		return ServiceProvider.getInstance().getDeviceInfoService().getApplicationVersion();
	}

	@Override
	public String getApplicationVersionCode() {
		return ServiceProvider.getInstance().getDeviceInfoService().getApplicationVersionCode();
	}

	@SuppressWarnings("deprecation")
	@Override
	public Locale getActiveLocale() {
		return ServiceProvider.getInstance().getDeviceInfoService().getActiveLocale();
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
		return ServiceProvider.getInstance().getDeviceInfoService().getMobileCarrierName();
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
		return ServiceProvider.getInstance().getDeviceInfoService().getDefaultUserAgent();
	}

	@Override
	public String getLocaleString() {
		return ServiceProvider.getInstance().getDeviceInfoService().getLocaleString();
	}
}