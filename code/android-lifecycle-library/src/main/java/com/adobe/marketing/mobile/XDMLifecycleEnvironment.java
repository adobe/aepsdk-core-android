/* ************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2021 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/

package com.adobe.marketing.mobile;

import java.util.Map;
import java.util.HashMap;

/**
 * Class {@code Environment} representing a subset of the XDM Environment data type fields.
 * Information about the surrounding situation the event observation occurred in, specifically detailing transitory information
 * such as the network or software versions.
 */
@SuppressWarnings("unused")
class XDMLifecycleEnvironment {
	private String carrier;
	private String language;
	private String operatingSystem;
	private String operatingSystemVersion;
	private XDMLifecycleEnvironmentTypeEnum type;

	XDMLifecycleEnvironment() {}

	Map<String, Object> serializeToXdm() {
		Map<String, Object> map = new HashMap<String, Object>();

		if (this.carrier != null) {
			map.put("carrier", this.carrier);
		}

		if (this.language != null) {
			Map<String, Object> dublinCoreLanguage = new HashMap<String, Object>();
			dublinCoreLanguage.put("language", this.language);
			map.put("_dc", dublinCoreLanguage);
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
	 * Returns the Mobile network carrier property
	 * A mobile network carrier or MNO, also known as a wireless service provider, wireless carrier, cellular company,
	 * or mobile network carrier, is a provider of services wireless communications that owns or controls all the elements
	 * necessary to sell and deliver services to an end user.
	 * @return {@link String} value or null if the property is not set
	 */
	String getCarrier() {
		return this.carrier;
	}

	/**
	 * Sets the Mobile network carrier property
	 * A mobile network carrier or MNO, also known as a wireless service provider, wireless carrier, cellular company,
	 * or mobile network carrier, is a provider of services wireless communications that owns or controls all the elements
	 * necessary to sell and deliver services to an end user.
	 * @param newValue the new Mobile network carrier value
	 */
	void setCarrier(final String newValue) {
		this.carrier = newValue;
	}

	/**
	 * Returns the Language property
	 * The language of the environment to represent the user's linguistic, geographical, or cultural preferences for data presentation.
	 * @return {@link String} value or null if the property is not set
	 */
	String getLanguage() {
		return this.language;
	}

	/**
	 * Sets the Language property
	 * The language of the environment to represent the user's linguistic, geographical, or cultural preferences for data
	 * presentation (according to IETF RFC 3066).
	 * @param newValue the new Language value
	 */
	void setLanguage(final String newValue) {
		this.language = newValue;
	}
	/**
	 * Returns the Operating system property
	 * The name of the operating system used when the observation was made. The attribute should not contain any version
	 * information such as '10.5.3', but instead contain 'edition' designations such as 'Ultimate' or 'Professional'.
	 * @return {@link String} value or null if the property is not set
	 */
	String getOperatingSystem() {
		return this.operatingSystem;
	}

	/**
	 * Sets the Operating system property
	 * The name of the operating system used when the observation was made. The attribute should not contain any version
	 * information such as '10.5.3', but instead contain 'edition' designations such as 'Ultimate' or 'Professional'.
	 * @param newValue the new Operating system value
	 */
	void setOperatingSystem(final String newValue) {
		this.operatingSystem = newValue;
	}

	/**
	 * Returns the Operating system version property
	 * The full version identifier for the operating system used when the observation was made. Versions are generally
	 * numerically composed but may be in a vendor defined format.
	 * @return {@link String} value or null if the property is not set
	 */
	String getOperatingSystemVersion() {
		return this.operatingSystemVersion;
	}

	/**
	 * Sets the Operating system version property
	 * The full version identifier for the operating system used when the observation was made. Versions are generally
	 * numerically composed but may be in a vendor defined format.
	 * @param newValue the new Operating system version value
	 */
	void setOperatingSystemVersion(final String newValue) {
		this.operatingSystemVersion = newValue;
	}

	/**
	 * Returns the Type property
	 * The type of the application environment.
	 * @return {@link XDMLifecycleEnvironmentTypeEnum} value or null if the property is not set
	 */
	XDMLifecycleEnvironmentTypeEnum getType() {
		return this.type;
	}

	/**
	 * Sets the Type property
	 * The type of the application environment.
	 * @param newValue the new Type value
	 */
	void setType(final XDMLifecycleEnvironmentTypeEnum newValue) {
		this.type = newValue;
	}
}
