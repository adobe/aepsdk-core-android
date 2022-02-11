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
 * VariantSerializer implementation for strings.
 *
 * Used to implement string maps and string lists.
 */
final class StringVariantSerializer implements VariantSerializer<String> {

	public StringVariantSerializer() {}

	/**
	 * @return {@code s} as a string variant.
	 */
	@Override
	public Variant serialize(final String s) {
		if (s == null) {
			return Variant.fromNull();
		}

		return Variant.fromString(s);
	}

	/**
	 * @return {@code v.convertToString()}
	 */
	@Override
	public String deserialize(final Variant v) throws VariantException {
		if (v == null) {
			throw new IllegalArgumentException();
		}

		if (v.getKind() == VariantKind.NULL) {
			return null;
		}

		return v.convertToString();
	}
}
