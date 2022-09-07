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

final class ContextData {
	protected Object value = null;
	protected Map<String, Object> data = new HashMap<String, Object>();

	@Override

	public synchronized String toString() {
		String addString = "";

		if (value != null) {
			addString = value.toString();
		}

		return super.toString() + addString;
	}

	protected boolean containsKey(final String key) {
		return data.containsKey(key);
	}

	protected void put(final String key, final ContextData value) {
		data.put(key, value);
	}

	protected ContextData get(final String key) {
		return (ContextData) data.get(key);
	}

	protected int size() {
		return data.size();
	}
}
