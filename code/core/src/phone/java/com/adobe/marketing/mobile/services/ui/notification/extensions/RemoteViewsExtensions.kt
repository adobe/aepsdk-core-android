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

package com.adobe.marketing.mobile.services.ui.notification.extensions

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ui.notification.PendingIntentUtils
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateImageUtil
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateUtils
import com.adobe.marketing.mobile.util.UrlUtils

private const val SELF_TAG = "RemoteViewExtensions"

/**
 * Sets a provided color hex string to a UI element contained in a specified [RemoteViews]
 * view.
 *
 * @param elementId [Int] containing the resource id of the UI element
 * @param colorHex [String] containing the color hex string
 * @param methodName `String` containing the method to be called on the UI element to
 * update the color
 * @param viewFriendlyName `String` containing the friendly name of the view to be used
 * for logging purposes
 */
internal fun RemoteViews.setElementColor(
    elementId: Int,
    colorHex: String,
    methodName: String,
    viewFriendlyName: String
) {
    if (colorHex.isEmpty()) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Empty color hex string found, custom color will not be applied to $viewFriendlyName."
        )
        return
    }

    try {
        setInt(elementId, methodName, Color.parseColor(colorHex))
    } catch (exception: IllegalArgumentException) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Unrecognized hex string passed to Color.parseColor(), custom color will not be applied to $viewFriendlyName."
        )
    }
}

/**
 * Sets custom colors to the notification background.
 *
 * @param backgroundColor [String] containing the hex color code for the notification background
 * @param containerViewId [Int] containing the resource id of the push template notification RemoteViews
 */
internal fun RemoteViews.setNotificationBackgroundColor(
    backgroundColor: String?,
    containerViewId: Int
) {
    // get custom color from hex string and set it the notification background
    if (backgroundColor.isNullOrEmpty()) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Empty background color hex string found, custom color will not be applied to the notification background."
        )
        return
    }
    setElementColor(
        containerViewId,
        "#$backgroundColor",
        PushTemplateConstants.MethodNames.SET_BACKGROUND_COLOR,
        PushTemplateConstants.FriendlyViewNames.NOTIFICATION_BACKGROUND
    )
}

/**
 * Sets custom colors to the notification title text.
 *
 * @param titleTextColor [String] containing the hex color code for the notification title text
 * @param containerViewId [Int] containing the resource id of the push template notification RemoteViews
 */
internal fun RemoteViews.setNotificationTitleTextColor(
    titleTextColor: String?,
    containerViewId: Int
) {
    // get custom color from hex string and set it the notification title
    if (titleTextColor.isNullOrEmpty()) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Empty title text color hex string found, custom color will not be applied to the notification title text."
        )
        return
    }
    setElementColor(
        containerViewId,
        "#$titleTextColor",
        PushTemplateConstants.MethodNames.SET_TEXT_COLOR,
        PushTemplateConstants.FriendlyViewNames.NOTIFICATION_TITLE
    )
}

/**
 * Sets custom colors to the notification body text.
 *
 * @param expandedBodyTextColor [String] containing the hex color code for the expanded
 * notification body text
 * @param containerViewId [Int] containing the resource id of the push template notification RemoteViews
 */
internal fun RemoteViews.setNotificationBodyTextColor(
    expandedBodyTextColor: String?,
    containerViewId: Int
) {
    // get custom color from hex string and set it the notification body text
    if (expandedBodyTextColor.isNullOrEmpty()) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Empty expanded body text color hex string found, custom color will not be applied to the notification body text."
        )
        return
    }
    setElementColor(
        containerViewId,
        "#$expandedBodyTextColor",
        PushTemplateConstants.MethodNames.SET_TEXT_COLOR,
        PushTemplateConstants.FriendlyViewNames.NOTIFICATION_BODY_TEXT
    )
}

/**
 * Sets the large icon for the provided [RemoteViews]. If a large icon contains a filename
 * only then the large icon is set from a bundle image resource. If a large icon contains a URL,
 * the large icon is downloaded then set.
 *
 * @param largeIcon `String` containing the large icon to use
 */
internal fun RemoteViews.setRemoteViewLargeIcon(largeIcon: String?) {
    if (largeIcon.isNullOrEmpty()) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Null or empty large icon string found, large icon will not be applied."
        )
        setViewVisibility(R.id.large_icon, View.GONE)
        return
    }

    if (UrlUtils.isValidUrl(largeIcon)) {
        setRemoteLargeIcon(largeIcon)
    } else {
        setBundledLargeIcon(largeIcon)
    }
}

/**
 * Sets the click action for the specified view in the custom push template [RemoteViews].
 *
 * @param context the application [Context]
 * @param trackerActivityClass the [Class] of the activity to set in the created pending intent for tracking purposes
 * template notification
 * @param targetViewResourceId [Int] containing the resource id of the view to attach the click action
 * @param actionUri `String` containing the action uri defined for the push template image
 * @param tag `String` containing the tag to use when scheduling the notification
 * @param stickyNotification [Boolean] if false, remove the [NotificationCompat] after the `RemoteViews` is pressed
 */
internal fun RemoteViews.setRemoteViewClickAction(
    context: Context,
    trackerActivityClass: Class<out Activity>?,
    targetViewResourceId: Int,
    actionUri: String?,
    tag: String?,
    stickyNotification: Boolean
) {
    if (actionUri.isNullOrEmpty()) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "No valid action uri found for the clicked view with id $targetViewResourceId. No click action will be assigned."
        )
        return
    }
    Log.trace(
        PushTemplateConstants.LOG_TAG,
        SELF_TAG,
        "Setting remote view click action uri: $actionUri."
    )

    val pendingIntent: PendingIntent? =
        PendingIntentUtils.createPendingIntent(
            context,
            trackerActivityClass,
            actionUri,
            null,
            tag,
            stickyNotification
        )
    setOnClickPendingIntent(targetViewResourceId, pendingIntent)
}

internal fun RemoteViews.setRemoteLargeIcon(largeIcon: String?) {
    if (UrlUtils.isValidUrl(largeIcon)) {
        val downloadedIcon: Bitmap? = PushTemplateImageUtil.downloadImage(
            ServiceProvider.getInstance().cacheService, largeIcon
        )
        if (downloadedIcon == null) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Unable to download an image from $largeIcon, large icon will not be applied."
            )
            setViewVisibility(R.id.large_icon, View.GONE)
            return
        }
        setImageViewBitmap(R.id.large_icon, downloadedIcon)
    }
}

internal fun RemoteViews.setBundledLargeIcon(largeIcon: String?) {
    val bundledIconId: Int? = ServiceProvider.getInstance()
        .appContextService
        .applicationContext?.let {
            PushTemplateUtils.getIconWithResourceName(
                largeIcon,
                it
            )
        }
    if (bundledIconId == null || bundledIconId == 0) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Unable to find a bundled image with name $largeIcon, large icon will not be applied."
        )
        setViewVisibility(R.id.large_icon, View.GONE)
        return
    }
    setImageViewResource(R.id.large_icon, bundledIconId)
}
