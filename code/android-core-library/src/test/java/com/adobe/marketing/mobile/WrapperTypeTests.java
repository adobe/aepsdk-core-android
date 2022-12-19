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

import org.junit.Test;

public class WrapperTypeTests {

    @Test
    public void wrapperFromString() {
        assertEquals(WrapperType.FLUTTER, WrapperType.fromString("F"));
        assertEquals(WrapperType.REACT_NATIVE, WrapperType.fromString("R"));
        assertEquals(WrapperType.UNITY, WrapperType.fromString("U"));
        assertEquals(WrapperType.CORDOVA, WrapperType.fromString("C"));
        assertEquals(WrapperType.XAMARIN, WrapperType.fromString("X"));
        assertEquals(WrapperType.NONE, WrapperType.fromString("N"));
    }

    @Test
    public void wrapperFriendlyName() {
        assertEquals(WrapperType.FLUTTER.getFriendlyName(), "Flutter");
        assertEquals(WrapperType.REACT_NATIVE.getFriendlyName(), "React Native");
        assertEquals(WrapperType.UNITY.getFriendlyName(), "Unity");
        assertEquals(WrapperType.CORDOVA.getFriendlyName(), "Cordova");
        assertEquals(WrapperType.XAMARIN.getFriendlyName(), "Xamarin");
        assertEquals(WrapperType.NONE.getFriendlyName(), "None");
    }

    @Test
    public void wrapperFromString_defaultNone() {
        assertEquals(WrapperType.NONE, WrapperType.fromString("Invalid"));
        assertEquals(WrapperType.NONE, WrapperType.fromString(""));
        assertEquals(WrapperType.NONE, WrapperType.fromString(null));
    }
}
