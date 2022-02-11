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
 * Abstract class that defines an {@code Event} dispatcher for a {@code Module}
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
abstract class ModuleEventDispatcher<T extends Module> {
	private final EventHub eventHub;
	protected final T parentModule;

	/**
	 * Default constructor.  Most be implemented by any extending classes, and must be called by the extending class'
	 * constructor.
	 *
	 * @param    hub    {@code EventHub} that this dispatcher will interoperate with
	 * @param    module     parent {@code Module} that owns this dispatcher.
	 */
	protected ModuleEventDispatcher(final EventHub hub, final T module) {
		this.eventHub = hub;
		this.parentModule = module;
	}

	/**
	 * Dispatches an event to the associated {@code EventHub}
	 *
	 * @param    e    {@code Event} to dispatch
	 */
	protected final void dispatch(final Event e) {
		eventHub.dispatch(e);
	}
}
