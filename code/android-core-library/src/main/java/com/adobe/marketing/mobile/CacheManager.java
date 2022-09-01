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

import com.adobe.marketing.mobile.internal.util.FileUtils;
import com.adobe.marketing.mobile.internal.util.StringEncoder;
import com.adobe.marketing.mobile.services.DeviceInforming;

import com.adobe.marketing.mobile.internal.util.StringUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Use {@link com.adobe.marketing.mobile.internal.compatibility.CacheManager} instead for compatibility.
 * TODO: Remove this class when Java version of ConfigurationExtension is deleted
 */
@Deprecated
class CacheManager {

	private static final String LOG_TAG = CacheManager.class.getSimpleName();
	private static final int    LSB_8_MASK          = 0xFF;
	private static final String DEFAULT_CACHE_DIR = "adbdownloadcache";
	private static final String PARTIAL_FILE_SUFFIX = "_partial";

	private DeviceInforming deviceInforming;

	/**
	 * A valid {@link SystemInfoService} instance is required for instantiating the CacheManager.
	 *
	 * @param deviceInforming A  valid {@link DeviceInforming} instance.
	 * @throws MissingPlatformServicesException Thrown if the {@link SystemInfoService} instance is null.
	 */
	CacheManager(final DeviceInforming deviceInforming) throws MissingPlatformServicesException {
		if (deviceInforming == null) {
			throw new MissingPlatformServicesException("DeviceInforming implementation missing");
		}

		this.deviceInforming = deviceInforming;
	}

	/**
	 * Return the filename in the provided directory that corresponds to the provided url.
	 * The sha2hash of the URL is the prefix of the file.
	 *
	 * @param url           The URL for which the cached file is being searched for.
	 * @param cacheDirectoryOverride Optional directory name.  If not provided, {@link #DEFAULT_CACHE_DIR} will be used
	 * @param ignorePartial If true, then the cached file if found will be returned even if the file is a partial file
	 *                      (contains the "_partial") post fix.
	 * @return The cached file if one is found, null otherwise.
	 */
	File getFileForCachedURL(final String url, final String cacheDirectoryOverride, final boolean ignorePartial) {
		final File cacheDirectory = getDownloadCacheDirectory(cacheDirectoryOverride);

		if (cacheDirectory == null) {
			return null;
		}

		final File[] cachedFiles = cacheDirectory.listFiles();

		if (cachedFiles == null || cachedFiles.length == 0) {
			Log.debug(LOG_TAG, "Cached Files - Directory is empty (%s).", cacheDirectory.getAbsolutePath());
			return null;
		}

		final String hashedName = sha2hash(url);

		for (File file : cachedFiles) {

			String pathWithoutExtension = getPathWithoutExtension(file.getName());

			if (pathWithoutExtension != null && pathWithoutExtension.equals(hashedName)) {
				String pathExtension = getPathExtension(file.getName());

				if (ignorePartial && pathExtension != null && pathExtension.contains(PARTIAL_FILE_SUFFIX)) {
					Log.debug(LOG_TAG, "Cached Files - File is incomplete (%s).", url);
					return null;
				}

				return file;
			}
		}

		Log.debug(LOG_TAG, "Cached Files - File has not previously been cached (%s).", url);
		return null;
	}

	/**
	 * Returns the absolute file path (without any extension) for the {@code url}.
	 *
	 * @param url The URL to be hashed in order to form the file name.
	 * @param cacheDirectoryOverride The directory where the file will reside - overridden from the cache manager default ({@link #DEFAULT_CACHE_DIR})
	 * @return The path as a string.
	 */
	String getBaseFilePath(final String url, final String cacheDirectoryOverride) {
		final File cacheDirectory = getDownloadCacheDirectory(cacheDirectoryOverride);

		if (cacheDirectory == null) {
			Log.debug(LOG_TAG, "Unable to create cache directory.");
			return null;
		}

		return (cacheDirectory.getPath() + File.separator + sha2hash(url));
	}

	/**
	 * Creates a new file in the specified directory. The file name format is
	 * <i>sha2hash.lastmodified_partial</i>.
	 *
	 * @param url             The url to use to construct a sha2hash. If this is null, the file will not be created.
	 * @param etag            The etag returned by the server to specify a resource version.
	 * @param cacheDirectoryOverride Optional directory name.  If not provided, {@link #DEFAULT_CACHE_DIR} will be used
	 * @param lastModified    The last modified date used to record the date in the file name itself. If this is null,
	 *                     file will not be created.
	 * @return A File object representing the new file created in the {@link #DEFAULT_CACHE_DIR}. Will be null if there
	 * are errors.
	 */
	File createNewCacheFile(final String url, final String etag, final String cacheDirectoryOverride,
							final Date lastModified) {
		final String sha2HashedUrl = sha2hash(url);

		if (sha2HashedUrl == null || sha2HashedUrl.isEmpty()) {
			Log.debug(LOG_TAG,
					  "Invalid url parameter while attempting to create cache file. Could not save data.");
			return null;
		}

		if (lastModified == null) {
			Log.debug(LOG_TAG,
					  "Invalid lastModified parameter while attempting to create cache file. Could not save data.");
			return null;
		}

		final File cacheDirectory = getDownloadCacheDirectory(cacheDirectoryOverride);

		if (cacheDirectory == null) {
			Log.debug(LOG_TAG, "Unable to create cache directory.");
			return null;
		}

		String baseFilePath = getBaseFilePath(url, cacheDirectoryOverride);

		if (baseFilePath != null) {
			if (etag != null) {
				return new File(baseFilePath + "." + StringEncoder.getHexString(etag) + "." + lastModified.getTime() +
								PARTIAL_FILE_SUFFIX);
			} else {
				Log.debug(LOG_TAG,
						  "Server did not return ETag for %s.", url);
				return new File(baseFilePath + "." + lastModified.getTime() + PARTIAL_FILE_SUFFIX);
			}
		} else {
			Log.debug(LOG_TAG, "Could not create a new cache file object!");
			return null;
		}
	}

	File createNewCacheFile(final String url, final String cacheDirectoryOverride, final Date lastModified) {
		return createNewCacheFile(url, null, cacheDirectoryOverride, lastModified);
	}

	File createNewCacheFile(final String url, final Date lastModified) {
		return createNewCacheFile(url, null, lastModified);
	}

	/**
	 * Deletes all files in the default cache directory that are not in the provided list of files.
	 * <p>
	 * The files can be either directory names or urls represented by a cached file.
	 *
	 * @param files list of {@link String}s indicating the white-list of files to be kept
	 */
	void deleteFilesNotInList(final List<String> files) {
		this.deleteFilesNotInList(files, null, false);
	}

	/**
	 * Deletes all files in provided cache directory that are not in the provided list of files.
	 * <p>
	 * The files can be either directory names or urls represented by a cached file.
	 *
	 * @param files list of {@link String}s indicating the white-list of files to be kept
	 * @param cacheDirectoryOverride Optional directory name.  If not provided, {@link #DEFAULT_CACHE_DIR} will be used
	 */
	void deleteFilesNotInList(final List<String> files, final String cacheDirectoryOverride) {
		deleteFilesNotInList(files, cacheDirectoryOverride, false);
	}

	/**
	 * Deletes all files in provided cache directory that are not in the provided list of files.
	 * <p>
	 * The files can be either directory names or urls represented by a cached file.
	 * <p>
	 * If {@code recursiveDelete} is true, this method will loop through sub-directories of the provided directory and
	 * recursively call this method to clear all files and sub-directories not in the provided list of {@code urls}.
	 *
	 * @param files list of string urls indicating the white-list of files to be kept
	 * @param cacheDirectoryOverride Optional directory name.  If not provided, {@link #DEFAULT_CACHE_DIR} will be used
	 * @param recursiveDelete {@code boolean} indicating whether the file delete should include sub-directories
	 */
	void deleteFilesNotInList(final List<String> files, final String cacheDirectoryOverride,
							  final boolean recursiveDelete) {
		final File cacheDirectory = getDownloadCacheDirectory(cacheDirectoryOverride);

		if (cacheDirectory == null) {
			//delete successful
			return;
		}

		final File[] cachedFiles = cacheDirectory.listFiles();

		if (cachedFiles == null || cachedFiles.length == 0) {
			Log.debug(LOG_TAG, "Cached Files - Directory is empty (%s).", cacheDirectory.getAbsolutePath());
			return;
		}

		// turn our list of urls into hashed versions
		final ArrayList<String> hashedUrls = new ArrayList<String>();

		if (files != null) {
			for (final String file : files) {
				// in case of recursion, make sure we only hash once
				if (StringUtils.stringIsUrl(file)) {
					hashedUrls.add(sha2hash(file));
				} else {
					hashedUrls.add(file);
				}
			}
		}

		for (final File file : cachedFiles) {
			// if this is a directory and we are doing a recursive delete,
			// we need to make sure we clear its contents before we delete it
			if (file.isDirectory()) {
				final String directoryName = file.getName();

				// this directory is in the whitelist, leave it alone
				if (hashedUrls.contains(directoryName)) {
					continue;
				}

				deleteDirectory(file, recursiveDelete);

			} else {
				final String fileName = file.getName();
				final String fileHash = getPathWithoutExtension(fileName);

				if (!hashedUrls.contains(fileHash) && !file.delete()) {
					Log.debug(LOG_TAG, "Unable to delete cached file that is no longer needed: %s", file.getName());
				}
			}
		}
	}

	/**
	 * Delete the file cached for the URL given. This will delete the file even if it has been partially downloaded.
	 *
	 * @param url The URL to use for calculating the sha2hash which will then determine the cached file name.
	 * @param cacheDirectoryOverride Optional directory name.  If not provided, {@link #DEFAULT_CACHE_DIR} will be used
	 * @return true, if the file was found and deleted. False otherwise.
	 */
	boolean deleteCachedDataForURL(final String url, final String cacheDirectoryOverride) {
		if (url == null || url.isEmpty()) {
			Log.debug(LOG_TAG, "Cached File - Failed to delete cached file (file path was empty)");
			return false;
		}

		final File cachedFile = getFileForCachedURL(url, cacheDirectoryOverride, false);

		if (cachedFile != null) {
			if (cachedFile.isDirectory()) {
				return deleteDirectory(cachedFile, true);
			} else {
				return cachedFile.delete();
			}
		}

		return false;
	}

	boolean deleteCachedDataForURL(final String url) {
		return this.deleteCachedDataForURL(url, null);
	}

	/**
	 *
	 * Delete the directory specified.
	 *
	 * @param directory The Directory to be deleted
	 * @param recursive true if the directory should be deleted recursively. If false, then the directory
	 *                  will be attempted to be deleted if empty
	 * @return The indication whether the directory was successfully deleted
	 */
	private boolean deleteDirectory(final File directory, final boolean recursive) {
		if (directory == null) {
			//delete successful
			return true;
		}

		final File[] cachedFiles = directory.listFiles();

		if (cachedFiles == null) {
			return false;
		}

		if (cachedFiles.length == 0) {
			return directory.delete();
		}

		for (final File file : cachedFiles) {
			if (!file.isDirectory()) {
				// If it is a file, delete it
				if (!file.delete()) {
					Log.warning(LOG_TAG, "Could not delete %s - this was not needed anymore", file.getName());
				}

				continue;
			}

			// if this is a directory and we are doing a recursive delete,
			// we need to make sure we clear its contents before we delete it
			final String[] innerFiles = file.list();

			// if the directory doesn't have files, delete it
			if (innerFiles == null || innerFiles.length == 0) {
				if (!file.delete()) {
					Log.warning(LOG_TAG, "Could not delete file: %s", file.getName());
				}

				continue;
			}

			// if we have aren't doing recursive delete, we can't delete the directory...move along
			if (!recursive) {
				continue;
			}

			//Otherwise delete the contents recursively.
			deleteDirectory(file, true);

			//Now delete the directory itself.
			if (!file.delete()) {
				Log.warning(LOG_TAG, "Could not delete file: %s", file.getName());
			}

		}

		return directory.delete();
	}

	/**
	 * Removes the "_partial" post fix from the file name.
	 *
	 * @param cacheFile The file to be renamed.
	 * @return The renamed file. Will be null if the rename failed.
	 */
	File markFileAsCompleted(final File cacheFile) {
		if (cacheFile == null) {
			return null;
		}

		// remove "_partial" from extension
		String cacheFileName = cacheFile.getAbsolutePath();

		if (!cacheFileName.contains(PARTIAL_FILE_SUFFIX)) {
			return cacheFile;
		}

		File renamedCacheFile = new File(cacheFileName.replace(PARTIAL_FILE_SUFFIX, ""));

		if (renamedCacheFile.exists() && !renamedCacheFile.delete()) {
			Log.warning(LOG_TAG, "Cached Files - Failed to delete partial file %s", cacheFileName);
		}

		if (cacheFile.renameTo(renamedCacheFile)) {
			return renamedCacheFile;
		}

		return null;
	}

	/**
	 * Returns the File representing the cache directory that can be used to read and write from.
	 *
	 * @param cacheDirectoryOverride Optional directory name.  If not provided, {@link #DEFAULT_CACHE_DIR} will be used
	 *
	 * @return The cache directory that is readable and writable. If cannot be created or is not valid then null.
	 */
	File getDownloadCacheDirectory(final String cacheDirectoryOverride) {
		final String cacheSubDirectory = !StringUtils.isNullOrEmpty(cacheDirectoryOverride)
										 ? cacheDirectoryOverride : DEFAULT_CACHE_DIR ;
		final File baseCacheDirectory = deviceInforming.getApplicationCacheDir();

		File downloadCacheDirectory = null;

		if (FileUtils.isValidDirectory(baseCacheDirectory)) {
			downloadCacheDirectory = new File(baseCacheDirectory, cacheSubDirectory);

			if (!downloadCacheDirectory.exists() && !downloadCacheDirectory.mkdir()) {
				Log.warning(LOG_TAG, "Cached File - Failed to open/make download cache directory (%s)",
							downloadCacheDirectory.toString());
				return null;
			}
		}

		return downloadCacheDirectory;
	}
	File getDownloadCacheDirectory() {
		return this.getDownloadCacheDirectory(null);
	}

	/**
	 * Looks for a date in the file extension of the file represented by the path, and return the datetime.
	 *
	 * @param path The file path.
	 * @return A datetime value parsed from the file extension in the path. If parse fails, then return 0.
	 */
	long getLastModifiedOfFile(final String path) {
		// quick out
		if (path == null || path.isEmpty()) {
			Log.debug(LOG_TAG, "Cached File - Path was null or empty for Cache File. Could not get Last Modified Date.");
			return 0;
		}

		final String[] splitExtension = splitPathExtension(getPathExtension(path));

		if (splitExtension == null || splitExtension.length == 0) {
			Log.debug(LOG_TAG, "Cached File - No last modified date for file. Extension had no values after split.");
			return 0;
		}

		long lastModified = 0;

		try {
			lastModified = Long.parseLong(splitExtension[0]);
		} catch (NumberFormatException ex) {
			Log.debug(LOG_TAG, "Could not get the last modified date for cache file (%s)", path);
		}

		return lastModified;
	}

	/**
	 * Returns the substring of the file name after the last "."
	 *
	 * @param path The file path to return an extension for.
	 * @return The extension of the file. If no "." was found, then return the same path back.
	 */
	String getPathExtension(final String path) {
		// quick out
		if (path == null || path.isEmpty()) {
			Log.debug(LOG_TAG, "Cached File - Path was null or empty for Cache File");
			return null;
		}

		return path.substring(path.lastIndexOf('.') + 1);
	}

	/**
	 * Returns a string array with all the tokens that were separated by "_" in the input parameter.
	 *
	 * @param extension The string to tokenize.
	 * @return A string array with the tokens.
	 */
	String[] splitPathExtension(final String extension) {
		if (extension == null || extension.length() == 0) {
			Log.trace(LOG_TAG, "Extension was null or empty on Cache File.");
			return new String[] {};
		}

		return extension.split("_");
	}

	/**
	 * Return a sha2hash value for the {@code String} input.
	 *
	 * @param input the {@link String} input
	 * @return A sha2hash value computed for the {@code String} input. null, the input is invalid,
	 * or the platform does not support calculating sha2hash.
	 */
	// Can be moved to utility
	String sha2hash(final String input) {
		// quick out
		if (input == null || input.isEmpty()) {
			return null;
		}

		try {
			final MessageDigest messagedigest = MessageDigest.getInstance("SHA-256");
			messagedigest.update(input.getBytes("UTF-8"));
			final byte[] messageDigest = messagedigest.digest();
			final StringBuilder sha2HexBuilder = new StringBuilder();

			for (byte aMessageDigest : messageDigest) {
				StringBuilder hexString = new StringBuilder(Integer.toHexString(LSB_8_MASK & aMessageDigest));

				while (hexString.length() < 2) {
					hexString.insert(0, "0");
				}

				sha2HexBuilder.append(hexString);
			}

			return sha2HexBuilder.toString();
		} catch (NoSuchAlgorithmException e) {
			Log.warning(LOG_TAG, "Cached Files - Failed to get sha2 hash (%s)", e);
		} catch (UnsupportedEncodingException e) {
			Log.warning(LOG_TAG, "Cached Files - Unsupported Encoding: UTF-8 (%s)", e);
		}

		return null;
	}

	/**
	 * Returns the substring of {@code path} before the first '.' character. If no '.' character is found,
	 * then {@code path} is returned unmodified.
	 *
	 * @param path the path to remove the timestamp extension
	 * @return the substring of {@code path} before the first '.' character, or the full {@code path} string
	 * if no '.' character is found, or null if {@code path} is null
	 */
	String getPathWithoutExtension(final String path) {
		if (path == null || path.isEmpty()) {
			return path;
		}

		int index = path.indexOf('.');
		return index != -1 ? path.substring(0, index) : path;
	}

}

