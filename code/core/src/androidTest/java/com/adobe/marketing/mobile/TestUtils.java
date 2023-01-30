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

import android.content.Context;
import java.io.File;

@SuppressWarnings("all")
public class TestUtils {

    public static void deleteAllFilesInCacheDir(Context appContext) {
        if (appContext == null) {
            return;
        }

        File cacheDir = appContext.getCacheDir();
        File[] files = cacheDir.listFiles();

        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static String getCacheDir(Context appContext) {
        if (appContext == null) {
            return null;
        }

        return appContext.getCacheDir().getPath();
    }

    public static boolean almostEqual(long actual, long expected, long tolerance) {
        return Math.abs(actual - expected) < tolerance;
    }
}
