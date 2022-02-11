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

class ConfigurationWildCardListener extends ModuleEventListener<ConfigurationExtension> {
	/**
	 * Creates a ConfigurationWildCardListener instance
	 *
	 * @param type   the type of event this listener is listening for
	 * @param source the source of event this listener is listening for
	 *
	 * @param    module a reference to the ConfigurationExtension module
	 * @see EventType
	 * @see EventSource
	 */
	ConfigurationWildCardListener(final ConfigurationExtension module, final EventType type,
								  final EventSource source) {
		super(module, type, source);
	}

	/**
	 * hear all events dispatched from event hub
	 *
	 * @param    event the Event object dispatched to event hub
	 * @see Event
	 */
	@Override
	public void hear(final Event event) {
		parentModule.handleWildcardEvent(event);
	}
}

