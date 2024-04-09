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

package com.adobe.marketing.mobile.services.ui.pushtemplate

import android.app.Activity
import android.content.BroadcastReceiver

internal class PushTemplateTrackers private constructor() {

    private var trackerActivities: HashMap<String, Activity> = HashMap()
    private var broadcastReceivers: HashMap<String, BroadcastReceiver> = HashMap()

    fun setTrackerActivity(activityName: String, trackerActivity: Activity?) {
        trackerActivities[activityName] = trackerActivity ?: return
    }

    fun setBroadcastReceiver(broadcastReceiverName: String, broadcastReceiver: BroadcastReceiver?) {
        broadcastReceivers[broadcastReceiverName] = broadcastReceiver ?: return
    }

    fun getTrackerActivity(activityName: String?): Activity? {
        return trackerActivities[activityName]
    }

    fun getBroadcastReceiver(broadcastReceiverName: String?): BroadcastReceiver? {
        return broadcastReceivers[broadcastReceiverName]
    }

    companion object {
        private val instance = PushTemplateTrackers()

        fun getInstance(): PushTemplateTrackers {
            return instance
        }
    }
}
