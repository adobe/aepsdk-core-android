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

public class MockSignal extends SignalExtension {

	public MockSignal(EventHub hub, PlatformServices services) {
		super(hub, services);
	}

	Event onReceivePostbackConsequenceEvent;
	@Override
	void handleSignalConsequenceEvent(final Event event) {
		this.onReceivePostbackConsequenceEvent = event;
	}

	Event onReceiveOpenUrlEventParameterEvent;
	@Override
	void handleOpenURLConsequenceEvent(final Event event) {
		this.onReceiveOpenUrlEventParameterEvent = event;
	}

	boolean tryProcessQueuedEventWasCalled;

	@Override
	void tryProcessQueuedEvent() {
		this.tryProcessQueuedEventWasCalled = true;
	}

	boolean updatePrivacyStatusWasCalled;
	MobilePrivacyStatus updatePrivacyStatusParameter;
	@Override
	void updatePrivacyStatus(MobilePrivacyStatus privacyStatus) {
		updatePrivacyStatusWasCalled = true;
		updatePrivacyStatusParameter = privacyStatus;
	}
}