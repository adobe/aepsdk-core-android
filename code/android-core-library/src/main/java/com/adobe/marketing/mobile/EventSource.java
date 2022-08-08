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
 * Class to define the source of an {@code Event}
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 * @see Event
 * @see EventHub
 * @see EventType
 */
public final class EventSource {

	// Todo - Expose String constants. Remove 'TYPE' prefix after fixing build issues
	public static final String TYPE_WILDCARD = "com.adobe.eventSource._wildcard_";
	public static final String TYPE_SHARED_STATE = "com.adobe.eventSource.sharedState";
	public static final String TYPE_REQUEST_CONTENT = "com.adobe.eventSource.requestContent";
	public static final String TYPE_REQUEST_IDENTITY = "com.adobe.eventSource.requestIdentity";
	public static final String TYPE_REQUEST_RESET = "com.adobe.eventSource.requestReset";
	public static final String TYPE_OS = "com.adobe.eventSource.os";
	private static final String ADOBE_PREFIX = "com.adobe.eventSource.";
	private static final Map<String, EventSource> knownSources = new HashMap<String, EventSource>();
	private static final Object knownSourcesMutex = new Object();

	static final EventSource BOOTED = get(ADOBE_PREFIX + "booted");
	static final EventSource NONE = get(ADOBE_PREFIX + "none");
	static final EventSource OS = get(ADOBE_PREFIX + "os");
	static final EventSource REQUEST_CONTENT = get(ADOBE_PREFIX + "requestContent");
	static final EventSource REQUEST_IDENTITY = get(ADOBE_PREFIX + "requestIdentity");
	static final EventSource REQUEST_PROFILE = get(ADOBE_PREFIX + "requestProfile");
	static final EventSource REQUEST_RESET = get(ADOBE_PREFIX + "requestReset");
	static final EventSource RESPONSE_CONTENT = get(ADOBE_PREFIX + "responseContent");
	static final EventSource RESPONSE_IDENTITY = get(ADOBE_PREFIX + "responseIdentity");
	static final EventSource RESPONSE_PROFILE = get(ADOBE_PREFIX + "responseProfile");
	static final EventSource SHARED_STATE = get(ADOBE_PREFIX + "sharedState");
	static final EventSource WILDCARD = get(ADOBE_PREFIX + "_wildcard_");
	static final EventSource APPLICATION_LAUNCH = get(ADOBE_PREFIX + "applicationLaunch");
	static final EventSource APPLICATION_CLOSE = get(ADOBE_PREFIX + "applicationClose");


	private final String name;

	/**
	 * Returns an {@code EventSource} representing the provided {@code String},
	 * null if the provided string is null/empty or it has whitespaces only.
	 * <p>
	 * Whenever the lower cased string of {@code sourceName} is the same, this method will return the same
	 * {@link EventSource} instance, so '==' can be used to compare the equivalent of two {@code EventSource}s.
	 *
	 * @param sourceName the name of the {@link EventSource} to return
	 * @return an {@code EventSource} from the @{code knownTypes} map
	 */
	static EventSource get(final String sourceName) {
		if (StringUtils.isNullOrEmpty(sourceName)) {
			return null;
		}

		final String normalizedSourceName = sourceName.toLowerCase();

		synchronized (knownSourcesMutex) {
			if (knownSources.containsKey(normalizedSourceName)) {
				return knownSources.get(normalizedSourceName);
			}

			final EventSource eventSource = new EventSource(normalizedSourceName);
			knownSources.put(normalizedSourceName, eventSource);
			return eventSource;
		}
	}

	/**
	 * Constructor for an {@code EventSource}
	 *
	 * @param sourceName {@link String} description of the {@link EventSource} being created
	 */
	private EventSource(final String sourceName) {
		name = sourceName;
	}

	/**
	 * Returns the {@code String} name of the current {@code EventSource}
	 *
	 * @return the {@link String} representation of this {@link EventSource}
	 */
	String getName() {
		return name;
	}
}