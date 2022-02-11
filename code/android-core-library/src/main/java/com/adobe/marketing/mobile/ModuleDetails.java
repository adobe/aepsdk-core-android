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

import java.util.Map;

/**
 * Provides details for an AEP SDK Module/Extension
 *
 * Ideally this interface will be implemented in the platform code to provide better
 * accuracy over which version of the Extensions is actually running.
 */

@SuppressWarnings("unused")
interface ModuleDetails {
	/**
	 * Gets the name of the module.
	 *
	 * @return {@link String} containing the name of the module.
	 */
	String getName();

	/**
	 * Gets the version of the module.
	 *
	 * @return {@link String} containing the version of the module.
	 */
	String getVersion();

	/**
	 * Gets additional info about the module.
	 *
	 * @return {@code Map<String, String>} containing key-value pairs of information about the module.
	 */
	Map<String, String> getAdditionalInfo();
}
