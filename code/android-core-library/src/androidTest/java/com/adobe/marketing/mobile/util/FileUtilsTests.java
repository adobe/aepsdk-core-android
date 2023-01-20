/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import com.adobe.marketing.mobile.MobileCore;
import java.io.File;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileUtilsTests {

    @BeforeClass
    public static void setup() {
        MobileCore.setApplication(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void testHappy() throws IOException {
        Context targetCtx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        File testFile = new File(targetCtx.getCacheDir(), "test_file.txt");
        testFile.createNewFile();
        assertTrue(testFile.exists());

        boolean result = FileUtils.deleteFileFromCacheDir("test_file.txt");
        assertTrue(result);
        assertFalse(testFile.exists());
    }

    @Test
    public void testFileNotExist() {
        Context targetCtx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        File testFile = new File(targetCtx.getCacheDir(), "test_file.txt");
        assertFalse(testFile.exists());

        boolean result = FileUtils.deleteFileFromCacheDir("test_file.txt");
        assertFalse(result);
        assertFalse(testFile.exists());
    }

    @Test
    public void testFileNameIsNull() {
        boolean result = FileUtils.deleteFileFromCacheDir(null);
        assertFalse(result);
    }

    @Test
    public void testFileNameIsEmpty() {
        boolean result = FileUtils.deleteFileFromCacheDir("");
        assertFalse(result);
    }
}
