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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.adobe.marketing.mobile.identity.IdentityExtension;
import com.adobe.marketing.mobile.internal.util.VisitorIDSerializer;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Identity {

    private static final String CLASS_NAME = "Identity";
    private static final String EXTENSION_VERSION = "2.0.1";
    private static final String REQUEST_IDENTITY_EVENT_NAME = "IdentityRequestIdentity";
    private static final int PUBLIC_API_TIME_OUT_MILLISECOND = 500; // ms
    private static final String LOG_TAG = "Identity";

    public static final Class<? extends Extension> EXTENSION = IdentityExtension.class;

    private Identity() {}

    /**
     * Returns the version of the {@link Identity} extension
     *
     * @return The version as {@code String}
     */
    public static String extensionVersion() {
        return EXTENSION_VERSION;
    }

    /**
     * Registers the extension with the Mobile SDK. This method should be called only once in your
     * application class.
     *
     * @deprecated as of 2.0.0, use {@link MobileCore#registerExtensions(List, AdobeCallback)} with
     *     {@link Identity#EXTENSION} instead.
     */
    @Deprecated
    public static void registerExtension() {
        MobileCore.registerExtension(
                IdentityExtension.class,
                errorCode -> {
                    if (errorCode == null) {
                        return;
                    }
                    Log.error(
                            LOG_TAG,
                            CLASS_NAME,
                            "There was an error when registering the UserProfile extension: %s",
                            errorCode.getErrorName());
                });
    }

    /**
     * Updates the given customer IDs with the Adobe Experience Cloud ID Service.
     *
     * <p>Synchronizes the provided customer identifiers to the Adobe Experience Cloud ID Service.
     * If a customer ID type matches an existing ID type and identifier, then it is updated with the
     * new ID authentication state. New customer IDs are added. All given customer IDs are given the
     * default authentication state of {@link
     * com.adobe.marketing.mobile.VisitorID.AuthenticationState#UNKNOWN}.
     *
     * <p>These IDs are preserved between app upgrades, are saved and restored during the standard
     * application backup process, and are removed at uninstall.
     *
     * <p>If the current SDK privacy status is {@link MobilePrivacyStatus#OPT_OUT}, then calling
     * this method results with no operations being performed.
     *
     * @param identifiers {@code Map<String, String>} containing identifier type as the key and
     *     identifier as the value. Both identifier type and identifier should be non empty and non
     *     null values, otherwise they will be ignored
     */
    public static void syncIdentifiers(@NonNull final Map<String, String> identifiers) {
        syncIdentifiers(identifiers, VisitorID.AuthenticationState.UNKNOWN);
    }

    /**
     * Updates the given customer IDs with the Adobe Experience Cloud ID Service.
     *
     * <p>Synchronizes the provided customer identifiers to the Adobe Experience Cloud ID Service.
     * If a customer ID type matches an existing ID type and identifiers, then it is updated with
     * the new ID authentication state. New customer IDs are added.
     *
     * <p>These IDs are preserved between app upgrades, are saved and restored during the standard
     * application backup process, and are removed at uninstall.
     *
     * <p>If the current SDK privacy status is {@link MobilePrivacyStatus#OPT_OUT}, then calling
     * this method results with no operations being performed.
     *
     * @param identifiers {@code Map<String, String>} containing identifier type as the key and
     *     identifier as the value. Both identifier type and identifier should be non empty and non
     *     null values, otherwise they will be ignored
     * @param authenticationState {@code VisitorIDAuthenticationState} value indicating
     *     authentication state for the user
     */
    public static void syncIdentifiers(
            @NonNull final Map<String, String> identifiers,
            @NonNull final VisitorID.AuthenticationState authenticationState) {
        if (identifiers == null || identifiers.isEmpty()) {
            Log.warning(
                    LOG_TAG,
                    CLASS_NAME,
                    "syncIdentifiers(ids, state) : Unable to sync Visitor identifiers, provided"
                            + " map was null or empty");
            return;
        }

        Log.trace(
                LOG_TAG,
                CLASS_NAME,
                "syncIdentifiers(ids, state) : Processing a request to sync Visitor identifiers.");
        Map<String, Object> syncMap = new HashMap<>();
        syncMap.put(IdentityEventDataKeys.IDENTIFIERS, identifiers);
        syncMap.put(IdentityEventDataKeys.AUTHENTICATION_STATE, authenticationState.getValue());
        syncMap.put(IdentityEventDataKeys.FORCE_SYNC, false);
        syncMap.put(IdentityEventDataKeys.IS_SYNC_EVENT, true);

        Event event =
                new Event.Builder(
                                REQUEST_IDENTITY_EVENT_NAME,
                                EventType.IDENTITY,
                                EventSource.REQUEST_IDENTITY)
                        .setEventData(syncMap)
                        .build();
        MobileCore.dispatchEvent(event);
        Log.trace(
                LOG_TAG,
                CLASS_NAME,
                "dispatchIDSyncEvent : Identity Sync event has been added to event hub : %s",
                event);
    }

    /**
     * Updates the given customer ID with the Adobe Experience Cloud ID Service.
     *
     * <p>Synchronizes the provided customer identifier type key and value with the given
     * authentication state to the Adobe Experience Cloud ID Service. If the given customer ID type
     * already exists in the service, then it is updated with the new ID and authentication state.
     * Otherwise a new customer ID is added.
     *
     * <p>This ID is preserved between app upgrades, is saved and restored during the standard
     * application backup process, and is removed at uninstall.
     *
     * <p>If the current SDK privacy status is {@link MobilePrivacyStatus#OPT_OUT}, then calling
     * this method results with no operations being performed.
     *
     * @param identifierType {@code String} containing identifier type; should not be null or empty
     * @param identifier {@code String} containing identifier value; should not be null or empty
     * @param authenticationState {@code VisitorIDAuthenticationState} value indicating
     *     authentication state for the user
     */
    public static void syncIdentifier(
            @NonNull final String identifierType,
            @Nullable final String identifier,
            @NonNull final VisitorID.AuthenticationState authenticationState) {
        if (StringUtils.isNullOrEmpty(identifierType)) {
            Log.warning(
                    LOG_TAG,
                    CLASS_NAME,
                    "syncIdentifier : Unable to sync Visitor identifier due to null or empty"
                            + " identifierType");
            return;
        }

        Log.trace(
                LOG_TAG,
                CLASS_NAME,
                "syncIdentifier : Processing a request to sync Visitor identifier.");

        HashMap<String, String> identifiers = new HashMap<>();
        identifiers.put(identifierType, identifier);
        syncIdentifiers(identifiers, authenticationState);
    }

    /**
     * Appends Adobe visitor data to a URL string.
     *
     * <p>If the provided URL is null or empty, it is returned as is. Otherwise, the following
     * information is added to the {@link String} returned in the {@link AdobeCallback}:
     *
     * <ul>
     *   <li>The {@code adobe_mc} attribute is an URL encoded list containing:
     *       <ul>
     *         <li>Experience Cloud ID (ECID)
     *         <li>Experience Cloud Org ID
     *         <li>Analytics Tracking ID, if available from Analytics
     *         <li>A timestamp taken when this request was made
     *       </ul>
     *   <li>The optional {@code adobe_aa_vid} attribute is the URL encoded Analytics Custom Visitor
     *       ID, if available from Analytics.
     * </ul>
     *
     * @param baseURL {@code String} URL to which the visitor info needs to be appended
     * @param callback {@code AdobeCallback} invoked with the updated URL {@code String}; when an
     *     {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can be returned in the
     *     eventuality of an unexpected error or if the default timeout (500ms) is met before the
     *     Identity URL variables are retrieved
     */
    public static void appendVisitorInfoForURL(
            @NonNull final String baseURL, @NonNull final AdobeCallback<String> callback) {
        if (callback == null) {
            Log.warning(
                    LOG_TAG, CLASS_NAME, "appendVisitorInfoForURL : callback shouldn't be null.");
            return;
        }
        Log.trace(
                LOG_TAG,
                CLASS_NAME,
                "appendVisitorInfoForURL : Processing a request to append Adobe visitor data to a"
                        + " URL string.");
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(IdentityEventDataKeys.BASE_URL, baseURL);
        createIdentityRequestWithCallbacks(
                eventData,
                callback,
                event -> {
                    String url =
                            DataReader.optString(
                                    event.getEventData(), IdentityEventDataKeys.UPDATED_URL, "");
                    callback.call(url);
                });
    }

    /**
     * Gets Visitor ID Service variables in URL query parameter form for consumption in hybrid app.
     *
     * <p>This method will return an appropriately formed {@link String} containing Visitor ID
     * Service URL variables. There will be no leading {@literal &} or {@literal ?} punctuation, as
     * the caller is responsible for placing it in their resulting {@link java.net.URI} in the
     * correct location.
     *
     * <p>If an error occurs while retrieving the URL string, {@code callback} will be called with
     * null. Otherwise, the following information is added to the {@link String} returned in the
     * {@link AdobeCallback}:
     *
     * <ul>
     *   <li>The {@code adobe_mc} attribute is an URL encoded list containing:
     *       <ul>
     *         <li>Experience Cloud ID (ECID)
     *         <li>Experience Cloud Org ID
     *         <li>Analytics Tracking ID, if available from Analytics
     *         <li>A timestamp taken when this request was made
     *       </ul>
     *   <li>The optional {@code adobe_aa_vid} attribute is the URL encoded Analytics Custom Visitor
     *       ID, if available from Analytics.
     * </ul>
     *
     * @param callback {@link AdobeCallback} which will be called containing Visitor ID Service URL
     *     parameters; when an {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can
     *     be returned in the eventuality of an unexpected error or if the default timeout (500ms)
     *     is met before the Identity URL variables are retrieved
     */
    public static void getUrlVariables(@NonNull final AdobeCallback<String> callback) {
        if (callback == null) {
            Log.warning(LOG_TAG, CLASS_NAME, "getUrlVariables : callback shouldn't be null.");
            return;
        }
        Log.trace(
                LOG_TAG,
                CLASS_NAME,
                "getUrlVariables : Processing the request to get Visitor information as URL query"
                        + " parameters.");
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(IdentityEventDataKeys.URL_VARIABLES, true);
        createIdentityRequestWithCallbacks(
                eventData,
                callback,
                event -> {
                    String url =
                            DataReader.optString(
                                    event.getEventData(), IdentityEventDataKeys.URL_VARIABLES, "");
                    callback.call(url);
                });
    }

    /**
     * Returns all customer identifiers which were previously synced with the Adobe Experience
     * Cloud.
     *
     * @param callback {@link AdobeCallback} invoked with the list of {@link VisitorID} objects;
     *     when an {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can be returned
     *     in the eventuality of an unexpected error or if the default timeout (500ms) is met before
     *     the customer identifiers are retrieved
     */
    public static void getIdentifiers(@NonNull final AdobeCallback<List<VisitorID>> callback) {
        if (callback == null) {
            Log.warning(LOG_TAG, CLASS_NAME, "getIdentifiers : callback shouldn't be null.");
            return;
        }
        Log.trace(
                LOG_TAG,
                CLASS_NAME,
                "getIdentifiers : Processing a request to get all customer identifiers.");
        createIdentityRequestWithCallbacks(
                null,
                callback,
                event -> {
                    List<Map> data =
                            DataReader.optTypedList(
                                    Map.class,
                                    event.getEventData(),
                                    IdentityEventDataKeys.VISITOR_IDS_LIST,
                                    new ArrayList<>());
                    List<VisitorID> list = VisitorIDSerializer.convertToVisitorIds(data);
                    callback.call(list);
                });
    }

    /**
     * Retrieves the Adobe Experience Cloud Visitor ID from the Adobe Experience Cloud ID Service.
     *
     * <p>The Adobe Experience Cloud ID (ECID) is generated at initial launch and is stored and used
     * from that point forward. This ID is preserved between app upgrades, is saved and restored
     * during the standard application backup process, and is removed at uninstall.
     *
     * @param callback {@link AdobeCallback} invoked with the ECID {@code String}; when an {@link
     *     AdobeCallbackWithError} is provided, an {@link AdobeError} can be returned in the
     *     eventuality of an unexpected error or if the default timeout (500ms) is met before the
     *     ECID is retrieved
     */
    public static void getExperienceCloudId(@NonNull final AdobeCallback<String> callback) {
        if (callback == null) {
            Log.warning(LOG_TAG, CLASS_NAME, "getIdentifiers : callback shouldn't be null.");
            return;
        }

        Log.trace(
                LOG_TAG, CLASS_NAME, "getExperienceCloudId : Processing the request to get ECID.");
        createIdentityRequestWithCallbacks(
                null,
                callback,
                event -> {
                    String mid =
                            DataReader.optString(
                                    event.getEventData(), IdentityEventDataKeys.VISITOR_ID_MID, "");
                    callback.call(mid);
                });
    }

    private static <T> void createIdentityRequestWithCallbacks(
            final Map<String, Object> eventData,
            final AdobeCallback<T> errorCallback,
            @NonNull final AdobeCallback<Event> callback) {
        Event event;
        // do not want to set event data to null.
        if (eventData == null) {
            event =
                    new Event.Builder(
                                    REQUEST_IDENTITY_EVENT_NAME,
                                    EventType.IDENTITY,
                                    EventSource.REQUEST_IDENTITY)
                            .build();
        } else {
            event =
                    new Event.Builder(
                                    REQUEST_IDENTITY_EVENT_NAME,
                                    EventType.IDENTITY,
                                    EventSource.REQUEST_IDENTITY)
                            .setEventData(eventData)
                            .build();
        }

        MobileCore.dispatchEventWithResponseCallback(
                event,
                PUBLIC_API_TIME_OUT_MILLISECOND,
                new AdobeCallbackWithError<Event>() {
                    @Override
                    public void fail(final AdobeError error) {
                        if (errorCallback instanceof AdobeCallbackWithError) {
                            AdobeCallbackWithError adobeCallbackWithError =
                                    (AdobeCallbackWithError) errorCallback;
                            adobeCallbackWithError.fail(error);
                        }
                    }

                    @Override
                    public void call(final Event e) {
                        callback.call(e);
                    }
                });
        Log.trace(
                LOG_TAG,
                CLASS_NAME,
                "createIdentityRequestWithOneTimeCallbackWithCallbackParam : Identity request"
                        + " event has been added to the event hub : %s",
                event);
    }

    private static final class IdentityEventDataKeys {

        // Event Data key for fetching marketing cloud id from the IdentityExtension Response Event.
        public static final String VISITOR_ID_MID = "mid";

        // Event Data key for reading a list of maps, with each map representing a visitor id,from
        // Response IdentityExtension event dispatched by the module.
        public static final String VISITOR_IDS_LIST = "visitoridslist";

        // Event Data key for reading the updated url in the event received by the one time event
        // listener as a response to setting BASE_URL in Requent IdentityExtension event.
        public static final String UPDATED_URL = "updatedurl";

        // Event Data key for url variable string when creating a Request or receiving a Response
        // for getUrlVariables()
        public static final String URL_VARIABLES = "urlvariables";

        // Event Data key for base URL for appending visitor data to, when creating Request
        // IdentityExtension event for appendToURL()
        public static final String BASE_URL = "baseurl";

        // Event Data key for forcing syncing of identifiers, when creating Request
        // IdentityExtension event for syncIdenfiers()
        public static final String FORCE_SYNC = "forcesync";

        // Event Data key for setting <String,String> map of identifiers, when creating Request
        // IdentityExtension event for syncIdenfiers()
        public static final String IDENTIFIERS = "visitoridentifiers";

        /*
         * Event Data key for marking an event of sync type when creating Request IdentityExtension event .
         * Setting this value to true will result in a sync identifiers network call.
         * */
        public static final String IS_SYNC_EVENT = "issyncevent";

        /*
         * Event Data key for setting visitor id authentication value in Request Identity event for syncIdentifiers.
         * Also, Event Data key for reading visitor id authentication value from Response IdentityExtension event dispatched by the module.
         * */
        public static final String AUTHENTICATION_STATE = "authenticationstate";

        private IdentityEventDataKeys() {}
    }
}
