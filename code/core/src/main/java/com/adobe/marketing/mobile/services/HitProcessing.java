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

package com.adobe.marketing.mobile.services;

import androidx.annotation.NonNull;

// A Type provide the functionality for processing hits.
public interface HitProcessing {
    /**
     * Determines the interval at which a hit should be retried
     *
     * @param entity The hit whose retry interval is to be computed
     * @return Hit retry interval in seconds.
     */
    int retryInterval(@NonNull DataEntity entity);

    /**
     * Function that is invoked with a {@link DataEntity} and provides functionality for processing
     * the hit.
     *
     * @param entity The <code>DataEntity</code> to be processed.
     * @param processingResult Return a boolean variable indicating <code>DataEntity</code> is
     *     successfully processed or not.
     */
    void processHit(@NonNull DataEntity entity, @NonNull HitProcessingResult processingResult);
}
