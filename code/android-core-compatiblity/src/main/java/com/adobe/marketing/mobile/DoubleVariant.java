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
 * Implementation of Variant for {@code VariantKind.DOUBLE}.
 *
 * Intended for use by Variant only.
 */
final class DoubleVariant extends Variant {
	private final double value;

	/**
	 * @return a {@code DoubleVariant} containing the specified value
	 * @param value {@code double} value for the new variant
	 */
	public static Variant from(final double value) {
		return new DoubleVariant(value);
	}

	private DoubleVariant(final double value) {
		this.value = value;
	}

	private DoubleVariant(final DoubleVariant right) {
		if (right == null) {
			throw new IllegalArgumentException();
		}

		this.value = right.value;
	}

	@Override
	public VariantKind getKind() {
		return VariantKind.DOUBLE;
	}

	@Override
	public int getInteger() throws VariantException {
		final long valueAsLong = getLong();

		if (valueAsLong > (long)Integer.MAX_VALUE || valueAsLong < (long)Integer.MIN_VALUE) {
			// if the long is not expressible as an int, give up
			throw new VariantRangeException("double value is not expressible as an int");
		}

		return (int)valueAsLong;
	}

	@Override
	public long getLong() throws VariantException {
		final double longMaxAsDouble = (double)Long.MAX_VALUE;
		final double longMinAsDouble = (double)Long.MIN_VALUE;

		if (value > longMaxAsDouble || value < longMinAsDouble || Double.isNaN(value) || Double.isInfinite(value)) {
			// if the double is not expressible as an long, give up
			throw new VariantRangeException("double value is not expressible as a long");
		}

		// round because if an integral type is stored as a double, it may resolve to something
		// like "41.9999" or "42.00001" where "42" is more correct than "41"
		final double roundedValue = Math.rint(value);

		if (roundedValue > longMaxAsDouble || roundedValue < longMinAsDouble || Double.isNaN(roundedValue)
				|| Double.isInfinite(roundedValue)) {
			// if the double is not expressible as an long, give up
			throw new VariantRangeException("double value is not expressible as a long");
		}

		return Math.round(roundedValue);
	}

	@Override
	public double getDouble() {
		return value;
	}

	@Override
	public String convertToString() {
		return String.valueOf(value);
	}

	@Override
	public double convertToDouble() {
		return value;
	}

	@Override
	public DoubleVariant clone() {
		return new DoubleVariant(this);
	}

	@Override
	public String toString() {
		// TODO: match this behavior on C++
		return convertToString();
	}
}
