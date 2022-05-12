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

public class MockSignalHitsDatabase extends SignalHitsDatabase {
	MockSignalHitsDatabase(PlatformServices services) {
		super(services);
	}

	@Override
	void updatePrivacyStatus(final MobilePrivacyStatus privacyStatus) {
	}

	boolean queueWasCalled;
	SignalHit queueParametersSignalHit;
	MobilePrivacyStatus queueParametersMobilePrivacyStatus;
	long queueParametersTimestamp;

	@Override
	void queue(final SignalHit signalHit, final long timestamp, final MobilePrivacyStatus privacyStatus) {
		this.queueParametersSignalHit = signalHit;
		this.queueParametersTimestamp = timestamp;
		this.queueParametersMobilePrivacyStatus = privacyStatus;
		this.queueWasCalled = true;
	}
}
