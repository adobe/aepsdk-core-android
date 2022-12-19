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

interface PlatformServices {

	/**
	 * Returns the Logging service implementation from the Platform.
	 *
	 * @return LoggingService implementation, if the platform provides any. null otherwise.
	 *
	 * @see LoggingService
	 */
	LoggingService getLoggingService();

	/**
	 * Returns the Network service implementation from the Platform.
	 *
	 * @return NetworkService implementation, if the platform provides any. null otherwise.
	 *
	 * @see NetworkService
	 */
	NetworkService getNetworkService();

	/**
	 * Returns the Local Storage Service implementation from the Platform.
	 *
	 * @return LocalStorageService implementation, if the platform provides any. null otherwise.
	 *
	 * @see LocalStorageService
	 */
	LocalStorageService getLocalStorageService();

	/**
	 * Returns the System Notification service implementation from the Platform.
	 *
	 * @return SystemNotificationService implementation, if the platform provides any. null otherwise.
	 *
	 * @see SystemNotificationService
	 */
	SystemNotificationService getSystemNotificationService();

	/**
	 * Returns the System Information service implementation from the Platform.
	 *
	 * @return SystemInfoService implementation, if the platform provides any. null otherwise.
	 *
	 * @see SystemInfoService
	 */
	SystemInfoService getSystemInfoService();

	/**
	 * Returns the UI Service implementation from the Platform.
	 *
	 * @return UIService implementation, if the platform provides any. null otherwise.
	 *
	 * @see UIService
	 */
	UIService getUIService();

	/**
	 * Returns the Json Utility service implementation from the Platform.
	 *
	 * @return JsonUtilityService implementation, if the platform provides any. null otherwise.
	 *
	 * @see JsonUtilityService
	 */
	JsonUtilityService getJsonUtilityService();


	/**
	 * Returns the Deeplink service implementation from the Platform.
	 *
	 * @return DeepLinkService implementation, if the platform provides any. null otherwise.
	 *
	 * @see DeepLinkService
	 */
	DeepLinkService getDeepLinkService();


	/**
	 * Returns the encoding service implementation from the Platform.
	 *
	 * @return EncodingService implementation, if the platform provides any. null otherwise.
	 *
	 * @see EncodingService
	 */
	EncodingService getEncodingService();

	CompressedFileService getCompressedFileService();
}
