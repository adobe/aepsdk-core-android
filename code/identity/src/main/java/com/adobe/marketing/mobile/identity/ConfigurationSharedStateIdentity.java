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

import com.adobe.marketing.mobile.MobilePrivacyStatus;
import com.adobe.marketing.mobile.identity.IdentityConstants.Defaults;
import com.adobe.marketing.mobile.services.Log;
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

    private static final String LOG_SOURCE = "ConfigurationSharedStateIdentity";

    /** {@link String} containing value of the customer's Experience Cloud Organization ID */
    String orgID;

    /** {@link MobilePrivacyStatus} value containing the current opt status of the user */
    MobilePrivacyStatus privacyStatus;

    /** {@link String} containing the host value for the Experience Cloud Server */
    String marketingCloudServer;

    /**
     * Constructor sets default values for {@link #privacyStatus}, and {@link #marketingCloudServer}
     */
    ConfigurationSharedStateIdentity() {
        this.orgID = null;
        this.privacyStatus = Defaults.DEFAULT_MOBILE_PRIVACY;
        this.marketingCloudServer = Defaults.SERVER;
    }

    /**
     * Extracts data from the provided shared state data and sets the internal configuration
     * appropriately
     *
     * @param sharedState EventData representing a {@code Configuration} shared state
     */
    void getConfigurationProperties(final Map<String, Object> sharedState) {
        if (sharedState == null) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "getConfigurationProperties : Using default configurations because config"
                            + " state was null.");
            return;
        }

        this.orgID =
                DataReader.optString(sharedState, IdentityConstants.JSON_CONFIG_ORGID_KEY, null);
        this.marketingCloudServer =
                DataReader.optString(
                        sharedState,
                        IdentityConstants.JSON_EXPERIENCE_CLOUD_SERVER_KEY,
                        Defaults.SERVER);

        if (StringUtils.isNullOrEmpty(this.marketingCloudServer)) {
            this.marketingCloudServer = Defaults.SERVER;
        }

        this.privacyStatus =
                MobilePrivacyStatus.fromString(
                        DataReader.optString(
                                sharedState,
                                IdentityConstants.JSON_CONFIG_PRIVACY_KEY,
                                Defaults.DEFAULT_MOBILE_PRIVACY.getValue()));
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
