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
 * Implementation of Variant for {@code VariantKind.STRING}.
 *
 * Intended for use by Variant only.
 */
final class StringVariant extends Variant implements Cloneable {

	private final String value;

	/**
	 * @return a {@code StringVariant} containing the specified value
	 * @param value {@code String} value for the new variant.  Cannot be null.
	 */
	public static Variant from(final String value) {
		return new StringVariant(value);
	}

	private StringVariant(final String value) {
		if (value == null) {
			throw new IllegalArgumentException();
		}

		this.value = value;
	}

	private StringVariant(final StringVariant right) {
		if (right == null) {
			throw new IllegalArgumentException();
		}

		this.value = right.value;
	}

	@Override
	public VariantKind getKind() {
		return VariantKind.STRING;
	}

	@Override
	public String getString() {
		return value;
	}

	@Override
	public String convertToString() {
		return value;
	}

	@Override
	public double convertToDouble() throws VariantException {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw new VariantException(e);
		}
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		final String QUOTE = "\"";
		final String SLASH = "\\";
		b.append(QUOTE);
		b.append(value.replaceAll(QUOTE, SLASH + QUOTE));
		b.append(QUOTE);
		return b.toString();
	}

	@Override
	public final StringVariant clone() {
		return new StringVariant(this);
	}
}
