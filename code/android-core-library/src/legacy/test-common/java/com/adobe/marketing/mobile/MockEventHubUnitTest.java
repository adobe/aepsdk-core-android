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

import java.util.ArrayList;
import java.util.List;

class MockEventHubUnitTest extends EventHub {

	MockEventHubUnitTest(final String name, final PlatformServices services) {
		super(name, services);
	}

	boolean isDispatchedCalled;
	Event   dispatchedEvent;
	List<Event> dispatchedEventList = new ArrayList<Event>();

	@Override
	void dispatch(final Event e) {
		this.isDispatchedCalled = true;
		this.dispatchedEvent = e;
		this.dispatchedEventList.add(e);
	}

	void dispatchEvent(final Event e) {
		super.dispatch(e);
	}

	boolean                     registerOneTimeListenerCalled           = false;
	String                      registerOneTimeListenerParamPairId      = null;
	Module.OneTimeListenerBlock registerOneTimeListenerParamBlock       = null;

	@Override
	public void registerOneTimeListener(String pairId, Module.OneTimeListenerBlock listenerBlock) {
		registerOneTimeListenerCalled = true;
		registerOneTimeListenerParamPairId = pairId;
		registerOneTimeListenerParamBlock = listenerBlock;
	}


	boolean                     registerOneTimeListenerWithErrorCalled           = false;
	String                      registerOneTimeListenerWithErrorParamPairId      = null;
	Module.OneTimeListenerBlock registerOneTimeListenerWithErrorParamBlock       = null;
	AdobeCallbackWithError registerOneTimeListenerWithErrorParamErrorCallback    = null;
	int registerOneTimeListenerWithErrorParamTimeoutInMilliSec    				 = 0;

	@Override
	void registerOneTimeListener(final String pairID,
								 final Module.OneTimeListenerBlock block,
								 final AdobeCallbackWithError adobeCallbackWithError,
								 final int timeoutInMilliSec) {
		registerOneTimeListenerWithErrorCalled = true;
		registerOneTimeListenerWithErrorParamPairId = pairID;
		registerOneTimeListenerWithErrorParamBlock = block;
		registerOneTimeListenerWithErrorParamErrorCallback = adobeCallbackWithError;
		registerOneTimeListenerWithErrorParamTimeoutInMilliSec = timeoutInMilliSec;
	}

	void setSharedState(final String stateName, final EventData eventData) {
		createAndUpdateSharedState(stateName, Event.SHARED_STATE_OLDEST.getEventNumber(), eventData);
		createAndUpdateSharedState(stateName, Event.SHARED_STATE_NEWEST.getEventNumber(), eventData);
	}

	void createAndUpdateSharedState(final String stateName, final int version, final EventData state) {
		try {
			createSharedState(new InternalModule(stateName, this, getPlatformServices()) {
			}, version, state);
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}

		try {
			updateSharedState(new InternalModule(stateName, this, getPlatformServices()) {
			}, version, state);
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	void createAndUpdateSharedState(final String stateName, final int version, final EventData state,
									final SharedStateType sharedStateType) {
		try {
			createSharedState(new InternalModule(stateName, this, getPlatformServices()) {
			}, version, state, sharedStateType);
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}

		try {
			updateSharedState(new InternalModule(stateName, this, getPlatformServices()) {
			}, version, state, sharedStateType);
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	void createSharedState(final String stateName, final int version, final EventData state) {
		try {
			createSharedState(new InternalModule(stateName, this, getPlatformServices()) {
			}, version, state);
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	void createSharedState(final String stateName, final int version, final EventData state,
						   final SharedStateType sharedStateType) {
		try {
			createSharedState(new InternalModule(stateName, this, getPlatformServices()) {
			}, version, state, sharedStateType);
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	boolean throwException = false;
	boolean getSharedEventStateCalled             = false;
	String  getSharedEventStateParameterStateName = null;
	Event   getSharedEventStateParameterEvent     = null;
	SharedStateType getSharedEventStateParameterStateType;

	@Override
	protected EventData getSharedEventState(final String stateName, Event event, Module callingModule) {
		return getSharedEventState(stateName, event, callingModule, SharedStateType.STANDARD);
	}

	@Override
	protected EventData getSharedEventState(final String stateName, Event event, Module callingModule,
											SharedStateType sharedStateType) {
		getSharedEventStateCalled = true;
		getSharedEventStateParameterStateName = stateName;
		getSharedEventStateParameterEvent = event;
		getSharedEventStateParameterStateType = sharedStateType;

		if (throwException) {
			throw new RuntimeException("error occurred");
		}

		if (event == null) {
			event = new Event.Builder("Test", EventType.CUSTOM, EventSource.NONE)
			.build();
			event.setEventNumber(Event.SHARED_STATE_NEWEST.getEventNumber());
		}

		return super.getSharedEventState(stateName,
										 event,
										 callingModule != null ?
										 callingModule :
		new InternalModule(stateName, this, getPlatformServices()) {
		}, sharedStateType);
	}

	boolean createSharedStateCalled;
	int  createSharedStateParamVersion;
	EventData createSharedStateParamState;
	SharedStateType createSharedStateParamType;

	@Override
	void createSharedState(final Module module, final int version,
						   final EventData state) throws InvalidModuleException {
		createSharedState(module, version, state, SharedStateType.STANDARD);
	}

	@Override
	void createSharedState(final Module module, final int version,
						   final EventData state, final SharedStateType sharedStateType) throws InvalidModuleException {
		createSharedStateCalled = true;
		createSharedStateParamVersion = version;
		createSharedStateParamState = state;
		createSharedStateParamType = sharedStateType;

		if (throwException) {
			throw new IllegalArgumentException("error occurred");
		}

		super.createSharedState(module, version, state, sharedStateType);
	}


	boolean updateSharedStateWasCalled;
	int  updateSharedStateParamVersion;
	EventData updateSharedStateParamState;
	SharedStateType updateSharedStateParamType;

	@Override
	void updateSharedState(final Module module, final int version,
						   final EventData state) throws InvalidModuleException {
		updateSharedState(module, version, state, SharedStateType.STANDARD);
	}

	@Override
	void updateSharedState(final Module module, final int version,
						   final EventData state, final SharedStateType sharedStateType) throws InvalidModuleException {
		updateSharedStateWasCalled = true;
		updateSharedStateParamVersion = version;
		updateSharedStateParamState = state;
		updateSharedStateParamType = sharedStateType;

		if (throwException) {
			throw new IllegalArgumentException("error occurred");
		}

		super.updateSharedState(module, version, state, sharedStateType);
	}

	boolean createOrUpdateSharedStateWithVersionCalled;
	int  createOrUpdateSharedStateWithVersionParamVersion;
	EventData createOrUpdateSharedStateWithVersionParamState;
	SharedStateType createOrUpdateSharedStateWithVersionParamType;

	@Override
	void createOrUpdateSharedState(final Module module,
								   final int version,
								   final EventData state) throws InvalidModuleException {
		createOrUpdateSharedState(module, version, state, SharedStateType.STANDARD);
	}

	@Override
	void createOrUpdateSharedState(final Module module,
								   final int version,
								   final EventData state, SharedStateType sharedStateType) throws InvalidModuleException {
		createOrUpdateSharedStateWithVersionCalled = true;
		createOrUpdateSharedStateWithVersionParamVersion = version;
		createOrUpdateSharedStateWithVersionParamState = state;
		createOrUpdateSharedStateWithVersionParamType = sharedStateType;

		if (throwException) {
			throw new IllegalArgumentException("error occurred");
		}

		super.createOrUpdateSharedState(module, version, state, sharedStateType);
	}

	boolean createOrUpdateSharedStateWithoutVersionCalled;
	EventData createOrUpdateSharedStateWithoutVersionParamState;
	SharedStateType createOrUpdateSharedStateWithoutVersionParamType;

	@Override
	void createOrUpdateSharedState(final Module module,
								   final EventData state) throws InvalidModuleException {
		createOrUpdateSharedState(module, state, SharedStateType.STANDARD);
	}

	@Override
	void createOrUpdateSharedState(final Module module,
								   final EventData state, final SharedStateType sharedStateType) throws InvalidModuleException {
		createOrUpdateSharedStateWithoutVersionCalled = true;
		createOrUpdateSharedStateWithoutVersionParamState = state;
		createOrUpdateSharedStateWithoutVersionParamType = sharedStateType;

		if (throwException) {
			throw new IllegalArgumentException("error occurred");
		}

		super.createOrUpdateSharedState(module, state, sharedStateType);
	}

	boolean finishModulesRegistrationCalled;
	@Override
	void finishModulesRegistration(final AdobeCallback<Void> completionCallback) {
		super.finishModulesRegistration(completionCallback);
		finishModulesRegistrationCalled = true;
	}

	void clearSharedStates(final Module module) throws InvalidModuleException {
		clearSharedStates(module, SharedStateType.STANDARD);
	}

	void clearSharedStates(final Module module, final SharedStateType sharedStateType) throws InvalidModuleException {
		if (throwException) {
			throw new IllegalArgumentException("error occurred");
		}

		super.clearSharedStates(module, sharedStateType);
	}


}
