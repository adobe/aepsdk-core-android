package com.adobe.marketing.mobile;

import com.adobe.marketing.mobile.services.Logging;
import com.adobe.marketing.mobile.services.ServiceProvider;

@Deprecated
class AndroidLoggingService implements LoggingService {

    private static final String TAG = "AdobeExperienceSDK";
    private static final Logging loggingService = ServiceProvider.getInstance().getLoggingService();

    @Override
    public void trace(String tag, String message) {
        loggingService.trace(tag, message);
    }

    @Override
    public void debug(String tag, String message) {
        loggingService.debug(tag, message);
    }

    @Override
    public void warning(String tag, String message) {
        loggingService.warning(tag, message);
    }

    @Override
    public void error(String tag, String message) {
        loggingService.error(tag, message);
    }

}