/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 *//*


package com.adobe.marketing.mobile;

import java.util.Map;

*/
/**
 * Listens for {@code EventType.RULES_ENGINE} - {@code EventSource.RESPONSE_CONTENT} {@code Event}.
 *//*

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
*/
