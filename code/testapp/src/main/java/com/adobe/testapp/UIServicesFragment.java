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

/**
 * A simple {@link Fragment} subclass.
 */
public class UIServicesFragment extends Fragment implements View.OnClickListener {

	private TestAppUIServices testAppUIServices;

	public UIServicesFragment() {
		testAppUIServices = new TestAppUIServices();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_ui_services, container, false);

		view.findViewById(R.id.btnShowAlert).setOnClickListener(this);
		view.findViewById(R.id.btnShowLocalNotification).setOnClickListener(this);
		view.findViewById(R.id.btnShowLocalNotificationWithTitle).setOnClickListener(this);
		view.findViewById(R.id.btnShowFullScreenMsg).setOnClickListener(this);
		view.findViewById(R.id.btnShowUrl).setOnClickListener(this);
		view.findViewById(R.id.btnShowFloatingButton).setOnClickListener(this);
		view.findViewById(R.id.btnHideFloatingButton).setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();

		if (viewId == R.id.btnShowAlert) {
			testAppUIServices.showAlert(getString(R.string.test_alert_title), getString(R.string.test_alert_message),
										getString(R.string.test_alert_positive_text), getString(R.string.test_alert_negative_text));
		} else if (viewId == R.id.btnShowLocalNotification) {
			testAppUIServices.showLocalNotification("id", getString(R.string.test_notification_content),
													System.currentTimeMillis() / 1000, 0, "myscheme://link",  null,
													"sound.wav");
		} else if (viewId == R.id.btnShowLocalNotificationWithTitle) {
			testAppUIServices.showLocalNotification("id", getString(R.string.test_notification_content),
													System.currentTimeMillis() / 1000, 0, "myscheme://link",  null,
													"sound.wav", getString(R.string.test_notification_title));
		} else if (viewId == R.id.btnShowFullScreenMsg) {
			testAppUIServices.showFullscreenMessage("<html><body>" + getString(R.string.test_fullscreen_html) + "<p /><a href=\"adbinapp://dismiss\"><button>Dismiss me</button></a></body></html>");
		} else if (viewId == R.id.btnShowUrl) {
			testAppUIServices.showUrl(getString(R.string.test_url));
		} else if (viewId == R.id.btnShowFloatingButton) {
			testAppUIServices.showFloatingButton();
		} else if (viewId == R.id.btnHideFloatingButton) {
			testAppUIServices.hideFloatingButton();
		}
	}
}