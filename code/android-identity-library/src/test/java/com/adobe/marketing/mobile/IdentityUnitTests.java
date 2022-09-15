/* ******************************************************************************
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

package com.adobe.marketing.mobile;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.*;

public class IdentityUnitTests {

	private static String GRADLE_PROPERTIES_PATH = "../gradle.properties";
	private static String PROPERTY_MODULE_VERSION = "moduleVersion";

	@Test
	public void extensionVersion_verifyModuleVersionInPropertiesFile_asEqual() {
		Properties properties = loadProperties(GRADLE_PROPERTIES_PATH);

		assertNotNull(Identity.extensionVersion());
		assertFalse(Identity.extensionVersion().isEmpty());

		String moduleVersion = properties.getProperty(PROPERTY_MODULE_VERSION);
		assertNotNull(moduleVersion);
		assertFalse(moduleVersion.isEmpty());

		assertEquals(moduleVersion, Identity.extensionVersion());
	}


	private Properties loadProperties(final String filepath) {
		Properties properties = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(filepath);

			properties.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return properties;
	}
}
