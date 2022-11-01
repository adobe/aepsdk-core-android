package com.adobe.marketing.mobile.services;

import android.util.DisplayMetrics;

class DisplayInfoService implements DeviceInforming.DisplayInformation {

	private DisplayMetrics displayMetrics;

	DisplayInfoService(final DisplayMetrics displayMetrics) {
		this.displayMetrics = displayMetrics;
	}

	@Override
	public int getWidthPixels() {
		return displayMetrics.widthPixels;
	}

	@Override
	public int getHeightPixels() {
		return displayMetrics.heightPixels;
	}

	@Override
	public int getDensityDpi() {
		return displayMetrics.densityDpi;
	}
}

