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

enum class NotificationPriority(private val priority: Int, private val priorityString: String) {
    PRIORITY_DEFAULT(NotificationCompat.PRIORITY_DEFAULT, "PRIORITY_DEFAULT"),
    PRIORITY_MIN(NotificationCompat.PRIORITY_MIN, "PRIORITY_MIN"),
    PRIORITY_LOW(NotificationCompat.PRIORITY_LOW, "PRIORITY_LOW"),
    PRIORITY_HIGH(NotificationCompat.PRIORITY_HIGH, "PRIORITY_HIGH"),
    PRIORITY_MAX(NotificationCompat.PRIORITY_MAX, "PRIORITY_MAX");

    companion object {
        private val notificationPriorityMap = values().associateBy(NotificationPriority::priorityString, NotificationPriority::priority)

        /**
         * Returns the [NotificationCompat] priority value represented by the [String].
         *
         * @param priority [String] representation of the [NotificationCompat] priority
         * @return [Int] containing the [NotificationCompat] priority value
         */
        internal fun getNotificationCompatPriorityFromString(priority: String?): Int {
            return if (priority == null) NotificationCompat.PRIORITY_DEFAULT
            else notificationPriorityMap[priority] ?: NotificationCompat.PRIORITY_DEFAULT
        }

        private val notificationCompatPriorityMap: Map<Int, String> = values().associateBy(NotificationPriority::priority, NotificationPriority::priorityString)

        /**
         * Returns the [String] representation for the [NotificationCompat] priority value.
         *
         * @param priority [Int] containing the [NotificationCompat] priority value
         * @return [String] representation of the [NotificationCompat] priority
         */
        @JvmStatic
        fun getNotificationPriority(priority: Int?): String {
            return if (priority == null) PRIORITY_DEFAULT.priorityString
            else notificationCompatPriorityMap[priority] ?: PRIORITY_DEFAULT.priorityString
        }
    }

    override fun toString(): String {
        return priorityString
    }
}
