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

import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.signal.SignalConstants;
import com.adobe.marketing.mobile.signal.SignalExtension;

public class Signal {
    private final static String EXTENSION_VERSION = "2.0.0";
    private static final String CLASS_NAME = "Signal";

    public static final Class<? extends Extension> EXTENSION = SignalExtension.class;

    private Signal() {
    }

    /**
     * Returns the version of the Signal extension.
     *
     * @return the version of the Signal extension.
     */
    public static String extensionVersion() {
        return EXTENSION_VERSION;
    }

    /**
     * Registers the Signal extension with the Mobile Core.
     * This method should be called before calling {@link MobileCore#start(AdobeCallback)}.
     */
    @Deprecated
    public static void registerExtension() {
        MobileCore.registerExtension(SignalExtension.class, extensionError -> {
            Log.error(SignalConstants.LOG_TAG, CLASS_NAME, "%s - There was an error when registering the UserProfile extension: %s", CLASS_NAME,
                    extensionError.getErrorName());
        });
    }
}
