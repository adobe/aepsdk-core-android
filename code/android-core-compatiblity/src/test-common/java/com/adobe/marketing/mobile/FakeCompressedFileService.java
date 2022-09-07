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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FakeCompressedFileService implements CompressedFileService {

	private static final String LOG_TAG = FakeCompressedFileService.class.getSimpleName();
	private static final int MAX_BUFFER_SIZE = 4096;

	@Override
	public boolean extract(final File zipFilePath, final FileType fileType, final String outputDirectoryPath) {

		if (!FileType.ZIP.equals(fileType)) {
			Log.error(LOG_TAG, "%s file type is not supported!", fileType);
			return false;
		}

		if (zipFilePath == null || outputDirectoryPath == null) {
			Log.warning(LOG_TAG, "Extraction failed - Invalid source or destination specified");
			return false;
		}

		File folder = new File(outputDirectoryPath);

		if (!folder.exists() && !folder.mkdir()) {
			Log.error(LOG_TAG, "Could not create the output directory %s", outputDirectoryPath);
			return false;
		}

		ZipInputStream zipInputStream = null;
		FileInputStream fileInputStream = null;
		boolean entryProcessedSuccessfully = true;

		Log.debug(LOG_TAG, "Zip file path %s ", zipFilePath.getAbsolutePath());
		Log.debug(LOG_TAG,  "Zip file size %d", zipFilePath.length());

		try {
			fileInputStream = new FileInputStream(zipFilePath);
			zipInputStream = new ZipInputStream(fileInputStream);
			//get the zipped file list entry
			ZipEntry ze = zipInputStream.getNextEntry();

			if (ze == null) {
				//Invalid zip file!
				entryProcessedSuccessfully = false;
				Log.error(LOG_TAG, "Zip file was invalid");
			}

			while (ze != null && entryProcessedSuccessfully) {
				String fileName = ze.getName();
				File newZipEntryFile = new File(outputDirectoryPath + File.separator + fileName);

				if (ze.isDirectory()) {
					//handle directory
					entryProcessedSuccessfully = handleDirectory(newZipEntryFile);
				} else {
					//handle file
					entryProcessedSuccessfully = handleFile(newZipEntryFile, zipInputStream);
				}

				ze = zipInputStream.getNextEntry();
			}

			zipInputStream.closeEntry();

		} catch (IOException ex) {
			Log.error(LOG_TAG, "Extraction failed - %s", ex);
			ex.printStackTrace();
			entryProcessedSuccessfully = false;
		} finally {
			if (zipInputStream != null) {
				try {
					zipInputStream.close();
				} catch (Exception e) {
					Log.trace(LOG_TAG, "Error closing the zip inputstream - %s", e);
				}
			}

			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (Exception e) {
					Log.trace(LOG_TAG, "Error closing the inputstream - %s", e);
				}
			}
		}

		return entryProcessedSuccessfully;
	}

	/**
	 * Handle creation of a directory specified in the {@code directory}.
	 *
	 * @param directory The {@link File} specifying the directory to be created
	 *
	 * @return Indication whether the directory was created or not
	 */
	private boolean handleDirectory(final File directory) {
		if (directory.exists()) {
			return true;
		}

		boolean parentFoldersCreated = directory.mkdirs();

		if (!parentFoldersCreated) {
			Log.error(LOG_TAG, "Extraction failed - Could not create the folder structure during extraction!");
		}

		return parentFoldersCreated;
	}

	/**
	 * Handle creation of a file in the extracted archive.
	 *
	 * @param newZipEntryFile The {@link File} that needs to be written to.
	 * @param zipInputStream The {@link ZipInputStream} of the archive being extracted. This is the InputStream that will
	 *                       be read from.
	 * @return Indication whether the file was successfully created with content from the input stream, or not.
	 */
	private boolean handleFile(final File newZipEntryFile, final ZipInputStream zipInputStream) {
		//handle a file
		FileOutputStream fos = null;
		boolean isError = false;

		try {
			fos = new FileOutputStream(newZipEntryFile);
			int len;
			byte[] buffer = new byte[MAX_BUFFER_SIZE];

			while ((len = zipInputStream.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
		} catch (IOException e) {
			Log.error(LOG_TAG, "Extraction failed - Could not write to file - %s", e);
			isError = true;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ex) {
					Log.trace(LOG_TAG, "Error closing file output stream - %s", ex);
				}
			}
		}

		return !isError;
	}


}
