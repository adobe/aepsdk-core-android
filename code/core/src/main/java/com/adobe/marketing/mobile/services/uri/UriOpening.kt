/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services.uri

/**
 * Represents a component capable of opening URIs.
 */
interface UriOpening {
    /**
     * Opens the given [uri] after consulting the [URIHandler] if one is set.
     * @param uri the URI to open
     * @return true if the URI was opened successfully, false otherwise.
     */
    fun openUri(uri: String): Boolean

    /**
     * Sets the [URIHandler] to be used for fetching destination intents when opening URIs.
     */
    fun setUriHandler(handler: URIHandler)
}
