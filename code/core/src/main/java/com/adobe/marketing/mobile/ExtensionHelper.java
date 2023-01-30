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

/** Helper methods to access protected Extension methods from different packages */
public class ExtensionHelper {

    private ExtensionHelper() {}

    public static @Nullable String getName(@NonNull final Extension extension) {
        try {
            return extension.getName();
        } catch (Exception e) {
            return null;
        }
    }

    public static @Nullable String getFriendlyName(@NonNull final Extension extension) {
        try {
            return extension.getFriendlyName();
        } catch (Exception e) {
            return null;
        }
    }

    public static @Nullable String getVersion(@NonNull final Extension extension) {
        try {
            return extension.getVersion();
        } catch (Exception e) {
            return null;
        }
    }

    public static @Nullable Map<String, String> getMetadata(@NonNull final Extension extension) {
        try {
            return extension.getMetadata();
        } catch (Exception e) {
            return null;
        }
    }

    public static void notifyUnregistered(@NonNull final Extension extension) {
        try {
            extension.onUnregistered();
        } catch (Exception ignored) {
        }
    }

    public static void notifyRegistered(@NonNull final Extension extension) {
        try {
            extension.onRegistered();
        } catch (Exception ignored) {
        }
    }

    public static void notifyError(
            @NonNull final Extension extension, @NonNull final ExtensionUnexpectedError error) {
        try {
            extension.onUnexpectedError(error);
        } catch (Exception ignored) {
        }
    }
}
