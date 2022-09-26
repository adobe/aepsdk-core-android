///* *****************************************************************************
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2018 Adobe
// * All Rights Reserved.
// *
// * NOTICE: All information contained herein is, and remains
// * the property of Adobe and its suppliers, if any. The intellectual
// * and technical concepts contained herein are proprietary to Adobe
// * and its suppliers and are protected by all applicable intellectual
// * property laws, including trade secret and copyright laws.
// * Dissemination of this information or reproduction of this material
// * is strictly forbidden unless prior written permission is obtained
// * from Adobe.
// ******************************************************************************/
//
//package com.adobe.marketing.mobile.identity;
//
//import com.adobe.marketing.mobile.Event;
//import com.adobe.marketing.mobile.Log;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * Extends {@link AbstractHitSchema} and defines the structure for the IdentityExtension database table
// */
//final class IdentityHitSchema extends AbstractHitSchema<IdentityHit> {
//
//	private static final String LOG_TAG = "IdentityHitSchema";
//
//
//	private static final int COL_INDEX_REQUESTS_ID           = 0;
//	private static final int COL_INDEX_REQUESTS_URL          = 1;
//	private static final int COL_INDEX_REQUESTS_TIMESTAMP    = 2;
//	private static final int COL_INDEX_REQUESTS_PAIR_ID		 = 3;
//	private static final int COL_INDEX_REQUESTS_EVENT_NUMBER = 4;
//	private static final int COL_INDEX_REQUESTS_SSL          = 5;
//
//	private static final String COL_REQUESTS_ID	          = "ID";
//	private static final String COL_REQUESTS_URL          = "URL";
//	private static final String COL_REQUESTS_CONFIG_SSL   = "SSL";
//	private static final String COL_REQUESTS_PAIR_ID      = "PAIR_ID";
//	private static final String COL_REQUESTS_EVENT_NUMBER = "EVENT_NUMBER";
//	private static final String COL_REQUESTS_TIMESTAMP    = "TIMESTAMP";
//
//	/**
//	 * Constructor
//	 * <ul>
//	 *     <li>Initializes and populates {@link #columnConstraints}</li>
//	 *     <li>Initializes and populates {@link #columnNames}</li>
//	 *     <li>Initializes and populates {@link #columnDataTypes}</li>
//	 * </ul>
//	 */
//	IdentityHitSchema() {
//
//		// Note : Since we removed the reference to global.ssl as in AMSDK-8429, the DB Column COL_REQUESTS_CONFIG_SSL will not be used for any new data
//
//		this.columnConstraints =
//			new ArrayList<List<DatabaseService.Database.ColumnConstraint>>();
//
//		// create constraints for the id column
//		final List<DatabaseService.Database.ColumnConstraint> idColumnConstraints =
//			new ArrayList<DatabaseService.Database.ColumnConstraint>();
//		idColumnConstraints.add(DatabaseService.Database.ColumnConstraint.PRIMARY_KEY);
//		idColumnConstraints.add(DatabaseService.Database.ColumnConstraint.AUTOINCREMENT);
//
//		columnConstraints.add(idColumnConstraints);                                         // id
//		columnConstraints.add(new ArrayList<DatabaseService.Database.ColumnConstraint>()); 	// url
//		columnConstraints.add(new ArrayList<DatabaseService.Database.ColumnConstraint>()); 	// timestamp
//		columnConstraints.add(new ArrayList<DatabaseService.Database.ColumnConstraint>()); 	// pair id
//		columnConstraints.add(new ArrayList<DatabaseService.Database.ColumnConstraint>()); 	// event number
//		columnConstraints.add(new ArrayList<DatabaseService.Database.ColumnConstraint>()); 	// ssl
//
//		this.columnNames = new String[] {
//			COL_REQUESTS_ID,
//			COL_REQUESTS_URL,
//			COL_REQUESTS_TIMESTAMP,
//			COL_REQUESTS_PAIR_ID,
//			COL_REQUESTS_EVENT_NUMBER,
//			COL_REQUESTS_CONFIG_SSL
//		};
//
//		this.columnDataTypes = new DatabaseService.Database.ColumnDataType[] {
//			DatabaseService.Database.ColumnDataType.INTEGER, // id
//			DatabaseService.Database.ColumnDataType.TEXT,    // url
//			DatabaseService.Database.ColumnDataType.INTEGER, // timestamp
//			DatabaseService.Database.ColumnDataType.TEXT,    // pair id
//			DatabaseService.Database.ColumnDataType.INTEGER, // event number
//			DatabaseService.Database.ColumnDataType.INTEGER  // ssl
//		};
//	}
//
//	/**
//	 * Marshals the provided {@link com.adobe.marketing.mobile.DatabaseService.QueryResult} into an equivalent
//	 * {@link IdentityHit} instance
//	 * <p>
//	 * If an exception occurs while processing the queryResult, null will be returned
//	 *
//	 * @param queryResult {@code QueryResult} instance representing a record in the {@link IdentityExtension} database
//	 * @return {@code IdentityHit} represented by the provided query result
//	 */
//	@Override
//	IdentityHit generateHit(final DatabaseService.QueryResult queryResult) {
//		try {
//			final IdentityHit identityHit = new IdentityHit();
//			identityHit.identifier = queryResult.getString(COL_INDEX_REQUESTS_ID);
//			identityHit.url = queryResult.getString(COL_INDEX_REQUESTS_URL);
//			identityHit.timestamp = queryResult.getInt(COL_INDEX_REQUESTS_TIMESTAMP);
//			identityHit.pairId = queryResult.getString(COL_INDEX_REQUESTS_PAIR_ID);
//			identityHit.eventNumber = queryResult.getInt(COL_INDEX_REQUESTS_EVENT_NUMBER);
//			identityHit.configSSL = queryResult.getInt(COL_INDEX_REQUESTS_SSL) == 1;
//			return identityHit;
//		} catch (final Exception e) {
//			Log.error(LOG_TAG, "generateHit : Unable to read the Identity hits from the database due to an error: (%s).", e);
//			return null;
//		} finally {
//			if (queryResult == null) {
//				Log.trace(LOG_TAG, "generateHit : Unable to generate Identity database hit, query result was null.");
//			} else {
//				Log.trace(LOG_TAG, "generateHit : The Identity hits were successfully fetched from the database.");
//				queryResult.close();
//			}
//		}
//	}
//
//	/**
//	 * Generates a {@link Map} to be used for a database insert operation
//	 *
//	 * @param hit {@link IdentityHit} representing the record that is to be created
//	 * @return {@code Map<String, Object>} containing proper {@code IdentityHit} data in their respective database
//	 * column indexes
//	 */
//	@Override
//	Map<String, Object> generateDataMap(final IdentityHit hit) {
//		final Map<String, Object> dataMap = new HashMap<String, Object>();
//		dataMap.put(COL_REQUESTS_URL, hit.url);
//		dataMap.put(COL_REQUESTS_TIMESTAMP, hit.timestamp);
//		dataMap.put(COL_REQUESTS_PAIR_ID, hit.pairId);
//		dataMap.put(COL_REQUESTS_EVENT_NUMBER, hit.eventNumber);
//		dataMap.put(COL_REQUESTS_CONFIG_SSL, hit.configSSL);
//		return dataMap;
//
//	}
//
//	/**
//	 * Generates a {@link Map} to be used for a database update operation
//	 *
//	 * When an {@link IdentityExtension} database is initialized with data already in it, the {@link Event#eventNumber}
//	 * and {@link Event#pairID} values are no longer valid due to the {@link EventHub} resetting.  This method
//	 * facilitates the updating of those values for pre-existing {@code IdentityExtension} requests in the database
//	 *
//	 * @return {@code Map<String, Object>} containing "reset" values for EventNumber and PairID columns
//	 */
//	Map<String, Object> generateUpdateValuesForResetEventNumberAndPairId() {
//		final Map<String, Object> updateValues = new HashMap<String, Object>();
//		updateValues.put(COL_REQUESTS_PAIR_ID, "");
//		updateValues.put(COL_REQUESTS_EVENT_NUMBER, -1);
//		return updateValues;
//	}
//}
