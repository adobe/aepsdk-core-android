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

package com.adobe.marketing.mobile.services.ui.notification.templates

import android.content.Intent
import android.os.Bundle
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import kotlin.test.assertFailsWith

@RunWith(MockitoJUnitRunner::class)
class AEPPushTemplateTests {
    private lateinit var aepPushTemplate: AEPPushTemplate
    private lateinit var messageData: HashMap<String, String>
    private lateinit var basicMessageData: HashMap<String, String>

    private lateinit var mockIntent: Intent
    private lateinit var mockBundle: Bundle
    @Before
    fun setUp() {
       // MockitoAnnotations.openMocks(this)
        mockIntent = mock(Intent::class.java)
        setupBundle()
        messageData = hashMapOf(
            PushTemplateConstants.PushPayloadKeys.TAG to "notificationTag",
            PushTemplateConstants.PushPayloadKeys.TEMPLATE_TYPE to "basic",
            PushTemplateConstants.PushPayloadKeys.ACTION_URI to "actionUri",
            PushTemplateConstants.PushPayloadKeys.ACC_PAYLOAD_BODY to "accPayloadBody",
            PushTemplateConstants.PushPayloadKeys.ACTION_TYPE to "actionType",
            PushTemplateConstants.PushPayloadKeys.ACTION_BUTTONS to "[{\"label\":\"Go to chess.com\",\"uri\":\"https://chess.com/games/552\",\"type\":\"DEEPLINK\"},{\"label\":\"Open the app\",\"uri\":\"\",\"type\":\"OPENAPP\"}]",
            PushTemplateConstants.PushPayloadKeys.BADGE_NUMBER to "5",
            PushTemplateConstants.PushPayloadKeys.BODY to "body",
            PushTemplateConstants.PushPayloadKeys.CHANNEL_ID to "channelId",
            PushTemplateConstants.PushPayloadKeys.EXPANDED_BODY_TEXT to "expandedBodyText",
            PushTemplateConstants.PushPayloadKeys.EXPANDED_BODY_TEXT_COLOR to "FFD966",
            PushTemplateConstants.PushPayloadKeys.IMAGE_URL to "imageUrl",
            PushTemplateConstants.PushPayloadKeys.LARGE_ICON to "largeIcon",
            PushTemplateConstants.PushPayloadKeys.NOTIFICATION_BACKGROUND_COLOR to "FFD966",
            PushTemplateConstants.PushPayloadKeys.NOTIFICATION_PRIORITY to "PRIORITY_HIGH",
            PushTemplateConstants.PushPayloadKeys.NOTIFICATION_VISIBILITY to "PUBLIC",
            PushTemplateConstants.PushPayloadKeys.REMIND_LATER_TEXT to "remind me",
            PushTemplateConstants.PushPayloadKeys.REMIND_LATER_EPOCH_TIMESTAMP to "1234567890",
            PushTemplateConstants.PushPayloadKeys.SOUND to "bell",
            PushTemplateConstants.PushPayloadKeys.SMALL_ICON to "notificationIcon",
            PushTemplateConstants.PushPayloadKeys.SMALL_ICON_COLOR to "FFD966",
            PushTemplateConstants.PushPayloadKeys.TITLE to "title",
            PushTemplateConstants.PushPayloadKeys.TITLE_TEXT_COLOR to "FFD966",
            PushTemplateConstants.PushPayloadKeys.TICKER to "ticker",
            PushTemplateConstants.PushPayloadKeys.VERSION to "1",
            PushTemplateConstants.PushPayloadKeys.STICKY to "true"
        )
    }

    private fun setupBundle() {
        mockBundle = mock(Bundle::class.java)
        `when`(mockIntent.extras).thenReturn(mockBundle)
        `when`(mockBundle.getString(PushTemplateConstants.IntentKeys.TITLE_TEXT)).thenReturn("Test Title")
        `when`(mockBundle.getString(PushTemplateConstants.IntentKeys.BODY_TEXT)).thenReturn("Test Body")
        `when`(mockBundle.getInt(PushTemplateConstants.IntentKeys.PAYLOAD_VERSION)).thenReturn(1)
    }

    @Test
    fun `Test exception with missing data adb_title`() {
        val data: MutableMap<String, String> = HashMap()
        data["title"] = "Example Title"
        data["body"] = "This is the body text"
        data["version"] = "1"
        val exception = assertFailsWith<IllegalArgumentException> {
            BasicPushTemplate(data)
        }
        assertEquals("Required field \"adb_title\" not found.", exception.message)
    }

    @Test
    fun `Test BasicPushTemplate initialization with Intent`() {
        val template = BasicPushTemplate(mockIntent)

        assertEquals("Example Title", template.title)
        assertEquals("This is the body text", template.body)
        assertEquals(1, template.payloadVersion)
        assertEquals("Remind Me Later", template.remindLaterText)
    }

//    @Test
//    fun `Test create BasicPushTemplate with complete message data`() {
//        aepPushTemplate = BasicPushTemplate(messageData)
//        assertNotNull(aepPushTemplate)
//        assertEquals("messageId", aepPushTemplate.getMessageId())
//        assertEquals("deliveryId", aepPushTemplate.getDeliveryId())
//        assertEquals("notificationTag", aepPushTemplate.getTag())
//        assertEquals("channelId", aepPushTemplate.getChannelId())
//        assertEquals("title", aepPushTemplate.getTitle())
//        assertEquals("body", aepPushTemplate.getBody())
//        assertEquals("actionUri", aepPushTemplate.getActionUri())
//        assertEquals(
//            "[{\"label\":\"Go to chess.com\",\"uri\":\"https://chess.com/games/552\",\"type\":\"DEEPLINK\"},{\"label\":\"Open the app\",\"uri\":\"\",\"type\":\"OPENAPP\"}]",
//            aepPushTemplate.getActionButtons()
//        )
//        assertEquals("expandedBodyText", aepPushTemplate.getExpandedBodyText())
//        assertEquals("FFD966", aepPushTemplate.getExpandedBodyTextColor())
//        assertEquals("FFD966", aepPushTemplate.getNotificationBackgroundColor())
//        assertEquals("FFD966", aepPushTemplate.getTitleTextColor())
//        assertEquals("notificationIcon", aepPushTemplate.getSmallIcon())
//        assertEquals("FFD966", aepPushTemplate.getSmallIconColor())
//        assertEquals("imageUrl", aepPushTemplate.getImageUrl())
//        assertEquals("largeIcon", aepPushTemplate.getLargeIcon())
//        assertEquals("ticker", aepPushTemplate.getTicker())
//        assertEquals("remind me", aepPushTemplate.getRemindLaterText())
//        assertEquals(1234567890, aepPushTemplate.getRemindLaterTimestamp())
//        assertEquals(5, aepPushTemplate.getBadgeCount())
//        assertEquals(true, aepPushTemplate.getStickyStatus())
//        assertEquals("bell", aepPushTemplate.getSound())
//        // TODO: disabled this assert as Build.VERSION returns 0 in unit tests
// //        assertEquals(
// //            NotificationCompat.PRIORITY_HIGH,
// //            aepPushTemplate.getNotificationImportance()
// //        )
//        assertEquals(
//            NotificationCompat.VISIBILITY_PUBLIC,
//            aepPushTemplate.getNotificationVisibility()
//        )
//    }
//
//    @Test
//    fun `Test create BasicPushTemplate with no small icon key then legacy icon key used`() {
//        messageData.remove(PushTemplateConstants.PushPayloadKeys.SMALL_ICON)
//        messageData[PushTemplateConstants.PushPayloadKeys.LEGACY_SMALL_ICON] = "legacy_icon"
//        aepPushTemplate = BasicPushTemplate(messageData)
//        assertNotNull(aepPushTemplate)
//        assertEquals("messageId", aepPushTemplate.getMessageId())
//        assertEquals("deliveryId", aepPushTemplate.getDeliveryId())
//        assertEquals("notificationTag", aepPushTemplate.getTag())
//        assertEquals("channelId", aepPushTemplate.getChannelId())
//        assertEquals("title", aepPushTemplate.getTitle())
//        assertEquals("body", aepPushTemplate.getBody())
//        assertEquals("actionUri", aepPushTemplate.getActionUri())
//        assertEquals(
//            "[{\"label\":\"Go to chess.com\",\"uri\":\"https://chess.com/games/552\",\"type\":\"DEEPLINK\"},{\"label\":\"Open the app\",\"uri\":\"\",\"type\":\"OPENAPP\"}]",
//            aepPushTemplate.getActionButtons()
//        )
//        assertEquals("expandedBodyText", aepPushTemplate.getExpandedBodyText())
//        assertEquals("FFD966", aepPushTemplate.getExpandedBodyTextColor())
//        assertEquals("FFD966", aepPushTemplate.getNotificationBackgroundColor())
//        assertEquals("FFD966", aepPushTemplate.getTitleTextColor())
//        assertEquals("legacy_icon", aepPushTemplate.getSmallIcon())
//        assertEquals("FFD966", aepPushTemplate.getSmallIconColor())
//        assertEquals("imageUrl", aepPushTemplate.getImageUrl())
//        assertEquals("largeIcon", aepPushTemplate.getLargeIcon())
//        assertEquals("ticker", aepPushTemplate.getTicker())
//        assertEquals("remind me", aepPushTemplate.getRemindLaterText())
//        assertEquals(1234567890, aepPushTemplate.getRemindLaterTimestamp())
//        assertEquals(5, aepPushTemplate.getBadgeCount())
//        assertEquals(true, aepPushTemplate.getStickyStatus())
//        assertEquals("bell", aepPushTemplate.getSound())
//        assertEquals(
//            NotificationCompat.VISIBILITY_PUBLIC,
//            aepPushTemplate.getNotificationVisibility()
//        )
//    }
//
//    @Test
//    fun `Test create BasicPushTemplate with no message id key`() {
//        messageData.remove(PushTemplateConstants.Tracking.Keys.MESSAGE_ID)
//        assertFailsWith<IllegalArgumentException> {
//            BasicPushTemplate(messageData)
//        }
//    }
//
//    @Test
//    fun `Test create BasicPushTemplate with empty message id value`() {
//        messageData[PushTemplateConstants.Tracking.Keys.MESSAGE_ID] = ""
//        assertFailsWith<IllegalArgumentException> {
//            BasicPushTemplate(messageData)
//        }
//    }
//
//    @Test
//    fun `Test create BasicPushTemplate with no delivery id key`() {
//        messageData.remove(PushTemplateConstants.Tracking.Keys.DELIVERY_ID)
//        assertFailsWith<IllegalArgumentException> {
//            BasicPushTemplate(messageData)
//        }
//    }
//
//    @Test
//    fun `Test create BasicPushTemplate with empty delivery id value`() {
//        messageData[PushTemplateConstants.Tracking.Keys.DELIVERY_ID] = ""
//        assertFailsWith<IllegalArgumentException> {
//            BasicPushTemplate(messageData)
//        }
//    }
//
//    @Test
//    fun `Test create BasicPushTemplate with no title key`() {
//        messageData.remove(PushTemplateConstants.PushPayloadKeys.TITLE)
//        assertFailsWith<IllegalArgumentException> {
//            BasicPushTemplate(messageData)
//        }
//    }
//
//    @Test
//    fun `Test create BasicPushTemplate with empty title value`() {
//        messageData[PushTemplateConstants.PushPayloadKeys.TITLE] = ""
//        assertFailsWith<IllegalArgumentException> {
//            BasicPushTemplate(messageData)
//        }
//    }
//
//    @Test
//    fun `Test create BasicPushTemplate with no body or acc body key`() {
//        messageData.remove(PushTemplateConstants.PushPayloadKeys.BODY)
//        messageData.remove(PushTemplateConstants.PushPayloadKeys.ACC_PAYLOAD_BODY)
//        assertFailsWith<IllegalArgumentException> {
//            BasicPushTemplate(messageData)
//        }
//    }
//
//    @Test
//    fun `Test create BasicPushTemplate with no body key then acc body value used`() {
//        messageData.remove(PushTemplateConstants.PushPayloadKeys.BODY)
//        aepPushTemplate = BasicPushTemplate(messageData)
//        assertNotNull(aepPushTemplate)
//        assertEquals("messageId", aepPushTemplate.getMessageId())
//        assertEquals("deliveryId", aepPushTemplate.getDeliveryId())
//        assertEquals("notificationTag", aepPushTemplate.getTag())
//        assertEquals("channelId", aepPushTemplate.getChannelId())
//        assertEquals("title", aepPushTemplate.getTitle())
//        assertEquals("accPayloadBody", aepPushTemplate.getBody())
//        assertEquals("actionUri", aepPushTemplate.getActionUri())
//        assertEquals(
//            "[{\"label\":\"Go to chess.com\",\"uri\":\"https://chess.com/games/552\",\"type\":\"DEEPLINK\"},{\"label\":\"Open the app\",\"uri\":\"\",\"type\":\"OPENAPP\"}]",
//            aepPushTemplate.getActionButtons()
//        )
//        assertEquals("expandedBodyText", aepPushTemplate.getExpandedBodyText())
//        assertEquals("FFD966", aepPushTemplate.getExpandedBodyTextColor())
//        assertEquals("FFD966", aepPushTemplate.getNotificationBackgroundColor())
//        assertEquals("FFD966", aepPushTemplate.getTitleTextColor())
//        assertEquals("notificationIcon", aepPushTemplate.getSmallIcon())
//        assertEquals("FFD966", aepPushTemplate.getSmallIconColor())
//        assertEquals("imageUrl", aepPushTemplate.getImageUrl())
//        assertEquals("largeIcon", aepPushTemplate.getLargeIcon())
//        assertEquals("ticker", aepPushTemplate.getTicker())
//        assertEquals("remind me", aepPushTemplate.getRemindLaterText())
//        assertEquals(1234567890, aepPushTemplate.getRemindLaterTimestamp())
//        assertEquals(5, aepPushTemplate.getBadgeCount())
//        assertEquals(true, aepPushTemplate.getStickyStatus())
//        assertEquals("bell", aepPushTemplate.getSound())
//        assertEquals(
//            NotificationCompat.VISIBILITY_PUBLIC,
//            aepPushTemplate.getNotificationVisibility()
//        )
//    }
//
//    @Test
//    fun `Test create BasicPushTemplate with empty body value then acc body value used`() {
//        messageData[PushTemplateConstants.PushPayloadKeys.BODY] = ""
//        aepPushTemplate = BasicPushTemplate(messageData)
//        assertNotNull(aepPushTemplate)
//        assertEquals("messageId", aepPushTemplate.getMessageId())
//        assertEquals("deliveryId", aepPushTemplate.getDeliveryId())
//        assertEquals("notificationTag", aepPushTemplate.getTag())
//        assertEquals("channelId", aepPushTemplate.getChannelId())
//        assertEquals("title", aepPushTemplate.getTitle())
//        assertEquals("accPayloadBody", aepPushTemplate.getBody())
//        assertEquals("actionUri", aepPushTemplate.getActionUri())
//        assertEquals(
//            "[{\"label\":\"Go to chess.com\",\"uri\":\"https://chess.com/games/552\",\"type\":\"DEEPLINK\"},{\"label\":\"Open the app\",\"uri\":\"\",\"type\":\"OPENAPP\"}]",
//            aepPushTemplate.getActionButtons()
//        )
//        assertEquals("expandedBodyText", aepPushTemplate.getExpandedBodyText())
//        assertEquals("FFD966", aepPushTemplate.getExpandedBodyTextColor())
//        assertEquals("FFD966", aepPushTemplate.getNotificationBackgroundColor())
//        assertEquals("FFD966", aepPushTemplate.getTitleTextColor())
//        assertEquals("notificationIcon", aepPushTemplate.getSmallIcon())
//        assertEquals("FFD966", aepPushTemplate.getSmallIconColor())
//        assertEquals("imageUrl", aepPushTemplate.getImageUrl())
//        assertEquals("largeIcon", aepPushTemplate.getLargeIcon())
//        assertEquals("ticker", aepPushTemplate.getTicker())
//        assertEquals("remind me", aepPushTemplate.getRemindLaterText())
//        assertEquals(1234567890, aepPushTemplate.getRemindLaterTimestamp())
//        assertEquals(5, aepPushTemplate.getBadgeCount())
//        assertEquals(true, aepPushTemplate.getStickyStatus())
//        assertEquals("bell", aepPushTemplate.getSound())
//        assertEquals(
//            NotificationCompat.VISIBILITY_PUBLIC,
//            aepPushTemplate.getNotificationVisibility()
//        )
//    }
//
//    @Test
//    fun `Test create BasicPushTemplate with no version key`() {
//        messageData.remove(PushTemplateConstants.PushPayloadKeys.VERSION)
//        assertFailsWith<IllegalArgumentException> {
//            BasicPushTemplate(messageData)
//        }
//    }
//
//    @Test
//    fun `Test create BasicPushTemplate with empty version value`() {
//        messageData.set(PushTemplateConstants.PushPayloadKeys.VERSION, "")
//        assertFailsWith<IllegalArgumentException> {
//            BasicPushTemplate(messageData)
//        }
//    }
//
//    @Test
//    fun `Test create BasicPushTemplate with invalid badge number then badge number is 0`() {
//        messageData[PushTemplateConstants.PushPayloadKeys.BADGE_NUMBER] = "invalid"
//        aepPushTemplate = BasicPushTemplate(messageData)
//        assertNotNull(aepPushTemplate)
//        assertEquals("messageId", aepPushTemplate.getMessageId())
//        assertEquals("deliveryId", aepPushTemplate.getDeliveryId())
//        assertEquals("notificationTag", aepPushTemplate.getTag())
//        assertEquals("channelId", aepPushTemplate.getChannelId())
//        assertEquals("title", aepPushTemplate.getTitle())
//        assertEquals("body", aepPushTemplate.getBody())
//        assertEquals("actionUri", aepPushTemplate.getActionUri())
//        assertEquals(
//            "[{\"label\":\"Go to chess.com\",\"uri\":\"https://chess.com/games/552\",\"type\":\"DEEPLINK\"},{\"label\":\"Open the app\",\"uri\":\"\",\"type\":\"OPENAPP\"}]",
//            aepPushTemplate.getActionButtons()
//        )
//        assertEquals("expandedBodyText", aepPushTemplate.getExpandedBodyText())
//        assertEquals("FFD966", aepPushTemplate.getExpandedBodyTextColor())
//        assertEquals("FFD966", aepPushTemplate.getNotificationBackgroundColor())
//        assertEquals("FFD966", aepPushTemplate.getTitleTextColor())
//        assertEquals("notificationIcon", aepPushTemplate.getSmallIcon())
//        assertEquals("FFD966", aepPushTemplate.getSmallIconColor())
//        assertEquals("imageUrl", aepPushTemplate.getImageUrl())
//        assertEquals("largeIcon", aepPushTemplate.getLargeIcon())
//        assertEquals("ticker", aepPushTemplate.getTicker())
//        assertEquals("remind me", aepPushTemplate.getRemindLaterText())
//        assertEquals(1234567890, aepPushTemplate.getRemindLaterTimestamp())
//        assertEquals(0, aepPushTemplate.getBadgeCount())
//        assertEquals(true, aepPushTemplate.getStickyStatus())
//        assertEquals("bell", aepPushTemplate.getSound())
//        assertEquals(
//            NotificationCompat.VISIBILITY_PUBLIC,
//            aepPushTemplate.getNotificationVisibility()
//        )
//    }
//
//    @Test
//    fun `Test get action buttons from BasicPushTemplate with complete message data`() {
//        aepPushTemplate = BasicPushTemplate(messageData)
//        assertNotNull(aepPushTemplate)
//        assertEquals("messageId", aepPushTemplate.getMessageId())
//        assertEquals("deliveryId", aepPushTemplate.getDeliveryId())
//        assertEquals("notificationTag", aepPushTemplate.getTag())
//        assertEquals("channelId", aepPushTemplate.getChannelId())
//        assertEquals("title", aepPushTemplate.getTitle())
//        assertEquals("body", aepPushTemplate.getBody())
//        assertEquals("actionUri", aepPushTemplate.getActionUri())
//        assertEquals(
//            "[{\"label\":\"Go to chess.com\",\"uri\":\"https://chess.com/games/552\",\"type\":\"DEEPLINK\"},{\"label\":\"Open the app\",\"uri\":\"\",\"type\":\"OPENAPP\"}]",
//            aepPushTemplate.getActionButtons()
//        )
//        assertEquals("expandedBodyText", aepPushTemplate.getExpandedBodyText())
//        assertEquals("FFD966", aepPushTemplate.getExpandedBodyTextColor())
//        assertEquals("FFD966", aepPushTemplate.getNotificationBackgroundColor())
//        assertEquals("FFD966", aepPushTemplate.getTitleTextColor())
//        assertEquals("notificationIcon", aepPushTemplate.getSmallIcon())
//        assertEquals("FFD966", aepPushTemplate.getSmallIconColor())
//        assertEquals("imageUrl", aepPushTemplate.getImageUrl())
//        assertEquals("largeIcon", aepPushTemplate.getLargeIcon())
//        assertEquals("ticker", aepPushTemplate.getTicker())
//        assertEquals("remind me", aepPushTemplate.getRemindLaterText())
//        assertEquals(1234567890, aepPushTemplate.getRemindLaterTimestamp())
//        assertEquals(5, aepPushTemplate.getBadgeCount())
//        assertEquals(true, aepPushTemplate.getStickyStatus())
//        assertEquals("bell", aepPushTemplate.getSound())
//        // TODO: disabled this assert as Build.VERSION returns 0 in unit tests
// //        assertEquals(
// //            NotificationCompat.PRIORITY_HIGH,
// //            aepPushTemplate.getNotificationImportance()
// //        )
//        assertEquals(
//            NotificationCompat.VISIBILITY_PUBLIC,
//            aepPushTemplate.getNotificationVisibility()
//        )
//    }
}
