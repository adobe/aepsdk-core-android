///*
//  Copyright 2022 Adobe. All rights reserved.
//  This file is licensed to you under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License. You may obtain a copy
//  of the License at http://www.apache.org/licenses/LICENSE-2.0
//  Unless required by applicable law or agreed to in writing, software distributed under
//  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
//  OF ANY KIND, either express or implied. See the License for the specific language
//  governing permissions and limitations under the License.
// */
//
//package com.adobe.marketing.mobile;
//
//import java.util.*;
//
//import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType.INTEGER;
//
//import com.adobe.marketing.mobile.internal.eventhub.history.EventHistory;
//import com.adobe.marketing.mobile.EventHistoryResultHandler;
//import com.adobe.marketing.mobile.internal.util.StringEncoder;
//
//public class FakeEventHistory implements EventHistory {
//	FakeEventHistoryDatabase fakeEventHistoryDatabase = new FakeEventHistoryDatabase();
//
//	FakeEventHistory() {
//		fakeEventHistoryDatabase.openDatabase();
//		fakeEventHistoryDatabase.createTable(new String[] {"eventHash", "timestamp"}, new
//											 DatabaseService.Database.ColumnDataType[] {INTEGER, INTEGER},
//											 null);
//	}
//
//	@Override
//	public void recordEvent(Event event, EventHistoryResultHandler<Boolean> handler) {
//		handler.call(fakeEventHistoryDatabase.insert(event.getData().toFnv1aHash(event.getMask())));
//	}
//
//	@Override
//	public void getEvents(EventHistoryRequest[] eventHistoryRequests, boolean enforceOrder,
//						  EventHistoryResultHandler<Integer> handler) {
//		long[] previousEventOldestOccurrence = {0L};
//		final int[] foundEventCount = {0};
//
//		for (EventHistoryRequest request : eventHistoryRequests) {
//			long from = (enforceOrder
//						 && previousEventOldestOccurrence[0] != 0) ? previousEventOldestOccurrence[0] : request.fromDate;
//			long to = request.toDate == 0 ? System.currentTimeMillis() : request.toDate;
//			Map<String, Variant> flattenedMask = EventDataFlattener.getFlattenedEventDataMask(request.mask);
//			SortedMap<String, Variant> sortedMap = new TreeMap<>(flattenedMask);
//			long eventHash = StringEncoder.convertMapToDecimalHash(sortedMap);
//			DatabaseService.QueryResult result = fakeEventHistoryDatabase.select(eventHash, from, to);
//
//			try {
//				result.moveToFirst();
//
//				if (result.getInt(0) != 0) {
//					previousEventOldestOccurrence[0] = result.getLong(1);
//
//					if (enforceOrder) {
//						foundEventCount[0]++;
//					} else {
//						foundEventCount[0] += result.getInt(0);
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		// for ordered searches, if found event count matches the total number of requests, then all requests were found. return 1 / true.
//		if (enforceOrder) {
//			if (foundEventCount[0] == eventHistoryRequests.length) {
//				handler.call(1);
//			} else {
//				handler.call(0);
//			}
//		} else { // for "any" search, return total number of matching events
//			handler.call(foundEventCount[0]);
//		}
//	}
//
//	@Override
//	public void deleteEvents(EventHistoryRequest[] eventHistoryRequests, EventHistoryResultHandler<Integer> handler) {
//		final int[] deleteCount = {0};
//
//		for (EventHistoryRequest request : eventHistoryRequests) {
//			// if no "from" date is provided, delete from the beginning of the database
//			long from = request.fromDate == 0 ? 0 : request.fromDate;
//			// if no "to" date is provided, delete until the end of the database
//			long to = request.toDate == 0 ? System.currentTimeMillis() : request.toDate;
//			Map<String, Variant> flattenedMask = EventDataFlattener.getFlattenedEventDataMask(request.mask);
//			SortedMap<String, Variant> sortedMap = new TreeMap<>(flattenedMask);
//			long eventHash = StringEncoder.convertMapToDecimalHash(sortedMap);
//			deleteCount[0] += fakeEventHistoryDatabase.delete(eventHash, from, to);
//		}
//
//		handler.call(deleteCount[0]);
//	}
//}