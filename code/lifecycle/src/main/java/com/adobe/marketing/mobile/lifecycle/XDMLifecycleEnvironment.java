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

package com.adobe.marketing.mobile.lifecycle;

import androidx.annotation.NonNull;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Class {@code Environment} representing a subset of the XDM Environment data type fields.
 * Information about the surrounding situation the event observation occurred in, specifically
 * detailing transitory information such as the network or software versions.
 */
@SuppressWarnings("unused")
class XDMLifecycleEnvironment {
    private final String LOG_SOURCE = "XDMLifecycleEnvironment";
    private String carrier;
    private String language;
    private String operatingSystem;
    private String operatingSystemVersion;
    private XDMLifecycleEnvironmentTypeEnum type;
    private final String languageRegex =
            "^(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5,8})(-([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z](-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)|((en-GB-oed|i-ami|i-bnn|i-default|i-enochian|i-hak|i-klingon|i-lux|i-mingo|i-navajo|i-pwn|i-tao|i-tay|i-tsu|sgn-BE-FR|sgn-BE-NL|sgn-CH-DE)|(art-lojban|cel-gaulish|no-bok|no-nyn|zh-guoyu|zh-hakka|zh-min|zh-min-nan|zh-xiang)))$";
    private final Pattern languagePattern = Pattern.compile(languageRegex);

    XDMLifecycleEnvironment() {}

    Map<String, Object> serializeToXdm() {
        Map<String, Object> map = new HashMap<String, Object>();

        if (this.carrier != null) {
            map.put("carrier", this.carrier);
        }

        if (!StringUtils.isNullOrEmpty(this.language)) {
            if (isValidLanguageTag(this.language)) {
                Map<String, Object> dublinCoreLanguage = new HashMap<String, Object>();
                dublinCoreLanguage.put("language", this.language);
                map.put("_dc", dublinCoreLanguage);
            } else {
                Log.warning(
                        LifecycleConstants.LOG_TAG,
                        LOG_SOURCE,
                        "Language tag '%s' failed validation and will be dropped. Values for XDM"
                                + " field 'environment._dc.language' must conform to BCP 47.",
                        this.language);
            }
        }

        if (this.operatingSystem != null) {
            map.put("operatingSystem", this.operatingSystem);
        }

        if (this.operatingSystemVersion != null) {
            map.put("operatingSystemVersion", this.operatingSystemVersion);
        }

        if (this.type != null) {
            map.put("type", this.type.toString());
        }

        return map;
    }

    /**
     * Returns the Mobile network carrier property A mobile network carrier or MNO, also known as a
     * wireless service provider, wireless carrier, cellular company, or mobile network carrier, is
     * a provider of services wireless communications that owns or controls all the elements
     * necessary to sell and deliver services to an end user.
     *
     * @return {@link String} value or null if the property is not set
     */
    String getCarrier() {
        return this.carrier;
    }

    /**
     * Sets the Mobile network carrier property A mobile network carrier or MNO, also known as a
     * wireless service provider, wireless carrier, cellular company, or mobile network carrier, is
     * a provider of services wireless communications that owns or controls all the elements
     * necessary to sell and deliver services to an end user.
     *
     * @param newValue the new Mobile network carrier value
     */
    void setCarrier(final String newValue) {
        this.carrier = newValue;
    }

    /**
     * Returns the Language property The language of the environment to represent the user's
     * linguistic, geographical, or cultural preferences for data presentation.
     *
     * @return {@link String} value or null if the property is not set
     */
    String getLanguage() {
        return this.language;
    }

    /**
     * Sets the Language property The language of the environment to represent the user's
     * linguistic, geographical, or cultural preferences for data presentation (according to IETF
     * RFC 3066).
     *
     * @param newValue the new Language value
     */
    void setLanguage(final String newValue) {
        this.language = newValue;
    }

    /**
     * Returns the Operating system property The name of the operating system used when the
     * observation was made. The attribute should not contain any version information such as
     * '10.5.3', but instead contain 'edition' designations such as 'Ultimate' or 'Professional'.
     *
     * @return {@link String} value or null if the property is not set
     */
    String getOperatingSystem() {
        return this.operatingSystem;
    }

    /**
     * Sets the Operating system property The name of the operating system used when the observation
     * was made. The attribute should not contain any version information such as '10.5.3', but
     * instead contain 'edition' designations such as 'Ultimate' or 'Professional'.
     *
     * @param newValue the new Operating system value
     */
    void setOperatingSystem(final String newValue) {
        this.operatingSystem = newValue;
    }

    /**
     * Returns the Operating system version property The full version identifier for the operating
     * system used when the observation was made. Versions are generally numerically composed but
     * may be in a vendor defined format.
     *
     * @return {@link String} value or null if the property is not set
     */
    String getOperatingSystemVersion() {
        return this.operatingSystemVersion;
    }

    /**
     * Sets the Operating system version property The full version identifier for the operating
     * system used when the observation was made. Versions are generally numerically composed but
     * may be in a vendor defined format.
     *
     * @param newValue the new Operating system version value
     */
    void setOperatingSystemVersion(final String newValue) {
        this.operatingSystemVersion = newValue;
    }

    /**
     * Returns the Type property The type of the application environment.
     *
     * @return {@link XDMLifecycleEnvironmentTypeEnum} value or null if the property is not set
     */
    XDMLifecycleEnvironmentTypeEnum getType() {
        return this.type;
    }

    /**
     * Sets the Type property The type of the application environment.
     *
     * @param newValue the new Type value
     */
    void setType(final XDMLifecycleEnvironmentTypeEnum newValue) {
        this.type = newValue;
    }

    /**
     * Validate the language tag is formatted per the XDM Environment Schema required pattern.
     *
     * @param tag the language tag to validate
     * @return true if the language tag matches the pattern.
     */
    private boolean isValidLanguageTag(@NonNull final String tag) {
        return languagePattern.matcher(tag).matches();
    }
}
