/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2020 Adobe
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

/**
 * Listens for {@link EventType#IDENTITY}, {@link EventSource#RESPONSE_IDENTITY} events and passes them to
 * the parent {@link IdentityExtension} for processing
 */
public class IdentityListenerResponseIdentity extends ModuleEventListener<IdentityExtension> {

	/**
	 * Constructor
	 *
	 * @param extension {@link IdentityExtension} that owns this listener
	 * @param type {@link EventType} that this listener will hear
	 * @param source {@link EventSource} that this listener will hear
	 */
	protected IdentityListenerResponseIdentity(final IdentityExtension extension, final EventType type,
			final EventSource source) {
		super(extension, type, source);
	}

	/**
	 * This method is invoked when and event with {@link EventType#IDENTITY}, {@link EventSource#RESPONSE_IDENTITY} is received
	 * by the Event Hub. The parent extension processes the event if a shared state update is indicated.
	 *
	 * @param event {@link Event} containing identity response data and the shared state update flag (optional)
	 */
	@Override
	public void hear(final Event event) {
		parentModule.getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				parentModule.handleIdentityResponseIdentityForSharedState(event);
			}
		});
	}
}
