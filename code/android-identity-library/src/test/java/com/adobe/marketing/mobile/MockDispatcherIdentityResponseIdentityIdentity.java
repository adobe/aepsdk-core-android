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

public class MockDispatcherIdentityResponseIdentityIdentity extends DispatcherIdentityResponseIdentityIdentity {

	boolean dispatchResponseWasCalled;
	EventData dispatchResponseParameterEventData;
	String dispatchResponseParameterPairID;

	/**
	 * Default constructor.  Most be implemented by any extending classes, and must be called by the extending class'
	 * constructor.
	 *
	 * @param hub    {@code EventHub} that this dispatcher will interoperate with
	 * @param module parent {@code Module} that owns this dispatcher.
	 */
	protected MockDispatcherIdentityResponseIdentityIdentity(EventHub hub, IdentityExtension module) {
		super(hub, module);
		dispatchResponseWasCalled = false;
		dispatchResponseParameterEventData = null;
		dispatchResponseParameterPairID = null;
	}

	void dispatchResponse(final String eventName, final EventData eventData, final String pairID) {
		dispatchResponseWasCalled = true;
		dispatchResponseParameterEventData = eventData;
		dispatchResponseParameterPairID = pairID;
		super.dispatchResponse(eventName, eventData, pairID);
	}
}
