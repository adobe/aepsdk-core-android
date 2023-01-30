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

package com.adobe.marketing.mobile;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("all")
@RunWith(AndroidJUnit4.class)
public class DataMarshallerTests {

    static final String LEGACY_PUSH_MESSAGE_ID = "adb_m_id";
    // acquisition
    static final String PUSH_MESSAGE_ID_KEY = "pushmessageid";
    static final String LOCAL_NOTIFICATION_ID_KEY = "notificationid";
    static final String DEEPLINK_KEY = "deeplink";

    static final String ANDROID_UI_SERVICE_NOTIFICATION_IDENTIFIER_KEY = "NOTIFICATION_IDENTIFIER";

    private static final String ADOBE_QUERY_KEYS_PREVIEW_TOKEN = "at_preview_token";
    private static final String ADOBE_QUERY_KEYS_PREVIEW_URL = "at_preview_endpoint";
    private static final String ADOBE_QUERY_KEYS_DEEPLINK_ID = "a.deeplink.id";

    /** Dummy Activity for holding Intent for testing. */
    public static class TestActivity extends Activity {}

    @Rule
    public ActivityTestRule<TestActivity> activityTestRule =
            new ActivityTestRule<TestActivity>(TestActivity.class, false, false);

    @Test
    public void getDataIsNonNull() {
        DataMarshaller marshaller = new DataMarshaller();
        assertNotNull(marshaller.getData());
    }

    @Test
    public void marshalDeepLinkData() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("android-app://deeplink.uri?hello=world&goodnight=moon"));

        Activity activity = activityTestRule.launchActivity(intent);

        DataMarshaller marshaller = new DataMarshaller();
        marshaller.marshal(activity);

        Map<String, Object> result = marshaller.getData();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(DEEPLINK_KEY));
        assertEquals(
                "android-app://deeplink.uri?hello=world&goodnight=moon",
                String.valueOf(result.get(DEEPLINK_KEY)));
    }

    @Test
    public void marshalDeepLinkDataWithDuplicateParameters() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("android-app://deeplink.uri?hello=world&product=xyz&product=abc"));

        Activity activity = activityTestRule.launchActivity(intent);

        DataMarshaller marshaller = new DataMarshaller();
        marshaller.marshal(activity);

        Map<String, Object> result = marshaller.getData();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(DEEPLINK_KEY));
        assertEquals(
                "android-app://deeplink.uri?hello=world&product=xyz&product=abc",
                String.valueOf(result.get(DEEPLINK_KEY)));
    }

    @Test
    public void marshalDeepLinkDataWithAdobeQueryParamsUpdatesIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(
                Uri.parse(
                        "android-app://deeplink.uri?a.deeplink.id=123&hello=world&at_preview_token=a&at_preview_endpoint=b"));

        Activity activity = activityTestRule.launchActivity(intent);

        DataMarshaller marshaller = new DataMarshaller();
        marshaller.marshal(activity);

        Map<String, Object> result = marshaller.getData();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(DEEPLINK_KEY));
        assertEquals(
                "android-app://deeplink.uri?a.deeplink.id=123&hello=world&at_preview_token=a&at_preview_endpoint=b",
                String.valueOf(result.get(DEEPLINK_KEY)));

        // call to marshal updates intent data by filtering out Adobe query parameters
        Intent cleanedIntent = activity.getIntent();
        assertEquals("android-app://deeplink.uri?hello=world", cleanedIntent.getDataString());
    }

    @Test
    public void marshalDeepLinkDataWithAdobeQueryParamsAndDuplicateKeysUpdatesIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(
                Uri.parse(
                        "android-app://deeplink.uri?a.deeplink.id=123&product=xyz&product=abc&at_preview_token=a&at_preview_endpoint=b"));

        Activity activity = activityTestRule.launchActivity(intent);

        DataMarshaller marshaller = new DataMarshaller();
        marshaller.marshal(activity);

        Map<String, Object> result = marshaller.getData();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(DEEPLINK_KEY));
        assertEquals(
                "android-app://deeplink.uri?a.deeplink.id=123&product=xyz&product=abc&at_preview_token=a&at_preview_endpoint=b",
                String.valueOf(result.get(DEEPLINK_KEY)));

        // call to marshal updates intent data by filtering out Adobe query parameters
        Intent cleanedIntent = activity.getIntent();
        assertEquals(
                "android-app://deeplink.uri?product=xyz&product=abc",
                cleanedIntent.getDataString());
    }

    @Test
    public void marshalPushNotification() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(LEGACY_PUSH_MESSAGE_ID, "pushMessage");

        Activity activity = activityTestRule.launchActivity(intent);

        DataMarshaller marshaller = new DataMarshaller();
        marshaller.marshal(activity);

        Map<String, Object> result = marshaller.getData();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(PUSH_MESSAGE_ID_KEY));
        assertEquals("pushMessage", String.valueOf(result.get(PUSH_MESSAGE_ID_KEY)));
    }

    @Test
    public void marshalLocalNotification() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(ANDROID_UI_SERVICE_NOTIFICATION_IDENTIFIER_KEY, "localNotification");

        Activity activity = activityTestRule.launchActivity(intent);

        DataMarshaller marshaller = new DataMarshaller();
        marshaller.marshal(activity);

        Map<String, Object> result = marshaller.getData();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(LOCAL_NOTIFICATION_ID_KEY));
        assertEquals("localNotification", String.valueOf(result.get(LOCAL_NOTIFICATION_ID_KEY)));
    }

    @Test
    public void marshalUnknownKeys() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(Intent.EXTRA_REFERRER, "ptdemo");
        intent.putExtra(Intent.EXTRA_PACKAGE_NAME, "com.adobe.marketing.mobile");

        Activity activity = activityTestRule.launchActivity(intent);

        DataMarshaller marshaller = new DataMarshaller();
        marshaller.marshal(activity);

        Map<String, Object> result = marshaller.getData();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void marshalEmptyKeys() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(""));
        intent.putExtra(ANDROID_UI_SERVICE_NOTIFICATION_IDENTIFIER_KEY, "");
        intent.putExtra(LEGACY_PUSH_MESSAGE_ID, "");

        Activity activity = activityTestRule.launchActivity(intent);

        DataMarshaller marshaller = new DataMarshaller();
        marshaller.marshal(activity);

        Map<String, Object> result = marshaller.getData();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void marshalInvalidUrl_NoCrash() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("abc:abc"));

        Activity activity = activityTestRule.launchActivity(intent);

        DataMarshaller marshaller = new DataMarshaller();
        marshaller.marshal(activity);

        Map<String, Object> result = marshaller.getData();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("abc:abc", String.valueOf(result.get(DEEPLINK_KEY)));
    }
}
