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

package com.adobe.marketing.mobile.internal.eventhub;

import static org.junit.Assert.assertThrows;

import androidx.annotation.NonNull;
import com.adobe.marketing.mobile.AdobeCallbackWithError;
import com.adobe.marketing.mobile.AdobeError;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventHistoryRequest;
import com.adobe.marketing.mobile.EventHistoryResult;
import com.adobe.marketing.mobile.EventHistoryResultHandler;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.SharedStateResolution;
import java.util.concurrent.CountDownLatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ExtensionContainerJavaTests {

    static TestExtension testExtensionInstance;

    private class TestExtension extends Extension {
        TestExtension(ExtensionApi api) {
            super(api);
            testExtensionInstance = this;
        }

        @NonNull @Override
        protected String getName() {
            return "TestExtension";
        }

        @NonNull public ExtensionApi getExtensionApi() {
            return super.getApi();
        }
    }

    @Mock EventHub eventHub;

    ExtensionContainer container;

    Event testEvent;

    @Before
    public void setup() throws InterruptedException {
        testEvent = new Event.Builder("event-name", "type", "source").build();

        EventHub.Companion.setShared(eventHub);

        CountDownLatch latch = new CountDownLatch(1);
        container =
                new ExtensionContainer(
                        TestExtension.class,
                        eventHubError -> {
                            latch.countDown();
                            return null;
                        });
        latch.await();
    }

    @Test
    public void testInvalidParams() {
        // Register event listener
        assertThrows(
                NullPointerException.class,
                () ->
                        testExtensionInstance
                                .getExtensionApi()
                                .registerEventListener(null, "source", e -> {}));
        assertThrows(
                NullPointerException.class,
                () ->
                        testExtensionInstance
                                .getExtensionApi()
                                .registerEventListener("type", null, e -> {}));
        assertThrows(
                NullPointerException.class,
                () ->
                        testExtensionInstance
                                .getExtensionApi()
                                .registerEventListener("type", "source", null));

        // Dispatch event
        assertThrows(
                NullPointerException.class,
                () -> testExtensionInstance.getExtensionApi().dispatch(null));

        // Create shared state
        assertThrows(
                NullPointerException.class,
                () -> testExtensionInstance.getExtensionApi().createSharedState(null, testEvent));

        // Get shared state
        assertThrows(
                NullPointerException.class,
                () ->
                        testExtensionInstance
                                .getExtensionApi()
                                .getSharedState(null, testEvent, false, SharedStateResolution.ANY));
        assertThrows(
                NullPointerException.class,
                () ->
                        testExtensionInstance
                                .getExtensionApi()
                                .getSharedState("test-extension", testEvent, false, null));

        // Create XDM shared state
        assertThrows(
                NullPointerException.class,
                () ->
                        testExtensionInstance
                                .getExtensionApi()
                                .createXDMSharedState(null, testEvent));

        // Get XDM shared state
        assertThrows(
                NullPointerException.class,
                () ->
                        testExtensionInstance
                                .getExtensionApi()
                                .getXDMSharedState(
                                        null, testEvent, false, SharedStateResolution.ANY));
        assertThrows(
                NullPointerException.class,
                () ->
                        testExtensionInstance
                                .getExtensionApi()
                                .getXDMSharedState("test-extension", testEvent, false, null));

        // Get Historical events
        assertThrows(
                NullPointerException.class,
                () ->
                        testExtensionInstance
                                .getExtensionApi()
                                .getHistoricalEvents(null, true, e -> {}));
        assertThrows(
                NullPointerException.class,
                () ->
                        testExtensionInstance
                                .getExtensionApi()
                                .getHistoricalEvents(
                                        new EventHistoryRequest[0],
                                        true,
                                        (EventHistoryResultHandler<Integer>) null));

        assertThrows(
                NullPointerException.class,
                () ->
                        testExtensionInstance
                                .getExtensionApi()
                                .getHistoricalEvents(
                                        null,
                                        true,
                                        new AdobeCallbackWithError<EventHistoryResult[]>() {
                                            @Override
                                            public void fail(AdobeError error) {}

                                            @Override
                                            public void call(EventHistoryResult[] value) {}
                                        }));
        assertThrows(
                NullPointerException.class,
                () ->
                        testExtensionInstance
                                .getExtensionApi()
                                .getHistoricalEvents(
                                        new EventHistoryRequest[0],
                                        true,
                                        (AdobeCallbackWithError<EventHistoryResult[]>) null));

        // Record Historical events
        assertThrows(
                NullPointerException.class,
                () ->
                        testExtensionInstance
                                .getExtensionApi()
                                .recordHistoricalEvent(
                                        null,
                                        new AdobeCallbackWithError<Boolean>() {
                                            @Override
                                            public void fail(AdobeError error) {}

                                            @Override
                                            public void call(Boolean value) {}
                                        }));

        assertThrows(
                NullPointerException.class,
                () ->
                        testExtensionInstance
                                .getExtensionApi()
                                .recordHistoricalEvent(testEvent, null));
    }
}
