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

import java.util.concurrent.*;

class LifecycleConstants {

    /** General constants */
    static final String DATA_STORE_NAME = "AdobeMobile_Lifecycle";

    static final long MAX_SESSION_LENGTH_SECONDS = TimeUnit.DAYS.toSeconds(7);
    static final long WRONG_EPOCH_MAX_LENGTH_SECONDS =
            TimeUnit.DAYS.toSeconds(365 * 30); // 30 years
    static final int DEFAULT_LIFECYCLE_TIMEOUT = 300;
    static final String LOG_TAG = "Lifecycle";
    static final String FRIENDLY_NAME = "Lifecycle";

    static final class EventName {

        static final String LIFECYCLE_START_EVENT = "LifecycleStart";

        private EventName() {}
    }

    static class ContextDataValues {

        static final String UPGRADE_EVENT = "UpgradeEvent";
        static final String CRASH_EVENT = "CrashEvent";
        static final String LAUNCH_EVENT = "LaunchEvent";
        static final String INSTALL_EVENT = "InstallEvent";
        static final String DAILY_ENG_USER_EVENT = "DailyEngUserEvent";
        static final String MONTHLY_ENG_USER_EVENT = "MonthlyEngUserEvent";

        private ContextDataValues() {}
    }

    /** Module Data Store keys */
    static class DataStoreKeys {

        static final String LIFECYCLE_DATA = "LifecycleData";
        static final String START_DATE = "SessionStart";
        static final String INSTALL_DATE = "InstallDate";
        static final String UPGRADE_DATE = "UpgradeDate";
        static final String LAST_USED_DATE = "LastDateUsed";
        static final String LAUNCHES_AFTER_UPGRADE = "LaunchesAfterUpgrade";
        static final String LAUNCHES = "Launches";
        static final String LAST_VERSION = "LastVersion";
        static final String PAUSE_DATE = "PauseDate";
        static final String SUCCESSFUL_CLOSE = "SuccessfulClose";
        static final String OS_VERSION = "OsVersion";
        static final String APP_ID = "AppId";

        private DataStoreKeys() {}
    }

    private LifecycleConstants() {}

    static final class EventDataKeys {

        static final String STATE_OWNER = "stateowner";

        private EventDataKeys() {}

        static final class Configuration {

            static final String MODULE_NAME = "com.adobe.module.configuration";
            static final String LIFECYCLE_CONFIG_SESSION_TIMEOUT = "lifecycle.sessionTimeout";

            private Configuration() {}
        }

        static final class Lifecycle {

            static final String MODULE_NAME = "com.adobe.module.lifecycle";
            static final String ADDITIONAL_CONTEXT_DATA = "additionalcontextdata";
            static final String APP_ID = "appid";
            static final String CARRIER_NAME = "carriername";
            static final String CRASH_EVENT = "crashevent";
            static final String PREVIOUS_OS_VERSION = "previousosversion";
            static final String PREVIOUS_APP_ID = "previousappid";
            static final String DAILY_ENGAGED_EVENT = "dailyenguserevent";
            static final String DAY_OF_WEEK = "dayofweek";
            static final String DAYS_SINCE_FIRST_LAUNCH = "dayssincefirstuse";
            static final String DAYS_SINCE_LAST_LAUNCH = "dayssincelastuse";
            static final String DAYS_SINCE_LAST_UPGRADE = "dayssincelastupgrade";
            static final String DEVICE_NAME = "devicename";
            static final String DEVICE_RESOLUTION = "resolution";
            static final String HOUR_OF_DAY = "hourofday";
            static final String IGNORED_SESSION_LENGTH = "ignoredsessionlength";
            static final String INSTALL_DATE = "installdate";
            static final String INSTALL_EVENT = "installevent";
            static final String LAUNCH_EVENT = "launchevent";
            static final String LAUNCHES = "launches";
            static final String LAUNCHES_SINCE_UPGRADE = "launchessinceupgrade";
            static final String LIFECYCLE_ACTION_KEY = "action";
            static final String LIFECYCLE_CONTEXT_DATA = "lifecyclecontextdata";
            static final String LIFECYCLE_PAUSE = "pause";
            static final String LIFECYCLE_START = "start";
            static final String LOCALE = "locale";
            static final String MAX_SESSION_LENGTH = "maxsessionlength";
            static final String MONTHLY_ENGAGED_EVENT = "monthlyenguserevent";
            static final String OPERATING_SYSTEM = "osversion";
            static final String PREVIOUS_SESSION_LENGTH = "prevsessionlength";
            static final String PREVIOUS_SESSION_PAUSE_TIMESTAMP =
                    "previoussessionpausetimestampmillis";
            static final String PREVIOUS_SESSION_START_TIMESTAMP =
                    "previoussessionstarttimestampmillis";
            static final String RUN_MODE = "runmode";
            static final String SESSION_EVENT = "sessionevent";
            static final String SESSION_START_TIMESTAMP = "starttimestampmillis";
            static final String UPGRADE_EVENT = "upgradeevent";

            private Lifecycle() {}
        }

        static final class Identity {

            static final String MODULE_NAME = "com.adobe.module.identity";
            static final String ADVERTISING_IDENTIFIER = "advertisingidentifier";

            private Identity() {}
        }
    }
}
