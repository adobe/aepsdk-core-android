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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {

	private final int totalTabsCount;

	PagerAdapter(FragmentManager fm, int totalTabsCount) {
		super(fm);
		this.totalTabsCount = totalTabsCount;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return new RootFragment();

			case 1:
				return new UIServicesFragment();

			case 2:
				return new PlatformServicesFragment();

			default:
				return null;
		}
	}

	@Override
	public int getCount() {
		return totalTabsCount;
	}
}
