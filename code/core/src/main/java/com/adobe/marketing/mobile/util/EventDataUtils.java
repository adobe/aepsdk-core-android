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

package com.adobe.marketing.mobile.util;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Utility to clone event data which is represented as {@code Map<String, Object>}. Currently
 * supports cloning values which are Boolean, Byte, Short, Integer, Long, Float, Double, BigDecimal,
 * BigInteger, Character, String, UUID, Maps and Collections.
 */
public class EventDataUtils {

    private static final int MAX_DEPTH = 256;

    private enum CloneMode {
        ImmutableContainer,
        MutableContainer,
    }

    private static final Set<Class<?>> immutableClasses;

    static {
        immutableClasses = new HashSet<>();
        immutableClasses.add(Boolean.class);
        immutableClasses.add(Byte.class);
        immutableClasses.add(Short.class);
        immutableClasses.add(Integer.class);
        immutableClasses.add(Long.class);
        immutableClasses.add(Float.class);
        immutableClasses.add(Double.class);
        immutableClasses.add(BigDecimal.class);
        immutableClasses.add(BigInteger.class);
        immutableClasses.add(Character.class);
        immutableClasses.add(String.class);
        immutableClasses.add(UUID.class);
    }

    private EventDataUtils() {}

    private static Object cloneObject(final Object obj, final CloneMode mode, final int depth)
            throws CloneFailedException {
        if (obj == null) {
            return null;
        }

        if (depth > MAX_DEPTH) {
            throw new CloneFailedException("Max depth reached");
        }

        if (immutableClasses.contains(obj.getClass())) {
            return obj;
        }

        if (obj instanceof Map) {
            return cloneMap((Map<?, ?>) obj, mode, depth);
        } else if (obj instanceof Collection) {
            return cloneCollection((Collection<?>) obj, mode, depth);
        } else if (obj.getClass().isArray()) {
            return cloneArray(obj, mode, depth);
        } else {
            throw new CloneFailedException("Object is of unsupported type");
        }
    }

    private static Map<String, Object> cloneMap(
            final Map<?, ?> map, final CloneMode mode, final int depth)
            throws CloneFailedException {
        if (map == null) return null;

        Map<String, Object> ret = new HashMap<>();
        for (Map.Entry<?, ?> kv : map.entrySet()) {
            Object key = kv.getKey();
            if (key != null && key instanceof String) {
                Object clonedValue = cloneObject(kv.getValue(), mode, depth + 1);
                ret.put(key.toString(), clonedValue);
            }
        }
        return mode == CloneMode.ImmutableContainer ? Collections.unmodifiableMap(ret) : ret;
    }

    private static Collection<Object> cloneCollection(
            final Collection<?> collection, final CloneMode mode, final int depth)
            throws CloneFailedException {
        if (collection == null) return null;

        List<Object> ret = new ArrayList<>();
        for (Object element : collection) {
            Object clonedElement = cloneObject(element, mode, depth + 1);
            ret.add(clonedElement);
        }
        return mode == CloneMode.ImmutableContainer ? Collections.unmodifiableList(ret) : ret;
    }

    private static Collection<Object> cloneArray(
            final Object array, final CloneMode mode, final int depth) throws CloneFailedException {
        if (array == null) return null;

        List<Object> ret = new ArrayList<>();

        int length = Array.getLength(array);
        for (int i = 0; i < length; ++i) {
            ret.add(cloneObject(Array.get(array, i), mode, depth + 1));
        }

        return mode == CloneMode.ImmutableContainer ? Collections.unmodifiableList(ret) : ret;
    }

    /**
     * Deep clones the provided map. Support cloning values which are basic types, maps and
     * collections. <br>
     * Values which are {@code Map<?, ?>} are cloned as {@code HashMap<String, Object>}.
     *
     * <ul>
     *   <li>Entry with null key is dropped.
     *   <li>Entry with non {@code String} key is converted to entry with {@code String} key by
     *       calling {@link Object#toString()} method.
     * </ul>
     *
     * Values which are {@code Collection<?>} are cloned as {@code ArrayList<Object>}.
     *
     * @param map map to be cloned
     * @return Cloned map
     * @throws CloneFailedException if object depth exceeds {@value EventDataUtils#MAX_DEPTH} or
     *     contains unsupported type.
     */
    public static Map<String, Object> clone(final Map<String, ?> map) throws CloneFailedException {
        return cloneMap(map, CloneMode.MutableContainer, 0);
    }

    /**
     * Deep clones the provided map. Support cloning values which are basic types, maps and
     * collections. <br>
     * Values which are {@code Map<?, ?>} are cloned as unmodifiable {@code HashMap<String,
     * Object>}.
     *
     * <ul>
     *   <li>Entry with null key is dropped.
     *   <li>Entry with non {@code String} key is converted to entry with {@code String} key by
     *       calling {@link Object#toString()} method.
     * </ul>
     *
     * Values which are {@code Collection<?>} are cloned as unmodifiable {@code ArrayList<Object>}.
     *
     * @param map map to be cloned
     * @return Cloned immutable map
     * @throws CloneFailedException if object depth exceeds {@value EventDataUtils#MAX_DEPTH} or
     *     contains unsupported type.
     */
    public static Map<String, Object> immutableClone(final Map<String, ?> map)
            throws CloneFailedException {
        return cloneMap(map, CloneMode.ImmutableContainer, 0);
    }

    /**
     * Casts generic map {@code Map<?, ?>} to {@code HashMap<String, Object>}
     *
     * <ul>
     *   <li>Entry with null and non {@code String} key is dropped.
     *   <li>Entry withnon {@code String} key is dropped
     * </ul>
     *
     * @param map map to be cast
     * @return map cast to type {@code Map<String, Object>}
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> castFromGenericType(final Map<?, ?> map) {
        if (map == null) return null;

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                return null;
            }
        }
        return (Map<String, Object>) map;
    }
}
