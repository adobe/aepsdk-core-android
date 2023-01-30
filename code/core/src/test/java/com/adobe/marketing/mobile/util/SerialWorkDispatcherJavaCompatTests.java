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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class SerialWorkDispatcherJavaCompatTests {

    /**
     * A test implementation of [SerialWorkDispatcher] that enables testing compatibility with
     * Kotlin.
     */
    static class TestJavaSerialWorkDispatcher extends SerialWorkDispatcher<Integer> {

        private ArrayList<Integer> processedItems = null;

        final Runnable setupJob =
                () -> {
                    processedItems = new ArrayList<>();
                };
        final Runnable teardownJob =
                () -> {
                    processedItems = null;
                };

        TestJavaSerialWorkDispatcher(
                @NotNull final String name, @NotNull final WorkHandler<Integer> workHandler) {
            super(name, workHandler);
        }

        List<Integer> getProcessedItems() {
            return processedItems;
        }
    }

    private final SerialWorkDispatcher.WorkHandler<Integer> workHandler =
            new SerialWorkDispatcher.WorkHandler<Integer>() {
                @Override
                public boolean doWork(Integer item) {
                    javaSerialWorkDispatcher.getProcessedItems().add(item);
                    return true;
                }
            };

    private final TestJavaSerialWorkDispatcher javaSerialWorkDispatcher =
            new TestJavaSerialWorkDispatcher("JavaSerialWorkDispatcher", workHandler);

    @Mock private ExecutorService mockExecutorService;

    @Before
    public void setup() {
        javaSerialWorkDispatcher.setExecutorService(mockExecutorService);
        javaSerialWorkDispatcher.setInitialJob(javaSerialWorkDispatcher.setupJob);
        javaSerialWorkDispatcher.setFinalJob((javaSerialWorkDispatcher.teardownJob));

        Mockito.doAnswer(
                        new Answer() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                final Runnable runnable = invocation.getArgument(0);
                                runnable.run();
                                return null;
                            }
                        })
                .when(mockExecutorService)
                .submit(any(Runnable.class));
    }

    @Test
    public void testJavaSerialWorkDispatcherCompatibility() {
        javaSerialWorkDispatcher.offer(1);
        javaSerialWorkDispatcher.offer(2);
        javaSerialWorkDispatcher.offer(3);

        assertNull(javaSerialWorkDispatcher.getProcessedItems());

        javaSerialWorkDispatcher.start();
        assertNotNull(javaSerialWorkDispatcher.getProcessedItems());
        assertEquals(3, javaSerialWorkDispatcher.getProcessedItems().size());

        javaSerialWorkDispatcher.shutdown();
        assertNull(javaSerialWorkDispatcher.getProcessedItems());
    }
}
