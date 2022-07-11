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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
    Define the database schema for signal request
 */

//TODO refactor to use public hit queue
/*
class SignalHitSchema extends AbstractHitSchema<SignalHit> {
	private static final String LOG_TAG = "SignalHitType";

	private static final String HIT_ID_COL_NAME = "ID";
	private static final int HIT_ID_COL_INDEX = 0;
	private static final String HIT_URL_COL_NAME = "URL";
	private static final int HIT_URL_COL_INDEX = 1;
	private static final String HIT_TIMESTAMP_COL_NAME = "TIMESTAMP";
	private static final int HIT_TIMESTAMP_COL_INDEX = 2;
	private static final String HIT_POSTBODY_COL_NAME = "POSTBODY";
	private static final int HIT_POSTBODY_COL_INDEX = 3;
	private static final String HIT_CONTENTTYPE_COL_NAME = "CONTENTTYPE";
	private static final int HIT_CONTENTTYPE_COL_INDEX = 4;
	private static final String HIT_TIMEOUT_COL_NAME = "TIMEOUT";
	private static final int HIT_TIMEOUT_COL_INDEX = 5;

	SignalHitSchema() {
		this.columnConstraints =
			new ArrayList<List<DatabaseService.Database.ColumnConstraint>>();
		List<DatabaseService.Database.ColumnConstraint> idColumnConstraints =
			new ArrayList<DatabaseService.Database.ColumnConstraint>();
		idColumnConstraints.add(DatabaseService.Database.ColumnConstraint.PRIMARY_KEY);
		idColumnConstraints.add(DatabaseService.Database.ColumnConstraint.AUTOINCREMENT);
		this.columnConstraints.add(idColumnConstraints);
		this.columnConstraints.add(new ArrayList<DatabaseService.Database.ColumnConstraint>());
		this.columnConstraints.add(new ArrayList<DatabaseService.Database.ColumnConstraint>());
		this.columnConstraints.add(new ArrayList<DatabaseService.Database.ColumnConstraint>());
		this.columnConstraints.add(new ArrayList<DatabaseService.Database.ColumnConstraint>());
		this.columnConstraints.add(new ArrayList<DatabaseService.Database.ColumnConstraint>());

		this.columnNames = new String[] {HIT_ID_COL_NAME, HIT_URL_COL_NAME, HIT_TIMESTAMP_COL_NAME
										 , HIT_POSTBODY_COL_NAME, HIT_CONTENTTYPE_COL_NAME, HIT_TIMEOUT_COL_NAME
										};

		this.columnDataTypes = new DatabaseService.Database.ColumnDataType[] {
			DatabaseService.Database.ColumnDataType.INTEGER,
			DatabaseService.Database.ColumnDataType.TEXT,
			DatabaseService.Database.ColumnDataType.INTEGER,
			DatabaseService.Database.ColumnDataType.TEXT,
			DatabaseService.Database.ColumnDataType.TEXT,
			DatabaseService.Database.ColumnDataType.INTEGER,
		};
	}

	@Override
	SignalHit generateHit(final DatabaseService.QueryResult queryResult) {
		try {

			SignalHit signalHit = new SignalHit();
			signalHit.identifier = queryResult.getString(HIT_ID_COL_INDEX);
			signalHit.url = queryResult.getString(HIT_URL_COL_INDEX);
			signalHit.timestamp = queryResult.getLong(HIT_TIMESTAMP_COL_INDEX);
			signalHit.body = queryResult.getString(HIT_POSTBODY_COL_INDEX);
			signalHit.contentType = queryResult.getString(HIT_CONTENTTYPE_COL_INDEX);
			signalHit.timeout = queryResult.getInt(HIT_TIMEOUT_COL_INDEX);
			return signalHit;
		} catch (Exception e) {
			Log.error(LOG_TAG, "Unable to read from database. Query failed with error %s", e);
			return null;
		} finally {
			if (queryResult != null) {
				queryResult.close();
			}
		}
	}

	@Override
	Map<String, Object> generateDataMap(final SignalHit hit) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put(HIT_URL_COL_NAME, hit.url);
		dataMap.put(HIT_TIMESTAMP_COL_NAME, hit.timestamp);
		dataMap.put(HIT_POSTBODY_COL_NAME, hit.body);
		dataMap.put(HIT_CONTENTTYPE_COL_NAME, hit.contentType);
		dataMap.put(HIT_TIMEOUT_COL_NAME, hit.timeout);
		return dataMap;
	}

}
*/
