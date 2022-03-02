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

package com.adobe.marketing.mobile.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ObjectUtils {
    private static final int MAX_DEPTH = 256;

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

    /**
     * Deep clones the object. This method is used for cloning event data and shared states. It
     * only supports cloning basic types, collections and maps.
     *
     * @param obj object to be cloned
     * @return cloned object
     * @throws CloneFailedException if object depth exceeds {@value ObjectUtils#MAX_DEPTH} or contains unsupported type.
     */
    public static Object deepClone(Object obj) throws CloneFailedException {
        return deepClone(obj, 0);
    }

    private static Object deepClone(Object obj, int depth) throws CloneFailedException {
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
            return cloneMap((Map<?, ?>) obj, depth);
        } else if (obj instanceof Collection) {
            return cloneCollection((Collection<?>) obj, depth);
        }else {
            throw new CloneFailedException("Object is of unsupported type");
        }
    }

    private static Map<Object, Object> cloneMap(Map<?, ?> map, int depth) throws CloneFailedException {
        Map<Object, Object> ret = new HashMap<>();
        for (Map.Entry<?, ?> kv : map.entrySet()) {
            Object clonedKey = deepClone(kv.getKey(), depth + 1);
            Object clonedValue = deepClone(kv.getValue(), depth + 1);
            if (clonedKey != null) {
                ret.put(clonedKey, clonedValue);
            }
        }
        return ret;
    }

    private static Collection<Object> cloneCollection(Collection<?> collection, int depth) throws CloneFailedException {
        Collection<Object> ret = new ArrayList<>();
        for (Object element : collection) {
            Object clonedElement = deepClone(element, depth + 1);
            ret.add(clonedElement);
        }
        return ret;
    }
}