package com.adobe.marketing.mobile;

import java.util.HashMap;
import java.util.Map;

/**
 * VariantSerializer implementation for VisitorID.
 */
final class VisitorIDVariantSerializer implements VariantSerializer<VisitorID> {

	@Override
	public Variant serialize(final VisitorID visitorID) {
		if (visitorID == null) {
			return Variant.fromNull();
		}

		return Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("id_origin", Variant.fromString(visitorID.getIdOrigin()));
				put("id_type", Variant.fromString(visitorID.getIdType()));
				put("id", Variant.fromString(visitorID.getId()));

				int authenticationState = VisitorID.AuthenticationState.UNKNOWN.getValue();

				if (visitorID.getAuthenticationState() != null) {
					authenticationState = visitorID.getAuthenticationState().getValue();
				}

				put("authentication_state", Variant.fromInteger(authenticationState));
			}
		});
	}

	@Override
	public VisitorID deserialize(final Variant serialized) throws VariantException {
		if (serialized == null) {
			throw new IllegalArgumentException();
		}

		if (serialized.getKind() == VariantKind.NULL) {
			return null;
		}

		final Map<String, Variant> variantMap = serialized.getVariantMap();
		final String idOrigin = Variant.optVariantFromMap(variantMap, "id_origin").optString(null);
		final String idType = Variant.optVariantFromMap(variantMap, "id_type").optString(null);
		final String id = Variant.optVariantFromMap(variantMap, "id").optString(null);
		final int authenticationStateInt = Variant.optVariantFromMap(variantMap,
										   "authentication_state").optInteger(VisitorID.AuthenticationState.UNKNOWN.getValue());
		final VisitorID.AuthenticationState authenticationState =
			VisitorID.AuthenticationState.fromInteger(authenticationStateInt);

		return new VisitorID(idOrigin, idType, id, authenticationState);
	}
}
