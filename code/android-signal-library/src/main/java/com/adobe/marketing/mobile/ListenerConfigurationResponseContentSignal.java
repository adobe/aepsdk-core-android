/* ***********************************************************************
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

/**
 * Listens for {@code EventType.CONFIGURATION} - {@code EventSource.RESPONSE_CONTENT} {@code Event}.
 */
class ListenerConfigurationResponseContentSignal extends ModuleEventListener<SignalExtension> {

	ListenerConfigurationResponseContentSignal(final SignalExtension module, final EventType type,
			final EventSource source) {
		super(module, type, source);
	}

	public void hear(final Event e) {
		parentModule.updatePrivacyStatus(MobilePrivacyStatus.fromString(e.getData().optString(
											 SignalConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY, "")));
	}
}
