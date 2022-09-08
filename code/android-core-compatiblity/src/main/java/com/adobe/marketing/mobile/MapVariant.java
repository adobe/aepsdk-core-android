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

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of Variant for {@code VariantKind.MAP}.
 *
 * Intended for use by Variant only.
 */
final class MapVariant extends Variant implements Cloneable {
	private HashMap<String, Variant> values;

	/**
	 * @return a {@code MapVariant} containing the specified value
	 * @param map {@code Map<String, Variant>} value for the new variant.  Cannot be null.
	 */
	public static MapVariant from(final Map<String, Variant> map) {
		return new MapVariant(map);
	}

	private MapVariant(final MapVariant right) {
		if (right == null) {
			throw new IllegalArgumentException();
		}

		this.values = right.values; // don't need to copy, since we know the map doesn't contain nulls
	}

	private MapVariant(final Map<String, Variant> values) {
		if (values == null) {
			throw new IllegalArgumentException();
		}

		// copy values, removing any nulls
		this.values = new HashMap<String, Variant>();

		for (final Map.Entry<String, Variant> entry : values.entrySet()) {
			final String key = entry.getKey();
			Variant value = entry.getValue();

			if (key == null) {
				continue; // skip any null keys
			}

			if (value == null) {
				value = Variant.fromNull();
			}

			this.values.put(key, value);
		}
	}

	@Override
	public VariantKind getKind() {
		return VariantKind.MAP;
	}

	@Override
	public Map<String, Variant> getVariantMap() {
		// copy to keep this immutable
		return new HashMap<String, Variant>(values);
	}

	@Override
	public MapVariant clone() {
		return new MapVariant(this);
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("{");

		boolean isFirst = true;

		for (final Map.Entry<String, Variant> entry : values.entrySet()) {
			if (isFirst) {
				isFirst = false;
			} else {
				b.append(",");
			}

			final String quote = "\"";
			final String slashQuote = "\\\"";
			b.append(quote);
			b.append(entry.getKey().replaceAll(quote, slashQuote));
			b.append(quote);
			b.append(":");
			b.append(entry.getValue().toString());
		}

		b.append("}");
		return b.toString();
	}
}

