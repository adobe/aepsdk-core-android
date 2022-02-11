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
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;

public abstract class VariantTests {

	protected class BasicTest {
		public Variant variant = null;
		public VariantKind expectedKind = null;
		public Boolean expectedBoolean = null;
		public Double expectedDouble = null;
		public Integer expectedInteger = null;
		public Long expectedLong = null;
		public String expectedString = null;
		public Map<String, Variant> expectedMap = null;
		public List<Variant> expectedList = null;
		public Double expectedConvertedDouble = null;
		public String expectedConvertedString = null;
		public String expectedToString = null;
		public Class<? extends Throwable> expectedBooleanException = VariantKindException.class;
		public Class<? extends Throwable> expectedIntegerException = VariantKindException.class;
		public Class<? extends Throwable> expectedLongException = VariantKindException.class;
		public Class<? extends Throwable> expectedDoubleException = VariantKindException.class;
		public Class<? extends Throwable> expectedStringException = VariantKindException.class;
		public Class<? extends Throwable> expectedMapException = VariantKindException.class;
		public Class<? extends Throwable> expectedListException = VariantKindException.class;

		public void test() {
			testKind();
			testGetString();
			testGetBoolean();
			testGetInteger();
			testGetLong();
			testGetDouble();
			testGetVariantMap();
			testGetVariantList();
			testConvertToString();
			testConvertToDouble();
			testToString();
			testClone();
		}

		private void testClone() {
			final Variant clone = variant.clone();
			assertNotNull(clone);
			assertNotSame(clone, variant);
			assertEquals(variant.getKind(), clone.getKind());

			try {
				switch (variant.getKind()) {
					case NULL:
						break;

					case BOOLEAN:
						assertEquals(variant.getBoolean(), clone.getBoolean());
						break;

					case INTEGER:
						assertEquals(variant.getInteger(), clone.getInteger());
						break;

					case DOUBLE:
						assertEquals((Double) variant.getDouble(), (Double)clone.getDouble());
						break;

					case LONG:
						assertEquals(variant.getLong(), clone.getLong());
						break;

					case STRING:
						assertEquals(variant.getString(), clone.getString());
						break;

					case MAP:
						assertEquals(variant.getVariantMap(), clone.getVariantMap());
						break;

					case VECTOR:
						assertEquals(variant.getVariantList(), clone.getVariantList());
						break;

					default:
						throw new IllegalStateException(); // shouldn't happen
				}
			} catch (Exception ex) {
				assertTrue("Unexpected exception: " + ex.getMessage(), false);
			}

			assertEquals(variant.hashCode(), clone.hashCode());
			assertTrue(variant.equals(clone));
		}

		private void testToString() {
			assertEquals(expectedToString, variant.toString());
		}

		private void testConvertToDouble() {
			if (expectedConvertedDouble != null) {
				try {
					assertEquals(expectedConvertedDouble, (Double) variant.convertToDouble());
				} catch (Exception ex) {
					assertTrue("Unexpected exception: " + ex.getMessage(), false);
				}
			}
		}

		private void testConvertToString() {
			if (expectedConvertedString != null) {
				try {
					assertEquals(expectedConvertedString, variant.convertToString());
				} catch (Exception ex) {
					assertTrue("Unexpected exception: " + ex.getMessage(), false);
				}
			}
		}

		private void testGetVariantList() {
			try {
				assertEquals(expectedList, variant.getVariantList());
				assertNotNull(expectedList);
			} catch (Exception ex) {
				assertNotNull("Unexpected exception: " + ex.getMessage(), expectedListException);
				assertEquals(expectedListException, ex.getClass());
			}
		}

		private void testGetVariantMap() {
			try {
				assertEquals(expectedMap, variant.getVariantMap());
				assertNotNull(expectedMap);
			} catch (Exception ex) {
				assertNotNull("Unexpected exception: " + ex.getMessage(), expectedMapException);
				assertEquals(expectedMapException, ex.getClass());
			}
		}

		private void testGetDouble() {
			try {
				final double value = variant.getDouble();
				assertEquals(expectedDouble, value, 0.00001);
				assertNotNull(expectedDouble);
			} catch (Exception ex) {
				assertNotNull("Unexpected exception: " + ex.getMessage(), expectedDoubleException);
				assertEquals(expectedDoubleException, ex.getClass());
			}
		}

		private void testGetLong() {
			try {
				assertEquals(expectedLong, (Long) variant.getLong());
				assertNotNull(expectedLong);
			} catch (Exception ex) {
				assertNotNull("Unexpected exception: " + ex.getMessage(), expectedLongException);
				assertEquals(expectedLongException, ex.getClass());
			}
		}

		private void testGetInteger() {
			try {
				assertEquals(expectedInteger, (Integer) variant.getInteger());
				assertNotNull(expectedInteger);
			} catch (Exception ex) {
				assertNotNull("Unexpected exception: " + ex.getMessage(), expectedIntegerException);
				assertEquals(expectedIntegerException, ex.getClass());
			}
		}

		private void testGetBoolean() {
			try {
				assertEquals(expectedBoolean, variant.getBoolean());
				assertNotNull(expectedBoolean);
			} catch (Exception ex) {
				assertNotNull("Unexpected exception: " + ex.getMessage(), expectedBooleanException);
				assertEquals(expectedBooleanException, ex.getClass());
			}
		}

		private void testGetString() {
			try {
				assertEquals(expectedString, variant.getString());
				assertNotNull(expectedString);
			} catch (Exception ex) {
				assertNotNull("Unexpected exception: " + ex.getMessage(), expectedStringException);
				assertEquals(expectedStringException, ex.getClass());
			}
		}

		private void testKind() {
			assertEquals(expectedKind, variant.getKind());
		}
	}
}
