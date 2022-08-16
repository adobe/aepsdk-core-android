/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Systems Incorporated
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

class LifecycleListenerSharedState extends ModuleEventListener<LifecycleExtension> {

	/**
	 * Listener constructor
	 *
	 * @param extension parent extension that owns this listener
	 * @param type   EventType to register this listener for - HUB
	 * @param source EventSource to register this listener for - SHARED_STATE
	 */
	LifecycleListenerSharedState(final LifecycleExtension extension, final EventType type, final EventSource source) {
		super(extension, type, source);
	}

	/**
	 * Callback for EventHub Shared State events
	 *
	 * @param event EventHub Shared State event
	 */
	@Override
	public void hear(final Event event) {
		parentModule.getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				parentModule.handleSharedStateUpdateEvent(event);
			}
		});
	}
}
