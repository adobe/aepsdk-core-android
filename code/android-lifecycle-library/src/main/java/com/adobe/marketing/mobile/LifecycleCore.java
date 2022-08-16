/* ************************************************************************
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
 **************************************************************************/

package com.adobe.marketing.mobile;

class LifecycleCore {

	private static final String SELF_LOG_TAG = "LifecycleCore";
	//We are suppressing the eventHub unused warning - but keeping the local variable as is for now.
	@SuppressWarnings("unused")
	private EventHub eventHub;

	LifecycleCore(final EventHub eventHub, final ModuleDetails moduleDetails) {
		if (eventHub == null) {
			Log.debug(LifecycleConstants.LOG_TAG, "%s - Core initialization was not successful, %s (EventHub)", SELF_LOG_TAG,
					  Log.UNEXPECTED_NULL_VALUE);
			return;
		}

		this.eventHub = eventHub;

		try {
			eventHub.registerModule(LifecycleExtension.class, moduleDetails);
			Log.trace(LifecycleConstants.LOG_TAG, "%s - Registered %s ", SELF_LOG_TAG, LifecycleExtension.class.getSimpleName());
		} catch (InvalidModuleException e) {
			Log.error(LifecycleConstants.LOG_TAG, "%s - Failed to register LifecycleExtension (%s)", SELF_LOG_TAG, e);
		}
	}
}