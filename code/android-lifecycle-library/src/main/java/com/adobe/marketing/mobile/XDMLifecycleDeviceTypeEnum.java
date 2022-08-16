/* ************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2021 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/

package com.adobe.marketing.mobile;

/**
 * XDM Device type enum definition. Supported values by the Android SDK are:
 * <ul>
 *     <li>mobile</li>
 *     <li>tablet</li>
 * </ul>
 *
 * Other possible values, not supported at this time:
 * <ul>
 *     <li>desktop</li>
 *     <li>ereader</li>
 *     <li>gaming</li>
 *     <li>television</li>
 *     <li>settop</li>
 *     <li>mediaplayer</li>
 *     <li>computers</li>
 *     <li>tv screens</li>
 * </ul>
 */
@SuppressWarnings("unused")
enum XDMLifecycleDeviceTypeEnum {
	MOBILE("mobile"), // Mobile
	TABLET("tablet"); // Tablet
	// todo: watch to be added once included in the xdm enum

	private final String value;

	XDMLifecycleDeviceTypeEnum(final String enumValue) {
		this.value = enumValue;
	}

	@Override
	public String toString() {
		return value;
	}
}
