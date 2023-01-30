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
import static org.mockito.Mockito.mock;

import android.app.Application;
import com.adobe.marketing.mobile.extensions.Sample1;
import com.adobe.marketing.mobile.extensions.Sample1Kt;
import com.adobe.marketing.mobile.extensions.Sample2;
import com.adobe.marketing.mobile.extensions.Sample2Extension;
import com.adobe.marketing.mobile.extensions.Sample2Kt;
import com.adobe.marketing.mobile.extensions.Sample2KtExtension;
import com.adobe.marketing.mobile.internal.eventhub.EventHub;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MobileCoreRegistrationJavaTests {

    @Before
    public void setup() {
        MobileCore.sdkInitializedWithContext = new AtomicBoolean(false);
        EventHub.Companion.setShared(new EventHub());
    }

    @After
    public void cleanup() {
        EventHub.Companion.getShared().shutdown();
    }

    @Test
    public void testScenario1() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        Set<String> capturedIds = new HashSet<>();
        AdobeCallbackWithError<String> callback =
                new AdobeCallbackWithError<String>() {
                    @Override
                    public void fail(AdobeError error) {
                        latch.countDown();
                    }

                    @Override
                    public void call(String value) {
                        capturedIds.add(value);
                        latch.countDown();
                    }
                };

        //// SDK Initialization
        MobileCore.setApplication(mock(Application.class));
        List<Class<? extends Extension>> extensions = Arrays.asList(Sample1.class, Sample1Kt.class);
        MobileCore.registerExtensions(
                extensions,
                value -> {
                    Sample1.getTrackingIdentifier(callback);
                    Sample1Kt.getTrackingIdentifier(callback);
                });

        assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
        assertEquals(new HashSet<String>(Arrays.asList("Sample1_ID", "Sample1Kt_ID")), capturedIds);
    }

    @Test
    public void testScenario2() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        Set<String> capturedIds = new HashSet<>();
        AdobeCallbackWithError<String> callback =
                new AdobeCallbackWithError<String>() {
                    @Override
                    public void fail(AdobeError error) {
                        latch.countDown();
                    }

                    @Override
                    public void call(String value) {
                        capturedIds.add(value);
                        latch.countDown();
                    }
                };

        //// SDK Initialization
        MobileCore.setApplication(mock(Application.class));
        List<Class<? extends Extension>> extensions =
                Arrays.asList(Sample2Extension.class, Sample2KtExtension.class);

        MobileCore.registerExtensions(
                extensions,
                value -> {
                    Sample2.getTrackingIdentifier(callback);
                    Sample2Kt.getTrackingIdentifier(callback);
                });

        assertTrue(latch.await(100000, TimeUnit.MILLISECONDS));
        assertEquals(new HashSet<>(Arrays.asList("Sample2_ID", "Sample2Kt_ID")), capturedIds);
    }
}
