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

package com.adobe.marketing.mobile.services;

import androidx.annotation.VisibleForTesting;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides functionality for asynchronous processing of hits in a synchronous manner while
 * providing the ability to retry hits.
 */
public class PersistentHitQueue extends HitQueuing {

    private final DataQueue queue;
    private final HitProcessing processor;
    private AtomicBoolean suspended = new AtomicBoolean(true);
    private final ScheduledExecutorService scheduledExecutorService;
    private final AtomicBoolean isTaskScheduled = new AtomicBoolean(false);

    /**
     * Constructor to create {@link HitQueuing} with underlying {@link DataQueue}
     *
     * @param queue object of <code>DataQueue</code> for persisting hits
     * @param processor object of {@link HitProcessing} for processing hits
     * @throws IllegalArgumentException
     */
    public PersistentHitQueue(final DataQueue queue, final HitProcessing processor)
            throws IllegalArgumentException {
        this(queue, processor, Executors.newSingleThreadScheduledExecutor());
    }

    @VisibleForTesting
    PersistentHitQueue(
            final DataQueue queue,
            final HitProcessing processor,
            final ScheduledExecutorService executorService) {
        if (queue == null || processor == null) {
            throw new IllegalArgumentException(
                    "Null value is not allowed in PersistentHitQueue Constructor.");
        }

        this.queue = queue;
        this.processor = processor;
        this.scheduledExecutorService = executorService;
    }

    @Override
    public boolean queue(final DataEntity entity) {
        final boolean result = queue.add(entity);
        processNextHit();
        return result;
    }

    @Override
    public void beginProcessing() {
        suspended.set(false);
        processNextHit();
    }

    @Override
    public void suspend() {
        suspended.set(true);
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public int count() {
        return queue.count();
    }

    @Override
    public void close() {
        suspend();
        queue.close();
        scheduledExecutorService.shutdown();
    }

    /**
     * A Recursive function for processing persisted hits. I will continue processing all the Hits
     * until none are left in the DataQueue.
     */
    private void processNextHit() {
        if (suspended.get()) {
            return;
        }

        // If taskScheduled is false, then set to true and return true.
        // If taskScheduled is true, then compareAndSet returns false
        if (!isTaskScheduled.compareAndSet(false, true)) {
            return;
        }

        scheduledExecutorService.execute(
                () -> {
                    DataEntity entity = queue.peek();

                    if (entity == null) {
                        isTaskScheduled.set(false);
                        return;
                    }

                    processor.processHit(
                            entity,
                            result -> {
                                if (result) {
                                    queue.remove();
                                    isTaskScheduled.set(false);
                                    processNextHit();
                                } else {
                                    long delay = processor.retryInterval(entity);
                                    scheduledExecutorService.schedule(
                                            () -> {
                                                isTaskScheduled.set(false);
                                                processNextHit();
                                            },
                                            delay,
                                            TimeUnit.SECONDS);
                                }
                            });
                });
    }
}
