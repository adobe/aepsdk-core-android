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

import java.util.regex.Pattern;

/**
 * A rule condition matcher which evaluates if a given value ends with any of this matcher's {@code values}.
 */
final class MatcherEndsWith extends Matcher {

	/**
	 * Evaluates the given {@code value} against this {@code Matcher}'s values.
	 * Valid data types for {@code value} are {@link String} and {@link Number}. All other data types
	 * will evaluate to false.
	 *
	 * @param value the value to match
	 * @return true if {@code value} ends with any of this {@code Matcher}'s values
	 */
	@Override
	protected boolean matches(final Object value) {
		boolean valueIsString = value instanceof String;
		boolean valueIsNumber = value instanceof Number;

		if (!valueIsString && !valueIsNumber) {
			return false;
		}

		String stringToMatch = value.toString();

		for (Object v : values) {
			if (v instanceof String && stringToMatch.matches("(?i).*" + Pattern.quote(v.toString()))) {
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
			matcherStringBuilder.append(" ENDS WITH ");
			matcherStringBuilder.append(value.toString());
		}

		matcherStringBuilder.insert(0, "(");
		matcherStringBuilder.append(")");
		return matcherStringBuilder.toString();
	}
}
