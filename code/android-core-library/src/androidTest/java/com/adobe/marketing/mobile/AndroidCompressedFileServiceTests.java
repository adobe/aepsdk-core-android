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

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class AndroidCompressedFileServiceTests {

	private AndroidCompressedFileService androidCompressedFileService = new AndroidCompressedFileService();

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private File tempSource;

	@Before
	public void setup() {
		androidCompressedFileService = new AndroidCompressedFileService();
		prepareZipFile("rules.zip");
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testExtract_Happy() throws IOException {
		File tempDestination = folder.newFolder();
		assertTrue(androidCompressedFileService.extract(tempSource, CompressedFileService.FileType.ZIP,
				   tempDestination.getPath()));

		//Verify all the files
		File[] files = tempDestination.listFiles();
		assertTrue(fileNameIsFound(files, "rules.json"));
		assertTrue(fileNameIsFound(files, "assets"));

		//The test zip file also contains a rules.json (dummy file) inside of assets folder.
		//Verify that it got extracted properly
		File[] assetFolderFiles = new File(tempDestination, "assets").listFiles();
		assertTrue(fileNameIsFound(assetFolderFiles, "rules.json"));
	}

	@Test
	public void testExtract_Happy_pkzip() throws IOException {
		prepareZipFile("rules_pkzip.zip");

		File tempDestination = folder.newFolder();
		assertTrue(androidCompressedFileService.extract(tempSource, CompressedFileService.FileType.ZIP,
				   tempDestination.getPath()));

		//Verify all the files
		File[] files = tempDestination.listFiles();
		assertTrue(fileNameIsFound(files, "rules.json"));
		assertTrue(fileNameIsFound(files, "assets"));

		//The test zip file also contains a foo.html (dummy file) inside of assets folder.
		//Verify that it got extracted properly
		File[] assetFolderFiles = new File(tempDestination, "assets").listFiles();
		assertTrue(fileNameIsFound(assetFolderFiles, "foo.html"));
	}

	@Test
	public void testExtract_zipsilp() throws IOException {
		prepareZipFile("rules_zipslip.zip");

		File tempDestination = folder.newFolder();
		assertFalse(androidCompressedFileService.extract(tempSource, CompressedFileService.FileType.ZIP,
					tempDestination.getPath()));

	}

	@Test
	public void testExtract_EmptySourceFile() throws IOException {
		File emptySourceFile = folder.newFile();//empty file
		File tempDestination = folder.newFolder();
		assertFalse(androidCompressedFileService.extract(emptySourceFile, CompressedFileService.FileType.ZIP,
					tempDestination.getPath()));
	}

	@Test
	public void testExtract_InvalidSourceFile() throws IOException {
		File invalidSourceFile = folder.newFile();//Invalid File
		writeJunkToFile(invalidSourceFile);

		File tempDestination = folder.newFolder();
		assertFalse(androidCompressedFileService.extract(invalidSourceFile, CompressedFileService.FileType.ZIP,
					tempDestination.getPath()));
	}

	@Test
	public void testExtract_UnsupportedFileType() throws IOException {
		File tempDestination = folder.newFolder();
		assertFalse(androidCompressedFileService.extract(tempSource, CompressedFileService.FileType.TAR,
					tempDestination.getPath()));
	}

	@Test
	public void testExtract_NullFileType() throws IOException {
		File tempDestination = folder.newFolder();
		assertFalse(androidCompressedFileService.extract(tempSource, null, tempDestination.getPath()));
	}

	@Test
	public void testExtract_NullSource() throws IOException {
		File tempDestination = folder.newFolder();
		assertFalse(androidCompressedFileService.extract(null, CompressedFileService.FileType.ZIP, tempDestination.getPath()));
	}

	@Test
	public void testExtract_NullDestination() {
		assertFalse(androidCompressedFileService.extract(tempSource, CompressedFileService.FileType.ZIP, null));
	}

	@Test
	public void testExtract_NotCreatedDestination() throws IOException {
		//This has not been created yet = extract should create it
		String destination = tempSource.getAbsolutePath() + "temp123";
		assertTrue(androidCompressedFileService.extract(tempSource, CompressedFileService.FileType.ZIP, destination));
	}

	void prepareZipFile(String filename) {
		try {
			Resources res =     InstrumentationRegistry.getContext().getResources();
			InputStream instream = res.getAssets().open(filename);

			tempSource = folder.newFile();

			if (!handleFile(tempSource, instream)) {
				Assert.fail();
			}

		} catch (Exception e) {
			Assert.fail(e.toString());
		}
	}

	private boolean handleFile(File newFile, InputStream inputStream) {
		//handle a file
		FileOutputStream fos = null;
		boolean isError = false;

		try {
			fos = new FileOutputStream(newFile);
			int len;
			byte[] buffer = new byte[1024];

			while ((len = inputStream.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
		} catch (IOException e) {
			Log.error("", "Extraction failed - Could not write to file - %s", e);
			isError = true;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ex) {
					Log.trace("", "Error closing file output stream - %s", ex);
				}
			}
		}

		return !isError;
	}

	private void writeJunkToFile(File invalidSourceFile) throws IOException {
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(invalidSourceFile);
			fos.write("junk".getBytes());
		} catch (Exception e) {
			fail();
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}



	private boolean fileNameIsFound(File[] files, String fileName) {
		boolean found = false;

		for (File file : files) {
			if ((found = file.getName().equals(fileName))) {
				break;
			}
		}

		return found;
	}


}