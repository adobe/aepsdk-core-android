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

package com.adobe.marketing.mobile.identity;

import com.adobe.marketing.mobile.services.DataEntity;
import com.adobe.marketing.mobile.services.HitProcessing;
import com.adobe.marketing.mobile.services.HttpConnecting;
import com.adobe.marketing.mobile.services.HttpMethod;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.NetworkCallback;
import com.adobe.marketing.mobile.services.NetworkRequest;
import com.adobe.marketing.mobile.services.ServiceProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class IdentityHitsProcessing implements HitProcessing {
    private static final String LOG_SOURCE = "IdentityHitsProcessing";
    private final IdentityExtension identityExtension;
    private final int RETRY_INTERVAL = 30; //seconds

    IdentityHitsProcessing(IdentityExtension identityExtension) {
        this.identityExtension = identityExtension;
    }


    @Override
    public int retryInterval(DataEntity entity) {
        return RETRY_INTERVAL;
    }

    @Override
    public boolean processHit(DataEntity entity) {
        IdentityHit hit = IdentityHit.fromDataEntity(entity);
        if (hit == null) {
            return true;
        }
        if (hit.getUrl() == null || hit.getEvent() == null) {
            Log.debug(IdentityConstants.LOG_TAG, LOG_SOURCE,
                    "IdentityHitsDatabase.process : Unable to process IdentityExtension hit because it does not contain a url or the trigger event.");
            return true;
        }

        Log.debug(IdentityConstants.LOG_TAG, LOG_SOURCE, "IdentityHitsDatabase.process : Sending request: (%s).", hit.getUrl());
        Map<String, String> requestPropertyMap = NetworkConnectionUtil.getHeaders(true);

        // make the request synchronously
        CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicBoolean networkRequestNeedRetry = new AtomicBoolean(false);
        NetworkRequest networkRequest = new NetworkRequest(
                hit.getUrl(),
                HttpMethod.GET,
                null,
                requestPropertyMap,
                IdentityConstants.Defaults.TIMEOUT,
                IdentityConstants.Defaults.TIMEOUT);
        ServiceProvider.getInstance().getNetworkService().connectAsync(networkRequest, connection -> {
            if (connection == null) {
                Log.debug(IdentityConstants.LOG_TAG, LOG_SOURCE,
                        "IdentityHitsDatabase.process : An unknown error occurred during the Identity network call, connection is null. Will not retry.");

                // make sure the parent updates shared state and notifies one-time listeners accordingly
                identityExtension.networkResponseLoaded(null, hit.getEvent());
                networkRequestNeedRetry.set(true);
                countDownLatch.countDown();
                return;
            }
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                try {
                    String networkInputStreamJSONString = NetworkConnectionUtil.readFromInputStream(connection.getInputStream());
                    JSONObject jsonObject = new JSONObject(networkInputStreamJSONString);

                    IdentityResponseObject result = createIdentityObjectFromResponseJsonObject(jsonObject);
                    Log.trace(IdentityConstants.LOG_TAG, LOG_SOURCE, "IdentityHitsDatabase.process : ECID Service response data was parsed successfully.");
                    identityExtension.networkResponseLoaded(result, hit.getEvent());
                    networkRequestNeedRetry.set(true);
                } catch (final IOException | JSONException e) {
                    Log.debug(IdentityConstants.LOG_TAG, LOG_SOURCE,
                            "IdentityHitsDatabase.process : An unknown exception occurred while trying to process the response from the ECID Service: (%s).",
                            e);
                    networkRequestNeedRetry.set(false);
                    countDownLatch.countDown();
                    return;
                }

            } else if (!NetworkConnectionUtil.recoverableNetworkErrorCodes.contains(connection.getResponseCode())) {

                // unrecoverable error. delete the hit from the database and continue
                Log.debug(IdentityConstants.LOG_TAG, LOG_SOURCE,
                        "IdentityHitsDatabase.process : Discarding ECID Service request because of an un-recoverable network error with response code %d occurred while processing it.",
                        connection.getResponseCode());
                // make sure the parent updates shared state and notifies one-time listeners accordingly
                identityExtension.networkResponseLoaded(null, hit.getEvent());
                networkRequestNeedRetry.set(false);
            } else {
                // recoverable error.  leave the request in the queue, wait for 30 sec, and try again
                Log.debug(IdentityConstants.LOG_TAG, LOG_SOURCE,
                        "IdentityHitsDatabase.process : A recoverable network error occurred with response code %d while processing ECID Service requests.  Will retry in 30 seconds.",
                        connection.getResponseCode());
                networkRequestNeedRetry.set(true);
            }
            countDownLatch.countDown();
        });
        try {
            //noinspection ResultOfMethodCallIgnored
            countDownLatch.await(IdentityConstants.Defaults.TIMEOUT + 1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }

        return networkRequestNeedRetry.get();
    }

    IdentityResponseObject createIdentityObjectFromResponseJsonObject(final JSONObject jsonObject) {
        IdentityResponseObject result = null;

        if (jsonObject == null) {
            Log.debug(IdentityConstants.LOG_TAG, LOG_SOURCE,
                    "createIdentityObjectFromResponseJsonObject: Unable to parse identity network response because the JSON object created was null.");
            return result;
        }

        result = new IdentityResponseObject();

        result.blob = jsonObject.optString(IdentityConstants.UrlKeys.BLOB, null);
        result.error = jsonObject.optString(IdentityConstants.UrlKeys.RESPONSE_ERROR, null);
        result.mid = jsonObject.optString(IdentityConstants.UrlKeys.MID, null);

        int hintValue = jsonObject.optInt(IdentityConstants.UrlKeys.HINT, -1);
        result.hint = hintValue == -1 ? null : Integer.toString(hintValue);
        result.ttl = jsonObject.optLong(IdentityConstants.UrlKeys.TTL, IdentityConstants.Defaults.DEFAULT_TTL_VALUE);

        JSONArray optOutJsonArray = jsonObject.optJSONArray(IdentityConstants.UrlKeys.OPT_OUT);

        if (optOutJsonArray != null) {
            List<String> optOutVector = new ArrayList<>();

            for (int i = 0; i < optOutJsonArray.length(); i++) {
                try {
                    optOutVector.add(optOutJsonArray.getString(i));
                } catch (JSONException e) {
                    Log.debug(IdentityConstants.LOG_TAG, LOG_SOURCE,
                            "createIdentityObjectFromResponseJsonObject : Unable to read opt-out JSON array due to an exception: (%s).", e);
                }
            }

            result.optOutList = optOutVector;
        }

        return result;
    }
}
