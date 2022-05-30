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

import com.adobe.marketing.mobile.Event
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.stubbing.Answer
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.reflect.Whitebox
import java.lang.IllegalStateException
import java.util.concurrent.ExecutorService
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

@RunWith(PowerMockRunner::class)
class SerialWorkDispatcherTest {

    /**
     * A test implementation of [SerialWorkDispatcher] that enables testing internal state logic.
     */
    class TestSerialWorkDispatcher(name: String, workHandler: WorkHandler<Event>) : SerialWorkDispatcher<Event>(name, workHandler) {
        var processedEvents: ArrayList<Event>? = null
            private set

        var blockWork: Boolean = false

        override fun prepare() {
            processedEvents = ArrayList()
        }

        override fun cleanup() {
            processedEvents = null
        }

        override fun canWork(): Boolean {
            return !blockWork
        }
    }

    private val workHandler: SerialWorkDispatcher.WorkHandler<Event> = SerialWorkDispatcher.WorkHandler {
        serialWorkDispatcher.processedEvents?.add(it)
    }

    @Mock
    private lateinit var mockExecutorService: ExecutorService

    private val serialWorkDispatcher: TestSerialWorkDispatcher = TestSerialWorkDispatcher("TestImpl", workHandler)

    @Before
    fun setUp() {
        Whitebox.setInternalState(serialWorkDispatcher, "executorService", mockExecutorService)

        Mockito.doAnswer(
            Answer {
                val runnable = it.getArgument<Runnable>(0)
                runnable.run()
                return@Answer null
            }
        ).`when`(mockExecutorService).submit(any(Runnable::class.java))
    }

    @Test
    fun `Prepare is called when dispatcher is started`() {
        assertNull(serialWorkDispatcher.processedEvents)

        serialWorkDispatcher.start()

        assertEquals(SerialWorkDispatcher.State.ACTIVE, serialWorkDispatcher.getState())
        assertNotNull(serialWorkDispatcher.processedEvents)
    }

    @Test
    fun `Dispatcher can only be started once`() {
        assertTrue(serialWorkDispatcher.start())

        assertEquals(SerialWorkDispatcher.State.ACTIVE, serialWorkDispatcher.getState())
        assertFalse(serialWorkDispatcher.start())
    }

    @Test
    fun `Dispatcher does not restart after shutdown`() {
        assertTrue(serialWorkDispatcher.start())
        serialWorkDispatcher.shutdown()

        try {
            serialWorkDispatcher.start()
            fail("Dispatcher should not start after shutdown")
        } catch (exception: IllegalStateException) {
            // pass.
        }
    }

    @Test
    fun `Work queued when dispatcher not yet started is not processed`() {
        val event1: Event = Event.Builder("Event1", "Type", "Source").build()
        val event2: Event = Event.Builder("Event2", "Type", "Source").build()
        val event3: Event = Event.Builder("Event3", "Type", "Source").build()

        serialWorkDispatcher.offer(event1)
        serialWorkDispatcher.offer(event2)
        serialWorkDispatcher.offer(event3)
        assertNull(serialWorkDispatcher.processedEvents)
    }

    @Test
    fun `Work queued when dispatcher not yet started is processed on start`() {
        val event1: Event = Event.Builder("Event1", "Type", "Source").build()
        val event2: Event = Event.Builder("Event2", "Type", "Source").build()
        val event3: Event = Event.Builder("Event3", "Type", "Source").build()
        serialWorkDispatcher.offer(event1)
        serialWorkDispatcher.offer(event2)
        serialWorkDispatcher.offer(event3)
        assertNull(serialWorkDispatcher.processedEvents)

        serialWorkDispatcher.start()
        assertNotNull(serialWorkDispatcher.processedEvents)
        assertEquals(3, serialWorkDispatcher.processedEvents?.size)
    }

    @Test
    fun `Work is queued when processing is work condition is not met`() {
        // Setup
        val event1: Event = Event.Builder("Event1", "Type", "Source").build()
        val event2: Event = Event.Builder("Event2", "Type", "Source").build()
        val event3: Event = Event.Builder("Event3", "Type", "Source").build()
        //
        serialWorkDispatcher.offer(event1)
        serialWorkDispatcher.start()

        // Simulate work condition being blocked
        serialWorkDispatcher.blockWork = true

        // Offer new events
        serialWorkDispatcher.offer(event2)
        serialWorkDispatcher.offer(event3)

        // Verify that the work is submitted only once.
        verify(mockExecutorService, times(1)).submit(any(Runnable::class.java))
        // verify that only event 1 is processed
        assertNotNull(serialWorkDispatcher.processedEvents)
        assertEquals(1, serialWorkDispatcher.processedEvents?.size)
    }

    @Test
    fun `Queued work is resumed when new work is offered after work condition is met`() {
        // Setup
        val event1: Event = Event.Builder("Event1", "Type", "Source").build()
        val event2: Event = Event.Builder("Event2", "Type", "Source").build()
        val event3: Event = Event.Builder("Event3", "Type", "Source").build()
        // Offer only one event at start
        serialWorkDispatcher.offer(event1)
        serialWorkDispatcher.start()

        // Simulate work condition being blocked
        serialWorkDispatcher.blockWork = true

        // Offer new event
        serialWorkDispatcher.offer(event2)
        // Simulate condition being met
        serialWorkDispatcher.blockWork = false

        // Verify new work item is not yet started.
        assertEquals(1, serialWorkDispatcher.processedEvents?.size)

        // Offer new event
        serialWorkDispatcher.offer(event3)

        // verify that all events are processed
        assertNotNull(serialWorkDispatcher.processedEvents)
        assertEquals(3, serialWorkDispatcher.processedEvents?.size)
    }

    @Test
    fun `Executor terminated & cleanup called when shutdown`() {
        val event: Event = Event.Builder("Event1", "Type", "Source").build()
        serialWorkDispatcher.start()
        serialWorkDispatcher.offer(event)

        serialWorkDispatcher.shutdown()

        assertEquals(SerialWorkDispatcher.State.SHUTDOWN, serialWorkDispatcher.getState())
        verify(mockExecutorService, times(1)).shutdownNow()
        assertNull(serialWorkDispatcher.processedEvents)
    }

    @Test
    fun `Work cannot be queued when shutdown`() {
        val event: Event = Event.Builder("Event1", "Type", "Source").build()
        serialWorkDispatcher.start()

        serialWorkDispatcher.shutdown()

        assertEquals(SerialWorkDispatcher.State.SHUTDOWN, serialWorkDispatcher.getState())
        assertFalse(serialWorkDispatcher.offer(event))
    }
}
