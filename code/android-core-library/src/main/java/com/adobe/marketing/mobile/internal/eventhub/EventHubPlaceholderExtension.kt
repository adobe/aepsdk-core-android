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

package com.adobe.marketing.mobile.internal.eventhub

import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi

/**
 * An `Extension` for `EventHub`. This serves no purpose other than to allow `EventHub` to store share state and manage event listeners.
 */

internal class EventHubPlaceholderExtension(val extensionApi: ExtensionApi) : Extension(extensionApi) {
    override fun getName() = EventHubConstants.NAME
    override fun getFriendlyName() = EventHubConstants.FRIENDLY_NAME
    override fun getVersion() = EventHubConstants.VERSION_NUMBER
}
