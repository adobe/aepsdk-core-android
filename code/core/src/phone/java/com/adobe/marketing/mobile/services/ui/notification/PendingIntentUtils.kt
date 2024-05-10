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

package com.adobe.marketing.mobile.services.ui.notification

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Random

internal object PendingIntentUtils {

    private const val SELF_TAG = "IntentUtils"

    /**
     * Creates a pending intent for a notification.
     *
     * @param context the application [Context]
     * @param trackerActivityClass the [Class] of the activity to set in the created pending intent for tracking purposes
     * notification
     * @param actionUri the action uri
     * @param actionID the action ID
     * @param stickyNotification [Boolean] if false, remove the notification after it is interacted with
     * @return the created [PendingIntent]
     */
    internal fun createPendingIntent(
        context: Context,
        trackerActivityClass: Class<out Activity>?,
        actionUri: String?,
        actionID: String?,
        tag: String?,
        stickyNotification: Boolean
    ): PendingIntent? {
        val intent = Intent(PushTemplateConstants.NotificationAction.BUTTON_CLICKED)
        trackerActivityClass?.let {
            intent.setClass(context.applicationContext, trackerActivityClass)
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(PushTemplateConstants.PushPayloadKeys.TAG, tag)
        intent.putExtra(PushTemplateConstants.PushPayloadKeys.STICKY, stickyNotification)
        addActionDetailsToIntent(
            intent,
            actionUri,
            actionID
        )

        return PendingIntent.getActivity(
            context,
            Random().nextInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Adds action details to the provided [Intent].
     *
     * @param intent the intent
     * @param actionUri [String] containing the action uri
     * @param actionId `String` containing the action ID
     */
    private fun addActionDetailsToIntent(
        intent: Intent,
        actionUri: String?,
        actionId: String?
    ) {
        if (!actionUri.isNullOrEmpty()) {
            intent.putExtra(PushTemplateConstants.Tracking.TrackingKeys.ACTION_URI, actionUri)
        }
        if (!actionId.isNullOrEmpty()) {
            intent.putExtra(PushTemplateConstants.Tracking.TrackingKeys.ACTION_ID, actionId)
        }
    }
}
