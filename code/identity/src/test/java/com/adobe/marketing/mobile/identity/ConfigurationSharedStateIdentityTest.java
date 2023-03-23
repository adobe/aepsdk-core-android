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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class ConfigurationSharedStateIdentityTest {

    @Test
    public void testConstructor_ShouldSetDefault() {
        verifyDefaultValues(new ConfigurationSharedStateIdentity(Collections.emptyMap()));
    }

    @Test
    public void testExtractConfigurationProperties_ShouldLetDefaultValuesBe_When_NullSharedState() {
        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(null);
        verifyDefaultValues(configurationSharedStateIdentity);
    }

    // org id
    @Test
    public void testExtractConfigurationProperties_SetsOrgID_When_NonNullOrgID() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(IdentityTestConstants.JSON_CONFIG_ORGID_KEY, "test-org-id");

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        assertEquals(configurationSharedStateIdentity.getOrgID(), "test-org-id");
    }

    @Test
    public void testExtractConfigurationProperties_SetsOrgIDToDefault_When_NullOrgID() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put("random-key", "random-value");

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        assertNull(configurationSharedStateIdentity.getOrgID(), null);
    }

    // privacy
    @Test
    public void testExtractConfigurationProperties_SetsPrivacy_When_ValidPrivacy() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY, "optedin");

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        Assert.assertEquals(
                configurationSharedStateIdentity.getPrivacyStatus(), MobilePrivacyStatus.OPT_IN);
    }

    @Test
    public void testExtractConfigurationProperties_SetsPrivacyUnknown_When_InValidPrivacy() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY, "invalidValue");

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        assertEquals(
                configurationSharedStateIdentity.getPrivacyStatus(), MobilePrivacyStatus.UNKNOWN);
    }

    @Test
    public void testExtractConfigurationProperties_SetsPrivacyToDefault_When_NullPrivacy() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put("random-key", "random-value");

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        assertEquals(
                configurationSharedStateIdentity.getPrivacyStatus(), MobilePrivacyStatus.UNKNOWN);
    }

    @Test
    public void testExtractConfigurationProperties_SetsPrivacyToDefault_When_InvalidPrivacy() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY, "opted-whatever");

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        assertEquals(
                configurationSharedStateIdentity.getPrivacyStatus(), MobilePrivacyStatus.UNKNOWN);
    }

    // experience cloud server
    @Test
    public void
            testExtractConfigurationProperties_SetsExperienceCloudServer_When_NonNullExperienceCloudServer() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(
                IdentityTestConstants.JSON_EXPERIENCE_CLOUD_SERVER_KEY, "my-custom-server");

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        assertEquals(
                configurationSharedStateIdentity.getExperienceCloudServer(), "my-custom-server");
    }

    @Test
    public void
            testExtractConfigurationProperties_SetsExperienceCloudServerToDefault_When_NullExperienceCloudServer() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put("random-key", "random-value");

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        assertEquals(
                configurationSharedStateIdentity.getExperienceCloudServer(),
                IdentityTestConstants.Defaults.SERVER);
    }

    // canSyncIdentifiersWithCurrentConfiguration
    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_NullOrgID() {
        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(null);
        assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_EmptyOrgID() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(IdentityTestConstants.JSON_CONFIG_ORGID_KEY, "");

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_PrivacyOptedOut() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(
                IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY,
                MobilePrivacyStatus.OPT_OUT.getValue());

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_PrivacyOptedOut_NonEmptyOrgID() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(IdentityTestConstants.JSON_CONFIG_ORGID_KEY, "non-empty-org-id");
        testSharedData.put(
                IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY,
                MobilePrivacyStatus.OPT_OUT.getValue());

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_PrivacyOptedIn_EmptyOrgID() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(IdentityTestConstants.JSON_CONFIG_ORGID_KEY, "");
        testSharedData.put(
                IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY,
                MobilePrivacyStatus.OPT_IN.getValue());

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_PrivacyOptedIn_NullOrgID() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(
                IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY,
                MobilePrivacyStatus.OPT_IN.getValue());

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnFalse_When_PrivacyUnknown_NullOrgID() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(
                IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY,
                MobilePrivacyStatus.UNKNOWN.getValue());

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        assertFalse(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnTrue_When_PrivacyUnknown_NonNullOrgID() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(IdentityTestConstants.JSON_CONFIG_ORGID_KEY, "non-empty-org-id");
        testSharedData.put(
                IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY,
                MobilePrivacyStatus.UNKNOWN.getValue());

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        assertTrue(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    @Test
    public void
            testShouldSyncIdentifiersWithCurrentConfiguration_ShouldReturnTrue_When_PrivacyOptedIn_NonNullOrgID() {
        Map<String, Object> testSharedData = new HashMap<>();
        testSharedData.put(IdentityTestConstants.JSON_CONFIG_ORGID_KEY, "non-empty-org-id");
        testSharedData.put(
                IdentityTestConstants.JSON_CONFIG_PRIVACY_KEY,
                MobilePrivacyStatus.OPT_IN.getValue());

        ConfigurationSharedStateIdentity configurationSharedStateIdentity =
                new ConfigurationSharedStateIdentity(testSharedData);
        assertTrue(configurationSharedStateIdentity.canSyncIdentifiersWithCurrentConfiguration());
    }

    private void verifyDefaultValues(
            ConfigurationSharedStateIdentity configurationSharedStateIdentity) {
        assertNull(configurationSharedStateIdentity.getOrgID());
        Assert.assertEquals(
                configurationSharedStateIdentity.getPrivacyStatus(),
                IdentityTestConstants.Defaults.DEFAULT_MOBILE_PRIVACY);
        assertEquals(
                configurationSharedStateIdentity.getExperienceCloudServer(),
                IdentityTestConstants.Defaults.SERVER);
    }
}
