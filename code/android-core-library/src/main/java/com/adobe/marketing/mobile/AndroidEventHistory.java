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

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Android implementation of {@link EventHistory} which provides functionality for performing database
 * operations on an {@link AndroidEventHistoryDatabase}.
 */
class AndroidEventHistory implements EventHistory {
	private static final String LOG_TAG = "AndroidEventHistory";
	private static final int THREAD_POOL_SIZE = 1;
	private static final int COUNT_INDEX = 0;
	private static final int OLDEST_INDEX = 1;
	private final AndroidEventHistoryDatabase androidEventHistoryDatabase;
	private final ExecutorService executorService;

	/**
	 * Constructor.
	 */
	AndroidEventHistory() throws EventHistoryDatabaseCreationException {
		androidEventHistoryDatabase = new AndroidEventHistoryDatabase();
		executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	}

	/**
	 * Record an event in the {@link AndroidEventHistoryDatabase}.
	 *
	 * @param event   the {@link Event} to be recorded
	 * @param handler {@link EventHistoryResultHandler} a callback which will contain a {@code boolean} indicating if
	 *                the database operation was successful
	 */
	public void recordEvent(final Event event, final EventHistoryResultHandler<Boolean> handler) {
		final long fnv1aHash = event.getData().toFnv1aHash(event.getMask());

		if (fnv1aHash == 0) {
			Log.debug(LOG_TAG, " The event with name \"%s\" has a fnv1a hash equal to 0. The event will not be recorded.",
					  event.getName());
			return;
		}

		executorService.submit(new Runnable() {
			@Override
			public void run() {
				handler.call(androidEventHistoryDatabase.insert(fnv1aHash));
			}
		});
	}

	/**
	 * Query the {@link AndroidEventHistoryDatabase} for {@link Event}s which match the contents of the
	 * {@link EventHistoryRequest} array.
	 *
	 * @param eventHistoryRequests an array of {@code EventHistoryRequest}s to be matched
	 * @param enforceOrder         {@code boolean} if true, consecutive lookups will use the oldest timestamp from the previous event
	 *                             as their from date
	 * @param handler              {@link EventHistoryResultHandler<Integer>} containing the the total number of matching events in the {@code AndroidEventHistoryDatabase}
	 *                             if an "any" search was done. If an "ordered" search was done, the handler will contain a "1"
	 *                             if the event history requests were found in the order specified in the eventHistoryRequests array
	 *                             and a "0" if the events were not found in the order specified.
	 */
	@Override
	public void getEvents(final EventHistoryRequest[] eventHistoryRequests,
						  final boolean enforceOrder,
						  final EventHistoryResultHandler<Integer> handler) {
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				long previousEventOldestOccurrence = 0L;
				int foundEventCount = 0;

				for (final EventHistoryRequest request : eventHistoryRequests) {
					final long from = (enforceOrder
									   && previousEventOldestOccurrence != 0) ? previousEventOldestOccurrence : request.fromDate;
					final long to = request.toDate == 0 ? System.currentTimeMillis() : request.toDate;
					final Map<String, Variant> flattenedMask = EventDataFlattener.getFlattenedEventDataMask(request.mask);
					final SortedMap<String, Variant> sortedMap = new TreeMap<>(flattenedMask);
					final long eventHash = StringEncoder.convertMapToDecimalHash(sortedMap);
					final DatabaseService.QueryResult result = androidEventHistoryDatabase.select(eventHash, from, to);

					try { // columns are index 0: count, index 1: oldest, index 2: newest
						result.moveToFirst();

						if (result.getInt(COUNT_INDEX) != 0) {
							previousEventOldestOccurrence = result.getLong(OLDEST_INDEX);

							if (enforceOrder) {
								foundEventCount++;
							} else {
								foundEventCount += result.getInt(COUNT_INDEX);
							}
						}
					} catch (final Exception exception) {
						Log.debug(LOG_TAG,
								  "Exception occurred when attempting to retrieve events with eventHash %s from the EventHistoryDatabase: %s",
								  eventHash, exception.getMessage());
					}
				}

				// for ordered searches, if found event count matches the total number of requests, then all requests were found. return 1 / true.
				if (enforceOrder) {
					if (foundEventCount == eventHistoryRequests.length) {
						handler.call(1);
					} else { // otherwise return 0 / false
						handler.call(0);
					}
				} else { // for "any" search, return total number of matching events
					handler.call(foundEventCount);
				}
			}
		});
	}

	/**
	 * Delete rows from the {@link AndroidEventHistoryDatabase} that contain {@link Event}s which match the contents
	 * of the {@link EventHistoryRequest} array.
	 *
	 * @param eventHistoryRequests an array of {@code EventHistoryRequest}s to be deleted
	 * @param handler              a callback which will be called with a {@code int} containing the total number of rows deleted from the
	 *                             {@code AndroidEventHistoryDatabase}
	 */
	@Override
	public void deleteEvents(final EventHistoryRequest[] eventHistoryRequests,
							 final EventHistoryResultHandler<Integer> handler) {
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				int deletedRows = 0;

				for (final EventHistoryRequest request : eventHistoryRequests) {
					// if no "from" date is provided, delete from the beginning of the database
					final long from = request.fromDate == 0 ? 0 : request.fromDate;
					// if no "to" date is provided, delete until the end of the database
					final long to = request.toDate == 0 ? System.currentTimeMillis() : request.toDate;
					final Map<String, Variant> flattenedMask = EventDataFlattener.getFlattenedEventDataMask(request.mask);
					final SortedMap<String, Variant> sortedMap = new TreeMap<>(flattenedMask);
					final long eventHash = StringEncoder.convertMapToDecimalHash(sortedMap);
					deletedRows += androidEventHistoryDatabase.delete(eventHash, from, to);
				}

				handler.call(deletedRows);
			}
		});
	}
}