/* ***********************************************************************
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
 **************************************************************************/

package com.adobe.marketing.mobile;

public class MockSignal extends SignalExtension {

	public MockSignal(EventHub hub, PlatformServices services) {
		super(hub, services);
	}

	Event onReceivePostbackConsequenceEvent;
	@Override
	void handleSignalConsequenceEvent(final Event event) {
		this.onReceivePostbackConsequenceEvent = event;
	}

	Event onReceiveOpenUrlEventParameterEvent;
	@Override
	void handleOpenURLConsequenceEvent(final Event event) {
		this.onReceiveOpenUrlEventParameterEvent = event;
	}

	boolean tryProcessQueuedEventWasCalled;

	@Override
	void tryProcessQueuedEvent() {
		this.tryProcessQueuedEventWasCalled = true;
	}

	boolean updatePrivacyStatusWasCalled;
	MobilePrivacyStatus updatePrivacyStatusParameter;
	@Override
	void updatePrivacyStatus(MobilePrivacyStatus privacyStatus) {
		updatePrivacyStatusWasCalled = true;
		updatePrivacyStatusParameter = privacyStatus;
	}
}