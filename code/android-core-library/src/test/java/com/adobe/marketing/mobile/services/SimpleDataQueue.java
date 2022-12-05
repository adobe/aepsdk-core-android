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

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A simple thread-safe implementation of a {@link DataQueue}. The {@link DataEntity} objects are
 * held in memory by an instance of this {@code DataQueue}.
 */
public class SimpleDataQueue implements DataQueue {

    private static final Object syncObject = new Object();
    private final Queue<DataEntity> entities = new ConcurrentLinkedQueue<>();

    /**
     * Wait the current thread until this data queue is empty or {@code timeoutms} time elapses.
     *
     * @param timeoutms the maximum time in milliseconds to wait
     * @throws InterruptedException if any thread interrupted this thread while waiting for the data
     *     queue to empty
     */
    public void waitUntilEmpty(long timeoutms) throws InterruptedException {
        synchronized (syncObject) {
            while (!entities.isEmpty()) {
                syncObject.wait(timeoutms);
            }
        }

        // wait a bit longer to allow processNextHit to finish after removing last entity
        Thread.sleep(100);
    }

    @Override
    public boolean add(DataEntity dataEntity) {
        synchronized (syncObject) {
            return entities.add(dataEntity);
        }
    }

    @Override
    public DataEntity peek() {
        synchronized (syncObject) {
            return entities.peek();
        }
    }

    @Override
    public List<DataEntity> peek(int n) {
        return null;
    }

    @Override
    public boolean remove() {
        // notify when queue is empty
        synchronized (syncObject) {
            boolean result = entities.poll() != null;
            syncObject.notify();
            return result;
        }
    }

    @Override
    public boolean remove(int n) {
        return false;
    }

    @Override
    public boolean clear() {
        // notify when queue is empty
        synchronized (syncObject) {
            entities.clear();
            syncObject.notify();
        }

        return true;
    }

    @Override
    public int count() {
        synchronized (syncObject) {
            return entities.size();
        }
    }

    @Override
    public void close() {}
}
