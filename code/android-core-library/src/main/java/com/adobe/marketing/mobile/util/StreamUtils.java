package com.adobe.marketing.mobile.util;

import com.adobe.marketing.mobile.internal.CoreConstants;
import com.adobe.marketing.mobile.services.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class StreamUtils {
    private static final int STREAM_READ_BUFFER_SIZE = 1024;
    private static final String TAG = "StreamUtils";

    private StreamUtils() {}

    /**
     * Reads the contents of {@code InputStream} as String
     *
     * @param inputStream {@link InputStream} to read
     * @return {@link String} representation of the input stream
     */
    public static String readAsString(final InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final byte[] data = new byte[STREAM_READ_BUFFER_SIZE];
        int bytesRead;

        try {
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            final byte[] byteArray = buffer.toByteArray();
            return new String(byteArray, StandardCharsets.UTF_8);
        } catch (final IOException ex) {
            Log.trace(CoreConstants.LOG_TAG, TAG, "Unable to convert InputStream to String," + ex.getLocalizedMessage());
            return null;
        }
    }
}
