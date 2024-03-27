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
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

/**
 * Abstract class defining a framework for constructing a [NotificationCompat.Builder] object containing a push template notification.
 */
internal abstract class TemplateNotificationBuilder {
    var pushTemplate: AEPPushTemplate? = null
        private set
    var intent: Intent? = null
        private set
    var trackerActivity: Activity? = null
        private set
    var broadcastReceiver: BroadcastReceiver? = null
        private set

    fun pushTemplate(pushTemplate: AEPPushTemplate?) = apply {
        this.pushTemplate = pushTemplate
    }

    fun intent(intent: Intent?) = apply {
        this.intent = intent
    }

    fun trackerActivity(trackerActivity: Activity?) = apply {
        this.trackerActivity = trackerActivity
    }

    fun broadcastReceiver(broadcastReceiver: BroadcastReceiver?) = apply {
        this.broadcastReceiver = broadcastReceiver
    }

    abstract fun build(context: Context): NotificationCompat.Builder
}
