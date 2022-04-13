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
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExtensionFragment extends Fragment implements View.OnClickListener {

	private TextView tvEventsProcessed;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_extension, container, false);
		tvEventsProcessed = view.findViewById(R.id.tvEventsProcessed);
		Button btnDispatchEvent = view.findViewById(R.id.btnDispatchEvent);
		Button btnDispatchPaired = view.findViewById(R.id.btnDispatchPaired);
		Button btnPrintLatestConfig = view.findViewById(R.id.btnPrintLatestConfig);
		Button btnUnregisterExtension = view.findViewById(R.id.btnUnregisterExtension);
		Button btnRegisterExtension = view.findViewById(R.id.btnRegisterExtension);
		Button gotoMain = view.findViewById(R.id.gotoMain);

		btnDispatchEvent.setOnClickListener(this);
		btnDispatchPaired.setOnClickListener(this);
		btnPrintLatestConfig.setOnClickListener(this);
		btnUnregisterExtension.setOnClickListener(this);
		btnRegisterExtension.setOnClickListener(this);
		gotoMain.setOnClickListener(this);

		return view;
	}


	public void onDispatchEvent(View view) {
		Map<String, String> profile = new HashMap<String, String>();
		profile.put("username", "Alex");
		profile.put("userage", "25");
		profile.put("city", "Bucharest");
		//		Victory.setProfile(profile);
	}

	public void onDispatchEventWithResponseCallback(View view) {
		//		Victory.getNoEventsProcessed(new AdobeCallback<Long>() {
		//			@Override
		//			public void call(final Long value) {
		//				runOnUiThread(new Runnable() {
		//					@Override
		//					public void run() {
		//						TextView t = (TextView) findViewById(R.id.tvEventsProcessed);
		//						String newText = "Events processed # " + String.valueOf(value);
		//						t.setText(newText);
		//					}
		//				});
		//			}
		//		});
	}

	public void onRegisterExtension(View view) {
		//		Victory.registerExtension();
	}

	public void onUnregisterExtension(View view) {
		//		Victory.unregisterExtension();
		if (getActivity() != null) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					tvEventsProcessed.setText(R.string.events_processed_0);
				}
			});
		}
	}

	public void onGotoMainActivity(final View view) {
		//		Victory.gotoActivity(MainActivity.class.getName());
	}

	public void onPrintLatestConfig(final View view) {
		//		Victory.printLatestConfig();
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();

		if (viewId == R.id.btnDispatchEvent) {
			onDispatchEvent(view);
		} else if (viewId == R.id.btnDispatchPaired) {
			onDispatchEventWithResponseCallback(view);
		} else if (viewId == R.id.btnPrintLatestConfig) {
			onPrintLatestConfig(view);
		} else if (viewId == R.id.btnUnregisterExtension) {
			onUnregisterExtension(view);
		} else if (viewId == R.id.btnRegisterExtension) {
			onRegisterExtension(view);
		} else if (viewId == R.id.gotoMain) {
			onGotoMainActivity(view);
		}

	}
}