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
 * defines a generic listener that can hear a specific kind of {@code Event} on an {@code EventHub}
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
interface EventListener {
	/**
	 * called when an event matching the {@code EventType} and {@code EventSource} of this listener is
	 * received on the {@code EventHub} that this listener is registered with.
	 *
	 * @param e {@code Event} that was received.
	 */
	void hear(final Event e);

	/**
	 * Called when current listener is unregistered from the parent module
	 */
	void onUnregistered();

	/**
	 * @return the {@code EventSource} this listener is registered to receive events from.
	 */
	EventSource getEventSource();

	/**
	 * @return the {@code EventType} this listener is registered to receive events from.
	 */
	EventType getEventType();
}
