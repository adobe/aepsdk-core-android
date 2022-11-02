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

import java.util.Map;

/**
 * Helper methods to access protected Extension methods from different packages
 */

public class ExtensionHelper {
    public static String getName(Extension extension) {
        try {
            if (extension != null) {
                return extension.getName();
            }
        } catch (Exception ex) { }
        return null;
    }

    public static String getFriendlyName(Extension extension) {
        try {
            if (extension != null) {
                return extension.getFriendlyName();
            }
        } catch (Exception ex) { }
        return null;
    }

    public static String getVersion(Extension extension) {
        try {
            if (extension != null) {
                return extension.getVersion();
            }
        } catch (Exception ex) { }
        return null;
    }

    public static Map<String, String> getMetadata(Extension extension) {
        try {
            if (extension != null) {
                return extension.getMetadata();
            }
        } catch (Exception ex) { }
        return null;
    }

    public static void notifyUnregistered(Extension extension) {
        try {
            if (extension != null) {
                extension.onUnregistered();
            }
        } catch (Exception ex) { }
    }

    public static void notifyRegistered(Extension extension) {
        try {
            if (extension != null) {
                extension.onRegistered();
            }
        } catch (Exception ex) { }
    }

    public static void notifyError(Extension extension, ExtensionUnexpectedError error) {
        try {
            if (extension != null) {
                extension.onUnexpectedError(error);
            }
        } catch (Exception ex) { }
    }
}
