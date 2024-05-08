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

import androidx.core.app.NotificationCompat

enum class NotificationVisibility(val visibility: Int, val visibilityString: String) {
    VISIBILITY_PRIVATE(NotificationCompat.VISIBILITY_PRIVATE, "VISIBILITY_PRIVATE"),
    VISIBILITY_PUBLIC(NotificationCompat.VISIBILITY_PUBLIC, "VISIBILITY_PUBLIC"),
    VISIBILITY_SECRET(NotificationCompat.VISIBILITY_SECRET, "VISIBILITY_SECRET");

    companion object {
        private val notificationVisibilityMap = values().associateBy(NotificationVisibility::visibilityString, NotificationVisibility::visibility)
        internal fun getNotificationCompatVisibilityFromString(visibility: String?): Int {
            return if (visibility == null) NotificationCompat.VISIBILITY_PRIVATE
            else notificationVisibilityMap[visibility] ?: NotificationCompat.VISIBILITY_PRIVATE
        }

        private val notificationCompatVisibilityMap: Map<Int, String> = values().associateBy(NotificationVisibility::visibility, NotificationVisibility::visibilityString)
        @JvmStatic
        fun getNotificationVisibility(visibility: Int?): String {
            return if (visibility == null) VISIBILITY_PRIVATE.visibilityString
            else notificationCompatVisibilityMap[visibility] ?: VISIBILITY_PRIVATE.visibilityString
        }
    }

    override fun toString(): String {
        return visibilityString
    }
}
