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
import static org.junit.Assert.assertNotNull;

import com.adobe.marketing.mobile.signal.internal.SignalExtension;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SignalAPITest {

    @Test
    public void test_extensionVersion() {
        assertEquals("2.0.1", Signal.extensionVersion());
    }

    @Test
    public void test_extensionClass() {
        assertEquals(SignalExtension.class, Signal.EXTENSION);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void test_registerExtension() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            // mock MobileCore.registerExtension()
            ArgumentCaptor<Class> extensionClassCaptor = ArgumentCaptor.forClass(Class.class);
            ArgumentCaptor<ExtensionErrorCallback> callbackCaptor =
                    ArgumentCaptor.forClass(ExtensionErrorCallback.class);
            mobileCoreMockedStatic
                    .when(
                            () ->
                                    MobileCore.registerExtension(
                                            extensionClassCaptor.capture(),
                                            callbackCaptor.capture()))
                    .thenReturn(true);
            // call registerExtension() API
            Signal.registerExtension();
            // verify: happy
            assertNotNull(callbackCaptor.getValue());
            assertEquals(SignalExtension.class, extensionClassCaptor.getValue());
            // verify: error callback was called
            callbackCaptor.getValue().error(ExtensionError.UNEXPECTED_ERROR);
        }
    }
}
