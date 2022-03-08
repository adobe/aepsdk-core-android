package com.adobe.marketing.mobile;


import java.io.File;
import java.util.UUID;

public class E2EAndroidSystemInfoService extends AndroidSystemInfoService {
	@Override
	public File getApplicationCacheDir() {
		File systemCacheDir = super.getApplicationCacheDir();
		File tempDir = new File(systemCacheDir, String.valueOf(UUID.randomUUID()).replaceAll("-", ""));
		tempDir.mkdir();
		return tempDir;
	}

	@Override
	public File getApplicationFilesDir() {
		File systemFilesDir = super.getApplicationFilesDir();
		File tempDir = new File(systemFilesDir, String.valueOf(UUID.randomUUID()).replaceAll("-", ""));
		tempDir.mkdir();
		return tempDir;
	}
}