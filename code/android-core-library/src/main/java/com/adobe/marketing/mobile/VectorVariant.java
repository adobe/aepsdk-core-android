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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Implementation of Variant for {@code VariantKind.VECTOR}.
 *
 * Intended for use by Variant only.
 */
final class VectorVariant extends Variant implements Cloneable {
	private ArrayList<Variant> values;

	/**
	 * @return a {@code VectorVariant} containing the specified value
	 * @param values {@code List<Variant>} value for the new variant.  Cannot be null.
	 */
	public static VectorVariant from(final List<Variant> values) {
		return new VectorVariant(values);
	}

	private VectorVariant(final List<Variant> values) {
		if (values == null) {
			throw new IllegalArgumentException();
		}

		this.values = new ArrayList<Variant>();

		for (Variant item : values) {
			if (item == null) {
				this.values.add(Variant.fromNull());
			} else {
				this.values.add(item);
			}
		}
	}

	private VectorVariant(final VectorVariant right) {
		if (right == null) {
			throw new IllegalArgumentException();
		}

		this.values = right.values;
	}

	@Override
	public VariantKind getKind() {
		return VariantKind.VECTOR;
	}

	@Override
	public List<Variant> getVariantList() {
		return new ArrayList<Variant>(values);
	}

	@Override
	public VectorVariant clone() {
		return new VectorVariant(this);
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("[");

		boolean isFirst = true;

		for (final Variant element : values) {
			if (isFirst) {
				isFirst = false;
			} else {
				b.append(",");
			}

			b.append(element.toString());
		}

		b.append("]");
		return b.toString();
	}
}
