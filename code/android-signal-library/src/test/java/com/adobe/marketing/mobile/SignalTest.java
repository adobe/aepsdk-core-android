///* ***********************************************************************
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2018 Adobe Systems Incorporated
// * All Rights Reserved.
// *
// * NOTICE:  All information contained herein is, and remains
// * the property of Adobe Systems Incorporated and its suppliers,
// * if any.  The intellectual and technical concepts contained
// * herein are proprietary to Adobe Systems Incorporated and its
// * suppliers and are protected by trade secret or copyright law.
// * Dissemination of this information or reproduction of this material
// * is strictly forbidden unless prior written permission is obtained
// * from Adobe Systems Incorporated.
// **************************************************************************/
//
//package com.adobe.marketing.mobile;
//
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//
//import java.util.HashMap;
//
//import static org.junit.Assert.*;
//
//public class SignalTest extends BaseTest {
//
//	private static final String CONFIGURATION_STATE_NAME = "com.adobe.module.configuration";
//	SignalExtension signalModule;
//	FakePlatformServices fakePlatformServices;
//	MockEventHubUnitTest eventHub;
//	MockSignalHitsDatabase mockSignalHitsDatabase;
//
//
//	@Before()
//	public void beforeEach() {
//		fakePlatformServices = new FakePlatformServices();
//		eventHub = new MockEventHubUnitTest("Test", fakePlatformServices);
//		mockSignalHitsDatabase = new MockSignalHitsDatabase(fakePlatformServices);
//		signalModule = new SignalExtension(eventHub, fakePlatformServices, mockSignalHitsDatabase);
//	}
//
//	@Test
//	public void testDefaultConstructor() {
//		SignalExtension module = new SignalExtension(eventHub, fakePlatformServices);
//		assertNotNull(module);
//	}
//
//	private Event createSignalEvent() {
//		HashMap<String, Variant> triggeredConsequence = new HashMap<String, Variant>();
//		HashMap<String, Variant> consequenceDetail = new HashMap<String, Variant>();
//
//		consequenceDetail.put("templateurl", Variant.fromString("http://my-url"));
//
//		triggeredConsequence.put("id", Variant.fromString("my-id"));
//		triggeredConsequence.put("type", Variant.fromString("pb"));
//		triggeredConsequence.put("detail", Variant.fromVariantMap(consequenceDetail));
//
//		EventData data = new EventData();
//		data.putVariantMap("triggeredconsequence", triggeredConsequence);
//
//		return new Event.Builder("Test", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).setData(data).build();
//	}
//
//	private Event createPIIEvent() {
//		HashMap<String, Variant> triggeredConsequence = new HashMap<String, Variant>();
//		HashMap<String, Variant> consequenceDetail = new HashMap<String, Variant>();
//
//		consequenceDetail.put("templateurl", Variant.fromString("https://my-url"));
//
//		triggeredConsequence.put("id", Variant.fromString("my-id"));
//		triggeredConsequence.put("type", Variant.fromString("pii"));
//		triggeredConsequence.put("detail", Variant.fromVariantMap(consequenceDetail));
//
//		EventData data = new EventData();
//		data.putVariantMap("triggeredconsequence", triggeredConsequence);
//
//		return new Event.Builder("Test", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).setData(data).build();
//	}
//
//	private Event createOpenUrlEvent(final String url) {
//		HashMap<String, Variant> triggeredConsequence = new HashMap<String, Variant>();
//		HashMap<String, Variant> consequenceDetail = new HashMap<String, Variant>();
//
//		consequenceDetail.put("url", Variant.fromString(url));
//
//		triggeredConsequence.put("id", Variant.fromString("my-id"));
//		triggeredConsequence.put("type", Variant.fromString("url"));
//		triggeredConsequence.put("detail", Variant.fromVariantMap(consequenceDetail));
//
//		EventData data = new EventData();
//		data.putVariantMap("triggeredconsequence", triggeredConsequence);
//
//		return new Event.Builder("Test", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).setData(data).build();
//	}
//
//	@Test
//	public void processEventReturnsNo_When_ConfigurationStateIsNull() {
//		eventHub.setSharedState(CONFIGURATION_STATE_NAME, null);
//		boolean isProcessed = signalModule.processSignalEvent(createSignalEvent());
//		assertEquals(false, isProcessed);
//	}
//
//	@Test
//	public void processEventReturnsYes_When_ConfigurationStateAreValid_EventDataContainsSignalId() throws Exception {
//		eventHub.setSharedState(CONFIGURATION_STATE_NAME, new EventData().putString("key", "value"));
//		Event signalEvent = createSignalEvent();
//		boolean isProcessed = signalModule.processSignalEvent(signalEvent);
//		assertEquals(true, isProcessed);
//		assertTrue(mockSignalHitsDatabase.queueWasCalled);
//
//		SignalHit hit = mockSignalHitsDatabase.queueParametersSignalHit;
//		assertEquals("http://my-url", hit.url);
//		assertEquals(signalEvent.getTimestamp(), mockSignalHitsDatabase.queueParametersTimestamp);
//	}
//
//	@Test
//	public void canProcessEvent_When_ConfigurationAndIdentityStateAreValid_EventDataContainsPiiId() throws Exception {
//		eventHub.setSharedState(CONFIGURATION_STATE_NAME, new EventData().putString("key", "value"));
//		boolean isProcessed = signalModule.processSignalEvent(createPIIEvent());
//		assertEquals(true, isProcessed);
//		assertTrue(mockSignalHitsDatabase.queueWasCalled);
//
//		SignalHit hit = mockSignalHitsDatabase.queueParametersSignalHit;
//		assertEquals("https://my-url", hit.url);
//	}
//
//	@Test
//	public void holdTheEvents_UntilConfigurationSharedStateIsReady() throws Exception {
//		eventHub.setSharedState(CONFIGURATION_STATE_NAME, null);
//		signalModule.handleSignalConsequenceEvent(createSignalEvent());
//
//		waitForExecutor(signalModule.getExecutor());
//		assertEquals(false, mockSignalHitsDatabase.queueWasCalled);
//
//		eventHub.setSharedState(CONFIGURATION_STATE_NAME, new EventData());
//		signalModule.tryProcessQueuedEvent();
//
//		waitForExecutor(signalModule.getExecutor());
//		assertEquals(true, mockSignalHitsDatabase.queueWasCalled);
//	}
//
//	@Test
//	public void callLogicMethodUpdatePrivacyStatus_When_UpdatePrivacyStatus() throws Exception {
//		signalModule.updatePrivacyStatus(MobilePrivacyStatus.OPT_IN);
//		waitForExecutor(signalModule.getExecutor());
//		mockSignalHitsDatabase.queueParametersMobilePrivacyStatus = MobilePrivacyStatus.OPT_IN;
//	}
//
//
//	//ToDo (module test??)
//	@Ignore
//	@Test
//	public void clearTheEventsInQueue_When_SetPrivacyToOptOut() throws Exception {
//
//		eventHub.setSharedState(CONFIGURATION_STATE_NAME, null);
//		signalModule.handleSignalConsequenceEvent(createSignalEvent());
//		signalModule.handleSignalConsequenceEvent(createSignalEvent());
//
//		waitForExecutor(signalModule.getExecutor());
//		signalModule.updatePrivacyStatus(MobilePrivacyStatus.OPT_OUT);
//
//		eventHub.setSharedState(CONFIGURATION_STATE_NAME, new EventData());
//		signalModule.handleSignalConsequenceEvent(createSignalEvent());
//
//		waitForExecutor(signalModule.getExecutor());
//		//assertEquals(1, mockSignalLogic.queueSignalRequestCalledTimes);
//	}
//
//	@Test
//	public void testOnReceiveOpenUrlEvent_When_Happy_Then_CallOpenURL() throws Exception {
//
//		// execute
//		// setup
//		final Event openUrlEvent = createOpenUrlEvent("mytest://deeplink");
//
//		signalModule.handleOpenURLConsequenceEvent(openUrlEvent);
//		waitForExecutor(signalModule.getExecutor());
//
//		// verify
//		final MockUIService uiService = fakePlatformServices.getMockUIService();
//		assertTrue(uiService.showUrlWasCalled);
//		assertEquals("mytest://deeplink", uiService.showUrlUrl);
//	}
//
//	@Test
//	public void testOnReceiveOpenUrlEvent_When_EmptyUrls_Then_DoNothing() throws Exception {
//		// setup
//		final Event openUrlEvent = createOpenUrlEvent("");
//
//		// execute
//		signalModule.handleOpenURLConsequenceEvent(openUrlEvent);
//		waitForExecutor(signalModule.getExecutor());
//
//		// verify
//		final MockUIService uiService = fakePlatformServices.getMockUIService();
//		assertFalse(uiService.showUrlWasCalled);
//	}
//
//	@Test
//	public void testOnReceiveOpenUrlEvent_When_NullUrls_Then_DoNothing() throws Exception {
//		// setup
//		final Event openUrlEvent = createOpenUrlEvent(null);
//
//		// execute
//		signalModule.handleOpenURLConsequenceEvent(openUrlEvent);
//		waitForExecutor(signalModule.getExecutor());
//
//		// verify
//		final MockUIService uiService = fakePlatformServices.getMockUIService();
//		assertFalse(uiService.showUrlWasCalled);
//	}
//
//	@Test
//	public void testOnReceiveOpenUrlEvent_When_NullUiService_Then_DontCrash() throws Exception {
//		// setup
//		final Event openUrlEvent = createOpenUrlEvent("mytest://deeplink");
//
//		fakePlatformServices.mockUIService = null;
//
//		// execute
//		signalModule.handleOpenURLConsequenceEvent(openUrlEvent);
//		waitForExecutor(signalModule.getExecutor());
//
//		// verify
//		// nothing to verify cause there's no ui service to validate against, just make sure we don't crash
//	}
//}
