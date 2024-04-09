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

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.services.Log

/**
 * Class responsible for constructing a legacy (non [RemoteViews] based) push notification.
 */
internal class LegacyNotificationBuilder : TemplateNotificationBuilder() {
    companion object {
        private const val SELF_TAG = "LegacyNotificationBuilder"

        private fun construct(
            context: Context,
            trackerActivityName: String?,
            pushTemplate: BasicPushTemplate?
        ): NotificationCompat.Builder {
            if (pushTemplate == null) {
                throw NotificationConstructionFailedException(
                    "push template is null, cannot build a notification."
                )
            }

            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Building a legacy style push notification."
            )

            val trackerActivity = PushTemplateTrackers.getInstance().getTrackerActivity(trackerActivityName)
            // create the notification channel
            val channelId: String = createChannelAndGetChannelID(
                context,
                pushTemplate.getChannelId(),
                pushTemplate.getSound(),
                pushTemplate.getNotificationImportance()
            )

            // Create the notification builder object and set the ticker, title, body, and badge count
            val builder = NotificationCompat.Builder(
                context, channelId
            )
                .setTicker(pushTemplate.getTicker())
                .setContentTitle(pushTemplate.getTitle())
                .setContentText(pushTemplate.getBody())
                .setNumber(pushTemplate.getBadgeCount())

            // set a large icon if one is present
            setLargeIcon(
                builder,
                pushTemplate.getImageUrl(),
                pushTemplate.getTitle(),
                pushTemplate.getExpandedBodyText()
            )

            // small Icon must be present, otherwise the notification will not be displayed.
            setSmallIcon(
                context,
                builder,
                pushTemplate.getSmallIcon(),
                pushTemplate.getSmallIconColor()
            )

            // set notification visibility
            setVisibility(
                builder, pushTemplate.getNotificationVisibility()
            )

            // add any action buttons defined for the notification
            addActionButtons(
                context,
                trackerActivity,
                builder,
                pushTemplate.getActionButtons(),
                pushTemplate.getMessageId(),
                pushTemplate.getDeliveryId(),
                pushTemplate.getTag(),
                pushTemplate.getStickyStatus() ?: false
            )

            // set custom sound, note this applies to API 25 and lower only as API 26 and up set the
            // sound on the notification channel
            setSound(context, builder, pushTemplate.getSound())

            // assign a click action pending intent to the notification
            setNotificationClickAction(
                context,
                trackerActivity,
                builder,
                pushTemplate.getMessageId(),
                pushTemplate.getDeliveryId(),
                pushTemplate.getActionUri(),
                pushTemplate.getTag(),
                pushTemplate.getStickyStatus() ?: false
            )

            // set notification delete action
            setNotificationDeleteAction(
                context,
                trackerActivity,
                builder,
                pushTemplate.getMessageId(),
                pushTemplate.getDeliveryId()
            )

            // if API level is below 26 (prior to notification channels) then notification priority is
            // set on the notification builder
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(LongArray(0)) // hack to enable heads up notifications as a HUD style
                // notification requires a tone or vibration
            }

            return builder
        }
    }

    override fun build(
        context: Context
    ): NotificationCompat.Builder {
        return construct(
            context,
            trackerActivityName,
            pushTemplate as? BasicPushTemplate
        )
    }
}
