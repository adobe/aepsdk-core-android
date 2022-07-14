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
