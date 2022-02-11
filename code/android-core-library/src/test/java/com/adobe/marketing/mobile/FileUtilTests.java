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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.adobe.marketing.mobile.FileTestHelper.MOCK_CONFIG_JSON;
import static com.adobe.marketing.mobile.FileTestHelper.MOCK_FILE_NAME;
import static org.junit.Assert.*;


public class FileUtilTests {

	private FileTestHelper fileTestHelper;

	@Before
	public void setup() {
		fileTestHelper = new FileTestHelper();
	}

	@After
	public void tearDown() {
		fileTestHelper.deleteTempCacheDirectory();

	}

	@Test
	public void testClassIsWellDefined() {
		try {
			TestHelper.assertUtilityClassWellDefined(FileUtil.class);
		} catch (Exception e) {
			fail("FileUtil class is not well defined, throwing exception " + e);
		}
	}

	// =================================================================================================================
	// protected boolean isValidDirectory(final File directory)
	// =================================================================================================================
	@Test
	public void testIsValidDirectory_when_Null() {
		// Test
		final boolean isValid = FileUtil.isValidDirectory(null);
		// Verify
		assertFalse("isValidDirectory should return false for null values ", isValid);
	}

	@Test
	public void testIsValidDirectory_when_File() {
		// Test
		fileTestHelper.placeSampleCacheFile();
		final boolean isValid = FileUtil.isValidDirectory(new File(fileTestHelper.getCacheDirectory() + File.separator +
								MOCK_FILE_NAME));
		// Verify
		assertFalse("isValidDirectory should return false for a file", isValid);
	}

	@Test
	public void testIsValidDirectory_when_Directory() {
		// Test
		final boolean isValid = FileUtil.isValidDirectory(fileTestHelper.getCacheDirectory());
		// Verify
		assertTrue("isValidDirectory should return true for a valid directory", isValid);
	}

	// =================================================================================================================
	// protected String readStringFromFile(final File file)
	// =================================================================================================================
	@Test
	public void testReadJsonStringFromFile_When_ValidFile() {
		String content = FileUtil.readStringFromFile(fileTestHelper.placeSampleCacheFile());
		assertEquals(MOCK_CONFIG_JSON, content);
	}

	@Test
	public void testReadJsonStringFromFile_When_NullFile_Then_ReturnsNull() {
		String content = FileUtil.readStringFromFile(null);
		assertNull(content);
	}

	@Test
	public void testReadJsonStringFromFile_When_DirectoryInsteadOfFile_Then_ReturnsNull() {
		String content = FileUtil.readStringFromFile(fileTestHelper.placeSampleCacheDirectory("testDirectory",
						 "testFileName"));
		assertNull(content);
	}
}