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


/**
 * Base class for internal (adobe only) defined modules
 * This class adds the {@code PlatformServices} tie in to the EventHub, which allows the module to access
 * Platform-specific functionality in the environment owning the Module.
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 * @see Module
 * @see PlatformServices
 * @see EventHub
 */
abstract class InternalModule extends Module {
	private final PlatformServices services;

	/**
	 * Constructor for an internal module, must be called by inheritors.
	 *
	 * @param moduleName the name of the module
	 * @param    hub    {@code EventHub} instance of eventhub that owns this module
	 * @param    services    {@code PlatformServices} instance
	 */
	InternalModule(final String moduleName, final EventHub hub, final PlatformServices services) {
		super(moduleName, hub);
		this.services = services;
	}

	/**
	 * Retrieves instance of {@code PlatformServices} that is associated with this module
	 *
	 * @return instance of platform services for this module
	 */
	final PlatformServices getPlatformServices() {
		return services;
	}

}
