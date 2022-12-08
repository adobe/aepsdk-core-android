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

package com.adobe.marketing.mobile.util

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SerialWorkDispatcherTests {
    private val processedItems = ConcurrentLinkedQueue<Int>()
    private val dispatchedItems = ConcurrentLinkedQueue<Int>()
    private val workCompletionLatch: CountDownLatch = CountDownLatch(20)
    private val dispatchMutex: Any = Any()

    /**
     * A Test work handler that simulates doing work by sleeping a random amount of time.
     */
    private val workHandler: SerialWorkDispatcher.WorkHandler<Int> = SerialWorkDispatcher.WorkHandler {
        val lag = (50L..100L).random()
        Thread.sleep(lag)
        processedItems.add(it)
        workCompletionLatch.countDown()
        true
    }

    private val serialWorkDispatcher: SerialWorkDispatcher<Int> =
        SerialWorkDispatcher<Int>("TestSerialDispatcherImpl", workHandler)

    @Test
    fun testDispatcher_ProcessesJobsInTheOrderOfDispatch() {
        val executorService: ExecutorService = Executors.newFixedThreadPool(5)
        // Start the event worker
        serialWorkDispatcher.start()

        // Dispatch 20 jobs
        runBlocking(executorService.asCoroutineDispatcher()) {
            for (i in 1..20) launch {
                synchronized(dispatchMutex) {
                    // Offer the item to the serial dispatcher
                    serialWorkDispatcher.offer(i)

                    // Also add them to verification queue of dispatched items
                    dispatchedItems.add(i)
                }
            }
        }

        workCompletionLatch.await(3, TimeUnit.SECONDS)

        // Verify that all dispatched items have been processed
        Assert.assertTrue(processedItems.size == dispatchedItems.size)
        // Verify that the items are processed in the order of their dispatch
        for (i in 0..dispatchedItems.size) {
            Assert.assertTrue(processedItems.poll() == dispatchedItems.poll())
        }
    }
}
