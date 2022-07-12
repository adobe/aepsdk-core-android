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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The database queue for signal requests
 */

// TODO refactor to use public HitQueue
/*class SignalHitsDatabase implements HitQueue.IHitProcessor<SignalHit> {

	private static final String LOG_TAG = "SignalHitsDatabase";
	private static final String SIGNAL_FILENAME = "ADBMobileSignalDataCache.sqlite";
	private static final String SIGNAL_TABLE_NAME = "HITS";
	private final NetworkService networkService;
	private final SystemInfoService systemInfoService;
	private final HitQueue<SignalHit, SignalHitSchema> hitQueue;
	private final static int HTTP_SUCCESS_RESPONSE_CODE_LOWER_LIMIT = 200;
	private final static int HTTP_SUCCESS_RESPONSE_CODE_UPPER_LIMIT = 299;

	*//**
	 * Default Constructor
	 * @param services PlatformServices
	 *//*
	SignalHitsDatabase(final PlatformServices services) {
		this(services, null);
	}

	*//**
	 * Constructor for test
	 * @param services PlatformServices
	 * @param hitQueue HitQueue for test
	 *//*
	SignalHitsDatabase(final PlatformServices services, final HitQueue<SignalHit, SignalHitSchema> hitQueue) {

		this.networkService = services.getNetworkService();
		this.systemInfoService = services.getSystemInfoService();

		if (hitQueue != null) { // for unit test
			this.hitQueue = hitQueue;
		} else {
			final File directory = systemInfoService != null ? systemInfoService.getApplicationCacheDir() : null;
			final File dbFilePath = new File(directory, SIGNAL_FILENAME);
			this.hitQueue = new HitQueue<SignalHit, SignalHitSchema>(services, dbFilePath,
					SIGNAL_TABLE_NAME, new SignalHitSchema(), this);
		}
	}

	@Override
	public HitQueue.RetryType process(final SignalHit hit) {
		HitQueue.RetryType retryType = HitQueue.RetryType.NO;

		try {
			byte[] outputBytes = null;

			if (hit.body != null) {
				outputBytes = hit.body.getBytes(StringUtils.CHARSET_UTF_8);
			}

			Map<String, String> headers = NetworkConnectionUtil.getHeaders(false, hit.contentType);
			NetworkService.HttpConnection connection = networkService.connectUrl(hit.url, hit.getHttpCommand(), outputBytes,
					headers, hit.timeout, hit.timeout);

			if (connection == null) {
				Log.warning(LOG_TAG, "Could not process a request because it was invalid. Discarding request");
				return retryType;
			}

			if ((connection.getResponseCode() >= HTTP_SUCCESS_RESPONSE_CODE_LOWER_LIMIT)
					&& (connection.getResponseCode() <= HTTP_SUCCESS_RESPONSE_CODE_UPPER_LIMIT)) {

				try {
					// have to read the stream in order to keep the SSL connection alive
					NetworkConnectionUtil.readFromInputStream(connection.getInputStream());
				} catch (IOException e) {
					//do nothing
				}

				Log.debug(LOG_TAG, "Request (%s)was sent", hit.url);
			} else if (!NetworkConnectionUtil.recoverableNetworkErrorCodes.contains(connection.getResponseCode())) {
				Log.debug(LOG_TAG, "Un-recoverable network error: (%s) while processing requests. Discarding request.",
						  connection.getResponseCode());
			} else {
				Log.debug(LOG_TAG, "Recoverable network error: (%s) while processing requests, will retry.",
						  connection.getResponseCode());
				retryType = HitQueue.RetryType.YES;
			}

			connection.close();

		} catch (UnsupportedEncodingException e) {
			Log.debug(LOG_TAG, "Unable to encode the post body (%s) for the signal request, %s", hit.body, e);
		}

		return retryType;
	}

	*//**
	 * update the privacy status. Resume the queue when optin. Clear the queue when optout. Suspend the queue when unkonwn.
	 *
	 * @param privacyStatus the new privacy status
	 *//*
	void updatePrivacyStatus(final MobilePrivacyStatus privacyStatus) {
		switch (privacyStatus) {
			case OPT_IN:
				this.hitQueue.bringOnline();
				break;

			case OPT_OUT:
				this.hitQueue.suspend();
				this.hitQueue.deleteAllHits();
				break;

			case UNKNOWN:
				this.hitQueue.suspend();
				break;
		}
	}

	*//**
	 * Add a signal hit to the queue
	 * @param signalHit the signal hit
	 * @param timestampMillis event timestamp to be associated with the signal hit
	 * @param privacyStatus the current privacy status
	 *//*
	void queue(final SignalHit signalHit, final long timestampMillis, final MobilePrivacyStatus privacyStatus) {
		if (signalHit != null) {
			signalHit.timestamp = TimeUnit.MILLISECONDS.toSeconds(timestampMillis);
		}

		this.hitQueue.queue(signalHit);

		if (privacyStatus == MobilePrivacyStatus.OPT_IN) {
			this.hitQueue.bringOnline();
		}
	}

}*/
