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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

/**
 * Helper methods to access protected Extension methods from different packages
 */

public class ExtensionHelper {
    public static @NonNull
    String getName(@NonNull final Extension extension) {
        return extension.getName();
    }

    public static @Nullable
    String getFriendlyName(@NonNull final Extension extension) {
        return extension.getFriendlyName();
    }

    public static @Nullable
    String getVersion(@NonNull final Extension extension) {
        return extension.getVersion();
    }

    public static @Nullable
    Map<String, String> getMetadata(@NonNull final Extension extension) {
        return extension.getMetadata();
    }

    public static void notifyUnregistered(@NonNull final Extension extension) {
        extension.onUnregistered();
    }

    public static void notifyRegistered(@NonNull final Extension extension) {
        extension.onRegistered();
    }

    public static void notifyError(@NonNull final Extension extension, @NonNull final ExtensionUnexpectedError error) {
        extension.onUnexpectedError(error);
    }
}
