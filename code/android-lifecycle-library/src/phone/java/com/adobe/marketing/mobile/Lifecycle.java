/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Systems Incorporated
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

public class Lifecycle {
	private final static String TAG = Lifecycle.class.getSimpleName();
	private final static String EXTENSION_VERSION = "1.1.1";
	@SuppressWarnings("unused")
	private static LifecycleCore lifecycleCore;

	private Lifecycle() {

	}

	public static String extensionVersion() {
		return EXTENSION_VERSION;
	}

	public static void registerExtension() throws InvalidInitException {
		Core core = MobileCore.getCore();

		if (core == null) {
			throw  new InvalidInitException();
		}

		try {
			//ADBCore may not be loaded or present (because may be Core extension was not
			//available). In that case, the Lifecycle extension will not initialize itself
			lifecycleCore = new LifecycleCore(core.eventHub, new LifecycleModuleDetails());
		} catch (Exception e) {
			throw new InvalidInitException();
		}
	}

}
