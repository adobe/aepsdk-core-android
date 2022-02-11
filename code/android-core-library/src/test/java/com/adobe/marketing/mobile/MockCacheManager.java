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

import org.junit.Assert;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.*;

public class MockCacheManager extends CacheManager {

	private TemporaryFolder temporaryFolder;
	private static final String DEFAULT_CACHE_DIR = "adbdownloadcache";

	public final HashMap<String, FileWithMetadata> cacheMap = new HashMap<String, FileWithMetadata>();
	public boolean allowNewCacheFileCreation = true;
	public boolean allowMarkingFileComplete = true;

	public MockCacheManager(TemporaryFolder temporaryFolder,
							SystemInfoService systemInfoService) throws MissingPlatformServicesException {
		super(systemInfoService);
		this.temporaryFolder = temporaryFolder;
	}



	@Override
	public File getFileForCachedURL(final String url, final String directory, boolean ignorePartial) {
		FileWithMetadata fileWithMetadata = cacheMap.get(url);

		if (fileWithMetadata != null) {
			return fileWithMetadata.file;
		} else {
			return null;
		}
	}

	@Override
	protected File createNewCacheFile(final String url, final String etag, final String cacheDirectoryOverride,
									  final Date lastModified) {
		if (!allowNewCacheFileCreation) {
			return null;
		}

		try {
			File newCache = null;

			if (etag != null) {
				newCache = temporaryFolder.newFile("newCacheFile" + UUID.randomUUID().toString() + "." + etag);
			} else {
				newCache = temporaryFolder.newFile("newCacheFile" + UUID.randomUUID().toString());
			}

			FileWithMetadata fileWithMetadata = new FileWithMetadata();
			fileWithMetadata.file = newCache;
			fileWithMetadata.lastModifiedDate = lastModified.getTime();
			cacheMap.put(url, fileWithMetadata);
			return newCache;
		} catch (Exception e) {
			Assert.fail("Could not create a new cached file " + e);
		}

		return null;
	}

	@Override
	protected boolean deleteCachedDataForURL(String url) {
		FileWithMetadata fileWithMetadata = cacheMap.remove(url);
		return (fileWithMetadata != null && fileWithMetadata.file != null) && fileWithMetadata.file.delete();
	}

	@Override
	protected File getDownloadCacheDirectory() {
		return new File(temporaryFolder.getRoot(), DEFAULT_CACHE_DIR);
	}

	@Override
	public long getLastModifiedOfFile(String path) {
		File file = new File(path);
		Collection<FileWithMetadata> fileWithMetadatas = cacheMap.values();
		Iterator<FileWithMetadata> it = fileWithMetadatas.iterator();

		while (it.hasNext()) {
			FileWithMetadata fileWithMetadata = it.next();

			if (fileWithMetadata.file.getName().equals(file.getName())) {
				return fileWithMetadata.lastModifiedDate;
			}
		}

		return 0;
	}


	@Override
	public File markFileAsCompleted(File cacheFile) {
		if (!allowMarkingFileComplete) {
			return null;
		}

		String url = findUrlForFile(cacheFile);

		if (url == null) {
			Assert.fail("Could not find a url that the cache file maps to!!");
		}

		try {
			File renamedFile = temporaryFolder.newFile("cacheFileRenamed");
			cacheFile.renameTo(renamedFile);
			FileWithMetadata fileWithMetadata = cacheMap.get(url);
			fileWithMetadata.file = renamedFile;
			cacheMap.put(url, fileWithMetadata);
			return renamedFile;
		} catch (Exception e) {
			Log.trace("test", "Exception while renaming file (%s)", e);
			return null;
		}
	}

	private String findUrlForFile(File cacheFile) {
		String url = null;
		Set<Map.Entry<String, FileWithMetadata>> set = cacheMap.entrySet();
		Iterator<Map.Entry<String, FileWithMetadata>> it = set.iterator();

		while (it.hasNext()) {
			Map.Entry<String, FileWithMetadata> entry = it.next();

			if (entry.getValue().file.getName().equals(cacheFile.getName())) {
				if (url == null) {
					url = entry.getKey();
				} else {
					Assert.fail("The same file resolves to more than one URL!");
				}
			}
		}

		return url;
	}

	public static class FileWithMetadata {
		public File file;
		public long lastModifiedDate;
	}
}
