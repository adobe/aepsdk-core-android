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
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.stubbing.Answer
import java.lang.IllegalStateException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

@RunWith(MockitoJUnitRunner.Silent::class)
class SerialWorkDispatcherTests {

    /**
     * A test implementation of [SerialWorkDispatcher] that enables testing internal state logic.
     */
    class TestSerialWorkDispatcher(name: String, workHandler: WorkHandler<Event>) :
        SerialWorkDispatcher<Event>(name, workHandler) {
        var processedEvents: ArrayList<Event>? = null
        val initJob = Runnable {
            processedEvents = ArrayList()
        }

        val teardownJob = Runnable {
            processedEvents = null
        }

        var blockWork: Boolean = false

        override fun canWork(): Boolean {
            return !blockWork
        }
    }

    private val workHandler: SerialWorkDispatcher.WorkHandler<Event> =
        SerialWorkDispatcher.WorkHandler {
            serialWorkDispatcher.processedEvents?.add(it)
            true
        }

    @Mock
    private lateinit var mockExecutorService: ExecutorService

    private val serialWorkDispatcher: TestSerialWorkDispatcher =
        TestSerialWorkDispatcher("TestImpl", workHandler)

    @Before
    fun setUp() {
        serialWorkDispatcher.setExecutorService(mockExecutorService)
        serialWorkDispatcher.setInitialJob(serialWorkDispatcher.initJob)
        serialWorkDispatcher.setFinalJob(serialWorkDispatcher.teardownJob)

        Mockito.doAnswer(
            Answer {
                val runnable = it.getArgument<Runnable>(0)
                runnable.run()
                return@Answer null
            }
        ).`when`(mockExecutorService).submit(any(Runnable::class.java))
    }

    @Test
    fun `Initial job is executed on start and before processing items when set`() {
        assertNull(serialWorkDispatcher.processedEvents)

        serialWorkDispatcher.offer(Event.Builder("Event1", "Type", "Source").build())
        serialWorkDispatcher.start()

        assertEquals(SerialWorkDispatcher.State.ACTIVE, serialWorkDispatcher.getState())
        assertNotNull(serialWorkDispatcher.processedEvents)
        assertEquals(1, serialWorkDispatcher.processedEvents?.size)
    }

    @Test
    fun `Initial job is NOT invoked when set after start()`() {
        val serialWorkDispatcher = TestSerialWorkDispatcher("TestImpl incorrect start", workHandler)
        assertNull(serialWorkDispatcher.processedEvents)

        serialWorkDispatcher.offer(Event.Builder("Event1", "Type", "Source").build())
        serialWorkDispatcher.offer(Event.Builder("Event1", "Type", "Source").build())
        serialWorkDispatcher.offer(Event.Builder("Event2", "Type", "Source").build())
        serialWorkDispatcher.start()

        // Set the initial job after start()
        serialWorkDispatcher.setInitialJob(serialWorkDispatcher.initJob)

        assertEquals(SerialWorkDispatcher.State.ACTIVE, serialWorkDispatcher.getState())
        assertNull(serialWorkDispatcher.processedEvents)
    }

    @Test
    fun `Final job is NOT invoked when set after shutdown()`() {
        val serialWorkDispatcher = TestSerialWorkDispatcher("TestImpl incorrect start", workHandler)
        assertNull(serialWorkDispatcher.processedEvents)

        serialWorkDispatcher.setInitialJob(serialWorkDispatcher.initJob)
        serialWorkDispatcher.offer(Event.Builder("Event1", "Type", "Source").build())
        serialWorkDispatcher.offer(Event.Builder("Event1", "Type", "Source").build())
        serialWorkDispatcher.offer(Event.Builder("Event2", "Type", "Source").build())
        serialWorkDispatcher.start()
        Thread.sleep(500L)
        serialWorkDispatcher.shutdown()

        // Set the initial job after shutdown()
        serialWorkDispatcher.setFinalJob(serialWorkDispatcher.teardownJob)

        assertEquals(SerialWorkDispatcher.State.SHUTDOWN, serialWorkDispatcher.getState())
        assertNotNull(serialWorkDispatcher.initJob)
    }

    @Test
    fun `Dispatcher without init job and teardown jobs`() {
        val serialWorkDispatcher = TestSerialWorkDispatcher("TestImpl Without Init and teardown", workHandler)

        assertNull(serialWorkDispatcher.processedEvents)

        // Test start()
        serialWorkDispatcher.offer(Event.Builder("Event1", "Type", "Source").build())
        serialWorkDispatcher.offer(Event.Builder("Event2", "Type", "Source").build())
        serialWorkDispatcher.offer(Event.Builder("Event3", "Type", "Source").build())
        serialWorkDispatcher.start()

        // Verify
        assertEquals(SerialWorkDispatcher.State.ACTIVE, serialWorkDispatcher.getState())
        assertNull(serialWorkDispatcher.processedEvents)

        // Test shutdown()
        serialWorkDispatcher.shutdown()
        assertEquals(SerialWorkDispatcher.State.SHUTDOWN, serialWorkDispatcher.getState())
        assertNull(serialWorkDispatcher.processedEvents)
    }

    @Test
    fun `Dispatcher can only be started once`() {
        assertTrue(serialWorkDispatcher.start())
        assertEquals(SerialWorkDispatcher.State.ACTIVE, serialWorkDispatcher.getState())

        assertFalse(serialWorkDispatcher.start())

        serialWorkDispatcher.pause()

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
        assertEquals(arrayListOf(event1, event2, event3), serialWorkDispatcher.processedEvents)
    }

    @Test
    fun `Work is queued when processing is work condition is not met`() {
        // Setup
        val event1: Event = Event.Builder("Event1", "Type", "Source").build()
        val event2: Event = Event.Builder("Event2", "Type", "Source").build()
        val event3: Event = Event.Builder("Event3", "Type", "Source").build()
        val event4: Event = Event.Builder("Event4", "Type", "Source").build()
        //
        serialWorkDispatcher.offer(event1)
        serialWorkDispatcher.start()

        // Simulate work condition being blocked
        serialWorkDispatcher.blockWork = true

        // Offer new events
        serialWorkDispatcher.offer(event2)
        serialWorkDispatcher.offer(event3)
        serialWorkDispatcher.offer(event4)

        // Verify that the work is submitted only once.
        verify(mockExecutorService, times(2)).submit(any(Runnable::class.java))
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
    fun `Dispatcher can only be paused after started`() {
        assertFalse(serialWorkDispatcher.pause())
        assertEquals(SerialWorkDispatcher.State.NOT_STARTED, serialWorkDispatcher.getState())

        serialWorkDispatcher.start()

        assertTrue(serialWorkDispatcher.pause())
        assertEquals(SerialWorkDispatcher.State.PAUSED, serialWorkDispatcher.getState())
    }

    @Test
    fun `Dispatcher does not pause after shutdown`() {
        serialWorkDispatcher.shutdown()

        try {
            serialWorkDispatcher.pause()
            fail("Dispatcher should not be paused after shutdown")
        } catch (exception: IllegalStateException) {
            // pass.
        }
    }

    @Test
    fun `Work queued when dispatcher is not active is processed after start or resume`() {
        val event1: Event = Event.Builder("Event1", "Type", "Source").build()
        val event2: Event = Event.Builder("Event2", "Type", "Source").build()
        val event3: Event = Event.Builder("Event3", "Type", "Source").build()
        val event4: Event = Event.Builder("Event4", "Type", "Source").build()
        val event5: Event = Event.Builder("Event5", "Type", "Source").build()
        serialWorkDispatcher.offer(event1)
        serialWorkDispatcher.offer(event2)
        serialWorkDispatcher.offer(event3)
        assertNull(serialWorkDispatcher.processedEvents)

        serialWorkDispatcher.start()
        assertEquals(arrayListOf(event1, event2, event3), serialWorkDispatcher.processedEvents)

        serialWorkDispatcher.pause()
        serialWorkDispatcher.processedEvents = ArrayList()
        serialWorkDispatcher.offer(event4)
        serialWorkDispatcher.offer(event5)

        serialWorkDispatcher.resume()
        assertEquals(arrayListOf(event4, event5), serialWorkDispatcher.processedEvents)
    }

    @Test
    fun `Executor terminated & cleanup called when shutdown`() {
        val event: Event = Event.Builder("Event1", "Type", "Source").build()
        serialWorkDispatcher.start()
        serialWorkDispatcher.offer(event)

        serialWorkDispatcher.shutdown()

        assertEquals(SerialWorkDispatcher.State.SHUTDOWN, serialWorkDispatcher.getState())
        verify(mockExecutorService, times(1)).shutdown()
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

    @Test
    fun `Should not process queued work when paused`() {
        val event1: Event = Event.Builder("Event1", "Type", "Source").build()
        val event2: Event = Event.Builder("Event2", "Type", "Source").build()
        val event3: Event = Event.Builder("Event3", "Type", "Source").build()
        val event4: Event = Event.Builder("Event4", "Type", "Source").build()
        val event5: Event = Event.Builder("Event5", "Type", "Source").build()

        var processedEvents = ArrayList<Event>()
        val latch = CountDownLatch(1)
        val workHandler: SerialWorkDispatcher.WorkHandler<Event> =
            SerialWorkDispatcher.WorkHandler {
                processedEvents.add(it)
                if (it.name == "Event3") {
                    latch.countDown()
                }
                Thread.sleep(50)
                true
            }

        val serialDispatcher = SerialWorkDispatcher("", workHandler)
        serialDispatcher.offer(event1)
        serialDispatcher.offer(event2)
        serialDispatcher.offer(event3)
        serialDispatcher.offer(event4)
        serialDispatcher.offer(event5)

        // Should stop after processing event1, event2, event3
        serialDispatcher.start()
        latch.await()
        serialDispatcher.pause()
        Thread.sleep(500)
        assertEquals(listOf(event1, event2, event3), processedEvents)

        processedEvents.clear()
        serialDispatcher.resume()
        Thread.sleep(500)
        assertEquals(listOf(event4, event5), processedEvents)
    }
}
