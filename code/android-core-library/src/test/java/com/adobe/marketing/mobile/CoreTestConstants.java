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

public final class CoreTestConstants {

    private CoreTestConstants() {}

    /** Holds {@code EventData} keys for the {@code Analytics} module. */
    static final class Analytics {

        static final String TRACK_ACTION = "action";
        static final String TRACK_STATE = "state";
        static final String CONTEXT_DATA = "contextdata";

        private Analytics() {}
    }

    /** Holds {@code EventData} keys for the {@code Configuration} module. */
    static final class Configuration {

        static final String CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID = "config.appId";
        static final String CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH = "config.filePath";
        static final String CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG = "config.update";
        static final String CONFIGURATION_REQUEST_CONTENT_CLEAR_UPDATED_CONFIG =
                "config.clearUpdates";
    }

    /** Holds {@code EventData} keys for the {@code Identity} module. */
    public static final class Identity {

        static final String PUSH_ID = "pushidentifier";
        static final String ADV_ID = "advertisingidentifier";

        private Identity() {}
    }

    /** Holds {@code EventData} keys for the {@code Lifecycle} module. */
    public static final class Lifecycle {

        static final String ADDITIONAL_CONTEXT_DATA = "additionalcontextdata";
        static final String LIFECYCLE_ACTION_KEY = "action";

        private Lifecycle() {}
    }

    /** Holds {@code EventData} keys for the {@code Signal} module. */
    static final class Signal {

        static final String SIGNAL_CONTEXT_DATA = "contextdata";

        private Signal() {}
    }
}
