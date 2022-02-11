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

import java.util.HashMap;
import java.util.Map;

public class FakeDatabaseService implements DatabaseService {
	private final Object dbServiceMutex = new Object();
	Map<String, Database> mapping;

	public FakeDatabaseService() {
		synchronized (dbServiceMutex) {
			mapping = new HashMap<String, Database>();
		}
	}


	@Override
	public Database openDatabase(final String filePath) {
		synchronized (dbServiceMutex) {
			if (!mapping.containsKey(filePath)) {
				mapping.put(filePath, new FakeDatabase());
			}

			return mapping.get(filePath);
		}
	}

	@Override
	public boolean deleteDatabase(final String filePath) {
		synchronized (dbServiceMutex) {
			if (mapping.containsKey(filePath)) {
				mapping.remove(filePath);
				return true;
			}

			return false;
		}
	}
}
