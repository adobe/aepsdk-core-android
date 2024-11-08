/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util;

import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.services.ServiceProvider;
import java.util.ArrayList;

/**
 * Helper class to update and remove persisted data to extension concerned with testing Identity.
 */
public class TestPersistenceHelper {

	private static ArrayList<String> knownDatastoreName = new ArrayList<String>() {
		{
			add(TestConstants.DataStoreKey.IDENTITY_DATASTORE);
			add(TestConstants.DataStoreKey.CONFIG_DATASTORE);
			add(TestConstants.DataStoreKey.IDENTITY_DIRECT_DATASTORE);
		}
	};

	/**
	 * Helper method to update the {@link NamedCollection} data.
	 *
	 * @param datastore the name of the datastore to be updated
	 * @param key       the persisted data key that has to be updated
	 * @param value     the new value
	 */
	public static void updatePersistence(final String datastore, final String key, final String value) {
		NamedCollection dataStore = ServiceProvider.getInstance().getDataStoreService().getNamedCollection(datastore);

		dataStore.setString(key, value);
	}

	/**
	 * Reads the requested persisted data from datastore.
	 *
	 * @param datastore the name of the datastore to be read
	 * @param key       the key that needs to be read
	 * @return {@link String} value of persisted data. {@code null} if data is not found in {@link NamedCollection}
	 */
	public static String readPersistedData(final String datastore, final String key) {
		NamedCollection dataStore = ServiceProvider.getInstance().getDataStoreService().getNamedCollection(datastore);

		return dataStore.getString(key, null);
	}

	/**
	 * Clears the Configuration and Consent extension's persisted data
	 */
	public static void resetKnownPersistence() {
		for (String eachDatastore : knownDatastoreName) {
			NamedCollection dataStore = ServiceProvider
				.getInstance()
				.getDataStoreService()
				.getNamedCollection(eachDatastore);

			dataStore.removeAll();
		}
	}
}
