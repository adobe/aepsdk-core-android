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
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.os.UserManagerCompat;

import com.adobe.marketing.mobile.internal.AppResourceStore;
import com.adobe.marketing.mobile.internal.CoreConstants;
import com.adobe.marketing.mobile.internal.DataMarshaller;
import com.adobe.marketing.mobile.internal.configuration.ConfigurationExtension;
import com.adobe.marketing.mobile.internal.eventhub.EventHub;
import com.adobe.marketing.mobile.internal.eventhub.EventHubConstants;
import com.adobe.marketing.mobile.internal.eventhub.Tenant;
import com.adobe.marketing.mobile.internal.migration.V4Migrator;
import com.adobe.marketing.mobile.services.Log;
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

    public static void setApplication(@NonNull final Application application) {

        if (application == null) {
            Log.error(
                    CoreConstants.LOG_TAG, LOG_TAG, "setApplication failed - application is null");
            return;
        }

        // Direct boot mode is supported on Android N and above
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            if (UserManagerCompat.isUserUnlocked(application)) {
                Log.debug(
                        CoreConstants.LOG_TAG,
                        LOG_TAG,
                        "setApplication - device is unlocked and not in direct boot mode,"
                                + " initializing the SDK.");
            } else {
                Log.error(
                        CoreConstants.LOG_TAG,
                        LOG_TAG,
                        "setApplication failed - device is in direct boot mode, SDK will not be"
                                + " initialized.");
                return;
            }
        }

        if (sdkInitializedWithContext.getAndSet(true)) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "setApplication failed - ignoring as setApplication was already called.");
            return;
        }

        if (VERSION.SDK_INT == VERSION_CODES.O || VERSION.SDK_INT == VERSION_CODES.O_MR1) {
            // AMSDK-8502
            // Workaround to prevent a crash happening on Android 8.0/8.1 related to
            // TimeZoneNamesImpl
            // https://issuetracker.google.com/issues/110848122
            try {
                new Date().toString();
            } catch (AssertionError e) {
                // Workaround for a bug in Android that can cause crashes on Android 8.0 and 8.1
            } catch (Exception e) {
                // Workaround for a bug in Android that can cause crashes on Android 8.0 and 8.1
            }
        }

        ServiceProvider.getInstance().getAppContextService().setApplication(application);
        App.INSTANCE.registerActivityResumedListener(MobileCore::collectLaunchInfo);

        // Migration and EventHistory operations must complete in a background thread before any
        // extensions are registered.
        // To ensure these tasks are completed before any registerExtension calls are made,
        // reuse the eventHubExecutor instead of using a separate executor instance.
        EventHub.Companion.getShared()
                .executeInEventHubExecutor(
                        () -> {
                            try {
                                V4Migrator migrator = new V4Migrator();
                                migrator.migrate();
                            } catch (Exception e) {
                                Log.error(
                                        CoreConstants.LOG_TAG,
                                        LOG_TAG,
                                        "Migration from V4 SDK failed with error - "
                                                + e.getLocalizedMessage());
                            }

                            // Initialize event history
                            EventHub.Companion.getShared().initializeEventHistory(Tenant.Default.INSTANCE);
                            return null;
                        });
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
    public static void setApplication(@NonNull final Application application, Tenant tenant) {

        if (application == null) {
            Log.error(
                    CoreConstants.LOG_TAG, LOG_TAG, "setApplication failed - application is null");
            return;
        }

        // Direct boot mode is supported on Android N and above
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            if (UserManagerCompat.isUserUnlocked(application)) {
                Log.debug(
                        CoreConstants.LOG_TAG,
                        LOG_TAG,
                        "setApplication - device is unlocked and not in direct boot mode,"
                                + " initializing the SDK.");
            } else {
                Log.error(
                        CoreConstants.LOG_TAG,
                        LOG_TAG,
                        "setApplication failed - device is in direct boot mode, SDK will not be"
                                + " initialized.");
                return;
            }
        }

        if (sdkInitializedWithContext.getAndSet(true)) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "setApplication failed - ignoring as setApplication was already called for tenant " + tenant.getId());
            return;
        }

        if (VERSION.SDK_INT == VERSION_CODES.O || VERSION.SDK_INT == VERSION_CODES.O_MR1) {
            // AMSDK-8502
            // Workaround to prevent a crash happening on Android 8.0/8.1 related to
            // TimeZoneNamesImpl
            // https://issuetracker.google.com/issues/110848122
            try {
                new Date().toString();
            } catch (AssertionError e) {
                // Workaround for a bug in Android that can cause crashes on Android 8.0 and 8.1
            } catch (Exception e) {
                // Workaround for a bug in Android that can cause crashes on Android 8.0 and 8.1
            }
        }

        ServiceProvider.getInstance().getAppContextService().setApplication(application);
        App.INSTANCE.registerActivityResumedListener(MobileCore::collectLaunchInfo);

        // Migration and EventHistory operations must complete in a background thread before any
        // extensions are registered.
        // To ensure these tasks are completed before any registerExtension calls are made,
        // reuse the eventHubExecutor instead of using a separate executor instance.

        EventHub.instance(tenant).executeInEventHubExecutor(
                () -> {
                    try {
                        V4Migrator migrator = new V4Migrator();
                        migrator.migrate();
                    } catch (Exception e) {
                        Log.error(
                                CoreConstants.LOG_TAG,
                                LOG_TAG,
                                "Migration from V4 SDK failed with error - "
                                        + e.getLocalizedMessage());
                    }

                    // Initialize event history
                    EventHub.instance(tenant).initializeEventHistory(tenant);
                    return null;
                });
    }

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

        final List<Class<? extends Extension>> extensionsToRegister = new ArrayList<>();
        extensionsToRegister.add(ConfigurationExtension.class);
        if (extensions != null) {
            for (final Class<? extends Extension> extension : extensions) {
                if (extension != null) {
                    extensionsToRegister.add(extension);
                }
            }
        }

        final AtomicInteger registeredExtensions = new AtomicInteger(0);
        for (final Class<? extends Extension> extension : extensionsToRegister) {
            EventHub.Companion.getShared()
                    .registerExtension(
                            extension,
                            eventHubError -> {
                                if (registeredExtensions.incrementAndGet()
                                        == extensionsToRegister.size()) {
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



    public static void registerExtensions(
            @NonNull final List<Class<? extends Extension>> extensions,
            Tenant tenant,
            @Nullable final AdobeCallback<?> completionCallback
    ) {
        if (!sdkInitializedWithContext.get()) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to registerExtensions - setApplication not called");
            return;
        }

        final List<Class<? extends Extension>> extensionsToRegister = new ArrayList<>();
        extensionsToRegister.add(ConfigurationExtension.class);
        if (extensions != null) {
            for (final Class<? extends Extension> extension : extensions) {
                if (extension != null) {
                    extensionsToRegister.add(extension);
                }
            }
        }

        final AtomicInteger registeredExtensions = new AtomicInteger(0);
        for (final Class<? extends Extension> extension : extensionsToRegister) {
            EventHub.instance(tenant)
                    .registerExtension(
                            extension,
                            eventHubError -> {
                                if (registeredExtensions.incrementAndGet()
                                        == extensionsToRegister.size()) {
                                    EventHub.instance(tenant).start();
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



    public static void registerEventListener(
            @NonNull final String eventType,
            @NonNull final String eventSource,
            @NonNull final AdobeCallback<Event> callback,
            Tenant tenant) {
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

        EventHub.instance(tenant).registerListener(eventType, eventSource, callback);
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

    public static void dispatchEvent(@NonNull final Event event, Tenant tenant) {
        if (event == null) {
            Log.error(CoreConstants.LOG_TAG, LOG_TAG, "Failed to dispatchEvent - event is null");
            return;
        }

        android.util.Log.d(LOG_TAG, "dispatchEvent: event = " + event.getEventData() + " tenant =" + tenant.getId());

        EventHub.instance(tenant).dispatch(event);
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


    public static void dispatchEventWithResponseCallback(
            @NonNull final Event event,
            final long timeoutMS,
            @NonNull final AdobeCallbackWithError<Event> responseCallback,
            Tenant tenant) {
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

        EventHub.instance(tenant).registerResponseListener(event, timeoutMS, responseCallback);
        EventHub.instance(tenant).dispatch(event);
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


    public static void setAdvertisingIdentifier(@Nullable final String advertisingIdentifier, Tenant tenant) {
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
        dispatchEvent(event, tenant);
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


    public static void setPushIdentifier(@Nullable final String pushIdentifier, Tenant tenant) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(CoreConstants.EventDataKeys.Identity.PUSH_IDENTIFIER, pushIdentifier);

        Event event =
                new Event.Builder(
                        CoreConstants.EventNames.SET_PUSH_IDENTIFIER,
                        EventType.GENERIC_IDENTITY,
                        EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        dispatchEvent(event, tenant);
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


    public static void collectPii(@NonNull final Map<String, String> data, Tenant tenant) {
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
        dispatchEvent(event, tenant);
    }


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


    public static void collectMessageInfo(@NonNull final Map<String, Object> messageInfo, Tenant tenant) {
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
        dispatchEvent(event, tenant);
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


    @VisibleForTesting
    static void collectLaunchInfo(final Activity activity, Tenant tenant) {
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
        dispatchEvent(event, tenant);
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
                        CoreConstants.EventNames.CONFIGURE_WITH_APP_ID,
                        EventType.CONFIGURATION,
                        EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        dispatchEvent(event);
    }


    public static void configureWithAppID(@NonNull final String appId, Tenant tenant) {
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
        dispatchEvent(event, tenant);
    }

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


    public static void configureWithFileInAssets(@NonNull final String fileName, Tenant tenant) {
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
        dispatchEvent(event, tenant);
    }


    public static void configureWithFileInPath(@NonNull final String filePath, Tenant tenant) {
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
        dispatchEvent(event, tenant);
    }


    public static void updateConfiguration(@NonNull final Map<String, Object> configMap, Tenant tenant) {
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
        dispatchEvent(event, tenant);
    }

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


    public static void clearUpdatedConfiguration(Tenant tenant) {
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
        dispatchEvent(event, tenant);
    }

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

    public static void setPrivacyStatus(@NonNull final MobilePrivacyStatus privacyStatus, Tenant tenant) {
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
        updateConfiguration(privacyStatusUpdateConfig, tenant);
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



    public static void getPrivacyStatus(final AdobeCallback<MobilePrivacyStatus> callback, Tenant tenant) {
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

        dispatchEventWithResponseCallback(event, API_TIMEOUT_MS, callbackWithError, tenant);
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



    public static void getSdkIdentities(@NonNull final AdobeCallback<String> callback, Tenant tenant) {
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
        dispatchEventWithResponseCallback(event, API_TIMEOUT_MS, callbackWithError, tenant);
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


    public static void resetIdentities(Tenant tenant) {
        Event event =
                new Event.Builder(
                        CoreConstants.EventNames.RESET_IDENTITIES_REQUEST,
                        EventType.GENERIC_IDENTITY,
                        EventSource.REQUEST_RESET)
                        .build();
        dispatchEvent(event, tenant);
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

    public static void lifecycleStart(@Nullable final Map<String, String> additionalContextData, Tenant tenant) {
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
        dispatchEvent(event, tenant);
    }

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

    public static void lifecyclePause(Tenant tenant) {
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
        dispatchEvent(event, tenant);
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


    public static void trackAction(
            @NonNull final String action, @Nullable final Map<String, String> contextData, Tenant tenant) {
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
        dispatchEvent(event, tenant);
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
            @NonNull final String state, @Nullable final Map<String, String> contextData, Tenant tenant) {
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
        dispatchEvent(event, tenant);
    }

    @VisibleForTesting
    static void resetSDK(Tenant tenant) {
        EventHub.instance(tenant).shutdown();
        EventHub.create(tenant);
        sdkInitializedWithContext.set(false);
    }

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
        sdkInitializedWithContext.set(false);
    }
}
