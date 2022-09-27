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

import com.adobe.marketing.mobile.identity.IdentityConstants;
import com.adobe.marketing.mobile.identity.IdentityExtension;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Identity {
    private final static String CLASS_NAME = "Identity";
    private final static String EXTENSION_VERSION = "2.0.0";
    private static final String REQUEST_IDENTITY_EVENT_NAME = "IdentityRequestIdentity";
    private static final int PUBLIC_API_TIME_OUT_MILLISECOND = 500; //ms

    private Identity() {

    }

    public static String extensionVersion() {
        return EXTENSION_VERSION;
    }

    /**
     * Registers the Identity extension with the {@code MobileCore}
     *
     * <p>
     * <p>
     * This will allow the extension to send and receive events to and from the SDK.
     */
    @Deprecated
    public static void registerExtension() {
        MobileCore.registerExtension(IdentityExtension.class, errorCode -> {
            if (errorCode == null) {
                return;
            }
            Log.error(IdentityConstants.LOG_TAG, CLASS_NAME, "There was an error when registering the UserProfile extension: %s",
                    errorCode.getErrorName());
        });
    }

    // =======================================================================
    // Identity Methods
    // =======================================================================

    /**
     * Updates the given customer IDs with the Adobe Experience Cloud ID Service.
     * <p>
     * Synchronizes the provided customer identifiers to the Adobe Experience Cloud ID Service.
     * If a customer ID type matches an existing ID type and identifier, then it is updated with the new ID
     * authentication state. New customer IDs are added. All given customer IDs are given the default
     * authentication state of {@link com.adobe.marketing.mobile.VisitorID.AuthenticationState#UNKNOWN}.
     * <p>
     * These IDs are preserved between app upgrades, are saved and restored during the standard application backup process,
     * and are removed at uninstall.
     * <p>
     * If the current SDK privacy status is {@link MobilePrivacyStatus#OPT_OUT}, then calling this method results
     * with no operations being performed.
     *
     * @param identifiers {@code Map<String, String>} containing identifier type as the key and identifier as the value.
     *                    Both identifier type and identifier should be non empty and non null values,
     *                    otherwise they will be ignored
     */
    public static void syncIdentifiers(@NonNull final Map<String, String> identifiers) {
        syncIdentifiers(identifiers, VisitorID.AuthenticationState.UNKNOWN);
    }

    /**
     * Updates the given customer IDs with the Adobe Experience Cloud ID Service.
     * <p>
     * Synchronizes the provided customer identifiers to the Adobe Experience Cloud ID Service.
     * If a customer ID type matches an existing ID type and identifiers, then it is updated with the new ID
     * authentication state. New customer IDs are added.
     * <p>
     * These IDs are preserved between app upgrades, are saved and restored during the standard application backup process,
     * and are removed at uninstall.
     * <p>
     * If the current SDK privacy status is {@link MobilePrivacyStatus#OPT_OUT}, then calling this method results
     * with no operations being performed.
     *
     * @param identifiers         {@code Map<String, String>} containing identifier type as the key and identifier as the value.
     *                            Both identifier type and identifier should be non empty and non null values,
     *                            otherwise they will be ignored
     * @param authenticationState {@code VisitorIDAuthenticationState} value indicating authentication state for the user
     */
    public static void syncIdentifiers(@NonNull final Map<String, String> identifiers,
                                       @NonNull final VisitorID.AuthenticationState authenticationState) {

        if (identifiers.isEmpty()) {
            Log.warning(IdentityConstants.LOG_TAG, CLASS_NAME, "syncIdentifiers(ids, state) : Unable to sync Visitor identifiers, provided map was null or empty");
            return;
        }

        Log.trace(IdentityConstants.LOG_TAG, CLASS_NAME, "syncIdentifiers(ids, state) : Processing a request to sync Visitor identifiers.");
        Map<String, Object> syncMap = new HashMap<>();
        syncMap.put(IdentityConstants.EventDataKeys.Identity.IDENTIFIERS, identifiers);
        syncMap.put(IdentityConstants.EventDataKeys.Identity.AUTHENTICATION_STATE, authenticationState.getValue());
        syncMap.put(IdentityConstants.EventDataKeys.Identity.FORCE_SYNC, false);
        syncMap.put(IdentityConstants.EventDataKeys.Identity.IS_SYNC_EVENT, true);

        Event event = new Event.Builder(REQUEST_IDENTITY_EVENT_NAME,
                EventType.IDENTITY,
                EventSource.REQUEST_IDENTITY)
                .setEventData(syncMap)
                .build();
        MobileCore.dispatchEvent(event);
        Log.trace(IdentityConstants.LOG_TAG, CLASS_NAME, "dispatchIDSyncEvent : Identity Sync event has been added to event hub : %s", event);
    }

    /**
     * Updates the given customer ID with the Adobe Experience Cloud ID Service.
     * <p>
     * Synchronizes the provided customer identifier type key and value with the given
     * authentication state to the Adobe Experience Cloud ID Service.
     * If the given customer ID type already exists in the service, then
     * it is updated with the new ID and authentication state. Otherwise a new customer ID is added.
     * <p>
     * This ID is preserved between app upgrades, is saved and restored during the standard application backup process,
     * and is removed at uninstall.
     * <p>
     * If the current SDK privacy status is {@link MobilePrivacyStatus#OPT_OUT}, then calling this method results
     * with no operations being performed.
     *
     * @param identifierType      {@code String} containing identifier type; should not be null or empty
     * @param identifier          {@code String} containing identifier value; should not be null or empty
     * @param authenticationState {@code VisitorIDAuthenticationState} value indicating authentication state for the user
     */
    public static void syncIdentifier(@NonNull final String identifierType,
                                      @Nullable final String identifier,
                                      @NonNull final VisitorID.AuthenticationState authenticationState) {

        if (StringUtils.isNullOrEmpty(identifierType)) {
            Log.warning(IdentityConstants.LOG_TAG, CLASS_NAME, "syncIdentifier : Unable to sync Visitor identifier due to null or empty identifierType");
            return;
        }

        Log.trace(IdentityConstants.LOG_TAG, CLASS_NAME, "syncIdentifier : Processing a request to sync Visitor identifier.");

        HashMap<String, String> identifiers = new HashMap<>();
        identifiers.put(identifierType, identifier);
        syncIdentifiers(identifiers, authenticationState);
    }

    /**
     * Appends Adobe visitor data to a URL string.
     * <p>
     * If the provided URL is null or empty, it is returned as is. Otherwise, the following information is added to the
     * {@link String} returned in the {@link AdobeCallback}:
     * <ul>
     *     <li>The {@code adobe_mc} attribute is an URL encoded list containing:
     *         <ul>
     *             <li>Experience Cloud ID (ECID)</li>
     *             <li>Experience Cloud Org ID</li>
     *             <li>Analytics Tracking ID, if available from Analytics</li>
     *             <li>A timestamp taken when this request was made</li>
     *         </ul>
     *     </li>
     *     <li>The optional {@code adobe_aa_vid} attribute is the URL encoded Analytics Custom Visitor ID, if available from Analytics.</li>
     * </ul>
     *
     * @param baseURL  {@code String} URL to which the visitor info needs to be appended
     * @param callback {@code AdobeCallback} invoked with the updated URL {@code String};
     *                 when an {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can be returned in the
     *                 eventuality of an unexpected error or if the default timeout (500ms) is met before the Identity URL variables are retrieved
     */
    public static void appendVisitorInfoForURL(final String baseURL, final AdobeCallback<String> callback) {
        Log.trace(IdentityConstants.LOG_TAG, CLASS_NAME, "appendVisitorInfoForURL : Processing a request to append Adobe visitor data to a URL string.");
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(IdentityConstants.EventDataKeys.Identity.BASE_URL, baseURL);
        createIdentityRequestWithCallbacks(
                eventData,
                callback,
                event -> {
                    String url = DataReader.optString(event.getEventData(), IdentityConstants.EventDataKeys.Identity.UPDATED_URL, "");
                    callback.call(url);

                });
    }


    /**
     * Gets Visitor ID Service variables in URL query parameter form for consumption in hybrid app.
     * <p>
     * This method will return an appropriately formed {@link String} containing Visitor ID Service URL variables.
     * There will be no leading {@literal &} or {@literal ?} punctuation, as the caller is responsible for placing it in their resulting
     * {@link java.net.URI} in the correct location.
     * <p>
     * If an error occurs while retrieving the URL string, {@code callback} will be called with null.
     * Otherwise, the following information is added to the
     * {@link String} returned in the {@link AdobeCallback}:
     * <ul>
     *     <li>The {@code adobe_mc} attribute is an URL encoded list containing:
     *         <ul>
     *             <li>Experience Cloud ID (ECID)</li>
     *             <li>Experience Cloud Org ID</li>
     *             <li>Analytics Tracking ID, if available from Analytics</li>
     *             <li>A timestamp taken when this request was made</li>
     *         </ul>
     *     </li>
     *     <li>The optional {@code adobe_aa_vid} attribute is the URL encoded Analytics Custom Visitor ID, if available from Analytics.</li>
     * </ul>
     *
     * @param callback {@link AdobeCallback} which will be called containing Visitor ID Service URL parameters;
     *                 when an {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can be returned in the
     *                 eventuality of an unexpected error or if the default timeout (500ms) is met before the Identity URL variables are retrieved
     */
    public static void getUrlVariables(final AdobeCallback<String> callback) {
        Log.trace(IdentityConstants.LOG_TAG, CLASS_NAME, "getUrlVariables : Processing the request to get Visitor information as URL query parameters.");
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(IdentityConstants.EventDataKeys.Identity.URL_VARIABLES, true);
        createIdentityRequestWithCallbacks(
                eventData,
                callback,
                event -> {
                    String url = DataReader.optString(event.getEventData(), IdentityConstants.EventDataKeys.Identity.URL_VARIABLES, "");
                    callback.call(url);
                });
    }

    /**
     * Returns all customer identifiers which were previously synced with the Adobe Experience Cloud.
     *
     * @param callback {@link AdobeCallback} invoked with the list of {@link VisitorID} objects;
     *                 when an {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can be returned in the
     *                 eventuality of an unexpected error or if the default timeout (500ms) is met before the customer identifiers are retrieved
     */
    public static void getIdentifiers(final AdobeCallback<List<VisitorID>> callback) {
        Log.trace(IdentityConstants.LOG_TAG, CLASS_NAME, "getIdentifiers : Processing a request to get all customer identifiers.");
        createIdentityRequestWithCallbacks(
                null,
                callback,
                event -> {
                    List<VisitorID> list = DataReader.optTypedList(
                            VisitorID.class,
                            event.getEventData(),
                            IdentityConstants.EventDataKeys.Identity.VISITOR_IDS_LIST,
                            new ArrayList<>());
                    callback.call(list);
                });
    }

    /**
     * Retrieves the Adobe Experience Cloud Visitor ID from the Adobe Experience Cloud ID Service.
     * <p>
     * The Adobe Experience Cloud ID (ECID) is generated at initial launch and is stored and used from that point forward.
     * This ID is preserved between app upgrades, is saved and restored during the standard application backup process,
     * and is removed at uninstall.
     *
     * @param callback {@link AdobeCallback} invoked with the ECID {@code String};
     *                 when an {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can be returned in the
     *                 eventuality of an unexpected error or if the default timeout (500ms) is met before the ECID is retrieved
     */
    public static void getExperienceCloudId(final AdobeCallback<String> callback) {

        Log.trace(IdentityConstants.LOG_TAG, CLASS_NAME, "getExperienceCloudId : Processing the request to get ECID.");
        createIdentityRequestWithCallbacks(
                null,
                callback,
                event -> {
                    String mid = DataReader.optString(event.getEventData(), IdentityConstants.EventDataKeys.Identity.VISITOR_ID_MID, "");
                    callback.call(mid);
                });
    }

    private static <T> void createIdentityRequestWithCallbacks(
            final Map<String, Object> eventData,
            final AdobeCallback<T> errorCallback,
            final AdobeCallback<Event> callback) {
        if (callback == null) {
            return;
        }

        Event event;

        // do not want to set event data to null.
        if (eventData == null) {
            event = new Event.Builder(REQUEST_IDENTITY_EVENT_NAME, EventType.IDENTITY,
                    EventSource.REQUEST_IDENTITY).build();
        } else {
            event = new Event.Builder(REQUEST_IDENTITY_EVENT_NAME, EventType.IDENTITY,
                    EventSource.REQUEST_IDENTITY).setEventData(eventData).build();
        }

        MobileCore.dispatchEventWithResponseCallback(event, PUBLIC_API_TIME_OUT_MILLISECOND, new AdobeCallbackWithError<Event>() {
            @Override
            public void fail(AdobeError error) {
                if (errorCallback instanceof AdobeCallbackWithError) {
                    AdobeCallbackWithError adobeCallbackWithError = (AdobeCallbackWithError) errorCallback;
                    adobeCallbackWithError.fail(error);
                }

            }

            @Override
            public void call(Event e) {
                callback.call(e);
            }
        });
        Log.trace(IdentityConstants.LOG_TAG, CLASS_NAME,
                "createIdentityRequestWithOneTimeCallbackWithCallbackParam : Identity request event has been added to the event hub : %s",
                event);

    }


}
