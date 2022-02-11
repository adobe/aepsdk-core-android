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
 * Used to wrap a {@code Module.OneTimeListenerBlock} in an {@code EventListener}
 * so it can be added to an {@code EventHub}
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
final class OneTimeListener implements EventListener {
	private final Module.OneTimeListenerBlock logicBlock;
	private boolean isCalled = false;
	private boolean isCancelled = false;
	private final Object mutex = new Object();

	protected OneTimeListener(final Module.OneTimeListenerBlock block) {
		this.logicBlock = block;
	}

	protected boolean isCalled() {
		synchronized (mutex) {
			return isCalled;
		}
	}

	protected void cancel() {
		synchronized (mutex) {
			isCancelled = true;
		}
	}

	@Override
	public void hear(final Event e) {

		synchronized (mutex) {
			if (!isCancelled) {
				logicBlock.call(e);
			}

			isCalled = true;
		}
	}

	@Override
	public void onUnregistered() {}

	@Override
	public EventType getEventType() {
		return null;
	}

	@Override
	public EventSource getEventSource() {
		return null;
	}
}
