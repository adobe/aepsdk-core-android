/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 * *************************************************************************/

package com.adobe.marketing.mobile;

import java.util.Map;

@SuppressWarnings("all")
class MockLifecycleExtension extends LifecycleExtension {
	boolean startLifecycleCalled;
	Map<String, String> startLifecycleParameterContextData;
	boolean handleSharedStateUpdateEventCalled;
	boolean queueLifecycleEventCalled;
	boolean pauseLifecycleCalled;
	boolean updateContextDataCalled;
	boolean updateLastKnownTimestampCalled;
	Map<String, String> updateContextDataParameterContextData;
	String advertisingIdentifier = "adid";
	private static final String LIFECYCLE_CONTEXT_DATA = "lifecyclecontextdata";

	MockLifecycleExtension(EventHub hub, PlatformServices services) {
		super(hub, services);
	}

	@Override
	void start(final Event event, final EventData configuration, final boolean isInstall) {
		startLifecycleCalled = true;
		EventData eventData = event.getData();

		if (eventData != null) {
			startLifecycleParameterContextData = eventData.optStringMap(LIFECYCLE_CONTEXT_DATA, null);
		}
	}

	@Override
	void queueEvent(Event event) {
		queueLifecycleEventCalled = true;
	}

	@Override
	void pause(final Event event) {
		pauseLifecycleCalled = true;
	}

	@Override
	void updateContextData(Map<String, String> contextData, int stateVersion) {
		updateContextDataCalled = true;
		updateContextDataParameterContextData = contextData;
	}

	@Override
	String getAdvertisingIdentifier(Event event) {
		return advertisingIdentifier;
	}

	@Override
	void handleSharedStateUpdateEvent(final Event event) {
		handleSharedStateUpdateEventCalled = true;
	}

	@Override
	void updateLastKnownTimestamp(final Event event) {
		updateLastKnownTimestampCalled = true;
	}
}
