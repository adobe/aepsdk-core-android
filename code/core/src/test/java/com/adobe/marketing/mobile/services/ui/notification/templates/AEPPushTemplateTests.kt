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
import com.adobe.marketing.mobile.services.ui.notification.testutils.MOCKED_BODY
import com.adobe.marketing.mobile.services.ui.notification.testutils.MOCKED_PAYLOAD_VERSION
import com.adobe.marketing.mobile.services.ui.notification.testutils.MOCKED_TITLE
import com.adobe.marketing.mobile.services.ui.notification.testutils.getMockedBundleWithMinimalData
import com.adobe.marketing.mobile.services.ui.notification.testutils.getMockedBundleWithoutBody
import com.adobe.marketing.mobile.services.ui.notification.testutils.getMockedBundleWithoutTitle
import com.adobe.marketing.mobile.services.ui.notification.testutils.getMockedDataMapWithBasicData
import com.adobe.marketing.mobile.services.ui.notification.testutils.getMockedIntent
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule
import org.mockito.quality.Strictness
import kotlin.test.assertFailsWith


@RunWith(MockitoJUnitRunner.Silent::class)
class AEPPushTemplateTests {

    @Rule
    @JvmField
    val rule: MockitoRule = MockitoJUnit.rule().strictness(Strictness.LENIENT)

    private lateinit var aepPushTemplate: AEPPushTemplate
    private lateinit var basicMessageData: HashMap<String, String>

    private lateinit var mockIntent: Intent
    private lateinit var mockBundle: Bundle

    @Before
    fun setUp() {
       // MockitoAnnotations.openMocks(this)
    }

    /**
     * Test Suit for testing data initialization using Map
     */

    @Test
    fun `Test BasicPushTemplate initialization with Map`() {
        basicMessageData = getMockedDataMapWithBasicData()
        aepPushTemplate = BasicPushTemplate(basicMessageData)
        assertEquals(MOCKED_TITLE, aepPushTemplate.title)
        assertEquals(MOCKED_BODY, aepPushTemplate.body)
        assertEquals(MOCKED_PAYLOAD_VERSION, aepPushTemplate.payloadVersion)
    }

    @Test
    fun `Test exception with missing data adb_title`() {
        basicMessageData = getMockedDataMapWithBasicData()
        basicMessageData.remove(PushTemplateConstants.PushPayloadKeys.TITLE)
        val exception = assertFailsWith<IllegalArgumentException> {
            BasicPushTemplate(basicMessageData)
        }
        assertEquals("Required field \"${PushTemplateConstants.PushPayloadKeys.TITLE}\" not found.", exception.message)
    }

    @Test
    fun `Test exception with missing data adb_body`() {
        basicMessageData = getMockedDataMapWithBasicData()
        basicMessageData.remove(PushTemplateConstants.PushPayloadKeys.BODY)
        val exception = assertFailsWith<IllegalArgumentException> {
            BasicPushTemplate(basicMessageData)
        }
        assertEquals("Required field \"${PushTemplateConstants.PushPayloadKeys.BODY}\" not found.", exception.message)
    }

    /**
     * Test Suit for testing data initialization using Intent
     */

    @Test
    fun `Test BasicPushTemplate initialization with Intent`() {
        mockIntent = getMockedIntent()
        mockBundle = getMockedBundleWithMinimalData()
        `when`(mockIntent.extras).thenReturn(mockBundle)
        val template = BasicPushTemplate(mockIntent)
        assertEquals(MOCKED_TITLE, template.title)
        assertEquals(MOCKED_BODY, template.body)
        assertEquals(MOCKED_PAYLOAD_VERSION, template.payloadVersion)
    }

    @Test
    fun `Test BasicPushTemplate initialization with Intent without title`() {
        mockIntent = getMockedIntent()
        mockBundle = getMockedBundleWithoutTitle()
        `when`(mockIntent.extras).thenReturn(mockBundle)
        val exception = assertFailsWith<IllegalArgumentException> {
            BasicPushTemplate(mockIntent)
        }
        assertEquals(
            "Required field \"${PushTemplateConstants.IntentKeys.TITLE_TEXT}\" not found.",
            exception.message
        )
    }

    @Test
    fun `Test BasicPushTemplate initialization with Intent without body`() {
        mockIntent = getMockedIntent()
        mockBundle = getMockedBundleWithoutBody()
        `when`(mockIntent.extras).thenReturn(mockBundle)
        val exception = assertFailsWith<IllegalArgumentException> {
            BasicPushTemplate(mockIntent)
        }
        assertEquals(
            "Required field \"${PushTemplateConstants.IntentKeys.BODY_TEXT}\" not found.",
            exception.message
        )
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
