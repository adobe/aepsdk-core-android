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

package com.adobe.testapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagKey;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static androidx.test.espresso.web.webdriver.DriverAtoms.findElement;
import static androidx.test.espresso.web.webdriver.DriverAtoms.getText;

import android.content.Context;
import android.content.Intent;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.web.webdriver.Locator;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

import com.adobe.marketing.mobile.TestAppUIServices;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class UIServicesInstrumentedTest {

	@Rule
	public final IntentsTestRule<MainActivity> mActivityRule = new IntentsTestRule<>(MainActivity.class);
	private final UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
	private static final int TIMEOUT = 5000;

	@Before
	public void setup() {
		onView(withText(R.string.tab_ui_services))
		.perform(click());
	}

	@Test
	public void testShowAlert() {
		onView(withId(R.id.btnShowAlert))
		.perform(click());

		int titleId = mActivityRule.getActivity().getResources()
					  .getIdentifier("alertTitle", "id", "android");

		onView(withId(titleId))
		.inRoot(isDialog())
		.check(matches(withText(R.string.test_alert_title)))
		.check(matches(isDisplayed()));

		onView(withId(android.R.id.message))
		.inRoot(isDialog())
		.check(matches(withText(R.string.test_alert_message)))
		.check(matches(isDisplayed()));

		onView(withId(android.R.id.button2))
		.inRoot(isDialog())
		.check(matches(withText(R.string.test_alert_negative_text)))
		.check(matches(isDisplayed()));

		onView(withId(android.R.id.button1))
		.inRoot(isDialog())
		.check(matches(withText(R.string.test_alert_positive_text)))
		.check(matches(isDisplayed()));

		onView(withId(android.R.id.button1))
		.perform(click());

		onView(withText(titleId))
		.check(doesNotExist());
	}

	@Test
	public void testShowLocalNotification() {
		String expectedAppName = mActivityRule.getActivity().getString(R.string.app_name);

		onView(withId(R.id.btnShowLocalNotification))
		.perform(click());

		uiDevice.openNotification();
		uiDevice.wait(Until.hasObject(By.text(expectedAppName)), TIMEOUT);
		UiObject2 notificationTitle = uiDevice.findObject(By.text(expectedAppName));
		assertEquals(notificationTitle.getText(), expectedAppName);

		UiObject2 notificationContent = uiDevice.findObject(By.text(getResourceString(R.string.test_notification_content)));
		assertEquals(notificationContent.getText(), getResourceString(R.string.test_notification_content));

		tearDown();
	}

	@Test
	public void testShowLocalNotificationWithTitle() {
		String expectedAppName = mActivityRule.getActivity().getString(R.string.app_name);

		onView(withId(R.id.btnShowLocalNotificationWithTitle))
		.perform(click());

		uiDevice.openNotification();
		uiDevice.wait(Until.hasObject(By.text(expectedAppName)), TIMEOUT);
		UiObject2 notificationTitle = uiDevice.findObject(By.text(getResourceString(R.string.test_notification_title)));
		assertEquals(notificationTitle.getText(), getResourceString(R.string.test_notification_title));

		UiObject2 notificationContent = uiDevice.findObject(By.text(getResourceString(R.string.test_notification_content)));
		assertEquals(notificationContent.getText(), getResourceString(R.string.test_notification_content));

		tearDown();
	}

	@Test
	public void testShowWebView() {
		onView(withId(R.id.btnShowFullScreenMsg))
		.perform(click());

		onWebView().forceJavascriptEnabled();

		onWebView().withElement(findElement(Locator.TAG_NAME, "body"))
		.check(webMatches(getText(), containsString(getResourceString(R.string.test_fullscreen_html))));
	}

	@Test
	public void testShowUrl() {
		onView(withId(R.id.btnShowUrl))
		.perform(click());
		intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(getResourceString(R.string.test_url))));
		tearDown();
	}

	@Test
	public void testShowHideFloatingButton() {
		onView(withId(R.id.btnShowFloatingButton))
		.perform(click());

		onView(allOf(withTagValue(is((Object) TestAppUIServices.getFloatingButtonTag())), isDisplayed()))
		.check(matches(isDisplayed()));

		onView(withId(R.id.btnHideFloatingButton))
		.perform(click());

		onView(allOf(withTagValue(is((Object) TestAppUIServices.getFloatingButtonTag())), isDisplayed()))
		.check(doesNotExist());
	}

	public void tearDown() {
		uiDevice.pressBack();
	}

	private String getResourceString(int id) {
		Context targetContext = InstrumentationRegistry.getTargetContext();
		return targetContext.getString(id);
	}

	@Test
	public void useAppContext() {
		// Context of the app under test.
		Context appContext = InstrumentationRegistry.getTargetContext();

		assertEquals("com.adobe.testapp", appContext.getPackageName());
	}
}
