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

package com.adobe.marketing.mobile.internal.compatibility;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.services.CacheFileService;
import com.adobe.marketing.mobile.internal.util.FileUtils;
import com.adobe.marketing.mobile.internal.util.StringEncoder;
import com.adobe.marketing.mobile.internal.util.StringUtils;
import com.adobe.marketing.mobile.services.DeviceInforming;

import java.io.File;
import java.util.Map;

/**
 * Legacy cache file management class. Only exists to allow compatibility with
 * Configuration and Campaign extension for using {@link RemoteDownloader}.
 * NOTE: Bulk of the logic in this class has been copied from
 * {@link com.adobe.marketing.mobile.CacheManager}.
 */
public class CacheManager implements CacheFileService {

    private static final String LOG_TAG = CacheManager.class.getSimpleName();
    public static final String DEFAULT_CACHE_DIR = "adbdownloadcache";
    private static final String PARTIAL_FILE_SUFFIX = "_partial";

    private final DeviceInforming deviceInforming;

    public CacheManager(final DeviceInforming deviceInforming) {
        this.deviceInforming = deviceInforming;
    }

    /**
     * Creates a new file in the specified directory. The file name format is
     * <i>sha2hash.lastmodified_partial</i>.
     *
     * @param key the url to use to construct a sha2hash. If this is null, the file will not be created.
     * @param metadata the metadata associated with the file, that will be used to name the cache file.
     *                 {@code METADATA_LAST_MODIFIED_DATE} is required and will be
     *                 used to record the date in the file name itself. If this is null, file will not be created.
     *                 Also supports {@code METADATA_ETAG}.
     * @param cacheDir Optional sub-directory name where the file is to be created. If not provided, {@link #DEFAULT_CACHE_DIR} will be used
     * @return A File object representing the new file created in the {@link #DEFAULT_CACHE_DIR}. Will be null if there
     * are errors.
     */
    @Nullable
    @Override
    public File createCacheFile(@NonNull String key, @NonNull Map<String, String> metadata, @Nullable String cacheDir) {
        final String sha2HashedUrl = StringEncoder.sha2hash(key);

        if (sha2HashedUrl == null || sha2HashedUrl.isEmpty()) {
            MobileCore.log(LoggingMode.DEBUG, LOG_TAG,
                    "Invalid url parameter while attempting to create cache file. Could not save data.");
            return null;
        }

        final String lastModified = metadata.get(METADATA_KEY_LAST_MODIFIED_EPOCH);
        final String eTag = metadata.get(METADATA_KEY_ETAG);

        if (lastModified == null) {
            MobileCore.log(LoggingMode.DEBUG, LOG_TAG,
                    "Invalid lastModified parameter while attempting to create cache file. Could not save data.");
            return null;
        }

        final File cacheDirectory = getDownloadCacheDirectory(cacheDir);

        if (cacheDirectory == null) {
            MobileCore.log(LoggingMode.DEBUG, LOG_TAG, "Unable to create cache directory.");
            return null;
        }

        String baseFilePath = getBaseFilePath(key, cacheDir);

        if (baseFilePath != null) {
            if (eTag != null) {
                return new File(baseFilePath + "." + StringEncoder.getHexString(eTag) + "." + lastModified +
                        PARTIAL_FILE_SUFFIX);
            } else {
                MobileCore.log(LoggingMode.DEBUG, LOG_TAG,
                        "Server did not return ETag for " + key);
                return new File(baseFilePath + "." + lastModified + PARTIAL_FILE_SUFFIX);
            }
        } else {
            MobileCore.log(LoggingMode.DEBUG, LOG_TAG, "Could not create a new cache file object!");
            return null;
        }
    }

    /**
     * Return the filename in the provided directory that corresponds to the provided url.
     * The sha2hash of the URL is the prefix of the file.
     *
     * @param key The handle originally used to create the cache file which is being searched for.
     * @param cacheDir Optional directory name.  If not provided, {@link #DEFAULT_CACHE_DIR} will be used
     * @param ignorePartial If true, then the cached file if found will be returned even if the file is a partial file
     *                      (contains the "_partial") post fix.
     * @return The cached file if one is found, null otherwise.
     */
    @Nullable
    @Override
    public File getCacheFile(@NonNull String key, @Nullable String cacheDir, boolean ignorePartial) {
        final File cacheDirectory = getDownloadCacheDirectory(cacheDir);

        if (cacheDirectory == null) {
            return null;
        }

        final File[] cachedFiles = cacheDirectory.listFiles();

        if (cachedFiles == null || cachedFiles.length == 0) {
            MobileCore.log(LoggingMode.DEBUG, "Cached Files - Directory is empty (%s).", cacheDirectory.getAbsolutePath());
            return null;
        }

        final String hashedName = StringEncoder.sha2hash(key);

        for (File file : cachedFiles) {

            String pathWithoutExtension = getPathWithoutExtension(file.getName());

            if (pathWithoutExtension != null && pathWithoutExtension.equals(hashedName)) {
                String pathExtension = getPathExtension(file.getName());

                if (ignorePartial && pathExtension != null && pathExtension.contains(PARTIAL_FILE_SUFFIX)) {
                    MobileCore.log(LoggingMode.DEBUG, LOG_TAG, "Cached Files - File is incomplete (%s)." + key);
                    return null;
                }

                return file;
            }
        }

        MobileCore.log(LoggingMode.DEBUG, "Cached Files - File has not previously been cached (%s).", key);
        return null;
    }

    /**
     * Deletes the file cached for the handle given. This will delete the file even if it has been partially downloaded.
     *
     * @param key The handle to use for retrieving the cached file
     * @param cacheDir Optional directory name where the file was cached.  If not provided, {@link #DEFAULT_CACHE_DIR} will be used
     * @return true, if the file was found and deleted. False otherwise.
     */
    @Override
    public boolean deleteCacheFile(@NonNull final String key, @Nullable final String cacheDir) {
        if (key == null || key.isEmpty()) {
            MobileCore.log(LoggingMode.DEBUG, LOG_TAG, "Cached File - Failed to delete cached file (file path was empty)");
            return false;
        }

        final File cachedFile = getCacheFile(key, cacheDir, false);

        if (cachedFile != null) {
            if (cachedFile.isDirectory()) {
                return deleteDirectory(cachedFile, true);
            } else {
                return cachedFile.delete();
            }
        }

        return false;
    }

    /**
     * Marks the file provided as complete, regarding it as fully processed and not partial anymore.
     * @param cacheFile the file to be marked complete
     *
     * @return the completed cache file if marking complete was successful, null otherwise.
     */
    @Nullable
    @Override
    public File markComplete(@Nullable final File cacheFile) {
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
            MobileCore.log(LoggingMode.WARNING, LOG_TAG, "Cached Files - Failed to delete partial file " + cacheFileName);
        }

        if (cacheFile.renameTo(renamedCacheFile)) {
            return renamedCacheFile;
        }

        return null;
    }

    /**
     * Retrieves the metadata based on the {@code cacheFilePath} provided.
     * @param metadataKey the key for the metadata that is to be fetched
     * @param cacheFilePath the path of the cache file for which metadata is to be fetched
     *
     * @return value of the metadataKey if it exists and is valid, null otherwise
     */
    @Nullable
    @Override
    public String getMetadata(@NonNull final String metadataKey, @Nullable final String cacheFilePath) {
        if (METADATA_KEY_LAST_MODIFIED_EPOCH.equals(metadataKey)) {
            return getLastModified(cacheFilePath);
        } else {
            return null;
        }
    }

    /**
     * Returns the absolute file path (without any extension) for the {@code url}.
     *
     * @param url The URL to be hashed in order to form the file name.
     * @param cacheDir The directory where the file will reside - overridden from the cache manager default ({@link #DEFAULT_CACHE_DIR})
     * @return The path as a string.
     */
    @Nullable
    @Override
    public String getBaseFilePath(@NonNull final String url, final String cacheDir) {
        final File cacheDirectory = getDownloadCacheDirectory(cacheDir);

        if (cacheDirectory == null) {
            MobileCore.log(LoggingMode.DEBUG, LOG_TAG, "Unable to create cache directory.");
            return null;
        }

        return (cacheDirectory.getPath() + File.separator + StringEncoder.sha2hash(url));
    }

    /**
     * Returns the File representing the cache directory that can be used to read and write from.
     *
     * @param cacheDir Optional directory name.  If not provided, {@link #DEFAULT_CACHE_DIR} will be used
     * @return The cache directory that is readable and writable. If cannot be created or is not valid then null.
     */
    private File getDownloadCacheDirectory(final String cacheDir) {
        final String cacheSubDirectory = !StringUtils.isNullOrEmpty(cacheDir)
                ? cacheDir : DEFAULT_CACHE_DIR;
        final File baseCacheDirectory = deviceInforming.getApplicationCacheDir();

        File downloadCacheDirectory = null;

        if (FileUtils.isWritableDirectory(baseCacheDirectory)) {
            downloadCacheDirectory = new File(baseCacheDirectory, cacheSubDirectory);

            if (!downloadCacheDirectory.exists() && !downloadCacheDirectory.mkdir()) {
                MobileCore.log(LoggingMode.WARNING, LOG_TAG, "Cached File - Failed to open/make download cache directory " + downloadCacheDirectory.toString());
                return null;
            }
        }

        return downloadCacheDirectory;
    }

    /**
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
                    MobileCore.log(LoggingMode.WARNING, LOG_TAG, "Could not delete %s - this was not needed anymore" + file.getName());
                }

                continue;
            }

            // if this is a directory and we are doing a recursive delete,
            // we need to make sure we clear its contents before we delete it
            final String[] innerFiles = file.list();

            // if the directory doesn't have files, delete it
            if (innerFiles == null || innerFiles.length == 0) {
                if (!file.delete()) {
                    MobileCore.log(LoggingMode.WARNING, LOG_TAG, "Could not delete file: " + file.getName());
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
                MobileCore.log(LoggingMode.WARNING, LOG_TAG, "Could not delete file: " + file.getName());
            }

        }

        return directory.delete();
    }

    /**
     * Looks for a date in the file extension of the file represented by the path, and return the datetime.
     *
     * @param path The file path.
     * @return A datetime value parsed from the file extension in the path. If parse fails, then return 0.
     */
    private String getLastModified(final String path) {
        // quick out
        if (path == null || path.isEmpty()) {
            MobileCore.log(LoggingMode.DEBUG, LOG_TAG, "Cached File - Path was null or empty for Cache File. Could not get Last Modified Date.");
            return null;
        }

        final String[] splitExtension = splitPathExtension(getPathExtension(path));

        if (splitExtension == null || splitExtension.length == 0) {
            MobileCore.log(LoggingMode.DEBUG, LOG_TAG, "Cached File - No last modified date for file. Extension had no values after split.");
            return null;
        }

        String lastModified = null;

        try {
            lastModified = splitExtension[0];
        } catch (NumberFormatException ex) {
            MobileCore.log(LoggingMode.DEBUG, LOG_TAG, "Could not get the last modified date for cache file " + path);
        }

        return lastModified;
    }

    /**
     * Returns the substring of the file name after the last "."
     *
     * @param path The file path to return an extension for.
     * @return The extension of the file. If no "." was found, then return the same path back.
     */
    private String getPathExtension(final String path) {
        // quick out
        if (path == null || path.isEmpty()) {
            MobileCore.log(LoggingMode.DEBUG, LOG_TAG, "Cached File - Path was null or empty for Cache File");
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
    private String[] splitPathExtension(final String extension) {
        if (extension == null || extension.length() == 0) {
            MobileCore.log(LoggingMode.VERBOSE, LOG_TAG, "Extension was null or empty on Cache File.");
            return new String[]{};
        }

        return extension.split("_");
    }

    /**
     * Returns the substring of {@code path} before the first '.' character. If no '.' character is found,
     * then {@code path} is returned unmodified.
     *
     * @param path the path to remove the timestamp extension
     * @return the substring of {@code path} before the first '.' character, or the full {@code path} string
     * if no '.' character is found, or null if {@code path} is null
     */
    private String getPathWithoutExtension(final String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }

        int index = path.indexOf('.');
        return index != -1 ? path.substring(0, index) : path;
    }
}

