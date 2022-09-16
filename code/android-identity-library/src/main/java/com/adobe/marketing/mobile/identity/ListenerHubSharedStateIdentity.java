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
 * Listens for {@link EventType#HUB}, {@link EventSource#SHARED_STATE} events and kicks event queue processing
 * for parent {@link IdentityExtension} module
 */
class ListenerHubSharedStateIdentity extends ModuleEventListener<IdentityExtension> {

	/**
	 * Constructor
	 *
	 * @param extension {@link IdentityExtension} that owns this listener
	 * @param type {@link EventType} that this listener will hear
	 * @param source {@link EventSource} that this listener will hear
	 */
	ListenerHubSharedStateIdentity(final IdentityExtension extension, final EventType type, final EventSource source) {
		super(extension, type, source);
	}

	/**
	 * Kicks off the parent {@link IdentityExtension}'s event queue processing
	 * after a {@code Configuration} shared state change
	 *
	 * @param event {@link Event} containing shared state information
	 */
	@Override
	public void hear(final Event event) {
		parentModule.getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				final String stateName = event.getData().optString(IdentityConstants.EventDataKeys.STATE_OWNER, null);

				if (IdentityConstants.EventDataKeys.Configuration.MODULE_NAME.equals(stateName) ||
						IdentityConstants.EventDataKeys.Analytics.MODULE_NAME.equals(stateName) ||
						IdentityConstants.EventDataKeys.EventHub.MODULE_NAME.equals(stateName)) {
					parentModule.processEventQueue();
				}
			}
		});
	}
}
