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

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public abstract class TypedVariantTests extends VariantTests {
	protected static class Food {
		public final String name;
		public final String taste;

		public Food(final String name, final String taste) {
			this.name = name;
			this.taste = taste;
		}

		@Override
		public boolean equals(final Object right) {
			if (this == right) {
				return true;
			}

			if (right == null) {
				return false;
			}

			if (getClass() != right.getClass()) {
				return false;
			}

			final Food rightFood = (Food)right;

			if (!name.equals(rightFood.name)) {
				return false;
			}

			if (!taste.equals(rightFood.taste)) {
				return false;
			}

			return true;
		}
	}

	protected static class Fruit extends Food {
		public final int seedCount;

		public Fruit(final int seedCount, final String taste) {
			super("fruit", taste);
			this.seedCount = seedCount;
		}

		@Override
		public boolean equals(Object right) {
			if (!super.equals(right)) {
				return false;
			}

			final Fruit rightFruit = (Fruit)right;

			if (seedCount != rightFruit.seedCount) {
				return false;
			}

			return true;
		}
	}

	protected static class FoodSerializer implements VariantSerializer<Food> {
		public FoodSerializer() {}

		@Override
		public Variant serialize(final Food food) throws VariantException {
			if (food == null) {
				return Variant.fromNull();
			}

			final Map<String, Variant> map = new HashMap<String, Variant>();
			map.put("name", Variant.fromString(food.name));
			map.put("taste", Variant.fromString(food.taste));

			if (food instanceof Fruit) {
				final Fruit fruit = (Fruit)food;
				map.put("seedCount", Variant.fromInteger(fruit.seedCount));
			} else if (food.name == "throw") {
				throw new IllegalStateException();
			}

			return Variant.fromVariantMap(map);
		}

		@Override
		public Food deserialize(final Variant variant) throws VariantException {
			if (variant == null) {
				throw new IllegalArgumentException();
			}

			if (variant.getKind() == VariantKind.NULL) {
				return null;
			}

			final Map<String, Variant> map = variant.getVariantMap();
			final String name = Variant.getVariantFromMap(map, "name").getString();
			final String taste = Variant.getVariantFromMap(map, "taste").getString();

			if (name.equals("fruit")) {
				final int seedCount = Variant.getVariantFromMap(map, "seedCount").getInteger();
				return new Fruit(seedCount, taste);
			} else if (name.equals("throw")) {
				throw new IllegalStateException();
			} else {
				return new Food(name, taste);
			}
		}
	}

	protected final Food SPAGHETTI = new Food("spaghetti", "yummy");

	protected final Food FRUIT = new Fruit(25, "sweet");

	protected final Food BURRITO = new Food("burrito", "delicious");

	protected final Food FOOD_THAT_THROWS = new Food("throw", "");

	protected final Variant SPAGHETTI_VARIANT = Variant.fromVariantMap(new HashMap<String, Variant>() {
		{
			put("name", Variant.fromString("spaghetti"));
			put("taste", Variant.fromString("yummy"));
		}
	});

	protected final Variant FRUIT_VARIANT = Variant.fromVariantMap(new HashMap<String, Variant>() {
		{
			put("name", Variant.fromString("fruit"));
			put("taste", Variant.fromString("sweet"));
			put("seedCount", Variant.fromInteger(25));
		}
	});

	protected final Variant BURRITO_VARIANT = Variant.fromVariantMap(new HashMap<String, Variant>() {
		{
			put("name", Variant.fromString("burrito"));
			put("taste", Variant.fromString("delicious"));
		}
	});

	protected final Variant FOOD_VARIANT_THAT_THROWS = Variant.fromVariantMap(new HashMap<String, Variant>() {
		{
			put("name", Variant.fromString("throw"));
			put("taste", Variant.fromString(""));
		}
	});
}
