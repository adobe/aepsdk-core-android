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
 * Implementation of Variant for {@code VariantKind.INTEGER}.
 *
 * Intended for use by Variant only.
 */
final class IntegerVariant extends Variant implements Cloneable {
	private final int value;

	/**
	 * @return an {@code IntegerVariant} containing the specified value
	 * @param value {@code int} value for the new variant
	 */
	public static Variant from(final int value) {
		return new IntegerVariant(value);
	}

	private IntegerVariant(final int value) {
		this.value = value;
	}

	private IntegerVariant(final IntegerVariant right) {
		if (right == null) {
			throw new IllegalArgumentException();
		}

		this.value = right.value;
	}

	@Override
	public VariantKind getKind() {
		return VariantKind.INTEGER;
	}

	@Override
	public int getInteger() {
		return value;
	}

	@Override
	public long getLong() {
		return (long)value;
	}

	@Override
	public double getDouble() {
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
	public IntegerVariant clone() {
		return new IntegerVariant(this);
	}

	@Override
	public String toString() {
		return convertToString();
	}
}
