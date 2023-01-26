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
 * Represents the possible privacy settings for the Adobe SDK.
 *
 * <p>The possible values for the Adobe Mobile Privacy Status. The privacy status controls whether
 * specific activity is allowed on the device. The default privacy status is set in any ADBMobile
 * JSON configuration file using the parameter {@code global.privacy}. Use {@code
 * AdobeMobileMarketing.setPrivacyStatus()} API to override the default privacy status.
 *
 * <p>(TODO - verify the documentation link is accurate for AMSDK V5) To learn more about the Adobe
 * Mobile Privacy Status, view the online documentation at <a
 * href="https://marketing.adobe.com/resources/help/en_US/mobile/ios/privacy.html">https://marketing.adobe.com/resources/help/en_US/mobile/ios/privacy.html</a>
 */
public enum MobilePrivacyStatus {
    /** Adobe Mobile Privacy Status opted-in. */
    OPT_IN("optedin"),

    /** Adobe Mobile Privacy Status opted-out. */
    OPT_OUT("optedout"),

    /** Adobe Mobile Privacy Status is unknown. */
    UNKNOWN("optunknown");

    private final String value;

    MobilePrivacyStatus(final String value) {
        this.value = value;
    }

    /**
     * Returns the string value for this enum type.
     *
     * @return the string name for this enum type.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns a {@link MobilePrivacyStatus} object based on the provided {@code text}.
     *
     * <p>If the text provided is not valid, {@link #UNKNOWN} will be returned.
     *
     * @param text {@link String} to be converted to a {@code MobilePrivacyStatus} object
     * @return {@code MobilePrivacyStatus} object equivalent to the provided text
     */
    public static MobilePrivacyStatus fromString(final String text) {
        for (MobilePrivacyStatus b : MobilePrivacyStatus.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }

        return UNKNOWN;
    }
}
