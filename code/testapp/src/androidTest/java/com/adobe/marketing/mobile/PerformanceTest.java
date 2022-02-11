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


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PerformanceTest {

	class TestModule extends Module {

		TestModule(final EventHub hub) {
			super("TestModule", hub);

		}

		@Override
		protected void onUnregistered() {

		}
	}

	@Test
	public void useAppContext() {
		// Context of the app under test.
		Context appContext = InstrumentationRegistry.getTargetContext();

		assertEquals("com.adobe.testapp", appContext.getPackageName());
	}

	@Test
	public void rulesEnginePerformanceTest() throws UnsupportedConditionException, JsonException, InvalidModuleException,
		InterruptedException {
		PlatformServices platformServices = new AndroidPlatformServices();
		EventHub eventHub = new EventHub("RulesEeningTest", platformServices);
		RulesEngine rulesEngine = new RulesEngine(eventHub);
		JsonUtilityService.JSONObject testRuleConditionJson =
			platformServices.getJsonUtilityService().createJSONObject("{\"type\":\"matcher\",\"definition\":{\"key\":\"key\",\"matcher\":\"eq\",\"values\":[\"value\"]}}");
		RuleCondition ruleCondition = RuleCondition.ruleConditionFromJson(testRuleConditionJson);
		Event event = new Event.Builder("", EventType.CONFIGURATION,
										EventSource.RESPONSE_CONTENT).build();
		List<Event> events = new ArrayList<>();
		events.add(event);
		List<Rule> rules = new ArrayList<>();

		for (int i = 0; i < 1000; i++) {
			rules.add(new Rule(ruleCondition, events));
		}

		long start = System.currentTimeMillis();
		rulesEngine.replaceRules(new TestModule(eventHub), rules);

		for (int i = 0; i < 100; i++) {
			rulesEngine.evaluateRules(new Event.Builder("", EventType.CONFIGURATION,
									  EventSource.RESPONSE_CONTENT).build());
		}

		Log.d("+++++", " time =>>>>>>>>>> " + (System.currentTimeMillis() - start));

	}
}