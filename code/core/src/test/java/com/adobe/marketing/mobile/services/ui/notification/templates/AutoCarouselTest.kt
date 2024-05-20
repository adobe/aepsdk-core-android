package com.adobe.marketing.mobile.services.ui.notification.templates

import com.adobe.marketing.mobile.services.ui.notification.testutils.MOCKED_BODY
import com.adobe.marketing.mobile.services.ui.notification.testutils.MOCKED_CAROUSEL_LAYOUT_DATA
import com.adobe.marketing.mobile.services.ui.notification.testutils.MOCKED_PAYLOAD_VERSION
import com.adobe.marketing.mobile.services.ui.notification.testutils.MOCKED_TITLE
import com.adobe.marketing.mobile.services.ui.notification.testutils.getMockedBundleWithAutoCarouselData
import com.adobe.marketing.mobile.services.ui.notification.testutils.getMockedBundleWithManualCarouselData
import com.adobe.marketing.mobile.services.ui.notification.testutils.getMockedIntent
import com.adobe.marketing.mobile.services.ui.notification.testutils.getMockedMapWithAutoCarouselData
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(MockitoJUnitRunner::class)
class AutoCarouselTest {

    @Test
    fun `Test AutoCarouselPushTemplate initialization with Intent`() {
        val mockIntent = getMockedIntent()
        val mockBundle = getMockedBundleWithAutoCarouselData()
        `when`(mockIntent.extras).thenReturn(mockBundle)
        val template = AutoCarouselPushTemplate(mockIntent)
        assertEquals(MOCKED_TITLE, template.title)
        assertEquals(MOCKED_BODY, template.body)
        assertEquals(MOCKED_PAYLOAD_VERSION, template.payloadVersion)
        assertEquals("auto", template.carouselLayoutType)
        assertEquals("#FFFFFF", template.expandedBodyTextColor)
        assertEquals("small_icon", template.smallIcon)
        assertEquals("large_icon", template.largeIcon)
        assertEquals("#000000", template.smallIconColor)
        assertEquals("ticker", template.ticker)
        assertEquals("tag", template.tag)
        assertEquals(true, template.isNotificationSticky)
        assertEquals("action_uri", template.actionUri)
        assertEquals(MOCKED_CAROUSEL_LAYOUT_DATA, template.rawCarouselItems)
    }

    @Test
    fun `Test AutoCarouselPushTemplate initialization with Map`() {
        val map = getMockedMapWithAutoCarouselData()
        val template = AutoCarouselPushTemplate(map)
        assertEquals(MOCKED_TITLE, template.title)
        assertEquals(MOCKED_BODY, template.body)
        assertEquals(MOCKED_PAYLOAD_VERSION, template.payloadVersion)
        assertEquals("auto", template.carouselLayoutType)
        assertEquals("#FFFFFF", template.expandedBodyTextColor)
        assertEquals("small_icon", template.smallIcon)
        assertEquals("large_icon", template.largeIcon)
        assertEquals("#000000", template.smallIconColor)
        assertEquals("ticker", template.ticker)
        assertEquals("tag", template.tag)
        assertEquals(true, template.isNotificationSticky)
        assertEquals("action_uri", template.actionUri)
        assertEquals(MOCKED_CAROUSEL_LAYOUT_DATA, template.rawCarouselItems)
    }
}