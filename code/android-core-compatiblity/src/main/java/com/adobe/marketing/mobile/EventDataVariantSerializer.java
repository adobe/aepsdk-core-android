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
 * {@code VariantSerializer} implementation for {@code EventData}.
 */
final class EventDataVariantSerializer implements VariantSerializer<EventData> {

	@Override
	public Variant serialize(final EventData value) {
		if (value == null) {
			return Variant.fromNull();
		}

		return Variant.fromVariantMap(value.asMapCopy());
	}

	@Override
	public EventData deserialize(final Variant variant) throws VariantException {
		if (variant == null) {
			throw new IllegalArgumentException();
		}

		if (variant.getKind() == VariantKind.NULL) {
			return null;
		}

		return new EventData(variant.getVariantMap());
	}
}
