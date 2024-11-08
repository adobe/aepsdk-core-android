/*
  Copyright 2021 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MockDataStoreService implements DataStoring {

	private static final ConcurrentMap<String, NamedCollection> stores = new ConcurrentHashMap<>();

	public static void clearStores() {
		stores.clear();
	}

	@Override
	public NamedCollection getNamedCollection(String s) {
		if (stores.containsKey(s)) {
			return stores.get(s);
		}

		NamedCollection newStore = new MockTestDataStore();
		stores.put(s, newStore);
		return newStore;
	}

	public static class MockTestDataStore implements NamedCollection {

		private final ConcurrentMap<String, Object> store;

		public MockTestDataStore() {
			this.store = new ConcurrentHashMap<>();
		}

		@Override
		public void setInt(String s, int i) {
			store.put(s, i);
		}

		@Override
		public int getInt(String s, int i) {
			if (store.containsKey(s)) {
				return (int) store.get(s);
			}

			return i;
		}

		@Override
		public void setString(String s, String s1) {
			store.put(s, s1);
		}

		@Override
		public String getString(String s, String s1) {
			if (store.containsKey(s)) {
				return (String) store.get(s);
			}

			return s1;
		}

		@Override
		public void setDouble(String s, double v) {
			store.put(s, v);
		}

		@Override
		public double getDouble(String s, double v) {
			if (store.containsKey(s)) {
				return (double) store.get(s);
			}

			return v;
		}

		@Override
		public void setLong(String s, long l) {
			store.put(s, l);
		}

		@Override
		public long getLong(String s, long l) {
			if (store.containsKey(s)) {
				return (long) store.get(s);
			}

			return l;
		}

		@Override
		public void setFloat(String s, float v) {
			store.put(s, v);
		}

		@Override
		public float getFloat(String s, float v) {
			if (store.containsKey(s)) {
				return (float) store.get(s);
			}

			return v;
		}

		@Override
		public void setBoolean(String s, boolean b) {
			store.put(s, b);
		}

		@Override
		public boolean getBoolean(String s, boolean b) {
			if (store.containsKey(s)) {
				return (boolean) store.get(s);
			}

			return b;
		}

		@Override
		public void setMap(String s, Map<String, String> map) {
			store.put(s, map);
		}

		@Override
		public Map<String, String> getMap(String s) {
			if (store.containsKey(s)) {
				return (Map<String, String>) store.get(s);
			}

			return new HashMap<>();
		}

		@Override
		public boolean contains(String s) {
			return store.containsKey(s);
		}

		@Override
		public void remove(String s) {
			store.remove(s);
		}

		@Override
		public void removeAll() {
			store.clear();
		}
	}
}
