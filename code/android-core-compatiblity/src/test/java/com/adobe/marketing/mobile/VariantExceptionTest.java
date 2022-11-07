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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public final class VariantExceptionTest {
	@Test
	public void testConstructor() {
		final VariantException ex = new VariantException();
		assertNotNull(ex.getMessage());
	}

	@Test
	public void testConstructorWithException() {
		final Exception inner = new Exception();
		final VariantException ex = new VariantException(inner);
		assertNotNull(ex.getMessage());
	}

	@Test
	public void testConstructorWithMessage() {
		final VariantException ex = new VariantException("hi");
		assertNotNull(ex.getMessage());
	}
}
