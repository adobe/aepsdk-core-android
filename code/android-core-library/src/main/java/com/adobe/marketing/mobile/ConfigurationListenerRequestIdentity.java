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

public class ConfigurationListenerRequestIdentity extends ModuleEventListener<ConfigurationExtension> {

	/**
	 * Creates a ConfigurationListenerRequestIdentity instance.
	 *
	 * @param type   the type of event this listener is listening for
	 * @param source the source of event this listener is listening for
	 *
	 * @param    module a reference to the ConfigurationExtension module
	 * @see EventType
	 * @see EventSource
	 */
	ConfigurationListenerRequestIdentity(final ConfigurationExtension module, final EventType type,
										 final EventSource source) {
		super(module, type, source);
	}

	/**
	 * Handler for {@code EventType.CONFIGURATION} {@code EventSource.REQUEST_IDENTITY} {@code Event}
	 * requesting SDK identities retrieval.
	 *
	 * @param    event the Event object representing a configuration request content event
	 * @see Event
	 */
	@Override
	public void hear(final Event event) {
		parentModule.handleGetSdkIdentitiesEvent(event);
	}
}
