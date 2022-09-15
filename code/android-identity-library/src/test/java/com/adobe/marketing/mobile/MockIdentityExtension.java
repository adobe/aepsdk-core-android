
/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2017 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 ******************************************************************************/
package com.adobe.marketing.mobile;

import java.util.List;
import java.util.Map;

public class MockIdentityExtension extends IdentityExtension {
	boolean tryProcessingEventQueueWasCalled;
	boolean convertVisitorIdsStringToVisitorIDObjectsWasCalled;
	boolean handleSyncIdentifiersWasCalled;
	boolean handleAppendURLWasCalled;
	boolean handleIdentifersRequestWasCalled;
	boolean appendVisitorInfoForURLWasCalled;
	boolean networkResponseLoadedCalled;
	boolean bootupWasCalled;
	boolean updateLatestValidConfigurationWasCalled;
	StringBuilder visitorIDURLPayload;
	IdentityResponseObject networkResponseLoadedResult;
	String networkResponseLoadedEventPairID;
	int networkResponseLoadedEventStateVersion;

	MockIdentityExtension(EventHub hub, PlatformServices platformServices) {
		this(hub, platformServices, null, null);

	}

	MockIdentityExtension(
		final EventHub hub,
		final PlatformServices platformServices,
		final DispatcherIdentityResponseIdentityIdentity idResponseDispatcher,
		final DispatcherAnalyticsRequestContentIdentity idAnalyticsDispatcher) {

		super(hub, platformServices);

		// Dispatcher responsible for dispatching RESPONSE_IDENTITY events going out of the identity module.
		idResponseEventDispatcher = idResponseDispatcher;

		// Dispatcher responsible for dispatching ANALYTICS_ACTION events going out of the identity module.
		idAnalyticsEventDispatcher = idAnalyticsDispatcher;

		tryProcessingEventQueueWasCalled = false;
		convertVisitorIdsStringToVisitorIDObjectsWasCalled = false;
		handleSyncIdentifiersWasCalled = false;
		handleIdentifersRequestWasCalled = false;
		handleAppendURLWasCalled = false;
		appendVisitorInfoForURLWasCalled = false;
		networkResponseLoadedCalled = false;
		networkResponseLoadedResult = null;
		networkResponseLoadedEventPairID = null;
		networkResponseLoadedEventStateVersion = -1;
		bootupWasCalled = false;
		visitorIDURLPayload = new StringBuilder();
		updateLatestValidConfigurationWasCalled = false;
		super.database = new MockIdentityHitsDatabase(this, platformServices);
	}

	@Override
	protected void processEventQueue() {
		tryProcessingEventQueueWasCalled = true;
		super.processEventQueue();
	}

	@Override
	void updateLatestValidConfiguration(final EventData data) {
		updateLatestValidConfigurationWasCalled = true;
		super.updateLatestValidConfiguration(data);
	}


	boolean handleOptOutWasCalled = false;
	@Override
	void handleOptOut(Event event) {
		handleOptOutWasCalled = true;
		super.handleOptOut(event);
	}

	boolean processPrivacyChangeWasCalled = false;
	@Override
	void processPrivacyChange(final int version, final EventData data) {
		processPrivacyChangeWasCalled = true;
		super.processPrivacyChange(version, data);
	}

	boolean handleAnalyticsResponseIdentityWasCalled = false;
	@Override
	void handleAnalyticsResponseIdentity(final Event event) {
		handleAnalyticsResponseIdentityWasCalled = true;
		super.handleAnalyticsResponseIdentity(event);
	}

	@Override
	List<VisitorID> convertVisitorIdsStringToVisitorIDObjects(final String idString) {
		convertVisitorIdsStringToVisitorIDObjectsWasCalled = true;
		return super.convertVisitorIdsStringToVisitorIDObjects(idString);
	}

	@Override
	boolean handleSyncIdentifiers(final Event event, final ConfigurationSharedStateIdentity configSharedState) {
		handleSyncIdentifiersWasCalled = true;
		return super.handleSyncIdentifiers(event, configSharedState);
	}

	@Override
	void handleAppendURL(final Event event, final ConfigurationSharedStateIdentity configSharedState,
						 final EventData analyticsSharedState) {
		handleAppendURLWasCalled = true;
		super.handleAppendURL(event, configSharedState, analyticsSharedState);
	}

	boolean handleGetUrlVariablesWasCalled = false;
	@Override
	void handleGetUrlVariables(final Event event, final ConfigurationSharedStateIdentity configSharedState,
							   final EventData analyticsSharedState) {
		handleGetUrlVariablesWasCalled = true;
		super.handleGetUrlVariables(event, configSharedState, analyticsSharedState);
	}

	String appendVisitorInfoForURLParamBaseURL = "";
	String appendVisitorInfoForURLParamPairID = "";
	ConfigurationSharedStateIdentity appendVisitorInfoForURLParamConfigSharedState = null;
	EventData appendVisitorInfoForURLParamAnalyticsSharedState = null;
	@Override
	void appendVisitorInfoForURL(final String baseURL, final String pairID,
								 final ConfigurationSharedStateIdentity configSharedState, final EventData analyticsSharedState) {
		appendVisitorInfoForURLWasCalled = true;
		appendVisitorInfoForURLParamBaseURL = baseURL;
		appendVisitorInfoForURLParamPairID = pairID;
		appendVisitorInfoForURLParamConfigSharedState = configSharedState;
		appendVisitorInfoForURLParamAnalyticsSharedState = analyticsSharedState;
		super.appendVisitorInfoForURL(baseURL, pairID, configSharedState, analyticsSharedState);
	}

	@Override
	void networkResponseLoaded(final IdentityResponseObject result, final String pairID, final int stateVersion) {

		networkResponseLoadedCalled = true;
		networkResponseLoadedResult = result;
		networkResponseLoadedEventPairID = pairID;
		networkResponseLoadedEventStateVersion = stateVersion;
		super.networkResponseLoaded(result, pairID, stateVersion);
	}

	boolean sendOptOutHitWasCalled = false;
	ConfigurationSharedStateIdentity sendOptOutHitParameterConfigSharedState;
	@Override
	protected void sendOptOutHit(ConfigurationSharedStateIdentity configSharedState) {
		sendOptOutHitWasCalled = true;
		sendOptOutHitParameterConfigSharedState = configSharedState;
		super.sendOptOutHit(configSharedState);
	}

	void setGenerateVisitorIDURLPayload(StringBuilder payload) {
		visitorIDURLPayload = payload;
	}

	boolean generateVisitorIDURLPayloadWasCalled = false;
	ConfigurationSharedStateIdentity generateVisitorIDURLPayloadParamConfigSharedState = null;
	EventData generateVisitorIDURLPayloadParamAnalyticsSharedState = null;
	@Override
	StringBuilder generateVisitorIDURLPayload(final ConfigurationSharedStateIdentity configSharedState,
			final EventData analyticsSharedState) {
		generateVisitorIDURLPayloadWasCalled = true;
		generateVisitorIDURLPayloadParamConfigSharedState = configSharedState;
		generateVisitorIDURLPayloadParamAnalyticsSharedState = analyticsSharedState;

		return visitorIDURLPayload;
	}

	boolean handleIdentityResponseIdentityForSharedStateWasCalled;
	Event handleIdentityResponseIdentityForSharedStateParamEvent;
	@Override
	void handleIdentityResponseIdentityForSharedState(final Event event) {
		handleIdentityResponseIdentityForSharedStateWasCalled = true;
		handleIdentityResponseIdentityForSharedStateParamEvent = event;
		super.handleIdentityResponseIdentityForSharedState(event);
	}

	boolean handleIdentityRequestResetWasCalled;
	Event handleIdentityRequestResetParamEvent;
	@Override
	void handleIdentityRequestReset(final Event event) {
		handleIdentityRequestResetWasCalled = true;
		handleIdentityRequestResetParamEvent = event;
		super.handleIdentityRequestReset(event);
	}

	boolean shouldWaitForPendingSharedState = false;
	boolean shouldWaitForPendingSharedStateCalled;
	@Override
	boolean shouldWaitForPendingSharedState(final Event event, final String extensionName,
											final EventData extensionSharedState) {
		shouldWaitForPendingSharedStateCalled = true;
		return shouldWaitForPendingSharedState;
	}

	boolean callShouldWaitForPendingSharedState(final Event event,
			final String extensionName,
			final EventData extensionSharedState) {
		return super.shouldWaitForPendingSharedState(event, extensionName, extensionSharedState);
	}

	StringBuilder callGenerateVisitorIDURLPayload(final ConfigurationSharedStateIdentity configSharedState,
			final EventData analyticsSharedState) {
		return super.generateVisitorIDURLPayload(configSharedState, analyticsSharedState);
	}

	boolean generateCustomerIdsWasCalled;
	List<VisitorID> generateCustomerIds(final Map<String, String> identifiers,
										final VisitorID.AuthenticationState authenticationState) {
		generateCustomerIdsWasCalled = true;
		return super.generateCustomerIds(identifiers, authenticationState);
	}

	void bootup(final Event bootEvent)  {
		bootupWasCalled = true;
		super.bootup(bootEvent);
	}

	void setDataStore(final LocalStorageService.DataStore dataStore) {
		internalDataStore = dataStore;
	}

	EventData getAnalyticsSharedState() {
		EventData data = new EventData();
		data.putString(IdentityTestConstants.EventDataKeys.Analytics.ANALYTICS_ID, "test_aid");
		return data;
	}
}
