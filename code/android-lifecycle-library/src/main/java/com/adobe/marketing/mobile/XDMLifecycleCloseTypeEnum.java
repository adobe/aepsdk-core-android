/* **************************************************************************
 *
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
 *
 * *************************************************************************/

package com.adobe.marketing.mobile;

@SuppressWarnings("unused")
enum XDMLifecycleCloseTypeEnum {
	CLOSE("close"), // Close
	UNKNOWN("unknown"); // Unknown

	private final String value;

	XDMLifecycleCloseTypeEnum(final String enumValue) {
		this.value = enumValue;
	}

	@Override
	public String toString() {
		return value;
	}
}
