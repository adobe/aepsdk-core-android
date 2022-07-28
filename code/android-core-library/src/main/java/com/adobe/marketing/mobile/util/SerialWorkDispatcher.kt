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

import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Provides a template for processing a queue of work items serially. Allows adding new work items
 * while the another item is being processed. Aims to separate the lifecycle of the worker thread
 * that process work items with the queue that they are fetched from to allow sub-classes to be
 * agnostic of worker thread management.
 */
open class SerialWorkDispatcher<T>(private val name: String, private val workHandler: WorkHandler<T>) {

    companion object {
        const val LOG_TAG = "SerialWorkDispatcher"
    }

    /**
     * Represents the state of the [SerialWorkDispatcher].
     */
    enum class State {
        /**
         * Indicates that the dispatcher has not yet started.
         * New work will be accepted but not executed until started.
         */
        NOT_STARTED,

        /**
         * Indicates that the dispatcher has been started and work will
         * be accepted.
         */
        ACTIVE,

        /**
         * Indicates that the dispatcher has been paused. New work will
         * be accepted but not executed until resumed
         */
        PAUSED,

        /**
         * Indicates that the dispatcher has been shutdown and no more work
         * will be accepted.
         */
        SHUTDOWN
    }

    /**
     * Represents the functional interface that is responsible for doing the desired work on each item of the [workQueue].
     * [WorkHandler.doWork] is called from the background worker thread that the [SerialWorkDispatcher] maintains.
     */
    fun interface WorkHandler<W> {
        /**
         * Handles processing on [item]
         *
         * @param item  the work item on which is dispatched for processing.
         */
        fun doWork(item: W)
    }

    /**
     * The [Executor] to which work is submitted for sequencing.
     */
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * Holds the work items that need to be processed by this dispatcher.
     */
    private val workQueue: Queue<T> = ConcurrentLinkedQueue()

    /**
     * A runnable responsible for draining the work items from the [workQueue]
     * and processing them via [WorkHandler.doWork].
     */
    private val workProcessor: WorkProcessor by lazy { WorkProcessor() }

    /**
     * A handle for identifying and manipulating the state of the thread that this dispatcher owns.
     * Can be used to ensure that only one [workProcessor] is active at a time.
     */
    private var workProcessorFuture: Future<*>? = null

    /**
     * Denotes the current state of the [SerialWorkDispatcher]. It is synonymous with the ability of the
     * [workQueue] to accept new items. Note that this is not the state of the worker-thread that this
     * dispatcher maintains.
     */
    @Volatile
    private var state: State = State.NOT_STARTED

    /**
     * Used for guarding the "activeness" logic.
     */
    private val activenessMutex: Any = Any()

    /**
     * Enqueues an item to the end of the [workQueue]. Additionally,
     * resumes the queue processing if the [SerialWorkDispatcher] is active
     * (but processing was stopped earlier due to lack of work).
     *
     * @param item item that needs to be processed.
     * @return true if [item] has been enqueued successfully, false otherwise
     */
    fun offer(item: T): Boolean {
        // Hold the activeness lock to ensure that an update to state is
        // not being made while a state change operation start/resume/stop is being done.
        synchronized(activenessMutex) {
            if (state == State.SHUTDOWN) return false
            workQueue.offer(item)

            if (state == State.ACTIVE) {
                // resume the processing the work items in the queue if necessary
                resume()
            }
            return true
        }
    }

    /**
     * Invoked immediately before processing the items in the queue for the first time.
     * Implementers are expected to perform any one-time setup operations (bound by the activeness of
     * [SerialWorkDispatcher]) before processing starts.
     * Invoked on the thread that calls [start]
     */
    protected open fun prepare() {
        // no-op
        // Intentionally non-abstract as most implementers
        // may not need this.
    }

    /**
     * Puts the [SerialWorkDispatcher] in active state and starts processing the {@link #workQueue}
     * if not already active.
     *
     * @return true - if [SerialWorkDispatcher] was successfully started,
     *         false - if it is already active or was shutdown
     * @throws IllegalStateException when attempting to start a dispatcher after being shutdown
     */
    fun start(): Boolean {
        synchronized(activenessMutex) {
            if (state == State.SHUTDOWN) {
                throw IllegalStateException("Cannot start SerialWorkDispatcher ($name). Already shutdown.")
            }

            if (state != State.NOT_STARTED) {
                MobileCore.log(LoggingMode.VERBOSE, getTag(), "SerialWorkDispatcher ($name) has already started.")
                return false
            }

            state = State.ACTIVE
            prepare()
            resume()
            return true
        }
    }

    /**
     * Puts the [SerialWorkDispatcher] in paused state and stops processing the {@link #workQueue}
     *
     * @return true - if [SerialWorkDispatcher] was successfully stopped,
     *         false - if it is already stopped or was shutdown
     * @throws IllegalStateException when attempting to start a dispatcher after being shutdown
     */
    fun pause(): Boolean {
        synchronized(activenessMutex) {
            if (state == State.SHUTDOWN) {
                throw IllegalStateException("Cannot pause SerialWorkDispatcher ($name). Already shutdown.")
            }

            if (state != State.ACTIVE) {
                MobileCore.log(LoggingMode.VERBOSE, getTag(), "SerialWorkDispatcher ($name) is not active.")
                return false
            }

            state = State.PAUSED
            return true
        }
    }

    /**
     * Resumes processing the work items in the [workQueue] if the [SerialWorkDispatcher]
     * is active and if no worker thread is actively processing the [workQueue].
     * Implementers can optionally trigger work via [resume] after any changes to logic in [canWork]
     * now being true, without having to wait for the next item to be added.
     */
    fun resume(): Boolean {
        synchronized(activenessMutex) {
            if (state == State.SHUTDOWN) {
                throw IllegalStateException("Cannot resume SerialWorkDispatcher ($name). Already shutdown.")
            }

            if (state == State.NOT_STARTED) {
                MobileCore.log(LoggingMode.VERBOSE, getTag(), "SerialWorkDispatcher ($name) has not started.")
                return false
            }

            state = State.ACTIVE
            val activeWorkProcessor: Future<*>? = workProcessorFuture
            if ((activeWorkProcessor != null && !activeWorkProcessor.isDone) || !canWork()) {
                // if the dispatcher is inactive or, if there is any active worker processing
                // the queue - do not do anything as the existing processor will process the items
                return true
            }

            // start the work processor
            workProcessorFuture = executorService.submit(workProcessor)
            return true
        }
    }

    /**
     * Invoked before processing each work item. Results in the worker thread being completed
     * if the implementer returns false. Returning false will result in "pausing" the processing (which
     * can later be resumed via [resume] explicitly or, when a re-evaluation of [canWork] happens when
     * new item is added to the [workQueue] via [offer]).
     * Implementers are expected to enforce any conditions that need
     * to be checked before performing work here.
     *
     * @return true if all conditions are met for performing work, false otherwise.
     */
    protected open fun canWork(): Boolean {
        // Return true by default
        return true
    }

    /**
     * Checks if there are any work items available for processing.
     *
     * @return true if there are any available in [workQueue] work items for processing,
     *         false otherwise
     */
    private fun hasWork(): Boolean {
        return workQueue.peek() != null
    }

    /**
     * Removes the work item at the front (earliest queued) of the [workQueue]
     *
     * @return the work item at the front (earliest queued) of the [workQueue], null if [workQueue] is empty
     */
    private fun getWorkItem(): T? {
        return workQueue.poll()
    }

    /**
     * Invoked immediately after stopping processing as a result of [shutdown].
     * Implementers are expected to perform any cleanup operations as a result of
     * the [SerialWorkDispatcher] being stopped.
     * Invoked on the calling thread that invokes [shutdown]
     */
    protected open fun cleanup() {
        // no-op
        // Intentionally non-abstract as most implementers
        // may not need this.
    }

    /**
     * Puts the [SerialWorkDispatcher] into inactive state and clears the [workQueue].
     * Calling [resume] or [start] will have no effect on the state of the [SerialWorkDispatcher] after
     * this method is invoked.
     */
    fun shutdown() {
        synchronized(activenessMutex) {
            if (state == State.SHUTDOWN) return

            state = State.SHUTDOWN

            // Cancel active work processing (if any)
            val activeTask: Future<*>? = workProcessorFuture
            activeTask?.cancel(true)
            workProcessorFuture = null
            workQueue.clear()
        }

        executorService.shutdownNow()
        cleanup()
    }

    fun getState(): State {
        return state
    }

    private fun getTag() = "$LOG_TAG-$name"

    /**
     * A runnable responsible for looping through the work items maintained by [SerialWorkDispatcher]
     * in its [workQueue]
     */
    @VisibleForTesting
    internal inner class WorkProcessor : Runnable {
        override fun run() {
            // Perform work only if the dispatcher is unblocked and there are
            // items in the queue to perform work on.
            while (!Thread.interrupted() && state == State.ACTIVE && canWork() && hasWork()) {
                try {
                    val workItem = getWorkItem() ?: return
                    workHandler.doWork(workItem)
                } catch (exception: Exception) {
                    Thread.currentThread().interrupt()
                    MobileCore.log(
                        LoggingMode.ERROR,
                        getTag(),
                        "Exception encountered while processing item. $exception"
                    )
                }
            }
        }
    }
}
