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

/**
 * A rule condition matcher which is unknown. This class is used as a marker to denote a {@code Matcher} instance
 * which is not of a known type. It always evaluates to false.
 */
class MatcherUnknown extends Matcher {

	/**
	 * Evaluates to false for this unknown {@code Matcher} type.
	 *
	 * @param value the value to match
	 *
	 * @return false, always.
	 */
	@Override
	public boolean matches(final Object value) {
		return false;
	}

	@Override public String toString() {
		return "(UNKNOWN)";
	}
}
