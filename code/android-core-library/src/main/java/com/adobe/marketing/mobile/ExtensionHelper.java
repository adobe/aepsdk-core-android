package com.adobe.marketing.mobile;

import java.util.Map;

/**
 * Helper methods to access protected Extension methods from different packages
 */

public class ExtensionHelper {
    public static String getName(final Extension extension) {
        try {
            if (extension != null) {
                return extension.getName();
            }
        } catch (Exception ex) { }
        return null;
    }

    public static String getFriendlyName(final Extension extension) {
        try {
            if (extension != null) {
                return extension.getFriendlyName();
            }
        } catch (Exception ex) { }
        return null;
    }

    public static String getVersion(final Extension extension) {
        try {
            if (extension != null) {
                return extension.getVersion();
            }
        } catch (Exception ex) { }
        return null;
    }

    public static Map<String, String> getMetadata(final Extension extension) {
        try {
            if (extension != null) {
                return extension.getMetadata();
            }
        } catch (Exception ex) { }
        return null;
    }

    public static void notifyUnregistered(final Extension extension) {
        try {
            if (extension != null) {
                extension.onUnregistered();
            }
        } catch (Exception ex) { }
    }

    public static void notifyRegistered(final Extension extension) {
        try {
            if (extension != null) {
                extension.onRegistered();
            }
        } catch (Exception ex) { }
    }

    public static void notifyError(final Extension extension, final ExtensionUnexpectedError error) {
        try {
            if (extension != null) {
                extension.onUnexpectedError(error);
            }
        } catch (Exception ex) { }
    }
}
