/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2020 Adobe Inc.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Inc. and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Inc.
 *
 **************************************************************************/

package com.adobe.marketing.mobile;

import java.util.HashMap;
import java.util.Map;

final class LifecycleModuleDetails implements ModuleDetails {
	private final String FRIENDLY_NAME = "Lifecycle";

	public String getName() {
		return FRIENDLY_NAME;
	}

	public String getVersion() {
		return Lifecycle.extensionVersion();
	}

	public Map<String, String> getAdditionalInfo() {
		return new HashMap<>();
	}
}