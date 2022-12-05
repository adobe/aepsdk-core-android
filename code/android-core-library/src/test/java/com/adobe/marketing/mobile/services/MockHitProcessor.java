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
import java.util.ArrayList;
import java.util.List;

class MockHitProcessor implements HitProcessing {

    public int retryInterval = 1;
    public boolean hitResult = true;
    public List<DataEntity> processedHits = new ArrayList<>();

    @Override
    public int retryInterval(@NonNull DataEntity entity) {
        return retryInterval;
    }

    @Override
    public void processHit(
            @NonNull DataEntity entity, @NonNull HitProcessingResult processingResult) {
        processedHits.add(entity);
        processingResult.complete(hitResult);
    }
}
