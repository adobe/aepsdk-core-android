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
 * Implementation of Variant for {@code VariantKind.BOOLEAN}.
 *
 * Intended for use by Variant only.
 */
final class BooleanVariant extends Variant {
	private final static BooleanVariant TRUE = new BooleanVariant(true);
	private final static BooleanVariant FALSE = new BooleanVariant(false);

	private final boolean value;

	/**
	 * @return a {@code BooleanVariant} containing the specified value
	 * @param value {@code boolean} the value for the new variant
	 */
	public static Variant from(final boolean value) {
		return value ? TRUE : FALSE;
	}

	private BooleanVariant(final boolean value) {
		this.value = value;
	}

	private BooleanVariant(final BooleanVariant right) {
		if (right == null) {
			throw new IllegalArgumentException();
		}

		this.value = right.value;
	}

	@Override
	public VariantKind getKind() {
		return VariantKind.BOOLEAN;
	}

	@Override
	public boolean getBoolean() {
		return value;
	}

	@Override
	public double convertToDouble() {
		return value ? 1.0 : 0.0;
	}

	@Override
	public String convertToString() {
		return value ? "true" : "false";
	}

	@Override
	public String toString() {
		return convertToString();
	}

	@Override
	public final BooleanVariant clone() {
		return new BooleanVariant(this);
	}
}
