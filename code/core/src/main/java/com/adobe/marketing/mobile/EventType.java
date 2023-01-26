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
 * Class to define the type of an {@code Event}
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 * @see Event
 * @see EventSource
 */
public final class EventType {

    private EventType() {}

    public static final String ACQUISITION = "com.adobe.eventType.acquisition";
    public static final String ANALYTICS = "com.adobe.eventType.analytics";
    public static final String ASSURANCE = "com.adobe.eventType.assurance";
    public static final String AUDIENCEMANAGER = "com.adobe.eventType.audienceManager";
    public static final String CAMPAIGN = "com.adobe.eventType.campaign";
    public static final String CONFIGURATION = "com.adobe.eventType.configuration";
    public static final String CONSENT = "com.adobe.eventType.edgeConsent";
    public static final String CUSTOM = "com.adobe.eventType.custom";
    public static final String EDGE = "com.adobe.eventType.edge";
    public static final String EDGE_IDENTITY = "com.adobe.eventType.edgeIdentity";
    public static final String GENERIC_DATA = "com.adobe.eventType.generic.data";
    public static final String GENERIC_IDENTITY = "com.adobe.eventType.generic.identity";
    public static final String GENERIC_LIFECYCLE = "com.adobe.eventType.generic.lifecycle";
    public static final String GENERIC_PII = "com.adobe.eventType.generic.pii";
    public static final String GENERIC_TRACK = "com.adobe.eventType.generic.track";
    public static final String HUB = "com.adobe.eventType.hub";
    public static final String IDENTITY = "com.adobe.eventType.identity";
    public static final String LIFECYCLE = "com.adobe.eventType.lifecycle";
    public static final String LOCATION = "com.adobe.eventType.location";
    public static final String MEDIA = "com.adobe.eventType.media";
    public static final String MESSAGING = "com.adobe.eventType.messaging";
    public static final String PII = "com.adobe.eventType.pii";
    public static final String PLACES = "com.adobe.eventType.places";
    public static final String RULES_ENGINE = "com.adobe.eventType.rulesEngine";
    public static final String SIGNAL = "com.adobe.eventType.signal";
    public static final String SYSTEM = "com.adobe.eventType.system";
    public static final String TARGET = "com.adobe.eventType.target";
    public static final String USERPROFILE = "com.adobe.eventType.userProfile";
    public static final String WILDCARD = "com.adobe.eventType._wildcard_";
}
