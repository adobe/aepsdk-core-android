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

import android.database.Cursor;
import com.adobe.marketing.mobile.EventHistoryResultHandler;

/** Interface defining a database to be used by the SDK for storing event history. */
interface EventHistoryDatabase {
    /**
     * Insert a row into a table in the database.
     *
     * @param hash {@code long} containing the 32-bit FNV-1a hashed representation of an Event's
     *     data
     * @return a {@code boolean} which will contain the status of the database insert operation
     */
    boolean insert(final long hash);

    /**
     * Queries the event history database to search for the existence of an event.
     *
     * <p>This method will count all records in the event history database that match the provided
     * hash and are within the bounds of the provided from and to timestamps. If the "from" date is
     * equal to 0, the search will use the beginning of event history as the lower bounds of the
     * date range. If the "to" date is equal to 0, the search will use the current system timestamp
     * as the upper bounds of the date range. The {@link EventHistoryResultHandler} will be called
     * with a {@link Cursor} which contains the number of matching records, the oldest timestamp,
     * and the newest timestamp for a matching event. If no database connection is available, the
     * handler will be called with a null {@code DatabaseService.QueryResult}.
     *
     * @param hash {@code long} containing the 32-bit FNV-1a hashed representation of an Event's
     *     data
     * @param from {@code long} a timestamp representing the lower bounds of the date range to use
     *     when searching for the hash
     * @param to {@code long} a timestamp representing the upper bounds of the date range to use
     *     when searching for the hash
     * @return a {@code Cursor} which will contain the matching events
     */
    Cursor select(final long hash, final long from, final long to);

    /**
     * Delete entries from the event history database.
     *
     * @param hash {@code long} containing the 32-bit FNV-1a hashed representation of an Event's
     *     data
     * @param from {@code long} representing the lower bounds of the date range to use when
     *     searching for the hash
     * @param to {@code long} representing the upper bounds of the date range to use when searching
     *     for the hash
     * @return {@code int} which will contain the number of rows deleted.
     */
    int delete(final long hash, final long from, final long to);
    /** Close this database. */
    void closeDatabase();
}
