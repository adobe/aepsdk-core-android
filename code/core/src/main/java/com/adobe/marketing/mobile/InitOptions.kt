/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile

/**
 * This class holds initialization options for configuring the AEP SDK.
 */
class InitOptions {
    // Flag indicating whether automatic lifecycle tracking is enabled.
    var lifecycleAutomaticTrackingEnabled: Boolean = true

    // Additional context data to be included in lifecycle start event.
    var lifecycleAdditionalContextData: Map<String, String>? = null
        set(value) {
            field = value?.toMap()
        }

    @JvmSynthetic
    internal var config: ConfigType = ConfigType.Bundled

    companion object {
        /**
         * Configures the SDK by downloading the remote configuration file hosted on Adobe servers
         * specified by the given application ID(App ID) from tags property.
         * @param appID The App ID for the Adobe SDK configuration.
         * @return An instance of InitOptions configured with the App ID.
         */
        @JvmStatic
        fun configureWithAppID(appID: String): InitOptions {
            return InitOptions().apply {
                config = ConfigType.AppID(appID)
            }
        }

        /**
         * Configures the SDK by using a file located at a specific path.
         * @param filePath The file path for the configuration file.
         * @return An instance of InitOptions configured with the file path.
         */
        @JvmStatic
        fun configureWithFileInPath(filePath: String): InitOptions {
            return InitOptions().apply {
                config = ConfigType.FileInPath(filePath)
            }
        }

        /**
         * Configures the SDK by using a file stored in application assets.
         * @param filePath The path of the file in assets.
         * @return An instance of InitOptions configured with the asset file path.
         */
        @JvmStatic
        fun configureWithFileInAssets(filePath: String): InitOptions {
            return InitOptions().apply {
                config = ConfigType.FileInAssets(filePath)
            }
        }
    }
}

/**
 * Sealed interface representing different types of configuration options.
 */
internal sealed interface ConfigType {

    // Default configuration that uses bundled settings
    object Bundled : ConfigType

    /**
     * Configuration using an App ID.
     * @param appID The App ID string.
     */
    data class AppID(val appID: String) : ConfigType

    /**
     * Configuration using a file located in application assets.
     * @param filePath The path to the configuration file in assets.
     */
    data class FileInAssets(val filePath: String) : ConfigType

    /**
     * Configuration using a file located at a specified path.
     * @param filePath The path to the configuration file.
     */
    data class FileInPath(val filePath: String) : ConfigType
}
