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

import androidx.annotation.NonNull;
import com.adobe.marketing.mobile.services.DataEntity;
import com.adobe.marketing.mobile.services.HitProcessing;
import com.adobe.marketing.mobile.services.HitProcessingResult;
import com.adobe.marketing.mobile.services.HttpMethod;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.NetworkRequest;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.util.StreamUtils;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class IdentityHitsProcessing implements HitProcessing {

    private static final String LOG_SOURCE = "IdentityHitsProcessing";
    private final IdentityExtension identityExtension;
    private final int RETRY_INTERVAL = 30; // seconds

    IdentityHitsProcessing(final IdentityExtension identityExtension) {
        this.identityExtension = identityExtension;
    }

    @Override
    public int retryInterval(final DataEntity entity) {
        return RETRY_INTERVAL;
    }

    public void processHit(
            @NonNull final DataEntity entity,
            @NonNull final int networkTimeoutInSeconds,
            @NonNull final HitProcessingResult processingResult) {
        IdentityHit hit = IdentityHit.fromDataEntity(entity);
        if (hit == null) {
            processingResult.complete(true);
            return;
        }
        if (hit.getUrl() == null || hit.getEvent() == null) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "IdentityHitsDatabase.process : Unable to process IdentityExtension hit"
                            + " because it does not contain a url or the trigger event.");
            processingResult.complete(true);
            return;
        }

        Log.debug(
                IdentityConstants.LOG_TAG,
                LOG_SOURCE,
                "IdentityHitsDatabase.process : Sending request: (%s).",
                hit.getUrl());
        Map<String, String> requestPropertyMap = NetworkConnectionUtil.getHeaders(true);

        // make the request synchronously
        NetworkRequest networkRequest =
                new NetworkRequest(
                        hit.getUrl(),
                        HttpMethod.GET,
                        null,
                        requestPropertyMap,
                        networkTimeoutInSeconds,
                        networkTimeoutInSeconds);
        ServiceProvider.getInstance()
                .getNetworkService()
                .connectAsync(
                        networkRequest,
                        connection -> {
                            if (connection == null) {
                                Log.debug(
                                        IdentityConstants.LOG_TAG,
                                        LOG_SOURCE,
                                        "IdentityHitsDatabase.process : An unknown error occurred"
                                                + " during the Identity network call, connection is"
                                                + " null. Will not retry.");

                                // make sure the parent updates shared state and notifies one-time
                                // listeners accordingly
                                identityExtension.networkResponseLoaded(null, hit.getEvent());
                                processingResult.complete(true);
                                return;
                            }
                            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                try {
                                    String networkInputStreamJSONString =
                                            StreamUtils.readAsString(connection.getInputStream());
                                    JSONObject jsonObject =
                                            new JSONObject(networkInputStreamJSONString);

                                    IdentityResponseObject result =
                                            createIdentityObjectFromResponseJsonObject(jsonObject);
                                    Log.trace(
                                            IdentityConstants.LOG_TAG,
                                            LOG_SOURCE,
                                            "IdentityHitsDatabase.process : ECID Service response"
                                                    + " data was parsed successfully.");
                                    identityExtension.networkResponseLoaded(result, hit.getEvent());
                                    processingResult.complete(true);
                                } catch (final JSONException e) {
                                    Log.debug(
                                            IdentityConstants.LOG_TAG,
                                            LOG_SOURCE,
                                            "IdentityHitsDatabase.process : An unknown exception"
                                                + " occurred while trying to process the response"
                                                + " from the ECID Service: (%s).",
                                            e);
                                    processingResult.complete(false);
                                }
                            } else if (!NetworkConnectionUtil.recoverableNetworkErrorCodes.contains(
                                    connection.getResponseCode())) {
                                // unrecoverable error. delete the hit from the database and
                                // continue
                                Log.debug(
                                        IdentityConstants.LOG_TAG,
                                        LOG_SOURCE,
                                        "IdentityHitsDatabase.process : Discarding ECID Service"
                                            + " request because of an un-recoverable network error"
                                            + " with response code %d occurred while processing"
                                            + " it.",
                                        connection.getResponseCode());
                                // make sure the parent updates shared state and notifies one-time
                                // listeners accordingly
                                identityExtension.networkResponseLoaded(null, hit.getEvent());
                                processingResult.complete(true);
                            } else {
                                // recoverable error.  leave the request in the queue, wait for 30
                                // sec, and try again
                                Log.debug(
                                        IdentityConstants.LOG_TAG,
                                        LOG_SOURCE,
                                        "IdentityHitsDatabase.process : A recoverable network error"
                                            + " occurred with response code %d while processing"
                                            + " ECID Service requests.  Will retry in 30 seconds.",
                                        connection.getResponseCode());
                                processingResult.complete(false);
                            }

                            connection.close();
                        });
    }

    @Override
    public void processHit(
            @NonNull final DataEntity entity, @NonNull final HitProcessingResult processingResult) {
        processHit(entity, IdentityConstants.Defaults.TIMEOUT_IN_SECONDS, processingResult);
    }

    IdentityResponseObject createIdentityObjectFromResponseJsonObject(final JSONObject jsonObject) {
        IdentityResponseObject result;

        if (jsonObject == null) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "createIdentityObjectFromResponseJsonObject: Unable to parse identity network"
                            + " response because the JSON object created was null.");
            return null;
        }

        result = new IdentityResponseObject();

        result.blob = jsonObject.optString(IdentityConstants.UrlKeys.BLOB, null);
        result.error = jsonObject.optString(IdentityConstants.UrlKeys.RESPONSE_ERROR, null);
        result.mid = jsonObject.optString(IdentityConstants.UrlKeys.MID, null);

        int hintValue = jsonObject.optInt(IdentityConstants.UrlKeys.HINT, -1);
        result.hint = hintValue == -1 ? null : Integer.toString(hintValue);
        result.ttl =
                jsonObject.optLong(
                        IdentityConstants.UrlKeys.TTL,
                        IdentityConstants.Defaults.DEFAULT_TTL_VALUE);

        JSONArray optOutJsonArray = jsonObject.optJSONArray(IdentityConstants.UrlKeys.OPT_OUT);

        if (optOutJsonArray != null) {
            List<String> optOutVector = new ArrayList<>();

            for (int i = 0; i < optOutJsonArray.length(); i++) {
                try {
                    optOutVector.add(optOutJsonArray.getString(i));
                } catch (JSONException e) {
                    Log.debug(
                            IdentityConstants.LOG_TAG,
                            LOG_SOURCE,
                            "createIdentityObjectFromResponseJsonObject : Unable to read opt-out"
                                    + " JSON array due to an exception: (%s).",
                            e);
                }
            }

            result.optOutList = optOutVector;
        }

        return result;
    }
}
