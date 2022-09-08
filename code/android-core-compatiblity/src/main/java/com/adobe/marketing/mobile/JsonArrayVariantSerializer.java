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
import java.util.List;

/**
 * {@code VariantSerializer} implementation for {@code JsonUtilityService.JSONArray}.
 */
final class JsonArrayVariantSerializer implements VariantSerializer<JsonUtilityService.JSONArray> {
	private final JsonUtilityService jsonUtilityService;

	/**
	 * Constructor.
	 *
	 * @param jsonUtilityService {@code JsonUtilityService} to use when deserializing
	 */
	public JsonArrayVariantSerializer(final JsonUtilityService jsonUtilityService) {
		if (jsonUtilityService == null) {
			throw new IllegalArgumentException();
		}

		this.jsonUtilityService = jsonUtilityService;
	}


	@Override
	public Variant serialize(final JsonUtilityService.JSONArray jsonValueAsJsonArray) throws VariantException {
		if (jsonValueAsJsonArray == null) {
			return Variant.fromNull();
		}

		final List<Variant> serializedArray = new ArrayList<Variant>();

		for (int i = 0, c = jsonValueAsJsonArray.length(); i < c; ++i) {
			Object element;

			try {
				element = jsonValueAsJsonArray.get(i);
			} catch (JsonException ex) {
				throw new VariantSerializationFailedException(ex);
			}

			final Variant serializedElement = new JsonValueVariantSerializer(jsonUtilityService).serialize(element);
			serializedArray.add(serializedElement);
		}

		return Variant.fromVariantList(serializedArray);
	}

	@Override
	public JsonUtilityService.JSONArray deserialize(final Variant variant) throws VariantException {
		if (variant == null) {
			throw new IllegalArgumentException();
		}

		if (variant.getKind() == VariantKind.NULL) {
			return null;
		}

		final JsonUtilityService.JSONArray jsonArray = jsonUtilityService.createJSONArray("[]");
		final List<Variant> variantVector = variant.getVariantList();

		for (Variant value : variantVector) {
			final Object jsonValue = new JsonValueVariantSerializer(jsonUtilityService).deserialize(value);

			// JsonValueVariantSerializer serializes null
			try {
				jsonArray.put(jsonValue);
			} catch (JsonException ex) {
				throw new VariantSerializationFailedException(ex);
			}
		}

		return jsonArray;
	}
}
