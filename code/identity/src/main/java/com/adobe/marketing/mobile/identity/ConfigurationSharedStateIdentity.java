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

package com.adobe.marketing.mobile.identity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.adobe.marketing.mobile.MobilePrivacyStatus;
import com.adobe.marketing.mobile.identity.IdentityConstants.Defaults;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.StringUtils;
import java.util.Map;

/**
 * ConfigurationSharedStateIdentity
 *
 * <p>Holds metadata containing all of the configuration information needed for the {@link
 * IdentityExtension} module to know if and how it should operate. The {@code IdentityExtension}
 * module will create an instance of this class
 */
final class ConfigurationSharedStateIdentity {

    /** {@link String} containing value of the customer's Experience Cloud Organization ID */
    private final String orgID;

    /** {@link MobilePrivacyStatus} value containing the current opt status of the user */
    private final MobilePrivacyStatus privacyStatus;

    /** {@link String} containing the host value for the Experience Cloud Server */
    private final String experienceCloudServer;

    /**
     * Extracts data from the provided shared state data and sets the internal configuration
     * appropriately. Sets default values for {@link #privacyStatus}, and {@link
     * #experienceCloudServer} if provided shared state does not contain valid values.
     *
     * @param sharedState EventData representing a {@code Configuration} shared state
     */
    ConfigurationSharedStateIdentity(final Map<String, Object> sharedState) {
        this.orgID =
                DataReader.optString(sharedState, IdentityConstants.JSON_CONFIG_ORGID_KEY, null);
        String server =
                DataReader.optString(
                        sharedState,
                        IdentityConstants.JSON_EXPERIENCE_CLOUD_SERVER_KEY,
                        Defaults.SERVER);

        this.experienceCloudServer = StringUtils.isNullOrEmpty(server) ? Defaults.SERVER : server;

        this.privacyStatus =
                MobilePrivacyStatus.fromString(
                        DataReader.optString(
                                sharedState,
                                IdentityConstants.JSON_CONFIG_PRIVACY_KEY,
                                Defaults.DEFAULT_MOBILE_PRIVACY.getValue()));
    }

    /**
     * The Experience Cloud organization ID.
     *
     * @return the Experience Cloud organization ID or null if one wasn't set.
     */
    @Nullable String getOrgID() {
        return orgID;
    }

    /**
     * The privacy status.
     *
     * @return the privacy status; defaults to {@link Defaults#DEFAULT_MOBILE_PRIVACY}.
     */
    @NonNull MobilePrivacyStatus getPrivacyStatus() {
        return privacyStatus;
    }

    /**
     * The Experience Cloud server.
     *
     * @return the Experience Cloud server; defaults to {@link Defaults#SERVER}
     */
    @NonNull String getExperienceCloudServer() {
        return experienceCloudServer;
    }

    /**
     * Determine if this contains configuration valid for a network sync
     *
     * @return whether this contains configuration valid for a network sync
     */
    boolean canSyncIdentifiersWithCurrentConfiguration() {
        return !StringUtils.isNullOrEmpty(this.orgID)
                && this.privacyStatus != MobilePrivacyStatus.OPT_OUT;
    }
}
