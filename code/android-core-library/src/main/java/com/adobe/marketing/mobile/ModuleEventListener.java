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
 * Abstract class that defines an {@code Event} listener for a {@code Module}
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
abstract class ModuleEventListener<T extends Module> implements EventListener {
	final T parentModule;
	private final EventType type;
	private final EventSource source;

	/**
	 * Default constructor. Must be implemented by any extending classes, and must be called by the extending class'
	 * constructor.
	 *
	 * @param module parent {@code Module} that owns this dispatcher.
	 * @param type   {@code EventType} to register this listener for
	 * @param source {@code EventSource} to register this listener for
	 */
	protected ModuleEventListener(final T module, final EventType type, final EventSource source) {
		this.parentModule = module;
		this.type = type;
		this.source = source;
	}

	/**
	 * @return {@code EventSource} this listener is currently listening for
	 */
	@Override
	public final EventSource getEventSource() {
		return source;
	}

	/**
	 * @return {@code EventType} this listener is currently listening for
	 */
	@Override
	public final EventType getEventType() {
		return type;
	}

	@Override
	public void onUnregistered() {}

}
