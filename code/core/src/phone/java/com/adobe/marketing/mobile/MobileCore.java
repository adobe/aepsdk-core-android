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

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.adobe.marketing.mobile.internal.AppResourceStore;
import com.adobe.marketing.mobile.internal.CoreConstants;
import com.adobe.marketing.mobile.internal.DataMarshaller;
import com.adobe.marketing.mobile.internal.eventhub.EventHub;
import com.adobe.marketing.mobile.internal.eventhub.EventHubConstants;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.util.DataReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MobileCore {

    private static final String LOG_TAG = "MobileCore";
    private static final long API_TIMEOUT_MS = 5000;

    private MobileCore() {}

    // ========================================================
    // MobileCore methods
    // ========================================================

    /**
     * Initializes the AEP SDK with the specified {@link InitOptions}. This automatically registers
     * all bundled extensions and sets up lifecycle tracking. You can disable automatic lifecycle
     * tracking using {@link InitOptions}.
     *
     * @param application The Android {@link Application} instance. It should not be null.
     * @param initOptions The {@link InitOptions} to configure the SDK. It should not be null.
     * @param completionCallback An optional {@link AdobeCallback} triggered once initialization is
     *     complete.
     */
    public static void initialize(
            @NonNull final Application application,
            @NonNull final InitOptions initOptions,
            @Nullable final AdobeCallback<?> completionCallback) {
        if (application == null) {
            Log.error(CoreConstants.LOG_TAG, LOG_TAG, "initialize failed - application is null.");
            return;
        }

        if (initOptions == null) {
            Log.error(CoreConstants.LOG_TAG, LOG_TAG, "initialize failed - initOptions is null.");
            return;
        }

        MobileCoreInitializer.INSTANCE.initialize(application, initOptions, completionCallback);
    }

    /**
     * Convenience overload that initializes the AEP SDK with bundled extensions, automatic
     * lifecycle tracking, and configures the SDK using the specified application ID via {@link
     * MobileCore#configureWithAppID(String)}. An optional completion callback is invoked once
     * initialization is complete.
     *
     * @param application The Android {@link Application} instance. It should not be null.
     * @param appId A unique identifier assigned to the app instance by Adobe tags. It should not be
     *     null.
     * @param completionCallback An optional callback triggered once initialization is complete. Can
     *     be {@code null}.
     */
    public static void initialize(
            @NonNull final Application application,
            @NonNull final String appId,
            @Nullable final AdobeCallback<?> completionCallback) {
        if (appId == null) {
            Log.error(CoreConstants.LOG_TAG, LOG_TAG, "initialize failed - appId is null.");
            return;
        }

        InitOptions options = InitOptions.configureWithAppID(appId);
        initialize(application, options, completionCallback);
    }

    /**
     * Convenience overload that initializes the AEP SDK with bundled extensions, automatic
     * lifecycle tracking, and configures the SDK using the specified application ID via {@link
     * MobileCore#configureWithAppID(String)}.
     *
     * @param application The Android {@link Application} instance. It should not be null.
     * @param appId A unique identifier assigned to the app instance by Adobe tags. It should not be
     *     null.
     */
    public static void initialize(
            @NonNull final Application application, @NonNull final String appId) {
        if (appId == null) {
            Log.error(CoreConstants.LOG_TAG, LOG_TAG, "initialize failed - appId is null.");
            return;
        }

        InitOptions options = InitOptions.configureWithAppID(appId);
        initialize(application, options, null);
    }

    /**
     * Returns the version for the {@code MobileCore} extension
     *
     * @return The version string
     */
    @NonNull public static String extensionVersion() {
        WrapperType wrapperType = EventHub.Companion.getShared().getWrapperType();
        if (wrapperType == WrapperType.NONE) {
            return EventHubConstants.VERSION_NUMBER;
        } else {
            return EventHubConstants.VERSION_NUMBER + "-" + wrapperType.getWrapperTag();
        }
    }

    /**
     * Sets the SDK's current wrapper type. This API should only be used if being developed on
     * platforms such as React Native or Flutter
     *
     * <p>
     *
     * @param wrapperType the type of wrapper being used. It should not be null.
     */
    public static void setWrapperType(@NonNull final WrapperType wrapperType) {
        if (wrapperType == null) {
            Log.error(
                    CoreConstants.LOG_TAG, LOG_TAG, "setWrapperType failed - wrapperType is null.");
            return;
        }

        EventHub.Companion.getShared().setWrapperType(wrapperType);
    }

    /**
     * Set the Android {@link Application}, which enables the SDK get the app {@code Context},
     * register a {@link ActivityLifecycleCallbacks} to monitor the lifecycle of the app and get the
     * {@link Activity} on top of the screen.
     *
     * <p>NOTE: This method should be called right after the app starts, so it gives the SDK all the
     * contexts it needed.
     *
     * @param application the Android {@link Application} instance. It should not be null.
     */
    public static void setApplication(@NonNull final Application application) {

        if (application == null) {
            Log.error(
                    CoreConstants.LOG_TAG, LOG_TAG, "setApplication failed - application is null");
            return;
        }

        MobileCoreInitializer.INSTANCE.setApplication(application);
    }

    /**
     * Get the global {@link Application} object of the current process.
     *
     * <p>NOTE: {@link #setApplication(Application)} must be called before calling this method.
     *
     * @return the current {@code Application}, or null if no {@code Application} was set or the
     *     {@code Application} process was destroyed.
     */
    @Nullable public static Application getApplication() {
        return ServiceProvider.getInstance().getAppContextService().getApplication();
    }

    /**
     * Set the {@link LoggingMode} level for the Mobile SDK.
     *
     * @param mode the logging mode. It should not be null.
     */
    public static void setLogLevel(@NonNull final LoggingMode mode) {
        if (mode == null) {
            Log.error(CoreConstants.LOG_TAG, LOG_TAG, "setLogLevel failed - mode is null");
            return;
        }

        com.adobe.marketing.mobile.services.Log.setLogLevel(mode);
    }

    /**
     * Get the {@link LoggingMode} level for the Mobile SDK
     *
     * @return the set {@code LoggingMode}
     */
    @NonNull public static LoggingMode getLogLevel() {
        return com.adobe.marketing.mobile.services.Log.getLogLevel();
    }

    /**
     * Registers all extensions with Core and starts event processing.
     *
     * <p>This method needs to be called after {@link MobileCore#setApplication(Application)} is
     * called.
     *
     * @param extensions List of extension classes whose parent is {@link Extension}. It should not
     *     be null.
     * @param completionCallback an optional {@link AdobeCallback} invoked after registrations are
     *     completed
     */
    public static void registerExtensions(
            @NonNull final List<Class<? extends Extension>> extensions,
            @Nullable final AdobeCallback<?> completionCallback) {

        MobileCoreInitializer.INSTANCE.registerExtensions(extensions, completionCallback);
    }

    /**
     * Registers an event listener for the provided event type and source.
     *
     * @param eventType the event type as a valid string. It should not be null or empty.
     * @param eventSource the event source as a valid string. It should not be null or empty.
     * @param callback the callback whose {@link AdobeCallback#call(Object)} will be called when the
     *     event is heard. It should not be null.
     */
    public static void registerEventListener(
            @NonNull final String eventType,
            @NonNull final String eventSource,
            @NonNull final AdobeCallback<Event> callback) {
        if (callback == null) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to registerEventListener - callback is null",
                    Log.UNEXPECTED_NULL_VALUE);
            return;
        }

        if (eventType == null || eventSource == null) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to registerEventListener - event type/source is null");
            return;
        }

        EventHub.Companion.getShared().registerListener(eventType, eventSource, callback);
    }

    /**
     * This method will dispatch the provided {@code Event} to dispatch an event for other
     * extensions or the internal SDK to consume.
     *
     * @param event the {@link Event} to be dispatched. It should not be null
     */
    public static void dispatchEvent(@NonNull final Event event) {
        if (event == null) {
            Log.error(CoreConstants.LOG_TAG, LOG_TAG, "Failed to dispatchEvent - event is null");
            return;
        }

        EventHub.Companion.getShared().dispatch(event);
    }

    /**
     * This method will be used when the provided {@code Event} is used as a trigger and a response
     * event is expected in return.
     *
     * <p>Passes an {@link AdobeError#UNEXPECTED_ERROR} to {@link
     * AdobeCallbackWithError#fail(AdobeError)} if {@code event} is null. Passes an {@link
     * AdobeError#CALLBACK_TIMEOUT} to {@link AdobeCallbackWithError#fail(AdobeError)} if {@code
     * event} processing timeout occurs.
     *
     * @param event the {@link Event} to be dispatched, used as a trigger. It should not be null.
     * @param timeoutMS the timeout specified in milliseconds. Use Long.MAX_DURATION to wait
     *     indefinitely without triggering a timeout.
     * @param responseCallback the callback whose {@link AdobeCallbackWithError#call(Object)} will
     *     be called when the response event is heard. It should not be null.
     */
    public static void dispatchEventWithResponseCallback(
            @NonNull final Event event,
            final long timeoutMS,
            @NonNull final AdobeCallbackWithError<Event> responseCallback) {
        if (responseCallback == null) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to dispatchEventWithResponseCallback - callback is null");
            return;
        }

        if (event == null) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to dispatchEventWithResponseCallback - event is null");
            responseCallback.fail(AdobeError.UNEXPECTED_ERROR);
            return;
        }

        EventHub.Companion.getShared().registerResponseListener(event, timeoutMS, responseCallback);
        EventHub.Companion.getShared().dispatch(event);
    }

    /**
     * Sets the resource Id for small icon.
     *
     * @param resourceID the resource Id of the icon
     */
    public static void setSmallIconResourceID(final int resourceID) {
        AppResourceStore.INSTANCE.setSmallIconResourceID(resourceID);
    }

    /**
     * Returns the resource Id for small icon if it was set by `setSmallIconResourceID`.
     *
     * @return a `int` value if it has been set, otherwise -1
     */
    public static int getSmallIconResourceID() {
        return AppResourceStore.INSTANCE.getSmallIconResourceID();
    }

    /**
     * Sets the resource Id for large icon.
     *
     * @param resourceID the resource Id of the icon
     */
    public static void setLargeIconResourceID(final int resourceID) {
        AppResourceStore.INSTANCE.setLargeIconResourceID(resourceID);
    }

    /**
     * Returns the resource Id for large icon if it was set by `setLargeIconResourceID`.
     *
     * @return a `int` value if it has been set, otherwise -1
     */
    public static int getLargeIconResourceID() {
        return AppResourceStore.INSTANCE.getLargeIconResourceID();
    }

    // ========================================================
    // Identifiers
    // ========================================================

    /**
     * This method dispatches an event to notify the SDK of a new {@code advertisingIdentifier}
     *
     * @param advertisingIdentifier {@code String} representing Android advertising identifier
     */
    public static void setAdvertisingIdentifier(@Nullable final String advertisingIdentifier) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(
                CoreConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER, advertisingIdentifier);

        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.SET_ADVERTISING_IDENTIFIER,
                                EventType.GENERIC_IDENTITY,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        dispatchEvent(event);
    }

    /**
     * This method dispatches an event to notify the SDK of a new {@code pushIdentifier}
     *
     * @param pushIdentifier {@code String} representing the new push identifier
     */
    public static void setPushIdentifier(@Nullable final String pushIdentifier) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(CoreConstants.EventDataKeys.Identity.PUSH_IDENTIFIER, pushIdentifier);

        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.SET_PUSH_IDENTIFIER,
                                EventType.GENERIC_IDENTITY,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        dispatchEvent(event);
    }

    /**
     * Collect PII data. Although using this call enables collection of PII data, the SDK does not
     * automatically send the data to any Adobe endpoint.
     *
     * @param data the map containing the PII data to be collected. It should not be null or empty.
     */
    public static void collectPii(@NonNull final Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Could not trigger PII, the data is null or empty.");
            return;
        }

        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(CoreConstants.EventDataKeys.Signal.SIGNAL_CONTEXT_DATA, data);
        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.COLLECT_PII,
                                EventType.GENERIC_PII,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        dispatchEvent(event);
    }

    /**
     * Collects message data from various points in the application.
     *
     * <p>This method can be invoked to support the following use cases:
     *
     * <ol>
     *   <li>Tracking Push Message receive and click.
     *   <li>Tracking Local Notification receive and click.
     * </ol>
     *
     * <p>The message tracking information can be supplied in the {@code messageInfo} Map. For
     * scenarios where the application is launched as a result of notification click, {@link
     * #collectLaunchInfo(Activity)} will be invoked with the target Activity and message data will
     * be extracted from the Intent extras.
     *
     * @param messageInfo {@code Map<String, Object>} containing message tracking information. It
     *     should not be null or empty.
     */
    public static void collectMessageInfo(@NonNull final Map<String, Object> messageInfo) {
        if (messageInfo == null || messageInfo.isEmpty()) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "collectData: Could not dispatch generic data event, data is null or empty.");
            return;
        }

        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.COLLECT_DATA,
                                EventType.GENERIC_DATA,
                                EventSource.OS)
                        .setEventData(messageInfo)
                        .build();
        dispatchEvent(event);
    }

    /**
     * Collects data from the Activity / context to be used later by the SDK.
     *
     * <p>This method marshals the {@code activity} instance and extracts the intent data / extras.
     *
     * <ol>
     *   <li>Tracking Deep Link click-through
     *       <ul>
     *         <li>Update AndroidManifest.xml to support intent-filter in the activity with the
     *             intended action and type of data.
     *         <li>Handle the intent in the activity.
     *       </ul>
     *   <li>Tracking Push Message click-through
     *   <li>Tracking Local Notification click-through
     * </ol>
     *
     * <p>Invoke this method from Activity.onResume() callback in your activity.
     *
     * @param activity current {@link Activity} reference.
     */
    @VisibleForTesting
    static void collectLaunchInfo(final Activity activity) {
        final Map<String, Object> marshalledData = DataMarshaller.marshal(activity);
        if (marshalledData == null || marshalledData.isEmpty()) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "collectData: Could not dispatch generic data event, data is null or empty.");
            return;
        }

        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.COLLECT_DATA,
                                EventType.GENERIC_DATA,
                                EventSource.OS)
                        .setEventData(marshalledData)
                        .build();
        dispatchEvent(event);
    }

    // ========================================================
    // Configuration methods
    // ========================================================

    /**
     * Configure the SDK by downloading the remote configuration file hosted on Adobe servers
     * specified by the given application ID.
     *
     * <p>The configuration file is cached once downloaded and used in subsequent calls to this API.
     * If the remote file is updated after the first download, the updated file is downloaded and
     * replaces the cached file.
     *
     * @param appId A unique identifier assigned to the app instance by Adobe Tags. It should not be
     *     null.
     */
    public static void configureWithAppID(@NonNull final String appId) {
        if (appId == null) {
            Log.error(CoreConstants.LOG_TAG, LOG_TAG, "configureWithAppID failed - appId is null.");
            return;
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(
                CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID,
                appId);

        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.CONFIGURE_WITH_APP_ID,
                                EventType.CONFIGURATION,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        dispatchEvent(event);
    }

    /**
     * Load configuration from the file in the assets folder. SDK automatically reads config from
     * `ADBMobileConfig.json` file if it exists in the assets folder. Use this API only if the
     * config needs to be read from a different file.
     *
     * <p>On application relaunch, the configuration from the file at {@code filepath} is not
     * preserved and this method must be called again if desired.
     *
     * <p>On failure to read the file or parse the JSON contents, the existing configuration remains
     * unchanged.
     *
     * <p>Calls to this API will replace any existing SDK configuration except those set using
     * {@link #updateConfiguration(Map)} or {@link #setPrivacyStatus(MobilePrivacyStatus)}.
     * Configuration updates made using {@link #updateConfiguration(Map)} and {@link
     * #setPrivacyStatus(MobilePrivacyStatus)} are always applied on top of configuration changes
     * made using this API.
     *
     * @param fileName the name of the configure file in the assets folder. It should not be null.
     */
    public static void configureWithFileInAssets(@NonNull final String fileName) {
        if (fileName == null) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "configureWithFileInAssets failed - fileName is null.");
            return;
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(
                CoreConstants.EventDataKeys.Configuration
                        .CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE,
                fileName);

        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.CONFIGURE_WITH_FILE_PATH,
                                EventType.CONFIGURATION,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        dispatchEvent(event);
    }

    /**
     * Load configuration from local file.
     *
     * <p>Configure the SDK by reading a local file containing the JSON configuration. On
     * application relaunch, the configuration from the file at {@code filepath} is not preserved
     * and this method must be called again if desired.
     *
     * <p>On failure to read the file or parse the JSON contents, the existing configuration remains
     * unchanged.
     *
     * <p>Calls to this API will replace any existing SDK configuration except those set using
     * {@link #updateConfiguration(Map)} or {@link #setPrivacyStatus(MobilePrivacyStatus)}.
     * Configuration updates made using {@link #updateConfiguration(Map)} and {@link
     * #setPrivacyStatus(MobilePrivacyStatus)} are always applied on top of configuration changes
     * made using this API.
     *
     * @param filePath absolute path to a local configuration file. It should not be null.
     */
    public static void configureWithFileInPath(@NonNull final String filePath) {
        if (filePath == null) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "configureWithFileInPath failed - filePath is null.");
            return;
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(
                CoreConstants.EventDataKeys.Configuration
                        .CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH,
                filePath);

        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.CONFIGURE_WITH_FILE_PATH,
                                EventType.CONFIGURATION,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        dispatchEvent(event);
    }

    /**
     * Update specific configuration parameters.
     *
     * <p>Update the current SDK configuration with specific key/value pairs. Keys not found in the
     * current configuration are added. Configuration updates are preserved and applied over
     * existing or new configurations set by calling {@link #configureWithAppID(String)} or {@link
     * #configureWithFileInPath(String)}, even across application restarts.
     *
     * <p>Using {@code null} values is allowed and effectively removes the configuration parameter
     * from the current configuration.
     *
     * @param configMap configuration key/value pairs to be updated or added. It should not be null.
     */
    public static void updateConfiguration(@NonNull final Map<String, Object> configMap) {
        if (configMap == null) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "updateConfiguration failed - configMap is null.");
            return;
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(
                CoreConstants.EventDataKeys.Configuration
                        .CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG,
                configMap);

        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.CONFIGURATION_UPDATE,
                                EventType.CONFIGURATION,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        dispatchEvent(event);
    }

    /**
     * Clear the changes made by {@link #updateConfiguration(Map)} and {@link
     * #setPrivacyStatus(MobilePrivacyStatus)} to the initial configuration provided either by
     * {@link #configureWithAppID(String)} or {@link #configureWithFileInPath(String)} or {@link
     * #configureWithFileInAssets(String)}
     */
    public static void clearUpdatedConfiguration() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(
                CoreConstants.EventDataKeys.Configuration
                        .CONFIGURATION_REQUEST_CONTENT_CLEAR_UPDATED_CONFIG,
                true);

        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.CLEAR_UPDATED_CONFIGURATION,
                                EventType.CONFIGURATION,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        dispatchEvent(event);
    }

    /**
     * Set the Adobe Mobile Privacy status.
     *
     * <p>Sets the {@link MobilePrivacyStatus} for this SDK. The set privacy status is preserved and
     * applied over any new configuration changes from calls to {@link #configureWithAppID(String)}
     * or {@link #configureWithFileInPath(String)}, even across application restarts.
     *
     * @param privacyStatus {@link MobilePrivacyStatus} to be set to the SDK
     * @see MobilePrivacyStatus
     */
    public static void setPrivacyStatus(@NonNull final MobilePrivacyStatus privacyStatus) {
        if (privacyStatus == null) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "setPrivacyStatus failed - privacyStatus is null.");
            return;
        }

        Map<String, Object> privacyStatusUpdateConfig = new HashMap<>();
        privacyStatusUpdateConfig.put(
                CoreConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
                privacyStatus.getValue());
        updateConfiguration(privacyStatusUpdateConfig);
    }

    /**
     * Get the current Adobe Mobile Privacy Status.
     *
     * <p>Gets the currently configured {@link MobilePrivacyStatus} and passes it as a parameter to
     * the given {@link AdobeCallback#call(Object)} function.
     *
     * @param callback {@link AdobeCallback} instance which is invoked with the configured privacy
     *     status as a parameter. It should not be null.
     * @see AdobeCallback
     * @see MobilePrivacyStatus
     */
    public static void getPrivacyStatus(final AdobeCallback<MobilePrivacyStatus> callback) {
        if (callback == null) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to retrieve the privacy status - callback is null");
            return;
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(
                CoreConstants.EventDataKeys.Configuration
                        .CONFIGURATION_REQUEST_CONTENT_RETRIEVE_CONFIG,
                true);
        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.PRIVACY_STATUS_REQUEST,
                                EventType.CONFIGURATION,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();

        AdobeCallbackWithError<Event> callbackWithError =
                new AdobeCallbackWithError<Event>() {
                    @Override
                    public void fail(final AdobeError error) {
                        if (callback instanceof AdobeCallbackWithError) {
                            ((AdobeCallbackWithError<?>) callback)
                                    .fail(AdobeError.CALLBACK_TIMEOUT);
                        } else {
                            // Todo - Check if this is a valid return value.
                            callback.call(null);
                        }
                    }

                    @Override
                    public void call(final Event event) {
                        String status =
                                DataReader.optString(
                                        event.getEventData(),
                                        CoreConstants.EventDataKeys.Configuration
                                                .GLOBAL_CONFIG_PRIVACY,
                                        null);
                        callback.call(MobilePrivacyStatus.fromString(status));
                    }
                };

        dispatchEventWithResponseCallback(event, API_TIMEOUT_MS, callbackWithError);
    }

    /**
     * Retrieve all identities stored by/known to the SDK in a JSON {@code String} format.
     *
     * @param callback {@link AdobeCallback} instance which is invoked with all the known identifier
     *     in JSON {@link String} format. It should not be null.
     * @see AdobeCallback
     */
    public static void getSdkIdentities(@NonNull final AdobeCallback<String> callback) {
        if (callback == null) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to get SDK identities - callback is null");
            return;
        }

        AdobeCallbackWithError<Event> callbackWithError =
                new AdobeCallbackWithError<Event>() {
                    @Override
                    public void fail(final AdobeError error) {
                        if (callback instanceof AdobeCallbackWithError) {
                            ((AdobeCallbackWithError<?>) callback)
                                    .fail(AdobeError.CALLBACK_TIMEOUT);
                        } else {
                            callback.call("{}");
                        }
                    }

                    @Override
                    public void call(final Event event) {
                        String value =
                                DataReader.optString(
                                        event.getEventData(),
                                        CoreConstants.EventDataKeys.Configuration
                                                .CONFIGURATION_RESPONSE_IDENTITY_ALL_IDENTIFIERS,
                                        "{}");
                        callback.call(value);
                    }
                };

        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.GET_SDK_IDENTITIES,
                                EventType.CONFIGURATION,
                                EventSource.REQUEST_IDENTITY)
                        .build();
        dispatchEventWithResponseCallback(event, API_TIMEOUT_MS, callbackWithError);
    }

    /**
     * Clears all identifiers from Edge extensions and generates a new Experience Cloud ID (ECID).
     */
    public static void resetIdentities() {
        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.RESET_IDENTITIES_REQUEST,
                                EventType.GENERIC_IDENTITY,
                                EventSource.REQUEST_RESET)
                        .build();
        dispatchEvent(event);
    }

    // ========================================================
    // Lifecycle methods
    // ========================================================

    /**
     * Start/resume lifecycle session.
     *
     * <p>Start a new lifecycle session or resume a previously paused lifecycle session. If a
     * previously paused session timed out, then a new session is created. If a current session is
     * running, then calling this method does nothing.
     *
     * <p>Additional context data may be passed when calling this method. Lifecycle data and any
     * additional data are sent as context data parameters to Analytics, to Target as mbox
     * parameters, and for Audience Manager they are sent as customer variables. Any additional data
     * is also used by the Rules Engine when processing rules.
     *
     * <p>This method should be called from the Activity onResume method.
     *
     * @param additionalContextData optional additional context for this session.
     */
    public static void lifecycleStart(@Nullable final Map<String, String> additionalContextData) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(
                CoreConstants.EventDataKeys.Lifecycle.LIFECYCLE_ACTION_KEY,
                CoreConstants.EventDataKeys.Lifecycle.LIFECYCLE_START);
        eventData.put(
                CoreConstants.EventDataKeys.Lifecycle.ADDITIONAL_CONTEXT_DATA,
                additionalContextData);

        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.LIFECYCLE_RESUME,
                                EventType.GENERIC_LIFECYCLE,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        dispatchEvent(event);
    }

    /**
     * Pause/stop lifecycle session.
     *
     * <p>Pauses the current lifecycle session. Calling pause on an already paused session updates
     * the paused timestamp, having the effect of resetting the session timeout timer. If no
     * lifecycle session is running, then calling this method does nothing.
     *
     * <p>A paused session is resumed if {@link #lifecycleStart(Map)} is called before the session
     * timeout. After the session timeout, a paused session is closed and calling {@link
     * #lifecycleStart(Map)} will create a new session. The session timeout is defined by the {@code
     * lifecycle.sessionTimeout} configuration parameter. If not defined, the default session
     * timeout is five minutes.
     *
     * <p>This method should be called from the Activity onPause method.
     */
    public static void lifecyclePause() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(
                CoreConstants.EventDataKeys.Lifecycle.LIFECYCLE_ACTION_KEY,
                CoreConstants.EventDataKeys.Lifecycle.LIFECYCLE_PAUSE);

        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.LIFECYCLE_PAUSE,
                                EventType.GENERIC_LIFECYCLE,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        dispatchEvent(event);
    }

    // ========================================================
    // Track methods
    // ========================================================

    /**
     * This method dispatches an Analytics track {@code action} event
     *
     * <p>Actions represent events that occur in your application that you want to measure; the
     * corresponding metrics will be incremented each time the event occurs. For example, you may
     * want to track when an user click on the login button or a certain article was viewed.
     *
     * <p>
     *
     * @param action {@code String} containing the name of the action to track
     * @param contextData {@code Map<String, String>} containing context data to attach on this hit
     */
    public static void trackAction(
            @NonNull final String action, @Nullable final Map<String, String> contextData) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(
                CoreConstants.EventDataKeys.Analytics.TRACK_ACTION, action == null ? "" : action);
        eventData.put(
                CoreConstants.EventDataKeys.Analytics.CONTEXT_DATA,
                contextData == null ? new HashMap<String, String>() : contextData);
        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.ANALYTICS_TRACK,
                                EventType.GENERIC_TRACK,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        dispatchEvent(event);
    }

    /**
     * This method dispatches an Analytics track {@code state} event
     *
     * <p>States represent different screens or views of your application. When the user navigates
     * between application pages, a new track call should be sent with current state name. Tracking
     * state name is typically called from an Activity in the onResume method.
     *
     * <p>
     *
     * @param state {@code String} containing the name of the state to track. It should not be null.
     * @param contextData optional contextData {@code Map<String, String>} containing context data
     *     to attach on this hit
     */
    public static void trackState(
            @NonNull final String state, @Nullable final Map<String, String> contextData) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(
                CoreConstants.EventDataKeys.Analytics.TRACK_STATE, state == null ? "" : state);
        eventData.put(
                CoreConstants.EventDataKeys.Analytics.CONTEXT_DATA,
                contextData == null ? new HashMap<String, String>() : contextData);
        Event event =
                new Event.Builder(
                                CoreConstants.EventNames.ANALYTICS_TRACK,
                                EventType.GENERIC_TRACK,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        dispatchEvent(event);
    }

    @VisibleForTesting
    static void resetSDK() {
        EventHub.Companion.getShared().shutdown();
        EventHub.Companion.setShared(new EventHub());
        MobileCoreInitializer.INSTANCE.reset();
    }
}
