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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.adobe.marketing.mobile.internal.AppResourceStore;
import com.adobe.marketing.mobile.internal.CoreConstants;
import com.adobe.marketing.mobile.internal.configuration.ConfigurationExtension;
import com.adobe.marketing.mobile.internal.eventhub.EventHub;
import com.adobe.marketing.mobile.internal.eventhub.EventHubConstants;
import com.adobe.marketing.mobile.internal.eventhub.EventHubError;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.MessagingDelegate;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.internal.context.App;
import com.adobe.marketing.mobile.util.DataReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class MobileCore {

    private static final String LOG_TAG = "MobileCore";
    private static final long API_TIMEOUT_MS = 5000;

    static AtomicBoolean sdkInitializedWithContext = new AtomicBoolean(false);

    private MobileCore() {}

    // ========================================================
    // MobileCore methods
    // ========================================================

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
     * register a {@link Application.ActivityLifecycleCallbacks} to monitor the lifecycle of the app
     * and get the {@link android.app.Activity} on top of the screen.
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

        if (sdkInitializedWithContext.getAndSet(true)) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Ignoring as setApplication was already called.");
            return;
        }

        // AMSDK-8502
        // Workaround to prevent a crash happening on Android 8.0/8.1 related to TimeZoneNamesImpl
        // https://issuetracker.google.com/issues/110848122
        try {
            new Date().toString();
        } catch (AssertionError e) {
            // Workaround for a bug in Android that can cause crashes on Android 8.0 and 8.1
        } catch (Exception e) {
            // Workaround for a bug in Android that can cause crashes on Android 8.0 and 8.1
        }

        ServiceProvider.getInstance().getAppContextService().setApplication(application);
        App.INSTANCE.registerActivityResumedListener(MobileCore::collectLaunchInfo);

        try {
            V4ToV5Migration migrationTool = new V4ToV5Migration();
            migrationTool.migrate();
        } catch (Exception e) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "V4 to V5 migration failed - " + e.getLocalizedMessage());
        }

        // Register configuration extension
        EventHub.Companion.getShared().registerExtension(ConfigurationExtension.class);
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
        if (!sdkInitializedWithContext.get()) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to registerExtensions - setApplication not called");
            return;
        }

        final List<Class<? extends Extension>> allExtensions = new ArrayList<>();
        if (extensions != null) {
            for (final Class<? extends Extension> extension : extensions) {
                if (extension != null) {
                    allExtensions.add(extension);
                }
            }
        }

        final AtomicInteger registeredExtensions = new AtomicInteger(0);
        for (final Class<? extends Extension> extension : allExtensions) {
            EventHub.Companion.getShared()
                    .registerExtension(
                            extension,
                            eventHubError -> {
                                if (registeredExtensions.incrementAndGet()
                                        == allExtensions.size()) {
                                    EventHub.Companion.getShared().start();
                                    try {
                                        if (completionCallback != null) {
                                            completionCallback.call(null);
                                        }
                                    } catch (Exception ex) {
                                    }
                                }
                                return null;
                            });
        }
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
     * @param timeoutMS the timeout specified in milliseconds.
     * @param responseCallback the callback whose {@link AdobeCallbackWithError#call(Object)} will
     *     be called when the response event is heard. It should not be null.
     * @see MobileCore#dispatchResponseEvent(Event, Event, ExtensionErrorCallback)
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
     * Sets the resource Id for small icon.
     *
     * @param resourceID the resource Id of the icon
     */
    public static void setLargeIconResourceID(final int resourceID) {
        AppResourceStore.INSTANCE.setLargeIconResourceID(resourceID);
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
                                "SetAdvertisingIdentifier",
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
                                "SetPushIdentifier",
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
                new Event.Builder("CollectPII", EventType.GENERIC_PII, EventSource.REQUEST_CONTENT)
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
                new Event.Builder("CollectData", EventType.GENERIC_DATA, EventSource.OS)
                        .setEventData(messageInfo)
                        .build();
        dispatchEvent(event);
    }

    /**
     * Collects data from the Activity / context to be used later by the SDK.
     *
     * <p>This method marshals the {@code activity} instance and extracts the intent data / extras.
     * It should be called to support the following use cases:
     *
     * <ol>
     *   <li>Tracking Deep Link click-through
     *       <ul>
     *         <li>Update AndroidManifest.xml to support intent-filter in the activity with the
     *             intended action and type of data.
     *         <li>Handle the intent in the activity.
     *         <li>Pass activity with deepLink intent to SDK in {@code collectLaunchInfo}.
     *       </ul>
     *   <li>Tracking Push Message click-through
     *       <ul>
     *         <li>Push message data must be added to the Intent used to open target activity on
     *             click-through.
     *         <li>The data can be added in intent extras which is then collected by SDK when target
     *             activity is passed in {@code collectedLaunchInfo}.
     *       </ul>
     *   <li>Tracking Local Notification click-through
     *       <ul>
     *         <li>Add manifest-declared broadcast receiver {@code <receiver
     *             android:name=".LocalNotificationHandler" />} in your app.
     *         <li>Pass notifications activity reference in {@code collectLaunchInfo}.
     *       </ul>
     * </ol>
     *
     * <p>Invoke this method from Activity.onResume() callback in your activity.
     *
     * @param activity current {@link Activity} reference.
     */
    @VisibleForTesting
    static void collectLaunchInfo(final Activity activity) {
        DataMarshaller marshaller = new DataMarshaller();
        marshaller.marshal(activity);
        final Map<String, Object> marshalledData = marshaller.getData();
        if (marshalledData == null || marshalledData.isEmpty()) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "collectData: Could not dispatch generic data event, data is null or empty.");
            return;
        }

        Event event =
                new Event.Builder("CollectData", EventType.GENERIC_DATA, EventSource.OS)
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
     * @param appId A unique identifier assigned to the app instance by Adobe Launch. It should not
     *     be null.
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
                                "Configure with AppID",
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
                                "Configure with FilePath",
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
                                "Configure with FilePath",
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
                                "Configuration Update",
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
                                "Configuration Update",
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
                                "PrivacyStatusRequest",
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
                                "getSdkIdentities",
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
                                "Reset Identities Request",
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
                                "LifecycleResume",
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
                                "LifecyclePause",
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
                                "Analytics Track",
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
                                "Analytics Track",
                                EventType.GENERIC_TRACK,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        dispatchEvent(event);
    }

    // ========================================================
    // Messaging Delegate methods
    // ========================================================

    /**
     * Gets a previously set Message delegate.
     *
     * @return {@link MessagingDelegate} used to listen for current message lifecycle events
     */
    @Nullable public static MessagingDelegate getMessagingDelegate() {
        return ServiceProvider.getInstance().getMessageDelegate();
    }

    /**
     * Sets a Message delegate used to listen for current message lifecycle events.
     *
     * @param messagingDelegate {@link MessagingDelegate} to use for listening to current message
     *     lifecycle events
     */
    public static void setMessagingDelegate(@Nullable final MessagingDelegate messagingDelegate) {
        ServiceProvider.getInstance().setMessageDelegate(messagingDelegate);
    }

    // ========================================================
    // Deprecated methods
    // ========================================================

    /**
     * Sends a log message of the given {@code LoggingMode}. If the specified {@code mode} is more
     * verbose than the current {@link LoggingMode} set from {@link #setLogLevel(LoggingMode)} then
     * the message is not printed.
     *
     * @param mode the {@link LoggingMode} used to print the message. It should not be null.
     * @param tag used to identify the source of the log message
     * @param message the message to log
     * @deprecated Use logging methods exposed in {@link com.adobe.marketing.mobile.services.Log}
     *     class.
     */
    @Deprecated
    public static void log(
            @NonNull final LoggingMode mode, final String tag, final String message) {
        if (mode == null) {
            return;
        }

        switch (mode) {
            case ERROR:
                com.adobe.marketing.mobile.services.Log.error("", tag, message);
                break;
            case WARNING:
                com.adobe.marketing.mobile.services.Log.warning("", tag, message);
                break;
            case DEBUG:
                com.adobe.marketing.mobile.services.Log.debug("", tag, message);
                break;
            case VERBOSE:
                com.adobe.marketing.mobile.services.Log.trace("", tag, message);
                break;
        }
    }

    /**
     * Registers an extension class which has {@code Extension} as parent.
     *
     * <p>In order to ensure that your extension receives all the internal events, this method needs
     * to be called after {@link MobileCore#setApplication(Application)} is called, but before any
     * other method in this class.
     *
     * @param extensionClass a class whose parent is {@link Extension}. It should not be null.
     * @param errorCallback an optional {@link ExtensionErrorCallback} for the eventuality of an
     *     error, called when this method returns false
     * @return {@code boolean} indicating if the provided parameters are valid and no error occurs
     * @deprecated Use {@link MobileCore#registerExtensions(List, AdobeCallback)} instead.
     */
    @Deprecated
    public static boolean registerExtension(
            @NonNull final Class<? extends Extension> extensionClass,
            @Nullable final ExtensionErrorCallback<ExtensionError> errorCallback) {
        if (!sdkInitializedWithContext.get()) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to registerExtension - setApplication not called");
            return false;
        }

        if (extensionClass == null) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to registerExtension - extensionClass is null");
            return false;
        }

        EventHub.Companion.getShared()
                .registerExtension(
                        extensionClass,
                        eventHubError -> {
                            if (eventHubError == EventHubError.None) {
                                return null;
                            }

                            ExtensionError error;
                            if (eventHubError == EventHubError.InvalidExtensionName) {
                                error = ExtensionError.BAD_NAME;
                            } else if (eventHubError == EventHubError.DuplicateExtensionName) {
                                error = ExtensionError.DUPLICATE_NAME;
                            } else {
                                error = ExtensionError.UNEXPECTED_ERROR;
                            }

                            if (errorCallback != null) {
                                errorCallback.error(error);
                            }

                            return null;
                        });

        return true;
    }

    /**
     * Start the Core processing. This should be called after the initial set of extensions have
     * been registered.
     *
     * <p>This call will wait for any outstanding registrations to complete and then start event
     * processing. You can use the callback to kickoff additional operations immediately after any
     * operations kicked off during registration. You shouldn't call this method more than once in
     * your app, if so, sdk will ignore it and print error log.
     *
     * @param completionCallback an optional {@link AdobeCallback} invoked after registrations are
     *     completed
     * @deprecated Use {@link MobileCore#registerExtensions(List, AdobeCallback)} instead.
     */
    @Deprecated
    public static void start(@Nullable final AdobeCallback<?> completionCallback) {
        Log.warning(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "Use 'MobileCore.registerExtensions' method to initialize AEP SDK. Refer install"
                    + " instructions in Tags mobile property corresponding to this application.");

        if (!sdkInitializedWithContext.get()) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to registerExtension - setApplication not called");
            return;
        }

        EventHub.Companion.getShared()
                .start(
                        () -> {
                            if (completionCallback != null) {
                                completionCallback.call(null);
                            }
                            return null;
                        });
    }

    /**
     * This method will be used when the provided {@code Event} is used as a trigger and a response
     * event is expected in return.
     *
     * <p>Passes an {@link AdobeError#UNEXPECTED_ERROR} to {@link
     * AdobeCallbackWithError#fail(AdobeError)} if {@code event} is null. Passes an {@link
     * AdobeError#CALLBACK_TIMEOUT} to {@link AdobeCallbackWithError#fail(AdobeError)} if {@code
     * event} processing timeout occurs after {@link MobileCore#API_TIMEOUT_MS} milliseconds.
     *
     * @param event the {@link Event} to be dispatched, used as a trigger. It should not be null.
     * @param responseCallback the callback whose {@link AdobeCallbackWithError#call(Object)} will
     *     be called when the response event is heard. It should not be null.
     * @deprecated Use {@link MobileCore#dispatchEventWithResponseCallback(Event, long,
     *     AdobeCallbackWithError)} instead by explicitly specifying timeout.
     */
    @Deprecated
    public static void dispatchEventWithResponseCallback(
            @NonNull final Event event,
            @NonNull final AdobeCallbackWithError<Event> responseCallback) {
        dispatchEventWithResponseCallback(event, API_TIMEOUT_MS, responseCallback);
    }

    /**
     * Called by the extension public API to dispatch an event for other extensions or the internal
     * SDK to consume.
     *
     * @param event the {@link Event} instance to be dispatched. It should not be null.
     * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if an error
     *     occurred during dispatching
     * @return {@code boolean} indicating if the the event dispatching operation succeeded
     * @deprecated Extensions should use {@link ExtensionApi#dispatch(Event)} instead
     */
    @Deprecated
    public static boolean dispatchEvent(
            @NonNull final Event event,
            @Nullable final ExtensionErrorCallback<ExtensionError> errorCallback) {
        if (event == null) {
            Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "dispatchEvent failed - event is null");

            if (errorCallback != null) {
                errorCallback.error(ExtensionError.EVENT_NULL);
            }

            return false;
        }

        dispatchEvent(event);
        return true;
    }

    /**
     * This method will be used when the provided {@code Event} is used as a trigger and a response
     * event is expected in return. The returned event needs to be sent using {@link
     * #dispatchResponseEvent(Event, Event, ExtensionErrorCallback)}.
     *
     * <p>Passes an {@link ExtensionError} to {@code errorCallback} if {@code event} or {@code
     * responseCallback} are null.
     *
     * @param event the {@link Event} instance to be dispatched, used as a trigger. It should not be
     *     null.
     * @param responseCallback the {@link AdobeCallback} to be called with the response event
     *     received. It should not be null.
     * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if an error
     *     occurred during dispatching
     * @return {@code boolean} indicating if the the event dispatching operation succeeded
     * @deprecated Extensions should use {@link MobileCore#dispatchEventWithResponseCallback(Event,
     *     long, AdobeCallbackWithError)} instead.
     */
    @Deprecated
    public static boolean dispatchEventWithResponseCallback(
            @NonNull final Event event,
            @NonNull final AdobeCallback<Event> responseCallback,
            @Nullable final ExtensionErrorCallback<ExtensionError> errorCallback) {
        // the core will validate and copy this event
        if (responseCallback == null) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to dispatchEventWithResponseCallback - responseCallback is null");

            if (errorCallback != null) {
                errorCallback.error(ExtensionError.CALLBACK_NULL);
            }
            return false;
        }

        if (event == null) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to dispatchEventWithResponseCallback - event is null");

            if (errorCallback != null) {
                errorCallback.error(ExtensionError.EVENT_NULL);
            }

            return false;
        }

        AdobeCallbackWithError<Event> callbackWithError =
                new AdobeCallbackWithError<Event>() {
                    @Override
                    public void fail(final AdobeError error) {
                        if (responseCallback instanceof AdobeCallbackWithError) {
                            ((AdobeCallbackWithError<?>) responseCallback)
                                    .fail(AdobeError.CALLBACK_TIMEOUT);
                        } else {
                            // Todo - Check if this is a valid return value.
                            responseCallback.call(null);
                        }
                    }

                    @Override
                    public void call(final Event event) {
                        responseCallback.call(event);
                    }
                };
        dispatchEventWithResponseCallback(event, API_TIMEOUT_MS, callbackWithError);
        return true;
    }

    /**
     * Dispatches a response event for a paired event that was sent to {@code
     * dispatchEventWithResponseCallback} and received by an extension listener {@code hear} method.
     *
     * <p>Passes an {@link ExtensionError} to {@code errorCallback} if {@code responseEvent} or
     * {@code requestEvent} are null.
     *
     * <p>Note: The {@code responseEvent} will not be sent to any listeners, it is sent only to the
     * response callback registered using {@link MobileCore#dispatchEventWithResponseCallback(Event,
     * AdobeCallback, ExtensionErrorCallback)}.
     *
     * @param responseEvent the {@link Event} instance dispatched as response to request event. It
     *     should not be null.
     * @param requestEvent the {@link Event} instance which acts as a trigger for response event. It
     *     should not be null.
     * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if an error
     *     occurred during dispatching
     * @return {@code boolean} indicating if the the event dispatching operation succeeded
     * @deprecated Use {@link Event.Builder#inResponseToEvent(Event)} to create a response event.
     */
    @Deprecated
    public static boolean dispatchResponseEvent(
            @NonNull final Event responseEvent,
            @NonNull final Event requestEvent,
            @Nullable final ExtensionErrorCallback<ExtensionError> errorCallback) {
        if (requestEvent == null || responseEvent == null) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to dispatchResponseEvent - requestEvent/responseEvent is null");

            if (errorCallback != null) {
                errorCallback.error(ExtensionError.EVENT_NULL);
            }

            return false;
        }

        responseEvent.setResponseID(requestEvent.getResponseID());
        dispatchEvent(responseEvent);
        return true;
    }

    @VisibleForTesting
    static void resetSDK() {
        EventHub.Companion.getShared().shutdown();
        EventHub.Companion.setShared(new EventHub());
        sdkInitializedWithContext.set(false);
    }
}
