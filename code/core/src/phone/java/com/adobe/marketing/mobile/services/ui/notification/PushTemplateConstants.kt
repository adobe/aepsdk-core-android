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

import java.util.concurrent.TimeUnit

/**
 * This object holds all constant values for handling out-of-the-box push template notifications
 */
internal object PushTemplateConstants {
    const val LOG_TAG = "AEPSDKPushTemplates"
    const val CACHE_BASE_DIR = "pushtemplates"
    const val PUSH_IMAGE_CACHE = "pushimagecache"
    const val DEFAULT_CHANNEL_ID = "AEPSDKPushChannel"

    // When no channel name is received from the push notification, this default channel name is
    // used.
    // This will appear in the notification settings for the app.
    const val DEFAULT_CHANNEL_NAME = "AEPSDK Push Notifications"
    const val SILENT_CHANNEL_NAME = "AEPSDK Silent Push Notifications"

    /** Enum to denote the type of action  */
    enum class ActionType {
        DEEPLINK, WEBURL, DISMISS, OPENAPP, NONE
    }

    internal object ActionButtons {
        const val LABEL = "label"
        const val URI = "uri"
        const val TYPE = "type"
    }

    internal object NotificationAction {
        const val DISMISSED = "Notification Dismissed"
        const val OPENED = "Notification Opened"
        const val BUTTON_CLICKED = "Notification Button Clicked"
    }

    internal class Tracking private constructor() {
        internal object TrackingKeys {
            const val ACTION_ID = "actionId"
            const val ACTION_URI = "actionUri"
        }
    }

    internal object DefaultValues {
        const val SILENT_NOTIFICATION_CHANNEL_ID = "AEPSDK Silent Push Notifications"
        const val CAROUSEL_MAX_BITMAP_WIDTH = 300
        const val CAROUSEL_MAX_BITMAP_HEIGHT = 200
        const val AUTO_CAROUSEL_MODE = "auto"
        const val DEFAULT_MANUAL_CAROUSEL_MODE = "default"
        const val FILMSTRIP_CAROUSEL_MODE = "filmstrip"
        const val CAROUSEL_MINIMUM_IMAGE_COUNT = 3
        const val MANUAL_CAROUSEL_START_INDEX = 0
        const val FILMSTRIP_CAROUSEL_CENTER_INDEX = 1
        const val NO_CENTER_INDEX_SET = -1

        // TODO: revisit this value. should cache time be configurable rather than have a static
        // value?
        val PUSH_NOTIFICATION_IMAGE_CACHE_EXPIRY_IN_MILLISECONDS: Long =
            TimeUnit.DAYS.toMillis(3) // 3 days
    }

    internal object IntentActions {
        const val FILMSTRIP_LEFT_CLICKED = "filmstrip_left"
        const val FILMSTRIP_RIGHT_CLICKED = "filmstrip_right"
        const val REMIND_LATER_CLICKED = "remind_clicked"
        const val MANUAL_CAROUSEL_LEFT_CLICKED = "manual_left"
        const val MANUAL_CAROUSEL_RIGHT_CLICKED = "manual_right"
        const val INPUT_RECEIVED = "input_received"
    }

    internal object IntentKeys {
        const val CENTER_IMAGE_INDEX = "centerImageIndex"
        const val IMAGE_URI = "imageUri"
        const val IMAGE_URLS = "imageUrls"
        const val IMAGE_CAPTIONS = "imageCaptions"
        const val IMAGE_CLICK_ACTIONS = "imageClickActions"
        const val ACTION_URI = "actionUri"
        const val ACTION_TYPE = "actionType"
        const val CHANNEL_ID = "channelId"
        const val CUSTOM_SOUND = "customSound"
        const val TITLE_TEXT = "titleText"
        const val BODY_TEXT = "bodyText"
        const val EXPANDED_BODY_TEXT = "expandedBodyText"
        const val NOTIFICATION_BACKGROUND_COLOR = "notificationBackgroundColor"
        const val TITLE_TEXT_COLOR = "titleTextColor"
        const val EXPANDED_BODY_TEXT_COLOR = "expandedBodyTextColor"
        const val BADGE_COUNT = "badgeCount"
        const val LARGE_ICON = "largeIcon"
        const val SMALL_ICON = "smallIcon"
        const val SMALL_ICON_COLOR = "smallIconColor"
        const val PRIORITY = "priority"
        const val VISIBILITY = "visibility"
        const val IMPORTANCE = "importance"
        const val REMIND_DELAY_SECONDS = "remindDelaySeconds"
        const val REMIND_EPOCH_TS = "remindEpochTimestamp"
        const val REMIND_LABEL = "remindLaterLabel"
        const val ACTION_BUTTONS_STRING = "actionButtonsString"
        const val STICKY = "sticky"
        const val TAG = "tag"
        const val TICKER = "ticker"
        const val PAYLOAD_VERSION = "version"
        const val TEMPLATE_TYPE = "templateType"
        const val CAROUSEL_OPERATION_MODE = "carouselOperationMode"
        const val CAROUSEL_LAYOUT_TYPE = "carouselLayoutType"
        const val CAROUSEL_ITEMS = "carouselItems"
        const val INPUT_BOX_HINT = "inputBoxHint"
        const val INPUT_BOX_FEEDBACK_TEXT = "feedbackText"
        const val INPUT_BOX_FEEDBACK_IMAGE = "feedbackImage"
        const val INPUT_BOX_RECEIVER_NAME = "feedbackReceiverName"
    }

    internal object MethodNames {
        const val SET_BACKGROUND_COLOR = "setBackgroundColor"
        const val SET_TEXT_COLOR = "setTextColor"
    }

    internal object FriendlyViewNames {
        const val NOTIFICATION_BACKGROUND = "notification background"
        const val NOTIFICATION_TITLE = "notification title"
        const val NOTIFICATION_BODY_TEXT = "notification body text"
    }

    internal object PushPayloadKeys {
        const val TEMPLATE_TYPE = "adb_template_type"
        const val TITLE = "adb_title"
        const val BODY = "adb_body"
        const val ACC_PAYLOAD_BODY = "_msg"
        const val SOUND = "adb_sound"
        const val BADGE_NUMBER = "adb_n_count"
        const val NOTIFICATION_VISIBILITY = "adb_n_visibility"
        const val NOTIFICATION_PRIORITY = "adb_n_priority"
        const val CHANNEL_ID = "adb_channel_id"
        const val LEGACY_SMALL_ICON = "adb_icon"
        const val SMALL_ICON = "adb_small_icon"
        const val LARGE_ICON = "adb_large_icon"
        const val IMAGE_URL = "adb_image"
        const val TAG = "adb_tag"
        const val TICKER = "adb_ticker"
        const val STICKY = "adb_sticky"
        const val ACTION_TYPE = "adb_a_type"
        const val ACTION_URI = "adb_uri"
        const val ACTION_BUTTONS = "adb_act"
        const val VERSION = "adb_version"
        const val CAROUSEL_LAYOUT = "adb_car_layout"
        const val CAROUSEL_ITEMS = "adb_items"
        const val CAROUSEL_ITEM_IMAGE = "img"
        const val CAROUSEL_ITEM_TEXT = "txt"
        const val CAROUSEL_ITEM_URI = "uri"
        const val EXPANDED_BODY_TEXT = "adb_body_ex"
        const val EXPANDED_BODY_TEXT_COLOR = "adb_clr_body"
        const val TITLE_TEXT_COLOR = "adb_clr_title"
        const val SMALL_ICON_COLOR = "adb_clr_icon"
        const val NOTIFICATION_BACKGROUND_COLOR = "adb_clr_bg"
        const val REMIND_LATER_TEXT = "adb_rem_txt"
        const val REMIND_LATER_EPOCH_TIMESTAMP = "adb_rem_ts"
        const val REMIND_LATER_DELAY_SECONDS = "adb_rem_sec"
        const val CAROUSEL_OPERATION_MODE = "adb_car_mode"
        const val INPUT_BOX_HINT = "adb_input_txt"
        const val INPUT_BOX_FEEDBACK_TEXT = "adb_feedback_txt"
        const val INPUT_BOX_FEEDBACK_IMAGE = "adb_feedback_img"
        const val INPUT_BOX_RECEIVER_NAME = "adb_input_receiver"
    }

    internal object CarouselItemKeys {
        const val IMAGE = "img"
        const val TEXT = "txt"
        const val URL = "uri"
    }
}
