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

final class ObjectUtil {
	/**
	 * Backfill for {@code Objects.equals(Object, Object)}, which is only available in api 17+
	 *
	 * @param a - (nullable) first object to compare
	 * @param b - (nullable) second object to compare
	 * @return whether a and b are equal
	 */
	static boolean equals(final Object a, final Object b) {
		if (a == b) {
			return true;
		}

		if (a == null) {
			return false;
		}

		return a.equals(b);
	}

	/**
	 * Backfill for {@code Objects.hashCode(Object)}, which is only available in api 17+
	 *
	 * @param o - (nullable) object
	 * @return {@code o.hashCode()} or 0 if o is null
	 */
	static int hashCode(final Object o) {
		if (o == null) {
			return 0;
		} else {
			return o.hashCode();
		}
	}
}
