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
 * XDM Environment type enum definition. Supported values by the Android SDK are:
 * <ul>
 *     <li>application</li>
 * </ul>
 *
 * Other possible values, not supported at this time:
 * <ul>
 *     <li>browser</li>
 *     <li>iot</li>
 *     <li>external</li>
 *     <li>widget</li>
 * </ul>
 */
@SuppressWarnings("unused")
enum XDMLifecycleEnvironmentTypeEnum {
	APPLICATION("application"); // Application

	private final String value;

	XDMLifecycleEnvironmentTypeEnum(final String enumValue) {
		this.value = enumValue;
	}

	@Override
	public String toString() {
		return value;
	}
}
