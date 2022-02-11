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

public class ConfigurationListenerSharedState extends ModuleEventListener<ConfigurationExtension> {

	/**
	 * Creates a ConfigurationListenerSharedState instance.
	 *
	 * @param type   the type of event this listener is listening for
	 * @param source the source of event this listener is listening for
	 *
	 * @param    module a reference to the ConfigurationExtension module
	 * @see EventType
	 * @see EventSource
	 */
	ConfigurationListenerSharedState(final ConfigurationExtension module, final EventType type,
									 final EventSource source) {
		super(module, type, source);
	}

	/**
	 * Handler for {@code EventType.HUB} {@code EventSource.SHARED_STATE} {@code Event}
	 *
	 * @param    event the Event object representing shared state change event
	 * @see Event
	 */
	@Override
	public void hear(final Event event) {
		final EventData eventData = event.getData();

		if (eventData == null) {
			return;
		}

		String sharedStateOwner = eventData.optString(ConfigurationConstants.EventDataKeys.Configuration.EVENT_STATE_OWNER,
								  null);

		if (StringUtils.isNullOrEmpty(sharedStateOwner)) {
			return;
		}

		boolean shouldProcess = ConfigurationConstants.EventDataKeys.Configuration.MODULE_NAME.equals(sharedStateOwner);
		shouldProcess |= ConfigurationConstants.SharedStateKeys.Identity.MODULE_NAME.equals(sharedStateOwner);
		shouldProcess |= ConfigurationConstants.SharedStateKeys.Target.MODULE_NAME.equals(sharedStateOwner);
		shouldProcess |= ConfigurationConstants.SharedStateKeys.Audience.MODULE_NAME.equals(sharedStateOwner);
		shouldProcess |= ConfigurationConstants.SharedStateKeys.Analytics.MODULE_NAME.equals(sharedStateOwner);

		if (shouldProcess) {
			parentModule.getExecutor().execute(new Runnable() {
				@Override
				public void run() {
					parentModule.processGetSdkIdsEvent();
				}
			});
		}
	}
}
