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

package com.adobe.testapp;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adobe.marketing.mobile.services.ui.TestAppUIServices;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.ServiceProvider;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlatformServicesFragment extends Fragment implements View.OnClickListener {

	private TestAppUIServices testAppUIServices;


	public PlatformServicesFragment() {
		testAppUIServices = new TestAppUIServices();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_platform_services, container, false);

		view.findViewById(R.id.btnShowDeviceInfo).setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
			case R.id.btnShowDeviceInfo:
				DeviceInforming deviceInforming = ServiceProvider.getInstance().getDeviceInfoService();
				StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append("\ngetApplicationName() - " + deviceInforming.getApplicationName());
				stringBuffer.append("\ngetApplicationPackageName() - " + deviceInforming.getApplicationPackageName());
				stringBuffer.append("\ngetApplicationVersion() - " + deviceInforming.getApplicationVersion());
				stringBuffer.append("\ngetApplicationVersionCode() - " + deviceInforming.getApplicationVersionCode());
				stringBuffer.append("\ngetApplicationBaseDir() - " + deviceInforming.getApplicationBaseDir());
				stringBuffer.append("\ngetApplicationCacheDir() - " + deviceInforming.getApplicationCacheDir());
				stringBuffer.append("\ngetActiveLocale() - " + deviceInforming.getActiveLocale());
				stringBuffer.append("\ngetCanonicalPlatformName() - " + deviceInforming.getCanonicalPlatformName());
				stringBuffer.append("\ngetDefaultUserAgent() - " + deviceInforming.getDefaultUserAgent());
				stringBuffer.append("\ngetDeviceBuildId() - " + deviceInforming.getDeviceBuildId());
				stringBuffer.append("\ngetDeviceType() - " + deviceInforming.getDeviceType());
				stringBuffer.append("\ngetDeviceManufacturer() - " + deviceInforming.getDeviceManufacturer());
				stringBuffer.append("\ngetDeviceName() - " + deviceInforming.getDeviceName());
				stringBuffer.append("\ngetLocaleString() - " + deviceInforming.getLocaleString());
				stringBuffer.append("\ngetMobileCarrierName() - " + deviceInforming.getMobileCarrierName());
				stringBuffer.append("\ngetNetworkConnectionStatus() - " + deviceInforming.getNetworkConnectionStatus());
				stringBuffer.append("\ngetOperatingSystemVersion() - " + deviceInforming.getOperatingSystemVersion());
				stringBuffer.append("\ngetRunMode() - " + deviceInforming.getRunMode());
				stringBuffer.append("\ngetWidthPixels - " + deviceInforming.getDisplayInformation().getWidthPixels());
				stringBuffer.append("\ngetHeightPixels - " + deviceInforming.getDisplayInformation().getHeightPixels());
				stringBuffer.append("\ngetDensityDpi - " + deviceInforming.getDisplayInformation().getDensityDpi());



				testAppUIServices.showAlert(getString(R.string.test_alert_title), stringBuffer.toString(),
											getString(R.string.test_alert_positive_text), getString(R.string.test_alert_negative_text));
				break;

			default:
				break;
		}


	}
}