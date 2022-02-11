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
 * A {@code VariantKind} indicates the type of value stored within a Variant.
 */
enum VariantKind {
	NULL,
	STRING,
	INTEGER,
	LONG,
	DOUBLE,
	BOOLEAN,
	VECTOR,
	MAP,

	@Deprecated
	OBJECT; // DO NOT USE THIS VALUE, SEE Variant

	/**
	 * @return whether the type is a number type
	 */
	public boolean isNumeric() {
		return this.equals(VariantKind.INTEGER) || this.equals(VariantKind.LONG) || this.equals(VariantKind.DOUBLE);
	}
}
