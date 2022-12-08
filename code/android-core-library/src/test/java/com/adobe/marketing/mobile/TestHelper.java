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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TestHelper {

    /**
     * Verifies that an utility class is well defined.
     *
     * @param clazz utility class to verify.
     */
    public static void assertUtilityClassWellDefined(final Class<?> clazz)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException,
                    IllegalAccessException {
        assertTrue("Class must be final", Modifier.isFinal(clazz.getModifiers()));
        assertEquals(
                "There must be only one constructor", 1, clazz.getDeclaredConstructors().length);

        final Constructor<?> constructor = clazz.getDeclaredConstructor();

        if (constructor.isAccessible() || !Modifier.isPrivate(constructor.getModifiers())) {
            fail("Constructor is not private");
        }

        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);

        for (final Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())
                    && method.getDeclaringClass().equals(clazz)) {
                fail("There exists a non-static method:" + method);
            }
        }
    }
}
