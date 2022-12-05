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

import java.util.List;
import java.util.Map;

/**
 * Utility to read data from {@code Event} data which is represented as {@code Map<String, Object>}
 * in a type safe way.
 *
 * <p>The value of an {@code Event} data key can be obtained in multiple ways:
 *
 * <ul>
 *   <li>The {@code DataReader.optXyz(...)} methods are typically the best choice for users. These
 *       methods return the value as an {@code xyz}. If the value is missing or is not an {@code
 *       xyz}, the method will return a default value. Implicit conversions between types are not
 *       performed, except between numeric types. No implicit conversions even between numeric types
 *       are performed when reading as {@code Map} or {@code List}.
 *   <li>The {@code DataReader.getXyz(...)} methods return the value as an {@code xyz}. If the value
 *       is missing or is not an {@code xyz} the method will throw. Implicit conversions between
 *       types are not performed, except between numeric types. No implicit conversions even between
 *       numeric types are performed when reading as {@code Map} or {@code List}.
 * </ul>
 */
public class DataReader {

    private DataReader() {}

    private static boolean checkOverflow(final Class clazz, final Number n) {
        if (Double.class.equals(clazz)) {
            return false;
        } else if (Float.class.equals(clazz)) {
            if (n instanceof Double) {
                double valAsDouble = n.doubleValue();
                return valAsDouble < Float.MIN_VALUE || valAsDouble > Float.MAX_VALUE;
            }
            return false;
        } else if (Long.class.equals(clazz)) {
            if (n instanceof Double || n instanceof Float) {
                double valAsDouble = n.doubleValue();
                return valAsDouble < Long.MIN_VALUE || valAsDouble > Long.MAX_VALUE;
            }
            return false;
        } else if (Integer.class.equals(clazz)) {
            if (n instanceof Double || n instanceof Float) {
                double valAsDouble = n.doubleValue();
                return valAsDouble < Integer.MIN_VALUE || valAsDouble > Integer.MAX_VALUE;
            } else {
                long valAsLong = n.longValue();
                return valAsLong < Integer.MIN_VALUE || valAsLong > Integer.MAX_VALUE;
            }
        } else if (Short.class.equals(clazz)) {
            if (n instanceof Double || n instanceof Float) {
                double valAsDouble = n.doubleValue();
                return valAsDouble < Short.MIN_VALUE || valAsDouble > Short.MAX_VALUE;
            } else {
                long valAsLong = n.longValue();
                return valAsLong < Short.MIN_VALUE || valAsLong > Short.MAX_VALUE;
            }
        } else if (Byte.class.equals(clazz)) {
            if (n instanceof Double || n instanceof Float) {
                double valAsDouble = n.doubleValue();
                return valAsDouble < Byte.MIN_VALUE || valAsDouble > Byte.MAX_VALUE;
            } else {
                long valAsLong = n.longValue();
                return valAsLong < Byte.MIN_VALUE || valAsLong > Byte.MAX_VALUE;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private static <T> T castObject(final Class<T> tClass, final Object obj)
            throws DataReaderException {
        if (obj == null) {
            return null;
        }

        try {
            if (Number.class.isAssignableFrom(tClass) && obj instanceof Number) {
                Number objAsNumber = (Number) obj;

                if (DataReader.checkOverflow(tClass, objAsNumber)) {
                    throw new DataReaderException("Value overflows type " + tClass);
                }

                if (Byte.class.equals(tClass)) {
                    return (T) Byte.valueOf(objAsNumber.byteValue());
                } else if (Short.class.equals(tClass)) {
                    return (T) Short.valueOf(objAsNumber.shortValue());
                } else if (Integer.class.equals(tClass)) {
                    return (T) Integer.valueOf(objAsNumber.intValue());
                } else if (Long.class.equals(tClass)) {
                    return (T) Long.valueOf(objAsNumber.longValue());
                } else if (Double.class.equals(tClass)) {
                    return (T) Double.valueOf(objAsNumber.doubleValue());
                } else if (Float.class.equals(tClass)) {
                    return (T) Float.valueOf(objAsNumber.floatValue());
                }
            } else if (String.class.equals(tClass) && obj instanceof String) {
                return (T) obj;
            } else {
                return tClass.cast(obj);
            }
        } catch (ClassCastException ex) {
            throw new DataReaderException(ex);
        }

        return null;
    }

    /**
     * Gets the value for {@code key} from {@code map} as a custom object.
     *
     * @param <T> Custom type
     * @param tClass Custom class
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @return {@code T} value associated with {@code key} or null if {@code key} is not present in
     *     {@code map}
     * @throws DataReaderException if value is not gettable as a {@code T}
     */
    private static <T> T getTypedObject(
            final Class<T> tClass, final Map<String, ?> map, final String key)
            throws DataReaderException {
        if (map == null || key == null) {
            throw new DataReaderException("Map or key is null");
        }

        Object value = map.get(key);
        return castObject(tClass, value);
    }

    /**
     * Gets the value for {@code key} from {@code map} as a custom object or returns default value
     *
     * @param <T> Custom type
     * @param tClass Custom class
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @param fallback {@code T} value to return in case of failure. Can be null.
     * @return {@code T} value associated with {@code key}, or {@code fallback} if value is not
     *     gettable as a {@code T}
     */
    private static <T> T optTypedObject(
            final Class<T> tClass, final Map<String, ?> map, final String key, final T fallback) {
        T ret = null;
        try {
            ret = getTypedObject(tClass, map, key);
        } catch (DataReaderException ex) {
        }
        return ret != null ? ret : fallback;
    }

    /**
     * Gets the value for {@code key} from {@code map} as a {@code Map<String, T>}
     *
     * @param <T> Custom type
     * @param tClass Custom class
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @return {@code Map<String, T>} Map associated with {@code key} or null if {@code key} is not
     *     present in {@code map}
     * @throws DataReaderException if value is not gettable as a {@code Map<String,T>}
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> getTypedMap(
            final Class<T> tClass, final Map<String, ?> map, final String key)
            throws DataReaderException {
        if (tClass == null) {
            throw new DataReaderException("Class type is null");
        }

        if (map == null || key == null) {
            throw new DataReaderException("Map or key is null");
        }

        Object value = map.get(key);
        if (value == null) {
            return null;
        }

        if (!(value instanceof Map)) {
            throw new DataReaderException("Value is not a map");
        }

        Map<?, ?> valueAsMap = (Map<?, ?>) value;
        for (Map.Entry<?, ?> kv : valueAsMap.entrySet()) {
            if (!(kv.getKey() instanceof String)) {
                throw new DataReaderException("Map entry is not of expected type");
            }
            if (kv.getValue() != null && !tClass.isInstance(kv.getValue())) {
                throw new DataReaderException("Map entry is not of expected type");
            }
        }
        return (Map<String, T>) valueAsMap;
    }

    /**
     * Gets the value for {@code key} from {@code map} as a {@code Map<String, T>} or returns
     * default value
     *
     * @param <T> Custom type
     * @param tClass Custom class
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @param fallback {@code Map<String, T>} value to return in case of failure. Can be null.
     * @return {@code Map<String, T>} Map associated with {@code key}, or {@code fallback} if value
     *     is not gettable as a {@code Map<String, T>}
     */
    public static <T> Map<String, T> optTypedMap(
            final Class<T> tClass,
            final Map<String, ?> map,
            final String key,
            final Map<String, T> fallback) {
        Map<String, T> ret = null;
        try {
            ret = getTypedMap(tClass, map, key);
        } catch (DataReaderException ex) {
        }
        return ret != null ? ret : fallback;
    }

    /**
     * Gets the value for {@code key} from {@code map} as a {@code List<T>}
     *
     * @param <T> Custom type
     * @param tClass Custom class
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @return {@code List<T>} List associated with {@code key} or null if {@code key} is not
     *     present in {@code map}
     * @throws DataReaderException if value is not gettable as a {@code List<T>}
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getTypedList(
            final Class<T> tClass, final Map<String, ?> map, final String key)
            throws DataReaderException {
        if (tClass == null) {
            throw new DataReaderException("Class type is null");
        }

        if (map == null || key == null) {
            throw new DataReaderException("Map or key is null");
        }

        Object value = map.get(key);
        if (value == null) {
            return null;
        }

        if (!(value instanceof List)) {
            throw new DataReaderException("Value is not a list");
        }

        List<?> valueAsList = (List<?>) value;
        for (Object obj : valueAsList) {
            if (!tClass.isInstance(obj)) {
                throw new DataReaderException("List entry is not of expected type");
            }
        }

        return (List<T>) valueAsList;
    }

    /**
     * Gets the value for {@code key} from {@code map} as a {@code List<T>} or returns default value
     *
     * @param <T> Custom type
     * @param tClass Custom class
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @param fallback {@code List<T>} value to return in case of failure. Can be null.
     * @return {@code List<T>} List associated with {@code key}, or {@code fallback} if value is not
     *     gettable as a {@code List<T>}
     */
    public static <T> List<T> optTypedList(
            final Class<T> tClass,
            final Map<String, ?> map,
            final String key,
            final List<T> fallback) {
        List<T> ret = null;
        try {
            ret = getTypedList(tClass, map, key);
        } catch (DataReaderException ex) {
        }
        return ret != null ? ret : fallback;
    }

    /**
     * Gets the value for {@code key} from {@code map} as a {@code List<Map<String, T>>}
     *
     * @param <T> Custom type
     * @param tClass Custom class
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @return {@code List<Map<String, T>>} List associated with {@code key} or null if {@code key}
     *     is not present in {@code map}
     * @throws DataReaderException if value is not gettable as a {@code List<Map<String, T>>}
     */
    @SuppressWarnings("unchecked")
    public static <T> List<Map<String, T>> getTypedListOfMap(
            final Class<T> tClass, final Map<String, ?> map, final String key)
            throws DataReaderException {
        if (tClass == null) {
            throw new DataReaderException("Class type is null");
        }

        if (map == null || key == null) {
            throw new DataReaderException("Map or key is null");
        }

        Object value = map.get(key);
        if (value == null) {
            return null;
        }

        if (!(value instanceof List)) {
            throw new DataReaderException("Value is not a list");
        }

        List<?> valueAsList = (List<?>) value;
        for (Object obj : valueAsList) {
            if (!(obj instanceof Map)) {
                throw new DataReaderException("List entry is not of expected type");
            }

            Map<?, ?> objAsMap = (Map<?, ?>) obj;
            for (Map.Entry<?, ?> kv : objAsMap.entrySet()) {
                if (!(kv.getKey() instanceof String)) {
                    throw new DataReaderException("Map entry is not of expected type");
                }
                if (kv.getValue() != null && !tClass.isInstance(kv.getValue())) {
                    throw new DataReaderException("Map entry is not of expected type");
                }
            }
        }

        return (List<Map<String, T>>) valueAsList;
    }

    /**
     * Gets the value for {@code key} from {@code map} as a {@code List<Map<String, T>>} or returns
     * default value
     *
     * @param <T> Custom type
     * @param tClass Custom class
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @param fallback {@code List<Map<String, T>>} value to return in case of failure. Can be null.
     * @return {@code List<Map<String, T>>} List associated with {@code key}, or {@code fallback} if
     *     value is not gettable as a {@code List<Map<String, T>>}
     */
    public static <T> List<Map<String, T>> optTypedListOfMap(
            final Class<T> tClass,
            final Map<String, ?> map,
            final String key,
            final List<Map<String, T>> fallback) {
        List<Map<String, T>> ret = null;
        try {
            ret = getTypedListOfMap(tClass, map, key);
        } catch (DataReaderException ex) {
        }
        return ret != null ? ret : fallback;
    }

    /**
     * Gets the value for {@code key} from {@code map} as a {@code boolean}
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @return {@code boolean} value associated with {@code key}
     * @throws DataReaderException if value is not gettable as a {@code boolean} or if {@code key}
     *     is not present in {@code map}
     */
    public static boolean getBoolean(final Map<String, ?> map, final String key)
            throws DataReaderException {
        Boolean ret = getTypedObject(Boolean.class, map, key);
        if (ret == null) {
            throw new DataReaderException("Map contains null value for key");
        }
        return ret;
    }

    /**
     * Gets the value for {@code key} from {@code map} as a {@code boolean} or returns default value
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @param fallback {@code boolean} value to return in case of failure. Can be null.
     * @return {@code boolean} value associated with {@code key}, or {@code fallback} if value is
     *     not gettable as a {@code boolean}
     */
    public static boolean optBoolean(
            final Map<String, ?> map, final String key, final boolean fallback) {
        return optTypedObject(Boolean.class, map, key, fallback);
    }

    /**
     * Gets the value for {@code key} from {@code map} as an {@code int}
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @return {@code int} value associated with {@code key}
     * @throws DataReaderException if value is not gettable as an {@code int} or if {@code key} is
     *     not present in {@code map}
     */
    public static int getInt(final Map<String, ?> map, final String key)
            throws DataReaderException {
        Integer ret = getTypedObject(Integer.class, map, key);
        if (ret == null) {
            throw new DataReaderException("Map contains null value for key");
        }
        return ret;
    }

    /**
     * Gets the value for {@code key} from {@code map} as an {@code int} or returns default value
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @param fallback {@code int} value to return in case of failure. Can be null.
     * @return {@code int} value associated with {@code key}, or {@code fallback} if value is not
     *     gettable as a {@code int}
     */
    public static int optInt(final Map<String, ?> map, final String key, final int fallback) {
        return optTypedObject(Integer.class, map, key, fallback);
    }

    /**
     * Gets the value for {@code key} from {@code map} as an {@code long}
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @return {@code long} value associated with {@code key}
     * @throws DataReaderException if value is not gettable as an {@code long} or if {@code key} is
     *     not present in {@code map}
     */
    public static long getLong(final Map<String, ?> map, final String key)
            throws DataReaderException {
        Long ret = getTypedObject(Long.class, map, key);
        if (ret == null) {
            throw new DataReaderException("Map contains null value for key");
        }
        return ret;
    }

    /**
     * Gets the value for {@code key} from {@code map} as an {@code long} or returns default value
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @param fallback {@code long} value to return in case of failure. Can be null.
     * @return {@code long} value associated with {@code key}, or {@code fallback} if value is not
     *     gettable as a {@code long}
     */
    public static long optLong(final Map<String, ?> map, final String key, final long fallback) {
        return optTypedObject(Long.class, map, key, fallback);
    }

    /**
     * Gets the value for {@code key} from {@code map} as an {@code float}
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @return {@code float} value associated with {@code key}
     * @throws DataReaderException if value is not gettable as an {@code float} or if {@code key} is
     *     not present in {@code map}
     */
    public static float getFloat(final Map<String, ?> map, final String key)
            throws DataReaderException {
        Float ret = getTypedObject(Float.class, map, key);
        if (ret == null) {
            throw new DataReaderException("Map contains null value for key");
        }
        return ret;
    }

    /**
     * Gets the value for {@code key} from {@code map} as an {@code float} or returns default value
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @param fallback {@code float} value to return in case of failure. Can be null.
     * @return {@code float} value associated with {@code key}, or {@code fallback} if value is not
     *     gettable as a {@code float}
     */
    public static float optFloat(final Map<String, ?> map, final String key, final float fallback) {
        return optTypedObject(Float.class, map, key, fallback);
    }

    /**
     * Gets the value for {@code key} from {@code map} as an {@code double}
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @return {@code double} value associated with {@code key}
     * @throws DataReaderException if value is not gettable as an {@code double} or if {@code key}
     *     is not present in {@code map}
     */
    public static double getDouble(final Map<String, ?> map, final String key)
            throws DataReaderException {
        Double ret = getTypedObject(Double.class, map, key);
        if (ret == null) {
            throw new DataReaderException("Map contains null value for key");
        }
        return ret;
    }

    /**
     * Gets the value for {@code key} from {@code map} as an {@code double} or returns default value
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @param fallback {@code double} value to return in case of failure. Can be null.
     * @return {@code double} value associated with {@code key}, or {@code fallback} if value is not
     *     gettable as a {@code double}
     */
    public static double optDouble(
            final Map<String, ?> map, final String key, final double fallback) {
        return optTypedObject(Double.class, map, key, fallback);
    }

    /**
     * Gets the value for {@code key} from {@code map} as an {@code String}
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @return {@code String} value associated with {@code key} or null if {@code key} is not
     *     present in {@code map}
     * @throws DataReaderException if value is not gettable as an {@code String}
     */
    public static String getString(final Map<String, ?> map, final String key)
            throws DataReaderException {
        return getTypedObject(String.class, map, key);
    }

    /**
     * Gets the value for {@code key} from {@code map} as a {@code String} or returns default value
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @param fallback {@code String} value to return in case of failure. Can be null.
     * @return {@code String} value associated with {@code key}, or {@code fallback} if value is not
     *     gettable as a {@code String}
     */
    public static String optString(
            final Map<String, ?> map, final String key, final String fallback) {
        return optTypedObject(String.class, map, key, fallback);
    }

    /**
     * Gets the value for {@code key} from {@code map} as a {@code Map<String, String>}
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @return {@code Map<String, String>} Map associated with {@code key} or null if {@code key} is
     *     not present in {@code map}
     * @throws DataReaderException if value is not gettable as a {@code Map<String, String>}
     */
    public static Map<String, String> getStringMap(final Map<String, ?> map, final String key)
            throws DataReaderException {
        return getTypedMap(String.class, map, key);
    }

    /**
     * Gets the value for {@code key} from {@code map} as a {@code Map<String, String>} or returns
     * default value
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @param fallback {@code Map<String, String>} value to return in case of failure. Can be null.
     * @return {@code Map<String, String>} value associated with {@code key}, or {@code fallback} if
     *     value is not gettable as a {@code Map<String, String>}
     */
    public static Map<String, String> optStringMap(
            final Map<String, ?> map, final String key, final Map<String, String> fallback) {
        return optTypedMap(String.class, map, key, fallback);
    }

    /**
     * Gets the value for {@code key} from {@code map} as a {@code List<String>}
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @return {@code List<String>} List associated with {@code key} or null if {@code key} is not
     *     present in {@code map}
     * @throws DataReaderException if value is not gettable as a {@code List<String>}
     */
    public static List<String> getStringList(final Map<String, ?> map, final String key)
            throws DataReaderException {
        return getTypedList(String.class, map, key);
    }

    /**
     * Gets the value for {@code key} from {@code map} as a {@code List<String>} or returns default
     * value
     *
     * @param map {@code Map} map to fetch data
     * @param key {@code String} key to fetch
     * @param fallback {@code List<String>} value to return in case of failure. Can be null.
     * @return {@code List<String>} List associated with {@code key}, or {@code fallback} if value
     *     is not gettable as a {@code List<String>}
     */
    public static List<String> optStringList(
            final Map<String, ?> map, final String key, final List<String> fallback) {
        return optTypedList(String.class, map, key, fallback);
    }
}
