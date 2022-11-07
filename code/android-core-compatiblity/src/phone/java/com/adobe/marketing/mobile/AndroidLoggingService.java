package com.adobe.marketing.mobile;

import com.adobe.marketing.mobile.services.ServiceProvider;

@Deprecated
class AndroidLoggingService implements LoggingService {

    private static final String TAG = "AdobeExperienceSDK";

    @Override
    public void trace(String tag, String message) {
        ServiceProvider.getInstance().getLoggingService().trace(tag, message);
    }

    @Override
    public void debug(String tag, String message) {
        ServiceProvider.getInstance().getLoggingService().debug(tag, message);
    }

    @Override
    public void warning(String tag, String message) {
        ServiceProvider.getInstance().getLoggingService().warning(tag, message);
    }

    @Override
    public void error(String tag, String message) {
        ServiceProvider.getInstance().getLoggingService().error(tag, message);
    }

}