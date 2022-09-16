/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 ******************************************************************************/

package com.adobe.marketing.mobile.identity;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.Log;
import com.adobe.marketing.mobile.MobilePrivacyStatus;
import com.adobe.marketing.mobile.identity.IdentityConstants.Defaults;

/**
 * ConfigurationSharedStateIdentity
 *
 * Holds metadata containing all of the configuration information needed for the {@link IdentityExtension} module to know
 * if and how it should operate.  The {@code IdentityExtension} module will create an instance of this class
 * for each {@link Event} while iterating through its {@link IdentityExtension#eventsQueue}
 */
final class ConfigurationSharedStateIdentity {

	/**
	 * {@link String} containing value of the customer's Experience Cloud Organization ID
	 */
	String orgID;


	/**
	 * {@link MobilePrivacyStatus} value containing the current opt status of the user
	 */
	MobilePrivacyStatus privacyStatus;

	/**
	 * {@link String} containing the host value for the Experience Cloud Server
	 */
	String marketingCloudServer;

	/**
	 * Constructor sets default values for  {@link #privacyStatus}, and {@link #marketingCloudServer}
	 */
	ConfigurationSharedStateIdentity() {
		this.orgID = null;
		this.privacyStatus = Defaults.DEFAULT_MOBILE_PRIVACY;
		this.marketingCloudServer = Defaults.SERVER;
	}

	/**
	 * Extracts data from the provided shared state data and sets the internal configuration appropriately
	 *
	 * @param sharedState {@link EventData} representing a {@code Configuration} shared state
	 */
	void getConfigurationProperties(final EventData sharedState) {
		if (sharedState == null) {
			Log.debug(IdentityExtension.LOG_SOURCE,
					  "getConfigurationProperties : Using default configurations because config state was null.");
			return;
		}

		this.orgID = sharedState.optString(IdentityConstants.JSON_CONFIG_ORGID_KEY, null);
		this.marketingCloudServer = sharedState.optString(IdentityConstants.JSON_EXPERIENCE_CLOUD_SERVER_KEY, Defaults.SERVER);

		if (StringUtils.isNullOrEmpty(this.marketingCloudServer)) {
			this.marketingCloudServer = Defaults.SERVER;
		}

		this.privacyStatus = MobilePrivacyStatus.fromString(sharedState.optString(
								 IdentityConstants.JSON_CONFIG_PRIVACY_KEY, Defaults.DEFAULT_MOBILE_PRIVACY.getValue()));
	}

	/**
	 * Determine if this contains configuration valid for a network sync
	 *
	 * @return whether this contains configuration valid for a network sync
	 */
	boolean canSyncIdentifiersWithCurrentConfiguration() {
		return !StringUtils.isNullOrEmpty(this.orgID) &&
			   this.privacyStatus != MobilePrivacyStatus.OPT_OUT;
	}
}
