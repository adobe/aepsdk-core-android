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

package com.adobe.marketing.mobile.identity;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;

/**
 * Listens for {@link EventType#IDENTITY}, {@link EventSource#REQUEST_IDENTITY} events and passes them to
 * the parent {@link IdentityExtension} for processing
 */
class ListenerIdentityRequestIdentity extends ModuleEventListener<IdentityExtension> {

	/**
	 * Constructor
	 *
	 * @param extension {@link IdentityExtension} that owns this listener
	 * @param type {@link EventType} that this listener will hear
	 * @param source {@link EventSource} that this listener will hear
	 */
	ListenerIdentityRequestIdentity(final IdentityExtension extension, final EventType type, final EventSource source) {
		super(extension, type, source);
	}

	/**
	 * All {@link IdentityExtension} public APIs are of {@link EventType#IDENTITY}, {@link EventSource#REQUEST_IDENTITY}
	 * combination.
	 * <ul>
	 *     <li>If {@link Event#getData()} contains an identifier and identifier type OR map of identifiers,
	 *     the result will be a call to {@link IdentityExtension#handleSyncIdentifiers(Event, ConfigurationSharedStateIdentity)}</li>
	 *     <li>If {@code Event.getData()} contains a base URL, the result will append visitor information to the URL</li>
	 *     <li>If neither of the above are true, this {@link Event} represents a request to {@code getIdentifiers()}</li>
	 * </ul>
	 *
	 * @param event {@link Event} containing data to represent a corresponding public API request
	 */
	@Override
	public void hear(final Event event) {
		parentModule.getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				parentModule.enqueueEvent(event);
				parentModule.processEventQueue();
			}
		});
	}
}
