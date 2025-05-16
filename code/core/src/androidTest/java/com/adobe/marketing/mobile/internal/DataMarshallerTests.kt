/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.internal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.io.Serializable

@RunWith(AndroidJUnit4::class)
class DataMarshallerTests {

    /** Dummy Activity for holding Intent for testing.  */
    class TestActivity : Activity()

    // Not switching to ActivityScenario as test failures happen due to
    // https://github.com/android/android-test/issues/676
    @get:Rule
    var activityTestRule: ActivityTestRule<TestActivity> = ActivityTestRule(
        TestActivity::class.java,
        false,
        false
    )

    @Test
    fun marshalDeepLinkData() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), TestActivity::class.java).apply {
                data = Uri.parse("android-app://deeplink.uri?hello=world&goodnight=moon")
            }

        val activity = activityTestRule.launchActivity(intent)
        val result = DataMarshaller.marshal(activity)
        assertEquals(
            mapOf(DEEPLINK_KEY to "android-app://deeplink.uri?hello=world&goodnight=moon"),
            result
        )
    }

    @Test
    fun marshalDeepLinkDataWithDuplicateParameters() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), TestActivity::class.java).apply {
                data = Uri.parse("android-app://deeplink.uri?hello=world&product=xyz&product=abc")
            }

        val activity = activityTestRule.launchActivity(intent)
        val result = DataMarshaller.marshal(activity)
        assertEquals(
            mapOf(DEEPLINK_KEY to "android-app://deeplink.uri?hello=world&product=xyz&product=abc"),
            result
        )
    }

    @Test
    fun marshalDeepLinkDataWithAdobeQueryParamsUpdatesIntent() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), TestActivity::class.java).apply {
                data =
                    Uri.parse("android-app://deeplink.uri?a.deeplink.id=123&hello=world&at_preview_token=a&at_preview_endpoint=b")
            }

        val activity = activityTestRule.launchActivity(intent)
        val result = DataMarshaller.marshal(activity)
        assertEquals(
            mapOf(DEEPLINK_KEY to "android-app://deeplink.uri?a.deeplink.id=123&hello=world&at_preview_token=a&at_preview_endpoint=b"),
            result
        )

        // call to marshal updates intent data by filtering out Adobe query parameters
        assertEquals("android-app://deeplink.uri?hello=world", activity.intent.dataString)
    }

    @Test
    fun marshalDeepLinkDataWithAdobeQueryParamsAndDuplicateKeysUpdatesIntent() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), TestActivity::class.java).apply {
                data =
                    Uri.parse("android-app://deeplink.uri?a.deeplink.id=123&product=xyz&product=abc&at_preview_token=a&at_preview_endpoint=b")
            }

        val activity = activityTestRule.launchActivity(intent)
        val result = DataMarshaller.marshal(activity)
        assertEquals(
            mapOf(DEEPLINK_KEY to "android-app://deeplink.uri?a.deeplink.id=123&product=xyz&product=abc&at_preview_token=a&at_preview_endpoint=b"),
            result
        )

        // call to marshal updates intent data by filtering out Adobe query parameters
        assertEquals(
            "android-app://deeplink.uri?product=xyz&product=abc",
            activity.intent.dataString
        )
    }

    @Test
    fun marshalPushNotification() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), TestActivity::class.java).apply {
                putExtra(LEGACY_PUSH_MESSAGE_ID, "pushMessage")
            }

        val activity = activityTestRule.launchActivity(intent)
        val result = DataMarshaller.marshal(activity)
        assertEquals(
            mapOf(PUSH_MESSAGE_ID_KEY to "pushMessage"),
            result
        )
    }

    @Test
    fun marshalLocalNotification() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), TestActivity::class.java).apply {
                putExtra(ANDROID_UI_SERVICE_NOTIFICATION_IDENTIFIER_KEY, "localNotification")
            }

        val activity = activityTestRule.launchActivity(intent)
        val result = DataMarshaller.marshal(activity)
        assertEquals(
            mapOf(LOCAL_NOTIFICATION_ID_KEY to "localNotification"),
            result
        )
    }

    @Test
    fun marshalUnknownKeys() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), TestActivity::class.java).apply {
                putExtra(Intent.EXTRA_REFERRER, "ptdemo")
                putExtra(Intent.EXTRA_PACKAGE_NAME, "com.adobe.marketing.mobile")
            }

        val activity = activityTestRule.launchActivity(intent)
        val result = DataMarshaller.marshal(activity)
        assertEquals(
            mapOf(
                Intent.EXTRA_REFERRER to "ptdemo",
                Intent.EXTRA_PACKAGE_NAME to "com.adobe.marketing.mobile"
            ),
            result
        )
    }

    @Test
    fun marshalEmptyKeys() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), TestActivity::class.java).apply {
                data = Uri.parse("")
                putExtra(ANDROID_UI_SERVICE_NOTIFICATION_IDENTIFIER_KEY, "")
                putExtra(LEGACY_PUSH_MESSAGE_ID, "")
            }

        val activity = activityTestRule.launchActivity(intent)
        val result = DataMarshaller.marshal(activity)
        assertEquals(
            emptyMap<String, Any>(),
            result
        )
    }

    @Test
    fun marshalInvalidUrl_NoCrash() {
        val throwsException = ObjectThrowsOnToString()
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), TestActivity::class.java).apply {
                putExtra("key", "value")
                putExtra("exceptionKey", throwsException)
            }

        val activity = activityTestRule.launchActivity(intent)
        val result = DataMarshaller.marshal(activity)
        assertEquals(
            mapOf("key" to "value"),
            result
        )
    }

    @Test
    fun marshal_whenBundleThrowException_NoCrash() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), TestActivity::class.java).apply {
                data = Uri.parse("abc:abc")
            }

        val activity = activityTestRule.launchActivity(intent)
        val result = DataMarshaller.marshal(activity)
        assertEquals(
            mapOf(DEEPLINK_KEY to "abc:abc"),
            result
        )
    }

    @Test
    fun marshalIntentExtras_whenBundleKeySetThrowsException_NoCrash() {

        val mockIntent = Mockito.mock(Intent::class.java)
        val mockBundle = Mockito.mock(Bundle::class.java)

        `when`(mockIntent.extras).thenReturn(mockBundle)
        `when`(mockBundle.keySet()).thenThrow(RuntimeException::class.java)

        DataMarshaller.marshalIntentExtras(mockIntent, mutableMapOf())
    }

    @Test
    fun marshalIntentExtras_whenBundleKeySetThrowsException_knownKeysShouldBeProcessed() {

        val mockIntent = Mockito.mock(Intent::class.java)
        val mockBundle = Mockito.mock(Bundle::class.java)

        `when`(mockIntent.extras).thenReturn(mockBundle)
        `when`(mockBundle.getString(LEGACY_PUSH_MESSAGE_ID)).thenReturn("pushMessage")
        `when`(mockBundle.getString(ANDROID_UI_SERVICE_NOTIFICATION_IDENTIFIER_KEY)).thenReturn("notificationId")
        `when`(mockBundle.keySet()).thenThrow(RuntimeException::class.java)
        val result = mutableMapOf<String, Any>()
        DataMarshaller.marshalIntentExtras(mockIntent, result)

        assertEquals(mapOf(PUSH_MESSAGE_ID_KEY to "pushMessage", LOCAL_NOTIFICATION_ID_KEY to "notificationId"), result)
    }

    @Test
    fun marshalIntentExtras_whenBundleRemoveThrowsException_AllKeysShouldBeProcessed() {

        val mockIntent = Mockito.mock(Intent::class.java)
        val mockBundle = Mockito.mock(Bundle::class.java)

        `when`(mockIntent.extras).thenReturn(mockBundle)
        `when`(mockBundle.remove(LEGACY_PUSH_MESSAGE_ID)).thenThrow(RuntimeException::class.java)
        `when`(mockBundle.getString(LEGACY_PUSH_MESSAGE_ID)).thenReturn("pushMessage")
        `when`(mockBundle.getString(ANDROID_UI_SERVICE_NOTIFICATION_IDENTIFIER_KEY)).thenReturn("notificationId")
        `when`(mockBundle.get("otherKey")).thenReturn("value")
        `when`(mockBundle.keySet()).thenReturn(mutableSetOf(LEGACY_PUSH_MESSAGE_ID, ANDROID_UI_SERVICE_NOTIFICATION_IDENTIFIER_KEY, "otherKey"))
        val result = mutableMapOf<String, Any>()
        DataMarshaller.marshalIntentExtras(mockIntent, result)

        assertEquals(mapOf(PUSH_MESSAGE_ID_KEY to "pushMessage", LOCAL_NOTIFICATION_ID_KEY to "notificationId", "otherKey" to "value"), result)
    }

    @Test
    fun marshalIntentExtras_whenBundleGetStringThrowsException_AllKeysShouldBeProcessed() {

        val mockIntent = Mockito.mock(Intent::class.java)
        val mockBundle = Mockito.mock(Bundle::class.java)

        `when`(mockIntent.extras).thenReturn(mockBundle)
        `when`(mockBundle.getString(LEGACY_PUSH_MESSAGE_ID)).thenThrow(RuntimeException::class.java)
        `when`(mockBundle.getString(ANDROID_UI_SERVICE_NOTIFICATION_IDENTIFIER_KEY)).thenReturn("notificationId")
        `when`(mockBundle.get("otherKey")).thenReturn("value")
        `when`(mockBundle.keySet()).thenReturn(mutableSetOf(LEGACY_PUSH_MESSAGE_ID, ANDROID_UI_SERVICE_NOTIFICATION_IDENTIFIER_KEY, "otherKey"))
        val result = mutableMapOf<String, Any>()
        DataMarshaller.marshalIntentExtras(mockIntent, result)

        assertEquals(mapOf(LOCAL_NOTIFICATION_ID_KEY to "notificationId", "otherKey" to "value"), result)
    }

    private class ObjectThrowsOnToString : Serializable {
        override fun toString(): String {
            throw IllegalStateException("This is a test exception")
        }
    }
    companion object {
        const val LEGACY_PUSH_MESSAGE_ID = "adb_m_id"
        const val PUSH_MESSAGE_ID_KEY = "pushmessageid"
        const val ANDROID_UI_SERVICE_NOTIFICATION_IDENTIFIER_KEY = "NOTIFICATION_IDENTIFIER"
        const val LOCAL_NOTIFICATION_ID_KEY = "notificationid"

        const val DEEPLINK_KEY = "deeplink"
    }
}
