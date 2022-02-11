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
 * {@code VariantSerializer} implementation for {@code Object} values returned by {@code JsonUtilityService}.
 *
 * Supports null.
 */
public final class JsonValueVariantSerializer implements VariantSerializer<Object> {
	private final JsonUtilityService jsonUtilityService;

	/**
	 * Constructor.
	 *
	 * @param jsonUtilityService {@code JsonUtilityService} to use when deserializing
	 */
	public JsonValueVariantSerializer(final JsonUtilityService jsonUtilityService) {
		if (jsonUtilityService == null) {
			throw new IllegalArgumentException();
		}

		this.jsonUtilityService = jsonUtilityService;
	}


	@Override
	public Variant serialize(final Object jsonValueAsObject) throws VariantException {
		if (jsonValueAsObject == null) {
			return Variant.fromNull();
		} else if (jsonValueAsObject instanceof JsonUtilityService.JSONObject) {
			final JsonUtilityService.JSONObject jsonValueAsJsonObject = (JsonUtilityService.JSONObject) jsonValueAsObject;
			return new JsonObjectVariantSerializer(jsonUtilityService).serialize(jsonValueAsJsonObject);
		} else if (jsonValueAsObject instanceof JsonUtilityService.JSONArray) {
			final JsonUtilityService.JSONArray jsonValueAsJsonArray = (JsonUtilityService.JSONArray) jsonValueAsObject;
			return new JsonArrayVariantSerializer(jsonUtilityService).serialize(jsonValueAsJsonArray);
		} else if (jsonValueAsObject instanceof Integer) {
			final Integer jsonValueAsInteger = (Integer) jsonValueAsObject;
			return Variant.fromInteger(jsonValueAsInteger);
		} else if (jsonValueAsObject instanceof Long) {
			final Long jsonValueAsLong = (Long) jsonValueAsObject;
			return Variant.fromLong(jsonValueAsLong);
		} else if (jsonValueAsObject instanceof Double) {
			final Double jsonValueAsDouble = (Double) jsonValueAsObject;
			return Variant.fromDouble(jsonValueAsDouble);
		} else if (jsonValueAsObject instanceof Boolean) {
			final Boolean jsonValueAsBoolean = (Boolean) jsonValueAsObject;
			return Variant.fromBoolean(jsonValueAsBoolean);
		} else if (jsonValueAsObject instanceof String) {
			final String jsonValueAsString = (String) jsonValueAsObject;
			return Variant.fromString(jsonValueAsString);
		} else {
			throw new VariantSerializationFailedException();
		}
	}

	@Override
	public Object deserialize(final Variant variant) throws VariantException {
		if (variant == null) {
			throw new IllegalArgumentException();
		}

		switch (variant.getKind()) {
			case NULL:
				return null;

			case STRING:
				return variant.getString();

			case INTEGER:
				return variant.getInteger();

			case LONG:
				return variant.getLong();

			case DOUBLE:
				return variant.getDouble();

			case BOOLEAN:
				return variant.getBoolean();

			case MAP:
				return new JsonObjectVariantSerializer(jsonUtilityService).deserialize(variant);

			case VECTOR:
				return new JsonArrayVariantSerializer(jsonUtilityService).deserialize(variant);

			default:
				throw new VariantSerializationFailedException();
		}
	}
}
