/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe
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

import com.adobe.marketing.mobile.identity.ConfigurationSharedStateIdentity;
import com.adobe.marketing.mobile.identity.IdentityExtension;
import com.adobe.marketing.mobile.identity.IdentityHitsDatabase;

public class MockIdentityHitsDatabase extends IdentityHitsDatabase {
	public MockIdentityHitsDatabase(final IdentityExtension parent, final PlatformServices services) {
		super(parent, services);
	}

	boolean queueWasCalled = false;
	String queueParameterUrl;
	Event queueParameterEvent;
	ConfigurationSharedStateIdentity queuedConfig;

	@Override
	boolean queue(final String url, final Event event, final ConfigurationSharedStateIdentity configSharedState) {
		queueWasCalled = true;
		queueParameterUrl = url;
		queueParameterEvent = event;
		queuedConfig = configSharedState;
		return true;
	}

	boolean updatePrivacyStatusWasCalled = false;
	@Override
	void updatePrivacyStatus(final MobilePrivacyStatus privacyStatus) {
		updatePrivacyStatusWasCalled = true;
	}
}
