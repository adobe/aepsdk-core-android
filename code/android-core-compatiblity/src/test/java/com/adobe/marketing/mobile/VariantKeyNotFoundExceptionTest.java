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

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public final class VariantKeyNotFoundExceptionTest {
	@Test
	public void testConstructor() {
		final VariantKeyNotFoundException ex = new VariantKeyNotFoundException();
		assertNotNull(ex.getMessage());
	}

	@Test
	public void testConstructorWithException() {
		final Exception inner = new Exception();
		final VariantKeyNotFoundException ex = new VariantKeyNotFoundException(inner);
		assertNotNull(ex.getMessage());
	}

	@Test
	public void testConstructorWithMessage() {
		final VariantKeyNotFoundException ex = new VariantKeyNotFoundException("hi");
		assertNotNull(ex.getMessage());
	}
}
