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

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import com.adobe.marketing.mobile.JsonUtilityService.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class ConfigurationDataTest {

	private ConfigurationData configurationData;
	private FakeJsonUtilityService fakeJsonUtilityService;

	@Before
	public void setup() {
		// Initialize Module
		fakeJsonUtilityService = new FakeJsonUtilityService();
		configurationData = new ConfigurationData(fakeJsonUtilityService);
	}


	// ===============================================================
	// public ConfigurationData put(final String jsonString)
	// ===============================================================

	@Test
	public void testPutJSONString_when_NullString() {
		String testString = null;
		configurationData.put(testString);
		//verify
		assertTrue(configurationData.getEventData().isEmpty());
	}

	@Test
	public void testPutJSONString_when_EmptyString() {
		configurationData.put("");
		//verify
		assertTrue(configurationData.getEventData().isEmpty());
	}

	@Test
	public void testPutJSONString_when_InvalidJSON() {
		configurationData.put("InvalidJSON");
		//verify
		assertTrue(configurationData.getEventData().isEmpty());
	}

	@Test
	public void testPutJSONString_when_ValidJSON() {
		configurationData.put(sampleJSONString());
		//verify
		Integer value = configurationData.getEventData().optInteger("IntegerKey", 0);
		assertEquals(3, configurationData.getEventData().size());
		assertEquals("StringValue", configurationData.getEventData().optString("StringKey", null));
		assertEquals(5, ((Number)configurationData.getEventData().optInteger("IntegerKey", 0)));
		assertFalse(configurationData.getEventData().optBoolean("BooleanKey", true));
	}

	@Test
	public void testPutJSONString_when_preLoaded_appendsNewKey() {
		preloadConfigurationData();
		configurationData.put(sampleJSONString());
		//verify
		assertEquals(6, configurationData.getEventData().size());
	}

	@Test
	public void testPutJSONString_when_preLoaded_OverridesExistingKey() {
		preloadConfigurationData();
		configurationData.put(overridingJSONString());
		//verify
		assertEquals(3, configurationData.getEventData().size());
		assertEquals("newStringValue", configurationData.getEventData().optString("preloadedStringKey", null));
		assertEquals(10, configurationData.getEventData().optInteger("preloadedIntegerKey", 0));
		assertTrue(configurationData.getEventData().optBoolean("preloadedBooleanKey", false));
	}

	@Test
	public void testPutJSONString_when_JSONUtilityService_NotAvailable_shouldNotCrash() {
		configurationData = new ConfigurationData(null);
		configurationData.put(sampleJSONString());
		//verify
		int value = configurationData.getEventData().optInteger("IntegerKey", 0);
		assertEquals(0, configurationData.getEventData().size());
	}


	// =============================================================================
	// public ConfigurationData put(final JsonUtilityService.JSONObject jsonObject)
	// =============================================================================

	@Test
	public void testPutJSONObject_when_Null() {
		JSONObject testObject = null;
		configurationData.put(testObject);
		//verify
		assertTrue(configurationData.getEventData().isEmpty());
	}

	@Test
	public void testPutJSONObject_when_Empty() {
		JSONObject testObject = fakeJsonUtilityService.createJSONObject("{}");
		configurationData.put(testObject);
		//verify
		assertTrue(configurationData.getEventData().isEmpty());
	}

	@Test
	public void testPutJSONObject_when_Invalid() {
		JSONObject testObject = fakeJsonUtilityService.createJSONObject("{InvalidJSON}");
		configurationData.put(testObject);
		//verify
		assertTrue(configurationData.getEventData().isEmpty());
	}

	@Test
	public void testPutJSONObject_when_Valid() {
		JSONObject testObject = fakeJsonUtilityService.createJSONObject(sampleJSONString());
		configurationData.put(testObject);
		//verify
		assertEquals(3, configurationData.getEventData().size());
		assertEquals("StringValue", configurationData.getEventData().optString("StringKey", null));
		assertEquals(5, configurationData.getEventData().optInteger("IntegerKey", 0));
		assertFalse(configurationData.getEventData().optBoolean("BooleanKey", true));
	}

	@Test
	public void testPutJSONObject_when_preLoaded_appendsNewKey() {
		preloadConfigurationData();
		JSONObject testObject = fakeJsonUtilityService.createJSONObject(sampleJSONString());
		configurationData.put(testObject);
		//verify
		assertEquals(6, configurationData.getEventData().size());
	}

	@Test
	public void testPutJSONObject_when_preLoaded_OverridesExistingKey() {
		preloadConfigurationData();
		JSONObject testObject = fakeJsonUtilityService.createJSONObject(overridingJSONString());
		configurationData.put(testObject);
		//verify
		assertEquals(3, configurationData.getEventData().size());
		assertEquals("newStringValue", configurationData.getEventData().optString("preloadedStringKey", null));
		assertEquals(10, configurationData.getEventData().optInteger("preloadedIntegerKey", 0));
		assertTrue(configurationData.getEventData().optBoolean("preloadedBooleanKey", false));
	}

	@Test
	public void testPutJSONObject_Environment_Dev() {
		JSONObject testObject = fakeJsonUtilityService.createJSONObject(environmentDevJSONString());
		configurationData.put(testObject);
		EventData eventdata = configurationData.getEventData();
		//verify
		assertNotEquals(null, eventdata);
		assertEquals(2, eventdata.size());
		assertEquals("dev", eventdata.optString("build.environment", ""));
		assertEquals("myDevValue", eventdata.optString("myKey", ""));
	}

	@Test
	public void testPutJSONObject_Environment_Stage() {
		JSONObject testObject = fakeJsonUtilityService.createJSONObject(environmentStageJSONString());
		configurationData.put(testObject);
		EventData eventdata = configurationData.getEventData();
		//verify
		assertNotEquals(null, eventdata);
		assertEquals(2, eventdata.size());
		assertEquals("stage", eventdata.optString("build.environment", ""));
		assertEquals("myStageValue", eventdata.optString("myKey", ""));
	}

	@Test
	public void testPutJSONObject_Environment_Prod() {
		JSONObject testObject = fakeJsonUtilityService.createJSONObject(environmentProdJSONString());
		configurationData.put(testObject);
		EventData eventdata = configurationData.getEventData();
		//verify
		assertNotEquals(null, eventdata);
		assertEquals(2, eventdata.size());
		assertEquals("prod", eventdata.optString("build.environment", ""));
		assertEquals("myValue", eventdata.optString("myKey", ""));
	}

	@Test
	public void testPutJSONObject_Environment_None() {
		JSONObject testObject = fakeJsonUtilityService.createJSONObject(environmentNoneJSONString());
		configurationData.put(testObject);
		EventData eventdata = configurationData.getEventData();
		//verify
		assertNotEquals(null, eventdata);
		assertEquals(1, eventdata.size());
		assertEquals("myValue", eventdata.optString("myKey", ""));
	}

	@Test
	public void testPutJSONObject_ComplexEnvironment_Dev() {
		JSONObject testObject = fakeJsonUtilityService.createJSONObject(environmentComplexJSONString("dev"));
		configurationData.put(testObject);
		EventData eventdata = configurationData.getEventData();
		//verify
		assertNotEquals(null, eventdata);
		assertEquals(11, eventdata.size());
		assertEquals("dev", eventdata.optString("build.environment", ""));
		assertEquals("@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-dev-campaignkey", eventdata.optString("campaign.pkey", ""));
		assertEquals("dev.mcias.campaign-demo.adobe.com", eventdata.optString("campaign.server", ""));
	}

	@Test
	public void testPutJSONObject_ComplexEnvironment_Stage() {
		JSONObject testObject = fakeJsonUtilityService.createJSONObject(environmentComplexJSONString("stage"));
		configurationData.put(testObject);
		EventData eventdata = configurationData.getEventData();
		//verify
		assertNotEquals(null, eventdata);
		assertEquals(11, eventdata.size());
		assertEquals("stage", eventdata.optString("build.environment", ""));
		assertEquals("@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-stage-campaignkey", eventdata.optString("campaign.pkey", ""));
		assertEquals("stage.mcias.campaign-demo.adobe.com", eventdata.optString("campaign.server", ""));
	}

	@Test
	public void testPutJSONObject_ComplexEnvironment_Prod() {
		JSONObject testObject = fakeJsonUtilityService.createJSONObject(environmentComplexJSONString("prod"));
		configurationData.put(testObject);
		EventData eventdata = configurationData.getEventData();
		//verify
		assertNotEquals(null, eventdata);
		assertEquals(11, eventdata.size());
		assertEquals("prod", eventdata.optString("build.environment", ""));
		assertEquals("@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-campaignkey", eventdata.optString("campaign.pkey", ""));
		assertEquals("mcias.campaign-demo.adobe.com", eventdata.optString("campaign.server", ""));
	}

	// =============================================================================
	// public ConfigurationData put(final Map<String, Object> map)
	// =============================================================================

	@Test
	public void testPutMap_when_Null() {
		HashMap<String, Variant> testObject = null;
		configurationData.put(testObject);
		//verify
		assertTrue(configurationData.getEventData().isEmpty());
	}

	@Test
	public void testPutMap_when_Empty() {
		HashMap<String, Variant> testObject = new HashMap<String, Variant>();
		configurationData.put(testObject);
		//verify
		assertTrue(configurationData.getEventData().isEmpty());
	}

	@Test
	public void testPutMap_when_NullKeyInMap() {
		HashMap<String, Variant> testObject = new HashMap<String, Variant>();
		testObject.put(null, Variant.fromString("StringValue"));
		testObject.put("IntegerKey", Variant.fromInteger(2));
		testObject.put("BooleanKey", Variant.fromBoolean(true));
		configurationData.put(testObject);
		//verify
		assertEquals(2, configurationData.getEventData().size());
		assertEquals(2, configurationData.getEventData().optInteger("IntegerKey", 0));
		assertTrue(configurationData.getEventData().optBoolean("BooleanKey", false));
	}

	@Test
	public void testPutMap_when_Valid() {
		HashMap<String, Variant> testObject = new HashMap<String, Variant>();
		testObject.put("StringKey", Variant.fromString("StringValue"));
		testObject.put("IntegerKey", Variant.fromInteger(2));
		testObject.put("BooleanKey", Variant.fromBoolean(true));
		configurationData.put(testObject);
		//verify
		assertEquals(3, configurationData.getEventData().size());
		assertEquals("StringValue", configurationData.getEventData().optString("StringKey", null));
		assertEquals(2, configurationData.getEventData().optInteger("IntegerKey", 0));
		assertTrue(configurationData.getEventData().optBoolean("BooleanKey", false));
	}

	@Test
	public void testPutMap_when_preLoaded_appendsNewKey() {
		preloadConfigurationData();
		HashMap<String, Variant> testObject = new HashMap<String, Variant>();
		testObject.put("StringKey", Variant.fromString("StringValue"));
		testObject.put("IntegerKey", Variant.fromInteger(2));
		testObject.put("BooleanKey", Variant.fromBoolean(true));
		configurationData.put(testObject);
		//verify
		assertEquals(6, configurationData.getEventData().size());
	}

	@Test
	public void testPutMap_when_preLoaded_OverridesExistingKey() {
		preloadConfigurationData();
		HashMap<String, Variant> testObject = new HashMap<String, Variant>();
		testObject.put("preloadedStringKey", Variant.fromString("newValue"));
		testObject.put("preloadedIntegerKey", Variant.fromInteger(10));
		testObject.put("preloadedBooleanKey", Variant.fromBoolean(true));
		configurationData.put(testObject);
		//verify
		assertEquals(3, configurationData.getEventData().size());
		assertEquals("newValue", configurationData.getEventData().optString("preloadedStringKey", null));
		assertEquals(10, configurationData.getEventData().optInteger("preloadedIntegerKey", 0));
		assertTrue(configurationData.getEventData().optBoolean("preloadedBooleanKey", false));
	}

	// =============================================================================
	// public ConfigurationData put(final ConfigurationData configData)
	// =============================================================================
	@Test
	public void testPutConfigData_when_Null() {
		ConfigurationData testData = null;
		configurationData.put(testData);
		//verify
		assertTrue(configurationData.getEventData().isEmpty());
	}

	@Test
	public void testPutConfigData_when_Empty() {
		ConfigurationData testData = new ConfigurationData(fakeJsonUtilityService);
		configurationData.put(testData);
		//verify
		assertTrue(configurationData.getEventData().isEmpty());
	}

	@Test
	public void testPutConfigData_when_Valid() {
		ConfigurationData testData = new ConfigurationData(fakeJsonUtilityService).put(sampleJSONString());
		configurationData.put(testData);
		//verify
		assertEquals(3, configurationData.getEventData().size());
		assertEquals("StringValue", configurationData.getEventData().optString("StringKey", null));
		assertEquals(5, configurationData.getEventData().optInteger("IntegerKey", 0));
		assertFalse(configurationData.getEventData().optBoolean("BooleanKey", true));
	}

	@Test
	public void testPutConfigData_when_preLoaded_appendsNewKey() {
		preloadConfigurationData();
		ConfigurationData testData = new ConfigurationData(fakeJsonUtilityService).put(sampleJSONString());
		configurationData.put(testData);
		//verify
		assertEquals(6, configurationData.getEventData().size());
	}

	@Test
	public void testPutConfigData_when_preLoaded_OverridesExistingKey() {
		preloadConfigurationData();
		ConfigurationData testData = new ConfigurationData(fakeJsonUtilityService).put(overridingJSONString());
		configurationData.put(testData);
		//verify
		assertEquals(3, configurationData.getEventData().size());
		assertEquals("newStringValue", configurationData.getEventData().optString("preloadedStringKey", null));
		assertEquals(10, configurationData.getEventData().optInteger("preloadedIntegerKey", 0));
		assertTrue(configurationData.getEventData().optBoolean("preloadedBooleanKey", false));
	}

	// =============================================================================
	// public String getJSONString()
	// =============================================================================

	@Test
	public void testGetJSONString() {
		configurationData.put(sampleJSONString());
		ConfigurationData newConfig = new ConfigurationData(fakeJsonUtilityService);
		String jsonString = configurationData.getJSONString();
		// verify
		assertTrue(jsonString.contains("\"BooleanKey\":"));
		assertTrue(jsonString.contains("false"));
		assertTrue(jsonString.contains("\"StringKey\":"));
		assertTrue(jsonString.contains("\"StringValue"));
		assertTrue(jsonString.contains("\"IntegerKey\":"));
		assertTrue(jsonString.contains("5"));

	}

	// =============================================================================
	// public boolean isEmpty()
	// =============================================================================
	@Test
	public void testisEmpty_when_MapEmpty() {
		assertTrue(configurationData.isEmpty());
	}

	@Test
	public void testisEmpty_when_MapNotEmpty() {
		configurationData.put(sampleJSONString());
		assertFalse(configurationData.isEmpty());
	}

	// =============================================================================
	// Private Helper Methods
	// =============================================================================

	private void preloadConfigurationData() {
		configurationData.put("{\"preloadedStringKey\":\"preloadedStringvalue\"" +
							  ",\"preloadedIntegerKey\":2 ," +
							  "\"preloadedBooleanKey\":false}");
	}

	private String sampleJSONString() {
		return "{\"StringKey\":\"StringValue\"" +
			   ",\"IntegerKey\":5," +
			   "\"BooleanKey\":false}";
	}

	private String overridingJSONString() {
		return "{\"preloadedStringKey\":\"newStringValue\"" +
			   ",\"preloadedIntegerKey\":10" +
			   ",\"preloadedBooleanKey\":true}";
	}

	private String environmentDevJSONString() {
		return "{\"build.environment\":\"dev\"" +
			   ",\"myKey\":\"myValue\"" +
			   ",\"__stage__myKey\":\"myStageValue\"" +
			   ",\"__dev__myKey\":\"myDevValue\"}";
	}

	private String environmentStageJSONString() {
		return "{ \"build.environment\":\"stage\"" +
			   ", \"myKey\":\"myValue\"" +
			   ", \"__stage__myKey\":\"myStageValue\"" +
			   ", \"__dev__myKey\":\"myDevValue\" }";
	}

	private String environmentProdJSONString() {
		return "{ \"build.environment\":\"prod\"" +
			   ", \"myKey\":\"myValue\"" +
			   ", \"__stage__myKey\":\"myStageValue\"" +
			   ", \"__dev__myKey\":\"myDevValue\" }";
	}

	private String environmentNoneJSONString() {
		return "{ \"myKey\":\"myValue\"" +
			   ", \"__stage__myKey\":\"myStageValue\"" +
			   ", \"__dev__myKey\":\"myDevValue\" }";
	}

	private String environmentComplexJSONString(final String environment) {
		return "{ \"global.privacy\": \"optedin\"" +
			   ", \"__dev__campaign.pkey\": \"@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-dev-campaignkey\"" +
			   ", \"__stage__campaign.pkey\": \"@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-stage-campaignkey\"" +
			   ", \"campaign.timeout\": 5" +
			   ", \"__stage__campaign.server\": \"stage.mcias.campaign-demo.adobe.com\"" +
			   ", \"build.environment\":" + environment +
			   ", \"rules.url\": \"https://assets.adobedtm.com/staging/launch-EN1a264f7b23194f4db9d6256745f4c882-development-rules.zip\""
			   +
			   ", \"experienceCloud.org\": \"B1F855165B4C9EA50A495E06@AdobeOrg\"" +
			   ", \"lifecycle.sessionTimeout\": 3" +
			   ", \"campaign.mcias\": \"mcias-va7.cloud.adobe.io/mcias\"" +
			   ", \"campaign.pkey\": \"@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-campaignkey\"" +
			   ", \"campaign.server\": \"mcias.campaign-demo.adobe.com\"" +
			   ", \"__dev__campaign.server\": \"dev.mcias.campaign-demo.adobe.com\"" +
			   ", \"property.id\": \"PRdcd2f0f1ca414998b5ea07e5df469455\"" +
			   ", \"global.ssl\": true }";
	}

	private class MockObject {
		int objectIntVariable;
		String objectStringVariable;
	}

	// ================================================================================================================
	// getKeyForEnvironment
	// ================================================================================================================
	@Test
	public void testGetKeyForEnvironmentDev() {
		String baseString = "myKey";
		String environment = "dev";
		String result = configurationData.getKeyForEnvironment(baseString, environment);
		assertEquals("__dev__myKey", result);
	}

	@Test
	public void testGetKeyForEnvironmentStage() {
		String baseString = "myKey";
		String environment = "stage";
		String result = configurationData.getKeyForEnvironment(baseString, environment);
		assertEquals("__stage__myKey", result);
	}

	@Test
	public void testGetKeyForEnvironmentProd() {
		String baseString = "myKey";
		String environment = "prod";
		String result = configurationData.getKeyForEnvironment(baseString, environment);
		assertEquals("__prod__myKey", result);
	}

	@Test
	public void testGetKeyForEnvironmentSomeOtherEnvironment() {
		String baseString = "myKey";
		String environment = "somethingelse";
		String result = configurationData.getKeyForEnvironment(baseString, environment);
		assertEquals("__somethingelse__myKey", result);
	}
}
