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
 * A rule condition matcher which evaluates if a given value equals any of this matcher's {@code values}.
 */
class MatcherEquals extends Matcher {
	private final static String FALSE_STRING = "false";
	private final static String TRUE_STRING = "true";
	private final static String ONE_STRING = "1";
	private final static String ZERO_STRING = "0";

	/**
	 * Evaluates the given {@code value} against this {@code Matcher}'s values.
	 * Valid data types for {@code value} are {@link String} and {@link Number}. All other data types
	 * will evaluate to false.
	 *
	 * @param value the value to match
	 * @return true if {@code value} equals any of this {@code Matcher}'s values, ignoring case
	 */
	@Override
	@SuppressWarnings({  "squid:S3776"})
	//S3776 - Complexity warning. The below code is comprehensible enough that we can disable this warning here.
	protected boolean matches(final Object value) {
		for (Object potentialMatch : values) {
			// string v string OR string v Number
			if (potentialMatch instanceof String && value instanceof String ||
					potentialMatch instanceof String && value instanceof Number) {
				if (potentialMatch.toString().compareToIgnoreCase(value.toString()) == 0) {
					return true;
				}
			}
			// number v number
			else if (potentialMatch instanceof Number && value instanceof Number) {
				if (Double.compare(((Number) potentialMatch).doubleValue(), ((Number) value).doubleValue()) == 0) {
					return true;
				}
			}
			// number v string
			else if (potentialMatch instanceof Number && value instanceof String) {
				Double valueAsDouble = tryParseDouble(value);

				if (valueAsDouble != null && Double.compare(((Number) potentialMatch).doubleValue(), valueAsDouble) == 0) {
					return true;
				}
			}
			// boolean v string (according to the current code where this method gets called, value can only be string)
			else if (potentialMatch instanceof Boolean) {
				return compareObjectWithBoolean(value, ((Boolean) potentialMatch));
			}
		}

		return false;
	}

	protected boolean compareObjectWithBoolean(final Object object, final boolean booleanValue) {
		if (object instanceof Boolean) {
			return (Boolean)object == booleanValue;
		} else if (object instanceof Integer || object instanceof Long) {
			long objectAsLong = ((Number)object).longValue();
			return (objectAsLong == 1 && booleanValue) || (objectAsLong == 0 && !booleanValue);
		} else if (object instanceof String) {
			String objectAsString = (String)object;
			return booleanValue ? (ONE_STRING.equals(objectAsString) || TRUE_STRING.equalsIgnoreCase(objectAsString)) :
				   (ZERO_STRING.equals(objectAsString) || FALSE_STRING.equalsIgnoreCase(objectAsString));
		}

		return false;
	}

	@Override
	public String toString() {
		StringBuilder matcherStringBuilder = new StringBuilder();

		for (Object value : values) {
			if (matcherStringBuilder.length() > 0) {
				matcherStringBuilder.append(" OR ");
			}

			matcherStringBuilder.append(key);
			matcherStringBuilder.append(" EQUALS ");
			matcherStringBuilder.append(value.toString());
		}

		matcherStringBuilder.insert(0, "(");
		matcherStringBuilder.append(")");
		return matcherStringBuilder.toString();
	}
}
