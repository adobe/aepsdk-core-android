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

public class ListenerAnalyticsResponseIdentity extends ModuleEventListener<IdentityExtension> {
	/**
	 * Constructor
	 *
	 * @param extension {@link IdentityExtension} that owns this listener
	 * @param type {@link EventType} that this listener will hear
	 * @param source {@link EventSource} that this listener will hear
	 */
	ListenerAnalyticsResponseIdentity(final IdentityExtension extension, final EventType type, final EventSource source) {
		super(extension, type, source);
	}

	/**
	 * Handles {@link EventType#ANALYTICS} {@link EventSource#RESPONSE_IDENTITY} events which report changes to
	 * the Analytics ID.
	 *
	 * @param event {@link Event} containing the {@link IdentityConstants.EventDataKeys.Analytics#ANALYTICS_ID}
	 */
	@Override
	public void hear(final Event event) {
		parentModule.getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				parentModule.handleAnalyticsResponseIdentity(event);
			}
		});
	}
}
