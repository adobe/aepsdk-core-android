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

import com.adobe.marketing.mobile.internal.util.StringUtils;
import com.adobe.marketing.mobile.internal.util.UrlUtils;
import com.adobe.marketing.mobile.internal.util.TimeUtils;

import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RuleTokenParser {

	private static final int RANDOM_INT_BOUNDARY = 100000000;
	private static final String KEY_PREFIX = "~";
	private static final String SHARED_STATE_KEY_DELIMITER = "/";
	private static final int MIN_TOKEN_LEN = 4;
	private static final String KEY_URL_ENCODE = "urlenc";
	private static final String TOKEN_REGEX = "(i*)(\\{%(urlenc\\()?([a-zA-Z0-9_~./&]*?)(\\))?%\\})";
	private static final String KEY_URL_ENCODE_PREFIX = "{%(" + KEY_URL_ENCODE;
	private static final String KEY_URL_ENCODE_SUFFIX = ")%}";
	private static final int MIN_URL_ENC_TOKEN_LEN = KEY_URL_ENCODE_PREFIX.length() + KEY_URL_ENCODE_SUFFIX.length();

	private static final String KEY_EVENT_TYPE                 	= "~type";
	private static final String KEY_EVENT_SOURCE                = "~source";
	private static final String KEY_TIMESTAMP_UNIX              = "~timestampu";
	private static final String KEY_TIMESTAMP_ISO8601           = "~timestampz";
	private static final String KEY_TIMESTAMP_PLATFORM 			= "~timestampp";
	private static final String KEY_SDK_VERSION                 = "~sdkver";
	private static final String KEY_CACHEBUST                 	= "~cachebust";
	private static final String KEY_ALL_URL                 	= "~all_url";
	private static final String KEY_ALL_JSON                 	= "~all_json";
	private static final String KEY_SHARED_STATE                = "~state.";

	private final EventHub parentHub;
	private final Map<String, KeyFinder> specialKeyLookupTable;
	private final Pattern tokenPattern;

	// ========================================================
	// Constructor
	// ========================================================

	/**
	 * RuleTokenParser Constructor
	 *
	 * @param parentHub parent {@code EventHub} instance
	 */
	public RuleTokenParser(final EventHub parentHub) {
		this.parentHub = parentHub;
		this.specialKeyLookupTable = createSpecialKeyLookupTable();
		this.tokenPattern = Pattern.compile(TOKEN_REGEX);

	}

	// ========================================================
	// private methods
	// ========================================================

	/**
	 * Extracts the key from passed token.
	 * <p>
	 * For example, key extracted from a token of the given format {@literal {%myKey%}} shall be {@code myKey}
	 *
	 * @param token {@link String} containing the token
	 * @param urlEncode {@code boolean} the token contains url encoding keyword or not
	 *
	 * @return {@code String} containing the key in the passed token
	 */
	private String getKeyFromToken(final String token, final boolean urlEncode) {
		if (StringUtils.isNullOrEmpty(token)) {
			return token;
		}

		if (urlEncode) {
			return token.length() < MIN_URL_ENC_TOKEN_LEN ? token : token.substring(KEY_URL_ENCODE_PREFIX.length(),
					token.length() - KEY_URL_ENCODE_SUFFIX.length());
		} else {
			return token.length() < MIN_TOKEN_LEN ? token : token.substring(MIN_TOKEN_LEN / 2, token.length() - MIN_TOKEN_LEN / 2);
		}


	}

	/**
	 * Finds and returns all the valid tokens in the provided input.
	 * <p>
	 * A valid token follows the regex {@literal {%[a-zA-Z0-9_~./]*?%}}
	 *
	 * @param input the input {@link String} containing tokens to be parsed
	 *
	 * @return {@code List<String>} of valid tokens contained in the input string
	 */
	private List<String> findTokens(final String input) {
		List<String> tokenList = new ArrayList<String>();

		if (StringUtils.isNullOrEmpty(input)) {
			return tokenList;
		}

		Matcher matcher = tokenPattern.matcher(input);

		// check all occurrences
		while (matcher.find()) {
			String token = matcher.group(0);

			if (!StringUtils.isNullOrEmpty(token)) {
				tokenList.add(token);
			}
		}

		return tokenList;
	}

	// ========================================================
	// package-protected methods
	// ========================================================

	/**
	 * Returns the value for the {@code key} provided as input.
	 * <p>
	 * If the {@code key} is a special key recognized by SDK, the value is determined based on incoming {@link Event},
	 * or {@link EventHub#moduleSharedStates} data. Otherwise the key is searched in the current {@code Event}'s data
	 * and the corresponding value is returned.
	 *
	 * @param key {@link String} containing the key whose value needs to be determined
	 * @param event triggering {@link Event} instance
	 *
	 * @return {@code String} containing value to be substituted for the {@code key}
	 */
	String expandKey(final String key, final Event event) {
		if (StringUtils.isNullOrEmpty(key) || event == null) {
			return null;
		}


		if (key.startsWith(KEY_PREFIX)) { // special keys
			if (key.startsWith(KEY_SHARED_STATE) && (key.indexOf(SHARED_STATE_KEY_DELIMITER) > KEY_SHARED_STATE.length())) {
				return getSharedStateKey(key, event);
			} else if (specialKeyLookupTable.containsKey(key)) {
				return specialKeyLookupTable.get(key).find(event);
			}
		}

		// standard keys (lookup in eventData)
		final Map<String, Variant> eventDataMap = EventDataFlattener.getFlattenedDataMap(event.getData());

		if (!eventDataMap.containsKey(key)) {
			return null;

		}

		final Variant value = eventDataMap.get(key);

		if (value == null || value instanceof NullVariant) {
			return null;
		}

		try {
			return value.convertToString();
		} catch (VariantException ex) {
			// return empty string for map, vector
			return "";
		}
	}

	/**
	 * Finds and expands the tokens in the {@code sourceString}.
	 * <p>
	 * The token format is as follows {@literal {%key%}}, valid characters for key include [a-zA-Z0-9~_.]
	 * <p>
	 * If the token contains special {@code key} recognized by SDK, the value is determined based on incoming {@link Event},
	 * or {@link EventHub#moduleSharedStates} data. Otherwise the key contained in the token is searched in the current {@code Event}'s
	 * data and the corresponding value is used.
	 *
	 * @param sourceString input {@link String} containing tokens to be replaced
	 * @param event triggering {@link Event} instance
	 *
	 * @return {@code String} containing {@code sourceString} with the valid tokens replaced with the corresponding values
	 */
	String expandTokensForString(final String sourceString, final Event event) {
		if (StringUtils.isNullOrEmpty(sourceString) || event == null) {
			return sourceString;
		}

		List<String> tokenList = findTokens(sourceString);

		if (tokenList.isEmpty()) {
			return sourceString;
		}

		String outputString = sourceString;

		for (String token : tokenList) {
			boolean urlEncode = false;

			if (token.indexOf(KEY_URL_ENCODE, 2) > -1) {
				urlEncode = true;  // found "urlenc" keyword
			}

			final String key = getKeyFromToken(token, urlEncode);

			if (StringUtils.isNullOrEmpty(key)) {
				continue;
			}

			String replacementString = expandKey(key, event);

			if (StringUtils.isNullOrEmpty(replacementString)) {
				replacementString = "";
			}

			replacementString = urlEncode ? UrlUtils.urlEncode(replacementString) : replacementString;
			outputString = outputString.replace(token, replacementString);
		}

		return outputString;
	}

	// ========================================================
	// private getter methods
	// ========================================================

	/**
	 * Returns the value for shared state key specified by the {@code key}.
	 * <p>
	 * The key is provided in the format {@literal ~state.valid_shared_state_name/key}
	 * For example: {@literal ~state.com.adobe.marketing.mobile.Identity/mid}
	 *
	 * @param key {@link String} containing the key to search for in {@link EventHub#moduleSharedStates}
	 * @param event {@link Event} event that the state should be valid for
	 *
	 * @return {@code String} containing the value for the shared state key if valid, empty {@code String} otherwise
	 */
	private String getSharedStateKey(final String key, final Event event) {
		String sharedStateKeyString = key.substring(KEY_SHARED_STATE.length());

		if (StringUtils.isNullOrEmpty(sharedStateKeyString)) {
			return null;
		}

		int index = sharedStateKeyString.indexOf(SHARED_STATE_KEY_DELIMITER);

		if (index > -1 && sharedStateKeyString.length() > index) {
			final String sharedStateName = sharedStateKeyString.substring(0, index);
			final String sharedStateKeyName = sharedStateKeyString.substring(index + 1);

			final Map<String, Variant> sharedStateMap = EventDataFlattener.getFlattenedDataMap(parentHub.getSharedEventState(
						sharedStateName, event, null));

			if (sharedStateMap != null && !sharedStateMap.isEmpty() && sharedStateMap.containsKey(sharedStateKeyName)) {
				final Variant variant = sharedStateMap.get(sharedStateKeyName);

				try {
					return variant.convertToString();
				} catch (VariantException ex) {
					return null;
				}
			}
		}

		return null;
	}

	// ========================================================
	// special key lookup table utilities and creation
	// ========================================================

	/**
	 * Used to create special key lookup table
	 */
	interface KeyFinder {
		String find(Event e);
	}

	/**
	 * Creates a mapping of all supported special keys in rule {@code Matcher} condition keys to their getter {@code KeyFinder} objects.
	 *
	 * @return {@code Map<String, Method>} containing a mapping of rule condition special key types to their getter {@link Method} objects
	 */
	private Map<String, KeyFinder> createSpecialKeyLookupTable() {
		final HashMap<String, KeyFinder> lookupTable = new HashMap<String, KeyFinder>();
		final PlatformServices platformServices = parentHub.getPlatformServices();

		lookupTable.put(KEY_EVENT_TYPE, new KeyFinder() {
			@Override
			public String find(final Event e) {
				return e.getEventType().getName();
			}
		});
		lookupTable.put(KEY_EVENT_SOURCE, new KeyFinder() {
			@Override
			public String find(final Event e) {
				return e.getEventSource().getName();
			}
		});
		lookupTable.put(KEY_TIMESTAMP_UNIX, new KeyFinder() {
			@Override
			public String find(final Event e) {
				return String.valueOf(TimeUtils.getUnixTimeInSeconds());
			}
		});
		lookupTable.put(KEY_TIMESTAMP_ISO8601, new KeyFinder() {
			@Override
			public String find(final Event e) {
				String timestampz = null;

				if (platformServices != null) {
					timestampz = TimeUtils.getIso8601Date();
				}

				return timestampz;
			}
		});
		lookupTable.put(KEY_TIMESTAMP_PLATFORM, new KeyFinder() {
			@Override
			public String find(final Event e) {
				String timestampp = null;

				if (platformServices != null) {
					timestampp = TimeUtils.getIso8601DateTimeZoneISO8601();
				}

				return timestampp;
			}
		});
		lookupTable.put(KEY_SDK_VERSION, new KeyFinder() {
			@Override
			public String find(final Event e) {
				String sdkVersion = "unknown";

				if (platformServices != null) {
					final SystemInfoService systemInfoService = platformServices.getSystemInfoService();
					sdkVersion = systemInfoService.getCoreVersion();
				}

				return sdkVersion;
			}
		});
		lookupTable.put(KEY_CACHEBUST, new KeyFinder() {
			@Override
			public String find(final Event e) {
				return String.valueOf(new SecureRandom().nextInt(RANDOM_INT_BOUNDARY));
			}
		});
		lookupTable.put(KEY_ALL_JSON, new KeyFinder() {
			@Override
			public String find(final Event e) {
				if (e == null || e.getData() == null) {
					return "";
				}

				JsonUtilityService.JSONObject jsonObject = null;
				final PlatformServices platformServices = parentHub.getPlatformServices();

				if (platformServices != null) {
					final JsonUtilityService jsonUtilityService = platformServices.getJsonUtilityService();

					if (jsonUtilityService != null) {
						try {
							final Map<String, Variant> dataMap = EventDataFlattener.getFlattenedDataMap(e.getData());
							final Variant variant = Variant.fromVariantMap(dataMap);
							jsonObject = variant.getTypedObject(new JsonObjectVariantSerializer(
																	jsonUtilityService));
						} catch (Exception exception) {
							jsonObject = null;
						}
					}
				}

				return jsonObject == null ? "" : jsonObject.toString();
			}
		});
		lookupTable.put(KEY_ALL_URL, new KeyFinder() {
			@Override
			public String find(final Event e) {
				if (e == null || e.getData() == null) {
					return "";
				}

				final Map<String, Variant> eventDataAsObjectMap = EventDataFlattener.getFlattenedDataMap(e.getData());
				return com.adobe.marketing.mobile.UrlUtilities.serializeToQueryString(eventDataAsObjectMap);
			}
		});

		return lookupTable;
	}
}
