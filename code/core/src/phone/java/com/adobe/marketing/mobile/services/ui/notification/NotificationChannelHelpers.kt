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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.util.StringUtils

/**
 * Utility functions to assist in creating notification channels.
 */
private const val SELF_TAG = "NotificationChannelHelpers"

/**
 * Creates a channel if it does not exist and returns the channel ID. If a channel ID is
 * received from the payload and if channel exists for the channel ID, the same channel ID is
 * returned. If a channel ID is received from the payload and if channel does not exist for the
 * channel ID, Campaign Classic extension's default channel is used. If no channel ID is
 * received from the payload, Campaign Classic extension's default channel is used. For Android
 * versions below O, no channel is created. Just return the obtained channel ID.
 *
 * @param context the application [Context]
 * @param channelId `String` containing the notification channel id
 * @param customSound `String` containing the custom sound to use
 * @param importance `int` containing the notification importance
 * @return the channel ID
 */
internal fun createChannelAndGetChannelID(
    context: Context,
    channelId: String?,
    customSound: String?,
    importance: Int
): String {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        // For Android versions below O, no channel is created. Just return the obtained channel
        // ID.
        return channelId ?: PushTemplateConstants.DEFAULT_CHANNEL_ID
    } else {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // For Android versions O and above, create a channel if it does not exist and return
        // the channel ID.
        channelId?.let { channelIdFromPayload ->
            // setup a silent channel for notification carousel item change
            setupSilentNotificationChannel(context, notificationManager, importance)

            // if a channel from the payload is not null and if a channel exists for the channel ID
            // from the payload, use the same channel ID.
            if (notificationManager.getNotificationChannel(channelIdFromPayload) != null
            ) {
                Log.debug(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Channel exists for channel ID: $channelIdFromPayload. Using the existing channel for the push notification."
                )
                return channelIdFromPayload
            } else {
                Log.debug(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Channel does not exist for channel ID obtained from payload ($channelIdFromPayload). Creating a channel with the retrieved channel name."
                )
                val channel = NotificationChannel(
                    channelIdFromPayload, PushTemplateConstants.DEFAULT_CHANNEL_NAME, importance
                )

                // set a custom sound on the channel
                setSound(context, channel, customSound, false)

                // add the channel to the notification manager
                notificationManager.createNotificationChannel(channel)
                return channelIdFromPayload
            }
        }

        // Use the default channel ID if the channel ID from the payload is null or if a channel
        // does not exist for the channel ID from the payload.
        if (notificationManager.getNotificationChannel(PushTemplateConstants.DEFAULT_CHANNEL_ID) != null) {
            Log.debug(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Channel already exists for the default channel ID: ${PushTemplateConstants.DEFAULT_CHANNEL_ID}"
            )
        } else {
            Log.debug(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                ("Creating a new channel for the default channel ID: ${PushTemplateConstants.DEFAULT_CHANNEL_ID}.")
            )
            val channel = NotificationChannel(
                PushTemplateConstants.DEFAULT_CHANNEL_ID,
                PushTemplateConstants.DEFAULT_CHANNEL_NAME,
                importance
            )
            notificationManager.createNotificationChannel(channel)
        }
        return PushTemplateConstants.DEFAULT_CHANNEL_ID
    }
}

internal fun setupSilentNotificationChannel(
    context: Context,
    notificationManager: NotificationManager,
    importance: Int
) {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
        return
    }
    if ((
        notificationManager.getNotificationChannel(
                PushTemplateConstants.DefaultValues.SILENT_NOTIFICATION_CHANNEL_ID
            )
            != null
        )
    ) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Using previously created silent channel."
        )
        return
    }

    // create a channel containing no sound to be used when displaying an updated carousel
    // notification
    val silentChannel = NotificationChannel(
        PushTemplateConstants.DefaultValues.SILENT_NOTIFICATION_CHANNEL_ID,
        PushTemplateConstants.SILENT_CHANNEL_NAME,
        importance
    )

    // set no sound on the silent channel
    setSound(context, silentChannel, null, true)

    // add the silent channel to the notification manager
    notificationManager.createNotificationChannel(silentChannel)
}

/**
 * Sets the sound for the provided `NotificationChannel`. If a sound is received from the
 * payload, the same is used. If a sound is not received from the payload, the default sound is
 * used.
 *
 * @param context the application [Context]
 * @param notificationChannel the [NotificationChannel] to assign the sound to
 * @param customSound `String` containing the custom sound file name to load from the
 * bundled assets
 */
private fun setSound(
    context: Context,
    notificationChannel: NotificationChannel,
    customSound: String?,
    isSilent: Boolean
) {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
        return
    }
    if (isSilent) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Creating a silent notification channel."
        )
        notificationChannel.setSound(null, null)
        return
    }
    if (StringUtils.isNullOrEmpty(customSound)) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            (
                "No custom sound found in the push template, using the default" +
                    " notification sound."
                )
        )
        notificationChannel.setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null
        )
        return
    }
    Log.trace(
        PushTemplateConstants.LOG_TAG,
        SELF_TAG,
        "Setting sound from bundle named %s.",
        customSound
    )
    notificationChannel.setSound(
        getSoundUriForResourceName(customSound, context), null
    )
}
