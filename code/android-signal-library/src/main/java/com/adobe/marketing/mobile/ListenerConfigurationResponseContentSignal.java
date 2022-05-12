/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 */

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
