/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2021 Adobe
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
 * Listens for {@link EventType#GENERIC_IDENTITY}, {@link EventSource#REQUEST_RESET} events and passes them to
 * the parent {@link IdentityExtension} for processing
 */
class ListenerIdentityGenericIdentityRequestReset extends ModuleEventListener<IdentityExtension> {

	/**
	 * Constructor
	 *
	 * @param extension {@link IdentityExtension} that owns this listener
	 * @param type {@link EventType} that this listener will hear
	 * @param source {@link EventSource} that this listener will hear
	 */
	ListenerIdentityGenericIdentityRequestReset(final IdentityExtension extension, final EventType type,
			final EventSource source) {
		super(extension, type, source);
	}

	/**
	 * Listens for the request reset {@link Event}
	 *
	 * @param event {@link Event} of the reset {@link Event}
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
