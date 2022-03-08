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

import java.io.*;

final class FileUtil {

	private FileUtil() {}

	/**
	 * Reads the JSONString from the provided file. Returns null if there is no file or it does not have read permissions.
	 *
	 * @param file {@link File} from which the contents are to be read
	 * @return The contents of the file in JSONString format. Returns null if file does not exist, when encounters IOException
	 * 			or if the file do not have read permission
	 */
	static String readStringFromFile(final File file) {
		final String logPrefix = "File Reader";

		try {
			if (file == null || !file.exists() || !file.canRead() || !file.isFile()) {
				Log.warning(logPrefix, "Write to file - File does not exist or don't have read permission (%s)", file);
				return null;
			}
		} catch (SecurityException e) {
			Log.debug(logPrefix, "Failed to read file (%s)", e);
			return null;
		}

		BufferedReader bufferedReader = null;
		InputStream inStream = null;

		try {
			inStream = new FileInputStream(file);
			final Reader reader = new InputStreamReader(inStream, "UTF-8");
			bufferedReader = new BufferedReader(reader);
			final StringBuilder builder = new StringBuilder();
			String line = bufferedReader.readLine();

			while (line != null) {
				builder.append(line);
				line = bufferedReader.readLine();
			}

			return builder.toString();
		} catch (IOException e) {
			Log.debug(logPrefix, "Failed to close file (%s)", e);
			return null;
		} finally {
			try {
				if (inStream != null) {
					inStream.close();
				}

				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e) {
				Log.debug(logPrefix, "Failed to close file (%s)", e);
			}
		}
	}

	/**
	 * Verifies if the {@code File} object represents a directory and the directory is writable.
	 *
	 * @param directory {@link File} the directory to check for
	 * @return {code boolean} representing the directory validation result
	 */
	static boolean isValidDirectory(final File directory) {
		return directory != null && directory.isDirectory() && directory.canWrite();
	}

	/**
	 * Copies the contents from {@code src} to {@code dest}.
	 *
	 * @param src {@link File} from which the contents are read
	 * @param dest {@link File} to which contents are written to
	 * @throws IOException if {@code src} or {@code dest} is not present or it does not have read permissions
	 */
	static void copyFile(final File src, final File dest) throws IOException, NullPointerException{
		final String logPrefix = "File Copy";
		final int STREAM_READ_BUFFER_SIZE = 1024;

		InputStream input = new FileInputStream(src);
		try {
			OutputStream output = new FileOutputStream(dest);
			try {
				byte[] buffer = new byte[STREAM_READ_BUFFER_SIZE];
				int length;
				while ((length = input.read(buffer)) != -1) {
					output.write(buffer, 0, length);
				}
				Log.debug(logPrefix, "Successfully copied (%s) to (%s)", src.getCanonicalPath(), dest.getCanonicalPath());
			} finally {
				output.close();
			}
		} finally {
			input.close();
		}
	}
}
