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

import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RuleTests extends BaseTest {

	private RuleTokenParser ruleTokenParser;

	@BeforeClass
	public static void setupTests() {
		PlatformServices mockPlatformServices = new FakePlatformServices();
		Log.setLoggingService(mockPlatformServices.getLoggingService());
		Log.setLogLevel(LoggingMode.VERBOSE);
	}

	@AfterClass
	public static void tearDownTests() {
		//Reset logger
		Log.setLogLevel(LoggingMode.ERROR);
		Log.setLoggingService(null);
	}

	@Before
	public void setup() {
		super.beforeEach();
		final EventHub eventHub = new EventHub("testEventHub", new FakePlatformServices());
		ruleTokenParser = new RuleTokenParser(eventHub);
	}

	@Test
	public void evaluateCondition_ReturnsTrue_When_DataIsValid() {
		//setup
		EventData testEventData = new EventData();
		testEventData.putString("key0", "value0");
		testEventData.putString("key1", "value1");

		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.REQUEST_CONTENT).setData(testEventData).build();

		RuleCondition condition = new RuleCondition() {
			@Override
			protected boolean evaluate(final RuleTokenParser tokenParser, final Event event) {
				//Make it evaluate to true
				return true;
			}
			@Override
			public String toString() {
				return null;
			}
		};
		Rule testRule = new Rule(condition, new ArrayList<Event>());
		//test
		boolean result = testRule.evaluateCondition(ruleTokenParser, testEvent);
		//verify
		assertTrue("The condition evalutation should return true with valid data", result);
	}

	@Test
	public void toString_ReturnsValidString() {
		//setup
		RuleCondition condition = new RuleConditionMatcher(null);
		List<Event> consequenceEvents = new ArrayList<Event>();

		consequenceEvents.add(new Event.Builder("test", EventType.RULES_ENGINE, EventSource.NONE).build());
		//test
		final Rule testRule = new Rule(condition, consequenceEvents);

		//verify
		assertTrue(testRule.toString().contains("Condition: "));
		assertTrue(testRule.toString().contains("Consequences: "));
	}
}