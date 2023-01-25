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

package com.adobe.marketing.mobile.lifecycle;

class LifecycleV2Constants {

    static final int CACHE_TIMEOUT_MILLIS = 2000;
    static final int STATE_UPDATE_TIMEOUT_MILLIS = 500;

    static final class XDMEventType {

        static final String APP_LAUNCH = "application.launch";
        static final String APP_CLOSE = "application.close";

        private XDMEventType() {}
    }

    static final class EventName {

        static final String APPLICATION_LAUNCH_EVENT = "Application Launch (Foreground)";
        static final String APPLICATION_CLOSE_EVENT = "Application Close (Background)";

        private EventName() {}
    }

    static final class EventDataKeys {

        static final String XDM = "xdm";
        static final String DATA = "data";

        private EventDataKeys() {}
    }

    /** Module Data Store keys */
    static class DataStoreKeys {

        static final String LAST_APP_VERSION = "v2LastAppVersion";
        static final String APP_START_TIMESTAMP_MILLIS = "v2AppStartTimestampMillis";
        static final String APP_PAUSE_TIMESTAMP_MILLIS = "v2AppPauseTimestampMillis";
        static final String APP_CLOSE_TIMESTAMP_MILLIS = "v2AppCloseTimestampMillis";

        // Deprecated, use timestamps in milliseconds. Kept here for migration workflows.
        static final String APP_START_TIMESTAMP_SEC = "v2AppStartTimestamp";
        static final String APP_PAUSE_TIMESTAMP_SEC = "v2AppPauseTimestamp";
        static final String APP_CLOSE_TIMESTAMP_SEC = "v2AppCloseTimestamp";

        private DataStoreKeys() {}
    }

    private LifecycleV2Constants() {}
}
