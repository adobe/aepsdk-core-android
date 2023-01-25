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

import com.adobe.marketing.mobile.internal.CoreConstants;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.StringUtils;

/** An identifier to be used with the Adobe Experience Cloud Visitor ID Service */
public class VisitorID {

    private static final int RANDOM_HASH_BASE = 17;
    private static final int RANDOM_HASH_PADDING = 31;

    private AuthenticationState authenticationState;
    private final String id;
    private final String idOrigin;
    private final String idType;

    /**
     * {@link AuthenticationState} for this {@link VisitorID}
     *
     * @return The {@link AuthenticationState}
     */
    public AuthenticationState getAuthenticationState() {
        return authenticationState;
    }

    /**
     * ID for this {@link VisitorID}
     *
     * @return The {@link VisitorID} ID
     */
    public final String getId() {
        return id;
    }

    /**
     * ID Origin for this {@link VisitorID}
     *
     * @return {@link VisitorID} Origin string
     */
    public final String getIdOrigin() {
        return idOrigin;
    }

    /**
     * ID Type for this {@link VisitorID}
     *
     * @return {@link VisitorID} type
     */
    public final String getIdType() {
        return idType;
    }

    /** Used to indicate the authentication state for the current {@link VisitorID} */
    public enum AuthenticationState {
        UNKNOWN(0),
        AUTHENTICATED(1),
        LOGGED_OUT(2);

        private final int value;

        AuthenticationState(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static AuthenticationState fromInteger(final int authStateInteger) {
            for (AuthenticationState b : AuthenticationState.values()) {
                if (b.getValue() == authStateInteger) {
                    return b;
                }
            }

            return AuthenticationState.UNKNOWN;
        }
    }

    /**
     * Constructor initializes {@link #id}, {@link #idOrigin}, {@link #idType} and {@link
     * #authenticationState}.
     *
     * @param idOrigin {@link String} containing the ID Origin for the {@link VisitorID}
     * @param idType {@code String} containing the ID Type for the {@code VisitorID}; it should not
     *     be null/empty
     * @param id {@code String} containing the ID for the {@code VisitorID}; it should not be
     *     null/empty
     * @param authenticationState {@link AuthenticationState} containing the authentication state
     *     for the {@code VisitorID}
     * @throws IllegalStateException if the provided {@code idType} is null or empty
     */
    public VisitorID(
            final String idOrigin,
            final String idType,
            final String id,
            final AuthenticationState authenticationState) {
        // TODO: cleanContextDataKey logic will be moved to Analytics extension
        // https://github.com/adobe/aepsdk-core-android/issues/217
        //		final String cleanIdType = ContextDataUtil.cleanContextDataKey(idType);

        // idType cannot be null/empty
        if (StringUtils.isNullOrEmpty(idType)) {
            throw new IllegalStateException("idType parameter cannot be null or empty");
        }

        // id cannot be null/empty
        // Do not throw IllegalStateException to maintain backwards compatibility
        if (StringUtils.isNullOrEmpty(id)) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    "VisitorID",
                    "The custom VisitorID should not have null/empty id, this VisitorID will be"
                            + " ignored");
        }

        this.idOrigin = idOrigin;
        this.idType = idType;
        this.id = id;
        this.authenticationState = authenticationState;
    }

    /**
     * Compares the provided {@link VisitorID} object with this and determines if they are equal.
     *
     * <p>The comparison checks that the provided {@link Object} parameter is a {@code VisitorID}
     * instance. If it is, then checks the equality of the {@link #idType} and {@link #id} fields
     * between the two objects.
     *
     * @param o {@code VisitorID} object to compare against
     * @return {@code boolean} indicating whether the provided {@code VisitorID} object is equal to
     *     this
     */
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof VisitorID)) {
            return false;
        }

        VisitorID idToCompare = (VisitorID) o;

        if (!this.idType.equals(idToCompare.idType)) {
            return false;
        }

        // if id is null, the passed in ID must be null to match
        if (this.id == null) {
            return idToCompare.id == null;
        }

        if (idToCompare.id == null) {
            return false;
        }

        return this.id.compareTo(idToCompare.id) == 0;
    }

    @Override
    public int hashCode() {
        int result = RANDOM_HASH_BASE;
        result = RANDOM_HASH_PADDING * result + id.hashCode();
        result = RANDOM_HASH_PADDING * result + idType.hashCode();
        return result;
    }
}
