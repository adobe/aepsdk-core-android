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

/**
 * Internal representation of a shared event state.
 * Allows associating version and pending behavior with the state data in a queryable way.
 */
internal data class SharedState constructor(val data: Map<String, Any?>?, val status: Status) {

    /**
     * Represents the status of an extensions shared state, typically associated with a version.
     */
    internal enum class Status {
        SET,
        PENDING,
        NOT_SET
    }
}

/**
 * Represents the types of shared state that are supported.
 */
internal enum class SharedStateType {
    STANDARD,
    XDM
}
