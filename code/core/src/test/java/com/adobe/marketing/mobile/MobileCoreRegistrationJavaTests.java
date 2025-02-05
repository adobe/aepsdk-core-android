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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import android.app.Application;
import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MobileCoreRegistrationJavaTests {

    private class MockExtension extends Extension {

        MockExtension(ExtensionApi extensionApi) {
            super(extensionApi);
        }

        @NonNull @Override
        protected String getName() {
            return "MockExtension";
        }
    }

    @Before
    public void setup() {
        MobileCore.resetSDK();
    }

    @Test
    public void testExtensionRegistration() throws InterruptedException {
        MobileCore.setApplication(mock(Application.class));
        List<Class<? extends Extension>> extensions = Arrays.asList(MockExtension.class);

        CountDownLatch latch = new CountDownLatch(1);
        MobileCore.registerExtensions(extensions, value -> latch.countDown());

        assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
    }
}
