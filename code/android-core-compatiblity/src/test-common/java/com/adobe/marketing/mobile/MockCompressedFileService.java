package com.adobe.marketing.mobile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MockCompressedFileService implements CompressedFileService {

	boolean extractReturnValue = true;
	File extractParamCompressedFilePath;
	FileType extractParamFileType;
	String extractParamOutputDirectoryPath;

	interface TestExtract {
		void DoExtract(String outputDirectory);
	}

	private Map<String, TestExtract> testExtractImplMap = new HashMap<String, TestExtract>();

	public void addTestExtractForOutPutDirPath(String path, TestExtract testExtractImpl) {
		testExtractImplMap.put(path, testExtractImpl);
	}

	@Override
	public boolean extract(final File compressedFilePath, final FileType fileType, final String outputDirectoryPath) {
		extractParamCompressedFilePath = compressedFilePath;
		extractParamFileType = fileType;
		extractParamOutputDirectoryPath = outputDirectoryPath;

		TestExtract testExtractImpl = testExtractImplMap.get(outputDirectoryPath);

		if (testExtractImpl != null) {
			testExtractImpl.DoExtract(outputDirectoryPath);
		}

		return extractReturnValue;
	}
}
