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
// Todo
package com.adobe.marketing.mobile;
//
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.*;

//public class MobileIdentitiesTest extends BaseTest {
//
//	privaate ConfigurationExtension configurationExtension;
//	private JsonUtilityService jsonUtilityService;
//
//	@Before
//	public void beforeEach() {
//		super.beforeEach();
//		configurationExtension = new ConfigurationExtension(eventHub, platformServices);
//		jsonUtilityService = platformServices.fakeJsonUtilityService;
//	}
//
//
//	// ========================================================
//	// static String getAllIdentifiers(final JsonUtilityService jsonUtilityService,
//	//                                 final Event event, final Module module)
//	// ========================================================
//
//	// TODO uncomment after Configuration refactor
//	@Ignore
//	@Test
//	public void test_GetAllIdentifiers_Happy() {
//		// Setup
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//		setAllSharedState();
//
//		// test
//		String allIdentifiers = MobileIdentities.getAllIdentifiers(jsonUtilityService, event, configurationExtension);
//
//		// verify
//		assertEquals("{\"companyContexts\":" +
//					 "[{\"namespace\":\"imsOrgID\",\"value\":\"test_orgid\"}]," +
//					 "\"users\":" +
//					 "[{\"userIDs\":" +
//					 "[{\"namespace\":\"AVID\",\"type\":\"integrationCode\",\"value\":\"test_aid\"}," +
//					 "{\"namespace\":\"vid\",\"type\":\"analytics\",\"value\":\"test_vid\"}," +
//					 "{\"namespace\":\"tntid\",\"type\":\"target\",\"value\":\"test_tntid\"}," +
//					 "{\"namespace\":\"3rdpartyid\",\"type\":\"target\",\"value\":\"test_thirdpartyid\"}," +
//					 "{\"namespace\":\"test_dpid\",\"type\":\"namespaceId\",\"value\":\"test_dpuuid\"}," +
//					 "{\"namespace\":\"0\",\"type\":\"namespaceId\",\"value\":\"test_uuid\"}," +
//					 "{\"namespace\":\"4\",\"type\":\"namespaceId\",\"value\":\"test_mid\"}," +
//					 "{\"namespace\":\"type1\",\"type\":\"integrationCode\",\"value\":\"id1\"}," +
//					 "{\"namespace\":\"type2\",\"type\":\"integrationCode\",\"value\":\"id2\"}," +
//					 "{\"namespace\":\"DSID_20914\",\"type\":\"integrationCode\",\"value\":\"test_advertisingId\"}," +
//					 "{\"namespace\":\"20919\",\"type\":\"integrationCode\",\"value\":\"test_pushId\"}]}]}", allIdentifiers);
//	}
//
//	@Ignore
//	@Test
//	public void test_GetAllIdentifiers_WhenTargetStateNotAvailable() {
//		// Setup
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//		setAllSharedState();
//		eventHub.createSharedState("com.adobe.module.target", 2, new EventData());
//
//		// test
//		String allIdentifiers = MobileIdentities.getAllIdentifiers(jsonUtilityService, event, configurationExtension);
//
//		// verify
//		assertEquals("{\"companyContexts\":" +
//					 "[{\"namespace\":\"imsOrgID\",\"value\":\"test_orgid\"}]," +
//					 "\"users\":" +
//					 "[{\"userIDs\":" +
//					 "[{\"namespace\":\"AVID\",\"type\":\"integrationCode\",\"value\":\"test_aid\"}," +
//					 "{\"namespace\":\"vid\",\"type\":\"analytics\",\"value\":\"test_vid\"}," +
//					 "{\"namespace\":\"test_dpid\",\"type\":\"namespaceId\",\"value\":\"test_dpuuid\"}," +
//					 "{\"namespace\":\"0\",\"type\":\"namespaceId\",\"value\":\"test_uuid\"}," +
//					 "{\"namespace\":\"4\",\"type\":\"namespaceId\",\"value\":\"test_mid\"}," +
//					 "{\"namespace\":\"type1\",\"type\":\"integrationCode\",\"value\":\"id1\"}," +
//					 "{\"namespace\":\"type2\",\"type\":\"integrationCode\",\"value\":\"id2\"}," +
//					 "{\"namespace\":\"DSID_20914\",\"type\":\"integrationCode\",\"value\":\"test_advertisingId\"}," +
//					 "{\"namespace\":\"20919\",\"type\":\"integrationCode\",\"value\":\"test_pushId\"}]}]}", allIdentifiers);
//	}
//
//	@Ignore
//	@Test
//	public void test_GetAllIdentifiers_WhenAudienceStateNotAvailable() {
//		// Setup
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//		setAllSharedState();
//		eventHub.createSharedState("com.adobe.module.audience", 2, new EventData());
//
//		// test
//		String allIdentifiers = MobileIdentities.getAllIdentifiers(jsonUtilityService, event, configurationExtension);
//
//		// verify
//		assertEquals("{\"companyContexts\":" +
//					 "[{\"namespace\":\"imsOrgID\",\"value\":\"test_orgid\"}]," +
//					 "\"users\":" +
//					 "[{\"userIDs\":" +
//					 "[{\"namespace\":\"AVID\",\"type\":\"integrationCode\",\"value\":\"test_aid\"}," +
//					 "{\"namespace\":\"vid\",\"type\":\"analytics\",\"value\":\"test_vid\"}," +
//					 "{\"namespace\":\"tntid\",\"type\":\"target\",\"value\":\"test_tntid\"}," +
//					 "{\"namespace\":\"3rdpartyid\",\"type\":\"target\",\"value\":\"test_thirdpartyid\"}," +
//					 "{\"namespace\":\"4\",\"type\":\"namespaceId\",\"value\":\"test_mid\"}," +
//					 "{\"namespace\":\"type1\",\"type\":\"integrationCode\",\"value\":\"id1\"}," +
//					 "{\"namespace\":\"type2\",\"type\":\"integrationCode\",\"value\":\"id2\"}," +
//					 "{\"namespace\":\"DSID_20914\",\"type\":\"integrationCode\",\"value\":\"test_advertisingId\"}," +
//					 "{\"namespace\":\"20919\",\"type\":\"integrationCode\",\"value\":\"test_pushId\"}]}]}", allIdentifiers);
//	}
//
//	@Ignore
//	@Test
//	public void test_GetAllIdentifiers_WhenConfigurationStateNotAvailable() {
//		// Setup
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//		setAllSharedState();
//		eventHub.createSharedState("com.adobe.module.configuration", 2, new EventData());
//
//		// test
//		String allIdentifiers = MobileIdentities.getAllIdentifiers(jsonUtilityService, event, configurationExtension);
//
//		// verify
//		assertEquals("{\"users\":" +
//					 "[{\"userIDs\":" +
//					 "[{\"namespace\":\"AVID\",\"type\":\"integrationCode\",\"value\":\"test_aid\"}," +
//					 "{\"namespace\":\"vid\",\"type\":\"analytics\",\"value\":\"test_vid\"}," +
//					 "{\"namespace\":\"tntid\",\"type\":\"target\",\"value\":\"test_tntid\"}," +
//					 "{\"namespace\":\"3rdpartyid\",\"type\":\"target\",\"value\":\"test_thirdpartyid\"}," +
//					 "{\"namespace\":\"test_dpid\",\"type\":\"namespaceId\",\"value\":\"test_dpuuid\"}," +
//					 "{\"namespace\":\"0\",\"type\":\"namespaceId\",\"value\":\"test_uuid\"}," +
//					 "{\"namespace\":\"4\",\"type\":\"namespaceId\",\"value\":\"test_mid\"}," +
//					 "{\"namespace\":\"type1\",\"type\":\"integrationCode\",\"value\":\"id1\"}," +
//					 "{\"namespace\":\"type2\",\"type\":\"integrationCode\",\"value\":\"id2\"}," +
//					 "{\"namespace\":\"DSID_20914\",\"type\":\"integrationCode\",\"value\":\"test_advertisingId\"}," +
//					 "{\"namespace\":\"20919\",\"type\":\"integrationCode\",\"value\":\"test_pushId\"}]}]}", allIdentifiers);
//	}
//
//	@Ignore
//	@Test
//	public void test_GetAllIdentifiers_WhenAnalyticsStateNotAvailable() {
//		// Setup
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//		setAllSharedState();
//		eventHub.createSharedState("com.adobe.module.analytics", 2, new EventData());
//
//		// test
//		String allIdentifiers = MobileIdentities.getAllIdentifiers(jsonUtilityService, event, configurationExtension);
//
//		// verify
//		assertEquals("{\"companyContexts\":" +
//					 "[{\"namespace\":\"imsOrgID\",\"value\":\"test_orgid\"}]," +
//					 "\"users\":" +
//					 "[{\"userIDs\":[" +
//					 "{\"namespace\":\"tntid\",\"type\":\"target\",\"value\":\"test_tntid\"}," +
//					 "{\"namespace\":\"3rdpartyid\",\"type\":\"target\",\"value\":\"test_thirdpartyid\"}," +
//					 "{\"namespace\":\"test_dpid\",\"type\":\"namespaceId\",\"value\":\"test_dpuuid\"}," +
//					 "{\"namespace\":\"0\",\"type\":\"namespaceId\",\"value\":\"test_uuid\"}," +
//					 "{\"namespace\":\"4\",\"type\":\"namespaceId\",\"value\":\"test_mid\"}," +
//					 "{\"namespace\":\"type1\",\"type\":\"integrationCode\",\"value\":\"id1\"}," +
//					 "{\"namespace\":\"type2\",\"type\":\"integrationCode\",\"value\":\"id2\"}," +
//					 "{\"namespace\":\"DSID_20914\",\"type\":\"integrationCode\",\"value\":\"test_advertisingId\"}," +
//					 "{\"namespace\":\"20919\",\"type\":\"integrationCode\",\"value\":\"test_pushId\"}]}]}", allIdentifiers);
//	}
//
//	@Ignore
//	@Test
//	public void test_GetAllIdentifiers_WhenIdentityStateNotAvailable() {
//		// Setup
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//		setAllSharedState();
//		eventHub.createSharedState("com.adobe.module.identity", 2, new EventData());
//
//		// test
//		String allIdentifiers = MobileIdentities.getAllIdentifiers(jsonUtilityService, event, configurationExtension);
//
//		// verify
//		assertEquals("{\"companyContexts\":" +
//					 "[{\"namespace\":\"imsOrgID\",\"value\":\"test_orgid\"}]" +
//					 ",\"users\":" +
//					 "[{\"userIDs\":[" +
//					 "{\"namespace\":\"AVID\",\"type\":\"integrationCode\",\"value\":\"test_aid\"}," +
//					 "{\"namespace\":\"vid\",\"type\":\"analytics\",\"value\":\"test_vid\"}," +
//					 "{\"namespace\":\"tntid\",\"type\":\"target\",\"value\":\"test_tntid\"}," +
//					 "{\"namespace\":\"3rdpartyid\",\"type\":\"target\",\"value\":\"test_thirdpartyid\"}," +
//					 "{\"namespace\":\"test_dpid\",\"type\":\"namespaceId\",\"value\":\"test_dpuuid\"}," +
//					 "{\"namespace\":\"0\",\"type\":\"namespaceId\",\"value\":\"test_uuid\"}]}]}", allIdentifiers);
//	}
//
//	@Ignore
//	@Test
//	public void test_GetAllIdentifiers_WhenAllSharedStateAreInvalid() {
//		// Setup
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//		setAllSharedStateInvalid();
//
//		// test
//		String allIdentifiers = MobileIdentities.getAllIdentifiers(jsonUtilityService, event, configurationExtension);
//
//		// verify
//		assertEquals("{}", allIdentifiers);
//	}
//
//	@Ignore
//	@Test
//	public void test_GetAllIdentifiers_InvalidVisitorIDList_ShouldNotCrash() {
//		// Setup
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//		setAllSharedState();
//		EventData identity = new EventData();
//		identity.putString("visitoridslist", "InvalidShouldBeObject");
//		eventHub.createSharedState("com.adobe.module.identity", 2, identity);
//
//		// test
//		String allIdentifiers = MobileIdentities.getAllIdentifiers(jsonUtilityService, event, configurationExtension);
//
//		// verify
//		assertNotNull(allIdentifiers);
//	}
//
//	// ========================================================
//	// static boolean areAllSharedStatesReady(final Event event, final Module module)
//	// ========================================================
//
//	@Ignore
//	@Test
//	public void test_AreAllSharedStatesReady_WhenNoSharedStateSet() throws Exception {
//		// Setup
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//
//		// Test
//		boolean isReady = MobileIdentities.areAllSharedStatesReady(event, configurationExtension);
//
//		// Verify
//		assertTrue("areAllSharedStatesReady should return true", isReady);
//	}
//
//	@Ignore
//	@Test
//	public void test_AreAllSharedStatesReady_WhenALlSharedStateSetArePending() throws Exception {
//		// Setup
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//		setAllSharedStatePending();
//
//		// Test
//		boolean isReady = MobileIdentities.areAllSharedStatesReady(event, configurationExtension);
//
//		// Verify
//		assertFalse("areAllSharedStatesReady should return false", isReady);
//	}
//
//	@Ignore
//	@Test
//	public void test_AreAllSharedStatesReady_WhenAllSharedStateSet() throws Exception {
//		// Setup
//		setAllSharedState();
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//
//		// Test
//		boolean isReady = MobileIdentities.areAllSharedStatesReady(event, configurationExtension);
//
//		// Verify
//		assertTrue("areAllSharedStatesReady should return true", isReady);
//	}
//
//	@Ignore
//	@Test
//	public void test_AreAllSharedStatesReady_WhenAudienceStatePending() throws Exception {
//		// Setup
//		setAllSharedState();
//		eventHub.createSharedState("com.adobe.module.audience", 1, eventHub.SHARED_STATE_PENDING);
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//
//		// Test
//		boolean isReady = MobileIdentities.areAllSharedStatesReady(event, configurationExtension);
//
//		// Verify
//		assertFalse("areAllSharedStatesReady should return false", isReady);
//	}
//
//	@Ignore
//	@Test
//	public void test_AreAllSharedStatesReady_WhenAnalyticsStatePending() throws Exception {
//		// Setup
//		setAllSharedState();
//		eventHub.createSharedState("com.adobe.module.analytics", 1, eventHub.SHARED_STATE_PENDING);
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//
//		// Test
//		boolean isReady = MobileIdentities.areAllSharedStatesReady(event, configurationExtension);
//
//		// Verify
//		assertFalse("areAllSharedStatesReady should return false", isReady);
//	}
//
//	@Ignore
//	@Test
//	public void test_AreAllSharedStatesReady_WhenConfigurationStatePending() throws Exception {
//		// Setup
//		setAllSharedState();
//		eventHub.createSharedState("com.adobe.module.configuration", 1, eventHub.SHARED_STATE_PENDING);
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//
//		// Test
//		boolean isReady = MobileIdentities.areAllSharedStatesReady(event, configurationExtension);
//
//		// Verify
//		assertFalse("areAllSharedStatesReady should return false", isReady);
//	}
//
//	@Ignore
//	@Test
//	public void test_AreAllSharedStatesReady_WhenIdentityStatePending() throws Exception {
//		// Setup
//		setAllSharedState();
//		eventHub.createSharedState("com.adobe.module.identity", 1, eventHub.SHARED_STATE_PENDING);
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//
//		// Test
//		boolean isReady = MobileIdentities.areAllSharedStatesReady(event, configurationExtension);
//
//		// Verify
//		assertFalse("areAllSharedStatesReady should return false", isReady);
//	}
//
//	@Ignore
//	@Test
//	public void test_AreAllSharedStatesReady_WhenTargetStatePending() throws Exception {
//		// Setup
//		setAllSharedState();
//		eventHub.createSharedState("com.adobe.module.target", 1, eventHub.SHARED_STATE_PENDING);
//		Event event = new Event.Builder("EventHub", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
//		.setEventNumber(5)
//		.build();
//
//		// Test
//		boolean isReady = MobileIdentities.areAllSharedStatesReady(event, configurationExtension);
//
//		// Verify
//		assertFalse("areAllSharedStatesReady should return false", isReady);
//	}
//
//	// ========================================================
//	// Helper methods
//	// ========================================================
//	private void setAllSharedState() {
//		setIdentitySharedState();
//		setTargetSharedState();
//		setConfigurationSharedState();
//		setAudienceSharedState();
//		setAnalyticsSharedState();
//	}
//
//	private void setAllSharedStatePending() {
//		// set all shared state to pending
//		eventHub.createSharedState("com.adobe.module.configuration", 0, EventHub.SHARED_STATE_PENDING);
//		eventHub.createSharedState("com.adobe.module.analytics", 0, EventHub.SHARED_STATE_PENDING);
//		eventHub.createSharedState("com.adobe.module.audience", 0, EventHub.SHARED_STATE_PENDING);
//		eventHub.createSharedState("com.adobe.module.target", 0, EventHub.SHARED_STATE_PENDING);
//		eventHub.createSharedState("com.adobe.module.identity", 0, EventHub.SHARED_STATE_PENDING);
//	}
//
//	private void setAllSharedStateInvalid() {
//		// set all shared state to invalid
//		eventHub.createSharedState("com.adobe.module.configuration", 0, EventHub.SHARED_STATE_INVALID);
//		eventHub.createSharedState("com.adobe.module.analytics", 0, EventHub.SHARED_STATE_INVALID);
//		eventHub.createSharedState("com.adobe.module.audience", 0, EventHub.SHARED_STATE_INVALID);
//		eventHub.createSharedState("com.adobe.module.target", 0, EventHub.SHARED_STATE_INVALID);
//		eventHub.createSharedState("com.adobe.module.identity", 0, EventHub.SHARED_STATE_INVALID);
//	}
//
//	private void setIdentitySharedState() {
//		EventData identity = new EventData();
//		identity.putString("mid", "test_mid");
//		List<VisitorID> customerIds = new ArrayList<VisitorID>();
//		VisitorID visitor1 = new VisitorID("origin1", "type1", "id1", VisitorID.AuthenticationState.AUTHENTICATED);
//		VisitorID visitor2 = new VisitorID("origin2", "type2", "id2", VisitorID.AuthenticationState.LOGGED_OUT);
//		// Identity sets the advertising identifier both in ‘visitoridslist’ and as ‘advertisingidentifer’ in the Identity shared state.
//		VisitorID visitor3 = new VisitorID("origin2", "DSID_20914", "test_advertisingId",
//										   VisitorID.AuthenticationState.LOGGED_OUT);
//		customerIds.add(visitor1);
//		customerIds.add(visitor2);
//		customerIds.add(visitor3);
//		identity.putTypedList("visitoridslist", customerIds, new VisitorIDVariantSerializer());
//		identity.putString("advertisingidentifier", "test_advertisingId");
//		identity.putString("pushidentifier", "test_pushId");
//		eventHub.createSharedState("com.adobe.module.identity", 0, identity);
//	}
//
//	private void setConfigurationSharedState() {
//		EventData configuration = new EventData();
//		configuration.putString("experienceCloud.org", "test_orgid");
//		eventHub.createSharedState("com.adobe.module.configuration", 0, configuration);
//	}
//
//	private void setTargetSharedState() {
//		EventData target = new EventData();
//		target.putString("tntid", "test_tntid");
//		target.putString("thirdpartyid", "test_thirdpartyid");
//		eventHub.createSharedState("com.adobe.module.target", 0, target);
//	}
//
//	private void setAudienceSharedState() {
//		EventData audience = new EventData();
//		audience.putString("dpuuid", "test_dpuuid");
//		audience.putString("dpid", "test_dpid");
//		audience.putString("uuid", "test_uuid");
//		eventHub.createSharedState("com.adobe.module.audience", 0, audience);
//	}
//
//	private void setAnalyticsSharedState() {
//		EventData analytics = new EventData();
//		analytics.putString("aid", "test_aid");
//		analytics.putString("vid", "test_vid");
//		eventHub.createSharedState("com.adobe.module.analytics", 0, analytics);
//	}
//
//}
