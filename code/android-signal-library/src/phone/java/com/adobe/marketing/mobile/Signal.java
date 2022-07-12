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
//		Core core = MobileCore.getCore();
//
//		if (core == null) {
//			throw  new InvalidInitException();
//		}
//
//		try {
//			//MobileCore may not be loaded or present (because may be Core extension was not
//			//available). In that case, the Signal extension will not initialize itself
//			signalCore = new SignalCore(core.eventHub, new SignalModuleDetails());
//		} catch (Exception e) {
//			throw new InvalidInitException();
//		}
	}
}
