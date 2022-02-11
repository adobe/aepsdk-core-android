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

/**
 * A rule condition matcher which evaluates if a given value is less than or equal to
 * any of this matcher's {@code values}.
 */
final class MatcherLessThanOrEqual extends Matcher {

	/**
	 * Evaluates the given {@code value} against this {@code Matcher}'s values.
	 * Parses {@code value} as {@link Double} data type before evaluation. If {@code value} is not parsable
	 * to {@code Double}, then false is returned.
	 *
	 * @param value the value to match
	 * @return true if {@code value} is less than or equal to any of this {@code Matcher}'s values
	 */
	@Override
	protected boolean matches(final Object value) {
		if (value == null) {
			return false;
		}

		// figure out if value is a number
		Double valueAsDouble = tryParseDouble(value);

		if (valueAsDouble == null) {
			return false;
		}

		for (Object v : values) {
			if (v instanceof Number && valueAsDouble <= ((Number) v).doubleValue()) {
				return true;
			}
		}

		return false;
	}

	@Override public String toString() {
		StringBuilder matcherStringBuilder = new StringBuilder();

		for (Object value : values) {
			if (matcherStringBuilder.length() > 0) {
				matcherStringBuilder.append(" OR ");
			}

			matcherStringBuilder.append(key);
			matcherStringBuilder.append(" LESS THAN OR EQUALS ");
			matcherStringBuilder.append(value.toString());
		}

		matcherStringBuilder.insert(0, "(");
		matcherStringBuilder.append(")");
		return matcherStringBuilder.toString();
	}
}
