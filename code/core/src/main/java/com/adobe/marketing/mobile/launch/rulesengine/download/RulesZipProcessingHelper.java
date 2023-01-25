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

package com.adobe.marketing.mobile.launch.rulesengine.download;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.adobe.marketing.mobile.internal.CoreConstants;
import com.adobe.marketing.mobile.internal.util.FileUtils;
import com.adobe.marketing.mobile.internal.util.StringEncoder;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.util.StreamUtils;
import com.adobe.marketing.mobile.util.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/** Helper class to handle rules zip file related processing. */
class RulesZipProcessingHelper {

    private static final String TAG = "RulesZipProcessingHelper";

    @VisibleForTesting static final String TEMP_DOWNLOAD_DIR = "aepsdktmp";

    private static final String TEMP_RULES_ZIP = "rules.zip";
    private static final String TEMP_RULES_JSON = "rules.json";

    /**
     * Creates a temporary directory to store and process rules. Such a directory (if created
     * successfully) is accessible for operations in this class via {@code tag}.
     *
     * @param tag the tag which can be used later to perform operations on the content stored in the
     *     directory.
     * @return true if the directory creation is successful; false otherwise.
     */
    boolean createTemporaryRulesDirectory(final String tag) {
        if (!canProcess(tag)) return false;

        final File tempExtractDir = getTemporaryDirectory(tag);
        if (!tempExtractDir.exists() && !tempExtractDir.mkdirs()) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    TAG,
                    "Cannot access application cache directory to create temp dir.");
            return false;
        }
        return true;
    }

    /**
     * Stores the content read from the stream {@code zip} into the folder created via {@code
     * createTemporaryRulesDirectory(tag)} previously created
     *
     * @param tag tag for the folder into which the input stream should be stored into
     * @param zip the rules zip stream that is to be stored
     * @return true if the zip stream was successfully copied; false otherwise.
     */
    boolean storeRulesInTemporaryDirectory(final String tag, final InputStream zip) {
        if (!canProcess(tag)) return false;

        final boolean fileRead =
                FileUtils.readInputStreamIntoFile(getZipFileHandle(tag), zip, false);

        if (!fileRead) {
            Log.debug(CoreConstants.LOG_TAG, TAG, "Cannot read response content into temp dir.");
            return false;
        }

        return true;
    }

    /**
     * Extracts the rules.zip present in the directory created via {@code
     * createTemporaryRulesDirectory(tag)} and attempts to find and read the content of file with
     * name TEMP_RULES_JSON from that directory.
     *
     * @param tag the tag used when creating and storing contents into the temporary directory.
     * @return the contents of TEMP_RULES_JSON if it was found and was readable; null otherwise.
     */
    String unzipRules(final String tag) {
        if (!canProcess(tag)) return null;

        final File temporaryDirectory = getTemporaryDirectory(tag);
        final boolean extracted =
                FileUtils.extractFromZip(getZipFileHandle(tag), temporaryDirectory.getPath());

        if (!extracted) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    TAG,
                    "Failed to extract rules response zip into temp dir.");
            return null;
        }

        final File extractedRulesJsonFile =
                new File(temporaryDirectory.getPath() + File.separator + TEMP_RULES_JSON);

        if (!extractedRulesJsonFile.exists()) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    TAG,
                    "Extract rules directory does not contain a rules.json file.");
            return null;
        }

        try (final FileInputStream fileInputStream = new FileInputStream(extractedRulesJsonFile)) {
            final String rules = StreamUtils.readAsString(fileInputStream);

            if (rules == null) {
                Log.debug(CoreConstants.LOG_TAG, TAG, "Null content from rules.json file.");
                return null;
            }

            return rules;
        } catch (final IOException e) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    TAG,
                    "Exception while processing rules from source %s",
                    tag);
            return null;
        }
    }

    /**
     * Deletes the temporary directory created via {@code createTemporaryRulesDirectory()}
     *
     * @param tag the tag used to create that
     */
    void deleteTemporaryDirectory(final String tag) {
        if (!canProcess(tag)) return;

        FileUtils.deleteFile(getTemporaryDirectory(tag), true);
    }

    @VisibleForTesting
    File getTemporaryDirectory(@NonNull final String tag) {
        final String hash = StringEncoder.sha2hash(tag);
        return new File(
                ServiceProvider.getInstance()
                                .getDeviceInfoService()
                                .getApplicationCacheDir()
                                .getPath()
                        + File.separator
                        + TEMP_DOWNLOAD_DIR
                        + File.separator
                        + hash);
    }

    private File getZipFileHandle(@NonNull final String tag) {
        return new File(getTemporaryDirectory(tag).getPath() + File.separator + TEMP_RULES_ZIP);
    }

    private boolean canProcess(@NonNull final String tag) {
        return !StringUtils.isNullOrEmpty(tag);
    }
}
