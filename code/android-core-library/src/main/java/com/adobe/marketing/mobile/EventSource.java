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
 * Class to define the source of an {@code Event}
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 * @see Event
 * @see EventType
 */
public final class EventSource {

    private EventSource() {}

    public static final String NONE = "com.adobe.eventSource.none";
    public static final String OS = "com.adobe.eventSource.os";
    public static final String REQUEST_CONTENT = "com.adobe.eventSource.requestContent";
    public static final String REQUEST_IDENTITY = "com.adobe.eventSource.requestIdentity";
    public static final String REQUEST_PROFILE = "com.adobe.eventSource.requestProfile";
    public static final String REQUEST_RESET = "com.adobe.eventSource.requestReset";
    public static final String RESPONSE_CONTENT = "com.adobe.eventSource.responseContent";
    public static final String RESPONSE_IDENTITY = "com.adobe.eventSource.responseIdentity";
    public static final String RESPONSE_PROFILE = "com.adobe.eventSource.responseProfile";
    public static final String SHARED_STATE = "com.adobe.eventSource.sharedState";
    public static final String WILDCARD = "com.adobe.eventSource._wildcard_";
    public static final String APPLICATION_LAUNCH = "com.adobe.eventSource.applicationLaunch";
    public static final String APPLICATION_CLOSE = "com.adobe.eventSource.applicationClose";
    public static final String CONSENT_PREFERENCE = "consent:preferences";
    public static final String UPDATE_CONSENT = "com.adobe.eventSource.updateConsent";
    public static final String RESET_COMPLETE = "com.adobe.eventSource.resetComplete";
    public static final String UPDATE_IDENTITY = "com.adobe.eventSource.updateIdentity";
    public static final String REMOVE_IDENTITY = "com.adobe.eventSource.removeIdentity";
    public static final String ERROR_RESPONSE_CONTENT =
            "com.adobe.eventSource.errorResponseContent";
}
