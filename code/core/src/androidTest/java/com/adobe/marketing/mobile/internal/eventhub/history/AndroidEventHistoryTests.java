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

package com.adobe.marketing.mobile.internal.eventhub.history;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventHistoryRequest;
import com.adobe.marketing.mobile.EventHistoryResultHandler;
import com.adobe.marketing.mobile.TestUtils;
import com.adobe.marketing.mobile.services.MockAppContextService;
import com.adobe.marketing.mobile.services.ServiceProviderModifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AndroidEventHistoryTests {

    private AndroidEventHistory androidEventHistory;
    private HashMap<String, Object> data;
    private static final String DATABASE_NAME = "com.adobe.module.core.eventhistory";

    @Before
    public void beforeEach() {
        Context context = ApplicationProvider.getApplicationContext();

        MockAppContextService mockAppContextService = new MockAppContextService();
        mockAppContextService.appContext = context;
        ServiceProviderModifier.setAppContextService(mockAppContextService);

        TestUtils.deleteAllFilesInCacheDir(context);
        context.getApplicationContext().getDatabasePath(DATABASE_NAME).delete();

        try {
            androidEventHistory = new AndroidEventHistory();
        } catch (EventHistoryDatabaseCreationException e) {
            fail(e.getLocalizedMessage());
        }

        data =
                new HashMap<String, Object>() {
                    {
                        put("key", "value");
                    }
                };
    }

    @Test
    public void testRecordEvent() throws InterruptedException {
        // setup
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = new boolean[1];
        EventHistoryResultHandler<Boolean> handler =
                value -> {
                    result[0] = value;
                    latch.countDown();
                };

        Event event = new Event.Builder("name", "type", "source").setEventData(data).build();
        // test
        androidEventHistory.recordEvent(event, handler);
        latch.await();
        // verify record event successful
        assertTrue(result[0]);
    }

    @Test
    public void testGetEvents() throws Exception {
        // setup
        final CountDownLatch latch = new CountDownLatch(5);
        final CountDownLatch latch2 = new CountDownLatch(1);
        final int[] result = new int[1];
        EventHistoryResultHandler<Boolean> handler =
                result1 -> {
                    assertTrue(result1);
                    latch.countDown();
                };
        EventHistoryResultHandler<Integer> handler2 =
                value -> {
                    result[0] = value;
                    latch2.countDown();
                };

        for (int i = 0; i < 5; i++) {
            Event event = new Event.Builder("name", "type", "source").setEventData(data).build();
            androidEventHistory.recordEvent(event, handler);
        }

        latch.await();
        // test
        EventHistoryRequest[] requests = new EventHistoryRequest[1];
        HashMap<String, Object> mask = new HashMap<>();
        mask.put("key", "value");
        EventHistoryRequest request = new EventHistoryRequest(mask, 0, System.currentTimeMillis());
        requests[0] = request;
        androidEventHistory.getEvents(requests, false, handler2);
        latch2.await();
        // verify get events returns 5 events
        assertEquals(5, result[0]);
    }

    @Test
    public void testDeleteEvents() throws Exception {
        // setup
        final CountDownLatch latch = new CountDownLatch(5);
        final CountDownLatch latch2 = new CountDownLatch(10);
        final CountDownLatch latch3 = new CountDownLatch(1);
        final int[] result = new int[1];
        EventHistoryResultHandler<Boolean> handler =
                result1 -> {
                    assertTrue(result1);
                    latch.countDown();
                };
        EventHistoryResultHandler<Boolean> handler2 =
                result12 -> {
                    assertTrue(result12);
                    latch2.countDown();
                };
        EventHistoryResultHandler<Integer> handler3 =
                value -> {
                    result[0] = value;
                    latch3.countDown();
                };

        for (int i = 0; i < 5; i++) {
            Event event = new Event.Builder("name", "type", "source").setEventData(data).build();
            androidEventHistory.recordEvent(event, handler);
        }

        latch.await();
        Map<String, Object> data2 =
                new HashMap<String, Object>() {
                    {
                        put("key2", "value2");
                    }
                };

        for (int i = 0; i < 10; i++) {
            Event event = new Event.Builder("name", "type", "source").setEventData(data2).build();
            androidEventHistory.recordEvent(event, handler2);
        }

        latch2.await();
        // test
        EventHistoryRequest[] requests = new EventHistoryRequest[1];
        HashMap<String, Object> mask = new HashMap<>();
        mask.put("key2", "value2");
        EventHistoryRequest request = new EventHistoryRequest(mask, 0, 0);
        requests[0] = request;
        androidEventHistory.deleteEvents(requests, handler3);
        latch3.await();
        // verify 10 events deleted
        assertEquals(10, result[0]);
    }
}
