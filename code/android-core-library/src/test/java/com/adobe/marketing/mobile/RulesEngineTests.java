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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RulesEngineTests extends BaseTest {
	class MockModule extends Module {
		MockModule(String name, EventHub hub) {
			super(name, hub);
		}
	}

	class MockRule extends Rule {
		MockRule(RuleCondition condition, ArrayList<Event> consequences) {
			super(condition, consequences);
		}

		void setConsequenceEvents(final ArrayList<Event> newConsequenceEvents) {
			consequenceEvents = newConsequenceEvents;
		}

		void setCondition(final RuleCondition newRuleCondition) {
			condition = newRuleCondition;
		}
	}

	class TestableRulesEngine extends RulesEngine {
		TestableRulesEngine(EventHub hub) {
			super(hub);
		}

		ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>> getModuleRules() {
			return moduleRuleAssociation;
		}

		void setModuleRules(ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>> rules) {
			moduleRuleAssociation.clear();

			if (rules != null && rules.size() > 0) {
				moduleRuleAssociation.putAll(rules);
			}
		}
	}

	class TestableRuleCondition extends RuleCondition {
		TestableRuleCondition() {
			super();
		}

		public boolean evaluateReturnValue = true;

		@Override
		protected boolean evaluate(RuleTokenParser parser, Event event) {
			return evaluateReturnValue;
		}

		@Override
		public String toString() {
			return "";
		}
	}

	private static TestableRulesEngine _rulesEngine;
	private static EventHub _eventHub;
	private static PlatformServices _platformServices;

	private static MockModule _mockModule;
	private static MockModule _mockAnalyticsModule;
	private static ConcurrentLinkedQueue<Rule> _mockRules;
	private static MockRule _mockRuleOne;
	private static ArrayList<Event> _mockRuleOneConsequences;
	private static MockRule _mockRuleTwo;
	private static ArrayList<Event> _mockRuleTwoConsequences;
	private static MockRule _mockRuleThree;
	private static ArrayList<Event> _mockRuleThreeConsequences;
	private static MockRule _mockRuleFour;
	private static ArrayList<Event> _mockRuleFourConsequences;
	private static int _mockRulesCount;

	private static final String EVENT_DATA_RULES_URL_KEY = "rules.url";

	@Before
	public void setupTests() {
		super.beforeEach();
		Log.setLoggingService(platformServices.getLoggingService());
		Log.setLogLevel(LoggingMode.VERBOSE);

		_platformServices = new FakePlatformServices();
		_eventHub = new EventHub("rulesEngineTestsEventHub", _platformServices);
		_rulesEngine = new TestableRulesEngine(_eventHub);
		_mockModule = new MockModule("a.mock.module", _eventHub);
		_mockAnalyticsModule = new MockModule("com.adobe.module.analytics", _eventHub);

		try {
			// setup mock rule #1 - analytics track
			TestableRuleCondition conditionOne = new TestableRuleCondition();
			_mockRuleOneConsequences = new ArrayList<Event>();
			_mockRuleOneConsequences.add(GetConsequenceEventAnalytics());
			_mockRuleOne = new MockRule(conditionOne, _mockRuleOneConsequences);

			// setup mock rule #2 - attach data
			TestableRuleCondition conditionTwo = new TestableRuleCondition();
			_mockRuleTwoConsequences = new ArrayList<Event>();
			_mockRuleTwoConsequences.add(GetConsequenceEventAttachData());
			_mockRuleTwo = new MockRule(conditionTwo, _mockRuleTwoConsequences);

			// setup mock rule #3 - modify data
			TestableRuleCondition conditionThree = new TestableRuleCondition();
			_mockRuleThreeConsequences = new ArrayList<Event>();
			_mockRuleThreeConsequences.add(GetConsequenceEventModifyData());
			_mockRuleThree = new MockRule(conditionThree, _mockRuleThreeConsequences);

			// setup mock rule #4 - dispatch new event with copied data
			TestableRuleCondition conditionFour = new TestableRuleCondition();
			_mockRuleFourConsequences = new ArrayList<Event>();
			_mockRuleFourConsequences.add(GetConsequenceEventDispatch("test.dispatch.type", "test.dispatch.source",
										  RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_COPY, null));
			_mockRuleFour = new MockRule(conditionFour, _mockRuleFourConsequences);

			_mockRules = new ConcurrentLinkedQueue<Rule>();
			_mockRules.add(_mockRuleOne);
			_mockRules.add(_mockRuleTwo);
			_mockRules.add(_mockRuleThree);
			_mockRules.add(_mockRuleFour);
			_mockRulesCount = _mockRules.size();

			ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>> moduleRulesQueue = new
			ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>>();
			moduleRulesQueue.put(_mockModule, _mockRules);
			_rulesEngine.setModuleRules(moduleRulesQueue);

		} catch (Exception ex) {}
	}

	@After
	public void tearDownTests() {
		//Reset logger
		Log.setLogLevel(LoggingMode.ERROR);
		Log.setLoggingService(null);
	}

	static EventData GetTriggeredEventData() {
		EventData eventData = new EventData();
		eventData.putString("&&key1", "value1");
		eventData.putString("key2", "value2");
		eventData.putInteger("anInt", 552);
		Map<String, Variant> variantMap = new HashMap<String, Variant>();
		variantMap.put("&&embeddedString", Variant.fromString("embeddedStringValue"));
		eventData.putVariantMap("aMap", variantMap);
		ArrayList<Variant> variantList = new ArrayList<Variant>();
		variantList.add(Variant.fromString("stringInList"));
		eventData.putVariantList("aList", variantList);
		return eventData;
	}

	public static Event GetEventWithListOfObjects() {
		EventData eventData = GetTriggeredEventData();
		final List<Variant> listOfObjects = new ArrayList<Variant>();
		final Map<String, Variant> obj1 = new HashMap<String, Variant>();
		obj1.put("name", Variant.fromString("request1"));
		final Map<String, Variant> obj1Details = new HashMap<String, Variant>();
		obj1Details.put("size", Variant.fromString("large"));
		obj1Details.put("color", Variant.fromString("red"));
		obj1.put("details", Variant.fromVariantMap(obj1Details));
		final Map<String, Variant> obj2 = new HashMap<String, Variant>();
		obj2.put("name", Variant.fromString("request2"));
		obj2.put("location", Variant.fromString("central"));
		listOfObjects.add(Variant.fromVariantMap(obj1));
		listOfObjects.add(Variant.fromVariantMap(obj2));
		eventData.putVariantList("listOfObjects", listOfObjects);
		return new Event.Builder("Test", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).setData(eventData).build();
	}

	public static Event GetTriggeringEvent() {
		return new Event.Builder("Test", EventType.ANALYTICS,
								 EventSource.REQUEST_CONTENT).setData(GetTriggeredEventData()).build();
	}

	public static Event GetConsequenceEventAnalytics() {
		EventData eventData = new EventData();

		Map<String, Variant> consequenceMap = new HashMap<String, Variant>();
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_JSON_ID, Variant.fromString("analyticsId"));
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_JSON_TYPE,
						   Variant.fromString(RulesEngineConstantsTests.ConsequenceTypes.SEND_DATA_TO_ANALYTICS));
		Map<String, String> detailMap = new HashMap<String, String>();
		detailMap.put("key3", "{%&&key1%}");
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL, Variant.fromStringMap(detailMap));
		eventData.putVariantMap(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_TRIGGERED, consequenceMap);

		return new Event.Builder("Test", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).setData(eventData).build();
	}

	public static Event GetConsequenceEventAttachData() {
		EventData eventData = new EventData();

		Map<String, Variant> consequenceMap = new HashMap<String, Variant>();
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_JSON_ID, Variant.fromString("attachId"));
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_JSON_TYPE,
						   Variant.fromString(RulesEngineConstantsTests.ConsequenceTypes.ATTACH_DATA));

		Map<String, Variant> detailMap = new HashMap<String, Variant>();
		Map<String, Variant> eventDataMap = new HashMap<String, Variant>();
		Map<String, Variant> aMap = new HashMap<String, Variant>();
		Map<String, Variant> newMap = new HashMap<String, Variant>();
		List<Variant> aList = new ArrayList<Variant>();
		List<Variant> newList = new ArrayList<Variant>();

		eventDataMap.put("attachedKey", Variant.fromString("attachedValue"));
		eventDataMap.put("&&key1", Variant.fromString("updatedValue1"));
		eventDataMap.put("newInt", Variant.fromInteger(123));
		aMap.put("&&embeddedString", Variant.fromString("changedEmbeddedStringValue"));
		aMap.put("newEmbeddedString", Variant.fromString("newEmbeddedStringValue"));
		eventDataMap.put("aMap", Variant.fromVariantMap(aMap));
		newMap.put("newMapKey", Variant.fromString("newMapValue"));
		eventDataMap.put("newMap", Variant.fromVariantMap(newMap));
		aList.add(Variant.fromString("stringInList"));
		aList.add(Variant.fromString("newStringInList"));
		eventDataMap.put("aList", Variant.fromVariantList(aList));
		newList.add(Variant.fromString("newListString"));
		eventDataMap.put("newList", Variant.fromVariantList(newList));

		detailMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_EVENT_DATA,
					  Variant.fromVariantMap(eventDataMap));
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL, Variant.fromVariantMap(detailMap));
		eventData.putVariantMap(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_TRIGGERED, consequenceMap);

		return new Event.Builder("Test", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).setData(eventData).build();
	}

	/**
	 * Contents of the EventData for the below Consequence Event: (all maps are represented as Map<String, Variant>
	 * {
	 *    "triggeredconsequence":{
	 *       "id":"modifyId",
	 *       "type":"mod",
	 *       "detail":{
	 *          "eventdata":{
	 *             "&&key1":null,
	 *             "aList":null,
	 *             "aMap":null,
	 *             "listOfObjects[*]":{
	 *                "details":{
	 *                   "temp":58.8,
	 *                   "color":"orange"
	 *                }
	 *             }
	 *          }
	 *       }
	 *    }
	 * }
	 */
	public static Event GetConsequenceEventModifyData() {
		EventData eventData = new EventData();

		Map<String, Variant> consequenceMap = new HashMap<String, Variant>();
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_JSON_ID, Variant.fromString("modifyId"));
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_JSON_TYPE,
						   Variant.fromString(RulesEngineConstantsTests.ConsequenceTypes.MODIFY_DATA));

		Map<String, Variant> detailMap = new HashMap<String, Variant>();
		Map<String, Variant> eventDataMap = new HashMap<String, Variant>();

		eventDataMap.put("&&key1", Variant.fromNull());
		eventDataMap.put("aMap", Variant.fromNull());
		eventDataMap.put("aList", Variant.fromNull());

		final Map<String, Variant> listOfObjectsAsMap = new HashMap<String, Variant>();
		final Map<String, Variant> details = new HashMap<String, Variant>();
		details.put("color", Variant.fromString("orange"));
		details.put("temp", Variant.fromDouble(58.8));
		listOfObjectsAsMap.put("details", Variant.fromVariantMap(details));
		eventDataMap.put("listOfObjects[*]", Variant.fromVariantMap(listOfObjectsAsMap));

		detailMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_EVENT_DATA,
					  Variant.fromVariantMap(eventDataMap));
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL, Variant.fromVariantMap(detailMap));
		eventData.putVariantMap(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_TRIGGERED, consequenceMap);

		return new Event.Builder("Test", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).setData(eventData).build();
	}

	/**
	 * {
	 * 	"triggeredconsequence":{
	 * 		"id": "dispatchId",
	 * 		"type": "dispatch"
	 * 		"detail": {
	 * 			"source":<value of source>,
	 * 			"type":<value of type>",
	 * 		    "eventdataaction": <value of eventDataType>
	 * 		}
	 * 	}
	 * }
	 *
	 * @param type event type of the new event to be dispatched
	 * @param source event source of the new event to be dispatched
	 * @param eventDataType event data type of the new event. Possible values are copy (copies data from the triggering event)
	 *                      or new (event data is provided under detail.eventdata)
	 * @param eventData (optional) event data, to be used with the {@code eventDataType} new option
	 */
	public static Event GetConsequenceEventDispatch(final String type, final String source, final String eventDataType,
			final Map<String, Variant> eventData) {
		EventData consequenceEventData = new EventData();

		Map<String, Variant> consequenceMap = new HashMap<String, Variant>();
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_JSON_ID, Variant.fromString("dispatchId"));
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_JSON_TYPE,
						   Variant.fromString(RulesEngineConstantsTests.ConsequenceTypes.DISPATCH));

		Map<String, Variant> detailMap = new HashMap<String, Variant>();

		if (type != null) {
			detailMap.put(RulesEngineConstantsTests.EventDataKeys.DISPATCH_CONSEQUENCE_TYPE, Variant.fromString(type));
		}

		if (source != null) {
			detailMap.put(RulesEngineConstantsTests.EventDataKeys.DISPATCH_CONSEQUENCE_SOURCE, Variant.fromString(source));
		}

		if (eventDataType != null) {
			detailMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_EVENT_DATA_ACTION,
						  Variant.fromString(eventDataType));
		}

		if (eventData != null) {
			detailMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_EVENT_DATA, Variant.fromVariantMap(eventData));
		}

		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL, Variant.fromVariantMap(detailMap));
		consequenceEventData.putVariantMap(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_TRIGGERED, consequenceMap);

		return new Event.Builder("Test Dispatch", EventType.RULES_ENGINE,
								 EventSource.REQUEST_CONTENT).setData(consequenceEventData).build();
	}

	static Map<String, Variant> GetConsequenceMapFromConsequenceEvent(final Event consequenceEvent) {
		return consequenceEvent.getData().optVariantMap(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_TRIGGERED, null);
	}

	public static Event GetEvent(final EventType eventType, final EventSource eventSource) {
		EventData eventData = new EventData();
		eventData.putString("&&key1", "value1");
		return new Event.Builder("Test", eventType, eventSource).setData(eventData).build();
	}

	public static String GetStringFromDetailsOfTriggeredConsequenceEventData(final String key, final EventData eventData) {
		try {
			Map<String, Variant> triggeredConsequenceMap = eventData.getVariantMap(
						RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_TRIGGERED);
			Map<String, Variant> detailMap = triggeredConsequenceMap.get(
												 RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL).getVariantMap();

			return detailMap.containsKey(key) ? detailMap.get(key).convertToString() : "";
		} catch (Exception ex) {
			return "";
		}
	}

	public static void SetAnalyticsSharedState(final EventData eventData) {
		try {
			_eventHub.createSharedState(_mockAnalyticsModule, 0, eventData);
		} catch (Exception ex) {}
	}


	// =================================================================================================================
	// protected void addRule(final Module owningModule, final Rule rule)
	// =================================================================================================================
	@Test
	public void addRule_When_Happy_Then_RuleIsAdded() {
		// setup
		_rulesEngine.setModuleRules(null);
		final ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>> rulesBefore = _rulesEngine.getModuleRuleAssociation();
		assertEquals(0, rulesBefore.size());

		// test
		_rulesEngine.addRule(_mockModule, _mockRuleOne);

		// verify
		final ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>> rulesAfter = _rulesEngine.getModuleRules();
		assertEquals(1, rulesAfter.size());
	}

	@Test
	public void addRule_When_NullInput_Then_NoRuleIsAdded() {
		// setup
		_rulesEngine.setModuleRules(null);
		final ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>> rulesBefore = _rulesEngine.getModuleRuleAssociation();
		assertEquals(0, rulesBefore.size());

		// test
		_rulesEngine.addRule(_mockModule, null);

		// verify
		final ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>> rulesAfter = _rulesEngine.getModuleRules();
		assertEquals(0, rulesAfter.size());
	}

	@Test
	public void addRule_When_NoConsequenceEvents_Then_NoRuleIsAdded() {
		// setup
		_rulesEngine.setModuleRules(null);
		_mockRuleOne.setConsequenceEvents(new ArrayList<Event>());
		final ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>> rulesBefore = _rulesEngine.getModuleRuleAssociation();
		assertEquals(0, rulesBefore.size());

		// test
		_rulesEngine.addRule(_mockModule, _mockRuleOne);

		// verify
		final ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>> rulesAfter = _rulesEngine.getModuleRules();
		assertEquals(0, rulesAfter.size());
	}

	@Test
	public void addRule_When_ModuleAlreadyInModuleMap_Then_RuleIsAddedToExistingModule() {
		// setup
		final ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>> rulesBefore = _rulesEngine.getModuleRuleAssociation();
		assertEquals(1, rulesBefore.size());
		assertEquals(_mockRulesCount, rulesBefore.get(_mockModule).size());

		// test
		_rulesEngine.addRule(_mockModule, _mockRuleOne);

		// verify
		final ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>> rulesAfter = _rulesEngine.getModuleRules();
		assertEquals(1, rulesAfter.size());
		assertEquals(_mockRulesCount + 1, rulesAfter.get(_mockModule).size());
	}

	// =================================================================================================================
	// protected void replaceRules(final Module owningModule, final List<Rule> rules)
	// =================================================================================================================

	// =================================================================================================================
	// ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>> getModuleRuleAssociation()
	// =================================================================================================================

	// =================================================================================================================
	// protected void unregisterAllRules(final Module owningModule)
	// =================================================================================================================

	// =================================================================================================================
	// protected List<Event> evaluateRules(final Event triggerEvent)
	// =================================================================================================================
	@Test
	@Ignore
	public void evaluateRules_When_Happy_Then_ShouldProperlyHandleVariousRuleConsequenceTypes() throws Exception {
		// setup
		final Event triggeringEvent = GetTriggeringEvent();
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));

		// test
		final List<Event> result = _rulesEngine.evaluateRules(triggeringEvent);

		// verify
		assertEquals("Should have two consequence events: analytics + dispatch consequence", 2, result.size());
		final EventData event1Data = result.get(0).getData();
		final String valueForKey3 = GetStringFromDetailsOfTriggeredConsequenceEventData("key3", event1Data);
		assertEquals("Token value should be replaced in consequence event", "value1", valueForKey3);

		final EventData newData = triggeringEvent.getData();
		assertNotNull(newData);
		assertEquals(6, newData.size());
		assertTrue(newData.containsKey("attachedKey"));
		assertEquals("attachedValue", newData.optString("attachedKey", ""));
		assertFalse(newData.containsKey("&&key1"));
		assertEquals("value2", newData.optString("key2", ""));
		assertEquals(552, newData.optInteger("anInt", 0));
		assertEquals(123, newData.optInteger("newInt", 0));
		assertFalse(newData.containsKey("aMap"));
		assertEquals("newMapValue", newData.getVariantMap("newMap").get("newMapKey").convertToString());
		assertEquals("newListString", newData.getVariantList("newList").get(0).convertToString());
		assertFalse(newData.containsKey("aList"));

		final Map<String, Object> event2Data = result.get(1).getEventData();
		assertEquals(triggeringEvent.getEventData(), event2Data);
	}

	@Test
	public void evaluateRules_When_NoModulesHaveRules_Then_ShouldReturnEmpty() {
		// setup
		_rulesEngine.setModuleRules(new ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>>());

		// test
		final List<Event> result = _rulesEngine.evaluateRules(GetTriggeringEvent());

		// verify
		assertEquals(0, result.size());
	}

	@Test
	public void evaluateRules_When_ModulesHaveNoRules_Then_ShouldReturnEmpty() {
		// setup
		final ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>> rules = new
		ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>>();
		rules.put(_mockModule, new ConcurrentLinkedQueue<Rule>());
		_rulesEngine.setModuleRules(rules);

		// test
		final List<Event> result = _rulesEngine.evaluateRules(GetTriggeringEvent());

		// verify
		assertEquals(0, result.size());
	}

	@Test
	public void evaluateRules_When_NoRulesEvaluateToTrue_Then_ShouldReturnEmpty() {
		// setup
		RuleCondition falseCondition = new RuleCondition() {
			@Override
			protected boolean evaluate(RuleTokenParser ruleTokenParser, Event event) {
				return false;
			}

			@Override
			public String toString() {
				return null;
			}
		};
		_mockRuleOne.setCondition(falseCondition);
		_mockRuleTwo.setCondition(falseCondition);
		_mockRuleThree.setCondition(falseCondition);
		_mockRuleFour.setCondition(falseCondition);

		// test
		final List<Event> result = _rulesEngine.evaluateRules(GetTriggeringEvent());

		// verify
		assertEquals(0, result.size());
	}

	@Test
	public void evaluateRules_When_RulesHaveNoConsequenceEvents_Then_ShouldReturnEmpty() {
		// setup
		_mockRuleOne.setConsequenceEvents(new ArrayList<Event>());
		_mockRuleTwo.setConsequenceEvents(new ArrayList<Event>());
		_mockRuleThree.setConsequenceEvents(new ArrayList<Event>());
		_mockRuleFour.setConsequenceEvents(new ArrayList<Event>());

		// test
		final List<Event> result = _rulesEngine.evaluateRules(GetTriggeringEvent());

		// verify
		assertEquals(0, result.size());
	}

	@Test
	public void evaluateRules_When_ConsequenceEventsHaveNoEventData_Then_ShouldReturnEmpty() {
		// setup
		final Event eventWithoutData = new Event.Builder("Test", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).build();
		final ArrayList<Event> ruleOneEvents = new ArrayList<Event>();
		ruleOneEvents.add(eventWithoutData);
		final ArrayList<Event> ruleTwoEvents = new ArrayList<Event>();
		ruleTwoEvents.add(eventWithoutData);
		final ArrayList<Event> ruleThreeEvents = new ArrayList<Event>();
		ruleThreeEvents.add(eventWithoutData);
		final ArrayList<Event> ruleFourEvents = new ArrayList<Event>();
		ruleFourEvents.add(eventWithoutData);
		_mockRuleOne.setConsequenceEvents(ruleOneEvents);
		_mockRuleTwo.setConsequenceEvents(ruleTwoEvents);
		_mockRuleThree.setConsequenceEvents(ruleThreeEvents);
		_mockRuleFour.setConsequenceEvents(ruleFourEvents);

		// test
		final List<Event> result = _rulesEngine.evaluateRules(GetTriggeringEvent());

		// verify
		assertEquals(0, result.size());
	}

	@Test
	public void evaluateRules_When_ConsequenceEventsHaveNoConsequenceType_Then_ShouldReturnEmpty() {
		// setup
		setNoTypeConsequenceFor(_mockRuleOne);
		setNoTypeConsequenceFor(_mockRuleTwo);
		setNoTypeConsequenceFor(_mockRuleThree);
		setNoTypeConsequenceFor(_mockRuleFour);

		// test
		final List<Event> result = _rulesEngine.evaluateRules(GetTriggeringEvent());

		// verify
		assertEquals(0, result.size());
	}

	@Test
	public void evaluateRules_When_ConsequenceEventsHaveEmptyConsequenceType_Then_ShouldReturnEmpty() {
		// setup
		setEmptyConsequenceFor(_mockRuleOne);
		setEmptyConsequenceFor(_mockRuleTwo);
		setEmptyConsequenceFor(_mockRuleThree);
		setEmptyConsequenceFor(_mockRuleFour);

		// test
		final List<Event> result = _rulesEngine.evaluateRules(GetTriggeringEvent());

		// verify
		assertEquals(0, result.size());
	}

	// =================================================================================================================
	// protected List<Event> evaluateEventWithRules(final Event triggerEvent, final List<Rule> rules)
	// =================================================================================================================

	@Test
	public void evaluateEventWithRules_When_LoopDispatchConsequences_Then_ShouldTriggerDispatchOnceForEachRule() {
		// setup
		final int expectedConsequencesCount = 3;
		final String eventType = "test.type";
		final String eventSource = "test.source";
		Event triggerEvent = new Event.Builder("Test loop, chained consequence", eventType,
		eventSource).setEventData(new HashMap<String, Object>() {
			{
				put("hello", "world");
			}
		}).build();

		// add 3 dispatch consequences
		List<Event> consequences = new ArrayList<Event>();
		consequences.add(GetConsequenceEventDispatch(eventType, eventSource,
						 RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_COPY, null));
		consequences.add(GetConsequenceEventDispatch(eventType, eventSource,
						 RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_COPY, null));
		consequences.add(GetConsequenceEventDispatch(eventType, eventSource,
						 RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_NEW, null));
		List<Rule> rules = new ArrayList<Rule>() {
			{
				add(new Rule(new TestableRuleCondition(), consequences));
			}
		};

		// send triggering event, dispatch chain count = 0
		// verify 3 events dispatched based on consequences
		List<Event> triggeredConsequences1 = new ArrayList<Event>();
		triggeredConsequences1.addAll(_rulesEngine.evaluateEventWithRules(triggerEvent, rules));

		assertEquals(expectedConsequencesCount, triggeredConsequences1.size());

		// re-execute the triggered dispatch consequences to simulate rules run on EventHub events queue, dispatch chain count = 1
		// expect 0 new triggered consequences as max allowed chained events is 1, chain interrupted :)
		List<Event> triggeredConsequences2 = new ArrayList<Event>();

		for (Event event : triggeredConsequences1) {
			triggeredConsequences2.addAll(_rulesEngine.evaluateEventWithRules(event, rules));
		}

		assertEquals(0, triggeredConsequences2.size());
		assertEquals(0, _rulesEngine.getDispatchChainedEvents().size());
	}

	@Test
	public void
	evaluateEventWithRules_When_LoopDispatchConsequence_And_SameEventSeenMultipleTimes_ShouldTriggerDispatchEachTimeOnce() {
		// setup
		final int repeatTest = 5;
		final int expectedConsequencesCount = 15; // 3 consequences x 5 repeats
		final String eventType = "test.type";
		final String eventSource = "test.source";
		Event triggerEvent = new Event.Builder("Test loop, same event", eventType,
		eventSource).setEventData(new HashMap<String, Object>() {
			{
				put("hello", "world");
			}
		}).build();

		// add 3 dispatch consequences
		List<Event> consequences = new ArrayList<Event>();
		consequences.add(GetConsequenceEventDispatch(eventType, eventSource,
						 RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_COPY, null));
		consequences.add(GetConsequenceEventDispatch(eventType, eventSource,
						 RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_COPY, null));
		consequences.add(GetConsequenceEventDispatch(eventType, eventSource,
						 RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_NEW, null));
		List<Rule> rules = new ArrayList<Rule>() {
			{
				add(new Rule(new TestableRuleCondition(), consequences));
			}
		};

		// test
		List<Event> triggeredConsequences = new ArrayList<Event>();

		for (int i = 0; i < repeatTest; i++) {
			triggeredConsequences.addAll(_rulesEngine.evaluateEventWithRules(triggerEvent, rules));
		}

		// verify
		assertEquals(expectedConsequencesCount, triggeredConsequences.size());
		Map<String, Integer> dispatchChainedEvents = _rulesEngine.getDispatchChainedEvents();
		assertEquals(expectedConsequencesCount, dispatchChainedEvents.size());

		for (int i = 0; i < dispatchChainedEvents.size(); i++) {
			assertEquals(1, (int) dispatchChainedEvents.get(triggeredConsequences.get(i).getUniqueIdentifier()));
		}
	}

	@Test
	public void evaluateEventWithRules_When_NewDispatchConsequenceAddedAtRuntime_ShouldTriggerDispatchAgainAsCountResets() {
		// setup
		final String eventType = "test.type";
		final String eventSource = "test.source";
		Event triggerEvent = new Event.Builder("Test discontinued loop", eventType,
		eventSource).setEventData(new HashMap<String, Object>() {
			{
				put("hello", "world");
			}
		}).build();

		// add 1 dispatch consequences
		List<Event> consequencesDispatch = new ArrayList<Event>();
		consequencesDispatch.add(GetConsequenceEventDispatch(eventType, eventSource,
								 RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_COPY, null));
		List<Rule> rulesDispatch = new ArrayList<Rule>() {
			{
				add(new Rule(new TestableRuleCondition(), consequencesDispatch));
			}
		};

		// add 1 attachData consequence
		List<Event> consequencesAttachData = new ArrayList<Event>();
		consequencesAttachData.add(GetConsequenceEventAttachData());
		List<Rule> rulesAttachData = new ArrayList<Rule>() {
			{
				add(new Rule(new TestableRuleCondition(), consequencesAttachData));
			}
		};

		// test & verify
		// step1: dispatch consequence triggered, dispatch events chain = 1 after this step
		List<Event> triggeredConsequences = _rulesEngine.evaluateEventWithRules(triggerEvent, rulesDispatch);
		assertEquals(1, triggeredConsequences.size());
		Map<String, Integer> dispatchChainedEvents = _rulesEngine.getDispatchChainedEvents();
		assertEquals(1, dispatchChainedEvents.size());
		assertEquals(1, (int) dispatchChainedEvents.get(triggeredConsequences.get(0).getUniqueIdentifier()));
		Event cachedTriggeredConsequence = triggeredConsequences.get(0);

		// step2: rules updated at runtime, no dispatch consequence, cachedTriggeredConsequence uuid is now removed from dispatchChainedEvents
		// expect dispatch events chain = 0
		triggeredConsequences = _rulesEngine.evaluateEventWithRules(cachedTriggeredConsequence, rulesAttachData);
		assertEquals(0, triggeredConsequences.size());
		assertEquals(0, _rulesEngine.getDispatchChainedEvents().size());

		// step3: new dispatch rules added at runtime, cached triggered consequence event is replayed (edge-case)
		// expect dispatch events chain = 1, the event is treated as the original event
		triggeredConsequences = _rulesEngine.evaluateEventWithRules(cachedTriggeredConsequence, rulesDispatch);
		assertEquals(1, triggeredConsequences.size());
		dispatchChainedEvents = _rulesEngine.getDispatchChainedEvents();
		assertEquals(1, dispatchChainedEvents.size());
		assertEquals(1, (int) dispatchChainedEvents.get(triggeredConsequences.get(0).getUniqueIdentifier()));
	}

	@Test
	public void evaluateEventWithRules_When_MultipleConsequencesIncludingDispatch_ShouldTriggerDispatchOnce() {
		// setup
		final String eventType = "test.type";
		final String eventSource = "test.source";
		Event triggerEvent = new Event.Builder("Test multiple rules attach first, then dispatch", eventType,
		eventSource).setEventData(new HashMap<String, Object>() {
			{
				put("hello", "world");
			}
		}).build();

		// add 1 attach data and 1 dispatch consequence
		List<Event> consequencesAttachData = new ArrayList<Event>();
		consequencesAttachData.add(GetConsequenceEventAttachData());
		List<Event> consequencesDispatch = new ArrayList<Event>();
		consequencesDispatch.add(GetConsequenceEventDispatch(eventType, eventSource,
								 RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_COPY, null));

		List<Rule> rules = new ArrayList<Rule>() {
			{
				add(new Rule(new TestableRuleCondition(), consequencesAttachData));
				add(new Rule(new TestableRuleCondition(), consequencesDispatch));
			}
		};

		// test & verify
		List<Event> triggeredConsequences = _rulesEngine.evaluateEventWithRules(triggerEvent, rules);
		assertEquals(1, triggeredConsequences.size());
		Map<String, Integer> dispatchChainedEvents = _rulesEngine.getDispatchChainedEvents();
		assertEquals(1, dispatchChainedEvents.size());
		assertEquals(1, (int) dispatchChainedEvents.get(triggeredConsequences.get(0).getUniqueIdentifier()));


		// re-execute the triggered event to simulate EventHub queue, dispatch events chain = 1
		// max chain = 1, do not dispatch again
		triggeredConsequences.addAll(_rulesEngine.evaluateEventWithRules(triggeredConsequences.get(0), rules));
		assertEquals(1, triggeredConsequences.size());
	}

	// =================================================================================================================
	// protected List<Event> evaluateRuleForEvent(final Event triggerEvent, final Rule rule)
	// =================================================================================================================

	// =================================================================================================================
	// protected void processAttachDataConsequence(final Map<String, Variant> consequenceMap, final Event triggeringEvent)
	// =================================================================================================================evaluateRules_When_Happy_Then_ShouldProperlyHandleVariousRuleConsequenceTypes
	@Test
	@Ignore
	public void processAttachDataConsequence_When_Happy_Then_ShouldAttachDataButNotOverwriteExistingData() throws
		Exception {
		// setup
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventAttachData());
		Event triggeringEvent = GetTriggeringEvent();
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());

		// test
		_rulesEngine.processAttachDataConsequence(consequenceMap, triggeringEvent);

		// verify
		final EventData newData = triggeringEvent.getData();
		assertNotNull(newData);
		assertEquals(9, newData.size());
		assertTrue(newData.containsKey("attachedKey"));
		assertEquals("value1", newData.optString("&&key1", ""));
		assertEquals("value2", newData.optString("key2", ""));
		assertEquals(552, newData.optInteger("anInt", 0));
		assertEquals(123, newData.optInteger("newInt", 0));
		assertEquals("embeddedStringValue", newData.getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("newEmbeddedStringValue", newData.getVariantMap("aMap").get("newEmbeddedString").convertToString());
		assertEquals("newMapValue", newData.getVariantMap("newMap").get("newMapKey").convertToString());
		assertEquals("stringInList", newData.getVariantList("aList").get(0).convertToString());
		assertEquals("newStringInList", newData.getVariantList("aList").get(1).convertToString());
		assertEquals("newListString", newData.getVariantList("newList").get(0).convertToString());
	}

	@Test
	public void processAttachDataConsequence_When_NullConsequenceEvent_Then_ShouldReturnTriggeringEventData() throws
		Exception {
		// setup
		Event triggeringEvent = GetTriggeringEvent();
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());

		// test
		_rulesEngine.processAttachDataConsequence(null, triggeringEvent);

		// verify
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());
	}

	@Test
	public void processAttachDataConsequence_When_EmptyConsequenceEvent_Then_ShouldReturnTriggeringEventData() throws
		Exception {
		// setup
		Event triggeringEvent = GetTriggeringEvent();
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());

		// test
		_rulesEngine.processAttachDataConsequence(new HashMap<String, Variant>(), triggeringEvent);

		// verify
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());
	}

	@Test
	public void processAttachDataConsequence_When_NullTriggeringEvent_Then_ShouldNoOp() {
		// setup
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventAttachData());

		// test
		_rulesEngine.processAttachDataConsequence(consequenceMap, null);

		// verify
		// no-op
	}

	@Test
	public void processAttachDataConsequence_When_ConsequenceMapHasNoDetail_Then_ShouldReturnTriggeringEventData() throws
		Exception {
		// setup
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventAttachData());
		consequenceMap.remove(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL);
		Event triggeringEvent = GetTriggeringEvent();
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());

		// test
		_rulesEngine.processAttachDataConsequence(consequenceMap, triggeringEvent);

		// verify
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());
	}

	@Test
	public void
	processAttachDataConsequence_When_ConsequenceMapHasNoEventDataInDetails_Then_ShouldReturnTriggeringEventData() throws
		Exception {
		// setup
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventAttachData());
		Map<String, Variant> newDetailMap = consequenceMap.get(
												RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL).getVariantMap();
		newDetailMap.remove(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_EVENT_DATA);
		consequenceMap.remove(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL);
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL, Variant.fromVariantMap(newDetailMap));
		Event triggeringEvent = GetTriggeringEvent();
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());

		// test
		_rulesEngine.processAttachDataConsequence(consequenceMap, triggeringEvent);

		// verify
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());
	}

	// =================================================================================================================
	// protected void processModifyDataConsequence(final Map<String, Variant> consequenceMap, final Event triggeringEvent)
	// =================================================================================================================
	@Test
	@Ignore
	public void processModifyDataConsequence_When_Happy_Then_ShouldAttachDataButNotOverwriteExistingData() throws
		Exception {
		// setup
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventModifyData());
		Event triggeringEvent = GetTriggeringEvent();

		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());

		// test
		_rulesEngine.processModifyDataConsequence(consequenceMap, triggeringEvent);

		// verify
		final EventData newData = triggeringEvent.getData();
		assertNotNull(newData);
		assertEquals(2, newData.size());
		assertFalse(newData.containsKey("attachedKey"));
		assertFalse(triggeringEvent.getData().containsKey("value1"));
		assertEquals("value2", newData.optString("key2", ""));
		assertEquals(552, newData.optInteger("anInt", 0));
		assertFalse(newData.containsKey("aMap"));
		assertFalse(newData.containsKey("aList"));
	}

	public void processModifyDataConsequence_With_ListOfObjects_ShouldAttachDataButNotOverwriteExistingData() throws
		Exception {
		// setup
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventModifyData());
		Event triggeringEvent = GetEventWithListOfObjects();

		assertEquals(6, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());

		// test
		_rulesEngine.processModifyDataConsequence(consequenceMap, triggeringEvent);

		// verify
		final EventData newData = triggeringEvent.getData();
		assertNotNull(newData);
		assertEquals(3, newData.size());
		assertFalse(newData.containsKey("attachedKey"));
		assertFalse(triggeringEvent.getData().containsKey("value1"));
		assertEquals("value2", newData.optString("key2", ""));
		assertEquals(552, newData.optInteger("anInt", 0));
		assertFalse(newData.containsKey("aMap"));
		assertFalse(newData.containsKey("aList"));
		assertEquals("orange", newData.optVariantList("listOfObjects",
					 null).get(0).optVariantMap(null).get("details").optVariantMap(null).get("color").optString(null));
		assertTrue(newData.optVariantList("listOfObjects", null).get(1).optVariantMap(null).containsKey("details"));
	}

	@Test
	public void processModifyDataConsequence_When_NullConsequenceEvent() throws Exception {
		Event triggeringEvent = GetTriggeringEvent();
		assertEquals(5, triggeringEvent.getData().size());
		_rulesEngine.processModifyDataConsequence(null, triggeringEvent);
		assertEquals(5, triggeringEvent.getData().size());
	}

	@Test
	public void processModifyDataConsequence_When_NullConsequenceEvent_Then_ShouldReturnTriggeringEventData() throws
		Exception {
		// setup
		Event triggeringEvent = GetTriggeringEvent();
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());

		// test
		_rulesEngine.processModifyDataConsequence(null, triggeringEvent);

		// verify
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());
	}

	@Test
	public void processModifyDataConsequence_When_EmptyConsequenceEvent_Then_ShouldReturnTriggeringEventData() throws
		Exception {
		// setup
		Event triggeringEvent = GetTriggeringEvent();
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());

		// test
		_rulesEngine.processModifyDataConsequence(new HashMap<String, Variant>(), triggeringEvent);

		// verify
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());
	}

	@Test
	public void processModifyDataConsequence_When_NullTriggeringEvent_Then_ShouldNoOp() {
		// setup
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventModifyData());

		// test
		_rulesEngine.processAttachDataConsequence(consequenceMap, null);

		// verify
		// no-op
	}

	@Test
	public void processModifyDataConsequence_When_ConsequenceMapHasNoDetail_Then_ShouldReturnTriggeringEventData() throws
		Exception {
		// setup
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventModifyData());
		consequenceMap.remove(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL);
		Event triggeringEvent = GetTriggeringEvent();
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());

		// test
		_rulesEngine.processModifyDataConsequence(consequenceMap, triggeringEvent);

		// verify
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());
	}

	@Test
	public void
	processModifyDataConsequence_When_ConsequenceMapHasNoEventDataInDetails_Then_ShouldReturnTriggeringEventData() throws
		Exception {
		// setup
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventModifyData());
		Map<String, Variant> newDetailMap = consequenceMap.get(
												RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL).getVariantMap();
		newDetailMap.remove(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_EVENT_DATA);
		consequenceMap.remove(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL);
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL, Variant.fromVariantMap(newDetailMap));
		Event triggeringEvent = GetTriggeringEvent();
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());

		// test
		_rulesEngine.processModifyDataConsequence(consequenceMap, triggeringEvent);

		// verify
		assertEquals(5, triggeringEvent.getData().size());
		assertFalse(triggeringEvent.getData().containsKey("attachedKey"));
		assertEquals("value1", triggeringEvent.getData().optString("&&key1", ""));
		assertEquals("value2", triggeringEvent.getData().optString("key2", ""));
		assertEquals(552, triggeringEvent.getData().optInteger("anInt", 0));
		assertEquals("embeddedStringValue",
					 triggeringEvent.getData().getVariantMap("aMap").get("&&embeddedString").convertToString());
		assertEquals("stringInList", triggeringEvent.getData().getVariantList("aList").get(0).convertToString());
	}

	// =================================================================================================================
	// protected Event processDispatchConsequence(final Map<String, Variant> consequence, final Event triggeringEvent)
	// =================================================================================================================
	@Test
	public void processDispatchConsequence_When_Copy_Happy_Then_ShouldReturnNewEvent() throws
		Exception {
		// setup
		String testEventType = "new.type.dispatched";
		String testEventSource = "new.source.dispatched";
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventDispatch(testEventType,
											  testEventSource, RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_COPY, null));
		Event triggeringEvent = GetTriggeringEvent();
		// mock other Event properties
		triggeringEvent.setPairId("abcPairId");
		triggeringEvent.setEventNumber(10);
		Thread.sleep(5); // sleep 5ms to avoid having same timestamp for the result event

		// test
		Event result = _rulesEngine.processDispatchConsequence(consequenceMap, triggeringEvent, 0);

		// verify a new event was created with same data, new type and source
		assertEquals(testEventType, result.getType());
		assertEquals(testEventSource, result.getSource());
		assertEquals(triggeringEvent.getEventData(), result.getEventData());
		assertEquals(0, result.getEventNumber()); // event not dispatched yet, default 0
		assertNull(result.getPairID()); // pair ID not copied from the original event
		assertNotEquals(triggeringEvent.getResponsePairID(), result.getResponsePairID()); // new response pair ID is generated
		assertNotEquals(triggeringEvent.getTimestamp(), result.getTimestamp());
	}

	@Test
	public void processDispatchConsequence_When_NewEmptyData_Happy_Then_ShouldReturnNewEvent() throws
		Exception {
		// setup
		String testEventType = "new.type.dispatched";
		String testEventSource = "new.source.dispatched";
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventDispatch(testEventType,
											  testEventSource, RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_NEW, null));
		Event triggeringEvent = GetTriggeringEvent();
		// mock other Event properties
		triggeringEvent.setPairId("abcPairId");
		triggeringEvent.setEventNumber(10);
		Thread.sleep(5); // sleep 5ms to avoid having same timestamp for the result event

		// test
		Event result = _rulesEngine.processDispatchConsequence(consequenceMap, triggeringEvent, 0);

		// verify a new event was created with empty data, new type and source
		assertEquals(testEventType, result.getType());
		assertEquals(testEventSource, result.getSource());
		assertEquals(0, result.getEventData().size());
		assertEquals(0, result.getEventNumber()); // event not dispatched yet, default 0
		assertNull(result.getPairID()); // pair ID not copied from the original event
		assertNotEquals(triggeringEvent.getResponsePairID(), result.getResponsePairID()); // new response pair ID is generated
		assertNotEquals(triggeringEvent.getTimestamp(), result.getTimestamp());
	}

	@Test
	public void processDispatchConsequence_When_NewWithData_Happy_Then_ShouldReturnNewEvent() throws
		Exception {
		// setup
		String testEventType = "new.type.dispatched";
		String testEventSource = "new.source.dispatched";
		Map<String, Variant> data = new HashMap<String, Variant>();
		data.put("key1", Variant.fromString("value1"));
		data.put("key2", Variant.fromInteger(2));
		List<String> stringList = new ArrayList<String>() {
			{
				add("a");
				add("b");
				add(null);
			}
		};
		data.put("key3", Variant.fromStringList(stringList));
		Map<String, Object> objectMap = new HashMap<String, Object>() {
			{
				put("level2key1", "value1");
				put("level2key2", "value2");
				put("level2key3", 3);
			}
		};
		data.put("key4", Variant.fromTypedMap(objectMap, PermissiveVariantSerializer.DEFAULT_INSTANCE));
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventDispatch(testEventType,
											  testEventSource, RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_NEW, data));
		Event triggeringEvent = GetTriggeringEvent();
		// mock other Event properties
		triggeringEvent.setPairId("abcPairId");
		triggeringEvent.setEventNumber(10);
		Thread.sleep(5); // sleep 5ms to avoid having same timestamp for the result event

		// test
		Event result = _rulesEngine.processDispatchConsequence(consequenceMap, triggeringEvent, 0);

		// verify a new event was created with new data, new type and source
		assertEquals(testEventType, result.getType());
		assertEquals(testEventSource, result.getSource());
		assertEquals(4, result.getEventData().size());
		assertEquals("value1", result.getData().optString("key1", ""));
		assertEquals(2, result.getData().optInteger("key2", 0));
		assertEquals(stringList, result.getData().optStringList("key3", null));
		assertEquals(Variant.toVariantMap(objectMap), result.getData().optVariantMap("key4", null));
		assertEquals(0, result.getEventNumber()); // event not dispatched yet, default 0
		assertNull(result.getPairID()); // pair ID not copied from the original event
		assertNotEquals(triggeringEvent.getResponsePairID(), result.getResponsePairID()); // new response pair ID is generated
		assertNotEquals(triggeringEvent.getTimestamp(), result.getTimestamp());

		// verify event data is copied
		data.remove("key2");
		assertEquals(4, result.getEventData().size());
	}

	@Test
	public void processDispatchConsequence_When_NullConsequenceEvent_Then_ShouldReturnNull() throws
		Exception {
		// setup
		Event triggeringEvent = GetTriggeringEvent();

		// test
		Event result = _rulesEngine.processDispatchConsequence(null, triggeringEvent, 0);

		// verify
		assertNull(result);
	}

	@Test
	public void processDispatchConsequence_When_EmptyConsequenceEvent_Then_ShouldReturnNull() throws
		Exception {
		// setup
		Event triggeringEvent = GetTriggeringEvent();

		// test
		Event result = _rulesEngine.processDispatchConsequence(new HashMap<String, Variant>(), triggeringEvent, 0);

		// verify
		assertNull(result);
	}

	@Test
	public void processDispatchConsequence_When_NullTriggeringEvent_Then_ShouldReturnNull() {
		// setup
		String testEventType = "new.type.dispatched";
		String testEventSource = "new.source.dispatched";
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventDispatch(testEventType,
											  testEventSource, RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_COPY, null));

		// test
		Event result = _rulesEngine.processDispatchConsequence(consequenceMap, null, 0);

		// verify
		assertNull(result);
	}

	@Test
	public void processDispatchConsequence_When_InvalidDispatchConsequenceTypeSource_Then_ShouldReturnNull() throws
		Exception {
		// setup
		String testEventType = "new.type.dispatched";
		String testEventSource = "new.source.dispatched";
		Event triggeringEvent = GetTriggeringEvent();

		// test & verify - detail missing
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventDispatch(testEventType,
											  testEventSource, RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_COPY, null));
		consequenceMap.remove("detail");
		Event result = _rulesEngine.processDispatchConsequence(consequenceMap, triggeringEvent, 0);
		assertNull(result);

		// test & verify - event type missing
		consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventDispatch(null, testEventSource,
						 RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_COPY, null));
		result = _rulesEngine.processDispatchConsequence(consequenceMap, triggeringEvent, 0);
		assertNull(result);

		// test & verify - event type empty
		consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventDispatch("", testEventSource,
						 RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_COPY, null));
		result = _rulesEngine.processDispatchConsequence(consequenceMap, triggeringEvent, 0);
		assertNull(result);

		// test & verify - event source missing
		consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventDispatch(testEventType, null,
						 RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_COPY, null));
		result = _rulesEngine.processDispatchConsequence(consequenceMap, triggeringEvent, 0);
		assertNull(result);

		// test & verify - event source empty
		consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventDispatch(testEventType, "",
						 RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_COPY, null));
		result = _rulesEngine.processDispatchConsequence(consequenceMap, triggeringEvent, 0);
		assertNull(result);
	}

	@Test
	public void processDispatchConsequence_When_InvalidDispatchType_Then_ShouldReturnNull() throws
		Exception {
		// setup
		String testEventType = "new.type.dispatched";
		String testEventSource = "new.source.dispatched";
		Event triggeringEvent = GetTriggeringEvent();

		// test & verify - eventdataaction missing
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventDispatch(testEventType,
											  testEventSource, null, null));
		Event result = _rulesEngine.processDispatchConsequence(consequenceMap, triggeringEvent, 0);
		assertNull(result);

		// test & verify - eventdataaction unknown
		consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventDispatch(testEventType,
						 testEventSource, "unknown", null));
		result = _rulesEngine.processDispatchConsequence(consequenceMap, triggeringEvent, 0);
		assertNull(result);
	}

	@Test
	public void processDispatchConsequence_When_MetMaxChainLength_Then_ShouldReturnNull() throws
		Exception {
		// setup
		String testEventType = "new.type.dispatched";
		String testEventSource = "new.source.dispatched";
		Map<String, Variant> consequenceMap = GetConsequenceMapFromConsequenceEvent(GetConsequenceEventDispatch(testEventType,
											  testEventSource, RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_NEW, null));
		Event triggeringEvent = GetTriggeringEvent();

		// test&verify
		Event result = _rulesEngine.processDispatchConsequence(consequenceMap, triggeringEvent,
					   RulesEngineConstants.MAX_CHAINED_CONSEQUENCE_COUNT);
		assertNull(result);

		result = _rulesEngine.processDispatchConsequence(consequenceMap, triggeringEvent,
				 RulesEngineConstants.MAX_CHAINED_CONSEQUENCE_COUNT + 1);
		assertNull(result);
	}

	// =================================================================================================================
	// protected EventData getTokenExpandedEventData(final EventData eventData, final Event triggerEvent)
	// =================================================================================================================

	// =================================================================================================================
	// protected Map<String, Object> getTokenExpandedMap(final Map<String, Object> mapWithTokens, final Event event)
	// =================================================================================================================
	@Test
	public void getTokenExpanedMapHappyNested() {
		// setup
		String keyInTriggeringEvent = "\"{%aMap.&&embeddedString%}\"";
		String valueForKey1 = "embeddedStringValue";
		Map<String, Object> variantMap = new HashMap<String, Object>();
		variantMap.put("igotreplaced", keyInTriggeringEvent);

		Map<String, Object> consequenceDefinitionMap = new HashMap<String, Object>();
		consequenceDefinitionMap.put("maptype", variantMap);

		// test
		final Map<String, Object> result = _rulesEngine.getTokenExpandedMap(consequenceDefinitionMap, GetTriggeringEvent());

		// verify
		assertEquals(1, result.size());
		Map<String, Object> returnedMap = (Map<String, Object>) result.get("maptype");
		assertEquals("\"" + valueForKey1 + "\"", (String) returnedMap.get("igotreplaced"));
	}


	// =================================================================================================================
	// protected List<Object> getTokenExpandedList(final List<Object> listWithTokens, final Event event)
	// =================================================================================================================


	// =================================================================================================================
	// Private helpers for this test
	// =================================================================================================================
	private void setEmptyConsequenceFor(final MockRule mockRule) {
		EventData eventData = new EventData();
		Map<String, Variant> consequenceMap = new HashMap<String, Variant>();
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_JSON_ID, Variant.fromString("analyticsId"));
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_JSON_TYPE, Variant.fromString(""));
		Map<String, String> detailMap = new HashMap<String, String>();
		detailMap.put("key3", "{%&&key1%}");
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL, Variant.fromStringMap(detailMap));
		eventData.putVariantMap(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_TRIGGERED, consequenceMap);

		final Event eventWithNoConsequenceType = new Event.Builder("Test", EventType.ANALYTICS,
				EventSource.REQUEST_CONTENT).setData(eventData).build();
		final ArrayList<Event> mockEvents = new ArrayList<Event>();
		mockEvents.add(eventWithNoConsequenceType);
		mockRule.setConsequenceEvents(mockEvents);
	}

	private void setNoTypeConsequenceFor(final MockRule mockRule) {
		EventData eventData = new EventData();
		Map<String, Variant> consequenceMap = new HashMap<String, Variant>();
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_JSON_ID, Variant.fromString("analyticsId"));
		Map<String, String> detailMap = new HashMap<String, String>();
		detailMap.put("key3", "{%&&key1%}");
		consequenceMap.put(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_DETAIL, Variant.fromStringMap(detailMap));
		eventData.putVariantMap(RulesEngineConstantsTests.EventDataKeys.CONSEQUENCE_TRIGGERED, consequenceMap);

		final Event eventWithNoConsequenceType = new Event.Builder("Test", EventType.ANALYTICS,
				EventSource.REQUEST_CONTENT).setData(eventData).build();

		final ArrayList<Event> mockEvents = new ArrayList<Event>();
		mockEvents.add(eventWithNoConsequenceType);
		mockRule.setConsequenceEvents(mockEvents);
	}
}
