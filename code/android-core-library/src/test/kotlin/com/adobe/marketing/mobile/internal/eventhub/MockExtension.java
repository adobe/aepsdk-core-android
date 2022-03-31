package com.adobe.marketing.mobile.internal.eventhub;

import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;

public class MockExtension extends Extension {
    MockExtension(ExtensionApi extensionApi) {
        super(extensionApi);
    }

    @Override
    protected String getName() {
        return "com.adobe.mockextension";
    }
}
