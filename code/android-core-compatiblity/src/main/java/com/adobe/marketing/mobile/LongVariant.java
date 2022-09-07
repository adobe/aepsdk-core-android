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
 * Implementation of Variant for {@code VariantKind.LONG}.
 *
 * Intended for use by Variant only.
 */
final class LongVariant extends Variant {
	private final long value;

	/**
	 * @return a {@code LongVariant} containing the specified value
	 * @param value {@code long} value for the new variant
	 */
	public static Variant from(final long value) {
		return new LongVariant(value);
	}

	private LongVariant(final long value) {
		if (value > Variant.MAX_SAFE_INTEGER || value < Variant.MIN_SAFE_INTEGER) {
			throw new IllegalArgumentException("Integer cannot be stored accurately in a Variant");
		}

		this.value = value;
	}

	private LongVariant(final LongVariant right) {
		if (right == null) {
			throw new IllegalArgumentException();
		}

		this.value = right.value;
	}

	@Override
	public VariantKind getKind() {
		return VariantKind.LONG;
	}

	@Override
	public int getInteger() throws VariantException {
		if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
			throw new VariantRangeException("long value is not expressible as an int");
		}

		return (int)value;
	}

	@Override
	public long getLong() {
		return value;
	}

	@Override
	public double getDouble() {
		// accept possible loss of precision here
		return (double)value;
	}

	@Override
	public String convertToString() {
		return String.valueOf(value);
	}

	@Override
	public double convertToDouble() {
		return getDouble();
	}

	@Override
	public LongVariant clone() {
		return new LongVariant(this);
	}

	@Override
	public String toString() {
		return convertToString();
	}
}
