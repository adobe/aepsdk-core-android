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

import static org.junit.Assert.*;

import com.adobe.marketing.mobile.MobilePrivacyStatus;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConfigurationSharedStateIdentityTest {

    private ConfigurationSharedStateIdentity configurationSharedStateIdentity;

    @Before
    public void setup() {
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

    // org id
    @Test
    public void testExtractConfigurationProperties_SetsOrgID_When_NonNullOrgID() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(IdentityTestConstants.JSON_CONFIG_ORGID_KEY, "test-org-id");

        configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
        assertEquals(configurationSharedStateIdentity.orgID, "test-org-id");
    }

    @Test
    public void testExtractConfigurationProperties_SetsOrgIDToDefault_When_NullOrgID() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put("random-key", "random-value");

        configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
        assertNull(configurationSharedStateIdentity.orgID, null);
    }

    // privacy
    @Test
    public void testExtractConfigurationProperties_SetsPrivacy_When_ValidPrivacy() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY, "optedin");

        configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
        Assert.assertEquals(
                configurationSharedStateIdentity.privacyStatus, MobilePrivacyStatus.OPT_IN);
    }

    @Test
    public void testExtractConfigurationProperties_SetsPrivacyUnknown_When_InValidPrivacy() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY, "invalidValue");

        configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
        assertEquals(configurationSharedStateIdentity.privacyStatus, MobilePrivacyStatus.UNKNOWN);
    }

    @Test
    public void testExtractConfigurationProperties_SetsPrivacyToDefault_When_NullPrivacy() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put("random-key", "random-value");

        configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
        assertEquals(configurationSharedStateIdentity.privacyStatus, MobilePrivacyStatus.UNKNOWN);
    }

    @Test
    public void testExtractConfigurationProperties_SetsPrivacyToDefault_When_InvalidPrivacy() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY, "opted-whatever");

        configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
        assertEquals(configurationSharedStateIdentity.privacyStatus, MobilePrivacyStatus.UNKNOWN);
    }

    // marketing server
    @Test
    public void
            testExtractConfigurationProperties_SetsMarketingServer_When_NonNullMarketingServer() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(
                IdentityTestConstants.JSON_EXPERIENCE_CLOUD_SERVER_KEY, "my-custom-server");

        configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
        assertEquals(configurationSharedStateIdentity.marketingCloudServer, "my-custom-server");
    }

    @Test
    public void
            testExtractConfigurationProperties_SetsMarketingServerToDefault_When_NullMarketingServer() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put("random-key", "random-value");

        configurationSharedStateIdentity.getConfigurationProperties(testSharedData);
        assertEquals(
                configurationSharedStateIdentity.marketingCloudServer,
                IdentityTestConstants.Defaults.SERVER);
    }

    // canSyncIdentifiersWithCurrentConfiguration
    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_NullOrgID() {
        assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_EmptyOrgID() {
        configurationSharedStateIdentity.orgID = "";
        assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_PrivacyOptedOut() {
        configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_OUT;
        assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_PrivacyOptedOut_NonEmptyOrgID() {
        configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_OUT;
        configurationSharedStateIdentity.orgID = "non-empty-org-id";
        assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_PrivacyOptedIn_EmptyOrgID() {
        configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
        configurationSharedStateIdentity.orgID = "";
        assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_PrivacyOptedIn_NullOrgID() {
        configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
        configurationSharedStateIdentity.orgID = null;
        assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_PrivacyUnknown_NullOrgID() {
        configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.UNKNOWN;
        configurationSharedStateIdentity.orgID = null;
        assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnTrue_When_PrivacyUnknown_NonNullOrgID() {
        configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.UNKNOWN;
        configurationSharedStateIdentity.orgID = "non-empty-org-id";
        assertTrue(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnTrue_When_PrivacyOptedIn_NonNullOrgID() {
        configurationSharedStateIdentity.privacyStatus = MobilePrivacyStatus.OPT_IN;
        configurationSharedStateIdentity.orgID = "non-empty-org-id";
        assertTrue(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    private void verifyDefaultValues() {
        assertNull(configurationSharedStateIdentity.orgID);
        Assert.assertEquals(
                configurationSharedStateIdentity.privacyStatus,
                IdentityTestConstants.Defaults.DEFAULT_MOBILE_PRIVACY);
        assertEquals(
                configurationSharedStateIdentity.marketingCloudServer,
                IdentityTestConstants.Defaults.SERVER);
    }
}
