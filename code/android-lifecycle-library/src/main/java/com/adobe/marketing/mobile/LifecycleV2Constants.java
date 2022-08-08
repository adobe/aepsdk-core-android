/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2021 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 * *************************************************************************/

package com.adobe.marketing.mobile;

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
		static final String APPLICATION_CLOSE_EVENT  = "Application Close (Background)";

		private EventName() {}
	}

	static final class EventDataKeys {
		static final String XDM                     = "xdm";
		static final String DATA                    = "data";

		private EventDataKeys() {}
	}
	/**
	 * Module Data Store keys
	 */
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
