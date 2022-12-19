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

import android.content.Context;
import android.content.Intent;

interface BroadcastHandler {

    /**
     * Handle the Android system broadcast. This may marshall the data from the Intent, and convert
     * it into an event data {@code Map<String, Object>} and call the corresponding API to dispatch
     * the data in an {@link Event}.
     *
     * @param context Context as received from the Android Broadcast receiver.
     * @param intent Intent as received from the Android Broadcast receiver.
     */
    void handleBroadcast(Context context, Intent intent);
}
