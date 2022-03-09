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
package com.adobe.marketing.mobile.internal.utility;

import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EventDataHelper {
    private static final String LOG_TAG = "EventDataHelper";
    private static final String SUFFIX_FOR_OBJECT = "[*]";

    @SuppressWarnings("unchecked")
    public static Map<String, Object> merge(final Map<String, Object> to, final Map<String, Object> from, final boolean overwrite) {
        return mergeMap(to, from, (key, toValue, fromValue) -> {
            if (!overwrite) {
                return toValue;
            }

            if (fromValue instanceof Map && toValue instanceof Map) {
                try {
                    Map<String, Object> fromMap = (Map<String, Object>) fromValue;
                    Map<String, Object> toMap = (Map<String, Object>) toValue;
                    return merge(toMap, fromMap, true);
                } catch (Exception e) {
                    MobileCore.log(LoggingMode.DEBUG, LOG_TAG, "Error casting value to type [Map<String, Object>]");
                }
            }

            if (fromValue instanceof Collection && toValue instanceof Collection) {
                try {
                    Collection<Object> fromCollection = (Collection<Object>) fromValue;
                    Collection<Object> toCollection = (Collection<Object>) toValue;
                    return mergeCollection(toCollection, fromCollection);
                } catch (Exception e) {
                    MobileCore.log(LoggingMode.DEBUG, LOG_TAG, "Error casting value to type [Collection<Object>]");
                }
            }

            return fromValue;
        });
    }

    private static Collection<Object> mergeCollection(final Collection<Object> to, final Collection<Object> from) {
        if (from == null || from.isEmpty()) {
            return to;
        }
        if (to == null || to.isEmpty()) {
            return from;
        }
        Collection<Object> result = new ArrayList<>(to);
        result.addAll(from);
        return result;
    }

    private static Object[] mergeArray(final Object[] to, final Object[] from) {
        if (from == null || from.length == 0) {
            return to;
        }
        int fromLength = from.length;
        int toLength = (to == null ? 0 : to.length);
        Object[] result = new Object[toLength + fromLength];
        if (to != null) {
            System.arraycopy(to, 0, result, 0, toLength);
        }
        System.arraycopy(from, 0, result, toLength, fromLength);
        return result;
    }

    private static Map<String, Object> mergeMap(final Map<String, Object> to, final Map<String, Object> from, final Overwrite overwriteStrategy) {
        if (from == null || from.isEmpty()) {
            return to;
        }
        final Map<String, Object> returnMap = new HashMap<>(to);
        for (final Map.Entry<String, Object> entry : from.entrySet()) {
            final String key = entry.getKey();
            final Object fromValue = entry.getValue();
            if (!returnMap.containsKey(key)) {
                returnMap.put(key, fromValue);
            } else {
                final Object toValue = returnMap.get(key);
                returnMap.put(key, overwriteStrategy.compare(key, toValue, fromValue));
            }
        }
        return returnMap;
    }

}

@FunctionalInterface
interface Overwrite {
    Object compare(final String key, final Object toObj, final Object fromObj);
}
