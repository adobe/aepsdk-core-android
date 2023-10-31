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

package com.adobe.marketing.mobile.internal.eventhub.history

/** Interface defining a database to be used by the SDK for storing event history.  */
internal interface EventHistoryDatabase {

    data class QueryResult(val count: Int, val oldestTimestamp: Long?, val newestTimeStamp: Long?)

    /**
     * Insert a row into a table in the database.
     *
     * @param hash `long` containing the 32-bit FNV-1a hashed representation of an Event's data
     * @param timestampMS `long` Event's timestamp in milliseconds
     * @return a `boolean` which will contain the status of the database insert operation
     */
    fun insert(hash: Long, timestampMS: Long): Boolean

    /**
     * Queries the database to search for the existence of events.
     * This method will count all records in the event history database that match the provided
     * hash and are within the bounds of the provided from and to timestamps.
     *
     * @param hash `long` containing the 32-bit FNV-1a hashed representation of an Event's data
     * @param from `long` a timestamp representing the lower bounds of the date range to use when searching for the hash
     * @param to `long` a timestamp representing the upper bounds of the date range to use when searching for the hash
     * @return a `QueryResult` object containing details of the matching records. If no database connection is available, returns null
     */
    fun query(hash: Long, from: Long, to: Long): QueryResult?

    /**
     * Delete entries from the event history database.
     *
     * @param hash `long` containing the 32-bit FNV-1a hashed representation of an Event's data
     * @param from `long` representing the lower bounds of the date range to use when searching for the hash
     * @param to `long` representing the upper bounds of the date range to use when searching for the hash
     * @return `int` which will contain the number of rows deleted.
     */
    fun delete(hash: Long, from: Long, to: Long): Int
}
