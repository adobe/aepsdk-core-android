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

import java.util.*;
import java.util.concurrent.*;

/**
 * Used to dispatch events to registered EventListeners
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
class EventBus {

	private final String logPrefix;
	private long lastEventTimestamp = 0;
	private final ConcurrentHashMap<Integer, ConcurrentLinkedQueue<EventListener>> listenersPerMask;
	private final ExecutorService threadPool;
	private static final int MAX_LISTENER_TIME_MS = 1000;

	/**
	 * constructor
	 */
	public EventBus() {
		this.logPrefix = String.format("%s(%s)", this.getClass().getSimpleName(), "EventHub");
		this.listenersPerMask = new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<EventListener>>();
		this.threadPool = Executors.newCachedThreadPool();
	}

	/**
	 * dispatch event to the corresponding EventListeners
	 *
	 * @param event the event to be dispatched to the EventListeners
	 */
	public void dispatch(final Event event) {
		if (Log.getLogLevel().id >= LoggingMode.VERBOSE.id) {
			Log.trace(logPrefix, "Processing event #%d: %s", event.getEventNumber(), event.toString());
		}

		final long eventTimestamp = event.getTimestamp();

		if (eventTimestamp < lastEventTimestamp) {
			Log.debug(logPrefix, "Out of order event timestamp (%d) last event timestamp was (%d)", eventTimestamp,
					  lastEventTimestamp);
		}

		lastEventTimestamp = eventTimestamp;

		notifyListeners(event, Event.generateEventMask(EventType.WILDCARD, EventSource.WILDCARD, null));
		notifyListeners(event, event.getEventMask());
	}

	private void notifyListeners(final Event e, final int eventMask) {
		if (e == null) {
			return;
		}

		final List<Future<?>> futures = new ArrayList<Future<?>>();
		final HashMap<Future<?>, EventListener> futureListenerMap = new HashMap<Future<?>, EventListener>();
		final ConcurrentLinkedQueue<EventListener> maskListeners = listenersPerMask.get(eventMask);

		if (maskListeners != null) {
			for (final EventListener listener : maskListeners) {
				Future<?> f = threadPool.submit(new Runnable() {
					@Override
					public void run() {
						listener.hear(e);
					}
				});
				futureListenerMap.put(f, listener);
				futures.add(f);

				if (listener instanceof OneTimeListener) {
					maskListeners.remove(listener);
				}
			}

			for (final Future<?> f : futures) {
				try {
					f.get(MAX_LISTENER_TIME_MS, TimeUnit.MILLISECONDS);
				} catch (final TimeoutException ex) {
					final EventListener listener = futureListenerMap.get(f);
					Log.error(logPrefix, "Listener %s exceeded runtime limit of %d milliseconds (%s)", listener.getClass().getName(),
							  MAX_LISTENER_TIME_MS, ex);
				} catch (final Exception ex) {
					final EventListener listener = futureListenerMap.get(f);
					Log.error(logPrefix, "Thread exception while waiting for listener %s (%s)", listener.getClass().getName(), ex);
				}
			}
		}
	}

	/**
	 * remove a EventListener from EventBus
	 *
	 * @param listener the listener that should be removed
	 */
	public void removeListener(final EventListener listener) {
		final ConcurrentLinkedQueue<EventListener> listenersForThisEvent = listenersPerMask.get(Event.generateEventMask(
					listener.getEventType(), listener.getEventSource(), null));

		if (listenersForThisEvent == null) {
			return;
		}

		try {
			listener.onUnregistered();
		} catch (Exception e) {
			Log.error(logPrefix, "%s.onUnregistered() threw %s", getClass().getName(), e);
		}

		listenersForThisEvent.remove(listener);
	}

	/**
	 * remove a EventListener from EventBus
	 *
	 * @param listener the listener that should be removed
	 * @param type  the type of the Event
	 * @param source the source of the Event
	 * @param pairID unique ID for single use listener.  May be {@code null} if no pairing id is needed.
	 */
	public void removeListener(final EventListener listener, final EventType type, final EventSource source,
							   final String pairID) {
		if (listener == null) {
			return;
		}

		final int mask = Event.generateEventMask(type, source, pairID);
		final ConcurrentLinkedQueue<EventListener> maskListeners = listenersPerMask.get(mask);

		if (maskListeners != null) {
			maskListeners.remove(listener);
		}

	}

	/**
	 * add a EventListener to EventBus
	 *
	 * @param listener the listener that should be attached
	 * @param type the type of the Event
	 * @param source the source of the Event
	 * @param pairID unique ID for single use listener.  May be {@code null} if no pairing id is needed.
	 */
	public void addListener(final EventListener listener, final EventType type, final EventSource source,
							final String pairID) {
		if (listener == null) {
			return;
		}

		final int mask = Event.generateEventMask(type, source, pairID);
		listenersPerMask.putIfAbsent(mask, new ConcurrentLinkedQueue<EventListener>());
		listenersPerMask.get(mask).add(listener);
	}

}
