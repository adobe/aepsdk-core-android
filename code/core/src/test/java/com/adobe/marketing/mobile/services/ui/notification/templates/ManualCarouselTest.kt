package com.adobe.marketing.mobile.services.ui.notification.templates

import com.adobe.marketing.mobile.services.ui.notification.testutils.MOCKED_BODY
import com.adobe.marketing.mobile.services.ui.notification.testutils.MOCKED_CAROUSEL_LAYOUT_DATA
import com.adobe.marketing.mobile.services.ui.notification.testutils.MOCKED_PAYLOAD_VERSION
import com.adobe.marketing.mobile.services.ui.notification.testutils.MOCKED_TITLE
import com.adobe.marketing.mobile.services.ui.notification.testutils.getMockedBundleWithManualCarouselData
import com.adobe.marketing.mobile.services.ui.notification.testutils.getMockedDataMapWithBasicData
import com.adobe.marketing.mobile.services.ui.notification.testutils.getMockedIntent
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.Test

@RunWith(MockitoJUnitRunner::class)
class ManualCarouselTest {
    @Test
    fun `Test ManualCarouselPushTemplate initialization with Intent`() {
        val mockIntent = getMockedIntent()
        val mockBundle = getMockedBundleWithManualCarouselData()
        `when`(mockIntent.extras).thenReturn(mockBundle)
        val template = ManualCarouselPushTemplate(mockIntent)
        assertEquals(MOCKED_TITLE, template.title)
        assertEquals(MOCKED_BODY, template.body)
        assertEquals(MOCKED_PAYLOAD_VERSION, template.payloadVersion)
        assertEquals("manual", template.carouselLayoutType)
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