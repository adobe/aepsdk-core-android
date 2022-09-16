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

import com.adobe.marketing.mobile.identity.DispatcherAnalyticsRequestContentIdentity;
import com.adobe.marketing.mobile.identity.IdentityExtension;

public class MockDispatcherAnalyticsRequestContentIdentity extends DispatcherAnalyticsRequestContentIdentity {

	boolean dispatchAnalyticseWasCalled;
	EventData analyticsDataDispatched;

	/**
	 * Default constructor.  Most be implemented by any extending classes, and must be called by the extending class'
	 * constructor.
	 *
	 * @param hub    {@code EventHub} that this dispatcher will interoperate with
	 * @param module parent {@code Module} that owns this dispatcher.
	 */
	protected MockDispatcherAnalyticsRequestContentIdentity(EventHub hub, IdentityExtension module) {
		super(hub, module);
		dispatchAnalyticseWasCalled = false;
		analyticsDataDispatched = null;
	}

	void dispatchAnalyticsHit(final EventData eventData) {
		dispatchAnalyticseWasCalled = true;
		analyticsDataDispatched = eventData;
		super.dispatchAnalyticsHit(eventData);
	}
}
