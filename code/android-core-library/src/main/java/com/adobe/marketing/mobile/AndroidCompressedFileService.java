package com.adobe.marketing.mobile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class AndroidCompressedFileService implements CompressedFileService {
	private static final String LOG_TAG = AndroidCompressedFileService.class.getSimpleName();
	private static final int MAX_BUFFER_SIZE = 4096;

	@Override
	public boolean extract(File zipFilePath, FileType fileType, String outputDirectoryPath) {

		if (!FileType.ZIP.equals(fileType)) {
			Log.warning(LOG_TAG, "%s file type is not supported!", fileType);
			return false;
		}

		if (zipFilePath == null || outputDirectoryPath == null) {
			Log.warning(LOG_TAG, "Extraction failed - Invalid source or destination specified");
			return false;
		}

		File folder = new File(outputDirectoryPath);

		if (!folder.exists() && !folder.mkdir()) {
			Log.warning(LOG_TAG, "Could not create the output directory %s", outputDirectoryPath);
			return false;
		}

		ZipInputStream zipInputStream = null;
		FileInputStream fileInputStream = null;
		boolean entryProcessedSuccessfully = true;

		try {
			fileInputStream = new FileInputStream(zipFilePath);
			zipInputStream = new ZipInputStream(fileInputStream);
			//get the zipped file list entry
			ZipEntry ze = zipInputStream.getNextEntry();

			String outputFolderCanonicalPath = folder.getCanonicalPath();

			if (ze == null) {
				//Invalid zip file!
				entryProcessedSuccessfully = false;
				Log.warning(LOG_TAG, "Zip file was invalid");
			}

			while (ze != null && entryProcessedSuccessfully) {
				String fileName = ze.getName();
				File newZipEntryFile = new File(outputDirectoryPath + File.separator + fileName);

				if (!newZipEntryFile.getCanonicalPath().startsWith(outputFolderCanonicalPath)) {
					Log.error(LOG_TAG,
							  "The zip file contained an invalid path. Verify that your zip file is formatted correctly and has not been tampered with.");
					return false;
				}

				if (ze.isDirectory()) {
					//handle directory
					entryProcessedSuccessfully = handleDirectory(newZipEntryFile);
				} else {
					//handle file
					File parentFolder = newZipEntryFile.getParentFile();

					if (parentFolder.exists() || parentFolder.mkdirs()) {
						entryProcessedSuccessfully = handleFile(newZipEntryFile, zipInputStream);
					} else {
						Log.warning(LOG_TAG, "Could not extract the file %s", newZipEntryFile.getAbsolutePath());
						entryProcessedSuccessfully = false;
					}
				}

				ze = zipInputStream.getNextEntry();
			}

			zipInputStream.closeEntry();

		} catch (IOException ex) {
			Log.error(LOG_TAG, "Extraction failed - %s", ex);
			entryProcessedSuccessfully = false;
		} catch (IllegalArgumentException ex) {
			Log.error(LOG_TAG, "Extraction failed - %s", ex);
			entryProcessedSuccessfully = false;
		} catch (Exception ex) {
			Log.error(LOG_TAG, "Extraction failed - %s", ex);
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
	 * @param directory The {@link File} specifying the directory to be created.
	 *
	 * @return Indication whether the directory was created or not.
	 */
	private boolean handleDirectory(File directory) {
		if (directory.exists()) {
			return true;
		}

		boolean parentFoldersCreated = directory.mkdirs();

		if (!parentFoldersCreated) {
			Log.debug(LOG_TAG, "Extraction failed - Could not create the folder structure during extraction!");
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
	private boolean handleFile(File newZipEntryFile, ZipInputStream zipInputStream) {
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
