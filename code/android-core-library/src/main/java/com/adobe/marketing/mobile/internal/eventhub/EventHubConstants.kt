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

import com.adobe.marketing.mobile.internal.CoreConstants

internal object EventHubConstants {
    const val NAME = "com.adobe.module.eventhub"
    const val FRIENDLY_NAME = "EventHub"
    const val VERSION_NUMBER = CoreConstants.VERSION
    const val STATE_CHANGE = "Shared state change"
    const val XDM_STATE_CHANGE = "Shared state change (XDM)"

    object EventDataKeys {
        const val VERSION = "version"
        const val EXTENSIONS = "extensions"
        const val WRAPPER = "wrapper"
        const val TYPE = "type"
        const val METADATA = "metadata"
        const val FRIENDLY_NAME = "friendlyName"

        object Configuration {
            const val EVENT_STATE_OWNER = "stateowner"
        }
    }
}
