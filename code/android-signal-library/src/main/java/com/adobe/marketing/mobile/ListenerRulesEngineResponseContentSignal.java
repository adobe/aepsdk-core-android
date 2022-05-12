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

import java.util.Map;

/**
 * Listens for {@code EventType.RULES_ENGINE} - {@code EventSource.RESPONSE_CONTENT} {@code Event}.
 */
class ListenerRulesEngineResponseContentSignal extends ModuleEventListener<SignalExtension> {

	private static final String LOGTAG = "ListenerRulesEngineResponseContentSignal";

	ListenerRulesEngineResponseContentSignal(final SignalExtension module, final EventType type, final EventSource source) {
		super(module, type, source);
	}

	public void hear(final Event event) {
		final EventData eventData = event == null ? null : event.getData();

		if (eventData == null) {
			Log.debug(LOGTAG, "%s (Event Data)", Log.UNEXPECTED_NULL_VALUE);
			return;
		}

		final Map<String, Variant> triggeredConsequence = eventData.optVariantMap(
					SignalConstants.EventDataKeys.RuleEngine.CONSEQUENCE_TRIGGERED, null);

		if (triggeredConsequence == null || triggeredConsequence.isEmpty()) {
			Log.debug(LOGTAG, "Not a triggered rule. Return.");
			return;
		}

		//type
		final String consequenceType = Variant.optVariantFromMap(triggeredConsequence,
									   SignalConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_TYPE).optString(null);

		if (StringUtils.isNullOrEmpty(consequenceType)) {
			Log.debug(LOGTAG, "Triggered rule is not Signal type. Return.");
			return;
		}

		if (SignalConstants.EventDataKeys.Signal.RULES_RESPONSE_CONSEQUENCE_TYPE_POSTBACKS.equals(consequenceType)
				|| SignalConstants.EventDataKeys.Signal.RULES_RESPONSE_CONSEQUENCE_TYPE_PII.equals(consequenceType)) {
			parentModule.handleSignalConsequenceEvent(event);
		} else if (SignalConstants.EventDataKeys.Signal.RULES_RESPONSE_CONSEQUENCE_TYPE_OPEN_URL.equals(consequenceType)) {
			parentModule.handleOpenURLConsequenceEvent(event);
		} else {
			Log.debug(LOGTAG, "Triggered rule is not a valid Signal type. Cannot handle.");
		}

	}
}
