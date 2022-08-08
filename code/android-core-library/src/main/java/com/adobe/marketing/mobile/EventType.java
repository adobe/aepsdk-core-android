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

import java.util.HashMap;
import java.util.Map;

/**
 * Class to define the type of an {@code Event}
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 * @see Event
 * @see EventHub
 * @see EventSource
 */
public final class EventType {

	// Todo - Expose String constants. Remove 'TYPE' prefix after fixing build issues
	public static final String TYPE_WILDCARD = "com.adobe.eventType._wildcard_";
	public static final String TYPE_HUB = "com.adobe.eventType.hub";
	public static final String TYPE_CONFIGURATION = "com.adobe.eventType.configuration";
	public static final String TYPE_GENERIC_LIFECYCLE = "com.adobe.eventType.generic.lifecycle";
	public static final String TYPE_GENERIC_IDENTITY = "com.adobe.eventType.generic.identity";
	public static final String TYPE_GENERIC_TRACK = "com.adobe.eventType.generic.track";
	public static final String TYPE_GENERIC_DATA = "com.adobe.eventType.generic.data";
	public static final String TYPE_GENERIC_PII = "com.adobe.eventType.generic.pii";

	private static final String ADOBE_PREFIX = "com.adobe.eventType.";
	private static final Map<String, EventType> knownTypes = new HashMap<String, EventType>();
	private static final Object knownTypesMutex = new Object();

	static final EventType ACQUISITION = get(ADOBE_PREFIX + "acquisition");
	static final EventType ANALYTICS = get(ADOBE_PREFIX + "analytics");
	static final EventType AUDIENCEMANAGER = get(ADOBE_PREFIX + "audienceManager");
	static final EventType CAMPAIGN = get(ADOBE_PREFIX + "campaign");
	static final EventType CONFIGURATION = get(ADOBE_PREFIX + "configuration");
	static final EventType CUSTOM = get(ADOBE_PREFIX + "custom");
	static final EventType HUB = get(ADOBE_PREFIX + "hub");
	static final EventType IDENTITY = get(ADOBE_PREFIX + "identity");
	static final EventType LIFECYCLE = get(ADOBE_PREFIX + "lifecycle");
	static final EventType LOCATION = get(ADOBE_PREFIX + "location");
	static final EventType PII = get(ADOBE_PREFIX + "pii");
	static final EventType RULES_ENGINE = get(ADOBE_PREFIX + "rulesEngine");
	static final EventType SIGNAL = get(ADOBE_PREFIX + "signal");
	static final EventType SYSTEM = get(ADOBE_PREFIX + "system");
	static final EventType TARGET = get(ADOBE_PREFIX + "target");
	static final EventType USERPROFILE = get(ADOBE_PREFIX + "userProfile");
	static final EventType PLACES = get(ADOBE_PREFIX + "places");
	static final EventType GENERIC_TRACK = get(ADOBE_PREFIX + "generic.track");
	static final EventType GENERIC_LIFECYLE = get(ADOBE_PREFIX + "generic.lifecycle");
	static final EventType GENERIC_IDENTITY = get(ADOBE_PREFIX + "generic.identity");
	static final EventType GENERIC_PII = get(ADOBE_PREFIX + "generic.pii");
	static final EventType GENERIC_DATA = get(ADOBE_PREFIX + "generic.data");
	static final EventType WILDCARD = get(ADOBE_PREFIX + "_wildcard_");


	private final String name;

	/**
	 * Returns an {@code EventType} representing the provided {@code String},
	 * null if the provided string is null/empty or it has whitespaces only.
	 * <p>
	 * Whenever the lower cased string of {@code typeName} is the same, this method will return the same
	 * {@link EventType} instance, so '==' can be used to compare the equivalent of two {@code EventType}s.
	 *
	 * @param typeName the name of the {@code EventType} to return
	 * @return an {@code EventType} from the @{code knownTypes} map
	 */
	static EventType get(final String typeName) {
		if (StringUtils.isNullOrEmpty(typeName)) {
			return null;
		}

		final String normalizedTypeName = typeName.toLowerCase();

		synchronized (knownTypesMutex) {
			if (knownTypes.containsKey(normalizedTypeName)) {
				return knownTypes.get(normalizedTypeName);
			}

			final EventType eventType = new EventType(normalizedTypeName);
			knownTypes.put(normalizedTypeName, eventType);
			return eventType;
		}
	}

	/**
	 * Constructor for an {@code EventType}
	 *
	 * @param typeName {@link String} description of the {@code EventType} being created
	 */
	private EventType(final String typeName) {
		name = typeName;
	}

	/**
	 * Returns the {@code String} name of the current {@code EventType}
	 *
	 * @return the {@link String} representation of this {@code EventType}
	 */
	String getName() {
		return name;
	}

}