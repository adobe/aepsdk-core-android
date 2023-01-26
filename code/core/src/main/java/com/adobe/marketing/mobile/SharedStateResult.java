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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Map;

/** Contains the status and value for a given shared state */
public class SharedStateResult {

    private final SharedStateStatus status;
    private final Map<String, Object> value;

    /**
     * Creates a new shared state result with given status and value
     *
     * @param status status of the shared state
     * @param value value of the shared state
     */
    public SharedStateResult(
            @NonNull final SharedStateStatus status, @Nullable final Map<String, Object> value) {
        this.status = status;
        this.value = value;
    }

    /** Returns the {@link SharedStateStatus}. */
    public @NonNull SharedStateStatus getStatus() {
        return status;
    }

    /** Returns the shared state. */
    public @Nullable Map<String, Object> getValue() {
        return value;
    }
}
