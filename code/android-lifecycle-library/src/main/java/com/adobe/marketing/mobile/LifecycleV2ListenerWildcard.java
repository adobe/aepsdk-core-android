/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2021 Adobe Systems Incorporated
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
 *
 * *************************************************************************/

package com.adobe.marketing.mobile;

/**
 * LifecycleExtension wildcard listener used for computing the app close timestamp.
 */
class LifecycleV2ListenerWildcard extends ModuleEventListener<LifecycleExtension> {

	/**
	 * Listener constructor
	 *
	 * @param extension parent extension that owns this listener
	 * @param type   EventType to register this listener for - any
	 * @param source EventSource to register this listener for - any
	 */
	LifecycleV2ListenerWildcard(final LifecycleExtension extension, final EventType type, final EventSource source) {
		super(extension, type, source);
	}

	/**
	 * Callback for EventHub events
	 *
	 * @param event EventHub event
	 */
	@Override
	public void hear(final Event event) {
		if (event == null) {
			return;
		}

		parentModule.getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				parentModule.updateLastKnownTimestamp(event);
			}
		});
	}
}