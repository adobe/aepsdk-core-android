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
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.util.StringUtils
import com.adobe.marketing.mobile.util.UrlUtils
import java.util.Random

internal sealed class AEPPushTemplateNotificationBuilder {
    var channelIdToUse: String? = null

    @Throws(NotificationConstructionFailedException::class)
    protected fun construct(
        context: Context,
        pushTemplate: AEPPushTemplate,
        trackerActivityName: String?,
        smallLayout: RemoteViews,
        expandedLayout: RemoteViews
    ): NotificationCompat.Builder {

        // set any custom colors if needed
        setCustomNotificationColors(
            pushTemplate.notificationBackgroundColor,
            pushTemplate.titleTextColor,
            pushTemplate.expandedBodyTextColor,
            smallLayout,
            expandedLayout,
            R.id.basic_expanded_layout
        )

        val builder = NotificationCompat.Builder(context, channelIdToUse ?: PushTemplateConstants.DEFAULT_CHANNEL_ID)
            .setTicker(pushTemplate.ticker)
            .setNumber(pushTemplate.badgeCount)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(smallLayout)
            .setCustomBigContentView(expandedLayout)

        // small icon must be present, otherwise the notification will not be displayed.
        setSmallIcon(
            context, builder, pushTemplate.smallIcon, pushTemplate.smallIconColor
        )

        // set a large icon if one is present
        setRemoteViewLargeIcon(pushTemplate.largeIcon, smallLayout)
        setRemoteViewLargeIcon(
            pushTemplate.largeIcon, expandedLayout
        )

        // set notification visibility
        setVisibility(
            builder, pushTemplate.getNotificationVisibility()
        )

        // set custom sound, note this applies to API 25 and lower only as API 26 and up set the
        // sound on the notification channel
        setSound(context, builder, pushTemplate.sound)
        setNotificationClickAction(
            context,
            trackerActivityName,
            builder,
            pushTemplate.actionUri,
            pushTemplate.tag,
            pushTemplate.isNotificationSticky ?: false
        )
        setNotificationDeleteAction(
            context,
            trackerActivityName,
            builder
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

    /**
     * Sets custom colors to UI elements present in the specified [RemoteViews] object.
     *
     * @param backgroundColor [String] containing the hex color code for the notification
     * background
     * @param titleTextColor `String` containing the hex color code for the notification title
     * text
     * @param expandedBodyTextColor `String` containing the hex color code for the expanded
     * notification body text
     * @param smallLayout [RemoteViews] object for a collapsed custom notification
     * @param expandedLayout `RemoteViews` object for an expanded custom notification
     * @param containerViewId [Int] containing the resource id of the layout container
     */
    private fun setCustomNotificationColors(
        backgroundColor: String?,
        titleTextColor: String?,
        expandedBodyTextColor: String?,
        smallLayout: RemoteViews,
        expandedLayout: RemoteViews,
        containerViewId: Int
    ) {
        // get custom color from hex string and set it the notification background
        if (!backgroundColor.isNullOrEmpty()) {
            setElementColor(
                smallLayout,
                R.id.basic_small_layout,
                "#$backgroundColor",
                PushTemplateConstants.MethodNames.SET_BACKGROUND_COLOR,
                PushTemplateConstants.FriendlyViewNames.NOTIFICATION_BACKGROUND
            )
            setElementColor(
                expandedLayout,
                containerViewId,
                "#$backgroundColor",
                PushTemplateConstants.MethodNames.SET_BACKGROUND_COLOR,
                PushTemplateConstants.FriendlyViewNames.NOTIFICATION_BACKGROUND
            )
        }

        // get custom color from hex string and set it the notification title
        if (!titleTextColor.isNullOrEmpty()) {
            setElementColor(
                smallLayout,
                R.id.notification_title,
                "#$titleTextColor",
                PushTemplateConstants.MethodNames.SET_TEXT_COLOR,
                PushTemplateConstants.FriendlyViewNames.NOTIFICATION_TITLE
            )
            setElementColor(
                expandedLayout,
                R.id.notification_title,
                "#$titleTextColor",
                PushTemplateConstants.MethodNames.SET_TEXT_COLOR,
                PushTemplateConstants.FriendlyViewNames.NOTIFICATION_TITLE
            )
        }

        // get custom color from hex string and set it the notification body text
        if (!expandedBodyTextColor.isNullOrEmpty()) {
            setElementColor(
                smallLayout,
                R.id.notification_body,
                "#$expandedBodyTextColor",
                PushTemplateConstants.MethodNames.SET_TEXT_COLOR,
                PushTemplateConstants.FriendlyViewNames.NOTIFICATION_BODY_TEXT
            )
            setElementColor(
                expandedLayout,
                R.id.notification_body_expanded,
                "#$expandedBodyTextColor",
                PushTemplateConstants.MethodNames.SET_TEXT_COLOR,
                PushTemplateConstants.FriendlyViewNames.NOTIFICATION_BODY_TEXT
            )
        }
    }

    /**
     * Sets a provided color hex string to a UI element contained in a specified [RemoteViews]
     * view.
     *
     * @param remoteView `RemoteViews` object containing a UI element to be updated
     * @param elementId [Int] containing the resource id of the UI element
     * @param colorHex [String] containing the color hex string
     * @param methodName `String` containing the method to be called on the UI element to
     * update the color
     * @param viewFriendlyName `String` containing the friendly name of the view to be used
     * for logging purposes
     */
    private fun setElementColor(
        remoteView: RemoteViews,
        elementId: Int,
        colorHex: String,
        methodName: String,
        viewFriendlyName: String
    ) {
        if (StringUtils.isNullOrEmpty(methodName)) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                AEPPushTemplate.SELF_TAG,
                (
                    "Null or empty method name provided, custom color will not" +
                        " be applied to" +
                        viewFriendlyName
                    )
            )
            return
        }
        try {
            if (!StringUtils.isNullOrEmpty(colorHex)) {
                remoteView.setInt(elementId, methodName, Color.parseColor(colorHex))
            }
        } catch (exception: IllegalArgumentException) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                AEPPushTemplate.SELF_TAG,
                (
                    "Unrecognized hex string passed to Color.parseColor(), custom color will not" +
                        " be applied to" +
                        viewFriendlyName
                    )
            )
        }
    }

    /**
     * Sets the click action for the specified view in the custom push template [RemoteViews].
     *
     * @param context the application [Context]
     * @param trackerActivityName the [String] name of the activity to set in the created pending intent for tracking purposes
     * @param pushTemplateRemoteView `RemoteViews` the parent view representing a push
     * template notification
     * @param targetViewResourceId [Int] containing the resource id of the view to attach the
     * click action
     * notification
     * @param actionUri `String` containing the action uri defined for the push template image
     * @param tag `String` containing the tag to use when scheduling the notification
     * @param stickyNotification [Boolean] if false, remove the [NotificationCompat] after the `RemoteViews` is pressed
     */
    internal fun setRemoteViewClickAction(
        context: Context,
        trackerActivityName: String?,
        pushTemplateRemoteView: RemoteViews,
        targetViewResourceId: Int,
        actionUri: String?,
        tag: String?,
        stickyNotification: Boolean
    ) {
        if (actionUri.isNullOrEmpty()) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                AEPPushTemplate.SELF_TAG,
                "No valid action uri found for the clicked view with id %s. No click action" +
                    " will be assigned.",
                targetViewResourceId
            )
            return
        }
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            AEPPushTemplate.SELF_TAG,
            "Setting remote view click action uri: %s ",
            actionUri
        )

        val pendingIntent: PendingIntent? =
            createPendingIntent(
                context,
                trackerActivityName,
                actionUri,
                null,
                tag,
                stickyNotification
            )
        pushTemplateRemoteView.setOnClickPendingIntent(targetViewResourceId, pendingIntent)
    }

    /**
     * Sets the click action for the notification.
     *
     * @param context the application [Context]
     * @param trackerActivityName the [String] name of the activity to set in the created pending intent for tracking purposes
     * @param notificationBuilder the [NotificationCompat.Builder] to attach the click action
     * notification
     * @param actionUri `String` containing the action uri
     * @param tag `String` containing the tag to use when scheduling the notification
     * @param stickyNotification `boolean` if false, remove the notification after the `RemoteViews` is pressed
     */
    internal fun setNotificationClickAction(
        context: Context,
        trackerActivityName: String?,
        notificationBuilder: NotificationCompat.Builder,
        actionUri: String?,
        tag: String?,
        stickyNotification: Boolean
    ) {
        val pendingIntent: PendingIntent? =
            createPendingIntent(
                context,
                trackerActivityName,
                actionUri,
                null,
                tag,
                stickyNotification
            )
        notificationBuilder.setContentIntent(pendingIntent)
    }

    /**
     * Sets the delete action for the notification.
     *
     * @param context the application [Context]
     * @param trackerActivityName the [String] name of the activity to set in the created pending intent for tracking purposes
     * @param builder the [NotificationCompat.Builder] to attach the delete action
     * notification
     */
    internal fun setNotificationDeleteAction(
        context: Context,
        trackerActivityName: String?,
        builder: NotificationCompat.Builder,
    ) {
        val deleteIntent = Intent(PushTemplateConstants.NotificationAction.DISMISSED)
        trackerActivityName?.let {
            val trackerActivity = Class.forName(trackerActivityName)
            deleteIntent.setClass(context.applicationContext, trackerActivity::class.java)
        }
        deleteIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val intent = PendingIntent.getActivity(
            context,
            Random().nextInt(),
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setDeleteIntent(intent)
    }

    /**
     * Creates a pending intent for a notification.
     *
     * @param context the application [Context]
     * @param trackerActivityName the [String] name of the activity to set in the created pending intent for tracking purposes
     * notification
     * @param actionUri the action uri
     * @param actionID the action ID
     * @param stickyNotification [Boolean] if false, remove the notification after it is interacted with
     * @return the created [PendingIntent]
     */
    internal fun createPendingIntent(
        context: Context,
        trackerActivityName: String?,
        actionUri: String?,
        actionID: String?,
        tag: String?,
        stickyNotification: Boolean
    ): PendingIntent? {
        val intent = Intent(PushTemplateConstants.NotificationAction.BUTTON_CLICKED)
        trackerActivityName?.let {
            val trackerActivity = Class.forName(trackerActivityName)
            intent.setClass(context.applicationContext, trackerActivity::class.java)
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra(PushTemplateConstants.PushPayloadKeys.TAG, tag)
        intent.putExtra(PushTemplateConstants.PushPayloadKeys.STICKY, stickyNotification)
        addActionDetailsToIntent(
            intent,
            actionUri,
            actionID
        )

        // adding tracking details
        return TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(intent)
            .getPendingIntent(
                Random().nextInt(),
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
        if (!StringUtils.isNullOrEmpty(actionUri)) {
            intent.putExtra(PushTemplateConstants.Tracking.Keys.ACTION_URI, actionUri)
        }
        if (!StringUtils.isNullOrEmpty(actionId)) {
            intent.putExtra(PushTemplateConstants.Tracking.Keys.ACTION_ID, actionId)
        }
    }

    /**
     * Sets the sound for the legacy style notification. If a sound is received from the payload, the same is
     * used. If a sound is not received from the payload, the default sound is used.
     *
     * @param context the application [Context]
     * @param notificationBuilder the [NotificationCompat.Builder]
     * @param customSound [String] containing the custom sound file name to load from the
     * bundled assets
     */
    internal fun setSound(
        context: Context,
        notificationBuilder: NotificationCompat.Builder,
        customSound: String?
    ) {
        if (StringUtils.isNullOrEmpty(customSound)) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                AEPPushTemplate.SELF_TAG,
                (
                    "No custom sound found in the push template, using the default notification" +
                        " sound."
                    )
            )
            notificationBuilder.setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            )
            return
        }
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            AEPPushTemplate.SELF_TAG,
            "Setting sound from bundle named %s.",
            customSound
        )
        notificationBuilder.setSound(
            getSoundUriForResourceName(customSound, context)
        )
    }

    /**
     * Sets the provided image url as the large icon for the legacy style notification. If a large icon url is received
     * from the payload, the image is downloaded and the notification style is set to
     * BigPictureStyle. If large icon url is not received from the payload, default style is used
     * for the notification.
     *
     * @param notificationBuilder the [NotificationCompat.Builder]
     * @param imageUrl [String] containing the image url
     * @param title `String` containing the title
     * @param bodyText `String` containing the body text
     */
    internal fun setLargeIcon(
        notificationBuilder: NotificationCompat.Builder,
        imageUrl: String?,
        title: String?,
        bodyText: String?
    ) {
        // Quick bail out if there is no image url
        if (StringUtils.isNullOrEmpty(imageUrl)) return
        val bitmap: Bitmap = download(imageUrl) ?: return

        // Bail out if the download fails
        notificationBuilder.setLargeIcon(bitmap)
        val bigPictureStyle = NotificationCompat.BigPictureStyle()
        bigPictureStyle.bigPicture(bitmap)
        bigPictureStyle.bigLargeIcon(null)
        bigPictureStyle.setBigContentTitle(title)
        bigPictureStyle.setSummaryText(bodyText)
        notificationBuilder.setStyle(bigPictureStyle)
    }

    internal fun setVisibility(
        notificationBuilder: NotificationCompat.Builder,
        visibility: Int
    ) {
        when (visibility) {
            NotificationCompat.VISIBILITY_PUBLIC -> notificationBuilder.setVisibility(
                NotificationCompat.VISIBILITY_PUBLIC
            )

            NotificationCompat.VISIBILITY_PRIVATE -> notificationBuilder.setVisibility(
                NotificationCompat.VISIBILITY_PRIVATE
            )

            NotificationCompat.VISIBILITY_SECRET -> notificationBuilder.setVisibility(
                NotificationCompat.VISIBILITY_SECRET
            )

            else -> {
                notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                Log.debug(
                    PushTemplateConstants.LOG_TAG,
                    AEPPushTemplate.SELF_TAG,
                    (
                        "Invalid visibility value received from the payload. Using the default" +
                            " visibility value."
                        )
                )
            }
        }
    }

    /**
     * Sets the small icon for the notification. If a small icon is received from the payload, the
     * same is used. If a small icon is not received from the payload, we use the icon set using
     * MobileCore.setSmallIcon(). If a small icon is not set using MobileCore.setSmallIcon(), we use
     * the default small icon of the application.
     *
     * @param context the application [Context]
     * @param smallIcon `String` containing the small icon to use
     * @param smallIconColor `String` containing the small icon color to use
     * @param builder the notification builder
     */
    internal fun setSmallIcon(
        context: Context,
        builder: NotificationCompat.Builder,
        smallIcon: String?,
        smallIconColor: String?
    ) {
        val iconFromPayload = getIconWithResourceName(smallIcon, context)
        val iconFromMobileCore = MobileCore.getSmallIconResourceID()
        val iconResourceId: Int
        if (isValidIcon(iconFromPayload)) {
            iconResourceId = iconFromPayload
        } else if (isValidIcon(iconFromMobileCore)) {
            iconResourceId = iconFromMobileCore
        } else {
            val iconFromApp = getDefaultAppIcon(context)
            if (isValidIcon(iconFromApp)) {
                iconResourceId = iconFromApp
            } else {
                Log.warning(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "No valid small icon found. Notification will not be displayed."
                )
                return
            }
        }
        setSmallIconColor(builder, smallIconColor)
        builder.setSmallIcon(iconResourceId)
    }

// TODO: move icon helpers to AEPPushTemplateNotificationBuilder as they are common
    /**
     * Sets the large icon for the provided [RemoteViews]. If a large icon contains a filename
     * only then the large icon is set from a bundle image resource. If a large icon contains a URL,
     * the large icon is downloaded then set.
     *
     * @param largeIcon `String` containing the large icon to use
     * @param remoteView the remote view
     */
    private fun setRemoteViewLargeIcon(largeIcon: String?, remoteView: RemoteViews) {
        if (largeIcon.isNullOrEmpty()) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Null or empty large icon string found, large icon will not be applied."
            )
            remoteView.setViewVisibility(R.id.large_icon, View.GONE)
            return
        }
        if (UrlUtils.isValidUrl(largeIcon)) {
            val downloadedIcon: Bitmap? = downloadImage(
                ServiceProvider.getInstance().cacheService, largeIcon
            )
            if (downloadedIcon == null) {
                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Unable to download an image from %s, large icon will not be applied.",
                    largeIcon
                )
                remoteView.setViewVisibility(R.id.large_icon, View.GONE)
                return
            }
            remoteView.setImageViewBitmap(R.id.large_icon, downloadedIcon)
        } else {
            val bundledIconId: Int? = ServiceProvider.getInstance()
                .appContextService
                .applicationContext?.let {
                    getIconWithResourceName(
                        largeIcon,
                        it
                    )
                }
            if (bundledIconId == 0) {
                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    (
                        "Unable to find a bundled image with name %s, large icon will not be" +
                            " applied."
                        ),
                    largeIcon
                )
                remoteView.setViewVisibility(R.id.large_icon, View.GONE)
                return
            }
            if (bundledIconId != null) {
                remoteView.setImageViewResource(R.id.large_icon, bundledIconId)
            }
        }
    }

    private fun getDefaultAppIcon(context: Context): Int {
        val packageName = context.packageName
        try {
            return context.packageManager.getApplicationInfo(packageName, 0).icon
        } catch (e: PackageManager.NameNotFoundException) {
            Log.warning(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Package manager NameNotFoundException while reading default application icon." +
                    " Exception: %s",
                e.message
            )
        }
        return -1
    }

    /**
     * Sets a custom color to the notification's small icon.
     *
     * @param builder the notification builder
     * @param iconColorHex `String` containing a color code to be used in customizing the
     * small icon color
     */
    private fun setSmallIconColor(
        builder: NotificationCompat.Builder,
        iconColorHex: String?
    ) {
        try {
            // sets the icon color if provided
            if (!StringUtils.isNullOrEmpty(iconColorHex)) {
                val smallIconColor = "#$iconColorHex"
                builder.setColorized(true).color = Color.parseColor(smallIconColor)
            }
        } catch (exception: IllegalArgumentException) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                (
                    "Unrecognized hex string passed to Color.parseColor(), custom color will not" +
                        " be applied to the notification icon."
                    )
            )
        }
    }

    /**
     * Returns the resource id for the drawable with the given name. The file must be in the
     * res/drawable directory. If the drawable file is not found, 0 is returned.
     *
     * @param iconName the name of the icon file
     * @param context the application [Context]
     * @return the resource id for the icon with the given name
     */
    private fun getIconWithResourceName(
        iconName: String?,
        context: Context
    ): Int {
        return if (StringUtils.isNullOrEmpty(iconName)) {
            0
        } else context.resources.getIdentifier(iconName, "drawable", context.packageName)
    }

    /**
     * Checks if the icon is valid.
     *
     * @param icon the icon to be checked
     * @return true if the icon is valid, false otherwise
     */
    private fun isValidIcon(icon: Int): Boolean {
        return icon > 0
    }

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
                    setNotificationChannelSound(context, channel, customSound, false)

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

    private fun setupSilentNotificationChannel(
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
        setNotificationChannelSound(context, silentChannel, null, true)

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
    private fun setNotificationChannelSound(
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

    companion object {
        private const val SELF_TAG = "AEPPushTemplateNotificationBuilder"
    }
}
