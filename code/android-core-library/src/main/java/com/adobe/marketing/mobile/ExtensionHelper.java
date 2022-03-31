package com.adobe.marketing.mobile;

/**
 * Helper methods to access protected Extension methods from different packages
 */

public class ExtensionHelper {
    public static String getName(Extension extension) {
        try {
            if (extension != null) {
                return extension.getName();
            }
        } catch (Exception ex) {

        }
        return null;
    }

    public static String getFriendlyName(Extension extension) {
        try {
            if (extension != null) {
                return extension.getFriendlyName();
            }
        } catch (Exception ex) {

        }
        return null;
    }

    public static String getVersion(Extension extension) {
        try {
            if (extension != null) {
                return extension.getVersion();
            }
        } catch (Exception ex) {

        }
        return null;
    }

    public static void onUnregistered(Extension extension) {
        try {
            if (extension != null) {
                extension.onUnregistered();
            }
        } catch (Exception ex) {

        }
    }
}
