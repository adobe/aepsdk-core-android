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

import java.util.Map;

public class MockCore extends Core {

	MockCore(PlatformServices platformServices) {
		super(platformServices);
	}

	MockCore(PlatformServices platformServices, EventHub eventHub) {
		super(platformServices, eventHub);
	}

	@Override
	void registerExtension(Class<? extends Extension> extensionClass,
						   ExtensionErrorCallback<ExtensionError> errorCallback) {
		super.registerExtension(extensionClass, errorCallback);
	}

	@Override
	boolean dispatchEvent(Event event, ExtensionErrorCallback<ExtensionError> errorCallback) {
		return super.dispatchEvent(event, errorCallback);
	}

	@Override
	void configureWithAppID(String appId) {
		super.configureWithAppID(appId);
	}

	@Override
	void configureWithFileInPath(String filepath) {
		super.configureWithFileInPath(filepath);
	}

	@Override
	void updateConfiguration(Map<String, Object> configMap) {
		super.updateConfiguration(configMap);
	}

	@Override
	void clearUpdatedConfiguration() {
		super.clearUpdatedConfiguration();
	}

	@Override
	void setPrivacyStatus(MobilePrivacyStatus privacyStatus) {
		super.setPrivacyStatus(privacyStatus);
	}

	@Override
	void getPrivacyStatus(AdobeCallback<MobilePrivacyStatus> callback) {
		super.getPrivacyStatus(callback);
	}

	@Override
	void getSdkIdentities(AdobeCallback<String> callback) {
		super.getSdkIdentities(callback);
	}

	boolean trackActionCalled;
	String trackActionParameterAction;
	Map<String, String> trackActionParameterContextData;
	@Override
	void trackAction(String action, Map<String, String> contextData) {
		trackActionCalled = true;
		trackActionParameterAction = action;
		trackActionParameterContextData = contextData;
	}

	boolean trackStateCalled;
	String trackStateParameterState;
	Map<String, String> trackStateParameterContextData;
	@Override
	void trackState(String state, Map<String, String> contextData) {
		trackStateCalled = true;
		trackStateParameterState = state;
		trackStateParameterContextData = contextData;
	}

	boolean setAdvertisingIdentifierCalled;
	String setAdvertisingIdentifierParameteradid;
	@Override
	void setAdvertisingIdentifier(String adid) {
		setAdvertisingIdentifierCalled = true;
		setAdvertisingIdentifierParameteradid = adid;
	}

	boolean setPushIdentifierCalled;
	String setPushIdentifierParameterRegistrationID;
	@Override
	void setPushIdentifier(String registrationID) {
		setPushIdentifierCalled = true;
		setPushIdentifierParameterRegistrationID = registrationID;
	}

	boolean lifecycleStartCalled;
	Map<String, String> lifecycleStartCalledParameterAdditionalContextData;
	@Override
	void lifecycleStart(Map<String, String> additionalContextData) {
		lifecycleStartCalled = true;
		lifecycleStartCalledParameterAdditionalContextData = additionalContextData;
	}

	boolean lifecyclePauseCalled;
	@Override
	void lifecyclePause() {
		lifecyclePauseCalled = true;
	}

	boolean collectPiiCalled;
	Map<String, String> collectPiiParameterData;
	@Override
	void collectPii(Map<String, String> data) {
		collectPiiCalled = true;
		collectPiiParameterData = data;
	}

	boolean collectDataCalled;
	Map<String, Object> collectDataParameterMarshalledData;
	@Override
	void collectData(final Map<String, Object> marshalledData) {
		collectDataCalled = true;
		collectDataParameterMarshalledData = marshalledData;
	}


	boolean dispatchEventWithResponseCallbackWithTimeoutCalled;
	@Override
	void dispatchEventWithResponseCallback(Event event, AdobeCallbackWithError<Event> responseCallback) {
		dispatchEventWithResponseCallbackWithTimeoutCalled = true;
	}

	boolean resetIdentitiesCalled;
	@Override
	void resetIdentities() {
		resetIdentitiesCalled = true;
	}
}
