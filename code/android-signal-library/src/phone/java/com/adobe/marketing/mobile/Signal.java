/* ***********************************************************************
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
 **************************************************************************/

package com.adobe.marketing.mobile;

public class Signal {
	//Suppressing the Unused warning for now, since Signals now does not have any public APIs of its own -
	//making the SingalCore instance not being used.
	@SuppressWarnings("unused")
	private static SignalCore signalCore;
	private final static String EXTENSION_VERSION = "1.0.4";

	private Signal() {

	}

	public static String extensionVersion() {
		return EXTENSION_VERSION;
	}

	public  static void registerExtension() throws InvalidInitException {
		Core core = MobileCore.getCore();

		if (core == null) {
			throw  new InvalidInitException();
		}

		try {
			//MobileCore may not be loaded or present (because may be Core extension was not
			//available). In that case, the Signal extension will not initialize itself
			signalCore = new SignalCore(core.eventHub, new SignalModuleDetails());
		} catch (Exception e) {
			throw new InvalidInitException();
		}
	}
}
