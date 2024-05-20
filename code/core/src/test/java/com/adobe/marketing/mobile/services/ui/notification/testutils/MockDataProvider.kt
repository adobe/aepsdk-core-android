package com.adobe.marketing.mobile.services.ui.notification.testutils

import android.content.Intent
import android.os.Bundle
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants
import org.mockito.Mockito
import org.mockito.kotlin.mock

const val MOCKED_TITLE = "Mocked Title"
const val MOCKED_BODY = "Mocked Body"
const val MOCKED_PAYLOAD_VERSION = 1
const val MOCKED_CAROUSEL_LAYOUT_DATA = "[{\"img\":\"https://i.imgur.com/7ZolaOv.jpeg\",\"txt\":\"Basketball Shoes\"},{\"img\":\"https://i.imgur.com/mZvLuzU.jpg\",\"txt\":\"Red Jersey\",\"uri\":\"https://firefly.adobe.com/red_jersey\"},{\"img\":\"https://i.imgur.com/X5yjy09.jpg\",\"txt\":\"Volleyball\", \"uri\":\"https://firefly.adobe.com/volleyball\"},{\"img\":\"https://i.imgur.com/35B0mkh.jpg\",\"txt\":\"Basketball\",\"uri\":\"https://firefly.adobe.com/basketball\"},{\"img\":\"https://i.imgur.com/Cs5hmfb.jpg\",\"txt\":\"Black Batting Helmet\",\"uri\":\"https://firefly.adobe.com/black_helmet\"}]"

/**
 * Returns a mocked data map with basic data.
 */
fun getMockedDataMapWithBasicData(): HashMap<String, String> {
    return hashMapOf(
        Pair(PushTemplateConstants.PushPayloadKeys.TITLE, MOCKED_TITLE),
        Pair(PushTemplateConstants.PushPayloadKeys.BODY, MOCKED_BODY),
        Pair(PushTemplateConstants.PushPayloadKeys.VERSION, MOCKED_PAYLOAD_VERSION.toString())
    )
}
/**
 * Returns a mocked data bundle with basic data.
 */
fun getMockedBundleWithMinimalData(): Bundle {
    val mockBundle = mock<Bundle>()
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.TITLE_TEXT))
        .thenReturn(MOCKED_TITLE)
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.BODY_TEXT))
        .thenReturn(MOCKED_BODY)
    Mockito.`when`(mockBundle.getInt(PushTemplateConstants.IntentKeys.PAYLOAD_VERSION))
        .thenReturn(MOCKED_PAYLOAD_VERSION)
    return mockBundle
}

fun getMockedBundleWithoutTitle(): Bundle {
    val mockBundle = mock<Bundle>()
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.BODY_TEXT))
        .thenReturn(MOCKED_BODY)
    Mockito.`when`(mockBundle.getInt(PushTemplateConstants.IntentKeys.PAYLOAD_VERSION))
        .thenReturn(MOCKED_PAYLOAD_VERSION)
    return mockBundle
}

fun getMockedBundleWithoutBody(): Bundle {
    val mockBundle = mock<Bundle>()
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.TITLE_TEXT))
        .thenReturn(MOCKED_TITLE)
    Mockito.`when`(mockBundle.getInt(PushTemplateConstants.IntentKeys.PAYLOAD_VERSION))
        .thenReturn(MOCKED_PAYLOAD_VERSION)
    return mockBundle
}

fun getMockedIntent(): Intent {
    val mockIntent = mock<Intent>()
    return mockIntent
}


fun getMockedMapWithAutoCarouselData(): Map<String, String> {
    return mapOf(
        PushTemplateConstants.PushPayloadKeys.TITLE to MOCKED_TITLE,
        PushTemplateConstants.PushPayloadKeys.BODY to MOCKED_BODY,
        PushTemplateConstants.PushPayloadKeys.VERSION to MOCKED_PAYLOAD_VERSION.toString(),
        PushTemplateConstants.PushPayloadKeys.CAROUSEL_LAYOUT to "auto",
        PushTemplateConstants.PushPayloadKeys.EXPANDED_BODY_TEXT_COLOR to "#FFFFFF",
        PushTemplateConstants.PushPayloadKeys.SMALL_ICON to "small_icon",
        PushTemplateConstants.PushPayloadKeys.LARGE_ICON to "large_icon",
        PushTemplateConstants.PushPayloadKeys.SMALL_ICON_COLOR to "#000000",
        PushTemplateConstants.PushPayloadKeys.TICKER to "ticker",
        PushTemplateConstants.PushPayloadKeys.TAG to "tag",
        PushTemplateConstants.PushPayloadKeys.STICKY to "true",
        PushTemplateConstants.PushPayloadKeys.ACTION_URI to "action_uri",
        PushTemplateConstants.PushPayloadKeys.CAROUSEL_ITEMS to MOCKED_CAROUSEL_LAYOUT_DATA
    )
}

fun getMockedMapWithManualCarouselData(): Map<String, String> {
    return mapOf(
        PushTemplateConstants.PushPayloadKeys.TITLE to MOCKED_TITLE,
        PushTemplateConstants.PushPayloadKeys.BODY to MOCKED_BODY,
        PushTemplateConstants.PushPayloadKeys.VERSION to MOCKED_PAYLOAD_VERSION.toString(),
        PushTemplateConstants.PushPayloadKeys.CAROUSEL_LAYOUT to "manual",
        PushTemplateConstants.PushPayloadKeys.EXPANDED_BODY_TEXT_COLOR to "#FFFFFF",
        PushTemplateConstants.PushPayloadKeys.SMALL_ICON to "small_icon",
        PushTemplateConstants.PushPayloadKeys.LARGE_ICON to "large_icon",
        PushTemplateConstants.PushPayloadKeys.SMALL_ICON_COLOR to "#000000",
        PushTemplateConstants.PushPayloadKeys.TICKER to "ticker",
        PushTemplateConstants.PushPayloadKeys.TAG to "tag",
        PushTemplateConstants.PushPayloadKeys.STICKY to "true",
        PushTemplateConstants.PushPayloadKeys.ACTION_URI to "action_uri",
        //TODO : Add carousel items
        PushTemplateConstants.PushPayloadKeys.CAROUSEL_ITEMS to MOCKED_CAROUSEL_LAYOUT_DATA
    )
}
fun getMockedBundleWithManualCarouselData(): Bundle {
    val mockBundle = mock<Bundle>()
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.TITLE_TEXT))
        .thenReturn(MOCKED_TITLE)
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.BODY_TEXT))
        .thenReturn(MOCKED_BODY)
    Mockito.`when`(mockBundle.getInt(PushTemplateConstants.IntentKeys.PAYLOAD_VERSION))
        .thenReturn(MOCKED_PAYLOAD_VERSION)
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.CAROUSEL_LAYOUT_TYPE))
        .thenReturn("manual")
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT_COLOR))
        .thenReturn("#FFFFFF")
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.SMALL_ICON))
        .thenReturn("small_icon")
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.LARGE_ICON))
        .thenReturn("large_icon")
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR))
        .thenReturn("#000000")
    Mockito.`when`(mockBundle.getInt(PushTemplateConstants.IntentKeys.VISIBILITY))
        .thenReturn(1)
    Mockito.`when`(mockBundle.getInt(PushTemplateConstants.IntentKeys.IMPORTANCE))
        .thenReturn(1)
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.TICKER))
        .thenReturn("ticker")
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.TAG))
        .thenReturn("tag")
    Mockito.`when`(mockBundle.getBoolean(PushTemplateConstants.IntentKeys.STICKY))
        .thenReturn(true)
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.ACTION_URI))
        .thenReturn("action_uri")
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.CAROUSEL_ITEMS))
        .thenReturn(MOCKED_CAROUSEL_LAYOUT_DATA)
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.CAROUSEL_OPERATION_MODE))
        .thenReturn("manual")
    return mockBundle
}

fun getMockedBundleWithAutoCarouselData(): Bundle {
    val mockBundle = mock<Bundle>()
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.TITLE_TEXT))
        .thenReturn(MOCKED_TITLE)
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.BODY_TEXT))
        .thenReturn(MOCKED_BODY)
    Mockito.`when`(mockBundle.getInt(PushTemplateConstants.IntentKeys.PAYLOAD_VERSION))
        .thenReturn(MOCKED_PAYLOAD_VERSION)
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.CAROUSEL_LAYOUT_TYPE))
        .thenReturn("auto")
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT_COLOR))
        .thenReturn("#FFFFFF")
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.SMALL_ICON))
        .thenReturn("small_icon")
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.LARGE_ICON))
        .thenReturn("large_icon")
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR))
        .thenReturn("#000000")
    Mockito.`when`(mockBundle.getInt(PushTemplateConstants.IntentKeys.VISIBILITY))
        .thenReturn(1)
    Mockito.`when`(mockBundle.getInt(PushTemplateConstants.IntentKeys.IMPORTANCE))
        .thenReturn(1)
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.TICKER))
        .thenReturn("ticker")
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.TAG))
        .thenReturn("tag")
    Mockito.`when`(mockBundle.getBoolean(PushTemplateConstants.IntentKeys.STICKY))
        .thenReturn(true)
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.ACTION_URI))
        .thenReturn("action_uri")
    Mockito.`when`(mockBundle.getString(PushTemplateConstants.IntentKeys.CAROUSEL_ITEMS))
        .thenReturn(MOCKED_CAROUSEL_LAYOUT_DATA)
    return mockBundle
}