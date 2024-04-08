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

class PushTemplateTrackers {
    private constructor()

    private var trackerActivity: Activity? = null
    private var broadcastReceiver: BroadcastReceiver? = null

    fun setTrackerActivity(trackerActivity: Activity?) {
        this.trackerActivity = trackerActivity
    }

    fun setBroadcastReceiver(broadcastReceiver: BroadcastReceiver?) {
        this.broadcastReceiver = broadcastReceiver
    }

    fun getTrackerActivity(): Activity? {
        return trackerActivity
    }

    fun getBroadcastReceiver(): BroadcastReceiver? {
        return broadcastReceiver
    }

    companion object {
        private val instance = PushTemplateTrackers()

        fun getInstance(): PushTemplateTrackers {
            return instance
        }
    }
}
