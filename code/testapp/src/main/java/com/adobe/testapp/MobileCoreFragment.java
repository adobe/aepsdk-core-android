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

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;

import java.util.HashMap;
import java.util.Map;

public class MobileCoreFragment extends Fragment implements View.OnClickListener {

	private static final String TAG = "MobileCoreFragment";
	private LogModeSpinAdapter spinAdapter;

	public MobileCoreFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mobile_core, container, false);

		view.findViewById(R.id.btnSetConfig).setOnClickListener(this);
		view.findViewById(R.id.btnLifecyclePause).setOnClickListener(this);
		view.findViewById(R.id.btnSetAdvId).setOnClickListener(this);
		view.findViewById(R.id.btnSetPushId).setOnClickListener(this);
		view.findViewById(R.id.btnCollectPii).setOnClickListener(this);
		view.findViewById(R.id.btnTrackState).setOnClickListener(this);
		view.findViewById(R.id.btnTrackAction).setOnClickListener(this);
		view.findViewById(R.id.btnRegisterExtension).setOnClickListener(this);
		view.findViewById(R.id.btnLog).setOnClickListener(this);

		spinAdapter = new LogModeSpinAdapter(getActivity(), android.R.layout.simple_spinner_item, LoggingMode.values());
		Spinner logModeSpinner = view.findViewById(R.id.logMoreSpinner);
		logModeSpinner.setAdapter(spinAdapter);
		logModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				LoggingMode mode = spinAdapter.getItem(position);
				MobileCore.setLogLevel(mode);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nothing at all
			}
		});

		LoggingMode selectedMode = MobileCore.getLogLevel();
		logModeSpinner.setSelection(selectedMode.id);
		return view;
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();

		if (viewId == R.id.btnSetConfig) {
			onSetConfigClicked(view);
		} else if (viewId == R.id.btnLifecyclePause) {
			onLifecyclePause(view);
		} else if (viewId == R.id.btnSetAdvId) {
			onSetAdvId(view);
		} else if (viewId == R.id.btnSetPushId) {
			onSetPushId(view);
		} else if (viewId == R.id.btnLifecycleStart) {
			onLifecycleStart(view);
		} else if (viewId == R.id.btnCollectPii) {
			onCollectPii(view);
		} else if (viewId == R.id.btnTrackState) {
			onTrackState(view);
		} else if (viewId == R.id.btnTrackAction) {
			onTrackAction(view);
		} else if (viewId == R.id.btnRegisterExtension) {
			onRegisterExtension(view);
		} else if (viewId == R.id.btnLog) {
			onLog(view);
		}
	}


	public void onSetConfigClicked(View view) {
		Map<String, Object> config = new HashMap<>();
		config.put("lifecycle.timeout", 5);
		MobileCore.updateConfiguration(config);
	}

	public void onLifecyclePause(View view) {
		MobileCore.lifecyclePause();
	}

	public void onSetAdvId(View view) {
		MobileCore.setAdvertisingIdentifier("advid");
	}

	public void onSetPushId(View view) {
		MobileCore.setPushIdentifier("pushid");
	}

	public void onLifecycleStart(View view) {
		Map<String, String> c = new HashMap<>();
		c.put("key", "value");
		MobileCore.lifecycleStart(c);
	}

	public void onCollectPii(View view) {
		Map<String, String> c = new HashMap<>();
		c.put("key", "value");
		MobileCore.collectPii(c);
	}

	public void onTrackState(View view) {
		Map<String, String> c = new HashMap<>();
		c.put("key", "value");
		MobileCore.trackState("state", c);
	}

	public void onTrackAction(View view) {
		Map<String, String> c = new HashMap<>();
		c.put("key", "value");
		MobileCore.trackAction("action", c);
	}

	public void onRegisterExtension(View view) {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.root_frame, new ExtensionFragment());
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	/**
	 * Produce sample log entries.
	 */
	public void onLog(View view) {
		Log.v(TAG, "Logging VERBOSE from android.util.Log");
		MobileCore.log(LoggingMode.VERBOSE, TAG, "Logging TRACE from MobileCore");

		Log.d(TAG, "Logging DEBUG from android.util.Log");
		MobileCore.log(LoggingMode.DEBUG, TAG, "Logging DEBUG from MobileCore");

		Log.w(TAG, "Logging WARNING from android.util.Log");
		MobileCore.log(LoggingMode.WARNING, TAG, "Logging WARNING from MobileCore");

		Log.e(TAG, "Logging ERROR from android.util.Log");
		MobileCore.log(LoggingMode.ERROR, TAG, "Logging ERROR from MobileCore");

		Log.i(TAG, "Logging INFO from android.util.Log");
	}

	static class LogModeSpinAdapter extends ArrayAdapter<LoggingMode> {

		private final LoggingMode[] values;

		public LogModeSpinAdapter(Context context, int textViewResourceId, LoggingMode[] values) {
			super(context, textViewResourceId, values);
			this.values = values;
		}

		@Override
		public int getCount() {
			return values.length;
		}

		@Override
		public LoggingMode getItem(int position) {
			return values[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		@NonNull
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			TextView label = (TextView) super.getView(position, convertView, parent);
			label.setTextColor(Color.BLACK);
			label.setText(values[position].name());

			return label;
		}

		@Override
		public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
			TextView label = (TextView) super.getDropDownView(position, convertView, parent);
			label.setTextColor(Color.BLACK);
			label.setText(values[position].name());

			return label;
		}
	}
}