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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CollectionUtils {
	static final private String SUFFIX_FOR_EACH_OBJECT = "[*]";

	/**
	 * Merges the contents of fromMap into toMap.
	 * While merging it does not delete any keys with null value.
	 *
	 * @param toMap containing the higher priority data
	 * @param fromMap containing the lower priority data
	 * @return a Map<String, Variant> with the combined contents of toMap and fromMap
	 */
	static Map<String, Variant> addDataToMap(final Map<String, Variant> toMap, final Map<String, Variant> fromMap) {
		return addDataToMap(toMap, fromMap, false, false);
	}

	/**
	 * Merges the contents of fromMap into toMap
	 *
	 * While merging fromMap into toMap, this method will prioritize toMap for conflicting
	 * values. If the value type being processed is a VariantMapVariant type, this method will recurse.
	 * If fromMap is empty, this method will return toMap.
	 *
	 * @param toMap containing the higher priority data
	 * @param fromMap containing the lower priority data
	 * @param deleteIfEmpty a bool indicating whether a key should be removed from the toMap if its value is empty
	 * @param isToMapConsequenceData a bool indicating whether the toMap value is from triggered consequence.
	 *                if true means toMap may have the keys with suffix [*]
	 *                if false means fromMap may have the keys with suffix [*]
	 * @return a Map<String, Variant> with the combined contents of toMap and fromMap
	 */
	@SuppressWarnings("checkstyle:nestedifdepth")
	static Map<String, Variant> addDataToMap(final Map<String, Variant> toMap, final Map<String, Variant> fromMap,
			final boolean deleteIfEmpty, final boolean isToMapConsequenceData) {
		// quick return of toMap if fromMap is empty
		if (fromMap.isEmpty()) {
			return toMap;
		}

		final Map<String, Variant> returnMap = new HashMap<String, Variant>(toMap);

		for (final Map.Entry<String, Variant> entry : fromMap.entrySet()) {
			final String key = entry.getKey();
			final Variant value = entry.getValue();
			final VariantKind valueType = value.getKind();

			// adding support for deleting values from toMap if the key exists and is null
			if (deleteIfEmpty && keyExistsAndValueIsNull(key, toMap)) {
				returnMap.remove(key);
			} else if (VariantKind.NULL.equals(valueType)) {
				// do nothing
			} else if (VariantKind.MAP.equals(valueType)) {
				// get a reference to the new map for this key
				final Map<String, Variant> newFromMap = value.optVariantMap(new HashMap<String, Variant>());

				if (key.endsWith(SUFFIX_FOR_EACH_OBJECT)) {
					// this value is represented as a map, but the destination object is an array of objects
					// we will loop through each object in the array and add the contents of this object
					final String toMapKey = key.substring(0, key.length() - SUFFIX_FOR_EACH_OBJECT.length());

					List<Variant> listOfObjects = new ArrayList<Variant>();

					if (toMap.containsKey(toMapKey)) {
						try {
							listOfObjects = toMap.get(toMapKey).getVariantList();
						} catch (final VariantException ex) {
							// key exists in toMap but it's not a list type so we can't merge.  ignore value in fromMap.
							continue;
						}
					}

					final List<Variant> mergedListOfObjects = new ArrayList<Variant>();

					for (final Variant anonymousObject : listOfObjects) {
						if (!VariantKind.MAP.equals(anonymousObject.getKind())) {
							// we should only modify maps in this list. this object isn't a map, so put it in
							// mergedListOfObjects and move along.
							mergedListOfObjects.add(anonymousObject);
							continue;
						}

						final Map<String, Variant> objectAsVariantMap = anonymousObject.optVariantMap(null);
						Variant mergedVariantMap = Variant.fromVariantMap(addDataToMap(objectAsVariantMap, newFromMap, deleteIfEmpty,
												   isToMapConsequenceData));
						mergedListOfObjects.add(mergedVariantMap);
					}

					returnMap.put(toMapKey, Variant.fromVariantList(mergedListOfObjects));
				} else {
					// get a reference to our existing map for this key if it exists
					Map<String, Variant> newToMap = new HashMap<String, Variant>();

					if (toMap.containsKey(key)) {
						try {
							newToMap = toMap.get(key).getVariantMap();
						} catch (final VariantException ex) {
							// key exists in toMap but it's not a map type so we can't merge.  ignore value in fromMap.
							continue;
						}
					}

					// recursively fill in the map
					returnMap.put(key, Variant.fromVariantMap(addDataToMap(newToMap, newFromMap, deleteIfEmpty, isToMapConsequenceData)));
				}
			} else if (VariantKind.VECTOR.equals(valueType)) {
				List<Variant> fromList;

				try {
					fromList = value.getVariantList();
				} catch (VariantException e) {
					continue;
				}

				// check for a matching key with a suffix in to_map
				String keyWithSuffix = key + SUFFIX_FOR_EACH_OBJECT;

				// if it's a list and the toMap contains the consequence data, we need to check the toMap
				// for a key with suffix
				if (isToMapConsequenceData && toMap.containsKey(keyWithSuffix)) {
					// toMap contains an entry with the special notation, it's type should be map
					if (!VariantKind.MAP.equals(toMap.get(keyWithSuffix).getKind())) {
						// key exists in toMap, but it's not a map type so we can't merge. Ignore values in fromMap
						continue;
					}

					final Map<String, Variant> newToMap = toMap.get(keyWithSuffix).optVariantMap(new HashMap<String, Variant>());
					List<Variant> mergedListOfObjects = new ArrayList<Variant>();

					// add values from newToMap to each VariantKind.Map object contained in fromList
					for (final Variant anonymousObject : fromList) {
						if (!VariantKind.MAP.equals(anonymousObject.getKind())) {
							// this notation only supports lists of anonymous objects
							mergedListOfObjects.add(anonymousObject);
							continue;
						}

						final Map<String, Variant> objectAsVariantMap = anonymousObject.optVariantMap(null);
						Variant mergedVariantMap = Variant.fromVariantMap(addDataToMap(newToMap, objectAsVariantMap, deleteIfEmpty,
												   isToMapConsequenceData));
						mergedListOfObjects.add(mergedVariantMap);
					}

					returnMap.put(key, Variant.fromVariantList(mergedListOfObjects));
				} else {
					// get a reference to our existing list for this key
					List<Variant> newToList = new ArrayList<Variant>();

					if (toMap.containsKey(key)) {
						try {
							newToList = toMap.get(key).getVariantList();
						} catch (final VariantException ex) {
							// key exists in toMap but it's not a vector type so we can't merge.  ignore value in fromMap.
							continue;
						}
					}

					List<Variant> newFromList = value.optVariantList(new ArrayList<Variant>());

					// recursively fill in the list
					returnMap.put(key, Variant.fromVariantList(addDataToList(newToList, newFromList)));
				}
			} else {
				// don't replace an existing value
				if (!toMap.containsKey(key)) {
					returnMap.put(key, value);
				}
			}
		}

		return deleteIfEmpty ? deleteIfEmpty(returnMap) : returnMap;
	}

	/**
	 * Recursively loops through the provided map and removes any keys with null values
	 *
	 * @param map Map from which null valued keys need to be deleted
	 * @return map after deleting keys with null value
	 */
	public static Map<String, Variant> deleteIfEmpty(final Map<String, Variant> map) {
		if (map.isEmpty()) {
			return map;
		}

		final Map<String, Variant> returnMap = new HashMap<String, Variant>(map);

		for (final Map.Entry<String, Variant> entry : map.entrySet()) {
			final String key = entry.getKey();
			final Variant value = entry.getValue();
			final VariantKind valueType = value.getKind();

			if (VariantKind.NULL.equals(valueType)) {
				returnMap.remove(key);
			} else if (VariantKind.MAP.equals(valueType)) {
				Map<String, Variant> newToMap = new HashMap<String, Variant>();

				if (key.endsWith(SUFFIX_FOR_EACH_OBJECT)) {
					returnMap.remove(key);
					continue;
				}

				try {
					newToMap = map.get(key).getVariantMap();
				} catch (final VariantException ex) {
					// key exists in toMap but it's not a map type so we can't merge.  ignore value in fromMap.
					continue;
				}

				// recursively fill in the map
				returnMap.put(key, Variant.fromVariantMap(deleteIfEmpty(newToMap)));
			}
		}

		return returnMap;
	}

	/**
	 * Merges the contents of fromList into toList
	 *
	 * @param toList Higher priority list
	 * @param fromList Lower priority list
	 * @return a List<Variant> with the combined contents of toList and fromList
	 */
	static List<Variant> addDataToList(final List<Variant> toList, final List<Variant> fromList) {
		// quick return if there's nothing in fromList
		if (fromList.isEmpty()) {
			return toList;
		}

		List<Variant> returnList = new ArrayList<Variant>(toList);

		for (final Variant value : fromList) {
			final VariantKind valueType = value.getKind();

			if (VariantKind.NULL.equals(valueType)) {
				continue;
			} else if (VariantKind.MAP.equals(valueType) || VariantKind.VECTOR.equals(valueType)) {
				// we don't de-duplicate anonymous collections, so just add them
				returnList.add(value);
			} else {
				// don't add duplicates to the list
				// because we have to check values and we are using Variants, we have to loop the collection
				boolean valueExistsInList = false;

				for (final Variant inner : toList) {
					if (inner.equals(value)) {
						valueExistsInList = true;
						break;
					}
				}

				if (!valueExistsInList) {
					returnList.add(value);
				}
			}
		}

		return returnList;
	}

	static String getPrettyString(final Map<String, Variant> map, final int indentDepth) {
		if (map.isEmpty()) {
			return "{}";
		}

		StringBuilder returnString = new StringBuilder("{");
		final String NEWLINE = "\n";
		final String COMMA = ",";
		final String QUOTE = "\"";
		final int SPACES_PER_INDENT = 4;

		for (final Map.Entry<String, Variant> entry : map.entrySet()) {
			final String key = entry.getKey();
			final Variant value = entry.getValue();
			final VariantKind valueType = value.getKind();

			// add newline and whitespace.  add a comma if this isn't our first entry
			if (returnString.length() > 1) {
				returnString.append(COMMA);
			}

			returnString.append(NEWLINE);
			returnString.append(getWhitespace(SPACES_PER_INDENT * indentDepth));

			if (VariantKind.NULL == valueType) {
				returnString.append(QUOTE).append(key).append(QUOTE).append(" : null");
			} else if (VariantKind.STRING == valueType) {
				returnString.append(QUOTE).append(key).append(QUOTE).append(" : ").append(QUOTE).append(value.optString("")).append(
					QUOTE);
			} else if (VariantKind.INTEGER == valueType) {
				returnString.append(QUOTE).append(key).append(QUOTE).append(" : ").append(value.optInteger(0));
			} else if (VariantKind.LONG == valueType) {
				returnString.append(QUOTE).append(key).append(QUOTE).append(" : ").append(value.optLong(0));
			} else if (VariantKind.DOUBLE == valueType) {
				returnString.append(QUOTE).append(key).append(QUOTE).append(" : ").append(value.optDouble(0));
			} else if (VariantKind.BOOLEAN == valueType) {
				returnString.append(QUOTE).append(key).append(QUOTE).append(" : ").append(value.optBoolean(false));
			} else if (VariantKind.MAP == valueType) {
				final Map<String, Variant> newMap = value.optVariantMap(new HashMap<String, Variant>());

				if (newMap.size() <= 0) {
					// don't recurse if the map is empty
					returnString.append(QUOTE).append(key).append(QUOTE).append(" : { }");
				} else {
					// recursively get the string for this map
					returnString.append(QUOTE).append(key).append(QUOTE).append(" : ").append(getPrettyString(newMap, indentDepth + 1));
				}

			} else if (VariantKind.VECTOR == valueType) {
				// get a reference to the existing array for this key
				final List<Variant> newList = value.optVariantList(new ArrayList<Variant>());

				if (newList.size() <= 0) {
					// don't recurse if the list is empty
					returnString.append(QUOTE).append(key).append(QUOTE).append(" : [ ]");
				} else {
					// recursively get the string for this list
					returnString.append(QUOTE).append(key).append(QUOTE).append(" : ").append(getPrettyString(newList, indentDepth + 1));
				}
			}
		}

		returnString.append(NEWLINE);
		returnString.append(getWhitespace(SPACES_PER_INDENT * (indentDepth - 1)));
		returnString.append("}");

		return returnString.toString();
	}

	static String getPrettyString(final List<Variant> list, final int indentDepth) {
		if (list.isEmpty()) {
			return "[]";
		}

		StringBuilder returnString = new StringBuilder("[");
		final String NEWLINE = "\n";
		final String COMMA = ",";
		final String QUOTE = "\"";
		final int SPACES_PER_INDENT = 4;

		for (final Variant entry : list) {
			final Variant value = entry;
			final VariantKind valueType = value.getKind();

			// add newline and whitespace.  add a comma if this isn't our first entry
			if (returnString.length() > 1) {
				returnString.append(COMMA);
			}

			returnString.append(NEWLINE);
			returnString.append(getWhitespace(SPACES_PER_INDENT * indentDepth));

			if (VariantKind.NULL == valueType) {
				returnString.append("null");
			} else if (VariantKind.STRING == valueType) {
				returnString.append(QUOTE).append(value.optString("")).append(QUOTE);
			} else if (VariantKind.INTEGER == valueType) {
				returnString.append(value.optInteger(0));
			} else if (VariantKind.LONG == valueType) {
				returnString.append(value.optLong(0));
			} else if (VariantKind.DOUBLE == valueType) {
				returnString.append(value.optDouble(0));
			} else if (VariantKind.BOOLEAN == valueType) {
				returnString.append(value.optBoolean(false));
			} else if (VariantKind.MAP == valueType) {
				final Map<String, Variant> newMap = value.optVariantMap(new HashMap<String, Variant>());

				// recursively get the string for this map
				returnString.append(getPrettyString(newMap, indentDepth + 1));
			} else if (VariantKind.VECTOR == valueType) {
				// get a reference to the existing array for this key
				final List<Variant> newList = value.optVariantList(new ArrayList<Variant>());

				// recursively get the string for this list
				returnString.append(getPrettyString(newList, indentDepth + 1));
			}
		}

		returnString.append(NEWLINE);
		returnString.append(getWhitespace(SPACES_PER_INDENT * (indentDepth - 1)));
		returnString.append("]");

		return returnString.toString();
	}

	static String getWhitespace(final int numSpaces) {
		final StringBuilder whitespace = new StringBuilder();

		for (int i = 0; i < numSpaces; i++) {
			whitespace.append(" ");
		}

		return whitespace.toString();
	}

	/**
	 * Checks the given map to see if the provided key exists and is null
	 *
	 * @param key a string key to search for in the map
	 * @param map a Map<String, Variant> to be searched
	 * @return true if the key exists in the map and its Variant value has a kind of VariantKind.NULL
	 */
	static boolean keyExistsAndValueIsNull(final String key, final Map<String, Variant> map) {
		if (key != null && !key.isEmpty() && map.containsKey(key)) {
			// key exists in map
			Variant value = map.get(key);
			return value == null || value.getKind() == VariantKind.NULL;
		}

		return false;
	}
}
