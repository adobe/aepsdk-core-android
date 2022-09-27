///* *****************************************************************************
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2017 Adobe
// * All Rights Reserved.
// *
// * NOTICE: All information contained herein is, and remains
// * the property of Adobe and its suppliers, if any. The intellectual
// * and technical concepts contained herein are proprietary to Adobe
// * and its suppliers and are protected by all applicable intellectual
// * property laws, including trade secret and copyright laws.
// * Dissemination of this information or reproduction of this material
// * is strictly forbidden unless prior written permission is obtained
// * from Adobe.
// ******************************************************************************/
//package com.adobe.marketing.mobile.identity;
//
//import org.hamcrest.BaseMatcher;
//import org.hamcrest.Description;
//import org.hamcrest.Matcher;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.*;
//import java.net.URL;
//import java.util.*;
//
//import static org.junit.Assert.*;
//
//import com.adobe.marketing.mobile.Event;
//import com.adobe.marketing.mobile.EventSource;
//import com.adobe.marketing.mobile.EventType;
//import com.adobe.marketing.mobile.MobilePrivacyStatus;
//import com.adobe.marketing.mobile.VisitorID;
//
//public class IdentityTest extends BaseTest {
//	private static final String LIST_BEGIN                          = "{ ";
//	private static final String LIST_END                            = " }";
//	private static final String ITEM_BREAK                          = ", ";
//
//	private MockIdentityExtension identityModule;
//	final long DEFAULT_TTL_VALUE = 600;
//	final int DEFAULT_LAST_SYNC_VALUE = 0;
//
//
//	private MockDispatcherIdentityResponseIdentityIdentity mockDispatcherIdentityResponseEvent;
//	private MockDispatcherAnalyticsRequestContentIdentity mockDispatcherIdentityAnalyticsEvent;
//	private MockIdentityHitsDatabase mockIdentityHitsDatabase;
//
//	private ConfigurationSharedStateIdentity genericConfigurationSharedState;
//	private EventData genericAnalyticsSharedState;
//
//	@Before
//	public void setup() throws Exception {
//		super.beforeEach();
//		mockDispatcherIdentityResponseEvent = new MockDispatcherIdentityResponseIdentityIdentity(eventHub, null);
//		mockDispatcherIdentityAnalyticsEvent = new MockDispatcherAnalyticsRequestContentIdentity(eventHub, null);
//		identityModule = new MockIdentityExtension(eventHub, platformServices, mockDispatcherIdentityResponseEvent,
//				mockDispatcherIdentityAnalyticsEvent);
//		mockIdentityHitsDatabase = new MockIdentityHitsDatabase(identityModule, platformServices);
//		identityModule.database =  mockIdentityHitsDatabase;
//
//		genericConfigurationSharedState = generateConfigurationSharedState("test-orgid");
//		genericAnalyticsSharedState = generateAnalyticsSharedState("test-aid", "test-vid");
//	}
//
//	@Test
//	public void testConstructor_ShouldCreateEventQueue() {
//		assertNotNull(identityModule.eventsQueue);
//	}
//
//	@Test
//	public void testConstructor_ShouldLoadDefaultValues() {
//		assertNull(identityModule.mid);
//		assertNull(identityModule.customerIds);
//		assertNull(identityModule.advertisingIdentifier);
//		assertNull(identityModule.pushIdentifier);
//		assertNull(identityModule.blob);
//		assertNull(identityModule.locationHint);
//		assertEquals(identityModule.ttl, DEFAULT_TTL_VALUE);
//		assertEquals(identityModule.lastSync, DEFAULT_LAST_SYNC_VALUE);
//	}
//
//	// =================================================================================================================
//	// void enqueueEvent(final Event event)
//	// =================================================================================================================
//	@Test
//	public void testEnqueueEvent_ShouldIncreaseEventQueueSizeByOne() {
//		int oldSize = identityModule.eventsQueue.size();
//
//		identityModule.enqueueEvent(new Event.Builder("test-event", EventType.ANALYTICS, EventSource.NONE).build());
//		assertEquals(oldSize + 1, identityModule.eventsQueue.size());
//	}
//
//	@Test
//	public void testEnqueueEvent_ShouldNotIncreaseEventQueueSizeByOne_When_NullEvent() {
//		int oldSize = identityModule.eventsQueue.size();
//		identityModule.enqueueEvent(null);
//		assertEquals(oldSize, identityModule.eventsQueue.size());
//	}
//
//	// =================================================================================================================
//	// void processEventQueue()
//	// =================================================================================================================
//	@Test
//	public void testProcessingEventQueue_ShouldReferConfigSharedState() {
//		//put an event
//		Event fakeEvent = new Event.Builder("fake-event", EventType.IDENTITY, EventSource.BOOTED).build();
//		identityModule.enqueueEvent(fakeEvent);
//
//		identityModule.processEventQueue();
//		assertTrue(eventHub.getSharedEventStateCalled);
//		assertEquals(IdentityTestConstants.EventDataKeys.Configuration.MODULE_NAME,
//					 eventHub.getSharedEventStateParameterStateName);
//	}
//
//	@Test
//	public void testProcessingEventQueue_ShouldNotProcessEvent_When_ConfigDataIsNull() {
//		Event mockEvent1 = new Event.Builder("mock-event-1", EventType.IDENTITY, EventSource.NONE).build();
//		Event mockEvent2 = new Event.Builder("mock-event-2", EventType.IDENTITY, EventSource.NONE).build();
//		identityModule.enqueueEvent(mockEvent1);
//		identityModule.enqueueEvent(mockEvent2);
//
//		int queueSize = identityModule.eventsQueue.size();
//
//		identityModule.processEventQueue();
//		assertEquals(queueSize, identityModule.eventsQueue.size());
//	}
//
//	@Test
//	public void testProcessingEventQueue_ShouldProcessEvent_When_ConfigDataIsNonNull() {
//		EventData fakeConfigSharedState = new EventData();
//
//		//put an event
//		Event fakeEvent = new Event.Builder("fake-event", EventType.IDENTITY, EventSource.BOOTED).build();
//		identityModule.enqueueEvent(fakeEvent);
//
//		eventHub.setSharedState(IdentityTestConstants.EventDataKeys.Configuration.MODULE_NAME, fakeConfigSharedState);
//		int queueSize = identityModule.eventsQueue.size();
//
//		identityModule.processEventQueue();
//		assertEquals(queueSize - 1, identityModule.eventsQueue.size());
//	}
//
//	// =================================================================================================================
//	// void loadVariablesFromPersistentData()
//	// =================================================================================================================
//	@Test
//	public void testLoadVariablesFromPersistentData_ShouldHandle_When_LocalStorageServiceIsNull() {
//		platformServices.fakeLocalStorageService = null;
//		identityModule.setDataStore(null);
//		identityModule.loadVariablesFromPersistentData();
//		assertFalse(identityModule.convertVisitorIdsStringToVisitorIDObjectsWasCalled);
//	}
//
//	@Test
//	public void
//	testLoadVariablesFromPersistentData_ShouldLoadPersistedValues_When_PersistedDataExists() {
//		identityModule.loadVariablesFromPersistentData();
//		assertTrue(identityModule.convertVisitorIdsStringToVisitorIDObjectsWasCalled);
//	}
//
//	@Test
//	public void
//	testLoadVariablesFromPersistentData_ShouldLoadDefaultValues_When_PersistedDataNotExist() {
//		identityModule.loadVariablesFromPersistentData();
//		assertNull(identityModule.mid);
//		assertNull(identityModule.customerIds);
//		assertNull(identityModule.advertisingIdentifier);
//		assertNull(identityModule.pushIdentifier);
//		assertNull(identityModule.blob);
//		assertNull(identityModule.locationHint);
//		assertEquals(DEFAULT_TTL_VALUE, identityModule.ttl);
//		assertEquals(DEFAULT_LAST_SYNC_VALUE, identityModule.lastSync);
//	}
//
//	@Test
//	public void
//	testLoadVariablesFromPersistentData_ShouldLoadSavedValues_When_PersistedDataExists() {
//
//		identityModule.setDataStore(null);
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore fakeDataStore = new FakeDataStore();
//		fakeDataStore.setString(IdentityTestConstants.DataStoreKeys.MARKETING_CLOUD_ID, "my-mid");
//		fakeDataStore.setString(IdentityTestConstants.DataStoreKeys.ADVERTISING_IDENTIFIER, "my-ad-id");
//		fakeDataStore.setString(IdentityTestConstants.DataStoreKeys.BLOB, "my-blob");
//		fakeDataStore.setLong(IdentityTestConstants.DataStoreKeys.LAST_SYNC, 06102017);
//		fakeDataStore.setString(IdentityTestConstants.DataStoreKeys.LOCATION_HINT, "my-location-hint");
//		fakeDataStore.setString(IdentityTestConstants.DataStoreKeys.PUSH_IDENTIFIER, "my-push-id");
//		fakeDataStore.setString(IdentityTestConstants.DataStoreKeys.VISITOR_IDS_STRING, "k1=v1&k2=v2");
//		fakeDataStore.setLong(IdentityTestConstants.DataStoreKeys.TTL, 10);
//
//		fakeLocalStorageService.mapping.put(IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME,
//											fakeDataStore);
//
//		identityModule.loadVariablesFromPersistentData();
//		assertEquals("my-mid", identityModule.mid);
//		assertEquals("my-ad-id", identityModule.advertisingIdentifier);
//		assertEquals("my-push-id", identityModule.pushIdentifier);
//		assertEquals("my-blob", identityModule.blob);
//		assertEquals(10, identityModule.ttl);
//		assertEquals(06102017, identityModule.lastSync);
//		assertEquals("my-location-hint", identityModule.locationHint);
//	}
//
//	// =================================================================================================================
//	// void processEvent(final Event event, final ConfigurationSharedStateIdentity configSharedState)
//	// =================================================================================================================
//	@Test
//	public void testProcessEvent_When_EventNull() {
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		boolean result = identityModule.processEvent(null, configurationSharedStateIdentity);
//
//		assertTrue(result);
//		assertFalse(identityModule.handleSyncIdentifiersWasCalled);
//		assertFalse(identityModule.handleAppendURLWasCalled);
//		assertFalse(identityModule.handleGetUrlVariablesWasCalled);
//		assertNull(eventHub.getSharedEventState(identityModule.getModuleName(), null, null));
//	}
//
//	@Test
//	public void testProcessEvent_When_ConfigDataNull() {
//		EventData eventData = new EventData();
//		eventData.putBoolean(IdentityTestConstants.EventDataKeys.Identity.IS_SYNC_EVENT, true);
//		Event event = new Event.Builder("test-sync-event", EventType.IDENTITY,
//										EventSource.REQUEST_IDENTITY).setData(eventData).build();
//		boolean result = identityModule.processEvent(event, null);
//
//		assertTrue(result);
//		assertFalse(identityModule.handleSyncIdentifiersWasCalled);
//		assertFalse(identityModule.handleGetUrlVariablesWasCalled);
//		assertNull(eventHub.getSharedEventState(identityModule.getModuleName(), null, null));
//	}
//
//	@Test
//	public void testProcessEvent_When_EventDataNull() {
//		Event event = new Event.Builder("test-visitorID-event", EventType.IDENTITY,
//										EventSource.REQUEST_IDENTITY).build();
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		boolean result = identityModule.processEvent(event, configurationSharedStateIdentity);
//
//		assertTrue(result);
//		assertFalse(identityModule.handleAppendURLWasCalled);
//		assertFalse(identityModule.handleGetUrlVariablesWasCalled);
//		assertNull(eventHub.getSharedEventState(identityModule.getModuleName(), null, null));
//	}
//
//	@Test
//	public void testProcessEvent_ShouldSyncIdentifiers_When_EventDataContains_ISSYNC_EVENT_as_True() {
//
//		HashMap<String, String> testMap = new HashMap<String, String>();
//		testMap.put("k1", "v1");
//		testMap.put("k2", "v2");
//
//		EventData eventData = new EventData();
//		eventData.putStringMap(IdentityTestConstants.EventDataKeys.Identity.IDENTIFIERS, testMap);
//		eventData.putBoolean(IdentityTestConstants.EventDataKeys.Identity.IS_SYNC_EVENT, true);
//		Event event = new Event.Builder("test-identifiers-event", EventType.IDENTITY,
//										EventSource.REQUEST_IDENTITY).setData(eventData).build();
//
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.orgID = "1234";
//		boolean result = identityModule.processEvent(event, configurationSharedStateIdentity);
//
//		assertTrue(result);
//		assertTrue(identityModule.handleSyncIdentifiersWasCalled);
//		assertFalse(identityModule.handleGetUrlVariablesWasCalled);
//	}
//
//	@Test
//	public void
//	testProcessEvent_ShouldReturnFalse_When_EventDataContains_ISSYNC_EVENT_as_True_and_ConfigContainsNullOrgId() {
//
//		HashMap<String, String> testMap = new HashMap<String, String>();
//		testMap.put("k1", "v1");
//		testMap.put("k2", "v2");
//
//		EventData eventData = new EventData();
//		eventData.putStringMap(IdentityTestConstants.EventDataKeys.Identity.IDENTIFIERS, testMap);
//		eventData.putBoolean(IdentityTestConstants.EventDataKeys.Identity.IS_SYNC_EVENT, true);
//		Event event = new Event.Builder("test-identifiers-event", EventType.IDENTITY,
//										EventSource.REQUEST_IDENTITY).setData(eventData).build();
//
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.orgID = null;
//		boolean result = identityModule.processEvent(event, configurationSharedStateIdentity);
//
//		assertFalse(result);
//		assertTrue(identityModule.handleSyncIdentifiersWasCalled);
//		assertFalse(identityModule.handleGetUrlVariablesWasCalled);
//	}
//
//	@Test
//	public void
//	testProcessEventShould_ReturnYes_SyncIdentifiers_When_EventDataContains_ISSYNC_EVENT_as_True_With_OptOutConfig() {
//
//		HashMap<String, String> testMap = new HashMap<String, String>();
//		testMap.put("k1", "v1");
//		testMap.put("k2", "v2");
//
//		EventData eventData = new EventData();
//		eventData.putStringMap(IdentityTestConstants.EventDataKeys.Identity.IDENTIFIERS, testMap);
//		eventData.putBoolean(IdentityTestConstants.EventDataKeys.Identity.IS_SYNC_EVENT, true);
//		Event event = new Event.Builder("test-identifiers-event", EventType.IDENTITY,
//										EventSource.REQUEST_IDENTITY).setData(eventData).build();
//
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.orgID = "1234";
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_OUT;
//		boolean result = identityModule.processEvent(event, configurationSharedStateIdentity);
//
//		assertTrue(result);
//		assertTrue(identityModule.handleSyncIdentifiersWasCalled);
//		assertFalse(identityModule.handleGetUrlVariablesWasCalled);
//	}
//
//	@Test
//	public void testProcessEvent_ShouldNotSyncIdentifiers_When_EventData_DoNotHave_SyncEventKey() {
//
//		HashMap<String, String> testMap = new HashMap<String, String>();
//		testMap.put("k1", "v1");
//		testMap.put("k2", "v2");
//
//		EventData eventData = new EventData();
//		eventData.putStringMap(IdentityTestConstants.EventDataKeys.Identity.IDENTIFIERS, testMap);
//		Event event = new Event.Builder("test-identifiers-event", EventType.IDENTITY,
//										EventSource.REQUEST_IDENTITY).setData(eventData).build();
//
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.orgID = "1234";
//		boolean result = identityModule.processEvent(event, configurationSharedStateIdentity);
//
//		assertTrue(result);
//		assertFalse(identityModule.handleSyncIdentifiersWasCalled);
//		assertFalse(identityModule.handleGetUrlVariablesWasCalled);
//	}
//
//	@Test
//	public void testProcessEvent_ShouldUpdatePushIdentifier_When_EventDataContains_PushIdentifier() {
//		// test
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.orgID = "1234"; // handleSyncIdentifiers needs an org id
//		boolean result = identityModule.processEvent(FakePushIDEvent(), configurationSharedStateIdentity);
//
//		// verify
//		assertTrue(result);
//		assertTrue(identityModule.handleSyncIdentifiersWasCalled);
//		assertEquals(identityModule.pushIdentifier, "test-push-id");
//		assertFalse(identityModule.handleGetUrlVariablesWasCalled);
//	}
//
//	@Test
//	public void testProcessEvent_ShouldUpdateAdid_When_EventDataContains_Adid() {
//
//
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.orgID = "1234"; // handleSyncIdentifiers needs an org id
//		boolean result = identityModule.processEvent(FakeAdIDEvent(), configurationSharedStateIdentity);
//
//		assertTrue(result);
//		assertEquals(identityModule.advertisingIdentifier, "testAdid");
//		assertFalse(identityModule.handleGetUrlVariablesWasCalled);
//	}
//
//	@Test
//	public void testProcessEvent_Should_ReturnTrue_And_CallAppendToURL_When_EventDataContains_BASE_URL() {
//		EventData eventData = new EventData();
//		eventData.putString(IdentityTestConstants.EventDataKeys.Identity.BASE_URL, "base-url");
//		Event event = new Event.Builder("test-appendToURL-event", EventType.IDENTITY,
//										EventSource.REQUEST_IDENTITY).setData(eventData).build();
//
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		identityModule.shouldWaitForPendingSharedState = false;
//		boolean result = identityModule.processEvent(event, configurationSharedStateIdentity);
//
//		assertTrue(result);
//		assertTrue(identityModule.shouldWaitForPendingSharedStateCalled);
//		assertTrue(identityModule.handleAppendURLWasCalled);
//		assertFalse(identityModule.handleGetUrlVariablesWasCalled);
//	}
//
//	@Test
//	public void
//	testProcessEvent_Should_ReturnFalse_And_CallAppendToURL_When_EventDataContains_BASE_URL_And_ShouldWaitForPendingSharedState() {
//		EventData eventData = new EventData();
//		eventData.putString(IdentityTestConstants.EventDataKeys.Identity.BASE_URL, "base-url");
//		Event event = new Event.Builder("test-appendToURL-event", EventType.IDENTITY,
//										EventSource.REQUEST_IDENTITY).setData(eventData).build();
//
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		identityModule.shouldWaitForPendingSharedState = true;
//		boolean result = identityModule.processEvent(event, configurationSharedStateIdentity);
//
//		assertFalse(result);
//		assertTrue(identityModule.shouldWaitForPendingSharedStateCalled);
//		assertFalse(identityModule.handleAppendURLWasCalled);
//		assertFalse(identityModule.handleGetUrlVariablesWasCalled);
//	}
//
//	@Test
//	public void testProcessEvent_Should_ReturnTrue_And_CallGetUrlVariables_When_EventDataContains_URL_VARIABLES() {
//		EventData eventData = new EventData();
//		eventData.putBoolean(IdentityTestConstants.EventDataKeys.Identity.URL_VARIABLES, true);
//		Event event = new Event.Builder("test-getUrlVariables-event", EventType.IDENTITY,
//										EventSource.REQUEST_IDENTITY).setData(eventData).build();
//
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		identityModule.shouldWaitForPendingSharedState = false;
//		boolean result = identityModule.processEvent(event, configurationSharedStateIdentity);
//
//		assertTrue(result);
//		assertTrue(identityModule.shouldWaitForPendingSharedStateCalled);
//		assertFalse(identityModule.handleAppendURLWasCalled);
//		assertTrue(identityModule.handleGetUrlVariablesWasCalled);
//	}
//
//	@Test
//	public void
//	testProcessEvent_Should_ReturnFalse_And_CallGetUrlVariables_When_EventDataContains_URL_VARIABLES_And_ShouldWaitforPendingSharedState() {
//		EventData eventData = new EventData();
//		eventData.putBoolean(IdentityTestConstants.EventDataKeys.Identity.URL_VARIABLES, true);
//		Event event = new Event.Builder("test-getUrlVariables-event", EventType.IDENTITY,
//										EventSource.REQUEST_IDENTITY).setData(eventData).build();
//
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		identityModule.shouldWaitForPendingSharedState = true;
//		boolean result = identityModule.processEvent(event, configurationSharedStateIdentity);
//
//		assertFalse(result);
//		assertTrue(identityModule.shouldWaitForPendingSharedStateCalled);
//		assertFalse(identityModule.handleAppendURLWasCalled);
//		assertFalse(identityModule.handleGetUrlVariablesWasCalled);
//	}
//
//	@Test
//	public void testProcessEvent_ShouldCallDispatcher_When_EventDataContains_NoSpecificKey() {
//		EventData eventData = new EventData();
//		Event event = new Event.Builder("test-appendToURL-event", EventType.IDENTITY,
//										EventSource.REQUEST_IDENTITY).setData(eventData).build();
//
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		boolean result = identityModule.processEvent(event, configurationSharedStateIdentity);
//
//		assertTrue(result);
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertFalse(identityModule.handleGetUrlVariablesWasCalled);
//	}
//
//	@Test
//	public void testProcessEvent_ShouldHandleReset() {
//		EventData eventData = new EventData();
//		Event event = new Event.Builder("test-rest-event", EventType.GENERIC_IDENTITY,
//										EventSource.REQUEST_RESET).setData(eventData).build();
//
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		boolean result = identityModule.processEvent(event, configurationSharedStateIdentity);
//
//		assertTrue(result);
//		assertTrue(identityModule.handleIdentityRequestResetWasCalled);
//	}
//
//	// ==============================================================================================================
//	// void handleSyncIdentifiers(final Event event, final ConfigurationSharedStateIdentity configSharedState)
//	// ==============================================================================================================
//
//	@Test
//	public void handleSyncIdentifiers_OnNullEvent_ShouldDoNOOP() {
//		// prepare
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(null, configurationSharedStateIdentity);
//
//		// verify
//		assertTrue(result);
//		EventData idSharedState = eventHub.getSharedEventState(identityModule.getModuleName(), FakeSycnIDEvent(), null);
//		assertFalse(identityModule.generateCustomerIdsWasCalled);
//		assertFalse(mockIdentityHitsDatabase.queueWasCalled);
//		assertNull(idSharedState);
//	}
//
//	@Test
//	public void handleSyncIdentifiers_happySyncIDs() throws Exception {
//		// prepare
//		Event syncIDEvent = FakeSycnIDEvent();
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		configurationSharedStateIdentity.orgID = "fakeOrg";
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(syncIDEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify setting of shared state
//		assertTrue(eventHub.createSharedStateCalled);
//		assertEquals(5, eventHub.createSharedStateParamVersion);
//		EventData sharedStateData = eventHub.createSharedStateParamState;
//		assertNotNull(sharedStateData);
//		assertEquals(3, sharedStateData.size());
//		assertEquals(2, sharedStateData.getTypedList(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST,
//					 VisitorID.VARIANT_SERIALIZER).size());
//		assertNotNull(sharedStateData.optString("mid", null));
//
//		// verify database queing
//		assertTrue(mockIdentityHitsDatabase.queueWasCalled);
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("https://dpm.demdex.net/"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_mid="));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_orgid=fakeOrg"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_cid_ic=k1%01v1%010"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_cid_ic=k2%01v2%010"));
//	}
//
//	@Test
//	public void handleSyncIdentifiers_happyPushID() {
//		// prepare
//		Event pushEvent = FakePushIDEvent();
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		configurationSharedStateIdentity.orgID = "fakeOrg";
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(pushEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify setting of shared state
//		assertTrue(eventHub.createSharedStateCalled);
//		assertEquals(5, eventHub.createSharedStateParamVersion);
//		EventData sharedStateData = eventHub.createSharedStateParamState;
//		assertNotNull(sharedStateData);
//		assertEquals(3, sharedStateData.size());
//		assertNull(sharedStateData.optTypedList(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST, null,
//												VisitorID.VARIANT_SERIALIZER));
//		assertNotNull(sharedStateData.optString("mid", null));
//		assertEquals("test-push-id", sharedStateData.optString(IdentityTestConstants.EventDataKeys.Identity.PUSH_IDENTIFIER,
//					 null));
//
//		// verify database queueing
//		assertTrue(mockIdentityHitsDatabase.queueWasCalled);
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("https://dpm.demdex.net/"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_mid="));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_orgid=fakeOrg"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_cid=20919%01test-push-id"));
//	}
//
//	@Test
//	public void handleSyncIdentifiers_happyAdid() {
//		// prepare
//		Event adidEvent = FakeAdIDEvent();
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		configurationSharedStateIdentity.orgID = "fakeOrg";
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(adidEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify setting of shared state
//		assertTrue(eventHub.createSharedStateCalled);
//		assertEquals(5, eventHub.createSharedStateParamVersion);
//		EventData sharedStateData = eventHub.createSharedStateParamState;
//		assertNotNull(sharedStateData);
//		assertEquals(4, sharedStateData.size());
//		assertNotNull(sharedStateData.optString("mid", null));
//		assertNotNull(sharedStateData.optTypedList(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST, null,
//					  VisitorID.VARIANT_SERIALIZER));
//		assertEquals(1, sharedStateData.optTypedList(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST, null,
//					 VisitorID.VARIANT_SERIALIZER).size());
//		VisitorID actualID = sharedStateData.optTypedList(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST, null,
//							 VisitorID.VARIANT_SERIALIZER).get(0);
//		assertEquals("testAdid", actualID.getId());
//		assertEquals("d_cid_ic", actualID.getIdOrigin());
//		assertEquals("DSID_20914", actualID.getIdType());
//		assertEquals("testAdid", sharedStateData.optString(IdentityTestConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER,
//					 null));
//
//		// verify database queueing
//		assertTrue(mockIdentityHitsDatabase.queueWasCalled);
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("https://dpm.demdex.net/id"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("device_consent=1"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_mid="));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_orgid=fakeOrg"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_cid_ic=DSID_20914%01testAdid%011"));
//
//	}
//
//	@Test
//	public void handleSyncIdentifiers_initialAdIdZero_eventAdIdValid_HitQueuedWithConsentYes() {
//		// prepare
//		Event adidEvent = FakeAdIDEvent();
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		configurationSharedStateIdentity.orgID = "fakeOrg";
//		identityModule.advertisingIdentifier =  IdentityTestConstants.Defaults.ZERO_ADVERTISING_ID;
//		identityModule.mid = "testMid"; // avoid force sync from ECID generation
//		identityModule.lastSync = TimeUtil.getUnixTimeInSeconds(); // avoid force sync from ttl timeout
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(adidEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify setting of shared state
//		assertTrue(eventHub.createSharedStateCalled);
//		assertEquals(5, eventHub.createSharedStateParamVersion);
//		EventData sharedStateData = eventHub.createSharedStateParamState;
//		assertNotNull(sharedStateData);
//		assertEquals(4, sharedStateData.size());
//		assertNotNull(sharedStateData.optString(IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_MID, null));
//		assertNotNull(sharedStateData.optTypedList(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST, null,
//					  VisitorID.VARIANT_SERIALIZER));
//		assertEquals(1, sharedStateData.optTypedList(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST, null,
//					 VisitorID.VARIANT_SERIALIZER).size());
//		VisitorID actualID = sharedStateData.optTypedList(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST, null,
//							 VisitorID.VARIANT_SERIALIZER).get(0);
//		assertEquals("testAdid", actualID.getId());
//		assertEquals("d_cid_ic", actualID.getIdOrigin());
//		assertEquals("DSID_20914", actualID.getIdType());
//		assertEquals("testAdid", sharedStateData.optString(IdentityTestConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER,
//					 null));
//
//		// verify database queueing
//		assertTrue(mockIdentityHitsDatabase.queueWasCalled);
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("https://dpm.demdex.net/id"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("device_consent=1"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_mid=testMid"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_orgid=fakeOrg"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_cid_ic=DSID_20914%01testAdid%011"));
//
//	}
//
//	@Test
//	public void handleSyncIdentifiers_Adid_IsSame_NoHitQueued() {
//		// prepare
//		Event adidEvent = generateAdidEvent("testAdid");
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		configurationSharedStateIdentity.orgID = "fakeOrg";
//		identityModule.advertisingIdentifier =  "testAdid";
//		identityModule.mid = "testMid"; // avoid force sync from ECID generation
//		identityModule.lastSync = TimeUtil.getUnixTimeInSeconds(); // avoid force sync from ttl timeout
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(adidEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify setting of shared state
//		assertTrue(eventHub.createSharedStateCalled);
//		EventData sharedStateData = eventHub.createSharedStateParamState;
//		assertNotNull(sharedStateData);
//		assertNotNull(sharedStateData.optString(IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_MID, null));
//		assertFalse(sharedStateData.containsKey(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST));
//		assertEquals("testAdid", sharedStateData.optString(IdentityTestConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER,
//					 null));
//		assertEquals("testAdid", identityModule.advertisingIdentifier);
//
//		// Ad ID did not change
//		assertFalse(mockIdentityHitsDatabase.queueWasCalled);
//	}
//
//	@Test
//	public void handleSyncIdentifiers_Adid_IsUpdated_HitQueued() {
//		// prepare
//		Event adidEvent = generateAdidEvent("testAdid");
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		configurationSharedStateIdentity.orgID = "fakeOrg";
//		identityModule.advertisingIdentifier =  "initialTestAdid";
//		identityModule.mid = "testMid"; // avoid force sync from ECID generation
//		identityModule.lastSync = TimeUtil.getUnixTimeInSeconds(); // avoid force sync from ttl timeout
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(adidEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify setting of shared state
//		assertTrue(eventHub.createSharedStateCalled);
//		EventData sharedStateData = eventHub.createSharedStateParamState;
//		assertNotNull(sharedStateData);
//		assertNotNull(sharedStateData.optString(IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_MID, null));
//		assertNotNull(sharedStateData.optTypedList(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST, null,
//					  VisitorID.VARIANT_SERIALIZER));
//		assertEquals(1, sharedStateData.optTypedList(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST, null,
//					 VisitorID.VARIANT_SERIALIZER).size());
//		VisitorID actualID = sharedStateData.optTypedList(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST, null,
//							 VisitorID.VARIANT_SERIALIZER).get(0);
//		assertEquals("testAdid", actualID.getId());
//		assertEquals("d_cid_ic", actualID.getIdOrigin());
//		assertEquals("DSID_20914", actualID.getIdType());
//		assertEquals("testAdid", sharedStateData.optString(IdentityTestConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER,
//					 null));
//		assertEquals("testAdid", identityModule.advertisingIdentifier);
//
//		// verify database queueing
//		assertTrue(mockIdentityHitsDatabase.queueWasCalled);
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("https://dpm.demdex.net/id"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_mid=testMid"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_orgid=fakeOrg"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_cid_ic=DSID_20914%01testAdid%011"));
//		assertFalse(mockIdentityHitsDatabase.queueParameterUrl.contains("device_consent"));
//	}
//
//	@Test
//	public void handleSyncIdentifiers_Adid_IsZero_HitQueuedWithConsentNo() {
//		// prepare
//		Event adidEvent = generateAdidEvent(IdentityTestConstants.Defaults.ZERO_ADVERTISING_ID);
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		configurationSharedStateIdentity.orgID = "fakeOrg";
//		identityModule.advertisingIdentifier =  "initialAdid";
//		identityModule.mid = "testMid"; // avoid force sync from ECID generation
//		identityModule.lastSync = TimeUtil.getUnixTimeInSeconds(); // avoid force sync from ttl timeout
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(adidEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify setting of shared state
//		assertTrue(eventHub.createSharedStateCalled);
//		EventData sharedStateData = eventHub.createSharedStateParamState;
//		assertNotNull(sharedStateData);
//		assertEquals(2, sharedStateData.size()); // 'lastsync' and 'mid'
//		assertTrue(sharedStateData.containsKey(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LAST_SYNC));
//		assertNotNull(sharedStateData.optString(IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_MID, null));
//		assertFalse(sharedStateData.containsKey(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST));
//		assertFalse(sharedStateData.containsKey(IdentityTestConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER));
//		assertEquals("", identityModule.advertisingIdentifier);
//
//		// verify database queueing
//		assertTrue(mockIdentityHitsDatabase.queueWasCalled);
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("https://dpm.demdex.net/id"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("device_consent=0"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_consent_ic=DSID_20914"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_mid=testMid"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_orgid=fakeOrg"));
//	}
//
//	@Test
//	public void handleSyncIdentifiers_Adid_IsEmpty_HitQueuedWithConsentNo() {
//		// prepare
//		Event adidEvent = generateAdidEvent("");
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		configurationSharedStateIdentity.orgID = "fakeOrg";
//		identityModule.advertisingIdentifier =  "initialAdid";
//		identityModule.mid = "testMid"; // avoid force sync from ECID generation
//		identityModule.lastSync = TimeUtil.getUnixTimeInSeconds(); // avoid force sync from ttl timeout
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(adidEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify setting of shared state
//		assertTrue(eventHub.createSharedStateCalled);
//		EventData sharedStateData = eventHub.createSharedStateParamState;
//		assertNotNull(sharedStateData);
//		assertEquals(2, sharedStateData.size()); // 'lastsync' and 'mid'
//		assertTrue(sharedStateData.containsKey(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LAST_SYNC));
//		assertNotNull(sharedStateData.optString(IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_MID, null));
//		assertFalse(sharedStateData.containsKey(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST));
//		assertFalse(sharedStateData.containsKey(IdentityTestConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER));
//		assertEquals("", identityModule.advertisingIdentifier);
//
//		// verify database queueing
//		assertTrue(mockIdentityHitsDatabase.queueWasCalled);
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("https://dpm.demdex.net/id"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("device_consent=0"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_consent_ic=DSID_20914"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_mid=testMid"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_orgid=fakeOrg"));
//	}
//
//	@Test
//	public void handleSyncIdentifiers_Adid_IsZero_initialIsEmpty_NoHitQueued() {
//		// prepare
//		Event adidEvent = generateAdidEvent(IdentityTestConstants.Defaults.ZERO_ADVERTISING_ID);
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		configurationSharedStateIdentity.orgID = "fakeOrg";
//		identityModule.advertisingIdentifier =  "";
//		identityModule.mid = "testMid"; // avoid force sync from ECID generation
//		identityModule.lastSync = TimeUtil.getUnixTimeInSeconds(); // avoid force sync from ttl timeout
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(adidEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify setting of shared state
//		assertTrue(eventHub.createSharedStateCalled);
//		EventData sharedStateData = eventHub.createSharedStateParamState;
//		assertNotNull(sharedStateData);
//		assertEquals(2, sharedStateData.size()); // 'lastsync' and 'mid'
//		assertTrue(sharedStateData.containsKey(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LAST_SYNC));
//		assertNotNull(sharedStateData.optString(IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_MID, null));
//		assertFalse(sharedStateData.containsKey(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST));
//		assertFalse(sharedStateData.containsKey(IdentityTestConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER));
//		assertEquals("", identityModule.advertisingIdentifier);
//
//		// No hit generated as Ad ID didn't change (all zeros is considered same as empty)
//		assertFalse(mockIdentityHitsDatabase.queueWasCalled);
//	}
//
//	@Test
//	public void handleSyncIdentifiers_Adid_IsZero_initialIsZero_HitQueuedWithConsentNo() {
//		// prepare
//		Event adidEvent = generateAdidEvent(IdentityTestConstants.Defaults.ZERO_ADVERTISING_ID);
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		configurationSharedStateIdentity.orgID = "fakeOrg";
//		identityModule.advertisingIdentifier =  IdentityTestConstants.Defaults.ZERO_ADVERTISING_ID;
//		identityModule.mid = "testMid"; // avoid force sync from ECID generation
//		identityModule.lastSync = TimeUtil.getUnixTimeInSeconds(); // avoid force sync from ttl timeout
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(adidEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify setting of shared state
//		assertTrue(eventHub.createSharedStateCalled);
//		EventData sharedStateData = eventHub.createSharedStateParamState;
//		assertNotNull(sharedStateData);
//		assertEquals(2, sharedStateData.size()); // 'lastsync' and 'mid'
//		assertTrue(sharedStateData.containsKey(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LAST_SYNC));
//		assertNotNull(sharedStateData.optString(IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_MID, null));
//		assertFalse(sharedStateData.containsKey(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST));
//		assertFalse(sharedStateData.containsKey(IdentityTestConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER));
//		assertEquals("", identityModule.advertisingIdentifier); // Notice Ad ID set as empty, not all zeros, as expected!
//
//		// Hit generated as initial Ad ID of all zeros is changed to empty string
//		// Send hit to update all zeros on server-side to consent NO
//		assertTrue(mockIdentityHitsDatabase.queueWasCalled);
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("https://dpm.demdex.net/id"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("device_consent=0"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_consent_ic=DSID_20914"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_mid=testMid"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_orgid=fakeOrg"));
//	}
//
//
//	@Test
//	public void handleSyncIdentifiers_Appends_BlobAndLocationHint() {
//
//		// prepare
//		Event pushEvent = FakePushIDEvent();
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		configurationSharedStateIdentity.orgID = "fakeOrg";
//		identityModule.locationHint =  "locHinty";
//		identityModule.blob =  "blobby";
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(pushEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify setting of shared state
//		assertTrue(eventHub.createSharedStateCalled);
//		assertEquals(5, eventHub.createSharedStateParamVersion);
//		EventData sharedStateData = eventHub.createSharedStateParamState;
//		assertNotNull(sharedStateData);
//		assertEquals("blobby", sharedStateData.optString(IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, null));
//		assertEquals("locHinty", sharedStateData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT, null));
//
//		// verify database queueing
//		assertTrue(mockIdentityHitsDatabase.queueWasCalled);
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("https://dpm.demdex.net/"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_mid="));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_orgid=fakeOrg"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("dcs_region=locHinty"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_blob=blobby"));
//	}
//
//	@Test
//	public void handleSyncIdentifiers_noDatabaseCallOrSharedStateIfSyncAborted() {
//		// prepare
//		EventData eventData = new EventData();
//		eventData.putBoolean(IdentityTestConstants.EventDataKeys.Identity.IS_SYNC_EVENT, true);
//		Event event = new Event.Builder("test-identifiers-event", EventType.IDENTITY,
//										EventSource.REQUEST_IDENTITY).setData(eventData).build();
//		event.setEventNumber(5);
//
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		configurationSharedStateIdentity.orgID = "fakeOrg";
//
//		identityModule.mid = "1234"; // visitor ID is null initially and set for the first time in
//		// shouldSync(). Mimic a second call to shouldSync by setting the mid
//		identityModule.lastSync = TimeUtil.getUnixTimeInSeconds(); // set last sync to now
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(event, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify setting of shared state
//		assertTrue(eventHub.createSharedStateCalled);
//		assertFalse(eventHub.updateSharedStateWasCalled);
//
//		// verify database queueing no called (no sync)
//		assertFalse(mockIdentityHitsDatabase.queueWasCalled);
//	}
//
//	@Test
//	public void handleSyncIdentifiers_setSharedStateWhenDatabaseIsNull() throws Exception {
//		// prepare
//		Event syncIDEvent = FakeSycnIDEvent();
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		configurationSharedStateIdentity.orgID = "fakeOrg";
//
//		identityModule.database = null;
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(syncIDEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify setting of shared state
//		assertTrue(eventHub.createSharedStateCalled);
//		assertEquals(5, eventHub.createSharedStateParamVersion);
//		EventData sharedStateData = eventHub.createSharedStateParamState;
//		assertEquals(3, sharedStateData.size());
//		assertEquals(2, sharedStateData.getTypedList("visitoridslist", VisitorID.VARIANT_SERIALIZER).size());
//		assertNotNull(sharedStateData.optString("mid", null));
//	}
//
//	@Test
//	public void testHandleSyncIdentifiersShould_ReturnYes_AndSync_WithEmptyCurrentConfig_ButValidLatestConfig() throws
//		Exception {
//
//		// set latest valid configuration with valid values
//		EventData validConfig = new EventData();
//		validConfig.putString(IdentityTestConstants.JSON_CONFIG_ORGID_KEY, "fakeOrg");
//		validConfig.putString(IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY, "optedin");
//		identityModule.updateLatestValidConfiguration(validConfig);
//
//		// prepare
//		Event syncIDEvent = FakeSycnIDEvent();
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.orgID = null;
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(syncIDEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify setting of shared state
//		assertTrue(eventHub.createSharedStateCalled);
//		assertEquals(5, eventHub.createSharedStateParamVersion);
//		EventData sharedStateData = eventHub.createSharedStateParamState;
//		assertNotNull(sharedStateData);
//		assertEquals(3, sharedStateData.size());
//		assertEquals(2, sharedStateData.getTypedList(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST,
//					 VisitorID.VARIANT_SERIALIZER).size());
//		assertNotNull(sharedStateData.optString("mid", null));
//
//		// verify database queing
//		assertTrue(mockIdentityHitsDatabase.queueWasCalled);
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("https://dpm.demdex.net/"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_mid="));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_orgid=fakeOrg"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_cid_ic=k1%01v1%010"));
//		assertTrue(mockIdentityHitsDatabase.queueParameterUrl.contains("d_cid_ic=k2%01v2%010"));
//
//	}
//
//	@Test
//	public void TestHandleSyncIdentifiersShould_ReturnNo_AndNotSync_WithEmptyCurrentConfig_AndNullLatestConfig() {
//
//		// do not set latest valid configuration with valid values
//
//		// prepare
//		Event syncIDEvent = FakeSycnIDEvent();
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.orgID = null;
//
//		// test
//		boolean result = identityModule.handleSyncIdentifiers(syncIDEvent, configurationSharedStateIdentity);
//		assertFalse(result);
//
//		// verify shared state not created
//		assertFalse(eventHub.createSharedStateCalled);
//		assertFalse(eventHub.updateSharedStateWasCalled);
//	}
//
//	@Test
//	public void testHandleSyncIdentifiersShould_ReturnYes_When_Privacy_is_Opt_Out() {
//
//		// set latest valid configuration with valid values
//		EventData validConfig = new EventData();
//		validConfig.putString(IdentityTestConstants.JSON_CONFIG_ORGID_KEY, "latestOrg");
//		validConfig.putString(IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY, "optedin"); // OPT_IN
//		identityModule.updateLatestValidConfiguration(validConfig);
//
//		// prepare
//		Event syncIDEvent = FakeSycnIDEvent();
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.orgID = "fakeOrg";
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN; // OPT_IN
//
//		// test
//		identityModule.privacyStatus = MobilePrivacyStatus.OPT_OUT; // OPT-OUT
//		boolean result = identityModule.handleSyncIdentifiers(syncIDEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify
//		assertFalse(eventHub.createSharedStateCalled);
//		assertFalse(mockIdentityHitsDatabase.queueWasCalled);
//
//	}
//
//	@Test
//	public void testHandleSyncIdentifiersShould_ReturnYes_When_Config_Privacy_is_Opt_Out() {
//
//		// set latest valid configuration with valid values
//		EventData validConfig = new EventData();
//		validConfig.putString(IdentityTestConstants.JSON_CONFIG_ORGID_KEY, "latestOrg");
//		validConfig.putString(IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY, "optedin"); // OPT_IN
//		identityModule.updateLatestValidConfiguration(validConfig);
//
//		// prepare
//		Event syncIDEvent = FakeSycnIDEvent();
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.orgID = "fakeOrg";
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_OUT; // OPT_OUT
//
//		// test
//		identityModule.privacyStatus = MobilePrivacyStatus.OPT_IN; // OPT_IN
//		boolean result = identityModule.handleSyncIdentifiers(syncIDEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify
//		assertFalse(eventHub.createSharedStateCalled);
//		assertFalse(mockIdentityHitsDatabase.queueWasCalled);
//
//	}
//
//	@Test
//	public void testHandleSyncIdentifiersShould_ReturnYes_When_LatestConfig_Privacy_is_Opt_Out() {
//
//		// set latest valid configuration with valid values
//		EventData validConfig = new EventData();
//		validConfig.putString(IdentityTestConstants.JSON_CONFIG_ORGID_KEY, "latestOrg");
//		validConfig.putString(IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY, "optedout"); // OPT_OUT
//		identityModule.updateLatestValidConfiguration(validConfig);
//
//		// prepare
//		Event syncIDEvent = FakeSycnIDEvent();
//		ConfigurationSharedStateIdentity configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
//		configurationSharedStateIdentity.orgID = null; // trigger use of "latest" config
//		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN; // OPT_IN
//
//		// test
//		identityModule.privacyStatus = MobilePrivacyStatus.OPT_IN; // OPT_IN
//		boolean result = identityModule.handleSyncIdentifiers(syncIDEvent, configurationSharedStateIdentity);
//		assertTrue(result);
//
//		// verify
//		assertFalse(eventHub.createSharedStateCalled);
//		assertFalse(mockIdentityHitsDatabase.queueWasCalled);
//
//	}
//
//	// ==============================================================================================================
//	// 	void handleIdentityRequestReset(final Event event)
//	// ==============================================================================================================
//	@Test
//	public void handleIdentityRequestReset_nullEvent_shouldReturn() {
//		// test
//		identityModule.handleIdentityRequestReset(null);
//
//		// verify
//		assertFalse(eventHub.createSharedStateCalled);
//		assertFalse(mockIdentityHitsDatabase.queueWasCalled);
//	}
//
//	@Test
//	public void handleIdentityRequestReset_optedOut_shouldReturn() {
//		// setup
//		// set latest valid configuration with valid values
//		EventData validConfig = new EventData();
//		validConfig.putString(IdentityTestConstants.JSON_CONFIG_ORGID_KEY, "latestOrg");
//		validConfig.putString(IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY, "optedout"); // OPT_OUT
//		identityModule.updateLatestValidConfiguration(validConfig);
//
//		Event event = new Event.Builder("test", EventType.GENERIC_IDENTITY, EventSource.REQUEST_RESET).build();
//
//		// test
//		identityModule.handleIdentityRequestReset(event);
//
//		// verify
//		assertFalse(eventHub.createSharedStateCalled);
//		assertFalse(mockIdentityHitsDatabase.queueWasCalled);
//	}
//
//	@Test
//	public void handleIdentityRequestReset_happy_regeneratesEcid() {
//		// setup
//		// set latest valid configuration with valid values
//		EventData validConfig = new EventData();
//		validConfig.putString(IdentityTestConstants.JSON_CONFIG_ORGID_KEY, "latestOrg");
//		validConfig.putString(IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY, "optedin"); // OPT_IN
//		identityModule.updateLatestValidConfiguration(validConfig);
//
//		String mockMarketingCloudIdentifier = "83056071767212492011535942034357093219";
//		identityModule.mid = mockMarketingCloudIdentifier;
//
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//		dataStore.setString(IdentityConstants.DataStoreKeys.MARKETING_CLOUD_ID,
//							mockMarketingCloudIdentifier);
//		// mock analytics already synced
//		dataStore.setBoolean(IdentityConstants.DataStoreKeys.PUSH_ENABLED, true);
//		dataStore.setBoolean(IdentityConstants.DataStoreKeys.AID_SYNCED_KEY, true);
//
//		// test
//		Event event = new Event.Builder("test", EventType.GENERIC_IDENTITY, EventSource.REQUEST_RESET).build();
//		identityModule.handleIdentityRequestReset(event);
//
//		// verify
//		assertTrue(identityModule.tryProcessingEventQueueWasCalled);
//		assertEquals(1, identityModule.eventsQueue.size());
//
//		// ecid should be cleared
//		assertNull(dataStore.getString(IdentityConstants.DataStoreKeys.MARKETING_CLOUD_ID, null));
//		assertNull(identityModule.mid);
//		assertNull(identityModule.advertisingIdentifier);
//		assertNull(identityModule.pushIdentifier);
//		assertNull(identityModule.blob);
//		assertNull(identityModule.locationHint);
//		assertNull(identityModule.customerIds);
//
//		// persisted push flags should be removed/false
//		assertFalse(dataStore.getBoolean(IdentityConstants.DataStoreKeys.PUSH_ENABLED, false));
//		assertFalse(dataStore.getBoolean(IdentityConstants.DataStoreKeys.AID_SYNCED_KEY, false));
//
//		// force sync event should be queued
//		Event forceSyncEvent = identityModule.eventsQueue.peek();
//		assertEquals(EventType.IDENTITY, forceSyncEvent.getEventType());
//		assertEquals(EventSource.REQUEST_IDENTITY, forceSyncEvent.getEventSource());
//		assertTrue((boolean) forceSyncEvent.getEventData().get(IdentityConstants.EventDataKeys.Identity.FORCE_SYNC));
//	}
//
//	// ==============================================================================================================
//	// 	Map<String, String> extractIdentifiers(final EventData eventData)
//	// ==============================================================================================================
//	@Test
//	public void extractIdentifiers_Should_ReturnEmptyMap_WhenNoSyncIDKeyPresentInEventData() {
//		EventData eventData = new EventData();
//		eventData.putString("blah", "blah");
//		HashMap<String, String> ids = (HashMap<String, String>) identityModule.extractIdentifiers(eventData);
//		assertNotNull(ids);
//		assertTrue(ids.isEmpty());
//	}
//
//	@Test
//	public void extractIdentifiers_Should_ReturnEmptyMap_WhenNullEventData() {
//		HashMap<String, String> ids = (HashMap<String, String>) identityModule.extractIdentifiers(null);
//		assertNotNull(ids);
//		assertTrue(ids.isEmpty());
//	}
//
//	@Test
//	public void extractIdentifiers_Should_ReturnNonEmptyMap_WhenIDTypeKeyPresentInEventData_WithNonNullNonEmptyValue() {
//		EventData eventData = new EventData();
//		HashMap<String, String> map = new HashMap<String, String>();
//		map.put("blah", null);
//		eventData.putStringMap(IdentityTestConstants.EventDataKeys.Identity.IDENTIFIERS, map);
//
//		Map<String, String> ids = identityModule.extractIdentifiers(eventData);
//		assertNotNull(ids);
//		assertEquals(1, ids.size());
//	}
//
//	@Test
//	public void extractIdentifiers_Should_ReturnEmptyMap_WhenIDTypeKeyPresentInEventData_WithNullValue() {
//		EventData eventData = new EventData();
//		HashMap<String, String> map = new HashMap<String, String>();
//		map.put(null, null);
//		eventData.putStringMap(IdentityTestConstants.EventDataKeys.Identity.IDENTIFIERS, map);
//
//		HashMap<String, String> ids = (HashMap<String, String>) identityModule.extractIdentifiers(eventData);
//		assertNotNull(ids);
//		assertTrue(ids.isEmpty());
//	}
//
//	@Test
//	public void extractIdentifiers_Should_ReturnNonEmptyMap_WhenIDTypeKeyPresentInEventData_WithEmptyValue() {
//		EventData eventData = new EventData();
//		HashMap<String, String> map = new HashMap<String, String>();
//		map.put("", "");
//		eventData.putStringMap(IdentityTestConstants.EventDataKeys.Identity.IDENTIFIERS, map);
//		HashMap<String, String> ids = (HashMap<String, String>) identityModule.extractIdentifiers(eventData);
//		assertNotNull(ids);
//		assertEquals(1, ids.size());
//	}
//
//	@Test
//	public void extractIdentifiers_Should_ReturnEmptyMap_WhenEmptyEventData() {
//		EventData eventData = new EventData();
//		HashMap<String, String> map = new HashMap<String, String>();
//		eventData.putStringMap(IdentityTestConstants.EventDataKeys.Identity.IDENTIFIERS, map);
//
//		HashMap<String, String> ids = (HashMap<String, String>) identityModule.extractIdentifiers(eventData);
//		assertNotNull(ids);
//		assertTrue(ids.isEmpty());
//	}
//
//	@Test
//	public void extractIdentifiers_Should_ReturnEmptyMap_WhenIDENTIFIERSKeyPresentInEventData_WithNullValue() {
//		EventData eventData = new EventData();
//		eventData.putStringMap(IdentityTestConstants.EventDataKeys.Identity.IDENTIFIERS, null);
//		HashMap<String, String> ids = (HashMap<String, String>) identityModule.extractIdentifiers(eventData);
//
//		assertNotNull(ids);
//		assertTrue(ids.isEmpty());
//	}
//
//	@Test
//	public void extractIdentifiers_Should_ReturnEmptyMap_WhenAdidEvent() {
//		EventData eventData = new EventData();
//		eventData.putString(IdentityTestConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER, "test-aid");
//
//		// test
//		Map<String, String> ids = identityModule.extractIdentifiers(eventData);
//
//		// verify
//		assertNotNull(ids);
//		assertTrue(ids.isEmpty());
//	}
//
//	// ==============================================================================================================
//	// 	VisitorID extractAndUpdateAdid(final EventData eventData)
//	// ==============================================================================================================
//	@Test
//	public void extractAndUpdateAdid_InitialAdidEmpty_ReturnsNewAdid() {
//		Event adidEvent = generateAdidEvent("test_ad_id");
//
//		// test
//		identityModule.advertisingIdentifier = "";
//		IdentityGenericPair<VisitorID, Boolean> result = identityModule.extractAndUpdateAdid(adidEvent.getData());
//
//		// verify
//		assertNotNull(result);
//		assertEquals("test_ad_id", result.getFirst().getId());
//		assertEquals("d_cid_ic", result.getFirst().getIdOrigin());
//		assertEquals("DSID_20914", result.getFirst().getIdType());
//		assertEquals("test_ad_id", identityModule.advertisingIdentifier);
//		assertTrue(result.getSecond());
//	}
//
//	@Test
//	public void extractAndUpdateAdid_NewValue_ReturnsNewAdid() {
//		Event adidEvent = generateAdidEvent("test_ad_id");
//
//		// test
//		identityModule.advertisingIdentifier = "initial_adid_value";
//		IdentityGenericPair<VisitorID, Boolean> result = identityModule.extractAndUpdateAdid(adidEvent.getData());
//
//		// verify
//		assertNotNull(result);
//		assertEquals("test_ad_id", result.getFirst().getId());
//		assertEquals("d_cid_ic", result.getFirst().getIdOrigin());
//		assertEquals("DSID_20914", result.getFirst().getIdType());
//		assertEquals("test_ad_id", identityModule.advertisingIdentifier);
//		assertFalse(result.getSecond());
//	}
//
//	@Test
//	public void extractAndUpdateAdid_SameValue_ReturnsNull() {
//		Event adidEvent = generateAdidEvent("test_ad_id");
//
//		// test
//		identityModule.advertisingIdentifier = "test_ad_id";
//		IdentityGenericPair<VisitorID, Boolean> result = identityModule.extractAndUpdateAdid(adidEvent.getData());
//
//		// verify
//		assertNull(result.getFirst());
//		assertFalse(result.getSecond());
//		assertEquals("test_ad_id", identityModule.advertisingIdentifier);
//	}
//
//	@Test
//	public void extractAndUpdateAdid_SameEmptyValue_ReturnsNull() {
//		Event adidEvent = generateAdidEvent("");
//
//		// test
//		identityModule.advertisingIdentifier = "";
//		IdentityGenericPair<VisitorID, Boolean> result = identityModule.extractAndUpdateAdid(adidEvent.getData());
//
//		// verify
//		assertNull(result.getFirst());
//		assertFalse(result.getSecond());
//		assertEquals("", identityModule.advertisingIdentifier);
//	}
//
//	@Test
//	public void extractAndUpdateAdid_NoAdidInEventData_ReturnsNull() {
//		EventData nonAdidEventData = new EventData();
//		HashMap<String, String> map = new HashMap<String, String>();
//		map.put("idtype", "id");
//		nonAdidEventData.putStringMap(IdentityTestConstants.EventDataKeys.Identity.IDENTIFIERS, map);
//
//		// test
//		identityModule.advertisingIdentifier = "test_ad_id";
//		IdentityGenericPair<VisitorID, Boolean> result = identityModule.extractAndUpdateAdid(nonAdidEventData);
//
//		// verify
//		assertNull(result.getFirst());
//		assertFalse(result.getSecond());
//		assertEquals("test_ad_id", identityModule.advertisingIdentifier);
//	}
//
//	@Test
//	public void extractAndUpdateAdid_EmptyAdidInEventData_ReturnsVisitorIdWithEmptyId() {
//		Event adidEvent = generateAdidEvent("");
//
//		// test
//		identityModule.advertisingIdentifier = "initial_adid_value";
//		IdentityGenericPair<VisitorID, Boolean> result = identityModule.extractAndUpdateAdid(adidEvent.getData());
//
//		// verify
//		assertNotNull(result.getFirst());
//		assertEquals("", result.getFirst().getId());
//		assertEquals("d_cid_ic", result.getFirst().getIdOrigin());
//		assertEquals("DSID_20914", result.getFirst().getIdType());
//		assertEquals("", identityModule.advertisingIdentifier);
//		assertTrue(result.getSecond());
//	}
//
//	@Test
//	public void extractAndUpdateAdid_ZeroAdidInEventData_ReturnsVisitorIdWithEmptyId() {
//		Event adidEvent = generateAdidEvent(IdentityTestConstants.Defaults.ZERO_ADVERTISING_ID);
//
//		// test
//		identityModule.advertisingIdentifier = "initial_adid_value";
//		IdentityGenericPair<VisitorID, Boolean> result = identityModule.extractAndUpdateAdid(adidEvent.getData());
//
//		// verify
//		assertNotNull(result.getFirst());
//		assertEquals("", result.getFirst().getId());
//		assertEquals("d_cid_ic", result.getFirst().getIdOrigin());
//		assertEquals("DSID_20914", result.getFirst().getIdType());
//		assertEquals("", identityModule.advertisingIdentifier);
//		assertTrue(result.getSecond());
//	}
//
//	@Test
//	public void extractAndUpdateAdid_InitialAdidZero_ReturnsNewAdid() {
//		Event adidEvent = generateAdidEvent("test_ad_id");
//
//		// test
//		identityModule.advertisingIdentifier = IdentityTestConstants.Defaults.ZERO_ADVERTISING_ID;
//		IdentityGenericPair<VisitorID, Boolean> result = identityModule.extractAndUpdateAdid(adidEvent.getData());
//
//		// verify
//		assertNotNull(result);
//		assertEquals("test_ad_id", result.getFirst().getId());
//		assertEquals("d_cid_ic", result.getFirst().getIdOrigin());
//		assertEquals("DSID_20914", result.getFirst().getIdType());
//		assertEquals("test_ad_id", identityModule.advertisingIdentifier);
//		assertTrue(result.getSecond());
//	}
//
//	@Test
//	public void extractAndUpdateAdid_SameZeroValue_ReturnsEmpty() {
//		Event adidEvent = generateAdidEvent(IdentityTestConstants.Defaults.ZERO_ADVERTISING_ID);
//
//		// test
//		identityModule.advertisingIdentifier = IdentityTestConstants.Defaults.ZERO_ADVERTISING_ID;
//		IdentityGenericPair<VisitorID, Boolean> result = identityModule.extractAndUpdateAdid(adidEvent.getData());
//
//		// verify
//		assertEquals("", result.getFirst().getId());
//		assertEquals("d_cid_ic", result.getFirst().getIdOrigin());
//		assertEquals("DSID_20914", result.getFirst().getIdType());
//		assertEquals("", identityModule.advertisingIdentifier);
//		assertTrue(result.getSecond()); // True because zero ad ID changed to empty string, as expected
//	}
//
//	@Test
//	public void extractAndUpdateAdid_InitialAdidZero_EmptyAdidInEventData_ReturnsEmpty() {
//		Event adidEvent = generateAdidEvent("");
//
//		// test
//		identityModule.advertisingIdentifier = IdentityTestConstants.Defaults.ZERO_ADVERTISING_ID;
//		IdentityGenericPair<VisitorID, Boolean> result = identityModule.extractAndUpdateAdid(adidEvent.getData());
//
//		// verify
//		assertEquals("", result.getFirst().getId());
//		assertEquals("d_cid_ic", result.getFirst().getIdOrigin());
//		assertEquals("DSID_20914", result.getFirst().getIdType());
//		assertEquals("", identityModule.advertisingIdentifier);
//		assertTrue(result.getSecond()); // True because zero ad ID changed to empty string, as expected
//	}
//
//	@Test
//	public void extractAndUpdateAdid_InitialAdidEmpty_ZeroAdidInEventData_ReturnsNull() {
//		Event adidEvent = generateAdidEvent(IdentityTestConstants.Defaults.ZERO_ADVERTISING_ID);
//
//		// test
//		identityModule.advertisingIdentifier = "";
//		IdentityGenericPair<VisitorID, Boolean> result = identityModule.extractAndUpdateAdid(adidEvent.getData());
//
//		// verify
//		assertNull(result.getFirst());
//		assertFalse(result.getSecond());
//		assertEquals("", identityModule.advertisingIdentifier);
//	}
//
//	// ==============================================================================================================
//	// void handleAppendURL(final Event event, final ConfigurationSharedStateIdentity configSharedState)
//	// ==============================================================================================================
//	@Test
//	public void handleAppendURL_Should_CallAppendVisitorInfoURL() {
//		// prepare
//		EventData eventData = new EventData();
//		eventData.putString(IdentityTestConstants.EventDataKeys.Identity.BASE_URL,  "url");
//		Event event = new Event.Builder("test-identifiers-event", EventType.IDENTITY,
//										EventSource.REQUEST_IDENTITY).setData(eventData).setResponsePairID("pairID").build();
//
//		setAnalyticsSharedStateToEventHub(1, genericAnalyticsSharedState);
//
//		// test
//		identityModule.handleAppendURL(event, genericConfigurationSharedState, genericAnalyticsSharedState);
//
//		// verify
//		assertTrue(identityModule.appendVisitorInfoForURLWasCalled);
//		assertEquals("url", identityModule.appendVisitorInfoForURLParamBaseURL);
//		assertEquals("pairID", identityModule.appendVisitorInfoForURLParamPairID);
//		assertEquals(genericConfigurationSharedState, identityModule.appendVisitorInfoForURLParamConfigSharedState);
//		assertEquals(genericAnalyticsSharedState, identityModule.appendVisitorInfoForURLParamAnalyticsSharedState);
//	}
//
//	// ==============================================================================================================
//	// void handleGetUrlVariables(final Event event, final ConfigurationSharedStateIdentity configSharedState)
//	// ==============================================================================================================
//	@Test
//	public void handleGetUrlVariables_Should_CallGenerateVisitorIDURLPayload_DispatchResponseEvent() {
//		// prepare
//		EventData eventData = new EventData();
//		eventData.putBoolean(IdentityTestConstants.EventDataKeys.Identity.URL_VARIABLES,  true);
//		Event event = new Event.Builder("test-url-variables-event", EventType.IDENTITY,
//										EventSource.REQUEST_IDENTITY).setData(eventData).setResponsePairID("pairID").build();
//
//		setAnalyticsSharedStateToEventHub(1, genericAnalyticsSharedState);
//
//		// test
//		identityModule.handleGetUrlVariables(event, genericConfigurationSharedState, genericAnalyticsSharedState);
//
//		// verify
//		assertTrue(identityModule.generateVisitorIDURLPayloadWasCalled);
//		assertEquals(genericConfigurationSharedState, identityModule.generateVisitorIDURLPayloadParamConfigSharedState);
//		assertEquals(genericAnalyticsSharedState, identityModule.generateVisitorIDURLPayloadParamAnalyticsSharedState);
//
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.containsKey(
//					   IdentityTestConstants.EventDataKeys.Identity.URL_VARIABLES));
//		assertEquals("pairID", mockDispatcherIdentityResponseEvent.dispatchResponseParameterPairID);
//	}
//
//	// ==============================================================================================================
//	// void updatePushIdentifier(final String pushId)
//	// ==============================================================================================================
//	@Test
//	public void
//	testUpdatePushIdentifier_when_ProcessNewPushTokenReturnsFalse_then_UnchangedStatusAndNoUpdatePersistenceAndNoAnalyticsHit() {
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//		dataStore.setBoolean(IdentityConstants.DataStoreKeys.ANALYTICS_PUSH_SYNC, true); // mock analytics already synced
//
//		boolean existingPushEnabledStatus = dataStore.getBoolean(IdentityTestConstants.DataStoreKeys.PUSH_ENABLED, false);
//
//		identityModule.updatePushIdentifier(null);
//
//		boolean newPushEnabledStatus = dataStore.getBoolean(IdentityTestConstants.DataStoreKeys.PUSH_ENABLED, false);
//		assertEquals(existingPushEnabledStatus, newPushEnabledStatus);
//		assertFalse(newPushEnabledStatus);
//		assertFalse(mockDispatcherIdentityAnalyticsEvent.dispatchAnalyticseWasCalled);
//	}
//
//	@Test
//	public void testUpdatePushIdentifier_when_NewNullPushToken_then_DisableStatusAndUpdatePersistenceAndSendAnalyticsHit() {
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//		dataStore.setString(IdentityTestConstants.DataStoreKeys.PUSH_IDENTIFIER, "tempToken");
//		dataStore.setBoolean(IdentityTestConstants.DataStoreKeys.PUSH_ENABLED, true);
//
//		identityModule.updatePushIdentifier(null);
//
//		boolean newPushEnabledStatus = dataStore.getBoolean(IdentityTestConstants.DataStoreKeys.PUSH_ENABLED, false);
//		assertFalse(newPushEnabledStatus);
//		assertTrue(mockDispatcherIdentityAnalyticsEvent.dispatchAnalyticseWasCalled);
//
//		EventData analyticsHitData = mockDispatcherIdentityAnalyticsEvent.analyticsDataDispatched;
//		assertNotNull(analyticsHitData);
//		assertEquals(IdentityTestConstants.EventDataKeys.Identity.PUSH_ID_ENABLED_ACTION_NAME,
//					 analyticsHitData.optString(IdentityTestConstants.EventDataKeys.Analytics.TRACK_ACTION, null));
//
//		HashMap<String, String>analyticsDataContextData = (HashMap<String, String>) analyticsHitData.optStringMap(
//					IdentityTestConstants.EventDataKeys.Analytics.CONTEXT_DATA,
//					null);
//		assertNotNull(analyticsDataContextData);
//		assertNotNull(analyticsDataContextData.get(IdentityTestConstants.EventDataKeys.Identity.EVENT_PUSH_STATUS));
//		assertEquals(String.valueOf(false), analyticsDataContextData.get(
//						 IdentityTestConstants.EventDataKeys.Identity.EVENT_PUSH_STATUS));
//	}
//
//	@Test
//	public void
//	testUpdatePushIdentifier_when_NullPushTokenFirstTime_then_DisableStatusAndUpdatePersistenceAndSendAnalyticsHit() {
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//		dataStore.setString(IdentityTestConstants.DataStoreKeys.PUSH_IDENTIFIER, "tempToken");
//
//		// Don't set PUSH_ENABLED in DataStore to simulate first time calling
//
//		identityModule.updatePushIdentifier(null);
//
//		boolean newPushEnabledStatus = dataStore.getBoolean(IdentityTestConstants.DataStoreKeys.PUSH_ENABLED, false);
//		assertFalse(newPushEnabledStatus);
//		assertTrue(mockDispatcherIdentityAnalyticsEvent.dispatchAnalyticseWasCalled);
//
//		EventData analyticsHitData = mockDispatcherIdentityAnalyticsEvent.analyticsDataDispatched;
//		assertNotNull(analyticsHitData);
//		assertEquals(IdentityTestConstants.EventDataKeys.Identity.PUSH_ID_ENABLED_ACTION_NAME,
//					 analyticsHitData.optString(IdentityTestConstants.EventDataKeys.Analytics.TRACK_ACTION, null));
//
//		HashMap<String, String>analyticsDataContextData = (HashMap<String, String>) analyticsHitData.optStringMap(
//					IdentityTestConstants.EventDataKeys.Analytics.CONTEXT_DATA,
//					null);
//		assertNotNull(analyticsDataContextData);
//		assertNotNull(analyticsDataContextData.get(IdentityTestConstants.EventDataKeys.Identity.EVENT_PUSH_STATUS));
//		assertEquals(String.valueOf(false), analyticsDataContextData.get(
//						 IdentityTestConstants.EventDataKeys.Identity.EVENT_PUSH_STATUS));
//	}
//
//	@Test
//	public void
//	testUpdatePushIdentifier_when_NewNonNullPushToken_then_EnableStatusAndUpdatePersistenceAndSendAnalyticsHit() {
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//
//		identityModule.updatePushIdentifier("testToken");
//
//		boolean newPushEnabledStatus = dataStore.getBoolean(IdentityTestConstants.DataStoreKeys.PUSH_ENABLED, false);
//		assertTrue(newPushEnabledStatus);
//		assertTrue(mockDispatcherIdentityAnalyticsEvent.dispatchAnalyticseWasCalled);
//
//		EventData analyticsHitData = mockDispatcherIdentityAnalyticsEvent.analyticsDataDispatched;
//		assertNotNull(analyticsHitData);
//		assertEquals(IdentityTestConstants.EventDataKeys.Identity.PUSH_ID_ENABLED_ACTION_NAME,
//					 analyticsHitData.optString(IdentityTestConstants.EventDataKeys.Analytics.TRACK_ACTION, null));
//
//		HashMap<String, String>analyticsDataContextData = (HashMap<String, String>) analyticsHitData.optStringMap(
//					IdentityTestConstants.EventDataKeys.Analytics.CONTEXT_DATA,
//					null);
//		assertNotNull(analyticsDataContextData);
//		assertNotNull(analyticsDataContextData.get(IdentityTestConstants.EventDataKeys.Identity.EVENT_PUSH_STATUS));
//		assertEquals(String.valueOf(true), analyticsDataContextData.get(
//						 IdentityTestConstants.EventDataKeys.Identity.EVENT_PUSH_STATUS));
//	}
//
//	@Test
//	public void
//	testUpdatePushIdentifier_when_NullDataStore() {
//		// setup
//		platformServices.fakeLocalStorageService = null;
//		identityModule.setDataStore(null);
//
//		// test
//		identityModule.updatePushIdentifier("testToken");
//
//		// verify
//		assertFalse(mockDispatcherIdentityAnalyticsEvent.dispatchAnalyticseWasCalled);
//	}
//
//	// ==============================================================================================================
//	// boolean processNewPushToken(final String pushToken)
//	// ==============================================================================================================
//	@Test
//	public void testProcessNewPushToken_when_inputIsNullAndExistingIsNull_then_returnTrueAndPreferencesUpdate() {
//		boolean shouldProcessNullToken = identityModule.processNewPushToken(null);
//		assertTrue(shouldProcessNullToken);
//
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//
//		String persistedPushID = dataStore.getString(IdentityTestConstants.DataStoreKeys.PUSH_IDENTIFIER, null);
//		assertNull(persistedPushID);
//
//		boolean analyticsSynced = dataStore.getBoolean(IdentityTestConstants.DataStoreKeys.ANALYTICS_PUSH_SYNC, false);
//		assertTrue("Expected analytics push sync true.", analyticsSynced);
//	}
//
//	@Test
//	public void
//	testProcessNewPushToken_when_inputIsNullAndExistingIsNullButAnalyticsSyncTrue_then_returnFalseAndNoPreferencesUpdate() {
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//
//		dataStore.setBoolean(IdentityTestConstants.DataStoreKeys.ANALYTICS_PUSH_SYNC, true);
//
//		boolean shouldProcessNullToken = identityModule.processNewPushToken(null);
//		assertFalse(shouldProcessNullToken);
//
//		String persistedPushID = dataStore.getString(IdentityTestConstants.DataStoreKeys.PUSH_IDENTIFIER, null);
//		assertNull(persistedPushID);
//	}
//
//	@Test
//	public void testProcessNewPushToken_when_inputIsEmptyAndExistingIsNull_then_returnTrueAndPreferencesUpdate() {
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//
//		boolean shouldProcessEmptyToken = identityModule.processNewPushToken("");
//		assertTrue(shouldProcessEmptyToken);
//
//		String persistedPushID = dataStore.getString(IdentityTestConstants.DataStoreKeys.PUSH_IDENTIFIER, null);
//		assertNull(persistedPushID);
//
//		boolean analyticsSynced = dataStore.getBoolean(IdentityTestConstants.DataStoreKeys.ANALYTICS_PUSH_SYNC, false);
//		assertTrue("Expected analytics push sync true.", analyticsSynced);
//	}
//
//	@Test
//	public void
//	testProcessNewPushToken_when_inputIsEmptyAndExistingIsNullButAnalyticsSyncTrue_then_returnFalseAndNoPreferencesUpdate() {
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//
//		dataStore.setBoolean(IdentityTestConstants.DataStoreKeys.ANALYTICS_PUSH_SYNC, true);
//
//		boolean shouldProcessNullToken = identityModule.processNewPushToken("");
//		assertFalse(shouldProcessNullToken);
//
//		String persistedPushID = dataStore.getString(IdentityTestConstants.DataStoreKeys.PUSH_IDENTIFIER, null);
//		assertNull(persistedPushID);
//	}
//
//	@Test
//	public void testProcessNewPushToken_when_inputIsValidAndMatchesExisting_then_returnFalseAndNoPreferencesUpdate() {
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//
//		dataStore.setString(IdentityTestConstants.DataStoreKeys.PUSH_IDENTIFIER, "tempToken");
//
//		boolean shouldProcessExistingToken = identityModule.processNewPushToken("tempToken");
//
//		assertFalse(shouldProcessExistingToken);
//		assertEquals(dataStore.getString(IdentityTestConstants.DataStoreKeys.PUSH_IDENTIFIER, null), "tempToken");
//	}
//
//	@Test
//	public void testProcessNewPushToken_when_inputIsValidAndDoesNotMatchExisting_then_returnTrueAndUpdatePreferences() {
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//
//		dataStore.setString(IdentityTestConstants.DataStoreKeys.PUSH_IDENTIFIER, "tempToken");
//
//		boolean shouldProcessNonExistingToken = identityModule.processNewPushToken("processNewPushToken");
//		assertTrue(shouldProcessNonExistingToken);
//		assertEquals(dataStore.getString(IdentityTestConstants.DataStoreKeys.PUSH_IDENTIFIER, null), "processNewPushToken");
//
//		boolean analyticsSynced = dataStore.getBoolean(IdentityTestConstants.DataStoreKeys.ANALYTICS_PUSH_SYNC, false);
//		assertTrue("Expected analytics push sync true.", analyticsSynced);
//	}
//
//	@Test
//	public void testProcessNewPushToken_when_inputIsNullAndDoesNotMatchExisting_then_returnTrueAndUpdatePreferences() {
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//
//		dataStore.setString(IdentityTestConstants.DataStoreKeys.PUSH_IDENTIFIER, "tempToken");
//
//		boolean shouldProcessNullToken = identityModule.processNewPushToken(null);
//
//		assertTrue(shouldProcessNullToken);
//		assertNull(dataStore.getString(IdentityTestConstants.DataStoreKeys.PUSH_IDENTIFIER, null));
//
//		boolean analyticsSynced = dataStore.getBoolean(IdentityTestConstants.DataStoreKeys.ANALYTICS_PUSH_SYNC, false);
//		assertTrue("Expected analytics push sync true.", analyticsSynced);
//	}
//
//
//	// ==============================================================================================================
//	// void appendVisitorInfoForURL(final String baseURL, final String pairID,
//	//								final ConfigurationSharedStateIdentity configData, final EventData analyticsSharedState)
//	// ==============================================================================================================
//	@Test
//	public void testAppendVisitorInfoForURL_ShouldDispatchNullUpdatedURL_When_BaseURLIsNull() {
//		identityModule.appendVisitorInfoForURL(null, null, genericConfigurationSharedState, genericAnalyticsSharedState);
//
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertNull(eventHub.dispatchedEvent.getData().optString(IdentityTestConstants.EventDataKeys.Identity.UPDATED_URL,
//				   null));
//	}
//
//	@Test
//	public void testAppendVisitorInfoForURL_ShouldDispatchEmptyUpdatedURL_When_BaseURLIsEmpty() {
//		identityModule.appendVisitorInfoForURL("", null, genericConfigurationSharedState, genericAnalyticsSharedState);
//
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertNotNull(eventHub.dispatchedEvent.getData().optString(IdentityTestConstants.EventDataKeys.Identity.UPDATED_URL,
//					  null));
//		assertEquals(0, eventHub.dispatchedEvent.getData().optString(IdentityTestConstants.EventDataKeys.Identity.UPDATED_URL,
//					 null).length());
//	}
//
//	@Test
//	public void testAppendVisitorInfoForURL_ShouldDispatchUpdatedURL_When_BaseURLIsNonNullNonEmpty() {
//		identityModule.appendVisitorInfoForURL("test-base-url", null, genericConfigurationSharedState,
//											   genericAnalyticsSharedState);
//
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//
//		EventData eventData = eventHub.dispatchedEvent.getData();
//		assertNotNull(eventData);
//		assertTrue(eventData.containsKey(IdentityTestConstants.EventDataKeys.Identity.UPDATED_URL));
//	}
//
//	@Test
//	public void testAppendVisitorInfoForURL_ShouldPutQuestionMark_When_BaseURL_IS_NOTNULL() {
//		StringBuilder mockVisitorIDURLPayload = new
//		StringBuilder(
//			"adobe_mc=MCMID%3D83056071767212492011535942034357093219%7CMCAID%3DMOCK_ANALYTICS_ID&adobe_aa_vid" +
//			"=Taz");
//		identityModule.setGenerateVisitorIDURLPayload(mockVisitorIDURLPayload);
//
//		String baseURL = "test-base-url";
//		identityModule.appendVisitorInfoForURL(baseURL, null, genericConfigurationSharedState, genericAnalyticsSharedState);
//
//		EventData eventData = eventHub.dispatchedEvent.getData();
//		assertTrue(eventData.containsKey(IdentityTestConstants.EventDataKeys.Identity.UPDATED_URL));
//		String updatedURL = eventData.optString(IdentityTestConstants.EventDataKeys.Identity.UPDATED_URL, null);
//		assertTrue(updatedURL.charAt(baseURL.length()) == '?');
//	}
//
//	@Test
//	public void testAppendVisitorInfoForURL_ShouldPutAmpersand_When_BaseURL_Has_QuestionMark() {
//		StringBuilder mockVisitorIDURLPayload = new
//		StringBuilder(
//			"adobe_mc=MCMID%3D83056071767212492011535942034357093219%7CMCAID%3DMOCK_ANALYTICS_ID&adobe_aa_vid" +
//			"=Taz");
//		identityModule.setGenerateVisitorIDURLPayload(mockVisitorIDURLPayload);
//
//		String baseURL = "test-base-?url";
//		identityModule.appendVisitorInfoForURL(baseURL, null, genericConfigurationSharedState, genericAnalyticsSharedState);
//
//		EventData eventData = eventHub.dispatchedEvent.getData();
//		assertTrue(eventData.containsKey(IdentityTestConstants.EventDataKeys.Identity.UPDATED_URL));
//		String updatedURL = eventData.optString(IdentityTestConstants.EventDataKeys.Identity.UPDATED_URL, null);
//		assertTrue(updatedURL.charAt(baseURL.length()) == '&');
//	}
//
//	// ==============================================================================================================
//	// 	void appendVisitorInfoForURL(final String baseURL, final String pairID,
//	//								 final ConfigurationSharedStateIdentity configData, EventData analyticsSharedState)
//	// ==============================================================================================================
//	@Test
//	public void appendVisitorInfoForURL_Should_HandleAllURLs() throws Exception {
//
//		InputStream is = null;
//
//		try {
//			is = new FileInputStream(getResource("SampleURLTestSet.tab"));
//		} catch (Exception ex) {
//			Log.debug(IdentityExtension.LOG_SOURCE, "Could not read the sample urls file!");
//		}
//
//		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
//
//		String line = buf.readLine();
//		StringBuilder sb = new StringBuilder();
//		String[] testComponents;
//		int lineNumber = 0;
//
//		while (line != null) {
//			lineNumber++;
//
//			if (lineNumber == 1 || line.equals("")) {
//				line = buf.readLine();
//				continue;
//			}
//
//			sb.append(line).append("\n");
//			testComponents = line.split("\t");
//			System.out.println("Contents : " + Arrays.toString(testComponents));
//			String testURL = testComponents[0];
//			StringBuilder idPayload = new StringBuilder(testComponents[1]);
//			String expectedResult = testComponents[2];
//
//			identityModule.setGenerateVisitorIDURLPayload(idPayload);
//
//			/*
//			 * Run
//			 */
//			identityModule.appendVisitorInfoForURL(testURL, null, genericConfigurationSharedState, genericAnalyticsSharedState);
//
//			/*
//			 * Test
//			 */
//			EventData eventData = eventHub.dispatchedEvent.getData();
//			assertNotNull(eventData);
//			String returnURL = eventData.optString(IdentityTestConstants.EventDataKeys.Identity.UPDATED_URL, null);
//			assertEquals(expectedResult, returnURL);
//
//			line = buf.readLine();
//		}
//	}
//
//	@Test
//	public void appendVisitorInfoForURL_Should_ReturnNullWhenNullURLArg() throws Exception {
//		String mockVisitorIDURLPayload =
//			"adobe_mc=MCMID%3D83056071767212492011535942034357093219%7CMCAID%3DMOCK_ANALYTICS_ID&adobe_aa_vid" +
//			"=Taz";
//		String expectedResult = null;
//		String inputURL = null;
//		executeIdentityAppendVisitorInfoForURL(mockVisitorIDURLPayload, inputURL, expectedResult);
//	}
//
//	@Test
//	public void appendVisitorInfoForURL_Should_ReturnEmptyStringWhenEmptyURLArg() throws Exception {
//		String mockVisitorIDURLPayload =
//			"adobe_mc=MCMID%3D83056071767212492011535942034357093219%7CMCAID%3DMOCK_ANALYTICS_ID&adobe_aa_vid" +
//			"=Taz";
//		String expectedResult = "";
//		String inputURL = "";
//		executeIdentityAppendVisitorInfoForURL(mockVisitorIDURLPayload, inputURL, expectedResult);
//	}
//
//	@Test
//	public void appendVisitorInfoForURL_Should_ReturnArgStringWhenEmptyIDPayload() throws Exception {
//		String expectedResult = "http://www.google.com";
//		String inputURL = "http://www.google.com";
//		executeIdentityAppendVisitorInfoForURL("", inputURL, expectedResult);
//	}
//
//	private void executeIdentityAppendVisitorInfoForURL(final String visitorPayload, final String givenUrl,
//			final String expectedUrl) {
//		identityModule.setGenerateVisitorIDURLPayload(new StringBuilder(visitorPayload));
//		identityModule.appendVisitorInfoForURL(givenUrl, null, genericConfigurationSharedState, genericAnalyticsSharedState);
//
//		EventData eventData = eventHub.dispatchedEvent.getData();
//		assertNotNull(eventData);
//		String returnURL = eventData.optString(IdentityTestConstants.EventDataKeys.Identity.UPDATED_URL, null);
//		assertEquals(expectedUrl, returnURL);
//	}
//
//	// ==============================================================================================================
//	// 	EventData packageEventData()
//	// ==============================================================================================================
//	@Test
//	public void packageEventDataMustHaveRequiredKeys() {
//		populateIdentifiers(identityModule);
//
//		EventData packagedData = identityModule.packageEventData();
//		assertTrue(packagedData.containsKey(IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_MID));
//		assertTrue(packagedData.containsKey(IdentityTestConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER));
//		assertTrue(packagedData.containsKey(IdentityTestConstants.EventDataKeys.Identity.PUSH_IDENTIFIER));
//		assertTrue(packagedData.containsKey(IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB));
//		assertTrue(packagedData.containsKey(IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT));
//		assertTrue(packagedData.containsKey(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LIST));
//		assertTrue(packagedData.containsKey(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LAST_SYNC));
//	}
//
//	// ==============================================================================================================
//	// 	String generateMID()
//	// ==============================================================================================================
//	@Test
//	public void generateMID_ShouldNeverReturnNull_AlwaysReturnSpecificLength() {
//		String manualId = identityModule.generateMID();
//		assertNotNull("manual id generation should not be null", manualId);
//		assertEquals("length should be correct", 38, manualId.length());
//	}
//
//	@Test
//	public void generateMID_ShouldContainOnlyNumbers() throws Exception {
//		String manualId = identityModule.generateMID();
//		assertNotNull("manual id generation should not be null", manualId);
//		assertTrue("id should contains only number", manualId.matches("[0-9]+"));
//	}
//
//	@Test
//	public void generateMID_ShouldBeReasonablyRandom() throws Exception {
//		final int count = 10000;
//		HashMap<String, String> dictionary = new HashMap<String, String>(count);
//
//		for (int i = 0; i < count; i++) {
//			String mid = identityModule.generateMID();
//			dictionary.put(mid, mid);
//		}
//
//		assertEquals("manual generation should be reasonably random over 10000 runs", count, dictionary.size());
//	}
//
//	// ==============================================================================================================
//	// 	String generateURLEncodedValuesCustomerIdString(final List<VisitorID> visitorIDs)
//	// ==============================================================================================================
//	@Test
//	public void generateURLEncodedValuesCustomerIdString_WhenNullIDs() throws Exception {
//		String testStoredIDString = identityModule.generateURLEncodedValuesCustomerIdString(null);
//		assertNull(testStoredIDString);
//	}
//
//	@Test
//	public void generateURLEncodedValuesCustomerIdString_ShouldGenerateCorrectString() throws Exception {
//		List<VisitorID> visitorIDList = new ArrayList<VisitorID>();
//		visitorIDList.add(new VisitorID("d_cid_ic", "!loginidhash", "97717",
//										VisitorID.AuthenticationState.AUTHENTICATED));
//		visitorIDList.add(new VisitorID("d_cid_ic", "psnidhash", "!1144032295",
//										VisitorID.AuthenticationState.LOGGED_OUT));
//		visitorIDList.add(new VisitorID("d_cid_ic", "!xboxlivehash", "1629158955",
//										VisitorID.AuthenticationState.UNKNOWN));
//
//		String testStoredIDString = identityModule.generateURLEncodedValuesCustomerIdString(visitorIDList);
//		String expectedString =
//			"d_cid_ic=loginidhash%0197717%011&d_cid_ic=psnidhash%01%211144032295%012&d_cid_ic" +
//			"=xboxlivehash%011629158955%010";
//
//		assertThat(testStoredIDString, unorderedStringParameterMatches(expectedString, "&"));
//	}
//
//	@Test
//	public void generateURLEncodedValuesCustomerIdString_ShouldGenerateCorrectStringForIDs() throws Exception {
//		List<VisitorID> visitorIDList = new ArrayList<VisitorID>();
//		visitorIDList.add(new VisitorID("d_cid_ic", "loginidhash", "97717",
//										VisitorID.AuthenticationState.UNKNOWN));
//		visitorIDList.add(new VisitorID("d_cid_ic", "psnidhash", "1144032295",
//										VisitorID.AuthenticationState.LOGGED_OUT));
//		visitorIDList.add(new VisitorID("d_cid_ic", "xboxlivehash", "1629158955",
//										VisitorID.AuthenticationState.AUTHENTICATED));
//
//		String testStoredIDString = identityModule.generateURLEncodedValuesCustomerIdString(visitorIDList);
//		String expectedString =
//			"d_cid_ic=loginidhash%0197717%010&d_cid_ic=psnidhash%011144032295%012&d_cid_ic" +
//			"=xboxlivehash%011629158955%011";
//
//		assertThat(testStoredIDString, unorderedStringParameterMatches(expectedString, "&"));
//	}
//
//	// ==============================================================================================================
//	// 	List<VisitorID> generateCustomerIds(final Map<String, String> identifiers,
//	//										final visitorID.getAuthenticationState() authenticationState)
//	// ==============================================================================================================
//	@Test
//	public void generateCustomerIds_ForIDIDTypeMap() throws Exception {
//		HashMap<String, String> testIds = new HashMap<String, String>();
//		testIds.put("id1", "hodor");
//		testIds.put("id2", "h-hodor");
//		testIds.put("id3", "h-h-hodor");
//
//		List<VisitorID> mockVisitorIDList = new ArrayList<VisitorID>();
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id1", "hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id2", "h-hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id3", "h-h-hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//
//		List<VisitorID> visitorIDList = identityModule.generateCustomerIds(testIds,
//										VisitorID.AuthenticationState.AUTHENTICATED);
//		assertThat(visitorIDList, listEquals(mockVisitorIDList));
//	}
//
//	@Test
//	public void generateCustomerIds_WhenNullID() throws Exception {
//		List<VisitorID> visitorIDList = identityModule.generateCustomerIds(null,
//										VisitorID.AuthenticationState.AUTHENTICATED);
//		assertTrue(visitorIDList.isEmpty());
//	}
//
//	// ==============================================================================================================
//	// 	List<VisitorID> mergeCustomerIds(final List<VisitorID> newCustomerIds)
//	// ==============================================================================================================
//	@Test
//	public void mergeCustomerIds_WhenNullIDsToAdd_shouldReturnExistingIDs() throws Exception {
//		List<VisitorID> mockVisitorIDList = new ArrayList<VisitorID>();
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id1", "hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		identityModule.customerIds = mockVisitorIDList;
//
//		// test
//		List<VisitorID> visitorIDList = identityModule.mergeCustomerIds(null);
//
//
//		// verify
//		assertThat(visitorIDList, listEquals(mockVisitorIDList));
//	}
//
//	@Test
//	public void mergeCustomerIds_Should_AddToExistingIDs() {
//		//setup
//		List<VisitorID> mockVisitorIDList = new ArrayList<VisitorID>();
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id1", "id1Value",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id2", "id2Value",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id3", "id3Value",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//
//		identityModule.customerIds = mockVisitorIDList;
//
//		List<VisitorID> newCustomerIds = new ArrayList<VisitorID>();
//		newCustomerIds.add(new VisitorID("d_cid_ic", "id4", "id4Value",
//										 VisitorID.AuthenticationState.LOGGED_OUT));
//
//		// test
//		List<VisitorID> resultVisitorIdsList = identityModule.mergeCustomerIds(newCustomerIds);
//
//		// verify
//		List<VisitorID> expectedVisitorIdsList = new ArrayList<VisitorID>();
//		expectedVisitorIdsList.add(new VisitorID("d_cid_ic", "id1", "id1Value",
//								   VisitorID.AuthenticationState.AUTHENTICATED));
//		expectedVisitorIdsList.add(new VisitorID("d_cid_ic", "id2", "id2Value",
//								   VisitorID.AuthenticationState.AUTHENTICATED));
//		expectedVisitorIdsList.add(new VisitorID("d_cid_ic", "id3", "id3Value",
//								   VisitorID.AuthenticationState.AUTHENTICATED));
//		expectedVisitorIdsList.add(new VisitorID("d_cid_ic", "id4", "id4Value",
//								   VisitorID.AuthenticationState.LOGGED_OUT));
//
//		assertThat(expectedVisitorIdsList, listEquals(resultVisitorIdsList));
//	}
//
//	@Test
//	public void mergeCustomerIds_Should_UpdateExistingIDs() {
//		// setup
//		List<VisitorID> mockVisitorIDList = new ArrayList<VisitorID>();
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id1", "id1Value",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id2", "id2Value",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id3", "id3Value",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//
//		identityModule.customerIds = mockVisitorIDList;
//
//		List<VisitorID> newCustomerIds = new ArrayList<VisitorID>();
//		newCustomerIds.add(new VisitorID("d_cid_ic", "id2", "id2ValueUpdated",
//										 VisitorID.AuthenticationState.LOGGED_OUT));
//		newCustomerIds.add(new VisitorID("d_cid_ic", "id4", "id4Value",
//										 VisitorID.AuthenticationState.LOGGED_OUT));
//
//		// test
//		List<VisitorID> resultVisitorIdsList = identityModule.mergeCustomerIds(newCustomerIds);
//
//		// verify
//		List<VisitorID> expectedVisitorIdsList = new ArrayList<VisitorID>();
//		expectedVisitorIdsList.add(new VisitorID("d_cid_ic", "id1", "id1Value",
//								   VisitorID.AuthenticationState.AUTHENTICATED));
//		expectedVisitorIdsList.add(new VisitorID("d_cid_ic", "id2", "id2ValueUpdated",
//								   VisitorID.AuthenticationState.LOGGED_OUT));
//		expectedVisitorIdsList.add(new VisitorID("d_cid_ic", "id3", "id3Value",
//								   VisitorID.AuthenticationState.AUTHENTICATED));
//		expectedVisitorIdsList.add(new VisitorID("d_cid_ic", "id4", "id4Value",
//								   VisitorID.AuthenticationState.LOGGED_OUT));
//
//		assertThat(expectedVisitorIdsList, listEquals(resultVisitorIdsList));
//	}
//
//	@Test(expected = IllegalStateException.class)
//	public void mergeCustomerIds_Should_HandleNullIDType() throws Exception {
//		/*
//		------------------ BUILD ------------------
//		 */
//		HashMap<String, String> testIds = new HashMap<String, String>();
//		testIds.put(null, "h-hodor");
//		testIds.put("id4", "h-h-h-hodor");
//
//		List<VisitorID> mockVisitorIDList = new ArrayList<VisitorID>();
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id1", "hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id2", "h-hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id3", "h-h-hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//
//		identityModule.customerIds = mockVisitorIDList;
//
//		List<VisitorID> newCustomerIds = new ArrayList<VisitorID>();
//		newCustomerIds.add(new VisitorID("d_cid_ic", null, "h-hodor",
//										 VisitorID.AuthenticationState.LOGGED_OUT));
//		newCustomerIds.add(new VisitorID("d_cid_ic", "id4", "h-h-h-hodor",
//										 VisitorID.AuthenticationState.LOGGED_OUT));
//
//		/*
//		------------------ RUN ------------------
//		 */
//		List<VisitorID> visitorIDList = identityModule.mergeCustomerIds(newCustomerIds);
//
//		/*
//		------------------ TEST ------------------
//		 */
//		List<VisitorID> testVisitorIDList = new ArrayList<VisitorID>();
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id1", "hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id2", "h-hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id3", "h-h-hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id4", "h-h-h-hodor",
//											VisitorID.AuthenticationState.LOGGED_OUT));
//
//		assertThat(visitorIDList, listEquals(testVisitorIDList));
//	}
//
//	@Test
//	public void mergeCustomerIds_Should_HandleNullID() throws Exception {
//		/*
//		------------------ BUILD ------------------
//		 */
//
//		List<VisitorID> mockVisitorIDList = new ArrayList<VisitorID>();
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id1", "hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id2", "h-hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id3", "h-h-hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//
//		identityModule.customerIds = mockVisitorIDList;
//
//		List<VisitorID> newCustomerIds = new ArrayList<VisitorID>();
//		newCustomerIds.add(new VisitorID("d_cid_ic", "id4", "h-h-h-hodor",
//										 VisitorID.AuthenticationState.LOGGED_OUT));
//		newCustomerIds.add(new VisitorID("d_cid_ic", "id5", null,
//										 VisitorID.AuthenticationState.LOGGED_OUT));
//
//		/*
//		------------------ RUN ------------------
//		 */
//		List<VisitorID> visitorIDList = identityModule.mergeCustomerIds(newCustomerIds);
//
//		/*
//		------------------ TEST ------------------
//		 */
//		List<VisitorID> testVisitorIDList = new ArrayList<VisitorID>();
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id1", "hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id2", "h-hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id3", "h-h-hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id4", "h-h-h-hodor",
//											VisitorID.AuthenticationState.LOGGED_OUT));
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id5", null,
//											VisitorID.AuthenticationState.LOGGED_OUT));
//
//		assertThat(visitorIDList, listEquals(testVisitorIDList));
//	}
//
//	@Test
//	public void mergeCustomerIds_Should_UpdateNullID() throws Exception {
//		/*
//		------------------ BUILD ------------------
//		 */
//
//		HashMap<String, String> testIds = new HashMap<String, String>();
//		testIds.put("id3", null);
//		testIds.put("id4", "h-h-h-hodor");
//
//		List<VisitorID> mockVisitorIDList = new ArrayList<VisitorID>();
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id1", "hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id2", "h-hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id3", null,
//											VisitorID.AuthenticationState.AUTHENTICATED));
//
//		identityModule.customerIds = mockVisitorIDList;
//
//		List<VisitorID> newCustomerIds = new ArrayList<VisitorID>();
//		newCustomerIds.add(new VisitorID("d_cid_ic", "id3", null,
//										 VisitorID.AuthenticationState.LOGGED_OUT));
//		newCustomerIds.add(new VisitorID("d_cid_ic", "id4", "h-h-h-hodor",
//										 VisitorID.AuthenticationState.LOGGED_OUT));
//
//		/*
//		------------------ RUN ------------------
//		 */
//		List<VisitorID> visitorIDList = identityModule.mergeCustomerIds(newCustomerIds);
//
//		/*
//		------------------ TEST ------------------
//		 */
//		List<VisitorID> testVisitorIDList = new ArrayList<VisitorID>();
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id1", "hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id2", "h-hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id3", null,
//											VisitorID.AuthenticationState.LOGGED_OUT));
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id4", "h-h-h-hodor",
//											VisitorID.AuthenticationState.LOGGED_OUT));
//
//		assertThat(visitorIDList, listEquals(testVisitorIDList));
//	}
//
//	@Test
//	public void mergeCustomerIds_Should_UpdateAdvertisingIdentifierValue() {
//		// setup
//		List<VisitorID> mockVisitorIDList = new ArrayList<VisitorID>();
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id1", "hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "DSID_20914", "oldadid",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		mockVisitorIDList.add(new VisitorID("d_cid_ic", "id3", null,
//											VisitorID.AuthenticationState.AUTHENTICATED));
//
//		identityModule.customerIds = mockVisitorIDList;
//
//		List<VisitorID> newCustomerIds = new ArrayList<VisitorID>();
//		newCustomerIds.add(new VisitorID("d_cid_ic", "id3", null,
//										 VisitorID.AuthenticationState.LOGGED_OUT));
//		newCustomerIds.add(new VisitorID("d_cid_ic", "id4", "h-h-h-hodor",
//										 VisitorID.AuthenticationState.LOGGED_OUT));
//		newCustomerIds.add(new VisitorID("d_cid_ic", "DSID_20914", "newadid",
//										 VisitorID.AuthenticationState.AUTHENTICATED));
//
//		// test
//		List<VisitorID> visitorIDList = identityModule.mergeCustomerIds(newCustomerIds);
//
//		// verify
//		List<VisitorID> testVisitorIDList = new ArrayList<VisitorID>();
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id1", "hodor",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id3", null,
//											VisitorID.AuthenticationState.LOGGED_OUT));
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "id4", "h-h-h-hodor",
//											VisitorID.AuthenticationState.LOGGED_OUT));
//		testVisitorIDList.add(new VisitorID("d_cid_ic", "DSID_20914", "newadid",
//											VisitorID.AuthenticationState.AUTHENTICATED));
//
//		assertThat(visitorIDList, listEquals(testVisitorIDList));
//	}
//
//	// ==============================================================================================================
//	// 	StringBuilder generateVisitorIDURLPayload(final ConfigurationSharedStateIdentity configData)
//	// ==============================================================================================================
//	@Test
//	public void generateVisitorIDURLPayloadShouldIncludeAllIds() throws Exception {
//		String expectedResult =
//			"MCMID%3D83056071767212492011535942034357093219%7CMCAID%3Dtestaid%7CMCORGID%3D29849020983%40adobeOrg&adobe_aa_vid=Taz";
//		String mockMarketingCloudIdentifier = "83056071767212492011535942034357093219";
//		String mockUserIdentifier = "Taz";
//		String mockMarketingCloudOrgId = "29849020983@adobeOrg";
//
//		identityModule.mid = mockMarketingCloudIdentifier;
//
//		ConfigurationSharedStateIdentity configSharedState = generateConfigurationSharedState(mockMarketingCloudOrgId);
//		EventData analyticsSharedState = generateAnalyticsSharedState("testaid", mockUserIdentifier);
//		String generatedVisitorIDURLPayloadString = identityModule.callGenerateVisitorIDURLPayload(configSharedState,
//				analyticsSharedState).toString();
//
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf(IdentityTestConstants.UrlKeys.ADB_VISITOR_PAYLOAD_KEY));
//
//		int generatedTSIndex = generatedVisitorIDURLPayloadString.indexOf("=");
//		generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedTSIndex + 1);
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf("TS"));
//
//		// chop the TS value part because that's generated using timeutil and cant be matched.
//		int generatedMCMIDIndex = generatedVisitorIDURLPayloadString.indexOf("MCMID");
//
//		if (generatedMCMIDIndex != -1) {
//			generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedMCMIDIndex);
//		}
//
//		assertEquals(expectedResult, generatedVisitorIDURLPayloadString);
//	}
//
//	@Test
//	public void generateVisitorIDURLPayloadShouldGenerateWithNullConfigState() throws Exception {
//		String expectedResult =
//			"MCMID%3D83056071767212492011535942034357093219%7CMCAID%3Dtestaid&adobe_aa_vid=Taz";
//		String mockMarketingCloudIdentifier = "83056071767212492011535942034357093219";
//		String mockUserIdentifier = "Taz";
//
//		identityModule.mid = mockMarketingCloudIdentifier;
//
//		EventData analyticsSharedState = generateAnalyticsSharedState("testaid", mockUserIdentifier);
//		String generatedVisitorIDURLPayloadString = identityModule.callGenerateVisitorIDURLPayload(null,
//				analyticsSharedState).toString();
//
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf(IdentityTestConstants.UrlKeys.ADB_VISITOR_PAYLOAD_KEY));
//
//		int generatedTSIndex = generatedVisitorIDURLPayloadString.indexOf("=");
//		generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedTSIndex + 1);
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf("TS"));
//
//		// chop the TS value part because that's generated using timeutil and cant be matched.
//		int generatedMCMIDIndex = generatedVisitorIDURLPayloadString.indexOf("MCMID");
//
//		if (generatedMCMIDIndex != -1) {
//			generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedMCMIDIndex);
//		}
//
//		assertEquals(expectedResult, generatedVisitorIDURLPayloadString);
//	}
//
//	@Test
//	public void generateVisitorIDURLPayloadShouldGenerateWithNullVID() throws Exception {
//		String expectedResult =
//			"MCMID%3D83056071767212492011535942034357093219%7CMCAID%3Dtestaid%7CMCORGID%3D29849020983%40adobeOrg";
//		String mockMarketingCloudIdentifier = "83056071767212492011535942034357093219";
//		String mockMarketingCloudOrgId = "29849020983@adobeOrg";
//
//		identityModule.mid = mockMarketingCloudIdentifier;
//
//		ConfigurationSharedStateIdentity configSharedState = generateConfigurationSharedState(mockMarketingCloudOrgId);
//		EventData analyticsSharedState = generateAnalyticsSharedState("testaid", null);
//		String generatedVisitorIDURLPayloadString = identityModule.callGenerateVisitorIDURLPayload(configSharedState,
//				analyticsSharedState).toString();
//
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf(IdentityTestConstants.UrlKeys.ADB_VISITOR_PAYLOAD_KEY));
//
//		int generatedTSIndex = generatedVisitorIDURLPayloadString.indexOf("=");
//		generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedTSIndex + 1);
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf("TS"));
//
//		// chop the TS value part because that's generated using timeutil and cant be matched.
//		int generatedMCMIDIndex = generatedVisitorIDURLPayloadString.indexOf("MCMID");
//
//		if (generatedMCMIDIndex != -1) {
//			generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedMCMIDIndex);
//		}
//
//		assertEquals(expectedResult, generatedVisitorIDURLPayloadString);
//	}
//
//	@Test
//	public void generateVisitorIDURLPayloadShouldGenerateWithNullAnalyticsSharedState() throws Exception {
//		String expectedResult =
//			"MCMID%3D83056071767212492011535942034357093219%7CMCORGID%3D29849020983%40adobeOrg";
//		String mockMarketingCloudIdentifier = "83056071767212492011535942034357093219";
//		String mockMarketingCloudOrgId = "29849020983@adobeOrg";
//
//		identityModule.mid = mockMarketingCloudIdentifier;
//
//		ConfigurationSharedStateIdentity configSharedState = generateConfigurationSharedState(mockMarketingCloudOrgId);
//		String generatedVisitorIDURLPayloadString = identityModule.callGenerateVisitorIDURLPayload(configSharedState,
//				null).toString();
//
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf(IdentityTestConstants.UrlKeys.ADB_VISITOR_PAYLOAD_KEY));
//
//		int generatedTSIndex = generatedVisitorIDURLPayloadString.indexOf("=");
//		generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedTSIndex + 1);
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf("TS"));
//
//		// chop the TS value part because that's generated using timeutil and cant be matched.
//		int generatedMCMIDIndex = generatedVisitorIDURLPayloadString.indexOf("MCMID");
//
//		if (generatedMCMIDIndex != -1) {
//			generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedMCMIDIndex);
//		}
//
//		assertEquals(expectedResult, generatedVisitorIDURLPayloadString);
//	}
//
//	@Test
//	public void generateVisitorIDURLPayloadShouldGenerateWithNoIdentities() throws Exception {
//		String expectedResult =
//			"MCMID%3D83056071767212492011535942034357093219";
//		String mockMarketingCloudIdentifier = "83056071767212492011535942034357093219";
//
//		identityModule.mid = mockMarketingCloudIdentifier;
//
//		ConfigurationSharedStateIdentity configSharedState = generateConfigurationSharedState("");
//		EventData analyticsSharedState = generateAnalyticsSharedState("", "");
//		String generatedVisitorIDURLPayloadString = identityModule.callGenerateVisitorIDURLPayload(configSharedState,
//				analyticsSharedState).toString();
//
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf(IdentityTestConstants.UrlKeys.ADB_VISITOR_PAYLOAD_KEY));
//
//		int generatedTSIndex = generatedVisitorIDURLPayloadString.indexOf("=");
//		generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedTSIndex + 1);
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf("TS"));
//
//		// chop the TS value part because that's generated using timeutil and cant be matched.
//		int generatedMCMIDIndex = generatedVisitorIDURLPayloadString.indexOf("MCMID");
//
//		if (generatedMCMIDIndex != -1) {
//			generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedMCMIDIndex);
//		}
//
//		assertEquals(expectedResult, generatedVisitorIDURLPayloadString);
//	}
//
//
//	@Test
//	public void generateVisitorIDURLPayloadShouldGenerateWithNullAnalyticsSharedStateAndConfigState() throws Exception {
//		String expectedResult =
//			"MCMID%3D83056071767212492011535942034357093219";
//		String mockMarketingCloudIdentifier = "83056071767212492011535942034357093219";
//
//		identityModule.mid = mockMarketingCloudIdentifier;
//
//		String generatedVisitorIDURLPayloadString = identityModule.callGenerateVisitorIDURLPayload(null, null).toString();
//
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf(IdentityTestConstants.UrlKeys.ADB_VISITOR_PAYLOAD_KEY));
//
//		int generatedTSIndex = generatedVisitorIDURLPayloadString.indexOf("=");
//		generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedTSIndex + 1);
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf("TS"));
//
//		// chop the TS value part because that's generated using timeutil and cant be matched.
//		int generatedMCMIDIndex = generatedVisitorIDURLPayloadString.indexOf("MCMID");
//
//		if (generatedMCMIDIndex != -1) {
//			generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedMCMIDIndex);
//		}
//
//		assertEquals(expectedResult, generatedVisitorIDURLPayloadString);
//	}
//
//	@Test
//	public void generateVisitorIDURLPayloadShouldGenerateWithEmptyVID() throws Exception {
//		String expectedResult =
//			"MCMID%3D83056071767212492011535942034357093219%7CMCAID%3Dtestaid%7CMCORGID%3D29849020983%40adobeOrg";
//		String mockMarketingCloudIdentifier = "83056071767212492011535942034357093219";
//		String mockMarketingCloudOrgId = "29849020983@adobeOrg";
//
//		identityModule.mid = mockMarketingCloudIdentifier;
//
//		ConfigurationSharedStateIdentity configSharedState = generateConfigurationSharedState(mockMarketingCloudOrgId);
//		EventData analyticsSharedState = generateAnalyticsSharedState("testaid", "");
//		String generatedVisitorIDURLPayloadString = identityModule.callGenerateVisitorIDURLPayload(configSharedState,
//				analyticsSharedState).toString();
//
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf(IdentityTestConstants.UrlKeys.ADB_VISITOR_PAYLOAD_KEY));
//
//		int generatedTSIndex = generatedVisitorIDURLPayloadString.indexOf("=");
//		generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedTSIndex + 1);
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf("TS"));
//
//		// chop the TS value part because that's generated using timeutil and cant be matched.
//		int generatedMCMIDIndex = generatedVisitorIDURLPayloadString.indexOf("MCMID");
//
//		if (generatedMCMIDIndex != -1) {
//			generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedMCMIDIndex);
//		}
//
//		assertEquals(expectedResult, generatedVisitorIDURLPayloadString);
//	}
//
//	@Test
//	public void generateVisitorIDURLPayloadShouldGenerateWithEncodedVID() throws Exception {
//		String expectedResult =
//			"MCMID%3D83056071767212492011535942034357093219%7CMCAID%3Dtestaid%7CMCORGID%3D29849020983%40adobeOrg&adobe_aa_vid=%3F%26%23%26%23%26%23%26%23%3F";
//		String mockMarketingCloudIdentifier = "83056071767212492011535942034357093219";
//		String mockMarketingCloudOrgId = "29849020983@adobeOrg";
//		String mockUserIdentifier = "?&#&#&#&#?";
//
//		identityModule.mid = mockMarketingCloudIdentifier;
//
//		ConfigurationSharedStateIdentity configSharedState = generateConfigurationSharedState(mockMarketingCloudOrgId);
//		EventData analyticsSharedState = generateAnalyticsSharedState("testaid", mockUserIdentifier);
//		String generatedVisitorIDURLPayloadString = identityModule.callGenerateVisitorIDURLPayload(configSharedState,
//				analyticsSharedState).toString();
//
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf(IdentityTestConstants.UrlKeys.ADB_VISITOR_PAYLOAD_KEY));
//
//		int generatedTSIndex = generatedVisitorIDURLPayloadString.indexOf("=");
//		generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedTSIndex + 1);
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf("TS"));
//
//		// chop the TS value part because that's generated using timeutil and cant be matched.
//		int generatedMCMIDIndex = generatedVisitorIDURLPayloadString.indexOf("MCMID");
//
//		if (generatedMCMIDIndex != -1) {
//			generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedMCMIDIndex);
//		}
//
//		assertEquals(expectedResult, generatedVisitorIDURLPayloadString);
//	}
//
//	@Test
//	public void generateVisitorIDURLPayloadShouldGenerateWithVIDTimeoutException() throws Exception {
//		String expectedResult =
//			"MCMID%3D83056071767212492011535942034357093219%7CMCAID%3Dtestaid%7CMCORGID%3D29849020983%40adobeOrg&adobe_aa_vid=Taz";
//		String mockMarketingCloudIdentifier = "83056071767212492011535942034357093219";
//		String mockMarketingCloudOrgId = "29849020983@adobeOrg";
//
//		identityModule.mid = mockMarketingCloudIdentifier;
//
//		ConfigurationSharedStateIdentity configSharedState = generateConfigurationSharedState(mockMarketingCloudOrgId);
//		EventData analyticsSharedState = generateAnalyticsSharedState("testaid", "Taz");
//		String generatedVisitorIDURLPayloadString = identityModule.callGenerateVisitorIDURLPayload(configSharedState,
//				analyticsSharedState).toString();
//
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf(IdentityTestConstants.UrlKeys.ADB_VISITOR_PAYLOAD_KEY));
//
//		int generatedTSIndex = generatedVisitorIDURLPayloadString.indexOf("=");
//		generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedTSIndex + 1);
//		assertEquals(0, generatedVisitorIDURLPayloadString.indexOf("TS"));
//
//		// chop the TS value part because that's generated using timeutil and cant be matched.
//		int generatedMCMIDIndex = generatedVisitorIDURLPayloadString.indexOf("MCMID");
//
//		if (generatedMCMIDIndex != -1) {
//			generatedVisitorIDURLPayloadString = generatedVisitorIDURLPayloadString.substring(generatedMCMIDIndex);
//		}
//
//		assertEquals(expectedResult, generatedVisitorIDURLPayloadString);
//	}
//
//	// ==============================================================================================================
//	// 	String appendKVPToVisitorIdString(final String originalString, final String key, final String value)
//	// ==============================================================================================================
//	@Test
//	public void testAppendKVPToVisitorIdStringHappy() throws Exception {
//		final String original = "imhere";
//		final String key = "aKey";
//		final String value = "some&value";
//
//		final String result = identityModule.appendKVPToVisitorIdString(original, key, value);
//		assertEquals("imhere|aKey=some&value", result);
//	}
//
//	@Test
//	public void testAppendKVPToVisitorIdStringNullKey() throws Exception {
//		final String original = "imhere";
//		final String key = null;
//		final String value = "some&value";
//
//		final String result = identityModule.appendKVPToVisitorIdString(original, key, value);
//
//		assertEquals(result, "imhere");
//	}
//
//	@Test
//	public void testAppendKVPToVisitorIdStringEmptyKey() throws Exception {
//		final String original = "imhere";
//		final String key = "";
//		final String value = "some&value";
//
//		final String result = identityModule.appendKVPToVisitorIdString(original, key, value);
//
//		assertEquals(result, "imhere");
//	}
//
//	@Test
//	public void testAppendKVPToVisitorIdStringNullValue() throws Exception {
//		final String original = "imhere";
//		final String key = "aKey";
//		final String value = null;
//
//		final String result = identityModule.appendKVPToVisitorIdString(original, key, value);
//
//
//		assertEquals(result, "imhere");
//	}
//
//	@Test
//	public void testAppendKVPToVisitorIdStringEmptyValue() throws Exception {
//		final String original = "imhere";
//		final String key = "aKey";
//		final String value = "";
//
//		final String result = identityModule.appendKVPToVisitorIdString(original, key, value);
//
//		assertEquals(result, "imhere");
//	}
//
//	@Test
//	public void testAppendKVPToVisitorIdStringNullOriginalString() throws Exception {
//		final String original = null;
//		final String key = "aKey";
//		final String value = "some&value";
//
//		final String result = identityModule.appendKVPToVisitorIdString(original, key, value);
//
//		assertEquals("aKey=some&value", result);
//	}
//
//	@Test
//	public void testAppendKVPToVisitorIdStringEmptyOriginalString() throws Exception {
//		final String original = "";
//		final String key = "aKey";
//		final String value = "some&value";
//
//		final String result = identityModule.appendKVPToVisitorIdString(original, key, value);
//
//		assertEquals("aKey=some&value", result);
//	}
//
//	// ==============================================================================================================
//	// 	List<VisitorID> convertVisitorIdsStringToVisitorIDObjects(final String idString)
//	// ==============================================================================================================
//	@Test
//	public void convertVisitorIdsStringToVisitorIDObjects_ConvertStringToVisitorIDsCorrectly() {
//		String visitorIdString =
//			"d_cid_ic=loginidhash%0197717%010&d_cid_ic=xboxlivehash%011629158955%011&d_cid_ic" +
//			"=psnidhash%011144032295%012&d_cid=pushid%01testPushId%011";
//		List<VisitorID> visitorIDList = new ArrayList<VisitorID>();
//		visitorIDList.add(new VisitorID("d_cid_ic", "loginidhash", "97717",
//										VisitorID.AuthenticationState.UNKNOWN));
//		visitorIDList.add(new VisitorID("d_cid_ic", "xboxlivehash", "1629158955",
//										VisitorID.AuthenticationState.AUTHENTICATED));
//		visitorIDList.add(new VisitorID("d_cid_ic", "psnidhash", "1144032295",
//										VisitorID.AuthenticationState.LOGGED_OUT));
//		visitorIDList.add(new VisitorID("d_cid", "pushid", "testPushId",
//										VisitorID.AuthenticationState.AUTHENTICATED));
//
//		List<VisitorID> visitorIds = identityModule.convertVisitorIdsStringToVisitorIDObjects(visitorIdString);
//
//		assertThat(visitorIds, listEquals(visitorIDList));
//	}
//
//	@Test
//	public void testConvertVisitorIdsStringToVisitorIDObjects_OneVisitorId_Works() {
//		// setup
//		List<VisitorID> visitorIds = new ArrayList<VisitorID>();
//		visitorIds.add(new VisitorID(IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType", "customIdValue",
//									 VisitorID.AuthenticationState.AUTHENTICATED));
//		final String visitorIdsString = stringFromVisitorIdList(visitorIds);
//
//		// test
//		final List<VisitorID> returnedIds = identityModule.convertVisitorIdsStringToVisitorIDObjects(visitorIdsString);
//
//		// verify
//		assertNotNull(returnedIds);
//		assertEquals(1, returnedIds.size());
//		assertEquals(visitorIds, returnedIds);
//	}
//
//	@Test
//	public void testConvertVisitorIdsStringToVisitorIDObjects_TwoVisitorIds_Works() {
//		// setup
//		List<VisitorID> visitorIds = new ArrayList<VisitorID>();
//		visitorIds.add(new VisitorID(IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType", "customIdValue",
//									 VisitorID.AuthenticationState.AUTHENTICATED));
//		visitorIds.add(new VisitorID(IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType2", "customIdValue2",
//									 VisitorID.AuthenticationState.UNKNOWN));
//		final String visitorIdsString = stringFromVisitorIdList(visitorIds);
//
//		// test
//		final List<VisitorID> returnedIds = identityModule.convertVisitorIdsStringToVisitorIDObjects(visitorIdsString);
//
//		// verify
//		assertNotNull(returnedIds);
//		assertEquals(2, returnedIds.size());
//		assertEquals(visitorIds, returnedIds);
//	}
//
//	@Test
//	public void testConvertVisitorIdsStringToVisitorIDObjects_EqualsInValue_Works() {
//		// setup
//		List<VisitorID> visitorIds = new ArrayList<VisitorID>();
//		visitorIds.add(new VisitorID(IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType",
//									 "customIdValue==withEquals", VisitorID.AuthenticationState.AUTHENTICATED));
//		final String visitorIdsString = stringFromVisitorIdList(visitorIds);
//
//		// test
//		final List<VisitorID> returnedIds = identityModule.convertVisitorIdsStringToVisitorIDObjects(visitorIdsString);
//
//		// verify
//		assertNotNull(returnedIds);
//		assertEquals(1, returnedIds.size());
//		assertEquals(visitorIds, returnedIds);
//	}
//
//	@Test
//	public void testConvertVisitorIdsStringToVisitorIDObjects_TwoVisitorIdsOneInvalid_ReturnsOnlyValidOne() {
//		// setup
//		List<VisitorID> visitorIds = new ArrayList<VisitorID>();
//		visitorIds.add(new VisitorID(IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType", "value1",
//									 VisitorID.AuthenticationState.AUTHENTICATED));
//		visitorIds.add(new VisitorID(IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType", "",
//									 VisitorID.AuthenticationState.LOGGED_OUT));
//		final String visitorIdsString = stringFromVisitorIdList(visitorIds);
//
//		// test
//		final List<VisitorID> returnedIds = identityModule.convertVisitorIdsStringToVisitorIDObjects(visitorIdsString);
//
//		// verify
//		assertNotNull(returnedIds);
//		assertEquals(returnedIds.size(), 1);
//		final VisitorID visitorID = returnedIds.get(0);
//		assertEquals(IdentityTestConstants.UrlKeys.VISITOR_ID, visitorID.getIdOrigin());
//		assertEquals("customIdType", visitorID.getIdType());
//		assertEquals("value1", visitorID.getId());
//		assertEquals(VisitorID.AuthenticationState.AUTHENTICATED, visitorID.getAuthenticationState());
//	}
//
//	@Test
//	public void testConvertVisitorIdsStringToVisitorIDObjects__removesDuplicatedIdTypes() {
//		// setup
//		List<VisitorID> visitorIds = new ArrayList<VisitorID>();
//		visitorIds.add(new VisitorID(IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType", "value1",
//									 VisitorID.AuthenticationState.AUTHENTICATED));
//		visitorIds.add(new VisitorID(IdentityTestConstants.UrlKeys.VISITOR_ID, "customIdType", "value2",
//									 VisitorID.AuthenticationState.LOGGED_OUT));
//		final String visitorIdsString = stringFromVisitorIdList(visitorIds);
//
//		// test
//		final List<VisitorID> returnedIds = identityModule.convertVisitorIdsStringToVisitorIDObjects(visitorIdsString);
//
//		// verify
//		assertNotNull(returnedIds);
//		assertEquals(1, returnedIds.size());
//		final VisitorID visitorID = returnedIds.get(0);
//		assertEquals(IdentityTestConstants.UrlKeys.VISITOR_ID, visitorID.getIdOrigin());
//		assertEquals("customIdType", visitorID.getIdType());
//		assertEquals("value2", visitorID.getId());
//		assertEquals(VisitorID.AuthenticationState.LOGGED_OUT, visitorID.getAuthenticationState());
//	}
//
//	// ==============================================================================================================
//	// 	void networkResponseLoaded(final HashMap<String, String> result, final String pairID, final int stateVersion)
//	// ==============================================================================================================
//
//	@Test
//	public void testNetworkResponseLoaded_Happy() {
//		// setup
//		IdentityResponseObject responseObject = new IdentityResponseObject();
//		responseObject.blob = "blobvalue";
//		responseObject.mid = "1234567890";
//		responseObject.ttl = 222222;
//		responseObject.hint = "region";
//
//		identityModule.lastSync = 0;
//		identityModule.mid = responseObject.mid;
//
//		// test
//		identityModule.networkResponseLoaded(responseObject, "pairID", 5);
//
//		// Note, MID is generated from shouldSync if if sync is necessary. This test bypasses that call
//		// and therefor the mid is null in all events, persistent storage, and shared states.
//
//		assertTrue(identityModule.lastSync > 0);
//
//		// verify persistence
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//		assertEquals("blobvalue", dataStore.getString(IdentityTestConstants.DataStoreKeys.BLOB, null));
//		assertEquals(222222, dataStore.getLong(IdentityTestConstants.DataStoreKeys.TTL, 0));
//		assertEquals("region", dataStore.getString(IdentityTestConstants.DataStoreKeys.LOCATION_HINT, null));
//		assertNotEquals(0, dataStore.getLong(IdentityTestConstants.DataStoreKeys.LAST_SYNC, 0));
//
//
//		// verify dispatched event
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertEquals("pairID", mockDispatcherIdentityResponseEvent.dispatchResponseParameterPairID);
//		assertEquals("blobvalue", mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, null));
//		assertEquals("region", mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT, null));
//
//	}
//
//	@Test
//	public void testNetworkResponseLoaded_WithError() {
//		// setup
//		IdentityResponseObject responseObject = new IdentityResponseObject();
//		responseObject.blob = "blobvalue";
//		responseObject.mid = "1234567890";
//		responseObject.ttl = 222222;
//		responseObject.hint = "region";
//		responseObject.error = "this is an error message";
//
//		identityModule.lastSync = 0;
//		identityModule.mid = responseObject.mid;
//
//		// test
//		identityModule.networkResponseLoaded(responseObject, "pairID", 5);
//
//		// Note, MID is generated from shouldSync but also from handleNetworkResponseMap if the IdentityResponseObject
//		// contains an error. This is why the mid is populated for this test case.
//
//		assertTrue(identityModule.lastSync > 0);
//
//		// verify persistence
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//		assertNotNull(dataStore.getString(IdentityTestConstants.DataStoreKeys.MARKETING_CLOUD_ID, null));
//		assertNull(dataStore.getString(IdentityTestConstants.DataStoreKeys.BLOB, null));
//		assertNull(dataStore.getString(IdentityTestConstants.DataStoreKeys.LOCATION_HINT, null));
//		assertNotEquals(0, dataStore.getLong(IdentityTestConstants.DataStoreKeys.LAST_SYNC, 0));
//
//		// verify dispatched event
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertEquals("pairID", mockDispatcherIdentityResponseEvent.dispatchResponseParameterPairID);
//		assertNotNull(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						  IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_MID, null));
//		assertNull(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//					   IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, null));
//		assertNull(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//					   IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT,
//					   null));
//	}
//
//	@Test
//	public void testNetworkResponseLoaded_WhenOptOut() {
//		// setup
//		IdentityResponseObject responseObject = new IdentityResponseObject();
//		responseObject.blob = "blobvalue";
//		responseObject.mid = "1234567890";
//		responseObject.ttl = 222222;
//		responseObject.hint = "region";
//
//		identityModule.lastSync = 0;
//		identityModule.privacyStatus = MobilePrivacyStatus.OPT_OUT;
//
//		// test
//		identityModule.networkResponseLoaded(responseObject, "pairID", 5);
//
//		// Note, MID is generated from shouldSync only if sync is necessary. This test bypasses that call
//		// and therefore the mid is null in all events, persistent storage, and shared states.
//
//		// when privacy is Opt-Out, the network response is not processed and nothing new is stored in persistence,
//		// however the response event is still dispatched
//
//		assertTrue(identityModule.lastSync > 0);
//
//		// verify persistence
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//		assertNull(dataStore.getString(IdentityTestConstants.DataStoreKeys.BLOB, null));
//		assertEquals(0, dataStore.getLong(IdentityTestConstants.DataStoreKeys.TTL, 0));
//		assertNull(dataStore.getString(IdentityTestConstants.DataStoreKeys.LOCATION_HINT, null));
//		assertEquals(0, dataStore.getLong(IdentityTestConstants.DataStoreKeys.LAST_SYNC, 0));
//
//		// verify dispatched event
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertEquals("pairID", mockDispatcherIdentityResponseEvent.dispatchResponseParameterPairID);
//		assertNull(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//					   IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, null));
//		assertNull(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//					   IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT, null));
//	}
//
//	// ------ AMSDK-10230 Test shared state updates based on the server response values
//	@Test
//	public void testNetworkResponseLoaded_WhenChangedBlob_setUpdateSharedStateTrue() {
//		String expectedLocationHint = identityModule.locationHint = "9";
//		String expectedMid = identityModule.mid = "1234567890";
//		identityModule.blob = "beforeBlob";
//		String expectedBlob = "afterBlob";
//		identityModule.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		IdentityResponseObject responseObject = new IdentityResponseObject();
//		responseObject.blob = expectedBlob;
//		responseObject.hint = expectedLocationHint;
//		responseObject.mid = expectedMid;
//
//		// test
//		identityModule.networkResponseLoaded(responseObject, "pairID", 5);
//
//		// verify persistence
//		assertPersistedValues(expectedMid, expectedBlob, expectedLocationHint);
//
//		// verify dispatched event
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertEquals("pairID", mockDispatcherIdentityResponseEvent.dispatchResponseParameterPairID);
//		assertEquals(expectedBlob, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, null));
//		assertEquals(expectedLocationHint, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT, null));
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optBoolean(
//					   IdentityTestConstants.EventDataKeys.Identity.UPDATE_SHARED_STATE, false));
//	}
//
//	@Test
//	public void testNetworkResponseLoaded_WhenChangedLocationHint_setUpdateSharedStateTrue() {
//		String expectedBlob = identityModule.blob = "blob";
//		String expectedMid = identityModule.mid = "1234567890";
//		identityModule.locationHint = "5";
//		String expectedLocationHint = "9";
//		identityModule.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		IdentityResponseObject responseObject = new IdentityResponseObject();
//		responseObject.blob = expectedBlob;
//		responseObject.hint = expectedLocationHint;
//		responseObject.mid = expectedMid;
//
//		// test
//		identityModule.networkResponseLoaded(responseObject, "pairID", 5);
//
//		// verify persistence
//		assertPersistedValues(expectedMid, expectedBlob, expectedLocationHint);
//
//		// verify dispatched event
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertEquals("pairID", mockDispatcherIdentityResponseEvent.dispatchResponseParameterPairID);
//		assertEquals(expectedBlob, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, null));
//		assertEquals(expectedLocationHint, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT, null));
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optBoolean(
//					   IdentityTestConstants.EventDataKeys.Identity.UPDATE_SHARED_STATE, false));
//	}
//
//	@Test
//	public void testNetworkResponseLoaded_WhenChangedLocationHintFromNull_setUpdateSharedStateTrue() {
//		String expectedBlob = identityModule.blob = "blob";
//		String expectedMid = identityModule.mid = "1234567890";
//		identityModule.locationHint = null;
//		String expectedLocationHint = "9";
//		identityModule.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		IdentityResponseObject responseObject = new IdentityResponseObject();
//		responseObject.blob = expectedBlob;
//		responseObject.hint = expectedLocationHint;
//		responseObject.mid = expectedMid;
//
//		// test
//		identityModule.networkResponseLoaded(responseObject, "pairID", 5);
//
//		// verify persistence
//		assertPersistedValues(expectedMid, expectedBlob, expectedLocationHint);
//
//		// verify dispatched event
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertEquals("pairID", mockDispatcherIdentityResponseEvent.dispatchResponseParameterPairID);
//		assertEquals(expectedBlob, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, null));
//		assertEquals(expectedLocationHint, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT, null));
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optBoolean(
//					   IdentityTestConstants.EventDataKeys.Identity.UPDATE_SHARED_STATE, false));
//	}
//
//	@Test
//	public void testNetworkResponseLoaded_WhenChangedBlobFromNull_setUpdateSharedStateTrue() {
//		String expectedLocationHint = identityModule.locationHint = "9";
//		String expectedMid = identityModule.mid = "1234567890";
//		identityModule.blob = null;
//		String expectedBlob = "afterBlob";
//		identityModule.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		IdentityResponseObject responseObject = new IdentityResponseObject();
//		responseObject.blob = expectedBlob;
//		responseObject.hint = expectedLocationHint;
//		responseObject.mid = expectedMid;
//
//		// test
//		identityModule.networkResponseLoaded(responseObject, "pairID", 5);
//
//		// verify persistence
//		assertPersistedValues(expectedMid, expectedBlob, expectedLocationHint);
//
//		// verify dispatched event
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertEquals("pairID", mockDispatcherIdentityResponseEvent.dispatchResponseParameterPairID);
//		assertEquals(expectedBlob, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, null));
//		assertEquals(expectedLocationHint, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT, null));
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optBoolean(
//					   IdentityTestConstants.EventDataKeys.Identity.UPDATE_SHARED_STATE, false));
//	}
//
//	@Test
//	public void testNetworkResponseLoaded_WhenChangedLocationHintToNull_setUpdateSharedStateTrue() {
//		String expectedBlob = identityModule.blob = "blob";
//		String expectedMid = identityModule.mid = "1234567890";
//		identityModule.locationHint = "9";
//		identityModule.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		IdentityResponseObject responseObject = new IdentityResponseObject();
//		responseObject.blob = expectedBlob;
//		responseObject.hint = null;
//		responseObject.mid = expectedMid;
//
//		// test
//		identityModule.networkResponseLoaded(responseObject, "pairID", 5);
//
//		// verify in-memory data
//		assertEquals(expectedBlob, identityModule.blob);
//		assertEquals(expectedMid, identityModule.mid);
//		assertNull(identityModule.locationHint);
//
//		// verify persistence
//		assertPersistedValues(expectedMid, expectedBlob, null);
//
//		// verify dispatched event
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertEquals("pairID", mockDispatcherIdentityResponseEvent.dispatchResponseParameterPairID);
//		assertEquals(expectedBlob, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, null));
//		assertFalse(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.containsKey(
//						IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT));
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optBoolean(
//					   IdentityTestConstants.EventDataKeys.Identity.UPDATE_SHARED_STATE, false));
//	}
//
//	@Test
//	public void testNetworkResponseLoaded_WhenChangedBlobToNull_setUpdateSharedStateTrue() {
//		String expectedLocationHint = identityModule.locationHint = "9";
//		String expectedMid = identityModule.mid = "1234567890";
//		identityModule.blob = "blob";
//		identityModule.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		IdentityResponseObject responseObject = new IdentityResponseObject();
//		responseObject.blob = null;
//		responseObject.hint = expectedLocationHint;
//		responseObject.mid = expectedMid;
//
//		// test
//		identityModule.networkResponseLoaded(responseObject, "pairID", 5);
//
//		// verify in-memory data
//		assertNull(identityModule.blob);
//		assertEquals(expectedMid, identityModule.mid);
//		assertEquals(expectedLocationHint, identityModule.locationHint);
//
//		// verify persistence
//		assertPersistedValues(expectedMid, null, expectedLocationHint);
//
//		// verify dispatched event
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertEquals("pairID", mockDispatcherIdentityResponseEvent.dispatchResponseParameterPairID);
//		assertFalse(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.containsKey(
//						IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB));
//		assertEquals(expectedLocationHint, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT, null));
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optBoolean(
//					   IdentityTestConstants.EventDataKeys.Identity.UPDATE_SHARED_STATE, false));
//	}
//
//	@Test
//	public void testNetworkResponseLoaded_WhenErrorAndGeneratedNewMID_setUpdateSharedStateTrue() {
//		String expectedBlob = identityModule.blob = "blob";
//		String expectedLocationHint = identityModule.locationHint = "9";
//		identityModule.mid = null;
//		identityModule.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		IdentityResponseObject responseObject = new IdentityResponseObject();
//		responseObject.error = "some error";
//
//		// test
//		identityModule.networkResponseLoaded(responseObject, "pairID", 5);
//
//		// verify persistence
//		assertNotNull(identityModule.mid);
//		assertPersistedValues(identityModule.mid, expectedBlob, expectedLocationHint);
//
//		// verify dispatched event
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertEquals("pairID", mockDispatcherIdentityResponseEvent.dispatchResponseParameterPairID);
//		assertEquals(expectedBlob, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, null));
//		assertEquals(expectedLocationHint, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT, null));
//		assertNotNull(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						  IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_MID, null));
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optBoolean(
//					   IdentityTestConstants.EventDataKeys.Identity.UPDATE_SHARED_STATE, false));
//	}
//
//	@Test
//	public void testNetworkResponseLoaded_WhenErrorAndNotGeneratedNewMID_doesNotSetUpdateSharedState() {
//		String expectedBlob = identityModule.blob = "blob";
//		String expectedLocationHint = identityModule.locationHint = "9";
//		String expectedMid = identityModule.mid = "123456";
//		identityModule.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		IdentityResponseObject responseObject = new IdentityResponseObject();
//		responseObject.error = "some error";
//
//		// test
//		identityModule.networkResponseLoaded(responseObject, "pairID", 5);
//
//		// verify persistence
//		assertPersistedValues(expectedMid, expectedBlob, expectedLocationHint);
//
//		// verify dispatched event
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertEquals("pairID", mockDispatcherIdentityResponseEvent.dispatchResponseParameterPairID);
//		assertEquals(expectedBlob, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, null));
//		assertEquals(expectedLocationHint, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT, null));
//		assertEquals(expectedMid, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_MID, null));
//		assertFalse(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.containsKey(
//						IdentityTestConstants.EventDataKeys.Identity.UPDATE_SHARED_STATE));
//	}
//
//	@Test
//	public void testNetworkResponseLoaded_WhenUnchangedBlobLocationHint_doesNotSetUpdateSharedState() {
//		String expectedBlob = identityModule.blob = "blob";
//		String expectedLocationHint = identityModule.locationHint = "9";
//		String expectedMid = identityModule.mid = "123456";
//		identityModule.privacyStatus = MobilePrivacyStatus.OPT_IN;
//		IdentityResponseObject responseObject = new IdentityResponseObject();
//		responseObject.blob = expectedBlob;
//		responseObject.hint = expectedLocationHint;
//		responseObject.mid = expectedMid;
//
//		// test
//		identityModule.networkResponseLoaded(responseObject, "pairID", 5);
//
//		// verify persistence
//		assertPersistedValues(expectedMid, expectedBlob, expectedLocationHint);
//
//		// verify dispatched event
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertEquals("pairID", mockDispatcherIdentityResponseEvent.dispatchResponseParameterPairID);
//		assertEquals(expectedBlob, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, null));
//		assertEquals(expectedLocationHint, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT, null));
//		assertEquals(expectedMid, mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_MID, null));
//		assertFalse(mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.containsKey(
//						IdentityTestConstants.EventDataKeys.Identity.UPDATE_SHARED_STATE));
//	}
//
//	// This situation can usually happen if a network response is handled at the same time as the resetIdentities request.
//	@Test
//	public void testNetworkResponseLoaded_MismatchECID() {
//		// setup
//		IdentityResponseObject responseObject = new IdentityResponseObject();
//		responseObject.blob = "blobvalue";
//		responseObject.mid = "1234567890";
//		responseObject.ttl = 222222;
//		responseObject.hint = "region";
//
//		identityModule.lastSync = 0;
//		identityModule.mid = "123"; // different MID than what is in the response
//
//		// test
//		identityModule.networkResponseLoaded(responseObject, "pairID", 5);
//
//		// Note, MID is generated from shouldSync if if sync is necessary. This test bypasses that call
//		// and therefor the mid is null in all events, persistent storage, and shared states.
//
//		assertTrue(identityModule.lastSync > 0);
//
//		// verify persistence
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//		assertNotEquals("blobvalue", dataStore.getString(IdentityTestConstants.DataStoreKeys.BLOB, null));
//		assertNotEquals(222222, dataStore.getLong(IdentityTestConstants.DataStoreKeys.TTL, 0));
//		assertNotEquals("region", dataStore.getString(IdentityTestConstants.DataStoreKeys.LOCATION_HINT, null));
//		assertNotEquals(0, dataStore.getLong(IdentityTestConstants.DataStoreKeys.LAST_SYNC, 0));
//
//
//		// verify dispatched event
//		assertTrue(mockDispatcherIdentityResponseEvent.dispatchResponseWasCalled);
//		assertEquals("pairID", mockDispatcherIdentityResponseEvent.dispatchResponseParameterPairID);
//		assertNotEquals("blobvalue", mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//							IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, null));
//		assertNotEquals("region", mockDispatcherIdentityResponseEvent.dispatchResponseParameterEventData.optString(
//							IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT, null));
//
//	}
//
//	@Test
//	public void handleIdentityResponseIdentityForSharedState_WhenUpdateSharedState_CreatesNewSharedState() throws
//		Exception {
//		String expectedBlob = identityModule.blob = "blob";
//		String expectedLocationHint = identityModule.locationHint = "9";
//		String expectedMid = identityModule.mid = "123456";
//
//		// test
//		EventData updateSharedStateData = new EventData();
//		updateSharedStateData.putBoolean(IdentityTestConstants.EventDataKeys.Identity.UPDATE_SHARED_STATE, true);
//		Event event = new Event.Builder("Test", EventType.IDENTITY,
//										EventSource.RESPONSE_IDENTITY).setData(updateSharedStateData).build();
//		identityModule.handleIdentityResponseIdentityForSharedState(event);
//
//		// verify
//		assertTrue(eventHub.createSharedStateCalled);
//		EventData data = eventHub.createSharedStateParamState;
//		assertEquals(expectedBlob, data.getString2(IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_BLOB));
//		assertEquals(expectedLocationHint, data.getString2(
//						 IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT));
//		assertEquals(expectedMid, data.getString2(IdentityTestConstants.EventDataKeys.Identity.VISITOR_ID_MID));
//	}
//
//	@Test
//	public void extractIdentifiers_Should_ReturnEmptyMap_WhenIDENTIFIERSKeyPresentInEventData_WithEmptyValue() {
//		EventData eventData = new EventData();
//		eventData.putStringMap(IdentityTestConstants.EventDataKeys.Identity.IDENTIFIERS, new HashMap<String, String>());
//		HashMap<String, String> ids = (HashMap<String, String>) identityModule.extractIdentifiers(eventData);
//		assertNotNull(ids);
//		assertEquals(ids.size(), 0);
//	}
//
//	@Test
//	public void generateInternalIdString_ShouldNotAppendCharInFront() {
//		HashMap<String, String> dpids = new HashMap<String, String>();
//		dpids.put("key1", "val1");
//		dpids.put("key2", "val2");
//
//		String expected_string = "d_cid=key1%01val1&d_cid=key2%01val2";
//
//		String generatedString = identityModule.generateInternalIdString(dpids);
//		assertEquals(expected_string, generatedString);
//	}
//
//	@Test
//	public void generateInternalIdString_WhenDpIdsIsEmpty() {
//		HashMap<String, String> dpids = new HashMap<String, String>();
//
//		String expected_string = "";
//
//		String generatedString = identityModule.generateInternalIdString(dpids);
//		assertEquals(expected_string, generatedString);
//	}
//
//	@Test
//	public void testProcessAudienceResponse_When_optedouthitsentWasTrue() {
//		//setup
//		EventData data = new EventData();
//		data.putBoolean("optedouthitsent", true);
//		Event audienceEvent = new Event.Builder("Audience Opt out event", EventType.AUDIENCEMANAGER,
//												EventSource.RESPONSE_CONTENT)
//		.setData(data).build();
//
//		//setup config shared state
//		EventData configurationSharedState = new EventData();
//		configurationSharedState.putString(IdentityTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
//										   MobilePrivacyStatus.OPT_OUT.getValue());
//		eventHub.createSharedState(IdentityTestConstants.EventDataKeys.Configuration.MODULE_NAME, 0, configurationSharedState);
//
//		//test
//		identityModule.processAudienceResponse(audienceEvent);
//		//verify
//		assertFalse(identityModule.sendOptOutHitWasCalled);
//
//	}
//
//	@Test
//	public void testProcessAudienceResponse_When_optedouthitsentWasFalse() {
//		//setup
//		EventData data = new EventData();
//		data.putBoolean("optedouthitsent", false);
//		Event audienceEvent = new Event.Builder("Audience Opt out event", EventType.AUDIENCEMANAGER,
//												EventSource.RESPONSE_CONTENT)
//		.setData(data).build();
//
//		//setup config shared state
//		EventData configurationSharedState = new EventData();
//		configurationSharedState.putString(IdentityTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
//										   MobilePrivacyStatus.OPT_OUT.getValue());
//		eventHub.createSharedState(IdentityTestConstants.EventDataKeys.Configuration.MODULE_NAME, 0, configurationSharedState);
//
//		//test
//		identityModule.processAudienceResponse(audienceEvent);
//		//verify
//		assertTrue(identityModule.sendOptOutHitWasCalled);
//
//	}
//
//	@Test
//	public void testProcessAudienceResponse_When_optedouthitsentWasFalse_but_privacy_optin() {
//		//setup
//		EventData data = new EventData();
//		data.putBoolean("optedouthitsent", false);
//		Event audienceEvent = new Event.Builder("Audience Opt out event", EventType.AUDIENCEMANAGER,
//												EventSource.RESPONSE_CONTENT)
//		.setData(data).build();
//
//		//setup config shared state
//		EventData configurationSharedState = new EventData();
//		configurationSharedState.putString(IdentityTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
//										   MobilePrivacyStatus.OPT_IN.getValue());
//		eventHub.createSharedState(IdentityTestConstants.EventDataKeys.Configuration.MODULE_NAME, 0, configurationSharedState);
//
//		//test
//		identityModule.processAudienceResponse(audienceEvent);
//		//verify
//		assertFalse(identityModule.sendOptOutHitWasCalled);
//
//	}
//
//	@Test
//	public void testProcessAudienceResponse_When_optedouthitsentWasFalse_but_privacy_optunknown() {
//		//setup
//		EventData data = new EventData();
//		data.putBoolean("optedouthitsent", false);
//		Event audienceEvent = new Event.Builder("Audience Opt out event", EventType.AUDIENCEMANAGER,
//												EventSource.RESPONSE_CONTENT)
//		.setData(data).build();
//
//		//setup config shared state
//		EventData configurationSharedState = new EventData();
//		configurationSharedState.putString(IdentityTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
//										   MobilePrivacyStatus.UNKNOWN.getValue());
//		eventHub.createSharedState(IdentityTestConstants.EventDataKeys.Configuration.MODULE_NAME, 0, configurationSharedState);
//
//		//test
//		identityModule.processAudienceResponse(audienceEvent);
//		//verify
//		assertFalse(identityModule.sendOptOutHitWasCalled);
//
//	}
//
//	@Test
//	public void testProcessAudienceResponse_When_optedouthitsentWasFalse_but_config_sharedstate_pending() {
//		//setup
//		EventData data = new EventData();
//		data.putBoolean("optedouthitsent", false);
//		Event audienceEvent = new Event.Builder("Audience Opt out event", EventType.AUDIENCEMANAGER,
//												EventSource.RESPONSE_CONTENT)
//		.setData(data).build();
//
//		//setup config shared state
//		eventHub.createSharedState(IdentityTestConstants.EventDataKeys.Configuration.MODULE_NAME, 0,
//								   EventHub.SHARED_STATE_PENDING);
//
//		//test
//		identityModule.processAudienceResponse(audienceEvent);
//		//verify
//		assertFalse(identityModule.sendOptOutHitWasCalled);
//
//	}
//
//	@Test
//	public void testhandleConfiguration_when_privacy_optout() {
//		//setup
//		EventData configuration = new EventData();
//		configuration.putString(IdentityTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
//								MobilePrivacyStatus.OPT_OUT.getValue());
//		Event configurationEvent = new Event.Builder("Configuration event", EventType.AUDIENCEMANAGER,
//				EventSource.RESPONSE_CONTENT)
//		.setData(configuration).build();
//
//		//setup config shared state
//		eventHub.createSharedState(IdentityTestConstants.EventDataKeys.Configuration.MODULE_NAME, 0, configuration);
//
//		//test
//		identityModule.handleConfiguration(configurationEvent);
//		//verify
//		assertTrue(identityModule.handleOptOutWasCalled);
//		assertTrue(identityModule.updateLatestValidConfigurationWasCalled);
//		assertTrue(identityModule.processPrivacyChangeWasCalled);
//
//	}
//
//	@Test
//	public void testhandleConfiguration_when_privacy_optin() {
//		//setup
//		EventData configuration = new EventData();
//		configuration.putString(IdentityTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
//								MobilePrivacyStatus.OPT_IN.getValue());
//		Event configurationEvent = new Event.Builder("Configuration event", EventType.AUDIENCEMANAGER,
//				EventSource.RESPONSE_CONTENT)
//		.setData(configuration).build();
//
//		//setup config shared state
//		eventHub.createSharedState(IdentityTestConstants.EventDataKeys.Configuration.MODULE_NAME, 0, configuration);
//
//		//test
//		identityModule.handleConfiguration(configurationEvent);
//		//verify
//		assertFalse(identityModule.handleOptOutWasCalled);
//		assertTrue(identityModule.updateLatestValidConfigurationWasCalled);
//		assertTrue(identityModule.processPrivacyChangeWasCalled);
//	}
//
//	@Test
//	public void testhandleConfiguration_when_privacy_optunknown() {
//		//setup
//		EventData configuration = new EventData();
//		configuration.putString(IdentityTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
//								MobilePrivacyStatus.UNKNOWN.getValue());
//		Event configurationEvent = new Event.Builder("Configuration event", EventType.AUDIENCEMANAGER,
//				EventSource.RESPONSE_CONTENT)
//		.setData(configuration).build();
//
//		//setup config shared state
//		eventHub.createSharedState(IdentityTestConstants.EventDataKeys.Configuration.MODULE_NAME, 0, configuration);
//
//		//test
//		identityModule.handleConfiguration(configurationEvent);
//		//verify
//		assertFalse(identityModule.handleOptOutWasCalled);
//		assertTrue(identityModule.updateLatestValidConfigurationWasCalled);
//		assertTrue(identityModule.processPrivacyChangeWasCalled);
//	}
//
//
//
//	@Test
//	public void testHandleOptOut_when_configOptOut_and_AAMNotSetup() {
//		//setup
//		EventData configuration = new EventData();
//		configuration.putString(IdentityTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
//								MobilePrivacyStatus.OPT_OUT.getValue());
//		Event configurationEvent = new Event.Builder("Configuration event", EventType.AUDIENCEMANAGER,
//				EventSource.RESPONSE_CONTENT)
//		.setData(configuration).build();
//
//		//setup config shared state
//		eventHub.createSharedState(IdentityTestConstants.EventDataKeys.Configuration.MODULE_NAME, 0, configuration);
//
//		//test
//		identityModule.handleOptOut(configurationEvent);
//		//verify
//
//		assertTrue(identityModule.sendOptOutHitWasCalled);
//
//	}
//
//	@Test
//	public void testHandleOptOut_when_configOptOut_and_AAMSetup() {
//		//setup
//		EventData configuration = new EventData();
//		configuration.putString(IdentityTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
//								MobilePrivacyStatus.OPT_OUT.getValue());
//		configuration.putString(IdentityTestConstants.EventDataKeys.Configuration.AAM_CONFIG_SERVER, "server");
//		Event configurationEvent = new Event.Builder("Configuration event", EventType.AUDIENCEMANAGER,
//				EventSource.RESPONSE_CONTENT)
//		.setData(configuration).build();
//
//		//setup config shared state
//		eventHub.createSharedState(IdentityTestConstants.EventDataKeys.Configuration.MODULE_NAME, 0, configuration);
//
//		//test
//		identityModule.handleOptOut(configurationEvent);
//		//verify
//
//		assertFalse(identityModule.sendOptOutHitWasCalled);
//
//	}
//
//	@Test
//	public void testHandleOptOut_when_configSharedStateOptIn_and_AAMNotSetup() {
//		//setup
//		EventData configuration = new EventData();
//		configuration.putString(IdentityTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
//								MobilePrivacyStatus.OPT_OUT.getValue());
//		Event configurationEvent = new Event.Builder("Configuration event", EventType.AUDIENCEMANAGER,
//				EventSource.RESPONSE_CONTENT)
//		.setData(configuration).build();
//
//		//setup config shared state
//		EventData configurationSharedState = new EventData();
//		configuration.putString(IdentityTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
//								MobilePrivacyStatus.OPT_IN.getValue());
//		eventHub.createSharedState(IdentityTestConstants.EventDataKeys.Configuration.MODULE_NAME, 0, configurationSharedState);
//
//		//test
//		identityModule.handleOptOut(configurationEvent);
//		//verify
//
//		assertFalse(identityModule.sendOptOutHitWasCalled);
//
//	}
//
//	// ==============================================================================================================
//	// 	void handleAnalyticsResponseIdentity()
//	// ==============================================================================================================
//
//	@Test
//	public void testHandleAnalyticsResponseIdentity() {
//		EventData data = new EventData();
//		data.putString(IdentityTestConstants.EventDataKeys.Analytics.ANALYTICS_ID, "aid");
//		Event event = new Event.Builder("Identity Test", EventType.ANALYTICS, EventSource.RESPONSE_IDENTITY)
//		.setData(data)
//		.build();
//
//		identityModule.handleAnalyticsResponseIdentity(event);
//
//		assertTrue(identityModule.tryProcessingEventQueueWasCalled);
//		assertEquals(1, identityModule.eventsQueue.size());
//
//		Event actualEvent = identityModule.eventsQueue.peek();
//		assertNotNull(actualEvent);
//		EventData actualData = actualEvent.getData();
//		assertNotNull(actualData);
//		Map<String, String> ids = actualData.optStringMap(IdentityTestConstants.EventDataKeys.Identity.IDENTIFIERS, null);
//		assertNotNull(ids);
//		assertEquals("aid", ids.get(IdentityTestConstants.EventDataKeys.Identity.ANALYTICS_ID));
//
//		assertTrue(identityModule.internalDataStore.contains(IdentityTestConstants.DataStoreKeys.AID_SYNCED_KEY));
//	}
//
//	@Test
//	public void testHandleAnalyticsResponseIdentity_Fails_AidEmpty() {
//		EventData data = new EventData();
//		data.putString(IdentityTestConstants.EventDataKeys.Analytics.ANALYTICS_ID, "");
//		Event event = new Event.Builder("Identity Test", EventType.ANALYTICS, EventSource.RESPONSE_IDENTITY)
//		.setData(data)
//		.build();
//
//		identityModule.handleAnalyticsResponseIdentity(event);
//
//		assertFalse(identityModule.tryProcessingEventQueueWasCalled);
//		assertEquals(0, identityModule.eventsQueue.size());
//
//		assertFalse(identityModule.internalDataStore.contains(IdentityTestConstants.DataStoreKeys.AID_SYNCED_KEY));
//	}
//
//	@Test
//	public void testHandleAnalyticsResponseIdentity_Fails_NoAid() {
//		EventData data = new EventData();
//		data.putString("key", "aid");
//		Event event = new Event.Builder("Identity Test", EventType.ANALYTICS, EventSource.RESPONSE_IDENTITY)
//		.setData(data)
//		.build();
//
//		identityModule.handleAnalyticsResponseIdentity(event);
//
//		assertFalse(identityModule.tryProcessingEventQueueWasCalled);
//		assertEquals(0, identityModule.eventsQueue.size());
//
//		assertFalse(identityModule.internalDataStore.contains(IdentityTestConstants.DataStoreKeys.AID_SYNCED_KEY));
//	}
//
//	@Test
//	public void testHandleAnalyticsResponseIdentity_Fails_NoEventData() {
//		EventData data = new EventData();
//		Event event = new Event.Builder("Identity Test", EventType.ANALYTICS, EventSource.RESPONSE_IDENTITY)
//		.build();
//
//		identityModule.handleAnalyticsResponseIdentity(event);
//
//		assertFalse(identityModule.tryProcessingEventQueueWasCalled);
//		assertEquals(0, identityModule.eventsQueue.size());
//
//		assertFalse(identityModule.internalDataStore.contains(IdentityTestConstants.DataStoreKeys.AID_SYNCED_KEY));
//	}
//
//	@Test
//	public void testHandleAnalyticsResponseIdentity_Fails_NoEvent() {
//
//		identityModule.handleAnalyticsResponseIdentity(null);
//
//		assertFalse(identityModule.tryProcessingEventQueueWasCalled);
//		assertEquals(0, identityModule.eventsQueue.size());
//
//		assertFalse(identityModule.internalDataStore.contains(IdentityTestConstants.DataStoreKeys.AID_SYNCED_KEY));
//	}
//
//	@Test
//	public void testHandleAnalyticsResponseIdentity_Fails_DatastoreContainsKey() {
//		identityModule.internalDataStore.setBoolean(IdentityTestConstants.DataStoreKeys.AID_SYNCED_KEY, true);
//
//		EventData data = new EventData();
//		data.putString(IdentityTestConstants.EventDataKeys.Analytics.ANALYTICS_ID, "aid");
//		Event event = new Event.Builder("Identity Test", EventType.ANALYTICS, EventSource.RESPONSE_IDENTITY)
//		.setData(data)
//		.build();
//
//		identityModule.handleAnalyticsResponseIdentity(event);
//
//		assertFalse(identityModule.tryProcessingEventQueueWasCalled);
//		assertEquals(0, identityModule.eventsQueue.size());
//
//		assertTrue(identityModule.internalDataStore.contains(IdentityTestConstants.DataStoreKeys.AID_SYNCED_KEY));
//	}
//
//
//	// ==============================================================================================================
//	// 	void processPrivacyChange()
//	// ==============================================================================================================
//
//	@Test
//	public void processPrivacyChange_With_Null_EventData() {
//		populateIdentifiers(identityModule);
//
//		identityModule.privacyStatus = MobilePrivacyStatus.UNKNOWN;
//		identityModule.processPrivacyChange(5, null);
//		assertEquals(MobilePrivacyStatus.UNKNOWN, identityModule.privacyStatus);
//
//		assertFalse(mockIdentityHitsDatabase.updatePrivacyStatusWasCalled);
//		assertFalse(identityModule.hasSharedEventState(IdentityTestConstants.EventDataKeys.Identity.MODULE_NAME));
//		assertFalse(StringUtils.isNullOrEmpty(identityModule.mid));
//	}
//
//	@Test
//	public void processPrivacyChange_With_NoPrivacyConfig_EventData() {
//		populateIdentifiers(identityModule);
//
//		EventData data = new EventData();
//
//		identityModule.privacyStatus = MobilePrivacyStatus.UNKNOWN;
//		identityModule.processPrivacyChange(5, data);
//		assertEquals(MobilePrivacyStatus.UNKNOWN, identityModule.privacyStatus);
//
//		assertFalse(mockIdentityHitsDatabase.updatePrivacyStatusWasCalled);
//		assertFalse(identityModule.hasSharedEventState(IdentityTestConstants.EventDataKeys.Identity.MODULE_NAME));
//		assertFalse(StringUtils.isNullOrEmpty(identityModule.mid));
//	}
//
//	@Test
//	public void processPrivacyChange_To_Opt_In() {
//		populateIdentifiers(identityModule);
//
//		EventData data = new EventData();
//		data.putString(IdentityTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
//					   MobilePrivacyStatus.OPT_IN.getValue());
//
//		identityModule.privacyStatus = MobilePrivacyStatus.UNKNOWN;
//		identityModule.processPrivacyChange(5, data);
//		assertEquals(MobilePrivacyStatus.OPT_IN, identityModule.privacyStatus);
//
//		assertTrue(mockIdentityHitsDatabase.updatePrivacyStatusWasCalled);
//		assertFalse(identityModule.hasSharedEventState(IdentityTestConstants.EventDataKeys.Identity.MODULE_NAME));
//		assertFalse(StringUtils.isNullOrEmpty(identityModule.mid));
//	}
//
//	@Test
//	public void processPrivacyChange_To_Opt_Out() {
//		populateIdentifiers(identityModule);
//
//		identityModule.setDataStore(null);
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore fakeDataStore = new FakeDataStore();
//		fakeDataStore.setBoolean(IdentityTestConstants.DataStoreKeys.AID_SYNCED_KEY, true);
//		fakeLocalStorageService.mapping.put(IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME,
//											fakeDataStore);
//
//		EventData data = new EventData();
//		data.putString(IdentityTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
//					   MobilePrivacyStatus.OPT_OUT.getValue());
//
//		identityModule.privacyStatus = MobilePrivacyStatus.UNKNOWN;
//		identityModule.processPrivacyChange(5, data);
//		assertEquals(MobilePrivacyStatus.OPT_OUT, identityModule.privacyStatus);
//
//		assertTrue(mockIdentityHitsDatabase.updatePrivacyStatusWasCalled);
//		assertTrue(identityModule.hasSharedEventState(IdentityTestConstants.EventDataKeys.Identity.MODULE_NAME));
//
//		EventData state = identityModule.getSharedEventState(IdentityTestConstants.EventDataKeys.Identity.MODULE_NAME,
//						  Event.SHARED_STATE_OLDEST);
//		assertNotNull(state);
//		assertEquals(1, state.size());
//		assertEquals(0, state.optLong(IdentityTestConstants.EventDataKeys.Identity.VISITOR_IDS_LAST_SYNC, 5));
//
//
//		assertNull(identityModule.mid);
//		assertNull(identityModule.advertisingIdentifier);
//		assertNull(identityModule.pushIdentifier);
//		assertNull(identityModule.blob);
//		assertNull(identityModule.locationHint);
//		assertNull(identityModule.customerIds);
//
//		assertFalse(fakeDataStore.contains(IdentityTestConstants.DataStoreKeys.AID_SYNCED_KEY));
//	}
//
//	// ==============================================================================================================
//	// 	void clearEventQueue()
//	// ==============================================================================================================
//
//	@Test
//	public void clearEventsQueue() {
//		Event event1 = new Event.Builder("IdentityTest", EventType.IDENTITY, EventSource.REQUEST_IDENTITY).build();
//		event1.getData().putBoolean(IdentityTestConstants.EventDataKeys.Identity.IS_SYNC_EVENT, true);
//
//		Event event2 = new Event.Builder("IdentityTest", EventType.IDENTITY, EventSource.REQUEST_IDENTITY).build();
//		event2.getData().putString(IdentityTestConstants.EventDataKeys.Identity.BASE_URL, "url");
//
//		Event event3 = new Event.Builder("IdentityTest", EventType.IDENTITY, EventSource.REQUEST_IDENTITY).build();
//
//		// populate queue with events
//		identityModule.eventsQueue.add(event1);
//		identityModule.eventsQueue.add(event2);
//		identityModule.eventsQueue.add(event3);
//
//		identityModule.privacyStatus = MobilePrivacyStatus.OPT_IN;
//
//		// trigger call to clearEventQueue by changing privacy status to Opt-Out
//		EventData data = new EventData();
//		data.putString(IdentityTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
//					   MobilePrivacyStatus.OPT_OUT.getValue());
//		identityModule.processPrivacyChange(5, data);
//
//		// verify queue is cleared except for AppendUrl request
//		assertEquals(1, identityModule.eventsQueue.size());
//		assertEquals(event2, identityModule.eventsQueue.peek());
//	}
//
//	// ===========================
//	// cleanupVisitorIdentifiers
//	// ===========================
//	@Test
//	public void cleanupVisitorIdentifiers_MixedIds_OnlyEmptyIdsAreRemoved() {
//		// setup
//		List<VisitorID> identifiers = new ArrayList<VisitorID>();
//		identifiers.add(new VisitorID("id_origin1", "id_type1", "id1", VisitorID.AuthenticationState.AUTHENTICATED));
//		identifiers.add(new VisitorID("id_origin2", "id_type2", "", VisitorID.AuthenticationState.AUTHENTICATED));
//		identifiers.add(new VisitorID("id_origin3", "id_type3", "", VisitorID.AuthenticationState.UNKNOWN));
//		identifiers.add(new VisitorID("id_origin4", "id_type4", "id4", VisitorID.AuthenticationState.LOGGED_OUT));
//
//		// test
//		List<VisitorID> result = identityModule.cleanupVisitorIdentifiers(identifiers);
//
//		// verify
//		List<VisitorID> expectedIdentifiers = new ArrayList<VisitorID>();
//		expectedIdentifiers.add(new VisitorID("id_origin1", "id_type1", "id1", VisitorID.AuthenticationState.AUTHENTICATED));
//		expectedIdentifiers.add(new VisitorID("id_origin4", "id_type4", "id4", VisitorID.AuthenticationState.LOGGED_OUT));
//		assertEquals(2, result.size());
//		assertThat(expectedIdentifiers, listEquals(result));
//		assertEquals(4, identifiers.size()); // should not be altered
//	}
//
//	@Test
//	public void cleanupVisitorIdentifiers_MixedIds_OnlyNullIdsAreRemoved() {
//		// setup
//		List<VisitorID> identifiers = new ArrayList<VisitorID>();
//		identifiers.add(new VisitorID("id_origin1", "id_type1", "id1", VisitorID.AuthenticationState.AUTHENTICATED));
//		identifiers.add(new VisitorID("id_origin2", "id_type2", null, VisitorID.AuthenticationState.AUTHENTICATED));
//		identifiers.add(new VisitorID("id_origin3", "id_type3", null, VisitorID.AuthenticationState.UNKNOWN));
//		identifiers.add(new VisitorID("id_origin4", "id_type4", "id4", VisitorID.AuthenticationState.LOGGED_OUT));
//
//		// test
//		List<VisitorID> result = identityModule.cleanupVisitorIdentifiers(identifiers);
//
//		// verify
//		List<VisitorID> expectedIdentifiers = new ArrayList<VisitorID>();
//		expectedIdentifiers.add(new VisitorID("id_origin1", "id_type1", "id1", VisitorID.AuthenticationState.AUTHENTICATED));
//		expectedIdentifiers.add(new VisitorID("id_origin4", "id_type4", "id4", VisitorID.AuthenticationState.LOGGED_OUT));
//		assertEquals(2, result.size());
//		assertThat(expectedIdentifiers, listEquals(result));
//		assertEquals(4, identifiers.size()); // should not be altered
//	}
//
//	@Test
//	public void cleanupVisitorIdentifiers_AllValidVisitorIds_NoIdsRemoved() {
//		// setup
//		List<VisitorID> identifiers = new ArrayList<VisitorID>();
//		identifiers.add(new VisitorID("id_origin1", "id_type1", "id1", VisitorID.AuthenticationState.AUTHENTICATED));
//		identifiers.add(new VisitorID("id_origin2", "id_type2", "id2", VisitorID.AuthenticationState.AUTHENTICATED));
//		identifiers.add(new VisitorID("id_origin3", "id_type3", "id3", VisitorID.AuthenticationState.UNKNOWN));
//		identifiers.add(new VisitorID("id_origin4", "id_type4", "id4", VisitorID.AuthenticationState.LOGGED_OUT));
//
//		// test
//		List<VisitorID> result = identityModule.cleanupVisitorIdentifiers(identifiers);
//
//		// verify
//		assertEquals(4, result.size());
//		assertThat(identifiers, listEquals(result));
//	}
//
//	@Test
//	public void cleanupVisitorIdentifiers_AllInvalid_AllIdsRemoved() {
//		// setup
//		List<VisitorID> identifiers = new ArrayList<VisitorID>();
//		identifiers.add(new VisitorID("id_origin1", "id_type1", "", VisitorID.AuthenticationState.AUTHENTICATED));
//		identifiers.add(new VisitorID("id_origin2", "id_type2", null, VisitorID.AuthenticationState.AUTHENTICATED));
//		identifiers.add(new VisitorID("id_origin3", "id_type3", "", VisitorID.AuthenticationState.UNKNOWN));
//		identifiers.add(new VisitorID("id_origin4", "id_type4", null, VisitorID.AuthenticationState.LOGGED_OUT));
//
//		// test
//		List<VisitorID> result = identityModule.cleanupVisitorIdentifiers(identifiers);
//
//		// verify
//		assertTrue(result.isEmpty());
//	}
//
//	@Test
//	public void cleanupVisitorIdentifiers_EmptyVisitorIdsList_ReturnsEmptyList() {
//		// test
//		List<VisitorID> result = identityModule.cleanupVisitorIdentifiers(new ArrayList<VisitorID>());
//
//		// verify
//		assertTrue(result.isEmpty());
//	}
//
//	@Test
//	public void cleanupVisitorIdentifiers_NullVisitorIdsList_ReturnsNull() {
//		// test
//		List<VisitorID> result = identityModule.cleanupVisitorIdentifiers(null);
//
//		// verify
//		assertNull(result);
//	}
//
//	// ==============================================================================================================
//	// 	boolean shouldWaitForPendingSharedState()
//	// ==============================================================================================================
//
//	@Test
//	public void shouldWaitForPendingSharedState_returnsFalse_when_extensionRegistered_stateNotPending() {
//		String[] registeredExtensions = {IdentityTestConstants.EventDataKeys.Analytics.MODULE_NAME};
//		setEventHubSharedStateWithExtensions(registeredExtensions);
//
//		EventData analyticsSharedState = generateAnalyticsSharedState("aid", "vid");
//
//		Event event = new Event.Builder("Test", EventType.IDENTITY, EventSource.REQUEST_IDENTITY).build();
//		boolean shouldWait = identityModule.callShouldWaitForPendingSharedState(event,
//							 IdentityTestConstants.EventDataKeys.Analytics.MODULE_NAME,
//							 analyticsSharedState);
//
//		// As Analytics shared state is not pending, should not wait
//		assertFalse(shouldWait);
//	}
//
//	@Test
//	public void shouldWaitForPendingSharedState_returnsFalse_when_extensionNotRegistered_stateNotPending() {
//		String[] registeredExtensions = {IdentityTestConstants.EventDataKeys.Configuration.MODULE_NAME};
//		setEventHubSharedStateWithExtensions(registeredExtensions);
//
//		EventData analyticsSharedState = generateAnalyticsSharedState("aid", "vid");
//
//		Event event = new Event.Builder("Test", EventType.IDENTITY, EventSource.REQUEST_IDENTITY).build();
//		boolean shouldWait = identityModule.callShouldWaitForPendingSharedState(event,
//							 IdentityTestConstants.EventDataKeys.Analytics.MODULE_NAME,
//							 analyticsSharedState);
//
//		// As Analytics shared state is not pending, should not wait when Analytics extension is not registered
//		assertFalse(shouldWait);
//	}
//
//	@Test
//	public void shouldWaitForPendingSharedState_returnsFalse_when_extensionMapMalformed_statePending() {
//		Map<String, Variant> moduleData = new HashMap<>();
//		moduleData.put(IdentityTestConstants.EventDataKeys.EventHub.VERSION, StringVariant.from("1.0.0"));
//
//		Map<String, Variant> extensionsMap = new HashMap<>();
//		extensionsMap.put(IdentityTestConstants.EventDataKeys.Analytics.MODULE_NAME, MapVariant.from(moduleData));
//
//		EventData hubSharedState = new EventData();
//		hubSharedState.putVariantMap("malformed", extensionsMap);
//
//		this.eventHub.createSharedState(IdentityTestConstants.EventDataKeys.EventHub.MODULE_NAME, 0, hubSharedState);
//
//		EventData analyticsSharedState = EventHub.SHARED_STATE_PENDING;
//
//		Event event = new Event.Builder("Test", EventType.IDENTITY, EventSource.REQUEST_IDENTITY).build();
//		boolean shouldWait = identityModule.callShouldWaitForPendingSharedState(event,
//							 IdentityTestConstants.EventDataKeys.Analytics.MODULE_NAME,
//							 analyticsSharedState);
//
//		// Event Hub state is set but cannot read registered extensions, expect false (not wait)
//		assertFalse(shouldWait);
//	}
//
//	@Test
//	public void shouldWaitForPendingSharedState_returnsTrue_when_extensionRegistered_statePending() {
//		String[] registeredExtensions = {IdentityTestConstants.EventDataKeys.Analytics.MODULE_NAME};
//		setEventHubSharedStateWithExtensions(registeredExtensions);
//
//		EventData analyticsSharedState = EventHub.SHARED_STATE_PENDING;
//
//		Event event = new Event.Builder("Test", EventType.IDENTITY, EventSource.REQUEST_IDENTITY).build();
//		boolean shouldWait = identityModule.callShouldWaitForPendingSharedState(event,
//							 IdentityTestConstants.EventDataKeys.Analytics.MODULE_NAME,
//							 analyticsSharedState);
//
//		// Analytics registered and state is PENDING, expect true (wait)
//		assertTrue(shouldWait);
//	}
//
//	@Test
//	public void shouldWaitForPendingSharedState_returnsTrue_when_eventHubPending_statePending() {
//		this.eventHub.createSharedState(IdentityTestConstants.EventDataKeys.EventHub.MODULE_NAME, 0,
//										EventHub.SHARED_STATE_PENDING);
//
//		EventData analyticsSharedState = EventHub.SHARED_STATE_PENDING;
//
//		Event event = new Event.Builder("Test", EventType.IDENTITY, EventSource.REQUEST_IDENTITY).build();
//		boolean shouldWait = identityModule.callShouldWaitForPendingSharedState(event,
//							 IdentityTestConstants.EventDataKeys.Analytics.MODULE_NAME,
//							 analyticsSharedState);
//
//		// EventHub and Analytics states are both PENDING, expect true (wait)
//		assertTrue(shouldWait);
//	}
//
//	@Test
//	public void shouldWaitForPendingSharedState_returnsTrue_when_eventHubNull_stateNull() {
//		this.eventHub.createSharedState(IdentityTestConstants.EventDataKeys.EventHub.MODULE_NAME, 0, null);
//
//		EventData analyticsSharedState = null;
//
//		Event event = new Event.Builder("Test", EventType.IDENTITY, EventSource.REQUEST_IDENTITY).build();
//		boolean shouldWait = identityModule.callShouldWaitForPendingSharedState(event,
//							 IdentityTestConstants.EventDataKeys.Analytics.MODULE_NAME,
//							 analyticsSharedState);
//
//		// EventHub and Analytics states are both PENDING, expect true (wait)
//		// Note SHARED_STATE_PENDING = null
//		assertTrue(shouldWait);
//	}
//
//
//	// ==============================================================================================================
//	// 	Test Helpers
//	// ==============================================================================================================
//
//	private static void populateIdentifiers(final IdentityExtension module) {
//		module.mid = "goodnight moon";
//		module.advertisingIdentifier = "goodnight mittens";
//		module.pushIdentifier = "goodnight kittens";
//		module.blob = "goodnight mush";
//		module.locationHint = "goodnight old lady saying 'hush'";
//
//		module.customerIds = new ArrayList<VisitorID>();
//		module.customerIds.add(new VisitorID("d_cid_ic", "loginidhash", "97717",
//											 VisitorID.AuthenticationState.UNKNOWN));
//	}
//
//	public File getResource(final String resourceName) {
//		File resourceFile = null;
//		URL resource = this.getClass().getClassLoader().getResource(resourceName);
//
//		if (resource != null) {
//			resourceFile = new File(resource.getFile());
//		}
//
//		return resourceFile;
//	}
//
//	public Event FakeSycnIDEvent() {
//		HashMap<String, String> testMap = new HashMap<String, String>();
//		testMap.put("k1", "v1");
//		testMap.put("k2", "v2");
//
//		EventData eventData = new EventData();
//		eventData.putStringMap(IdentityTestConstants.EventDataKeys.Identity.IDENTIFIERS, testMap);
//		eventData.putBoolean(IdentityTestConstants.EventDataKeys.Identity.IS_SYNC_EVENT, true);
//		Event event = new Event.Builder("test-identifiers-event", EventType.IDENTITY,
//										EventSource.REQUEST_IDENTITY).setData(eventData).build();
//		event.setEventNumber(5);
//		return event;
//	}
//
//	public Event FakePushIDEvent() {
//		EventData eventData = new EventData();
//		eventData.putString(IdentityTestConstants.EventDataKeys.Identity.PUSH_IDENTIFIER, "test-push-id");
//		Event event = new Event.Builder("test-pushID-event", EventType.GENERIC_IDENTITY,
//										EventSource.REQUEST_IDENTITY).setData(eventData).build();
//		event.setEventNumber(5);
//		return event;
//	}
//
//	public Event FakeAdIDEvent() {
//		EventData eventData = new EventData();
//		eventData.putString(IdentityTestConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER, "testAdid");
//		Event event = new Event.Builder("test-adidID-event", EventType.GENERIC_IDENTITY,
//										EventSource.REQUEST_CONTENT).setData(eventData).build();
//		event.setEventNumber(5);
//		return event;
//	}
//
//	private Matcher<List<VisitorID>> listEquals(final List<VisitorID> testIds) {
//		return new BaseMatcher<List<VisitorID>>() {
//			@Override
//			public boolean matches(Object o) {
//				final List<VisitorID> actualIds = (List<VisitorID>) o;
//
//				if (testIds.size() != actualIds.size()) {
//					return false;
//				}
//
//				for (VisitorID testId : actualIds) {
//					for (VisitorID expectedId : testIds) {
//						if (visitorIDsMatch(expectedId, testId)) {
//							break;
//						} else {
//							if (testIds.indexOf(expectedId) == testIds.size() - 1) {
//								return false;
//							}
//						}
//					}
//				}
//
//				return true;
//			}
//
//			private boolean visitorIDsMatch(VisitorID expected, VisitorID test) {
//				if (expected == null || test == null) {
//					return false;
//				}
//
//				if (!(expected.getIdType() == null ? test.getIdType() == null : expected.getIdType().equals(test.getIdType()))) {
//					return false;
//				}
//
//				if (!(expected.getIdOrigin() == null ? test.getIdOrigin() == null : expected.getIdOrigin().equals(
//							test.getIdOrigin()))) {
//					return false;
//				}
//
//				if (!(expected.getId() == null ? test.getId() == null : expected.getId().equals(test.getId()))) {
//					return false;
//				}
//
//				return expected.getAuthenticationState() == test.getAuthenticationState();
//			}
//
//			@Override
//			public void describeTo(Description description) {
//				final List<String> values = new ArrayList<String>();
//
//				for (VisitorID testId : testIds) {
//					StringBuilder valueBuilder = new StringBuilder(LIST_BEGIN);
//					valueBuilder.append(testId.getIdOrigin());
//					valueBuilder.append(ITEM_BREAK);
//					valueBuilder.append(ITEM_BREAK);
//					valueBuilder.append(testId.getIdType());
//					valueBuilder.append(ITEM_BREAK);
//					valueBuilder.append(testId.getId());
//					valueBuilder.append(ITEM_BREAK);
//					valueBuilder.append(testId.getAuthenticationState());
//					valueBuilder.append(LIST_END);
//					values.add(valueBuilder.toString());
//				}
//
//				description.appendText("List should contain the same values as: ");
//				description.appendValueList("\n       ", "\n       ", "", values);
//			}
//
//			@SuppressWarnings("unchecked")
//			public void describeMismatch(final Object item, final
//										 Description description) {
//				final List<VisitorID> actualIds = (List<VisitorID>) item;
//				final List<String> values = new ArrayList<String>();
//
//				for (VisitorID testId : actualIds) {
//					StringBuilder valueBuilder = new StringBuilder(LIST_BEGIN);
//					valueBuilder.append(testId.getIdOrigin());
//					valueBuilder.append(ITEM_BREAK);
//					valueBuilder.append(testId.getIdType());
//					valueBuilder.append(ITEM_BREAK);
//					valueBuilder.append(testId.getId());
//					valueBuilder.append(ITEM_BREAK);
//					valueBuilder.append(testId.getAuthenticationState());
//					valueBuilder.append(LIST_END);
//					values.add(valueBuilder.toString());
//				}
//
//				description.appendValueList("received:\n       ", "\n       ", "", values);
//			}
//		};
//	}
//
//	private Matcher<String> unorderedStringParameterMatches(final String testString, final String delimiter) {
//		return new BaseMatcher<String>() {
//			@Override
//			public boolean matches(Object o) {
//				final String expectedString = (String) o;
//
//				if (expectedString.length() != testString.length()) {
//					return false;
//				}
//
//				List<String> expectedParameterList = Arrays.asList(expectedString.split(delimiter));
//				List<String> testParameterList = Arrays.asList(testString.split(delimiter));
//				Collections.sort(testParameterList);
//				Collections.sort(expectedParameterList);
//
//				return expectedParameterList.equals(testParameterList);
//			}
//
//			@Override
//			public void describeTo(Description description) {
//				description.appendText("String should contain the same parameters as: ");
//				description.appendText(testString);
//				description.appendText(" with delimiter: ");
//				description.appendText(delimiter);
//			}
//
//			public void describeMismatch(final Object item, final
//										 Description description) {
//				description.appendText("recieved: ");
//				description.appendText((String) item);
//			}
//		};
//	}
//
//	private EventData generateAnalyticsSharedState(final String aid, final String vid) {
//		EventData eventData = new EventData();
//
//		if (aid != null) {
//			eventData.putString(IdentityTestConstants.EventDataKeys.Analytics.ANALYTICS_ID, aid);
//		}
//
//		if (vid != null) {
//			eventData.putString(IdentityTestConstants.EventDataKeys.Analytics.VISITOR_IDENTIFIER, vid);
//		}
//
//		return eventData;
//
//	}
//
//	private void setAnalyticsSharedStateToEventHub(final int version, final EventData analyticsSharedState) {
//		this.eventHub.createSharedState(IdentityTestConstants.EventDataKeys.Analytics.MODULE_NAME, version,
//										analyticsSharedState);
//	}
//
//	private void setEventHubSharedStateWithExtensions(final String[] extensions) {
//		Map<String, Variant> moduleData = new HashMap<>();
//		moduleData.put(IdentityTestConstants.EventDataKeys.EventHub.VERSION, StringVariant.from("1.0.0"));
//
//		Map<String, Variant> extensionsMap = new HashMap<>();
//
//		for (String extensionName : extensions) {
//			extensionsMap.put(extensionName, MapVariant.from(moduleData));
//		}
//
//		EventData hubSharedState = new EventData();
//		hubSharedState.putVariantMap(IdentityTestConstants.EventDataKeys.EventHub.EXTENSIONS, extensionsMap);
//
//		this.eventHub.createSharedState(IdentityTestConstants.EventDataKeys.EventHub.MODULE_NAME, 0, hubSharedState);
//	}
//
//	private ConfigurationSharedStateIdentity generateConfigurationSharedState(final String orgId) {
//		ConfigurationSharedStateIdentity configSharedState = new ConfigurationSharedStateIdentity();
//		configSharedState.orgID = orgId;
//		return configSharedState;
//	}
//
//	private Event generateAdidEvent(final String adidValue) {
//		EventData eventData = new EventData();
//		eventData.putString(IdentityTestConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER, adidValue);
//		return new Event.Builder("Test ADID Event", EventType.IDENTITY, EventSource.REQUEST_CONTENT).setData(eventData).build();
//	}
//
//	private String stringFromVisitorIdList(final List<VisitorID> visitorIDs) {
//		if (visitorIDs == null) {
//			return "";
//		}
//
//		final StringBuilder customerIdString = new StringBuilder();
//
//		for (final VisitorID visitorID : visitorIDs) {
//			customerIdString.append("&");
//			customerIdString.append(IdentityConstants.UrlKeys.VISITOR_ID);
//			customerIdString.append("=");
//			customerIdString.append(visitorID.getIdType());
//			customerIdString.append(IdentityTestConstants.Defaults.CID_DELIMITER);
//
//			if (visitorID.getId() != null) {
//				customerIdString.append(visitorID.getId());
//			}
//
//			customerIdString.append(IdentityTestConstants.Defaults.CID_DELIMITER);
//			customerIdString.append(visitorID.getAuthenticationState().getValue());
//		}
//
//		return customerIdString.toString();
//	}
//
//	private void assertPersistedValues(final String expectedMid, final String expectedBlob,
//									   final String expectedLocationHint) {
//		FakeLocalStorageService fakeLocalStorageService = platformServices.fakeLocalStorageService;
//		FakeDataStore dataStore = (FakeDataStore) fakeLocalStorageService.getDataStore(
//									  IdentityTestConstants.DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
//		assertEquals(expectedBlob, dataStore.getString(IdentityTestConstants.DataStoreKeys.BLOB, null));
//		assertEquals(expectedLocationHint, dataStore.getString(IdentityTestConstants.DataStoreKeys.LOCATION_HINT, null));
//		assertEquals(expectedMid, dataStore.getString(IdentityTestConstants.DataStoreKeys.MARKETING_CLOUD_ID, null));
//	}
//}
