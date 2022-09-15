/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2017 Adobe
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
package com.adobe.marketing.mobile;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigurationSharedStateIdentityTest {

	private ConfigurationSharedStateIdentity configurationSharedStateIdentity;

	@Before
	public void setup() throws Exception {
		configurationSharedStateIdentity = new ConfigurationSharedStateIdentity();
	}

	@Test
	public void testConstructor_ShouldSetDefault() {
		verifyDefaultValues();
	}

	@Test
	public void testExtractConfigurationProperties_ShouldLetDefaultValuesBe_When_NullSharedState() {
		configurationSharedStateIdentity.getConfigurationProperties(null);
		verifyDefaultValues();
	}

	//org id
	@Test
	public void testExtractConfigurationProperties_SetsOrgID_When_NonNullOrgID() {
		EventData testSharedData = new EventData();
		testSharedData.putString(IdentityTestConstants.JSON_CONFIG_ORGID_KEY, "test-org-id");

		configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
		assertEquals(configurationSharedStateIdentity.orgID, "test-org-id");
	}

	@Test
	public void testExtractConfigurationProperties_SetsOrgIDToDefault_When_NullOrgID() {
		EventData testSharedData = new EventData();
		testSharedData.putString("random-key", "random-value");

		configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
		assertNull(configurationSharedStateIdentity.orgID, null);
	}

	//privacy
	@Test
	public void testExtractConfigurationProperties_SetsPrivacy_When_ValidPrivacy() {
		EventData testSharedData = new EventData();
		testSharedData.putString(IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY, "optedin");

		configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
		assertEquals(configurationSharedStateIdentity.privacyStatus, MobilePrivacyStatus.OPT_IN);
	}

	@Test
	public void testExtractConfigurationProperties_SetsPrivacyUnknown_When_InValidPrivacy() {
		EventData testSharedData = new EventData();
		testSharedData.putString(IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY, "invalidValue");

		configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
		assertEquals(configurationSharedStateIdentity.privacyStatus, MobilePrivacyStatus.UNKNOWN);
	}

	@Test
	public void testExtractConfigurationProperties_SetsPrivacyToDefault_When_NullPrivacy() {
		EventData testSharedData = new EventData();
		testSharedData.putString("random-key", "random-value");

		configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
		assertEquals(configurationSharedStateIdentity.privacyStatus, MobilePrivacyStatus.UNKNOWN);
	}

	@Test
	public void testExtractConfigurationProperties_SetsPrivacyToDefault_When_InvalidPrivacy() {
		EventData testSharedData = new EventData();
		testSharedData.putString(IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY, "opted-whatever");

		configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
		assertEquals(configurationSharedStateIdentity.privacyStatus, MobilePrivacyStatus.UNKNOWN);
	}

	//marketing server
	@Test
	public void testExtractConfigurationProperties_SetsMarketingServer_When_NonNullMarketingServer() {
		EventData testSharedData = new EventData();
		testSharedData.putString(IdentityTestConstants.JSON_EXPERIENCE_CLOUD_SERVER_KEY, "my-custom-server");

		configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
		assertEquals(configurationSharedStateIdentity.marketingCloudServer, "my-custom-server");
	}

	@Test
	public void testExtractConfigurationProperties_SetsMarketingServerToDefault_When_NullMarketingServer() {
		EventData testSharedData = new EventData();
		testSharedData.putString("random-key", "random-value");

		configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
		assertEquals(configurationSharedStateIdentity.marketingCloudServer, IdentityTestConstants.Defaults.SERVER);
	}

	//canSyncIdentifiersWithCurrentConfiguration
	@Test
	public void testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_NullOrgID() {
		assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
	}

	@Test
	public void testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_EmptyOrgID() {
		configurationSharedStateIdentity.orgID = "";
		assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
	}

	@Test
	public void testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_PrivacyOptedOut() {
		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_OUT;
		assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
	}

	@Test
	public void testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_PrivacyOptedOut_NonEmptyOrgID() {
		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_OUT;
		configurationSharedStateIdentity.orgID = "non-empty-org-id";
		assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
	}

	@Test
	public void testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_PrivacyOptedIn_EmptyOrgID() {
		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
		configurationSharedStateIdentity.orgID = "";
		assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
	}

	@Test
	public void testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_PrivacyOptedIn_NullOrgID() {
		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
		configurationSharedStateIdentity.orgID = null;
		assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
	}

	@Test
	public void testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_PrivacyUnknown_NullOrgID() {
		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.UNKNOWN;
		configurationSharedStateIdentity.orgID = null;
		assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
	}

	@Test
	public void testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnTrue_When_PrivacyUnknown_NonNullOrgID() {
		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.UNKNOWN;
		configurationSharedStateIdentity.orgID = "non-empty-org-id";
		assertTrue(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
	}

	@Test
	public void testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnTrue_When_PrivacyOptedIn_NonNullOrgID() {
		configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
		configurationSharedStateIdentity.orgID = "non-empty-org-id";
		assertTrue(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
	}

	private void verifyDefaultValues() {
		assertNull(configurationSharedStateIdentity.orgID);
		assertEquals(configurationSharedStateIdentity.privacyStatus, IdentityTestConstants.Defaults.DEFAULT_MOBILE_PRIVACY);
		assertEquals(configurationSharedStateIdentity.marketingCloudServer, IdentityTestConstants.Defaults.SERVER);
	}

}
