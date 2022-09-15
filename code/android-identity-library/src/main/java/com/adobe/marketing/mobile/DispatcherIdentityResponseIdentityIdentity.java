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

class DispatcherIdentityResponseIdentityIdentity extends ModuleEventDispatcher<IdentityExtension> {
	/**
	 * Constructor
	 *
	 * @param hub {@link EventHub} instance used by this dispatcher
	 * @param extension parent {@link IdentityExtension} that owns this dispatcher
	 */
	DispatcherIdentityResponseIdentityIdentity(final EventHub hub, final IdentityExtension extension) {
		super(hub, extension);
	}

	/**
	 * Dispatches a {@link EventType#IDENTITY} {@link EventSource#RESPONSE_IDENTITY} event for the given
	 * {@link EventData}, name, and pair ID
	 *
	 * @param eventName {@link String} containing the name of the {@link Event} to be dispatched
	 * @param eventData {@code EventData} to be attached to the {@code Event} that will be dispatched
	 * @param pairID {@code String} containing the optional pair ID for a potential one-time listener
	 */
	void dispatchResponse(final String eventName, final EventData eventData, final String pairID) {
		Event newEvent = new Event.Builder(eventName, EventType.IDENTITY,
										   EventSource.RESPONSE_IDENTITY).setData(eventData).setPairID(pairID).build();
		dispatch(newEvent);
		Log.trace(IdentityExtension.LOG_SOURCE, "dispatchResponse : Identity Response event has been added to event hub : %s",
				  newEvent);
	}
}
