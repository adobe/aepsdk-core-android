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

package com.adobe.marketing.mobile.services.ui.notification.builders

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ui.notification.NotificationConstructionFailedException
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateImageUtils
import com.adobe.marketing.mobile.services.ui.notification.extensions.createNotificationChannelIfRequired
import com.adobe.marketing.mobile.services.ui.notification.templates.ZeroBezelPushTemplate

internal object ZeroBezelNotificationBuilder {
    private const val SELF_TAG = "BasicTemplateNotificationBuilder"

    @Throws(NotificationConstructionFailedException::class)
    fun construct(
        context: Context,
        pushTemplate: ZeroBezelPushTemplate,
        trackerActivityClass: Class<out Activity>?
    ): NotificationCompat.Builder {
        val cacheService = ServiceProvider.getInstance().cacheService
            ?: throw NotificationConstructionFailedException("Cache service is null, zero bezel template notification will not be constructed.")

        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Building a zero bezel template push notification."
        )
        val packageName = context.packageName
        val smallLayout = RemoteViews(packageName, R.layout.push_template_zero_bezel_collapsed)
        val expandedLayout = RemoteViews(packageName, R.layout.push_template_zero_bezel_expanded)

        // download and cache the image used in the notification
        val downloadedImageCount =
            PushTemplateImageUtils.cacheImages(cacheService, listOf(pushTemplate.imageUrl))

        // Check if the image was downloaded
        if (downloadedImageCount > 0) {
            // set the image on the notification if it was downloaded
            val pushImage =
                PushTemplateImageUtils.getCachedImage(cacheService, pushTemplate.imageUrl)
            expandedLayout.setImageViewBitmap(R.id.expanded_template_image, pushImage)

            // only set image on the collapsed view if the style is "img"
            if (pushTemplate.collapsedStyle == ZeroBezelPushTemplate.ZeroBezelStyle.IMAGE) {
                smallLayout.setImageViewBitmap(R.id.collapsed_template_image, pushImage)
            } else {
                smallLayout.setViewVisibility(R.id.collapsed_template_image, View.GONE)
                smallLayout.setViewVisibility(R.id.gradient_template_image, View.GONE)
            }
        } else {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "No image found for zero bezel push template."
            )
            // hide the image views if no image was downloaded
            expandedLayout.setViewVisibility(R.id.expanded_template_image, View.GONE)
            expandedLayout.setViewVisibility(R.id.gradient_template_image, View.GONE)
            smallLayout.setViewVisibility(R.id.collapsed_template_image, View.GONE)
            smallLayout.setViewVisibility(R.id.gradient_template_image, View.GONE)
        }

        // create the notification channel if required
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelIdToUse: String = notificationManager.createNotificationChannelIfRequired(
            context,
            pushTemplate.channelId,
            pushTemplate.sound,
            pushTemplate.getNotificationImportance(),
            pushTemplate.isFromIntent
        )

        // create the notification builder with the common settings applied
        return AEPPushNotificationBuilder.construct(
            context,
            pushTemplate,
            channelIdToUse,
            trackerActivityClass,
            smallLayout,
            expandedLayout,
            R.id.basic_expanded_layout
        )
    }
}
