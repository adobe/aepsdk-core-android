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

interface CompressedFileService {

	enum FileType {
		ZIP, TAR
	}

	/**
	 * Extract the compressed file found in the {@code compressedFilePath}.
	 *
	 * <p>
	 * The extracted contents will be written to the {@code outputDirectoryPath}. If a platform does not contain support for a particular
	 * {@link FileType}, as requested in {@code fileType} argument, then the extract might fail.
	 *
	 * @param compressedFilePath The  path to the compressed file
	 * @param fileType The {@link FileType} of the compressed file
	 * @param outputDirectoryPath The output directory where the extracted contents will be stored
	 * @return An indication of a successful extraction
	 */
	boolean extract(File compressedFilePath, FileType fileType, String outputDirectoryPath);
}
