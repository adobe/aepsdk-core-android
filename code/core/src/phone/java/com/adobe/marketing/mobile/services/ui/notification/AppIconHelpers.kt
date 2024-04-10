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

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.util.StringUtils
import com.adobe.marketing.mobile.util.UrlUtils

/**
 * Utility functions to assist in setting small and large app icons on a push template notification.
 */
private const val SELF_TAG = "AppIconHelpers"

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

/**
 * Sets the large icon for the provided [RemoteViews]. If a large icon contains a filename
 * only then the large icon is set from a bundle image resource. If a large icon contains a URL,
 * the large icon is downloaded then set.
 *
 * @param largeIcon `String` containing the large icon to use
 * @param remoteView the remote view
 */
internal fun setRemoteViewLargeIcon(largeIcon: String?, remoteView: RemoteViews) {
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
